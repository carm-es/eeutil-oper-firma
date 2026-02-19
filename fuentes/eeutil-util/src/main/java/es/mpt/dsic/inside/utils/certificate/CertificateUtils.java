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

package es.mpt.dsic.inside.utils.certificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.mpt.dsic.inside.utils.exception.EeutilException;

public class CertificateUtils {

  private static final String NO_SE_PUEDE_PARSEAR_LA_FIRMA = "No se puede parsear la firma ";


  private CertificateUtils() {

  }


  public static String getCertificateFromSignXML(byte[] sign) throws EeutilException {
    String certificado = null;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    // to be compliant, completely disable DOCTYPE declaration:
    try {
      // XMLSeguridadFactoria.setPreventAttackDocumentBuilderFactoryExternalStatic(dbf);
      dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    } catch (ParserConfigurationException e) {
      throw new EeutilException("PROBLEMAS DE PARSEO " + e.getMessage(), e);
    }

    DocumentBuilder db = null;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new EeutilException(NO_SE_PUEDE_PARSEAR_LA_FIRMA + e.getMessage(), e);
    }
    Document dom = null;
    try (ByteArrayInputStream bArray = new ByteArrayInputStream(sign)) {
      dom = db.parse(bArray);
    } catch (SAXException e) {
      throw new EeutilException(NO_SE_PUEDE_PARSEAR_LA_FIRMA + e.getMessage(), e);
    } catch (IOException e) {
      throw new EeutilException(NO_SE_PUEDE_PARSEAR_LA_FIRMA + e.getMessage(), e);
    }

    certificado = getCertificate(dom);
    return certificado;
  }


  private static String getCertificate(Document dom) throws EeutilException {
    String certificate = null;
    Element elementRoot = dom.getDocumentElement();

    NodeList list = elementRoot.getElementsByTagName("ds:X509Certificate");

    if (list == null) {
      throw new EeutilException("No se ha encontrado ningun nodo ds:X509Certificate");
    } else {
      Node node = list.item(0);
      certificate = node.getFirstChild().getNodeValue();
    }
    return certificate;
  }

}
