//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.06 at 11:42:21 AM EST 
//


package org.inex.ltw.rungenerator;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for linkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="linkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}anchor"/>
 *         &lt;element ref="{}linkto" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkType", propOrder = {
    "anchor",
    "linkto"
})
public class LinkType {

    @XmlElement(required = true)
    protected AnchorType anchor;
    protected List<LinktoType> linkto;

    /**
     * Gets the value of the anchor property.
     * 
     * @return
     *     possible object is
     *     {@link AnchorType }
     *     
     */
    public AnchorType getAnchor() {
        return anchor;
    }

    /**
     * Sets the value of the anchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnchorType }
     *     
     */
    public void setAnchor(AnchorType value) {
        this.anchor = value;
    }

    /**
     * Gets the value of the linkto property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkto property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkto().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinktoType }
     * 
     * 
     */
    public List<LinktoType> getLinkto() {
        if (linkto == null) {
            linkto = new ArrayList<LinktoType>();
        }
        return this.linkto;
    }

}
