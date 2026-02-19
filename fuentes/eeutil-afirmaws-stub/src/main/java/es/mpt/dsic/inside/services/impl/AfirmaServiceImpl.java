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

package es.mpt.dsic.inside.services.impl;

import es.mpt.dsic.inside.afirma.ws.client.AfirmaWSClient;
import es.mpt.dsic.inside.model.ConfiguracionAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.RequestAmpliarFirmaDSS;
import es.mpt.dsic.inside.model.RequestConfigAfirma;
import es.mpt.dsic.inside.model.RequestObtenerInformacionFirma;
import es.mpt.dsic.inside.model.RequestValidarCertificado;
import es.mpt.dsic.inside.model.RequestValidarFirma;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionFirmaFormatoAAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirmaExt;
import es.mpt.dsic.inside.model.ResultadoValidarCertificadoAfirma;
import es.mpt.dsic.inside.model.TipoDeFirmaAfirma;
import es.mpt.dsic.inside.services.AfirmaService;
import es.mpt.dsic.inside.utils.exception.EeutilException;

/**
 * Implementacion ws de afirma en eeutils.
 * 
 * @author miguel.moral
 *
 */
public class AfirmaServiceImpl implements AfirmaService {

  private AfirmaWSClient afirmaClient;

  private String idAplicacionDefault;
  private String idaplicacionServidorDedicado;
  private String truststore;
  private String passTruststore;

  /**
   * 
   * @return afirmaClient
   */
  public AfirmaWSClient getAfirmaClient() {
    return afirmaClient;
  }

  /**
   * 
   * @param afirmaClient
   */
  public void setAfirmaClient(AfirmaWSClient afirmaClient) {
    this.afirmaClient = afirmaClient;
  }

  /**
   * 
   * @return idAplicacionDefault
   */
  public String getIdAplicacionDefault() {
    return idAplicacionDefault;
  }

  /**
   * 
   * @param idAplicacionDefault
   */
  public void setIdAplicacionDefault(String idAplicacionDefault) {
    this.idAplicacionDefault = idAplicacionDefault;
  }

  /**
   * 
   * @return idaplicacionServidorDedicado
   */
  public String getIdaplicacionServidorDedicado() {
    return idaplicacionServidorDedicado;
  }

  /**
   * 
   * @param idaplicacionServidorDedicado
   */
  public void setIdaplicacionServidorDedicado(String idaplicacionServidorDedicado) {
    this.idaplicacionServidorDedicado = idaplicacionServidorDedicado;
  }

  /**
   * 
   * @return truststore
   */
  public String getTruststore() {
    return truststore;
  }

  /**
   * 
   * @param truststore
   */
  public void setTruststore(String truststore) {
    this.truststore = truststore;
  }

  /**
   * 
   * @return passTruststore
   */
  public String getPassTruststore() {
    return passTruststore;
  }

  /**
   * 
   * @param passTruststore
   */
  public void setPassTruststore(String passTruststore) {
    this.passTruststore = passTruststore;
  }

  private RequestConfigAfirma checkSecurity(String aplicacion) {
    RequestConfigAfirma retorno = new RequestConfigAfirma();
    String idAppLower = aplicacion.toLowerCase();
    String idAppServidorDedicadoLower = idaplicacionServidorDedicado.toLowerCase();
    if (idAppLower.contains(idAppServidorDedicadoLower)) {
      retorno.setIdAplicacion(idaplicacionServidorDedicado);
    } else {
      retorno.setIdAplicacion(idAplicacionDefault);
    }
    retorno.setTruststore(truststore);
    retorno.setPassTruststore(passTruststore);
    return retorno;
  }

