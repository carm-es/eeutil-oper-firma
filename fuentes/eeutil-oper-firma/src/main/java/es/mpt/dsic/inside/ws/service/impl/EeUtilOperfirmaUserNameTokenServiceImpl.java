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

package es.mpt.dsic.inside.ws.service.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.mpt.dsic.inside.aop.AuditEntryPointAnnotation;
import es.mpt.dsic.inside.reflection.MapUtil;
import es.mpt.dsic.inside.reflection.UtilReflection;
import es.mpt.dsic.inside.security.wss4j.CredentialUtil;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.ws.service.EeUtilOperFirmaUserNameTokenService;
import es.mpt.dsic.inside.ws.service.exception.InSideException;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.DatosFirmados;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.OpcionesObtenerInformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoComprobarFirmaFormatoA;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;

@Service("eeUtilOperFirmaUserNameTokenService")
@WebService(endpointInterface = "es.mpt.dsic.inside.ws.service.EeUtilOperFirmaUserNameTokenService")
@SOAPBinding(style = Style.RPC, parameterStyle = ParameterStyle.BARE, use = Use.LITERAL)
public class EeUtilOperfirmaUserNameTokenServiceImpl
    implements EeUtilOperFirmaUserNameTokenService {

  private static final String EXTRA_PARA_M = "ExtraParaM";

  private static final String DATOS_FIRMADOS_CONST = "datosFirmados";

  private static final String TIPO_FIRMA_CONST = "tipoFirma";

  private static final String FIRMA_CONST = "firma";

  protected static final Log logger =
      LogFactory.getLog(EeUtilOperfirmaUserNameTokenServiceImpl.class);

  public static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;

  @Resource
  private WebServiceContext wsContext;

  @Autowired
  CredentialUtil credentialUtil;

  @Autowired
  EeutilOperFirmaServiceImplBusiness eeutilOperFirmaServiceImplBusiness;

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidacionInfo validacionFirma(byte[] firma, String tipoFirma,
      DatosFirmados datosFirmados) throws InSideException {
    try {
      return eeutilOperFirmaServiceImplBusiness.validacionFirma(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma,
          tipoFirma, datosFirmados);

    } catch (EeutilException e) {
      if (e.getCOD_AFIRMA() != null) {
        final String ln = System.getProperty("line.separator");
        ResultadoValidacionInfo res = new ResultadoValidacionInfo();
        res.setEstado(false);
        StringBuilder mensaje = new StringBuilder("");
        mensaje.append("CodigoError: " + e.getCOD_AFIRMA() + ln);
        mensaje.append("Descripcion: " + "Validaci�n de Firma err�nea." + ln);
        mensaje.append("ExcepcionAsociada: " + e.getMessage());
        res.setDetalle(mensaje.toString());
        return res;
      } else {
        validacionFirmaMDC(firma, tipoFirma, datosFirmados);
        logger.error(e.getMessage());
        throw new InSideException(e.getMessage(), e);
      }
    }


    catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      validacionFirmaMDC(firma, tipoFirma, datosFirmados);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

  }

  /**
   * @param firma
   * @param tipoFirma
   * @param datosFirmados
   */
  private void validacionFirmaMDC(byte[] firma, String tipoFirma, DatosFirmados datosFirmados) {
    try {
      Object[] objs = new Object[3];
      String[] strP = new String[] {FIRMA_CONST, TIPO_FIRMA_CONST, DATOS_FIRMADOS_CONST};
      objs[0] = firma;
      objs[1] = tipoFirma;
      objs[2] = datosFirmados;

      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  /***
   * 
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidacionFirmaInfo validacionFirmaInfo(byte[] firma, String tipoFirma,
      DatosFirmados datosFirmados, boolean infoCertificados) throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.validacionFirmaInfo(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma,
          tipoFirma, datosFirmados, infoCertificados);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      validacionFirmaInfoMDC(firma, tipoFirma, datosFirmados, infoCertificados);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      validacionFirmaInfoMDC(firma, tipoFirma, datosFirmados, infoCertificados);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

  }

  private void validacionFirmaInfoMDC(byte[] firma, String tipoFirma, DatosFirmados datosFirmados,
      boolean infoCertificados) {
    try {
      Object[] objs = new Object[4];
      String[] strP =
          new String[] {FIRMA_CONST, TIPO_FIRMA_CONST, DATOS_FIRMADOS_CONST, "infoCertificados"};
      objs[0] = firma;
      objs[1] = tipoFirma;
      objs[2] = datosFirmados;
      objs[3] = infoCertificados;

      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }

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
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public InformacionFirma obtenerInformacionFirma(byte[] firma,
      OpcionesObtenerInformacionFirma opciones, byte[] content) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.obtenerInformacionFirma(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma,
          opciones, content);

    } catch (InSideException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      obtenerInformacionFirmaMDC(firma, opciones, content);
      logger.error(e.getMessage(), e);
      throw e;
    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      obtenerInformacionFirmaMDC(firma, opciones, content);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      obtenerInformacionFirmaMDC(firma, opciones, content);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

  }

  /**
   * @param firma
   * @param opciones
   * @param content
   */
  private void obtenerInformacionFirmaMDC(byte[] firma, OpcionesObtenerInformacionFirma opciones,
      byte[] content) {
    try {
      Object[] objs = new Object[3];
      String[] strP = new String[] {FIRMA_CONST, "opciones", "content"};
      objs[0] = firma;
      objs[1] = opciones;
      objs[2] = content;

      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ListaFirmaInfo obtenerFirmantes(byte[] firma, byte[] datos, String content)
      throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.obtenerFirmantes(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma);

    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      obtenerFirmantesMDC(firma, datos, content);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      obtenerFirmantesMDC(firma, datos, content);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param firma
   * @param datos
   * @param content
   */
  private void obtenerFirmantesMDC(byte[] firma, byte[] datos, String content) {
    try {
      Object[] objs = new Object[3];
      String[] strP = new String[] {FIRMA_CONST, "datos", "content"};
      objs[0] = firma;
      objs[1] = datos;
      objs[2] = content;

      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);

      // indicamos que este parametro aunque es obligatorio, si se rellena ya esta sin uso en el
      // negocio (datos)
      if (mParametros.get("byte[].datos_tamano") != null
          && !"0 bytes.".equals(mParametros.get("byte[].datos_tamano"))) {
        mParametros.put("byte[].datos_tamano", mParametros.get("byte[].datos_tamano") + "/Sin uso");
      }

      String resultado = MapUtil.mapToString(mParametros);

      MDC.put("ExtraParaM", resultado);


    } catch (IOException e1) {

      // si falla palante

    }
  }

  /**
   * Obtiene el resultado de la validaci?n de un certificado
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidarCertificado validarCertificado(String certificate) throws InSideException {



    try {

      ResultadoValidarCertificado resultado = null;

      resultado = eeutilOperFirmaServiceImplBusiness.validarCertificado(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), certificate);

      if (!resultado.isValidado()) {
        validarCertificadoMDC(certificate);
        logger.error("Certificado no valido. " + resultado.getDetalleValidacion());
      }

      return resultado;


    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      validarCertificadoMDC(certificate);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      validarCertificadoMDC(certificate);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param certificate
   */
  private void validarCertificadoMDC(String certificate) {
    try {
      Object[] objs = new Object[1];
      String[] strP = new String[] {"certificate"};
      objs[0] = certificate;


      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  /**
   * Obtiene el resultado de la validacion de un certificado
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public InfoCertificado getInfoCertificado(String certificate) throws InSideException {



    try {
      InfoCertificado infoCertificado = eeutilOperFirmaServiceImplBusiness.getInfoCertificado(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), certificate);

      if (!infoCertificado.isValidado()) {
        getInfoCertificadoMDC(certificate);
        logger.error("Certificado no valido. " + infoCertificado.getDetalleValidacion());
      }

      return infoCertificado;

    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      getInfoCertificadoMDC(certificate);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      getInfoCertificadoMDC(certificate);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }


  /**
   * @param certificate
   */
  private void getInfoCertificadoMDC(String certificate) {
    try {
      Object[] objs = new Object[1];
      String[] strP = new String[] {"certificate"};
      objs[0] = certificate;


      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  /**
   * Recibe una firma y una configuraci?n de ampliaci?n y devuelve la firma con el upgrade apropiado
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoAmpliarFirma ampliarFirma(byte[] firma, ConfiguracionAmpliarFirma configuracion)
      throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.ampliarFirma(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma,
          configuracion);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      ampliarFirmaMDC(firma, configuracion);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      ampliarFirmaMDC(firma, configuracion);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param configuracion
   */
  private void ampliarFirmaMDC(byte[] firma, ConfiguracionAmpliarFirma configuracion) {
    try {
      Object[] objs = new Object[2];
      String[] strP = new String[] {FIRMA_CONST, "configuracion"};
      objs[0] = firma;
      objs[1] = configuracion;


      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public byte[] postProcesarFirma(byte[] firma) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.postProcesarFirma(firma);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      postProcesarFirmaMDC(firma);

      if (e.COD_AFIRMA == null) {
        e.COD_AFIRMA = "Error postprocesando firma";
      }

      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      postProcesarFirmaMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param firma
   */
  private void postProcesarFirmaMDC(byte[] firma) {
    try {
      Object[] objs = new Object[1];
      String[] strP = new String[] {FIRMA_CONST};
      objs[0] = firma;


      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(byte[] firma)
      throws InSideException {



    try {

      return eeutilOperFirmaServiceImplBusiness.comprobarFirmaFormatoA(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      comprobarFirmaFormatoAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      comprobarFirmaFormatoAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

  }


  /**
   * @param firma
   */
  private void comprobarFirmaFormatoAMDC(byte[] firma) {
    try {
      Object[] objs = new Object[1];
      String[] strP = new String[] {FIRMA_CONST};
      objs[0] = firma;


      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoAmpliarFirma resellarFirmaA(byte[] firma) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.resellarFirmaA(
          credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion(), firma);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      resellarFirmaAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(credentialUtil.getCredentialEeutilUserToken(wsContext).getIdApplicacion());
      resellarFirmaAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }


  /**
   * @param firma
   */
  private void resellarFirmaAMDC(byte[] firma) {
    try {
      Object[] objs = new Object[1];
      String[] strP = new String[] {FIRMA_CONST};
      objs[0] = firma;


      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }


  private void ingresarMDCAppUUID(String idApp) {
    MDC.put("idApli", idApp);
    MDC.put("uUId", UUID.randomUUID().toString());
  }



}
