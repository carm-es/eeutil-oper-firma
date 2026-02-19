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

package es.mpt.dsic.inside.schredule;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import es.mpt.dsic.inside.schredule.filter.FileFilterByDate;
import es.mpt.dsic.inside.util.bbdd.ApplicationUtil;
import es.mpt.dsic.inside.utils.file.FileUtil;

/**
 * Spring task para el borrado de ficheros de la carpeta temporal
 * 
 * @author miguel.moral
 *
 */
public class BorrarFicherosTemporal {

  @Value("${borrarficheros.cron.active}")
  private Boolean isCronActivo;


  protected final static Log logger = LogFactory.getLog(BorrarFicherosTemporal.class);

  /***
   * Borrar los ficheros que no estan en uso que se hayan creado hace mas de 60 min De momento este
   * codigo no se mete cuando se meta el schreduled se descomenta
   * 
   * @throws UnknownHostException
   * @throws IOException
   */

  // At second :00, at minute :00, every 2 hours
  @Scheduled(cron = "${borrarficheros.cron.expression}")
  public void borrarFicherosTemporal() throws Exception {

    if (isCronActivo == null || !isCronActivo) {
      logger.warn("El cron para borrar ficheros esta desactivado");
      return;
    }

    String hostName = InetAddress.getLocalHost().getHostName();

    String modulo = ApplicationUtil.getAplicacionGestionada();

    // si es redsara o local solo se ejecutara el borrado en un modulo
    // del servidor
    if (hostName.toLowerCase().contains("sacae") || hostName.toLowerCase().contains("28apo")) {
      // solo borrara el modulo de misc
      if (!modulo.equalsIgnoreCase(ApplicationUtil.EEUTIL_MISC)) {
        return;
      }
    }



    String sdirTmp = FileUtil.getTmpDir();
    File dirTmp = new File(sdirTmp);

    File[] files = dirTmp.listFiles(new FileFilterByDate());



    for (File fileTemp : files) {
      try {
        Files.delete(Paths.get(fileTemp.getAbsolutePath()));
      } catch (IOException e) {
        logger.error("Error al eliminar el fichero " + fileTemp.getName());
      }
    }

  }

}
