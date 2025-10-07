package io.github.bsayli.customerservice.common.api.response.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemExtensions(List<ErrorItem> errors) {
  public static ProblemExtensions ofErrors(List<ErrorItem> errors) {
    return new ProblemExtensions(errors);
  }
}
