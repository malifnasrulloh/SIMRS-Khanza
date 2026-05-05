/*
 * RadiologyModalityMapper.java
 *
 * Utility class to load and cache the mapping from kd_jenis_prw (radiology
 * procedure code) to DICOM modality type (CT, US, CR, MR, etc.).
 *
 * The mapping data is read once from ./cache/mapping_tindakan_radiologi.iyem,
 * following the same iyem-file pattern used by alergisatusehat.iyem.
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a cached, singleton lookup from radiology procedure code
 * (kd_jenis_prw) to DICOM modality identifier (CT, US, CR, DX, MR, MG, etc.).
 *
 * The mapping file is read exactly once per JVM lifetime. Use {@link #reload()}
 * to force a re-read after the file has been edited externally.
 *
 * Expected JSON format inside mapping_tindakan_radiologi.iyem:
 * <pre>
 * {
 *   "mapping": [
 *     { "kd_jenis_prw": "RD001", "nm_perawatan": "CT Scan Kepala", "modality": "CT" },
 *     { "kd_jenis_prw": "RD002", "nm_perawatan": "USG Abdomen",    "modality": "US" }
 *   ]
 * }
 * </pre>
 *
 * Usage:
 * <pre>
 *   RadiologyModalityMapper mapper = RadiologyModalityMapper.getInstance();
 *   String modality = mapper.getModality("RD001");  // returns "CT" or null
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
            JsonNode mappingArray = root.path("mapping");

            if (!mappingArray.isArray()) {
                System.out.println("RadiologyModalityMapper : Format file tidak valid — field 'mapping' bukan array");
                loaded = true;
                return;
            }

            Map<String, String> temp = new HashMap<>();
            for (JsonNode entry : mappingArray) {
                String kd = entry.path("kd_jenis_prw").asText().trim();
                String modality = entry.path("modality").asText().trim().toUpperCase();

                // Skip blank entries and design-time placeholder rows
                if (kd.isEmpty() || modality.isEmpty() || "XXXXXX".equals(kd)) {
                    continue;
                }
                temp.put(kd, modality);
            }

            modalityMap = temp;
            loaded = true;
            System.out.println("RadiologyModalityMapper : Berhasil memuat " + modalityMap.size()
                    + " mapping modality radiologi dari " + MAPPING_FILE);

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
