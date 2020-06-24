package cse;

import cse.element.Element;

import java.util.Iterator;

/**
 * Stack used to store elements.
 * Implements iterable to enable use of for each loop.
 *
 * @param <T> Element type (Value or Element)
 */
public class Stack<T extends Element> implements Iterable<T> {
    protected final java.util.Stack<T> stack;

    Stack() {
        stack = new java.util.Stack<>();
    }

    /**
     * Push element into stack
     */
    void push(T element) {
        stack.push(element);
    }

    /**
     * Pop last element
     */
    T pop() {
        return stack.pop();
    }

    /**
     * Whether the stack is empty
     */
    boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Number of elements in the stack
     */
    int size() {
        return stack.size();
    }

    @Override
    public String toString() {
        return stack.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return stack.iterator();
    }
}
