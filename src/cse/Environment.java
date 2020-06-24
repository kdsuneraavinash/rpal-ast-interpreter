package cse;


import cse.element.Element;

import java.util.HashMap;

/**
 * Environment which will keep entries on names and their values.
 */
public class Environment {
    private Environment parent;
    private final HashMap<String, Element> memory;

    /**
     * Create primary environment.
     */
    Environment() {
        this.memory = new HashMap<>();
        remember("Print", null);
        remember("Isstring", null);
        remember("Isinteger", null);
        remember("Istruthvalue", null);
        remember("Istuple", null);
        remember("Isfunction", null);
        remember("Null", null);
        remember("Order", null);
        remember("Stern", null);
        remember("Stem", null);
        remember("ItoS", null);
        remember("neg", null);
        remember("not", null);
        remember("Conc", null);
    }

    /**
     * Create empty sub environment.
     *
     * @param parent Parent environment
     */
    Environment(Environment parent) {
        this.memory = new HashMap<>();
        this.parent = parent;
    }

    /**
     * Create sub environment with one entry.
     *
     * @param parent Parent environment
     * @param key    Key/name of variable
     * @param value  Value of variable
     */
    Environment(Environment parent, String key, Element value) {
        this.memory = new HashMap<>();
        this.parent = parent;
        remember(key, value);
    }

    /**
     * Remember an entry. Error if already defined.
     *
     * @param key   Key/name of variable
     * @param value Value of variable
     */
    void remember(String key, Element value) {
        if (memory.containsKey(key)) {
            throw new RuntimeException("Variable is already defined: " + key);
        }
        memory.put(key, value);
    }

    /**
     * Get the value of a variable.
     * Returns null if defined in primary environment.
     * Throws error if undefined.
     *
     * @param id Name of the variable
     * @return Value of the variable.
     */
    Element lookup(String id) {
        if (memory.containsKey(id)) {
            return memory.get(id);
        }
        if (parent == null) {
            // Primary Environment and not found
            throw new RuntimeException("Undefined variable: " + id);
        }
        return parent.lookup(id);
    }

    @Override
    public String toString() {
        if (parent != null) {
            String[] data = new String[memory.size()];
            int i = 0;
            for (String key : memory.keySet()) {
                data[i++] = "[" + memory.get(key) + "/" + key + "]";
            }
            return parent + " > " + String.join("", data);
        }
        return "PE";
    }
}
