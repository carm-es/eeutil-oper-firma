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

package es.mpt.dsic.inside.ws.service.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import es.mpt.dsic.inside.exception.base.DataWsEntradaBase;
import es.mpt.dsic.inside.exception.interfaz.IMDCAble;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "opcionesObtenerInformacionFirma",
    propOrder = {"obtenerFirmantes", "obtenerDatosFirmados", "obtenerTipoFirma"})
public class OpcionesObtenerInformacionFirma extends DataWsEntradaBase implements IMDCAble {

  @XmlElement(required = false, name = "obtenerFirmantes", defaultValue = "false")
  private boolean obtenerFirmantes;
  @XmlElement(required = false, name = "obtenerDatosFirmados", defaultValue = "false")
  private boolean obtenerDatosFirmados;
  @XmlElement(required = false, name = "obtenerTipoFirma", defaultValue = "false")
  private boolean obtenerTipoFirma;

  public boolean isObtenerFirmantes() {
    return obtenerFirmantes;
  }

  public void setObtenerFirmantes(boolean obtenerFirmantes) {
    this.obtenerFirmantes = obtenerFirmantes;
  }

  public boolean isObtenerDatosFirmados() {
    return obtenerDatosFirmados;
  }

  public void setObtenerDatosFirmados(boolean obtenerDatosFirmados) {
    this.obtenerDatosFirmados = obtenerDatosFirmados;
  }

  public boolean isObtenerTipoFirma() {
    return obtenerTipoFirma;
  }

  public void setObtenerTipoFirma(boolean obtenerTipoFirma) {
    this.obtenerTipoFirma = obtenerTipoFirma;
  }



}
