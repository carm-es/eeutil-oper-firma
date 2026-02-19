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

package es.mpt.dsic.inside.utils.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.file.FileUtil;
import es.mpt.dsic.inside.utils.xmlsecurity.XMLSeguridadFactoria;

public class XMLUtil {

  private static final String MIME_TYPE = "MimeType";

  private XMLUtil() {

  }

  protected static final Log logger = LogFactory.getLog(XMLUtil.class);

  public final static String VERSION_1_0_ENI = "1.0";
  public final static String VERSION_2_0_ENI = "2.0";

  public static final String KEY_MAPA_LISTAEXPEDIENTE = "listaExpediente";
  public static final String KEY_MAPA_LISTADOCUMENTO = "listaDocumento";

  final static String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  // EXPEDIENTE
  final static String NAMESPACE_EXPEDIENTE_V1_0 =
      "http://administracionelectronica.gob.es/ENI/XSD/v1.0/expediente-e";
  final static String NAMESPACE_EXPEDIENTE_V2_0 =
      "http://administracionelectronica.gob.es/ENI/XSD/v2.0/expediente-e";
  final static String TAG_EXPEDIENTE = "expediente";

  // DOCUMENTO
  final static String NAMESPACE_DOCUMENTO_V1_0 =
      "http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e";
  final static String NAMESPACE_DOCUMENTO_v2_0 =
      "http://administracionelectronica.gob.es/ENI/XSD/v2.0/documento-e";
  final static String TAG_DOCUMENTO = "documento";

  final static String NAMESPACE_INDICE_v1_0 =
      "http://administracionelectronica.gob.es/ENI/XSD/v1.0/expediente-e/indice-e";
  final static String NAMESPACE_INDICE_v2_0 =
      "http://administracionelectronica.gob.es/ENI/XSD/v2.0/expediente-e/indice-e";
  final static String TAG_INDICECONTENIDO = "IndiceContenido";

  // NAMESPACES POR AHORA OBLIGATORIOS PARA VALIDAR FIRMA DE EXPEDIENTEENI
  // PROCEDENTES DE ARCHIVE
  final static String NAMESPACE_INSIDEWS_V1_0 =
      "xmlns:insidews=https://ssweb.seap.minhap.es/Inside/XSD/v1.0/WebService";
  final static String NAMESPACE_INSIDEWS_V2_0 =
      "xmlns:insidews=https://ssweb.seap.minhap.es/Inside/XSD/v2.0/WebService";
  final static String NAMESPACE_NS9_V1_0 =
      "xmlns:ns9=https://ssweb.seap.minhap.es/Inside/XSD/v1.0/expediente-e";
  final static String NAMESPACE_NS9_V2_0 =
      "xmlns:ns9=https://ssweb.seap.minhap.es/Inside/XSD/v2.0/expediente-e";

  final static String NAMESPACE_NS8_V1_0 =
      "xmlns:ns8=https://ssweb.seap.minhap.es/Inside/XSD/v1.0/metadatosAdicionales";
  final static String NAMESPACE_NS8_V2_0 =
      "xmlns:ns8=https://ssweb.seap.minhap.es/Inside/XSD/v2.0/metadatosAdicionales";
  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");



  /**
   * Obtiene el arbol XML de una fuente, que puede ser File, InputStream o array de bytes.
   * 
   * @param source fuente de la que se quiere obtener el arbol XML.
   * @return arbol XML.
   * @throws ParserConfigurationException, se evalua en niveles posteriores no se lanza
   *         eeutilsexecption
   * @throws SAXException, se evalua en niveles posteriores no se lanza eeutilsexecption
   * @throws IOException, se evalua en niveles posteriores no se lanza eeutilsexecption
   */
  public static Document getDOMDocument(Object source, boolean namespaceAware)
      throws ParserConfigurationException, SAXException, IOException {
    logger.debug("getDOMDocument init");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(dbf);
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);

