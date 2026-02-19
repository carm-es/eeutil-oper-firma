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
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.mpt.dsic.inside.aop.AuditEntryPointAnnotation;
import es.mpt.dsic.inside.reflection.MapUtil;
import es.mpt.dsic.inside.reflection.UtilReflection;
import es.mpt.dsic.inside.security.model.ApplicationLogin;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.ws.service.EeUtilService;
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

@Service("eeUtilService")
@WebService(endpointInterface = "es.mpt.dsic.inside.ws.service.EeUtilService")
@SOAPBinding(style = Style.RPC, parameterStyle = ParameterStyle.BARE, use = Use.LITERAL)
public class EeUtilServiceImpl implements EeUtilService {

  private static final String EXTRA_PARA_M = "ExtraParaM";

  private static final String DATOS_FIRMADOS_CONST = "datosFirmados";

  private static final String TIPO_FIRMA_CONST = "tipoFirma";

  private static final String FIRMA_CONST = "firma";

  protected static final Log logger = LogFactory.getLog(EeUtilServiceImpl.class);

  @Autowired
  EeutilOperFirmaServiceImplBusiness eeutilOperFirmaServiceImplBusiness;

  public static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidacionInfo validacionFirma(ApplicationLogin info, byte[] firma,
      String tipoFirma, DatosFirmados datosFirmados) throws InSideException {

    try {
      ResultadoValidacionInfo resinfo = eeutilOperFirmaServiceImplBusiness
          .validacionFirma(info.getIdApplicacion(), firma, tipoFirma, datosFirmados);
      return resinfo;

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
        ingresarMDCAppUUID(info.getIdApplicacion());
        validacionFirmaMDC(firma, tipoFirma, datosFirmados);
        logger.error(e.getMessage());
        throw new InSideException(e.getMessage(), e);
      }
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      validacionFirmaMDC(firma, tipoFirma, datosFirmados);
      logger.error(e.getMessage());
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
  public InformacionFirma obtenerInformacionFirma(ApplicationLogin aplicacion, byte[] firma,
      OpcionesObtenerInformacionFirma opciones, byte[] content) throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness
          .obtenerInformacionFirma(aplicacion.getIdApplicacion(), firma, opciones, content);

    } catch (EeutilException e) {
      ingresarMDCAppUUID(aplicacion.getIdApplicacion());
      obtenerInformacionFirmaMDC(firma, opciones, content);
      logger.error(e.getMessage());
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(aplicacion.getIdApplicacion());
      obtenerInformacionFirmaMDC(firma, opciones, content);
      logger.error(e.getMessage());
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

  /**
   * Obtiene el objeto InfoCertificadoInfo que corresponde al certificado pasado por par�metro
   * 
   * @param cert Certificado del que se quiere buscar la informaci�n en la lista.
   * @param lista Lista de informaci�n de certificados en la que queremos buscar
   * @return El objeto InfoCertificadoInfo que se corresponda con el certificado pasado como
   *         par�metro. Se considera que se corresponden cuando coincide el n�mero de serie.
   */
  /*
   * private InfoCertificadoInfo getCertificado (X509Certificate cert, List<InfoCertificadoInfo>
   * lista) { logger.debug("Certificado: N�mero de serie " + cert.getSerialNumber().toString());
   * logger.debug("Certificado: Issuer Principal, Campo O= " +
   * MiscUtil.getOrganizationIssuerX500Principal (cert.getIssuerX500Principal().getName()));
   * logger.debug("Certificado: Issuer Principal: " + cert.getIssuerX500Principal().getName());
   * 
   * InfoCertificadoInfo infoCerti = null; boolean encontrado = false; int i=0; while (i <
   * lista.size() && !encontrado) { InfoCertificadoInfo info = lista.get(i); List<Campo> campos =
   * info.getCampo();
   * 
   * boolean encontradoNumSerie = false; boolean encontradoIdEmisor = false;
   * 
   * String numSerie = ""; String idEmisor = ""; int j = 0;
   * 
   * while (j < campos.size() && (!encontradoNumSerie || !encontradoIdEmisor)) {
   * logger.debug("IdCampo " + campos.get(j).getIdCampo()); logger.debug("ValorCampo " +
   * campos.get(j).getValorCampo()); if (campos.get(j).getIdCampo().contentEquals("numeroSerie")) {
   * encontradoNumSerie = true; numSerie = campos.get(j).getValorCampo(); } else if
   * (campos.get(j).getIdCampo().contentEquals("idEmisor")) { encontradoIdEmisor = true; idEmisor =
   * campos.get(j).getValorCampo(); } j++; }
   * 
   * if (numSerie.contentEquals(cert.getSerialNumber().toString())) {
   * logger.debug("Encontrado n�mero de serie coincidente"); if
   * (idEmisor.contentEquals(cert.getIssuerX500Principal().getName()) ||
   * idEmisor.contentEquals(MiscUtil.getOrganizationIssuerX500Principal(cert.
   * getIssuerX500Principal().getName()))) { logger.debug("Encontrado emisor coincidente"); } else {
   * logger.info("NO se ha encontrado emisor coincidente"); } infoCerti = info; }
   * 
   * i++; }
   * 
   * return infoCerti;
   * 
   * }
   */

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ListaFirmaInfo obtenerFirmantes(ApplicationLogin info, byte[] firma, byte[] datos,
      String tipoFirma) throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.obtenerFirmantes(info.getIdApplicacion(), firma);

    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      obtenerFirmantesMDC(firma, datos, tipoFirma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      obtenerFirmantesMDC(firma, datos, tipoFirma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param firma
   * @param datos
   * @param tipoFirma
   */
  private void obtenerFirmantesMDC(byte[] firma, byte[] datos, String tipoFirma) {
    try {
      Object[] objs = new Object[3];
      String[] strP = new String[] {FIRMA_CONST, "datos", TIPO_FIRMA_CONST};
      objs[0] = firma;
      objs[1] = datos;
      objs[2] = tipoFirma;

      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);

      // indicamos que este parametro aunque es obligatorio, si se rellena ya esta sin uso en el
      // negocio (datos)
      if (mParametros.get("byte[].datos_tamano") != null
          && !"0 bytes.".equals(mParametros.get("byte[].datos_tamano"))) {
        mParametros.put("byte[].datos_tamano", mParametros.get("byte[].datos_tamano") + "/Sin uso");
      }

      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  /**
   * Obtiene el resultado de la validaci?n de un certificado
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidarCertificado validarCertificado(ApplicationLogin info, String certificate)
      throws InSideException {

    try {
      ResultadoValidarCertificado resultado = null;

      resultado = eeutilOperFirmaServiceImplBusiness.validarCertificado(info.getIdApplicacion(),
          certificate);

      if (!resultado.isValidado()) {
        validarCertificadoMDC(certificate);
        logger.error("Certificado no valido. " + resultado.getDetalleValidacion());
      }

      return resultado;

    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      validarCertificadoMDC(certificate);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
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
   * Obtiene el resultado de la validaci?n de un certificado
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public InfoCertificado getInfoCertificado(ApplicationLogin info, String certificate)
      throws InSideException {

    try {

      InfoCertificado infoCertificado = eeutilOperFirmaServiceImplBusiness
          .getInfoCertificado(info.getIdApplicacion(), certificate);

      if (!infoCertificado.isValidado()) {
        getInfoCertificadoMDC(certificate);
        logger.error("Certificado no valido. " + infoCertificado.getDetalleValidacion());
      }

      return infoCertificado;

    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      getInfoCertificadoMDC(certificate);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
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
  public ResultadoAmpliarFirma ampliarFirma(ApplicationLogin info, byte[] firma,
      ConfiguracionAmpliarFirma configuracion) throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.ampliarFirma(info.getIdApplicacion(), firma,
          configuracion);

    }

    catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      ampliarFirmaMDC(firma, configuracion);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      ampliarFirmaMDC(firma, configuracion);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

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
  public byte[] postProcesarFirma(ApplicationLogin info, byte[] firma) throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.postProcesarFirma(firma);

    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      postProcesarFirmaMDC(firma);

      if (e.COD_AFIRMA == null) {
        e.COD_AFIRMA = "Error postprocesando firma";
      }

      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
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
  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(ApplicationLogin info, byte[] firma)
      throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.comprobarFirmaFormatoA(info.getIdApplicacion(),
          firma);

    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      comprobarFirmaFormatoAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

    catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
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
  public ResultadoAmpliarFirma resellarFirmaA(ApplicationLogin info, byte[] firma)
      throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.resellarFirmaA(info.getIdApplicacion(), firma);

    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      resellarFirmaAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
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

  /****
   * Servicio que se llama desde misc por soapbinding solamente Misc --> operfirma
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidacionFirmaInfo validacionFirmaInfo(ApplicationLogin info, byte[] firma,
      String tipoFirma, DatosFirmados datosFirmados, boolean infoCertificados)
      throws InSideException {

    try {
      return eeutilOperFirmaServiceImplBusiness.validacionFirmaInfo(info.getIdApplicacion(), firma,
          tipoFirma, datosFirmados, infoCertificados);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      validacionFirmaInfoMDC(firma, tipoFirma, datosFirmados, infoCertificados);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      validacionFirmaInfoMDC(firma, tipoFirma, datosFirmados, infoCertificados);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param firma
   * @param tipoFirma
   * @param datosFirmados
   * @param infoCertificados
   */
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

  private void ingresarMDCAppUUID(String idApp) {
    MDC.put("idApli", idApp);
    MDC.put("uUId", UUID.randomUUID().toString());
  }

}
