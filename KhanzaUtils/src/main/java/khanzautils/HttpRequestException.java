package khanzautils;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

/**
 * Domain-specific unchecked exception for HTTP request failures.
 * <p>
 * Carries structured context (URL, HTTP method, status code) so that callers
 * and loggers can produce actionable diagnostics without parsing the message
 * string.
 * </p>
 *
 * <p>
 * This is a drop-in replacement for the previous generic
 * {@code RuntimeException("HTTP request failed")} and is caught by any existing
 * {@code catch (RuntimeException)} blocks.
 * </p>
 *
 * @author malifnasrulloh
 */
public class HttpRequestException extends RuntimeException {

    private final String url;
    private final HttpMethod method;
    private final HttpStatusCode statusCode;

    /**
     * Creates an exception for a failed HTTP call.
     *
     * @param url the target URL of the request
     * @param method the HTTP method used (GET, POST, …)
     * @param statusCode the response status code, or {@code null} if the
     * failure occurred before a response was received
     * @param cause the underlying cause
     */
    public HttpRequestException(String url, HttpMethod method, HttpStatusCode statusCode, Throwable cause) {
        super(buildMessage(url, method, statusCode), cause);
        this.url = url;
        this.method = method;
        this.statusCode = statusCode;
    }

    /**
     * Creates an exception for a failed HTTP call without a status code (e.g.
     * connection timeout, DNS failure).
     *
     * @param url the target URL of the request
     * @param method the HTTP method used
     * @param cause the underlying cause
     */
    public HttpRequestException(String url, HttpMethod method, Throwable cause) {
        this(url, method, null, cause);
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    /**
     * @return the HTTP status code, or {@code null} if no response was received
     */
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    private static String buildMessage(String url, HttpMethod method, HttpStatusCode statusCode) {
        StringBuilder sb = new StringBuilder("HTTP request failed: ").append(method).append(' ').append(url);
        if (statusCode != null) {
            sb.append(" [status ").append(statusCode.value()).append(']');
        }
        return sb.toString();
    }
}
