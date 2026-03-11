package io.github.bsayli.apicontract.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Container for optional extension fields in {@code application/problem+json}.
 *
 * @param errors structured error details attached to the problem response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemExtensions(List<ErrorItem> errors) {

  /**
   * Creates a problem extension container with structured error items.
   *
   * @param errors structured error details
   * @return problem extensions instance
   */
  public static ProblemExtensions ofErrors(List<ErrorItem> errors) {
    return new ProblemExtensions(errors);
  }
}