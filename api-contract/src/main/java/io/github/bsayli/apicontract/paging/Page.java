package io.github.bsayli.apicontract.paging;

import java.util.List;

/**
 * Generic pagination container used in API responses.
 *
 * @param <T> element type
 * @param content current page content
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements total number of available elements
 * @param totalPages total number of available pages
 * @param hasNext whether a next page exists
 * @param hasPrev whether a previous page exists
 */
public record Page<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrev) {

  /**
   * Creates a page instance and derives navigation flags and total page count.
   *
   * @param content page content
   * @param page zero-based page index
   * @param size requested page size
   * @param totalElements total number of available elements
   * @param <T> element type
   * @return normalized page instance
   */
  public static <T> Page<T> of(List<T> content, int page, int size, long totalElements) {
    List<T> safeContent = (content == null) ? List.of() : List.copyOf(content);

    int p = Math.clamp(page, 0, Integer.MAX_VALUE);
    int s = Math.clamp(size, 1, Integer.MAX_VALUE);

    long totalPagesL = (totalElements <= 0L) ? 0L : ((totalElements + s - 1L) / s);
    int totalPages = (totalPagesL > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalPagesL;

    boolean hasNext = p < totalPages - 1;
    boolean hasPrev = p > 0;

    return new Page<>(safeContent, p, s, totalElements, totalPages, hasNext, hasPrev);
  }
}