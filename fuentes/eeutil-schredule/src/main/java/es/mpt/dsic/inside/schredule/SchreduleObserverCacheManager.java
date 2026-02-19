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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import es.mpt.dsic.inside.dao.EeutilDao;
import es.mpt.dsic.inside.util.bbdd.ApplicationUtil;
import net.sf.ehcache.CacheManager;

public class SchreduleObserverCacheManager {

  protected final static Log logger = LogFactory.getLog(SchreduleObserverCacheManager.class);

  @Autowired
  private EeutilDao eeutilDao;

  @Autowired
  @Qualifier("entityManagerFactory")
  private EntityManagerFactory entityManagerFactory;


  @Scheduled(cron = "${cron.expression}")
  @Transactional
  public void esNecesarioLimpiarCache() throws Exception {


    EntityManager entityManager = null;
    try {

      String hostName = InetAddress.getLocalHost().getHostName();

      String modulo = ApplicationUtil.getAplicacionGestionada();

      // consultamos el registro a ver si necesitamos cachear.
      entityManager = eeutilDao.getEntityManager();
      Query query = entityManager
          .createNativeQuery("SELECT A_CACHEAR from EEUTIL_CACHE where IP_HOST =?1 and MODULO =?2");
      query.setParameter(1, hostName);
      query.setParameter(2, modulo);
      Object obj = query.getSingleResult();

      if (obj != null && "1".equals(obj.toString())) {
        // borramos la cache.
        CacheManager.ALL_CACHE_MANAGERS.get(0)
            .getCache("es.mpt.dsic.inside.model.EeutilAplicacion.propiedades").removeAll();
        CacheManager.ALL_CACHE_MANAGERS.get(0)
            .getCache("es.mpt.dsic.inside.model.EeutilAplicacion.plantillas").removeAll();
        CacheManager.ALL_CACHE_MANAGERS.get(0)
            .getCache("es.mpt.dsic.inside.model.EeutilAplicacionPropiedad").removeAll();
        CacheManager.ALL_CACHE_MANAGERS.get(0)
            .getCache("es.mpt.dsic.inside.model.EeutilAplicacionPlantilla").removeAll();
        CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacion")
            .removeAll();
        CacheManager.ALL_CACHE_MANAGERS.get(0)
            .getCache("es.mpt.dsic.inside.model.UsuarioCredencial").removeAll();
        CacheManager.ALL_CACHE_MANAGERS.get(0).clearAll();

        // PENDIENTE actualizamos el registro A_CACHEAR=0 correspondiente a este host para indicar
        // que ya se ha borrado la cache.

        EntityManager em = entityManagerFactory.createEntityManager();
        actualizarEvitarCacheo(modulo, hostName, entityManager, em);

      }


    } catch (UnknownHostException e) {
      logger.error("Error al obtener la direccion de la maquina de servidor");
    } catch (Exception e) {
      logger.error("Error en base de datos al manipular datos de cache");
      throw e;
    } finally {
      if (entityManager != null)
        entityManager.close();
    }

    // System.out.println("Method executed at every 5 minutes. Current time is :: " + new Date());
  }


  @Transactional
  public void actualizarEvitarCacheo(String modulo, String hostName, EntityManager entityManager,
      EntityManager em) {
    try {

      long time = System.currentTimeMillis();
      java.sql.Timestamp timestamp = new java.sql.Timestamp(time);

      em.getTransaction().begin();

      // insertamos el registro del host registrado.
      Query queryUpdate = em.createNativeQuery(
          "UPDATE EEUTIL_CACHE SET A_CACHEAR = ?1, FECHA_ULTIMO_CACHEO =?2  WHERE IP_HOST = ?3 AND MODULO = ?4");
      queryUpdate.setParameter(1, 0);
      queryUpdate.setParameter(2, timestamp);
      queryUpdate.setParameter(3, hostName);
      queryUpdate.setParameter(4, modulo);
      queryUpdate.executeUpdate();

      em.flush();
      em.getTransaction().commit();
    } catch (Exception e) {


      logger.error("Error al actualizar la cache manager");
      if (em.getTransaction() != null && entityManager.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;

    } finally {
      em.close();
    }
  }



}
