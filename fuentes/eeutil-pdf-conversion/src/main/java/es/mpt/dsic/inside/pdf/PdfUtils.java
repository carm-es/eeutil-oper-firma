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

package es.mpt.dsic.inside.pdf;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

public class PdfUtils {

  protected static final Log logger = LogFactory.getLog(PdfUtils.class);


  public static PdfReader unlockPdf(PdfReader reader) {
    if (reader == null) {
      return reader;
    }
    try {
      Field encryptedField = reader.getClass().getDeclaredField("encrypted");
      encryptedField.setAccessible(true);
      encryptedField.set(reader, false);
    } catch (Exception e) { // ignore
      logger.error("Error desbloqueando PDF, aunque no lanzamos excepcion", e);
    }

    return reader;
  }

  public static void close(PdfReader input) {
    if (input != null) {
      input.close();
    }
  }

  public static void close(Document input) {
    if (input != null) {
      input.close();
    }
  }

  public static void close(PdfWriter input) {
    if (input != null) {
      input.close();
    }
  }

  public static void close(RandomAccessFileOrArray input) throws IOException {
    if (input != null) {
      input.close();
    }
  }

  public static void close(PdfStamper input) throws IOException, DocumentException {
    if (input != null) {
      input.close();
    }
  }

  /***
   * public static void main(String args[]) throws Exception {
   * 
   * PdfReader reader = null; try { reader = new PdfReader("E:/PDF_PROTEGIDO.pdf"); // reader =new
   * PdfReader("E:/Downloads/DOC._1_(2).pdf");
   * 
   * if (reader.isEncrypted()) System.out.println("El fichero esta protegido");
   * 
   * } catch (final BadPasswordException e) { System.out.println("El fichero tiene contrasena");
   * throw e; } catch (Exception e) { throw e; } finally { if (reader != null) reader.close(); }
   * 
   * 
   * try { reader = new PdfReader("E:/PDF_CONTRASENA.pdf"); // reader =new
   * PdfReader("E:/Downloads/DOC._1_(2).pdf");
   * 
   * if (reader.isEncrypted()) System.out.println("El fichero esta protegido");
   * 
   * } catch (final BadPasswordException e) { System.out.println("El fichero tiene contrasena");
   * throw e; } catch (final Exception e) { throw e; } finally { if (reader != null) reader.close();
   * }
   * 
   * 
   * }
   **/
}
