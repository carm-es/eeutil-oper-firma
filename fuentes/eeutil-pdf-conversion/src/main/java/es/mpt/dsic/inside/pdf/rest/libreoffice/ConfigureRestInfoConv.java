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

package es.mpt.dsic.inside.pdf.rest.libreoffice;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import es.mpt.dsic.inside.utils.exception.EeutilException;

@Component
public class ConfigureRestInfoConv {

  /*** INICIO LIBREOFFICE ****/

  private static String baseUrlSecureLibreoffice;
  private static boolean activeNewLibreoffice;

  private static String tokenNameLibreoffice;
  private static String tokenValueLibreoffice;

  /*** FIN LIBREOFFICE ***/



  public static String getBaseUrlSecureLibreoffice() {
    return baseUrlSecureLibreoffice;
  }



  public static boolean isActiveNewLibreoffice() {
    return activeNewLibreoffice;
  }



  public static String getTokenNameLibreoffice() {
    return tokenNameLibreoffice;
  }



  public static String getTokenValueLibreoffice() {
    return tokenValueLibreoffice;
  }



  @PostConstruct
  public static void cargarRutasRelativas() throws EeutilException, IOException {

    String rutaVisDocPath = System.getProperty("eeutil-vis-docexp.config.path");
    String rutaMiscPath = System.getProperty("eeutil-misc.config.path");
    String rutaUtilFirmaPath = System.getProperty("eeutil-util-firma.config.path");


    // obtenemos el path de las propiedades externas.
    String path =
        (rutaVisDocPath == null
            ? (rutaMiscPath == null ? (rutaUtilFirmaPath != null ? rutaUtilFirmaPath : null)
                : rutaMiscPath)
            : rutaVisDocPath);
    // String path=EeutilApplicationDataConfig.CONFIG_PATH;

    // FileReader fReader= null;

    try (FileReader fReader = new FileReader(path + "/" + "eeutil.properties");) {
      // fReader=new FileReader(path+"/"+"eeutil.properties");

      Properties prop = new Properties();
      prop.load(fReader);

      baseUrlSecureLibreoffice = prop.getProperty("eeutil.libreoffice.rs.base.url");
      tokenNameLibreoffice = prop.getProperty("eeutil.libreoffice.token.name");
      tokenValueLibreoffice = prop.getProperty("eeutil.libreoffice.token.value");
      activeNewLibreoffice = Boolean.parseBoolean(prop.getProperty("libreoffice.active"));

    }

  }

}
