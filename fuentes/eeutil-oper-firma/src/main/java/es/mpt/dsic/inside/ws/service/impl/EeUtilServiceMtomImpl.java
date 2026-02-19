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
import java.util.Map;
import java.util.UUID;
import javax.activation.DataHandler;
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
import es.mpt.dsic.inside.ws.service.EeUtilServiceMtom;
import es.mpt.dsic.inside.ws.service.exception.InSideException;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.DatosFirmadosMtom;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.OpcionesObtenerInformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirmaMtom;
import es.mpt.dsic.inside.ws.service.model.ResultadoComprobarFirmaFormatoA;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;

@Service("eeUtilServiceMtom")
@WebService(endpointInterface = "es.mpt.dsic.inside.ws.service.EeUtilServiceMtom")
@SOAPBinding(style = Style.RPC, parameterStyle = ParameterStyle.BARE, use = Use.LITERAL)
public class EeUtilServiceMtomImpl implements EeUtilServiceMtom {

  private static final String EXTRA_PARA_M = "ExtraParaM";

  private static final String TIPO_FIRMA_CONST = "tipoFirma";

  private static final String FIRMA_CONST = "firma";

  protected static final Log logger = LogFactory.getLog(EeUtilServiceMtomImpl.class);

  @Autowired
  EeutilOperFirmaServiceImplBusiness eeutilOperFirmaServiceImplBusiness;

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoValidacionInfo validacionFirma(ApplicationLogin info, DataHandler firma,
      String tipoFirma, DatosFirmadosMtom datosFirmadosMtom) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.validacionFirmaMtom(info.getIdApplicacion(), firma,
          tipoFirma, datosFirmadosMtom);
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
        validacionFirmaMDC(firma, tipoFirma, datosFirmadosMtom);
        logger.error(e.getMessage());
        throw new InSideException(e.getMessage(), e);
      }
    }

    catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      validacionFirmaMDC(firma, tipoFirma, datosFirmadosMtom);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

  }

  /**
   * @param firma
   * @param tipoFirma
   * @param datosFirmadosMtom
   */
  private void validacionFirmaMDC(DataHandler firma, String tipoFirma,
      DatosFirmadosMtom datosFirmadosMtom) {
    try {
      Object[] objs = new Object[3];
      String[] strP = new String[] {FIRMA_CONST, TIPO_FIRMA_CONST, "datosFirmadosMtom"};
      objs[0] = firma;
      objs[1] = tipoFirma;
      objs[2] = datosFirmadosMtom;

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
  public ResultadoValidacionFirmaInfo validacionFirmaInfo(ApplicationLogin info, DataHandler firma,
      String tipoFirma, DatosFirmadosMtom datosFirmadosMtom, boolean infoCertificadosMtom)
      throws InSideException {
    try {
      return eeutilOperFirmaServiceImplBusiness.validacionFirmaInfoMtom(info.getIdApplicacion(),
          firma, tipoFirma, datosFirmadosMtom, infoCertificadosMtom);
    } catch (EeutilException e) {
      if (e.getCOD_AFIRMA() != null) {
        final String ln = System.getProperty("line.separator");
        ResultadoValidacionFirmaInfo res = new ResultadoValidacionFirmaInfo();
        res.setEstado(false);
        StringBuilder mensaje = new StringBuilder("");
        mensaje.append("CodigoError: " + e.getCOD_AFIRMA() + ln);
        mensaje.append("Descripcion: " + "Validaci�n de Firma err�nea." + ln);
        mensaje.append("ExcepcionAsociada: " + e.getMessage());
        res.setDetalle(mensaje.toString());
        return res;
      } else {
        ingresarMDCAppUUID(info.getIdApplicacion());
        validacionFirmaInfoMDC(firma, tipoFirma, datosFirmadosMtom, infoCertificadosMtom);
        logger.error(e.getMessage());
        throw new InSideException(e.getMessage(), e);
      }
    }

    catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      validacionFirmaInfoMDC(firma, tipoFirma, datosFirmadosMtom, infoCertificadosMtom);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }



  private void validacionFirmaInfoMDC(DataHandler firma, String tipoFirma,
      DatosFirmadosMtom datosFirmadosMtom, boolean infoCertificadosMtom) {
    try {
      Object[] objs = new Object[4];
      String[] strP =
          new String[] {FIRMA_CONST, TIPO_FIRMA_CONST, "datosFirmadosMtom", "infoCertificadosMtom"};
      objs[0] = firma;
      objs[1] = tipoFirma;
      objs[2] = datosFirmadosMtom;
      objs[3] = infoCertificadosMtom;

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
   * @param info informacion de la aplicacion
   * @param firma bytes de la firma
   * @return
   * @throws InSideException
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public InformacionFirma obtenerInformacionFirma(ApplicationLogin aplicacion, DataHandler firma,
      OpcionesObtenerInformacionFirma opciones, DataHandler content) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness
          .obtenerInformacionFirmaMtom(aplicacion.getIdApplicacion(), firma, opciones, content);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(aplicacion.getIdApplicacion());
      obtenerInformacionFirmaMDC(firma, opciones, content);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(aplicacion.getIdApplicacion());
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
  private void obtenerInformacionFirmaMDC(DataHandler firma,
      OpcionesObtenerInformacionFirma opciones, DataHandler content) {
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
  public ListaFirmaInfo obtenerFirmantes(ApplicationLogin info, DataHandler firma, String tipoFirma)
      throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.obtenerFirmantesMtom(info.getIdApplicacion(),
          firma);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      obtenerFirmantesMDC(firma, tipoFirma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      obtenerFirmantesMDC(firma, tipoFirma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }
  }

  /**
   * @param firma
   * @param tipoFirma
   */
  private void obtenerFirmantesMDC(DataHandler firma, String tipoFirma) {
    try {
      Object[] objs = new Object[2];
      String[] strP = new String[] {FIRMA_CONST, TIPO_FIRMA_CONST};
      objs[0] = firma;
      // objs[1]=datos;
      objs[1] = tipoFirma;

      Map<String, String> mParametros =
          UtilReflection.getInstance().extractMultipleDataPermitted(null, objs, strP);
      String resultado = MapUtil.mapToString(mParametros);
      MDC.put(EXTRA_PARA_M, resultado);

    } catch (IOException e1) {

      // si falla palante

    }
  }

  /**
   * Recibe una firma y una configuracion de ampliacion y devuelve la firma con el upgrade apropiado
   */
  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoAmpliarFirmaMtom ampliarFirma(ApplicationLogin info, DataHandler firma,
      ConfiguracionAmpliarFirma configuracion) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.ampliarFirmaMtom(info.getIdApplicacion(), firma,
          configuracion);
    } catch (EeutilException e) {
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

  /**
   * @param firma
   * @param configuracion
   */
  private void ampliarFirmaMDC(DataHandler firma, ConfiguracionAmpliarFirma configuracion) {
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
  public DataHandler postProcesarFirma(ApplicationLogin info, DataHandler firma)
      throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.postProcesarFirmaMtom(firma);
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
  private void postProcesarFirmaMDC(DataHandler firma) {
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

  /**
   * Obtiene el resultado de la validaci�n de un certificado
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
   * Obtiene el resultado de la validaci�n de un certificado
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

  @Override
  @AuditEntryPointAnnotation(nombreApp = "EEUTIL-OPER-FIRMA")
  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(ApplicationLogin info,
      DataHandler firma) throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.comprobarFirmaFormatoAMtom(info.getIdApplicacion(),
          firma);
    } catch (EeutilException e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      comprobarFirmaFormatoAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    } catch (Exception e) {
      ingresarMDCAppUUID(info.getIdApplicacion());
      comprobarFirmaFormatoAMDC(firma);
      logger.error(e.getMessage(), e);
      throw new InSideException(e.getMessage(), e);
    }

  }

  /**
   * @param firma
   */
  private void comprobarFirmaFormatoAMDC(DataHandler firma) {
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
  public ResultadoAmpliarFirmaMtom resellarFirmaA(ApplicationLogin info, DataHandler firma)
      throws InSideException {



    try {
      return eeutilOperFirmaServiceImplBusiness.resellarFirmaAMtom(info.getIdApplicacion(), firma);
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
  private void resellarFirmaAMDC(DataHandler firma) {
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
