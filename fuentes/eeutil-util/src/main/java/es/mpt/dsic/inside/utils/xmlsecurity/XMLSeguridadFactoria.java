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

package es.mpt.dsic.inside.utils.xmlsecurity;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class XMLSeguridadFactoria {

  public static final String SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES =
      "http://xml.org/sax/features/external-general-entities";
  public static final String SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES =
      "http://xml.org/sax/features/external-parameter-entities";

  private static XMLSeguridadFactoria laInstancia;

  protected static final Log logger = LogFactory.getLog(XMLSeguridadFactoria.class);

  private XMLSeguridadFactoria() {

  }

  public static XMLSeguridadFactoria getInstance() {
    if (laInstancia == null)
      laInstancia = new XMLSeguridadFactoria();

    return laInstancia;
  }


  public void setPreventAttackDocumentBuilderFactoryExternal(DocumentBuilderFactory dbf)
      throws ParserConfigurationException {
    /***
     * //dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // or
     * completely disable external entities declarations: /
     ***/
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    /***
     * // or prohibit the use of all protocols by external entities:
     * //dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
     * //dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // or disable entity expansion
     * but keep in mind that this doesn't prevent fetching external entities // and this solution is
     * not correct for OpenJDK < 13 due to a bug: https://bugs.openjdk.java.net/browse/JDK-8206132
     * //dbf.setExpandEntityReferences(false);
     * 
     */
  }

  public static void setPreventAttackDocumentBuilderFactoryExternalStatic(
      final DocumentBuilderFactory dbf) throws ParserConfigurationException {
    /***
     * //dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // or
     * completely disable external entities declarations:
     * 
     */
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // dbf.setFeature(XMLSeguridadFactoria.SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
    /***
     * // or prohibit the use of all protocols by external entities:
     * //dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
     * //dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // or disable entity expansion
     * but keep in mind that this doesn't prevent fetching external entities // and this solution is
     * not correct for OpenJDK < 13 due to a bug: https://bugs.openjdk.java.net/browse/JDK-8206132
     * //dbf.setExpandEntityReferences(false);
     * 
     */
  }

  public void setPreventAttackExternalTransformer(final TransformerFactory tFactory)
      throws TransformerConfigurationException {
    // habilitamos seguridad para evitar problemas de hijacking.
    // deshabilitamos para evitar validaciones dtd
    tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    // to be compliant, prohibit the use of all protocols by external entities:
    // tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    // tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
  }


  public static void setPreventAttackExternalTransformerStatic(final TransformerFactory tFactory)
      throws TransformerConfigurationException {
    // habilitamos seguridad para evitar problemas de hijacking.
    // deshabilitamos para evitar validaciones dtd
    tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    // to be compliant, prohibit the use of all protocols by external entities:
    // tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    // tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
  }

  public void setPreventAttackExternalSchemaFactory(final SchemaFactory factory)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    /***
     * // to be compliant, completely disable DOCTYPE declaration:
     * //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // or
     * prohibit the use of all protocols by external entities:
     * 
     */
    // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
  }

  public static void setPreventAttackExternalSchemaFactoryStatic(final SchemaFactory factory)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    /***
     * // to be compliant, completely disable DOCTYPE declaration:
     * //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // or
     * prohibit the use of all protocols by external entities:
     * 
     */
    // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
  }


  public void setPreventAttackExternalSaxParserFactoryStatic(final SAXParserFactory factory)
      throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
    /***
     * // to be compliant, completely disable DOCTYPE declaration:
     * //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // or
     * completely disable external entities declarations:
     * 
     */
    // factory.setFeature(SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
    // factory.setFeature(SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
  }

}
