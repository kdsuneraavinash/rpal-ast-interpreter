package tree;

/**
 * Exception when standardizing the abstract syntax tree.
 */
public class AstException extends RuntimeException {
    AstException(String message) {
        super(message);
    }
}
