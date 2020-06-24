package cse.element;

import tree.Node;

import java.util.Objects;

/**
 * Value which will store all elements except tuples.
 */
public class Value extends Element {
    private final String value;

    /**
     * Create element from label: true, false, id, ...
     */
    public Value(String label) {
        super(label);
        this.value = null;
    }

    /**
     * Create element from label and value: str, int
     */
    public Value(String label, String value) {
        super(label);
        this.value = value;
    }

    /**
     * Create element from node. Helper function for parsing st.
     */
    public Value(Node node) {
        super(node.getLabel());
        this.value = node.getValue();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value that = (Value) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        if (value == null) return getLabel();
        return String.format("%s(%s)", getLabel(), getValue());
    }
}