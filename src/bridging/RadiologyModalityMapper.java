/*
 * RadiologyModalityMapper.java
 *
 * Utility class to load and cache the mapping from kd_jenis_prw (radiology
 * procedure code) to DICOM modality type (CT, US, CR, MR, etc.) and AE Titles.
 *
 * The mapping data is read once from ./cache/mapping_tindakan_radiologi.iyem.
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides a cached, singleton lookup from radiology procedure code
 * (kd_jenis_prw) to DICOM modality identifier (CT, US, CR, DX, MR, MG, etc.)
 * and Scheduled Station AE Title.
 *
 * The mapping file is read exactly once per JVM lifetime. Use {@link #reload()}
 * to force a re-read after the file has been edited externally.
 *
 * Expected JSON format inside mapping_tindakan_radiologi.iyem:
 * <pre>
 * {
 *   "default_aet": {
 *     "CR": "CR_STATION",
 *     "US": "USG_STATION"
 *   },
 *   "mapping": [
 *     { "kd_jenis_prw": "RD001", "nm_perawatan": "CT Scan Kepala", "modality": "CT" },
 *     { "kd_jenis_prw": "RD002", "nm_perawatan": "USG Abdomen",    "modality": "US", "aet": "USG_STATION_ALT" }
 *   ]
 * }
 * </pre>
 *
 * @author malifnasruloh
 */
public class RadiologyModalityMapper {

    private static final String MAPPING_FILE = "./cache/mapping_tindakan_radiologi.iyem";

    // Singleton: loaded once, shared across all callers.
    private static volatile RadiologyModalityMapper instance;

    // Unmodifiable after construction / reload to prevent accidental mutation.
    private Map<String, String> modalityMap = new HashMap<>();
    private Map<String, String> procedureAetMap = new HashMap<>();
    private Map<String, String> defaultAetMap = new HashMap<>();
    private boolean loaded = false;

    // Private constructor enforces singleton pattern.
    private RadiologyModalityMapper() {
        loadMapping();
    }

    /**
     * Returns the singleton instance of this mapper. Thread-safe via
     * double-checked locking; the file is loaded at most once.
     *
     * @return the shared RadiologyModalityMapper instance
     */
    public static RadiologyModalityMapper getInstance() {
        if (instance == null) {
            synchronized (RadiologyModalityMapper.class) {
                if (instance == null) {
                    instance = new RadiologyModalityMapper();
                }
            }
        }
        return instance;
    }

    /**
     * Looks up the DICOM modality for a given radiology procedure code.
     *
     * @param kdJenisPrw the radiology procedure code (e.g., "RD001")
     * @return the DICOM modality code (e.g., "CT", "US", "CR"), or {@code null}
     * if no mapping exists or input is blank
     */
    public String getModality(String kdJenisPrw) {
        if (kdJenisPrw == null || kdJenisPrw.trim().isEmpty()) {
            return null;
        }
        return modalityMap.get(kdJenisPrw.trim());
    }

    /**
     * Resolves the AE Title for a given procedure code and modality using
     * a 3-tier lookup strategy:
     * 1. Check for procedure-specific AET
     * 2. Check for modality-specific default AET
     * 3. Fallback to global fallback AET
     *
     * @param kdJenisPrw the procedure code
     * @param modality the modality code
     * @param defaultFallback the global fallback AE Title
     * @return the resolved AE Title
     */
    public String getAeTitle(String kdJenisPrw, String modality, String defaultFallback) {
        if (kdJenisPrw != null) {
            String customAet = procedureAetMap.get(kdJenisPrw.trim());
            if (customAet != null && !customAet.trim().isEmpty()) {
                return customAet.trim();
            }
        }
        if (modality != null) {
            String defaultAet = defaultAetMap.get(modality.trim().toUpperCase());
            if (defaultAet != null && !defaultAet.trim().isEmpty()) {
                return defaultAet.trim();
            }
        }
        return defaultFallback;
    }

