package io.github.bsayli.customerservice.client.common.problem;

import io.github.bsayli.apicontract.error.ErrorItem;
import io.github.bsayli.apicontract.error.ProblemExtensions;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ProblemDetail;

public final class ApiProblemException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private static final String KEY_ERROR_CODE = "errorCode";
  private static final String KEY_EXTENSIONS = "extensions";
  private static final String KEY_ERRORS = "errors";

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
    Map<String, Object> properties = propertiesOf(pd);
    if (properties.isEmpty()) {
      return "";
    }

    Object raw = properties.get(KEY_ERROR_CODE);
    if (!(raw instanceof String value)) {
      return "";
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? "" : trimmed;
  }

  private static List<ErrorItem> resolveErrors(ProblemDetail pd) {
    Map<String, Object> properties = propertiesOf(pd);
    if (properties.isEmpty()) {
      return List.of();
    }

    Object rawExtensions = properties.get(KEY_EXTENSIONS);
    if (rawExtensions instanceof ProblemExtensions(List<ErrorItem> errors1)) {
      return copyOfOrEmpty(errors1);
    }

    if (rawExtensions instanceof Map<?, ?> extensionsMap) {
      return mapErrors(extensionsMap.get(KEY_ERRORS));
    }

    return List.of();
  }

  private static Map<String, Object> propertiesOf(ProblemDetail pd) {
    if (pd == null) {
      return Map.of();
    }

    Map<String, Object> properties = pd.getProperties();
    return properties != null ? properties : Map.of();
  }

  private static List<ErrorItem> copyOfOrEmpty(List<ErrorItem> items) {
    return (items == null || items.isEmpty()) ? List.of() : List.copyOf(items);
  }

  private static List<ErrorItem> mapErrors(Object rawErrors) {
    if (!(rawErrors instanceof List<?> list) || list.isEmpty()) {
      return List.of();
    }

    List<ErrorItem> mapped = new ArrayList<>();

    for (Object item : list) {
      if (item instanceof ErrorItem errorItem) {
        mapped.add(errorItem);
        continue;
      }

      if (item instanceof Map<?, ?> map) {
        mapped.add(
            new ErrorItem(
                asString(map.get("code")),
                asString(map.get("message")),
                asString(map.get("field")),
                asString(map.get("resource")),
                asString(map.get("id"))));
      }
    }

    return mapped.isEmpty() ? List.of() : List.copyOf(mapped);
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private static String buildMessage(ProblemDetail pd, int status) {
    if (pd == null) {
      return "HTTP %d (no problem body)".formatted(status);
    }

    StringBuilder sb = new StringBuilder("HTTP %d".formatted(status));
    appendIfNotBlank(sb, " - ", pd.getTitle());
    appendIfNotBlank(sb, " | ", pd.getDetail());

    tag(sb, "code", normalize(resolveErrorCode(pd)));
    tag(sb, "type", pd.getType() != null ? pd.getType().toString() : null);
    tag(sb, "instance", pd.getInstance() != null ? pd.getInstance().toString() : null);

    int errorCount = resolveErrors(pd).size();
    if (errorCount > 0) {
      sb.append(" [errors=").append(errorCount).append(']');
    }

    return sb.toString();
  }

  private static String normalize(String s) {
    if (s == null) {
      return null;
    }
    String trimmed = s.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static void appendIfNotBlank(StringBuilder sb, String sep, String v) {
    if (v != null && !v.isBlank()) {
      sb.append(sep).append(v);
    }
  }

  private static void tag(StringBuilder sb, String key, String value) {
    if (value != null && !value.isBlank()) {
      sb.append(" [").append(key).append('=').append(value).append(']');
    }
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
