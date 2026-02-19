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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.xml.xmp.DublinCoreSchema;
import com.lowagie.text.xml.xmp.PdfSchema;
import com.lowagie.text.xml.xmp.XmpArray;
import com.lowagie.text.xml.xmp.XmpSchema;
import com.lowagie.text.xml.xmp.XmpWriter;

import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.io.IOUtil;

@Service("pdfConversion")

public class PdfConversion {

  protected final static Log logger = LogFactory.getLog(PdfConversion.class);

  private static final String REDUCED_PREFIX = "reduced";

  /**
   * Anade un documento PDF en una determinada escala a otro documento ya creado.
   * 
   * @param doc Documento ya creado y abierto.
   * @param baos para construir el PdfReader necesario para que no se pierdan los campos de las
   *        plantillas
   * @param pw Writer donde escribiremos el nuevo documento.
   * @param readerEntrada documento que queremos escribir.
   * @param percent Porcentaje que deseamos para nuestro nuevo documento.
   * @param positionX Coordenada X de la posicion absoluta donde vamos a estampar cada pagina del
   *        documento.
   * @param positionY Coordenada Y de la posicion absoluta donde vamos a estampar cada pagina del
   *        documento.
   * @throws IOException
   * @throws DocumentException
   * @throws Exception
   */
  public void addPdfScaled(Document doc,
      // ByteArrayOutputStream baos,
      File entrada, String salidaPath, PdfWriter pw, PdfReader readerEntrada, PdfOptions options,
      PageNumberInfo pageNumberInfo, String sTitle, String sLanguage, List aBookmarks)
      throws EeutilException {

    PdfStamper stamper = null;
    PdfReader readerForm = null;
    FileOutputStream salida = null;
    try {
      salida = new FileOutputStream(salidaPath);
      stamper = new PdfStamper(readerEntrada, salida, PdfWriter.VERSION_1_7);
      stamper.setFormFlattening(true);
      stamper.setFreeTextFlattening(false);

      // anadido titulo
      addTitleXMPMetadata(sTitle, stamper);

      // anadido lenguaje para accesibilidad
      if (sLanguage != null && stamper.getReader().getCatalog() != null) {
        stamper.getReader().getCatalog().put(PdfName.LANG, new PdfString(sLanguage));
      }
      if (aBookmarks != null && !aBookmarks.isEmpty()) {
        stamper.setOutlines(aBookmarks);
      }

      readerForm = new PdfReader(IOUtil.getBytesFromObject(entrada));

      int n = readerEntrada.getNumberOfPages();

      for (int i = 1; i <= n; i++) {

        PdfImportedPage page = pw.getImportedPage(readerForm, i);

        Image img = Image.getInstance(page);

        img.scalePercent(options.getPagePercent());
        img.setAlignment(Element.ALIGN_CENTER);
        img.setAbsolutePosition(options.getPagePositionX(), options.getPagePositionY());

        doc.setPageSize(readerEntrada.getPageSize(i));
        doc.newPage();
        pw.getDirectContent().addImage(img);
        pw.freeReader(readerForm);

        if (options.isPrintPageNumbers()) {
          float x = options.getPageSepHoriz();
          float y = options.getPageSepVerti();

          if (options.getPageSepHoriz() < 0f) {
            x = page.getWidth() + options.getPageSepHoriz();
          }

          if (options.getPageSepVerti() < 0f) {
            y = page.getHeight() + options.getPageSepVerti();
          }

          int actualPage = i;
          int totalPages = n;

          if (pageNumberInfo != null) {
            actualPage = pageNumberInfo.getNumberLastPage() + i;
            totalPages = pageNumberInfo.getNumberTotalPages();
          }
          // add page numbers
          ColumnText.showTextAligned(pw.getDirectContent(), Element.ALIGN_CENTER,
              new Phrase(String.format("page %d of %d", actualPage, totalPages)), x, y, 0);

          // 297.5f, 28, 0);
        }

      }
    } catch (NoClassDefFoundError e) {
      if (e.getMessage().contains("bouncycastle")) {

        // logger.error("El fichero esta protegido y no ha podido abrirse." + entrada.getName(), e);
        throw new EeutilException(
            "El fichero esta protegido y no ha podido abrirse." + entrada.getName(), e);
      } else {
        // logger.error(e.getMessage(), e);
        throw new EeutilException(e.getMessage(), e);
      }

    } catch (Exception e) {
      // logger.error(e.getMessage(), e);
      throw new EeutilException(e.getMessage(), e);
    }

    catch (Throwable t) {
      // logger.error("Error addPdfScaled" + t.getMessage());
      throw new EeutilException("Error addPdfScaled" + t.getMessage(), t);
    } finally {

      try {
        PdfUtils.close(stamper);
        PdfUtils.close(readerForm);
        if (salida != null)
          salida.close();
      } catch (DocumentException | IOException e) {
        throw new EeutilException(e.getMessage(), e);

      }
    }

  }


  private void addTitleXMPMetadata(String sTitle, PdfStamper stamper) throws IOException {
    XmpWriter xmp = null;

    try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
      xmp = new XmpWriter(os);
      XmpSchema dc = new DublinCoreSchema();
      XmpArray array = new XmpArray(XmpArray.ALTERNATIVE);
      array.add(sTitle);
      // SetProperty(TITLE, array);
      dc.setProperty(DublinCoreSchema.TITLE, sTitle);
      // XmpArray subject = new XmpArray(XmpArray.UNORDERED);
      // subject.add("Hello World");
      // subject.add("XMP & Metadata");
      // subject.add("Metadata");
      // dc.setProperty(DublinCoreSchema.SUBJECT, subject);
      xmp.addRdfDescription(dc);
      PdfSchema pdf = new PdfSchema();
      // pdf.setProperty(PdfSchema.KEYWORDS, "Hello World, XMP, Metadata");
      pdf.setProperty(PdfSchema.VERSION, "1.7");
      xmp.addRdfDescription(pdf);
      if (xmp != null)
        xmp.close();
      stamper.setXmpMetadata(os.toByteArray());
    }

  }

}
