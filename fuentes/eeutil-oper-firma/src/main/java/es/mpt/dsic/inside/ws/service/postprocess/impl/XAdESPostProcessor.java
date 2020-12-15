/*
 * Copyright (C) 2012-13 MINHAP, Gobierno de España This program is licensed and may be used,
 * modified and redistributed under the terms of the European Public License (EUPL), either version
 * 1.1 or (at your option) any later version as soon as they are approved by the European
 * Commission. Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * more details. You should have received a copy of the EUPL1.1 license along with this program; if
 * not, you may find it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.mpt.dsic.inside.ws.service.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import es.mpt.dsic.inside.utils.xml.XMLUtil;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessException;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessUtil;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessor;


public class XAdESPostProcessor implements PostProcessor {

  @Override
  public byte[] postProcessSign(byte[] sign)
      throws IOException, PostProcessException, XPathFactoryConfigurationException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    byte[] newSign = null;
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new ByteArrayInputStream(sign));

      Element root = doc.getDocumentElement();

      javax.xml.xpath.XPath xpath =
          javax.xml.xpath.XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom").newXPath();

      Node nodo = obtenerNodoContenido(xpath, root);
      if (nodo != null) {
        if (esContenidoDocumentoB64(nodo)) {
          modificarNodoContenido(nodo);
        }
      }

      NodeList lista = obtenerUnsignedSignaturePropertiesVacios(xpath, root);
      if (lista != null) {
        eliminarNodos(lista);
      }

      doc.normalize();

      newSign = PostProcessUtil.xmlDOMtoByteArray(doc);



    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new PostProcessException("Error al parsear la firma: " + e.getMessage(), e);
    } catch (XPathFactoryConfigurationException e) {
      throw new PostProcessException("Error al parsear la firma:" + e.getMessage(), e);
    } catch (SAXException e) {
      throw new PostProcessException("Error al parsear la firma: " + e.getMessage(), e);
    } catch (TransformerException e) {
      throw new PostProcessException(
          "Error al convertir la nueva firma a un array de bytes:" + e.getMessage(), e);
    }

    return newSign;

  }


  private Node obtenerNodoContenido(javax.xml.xpath.XPath xpath, Element element) {

    String expression = "//CONTENT[(@Id|@ID|@id) and (@Encoding|@ENCODING|@encoding)]";
    Node nodo = null;

    try {
      nodo = (Node) xpath.evaluate(expression, element, javax.xml.xpath.XPathConstants.NODE);

    } catch (XPathExpressionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return nodo;
  }

  private boolean esContenidoDocumentoB64(Node nodo) {
    NamedNodeMap atributos = nodo.getAttributes();
    int i = 0;
    boolean esEncodingB64 = false;
    boolean esHash = false;

    // Este nodo contendrá el documento en b64 cuando el atributo encoding lo marque y cuando el
    // mymetipe no sea "hash/algo".
    while (i < atributos.getLength()) {
      Node atributo = atributos.item(i);
      if (atributo.getNodeName().equalsIgnoreCase("encoding")) {
        if (atributo.getNodeValue().contains("base64")) {
          esEncodingB64 = true;
        }
      } else if (atributo.getNodeName().equalsIgnoreCase("mimetype")) {
        if (atributo.getNodeValue().contains("hash")) {
          esHash = true;
        }
      }
      i++;
    }

    boolean es = esEncodingB64 && !esHash;
    return es;
  }

  private void modificarNodoContenido(Node nodoContenido) {
    String contenido = nodoContenido.getChildNodes().item(0).getTextContent();

    try {
      byte[] bytes = Base64.decode(contenido.getBytes(XMLUtil.UTF8_CHARSET));
      contenido = Base64.encode(bytes);
    } catch (Exception e) {
      e.printStackTrace();
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
      // TODO Auto-generated catch block
      e.printStackTrace();
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
