package cse.applicator;

import cse.CseException;
import cse.element.Element;
import cse.element.Tuple;
import cse.element.Value;

/**
 * Applies functions and operators.
 */
public class Applicator {
    static final String[] binaryOps = {"+", "-", "/", "*", "**", "eq", "ne", "gr", "ge", "le",
            ">", "<", ">=", "<=", "or", "&", "aug", "ls"};
    static final String[] unaryOps = {"Print", "Isstring", "Isinteger", "Istruthvalue", "Isfunction", "Null",
            "Istuple", "Order", "Stern", "Stem", "ItoS", "neg", "not", "$ConcPartial"};

    /**
     * Checks if binary operation is applicable
     */
    public boolean isBinaryOperation(Element op) {
        for (String binaryOp : binaryOps) {
            if (op.isLabel(binaryOp)) return true;
        }
        return false;
    }

    /**
     * Checks if un-ary operation is applicable
     */
    public boolean isUnaryOperation(Element op) {
        for (String unaryOp : unaryOps) {
            if (op.isLabel(unaryOp)) return true;
        }
        return false;
    }

    /**
     * Applies binary operators. VAL1 OP VAL2
     */
    public Element apply(Element operation, Element operand1, Element operand2) {
        switch (operation.getLabel()) {
            case "+":
                return add(operand1, operand2);
            case "-":
                return subtract(operand1, operand2);
            case "*":
                return multiply(operand1, operand2);
            case "**":
                return power(operand1, operand2);
            case "/":
                return divide(operand1, operand2);
            case "or":
                return or(operand1, operand2);
            case "&":
                return and(operand1, operand2);
            case "eq":
                return eq(operand1, operand2);
            case "ne":
                return ne(operand1, operand2);
            case ">":
            case "gr":
                return gr(operand1, operand2);
            case "<":
            case "ls":
                return ls(operand1, operand2);
            case ">=":
            case "ge":
                return ge(operand1, operand2);
            case "<=":
            case "le":
                return le(operand1, operand2);
            case "aug":
                return aug(operand1, operand2);
            default:
                throw new CseException("Unknown operator: " + operation);
        }
    }

    /**
     * Applies un-ary functions and operators. OP VAL
     */
    public Element apply(Element operation, Element operand) {
        switch (operation.getLabel()) {
            case "Print":
                return print(operand);
            case "Isstring":
                return isString(operand);
            case "Isinteger":
                return isInteger(operand);
            case "Istruthvalue":
                return isTruthValue(operand);
            case "Istuple":
                return isTuple(operand);
            case "Isfunction":
                return isFunction(operand);
            case "Order":
                return order(operand);
            case "Null":
                return isNull(operand);
            case "Stern":
                return stern(operand);
            case "Stem":
                return stem(operand);
            case "Conc":
                return conc(operand);
            case "$ConcPartial":
                return conc(operation, operand);
            case "ItoS":
                return iToS(operand);
            case "neg":
                return neg(operand);
            case "not":
                return not(operand);
            case "tuple":
                return extract((Tuple) operation, operand);
            default:
                throw new CseException("Unknown uop/variable: " + operation);
        }
    }

    /**
     * Numerical + - * /  ** operators. Allows a lambda expression for calculation.
     * Also checks for not int error.
     *
     * @param operand1  Left operand
     * @param operand2  Right operand
     * @param operation Lambda expression for calculation
     * @return Resultant element
     */
    private Element numericalOperator(Element operand1, Element operand2, NumericalOperator operation) {
        if (operand1 instanceof Value && operand2 instanceof Value) {
            Value element1 = (Value) operand1;
            Value element2 = (Value) operand2;
            if (element1.isLabel("int") && element2.isLabel("int")) {
                int value1 = Integer.parseInt(element1.getValue());
                int value2 = Integer.parseInt(element2.getValue());
                int result = operation.operation(value1, value2);
                return new Value("int", Integer.toString(result));
            }
        }
        throw new RuntimeException("Incompatible types in numerical operator. Expected int.");
    }

    /**
     * Boolean and or operators. Allows a lambda expression for calculation.
     * Also checks for not boolean error.
     *
     * @param operand1  Left operand
     * @param operand2  Right operand
     * @param operation Lambda expression for calculation
     * @return Resultant element
     */
    private Element binaryBooleanOperator(Element operand1, Element operand2, BinaryBooleanOperator operation) {
        if (isTruthValue(operand1).isLabel("true") && isTruthValue(operand2).isLabel("true")) {
            boolean element1 = operand1.isLabel("true");
            boolean element2 = operand2.isLabel("true");
            return booleanCondition(operation.operation(element1, element2));
        }
        throw new RuntimeException("Or operator applicable only for truth values");
    }

