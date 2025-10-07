package io.github.bsayli.customerservice.common.api.response;

import java.util.List;

/**
 * Generic pagination container used in API responses. Designed to be language-agnostic for OpenAPI
 * client generation.
 *
 * @param <T> the element type
 */
public record Page<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrev) {

  public static <T> Page<T> of(List<T> content, int page, int size, long totalElements) {
    List<T> safeContent = content == null ? List.of() : List.copyOf(content);
    int totalPages = (int) Math.ceil((double) totalElements / size);
    boolean hasNext = page + 1 < totalPages;
    boolean hasPrev = page > 0;
    return new Page<>(safeContent, page, size, totalElements, totalPages, hasNext, hasPrev);
  }
}
