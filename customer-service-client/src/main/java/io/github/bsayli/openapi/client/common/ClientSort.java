package io.github.bsayli.openapi.client.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public class ClientSort {
  private String field;
  private Direction direction;

  public ClientSort() {}

  public ClientSort(String field, Direction direction) {
    this.field = field;
    this.direction = direction;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClientSort that)) return false;
    return Objects.equals(field, that.field) && direction == that.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, direction);
  }

  @Override
  public String toString() {
    return "ClientSort{field='" + field + "', direction=" + direction + '}';
  }

  public enum Direction {
    ASC("asc"),
    DESC("desc");

    private final String value;

    Direction(String v) {
      this.value = v;
    }

    @JsonCreator
    public static Direction from(String s) {
      if (s == null) return ASC;
      return "desc".equalsIgnoreCase(s) ? DESC : ASC;
    }

    @JsonValue
    public String value() {
      return value;
    }
  }
}
