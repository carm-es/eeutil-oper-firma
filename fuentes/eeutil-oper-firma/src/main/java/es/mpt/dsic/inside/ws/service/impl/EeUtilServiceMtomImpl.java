/*
 * Copyright (C) 2012-13 MINHAP, Gobierno de España This program is licensed and may be used,
 * modified and redistributed under the terms of the European Public License (EUPL), either version
 * 1.1 or (at your option) any later version as soon as they are approved by the European
 * Commission. Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * more details. You should have received a copy of the EUPL1.1 license along with this program; if
 * not, you may find it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.mpt.dsic.inside.ws.service.impl;

import java.io.IOException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.mpt.dsic.inside.converter.AfirmaModelToWsModelConverter;
import es.mpt.dsic.inside.converter.AfirmaValidationConverterType;
import es.mpt.dsic.inside.exception.AfirmaException;
import es.mpt.dsic.inside.security.context.AplicacionContext;
import es.mpt.dsic.inside.security.model.ApplicationLogin;
import es.mpt.dsic.inside.services.AfirmaService;
import es.mpt.dsic.inside.utils.xml.XMLUtil;
import es.mpt.dsic.inside.ws.service.EeUtilService;
import es.mpt.dsic.inside.ws.service.EeUtilServiceMtom;
import es.mpt.dsic.inside.ws.service.exception.InSideException;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.DatosFirmados;
import es.mpt.dsic.inside.ws.service.model.DatosFirmadosMtom;
import es.mpt.dsic.inside.ws.service.model.EstadoInfo;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.OpcionesObtenerInformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirmaMtom;
import es.mpt.dsic.inside.ws.service.model.ResultadoComprobarFirmaFormatoA;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFormatoAInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;

@Service("eeUtilServiceMtom")
@WebService(endpointInterface = "es.mpt.dsic.inside.ws.service.EeUtilServiceMtom")
@SOAPBinding(style = Style.RPC, parameterStyle = ParameterStyle.BARE, use = Use.LITERAL)
public class EeUtilServiceMtomImpl implements EeUtilServiceMtom {

  protected static final Log logger = LogFactory.getLog(EeUtilServiceMtomImpl.class);

  @Autowired(required = false)
  private AplicacionContext aplicacionContext;

  @Autowired
  private AfirmaService afirmaService;

  @Autowired
  private EeUtilService eeUtilService;

  @Override
  public ResultadoValidacionInfo validacionFirma(ApplicationLogin info, DataHandler firma,
      String tipoFirma, DatosFirmadosMtom datosFirmadosMtom) throws InSideException {
    try {

      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      byte[] documentoBytes = null;
      if (datosFirmadosMtom != null && datosFirmadosMtom.getDocumento() != null) {
        documentoBytes = IOUtils.toByteArray(datosFirmadosMtom.getDocumento().getInputStream());
      }

      byte[] hashBytes = null;
      if (datosFirmadosMtom != null && datosFirmadosMtom.getHash() != null) {
        hashBytes = IOUtils.toByteArray(datosFirmadosMtom.getHash().getInputStream());
      }

      DatosFirmados datosFirmados = new DatosFirmados();

      if (datosFirmadosMtom != null && StringUtils.isNotEmpty(datosFirmadosMtom.getAlgoritmo())) {
        datosFirmados.setAlgoritmo(datosFirmadosMtom.getAlgoritmo());
      }
      datosFirmados.setDocumento(documentoBytes);
      datosFirmados.setHash(hashBytes);

      return eeUtilService.validacionFirma(info, firmaBytes, tipoFirma, datosFirmados);
    } catch (IOException e) {
      logger.error("Error en peticion EeUtilServiceMtomImpl.validacionFirma:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL VALIDAR FIRMA", null), e);
    }
  }

  /**
   * Obtiene la informaci�n de una firma, a partir de una llamada a los WS DSS de Afirma
   * 
   * @param info informacion de la aplicación
   * @param firma bytes de la firma
   * @return
   * @throws InSideException
   */
  @Override
  public InformacionFirma obtenerInformacionFirma(ApplicationLogin aplicacion, DataHandler firma,
      OpcionesObtenerInformacionFirma opciones, DataHandler content) throws InSideException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }
      byte[] contenidoBytes = null;
      if (content != null) {
        contenidoBytes = IOUtils.toByteArray(content.getInputStream());
      }
      return eeUtilService.obtenerInformacionFirma(aplicacion, firmaBytes, opciones,
          contenidoBytes);
    } catch (IOException e) {
      logger.error(
          "Error en peticion EeUtilServiceMtomImpl.obtenerInformacionFirma:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL OBTENER INFORMACION DE FIRMA", null), e);
    }
  }

  @Override
  public ListaFirmaInfo obtenerFirmantes(ApplicationLogin info, DataHandler firma,
      DataHandler datos, String tipoFirma) throws InSideException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }
      byte[] datosBytes = null;
      if (datos != null) {
        datosBytes = IOUtils.toByteArray(datos.getInputStream());
      }
      return eeUtilService.obtenerFirmantes(info, firmaBytes, datosBytes, tipoFirma);
    } catch (IOException e) {
      logger.error(
          "Error en peticion EeUtilServiceMtomImpl.obtenerInformacionFirmas:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL OBTENER INFORMACION DE FIRMA", null), e);
    }
  }

  /**
   * Recibe una firma y una configuraci�n de ampliaci�n y devuelve la firma con el upgrade apropiado
   */
  @Override
  public ResultadoAmpliarFirmaMtom ampliarFirma(ApplicationLogin info, DataHandler firma,
      ConfiguracionAmpliarFirma configuracion) throws InSideException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      ResultadoAmpliarFirma resultado = eeUtilService.ampliarFirma(info, firmaBytes, configuracion);

      ResultadoAmpliarFirmaMtom retorno = new ResultadoAmpliarFirmaMtom();
      DataSource dataSource = new ByteArrayDataSource(resultado.getFirma());
      retorno.setFirma(new DataHandler(dataSource));

      return retorno;
    } catch (IOException e) {
      logger.error("Error en peticion EeUtilServiceMtomImpl.ampliarFirma:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL AMPLIAR FIRMA", null), e);
    }
  }

  @Override
  public DataHandler postProcesarFirma(ApplicationLogin info, DataHandler firma)
      throws InSideException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      byte[] retorno = eeUtilService.postProcesarFirma(info, firmaBytes);
      DataSource dataSource = new ByteArrayDataSource(retorno);
      return new DataHandler(dataSource);
    } catch (IOException e) {
      logger.error("Error en peticion EeUtilServiceMtomImpl.postProcesarFirma:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL POST PROCESAR FIRMA", null), e);
    }
  }

  /**
   * Obtiene el resultado de la validaci�n de un certificado
   */
  @Override
  public ResultadoValidarCertificado validarCertificado(ApplicationLogin info, String certificate)
      throws InSideException {
    return AfirmaModelToWsModelConverter
        .resultadoValidarCertificadoAfirmaToResultadoValidarCertificado(
            afirmaService.validarCertificado(info.getIdApplicacion(), certificate, false));
  }

  /**
   * Obtiene el resultado de la validaci�n de un certificado
   */
  @Override
  public InfoCertificado getInfoCertificado(ApplicationLogin info, String certificate)
      throws InSideException {
    return AfirmaModelToWsModelConverter
        .resultadoValidarCertificadoAfirmaToResultadoValidarCertificadoAmpliado(
            afirmaService.validarCertificado(info.getIdApplicacion(), certificate, true));
  }

  @Override
  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(ApplicationLogin info,
      DataHandler firma) throws InSideException {
    try {
      ResultadoComprobarFirmaFormatoA retorno = new ResultadoComprobarFirmaFormatoA();

      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      // obtenemos el tipo de firma DSS
      String tipoFirmaDss =
          afirmaService.obtenerTipoFirmaDss(info.getIdApplicacion(), firmaBytes, null);

      // realizamos la conversion para validar la firma al formato
      // correspondiente
      String formatoValidacion =
          AfirmaValidationConverterType.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

      // compobamos si se tiene q hacer la ampliacion de formato
      ResultadoValidacionFormatoAInfo resultadoValidar = AfirmaModelToWsModelConverter
          .resultadoValidacionFirmaFormatoAAfirmaToResultadoValidacionFormatoAInfo(
              afirmaService.validarFirmaFormatoA(info.getIdApplicacion(),
                  XMLUtil.decodeUTF8(firmaBytes), null, null, null, formatoValidacion));
      if (resultadoValidar.isEstado()) {
        retorno.setEsFirmaA(true);
        retorno.setFechaValidez(resultadoValidar.getFechaValidezCertificadoTSA());
      } else {
        retorno.setEsFirmaA(false);
      }
      return retorno;
    } catch (AfirmaException e) {
      logger.error("Error en EeUtilServiceMtomImpl.comprobarFirmaFormatoA", e);
      EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(), e.getMessage());
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    } catch (IOException e) {
      logger.error(
          "Error en peticion EeUtilServiceMtomImpl.comprobarFirmaFormatoA:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL POST PROCESAR FIRMA", null), e);
    }
  }

  @Override
  public ResultadoAmpliarFirmaMtom resellarFirmaA(ApplicationLogin info, DataHandler firma)
      throws InSideException {
    try {
      logger.debug("Inicio resellarFirmaA");

      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      // obtenemos el tipo de firma DSS
      String tipoFirmaDss =
          afirmaService.obtenerTipoFirmaDss(info.getIdApplicacion(), firmaBytes, null);

      // realizamos la conversion para validar la firma al formato
      // correspondiente
      String formatoAmpliar =
          AfirmaValidationConverterType.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

      ConfiguracionAmpliarFirma confAmpliar = new ConfiguracionAmpliarFirma();
      confAmpliar.setCertificadosFirmantes(null);
      confAmpliar.setFormatoAmpliacion(formatoAmpliar);
      confAmpliar.setIgnorarPeriodoDeGracia(true);
      return this.ampliarFirma(info, firma, confAmpliar);
    } catch (AfirmaException e) {
      logger.error("Error en resellarFirmaA", e);
      EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(), e.getMessage());
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    } catch (IOException e) {
      logger.error("Error en peticion EeUtilServiceMtomImpl.resellarFirmaA:" + e.getMessage());
      throw new InSideException(e.getMessage(),
          new EstadoInfo("ERROR", "ERROR INESPERADO AL RESELLAR FIRMA", null), e);
    }
  }
}
