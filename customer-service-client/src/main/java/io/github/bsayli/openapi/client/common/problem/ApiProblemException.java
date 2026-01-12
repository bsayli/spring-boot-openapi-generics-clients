package io.github.bsayli.openapi.client.common.problem;

import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;
import java.io.Serial;
import java.io.Serializable;

/**
 * Wraps non-2xx HTTP responses decoded into RFC 9457 (“Problem Details for HTTP APIs”) {@link
 * ProblemDetail}. Thrown by the RestClient defaultStatusHandler in client config. See: <a
 * href="https://www.rfc-editor.org/rfc/rfc9457">...</a>
 */
public final class ApiProblemException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private final transient ProblemDetail problem;
  private final int status;

  public ApiProblemException(ProblemDetail problem, int status) {
    super(buildMessage(problem, status));
    this.problem = problem;
    this.status = status;
  }

  public ApiProblemException(ProblemDetail problem, int status, Throwable cause) {
    super(buildMessage(problem, status), cause);
    this.problem = problem;
    this.status = status;
  }

  private static String buildMessage(ProblemDetail pd, int status) {
    if (pd == null) return "HTTP %d (no problem body)".formatted(status);

    var sb = new StringBuilder("HTTP %d".formatted(status));
    appendIfNotBlank(sb, " - ", pd.getTitle());
    appendIfNotBlank(sb, " | ", pd.getDetail());

    tag(sb, "code", pd.getErrorCode());
    tag(sb, "type", pd.getType() != null ? pd.getType().toString() : null);
    tag(sb, "instance", pd.getInstance() != null ? pd.getInstance().toString() : null);
    return sb.toString();
  }

  private static void appendIfNotBlank(StringBuilder sb, String sep, String v) {
    if (v != null && !v.isBlank()) sb.append(sep).append(v);
  }

  private static void tag(StringBuilder sb, String key, String value) {
    if (value != null && !value.isBlank())
      sb.append(" [").append(key).append('=').append(value).append(']');
  }

  public ProblemDetail getProblem() {
    return problem;
  }

  public int getStatus() {
    return status;
  }
}
