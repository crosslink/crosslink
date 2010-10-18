  package ltwassessmenttool.validation;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

// JDOM classes used for document representation
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * @author Darren
 */
public class schemaParser {

    private static final String SCHEMA_NAMESPACE_URI = "http://www.w3.org/1999/XMLschema";
    private URL schemaURL;
    private Map constraints;
    private Namespace schemaNamespace;

    void log(Object obj) {
        System.out.println(obj.toString());
    }

    public schemaParser(URL schemaURL) throws IOException {
        this.schemaURL = schemaURL;
        this.constraints = new HashMap();
        this.schemaNamespace = Namespace.getNamespace(SCHEMA_NAMESPACE_URI);
        parseschema();
    }

    public Map getConstraints() {
        return constraints;
    }

    public Constraint getConstraint(String constraintName) {
        Object obj = constraints.get(constraintName);
        if (obj != null) {
            return (Constraint) obj;
        } else {
            return null;
        }
    }

    /**
     * <p>
     *  This will do the work of parsing the schema.
     * </p>
     *
     * @throws <code>IOException</code> - when parsing errors occur.
     */
    private void parseschema() {
        try {

            // Create builder to generate JDOM representation of XML Schema
            //  without validation and using Apache Xerces.
            SAXBuilder builder = new SAXBuilder();
            Document schemaDoc = builder.build(schemaURL);
            // handle Elements
            List elements = schemaDoc.getRootElement().getChildren();
            // 1) Get definitions of simpleType and attributes
            HashMap simpleTypeHM = new HashMap();
            HashMap attributeHM = new HashMap();
            for (Iterator i = elements.iterator(); i.hasNext();) {
                // Iterate and handle
                Element element = (Element) i.next();
                if (element.getName().equals("simpleType")) {
                    simpleTypeHM = handleSimpleType(element);
                } else if (element.getName().equals("attribute")) {
                    attributeHM = handleAttribute(element);
                }
            }
            // handle attributes nested within complex types
            for (Iterator i = elements.iterator(); i.hasNext();) {
                // Iterate and handle
                Element element = (Element) i.next();
                if (element.getName().equals("element")) {
                    handleElement(element, simpleTypeHM, attributeHM);
                }
            }
        } catch (JDOMException ex) {
            Logger.getLogger(schemaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(schemaParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>
     *  This will get the Data Type(s) defined in simpleType element.
     * </p>
     *
     * @return <code>Empty HashMap</code> - when no simpleType has been defined.
     */
    private HashMap handleSimpleType(Element element) {
        HashMap simpleTypeHM = new HashMap();
        String nameValue = "null";
        List attrList = element.getAttributes();
        Attribute nameAttr = (Attribute) attrList.iterator().next();
        if (nameAttr.getName().equals("name")) {
            nameValue = nameAttr.getValue();
        }
        String baseValue = "null";
        List elmns = element.getChildren();
        for (Iterator i = elmns.iterator(); i.hasNext();) {
            Element subElmn = (Element) i.next();
            if (subElmn.getName().equals("restriction")) {
                List subAttrs = subElmn.getAttributes();
                Attribute subAttr = (Attribute) subAttrs.iterator().next();
                baseValue = subAttr.getValue();
            }
        }
        String thisSimpleType = baseValue.substring(baseValue.indexOf(":") + 1, baseValue.length());
//        log("simpleType: " + nameValue + " - " + baseValue + " - " + thisSimpleType);
        if (nameValue == null) {
            simpleTypeHM.put(nameValue, thisSimpleType);
        }
        return simpleTypeHM;
    }

    /**
     * <p>
     *  This will get the global Attribute(s) defined in Attribute element.
     * </p>
     *
     * @return <code>Empty HashMap</code> - when no global Attribute has been defined.
     */
    private HashMap handleAttribute(Element element) {
        HashMap attributeHM = new HashMap();
        String nameValue = "null";
        String typeValue = "null";
        List attrList = element.getAttributes();
        for (Iterator k = attrList.iterator(); k.hasNext();) {
            Attribute attrSet = (Attribute) k.next();
            if (attrSet.getName().equals("name")) {
                nameValue = element.getAttributeValue("name");
            } else if (attrSet.getName().equals("type")) {
                typeValue = element.getAttributeValue("type");
            }
        }
//        log("attribute: " + nameValue + " - " + typeValue);
        if (nameValue == null) {
            attributeHM.put(nameValue, typeValue);
        }
        return attributeHM;
    }

    /**
     * <p>
     *  This will convert an element into constraints.
     * </p>
     *
     * @throws <code>IOException</code> - when parsing errors occur.
     */
    private void handleElement(Element element, HashMap simpleTypeHM, HashMap attributeHM) throws IOException {
        /**
         * Get the Element name and create a Constraint
         * 2 types of structures are in our case
         * 1) element (name) --> complexType 
         *                      --> [ sequence --> element (name|ref, minOccurs, maxOccurs) | 
         *                            attribute (name|ref, use) ]
         * 2) element (name) --> complexType 
         *                      --> simpleContent 
         *                         --> extension (base) 
         *                            --> attribute (name|ref, use)
         */
        if (element.getName().equals("element")) {
            List elmnAttrList = element.getAttributes();
            if (elmnAttrList.isEmpty()) {
                throw new IOException("All schema elements must have name attributes.");
            }
            Attribute elmnAttr = (Attribute) elmnAttrList.iterator().next();
            String elmnAttrName = elmnAttr.getName();
            String elmnAttrValue = element.getAttributeValue(elmnAttrName);
            // -----------------------------------------------------------------
            // initiate Constraint for each Main Element
            Constraint constraint = new Constraint(elmnAttrValue);
            Vector<String> elements = new Vector<String>();
            Vector<String> attributes = new Vector<String>();
            // key: element or attribute name|ref
            // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
            Hashtable<String, Vector<String>> conditions = new Hashtable<String, Vector<String>>();
            // -----------------------------------------------------------------
            // complexType
            List complexTypeElmns = element.getChildren();
            Element complexTypeElmn = (Element) complexTypeElmns.iterator().next();
            List thisElmns = complexTypeElmn.getChildren();
            for (Iterator i = thisElmns.iterator(); i.hasNext();) {
                Element thisElmn = (Element) i.next();
                if (thisElmn.getName().equals("simpleContent")) {
                    // CASE 1: simpleContent
                    List extensionElmns = thisElmn.getChildren();
                    Element extensionElmn = (Element) extensionElmns.iterator().next();
                    String extensionBaseValue = extensionElmn.getAttributeValue("base");
                    List attrElmns = extensionElmn.getChildren();
                    for (Iterator j = attrElmns.iterator(); j.hasNext();) {
                        Element attrElmn = (Element) j.next();
                        String nameValue = "null";
                        String refValue = "null";
                        String useValue = "null";
                        if (attrElmn.getName().equals("attribute")) {
                            List attrList = attrElmn.getAttributes();
                            for (Iterator k = attrList.iterator(); k.hasNext();) {
                                Attribute attrSet = (Attribute) k.next();
                                if (attrSet.getName().equals("name")) {
                                    nameValue = attrElmn.getAttributeValue("name");
                                } else if (attrSet.getName().equals("ref")) {
                                    refValue = attrElmn.getAttributeValue("ref");
                                } else if (attrSet.getName().equals("use")) {
                                    useValue = attrElmn.getAttributeValue("use");
                                }
                            }
                            if (nameValue.equals("null") && refValue.equals("null")) {
                                throw new IOException("Attributes inside simpleContent extension must contain name or ref and use.");
                            } else if (refValue.equals("null")) {
                                attributes.add(nameValue);
                                if (!conditions.containsKey(nameValue)) {
                                    String javaDataType = getDataTypeByKey(nameValue, simpleTypeHM, attributeHM);
                                    // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
                                    Vector<String> conditionFactors = new Vector<String>();
                                    conditionFactors.add(javaDataType);
                                    conditionFactors.add("null");
                                    conditionFactors.add("null");
                                    conditionFactors.add(useValue);
                                    conditions.put(nameValue, conditionFactors);
                                }
                            } else if (nameValue.equals("null")) {
                                attributes.add(refValue);
                                if (!conditions.containsKey(refValue)) {
                                    String javaDataType = getDataTypeByKey(refValue, simpleTypeHM, attributeHM);
                                    // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
                                    Vector<String> conditionFactors = new Vector<String>();
                                    conditionFactors.add(javaDataType);
                                    conditionFactors.add("null");
                                    conditionFactors.add("null");
                                    conditionFactors.add(useValue);
                                    conditions.put(refValue, conditionFactors);
                                }
                            }
                            
                        }
                    }
                    constraint.setElements(elements);
                    constraint.setAttributes(attributes);
                    constraint.setConditions(conditions);
                } else {
                    // CASE 2: sequence | attribute
                    if (thisElmn.getName().equals("sequence")) {
                        List subElmns = thisElmn.getChildren();
                        for (Iterator j = subElmns.iterator(); j.hasNext();) {
                            Element subElmn = (Element) j.next();
                            String nameValue = "null";
                            String refValue = "null";
                            String minOccursValue = "null";
                            String maxOccursValue = "null";
                            String useValue = "null";
                            List subElmnAttr = subElmn.getAttributes();
                            for (Iterator k = subElmnAttr.iterator(); k.hasNext();) {
                                Attribute attrSet = (Attribute) k.next();
                                if (attrSet.getName().equals("name")) {
                                    nameValue = subElmn.getAttributeValue("name");
                                } else if (attrSet.getName().equals("ref")) {
                                    refValue = subElmn.getAttributeValue("ref");
                                } else if (attrSet.getName().equals("minOccurs")) {
                                    minOccursValue = subElmn.getAttributeValue("minOccurs");
                                } else if (attrSet.getName().equals("maxOccurs")) {
                                    maxOccursValue = subElmn.getAttributeValue("maxOccurs");
                                }
                                if (attrSet.getName().equals("use")) {
                                    useValue = subElmn.getAttributeValue("use");
                                }
                            }
                            if (nameValue.equals("null") && refValue.equals("null")) {
                                throw new IOException("Elements inside sequence extension must contain name or ref and use.");
                            } else if (refValue.equals("null")) {
                                elements.add(nameValue);
                                if (!conditions.containsKey(nameValue)) {
                                    String javaDataType = getDataTypeByKey(nameValue, simpleTypeHM, attributeHM);
                                    // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
                                    Vector<String> conditionFactors = new Vector<String>();
                                    conditionFactors.add(javaDataType);
                                    conditionFactors.add(minOccursValue);
                                    conditionFactors.add(maxOccursValue);
                                    conditionFactors.add(useValue);
                                    conditions.put(nameValue, conditionFactors);
                                }
                            } else if (nameValue.equals("null")) {
                                elements.add(refValue);
                                if (!conditions.containsKey(refValue)) {
                                    String javaDataType = getDataTypeByKey(refValue, simpleTypeHM, attributeHM);
                                    // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
                                    Vector<String> conditionFactors = new Vector<String>();
                                    conditionFactors.add(javaDataType);
                                    conditionFactors.add(minOccursValue);
                                    conditionFactors.add(maxOccursValue);
                                    conditionFactors.add(useValue);
                                    conditions.put(refValue, conditionFactors);
                                }
                            }
                            
                        }
                    } else if (thisElmn.getName().equals("attribute")) {
                        String nameValue = "null";
                        String refValue = "null";
                        String useValue = "null";
                        List attrList = thisElmn.getAttributes();
                        for (Iterator k = attrList.iterator(); k.hasNext();) {
                            Attribute attrSet = (Attribute) k.next();
                            if (attrSet.getName().equals("name")) {
                                nameValue = thisElmn.getAttributeValue("name");
                            } else if (attrSet.getName().equals("ref")) {
                                refValue = thisElmn.getAttributeValue("ref");
                            } else if (attrSet.getName().equals("use")) {
                                useValue = thisElmn.getAttributeValue("use");
                            }
                        }
                        if (nameValue.equals("null") && refValue.equals("null")) {
                            throw new IOException("Attributes inside simpleContent extension must contain name or ref and use.");
                        } else if (refValue.equals("null")) {
                            attributes.add(nameValue);
                            if (!conditions.containsKey(nameValue)) {
                                String javaDataType = getDataTypeByKey(nameValue, simpleTypeHM, attributeHM);
                                // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
                                Vector<String> conditionFactors = new Vector<String>();
                                conditionFactors.add(javaDataType);
                                conditionFactors.add("null");
                                conditionFactors.add("null");
                                conditionFactors.add(useValue);
                                conditions.put(nameValue, conditionFactors);
                            }
                        } else if (nameValue.equals("null")) {
                            attributes.add(refValue);
                            if (!conditions.containsKey(refValue)) {
                                String javaDataType = getDataTypeByKey(refValue, simpleTypeHM, attributeHM);
                                // value: 0: type, 1: minOccurs, 2: maxOccurs, 3: use
                                Vector<String> conditionFactors = new Vector<String>();
                                conditionFactors.add(javaDataType);
                                conditionFactors.add("null");
                                conditionFactors.add("null");
                                conditionFactors.add(useValue);
                                conditions.put(refValue, conditionFactors);
                            }
                        }
                        
                    }
                }
                constraint.setElements(elements);
                constraint.setAttributes(attributes);
                constraint.setConditions(conditions);
            }
            // Store this constraint
            constraints.put(elmnAttrValue, constraint);
        }
    }

    private String getDataTypeByKey(String nameValue, HashMap simpleTypeHM, HashMap attributeHM) {
        String javaDataType = "null";
        if (attributeHM.containsKey(nameValue)) {
            String typeAbbr = attributeHM.get(nameValue).toString();
            String schemaDataType = simpleTypeHM.get(typeAbbr).toString();
            javaDataType = DataConverter.getInstance().getJavaType(schemaDataType);
        }
        return javaDataType;
    }
}
