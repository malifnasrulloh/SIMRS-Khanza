package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client wrapper for the Orthanc DICOM server REST API.
 *
 * <p>
 * Provides primitive operations only — each method does exactly one HTTP call.
 * Workflow orchestration (e.g. update ACSN then send) belongs in the calling UI
 * class, not here.
 *
 * <p>
 * <b>Silent flag convention:</b> Methods that normally show a
 * {@code JOptionPane} dialog accept an overloaded {@code silent} variant. Pass
 * {@code silent=true} when calling inside a batch loop to suppress per-row
 * dialog boxes; the caller is responsible for showing a summary after the loop
 * completes.
 *
 * <p>
 * <b>DICOM Router AE title:</b> The target modality name is defined in
 * {@link #DICOM_ROUTER_AE_TITLE}. Change that constant (or move it to
 * {@code koneksiDB}) if your router has a different AE title.
 *
 * @author windiartonugroho (base), malifnasrulloh (extensions)
 */
public class ApiOrthanc {

    private static final ObjectMapper mapper = new ObjectMapper();
    private SSLContext sslContext;
    private org.springframework.http.client.SimpleClientHttpRequestFactory factory;
    private String auth, authEncrypt;
    private byte[] encodedBytes;
    private RestTemplate restTemplate;

    public ApiOrthanc() {
        try {
            auth = koneksiDB.USERORTHANC() + ":" + koneksiDB.PASSORTHANC();
            encodedBytes = Base64.encodeBase64(auth.getBytes());
            authEncrypt = new String(encodedBytes);
        } catch (Exception ex) {
            System.out.println("ApiOrthanc constructor error : " + ex);
        }
    }

    private String getDicomRouterAeTitle() {
        String ae = koneksiDB.AETITLE_DICOMROUTER();
        return ae == null || ae.trim().isEmpty() ? "DCMROUTER" : ae.trim();
    }

    /**
     * Returns the Base64-encoded Basic Auth token.
     */
    public String Auth() {
        return authEncrypt;
    }

