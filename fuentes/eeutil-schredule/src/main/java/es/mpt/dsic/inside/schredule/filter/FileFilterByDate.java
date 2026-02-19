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

package es.mpt.dsic.inside.schredule.filter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import es.mpt.dsic.inside.utils.file.FileUtil;

/**
 * Saca solo los ficheros que hayan sido modificados hace mas de una hora y no esten en uso.
 * 
 * @author miguel.moral
 *
 */
public class FileFilterByDate implements FilenameFilter {

  @Override
  public boolean accept(File file, String name) {

    File filePath = new File(file.getPath() + File.separator + name);

    if (filePath.isDirectory())
      return false;

    Date dFile = null;
    Date dActual = null;

    try {
      dFile = new Date(filePath.lastModified());
      dActual = new Date();
      org.apache.commons.io.FileUtils.touch(filePath);
    } catch (IOException e) {
      // esta en uso.
      return false;
    }



    if ((dActual.getTime() - dFile.getTime()) > 3600000l) {
      // su ultima modificacion fue hace mas de una hora
      return true;
    } else {
      // se ha modificado hace menos de una hora
      return false;
    }
  }



}
