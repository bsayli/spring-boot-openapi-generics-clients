package io.github.bsayli.customerservice.common.api;

public final class ApiConstants {
  private ApiConstants() {}

  public static final class ErrorCode {
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {}
  }
}
