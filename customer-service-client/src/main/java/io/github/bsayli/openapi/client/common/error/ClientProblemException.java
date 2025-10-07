package io.github.bsayli.openapi.client.common.error;

import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;
import java.io.Serial;
import java.io.Serializable;

/**
 * Wraps non-2xx HTTP responses decoded into RFC7807-style {@link ProblemDetail}. Thrown by
 * RestClient defaultStatusHandler in client config.
 */
public class ClientProblemException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private final transient ProblemDetail problem;
  private final int status;

  public ClientProblemException(ProblemDetail problem, int status) {
    super(buildMessage(problem, status));
    this.problem = problem;
    this.status = status;
  }

  public ClientProblemException(ProblemDetail problem, int status, Throwable cause) {
    super(buildMessage(problem, status), cause);
    this.problem = problem;
    this.status = status;
  }

  private static String buildMessage(ProblemDetail pd, int status) {
    if (pd == null) {
      return "HTTP " + status + " (no problem body)";
    }
    StringBuilder sb = new StringBuilder("HTTP ").append(status);
    if (pd.getTitle() != null && !pd.getTitle().isBlank()) {
      sb.append(" - ").append(pd.getTitle());
    }
    if (pd.getDetail() != null && !pd.getDetail().isBlank()) {
      sb.append(" | ").append(pd.getDetail());
    }
    if (pd.getErrorCode() != null && !pd.getErrorCode().isBlank()) {
      sb.append(" [code=").append(pd.getErrorCode()).append(']');
    }
    return sb.toString();
  }

  public ProblemDetail getProblem() {
    return problem;
  }

  public int getStatus() {
    return status;
  }
}
