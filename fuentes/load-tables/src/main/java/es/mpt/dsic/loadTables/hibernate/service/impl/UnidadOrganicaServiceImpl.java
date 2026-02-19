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

package es.mpt.dsic.loadTables.hibernate.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import es.mpt.dsic.inside.model.EeutilUnidadOrganica;
import es.mpt.dsic.loadTables.converter.UnidadOrganicaConverter;
import es.mpt.dsic.loadTables.model.UnidadOrganica;
import es.mpt.dsic.loadTables.objects.Organismo;

public class UnidadOrganicaServiceImpl extends GenericServiceImpl<UnidadOrganica> {

  protected static final Log log = LogFactory.getLog(UnidadOrganicaServiceImpl.class);

  public void saveList(List<Organismo> organimos, Date syncDate) {
    log.debug("Inicio saveList");
    if (organimos != null) {
      List<UnidadOrganica> datos = UnidadOrganicaConverter.toEntities(organimos, syncDate);

      saveOrUpdateListFlush(datos);

    }
    log.debug("Fin saveList");
  }

  public Date geLastSync() {
    Date retorno = null;
    UnidadOrganica unidad = maxRecord(UnidadOrganica.class, "timestamp");
    if (unidad != null) {
      retorno = unidad.getTimestamp();
    }
    return retorno;
  }

  @Override
  public String getClavePrimaria(UnidadOrganica instance) {
    return instance.getCodigoUnidadOrganica();
  }

  /*
   * (non-Javadoc)
   * 
   * @see es.mpt.dsic.loadTables.hibernate.service.GenericService# saveOrUpdateListFlush
   * (java.lang.Class)
   */
  public void saveOrUpdateListFlush(List<UnidadOrganica> list) {
    log.debug("Inicio saveOrUpdate");

    try {

      for (UnidadOrganica data : list) {
        try {
          salvarUnidadOrganica(data);

        } catch (Exception e) {
          log.error("Error al procesar el registro: " + data.toString());
        }
      }

    } catch (Exception e) {
      log.debug("Error saveOrUpdate:" + e.getMessage());
    } finally {
    }
    log.debug("Fin saveOrUpdate");

  }


  public void salvarUnidadOrganica(UnidadOrganica unidadOrganica) {
    EeutilUnidadOrganica eeutilUnidadOrganica = new EeutilUnidadOrganica(
        unidadOrganica.getCodigoUnidadOrganica(), unidadOrganica.getNombreUnidadOrganica(),
        unidadOrganica.getNivelAdministracion(), unidadOrganica.getEntidadDerechoPublico(),
        unidadOrganica.getCodigoExterno(), unidadOrganica.getCodigoUnidadSuperior(),
        unidadOrganica.getNombreUnidadSuperior(), unidadOrganica.getCodigoUnidadRaiz(),
        unidadOrganica.getNombreUnidadRaiz(), unidadOrganica.getCodigoRaizDerechoPublico(),
        unidadOrganica.getNombreRaizDerechoPublico(), unidadOrganica.getNivelJerarquico(),
        unidadOrganica.getEstado(), unidadOrganica.getFechaAlta(), unidadOrganica.getFechaBaja(),
        unidadOrganica.getFechaAnulacion(), unidadOrganica.getFechaExtincion(),

        unidadOrganica.getFechaUltimaActualizacion(), unidadOrganica.getHorarioAtencion(),
        unidadOrganica.getVersion(), unidadOrganica.getvUnidadRaiz(),
        unidadOrganica.getvUnidadSuperiorOResponsable(), unidadOrganica.getPoder());

    eeutilUnidadOrganica.setTimestamp(unidadOrganica.getTimestamp());



    EeutilUnidadOrganica oUnidadOrganicaBD =
        findUnidadOrganicaByCodigoUnidadOrganica(unidadOrganica.getCodigoUnidadOrganica());

    // hacemos insert
    if (oUnidadOrganicaBD == null) {
      eeutilUnidadOrganica.setId(unidadOrganica.getId());
      eeutilDao.salvar(eeutilUnidadOrganica);
    }
    // hacemos update
    else {

      // validamos si la version que se ofrece es posterior a la version guardada en bbdd, si no es
      // asi, no actualizamos.
      if (oUnidadOrganicaBD != null && eeutilUnidadOrganica != null
          && oUnidadOrganicaBD.getId() != null && eeutilUnidadOrganica.getId() != null
          && Integer.parseInt(eeutilUnidadOrganica.getVersion()) < Integer
              .parseInt(oUnidadOrganicaBD.getVersion())) {
        return;
      }

      eeutilUnidadOrganica.setId(oUnidadOrganicaBD.getId());
      eeutilDao.update(eeutilUnidadOrganica);
    }

  }

  /*** Obtenemos el id ***/
  public EeutilUnidadOrganica findUnidadOrganicaByCodigoUnidadOrganica(
      String codigoUnidadOrganica) {
    EeutilUnidadOrganica eeutilUnidadOrganica = (EeutilUnidadOrganica) eeutilDao
        .findObjeto(EeutilUnidadOrganica.class, criteriasCodUnidadOrganica(codigoUnidadOrganica));
    return eeutilUnidadOrganica == null ? null : eeutilUnidadOrganica;
  }


  private List<Criterion> criteriasCodUnidadOrganica(String codigoUnidadOrganica) {
    List<Criterion> retorno = new ArrayList<Criterion>();
    retorno.add(Restrictions.eq("codigoUnidadOrganica", codigoUnidadOrganica));
    return retorno;
  }

}
