package io.github.bsayli.openapi.client.common;

import io.github.bsayli.openapi.client.common.sort.ClientSort;
import java.time.Instant;
import java.util.List;

public record ClientMeta(String requestId, Instant serverTime, List<ClientSort> sort) {
  public ClientMeta {
    sort = (sort == null) ? List.of() : List.copyOf(sort);
  }
}
