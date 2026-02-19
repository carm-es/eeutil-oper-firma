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

package es.mpt.dsic.loadTables.objects;

import es.mpt.dsic.loadTables.objects.unitarios.ObjetoSimple;

public class DatosIdentificativos {

  private ObjetoSimple unidadOrganica;
  private byte nivelAdministracion;
  private String indicadorEntidadDerechoPublico;
  private String codigoExterno;

  // nuevos valores DIR3-unidadesOrganicas
  private String version;
  private String versionUnidadRaiz;
  private String poder;

  // nuevos valores DIR3-Oficinas

  private String horarioAtencion;

  // nuevos comun
  private String vUOSuperiorOResponsable;

  public ObjetoSimple getUnidadOrganica() {
    return unidadOrganica;
  }

  public void setUnidadOrganica(ObjetoSimple unidadOrganica) {
    this.unidadOrganica = unidadOrganica;
  }

  public byte getNivelAdministracion() {
    return nivelAdministracion;
  }

  public void setNivelAdministracion(byte nivelAdministracion) {
    this.nivelAdministracion = nivelAdministracion;
  }

  public String getIndicadorEntidadDerechoPublico() {
    return indicadorEntidadDerechoPublico;
  }

  public void setIndicadorEntidadDerechoPublico(String indicadorEntidadDerechoPublico) {
    this.indicadorEntidadDerechoPublico = indicadorEntidadDerechoPublico;
  }

  public String getCodigoExterno() {
    return codigoExterno;
  }

  public void setCodigoExterno(String codigoExterno) {
    this.codigoExterno = codigoExterno;
  }

  public void setUnidadOrganicaCodigo(String codigo) {
    if (this.unidadOrganica == null) {
      this.unidadOrganica = new ObjetoSimple(new String[] {"codigo", codigo});
    } else {
      this.unidadOrganica.setCodigo(codigo);
    }
  }

  public void setUnidadOrganicaDescripcion(String descripcion) {
    if (this.unidadOrganica == null) {
      this.unidadOrganica = new ObjetoSimple(new String[] {"descripcion", descripcion});
    } else {
      this.unidadOrganica.setDescripcion(descripcion);
    }
  }

  public void setUnidadOrganica(String codigo, String descripcion) {
    if (this.unidadOrganica == null) {
      this.unidadOrganica = new ObjetoSimple(codigo, descripcion);
    } else {
      this.unidadOrganica.setCodigo(codigo);
      this.unidadOrganica.setDescripcion(descripcion);
    }
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  public String getVersionUnidadRaiz() {
    return versionUnidadRaiz;
  }

  public void setVersionUnidadRaiz(String versionUnidadRaiz) {
    this.versionUnidadRaiz = versionUnidadRaiz;
  }



  public String getHorarioAtencion() {
    return horarioAtencion;
  }

  public void setHorarioAtencion(String horarioAtencion) {
    this.horarioAtencion = horarioAtencion;
  }

  public String getvUOSuperiorOResponsable() {
    return vUOSuperiorOResponsable;
  }

  public void setvUOSuperiorOResponsable(String vUOSuperiorOResponsable) {
    this.vUOSuperiorOResponsable = vUOSuperiorOResponsable;
  }

  public String getPoder() {
    return poder;
  }

  public void setPoder(String poder) {
    this.poder = poder;
  }



}
