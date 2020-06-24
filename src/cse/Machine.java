package cse;

import cse.applicator.Applicator;
import cse.element.Element;
import cse.element.Tuple;
import cse.element.Value;

import java.util.ArrayList;

/**
 * CSE machine to evaluate the traversed tree
 */
public class Machine {
    private final Stack<Value> control;
    private final Stack<Element> stack;
    private final Applicator applicator;
    private final ArrayList<Environment> environments;
    private final ArrayList<Stack<Value>> controlStructures;

    public Machine(ArrayList<Stack<Value>> controlStructures) {
        this.controlStructures = controlStructures;
        this.stack = new Stack<>();
        this.applicator = new Applicator();

        control = new Stack<>();
        control.push(new Value("environment", "0"));
        control.push(new Value("delta", "0"));
        stack.push(new Value("environment", "0"));
        environments = new ArrayList<>();
        environments.add(new Environment());
    }

    @Override
    public String toString() {
        return control + "\n" + stack + "\n" + currentEnvironment() + "\n";
    }

    /**
     * Get current environment index by peeking the stack and fining the closest environment element.
     *
     * @return Current active environment index
     */
    private int currentEnvironmentIndex() {
        int closestEnvironment = 0;
        for (Element element : control) {
            if (element instanceof Value && element.isLabel("environment")) {
                String closestEnvironmentStr = ((Value) element).getValue();
                closestEnvironment = Integer.parseInt(closestEnvironmentStr);
            }
        }
        return closestEnvironment;
    }

    /**
     * Get current environment
     */
    private Environment currentEnvironment() {
        return environments.get(currentEnvironmentIndex());
    }

    /**
     * Start processing the control stack to evaluate result.
     */
    public void evaluate() {
        while (!control.isEmpty()) {
            Value currentElement = control.pop();

            if (currentElement.isLabel("gamma")) {
                Element firstElem = stack.pop();
                Element secondElem = stack.pop();
                if (firstElem.isLabel("yStar")) {
                    cseRule12(secondElem);
                } else if (firstElem.isLabel("eta")) {
                    stack.push(secondElem);
                    cseRule13(currentElement, firstElem);
                } else if (firstElem.isLabel("lambda")) {
                    Value firstValue = (Value) firstElem;
                    if (firstValue.getValue().contains(",")) {
                        cseRule11(firstElem, secondElem);
                    } else {
                        cseRule4(firstElem, secondElem);
                    }
                } else if (firstElem.isLabel("tau")) {
                    cseRule10(firstElem, secondElem);
                } else {
                    cseRule3(firstElem, secondElem);
                }
            } else if (currentElement.isLabel("delta")) {
                int controlIndex = Integer.parseInt(currentElement.getValue());
                extractDelta(controlIndex);
            } else if (currentElement.isLabel("id")) {
                cseRule1(currentElement);
            } else if (currentElement.isLabel("lambda")) {
                cseRule2(currentElement);
            } else if (currentElement.isLabel("environment")) {
                cseRule5(currentElement);
            } else if (currentElement.isLabel("beta")) {
                cseRule8();
            } else if (currentElement.isLabel("tau")) {
                cseRule9(currentElement);
            } else if (!cseRule6And7(currentElement)) {
                stack.push(currentElement);
            }
            // System.out.println(this);
        }
    }

    /**
     * Extract elements from the control structure specified.
     *
     * @param controlIndex Index of control structure to extract
     */
    private void extractDelta(int controlIndex) {
        Stack<Value> control = controlStructures.get(controlIndex);
        for (Value controlElem : control) {
            this.control.push(controlElem);
        }
    }

    /**
     * <pre>
     * ... Name                     ...
     * ...                      Ob  ...
     * Ob = Lookup(Name, ec)
     * </pre>
     *
     * @param name Current element
     */
    private void cseRule1(Value name) {
        String id = name.getValue();
        Element value = currentEnvironment().lookup(id);
        if (value == null) {
            value = new Value(id);
        }
        stack.push(value);
    }

    /**
     * <pre>
     * ... lambda(k, x)             ...
     * ...         lambda(k, x, c)  ...
     * env(c) = current environment
     * </pre>
     *
     * @param lambda Current element
     */
    private void cseRule2(Value lambda) {
        String[] kAndX = lambda.getValue().split(" ");
        String c = Integer.toString(currentEnvironmentIndex());
        String[] newValues = {kAndX[0], kAndX[1], c};
        Element newLambda = new Value("lambda", String.join(" ", newValues));
        stack.push(newLambda);
    }

    /**
     * <pre>
     * ... gamma         rator rand ...
     * ...                  result  ...
     * result = apply(rator, rand)
     * </pre>
     *
     * @param rator first argument
     * @param rand  second argument
     */
    private void cseRule3(Element rator, Element rand) {
        Element result = applicator.apply(rator, rand);
        stack.push(result);
    }

    /**
     * <pre>
     * ... gamma            lambda(k, x, c) rand ...
     * ... e(n) delta(k)                   e(n)  ...
     * e(n) = [Rand/x]e(c)
     * </pre>
     *
     * @param lambda lambda(k, x, c) argument
     * @param rand   second argument
     */
    private void cseRule4(Element lambda, Element rand) {
        if (lambda instanceof Value && lambda.isLabel("lambda")) {
            String[] kAndXAndC = ((Value) lambda).getValue().split(" ");
            String k = kAndXAndC[0];
            String x = kAndXAndC[1];
            String c = kAndXAndC[2];
            Environment envC = environments.get(Integer.parseInt(c));

            Environment newEnvironment = new Environment(envC, x, rand);
            String newEnvIndex = Integer.toString(environments.size());
            environments.add(newEnvironment);
            control.push(new Value("environment", newEnvIndex));
            control.push(new Value("delta", k));
            stack.push(new Value("environment", newEnvIndex));
            return;
        }
        throw new CseException("Expected lambda element but found: " + lambda);
    }


