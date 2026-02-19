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

package es.mpt.dsic.inside.utils.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.mpt.dsic.inside.utils.exception.EeutilException;

public class IOUtil {

  protected static final Log logger = LogFactory.getLog(IOUtil.class);

  private IOUtil() {

  }

  public static byte[] getBytesFromFile(File file) throws EeutilException {
    try (InputStream is = new FileInputStream(file)) {
      long length = file.length();

      if (length > Integer.MAX_VALUE) {
      }

      byte[] bytes = new byte[(int) length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
          && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
        offset += numRead;
      }
      if (offset < bytes.length) {
        throw new IOException("Could not completely read file " + file.getName());
      }

      return bytes;
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  /**
   * Obtiene los bytes a partir de un objeto.
   * 
   * @param source Objeto del que se quieren obtener los bytes.
   * @return array de bytes
   * @throws IOException si no se puede leer la fuente.
   */
  public static byte[] getBytesFromObject(Object source) throws EeutilException {
    byte[] ret = null;

    try {

      if (source instanceof byte[]) {
        byte[] s = (byte[]) source;
        ret = s;
      } else if (source instanceof File) {
        File f = (File) source;
        try (InputStream is = new FileInputStream(f);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();) {
          IOUtils.copyLarge(is, bout);
          ret = bout.toByteArray();
        }

      } else if (source instanceof InputStream) {
        try (InputStream is = (InputStream) source;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();) {
          IOUtils.copyLarge(is, bout);
          ret = bout.toByteArray();
        }
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

    return ret;
  }

  public static byte[] getBytes(InputStream is) throws EeutilException, IOException {

    return readAllBytesCloseIs(is);
  }

  public static byte[] toByteArray(InputStream is) throws EeutilException, IOException {
    return readAllBytesCloseIs(is);
  }

  /*
   * public static byte[] fromInputStreamToByteArray(InputStream is) {
   * 
   * byte[] copyToByteArray=null; try { if (is.markSupported()==true) is.reset(); copyToByteArray =
   * IOUtils.toByteArray(is); //copyToByteArray = FileCopyUtils.copyToByteArray(is); } catch
   * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
   * 
   * return copyToByteArray; }
   */
  /**
   * Copiar a array de byte un input stream cerrando descriptores.
   * 
   * @param inputStream
   * @return
   * @throws IOException
   */
  public static byte[] readAllBytesCloseIs(final InputStream inputStream)
      throws EeutilException, IOException {
    final int bufLen = 4 * 0x400; // 4KB
    byte[] buf = new byte[bufLen];
    int readLen;

    try {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
          outputStream.write(buf, 0, readLen);

        return outputStream.toByteArray();
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    } finally {
      // if (inputStream != null)
      // {
      inputStream.close();
      // }
    }
  }

  /**
   * Formatea el csv: Cada "num" elementos escribe el String "ss" Ejemplo: csv="123456789", num=4,
   * ss="-" devolver�a 1234-6789
   * 
   * @param csv csv a formatear
   * @param num cada cuantos elementos se desea escribir el String "ss"
   * @param ss cadena que se desea introducir
   * @return el csv formateado.
   */
  public static String formatCSV(String csv, int num, String ss) {
    StringBuilder result = new StringBuilder("");
    String resultado = null;

    if (csv.length() < num) {
      result.append(csv);
      // result = new StringBuilder(csv);
    } else {
      int i = 0;
      while (i + num <= csv.length()) {
        String aux = csv.substring(i, i + num);
        result.append(ss);
        result.append(aux);
        // result = result + ss + aux;
        i += num;
      }
      if (i < csv.length()) {
        String aux = csv.substring(i, csv.length());
        result.append(ss);
        result.append(aux);
        // result = result + ss + aux;
      }
      resultado = result.substring(1);
    }
    return resultado;

  }



  public static void main(String args[]) throws IOException {
    IOUtil.esPosibleFormatoFirmaCadesXadesAnalyzeBytes(
        Files.readAllBytes(Paths.get("D:/Descargas/auxiiii.pdf")));
  }


  /**
   * 
   * Evalua los primeros bytes de un fichero para saber si es una "POSIBLE" firma
   * 
   * @return
   * @throws IOException
   */
  public static boolean esPosibleFormatoFirmaCadesXadesAnalyzeBytes(byte[] byteFileAnalyze)
      throws IOException {
    // Comprueba los tres o cuatro primeros bytes de cada fichero
    // PD94 ==> xml (posible xades)
    // MIIY ==> (posible cades)
    // MIM ==> (posible cades)

    byte[] byteXML = Base64.decodeBase64("PD94");
    byte[] byteCades = Base64.decodeBase64("MIIY");
    byte[] byteOtroCades = Base64.decodeBase64("MIM");

    // es un xml (posible firma xades)
    if (byteFileAnalyze[0] == byteXML[0] && byteFileAnalyze[1] == byteXML[1]
        && byteFileAnalyze[2] == byteXML[2]) {
      return true;
    }
    // posible firma cades
    else if (byteFileAnalyze[0] == byteCades[0] && byteFileAnalyze[1] == byteCades[1]
        && byteFileAnalyze[2] == byteCades[2]) {
      return true;
    }
    // posible firma cades
    else if (byteFileAnalyze[0] == byteOtroCades[0] && byteFileAnalyze[1] == byteOtroCades[1]) {
      return true;
    }

    return false;

  }

  /***
   * 
   * @param byteFileAnalyze
   * @return
   * @throws IOException
   */

  public static boolean esPosibleFormatoFirmaCadesXadesPadesExtendedAnalyzeBytes(
      byte[] byteFileAnalyze) throws EeutilException {
    // Comprueba los tres o cuatro primeros bytes de cada fichero
    // PD94 ==> xml (posible xades)
    // MIIY ==> (posible cades)
    // MIM ==> (posible cades)
    // JVBE ==> (posible pades)

    byte[] byteXML = Base64.decodeBase64("PD94");
    byte[] byteCades = Base64.decodeBase64("MIIY");
    byte[] byteOtroCades = Base64.decodeBase64("MIM");
    byte[] bytePades = Base64.decodeBase64("JVBE");

    // es un xml (posible firma xades)
    if (byteFileAnalyze[0] == byteXML[0] && byteFileAnalyze[1] == byteXML[1]
        && byteFileAnalyze[2] == byteXML[2]) {
      return true;
    }
    // posible firma cades
    else if (byteFileAnalyze[0] == byteCades[0] && byteFileAnalyze[1] == byteCades[1]
        && byteFileAnalyze[2] == byteCades[2]) {
      return true;
    }
    // posible firma cades
    else if (byteFileAnalyze[0] == byteOtroCades[0] && byteFileAnalyze[1] == byteOtroCades[1]) {
      return true;
    }
    // posible firma pades
    else if (byteFileAnalyze[0] == bytePades[0] && byteFileAnalyze[1] == bytePades[1]
        && byteFileAnalyze[2] == bytePades[2]) {
      return true;
    }

    return false;

  }


}
