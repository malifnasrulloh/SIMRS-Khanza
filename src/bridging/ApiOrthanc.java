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
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    /**
     * AE title of the DICOM router as configured in Orthanc's modalities list.
     * Update this constant (or externalise via koneksiDB) if the name changes.
     */
    private static final String DICOM_ROUTER_AE_TITLE = "DICOMROUTER";

    private HttpHeaders headers;
    private JsonNode root;
    private HttpEntity requestEntity;
    private final ObjectMapper mapper = new ObjectMapper();
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    private String auth, authEncrypt, requestJson;
    private byte[] encodedBytes;
    private int i = 1;

    public ApiOrthanc() {
        try {
            auth = koneksiDB.USERORTHANC() + ":" + koneksiDB.PASSORTHANC();
            encodedBytes = Base64.encodeBase64(auth.getBytes());
            authEncrypt = new String(encodedBytes);
        } catch (Exception ex) {
            System.out.println("ApiOrthanc constructor error : " + ex);
        }
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
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"StudyDate\": \"" + tanggal1 + "-" + tanggal2 + "\","
                    + "\"PatientID\": \"" + norm + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON AmbilSeries : " + requestJson);
            requestEntity = new HttpEntity(requestJson, headers);
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
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"StudyDate\": \"" + tanggal1 + "-" + tanggal2 + "\","
                    + "\"PatientID\": \"" + norm + "\","
                    + "\"ModalitiesInStudy\": \"" + modality + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON AmbilSeriesDenganModality : " + requestJson);
            requestEntity = new HttpEntity(requestJson, headers);
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
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "{"
                    + "\"Level\": \"Study\","
                    + "\"Expand\": true,"
                    + "\"Query\": {"
                    + "\"AccessionNumber\": \"" + accessionNumber + "\""
                    + "}"
                    + "}";
            System.out.println("Request JSON findStudyByAccession : " + requestJson);
            requestEntity = new HttpEntity(requestJson, headers);
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
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode instance : root.path("Instances")) {
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/png");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
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
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode instance : root.path("Instances")) {
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/jpeg");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
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

    public JsonNode AmbilBmp(String noRawat, String series) {
        System.out.println("Mengambil Gambar BMP : " + noRawat + ", Series : " + series);
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode instance : root.path("Instances")) {
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/bmp");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
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
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            requestJson = getRest().exchange(
                    orthancUrl("/series/" + series), HttpMethod.GET, requestEntity, String.class
            ).getBody();
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode instance : root.path("Instances")) {
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                HttpEntity<String> entity = new HttpEntity<>(headers);
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
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "{"
                    + "\"Replace\": {"
                    + "\"AccessionNumber\": \"" + accessionBaru + "\""
                    + "},"
                    + "\"KeepSource\": false"
                    + "}";
            System.out.println("Request JSON UbahAccession : " + requestJson);
            requestEntity = new HttpEntity(requestJson, headers);
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
     * {@link #DICOM_ROUTER_AE_TITLE}.
     *
     * @param studyId Orthanc internal study ID to send
     * @param silent if {@code true}, suppresses success/failure dialogs (use in
     * batch loops; show a summary after the loop)
     * @return {@code true} on success, {@code false} on any error
     */
    public boolean kirimKeModality(String studyId, boolean silent) {
        System.out.println("kirimKeModality : Study=" + studyId
                + " → Router=" + DICOM_ROUTER_AE_TITLE);
        try {
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.APPLICATION_JSON);
            requestJson = "[\"" + studyId + "\"]";
            requestEntity = new HttpEntity(requestJson, headers);
            String url = orthancUrl("/modalities/" + DICOM_ROUTER_AE_TITLE + "/store");
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
                        "Gagal kirim ke Modality. Pastikan DICOM Router (" + DICOM_ROUTER_AE_TITLE
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
    private String orthancUrl(String path) {
        return koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + path;
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
        sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        scheme = new Scheme("https", 443, sslFactory);
        factory = new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }
}