    /**
     * Converts element into string expression.
     *
     * @param element Element to stringify
     * @return Str element
     */
    private String covertToString(Element element) {
        if (element instanceof Tuple) {
            Element[] subElements = ((Tuple) element).getValue();
            String[] data = new String[subElements.length];
            for (int i = 0; i < subElements.length; i++) {
                data[i] = covertToString(subElements[i]);
            }
            return "(" + String.join(", ", data) + ")";
        } else if (element instanceof Value) {
            if (element.isLabel("lambda")) {
                String[] kAndXAndC = ((Value) element).getValue().split(" ");
                String k = kAndXAndC[0];
                String x = kAndXAndC[1];
                return "[lambda closure: " + x + ": " + k + "]";
            } else if (element.isLabel("str") || element.isLabel("int")) {
                return ((Value) element).getValue();
            } else {
                return element.getLabel();
            }
        } else {
            throw new CseException("Unknown element type.");
        }
    }

    /**
     * Function to convert boolean primitive into boolean element.
     *
     * @param condition Boolean primitive value
     * @return Boolean element
     */
    private Element booleanCondition(boolean condition) {
        if (condition) {
            return new Value("true");
        }
        return new Value("false");
    }

    /**
     * String stem stern operators. Allows a lambda expression for calculation.
     * Also checks for not string error.
     * If the operand is empty, returns empty string without calculation.
     *
     * @param operand   Operand value
     * @param operation Lambda expression for calculation
     * @return Resultant element
     */
    private Element substringOperation(Element operand, SubstringOperation operation) {
        if (operand instanceof Value && operand.isLabel("str")) {
            String string = ((Value) operand).getValue();
            if (string.isEmpty()) return new Value("str", "");
            String stern = operation.operation(string);
            return new Value("str", stern);
        }
        throw new RuntimeException("Substring operations are only applicable for strings");
    }


    /**
     * @return Integer addition; operand1 + operand2
     */
    private Element add(Element operand1, Element operand2) {
        return numericalOperator(operand1, operand2, Integer::sum);
    }

    /**
     * @return Integer subtraction; operand1 - operand2
     */
    private Element subtract(Element operand1, Element operand2) {
        return numericalOperator(operand1, operand2, (a, b) -> a - b);
    }

    /**
     * @return Integer multiplication; operand1 * operand2
     */
    private Element multiply(Element operand1, Element operand2) {
        return numericalOperator(operand1, operand2, (a, b) -> a * b);
    }

    /**
     * @return Integer raising to power; operand1 ^ operand2
     */
    private Element power(Element operand1, Element operand2) {
        return numericalOperator(operand1, operand2, (a, b) -> (int) Math.pow(a, b));
    }

    /**
     * @return Integer division; operand1 / operand2
     */
    private Element divide(Element operand1, Element operand2) {
        return numericalOperator(operand1, operand2, (a, b) -> a / b);
    }

    /**
     * @return Prints into standard output and returns Dummy value
     */
    private Element print(Element operand) {
        System.out.println(covertToString(operand));
        return new Value("dummy");
    }

    /**
     * @return Whether operand is a string.
     */
    private Element isString(Element operand) {
        return booleanCondition(operand.isLabel("str"));
    }

    /**
     * @return Whether operand is an int.
     */
    private Element isInteger(Element operand) {
        return booleanCondition(operand.isLabel("int"));
    }

    /**
     * @return Whether operand is a truth value(true/false).
     */
    private Element isTruthValue(Element operand) {
        return booleanCondition(operand.isLabel("true") || operand.isLabel("false"));
    }

    /**
     * @return Whether operand is a tuple.
     */
    private Element isTuple(Element operand) {
        return booleanCondition(operand.isLabel("tuple"));
    }

    /**
     * @return Whether operand is a function/lambda node.
     */
    private Element isFunction(Element operand) {
        return booleanCondition(operand.isLabel("lambda"));
    }

    /**
     * @return Number of elements in the tuple operand; len(operand)
     */
    private Element order(Element operand) {
        if (operand instanceof Tuple) {
            int elements = ((Tuple) operand).getValue().length;
            return new Value("int", Integer.toString(elements));
        }
        throw new RuntimeException("Order operation is only applicable for tuples");
    }

    /**
     * @return true if tuple is nil, false otherwise
     */
    private Element isNull(Element operand) {
        return  booleanCondition(operand.isLabel("nil"));
    }

    /**
     * @return All except first character in string operand; operand[1:]
     */
    private Element stern(Element operand) {
        return substringOperation(operand, (str) -> str.substring(1));
    }

    /**
     * @return First character in string operand; operand[0]
     */
    private Element stem(Element operand) {
        return substringOperation(operand, (str) -> str.substring(0, 1));
    }

