package com.github.steveice10.netty.handler.codec.xml;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import javax.xml.stream.XMLStreamException;

public class XmlDecoder extends ByteToMessageDecoder {
  private static final AsyncXMLInputFactory XML_INPUT_FACTORY = (AsyncXMLInputFactory)new InputFactoryImpl();
  
  private static final XmlDocumentEnd XML_DOCUMENT_END = XmlDocumentEnd.INSTANCE;
  
  private final AsyncXMLStreamReader<AsyncByteArrayFeeder> streamReader = XML_INPUT_FACTORY.createAsyncForByteArray();
  
  private final AsyncByteArrayFeeder streamFeeder = (AsyncByteArrayFeeder)this.streamReader.getInputFeeder();
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    byte[] buffer = new byte[in.readableBytes()];
    in.readBytes(buffer);
    try {
      this.streamFeeder.feedInput(buffer, 0, buffer.length);
    } catch (XMLStreamException exception) {
      in.skipBytes(in.readableBytes());
      throw exception;
    } 
    while (!this.streamFeeder.needMoreInput()) {
      XmlElementStart elementStart;
      int x;
      XmlElementEnd elementEnd;
      int i, type = this.streamReader.next();
      switch (type) {
        case 7:
          out.add(new XmlDocumentStart(this.streamReader.getEncoding(), this.streamReader.getVersion(), this.streamReader
                .isStandalone(), this.streamReader.getCharacterEncodingScheme()));
        case 8:
          out.add(XML_DOCUMENT_END);
        case 1:
          elementStart = new XmlElementStart(this.streamReader.getLocalName(), this.streamReader.getName().getNamespaceURI(), this.streamReader.getPrefix());
          for (x = 0; x < this.streamReader.getAttributeCount(); x++) {
            XmlAttribute attribute = new XmlAttribute(this.streamReader.getAttributeType(x), this.streamReader.getAttributeLocalName(x), this.streamReader.getAttributePrefix(x), this.streamReader.getAttributeNamespace(x), this.streamReader.getAttributeValue(x));
            elementStart.attributes().add(attribute);
          } 
          for (x = 0; x < this.streamReader.getNamespaceCount(); x++) {
            XmlNamespace namespace = new XmlNamespace(this.streamReader.getNamespacePrefix(x), this.streamReader.getNamespaceURI(x));
            elementStart.namespaces().add(namespace);
          } 
          out.add(elementStart);
        case 2:
          elementEnd = new XmlElementEnd(this.streamReader.getLocalName(), this.streamReader.getName().getNamespaceURI(), this.streamReader.getPrefix());
          for (i = 0; i < this.streamReader.getNamespaceCount(); i++) {
            XmlNamespace namespace = new XmlNamespace(this.streamReader.getNamespacePrefix(i), this.streamReader.getNamespaceURI(i));
            elementEnd.namespaces().add(namespace);
          } 
          out.add(elementEnd);
        case 3:
          out.add(new XmlProcessingInstruction(this.streamReader.getPIData(), this.streamReader.getPITarget()));
        case 4:
          out.add(new XmlCharacters(this.streamReader.getText()));
        case 5:
          out.add(new XmlComment(this.streamReader.getText()));
        case 6:
          out.add(new XmlSpace(this.streamReader.getText()));
        case 9:
          out.add(new XmlEntityReference(this.streamReader.getLocalName(), this.streamReader.getText()));
        case 11:
          out.add(new XmlDTD(this.streamReader.getText()));
        case 12:
          out.add(new XmlCdata(this.streamReader.getText()));
      } 
    } 
  }
}
