//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.12.07 at 02:27:09 PM EST 
//


package crosslink.resultsetGenerator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LtwTopic_QNAME = new QName("", "ltw_Topic");
    private final static QName _OutgoingLinks_QNAME = new QName("", "outgoingLinks");
    private final static QName _IncomingLinks_QNAME = new QName("", "incomingLinks");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OutlinkingType }
     * 
     */
    public OutlinkingType createOutlinkingType() {
        return new OutlinkingType();
    }

    /**
     * Create an instance of {@link LtwResultsetType }
     * 
     */
    public LtwResultsetType createLtwResultsetType() {
        return new LtwResultsetType();
    }

    /**
     * Create an instance of {@link LtwTopicType }
     * 
     */
    public LtwTopicType createLtwTopicType() {
        return new LtwTopicType();
    }

    /**
     * Create an instance of {@link OutLink }
     * 
     */
    public OutLink createOutLink() {
        return new OutLink();
    }

    /**
     * Create an instance of {@link InlinkingType }
     * 
     */
    public InlinkingType createInlinkingType() {
        return new InlinkingType();
    }

    /**
     * Create an instance of {@link InLink }
     * 
     */
    public InLink createInLink() {
        return new InLink();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LtwTopicType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ltw_Topic")
    public JAXBElement<LtwTopicType> createLtwTopic(LtwTopicType value) {
        return new JAXBElement<LtwTopicType>(_LtwTopic_QNAME, LtwTopicType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OutlinkingType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "outgoingLinks")
    public JAXBElement<OutlinkingType> createOutgoingLinks(OutlinkingType value) {
        return new JAXBElement<OutlinkingType>(_OutgoingLinks_QNAME, OutlinkingType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InlinkingType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "incomingLinks")
    public JAXBElement<InlinkingType> createIncomingLinks(InlinkingType value) {
        return new JAXBElement<InlinkingType>(_IncomingLinks_QNAME, InlinkingType.class, null, value);
    }

}
