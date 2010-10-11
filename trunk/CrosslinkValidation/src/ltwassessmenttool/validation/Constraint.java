package ltwassessmenttool.validation;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Darren Huang
 */
public class Constraint {

    /** The identifier for this constraint */
    private String identifier;
    /** Minimum inclusive value allowed */
    private int minInclusive;
    /** Maximum inclusive value allowed */
    private int maxInclusive;
    // =========================================================
    private Vector<String> elements = new Vector<String>();
    private Vector<String> attributes = new Vector<String>();
    // key: element or attribute name|ref
    // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
    private Hashtable<String, Vector<String>> conditions = new Hashtable<String, Vector<String>>();

    /**
     * <p>
     *  This will create a new <code>Constraints</code> with the specified
     *    identifier as the "name".
     * </p>
     *
     * @param identifier <code>String</code> identifier for <code>Constraint</code>.
     */
    public Constraint(String identifier) {
        this.identifier = identifier;
    }

    /**
     * <p>
     *  This will return the identifier for this <code>Constraint</code>.
     * </p>
     *
     * @return <code>String</code> - identifier for this constraint.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * <p>
     *  This will allow elements needed to be included in the main Element to be set.
     *    The type is specified as a Java <code>Vector<String></code>.
     * </p>
     *
     * @param dataType <code>Vector<String></code> that is the Java data type for this constraint.
     */
    public void setElements(Vector<String> elements) {
        this.elements = elements;
    }

    /**
     * <p>
     *  This will return the <code>Vector<String></code> version of the Java data type for this
     *    constraint.
     * </p>
     *
     * @return <code>Vector<String></code> - the data type for this constraint.
     */
    public Vector<String> getElements() {
        return elements;
    }

    /**
     * <p>
     *  This will allow attributes needed to be included in the main Element to be set.
     *    The type is specified as a Java <code>Vector<String></code>.
     * </p>
     *
     * @param dataType <code>Vector<String></code> that is the Java data type for this constraint.
     */
    public void setAttributes(Vector<String> attributes) {
        this.attributes = attributes;
    }

    /**
     * <p>
     *  This will return the <code>Vector<String></code> version of the Java data type for this
     *    constraint.
     * </p>
     *
     * @return <code>Vector<String></code> - the data type for this constraint.
     */
    public Vector<String> getAttributes() {
        return attributes;
    }

    /**
     * <p>
     *  This will allow attributes needed to be included in the main Element to be set.
     *    The type is specified as a Java <code>Vector<String></code>.
     * </p>
     *
     * @param dataType <code>Vector<String></code> that is the Java data type for this constraint.
     */
    public void setConditions(Hashtable<String, Vector<String>> conditions) {
        // key: element or attribute name|ref
        // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
        this.conditions = conditions;
    }

    /**
     * <p>
     *  This will return the <code>Vector<String></code> version of the Java data type for this
     *    constraint.
     * </p>
     *
     * @return <code>Vector<String></code> - the data type for this constraint.
     */
    public Hashtable<String, Vector<String>> getConditions() {
        // key: element or attribute name|ref
        // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
        return conditions;
    }

    /**
     * <p>
     *  This will return the minimum allowed value for this data type (inclusive).
     * </p>
     *
     * @return <code>int</code> - minimum value allowed (inclusive)
     */
    public int getMinInclusive(String keyName) {
        // key: element or attribute name|ref
        // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
        if (conditions.containsKey(keyName)) {
            Vector<String> values = conditions.get(keyName);
            if (values.elementAt(1).equals("null")) {
                return minInclusive = -1;
            }
            return minInclusive = Integer.valueOf(values.elementAt(1));
        } else {
            return minInclusive = -1;
        }
    }

    /**
     * <p>
     *  This will return the maximum allowed value for this data type (inclusive).
     * </p>
     *
     * @return <code>int</code> - maximum value allowed (inclusive)
     */
    public int getMaxInclusive(String keyName) {
        if (conditions.containsKey(keyName)) {
            Vector<String> values = conditions.get(keyName);
            if (values.elementAt(2).equals("null")) {
                return maxInclusive = -1;
            }
            return maxInclusive = Integer.valueOf(values.elementAt(2));
        } else {
            return maxInclusive = -1;
        }
    }
}
