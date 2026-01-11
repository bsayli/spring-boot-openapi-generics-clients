package io.github.bsayli.apicontract.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Container for optional extension fields in {@code application/problem+json}.
 *
 * <p>Initially includes {@link ErrorItem} list, but designed to be extensible for future metadata
 * (traceId, correlationId, pagination info, etc.).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemExtensions(List<ErrorItem> errors) {
  public static ProblemExtensions ofErrors(List<ErrorItem> errors) {
    return new ProblemExtensions(errors);
  }
}
