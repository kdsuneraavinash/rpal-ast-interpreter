package cse;

import cse.element.Value;
import tree.Node;

import java.util.ArrayList;

/**
 * Parser that will convert ast to Element stacks by preorder traversal.
 */
public class ElementParser {
    /**
     * Generates the control structure array by preorder traversal.
     *
     * @return Generated control structure array.
     */
    public static ArrayList<Stack<Value>> generateControlStructures(Node root) {
        ArrayList<Stack<Value>> controls = new ArrayList<>();
        Stack<Value> control = new Stack<>();
        controls.add(control);
        generateControlStructures(root, controls, control);
        return controls;
    }

    /**
     * Generates the control structure array by preorder traversal.
     *
     * @param node           Current traversing node
     * @param controls       Array with all control structures
     * @param currentControl Current traversing control structure
     */
    private static void generateControlStructures(Node node, ArrayList<Stack<Value>> controls,
                                                  Stack<Value> currentControl) {
        if (node.isLabel("lambda")) {
            generateCsForLambda(node, controls, currentControl);
        } else if (node.isLabel("->")) {
            generateCsForIf(node, controls, currentControl);
        } else if (node.isLabel("tau")) {
            generateCsForTau(node, controls, currentControl);
        } else {
            // Add this node and recurse on children
            currentControl.push(new Value(node));
            node.forEachChild(child -> generateControlStructures(child, controls, currentControl));
        }
    }

    /**
     * Split the control structure on lambda nodes and use a delta node to traverse in the sub tree.
     *
     * @param node           Current traversing node
     * @param controls       Array with all control structures
     * @param currentControl Current traversing control structure
     */
    private static void generateCsForLambda(Node node, ArrayList<Stack<Value>> controls,
                                            Stack<Value> currentControl) {
        // Get right and left children
        int newIndex = controls.size();
        Node leftChild = node.getChild(0);
        Node rightChild = node.getChild(1);

        if (leftChild.isLabel(",")) {
            ArrayList<String> children = new ArrayList<>();
            leftChild.forEachChild(child -> children.add(child.getValue()));
            String combinedParams = String.join(",", children);
            leftChild = new Node("id", combinedParams);
        }

        // Create the control element
        String controlValue = String.format("%s %s", newIndex, leftChild.getValue());
        Value newControlElem = new Value("lambda", controlValue);
        currentControl.push(newControlElem);

        // Create new control structure
        Stack<Value> newControl = new Stack<>();
        controls.add(newControl);

        // Traverse in new structure
        generateControlStructures(rightChild, controls, newControl);
    }

    /**
     * Split if node to then and else delta nodes and traverse in subtrees.
     *
     * @param node           Current traversing node
     * @param controls       Array with all control structures
     * @param currentControl Current traversing control structure
     */
    private static void generateCsForIf(Node node, ArrayList<Stack<Value>> controls,
                                        Stack<Value> currentControl) {
        Node conditionNode = node.getChild(0);
        Node thenNode = node.getChild(1);
        Node elseNode = node.getChild(2);

        // Then subtree
        int thenIndex = controls.size();
        Value thenElem = new Value("delta", Integer.toString(thenIndex));
        currentControl.push(thenElem);
        Stack<Value> thenControl = new Stack<>();
        controls.add(thenControl);
        generateControlStructures(thenNode, controls, thenControl);

        // Else subtree
        int elseIndex = controls.size();
        Value elseElem = new Value("delta", Integer.toString(elseIndex));
        currentControl.push(elseElem);
        Stack<Value> elseControl = new Stack<>();
        controls.add(elseControl);
        generateControlStructures(elseNode, controls, elseControl);

        currentControl.push(new Value("beta"));
        generateControlStructures(conditionNode, controls, currentControl);
    }

    /**
     * Add number of elements in tau node and traverse in each subtree.
     *
     * @param node           Current traversing node
     * @param controls       Array with all control structures
     * @param currentControl Current traversing control structure
     */
    private static void generateCsForTau(Node node, ArrayList<Stack<Value>> controls,
                                         Stack<Value> currentControl) {
        currentControl.push(new Value("tau", Integer.toString(node.getNumberOfChildren())));
        node.forEachChild(child -> generateControlStructures(child, controls, currentControl));
    }
}
