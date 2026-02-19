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

package es.mpt.dsic.inside.ws.service.postprocess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import es.mpt.dsic.inside.utils.xmlsecurity.XMLSeguridadFactoria;

public class PostProcessUtil {

  protected static final Log logger = LogFactory.getLog(PostProcessUtil.class);

  // constructor privado hide public
  private PostProcessUtil() {

  }

  /**
   * 
   * @param doc
   * @return
   * @throws TransformerException Las excepciones se quedan como estan
   * @throws IOException Las excepciones se quedan como estan
   */
  public static byte[] xmlDOMtoByteArray(Document doc) throws TransformerException, IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
      Source xmlSource = new DOMSource(doc);
      Result outputTarget = new StreamResult(outputStream);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      // habilitamos seguridad para evitar problemas de hijacking.
      // deshabilitamos para evitar validaciones dtd
      // transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      // to be compliant, prohibit the use of all protocols by external entities:
      // transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      // transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      // XMLSeguridadFactoria.setPreventAttackExternalTransformerStatic(transformerFactory);
      XMLSeguridadFactoria.setPreventAttackExternalTransformerStatic(transformerFactory);
      transformerFactory.newTransformer().transform(xmlSource, outputTarget);
      // return Arrays.copyOf(outputStream.toByteArray(),outputStream.toByteArray().length);



      return outputStream.toByteArray();
    }
  }
}
