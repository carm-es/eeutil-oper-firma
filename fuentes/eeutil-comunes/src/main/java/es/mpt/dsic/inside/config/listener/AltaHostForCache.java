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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import es.mpt.dsic.inside.util.bbdd.ApplicationUtil;

public class AltaHostForCache implements ServletContextListener {

  // @Autowired
  // private EeutilDao eeutilDao;

  @Autowired
  @Qualifier("entityManagerFactory")
  private EntityManagerFactory entityManagerFactory;

  protected static final Log logger = LogFactory.getLog(AltaHostForCache.class);

  @Override
  @Transactional
  public void contextInitialized(ServletContextEvent sce) {

    // activar los autowire cuando no estamos en un contexto spring.
    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

    EntityManager em = entityManagerFactory.createEntityManager();

    String hostName = null;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e1) {
      logger.error("Error al obtener la direccion con InetAddress", e1);
    }

    String modulo = ApplicationUtil.getAplicacionGestionada();

    try {

      em.getTransaction().begin();

      // borramos el registro del host registrado..
      Query queryDelete =
          em.createNativeQuery("delete from EEUTIL_CACHE where IP_HOST=?1 and MODULO = ?2");
      queryDelete.setParameter(1, hostName);
      queryDelete.setParameter(2, modulo);

      queryDelete.executeUpdate();

      // insertamos el registro del host registrado.
      Query queryInsert =
          em.createNativeQuery("INSERT INTO EEUTIL_CACHE (IP_HOST,MODULO) values(?1,?2)");
      queryInsert.setParameter(1, hostName);
      queryInsert.setParameter(2, modulo);
      queryInsert.executeUpdate();

      em.flush();
      em.getTransaction().commit();

    } catch (Exception e) {

      logger
          .error("Error al realizar la transaccion al dar de alta la maquina en el cache manager");
      if (em.getTransaction() != null && em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }

    } finally {
      em.close();
    }

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // DO NOTHING

  }



}
