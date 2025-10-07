package io.github.bsayli.openapi.client.common;

import java.util.Objects;

public class ServiceClientResponse<T> {

  private T data;
  private ClientMeta meta;

  public ServiceClientResponse() {}

  public ServiceClientResponse(T data, ClientMeta meta) {
    this.data = data;
    this.meta = meta;
  }

  public static <T> ServiceClientResponse<T> of(T data, ClientMeta meta) {
    return new ServiceClientResponse<>(data, meta);
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public ClientMeta getMeta() {
    return meta;
  }

  public void setMeta(ClientMeta meta) {
    this.meta = meta;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServiceClientResponse<?> that)) return false;
    return Objects.equals(data, that.data) && Objects.equals(meta, that.meta);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, meta);
  }

  @Override
  public String toString() {
    return "ServiceClientResponse{data=" + data + ", meta=" + meta + '}';
  }
}
