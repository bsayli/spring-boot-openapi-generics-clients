package io.github.blueprintplatform.samples.typecoverage.contract;

import java.util.List;

/**
 * Application-owned batch-oriented generic container.
 *
 * @param elements current batch elements
 * @param sequence zero-based batch sequence
 * @param complete whether this is the final batch
 * @param <T>      item type
 */
public record Batch<T>(List<T> elements, long sequence, boolean complete) {

    public Batch {
        elements = elements == null ? List.of() : List.copyOf(elements);
        sequence = Math.max(sequence, 0L);
    }

    public static <T> Batch<T> of(List<T> elements, long sequence, boolean complete) {
        return new Batch<>(elements, sequence, complete);
    }
}