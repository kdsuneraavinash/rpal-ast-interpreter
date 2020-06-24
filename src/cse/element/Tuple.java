package cse.element;

import java.util.Arrays;

/**
 * Tuple for storing multiple elements.
 */
public class Tuple extends Element {
    private final Element[] value;

    /**
     * Create a tuple element with tuple label.
     *
     * @param value Tuple of elements
     */
    public Tuple(Element[] value) {
        super("tuple");
        this.value = value;
    }

    /**
     * Get the tuples inside the element.
     */
    public Element[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple that = (Tuple) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
