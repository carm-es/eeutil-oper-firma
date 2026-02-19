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

package es.mpt.dsic.inside.pdf.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.file.FileUtil;

public class StamperWrapper {

  private String nameFileFileOutPrefix;

  private PdfReader reader;

  private FileOutputStream fOut;

  private PdfStamper stamper;



  public String getNameFileFileOutPrefix() {
    return nameFileFileOutPrefix;
  }

  public PdfReader getReader() {
    return reader;
  }

  public FileOutputStream getfOut() {
    return fOut;
  }

  public PdfStamper getStamper() {
    return stamper;
  }

  /**
   * Creacion de un nombre de fichero de salida
   * 
   * @param preffix, prefijo del nombre del fichero.
   * @param extension, extension con punto
   * @return
   */
  public String createFilePrefix(String preffix, String extension) throws EeutilException {
    return FileUtil.createFilePath(preffix) + extension;
  }

  public void createStamperWrapper(byte[] fileContent, String nameFileOutPrefix)
      throws EeutilException {
    try {
      reader = new PdfReader(fileContent);
      nameFileFileOutPrefix = nameFileOutPrefix;
      fOut = new FileOutputStream(nameFileFileOutPrefix);
      stamper = new PdfStamper(reader, fOut);
      stamper.setFormFlattening(true);
      stamper.setFreeTextFlattening(false);
      stamper.setRotateContents(true);
      // stamper.getWriter().setFullCompression();
      stamper.setFullCompression();
      stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_1_7);
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  public void createStamperWrapper(File nameFile, String nameFileOutPrefix) throws EeutilException {
    try {
      reader = new PdfReader(FileUtils.readFileToByteArray(nameFile));
      nameFileFileOutPrefix = nameFileOutPrefix;
      fOut = new FileOutputStream(nameFileFileOutPrefix);
      stamper = new PdfStamper(reader, fOut);
      stamper.setFormFlattening(true);
      stamper.setFreeTextFlattening(false);
      stamper.setRotateContents(true);
      // stamper.getWriter().setFullCompression();
      stamper.setFullCompression();
      stamper.getWriter().setPdfVersion(PdfWriter.PDF_VERSION_1_7);
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  public void closeAll() throws EeutilException {

    try {
      if (stamper != null)
        stamper.close();
      if (fOut != null)
        fOut.close();
      if (reader != null)
        reader.close();

    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }



}
