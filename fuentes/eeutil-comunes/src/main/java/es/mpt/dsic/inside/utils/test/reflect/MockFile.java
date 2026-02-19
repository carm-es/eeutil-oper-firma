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

package es.mpt.dsic.inside.utils.test.reflect;

import java.io.File;

public class MockFile {


  public static boolean esFichero(String ruta) throws SecurityException {
    File f = new File(ruta);

    return f.exists() && f.isFile();
  }


  public static boolean esDirectorio(String ruta) throws SecurityException {
    File f = new File(ruta);

    return f.exists() && f.isDirectory();
  }


  private static boolean esDirectorioOtro(String ruta) throws SecurityException {
    File f = new File(ruta);

    return f.exists() && f.isDirectory();
  }

}
