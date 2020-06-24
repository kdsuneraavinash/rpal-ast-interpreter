package tree;

/**
 * Node with the depth information.
 * Helper class to parse the node from a file/string.
 */
public class NodeWithDepth extends Node {
    private final int depth;

    /**
     * Creates intermediate node: let,where,etc...
     *
     * @param parent Parent node(null if root)
     * @param label  Type of node
     * @param depth  Depth number
     */
    NodeWithDepth(Node parent, String label, int depth) {
        super(label);
        this.depth = depth;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Creates leaf node: id,etc...
     *
     * @param parent Parent node(null if root)
     * @param label  Type of node
     * @param value  String form of the data in the node
     * @param depth  Depth number
     */
    NodeWithDepth(Node parent, String label, String value, int depth) {
        super(label, value);
        this.depth = depth;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * @return The depth of this node
     */
    int getDepth() {
        return depth;
    }

    @Override
    NodeWithDepth getParent() {
        return (NodeWithDepth) super.getParent();
    }
}