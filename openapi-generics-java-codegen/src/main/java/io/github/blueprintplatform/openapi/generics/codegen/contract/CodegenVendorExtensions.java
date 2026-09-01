package io.github.blueprintplatform.openapi.generics.codegen.contract;

/**
 * Vendor extension keys consumed and produced by the generics-aware Java code generator.
 *
 * <p>Canonical OpenAPI metadata is read directly from generated schemas. Additional derived
 * metadata is produced for use by the Java model templates.
 */
public final class CodegenVendorExtensions {

  public static final String API_WRAPPER = "x-api-wrapper";

  public static final String API_WRAPPER_TYPE = "x-api-wrapper-type";

  public static final String API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";

  public static final String DATA_CONTAINER = "x-data-container";

  public static final String DATA_CONTAINER_TYPE = "x-data-container-type";

  public static final String DATA_ITEM = "x-data-item";

  public static final String IGNORE_MODEL = "x-ignore-model";

  public static final String ENVELOPE_IMPORT = "x-envelope-import";

  public static final String ENVELOPE_TYPE = "x-envelope-type";

  public static final String EXTRA_IMPORTS = "x-extra-imports";

  private CodegenVendorExtensions() {}
}
