package io.github.bsayli.openapi.client.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientMeta {
  private String requestId;
  private Instant serverTime;
  private List<ClientSort> sort = new ArrayList<>();

  public ClientMeta() {}

  public ClientMeta(String requestId, Instant serverTime, List<ClientSort> sort) {
    this.requestId = requestId;
    this.serverTime = serverTime;
    if (sort != null) this.sort = new ArrayList<>(sort);
  }

  public static ClientMeta nowLike(String requestId, Instant serverTime, List<ClientSort> sort) {
    return new ClientMeta(requestId, serverTime, sort);
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Instant getServerTime() {
    return serverTime;
  }

  public void setServerTime(Instant serverTime) {
    this.serverTime = serverTime;
  }

  public List<ClientSort> getSort() {
    return sort;
  }

  public void setSort(List<ClientSort> sort) {
    this.sort = (sort == null) ? new ArrayList<>() : new ArrayList<>(sort);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClientMeta that)) return false;
    return Objects.equals(requestId, that.requestId)
        && Objects.equals(serverTime, that.serverTime)
        && Objects.equals(sort, that.sort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, serverTime, sort);
  }

  @Override
  public String toString() {
    return "ClientMeta{requestId='"
        + requestId
        + "', serverTime="
        + serverTime
        + ", sort="
        + sort
        + '}';
  }
}
