/*
 * Copyright (C) 2025, Gobierno de España This program is licensed and may be used, modified and
 * redistributed under the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European Commission. Unless
 * required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and more details. You
 * should have received a copy of the EUPL1.1 license along with this program; if not, you may find
 * it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.mpt.dsic.inside.ws.service.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import es.mpt.dsic.inside.utils.xml.XMLUtil;
import es.mpt.dsic.inside.utils.xmlsecurity.XMLSeguridadFactoria;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessException;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessUtil;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessor;


public class XAdESPostProcessor implements PostProcessor {

  private static final String ERROR_AL_CONVERTIR_LA_NUEVA_FIRMA_A_UN_ARRAY_DE_BYTES =
      "Error al convertir la nueva firma a un array de bytes:";
  private static final String ERROR_AL_PARSEAR_LA_FIRMA = "Error al parsear la firma: ";

  protected final static Log logger = LogFactory.getLog(XAdESPostProcessor.class);

  /**
   * Las excepciones se quedan como estan
   */
  @Override
  public byte[] postProcessSign(byte[] sign)
      throws IOException, PostProcessException, XPathFactoryConfigurationException {


    byte[] newSign = null;
    try {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      XMLSeguridadFactoria.getInstance().setPreventAttackDocumentBuilderFactoryExternal(dbf);
      // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
      // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
      DocumentBuilder db = dbf.newDocumentBuilder();

      try (ByteArrayInputStream bArray = new ByteArrayInputStream(sign)) {
        Document doc = db.parse(bArray);

        Element root = doc.getDocumentElement();

        javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory
            .newInstance("http://java.sun.com/jaxp/xpath/dom").newXPath();

        Node nodo = obtenerNodoContenido(xpath, root);

        if (nodo != null && esContenidoDocumentoB64(nodo)) {
          modificarNodoContenido(nodo);
        }


        NodeList lista = obtenerUnsignedSignaturePropertiesVacios(xpath, root);
        if (lista != null) {
          eliminarNodos(lista);
        }

        doc.normalize();

        newSign = PostProcessUtil.xmlDOMtoByteArray(doc);

      }


    } catch (javax.xml.parsers.ParserConfigurationException | XPathFactoryConfigurationException
        | SAXException e) {
      // logger.error(ERROR_AL_PARSEAR_LA_FIRMA + e.getMessage(), e);
      throw new PostProcessException(ERROR_AL_PARSEAR_LA_FIRMA + e.getMessage(), e);
    } catch (TransformerException | IOException e) {
      // logger.error(ERROR_AL_CONVERTIR_LA_NUEVA_FIRMA_A_UN_ARRAY_DE_BYTES + e.getMessage(), e);
      throw new PostProcessException(
          ERROR_AL_CONVERTIR_LA_NUEVA_FIRMA_A_UN_ARRAY_DE_BYTES + e.getMessage(), e);
    } catch (Exception e) {
      // logger.error("Error inesperado en postProcessSign " + e.getMessage(), e);
      throw new PostProcessException("Error inesperado en postProcessSign " + e.getMessage(), e);
    }
    return newSign;

  }


  private Node obtenerNodoContenido(javax.xml.xpath.XPath xpath, Element element) {

    String expression = "//CONTENT[(@Id|@ID|@id) and (@Encoding|@ENCODING|@encoding)]";
    Node nodo = null;

    try {
      nodo = (Node) xpath.evaluate(expression, element, javax.xml.xpath.XPathConstants.NODE);

    } catch (XPathExpressionException e) {
      logger.error("Excepcion en obtenerNodoContenido " + e.getMessage(), e);
    }

    return nodo;
  }

  private boolean esContenidoDocumentoB64(Node nodo) {
    NamedNodeMap atributos = nodo.getAttributes();
    int i = 0;
    boolean esEncodingB64 = false;
    boolean esHash = false;

    // Este nodo contendra el documento en b64 cuando el atributo encoding lo marque y cuando el
    // mymetipe no sea "hash/algo".
    while (i < atributos.getLength()) {
      Node atributo = atributos.item(i);
      if (atributo.getNodeName().equalsIgnoreCase("encoding")) {
        if (atributo.getNodeValue().contains("base64")) {
          esEncodingB64 = true;
        }
      } else if (atributo.getNodeName().equalsIgnoreCase("mimetype")
          && atributo.getNodeValue().contains("hash")) {
        esHash = true;
      }
      i++;
    }

    return esEncodingB64 && !esHash;
  }

  private void modificarNodoContenido(Node nodoContenido) {
    String contenido = nodoContenido.getChildNodes().item(0).getTextContent();

    try {
      byte[] bytes = Base64.decode(contenido.getBytes(XMLUtil.UTF8_CHARSET));
      contenido = Base64.encode(bytes);
    } catch (Exception e) {
      logger.error("Excepcion en modificarNodoContenido " + e.getMessage(), e);
    }

    if (contenido.indexOf("&#13;") > -1) {
      contenido = contenido.replace("&#13;", "");
    }

    nodoContenido.getChildNodes().item(0).setTextContent(contenido);
  }

  private NodeList obtenerUnsignedSignaturePropertiesVacios(javax.xml.xpath.XPath xpath,
      Element element) {

    String expresion = "//UnsignedProperties/UnsignedSignatureProperties[not (*)]";

    NodeList lista = null;

    try {
      lista = (NodeList) xpath.evaluate(expresion, element, javax.xml.xpath.XPathConstants.NODESET);

    } catch (XPathExpressionException e) {
      logger.error("Excepcion en obtenerUnsignedSignaturePropertiesVacios " + e.getMessage(), e);
    }
    return lista;
  }

  private void eliminarNodos(NodeList lista) {
    // Recorremos la lista y eliminamos los nodos.
    for (int i = 0; i < lista.getLength(); i++) {
      Node eliminar = lista.item(i);
      eliminar.getParentNode().removeChild(eliminar);
    }
  }
}
