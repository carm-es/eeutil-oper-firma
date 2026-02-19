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

package es.mpt.dsic.inside.utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.mpt.dsic.inside.utils.exception.EeutilException;

public class FileUtil {

  private static final String FILE_SEPARATOR = "file.separator";

  private FileUtil() {

  }

  protected static final Log logger = LogFactory.getLog(FileUtil.class);

  private static String lastName;

  /**
   * Metodo para generar un nombre unico de fichero En principio sera el milisegundo actual mas el
   * identificador de thread. Si ese nombre coincide con el ultimo que se genera, entonces se le
   * anade un sufijo.
   * 
   * @return
   */
  private static synchronized String uniqueName() {
    String currentName = System.currentTimeMillis() + "" + Thread.currentThread().getId();
    if (lastName != null && lastName.contentEquals(currentName)) {
      lastName = lastName + "suffix";
    } else {
      lastName = currentName;
    }
    return lastName;

  }

  /**
   * Devuelve la ruta del directorio temporal.
   * 
   * @return
   */
  public static String getTmpDir() {
    return System.getProperty("java.io.tmpdir");
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, según el patrón:
   * currentTimeMillis () + id-thread-actual.
   * 
   * @return
   */
  public static String createFilePath() {
    return createFilePath(null);
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, según el patrón: prefix +
   * uniqueName().
   * 
   * @param prefix Si es nulo, no se mete en el nombre.
   * @return
   */
  public static String createFilePath(String prefix) {

    String s = getTmpDir();
    s += System.getProperty(FILE_SEPARATOR);
    s += prefix != null ? prefix : "";
    s += uniqueName();

    /*
     * String s = getTmpDir() + System.getProperty("file.separator") + prefix != null ? prefix : ""
     * + System.currentTimeMillis() + Thread.currentThread().getId();
     */
    return s;
  }

  /***
   * 
   * @param bytesFile
   * @return
   * @throws IOException. La excepcion no se cambia, se evalua en el siguiente nivel
   */
  public static ZipFile createTempZipFile(byte[] bytesFile) throws IOException {
    String tmpDir = getTmpDir();
    String tmpFilePath =
        tmpDir + System.getProperty(FILE_SEPARATOR) + "zip" + System.currentTimeMillis();
    try (FileOutputStream fout = new FileOutputStream(tmpFilePath);) {

      logger.debug("Fichero temporal: " + tmpFilePath);
      fout.write(bytesFile);

      logger.debug("Fichero temporal: " + tmpFilePath + " creado ");
      return new ZipFile(tmpFilePath);
    }

  }

  /**
   * Crea un directorio en el directorio temporal y devuelve el objeto File
   * 
   * @param dirName Nombre del directorio
   * @return
   */
  public static boolean createTempDir(String dirName) {

    boolean bCreado = false;

    String tempPath = getTmpDir();
    logger.debug("tempPath " + tempPath);
    String sep = System.getProperty(FILE_SEPARATOR);

    String dirSalida = tempPath + sep + dirName;
    logger.debug("dirSalida " + dirSalida);

    File dir = new File(dirSalida);
    if (!dir.exists() || !dir.isDirectory()) {
      bCreado = dir.mkdir();
    }

    return bCreado;

  }

  /**
   * Borra un directorio y lo que hay dentro
   * 
   * @param dir
   * @throws IOException
   */
  /*
   * public static void deleteDirRecursively(File dir) { if (dir != null) { File[] files =
   * dir.listFiles(); if (files != null) { for (File f : files) { if (f.isDirectory()) {
   * deleteDirRecursively(f); } else { try { f.delete(); } catch (Exception e) {
   * logger.warn("No se ha podido eliminar el fichero:" + f.getAbsolutePath()); } } } } try {
   * dir.delete(); } catch (Exception e) { logger.warn("No se ha podido eliminar el fichero:" +
   * dir.getAbsolutePath()); } } }
   */

  public static void deleteDirRecursively(Path path) throws EeutilException {

    try {
      // Path path= Paths.get(dir.getAbsolutePath());
      if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
          for (Path entry : entries) {
            deleteDirRecursively(entry);
          }
        }
      }
      Files.delete(path);
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  /**
   * Obtiene una lista de los ficheros con una extensión dentro de un directorio. Si el directorio
   * no se puede acceder, no existe o no contiene ficheros devuelve una lista vac�a.
   * 
   * @param f Directorio donde se desea buscar
   * @param extension extensión de los ficheros.
   * @return
   */
  public static List<File> getFilesInFolder(File f, String extension) {
    List<File> files = new ArrayList<>();

    if (f != null) {
      if (!f.isDirectory() || !f.canRead()) {
        return files;
      }

      File[] filesIn = f.listFiles();

      if (filesIn != null) {
        for (File file : filesIn) {
          if (file.getName().endsWith(extension)) {
            files.add(file);
          }
        }
      }
    }

    return files;
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, según el patrón: prefix +
   * uniqueName().
   * 
   * @param prefix Si es nulo, no se mete en el nombre.
   * @return
   * @throws IOException
   */
  public static String createFilePath(String prefix, byte[] contenido) throws EeutilException {
    String inputPathFile = null;
    inputPathFile = createFilePath(prefix);
    try (FileOutputStream fos = new FileOutputStream(inputPathFile);) {
      if (contenido != null) {
        fos.write(contenido);
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return inputPathFile;
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, según el patrón: prefix +
   * uniqueName().
   * 
   * @param prefix Si es nulo, no se mete en el nombre.
   * @return
   * @throws IOException
   */
  public static String createFilePath(String prefix, byte[] contenido, String suffix)
      throws EeutilException {
    String inputPathFile = null;
    inputPathFile = createFilePath(prefix) + suffix;
    try (FileOutputStream fos = new FileOutputStream(inputPathFile);) {
      fos.write(contenido);
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return inputPathFile;
  }

  public static void close(FileInputStream input) throws EeutilException {

    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  public static void close(FileOutputStream input) throws EeutilException {
    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  public static void close(ByteArrayOutputStream input) throws EeutilException {
    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  public static void close(ByteArrayInputStream input) throws EeutilException {
    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  /**
   * @param stream
   * @throws IOException
   */
  public static void close(InputStream stream) throws EeutilException {

    try {
      stream.close();
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }
  }

  /*
   * public static void deleteFilesStartWidth(String path, String prefix) { File tmpdir = new
   * File(path); if (tmpdir.listFiles() != null) { for (File tmpfile : tmpdir.listFiles()) { if
   * (tmpfile.getName().startsWith(prefix)) { try { tmpfile.delete(); } catch (Exception e) {
   * logger.warn("No se ha podido eliminar el fichero:" + tmpfile.getAbsolutePath()); } } } } }
   */
}