  @Override
  public ResultadoValidacionInfoAfirma validarFirma(String aplicacion, String firmaElectronica,
      String datos, String hash, String algoritmo, String tipoFirma) throws EeutilException {
    ResultadoValidacionInfoAfirma resultadoValidacionInfoAfirma = null;
    try {
      RequestValidarFirma requestValidarFirma = new RequestValidarFirma(checkSecurity(aplicacion),
          firmaElectronica, datos, hash, algoritmo, tipoFirma);
      resultadoValidacionInfoAfirma = afirmaClient.validarFirma(requestValidarFirma);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return resultadoValidacionInfoAfirma;
  }


  @Override
  public ResultadoValidacionInfoAfirmaExt validarFirmaInfo(String aplicacion,
      String firmaElectronica, String datos, String hash, String algoritmo, String tipoFirma,
      boolean infoCertificados) throws EeutilException {
    ResultadoValidacionInfoAfirmaExt resultadoValidacionInfoAfirmaExt = null;
    try {
      RequestValidarFirma requestValidarFirma = new RequestValidarFirma(checkSecurity(aplicacion),
          firmaElectronica, datos, hash, algoritmo, tipoFirma);
      resultadoValidacionInfoAfirmaExt =
          afirmaClient.validarFirmaInfo(requestValidarFirma, infoCertificados);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

    return resultadoValidacionInfoAfirmaExt;
  }


  @Override
  public ResultadoValidarCertificadoAfirma validarCertificado(String aplicacion, String certificado,
      Boolean infAmpliada) throws EeutilException {
    ResultadoValidarCertificadoAfirma resultadoValidarCertificadoAfirma = null;
    try {
      RequestValidarCertificado requestValidarCertificado = new RequestValidarCertificado(
          checkSecurity(aplicacion), certificado, infAmpliada, 3, true);
      resultadoValidarCertificadoAfirma =
          afirmaClient.validarCertificado(requestValidarCertificado);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return resultadoValidarCertificadoAfirma;
  }

  @Override
  public InformacionFirmaAfirma obtenerInformacionFirma(String aplicacion, byte[] firma,
      boolean obtenerFirmantes, boolean obtenerDatosFirmados, boolean obtenerTipoFirma,
      byte[] content) throws EeutilException {

    InformacionFirmaAfirma informacionFirma = null;

    try {
      RequestObtenerInformacionFirma requestObtenerInformacionFirma =
          new RequestObtenerInformacionFirma(checkSecurity(aplicacion), firma, obtenerFirmantes,
              obtenerDatosFirmados, obtenerTipoFirma, content);
      // return afirmaClient
      // .obtenerInformacionFirma(requestObtenerInformacionFirma);
      informacionFirma = afirmaClient.obtenerInformacionFirma(requestObtenerInformacionFirma);

      // buscamos la cadena xmldsig#Manifest

      int ocurrenciaManifest = -1;

      if (firma != null) {
        ocurrenciaManifest = new String(firma).indexOf("xmldsig#Manifest");
      }

      if (ocurrenciaManifest != -1) {
        TipoDeFirmaAfirma tipoDeFirma = new TipoDeFirmaAfirma();
        tipoDeFirma.setTipoFirma("XADES MANIFEST");
        informacionFirma.setTipoDeFirma(tipoDeFirma);
      }
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

    return informacionFirma;
  }

  @Override
  public ResultadoAmpliarFirmaAfirma ampliarFirma(String aplicacion, byte[] sign,
      ConfiguracionAmpliarFirmaAfirma configuracion) throws EeutilException {
    ResultadoAmpliarFirmaAfirma resultadoAmpliarFirmaAfirma = null;
    try {
      RequestAmpliarFirmaDSS requestAmpliarFirmaDSS =
          new RequestAmpliarFirmaDSS(checkSecurity(aplicacion), sign, null, configuracion);
      resultadoAmpliarFirmaAfirma = afirmaClient.ampliarFirma(requestAmpliarFirmaDSS);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return resultadoAmpliarFirmaAfirma;
  }

  @Override
  public String obtenerTipoFirmaDss(String aplicacion, byte[] firma, byte[] content)
      throws EeutilException {
    String tipoFirmaDss = null;
    try {
      RequestObtenerInformacionFirma requestObtenerInformacionFirma =
          new RequestObtenerInformacionFirma(checkSecurity(aplicacion), firma, false, false, false,
              content);
      tipoFirmaDss = afirmaClient.obtenerTipoFirmaDss(requestObtenerInformacionFirma);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return tipoFirmaDss;
  }

  @Override
  public ResultadoValidacionFirmaFormatoAAfirma validarFirmaFormatoA(String aplicacion,
      String firmaElectronica, String datos, String hash, String algoritmo, String tipoFirma)
      throws EeutilException {
    ResultadoValidacionFirmaFormatoAAfirma resultadoValidacionFirmaFormatoAAfirma = null;
    try {
      RequestValidarFirma requestValidarFirma = new RequestValidarFirma(checkSecurity(aplicacion),
          firmaElectronica, datos, hash, algoritmo, tipoFirma);
      resultadoValidacionFirmaFormatoAAfirma =
          afirmaClient.validarFirmaFormatoA(requestValidarFirma);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }
    return resultadoValidacionFirmaFormatoAAfirma;
  }

}
