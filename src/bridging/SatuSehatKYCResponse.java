package bridging;

public class SatuSehatKYCResponse {
    private boolean success;
    private int statusCode;
    private String message = "";
    private String errorCode = "";
    private String token = "";
    private String validationUrl = "";
    private String agentName = "";
    private String agentNik = "";
    private String ihsNumber = "";
    private String challengeCode = "";
    private String createdTimestamp = "";
    private String expiredTimestamp = "";
    private String rawRequestBody = "";
    private String rawResponseBody = "";

    public static SatuSehatKYCResponse error(int statusCode, String message, String errorCode) {
        SatuSehatKYCResponse res = new SatuSehatKYCResponse();
        res.setSuccess(false);
        res.setStatusCode(statusCode);
        res.setMessage(message);
        res.setErrorCode(errorCode);
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message != null ? message : ""; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode != null ? errorCode : ""; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token != null ? token : ""; }

    public String getValidationUrl() { return validationUrl; }
    public void setValidationUrl(String validationUrl) { this.validationUrl = validationUrl != null ? validationUrl : ""; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName != null ? agentName : ""; }

    public String getAgentNik() { return agentNik; }
    public void setAgentNik(String agentNik) { this.agentNik = agentNik != null ? agentNik : ""; }

    public String getIhsNumber() { return ihsNumber; }
    public void setIhsNumber(String ihsNumber) { this.ihsNumber = ihsNumber != null ? ihsNumber : ""; }

    public String getChallengeCode() { return challengeCode; }
    public void setChallengeCode(String challengeCode) { this.challengeCode = challengeCode != null ? challengeCode : ""; }

    public String getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(String createdTimestamp) { this.createdTimestamp = createdTimestamp != null ? createdTimestamp : ""; }

    public String getExpiredTimestamp() { return expiredTimestamp; }
    public void setExpiredTimestamp(String expiredTimestamp) { this.expiredTimestamp = expiredTimestamp != null ? expiredTimestamp : ""; }

    public String getRawRequestBody() { return rawRequestBody; }
    public void setRawRequestBody(String rawRequestBody) { this.rawRequestBody = rawRequestBody != null ? rawRequestBody : ""; }

    public String getRawResponseBody() { return rawResponseBody; }
    public void setRawResponseBody(String rawResponseBody) { this.rawResponseBody = rawResponseBody != null ? rawResponseBody : ""; }
}
