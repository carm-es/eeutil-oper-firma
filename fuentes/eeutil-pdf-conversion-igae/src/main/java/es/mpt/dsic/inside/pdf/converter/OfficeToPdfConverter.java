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

package es.mpt.dsic.inside.pdf.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;

import es.mpt.dsic.inside.aop.AuditExternalServiceAnnotation;
import es.mpt.dsic.inside.pdf.exception.PdfConversionException;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.file.FileUtil;

@Component
@Deprecated
public class OfficeToPdfConverter {

  protected static final Log logger = LogFactory.getLog(OfficeToPdfConverter.class);

  // tiempo del timeout al ejecutar libreoffice en segundos.
  private long TIMEOUT_LIBREOFFICE = 120;

  private static final String OFFICE_PREFIX = "office";

  @Deprecated
  public File convertOfficeToPDFByMIME(File entrada, String fileExtension, String ipOOffice,
      String portOOfice) throws EeutilException {

    try {

      if (StringUtils.isNotEmpty(ipOOffice) && StringUtils.isNotEmpty(portOOfice)) {
        int port = Integer.parseInt(portOOfice);
        return jodConverter(entrada, fileExtension, ipOOffice, port);
      } else {
        throw new EeutilException("Debe definir correctamente el servicio de conversion office");
      }

    } catch (IOException | PdfConversionException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (InterruptedException e) {
      logger.error(e, e);
      Thread.currentThread().interrupt();
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  /**
   * JODConverter automates all conversions supported by OpenOffice.org, including
   * 
   * Microsoft Office to OpenDocument, and viceversa o Word to OpenDocument Text (odt); OpenDocument
   * Text (odt) to Word o Excel to OpenDocument Spreadsheet (ods); OpenDocument Spreadsheet (ods) to
   * Excel o PowerPoint to OpenDocument Presentation (odp); OpenDocument Presentation (odp) to
   * PowerPoint Any format to PDF o OpenDocument (Text, Spreadsheet, Presentation) to PDF o Word to
   * PDF; Excel to PDF; PowerPoint to PDF o RTF to PDF; WordPerfect to PDF; ... And more o
   * OpenDocument Presentation (odp) to Flash; PowerPoint to Flash o RTF to OpenDocument;
   * WordPerfect to OpenDocument o Any format to HTML (with limitations) o Support for
   * OpenOffice.org 1.0 and old StarOffice formats o .
   * 
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated
  private File jodConverter(File entrada, final String fileExtension, String ipOOffice,
      int portOOfice) throws PdfConversionException, IOException, InterruptedException {
    // FileOutputStream out = null;
    // FileInputStream fis = null;

    String filePath = FileUtil.createFilePath(OFFICE_PREFIX);

    final OpenOfficeConnection connection = new SocketOpenOfficeConnection(ipOOffice, portOOfice);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (final FileOutputStream out = new FileOutputStream(filePath);
        final FileInputStream fis = new FileInputStream(entrada);) {
      // out = new FileOutputStream(filePath);

      try {
        executeConvertOpenOfficeInThreadContext(fileExtension, connection, executor, out, fis);
      } catch (InterruptedException e) {
        // logger.error("Error en el hilo de ejecucion para ejecutar libreoffice",e);
        // throw new IOException("Error en el hilo de ejecucion para ejecutar libreoffice");
        logger.error(
            "Error, se ha producido un error de tipo Interrupt exception, procedemos a interrumpir el hilo activo "
                + e.getMessage(),
            e);
        Thread.currentThread().interrupt();
        throw new IOException(e.getMessage(), e);

      } catch (ExecutionException e) {
        // logger.error("Error en el hilo de ejecucion para ejecutar libreoffice",e);
        throw new IOException("Error en el hilo de ejecucion para ejecutar libreoffice");
      } catch (TimeoutException e) {
        // logger.error("Timeout al ejecutar convertir de libreoffice, se ha superado el maximo de
        // tiempo activo"+ TIMEOUT_LIBREOFFICE,e);
        throw new IOException(e);
      } finally {
        if (connection != null && connection.isConnected()) {
          logger.debug("HACEMOS LA DESCONEXION DE OPENOFFICE");
          connection.disconnect();
        }
      }

    }
    return new File(filePath);

  }

  @Deprecated
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-pdf-conversion-igae")
  private void executeConvertOpenOfficeInThreadContext(final String fileExtension,
      final OpenOfficeConnection connection, ExecutorService executor, final FileOutputStream out,
      final FileInputStream fis) throws InterruptedException, ExecutionException, TimeoutException {
    try {

      Future<String> future = executor.submit(new Callable<String>() {
        @Override
        public String call() throws Exception {

          try {
            connectOpenOffice(connection);
            DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection);
            DefaultDocumentFormatRegistry registry = new DefaultDocumentFormatRegistry();
            DocumentFormat inFormat = registry.getFormatByMimeType(fileExtension);
            DocumentFormat outFormat = registry.getFormatByFileExtension("pdf");
            converter.convert(fis, inFormat, out, outFormat);

          } catch (PdfConversionException e) {
            throw e;
          } catch (Exception t) {
            throw new PdfConversionException(
                "Error convirtiendo a PDF from Office.Mime " + fileExtension + " " + t.getMessage(),
                t);
          }


          return "ok";
        }
      });
      // el hilo de ejecucion tiene n segundos para procesar la conversion.
      // sino generara un timeout.
      future.get(TIMEOUT_LIBREOFFICE, TimeUnit.SECONDS);

    } catch (TimeoutException e) {
      // logger.error("Timeout al convertir el fichero. Ha superado el tiempo de conversion al
      // llamar a openoffice "+TIMEOUT_LIBREOFFICE+" segundos",e);
      throw new TimeoutException(
          "Timeout al convertir el fichero. Ha superado el tiempo de conversion al llamar a openoffice "
              + TIMEOUT_LIBREOFFICE + " segundos");
    }

    finally {
      if (connection != null && connection.isConnected()) {
        logger.debug("HACEMOS DESCONEXION DE OPENOFFICE");
        connection.disconnect();
      }
      executor.shutdownNow();
    }

  }

  @Deprecated
  private void connectOpenOffice(OpenOfficeConnection connection) throws PdfConversionException {
    try {
      connection.connect();
    } catch (Exception e) {
      throw new PdfConversionException(
          "No se puede abrir la conexi�n con el servidor de openoffice", e);
    }
  }


}
