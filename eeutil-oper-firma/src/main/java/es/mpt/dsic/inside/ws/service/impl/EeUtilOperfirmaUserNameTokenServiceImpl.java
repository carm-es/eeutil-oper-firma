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
import java.nio.charset.Charset;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.gob.afirma.core.signers.AOSignerFactory;
import es.mpt.dsic.inside.converter.AfirmaModelToWsModelConverter;
import es.mpt.dsic.inside.converter.AfirmaValidationConverterType;
import es.mpt.dsic.inside.exception.AfirmaException;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.security.context.AplicacionContext;
import es.mpt.dsic.inside.security.wss4j.CredentialUtil;
import es.mpt.dsic.inside.services.AfirmaService;
import es.mpt.dsic.inside.utils.xml.XMLUtil;
import es.mpt.dsic.inside.ws.service.EeUtilOperFirmaUserNameTokenService;
import es.mpt.dsic.inside.ws.service.exception.InSideException;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.DatosFirmados;
import es.mpt.dsic.inside.ws.service.model.EstadoInfo;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.OpcionesObtenerInformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoComprobarFirmaFormatoA;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFormatoAInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessException;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessFactory;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessor;

@Service("eeUtilOperFirmaUserNameTokenService")
@WebService(endpointInterface = "es.mpt.dsic.inside.ws.service.EeUtilOperFirmaUserNameTokenService")
@SOAPBinding(style = Style.RPC, parameterStyle = ParameterStyle.BARE, use = Use.LITERAL)
public class EeUtilOperfirmaUserNameTokenServiceImpl
    implements EeUtilOperFirmaUserNameTokenService {

  protected final static Log logger =
      LogFactory.getLog(EeUtilOperfirmaUserNameTokenServiceImpl.class);

  @Autowired(required = false)
  private AplicacionContext aplicacionContext;

  @Autowired
  private AfirmaService afirmaService;

  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  @Resource
  private WebServiceContext wsContext;

  @Autowired
  CredentialUtil credentialUtil;

  @Override
  public ResultadoValidacionInfo validacionFirma(byte[] firma, String tipoFirma,
      DatosFirmados datosFirmados) throws InSideException {

    if (firma == null) {
      EstadoInfo estadoInfo = new EstadoInfo();
      throw new InSideException("La firma no puede ser nula", estadoInfo,
          new IllegalArgumentException());
    }

    logger.debug("Tipo de firma: " + tipoFirma);
    String firmaElectronica64 = Base64.encodeBase64String(firma);

    String datos64 = null;
    String hash64 = null;
    String algoritmo = null;

    if (datosFirmados != null && datosFirmados.getDocumento() != null) {
      datos64 = Base64.encodeBase64String(datosFirmados.getDocumento());
    }

    if (datosFirmados != null && datosFirmados.getHash() != null) {
      hash64 = Base64.encodeBase64String(datosFirmados.getHash());
    }

    if (datosFirmados != null) {
      algoritmo = datosFirmados.getAlgoritmo();
    }

    ResultadoValidacionInfo validaFirma = AfirmaModelToWsModelConverter
        .resultadoValidacionInfoAfirmaToResultadoValidacionInfo(afirmaService.validarFirma(
            credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(),
            firmaElectronica64, datos64, hash64, algoritmo, tipoFirma));

    return validaFirma;

  }

  /**
   * Obtiene la informaci�n de una firma, a partir de una llamada a los WS DSS de Afirma
   * 
   * @param info informacion de la aplicaci�n
   * @param firma bytes de la firma
   * @return
   * @throws InSideException
   */
  @Override
  public InformacionFirma obtenerInformacionFirma(byte[] firma,
      OpcionesObtenerInformacionFirma opciones, byte[] content) throws InSideException {
    InformacionFirma info = null;
    try {
      InformacionFirmaAfirma infoAfirma = afirmaService.obtenerInformacionFirma(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma,
          opciones.isObtenerFirmantes(), opciones.isObtenerDatosFirmados(),
          opciones.isObtenerTipoFirma(), content);
      info = AfirmaModelToWsModelConverter.informacionFirmaAfirmaToInformacionFirma(infoAfirma);
    } catch (AfirmaException e) {
      logger.error("Error en obtenerInformacionFirma", e);
      EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(), e.getMessage());
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    } catch (Throwable t) {
      logger.error("Error en obtenerInformacionFirma", t);
      EstadoInfo estadoInfo =
          new EstadoInfo("ERROR", "ERROR INESPERADO AL OBTENER INFORMACION DE FIRMA", null);
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    }

    return info;

  }

  @Override
  public ListaFirmaInfo obtenerFirmantes(byte[] firma, byte[] datos, String tipoFirma)
      throws InSideException {
    OpcionesObtenerInformacionFirma opciones = new OpcionesObtenerInformacionFirma();
    opciones.setObtenerDatosFirmados(false);
    opciones.setObtenerFirmantes(true);
    opciones.setObtenerTipoFirma(false);

    InformacionFirma informacionFirma = this.obtenerInformacionFirma(firma, opciones, null);

    return informacionFirma.getFirmantes();
  }

  /**
   * Obtiene el resultado de la validaci?n de un certificado
   */
  @Override
  public ResultadoValidarCertificado validarCertificado(String certificate) throws InSideException {
    return AfirmaModelToWsModelConverter
        .resultadoValidarCertificadoAfirmaToResultadoValidarCertificado(
            afirmaService.validarCertificado(
                credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(),
                certificate, false));
  }

  /**
   * Obtiene el resultado de la validaci?n de un certificado
   */
  @Override
  public InfoCertificado getInfoCertificado(String certificate) throws InSideException {
    return AfirmaModelToWsModelConverter
        .resultadoValidarCertificadoAfirmaToResultadoValidarCertificadoAmpliado(
            afirmaService.validarCertificado(
                credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(),
                certificate, true));
  }

  /**
   * Recibe una firma y una configuraci?n de ampliaci?n y devuelve la firma con el upgrade apropiado
   */
  @Override
  public ResultadoAmpliarFirma ampliarFirma(byte[] firma, ConfiguracionAmpliarFirma configuracion)
      throws InSideException {

    ResultadoAmpliarFirma resultadoAmpliarFirma;

    try {

      ResultadoAmpliarFirmaAfirma resAfirma = afirmaService.ampliarFirma(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma,
          AfirmaModelToWsModelConverter
              .configuracionAmpliarFirmaToConfiguracionAmpliarFirmaAfirma(configuracion));
      resultadoAmpliarFirma = AfirmaModelToWsModelConverter
          .resultadoAmpliarFirmaAfirmaToResultadoAmpliarFirma(resAfirma);
    } catch (AfirmaException e) {
      logger.error("Error en ampliarFirma", e);
      EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(), e.getMessage());
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    } catch (Throwable t) {
      logger.error("Error en ampliarFirma", t);
      EstadoInfo estadoInfo =
          new EstadoInfo("ERROR", "ERROR INESPERADO AL INTENTAR AMPLIAR LA FIRMA", null);
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    }

    return resultadoAmpliarFirma;
  }

  @Override
  public byte[] postProcesarFirma(byte[] firma) throws InSideException {
    byte[] firmaPost = null;
    try {
      PostProcessFactory factory = PostProcessFactory.getInstance();
      PostProcessor proc = factory.getPostProcessor(AOSignerFactory.getSigner(firma));

      if (proc == null) {
        EstadoInfo estadoInfo = new EstadoInfo();
        throw new InSideException("No existe postProcessador para este tipo de firma", estadoInfo);
      }

      firmaPost = proc.postProcessSign(firma);
    } catch (IOException ioe) {
      logger.error("Error postprocesando firma", ioe);
      EstadoInfo estadoInfo = new EstadoInfo();
      throw new InSideException("No se puede leer la firma: " + ioe.getMessage(), estadoInfo);
    } catch (PostProcessException pe) {
      logger.error("Error postprocesando firma", pe);
      EstadoInfo estadoInfo = new EstadoInfo();
      throw new InSideException("No se puede procesar la firma: " + pe.getMessage(), estadoInfo);
    } catch (Exception e) {
      logger.error("Error postprocesando firma", e);
      EstadoInfo estadoInfo = new EstadoInfo();
      throw new InSideException("Error inesperado al procesar la firma: " + e.getMessage(),
          estadoInfo);
    }

    return firmaPost;
  }

  @Override
  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(byte[] firma)
      throws InSideException {
    try {
      ResultadoComprobarFirmaFormatoA retorno = new ResultadoComprobarFirmaFormatoA();

      // obtenemos el tipo de firma DSS
      String tipoFirmaDss = afirmaService.obtenerTipoFirmaDss(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma, null);

      // realizamos la conversion para validar la firma al formato
      // correspondiente
      String formatoValidacion =
          AfirmaValidationConverterType.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

      // comprobamos si la firma viene en base64
      byte[] firmaValidar = Base64.isBase64(firma) ? firma : Base64.encodeBase64(firma);

      // compobamos si se tiene q hacer la ampliacion de formato
      ResultadoValidacionFormatoAInfo resultadoValidar = AfirmaModelToWsModelConverter
          .resultadoValidacionFirmaFormatoAAfirmaToResultadoValidacionFormatoAInfo(
              afirmaService.validarFirmaFormatoA(
                  credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(),
                  XMLUtil.decodeUTF8(firmaValidar), null, null, null, formatoValidacion));
      if (resultadoValidar.isEstado()) {
        retorno.setEsFirmaA(true);
        retorno.setFechaValidez(resultadoValidar.getFechaValidezCertificadoTSA());
      } else {
        retorno.setEsFirmaA(false);
      }
      return retorno;
    } catch (AfirmaException e) {
      logger.error("Error en comprobarFirmaFormatoA", e);
      EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(), e.getMessage());
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    }
  }

  @Override
  public ResultadoAmpliarFirma resellarFirmaA(byte[] firma) throws InSideException {
    try {
      logger.debug("Inicio resellarFirmaA");

      // obtenemos el tipo de firma DSS
      String tipoFirmaDss = afirmaService.obtenerTipoFirmaDss(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma, null);

      // realizamos la conversion para validar la firma al formato
      // correspondiente
      String formatoAmpliar =
          AfirmaValidationConverterType.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

      ConfiguracionAmpliarFirma confAmpliar = new ConfiguracionAmpliarFirma();
      confAmpliar.setCertificadosFirmantes(null);
      confAmpliar.setFormatoAmpliacion(formatoAmpliar);
      confAmpliar.setIgnorarPeriodoDeGracia(true);
      return this.ampliarFirma(firma, confAmpliar);
    } catch (AfirmaException e) {
      logger.error("Error en resellarFirmaA", e);
      EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(), e.getMessage());
      throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
    }
  }

}
