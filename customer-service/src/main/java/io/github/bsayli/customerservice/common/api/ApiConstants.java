package io.github.bsayli.customerservice.common.api;

public final class ApiConstants {
  private ApiConstants() {}

  public static final class Response {
    public static final String CREATED = "CREATED";
    public static final String UPDATED = "UPDATED";
    public static final String DELETED = "DELETED";
    public static final String LISTED = "LISTED";

    private Response() {}
  }

  public static final class ErrorCode {
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {}
  }
}