    dbf.setNamespaceAware(namespaceAware);
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = null;
    if (source instanceof File) {
      File f = (File) source;
      doc = db.parse(f);
    } else if (source instanceof InputStream) {
      InputStream is = (InputStream) source;
      doc = db.parse(is);
    } else if (source instanceof byte[]) {
      byte[] bytes = (byte[]) source;

      try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
        doc = db.parse(bis);
      }
    }
    logger.debug("getDOMDocument end");
    return doc;

  }

  /**
   * Comprueba si se cumplen una serie de expresiones xpath.
   * 
   * @param doc Documento DOM
   * @param xpathExpressions array de expresiones xpath.
   * @param xpath instancia de xpath
   * @return true si se cumplen todas, false en caso contrario.
   * @throws XPathExpressionException si alguna de las expresiones no es correcta. No se lanza
   *         eeutilexception, se evalua despues
   */
  public static boolean seCumplen(Document doc, String[] xpathExpressions,
      javax.xml.xpath.XPath xpath) throws XPathExpressionException {
    Boolean seCumplen = true;

    int i = 0;

    while (i < xpathExpressions.length && seCumplen) {
      String expr = xpathExpressions[i];
      seCumplen = (Boolean) xpath.evaluate(expr, doc.getDocumentElement(),
          javax.xml.xpath.XPathConstants.BOOLEAN);
      xpath.reset();
      i++;
    }

    return seCumplen;
  }

  public static NodeList getNodeListByXpathExpression(Object obj, String xpathExpression)
      throws XPathFactoryConfigurationException, XPathExpressionException {

    javax.xml.xpath.XPath xpath =
        javax.xml.xpath.XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom").newXPath();

    NodeList nodeList =
        (NodeList) xpath.evaluate(xpathExpression, obj, javax.xml.xpath.XPathConstants.NODESET);

    return nodeList;

  }

  public static Node getNodeByXpathExpression(Document doc, String xpathExpression)
      throws XPathFactoryConfigurationException, XPathExpressionException {

    javax.xml.xpath.XPath xpath =
        javax.xml.xpath.XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom").newXPath();

    Node node = (Node) xpath.evaluate(xpathExpression, doc, javax.xml.xpath.XPathConstants.NODE);

    return node;
  }

  /**
   * Comprueba si un nodo tiene un atributo "MimeType" que contenga la cadena "hash"
   * 
   * @param node
   * @return
   */
  public static boolean isHashMimeType(Node node) {
    boolean ret = false;
    if (node.getAttributes() != null && node.getAttributes().getNamedItem(MIME_TYPE) != null &&
    // se cambia el order del build path y se pone delante a maven y despues la jre
    // y se cambia la linea siguiente pq daba error la anterior
    // node.getAttributes().getNamedItem("MimeType").getTextContent() != null &&
        node.getAttributes().getNamedItem(MIME_TYPE).getNodeValue() != null &&
        // se cambia el order del build path y se pone delante a maven y despues la jre
        // y se cambia la linea siguiente pq daba error la anterior
        // node.getAttributes().getNamedItem("MimeType").getTextContent().contains("hash"))
        // {
        node.getAttributes().getNamedItem(MIME_TYPE).getNodeValue().contains("hash")) {
      ret = true;
    }
    return ret;
  }

  /**
   * Devuelve el valor del atributo "Encoding" del nodo.
   * 
   * @param node
   * @return
   */
  public static String getEncoding(Node node) {
    String encoding = null;
    encoding = getAttribute(node, "Encoding");
    if (encoding == null) {
      encoding = getAttribute(node, "encoding");
    }
    return encoding;
  }

  public static String getHashAlgorithm(Node node) {
    return getAttribute(node, "hashAlgorithm");
  }

  /*
   * public static String getEncoding (Node node) { return getAttribute(node, "Encoding"); }
   */

  public static String getAttribute(Node node, String attName) {
    String attValue = null;

    if (node.getAttributes() != null && node.getAttributes().getNamedItem(attName) != null) {
      // se cambia el order del build path y se pone delante a maven y despues la jre
      // y se cambia la linea siguiente pq daba error la anterior
      // attValue = node.getAttributes().getNamedItem(attName).getTextContent();
      attValue = node.getAttributes().getNamedItem(attName).getNodeValue();
    }

    return attValue;
  }

  public static boolean isXMLMimeType(Node node) {
    String mime = null;
    boolean ret = false;

    if (node.getAttributes() != null && node.getAttributes().getNamedItem(MIME_TYPE) != null) {

      // se cambia el order del build path y se pone delante a maven y despues la jre
      // y se cambia la linea siguiente pq daba error la anterior
      // mime = node.getAttributes().getNamedItem("MimeType").getTextContent();
      mime = node.getAttributes().getNamedItem(MIME_TYPE).getNodeValue();

    }

    if ("text/xml".equalsIgnoreCase(mime) || "application/xml".equalsIgnoreCase(mime)) {
      ret = true;
    }

    return ret;
  }

  /**
   * Convierte un documento XML de DOM a un ByteArrayOutputStream.
   * 
   * @param doc
   * @return
   * @throws UnsupportedEncodingException
   * @throws TransformerException
   */
  public static ByteArrayOutputStream getBytesFromNode(Node node, String encoding)
      throws UnsupportedEncodingException, TransformerException {
    /*
     * TransformerFactory tf = TransformerFactory.newInstance(); Transformer t =
     * tf.newTransformer();
     * 
     * t.setOutputProperty(OutputKeys.INDENT, "yes"); t.setOutputProperty(OutputKeys.ENCODING,
     * encoding); t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
     */

    Transformer t = getGenericTransformer("yes", encoding, "2");

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    t.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(bout, encoding)));

    return bout;
  }

  public static String getStringFromNode(Node node, String encoding) throws TransformerException {
    /*
     * TransformerFactory tf = TransformerFactory.newInstance(); Transformer t =
     * tf.newTransformer();
     * 
     * t.setOutputProperty(OutputKeys.INDENT, "yes"); t.setOutputProperty(OutputKeys.ENCODING,
     * encoding); t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
     */

    Transformer t = getGenericTransformer("yes", encoding, "2");
    StringWriter sw = new StringWriter();

    t.transform(new DOMSource(node), new StreamResult(sw));
    return sw.toString();
  }

  private static Transformer getGenericTransformer(String indent, String encoding,
      String indent_amount) throws TransformerConfigurationException {
    TransformerFactory tf = TransformerFactory.newInstance();
    // habilitamos seguridad para evitar problemas de hijacking.
    // deshabilitamos para evitar validaciones dtd
    // tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    // to be compliant, prohibit the use of all protocols by external entities:
    // tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    // tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    XMLSeguridadFactoria.setPreventAttackExternalTransformerStatic(tf);
    Transformer t = tf.newTransformer();

    t.setOutputProperty(OutputKeys.INDENT, indent);
    t.setOutputProperty(OutputKeys.ENCODING, encoding);
    t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent_amount);

    return t;

  }

  /**
   * Elimina los nodos de un documento que cumplan una determinada expresión XPATH
   * 
   * @param xpathExpr
   * @param doc
   * @throws XPathFactoryConfigurationException
   * @throws XPathExpressionException
   */
  public static void removeNodesFromDocument(String xpathExpr, Document doc)
      throws XPathFactoryConfigurationException, XPathExpressionException {
    NodeList nodes = XMLUtil.getNodeListByXpathExpression(doc, xpathExpr);

    // Los eliminamos
    if (nodes != null) {
      for (int i = 0; i < nodes.getLength(); i++) {
        Node n = nodes.item(i);
        n.getParentNode().removeChild(n);
      }
    }

    doc.normalize();
  }

  /**
   * Comprueba que un nodo XML tenga el atributo Id y el atributo Encoding con valor Base64.
   * 
   * @param nodo
   * @return
   */
  public static boolean contieneIdEncoding(Node nodo) {

    NamedNodeMap atributos = nodo.getAttributes();

    // Comprobamos que tenga el atributo Id y el atributo Encoding:
    int i = 0;
    boolean encontrados = false;
    boolean idFound = false;
    boolean encodingFound = false;

    while (atributos != null && i < atributos.getLength() && !encontrados) {
      if (atributos.item(i).getNodeName().equalsIgnoreCase("id")) {
        idFound = true;
      } else if (atributos.item(i).getNodeName().equalsIgnoreCase("encoding")
          && atributos.item(i).getNodeValue().contains("base64")) {
        encodingFound = true;
      }

      encontrados = idFound && encodingFound;
      i++;
    }

    return encontrados;
  }

  public static Source[] getSchemasSources(String dir) {

    File f = new File(dir);

    List<File> filesIn = FileUtil.getFilesInFolder(f, ".xsd");

    Source[] schemasSources = new Source[filesIn.size()];
    int i = 0;
    for (File schema : filesIn) {
      schemasSources[i] = new StreamSource(schema);
      i++;
    }

    return schemasSources;
  }

  public static XMLReader createParserForValidation(Source[] schemasSources)
      throws EeutilException {

    XMLReader parser = null;
    try {

      SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA);
      XMLSeguridadFactoria.setPreventAttackExternalSchemaFactoryStatic(schemaFactory);
      if (schemasSources.length == 0) {
        throw new IOException("No se han proporcionado los schemas");
      }

      Schema schemaGrammar = schemaFactory.newSchema(schemasSources);

      Validator schemaValidator = schemaGrammar.newValidator();
      ValidatorHandler vHandler = schemaGrammar.newValidatorHandler();

      DefaultHandler validationHandler = new DefaultHandler();
      schemaValidator.setErrorHandler(validationHandler);

      ContentHandler cHandler = validationHandler;
      vHandler.setContentHandler(cHandler);

      parser = XMLReaderFactory.createXMLReader();

      // to be compliant, completely disable DOCTYPE declaration:
      // parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      // or completely disable external entities declarations:
      // parser.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
      // parser.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);

      parser.setContentHandler(vHandler);

    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

    return parser;
  }

  public static List<String> getNameSpacesNodoROOT(String xml)
      throws ParserConfigurationException, SAXException, IOException {
    org.w3c.dom.Node nodoPadre = XMLUtil.getNode(xml.getBytes(XMLUtil.UTF8_CHARSET), "*");// *
                                                                                          // recoge
                                                                                          // el
                                                                                          // primer
                                                                                          // nodo

    List<String> listaNameSpaces = new ArrayList<String>();
    for (int i = 0; i < nodoPadre.getAttributes().getLength(); i++) {
      org.w3c.dom.Node nodoTemp = nodoPadre.getAttributes().item(i);
      String nameSpace = nodoTemp.getNodeName() + "=" + nodoTemp.getNodeValue();
      listaNameSpaces.add(nameSpace);
    }
    return listaNameSpaces;
  }

  public static List<String> getNameSpacesDelHijoDelROOT(String xml)
      throws ParserConfigurationException, SAXException, IOException {
    org.w3c.dom.Node nodoPadre = XMLUtil.getNode(xml.getBytes(XMLUtil.UTF8_CHARSET), "*");// *
                                                                                          // recoge
                                                                                          // el
                                                                                          // primer
                                                                                          // nodo
    org.w3c.dom.Node nodoPrimerHijo = nodoPadre.getFirstChild();

    List<String> listaNameSpaces = new ArrayList<String>();
    for (int i = 0; i < nodoPrimerHijo.getAttributes().getLength(); i++) {
      org.w3c.dom.Node nodoTemp = nodoPrimerHijo.getAttributes().item(i);
      String nameSpace = nodoTemp.getNodeName() + "=" + nodoTemp.getNodeValue();
      listaNameSpaces.add(nameSpace);
    }
    return listaNameSpaces;
  }

  /**
   * 
   * @param nodoEni
   * @param listaNameSpaces
   * @param version. Si la version es null tomaremos que es una version 1.0 Valores admitidos: 1.0 y
   *        2.0
   * @return
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws TransformerFactoryConfigurationError
   * @throws TransformerException
   */
  public static String addNameSpacesToExpedienteENIXML(Node nodoEni, List<String> listaNameSpaces,
      String version) throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {
    Element nodoEniElem = (Element) nodoEni;
    for (int i = 0; i < listaNameSpaces.size(); i++) {
      nodoEniElem.setAttribute(listaNameSpaces.get(i).split("=")[0],
          listaNameSpaces.get(i).split("=")[1]);
    }

    if (version == null || VERSION_1_0_ENI.equals(version)) {

      if (!listaNameSpaces.contains(NAMESPACE_INSIDEWS_V1_0))
        nodoEniElem.setAttribute(NAMESPACE_INSIDEWS_V1_0.split("=")[0],
            NAMESPACE_INSIDEWS_V1_0.split("=")[1]);

      if (!listaNameSpaces.contains(NAMESPACE_NS8_V1_0))
        nodoEniElem.setAttribute(NAMESPACE_NS8_V1_0.split("=")[0],
            NAMESPACE_NS8_V1_0.split("=")[1]);

      if (!listaNameSpaces.contains(NAMESPACE_NS9_V1_0))
        nodoEniElem.setAttribute(NAMESPACE_NS9_V1_0.split("=")[0],
            NAMESPACE_NS9_V1_0.split("=")[1]);

    } else if (VERSION_2_0_ENI.equals(version)) {

      if (!listaNameSpaces.contains(NAMESPACE_INSIDEWS_V2_0))
        nodoEniElem.setAttribute(NAMESPACE_INSIDEWS_V2_0.split("=")[0],
            NAMESPACE_INSIDEWS_V2_0.split("=")[1]);

      if (!listaNameSpaces.contains(NAMESPACE_NS8_V2_0))
        nodoEniElem.setAttribute(NAMESPACE_NS8_V2_0.split("=")[0],
            NAMESPACE_NS8_V2_0.split("=")[1]);

      if (!listaNameSpaces.contains(NAMESPACE_NS9_V2_0))
        nodoEniElem.setAttribute(NAMESPACE_NS9_V2_0.split("=")[0],
            NAMESPACE_NS9_V2_0.split("=")[1]);
    } else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }

    return nodeToString(nodoEni);
  }

  public static String deleteNameSpacesToExpedienteENIXML(Node nodoEni, String version)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {

    Element nodoEniElem = (Element) nodoEni;

    if (version == null || VERSION_1_0_ENI.equals(version)) {
      nodoEniElem.removeAttribute(NAMESPACE_INSIDEWS_V1_0.split("=")[0]);
      nodoEniElem.removeAttribute(NAMESPACE_NS8_V1_0.split("=")[0]);
      nodoEniElem.removeAttribute(NAMESPACE_NS9_V1_0.split("=")[0]);
    } else if (VERSION_2_0_ENI.equals(version)) {
      nodoEniElem.removeAttribute(NAMESPACE_INSIDEWS_V2_0.split("=")[0]);
      nodoEniElem.removeAttribute(NAMESPACE_NS8_V2_0.split("=")[0]);
      nodoEniElem.removeAttribute(NAMESPACE_NS9_V2_0.split("=")[0]);
    } else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }

    return nodeToString(nodoEni);
  }

  private static String buscarPrefijoNodoNAMESPACE(List<String> listaNameSpaces,
      String NameSpaceABuscar) {
    String prefijo = "";

    for (int i = 0; i < listaNameSpaces.size(); i++) {

      if (listaNameSpaces.get(i).split("=")[1].equalsIgnoreCase(NameSpaceABuscar)) {
        // obtiene la/s parte/s del prefijo y se queda si tiene dos partes con la
        // segunda y si tiene una parte con esa.
        String[] arrayAux = listaNameSpaces.get(i).split("=")[0].split(":");
        String aux = arrayAux.length > 1 ? arrayAux[1] : arrayAux[0];

        if (!aux.equalsIgnoreCase("schemaLocation"))
          prefijo = aux + ":";// le a�ado el dos puntos
      }

    }

    return prefijo;
  }

  /* TIENE QUE EXISTIR SI O SI EN EL PRIMER NODO Y SI NO EN EL SEGUNDO NODO */
  public static String obtenerExpedienteENIXML(String stringXMLExpediente, String version)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {
    // busca el prefijo correspondiente al nodo expediente eni
    String prefijo = "";

    List<String> listaNameSpaces = XMLUtil.getNameSpacesNodoROOT(stringXMLExpediente);

    if (version == null || VERSION_1_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V1_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLExpediente);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V1_0);

      }
    } else if (VERSION_2_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V2_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLExpediente);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V2_0);

      }

    } else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }

    return XMLUtil.addNameSpacesToExpedienteENIXML(XMLUtil
        .getNode(stringXMLExpediente.getBytes(XMLUtil.UTF8_CHARSET), prefijo + TAG_EXPEDIENTE),
        listaNameSpaces, version);

  }

  public static String obtenerExpedienteENIXMLNoProcedenteInside(String stringXMLExpediente,
      String version) throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {
    String prefijo = "";

    List<String> listaNameSpaces = XMLUtil.getNameSpacesNodoROOT(stringXMLExpediente);

    if (version == null || VERSION_1_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V1_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLExpediente);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V1_0);

      }

    }

    else if (VERSION_2_0_ENI.equals(version)) {
      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V2_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLExpediente);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V2_0);

      }
    } else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }

    return nodeToString(XMLUtil.getNode(stringXMLExpediente.getBytes(XMLUtil.UTF8_CHARSET),
        prefijo + TAG_EXPEDIENTE));

  }

  public static String obtenerExpedienteENIXMLDELETENAMESPACES(String stringXMLExpediente,
      String version) throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {
    // busca el prefijo correspondiente al nodo expediente eni
    String prefijo = "";

    List<String> listaNameSpaces = XMLUtil.getNameSpacesNodoROOT(stringXMLExpediente);

    if (version == null || VERSION_1_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V1_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLExpediente);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V1_0);

      }

    } else if (VERSION_2_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V2_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLExpediente);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_EXPEDIENTE_V2_0);

      }

    } else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }



    return XMLUtil.deleteNameSpacesToExpedienteENIXML(XMLUtil.getNode(
        stringXMLExpediente.getBytes(XMLUtil.UTF8_CHARSET), prefijo + TAG_EXPEDIENTE), version);

  }

  public static org.w3c.dom.Node obtenerIndiceContenidoExpediente(String stringXMLExpediente,
      String version) throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {

    List<String> listaNameSpaces = XMLUtil.getNameSpacesNodoROOT(stringXMLExpediente);

    // busca el prefijo correspondiente al nodo indicecontenido
    String prefijo = "";

    if (version == null || VERSION_1_0_ENI.equals(version)) {

      for (int i = 0; i < listaNameSpaces.size(); i++) {

        if (listaNameSpaces.get(i).split("=")[1].equalsIgnoreCase(NAMESPACE_INDICE_v1_0)) {
          prefijo = listaNameSpaces.get(i).split("=")[0].split(":")[1] + ":";// le a�ado el dos
                                                                             // puntos
        }

      }


    }

    else if (VERSION_2_0_ENI.equals(version)) {

      for (int i = 0; i < listaNameSpaces.size(); i++) {

        if (listaNameSpaces.get(i).split("=")[1].equalsIgnoreCase(NAMESPACE_INDICE_v2_0)) {
          prefijo = listaNameSpaces.get(i).split("=")[0].split(":")[1] + ":";// le a�ado el dos
                                                                             // puntos
        }

      }

    }

    else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }



    return XMLUtil.getNode(stringXMLExpediente.getBytes(XMLUtil.UTF8_CHARSET),
        prefijo + TAG_INDICECONTENIDO);

  }

  public static org.w3c.dom.Node obtenerNodoByNamespaceAndTag(String stringXMLExpediente,
      String nameSpace, String tag) throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {

    List<String> listaNameSpaces = XMLUtil.getNameSpacesNodoROOT(stringXMLExpediente);

    // busca el prefijo correspondiente al nodo indicecontenido
    String prefijo = "";
    for (int i = 0; i < listaNameSpaces.size(); i++) {

      if (listaNameSpaces.get(i).split("=")[1].equalsIgnoreCase(nameSpace)) {
        prefijo = listaNameSpaces.get(i).split("=")[0].split(":")[1] + ":";// le a�ado el dos puntos
      }

    }

    return XMLUtil.getNode(stringXMLExpediente.getBytes(XMLUtil.UTF8_CHARSET), prefijo + tag);

  }

  public static String obtenerDocumentoENIXML(String stringXMLDocumento, String version)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerFactoryConfigurationError, TransformerException {

    List<String> listaNameSpaces = XMLUtil.getNameSpacesNodoROOT(stringXMLDocumento);

    // busca el prefijo correspondiente al nodo expediente eni
    String prefijo = "";

    if (version == null || VERSION_1_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_DOCUMENTO_V1_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLDocumento);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_DOCUMENTO_V1_0);

      }

    }

    else if (VERSION_2_0_ENI.equals(version)) {

      prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_DOCUMENTO_v2_0);

      // es quer no lo ha encontrado en el nodo root busca en el siguiente
      if ("".equals(prefijo)) {
        listaNameSpaces.clear();
        listaNameSpaces = XMLUtil.getNameSpacesDelHijoDelROOT(stringXMLDocumento);

        prefijo = buscarPrefijoNodoNAMESPACE(listaNameSpaces, NAMESPACE_DOCUMENTO_v2_0);

      }

    }

    else {
      throw new IOException("Error, Version ENI no puede ser distinta a version 1.0 o version 2.0");
    }

    return XMLUtil.addNameSpacesToExpedienteENIXML(
        XMLUtil.getNode(stringXMLDocumento.getBytes(XMLUtil.UTF8_CHARSET), prefijo + TAG_DOCUMENTO),
        listaNameSpaces, version);

  }

  public static Node getNode(byte[] xml, String tag)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(dbf);

    DocumentBuilder db = dbf.newDocumentBuilder();
    Node nodo = null;
    try (ByteArrayInputStream bArrayIn = new ByteArrayInputStream(xml)) {
      Document dom = db.parse(bArrayIn);


      // El * es comodin e indicamos que coja el primer nodo y recoge el nombre y se
      // lo asigna al tag.
      if ("*".equals(tag)) {
        tag = dom.getDocumentElement().getNodeName();

      }

      nodo = ((Node) dom.getElementsByTagName(tag).item(0));

      return nodo;
    }
  }

  public static String nodeToString(Node node)
      throws TransformerFactoryConfigurationError, TransformerException {

    TransformerFactory tFactory = TransformerFactory.newInstance();
    // habilitamos seguridad para evitar problemas de hijacking.
    // deshabilitamos para evitar validaciones dtd
    // tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    // to be compliant, prohibit the use of all protocols by external entities:
    // tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    // tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    XMLSeguridadFactoria.setPreventAttackExternalTransformerStatic(tFactory);
    Transformer transformer = tFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    Source source = new DOMSource(node);
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    transformer.transform(source, result);
    return sw.toString();
  }

  // public static String getNodeContentSignature(String expression, byte[] data, boolean
  // ommitDeclaration) throws ParserConfigurationException,
  // SAXException, IOException, XPathExpressionException, TransformerException {
  // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  // DocumentBuilder builder = factory.newDocumentBuilder();
  // Document doc = builder.parse(new ByteArrayInputStream(data));
  // XPathFactory xpf = XPathFactory.newInstance();
  // XPath xpath = xpf.newXPath();
  //
  // XPathExpression exprFirst = xpath.compile(expression);
  // //DeferredElementImpl nodeParent = (DeferredElementImpl) exprFirst.evaluate(doc,
  // XPathConstants.NODE);
  // Node nodeParent = (Node) exprFirst.evaluate(doc, XPathConstants.NODE);
  //
  // TransformerFactory transformerFactory = TransformerFactory.newInstance();
  // Transformer transformer = transformerFactory.newTransformer();
  // if (ommitDeclaration) {
  // transformer.setOutputProperty("omit-xml-declaration", "yes");
  // }
  // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
  // DOMSource source = new DOMSource(nodeParent);
  //
  // ByteArrayOutputStream baos = new ByteArrayOutputStream();
  // StreamResult result = new StreamResult(baos);
  // transformer.transform(source, result);
  // return result.getOutputStream().toString();
  // }

  public static String getContentNode(byte[] data, String path)
      throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(factory);

    DocumentBuilder builder = factory.newDocumentBuilder();
    try (ByteArrayInputStream bArrayIn = new ByteArrayInputStream(data)) {
      Document doc = builder.parse(bArrayIn);
      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();

      XPathExpression exprFirst = xpath.compile(path);

      return (String) exprFirst.evaluate(doc, XPathConstants.STRING);
    }
  }

  public static List<String> getContentNodeList(byte[] data, String path)
      throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(factory);

    DocumentBuilder builder = factory.newDocumentBuilder();
    try (ByteArrayInputStream bArrayIn = new ByteArrayInputStream(data)) {
      Document doc = builder.parse(bArrayIn);
      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();

      XPathExpression exprFirst = xpath.compile(path);

      NodeList listaNodos = (NodeList) exprFirst.evaluate(doc, XPathConstants.NODESET);

      List<String> listaValores = new ArrayList<String>();
      for (int i = 0; i < listaNodos.getLength(); i++) {
        Node nodo = listaNodos.item(i);
        listaValores.add(((Node) nodo.getChildNodes().item(0)).getNodeValue());
      }
      return listaValores;
    }

  }

  public static List<String> getContentListAtributoURI(byte[] data, String path)
      throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(factory);

    DocumentBuilder builder = factory.newDocumentBuilder();
    try (ByteArrayInputStream bArrayIn = new ByteArrayInputStream(data)) {
      Document doc = builder.parse(bArrayIn);
      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();

      XPathExpression exprFirst = xpath.compile(path);

      NodeList listaNodos = (NodeList) exprFirst.evaluate(doc, XPathConstants.NODESET);

      List<String> listaValores = new ArrayList<String>();
      for (int i = 0; i < listaNodos.getLength(); i++) {
        Node nodo = listaNodos.item(i);
        listaValores.add(getAtributoURI(nodo));
      }
      return listaValores;
    }

  }

  public static String getvalorNodoDatosXML(String dataXml, String expresionXPath)
      throws SAXException, XPathExpressionException, ParserConfigurationException, IOException {
    return XMLUtil.getContentNode(dataXml.getBytes(UTF8_CHARSET), expresionXPath);

  }

  public static String getvalorNodoDatosXMLByTag(String dataXml, String tag)
      throws IOException, SAXException, ParserConfigurationException {
    org.w3c.dom.Node nodo_Tag = XMLUtil.getNode(dataXml.getBytes(XMLUtil.UTF8_CHARSET), tag);
    Element nodoElem = (Element) nodo_Tag;
    return XMLUtil.getCharacterDataFromElement(nodoElem);

  }

  public static String getCharacterDataFromElement(Element e) {
    Node child = e.getFirstChild();
    if (child instanceof CharacterData) {
      CharacterData cd = (CharacterData) child;
      return cd.getData();
    }
    return "";
  }

  public static String decodeUTF8(byte[] bytes) {
    return new String(bytes, UTF8_CHARSET);
  }

  public static byte[] encodeUTF8(String string) {
    return string.getBytes(UTF8_CHARSET);
  }

  public static boolean getNodeRoot(byte[] xml)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(dbf);

    DocumentBuilder db = dbf.newDocumentBuilder();
    try (ByteArrayInputStream bArrayIn = new ByteArrayInputStream(xml)) {
      Document dom = db.parse(bArrayIn);

      // Indicamos que coja el primer nodo y recoge el nombre y se lo asigna al tag.
      return dom.getDocumentElement().getNodeName().contains("Expediente");
    }

  }

  public static Node getNodoByXpathExpresion(String expression, byte[] data)
      throws ParserConfigurationException, SAXException, IOException, XPathExpressionException,
      TransformerException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // factory.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(factory);

    DocumentBuilder builder = factory.newDocumentBuilder();
    try (ByteArrayInputStream bArrayIn = new ByteArrayInputStream(data)) {
      Document doc = builder.parse(bArrayIn);
      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();

      XPathExpression exprFirst = xpath.compile(expression);
      // DeferredElementImpl nodeParent = (DeferredElementImpl)
      // exprFirst.evaluate(doc, XPathConstants.NODE);
      Node nodeParent = (Node) exprFirst.evaluate(doc, XPathConstants.NODE);
      return nodeParent;
    }
  }

  /*
   * devuelve true si existe el nodo con el tag tagNodo del parametro false si no existe
   */
  public static boolean isNodoBuscadoENI(byte[] ficheroEniBytes, String tagNodo) {
    boolean isExpediente = true;

    String expresionNodoBuscado = "//*[local-name()='" + tagNodo + "']";

    try {
      Node nodo = getNodoByXpathExpresion(expresionNodoBuscado, ficheroEniBytes);
      String nameNodo = nodo.getNodeName();
      logger.debug("Nombre nodo encontrado en isNodoBuscadoENI: " + nameNodo);
      getContentNodeList(ficheroEniBytes, "//*[local-name()='IdentificadorDocumento']");

    } catch (Exception e) {
      isExpediente = false;
    }

    return isExpediente;
  }

  /*
   * devuelve la lista de los valores de un nodo (tagNodo) que puede estar varias veces
   * 
   */
  public static List<String> listaValoresNodo(byte[] ficheroEniBytes, String tagNodo) {
    List<String> listaValores;

    String expresionNodoBuscado = "//*[local-name()='" + tagNodo + "']";

    try {
      listaValores = getContentNodeList(ficheroEniBytes, expresionNodoBuscado);

    } catch (Exception e) {
      logger.error("ERROR en listaValoresNodo al obtener la lista de valores de la etiqueta nodo: "
          + tagNodo);
      listaValores = new ArrayList<String>();
    }

    return listaValores;
  }

  public static HashMap<String, List<byte[]>> unZipFileExpYDocs(byte[] expAndDocsZIPBase64) {

    byte[] buffer = new byte[1024];
    ArrayList<byte[]> listaExpediente = new ArrayList<byte[]>();
    ArrayList<byte[]> listaDocumento = new ArrayList<byte[]>();

    HashMap<String, List<byte[]>> expYDocs = new HashMap<String, List<byte[]>>();

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(expAndDocsZIPBase64));) {
      logger.debug("unZipFile inicio");

      ZipEntry ze = zis.getNextEntry();

      while (ze != null) {

        try (ByteArrayOutputStream arrayBytesFichero = new ByteArrayOutputStream();) {

          int len;
          while ((len = zis.read(buffer)) > 0) {
            arrayBytesFichero.write(buffer, 0, len);
          }

          // System.out.println(ze.getName());
          logger.debug("Nombre ficheroENI encontrado en el ZIP: " + ze.getName());

          if (isNodoBuscadoENI(arrayBytesFichero.toByteArray(), TAG_EXPEDIENTE)) {
            listaExpediente.add(arrayBytesFichero.toByteArray());
          } else {
            listaDocumento.add(arrayBytesFichero.toByteArray());
          }

          ze = zis.getNextEntry();

        }
      }

      zis.closeEntry();

      if (listaExpediente.isEmpty())
        throw new IOException("No se ha detectado fichero expedienteENI en el zip");

      expYDocs.put(KEY_MAPA_LISTAEXPEDIENTE, listaExpediente);
      expYDocs.put(KEY_MAPA_LISTADOCUMENTO, listaDocumento);

      logger.debug("unZipFile fin");

      return expYDocs;

    } catch (IOException ex) {
      logger.error("Error al procesar el fichero zip en unZipFileExpYDocs. " + ex.getMessage());
      return expYDocs;
    }

  }

  public static byte[] getContent(Document dom) {
    Element elementRoot = dom.getDocumentElement();

    Node child = elementRoot.getFirstChild();
    boolean lastChild = false;
    boolean encontrado = false;
    while (!lastChild && !encontrado) {
      encontrado = contieneIdEncoding(child);

      if (child == elementRoot.getLastChild()) {
        lastChild = true;
      } else if (!encontrado) {
        child = child.getNextSibling();
      }
    }

    String b64Content = null;
    byte[] document = null;

    if (encontrado) {
      b64Content = child.getFirstChild().getNodeValue();
      document = Base64.decodeBase64(b64Content);
    }

    return document;

  }

  public static String getContentValorAtributoID(Document dom) {
    Element elementRoot = dom.getDocumentElement();

    Node child = elementRoot.getFirstChild();

    String atributiID = null;
    boolean lastChild = false;
    while (!lastChild && atributiID == null) {
      atributiID = getAtributoIdDeNodoQuecontieneIdEncoding(child);

      if (child == elementRoot.getLastChild()) {
        lastChild = true;
      } else if (atributiID == null) {
        child = child.getNextSibling();
      }
    }

    return atributiID;

  }

  public static String getAtributoIdDeNodoQuecontieneIdEncoding(Node nodo) {

    NamedNodeMap atributos = nodo.getAttributes();

    String atributiID = null;
    // Comprobamos que tenga el atributo Id y el atributo Encoding:
    int i = 0;
    boolean encontrados = false;
    boolean idFound = false;
    boolean encodingFound = false;

    while (atributos != null && i < atributos.getLength() && !encontrados) {
      if (atributos.item(i).getNodeName().equalsIgnoreCase("id")) {
        atributiID = atributos.item(i).getNodeValue();
        idFound = true;
      } else if (atributos.item(i).getNodeName().equalsIgnoreCase("encoding")) {
        encodingFound = true;
      }

      encontrados = idFound && encodingFound;
      i++;
    }

    if (!encontrados)
      atributiID = null;

    return atributiID;
  }

  public static String getAtributoURI(Node nodo) {

    NamedNodeMap atributos = nodo.getAttributes();

    String atributiID = null;
    int i = 0;
    boolean idFound = false;

    while (atributos != null && i < atributos.getLength() && !idFound) {
      if (atributos.item(i).getNodeName().equalsIgnoreCase("URI")) {
        atributiID = atributos.item(i).getNodeValue();
        idFound = true;
      }

      i++;
    }

    return atributiID;
  }


  /**
   * Metodo para comprobar que una firma es xades y que el contenido que trae es un fichero de tipo
   * tcn.
   * 
   * @param contenido la firma
   * @param tipoFirma el tipo de firma.
   * @return true si es una xades con tcn y false en caso contrario.
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static boolean comprobarFirmaXadesEsTcn(byte[] contenido, String tipoFirma)
      throws ParserConfigurationException, SAXException, IOException {

    boolean esMimeTcn = false;

    if (tipoFirma != null && tipoFirma.contains("XADES")) {

      org.w3c.dom.Document dom = XMLUtil.getDOMDocument(contenido, true);
      if (dom != null) {
        NodeList mimeTypeNodeList = dom.getElementsByTagNameNS("*", "MimeType");
        if (mimeTypeNodeList != null) {
          for (int i = 0; i < mimeTypeNodeList.getLength(); i++) {
            Node mimeTypeNode = mimeTypeNodeList.item(i).getFirstChild();
            if ("text/tcn".equals(mimeTypeNode.getNodeValue())
                && (mimeTypeNode.getParentNode().getParentNode().getNodeName()
                    .contains("DataObjectFormat"))
                && (mimeTypeNode.getParentNode().getParentNode().getParentNode().getNodeName()
                    .contains("SignedDataObjectProperties"))) {
              // mime = mimeTypeNode.getNodeValue();
              esMimeTcn = true;
            }
          }
        }
      }
    }

    return esMimeTcn;
  }


  public static boolean isStrBase64(String data) {

    if (data != null) {
      String pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
      Pattern pat = Pattern.compile(pattern);

      Matcher mat = pat.matcher(data);
      if (mat.find()) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }


  /**
   * Comprueba que exista el nodo elementNode dentro del documento XML especificado en filePath
   * 
   * @param file fichero
   * @param elementNode nodo a buscar
   * @return texto encontrado
   * @throws IOException IOException
   * @throws XMLStreamException XMLStreamException
   */
  public static String getNodeValue(byte[] datosFichero, String elementNode)
      throws IOException, XMLStreamException {
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    // desactiva todos los DTDs para evitar código malicioso
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    try (final InputStream is = new ByteArrayInputStream(datosFichero)) {
      XMLStreamReader reader = null;
      try {
        reader = factory.createXMLStreamReader(is, "UTF-8");

        while (reader.hasNext()) {
          final int eventType = reader.next();
          if (eventType == XMLStreamConstants.START_ELEMENT
              && reader.getLocalName().equals(elementNode)) {
            String value = reader.getElementText();
            reader.close();
            return value;
          }
        }
      } finally {
        if (reader != null) {
          reader.close();
        }
      }

    }
    return null;
  }

}
