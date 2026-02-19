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

package es.mpt.dsic.inside.utils.mime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;



public class MimeUtil {

  private MimeUtil() {

  }

  protected final static Log logger = LogFactory.getLog(MimeUtil.class);

  public final static String DEFAULT_MIME = "application/octet-stream";
  public final static String ZIP_MIME_2 = "application/x-zip";
  public final static String ZIP_MIME_1 = "application/zip";
  public final static String WORD_EXCEL_97_MIME = "application/msword";

  // Falta el atributo private. Si se usa como atributo de clase porque se instancia un objeto?

  /**
   * Obtiene el tipo mime en base al contenido de un fichero. Si no se puede obtener con ninguna
   * utilidad devuelve el mime por defecto.
   * 
   * @param data bytes de los datos.
   * @return El mime obtenido con la utilidad, si no se detecta ninguno devuelve el mime por
   *         defecto.
   */
  public static String getMimeNotNull(byte[] data) {
    String mimeType = getMimeType(data);
    logger.debug("Mime detectado por MIMEUTIL: " + mimeType);
    if (ZIP_MIME_1.equalsIgnoreCase(mimeType) || ZIP_MIME_2
        .equalsIgnoreCase(mimeType)/* || WORD_EXCEL_97_MIME.equalsIgnoreCase(mimeType) */) {
      mimeType = OfficeAnalizer.getMimeType(data);
      logger.debug("Mime detectado por OfficeAnalizer: " + mimeType);
    } ;
    return mimeType != null ? mimeType : DEFAULT_MIME;
  }



  public static String getMimeType(byte[] bytesFichero) {
    try {
      Tika tika = new Tika();
      TikaConfig config = TikaConfig.getDefaultConfig();
      MimeType mimeType = config.getMimeRepository().forName(tika.detect(bytesFichero));

      return mimeType.getName();
    } catch (MimeTypeException e) {
      logger.error("Error al obtener el mimetype: Se devolvera null " + e.getMessage(), e);
      return null;
    }
  }



  public static boolean esMimeTikaPosibleFirma(String mime) {
    if (mime != null) {
      if ("application/pdf".equalsIgnoreCase(mime)) {
        return true;
      } else if ("text/xml".equalsIgnoreCase(mime) || "application/xml".equalsIgnoreCase(mime)) {
        return true;
      } else if ("application/octet-stream".equalsIgnoreCase(mime)) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }



}
