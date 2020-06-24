package tree;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Represents a node in the ast.
 * Each node has a parent and children.
 * Label represents the type of Node.
 * Value represents the value of node
 * if label is 'id'/'int'/ then value is the string representation.
 * if label is keyword then value is null.
 */
public class Node {
    private final ArrayList<Node> children;
    private Node parent;
    private String label;
    private String value;

    /**
     * Create an intermediate node: let, where, lambda, etc...
     *
     * @param label Node type
     */
    public Node(String label) {
        this.label = label;
        this.children = new ArrayList<>();
    }

    /**
     * Create a leaf node: id, int, str
     *
     * @param label Node type
     * @param value Node value as a string
     */
    public Node(String label, String value) {
        this.label = label;
        this.value = value;
        this.children = new ArrayList<>();
    }

    /**
     * @return Parent node reference. null if this node is root.
     */
    Node getParent() {
        return parent;
    }

    /**
     * @return Node type.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return Node value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return Number of child nodes.
     */
    public int getNumberOfChildren() {
        return children.size();
    }

    /**
     * Boolean specifying whether the node has specified number of children.
     *
     * @param n Number of expected children
     * @return Truth value
     */
    boolean hasChildren(int n) {
        return children.size() == n;
    }

    /**
     * Boolean specifying whether the label is the specified string.
     *
     * @param label Expected label
     * @return Truth value
     */
    public boolean isLabel(String label) {
        return getLabel().equals(label);
    }

    /**
     * Get the child node with specified offset.
     * Indexing starts from 0.
     *
     * @param i Node index to return
     * @return Child node reference
     */
    public Node getChild(int i) {
        return children.get(i);
    }

    /**
     * Execute a function for each child of the node.
     *
     * @param action A lambda expression to execute.
     */
    public void forEachChild(Consumer<? super Node> action) {
        children.forEach(action);
    }

    /**
     * Set the label of this node.
     * Will also change the value to null.
     * Label must be an intermediate label type.
     *
     * @param label Intermediate label type
     */
    void setLabel(String label) {
        this.label = label;
        this.value = null;
    }

    /**
     * Remove all children.
     * Will also set each child's parent node to null.
     */
    void clearChildren() {
        children.forEach(child -> child.parent = null);
        children.clear();
    }

    /**
     * Add a node as the child and set the parent of the child node.
     *
     * @param child Child node reference
     */
    void addChild(Node child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * Copies the complete tree starting from this node.
     *
     * @return Copy of the sub-tree.
     */
    Node copy() {
        Node copied = new Node(label, value);
        for (int i = 0; i < getNumberOfChildren(); i++) {
            copied.addChild(getChild(i).copy());
        }
        return copied;
    }
}

