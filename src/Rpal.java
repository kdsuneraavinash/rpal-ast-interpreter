import cse.CseException;
import cse.ElementParser;
import cse.Machine;
import cse.Stack;
import cse.element.Value;
import tree.*;

import java.util.ArrayList;

public class Rpal {
    public static void main(String[] args) {
        try {
            if (args.length == 0)
                throw new Exception("File name must be provided as an command line argument");

            String fileName = args[0];
            Node root = TreeParser.nodeFromFile(fileName);
            Converters.astToSt(root);
            ArrayList<Stack<Value>> controls = ElementParser.generateControlStructures(root);
            Machine cseMachine = new Machine(controls);
            cseMachine.evaluate();
        } catch (AstException exception) {
            System.out.println("Error occurred while standardizing ast:");
            System.out.println(exception.getMessage());
        } catch (CseException exception) {
            System.out.println("Error occurred while evaluating cse:");
            System.out.println(exception.getMessage());
        } catch (RuntimeException exception) {
            System.out.println("Runtime Exception:");
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
