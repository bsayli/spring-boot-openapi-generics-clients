package io.github.bsayli.customerservice.common.api.response;

public record ServiceResponse<T>(T data, Meta meta) {

  public static <T> ServiceResponse<T> ok(T data) {
    return new ServiceResponse<>(data, Meta.now());
  }

  public static <T> ServiceResponse<T> ok(T data, Meta meta) {
    return new ServiceResponse<>(data, meta != null ? meta : Meta.now());
  }
}
