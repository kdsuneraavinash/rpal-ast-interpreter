package cse.element;


/**
 * Element in the stack.
 */
public abstract class Element {
    private final String label;

    Element(String label) {
        this.label = label;
    }

    /**
     * Checks if the label is the given value.
     *
     * @param label Expected label
     * @return Whether label is the expected one
     */
    public boolean isLabel(String label) {
        return this.label.equals(label);
    }

    /**
     * @return Current element label (type)
     */
    public String getLabel() {
        return this.label;
    }
}