    /**
     * Checks whether a mapping exists for the given procedure code.
     *
     * @param kdJenisPrw the radiology procedure code
     * @return {@code true} if the mapping file contains an entry for this code
     */
    public boolean hasMapping(String kdJenisPrw) {
        if (kdJenisPrw == null || kdJenisPrw.trim().isEmpty()) {
            return false;
        }
        return modalityMap.containsKey(kdJenisPrw.trim());
    }

    /**
     * Returns an unmodifiable view of all loaded mappings. Useful for
     * diagnostics or UI display.
     *
     * @return read-only map of kd_jenis_prw → DICOM modality
     */
    public Map<String, String> getAllMappings() {
        return Collections.unmodifiableMap(modalityMap);
    }

    /**
     * Returns the total number of loaded procedure-to-modality mappings.
     *
     * @return mapping count
     */
    public int size() {
        return modalityMap.size();
    }

    /**
     * Returns {@code true} if the mapping file was read successfully and
     * contains at least one valid entry.
     *
     * @return {@code true} if loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Forces a re-read of the mapping file. Call this after the file has been
     * edited externally so changes are picked up without restarting the
     * application.
     */
    public synchronized void reload() {
        modalityMap.clear();
        procedureAetMap.clear();
        defaultAetMap.clear();
        loaded = false;
        loadMapping();
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------
    /**
     * Reads and parses the mapping_tindakan_radiologi.iyem JSON file. Entries
     * with a blank kd_jenis_prw, blank modality, or the placeholder value
     * "XXXXXX" are silently skipped.
     */
    private synchronized void loadMapping() {
        if (loaded) {
            return;
        }

        File file = new File(MAPPING_FILE);
        if (!file.exists()) {
            System.out.println("RadiologyModalityMapper : File mapping tidak ditemukan: " + MAPPING_FILE);
            loaded = true; // mark done so we don't keep retrying on every call
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(file);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(reader);

            // Parse default_aet map
            JsonNode defaultAetNode = root.path("default_aet");
            Map<String, String> tempDefaultAet = new HashMap<>();
            if (defaultAetNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = defaultAetNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String modalityKey = field.getKey().trim().toUpperCase();
                    String aetValue = field.getValue().asText().trim();
                    if (!modalityKey.isEmpty() && !aetValue.isEmpty()) {
                        tempDefaultAet.put(modalityKey, aetValue);
                    }
                }
            }
            defaultAetMap = tempDefaultAet;

            // Parse mapping array
            JsonNode mappingArray = root.path("mapping");
            if (!mappingArray.isArray()) {
                System.out.println("RadiologyModalityMapper : Format file tidak valid — field 'mapping' bukan array");
                loaded = true;
                return;
            }

            Map<String, String> tempModality = new HashMap<>();
            Map<String, String> tempProcedureAet = new HashMap<>();
            for (JsonNode entry : mappingArray) {
                String kd = entry.path("kd_jenis_prw").asText().trim();
                String modality = entry.path("modality").asText().trim().toUpperCase();
                String aet = entry.path("aet").asText().trim();

                // Skip blank entries and design-time placeholder rows
                if (kd.isEmpty() || modality.isEmpty() || "XXXXXX".equals(kd)) {
                    continue;
                }
                tempModality.put(kd, modality);
                if (!aet.isEmpty()) {
                    tempProcedureAet.put(kd, aet);
                }
            }

            modalityMap = tempModality;
            procedureAetMap = tempProcedureAet;
            loaded = true;
            System.out.println("RadiologyModalityMapper : Berhasil memuat " + modalityMap.size()
                    + " mapping modality, " + procedureAetMap.size() + " custom AET, dan "
                    + defaultAetMap.size() + " default AET dari " + MAPPING_FILE);

        } catch (Exception e) {
            System.out.println("RadiologyModalityMapper : Gagal membaca file mapping: " + e);
            loaded = true; // prevent infinite retry loop
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
