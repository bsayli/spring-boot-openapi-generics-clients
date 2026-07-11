package io.github.blueprintplatform.samples.typecoverage.contract;

import java.util.List;

/**
 * Application-owned cursor-based generic container.
 *
 * @param items      current window items
 * @param nextCursor cursor for the next window, if available
 * @param hasNext    whether another window exists
 * @param <T>        item type
 */
public record Window<T>(List<T> items, String nextCursor, boolean hasNext) {

    public Window {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static <T> Window<T> of(List<T> items, String nextCursor, boolean hasNext) {
        return new Window<>(items, nextCursor, hasNext);
    }
}