    /**
     * <pre>
     * ... e(n)            value e(n) ...
     * ...                     value  ...
     * </pre>
     */
    private void cseRule5(Value env) {
        Element value = stack.pop();
        Element envS = stack.pop();
        if (envS instanceof Value && envS.isLabel("environment")) {
            if (env.equals(envS)) {
                stack.push(value);
                return;
            }
            throw new CseException(String.format("Environment element mismatch: %s and %s", env, envS));
        }
        throw new CseException("Expected environment element but found: " + envS);
    }

    /**
     * @param element Operation element
     * @return true if element was either bin-op or un-op
     */
    private boolean cseRule6And7(Value element) {
        if (applicator.isBinaryOperation(element)) {
            Element rator = stack.pop();
            Element rand = stack.pop();
            Element result = applicator.apply(element, rator, rand);
            stack.push(result);
        } else if (applicator.isUnaryOperation(element)) {
            Element rand = stack.pop();
            Element result = applicator.apply(element, rand);
            stack.push(result);
        } else {
            return false;
        }
        return true;
    }

    /**
     * <pre>
     * ... delta(then) delta(else) beta           truth ...
     * ... delta()                                      ...
     * </pre>
     */
    private void cseRule8() {
        Value deltaElse = control.pop();
        Value deltaThen = control.pop();
        Element condition = stack.pop();

        if (deltaElse.isLabel("delta") && deltaThen.isLabel("delta")) {
            if (condition.isLabel("true")) {
                control.push(deltaThen);
                return;
            } else if (condition.isLabel("false")) {
                control.push(deltaElse);
                return;
            }
            throw new RuntimeException("If condition must evaluate to a truth value.");
        }
        throw new CseException("Expected delta elements.");
    }

    /**
     * <pre>
     * ... tau(n)                 V1 .. Vn ...
     * ...                      (V1 .. Vn) ...
     * </pre>
     *
     * @param tau Tau element
     */
    private void cseRule9(Value tau) {
        int elements = Integer.parseInt(tau.getValue());
        Element[] tupleElements = new Element[elements];
        for (int i = 0; i < elements; i++) {
            tupleElements[i] = stack.pop();
        }
        Element tuple = new Tuple(tupleElements);
        stack.push(tuple);
    }

    /**
     * <pre>
     * ... gamma                 (V1 .. Vn) I ...
     * ...                                 Vi ...
     * </pre>
     *
     * @param tuple tuple element
     * @param index index to extract
     */
    private void cseRule10(Element tuple, Element index) {
        if (tuple instanceof Tuple) {
            if (index instanceof Value && index.isLabel("int")) {
                int ind = Integer.parseInt(((Value) index).getValue());
                Element value = ((Tuple) tuple).getValue()[ind];
                stack.push(value);
                return;
            }
            throw new CseException("Expected integer index but found: " + index);
        }
        throw new CseException("Expected tuple but found: " + tuple);
    }

    /**
     * <pre>
     * ... gamma            lambda(k, v1 vn, c) rand ...
     * ... e(n) delta(k)                       e(n)  ...
     * e(n) = [Rand1/v1][Rand2/v2]e(c)
     * </pre>
     *
     * @param lambda lambda(k, v1,v2, c) argument
     * @param rand   second argument
     */
    private void cseRule11(Element lambda, Element rand) {
        if (lambda instanceof Value && lambda.isLabel("lambda")) {
            if (rand instanceof Tuple) {
                String[] kAndVAndC = ((Value) lambda).getValue().split(" ");
                String k = kAndVAndC[0];
                String[] v = kAndVAndC[1].split(",");
                String c = kAndVAndC[2];
                Environment envC = environments.get(Integer.parseInt(c));

                Environment newEnvironment = new Environment(envC);
                for (int i = 0; i < v.length; i++) {
                    newEnvironment.remember(v[i], ((Tuple) rand).getValue()[i]);
                }
                String newEnvIndex = Integer.toString(environments.size());
                environments.add(newEnvironment);
                control.push(new Value("environment", newEnvIndex));
                control.push(new Value("delta", k));
                stack.push(new Value("environment", newEnvIndex));
                return;
            }
            throw new CseException("Expected tuple but found: " + rand);
        }
        throw new CseException("Expected lambda element but found: " + lambda);
    }

    /**
     * <pre>
     * ... gamma            Y lambda(i, v, c) ...
     * ...                      eta(i, v, c) ...
     * </pre>
     *
     * @param lambda lambda(i, v, c) element
     */
    private void cseRule12(Element lambda) {
        if (lambda instanceof Value && lambda.isLabel("lambda")) {
            String iAndVAndC = ((Value) lambda).getValue();
            Element etaElement = new Value("eta", iAndVAndC);
            stack.push(etaElement);
            return;
        }
        throw new CseException("Expected lambda element but found: " + lambda);
    }

    /**
     * <pre>
     * ... gamma                             eta(i, v, c) R ...
     * ... gamma gamma       lambda(i, v, c) eta(i, v, c) R ...
     * </pre>
     *
     * @param gamma Gamma element
     */
    private void cseRule13(Value gamma, Element eta) {
        if (eta instanceof Value && eta.isLabel("eta")) {
            String iAndVAndC = ((Value) eta).getValue();
            Value lambda = new Value("lambda", iAndVAndC);
            Value newGamma = new Value("gamma");

            stack.push(eta);
            stack.push(lambda);

            control.push(gamma);
            control.push(newGamma);
            return;
        }
        throw new CseException("Expected eta element but found: " + eta);
    }
}
