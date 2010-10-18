//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.06 at 11:42:21 AM EST 
//


package org.inex.ltw.rungenerator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for topicType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="topicType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}outgoing"/>
 *         &lt;element ref="{}incoming"/>
 *       &lt;/sequence>
 *       &lt;attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "topicType", propOrder = {
    "outgoing",
    "incoming"
})
public class TopicType {

    @XmlElement(required = true)
    protected LinkingType outgoing;
    @XmlElement(required = true)
    protected LinkingType incoming;
    @XmlAttribute(required = true)
    protected String file;
    @XmlAttribute(required = true)
    protected String name;

    /**
     * Gets the value of the outgoing property.
     * 
     * @return
     *     possible object is
     *     {@link LinkingType }
     *     
     */
    public LinkingType getOutgoing() {
        return outgoing;
    }

    /**
     * Sets the value of the outgoing property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkingType }
     *     
     */
    public void setOutgoing(LinkingType value) {
        this.outgoing = value;
    }

    /**
     * Gets the value of the incoming property.
     * 
     * @return
     *     possible object is
     *     {@link LinkingType }
     *     
     */
    public LinkingType getIncoming() {
        return incoming;
    }

    /**
     * Sets the value of the incoming property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkingType }
     *     
     */
    public void setIncoming(LinkingType value) {
        this.incoming = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
