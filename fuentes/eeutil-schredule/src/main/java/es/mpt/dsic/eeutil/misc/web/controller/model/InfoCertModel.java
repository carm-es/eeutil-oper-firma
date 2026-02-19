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

package es.mpt.dsic.eeutil.misc.web.controller.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InfoCertModel {

  private static final String RUTA_CERTIFICADO_CONST = "rutaCertificado=";
  private static final String INFO_CERT_MODEL_APLICACIONES_CONST = "InfoCertModel [aplicaciones=";
  private static final String UNKNOWN_CONST = "unknown";
  private static final String ERROR2_CONST = "error=";
  private static final String SERIAL_CERT_CONST = "serialCert=";
  private static final String TYPE_CERT_CONST = "typeCert=";
  private static final String DATE_EXPIRY_CERT_CONST = "dateExpiryCert=";
  private static final String TIPO_ALMACEN_CONST = "tipoAlmacen=";
  private static final String ALIAS_CERTIFICADO_CONST = "aliasCertificado=";
  private static final String LINE_SEPARATOR = "line.separator";
  private List<String> aplicaciones;
  private String rutaCertificado;
  private String aliasCertificado;
  private String pwdCertificado;
  private String tipoAlmacen;
  private Date[] dateExpiryCert;
  private String typeCert;
  private String serialCert;
  private ResultadoValidarCertificado resultadoValidarCertificado;

  private String error;

  public void addAplicacion(String aplicacion) {
    aplicaciones.add(aplicacion);
  }

  public InfoCertModel(String rutaCertificado, String aliasCertificado, String pwdCertificado,
      String tipoAlmacen) {
    super();
    this.aplicaciones = new ArrayList<>();
    this.rutaCertificado = rutaCertificado;
    convertRutaCertificadoWithEnviromentVariable();
    this.aliasCertificado = aliasCertificado;
    this.pwdCertificado = pwdCertificado;
    this.tipoAlmacen = tipoAlmacen;
  }

  public List<String> getAplicaciones() {
    return aplicaciones;
  }

  public String getRutaCertificado() {
    return rutaCertificado;
  }

  public String getAliasCertificado() {
    return aliasCertificado;
  }

  public String getPwdCertificado() {
    return pwdCertificado;
  }

  public String getTipoAlmacen() {
    return tipoAlmacen;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Date[] getDateExpiryCert() {
    return dateExpiryCert;
  }

  public void setDateExpiryCert(Date[] dateExpiryCert) {
    this.dateExpiryCert = dateExpiryCert;
  }

  public ResultadoValidarCertificado getResultadoValidarCertificado() {
    return resultadoValidarCertificado;
  }

  public void setResultadoValidarCertificado(
      ResultadoValidarCertificado resultadoValidarCertificado) {
    this.resultadoValidarCertificado = resultadoValidarCertificado;
  }

  public void convertRutaCertificadoWithEnviromentVariable() {
    if (this.rutaCertificado.contains("${")) {
      int indexOrigen = this.rutaCertificado.indexOf("${");
      int indexFin = this.rutaCertificado.indexOf("}");
      String cadenaVarEnv = this.rutaCertificado.substring(indexOrigen + 2, indexFin);
      String valorVarEnt = System.getProperty(cadenaVarEnv);
      this.rutaCertificado = this.rutaCertificado.replace("${" + cadenaVarEnv + "}", valorVarEnt);
      this.rutaCertificado = this.rutaCertificado.replace("misc", "firma");
    }
  }

  public String getTypeCert() {
    return typeCert;
  }

  public void setTypeCert(String typeCert) {
    this.typeCert = typeCert;
  }

  public String getSerialCert() {
    return serialCert;
  }

  public void setSerialCert(String serialCert) {
    this.serialCert = serialCert;
  }

  @Override
  public String toString() {

    String datExpiryCertStr =
        (dateExpiryCert != null && dateExpiryCert.length == 2) ? dateExpiryCert[1].toString()
            : UNKNOWN_CONST;
    String typeCertStr = typeCert != null ? typeCert : UNKNOWN_CONST;
    String serialCertStr = serialCert != null ? serialCert : UNKNOWN_CONST;

    if (rutaCertificado != null && rutaCertificado.lastIndexOf('/') != -1) {

      return new StringBuilder(INFO_CERT_MODEL_APLICACIONES_CONST).append(aplicaciones)
          .append(System.getProperty(LINE_SEPARATOR)).append(RUTA_CERTIFICADO_CONST)
          .append(
              rutaCertificado.substring(rutaCertificado.lastIndexOf('/'), rutaCertificado.length()))
          .append(System.getProperty(LINE_SEPARATOR)).append(ALIAS_CERTIFICADO_CONST)
          .append(aliasCertificado).append(System.getProperty(LINE_SEPARATOR))
          .append(TIPO_ALMACEN_CONST + tipoAlmacen).append(System.getProperty(LINE_SEPARATOR))
          .append(DATE_EXPIRY_CERT_CONST + datExpiryCertStr)
          .append(System.getProperty(LINE_SEPARATOR)).append(TYPE_CERT_CONST + typeCertStr)
          .append(System.getProperty(LINE_SEPARATOR)).append(SERIAL_CERT_CONST + serialCertStr)
          .append(System.getProperty(LINE_SEPARATOR)).append(ERROR2_CONST + error + "]")
          .append(System.getProperty(LINE_SEPARATOR)).append(System.getProperty(LINE_SEPARATOR))
          .toString();

    } else if (rutaCertificado != null && rutaCertificado.lastIndexOf('\\') != -1) {

      return new StringBuilder(INFO_CERT_MODEL_APLICACIONES_CONST).append(aplicaciones)
          .append(System.getProperty(LINE_SEPARATOR)).append(RUTA_CERTIFICADO_CONST)
          .append(rutaCertificado.substring(rutaCertificado.lastIndexOf('\\'),
              rutaCertificado.length()))
          .append(System.getProperty(LINE_SEPARATOR)).append(ALIAS_CERTIFICADO_CONST)
          .append(aliasCertificado).append(System.getProperty(LINE_SEPARATOR))
          .append(TIPO_ALMACEN_CONST + tipoAlmacen).append(System.getProperty(LINE_SEPARATOR))
          .append(DATE_EXPIRY_CERT_CONST + datExpiryCertStr)
          .append(System.getProperty(LINE_SEPARATOR)).append(TYPE_CERT_CONST + typeCertStr)
          .append(System.getProperty(LINE_SEPARATOR)).append(SERIAL_CERT_CONST + serialCertStr)
          .append(System.getProperty(LINE_SEPARATOR)).append(ERROR2_CONST + error + "]")
          .append(System.getProperty(LINE_SEPARATOR)).append(System.getProperty(LINE_SEPARATOR))
          .toString();

    }

    return new StringBuilder(INFO_CERT_MODEL_APLICACIONES_CONST).append(aplicaciones)
        .append(System.getProperty(LINE_SEPARATOR)).append(RUTA_CERTIFICADO_CONST)
        .append(rutaCertificado).append(System.getProperty(LINE_SEPARATOR))
        .append(ALIAS_CERTIFICADO_CONST).append(aliasCertificado)
        .append(System.getProperty(LINE_SEPARATOR)).append(TIPO_ALMACEN_CONST + tipoAlmacen)
        .append(System.getProperty(LINE_SEPARATOR))
        .append(DATE_EXPIRY_CERT_CONST + datExpiryCertStr)
        .append(System.getProperty(LINE_SEPARATOR)).append(TYPE_CERT_CONST + typeCertStr)
        .append(System.getProperty(LINE_SEPARATOR)).append(SERIAL_CERT_CONST + serialCertStr)
        .append(System.getProperty(LINE_SEPARATOR)).append(ERROR2_CONST + error + "]")
        .append(System.getProperty(LINE_SEPARATOR)).append(System.getProperty(LINE_SEPARATOR))
        .toString();
  }

}
