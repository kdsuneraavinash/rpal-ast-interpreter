package tree;

/**
 * Helper class to convert ast to st.
 */
public class Converters {
    /**
     * Converts ast to st.
     * <p>
     * Not standardizing uop/op Nodes <br/>
     * Not standardizing -> Nodes <br/>
     * Not standardizing tau Nodes <br/>
     * Not standardizing , Nodes
     *
     * @param node Node of the subtree to standardize.
     */
    public static void astToSt(Node node) {
        // Ast -> st conversion
        node.forEachChild(Converters::astToSt);

        if (node.isLabel("let")) {
            stForLet(node);
        } else if (node.isLabel("where")) {
            stForWhere(node);
        } else if (node.isLabel("function_form")) {
            stForFuncForm(node);
        } else if (node.isLabel("and")) {
            stForAnd(node);
        } else if (node.isLabel("rec")) {
            stForRec(node);
        } else if (node.isLabel("lambda")) {
            stForLambda(node);
        } else if (node.isLabel("within")) {
            stForWithin(node);
        } else if (node.isLabel("@")) {
            stForAt(node);
        }
    }

    /**
     * Standardize the LET node
     * <pre>
     *   let                gamma
     *  /   \                /    \
     *  =    P	=>       lambda    E
     * /\                 /  \
     * X  E               X   P
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForLet(Node rootNode) {
        expectLabel(rootNode, "let");
        expectChildren(rootNode, 2);
        Node eqNode = rootNode.getChild(0);
        Node pNode = rootNode.getChild(1);

        expectLabel(eqNode, "=");
        expectChildren(eqNode, 2);
        Node xNode = eqNode.getChild(0);
        Node eNode = eqNode.getChild(1);

        // Reorganize tree
        rootNode.setLabel("gamma");
        eqNode.setLabel("lambda");
        rootNode.clearChildren();
        rootNode.addChild(eqNode);
        rootNode.addChild(eNode);
        eqNode.clearChildren();
        eqNode.addChild(xNode);
        eqNode.addChild(pNode);
    }

    /**
     * Standardize the WHERE node
     * <pre>
     * where            gamma
     *  / \               /  \
     *  P  =    =>    lambda  E
     *     /\          /  \
     *    X E        X    P
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForWhere(Node rootNode) {
        expectLabel(rootNode, "where");
        expectChildren(rootNode, 2);
        Node pNode = rootNode.getChild(0);
        Node eqNode = rootNode.getChild(1);

        expectLabel(eqNode, "=");
        expectChildren(eqNode, 2);
        Node xNode = eqNode.getChild(0);
        Node eNode = eqNode.getChild(1);

        // Reorganize tree
        rootNode.setLabel("gamma");
        eqNode.setLabel("lambda");

        rootNode.clearChildren();
        rootNode.addChild(eqNode);
        rootNode.addChild(eNode);
        eqNode.clearChildren();
        eqNode.addChild(xNode);
        eqNode.addChild(pNode);
    }

    /**
     * Standardize the FCN_FORM node
     * <pre>
     * FCN_FORM               =
     * /  |   \              /  \
     * p  V+   E       =>   P   +lambda
     *                          /  \
     *                         V   .E
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForFuncForm(Node rootNode) {
        expectLabel(rootNode, "function_form");
        expectMoreChildren(rootNode, 3);

        int numberOfVNodes = rootNode.getNumberOfChildren() - 2;
        Node pNode = rootNode.getChild(0);
        Node eNode = rootNode.getChild(numberOfVNodes + 1);
        Node[] vNodes = new Node[numberOfVNodes];
        for (int i = 0; i < numberOfVNodes; i++) {
            vNodes[i] = rootNode.getChild(i + 1);
        }

        // Reorganize tree
        rootNode.setLabel("=");
        rootNode.clearChildren();
        rootNode.addChild(pNode);
        Node prevNode = rootNode;
        for (int i = 0; i < numberOfVNodes; i++) {
            Node currentNode = new Node("lambda");
            prevNode.addChild(currentNode);
            currentNode.addChild(vNodes[i]);
            prevNode = currentNode;
        }
        prevNode.addChild(eNode);
    }

    /**
     * Standardize the AND node
     * <pre>
     *   AND          =
     *    |          /  \
     *   =++  =>    ,  TAU
     *   / \        |   |
     *  X   E       X++  E++
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForAnd(Node rootNode) {
        expectLabel(rootNode, "and");
        expectMoreChildren(rootNode, 2);

        int numberOfEqNodes = rootNode.getNumberOfChildren();
        Node[] eqNodes = new Node[numberOfEqNodes];
        for (int i = 0; i < numberOfEqNodes; i++) {
            eqNodes[i] = rootNode.getChild(i);
            expectLabel(eqNodes[i], "=");
            expectChildren(eqNodes[i], 2);
        }

        // Reorganize tree
        rootNode.setLabel("=");
        rootNode.clearChildren();
        Node commaNode = new Node(",");
        Node tauNode = new Node("tau");
        rootNode.addChild(commaNode);
        rootNode.addChild(tauNode);
        for (int i = 0; i < numberOfEqNodes; i++) {
            Node xNode = eqNodes[i].getChild(0);
            Node eNode = eqNodes[i].getChild(1);
            commaNode.addChild(xNode);
            tauNode.addChild(eNode);
        }
    }

    /**
     * Standardize the REC node
     * <pre>
     *  REC                   =
     *   |                   /  \
     *   =        =>       X  gamma
     *  / \                   /     \
     *  X  E                yStar  lambda
     *                              /  \
     *                             X    E
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForRec(Node rootNode) {
        expectLabel(rootNode, "rec");
        expectChildren(rootNode, 1);
        Node eqNode = rootNode.getChild(0);

        expectLabel(eqNode, "=");
        expectChildren(eqNode, 2);
        Node xNode = eqNode.getChild(0);
        Node eNode = eqNode.getChild(1);

        // Reorganize tree
        Node secondXNode = xNode.copy();
        rootNode.setLabel("=");
        rootNode.clearChildren();
        Node gammaNode = new Node("gamma");
        Node yStarNode = new Node("yStar");
        Node lambdaNode = new Node("lambda");
        rootNode.addChild(xNode);
        rootNode.addChild(gammaNode);
        gammaNode.addChild(yStarNode);
        gammaNode.addChild(lambdaNode);
        lambdaNode.addChild(secondXNode);
        lambdaNode.addChild(eNode);
    }

    /**
     * Standardize the Multi-func param (lambda) node
     * <pre>
     * lambda           ++lambda
     * /   \              /  \
     * V++   E    =>      ++V   .E
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForLambda(Node rootNode) {
        expectLabel(rootNode, "lambda");
        expectMoreChildren(rootNode, 2);

        int numberOfVNodes = rootNode.getNumberOfChildren() - 1;
        Node[] vNodes = new Node[numberOfVNodes];
        Node eNode = rootNode.getChild(numberOfVNodes);
        for (int i = 0; i < numberOfVNodes; i++) {
            vNodes[i] = rootNode.getChild(i);
        }

        // Reorganize tree
        Node currentLambdaNode = rootNode;
        currentLambdaNode.clearChildren();
        currentLambdaNode.addChild(vNodes[0]);
        for (int i = 1; i < numberOfVNodes; i++) {
            Node newLambdaNode = new Node("lambda");
            currentLambdaNode.addChild(newLambdaNode);
            newLambdaNode.addChild(vNodes[i]);
            currentLambdaNode = newLambdaNode;
        }
        currentLambdaNode.addChild(eNode);
    }


    /**
     * Standardize the WITHIN node
     * <pre>
     *   WITHIN            =
     *   /    \          /  \
     *  =     =  =>     X2 gamma
     * / \   / \          /   \
     * X1 E1 X2 E2      lambda  E1
     *                  /  \
     *                 X1  E2
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForWithin(Node rootNode) {
        expectLabel(rootNode, "within");
        expectChildren(rootNode, 2);
        Node eq1Node = rootNode.getChild(0);
        Node eq2Node = rootNode.getChild(1);

        expectLabel(eq1Node, "=");
        expectChildren(eq1Node, 2);
        expectLabel(eq2Node, "=");
        expectChildren(eq2Node, 2);
        Node x1Node = eq1Node.getChild(0);
        Node e1Node = eq1Node.getChild(1);
        Node x2Node = eq2Node.getChild(0);
        Node e2Node = eq2Node.getChild(1);

        // Reorganize tree
        Node gammaNode = new Node("gamma");
        Node lambdaNode = new Node("lambda");
        rootNode.setLabel("=");
        rootNode.clearChildren();
        rootNode.addChild(x2Node);
        rootNode.addChild(gammaNode);
        gammaNode.addChild(lambdaNode);
        gammaNode.addChild(e1Node);
        lambdaNode.addChild(x1Node);
        lambdaNode.addChild(e2Node);
    }

    /**
     * Standardize the @ node
     * <pre>
     *     @             gamma
     *   / | \           /  \
     *  E1 N E2  =>  gamma  E2
     *               /  \
     *              N   E1
     * </pre>
     *
     * @param rootNode Root Node
     **/
    private static void stForAt(Node rootNode) {
        expectLabel(rootNode, "@");
        expectChildren(rootNode, 3);
        Node e1Node = rootNode.getChild(0);
        Node nNode = rootNode.getChild(1);
        Node e2Node = rootNode.getChild(2);

        // Reorganize tree
        rootNode.clearChildren();
        rootNode.setLabel("gamma");
        Node gammaNode = new Node("gamma");
        rootNode.addChild(gammaNode);
        rootNode.addChild(e2Node);
        gammaNode.addChild(nNode);
        gammaNode.addChild(e1Node);
    }

    /**
     * Error throwing function to check for the given number of children
     *
     * @param node   Node to check
     * @param expect Expected number of children
     */
    private static void expectChildren(Node node, int expect) {
        if (!node.hasChildren(expect)) {
            String errorMessage = String.format("Expected %s node to have %s nodes", node.getLabel(), expect);
            throw new AstException(errorMessage);
        }
    }

    /**
     * Error throwing function to check for more than a given number of children
     *
     * @param node    Node to check
     * @param minimum Minimum expected number of children
     */
    private static void expectMoreChildren(Node node, int minimum) {
        if (node.getNumberOfChildren() < minimum) {
            String errorMessage = String.format("Expected %s node to have at least %s nodes", node.getLabel(), minimum);
            throw new AstException(errorMessage);
        }
    }

    /**
     * Error throwing function to check for the given label.
     *
     * @param node   Node to check
     * @param expect Expected type
     */
    private static void expectLabel(Node node, String expect) {
        if (!node.isLabel(expect)) {
            String errorMessage = String.format("Expected %s node but found %s node", expect, node.getLabel());
            throw new AstException(errorMessage);
        }
    }
}
