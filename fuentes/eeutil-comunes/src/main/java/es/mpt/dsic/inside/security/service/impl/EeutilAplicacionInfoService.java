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

package es.mpt.dsic.inside.security.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.mpt.dsic.inside.dao.EeutilDao;
import es.mpt.dsic.inside.model.EeutilAplicacion;
import es.mpt.dsic.inside.model.EeutilAplicacionPlantilla;
import es.mpt.dsic.inside.model.EeutilAplicacionPlantillaId;
import es.mpt.dsic.inside.security.model.AppInfo;
import es.mpt.dsic.inside.security.model.EeutilAppInfo;
import es.mpt.dsic.inside.security.model.EeutilUserDetails;
import es.mpt.dsic.inside.security.service.AplicacionInfoService;
import es.mpt.dsic.inside.utils.exception.EeutilException;

@Service("aplicacionInfoService")
public class EeutilAplicacionInfoService implements AplicacionInfoService {

  protected final static Log logger = LogFactory.getLog(EeutilAplicacionInfoService.class);

  @Autowired
  private EeutilDao eeutilDao;

  private final String DELETE_PLANTILLAS_BY_APLICACION =
      "DELETE FROM EeutilAplicacionPlantilla where id.idaplicacion = ?";

  @Override
  public AppInfo getAplicacionInfo(String appId) throws EeutilException {

    // long inicio=new Date().getTime();
    EeutilAplicacion isApp = (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class, appId);
    // long fin=new Date().getTime();

    // logger.debug("Tiempo en milisegundos"+String.valueOf(fin-inicio));

    // Para comprobar que cachea.
    /*
     * CacheManager.ALL_CACHE_MANAGERS.get(0).getCache("es.mpt.dsic.inside.model.EeutilAplicacion").
     * getSize();
     * 
     * Set<EeutilAplicacionPropiedad> temp=isApp.getPropiedades();
     * Iterator<EeutilAplicacionPropiedad> it=temp.iterator();
     * 
     * while(it.hasNext()) { EeutilAplicacionPropiedad prop=it.next();
     * System.out.println("ID:"+prop.getId()+" "+"VALUE"+prop.getValor());
     * 
     * }
     */

    if (isApp == null) {
      MDC.put("uUId", UUID.randomUUID().toString());
      throw new EeutilException("Error de autenticacion de usuario: " + appId);
    } else {
      // escribimos en el hilo para tenerlo disponible en log4j.properties

      MDC.put("idApli", appId);
      MDC.put("uUId", UUID.randomUUID().toString());
    }


    return new EeutilAppInfo(isApp);


  }

  @Override
  public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException, DataAccessException {

    logger.debug("loading user: " + username);

    EeutilAplicacion isApp =
        (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class, username);

    if (isApp == null) {
      throw new UsernameNotFoundException("No se puede encontrar la aplicacion: " + username,
          username);
    }
    Collection<GrantedAuthority> permisos = new ArrayList<GrantedAuthority>();
    permisos.add(new GrantedAuthorityImpl("ROLE_LECTURA"));
    if (isApp.isTramitar()) {
      permisos.add(new GrantedAuthorityImpl("ROLE_TRAMITAR"));
    }
    if (isApp.isFirma()) {
      permisos.add(new GrantedAuthorityImpl("ROLE_FIRMA"));
    }
    if (isApp.isSello()) {
      permisos.add(new GrantedAuthorityImpl("ROLE_SELLO"));
    }

    EeutilUserDetails user = new EeutilUserDetails(username, isApp.getPassword(), isApp.isActiva(),
        true, true, true, permisos, new EeutilAppInfo(isApp));
    return user;
  }

  @Override
  public AppInfo asociarPantilla(String appId, String idPlantilla, byte[] plantilla) {
    EeutilAplicacion appEntity =
        (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class, appId);

    boolean newPlantilla = true;

    if (CollectionUtils.isNotEmpty(appEntity.getPlantillas())) {
      for (EeutilAplicacionPlantilla plantillaEntity : appEntity.getPlantillas()) {
        if (plantillaEntity.getId().getIdplantilla().equals(idPlantilla)) {
          // actualizamos la plantilla
          plantillaEntity.setPlantilla(plantilla);
          newPlantilla = false;
        }
      }
    }
    if (newPlantilla) {
      // insertamos la plantilla
      appEntity.getPlantillas().add(new EeutilAplicacionPlantilla(
          new EeutilAplicacionPlantillaId(appId, idPlantilla), plantilla));
    }

    // actualizamos el objeto de bbdd
    appEntity = (EeutilAplicacion) eeutilDao.update(appEntity);

    return new EeutilAppInfo(appEntity);
  }

  @Override
  public AppInfo eliminarPlantilla(String appId, final String idPlantilla) {
    EeutilAplicacion appEntity =
        (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class, appId);

    if (CollectionUtils.isNotEmpty(appEntity.getPlantillas())) {
      eeutilDao.executeUpdate(DELETE_PLANTILLAS_BY_APLICACION, appId);

      CollectionUtils.filter(appEntity.getPlantillas(), new Predicate() {
        @Override
        public boolean evaluate(Object object) {
          return !((EeutilAplicacionPlantilla) object).getId().getIdplantilla().equals(idPlantilla);
        }
      });

      appEntity = (EeutilAplicacion) eeutilDao.update(appEntity);
    }

    return new EeutilAppInfo(appEntity);
  }

}
