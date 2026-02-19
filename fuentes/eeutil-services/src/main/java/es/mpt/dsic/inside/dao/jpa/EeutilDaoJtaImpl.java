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

package es.mpt.dsic.inside.dao.jpa;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.mpt.dsic.inside.dao.EeutilDao;
import es.mpt.dsic.inside.model.EeutilAplicacion;
import es.mpt.dsic.inside.model.EeutilAplicacionPlantilla;
import es.mpt.dsic.inside.model.EeutilAplicacionPropiedad;
import es.mpt.dsic.inside.model.UsuarioCredencial;

@Repository("inSideDao")
public class EeutilDaoJtaImpl implements EeutilDao {

  @PersistenceContext
  private EntityManager em;

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = SQLException.class)
  public void salvar(Object bean) {
    this.em.persist(bean);
    // this.em.flush();

    if (bean instanceof EeutilAplicacion || bean instanceof EeutilAplicacionPropiedad
        || bean instanceof EeutilAplicacionPlantilla || bean instanceof UsuarioCredencial) {
      borrarCache(bean);
    }

  }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = SQLException.class)
  public void borrarCache(Object bean) {

    // ponemos a 1 el registro de todos los nodos para obligar a borrar la cache.

    Query queryUpdate = em.createNativeQuery("UPDATE EEUTIL_CACHE set A_CACHEAR = ?1");
    queryUpdate.setParameter(1, 1);
    queryUpdate.executeUpdate();

    // borramos las caches.
    // CacheManager.ALL_CACHE_MANAGERS.get(0).clearAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacion.propiedades").removeAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacion.plantillas").removeAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacionPropiedad").removeAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacionPlantilla").removeAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacion").removeAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.UsuarioCredencial").removeAll();
    // CacheManager.ALL_CACHE_MANAGERS.get(0).clearAll();
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = SQLException.class)
  public Object update(Object bean) {

    Object res = this.em.merge(bean);
    if (bean instanceof EeutilAplicacion || bean instanceof EeutilAplicacionPropiedad
        || bean instanceof EeutilAplicacionPlantilla || bean instanceof UsuarioCredencial) {
      borrarCache(bean);
    }
    return res;
  }

  // @Transactional(readOnly=true)
  public Object getObjeto(Class bean, Object id) {
    return this.em.find(bean, id);
  }

  public EntityManager getEntityManager() {
    return this.em;
  }

  @Transactional(readOnly = true)
  public List getAllObjetos(Class bean) {

    Session session = (Session) getEntityManager().getDelegate();
    Criteria crit = session.createCriteria(bean);
    return crit.list();
  }


  @Transactional(readOnly = true)
  public <T> Object findObjeto(Class<T> bean, List<Criterion> criterias) {
    Session session = (Session) this.em.getDelegate();
    Criteria crit = session.createCriteria(bean);
    for (Criterion criteria : criterias) {
      crit.add(criteria);
    }
    return crit.uniqueResult();

    /*
     * ejemplo de uso List<Criterion> criterias = new ArrayList<Criterion>(); ObjetoInsideUsuario
     * usuarioInside = null; criterias.add(Restrictions.eq("nombreColumna", valorColumna));
     * eeutilServiceJta.findObjeto(Bean.class, criterias);
     */
  }

  @Deprecated
  @Transactional(readOnly = true, rollbackFor = SQLException.class)
  public void executeUpdate(String queryString, String... args) {
    Query query = em.createQuery(queryString);
    int position = 1;
    for (String arg : args) {
      query.setParameter(position, arg);
      position++;
    }
    query.executeUpdate();
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true,
      rollbackFor = SQLException.class)
  public void remove(Object bean) {

    this.em.remove(this.em.contains(bean) ? bean : em.merge(bean));
    this.em.flush();

    borrarCache(bean);

  }

  @Override
  @Transactional(readOnly = true)
  public <T> List<T> findObjetos(Class<T> bean, List<Criterion> criterias) {
    Session session = (Session) this.em.getDelegate();
    Criteria crit = session.createCriteria(bean);
    for (Criterion criteria : criterias) {
      crit.add(criteria);
    }
    return crit.list();
  }

  @Transactional(readOnly = true, rollbackFor = SQLException.class)
  public void executeStoreProcedure(String procedureName) {
    Query storeProcedure = em.createNativeQuery(procedureName);
    storeProcedure.executeUpdate();
  }

  @Override
  @Transactional(readOnly = true)
  public <T> List<T> findObjetosWithOrder(Class<T> bean, List<Criterion> criterias,
      List<Order> fieldsOrder) {
    Session session = (Session) this.em.getDelegate();
    Criteria crit = session.createCriteria(bean);
    for (Criterion criteria : criterias) {
      crit.add(criteria);
    }
    if (CollectionUtils.isNotEmpty(fieldsOrder)) {
      for (Order fieldOrder : fieldsOrder) {
        crit.addOrder(fieldOrder);
      }
    }
    return crit.list();
  }

}