    /**
     * @return Partially applies string concatenation; (operand2) -> operand + operand2
     */
    private Element conc(Element operand) {
        if (operand instanceof Value && operand.isLabel("str")) {
            return new Value("$ConcPartial", ((Value) operand).getValue());
        }
        throw new RuntimeException("Conc operation is only applicable for strings");
    }

    /**
     * @return Applies operand2 to string concatenation and returns result string; operand1 + operand.
     */
    private Element conc(Element operator, Element operand2) {
        if (operator instanceof Value && operand2 instanceof Value) {
            if (operator.isLabel("$ConcPartial") && operand2.isLabel("str")) {
                String string = ((Value) operator).getValue() + ((Value) operand2).getValue();
                return new Value("str", string);
            }
        }
        throw new RuntimeException("Invalid application of Conc");
    }

    /**
     * @return Converted integer; int(a)
     */
    private Element iToS(Element operand) {
        if (operand instanceof Value && operand.isLabel("int")) {
            String value = ((Value) operand).getValue();
            return new Value("str", value);
        }
        throw new RuntimeException("iToS operation is only applicable for strings");
    }

    /**
     * @return Numerical negation; -operand
     */
    private Element neg(Element operand) {
        return multiply(new Value("int", "-1"), operand);
    }

    /**
     * @return Boolean not operator; !operand
     */
    private Element not(Element operand) {
        if (isTruthValue(operand).isLabel("true")) {
            return booleanCondition(operand.isLabel("false"));
        }
        throw new RuntimeException("Not operator applicable only for truth values");
    }

    /**
     * @return Boolean or operator; operand1 || operand2
     */
    private Element or(Element operand1, Element operand2) {
        return binaryBooleanOperator(operand1, operand2, (a, b) -> a || b);
    }

    /**
     * @return Boolean and operator; operand1 && operand2
     */
    private Element and(Element operand1, Element operand2) {
        return binaryBooleanOperator(operand1, operand2, (a, b) -> a && b);
    }

    /**
     * @return Checks if two elements are similar; operand1 == operand2
     */
    private Element eq(Element operand1, Element operand2) {
        return booleanCondition(operand1.equals(operand2));
    }

    /**
     * @return Checks if two elements are not similar; operand1 != operand2
     */
    private Element ne(Element operand1, Element operand2) {
        return not(eq(operand1, operand2));
    }

    /**
     * Compares 2 elements.
     * If int - compares numerical value
     * If str - compares lexical value
     * Otherwise - throws error
     *
     * @return Comparison result; operand1 > operand2
     */
    private Element gr(Element operand1, Element operand2) {
        if (operand1 instanceof Value && operand2 instanceof Value) {
            if (operand1.isLabel("int") && operand2.isLabel("int")) {
                int value1 = Integer.parseInt(((Value) operand1).getValue());
                int value2 = Integer.parseInt(((Value) operand2).getValue());
                boolean condition = value1 > value2;
                return booleanCondition(condition);
            } else if (operand1.isLabel("str") && operand2.isLabel("str")) {
                String value1 = ((Value) operand1).getValue();
                String value2 = ((Value) operand2).getValue();
                boolean condition = value1.compareTo(value2) > 0;
                return booleanCondition(condition);
            }
        }
        throw new RuntimeException("Incompatible types for comparison operator.");
    }

    /**
     * @return Comparison result; operand1 < operand2
     */
    private Element ls(Element operand1, Element operand2) {
        return not(gr(operand1, operand2));
    }

    /**
     * @return Comparison result; operand1 >= operand2
     */
    private Element ge(Element operand1, Element operand2) {
        return or(gr(operand1, operand2), eq(operand1, operand2));
    }

    /**
     * @return Comparison result; operand1 <= operand2
     */
    private Element le(Element operand1, Element operand2) {
        return or(ls(operand1, operand2), eq(operand1, operand2));
    }

    /**
     * @return Appends element to a tuple; operand1.append(operand2)
     */
    private Element aug(Element operand1, Element operand2) {
        if (operand1.isLabel("nil")) {
            operand1 = new Tuple(new Element[]{});
        }
        if (operand1 instanceof Tuple) {
            Element[] op1Tuple = ((Tuple) operand1).getValue();
            Element[] combined = new Element[op1Tuple.length + 1];
            System.arraycopy(op1Tuple, 0, combined, 0, op1Tuple.length);
            combined[op1Tuple.length] = operand2;
            return new Tuple(combined);
        }
        throw new RuntimeException("Aug operator is only compatible for tuples.");
    }

    /**
     * @return ith element in the tuple(1 indexed); operation[operand]
     */
    private Element extract(Tuple operation, Element operand) {
        if (operand instanceof Value && operand.isLabel("int")) {
            int index = Integer.parseInt(((Value) operand).getValue());
            return operation.getValue()[index - 1];
        }
        throw new RuntimeException("Tuple index must be an integer.");
    }
}
