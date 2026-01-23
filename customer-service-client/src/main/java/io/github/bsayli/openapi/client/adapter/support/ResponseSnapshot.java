package io.github.bsayli.openapi.client.adapter.support;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

@SuppressWarnings("java:S6218")
record ResponseSnapshot(
    HttpStatusCode status,
    boolean statusUnavailable,
    MediaType contentType,
    byte[] body,
    IOException statusReadError,
    IOException bodyReadError) {

  private static final int MAX_BODY_BYTES = 128_000;

  static ResponseSnapshot read(ClientHttpResponse response) {
    MediaType contentType = response.getHeaders().getContentType();

    StatusRead statusRead = readStatus(response);
    BodyRead bodyRead = readBody(response);

    return new ResponseSnapshot(
        statusRead.status,
        statusRead.unavailable,
        contentType,
        bodyRead.body,
        statusRead.error,
        bodyRead.error);
  }

  private static StatusRead readStatus(ClientHttpResponse response) {
    try {
      return new StatusRead(response.getStatusCode(), false, null);
    } catch (IOException e) {
      return new StatusRead(HttpStatusCode.valueOf(500), true, e);
    }
  }

  private static BodyRead readBody(ClientHttpResponse response) {
    try (InputStream is = response.getBody()) {
      return new BodyRead(is.readNBytes(MAX_BODY_BYTES), null);
    } catch (IOException e) {
      return new BodyRead(new byte[0], e);
    }
  }

  private record StatusRead(HttpStatusCode status, boolean unavailable, IOException error) {}

  private record BodyRead(byte[] body, IOException error) {}
}
