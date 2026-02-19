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

package es.mpt.dsic.inside.model;

import java.util.List;

public class ResultadoValidacionInfoAfirmaExt {


  private boolean estado;

  private String detalle;

  private int numeroFirmantes;

  private List<String> certificados;

  public boolean isEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }

  public String getDetalle() {
    return detalle;
  }

  public void setDetalle(String detalle) {
    this.detalle = detalle;
  }

  public int getNumeroFirmantes() {
    return numeroFirmantes;
  }

  public void setNumeroFirmantes(int numeroFirmantes) {
    this.numeroFirmantes = numeroFirmantes;
  }

  public List<String> getCertificados() {
    return certificados;
  }

  public void setCertificados(List<String> certificados) {
    this.certificados = certificados;
  }

  @Override
  public String toString() {
    return "ResultadoValidacionInfoAfirma [estado=" + estado + ", detalle=" + detalle
        + ", numeroFirmantes=" + numeroFirmantes + "]";
  }


}
