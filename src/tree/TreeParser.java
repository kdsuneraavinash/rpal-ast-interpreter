package tree;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper class to parse tree from string.
 */
public class TreeParser {
    /**
     * Parse node from the given file
     *
     * @param fileName Filename/path as a string
     * @return Parsed tree root node
     * @throws FileNotFoundException If file did not exist
     */
    public static Node nodeFromFile(String fileName) throws FileNotFoundException {
        List<String> lines = readFile(fileName);
        return nodeFromString(lines);
    }

    /**
     * Parse node from the given list of strings
     *
     * @param lines String array to parse tree
     * @return Parsed tree root node
     */
    public static Node nodeFromString(List<String> lines) {
        NodeWithDepth root = null;
        NodeWithDepth parent = null;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String data = trimLeadingDots(line);
            int currentDepth = line.length() - data.length();
            if (currentDepth == 0 && root != null) break;
            while (parent != null && parent.getDepth() >= currentDepth) {
                parent = parent.getParent();
            }

            NodeWithDepth node;
            if (data.startsWith("<") && data.endsWith(">")) {
                // ID/Value nodes
                String label, value;
                if (data.contains(":")) {
                    // Str, Int, ... value nodes
                    int borderPos = data.indexOf(':');
                    label = data.substring(1, borderPos).toLowerCase();
                    if (label.equals("str")) {
                        // Str nodes: Remove quotations
                        value = data.substring(borderPos + 2, data.length() - 2);
                        // Evaluate string with \n,\t unescaped.
                        value = unescapeJavaString(value);
                    } else {
                        // Int nodes
                        value = data.substring(borderPos + 1, data.length() - 1);
                    }
                } else {
                    // Truth, value nodes
                    label = data.substring(1, data.length() - 1).toLowerCase();
                    value = null;
                }
                node = new NodeWithDepth(parent, label, value, currentDepth);
            } else {
                // Other nodes
                node = new NodeWithDepth(parent, data, currentDepth);
            }
            if (parent == null) {
                root = node;
            }
            parent = node;
        }
        return root;
    }

    /**
     * Reads the given file.
     *
     * @param fileName Filename/path as a string
     * @return String array of lines
     * @throws FileNotFoundException If file did not exist
     */
    private static List<String> readFile(String fileName) throws FileNotFoundException {
        List<String> lines = new LinkedList<>();
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String data = scanner.nextLine();
            lines.add(data);
        }
        scanner.close();
        return lines;
    }

    /**
     * Trims leading '.'s from a line.
     *
     * @param source Initial string
     * @return Trimmed string
     */
    private static String trimLeadingDots(String source) {
        for (int i = 0; i < source.length(); ++i) {
            char c = source.charAt(i);
            if (c != '.') {
                return source.substring(i);
            }
        }
        return "";
    }

    /**
     * Snippet Taken From:
     * https://stackoverflow.com/questions/3537706/how-to-unescape-a-java-string-literal-in-java?answertab=votes#tab-top
     * <p>
     * Unescape a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
     * </ul>
     *
     * @param st A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    private static String unescapeJavaString(String st) {
        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
