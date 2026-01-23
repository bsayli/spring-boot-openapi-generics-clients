package io.github.bsayli.openapi.client.common.problem;

import io.github.bsayli.openapi.client.generated.dto.ErrorItem;
import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;
import io.github.bsayli.openapi.client.generated.dto.ProblemDetailExtensions;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Wraps non-2xx HTTP responses decoded into RFC 9457 (“Problem Details for HTTP APIs”) {@link ProblemDetail}.
 * Thrown by the RestClient defaultStatusHandler in client config.
 */
public final class ApiProblemException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ProblemDetail problem;
    private final int status;

    private final String errorCode;
    private final transient List<ErrorItem> errors;

    public ApiProblemException(ProblemDetail problem, int status) {
        super(buildMessage(problem, status));
        this.problem = problem;
        this.status = status;
        this.errorCode = resolveErrorCode(problem);
        this.errors = resolveErrors(problem);
    }

    public ApiProblemException(ProblemDetail problem, int status, Throwable cause) {
        super(buildMessage(problem, status), cause);
        this.problem = problem;
        this.status = status;
        this.errorCode = resolveErrorCode(problem);
        this.errors = resolveErrors(problem);
    }

    private static String resolveErrorCode(ProblemDetail pd) {
        if (pd == null) return "";
        String c = pd.getErrorCode();
        return (c == null) ? "" : c.trim();
    }

    private static List<ErrorItem> resolveErrors(ProblemDetail pd) {
        if (pd == null) return List.of();
        ProblemDetailExtensions ext = pd.getExtensions();
        if (ext == null) return List.of();
        List<ErrorItem> list = ext.getErrors();
        if (list == null || list.isEmpty()) return List.of();
        return List.copyOf(list);
    }

    private static String buildMessage(ProblemDetail pd, int status) {
        if (pd == null) return "HTTP %d (no problem body)".formatted(status);

        var sb = new StringBuilder("HTTP %d".formatted(status));
        appendIfNotBlank(sb, " - ", pd.getTitle());
        appendIfNotBlank(sb, " | ", pd.getDetail());

        tag(sb, "code", normalize(pd.getErrorCode()));
        tag(sb, "type", pd.getType() != null ? pd.getType().toString() : null);
        tag(sb, "instance", pd.getInstance() != null ? pd.getInstance().toString() : null);

        int errorCount = countErrors(pd);
        if (errorCount > 0) sb.append(" [errors=").append(errorCount).append(']');

        return sb.toString();
    }

    private static int countErrors(ProblemDetail pd) {
        ProblemDetailExtensions ext = pd.getExtensions();
        if (ext == null) return 0;
        List<ErrorItem> errors = ext.getErrors();
        return (errors == null) ? 0 : errors.size();
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static void appendIfNotBlank(StringBuilder sb, String sep, String v) {
        if (v != null && !v.isBlank()) sb.append(sep).append(v);
    }

    private static void tag(StringBuilder sb, String key, String value) {
        if (value != null && !value.isBlank()) sb.append(" [").append(key).append('=').append(value).append(']');
    }

    public ProblemDetail getProblem() {
        return problem;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public ErrorItem firstErrorOrNull() {
        return errors.isEmpty() ? null : errors.getFirst();
    }
}