package io.github.blueprintplatform.openapi.generics.contract.envelope;

import java.util.Objects;

/**
 * Canonical success envelope shared across server and client code.
 *
 * @param <T> payload type
 */
public class ServiceResponse<T> {

  private T data;
  private Meta meta;

  /**
   * Creates an empty response with default metadata.
   */
  public ServiceResponse() {
    this.meta = Meta.now();
  }

  /**
   * Creates a response with payload and metadata.
   *
   * @param data response payload
   * @param meta response metadata, defaults to {@link Meta#now()} when {@code null}
   */
  public ServiceResponse(T data, Meta meta) {
    this.data = data;
    this.meta = (meta != null) ? meta : Meta.now();
  }

  /**
   * Creates a response with payload and explicit metadata.
   *
   * @param data response payload
   * @param meta response metadata
   * @param <T> payload type
   * @return response instance
   */
  public static <T> ServiceResponse<T> of(T data, Meta meta) {
    return new ServiceResponse<>(data, meta);
  }

  /**
   * Creates a response with payload and default metadata.
   *
   * @param data response payload
   * @param <T> payload type
   * @return response instance
   */
  public static <T> ServiceResponse<T> of(T data) {
    return new ServiceResponse<>(data, null);
  }

  /**
   * Returns the response payload.
   *
   * @return payload
   */
  public T getData() {
    return data;
  }

  /**
   * Updates the response payload.
   *
   * @param data response payload
   */
  public void setData(T data) {
    this.data = data;
  }

  /**
   * Returns the response metadata.
   *
   * @return metadata
   */
  public Meta getMeta() {
    return meta;
  }

  /**
   * Updates the response metadata.
   *
   * @param meta response metadata, defaults to {@link Meta#now()} when {@code null}
   */
  public void setMeta(Meta meta) {
    this.meta = (meta != null) ? meta : Meta.now();
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