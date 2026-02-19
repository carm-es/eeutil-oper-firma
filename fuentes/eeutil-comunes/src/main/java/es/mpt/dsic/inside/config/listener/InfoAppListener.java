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

package es.mpt.dsic.inside.config.listener;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Clase para extraer informacion asociada a las variables de la JVM
 * 
 * @author miguel.moral
 *
 */

public class InfoAppListener implements ServletContextListener {


  protected static final Log logger = LogFactory.getLog(InfoAppListener.class);


  @Override
  public void contextInitialized(ServletContextEvent sce) {

    // get a RuntimeMXBean reference
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

    // get the jvm's input arguments as a list of strings
    List<String> listOfArguments = runtimeMxBean.getInputArguments();

    System.out.println("ARRANCANDO LISTENER PARA OBTENER INFORMACION DEL MODULO");

    // print the arguments using my logger
    for (String argumentVM : listOfArguments) {
      System.out.println("ARG JVM :" + argumentVM);
      logger.info("ARG JVM :" + argumentVM);
    }

    // print the service providers.
    System.out.println("PROVEEDORES DE SERVICIO Y SERVICIOS");
    for (Provider provider : Security.getProviders()) {
      System.out.println("Provider: " + provider.getName());
      for (Provider.Service service : provider.getServices()) {
        System.out.println("  Algorithm: " + service.getAlgorithm());
      }
    }
    System.out.println("FIN PROVEEDORES DE SERVICIO Y SERVICIOS");


  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // DO NOTHING
  }

}