    // =========================================================================
    // Study / Series search
    // =========================================================================
    /**
     * Searches Orthanc for studies matching a Patient ID and date range.
     *
     * @param norm Patient ID (No. RM)
     * @param tanggal1 Start date in DICOM format (yyyyMMdd)
     * @param tanggal2 End date in DICOM format (yyyyMMdd)
     * @return JsonNode array of matching studies, or {@code null} on error
     */
    public JsonNode AmbilSeries(String norm, String tanggal1, String tanggal2) {
        System.out.println("Mengambil Study Pasien : " + norm);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"StudyDate\": \"" + tanggal1 + "-" + tanggal2 + "\","
                    + "\"PatientID\": \"" + norm + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON AmbilSeries : " + requestJson);
            HttpEntity<String> requestEntity = new HttpEntity(requestJson, headers);
            requestJson = getRest().exchange(
                    orthancUrl("/tools/find"), HttpMethod.POST, requestEntity, String.class
            ).getBody();
            System.out.println("Result JSON AmbilSeries : " + requestJson);
            root = mapper.readTree(requestJson);
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilSeries error : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil data dari Orthanc, silahkan hubungi administrator ..!!");
            root = null;
        }
        return root;
    }

    /**
     * Searches Orthanc for studies matching Patient ID, date range, AND
     * modality type. This is the strict filter required to prevent
     * cross-matching when a patient has multiple modality exams on the same day
     * (e.g. both CT and US).
     *
     * @param norm Patient ID (No. RM)
     * @param tanggal1 Start date in DICOM format (yyyyMMdd)
     * @param tanggal2 End date in DICOM format (yyyyMMdd)
     * @param modality DICOM modality code (e.g. "CR", "US", "CT", "MR")
     * @return JsonNode array of matching studies, or {@code null} on failure
     */
    public JsonNode AmbilSeriesDenganModality(String norm, String tanggal1,
            String tanggal2, String modality) {
        System.out.println("Mengambil Study Pasien : " + norm + " | Modality : " + modality);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"StudyDate\": \"" + tanggal1 + "-" + tanggal2 + "\","
                    + "\"PatientID\": \"" + norm + "\","
                    + "\"ModalitiesInStudy\": \"" + modality + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON AmbilSeriesDenganModality : " + requestJson);
            HttpEntity<String> requestEntity = new HttpEntity(requestJson, headers);
            requestJson = getRest().exchange(
                    orthancUrl("/tools/find"), HttpMethod.POST, requestEntity, String.class
            ).getBody();
            System.out.println("Result JSON AmbilSeriesDenganModality : " + requestJson);
            root = mapper.readTree(requestJson);
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilSeriesDenganModality error : " + e);
            root = null;
        }
        return root;
    }

    /**
     * Fetches ALL studies for a given Patient ID from Orthanc (no date or
     * modality filter). Used by the multi-signal scoring engine to collect
     * candidates for existing-data matching when AccessionNumber is not set.
     *
     * @param noRM Patient ID (No. RM)
     * @return JsonNode array of all studies for the patient, or {@code null}
     */
    public JsonNode AmbilSemuaStudyPasien(String noRM) {
        System.out.println("Mengambil Semua Study Pasien : " + noRM);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"PatientID\": \"" + noRM + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON AmbilSemuaStudyPasien : " + requestJson);
            HttpEntity<String> requestEntity = new HttpEntity(requestJson, headers);
            requestJson = getRest().exchange(
                    orthancUrl("/tools/find"), HttpMethod.POST, requestEntity, String.class
            ).getBody();
            System.out.println("Result JSON AmbilSemuaStudyPasien : " + requestJson);
            root = mapper.readTree(requestJson);
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilSemuaStudyPasien error : " + e);
            root = null;
        }
        return root;
    }

    /**
     * Finds the Orthanc internal study ID by searching for an exact
     * AccessionNumber. Used to re-resolve the study ID after a modify
     * operation, since {@code /studies/{id}/modify} with
     * {@code KeepSource:false} destroys the original study and assigns a new
     * internal ID.
     *
     * @param accessionNumber the AccessionNumber to search for
     * @return Orthanc internal study ID string, or empty string if not found
     */
    public String findStudyByAccession(String accessionNumber) {
        System.out.println("Mencari Study berdasarkan AccessionNumber : " + accessionNumber);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"AccessionNumber\": \"" + accessionNumber + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON findStudyByAccession : " + requestJson);
            HttpEntity<String> requestEntity = new HttpEntity(requestJson, headers);
            requestJson = getRest().exchange(
                    orthancUrl("/tools/find"), HttpMethod.POST, requestEntity, String.class
            ).getBody();
            System.out.println("Result JSON findStudyByAccession : " + requestJson);
            JsonNode result = mapper.readTree(requestJson);
            if (result.isArray() && result.size() > 0) {
                String studyId = result.get(0).path("ID").asText();
                System.out.println("findStudyByAccession : Ditemukan Study ID = " + studyId);
                return studyId;
            }
            System.out.println("findStudyByAccession : Study tidak ditemukan untuk ACSN=" + accessionNumber);
        } catch (Exception e) {
            System.out.println("ApiOrthanc findStudyByAccession error : " + e);
        }
        return "";
    }

    // =========================================================================
    // Image retrieval
    // =========================================================================
    public JsonNode AmbilPng(String noRawat, String series) {
        System.out.println("Mengambil Gambar PNG : " + noRawat + ", Series : " + series);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            String requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            int i = 1;
            for (JsonNode instance : root.path("Instances")) {
                HttpHeaders imgHeaders = new HttpHeaders();
                imgHeaders.add("Authorization", "Basic " + authEncrypt);
                imgHeaders.add("Accept", "image/png");
                imgHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                imgHeaders.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(imgHeaders);
                ResponseEntity<byte[]> response = getRest().exchange(
                        orthancUrl("/instances/" + instance.asText() + "/preview"),
                        HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + noRawat + i + ".png"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar PNG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilPng error : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar PNG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilJpg(String noRawat, String series) {
        System.out.println("Mengambil Gambar JPG : " + noRawat + ", Series : " + series);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            String requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            int i = 1;
            for (JsonNode instance : root.path("Instances")) {
                HttpHeaders imgHeaders = new HttpHeaders();
                imgHeaders.add("Authorization", "Basic " + authEncrypt);
                imgHeaders.add("Accept", "image/jpeg");
                imgHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                imgHeaders.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(imgHeaders);
                ResponseEntity<byte[]> response = getRest().exchange(
                        orthancUrl("/instances/" + instance.asText() + "/preview"),
                        HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + noRawat + i + ".jpg"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar JPG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilJpg error : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilJpg2(String Series) {
        System.out.println("Percobaan Mengambil Gambar JPG : " + Series + ", Series : " + Series);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + orthancUrl("/series/" + Series));
            String requestJson = getRest().exchange(orthancUrl("/series/" + Series), HttpMethod.GET, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            for (JsonNode list : root.path("Instances")) {
                HttpHeaders imgHeaders = new HttpHeaders();
                imgHeaders.add("Authorization", "Basic " + authEncrypt);
                imgHeaders.add("Accept", "image/jpeg");
                imgHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                imgHeaders.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(imgHeaders);
                ResponseEntity<byte[]> response = getRest().exchange(orthancUrl("/instances/" + list.asText() + "/preview"), HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + Series + ".jpg"), response.getBody());
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilBmp(String noRawat, String series) {
        System.out.println("Mengambil Gambar BMP : " + noRawat + ", Series : " + series);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            String requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            int i = 1;
            for (JsonNode instance : root.path("Instances")) {
                HttpHeaders imgHeaders = new HttpHeaders();
                imgHeaders.add("Authorization", "Basic " + authEncrypt);
                imgHeaders.add("Accept", "image/bmp");
                imgHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                imgHeaders.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(imgHeaders);
                ResponseEntity<byte[]> response = getRest().exchange(
                        orthancUrl("/instances/" + instance.asText() + "/preview"),
                        HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + noRawat + i + ".bmp"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar BMP dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilBmp error : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar BMP dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilDcm(String noRawat, String series) {
        System.out.println("Mengambil Gambar DCM : " + noRawat + ", Series : " + series);
        JsonNode root = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            String requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            int i = 1;
            for (JsonNode instance : root.path("Instances")) {
                HttpHeaders imgHeaders = new HttpHeaders();
                imgHeaders.add("Authorization", "Basic " + authEncrypt);
                imgHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                HttpEntity<String> entity = new HttpEntity<>(imgHeaders);
                ResponseEntity<byte[]> response = getRest().exchange(
                        orthancUrl("/instances/" + instance.asText() + "/file"),
                        HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + noRawat + i + ".dcm"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null,
                    "Pengambilan Gambar DCM dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("ApiOrthanc AmbilDcm error : " + e);
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil Gambar DCM dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public byte[] AmbilGambarWebapps(String url) {
        return AmbilGambarWebapps(url, false);
    }

    /**
     * Downloads an image from the hybrid webapps URL (radiologi folder).
     *
     * @param url full HTTP URL to the image file
     * @param silent when {@code true}, no {@link JOptionPane} on failure (batch
     * use)
     */
    public byte[] AmbilGambarWebapps(String url, boolean silent) {
        System.out.println("Mengambil Gambar : " + url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "image/*,*/*");
            headers.add("User-Agent", "SIMRS-Khanza-ApiOrthanc/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = getRest().exchange(
                    url,
                    HttpMethod.GET, entity, byte[].class);
            System.out.println("Pengambilan gambar dari Webapps berhasil.");
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Webapps Ambil Gambar error : " + e);
            if (!silent) {
                JOptionPane.showMessageDialog(null,
                        "Gagal mengambil Gambar dari Webapps, silahkan hubungi administrator ..!!");
            }
        }
        return null;
    }

    // =========================================================================
    // Dicom Converter Integration
    // =========================================================================
    private String dicomConverterUrl(String endpoint) {
        String base = koneksiDB.URLDICOMCONVERTER() == null ? "" : koneksiDB.URLDICOMCONVERTER().trim();
        if (base.isEmpty()) {
            base = "http://localhost";
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.matches("(?i)^https?://[^/:]+:\\d+(/.*)?$")) {
            return base + endpoint;
        }
        String port = koneksiDB.PORTDICOMCONVERTER() == null ? "8080" : koneksiDB.PORTDICOMCONVERTER().trim();
        return base + ":" + port + endpoint;
    }

    private String dicomConverterSendToOrthancUrl() {
        return dicomConverterUrl("/api/v1/send-to-orthanc");
    }

    private String dicomConverterSendToOrthancFromUrlsUrl() {
        return dicomConverterUrl("/api/v1/send-to-orthanc-from-urls");
    }

    // =========================================================================
    // Go API proxy methods (route Orthanc requests through dicom-converter-api)
    // =========================================================================
    private String dicomConverterFindByAcsnUrl() {
        return dicomConverterUrl("/api/v1/studies/find-by-acsn");
    }

    private String dicomConverterPatientStudiesUrl(String patientId) {
        return dicomConverterUrl("/api/v1/patients/" + patientId + "/studies");
    }

    private String dicomConverterSendToModalityUrl(String studyId, String aeTitle) {
        return dicomConverterUrl("/api/v1/studies/" + studyId + "/send-to-modality/" + aeTitle);
    }

    /**
     * Finds an Orthanc study by AccessionNumber via Go API proxy.
     * Falls back to direct Orthanc call if Go API is unreachable.
     *
     * @param accessionNumber the ACSN to search for
     * @return Orthanc internal study ID, or empty string on failure
     */
    public String findStudyByAccessionProxy(String accessionNumber) {
        System.out.println("Proxy findStudyByAccession : " + accessionNumber);
        try {
            String jsonPayload = "{\"accession_number\":\"" + accessionNumber + "\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "SIMRS-Khanza-ApiOrthanc/1.0");
            HttpEntity<String> requestEntity = new HttpEntity(jsonPayload, headers);
            ResponseEntity<String> response = getRest().exchange(
                    dicomConverterFindByAcsnUrl(), HttpMethod.POST, requestEntity, String.class);
            JsonNode result = mapper.readTree(response.getBody());
            String studyId = result.path("study_id").asText();
            return studyId;
        } catch (Exception e) {
            System.out.println("Proxy findStudyByAccession error (fallback to direct): " + e);
            return findStudyByAccession(accessionNumber);
        }
    }

    /**
     * Sends a study to DICOM modality via Go API proxy.
     * Falls back to direct Orthanc call if Go API is unreachable.
     */
    public boolean kirimKeModalityProxy(String studyId, String aeTitle, boolean silent) {
        System.out.println("Proxy kirimKeModality : Study=" + studyId + " → " + aeTitle);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "SIMRS-Khanza-ApiOrthanc/1.0");
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            ResponseEntity<String> response = getRest().exchange(
                    dicomConverterSendToModalityUrl(studyId, aeTitle),
                    HttpMethod.POST, requestEntity, String.class);
            JsonNode result = mapper.readTree(response.getBody());
            if ("success".equalsIgnoreCase(result.path("status").asText())) {
                if (!silent) {
                    JOptionPane.showMessageDialog(null, "Proses kirim ke Modality selesai..!!");
                }
                return true;
            }
            throw new RuntimeException("send-to-modality failed: " + result.toString());
        } catch (Exception e) {
            System.out.println("Proxy kirimKeModality error (fallback to direct): " + e);
            return kirimKeModality(studyId, silent);
        }
    }

    /**
     * Converts a list of remote attachment URLs and sends them to Orthanc via
     * dicom-converter-api in a single JSON POST request.
     *
     * @param urls The list of URLs to download
     * @param parametersJson JSON parameters for the converter (e.g. SOP class, Modality)
     * @param orthancModifyJson JSON payload for Orthanc tags modification
     * @return JsonNode of the API response
     */
    public JsonNode KirimKeDicomConverterFromURLs(java.util.List<String> urls, String parametersJson, String orthancModifyJson) {
        System.out.println("Kirim list URL ke dicom-converter-api, count: " + urls.size());
        java.net.HttpURLConnection connection = null;
        try {
            String urlStr = dicomConverterSendToOrthancFromUrlsUrl();
            System.out.println("URL Converter (From URLs) : " + urlStr);

            java.net.URL url = new java.net.URL(urlStr);
            connection = (java.net.HttpURLConnection) url.openConnection(java.net.Proxy.NO_PROXY);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("User-Agent", "SIMRS-Khanza-ApiOrthanc/1.0");

            java.util.Map<String, Object> reqMap = new java.util.HashMap<>();
            reqMap.put("filetype", "img");
            reqMap.put("urls", urls);
            if (parametersJson != null && !parametersJson.trim().isEmpty()) {
                reqMap.put("parameters", mapper.readTree(parametersJson));
            }
            if (orthancModifyJson != null && !orthancModifyJson.trim().isEmpty()) {
                reqMap.put("orthanc_modify", mapper.readTree(orthancModifyJson));
            }

            String requestBody = mapper.writeValueAsString(reqMap);

            java.io.OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int statusCode = connection.getResponseCode();
            java.io.InputStream inputStream;
            if (statusCode >= 200 && statusCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            if (inputStream == null) {
                System.out.println("KirimKeDicomConverterFromURLs HTTP " + statusCode + " : empty stream");
                return null;
            }

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseSB = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseSB.append(line);
            }
            reader.close();
            inputStream.close();

            String responseStr = responseSB.toString();
            System.out.println("Result dicom-converter-api from URLs (HTTP " + statusCode + ") : " + responseStr);
            JsonNode initialResponse = mapper.readTree(responseStr);
            String jobId = initialResponse.path("job_id").asText();
            if (jobId != null && !jobId.isEmpty()) {
                return PollJobStatus(jobId);
            }
            return initialResponse;
        } catch (Exception e) {
            System.out.println("Notifikasi KirimKeDicomConverterFromURLs : " + e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Converts a downloaded image and sends it to Orthanc via
     * dicom-converter-api.
     *
     * @param fileData The byte array of the image file
     * @param filename The name of the file (e.g. "image.jpg")
     * @param parametersJson JSON parameters for the converter (e.g. SOP class,
     * Modality)
     * @param orthancModifyJson JSON payload for Orthanc tags modification
     * @return JsonNode of the API response (success or structured error body)
     */
    public JsonNode KirimKeDicomConverter(byte[] fileData, String filename, String parametersJson, String orthancModifyJson) {
        System.out.println("Kirim file ke dicom-converter-api : " + filename);
        java.net.HttpURLConnection connection = null;
        try {
            String urlStr = dicomConverterSendToOrthancUrl();
            System.out.println("URL Converter : " + urlStr);

            String cleanFilename = filename;
            if (filename.contains("/")) {
                cleanFilename = filename.substring(filename.lastIndexOf("/") + 1);
            } else if (filename.contains("\\")) {
                cleanFilename = filename.substring(filename.lastIndexOf("\\") + 1);
            }

            java.net.URL url = new java.net.URL(urlStr);
            // 1. Bypass any system/JVM proxy natively by using Proxy.NO_PROXY!
            connection = (java.net.HttpURLConnection) url.openConnection(java.net.Proxy.NO_PROXY);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            String boundary = "Boundary" + System.currentTimeMillis();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("User-Agent", "SIMRS-Khanza-ApiOrthanc/1.0");

            java.io.OutputStream outputStream = connection.getOutputStream();
            java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(outputStream, "UTF-8"), true);

            String LINE_FEED = "\r\n";

            // Add file field
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(cleanFilename).append("\"").append(LINE_FEED);
            writer.append("Content-Type: application/octet-stream").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            outputStream.write(fileData);
            outputStream.flush();
            writer.append(LINE_FEED);
            writer.flush();

            // Add filetype field
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"filetype\"").append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append("img").append(LINE_FEED);
            writer.flush();

            // Add parameters field
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"parameters\"").append(LINE_FEED);
            writer.append("Content-Type: application/json; charset=UTF-8").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(parametersJson).append(LINE_FEED);
            writer.flush();

            // Add orthanc_modify field
            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"orthanc_modify\"").append(LINE_FEED);
            writer.append("Content-Type: application/json; charset=UTF-8").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(orthancModifyJson).append(LINE_FEED);
            writer.flush();

            // End of multipart
            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.flush();
            writer.close();
            outputStream.close();

            int statusCode = connection.getResponseCode();
            java.io.InputStream inputStream;
            if (statusCode >= 200 && statusCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            if (inputStream == null) {
                System.out.println("KirimKeDicomConverter HTTP " + statusCode + " : empty stream");
                return null;
            }

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseSB = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseSB.append(line);
            }
            reader.close();
            inputStream.close();

            String responseStr = responseSB.toString();
            System.out.println("Result dicom-converter-api (HTTP " + statusCode + ") : " + responseStr);
            JsonNode initialResponse = mapper.readTree(responseStr);
            String jobId = initialResponse.path("job_id").asText();
            if (jobId != null && !jobId.isEmpty()) {
                return PollJobStatus(jobId);
            }
            return initialResponse;
        } catch (Exception e) {
            System.out.println("Notifikasi KirimKeDicomConverter : " + e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Polls the Go API until the async job completes or fails.
     * This ensures synchronization with the Go background worker pool.
     *
     * @param jobId the UUID of the background task in Go
     * @return the final result node from Go, or an error node on failure/timeout
     */
    private JsonNode PollJobStatus(String jobId) {
        System.out.println("Polling status untuk Job ID: " + jobId);
        try {
            String urlStr = dicomConverterUrl("/api/v1/jobs/" + jobId);

            // Loop until terminal state (COMPLETED or FAILED)
            int maxAttempts = 150; // 150 attempts * 2 seconds = 5 minutes max
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                // Wait 2 seconds between polls
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("User-Agent", "SIMRS-Khanza-ApiOrthanc/1.0");
                HttpEntity<String> requestEntity = new HttpEntity(headers);

                ResponseEntity<String> response = getRest().exchange(urlStr, HttpMethod.GET, requestEntity, String.class);
                JsonNode jobData = mapper.readTree(response.getBody());

                String status = jobData.path("status").asText();
                System.out.println("Job " + jobId + " (attempt " + attempt + ") status: " + status);

                if ("COMPLETED".equals(status)) {
                    // Success! Extract the result payload (contains upload and modify data)
                    JsonNode resultNode = jobData.path("result");
                    // Inject success status for UI compatibility
                    if (resultNode.isObject()) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) resultNode).put("status", "success");
                    }
                    return resultNode;
                } else if ("FAILED".equals(status)) {
                    // Job failed in the Go worker
                    String errorMsg = jobData.path("error").asText();
                    String errJson = String.format("{\"status\":\"error\", \"error\":\"%s\", \"code\":\"JOB_FAILED\"}",
                            errorMsg.replace("\"", "\\\""));
                    return mapper.readTree(errJson);
                }
                // If "PENDING" or "PROCESSING", keep looping
            }

            return mapper.readTree("{\"status\":\"error\", \"error\":\"Job timed out after 5 minutes\", \"code\":\"TIMEOUT\"}");

        } catch (Exception e) {
            System.out.println("ApiOrthanc PollJobStatus error : " + e);
            try {
                return mapper.readTree(String.format("{\"status\":\"error\", \"error\":\"%s\"}",
                        e.getMessage().replace("\"", "\\\"")));
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    // =========================================================================
    // AccessionNumber update
    // =========================================================================
    /**
     * Convenience overload — shows a dialog on failure.
     *
     * @param studyId Orthanc internal study ID
     * @param accessionBaru the new AccessionNumber value
     * @return {@code true} on success
     */
    public boolean UbahAccession(String studyId, String accessionBaru) {
        return UbahAccession(studyId, accessionBaru, false);
    }

    /**
     * Modifies the AccessionNumber of a study in Orthanc.
     *
     * <p>
     * Uses {@code KeepSource: false}, which means Orthanc deletes the original
     * study and creates a new one with a new internal ID. After calling this
     * method, use {@link #findStudyByAccession(String)} to obtain the new ID
     * before performing any further operations on the study.
     *
     * @param studyId Orthanc internal study ID to modify
     * @param accessionBaru the new AccessionNumber value
     * @param silent if {@code true}, suppresses the error dialog on failure
     * (use in batch loops; show a summary after the loop)
     * @return {@code true} on success, {@code false} on any error
     */
    public boolean UbahAccession(String studyId, String accessionBaru, boolean silent) {
        System.out.println("UbahAccession : Study=" + studyId + ", ACSN=" + accessionBaru);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{"
                    + "\"Replace\": {"
                    + "\"AccessionNumber\": \"" + accessionBaru + "\""
                    + "},"
                    + "\"KeepSource\": false"
                    + "}";
            System.out.println("Request JSON UbahAccession : " + requestJson);
            HttpEntity<String> requestEntity = new HttpEntity(requestJson, headers);
            String response = getRest().exchange(
                    orthancUrl("/studies/" + studyId + "/modify"),
                    HttpMethod.POST, requestEntity, String.class
            ).getBody();
            System.out.println("Response UbahAccession : " + response);
            return true;
        } catch (Exception e) {
            System.out.println("ApiOrthanc UbahAccession error : " + e);
            if (!silent) {
                JOptionPane.showMessageDialog(null,
                        "Gagal mengubah Accession Number di Orthanc..!!");
            }
            return false;
        }
    }

    /**
     * Modifies study tags in Orthanc using a complete JSON payload.
     *
     * <p>
     * Routes through Go API gateway if {@code URLDICOMCONVERTER} is configured
     * (recommended), otherwise falls back to direct Orthanc REST call.
     *
     * <p>
     * <b>Note:</b> Demographic/clinical tags (PatientName, PatientID,
     * PatientBirthDate, PatientSex, AccessionNumber, StudyDate, Modality) are
     * embedded during DICOM conversion — do <i>not</i> include them in the
     * modify payload. The payload should contain only metadata tags such as
     * InstitutionName, ReferrringPhysicianName, ScheduledProcedureStepSequence,
     * etc.
     *
     * @param studyId Orthanc internal study ID to modify
     * @param modifyJson JSON string specifying tags to replace/remove
     * @param silent if {@code true}, suppresses the error dialog on failure
     * @return {@code true} on success, {@code false} on any error
     */
    public boolean UbahTagsStudy(String studyId, String modifyJson, boolean silent) {
        System.out.println("UbahTagsStudy : Study=" + studyId);

        // Strategy: Try Go API gateway first, fallback to direct Orthanc
        String converterBase = koneksiDB.URLDICOMCONVERTER() == null ? "" : koneksiDB.URLDICOMCONVERTER().trim();
        if (!converterBase.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity(modifyJson, headers);
                String response = getRest().exchange(
                        dicomConverterUrl("/api/v1/studies/" + studyId + "/modify"),
                        HttpMethod.POST, requestEntity, String.class
                ).getBody();
                System.out.println("UbahTagsStudy via Go API Success : " + response);
                return true;
            } catch (Exception ex) {
                System.out.println("UbahTagsStudy via Go API error (fallback to direct): " + ex);
            }
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            System.out.println("Request JSON UbahTagsStudy (direct) : " + modifyJson);
            HttpEntity<String> requestEntity = new HttpEntity(modifyJson, headers);
            String response = getRest().exchange(
                    orthancUrl("/studies/" + studyId + "/modify"),
                    HttpMethod.POST, requestEntity, String.class
            ).getBody();
            System.out.println("Response UbahTagsStudy (direct) : " + response);
            return true;
        } catch (Exception e) {
            System.out.println("UbahTagsStudy error : " + e);
            if (!silent) {
                JOptionPane.showMessageDialog(null,
                        "Gagal mengubah tags di Orthanc..!!");
            }
            return false;
        }
    }

    // =========================================================================
    // Send to DICOM router
    // =========================================================================
    /**
     * Convenience overload — shows a dialog on success and failure.
     *
     * @param studyId Orthanc internal study ID to send
     * @return {@code true} on success
     */
    public boolean kirimKeModality(String studyId) {
        return kirimKeModality(studyId, false);
    }

    /**
     * Sends a study from Orthanc to the DICOM router modality defined by
     * koneksiDB.AETITLE_DICOMROUTER().
     *
     * @param studyId Orthanc internal study ID to send
     * @param silent if {@code true}, suppresses success/failure dialogs (use in
     * batch loops; show a summary after the loop)
     * @return {@code true} on success, {@code false} on any error
     */
    public boolean kirimKeModality(String studyId, boolean silent) {
        String routerAe = getDicomRouterAeTitle();
        System.out.println("kirimKeModality : Study=" + studyId
                + " → Router=" + routerAe);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "[\"" + studyId + "\"]";
            HttpEntity<String> requestEntity = new HttpEntity(requestJson, headers);
            String url = orthancUrl("/modalities/" + routerAe + "/store");
            System.out.println("URL kirimKeModality : " + url);
            System.out.println("Request JSON kirimKeModality : " + requestJson);
            String response = getRest().exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Response kirimKeModality : " + response);
            if (!silent) {
                JOptionPane.showMessageDialog(null, "Proses kirim ke Modality selesai..!!");
            }
            return true;
        } catch (Exception e) {
            System.out.println("ApiOrthanc kirimKeModality error : " + e);
            if (!silent) {
                JOptionPane.showMessageDialog(null,
                        "Gagal kirim ke Modality. Pastikan DICOM Router (" + routerAe
                        + ") sedang aktif dan dapat dijangkau dari Orthanc..!!");
            }
            return false;
        }
    }

    // =========================================================================
    // Infrastructure
    // =========================================================================
    /**
     * Builds the full Orthanc REST endpoint URL.
     *
     * @param path path segment starting with '/', e.g. {@code "/tools/find"}
     * @return fully-qualified URL string
     */
    public String orthancUrl(String path) {
        String base = koneksiDB.URLORTHANC();
        if (base == null) base = "http://localhost";
        // Remove existing port if present (e.g. "http://localhost:8042" -> "http://localhost")
        base = base.replaceFirst("(?i)^(https?://[^:]+)(:\\d+)?(/.*)?$", "$1");
        return base + ":" + koneksiDB.PORTORTHANC() + path;
    }

    /**
     * Creates a {@link RestTemplate} configured to trust all SSL certificates.
     *
     * <p>
     * <b>Note:</b> This trust-all configuration is intentional for internal
     * hospital networks where Orthanc uses a self-signed certificate. Do NOT
     * use this pattern for public-facing services.
     */
    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        if (this.restTemplate == null) {
            sslContext = SSLContext.getInstance("SSL");
            TrustManager[] trustManagers = {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] c, String a) throws CertificateException {
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] c, String a) throws CertificateException {
                    }
                }
            };
            sslContext.init(null, trustManagers, new SecureRandom());

            org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws java.io.IOException {
                    super.prepareConnection(connection, httpMethod);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(15000);
                    if (connection instanceof javax.net.ssl.HttpsURLConnection) {
                        ((javax.net.ssl.HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                        ((javax.net.ssl.HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
                    }
                }
            };

            this.restTemplate = new RestTemplate(factory);
        }
        return this.restTemplate;
    }
}
