package bridging;

/**
 * Data Transfer Object for SATUSEHAT Rekam Medis Elektronik (SSRME) API responses.
 */
public class SatuSehatRMEResponse {
    private boolean success;
    private int statusCode;
    private String message;
    private String errorCode;
    private String shlinkId;
    private String verificationUrl;
    private String shlinkUrl;
    private String consentId;
    private String expiredAt;
    private String requestId;
    private String rawRequestBody;
    private String rawResponseBody;

    public SatuSehatRMEResponse() {
        this.success = false;
        this.statusCode = 0;
        this.message = "";
        this.errorCode = "";
        this.shlinkId = "";
        this.verificationUrl = "";
        this.shlinkUrl = "";
        this.consentId = "";
        this.expiredAt = "";
        this.requestId = "";
        this.rawRequestBody = "";
        this.rawResponseBody = "";
    }

    public static SatuSehatRMEResponse success(int statusCode, String message) {
        SatuSehatRMEResponse res = new SatuSehatRMEResponse();
        res.setSuccess(true);
        res.setStatusCode(statusCode);
        res.setMessage(message);
        return res;
    }

    public static SatuSehatRMEResponse error(int statusCode, String message, String errorCode) {
        SatuSehatRMEResponse res = new SatuSehatRMEResponse();
        res.setSuccess(false);
        res.setStatusCode(statusCode);
        res.setMessage(message);
        res.setErrorCode(errorCode);
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message != null ? message : "";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode != null ? errorCode : "";
    }

    public String getShlinkId() {
        return shlinkId;
    }

    public void setShlinkId(String shlinkId) {
        this.shlinkId = shlinkId != null ? shlinkId : "";
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl != null ? verificationUrl : "";
    }

    public String getShlinkUrl() {
        return shlinkUrl;
    }

    public void setShlinkUrl(String shlinkUrl) {
        this.shlinkUrl = shlinkUrl != null ? shlinkUrl : "";
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId != null ? consentId : "";
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expiredAt = expiredAt != null ? expiredAt : "";
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId != null ? requestId : "";
    }

    public String getRawRequestBody() {
        return rawRequestBody;
    }

    public void setRawRequestBody(String rawRequestBody) {
        this.rawRequestBody = rawRequestBody != null ? rawRequestBody : "";
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }

    public void setRawResponseBody(String rawResponseBody) {
        this.rawResponseBody = rawResponseBody != null ? rawResponseBody : "";
    }

    public boolean isConsentRequired() {
        return "CONSENT_REQUIRED".equalsIgnoreCase(errorCode) || statusCode == 403;
    }

    public boolean isDuplicateKeyError() {
        return "DUPLICATE_KEY_ERROR".equalsIgnoreCase(errorCode);
    }
}
