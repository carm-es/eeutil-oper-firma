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

import java.awt.print.PageFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfPage;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;

import es.mpt.dsic.inside.pdf.file.StamperWrapper;
import es.mpt.dsic.inside.utils.exception.EeutilException;

@Service("newPdfConversion")
public class NewPdfConversion {

  protected static final Log logger = LogFactory.getLog(NewPdfConversion.class);

  public static final String REDUCED_PREFIX = "newreduced";
  private static final String FIXED_ROTATION_PREFIX = "fixedrotation";
  private static final String CADENA_CONVERSION_PDF = "\nq %s 0 0 %s %s %s cm\nq\n";

  /**
   * Convierte una lista de documentos PDF a otro documento PDF en el que se concatenan todos, por
   * orden, y ademas se reduce el tamanio de las paginas.
   * 
   * @param listaDocs
   * @param options
   * @return
   * @throws Exception
   */
  public Object[] copiaPDFReducidoStamper(StamperWrapper stamperWrapper/* , PdfOptions options */)
      throws EeutilException {


    Object[] aObj = null;

    try {

      aObj = resizePdf(stamperWrapper/* FileUtils.readFileToByteArray(filePdf) */, 50, 85, null);

    }

    catch (EeutilException e) {
      // logger.error(new StringBuilder("Error al obtener PDF
      // reducido.").append(e.getMessage()).toString(), e);
      throw new EeutilException(
          new StringBuilder("Error al obtener PDF reducido.").append(e.getMessage()).toString(), e);
    } catch (Exception e) {
      // logger.error(new StringBuilder("Error al obtener PDF
      // reducido.").append(e.getMessage()).toString(), e);
      throw new EeutilException(
          new StringBuilder("Error al obtener PDF reducido.").append(e.getMessage()).toString(), e);
    }

    return aObj;
  }


