package io.github.bsayli.apicontract.envelope;

import java.util.Objects;

public class ServiceResponse<T> {

  private T data;
  private Meta meta;

  public ServiceResponse() {}

  public ServiceResponse(T data, Meta meta) {
    this.data = data;
    this.meta = meta;
  }

  public static <T> ServiceResponse<T> of(T data, Meta meta) {
    Meta effectiveMeta = (meta != null) ? meta : Meta.now();
    return new ServiceResponse<>(data, effectiveMeta);
  }

  public static <T> ServiceResponse<T> of(T data) {
    return new ServiceResponse<>(data, Meta.now());
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public Meta getMeta() {
    return meta;
  }

  public void setMeta(Meta meta) {
    this.meta = meta;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServiceResponse<?> that)) return false;
    return Objects.equals(data, that.data) && Objects.equals(meta, that.meta);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, meta);
  }

  @Override
  public String toString() {
    return "ServiceResponse{data=" + data + ", meta=" + meta + '}';
  }
}
