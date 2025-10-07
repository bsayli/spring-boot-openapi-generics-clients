package io.github.bsayli.openapi.client.common;

import java.util.List;

public record Page<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrev) {

  public Page {
    content = (content == null) ? List.of() : List.copyOf(content);
  }
}