  private int obtenerOrientacionPagina(PdfReader reader, int pagina) throws EeutilException {
    try {

      Rectangle rectangle = reader.getPageSizeWithRotation(pagina);

      if (rectangle.getHeight() >= rectangle.getWidth())
        return PageFormat.PORTRAIT;
      else
        return PageFormat.LANDSCAPE;

    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

  }

  public class Rotate extends PdfPageEventHelper {

    protected PdfNumber orientation = PdfPage.PORTRAIT;

    public void setOrientation(PdfNumber orientation) {
      this.orientation = orientation;
    }

    /*
     * TODO: @Override public void onStartPage(PdfWriter writer, Document document) {
     * writer.addPageDictEntry(PdfName.ROTATE, orientation); }
     */
  }

  public Object[] resizePdf(StamperWrapper stamperWrapper, int posx, int posy,
      Integer posVerticalRot90) throws EeutilException {


    Object[] aObj = null;


    try {

      aObj = getResizeNormalOptimized(stamperWrapper, posx, posy, posVerticalRot90);

    } catch (NoClassDefFoundError e) {


      if (e.getMessage().contains("bouncycastle")) {
        // logger.error("El fichero esta protegido y no ha podido abrirse." + " Tama�o del fichero "
        // + file.length / 1000 + "KB.");
        throw new EeutilException("El fichero esta protegido y no ha podido abrirse." +
        // " Tama�o del fichero "
        // + file.length / 1000 + "KB."+
            e.getMessage(), e);
      } else {
        // logger.error(e.getMessage(),e);
        throw new EeutilException(e.getMessage(), e);

      }
    } catch (Exception e) {
      // logger.error(e.getMessage() + " Tama�o del fichero" + file.length / 1000 + "KB.", e);
      throw new EeutilException(e.getMessage()
      // + " Tama�o del fichero" + file.length / 1000 + "KB."
          , e);
    }

    return aObj;

  }

  private void colocarImagen(Image img, int orientation, int posx, int posy)
      throws EeutilException {

    try {
      if (orientation == PageFormat.LANDSCAPE) {
        // horizontal
        img.scaleToFit(770, 523);
        float offsetX = (770 - img.getScaledWidth()) / 2;
        float offsetY = (523 - img.getScaledHeight()) / 2;
        img.setAbsolutePosition(posx + offsetX, posy + offsetY);
      } else {
        // vertical
        img.scaleToFit(523, 770);
        float offsetX = (523 - img.getScaledWidth()) / 2;
        float offsetY = (770 - img.getScaledHeight()) / 2;
        img.setAbsolutePosition(posx + offsetX, posy + offsetY);
      }
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

  }



  private Object[] getResizeNormalOptimized(StamperWrapper stamperWrapper,
      /* PdfReader reader, String rutaSalida, */int posx, int posy, Integer posVerticalRot90)
      throws EeutilException {

    // PdfDictionary root = reader.getCatalog();
    // PdfDictionary form = root.getAsDict(PdfName.ACROFORM);
    // PdfArray fields = form.getAsArray(PdfName.FIELDS);

    boolean bTieneRotaciones = false;

    // devuelve el metadato de titulo y lenguaje si lo posee
    String[] aTituloLenguajePDF = devuelveTituloLenguaje(stamperWrapper.getReader());
    List aBookmarks = devuelveBookmarks(stamperWrapper.getReader());

    String sTituloPdfOriginal = aTituloLenguajePDF[0];
    String sLenguajePDFOriginal = aTituloLenguajePDF[1];

    Object[] aObj = new Object[6];
    List<Integer> listPosicionamiento = new ArrayList();

    PdfDictionary page;
    PdfArray annots;


    try {
      // fOut = new FileOutputStream(rutaSalida);
      // stamper = new PdfStamper(reader, fOut);
      // stamper.setFormFlattening(true);
      // stamper.setFreeTextFlattening(true);
      // stamper.setRotateContents(true);
      // stamper.getWriter().setFullCompression();
      // stamper.setFullCompression();
      // stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_1_7);

      for (int i = 1; i <= stamperWrapper.getReader().getNumberOfPages(); i++) {
        page = stamperWrapper.getReader().getPageN(i);

        // Get all of the annotations for the current page
        PdfArray Annots = page.getAsArray(PdfName.ANNOTS);

        gestionarBorradoLinks(page, stamperWrapper, i);


        int rotacion = 0;

        Rectangle rectangle = stamperWrapper.getReader().getPageSize(i);

        Rectangle rectangleContent = stamperWrapper.getReader().getPageSizeWithRotation(i);

        logger.info("rotation normal" + rectangle.getRotation());
        logger.info("rotation contenido" + rectangleContent.getRotation());

        if (rectangleContent.getHeight() >= rectangleContent.getWidth())
          rotacion = PageFormat.PORTRAIT;
        else
          rotacion = PageFormat.LANDSCAPE;


        int orientacion = rotacion;


        if (rectangleContent.getRotation() == 0) {

          listPosicionamiento.add(rotacion);

          if (rotacion == PageFormat.PORTRAIT) {

            PdfArray cropArray = new PdfArray();

            Rectangle cropBox = stamperWrapper.getReader().getCropBox(i);
            cropArray.add(new PdfNumber(0.0));
            cropArray.add(new PdfNumber(0.0));
            cropArray.add(new PdfNumber(cropBox.getLeft() + 595));
            cropArray.add(new PdfNumber(cropBox.getBottom() + 842));

            page.put(PdfName.CROPBOX, cropArray);
            page.put(PdfName.MEDIABOX, cropArray);

            float factorX = (523 / rectangleContent.getWidth()) * 0.97f;
            float factorY = (770 / rectangleContent.getHeight()) * 0.97f;

            // stamper.setRotateContents(true);
            PdfContentByte cbUnder = stamperWrapper.getStamper().getUnderContent(i);
            PdfContentByte cbOver = stamperWrapper.getStamper().getOverContent(i);

            // cbUnder.getPdfDocument().setPageSize(PageSize.A4.rotate());

            cbUnder.setLiteral(String.format(CADENA_CONVERSION_PDF, factorX, factorY, posx, posy));
            stamperWrapper.getStamper().getOverContent(i)
                .setLiteral(String.format(CADENA_CONVERSION_PDF, factorX, factorY, posx, posy));
            cbOver.setLiteral("\nQ\nQ\n");
          }

          else if (rotacion == PageFormat.LANDSCAPE) {

            PdfArray cropArray = new PdfArray();

            Rectangle cropBox = stamperWrapper.getReader().getCropBox(i);
            cropArray.add(new PdfNumber(0.0));
            cropArray.add(new PdfNumber(0.0));
            cropArray.add(new PdfNumber(cropBox.getLeft() + 842));
            cropArray.add(new PdfNumber(cropBox.getBottom() + 595));

            page.put(PdfName.CROPBOX, cropArray);
            page.put(PdfName.MEDIABOX, cropArray);

            float factorY = (770 / rectangleContent.getWidth()) * 0.97f;
            float factorX = (523 / rectangleContent.getHeight()) * 0.97f;

            // stamper.setRotateContents(true);
            stamperWrapper.getStamper().getUnderContent(i)
                .setLiteral(String.format(CADENA_CONVERSION_PDF, factorY, factorX, posx, posy));
            stamperWrapper.getStamper().getOverContent(i)
                .setLiteral(String.format(CADENA_CONVERSION_PDF, factorY, factorX, posx, posy));
            stamperWrapper.getStamper().getOverContent(i).setLiteral("\nQ\nQ\n");
          }

        }
        // si hay rotacion.
        else {

          orientacion = obtenerOrientacionPagina(stamperWrapper.getReader(), i);

          listPosicionamiento.add(orientacion);

          // si es normal
          if (orientacion == PageFormat.PORTRAIT) {

            // magia
            Image img2 = getImgImportPage(stamperWrapper.getReader(),
                stamperWrapper.getStamper().getWriter(), i);

            ClassLoader classLoader = getClass().getClassLoader();
            File fi = new File(classLoader.getResource("blanco.jpg").getFile());

            Image img = Image.getInstance(fi.getAbsolutePath());
            img.setAlignment(Element.ALIGN_TOP);
            img.setAbsolutePosition(0, 0);
            // meterle una hoja en blanco
            // img.scaleToFit(1342, 795);
            stamperWrapper.getStamper().getOverContent(i).addImage(img);
            img2.setAlignment(Element.ALIGN_TOP);
            img2.setAbsolutePosition(posx, posy);
            colocarImagen(img2, orientacion, 50, 85);
            stamperWrapper.getStamper().getOverContent(i).addImage(img2);
            bTieneRotaciones = true;


          }

          // si es apaisado
          else {

            // magia
            Image img2 = getImgImportPage(stamperWrapper.getReader(),
                stamperWrapper.getStamper().getWriter(), i);
            ClassLoader classLoader = getClass().getClassLoader();
            File fi = new File(classLoader.getResource("blancorotado.jpg").getFile());

            Image img = Image.getInstance(fi.getAbsolutePath());
            img.setAlignment(Element.ALIGN_TOP);
            img.setAbsolutePosition(0, 0);
            // meterle una hoja en blanco
            // img.scaleToFit(795, 1342);
            stamperWrapper.getStamper().getOverContent(i).addImage(img);
            img2.setAlignment(Element.ALIGN_TOP);
            img2.setAbsolutePosition(posx, posy);
            if (posVerticalRot90 == null) {
              colocarImagen(img2, orientacion, 50, 75);
            } else {
              // posicion para el visualizar de visdoc (por el logo)
              colocarImagen(img2, orientacion, 50, posVerticalRot90);
            }

            stamperWrapper.getStamper().getOverContent(i).addImage(img2);
            bTieneRotaciones = true;
          }


        }

      }

    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (EeutilException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (DocumentException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }



    aObj[0] = stamperWrapper;
    aObj[1] = listPosicionamiento;
    aObj[2] = sLenguajePDFOriginal;
    aObj[3] = sTituloPdfOriginal;
    aObj[4] = aBookmarks;
    aObj[5] = bTieneRotaciones;


    return aObj;

  }


  private Image getImgImportPage(PdfReader reader, PdfWriter writer, int page)
      throws EeutilException {

    try {
      // TODO:
      PdfImportedPage importPage = writer.getImportedPage(reader, page);
      Image img = Image.getInstance(importPage);
      // int rotation = importPage.getRotation();

      Rectangle pageR = reader.getPageSizeWithRotation(page);
      int rotation = pageR.getRotation();


      switch (rotation) {
        case 90: {
          img.setRotationDegrees(270);
          break;
        }
        case 180: {
          img.setRotationDegrees(180);
          break;
        }
        case 270: {
          img.setRotationDegrees(90);
          break;
        }
        default: {
          img.setRotationDegrees(0);
          break;
        }
      }
      img.setCompressionLevel(9);
      return img;

    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }
  /*
   * public static void main(String args[]) throws FileNotFoundException, DocumentException,
   * IOException, EeutilException {
   * 
   * 
   * 
   * FileOutputStream fout=new FileOutputStream("E:/Downloads/f_Oficio_con_registro_5506.pdf");
   * 
   * // PdfReader reader = new // PdfReader(
   * "E:/Downloads/Acuse_Notificacionemplazamientorecursocontencioso1_000104_2020interpuestoporelexpropiadodelexpte1_2020.pdf"
   * ); PdfReader reader = new PdfReader("E:/Downloads/PROPUESTA.pdf"); // PdfReader reader = new //
   * PdfReader("E:/Downloads/Oficio_con_registro_5506.pdf"); // PdfReader reader = new
   * PdfReader("E:/Downloads/GEISER_OTRO.pdf"); // PdfReader reader = new
   * PdfReader("E:/Downloads/FORMULARIO.pdf");
   * 
   * // PdfDictionary root = reader.getCatalog(); // PdfDictionary form =
   * root.getAsDict(PdfName.ACROFORM); // PdfArray fields = form.getAsArray(PdfName.FIELDS);
   * 
   * PdfDictionary page; PdfArray annots;
   * 
   * PdfStamper stamper = new PdfStamper(reader, fout); stamper.setFormFlattening(true);
   * stamper.setFreeTextFlattening(true); stamper.setRotateContents(true);
   * stamper.setFullCompression(); stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_1_7);
   * 
   * for (int i = 1; i <= reader.getNumberOfPages(); i++) { page = reader.getPageN(i);
   * 
   * // PdfImportedPage imppage = stamper.getWriter().getImportedPage(reader, i);
   * 
   * int rotacion = 0;
   * 
   * Rectangle rectangle = reader.getPageSize(i);
   * 
   * Rectangle rectangleContent = reader.getPageSizeWithRotation(i);
   * 
   * logger.info("rotation normal" + rectangle.getRotation()); logger.info("rotation contenido" +
   * rectangleContent.getRotation());
   * 
   * if (rectangle.getHeight() >= rectangle.getWidth()) rotacion = PageFormat.PORTRAIT; else
   * rotacion = PageFormat.LANDSCAPE;
   * 
   * // page.put(PdfName.ROTATE, new PdfNumber(270));
   * 
   * // System.out.println("rotacion"+rotacion);
   * 
   * // float factorX=(523/imppage.getWidth())*0.98f; // float
   * factorY=(770/imppage.getHeight())*0.98f;
   * 
   * if (rectangleContent.getRotation() == 0) {
   * 
   * if (rotacion == PageFormat.PORTRAIT) {
   * 
   * PdfArray cropArray = new PdfArray();
   * 
   * Rectangle cropBox = reader.getCropBox(i); cropArray.add(new PdfNumber(0.0)); cropArray.add(new
   * PdfNumber(0.0)); cropArray.add(new PdfNumber(cropBox.getLeft() + 595)); cropArray.add(new
   * PdfNumber(cropBox.getBottom() + 842));
   * 
   * page.put(PdfName.CROPBOX, cropArray); page.put(PdfName.MEDIABOX, cropArray);
   * 
   * float factorX = (523 / rectangleContent.getWidth()) * 0.97f; float factorY = (770 /
   * rectangleContent.getHeight()) * 0.97f;
   * 
   * // stamper.setRotateContents(true); PdfContentByte cbUnder = stamper.getUnderContent(i);
   * PdfContentByte cbOver = stamper.getOverContent(i);
   * 
   * // cbUnder.getPdfDocument().setPageSize(PageSize.A4.rotate());
   * 
   * cbUnder.setLiteral(String.format("\nq %s 0 0 %s %s %s cm\nq\n", factorX, factorY, 50, 85));
   * stamper.getOverContent(i) .setLiteral(String.format("\nq %s 0 0 %s %s %s cm\nq\n", factorX,
   * factorY, 50, 85)); cbOver.setLiteral("\nQ\nQ\n"); }
   * 
   * else if (rotacion == PageFormat.LANDSCAPE) {
   * 
   * PdfArray cropArray = new PdfArray();
   * 
   * Rectangle cropBox = reader.getCropBox(i); cropArray.add(new PdfNumber(0.0)); cropArray.add(new
   * PdfNumber(0.0)); cropArray.add(new PdfNumber(cropBox.getLeft() + 842)); cropArray.add(new
   * PdfNumber(cropBox.getBottom() + 595));
   * 
   * page.put(PdfName.CROPBOX, cropArray); page.put(PdfName.MEDIABOX, cropArray);
   * 
   * float factorY = (770 / rectangleContent.getWidth()) * 0.97f; float factorX = (523 /
   * rectangleContent.getHeight()) * 0.97f;
   * 
   * // stamper.setRotateContents(true); stamper.getUnderContent(i)
   * .setLiteral(String.format("\nq %s 0 0 %s %s %s cm\nq\n", factorY, factorX, 50, 85));
   * stamper.getOverContent(i) .setLiteral(String.format("\nq %s 0 0 %s %s %s cm\nq\n", factorY,
   * factorX, 50, 85)); stamper.getOverContent(i).setLiteral("\nQ\nQ\n");
   * 
   * }
   * 
   * }
   * 
   * // si hay rotacion. else {
   * 
   * int orientacion = new NewPdfConversion().obtenerOrientacionPagina(reader, i);
   * 
   * // si es normal if (orientacion == PageFormat.PORTRAIT) {
   * 
   * // magia Image img2 = new NewPdfConversion().getImgImportPage(reader, stamper.getWriter(), i);
   * Image img = Image.getInstance("blanco.jpg"); img.setAlignment(Element.ALIGN_TOP);
   * img.setAbsolutePosition(0, 0); // meterle una hoja en blanco //img.scaleToFit(842, 842);
   * stamper.getOverContent(i).addImage(img); img2.setAlignment(Element.ALIGN_TOP);
   * img2.setAbsolutePosition(50, 85); new NewPdfConversion().colocarImagen(img2,
   * orientacion,50,85); stamper.getOverContent(i).addImage(img2);
   * 
   * }
   * 
   * // si es apaisado else {
   * 
   * // magia Image img2 = new NewPdfConversion().getImgImportPage(reader, stamper.getWriter(), i);
   * Image img = Image.getInstance("blancorotado.jpg"); img.setAlignment(Element.ALIGN_TOP);
   * img.setAbsolutePosition(0, 0); // meterle una hoja en blanco //img.scaleToFit(842, 842);
   * stamper.getOverContent(i).addImage(img); img2.setAlignment(Element.ALIGN_TOP);
   * img2.setAbsolutePosition(50, 85); new NewPdfConversion().colocarImagen(img2,
   * orientacion,50,85); stamper.getOverContent(i).addImage(img2); }
   * 
   * }
   * 
   * } fout.close(); stamper.close(); reader.close(); }
   */

  /***
   * Metodo que devuelve el titulo y el lenguaje de un documento
   * 
   * @param pdfReader Pdf origen del que sacar el dato. Si son nulos devuelven ambos valores
   * @return String[]
   */
  public static String[] devuelveTituloLenguaje(PdfReader pdfReader) {
    String[] atituloLenguaje = new String[2];
    String sTitle = null;
    String sLanguage = null;

    PdfDictionary pdfDict = pdfReader.getCatalog();
    Map<String, String> info = pdfReader.getInfo();
    if (info != null) {
      if (info.get("Title") != null) {
        sTitle = info.get("Title");
      } else {
        sTitle = "Reporte Eeutils";
      }
    } else {
      sTitle = "Reporte Eeutils";
    }

    if (pdfDict != null) {
      if (pdfDict.getAsString(PdfName.LANG) != null) {
        sLanguage = pdfDict.getAsString(PdfName.LANG).toString();
      } else {
        sLanguage = "es";
      }
    } else {
      sLanguage = "es";
    }

    if (pdfDict != null) {
      sLanguage =
          pdfDict.getAsString(PdfName.LANG) != null ? pdfDict.getAsString(PdfName.LANG).toString()
              : "es";
    } else {
      sLanguage = "es";
    }

    atituloLenguaje[0] = sTitle;
    atituloLenguaje[1] = sLanguage;

    return atituloLenguaje;
  }


  public static List devuelveBookmarks(PdfReader pdfReader) {
    return SimpleBookmark.getBookmarkList(pdfReader);

  }

  /*
   * public static void main(String args[]) throws FileNotFoundException, DocumentException,
   * IOException {
   * 
   * 
   * 
   * String directorio_ficheros= "E:/Downloads/ficheros_prueba_main"; File [] carpeta =
   * listFilesInDirectory(directorio_ficheros);
   * 
   * 
   * 
   * for (File aFile : carpeta) {
   * 
   * 
   * PdfReader reader = new PdfReader(aFile.getPath()); //PdfReader reader = new
   * PdfReader("E:/Downloads/Oficio_con_registro_5506.pdf");
   * 
   * //PdfDictionary root = reader.getCatalog(); //PdfDictionary form =
   * root.getAsDict(PdfName.ACROFORM); //PdfArray fields = form.getAsArray(PdfName.FIELDS);
   * 
   * PdfDictionary page; PdfArray annots;
   * 
   * PdfStamper stamper = new PdfStamper(reader, new
   * FileOutputStream("E:/Downloads/ficheros_prueba_main/resultado_"+aFile.getName ()));
   * stamper.setFormFlattening(true); stamper.setFreeTextFlattening(true);
   * stamper.setRotateContents(true); stamper.setFullCompression();
   * stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_1_7);
   * 
   * for (int i = 1; i <= reader.getNumberOfPages(); i++) { page = reader.getPageN(i);
   * 
   * 
   * 
   * //PdfImportedPage imppage = stamper.getWriter().getImportedPage(reader, i);
   * 
   * 
   * int rotacion=0;
   * 
   * 
   * Rectangle rectangle = reader.getPageSize(i);
   * 
   * Rectangle rectangleContent = reader.getPageSizeWithRotation(i);
   * 
   * 
   * logger.info("rotation normal"+rectangle.getRotation()+" "+aFile.getName() +" pagina "+i);
   * logger.info("rotation contenido"+rectangleContent.getRotation()+" "+aFile.
   * getName()+" pagina "+i);
   * 
   * if(rectangle.getHeight() >= rectangle.getWidth()) rotacion= PageFormat.PORTRAIT; else
   * rotacion=PageFormat.LANDSCAPE;
   * 
   * 
   * //page.put(PdfName.ROTATE, new PdfNumber(270));
   * 
   * 
   * 
   * System.out.println("rotacion"+rotacion);
   * 
   * //float factorX=(523/imppage.getWidth())*0.98f; //float
   * factorY=(770/imppage.getHeight())*0.98f;
   * 
   * 
   * if(rectangleContent.getRotation()==0) {
   * 
   * if(rotacion == PageFormat.PORTRAIT) {
   * 
   * 
   * PdfArray cropArray = new PdfArray();
   * 
   * Rectangle cropBox = reader.getCropBox(i); cropArray.add(new PdfNumber(0.0)); cropArray.add(new
   * PdfNumber(0.0)); cropArray.add(new PdfNumber(cropBox.getLeft() + 595)); cropArray.add(new
   * PdfNumber(cropBox.getBottom() + 842));
   * 
   * 
   * page.put(PdfName.CROPBOX, cropArray); page.put(PdfName.MEDIABOX, cropArray);
   * 
   * 
   * 
   * float factorX=(523/rectangleContent.getWidth())*0.97f; float
   * factorY=(770/rectangleContent.getHeight())*0.97f;
   * 
   * //stamper.setRotateContents(true); PdfContentByte cbUnder= stamper.getUnderContent(i);
   * PdfContentByte cbOver= stamper.getOverContent(i);
   * 
   * //cbUnder.getPdfDocument().setPageSize(PageSize.A4.rotate());
   * 
   * cbUnder.setLiteral( String.format("\nq %s 0 0 %s %s %s cm\nq\n", factorX, factorY, 50, 85));
   * stamper.getOverContent(i).setLiteral(String. format("\nq %s 0 0 %s %s %s cm\nq\n", factorX,
   * factorY, 50, 85)); cbOver.setLiteral("\nQ\nQ\n"); }
   * 
   * else if(rotacion == PageFormat.LANDSCAPE) {
   * 
   * 
   * PdfArray cropArray = new PdfArray();
   * 
   * Rectangle cropBox = reader.getCropBox(i); cropArray.add(new PdfNumber(0.0)); cropArray.add(new
   * PdfNumber(0.0)); cropArray.add(new PdfNumber(cropBox.getLeft() + 842)); cropArray.add(new
   * PdfNumber(cropBox.getBottom() + 595));
   * 
   * 
   * page.put(PdfName.CROPBOX, cropArray); page.put(PdfName.MEDIABOX, cropArray);
   * 
   * 
   * 
   * float factorY=(770/rectangleContent.getWidth())*0.97f; float
   * factorX=(523/rectangleContent.getHeight())*0.97f;
   * 
   * //stamper.setRotateContents(true); stamper.getUnderContent(i).setLiteral(
   * String.format("\nq %s 0 0 %s %s %s cm\nq\n", factorY, factorX, 50, 85));
   * stamper.getOverContent(i).setLiteral(String. format("\nq %s 0 0 %s %s %s cm\nq\n", factorY,
   * factorX, 50, 85)); stamper.getOverContent(i).setLiteral("\nQ\nQ\n"); }
   * 
   * }
   * 
   * 
   * //si hay rotacion. else {
   * 
   * int orientacion = new NewPdfConversion().obtenerOrientacionPagina(reader, i);
   * 
   * 
   * //si es normal if(orientacion==PageFormat.PORTRAIT) {
   * 
   * 
   * //magia Image img2 = new NewPdfConversion().getImgImportPage(reader, stamper.getWriter(), i);
   * Image img = Image.getInstance("blanco.jpg"); img.setAlignment(Element.ALIGN_TOP);
   * img.setAbsolutePosition(0 , 0); //meterle una hoja en blanco img.scaleToFit(842 , 842);
   * stamper.getOverContent(i).addImage(img); img2.setAlignment(Element.ALIGN_TOP);
   * img2.setAbsolutePosition(50 , 85); new NewPdfConversion().colocarImagen(img2, orientacion);
   * //img2.scaleToFit(750 , 750); stamper.getOverContent(i).addImage(img2);
   * 
   * }
   * 
   * //si es apaisado else {
   * 
   * //magia Image img2 = new NewPdfConversion().getImgImportPage(reader, stamper.getWriter(), i);
   * Image img = Image.getInstance("blanco.jpg"); img.setAlignment(Element.ALIGN_TOP);
   * img.setAbsolutePosition(0 , 0); //meterle una hoja en blanco img.scaleToFit(842 , 842);
   * stamper.getOverContent(i).addImage(img); img2.setAlignment(Element.ALIGN_TOP);
   * img2.setAbsolutePosition(50 , 85); new NewPdfConversion().colocarImagen(img2, orientacion);
   * //img2.scaleToFit(500,500); stamper.getOverContent(i).addImage(img2); }
   * 
   * 
   * }
   * 
   * 
   * }
   * 
   * 
   * 
   * 
   * stamper.close(); reader.close();
   * 
   * } // fin bucle for de ficheros }
   * 
   * private static File [] listFilesInDirectory(String pathString) { // A local class (a class
   * defined inside a block, here a method). class MyFilter implements FileFilter {
   * 
   * @Override public boolean accept(File file) { return !file.isHidden() &&
   * file.getName().endsWith(".pdf"); } }
   * 
   * File directory = new File(pathString); File[] files = directory.listFiles(new MyFilter());
   * 
   * return files; }
   */


  // public static final String SRC =
  // "C:\\Users\\miguel.moral\\Desktop\\accessible-pdf-example.pdf";
  // public static final String DEST = "C:\\\\Users\\\\miguel.moral\\\\Desktop\\\\kk.pdf";
  // public static void main(String[] args) throws IOException, DocumentException,
  // NoSuchFieldException, SecurityException {
  // File file = new File(DEST);
  // file.getParentFile().mkdirs();
  // new NewPdfConversion().manipulatePdf(SRC, DEST);
  // }


  /***
   * public static void main(String args[]) throws IOException { File fSalida = new
   * File("d:/Descargas/auxiiii.pdf"); FileOutputStream fos = null; PdfWriter writer = null;
   * PdfReader reader= new PdfReader("d:/Descargas/comparado.pdf.bak.pdf");
   * 
   * 
   * 
   * Document pdfDocument = new Document();
   * 
   * //String sFileFixedRotation = FileUtil.createFilePath(FIXED_ROTATION_PREFIX) + ".pdf";
   * 
   * //fSalida = new File(sFileFixedRotation);
   * 
   * try { fos = new FileOutputStream(fSalida);
   * 
   * writer = PdfWriter.getInstance(pdfDocument, fos);
   * writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7); writer.open(); pdfDocument.open(); for (int
   * i=1 ; i<= reader.getNumberOfPages(); i++) { PdfImportedPage importPage =
   * writer.getImportedPage(reader, i);
   * 
   * Rectangle rectangle=reader.getPageSizeWithRotation(i);
   * 
   * System.err.print(" "+ rectangle.getRotation());
   * 
   * if(rectangle.getHeight() >= rectangle.getWidth()) { pdfDocument.setPageSize(PageSize.A4);
   * pdfDocument.newPage(); } else { pdfDocument.setPageSize(PageSize.A4.rotate());
   * pdfDocument.newPage(); }
   * 
   * 
   * 
   * Image img = Image.getInstance(importPage);
   * 
   * 
   * if(reader.getPageSizeWithRotation(i).getRotation() != 0) { img.setRotationDegrees(360f -
   * reader.getPageSizeWithRotation(i).getRotation()); }
   * 
   * if (rectangle.getHeight() >= rectangle.getWidth()) img.scaleToFit(525, 770); else
   * img.scaleToFit(770, 525);
   * 
   * 
   * 
   * 
   * 
   * pdfDocument.add(img);
   * 
   * }
   * 
   * }catch(Exception e) { } finally { if(pdfDocument!=null) pdfDocument.close(); if(writer!=null)
   * writer.close(); if(fos!=null) try { fos.close(); } catch (IOException e) {
   * logger.error(e.getMessage()); } if(reader!=null) reader.close();
   * 
   * }
   * 
   * 
   * 
   * }
   * 
   **/

  /**
   * Los links se pondran a un tamano de 0 px puesto que al redimensionar se descolocan y no de
   * manera lineal (mas espacio arriba que abajo del documento)
   * 
   * @param page
   * @param stamperWrapper
   * @param numberPage
   */
  public void gestionarBorradoLinks(PdfDictionary page, StamperWrapper stamperWrapper,
      int numberPage) {

    PdfArray annots = page.getAsArray(PdfName.ANNOTS);
    if (annots != null) {
      // List<Integer> listLinkBorrables = new ArrayList<>();
      // int i=0;

      int idx = 0;
      boolean isBorrado = false;
      for (PdfObject annot : annots.getElements()) {

        // Get Annotation from PDF File
        PdfDictionary annotationDic =
            (PdfDictionary) stamperWrapper.getReader().getPdfObject(annot);
        PdfName subType = (PdfName) annotationDic.get(PdfName.SUBTYPE);

        // System.out.println(subType);
        // check only subtype is link
        if (subType != null && subType.equals(PdfName.LINK)) {


          annots.remove(idx);
          isBorrado = true;

          // listLinkBorrables.add(i);

          // Get Quadpoints and Rectangle of highlighted text
          // System.out.println("HighLight at Rectangle {0} with QuadPoints {1}\n"+
          // annotationDic.getAsArray(PdfName.RECT)+ "Pagina "+ numberPage);

          // Extract Text using rectangle strategy
          // PdfArray coordinates = annotationDic.getAsArray(PdfName.RECT);

          // coordinates.set(0, new PdfNumber(0));
          // coordinates.set(1, new PdfNumber(0));
          // coordinates.set(2, new PdfNumber(0));
          // coordinates.set(3, new PdfNumber(0));

          // coordinates.set(0, coordinates.getAsNumber(0));
          // coordinates.set(1, new PdfNumber(coordinates.getAsNumber(1).floatValue()+100));
          // coordinates.set(2, coordinates.getAsNumber(2));
          // coordinates.set(3, new PdfNumber(coordinates.getAsNumber(3).floatValue()+100));

        }
        if (!isBorrado) {
          idx += 1;
        } else {
          isBorrado = false;
        }

      }

      // Collections.reverse(listLinkBorrables);
      //
      // //borramos los links (indices recopilados)
      // for(Integer elemLinkBorrables : listLinkBorrables)
      // {
      // annots.remove(elemLinkBorrables);
      // }
    }

  }


}
