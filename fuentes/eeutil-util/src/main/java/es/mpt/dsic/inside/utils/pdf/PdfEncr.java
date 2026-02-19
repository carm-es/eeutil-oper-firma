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

package es.mpt.dsic.inside.utils.pdf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfReader;

import es.mpt.dsic.inside.utils.exception.EeutilException;

/**
 * Utilidades de PDF.
 */
public class PdfEncr {

  /**
   * Tama&ntilde;o m&iacute;nimo de un PDF. <a href=
   * "https://stackoverflow.com/questions/17279712/what-is-the-smallest-possible-valid-pdf">
   * https://stackoverflow.com/questions/17279712/what-is-the-smallest-possible-valid-pdf </a>.
   */
  private static final int PDF_MIN_FILE_SIZE = 70;

  /** Cabecera de los documentos PDF. */
  private static final String PDF_FILE_HEADER = "%PDF-"; //$NON-NLS-1$

  protected static final Log logger = LogFactory.getLog(PdfEncr.class);

  /**
   * Comprueba si los datos proporcionados se corresponden con un PDF protegido con
   * contrase&ntilde;a.
   * 
   * @param document Documento que se desea comprobar.
   * @return {@code true} si el documento es un PDF con contrase&ntilde;a, {@code false} en caso
   *         contrario.
   */
  public static boolean isProtectedPdf(final byte[] document) throws EeutilException {



    // Comprobamos que tenga la longitud minima para ser PDF
    if (document == null || document.length < PDF_MIN_FILE_SIZE) {
      return false;
    }

    // Comprobamos que cuente con una cabecera PDF
    final byte[] pdfHeader = Arrays.copyOf(document, PDF_FILE_HEADER.length());
    if (!PDF_FILE_HEADER.equals(new String(pdfHeader, StandardCharsets.UTF_8))) {
      return false;
    }


    // si no ha salido ya es que es un pdf, vamos a ver si esta
    // protegido con contrasena

    PdfReader r = null;

    try {

      r = new PdfReader(document);

    } catch (BadPasswordException e) {
      return true;
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {

      if (e instanceof NullPointerException) {
        throw new EeutilException(
            "El fichero PDF esta corrupto o no se ha podido leer correctamente", e);
      } else {
        throw new EeutilException(e.getMessage(), e);
      }



    } finally {
      if (r != null)
        r.close();
    }

    return false;



  }

  public static void main(String args[]) throws Exception {

    PdfReader reader = null;
    try {
      reader = new PdfReader("E:/PDF_PROTEGIDO.pdf");
      // reader =new PdfReader("E:/Downloads/DOC._1_(2).pdf");

      if (reader.isEncrypted())
        throw new Exception("El fichero esta protegido");

    } catch (final BadPasswordException e) {
      throw e;
    } catch (final NoClassDefFoundError | ClassNotFoundException e) {
      throw e;
    } finally {
      if (reader != null)
        reader.close();
    }

  }
}
