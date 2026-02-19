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
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import es.mpt.dsic.inside.converter.AfirmaModelToWsModelConverter;
import es.mpt.dsic.inside.converter.AfirmaValidationConverterType;
import es.mpt.dsic.inside.dssprocessing.constantes.DSSTiposFirmaConstantes;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.services.AfirmaService;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.xml.XMLUtil;
import es.mpt.dsic.inside.wrapper.AOSignerWrapperEeutils;
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
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFormatoAInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessException;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessFactory;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessor;

@Component
public class EeutilOperFirmaServiceImplBusiness {

  private static final String ERROR_INESPERADO_AL_VALIDAR_FIRMA =
      "ERROR INESPERADO AL VALIDAR FIRMA";

  private static final String ERROR_EN_AMPLIAR_FIRMA = "Error en ampliarFirma";

  private static final String ERROR_PROCESANDO_COMPROBARFIRMAFORMATOA =
      "ERROR PROCESANDO COMPROBARFIRMAFORMATOA";

  private static final String ERROR_INESPERADO_AL_OBTENER_INFORMACION_DE_CERTIFICADO =
      "Error inesperado al obtener informacion de certificado";

  private static final String ERROR_INESPERADO_AL_VALIDAR_CERTIFICADO =
      "Error inesperado al validarCertificado";

  private static final String ERROR_INESPERADO_AL_OBTENER_FIRMANTES =
      "Error inesperado al obtener firmantes";

  private static final String ERROR_INESPERADO_AL_OBTENER_INFORMACION_DE_FIRMA =
      "ERROR INESPERADO AL OBTENER INFORMACION DE FIRMA";

  private static final String LA_FIRMA_NO_PUEDE_SER_NULA = "La firma no puede ser nula";

  protected static final Log logger = LogFactory.getLog(EeutilOperFirmaServiceImplBusiness.class);

  private static final String ERROR = "ERROR";

  private static final String NO_SE_PUEDE_LEER_LA_FIRMA = "No se puede leer la firma: ";

  private static final String ERROR_POSTPROCESANDO_FIRMA = "Error postprocesando firma";

  private static final String FIRMA_VACIA = "La firma enviada no puede estar vacia";

  @Autowired
  private AfirmaService afirmaService;

  public ResultadoValidacionInfo validacionFirma(String idApp, byte[] firma, String tipoFirma,
      DatosFirmados datosFirmados) throws EeutilException {

    if (firma == null) {
      // EstadoInfo estadoInfo = new EstadoInfo();
      // estadoInfo.setDescripcion(LA_FIRMA_NO_PUEDE_SER_NULA);
      // logger.error("LA_FIRMA_NO_PUEDE_SER_NULA");
      throw new EeutilException(LA_FIRMA_NO_PUEDE_SER_NULA,
          new IllegalArgumentException(LA_FIRMA_NO_PUEDE_SER_NULA));
    }

    try {

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

      return AfirmaModelToWsModelConverter
          .resultadoValidacionInfoAfirmaToResultadoValidacionInfo(afirmaService.validarFirma(idApp,
              firmaElectronica64, datos64, hash64, algoritmo, tipoFirma));

    } catch (Exception e) {
      throw new EeutilException("ERROR INESPERADO AL VALIDAR FIRMA. " + e.getMessage(), e);
    }

  }

  public ResultadoValidacionInfo validacionFirmaMtom(String idApp, DataHandler firma,
      String tipoFirma, DatosFirmadosMtom datosFirmadosMtom) throws EeutilException {
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

      return this.validacionFirma(idApp, firmaBytes, tipoFirma, datosFirmados);
    } catch (IOException e) {

      throw new EeutilException(
          "Error en peticion EeUtilServiceMtomImpl.validacionFirma:" + e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException(
          "Error en peticion EeUtilServiceMtomImpl.validacionFirma:" + e.getMessage(), e);
    }
  }

  public InformacionFirma obtenerInformacionFirma(String idApp, byte[] firma,
      OpcionesObtenerInformacionFirma opciones, byte[] content)
      throws EeutilException, InSideException {
    InformacionFirma info = null;
    if (firma == null || firma.length == 0) {
      throw new EeutilException(
          "Error en peticion EeutilOperFirmaServiceImplBusiness.obtenerInformacionFirma: la firma enviada esta vacia");
    }
    try {
      InformacionFirmaAfirma infoAfirma =
          afirmaService.obtenerInformacionFirma(idApp, firma, opciones.isObtenerFirmantes(),
              opciones.isObtenerDatosFirmados(), opciones.isObtenerTipoFirma(), content);
      info = AfirmaModelToWsModelConverter.informacionFirmaAfirmaToInformacionFirma(infoAfirma);
    } catch (EeutilException e) {

      throw new EeutilException("Error en obtenerInformacionFirma " + e.getMessage(), e);
    } catch (Exception t) {

      throw new EeutilException("Error en obtenerInformacionFirma " + t.getMessage(), t);
    }

    return info;

  }

  public InformacionFirma obtenerInformacionFirmaMtom(String idApp, DataHandler firma,
      OpcionesObtenerInformacionFirma opciones, DataHandler content) throws EeutilException {
    try {
      byte[] firmaBytes = null;
      if (firma != null && firma.getDataSource().getInputStream().available() > 0) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      } else {
        logger.error(
            "Error en peticion EeutilOperFirmaServiceImplBusiness.obtenerInformacionFirmaMtom: la firma enviada esta vacia");
        throw new EeutilException("Peticion incorrecta "
            + "Error en peticion EeutilOperFirmaServiceImplBusiness.obtenerInformacionFirmaMtom: la firma enviada esta vacia"
            + FIRMA_VACIA);
      }
      byte[] contenidoBytes = null;
      if (content != null && content.getDataSource().getInputStream().available() > 0) {
        contenidoBytes = IOUtils.toByteArray(content.getInputStream());
      }
      return this.obtenerInformacionFirma(idApp, firmaBytes, opciones, contenidoBytes);
    } catch (Exception e) {

      throw new EeutilException(e.getMessage(), e);
    }
  }

  public ListaFirmaInfo obtenerFirmantes(String idApp, byte[] firma) throws EeutilException {
    try {
      OpcionesObtenerInformacionFirma opciones = new OpcionesObtenerInformacionFirma();
      opciones.setObtenerDatosFirmados(false);
      opciones.setObtenerFirmantes(true);
      opciones.setObtenerTipoFirma(false);

      InformacionFirma informacionFirma =
          this.obtenerInformacionFirma(idApp, firma, opciones, null);

      return informacionFirma.getFirmantes();
    } catch (Exception e) {

      throw new EeutilException(e.getMessage(), e);
    }
  }

  public ListaFirmaInfo obtenerFirmantesMtom(String idApp, DataHandler firma)
      throws EeutilException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      return this.obtenerFirmantes(idApp, firmaBytes);
    } catch (Exception e) {

      throw new EeutilException(
          "Error en peticion EeUtilServiceMtomImpl.obtenerFirmantes:" + e.getMessage(), e);
    }

  }

  public ResultadoValidarCertificado validarCertificado(String idApp, String certificate)
      throws EeutilException {

    try {
      return AfirmaModelToWsModelConverter
          .resultadoValidarCertificadoAfirmaToResultadoValidarCertificado(
              afirmaService.validarCertificado(idApp, certificate, false));
    } catch (Exception e) {

      throw new EeutilException(
          "Error en peticion EeUtilServiceMtomImpl.validarCertificado:" + e.getMessage(), e);
    }
  }

  public InfoCertificado getInfoCertificado(String idApp, String certificate)
      throws EeutilException {
    try {
      return AfirmaModelToWsModelConverter
          .resultadoValidarCertificadoAfirmaToResultadoValidarCertificadoAmpliado(
              afirmaService.validarCertificado(idApp, certificate, true));
    } catch (Exception e) {

      throw new EeutilException(
          "Error en peticion EeUtilServiceMtomImpl.getInfoCertificado:" + e.getMessage(), e);
    }
  }

  public ResultadoAmpliarFirma ampliarFirma(String idApp, byte[] firma,
      ConfiguracionAmpliarFirma configuracion) throws EeutilException {

    ResultadoAmpliarFirma resultadoAmpliarFirma;

    try {

      ResultadoAmpliarFirmaAfirma resAfirma =
          afirmaService.ampliarFirma(idApp, firma, AfirmaModelToWsModelConverter
              .configuracionAmpliarFirmaToConfiguracionAmpliarFirmaAfirma(configuracion));
      resultadoAmpliarFirma = AfirmaModelToWsModelConverter
          .resultadoAmpliarFirmaAfirmaToResultadoAmpliarFirma(resAfirma);
    } catch (EeutilException e) {

      throw new EeutilException(ERROR_EN_AMPLIAR_FIRMA + e.getMessage(), e);
    } catch (Exception t) {

      throw new EeutilException(ERROR_EN_AMPLIAR_FIRMA + t.getMessage(), t);
    }

    return resultadoAmpliarFirma;
  }

  public ResultadoAmpliarFirmaMtom ampliarFirmaMtom(String idApp, DataHandler firma,
      ConfiguracionAmpliarFirma configuracion) throws EeutilException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      ResultadoAmpliarFirma resultado = this.ampliarFirma(idApp, firmaBytes, configuracion);

      ResultadoAmpliarFirmaMtom retorno = new ResultadoAmpliarFirmaMtom();
      DataSource dataSource = new ByteArrayDataSource(resultado.getFirma());
      retorno.setFirma(new DataHandler(dataSource));

      return retorno;
    } catch (EeutilException e) {

      throw new EeutilException(ERROR_EN_AMPLIAR_FIRMA + e.getMessage(), e);
    } catch (Exception e) {

      throw new EeutilException(ERROR_EN_AMPLIAR_FIRMA + " " + e.getMessage(), e);
    }
  }

  public byte[] postProcesarFirma(byte[] firma) throws EeutilException {
    byte[] firmaPost = null;
    try {
      PostProcessFactory factory = PostProcessFactory.getInstance();
      PostProcessor proc =
          factory.getPostProcessor(new AOSignerWrapperEeutils().wrapperGetSigner(firma));

      if (proc == null) {
        EstadoInfo estadoInfo =
            new EstadoInfo(ERROR, "", "No existe postProcessador para este tipo de firma");
        throw new InSideException("No existe postProcessador para este tipo de firma", estadoInfo);
      }

      firmaPost = proc.postProcessSign(firma);
    } catch (IOException ioe) {

      throw new EeutilException(
          ERROR_POSTPROCESANDO_FIRMA + " " + NO_SE_PUEDE_LEER_LA_FIRMA + ioe.getMessage(), ioe);
    } catch (PostProcessException pe) {

      throw new EeutilException(
          ERROR_POSTPROCESANDO_FIRMA + " " + "No se puede procesar la firma: " + pe.getMessage(),
          pe);
    } catch (Exception e) {
      throw new EeutilException(ERROR_POSTPROCESANDO_FIRMA + " "
          + "Error inesperado al procesar la firma: " + e.getMessage(), e);
    }

    return firmaPost;
  }

  public DataHandler postProcesarFirmaMtom(DataHandler firma) throws EeutilException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {
        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
      }

      byte[] retorno = this.postProcesarFirma(firmaBytes);
      DataSource dataSource = new ByteArrayDataSource(retorno);
      return new DataHandler(dataSource);
    } catch (EeutilException ioe) {
      // para no repetir dos veces lo mismo, ya que viene de una llamada de business.
      throw ioe;
    } catch (Exception e) {
      throw new EeutilException(ERROR_POSTPROCESANDO_FIRMA + " "
          + "Error inesperado al procesar la firma: " + e.getMessage(), e);
    }
  }

  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(String idApp, byte[] firma)
      throws EeutilException {
    try {
      ResultadoComprobarFirmaFormatoA retorno = new ResultadoComprobarFirmaFormatoA();

      // obtenemos el tipo de firma DSS
      String tipoFirmaDss = afirmaService.obtenerTipoFirmaDss(idApp, firma, null);

      // realizamos la conversion para validar la firma al formato
      // correspondiente
      String formatoValidacion =
          AfirmaValidationConverterType.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

      // comprobamos si la firma viene en base64
      byte[] firmaValidar = Base64.isBase64(firma) ? firma : Base64.encodeBase64(firma);

      // compobamos si se tiene q hacer la ampliacion de formato
      ResultadoValidacionFormatoAInfo resultadoValidar = AfirmaModelToWsModelConverter
          .resultadoValidacionFirmaFormatoAAfirmaToResultadoValidacionFormatoAInfo(
              afirmaService.validarFirmaFormatoA(idApp, XMLUtil.decodeUTF8(firmaValidar), null,
                  null, null, formatoValidacion));
      if (resultadoValidar.isEstado()) {
        retorno.setEsFirmaA(true);
        retorno.setFechaValidez(resultadoValidar.getFechaValidezCertificadoTSA());
      } else {
        retorno.setEsFirmaA(false);
      }
      return retorno;
    } catch (EeutilException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException("Error en comprobarFirmaFormatoA" + " " + e.getMessage(), e);
    }
  }

  public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoAMtom(String idApp, DataHandler firma)
      throws EeutilException {
    try {
      byte[] firmaBytes = null;
      if (firma != null) {

        firmaBytes = IOUtils.toByteArray(firma.getInputStream());

      }

      return this.comprobarFirmaFormatoA(idApp, firmaBytes);
    } catch (EeutilException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException("Error en comprobarFirmaFormatoA" + " " + e.getMessage(), e);
    }

  }

  /****
   * Servicio que se llama desde misc por soapbinding solamente Misc --> operfirma, no esta en el
   * listado de distribuibles de oper.
   */
  public ResultadoValidacionFirmaInfo validacionFirmaInfo(String idApp, byte[] firma,
      String tipoFirma, DatosFirmados datosFirmados, boolean infoCertificados)
      throws EeutilException {

    if (firma == null) {
      throw new EeutilException(LA_FIRMA_NO_PUEDE_SER_NULA,
          new IllegalArgumentException(LA_FIRMA_NO_PUEDE_SER_NULA));
    }

    try {

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

      return AfirmaModelToWsModelConverter
          .resultadoValidacionInfoAfirmaToResultadoValidacionInfoExt(afirmaService.validarFirmaInfo(
              idApp, firmaElectronica64, datos64, hash64, algoritmo, tipoFirma, infoCertificados));

    } catch (EeutilException e) {
      throw new EeutilException(ERROR_INESPERADO_AL_VALIDAR_FIRMA + " "
          + (e.MSG_AFIRMA != null ? e.MSG_AFIRMA : e.getMessage()), e);
    } catch (Exception e) {
      throw new EeutilException(ERROR_INESPERADO_AL_VALIDAR_FIRMA + " " + e.getMessage(), e);
    }

  }


  public ResultadoValidacionFirmaInfo validacionFirmaInfoMtom(String idApp, DataHandler firma,
      String tipoFirma, DatosFirmadosMtom datosFirmados, boolean infoCertificados)
      throws EeutilException {

    try {

      if (firma == null) {
        throw new EeutilException(LA_FIRMA_NO_PUEDE_SER_NULA,
            new IllegalArgumentException(LA_FIRMA_NO_PUEDE_SER_NULA));
      } else {
        byte[] firmaBytes = null;

        firmaBytes = IOUtils.toByteArray(firma.getInputStream());
        String firmaElectronica64 = Base64.encodeBase64String(firmaBytes);

        String datos64 = null;
        String hash64 = null;
        String algoritmo = null;

        if (datosFirmados != null && datosFirmados.getDocumento() != null) {
          datos64 = Base64.encodeBase64String(
              IOUtils.toByteArray(datosFirmados.getDocumento().getInputStream()));
        }

        if (datosFirmados != null && datosFirmados.getHash() != null) {
          hash64 = Base64
              .encodeBase64String(IOUtils.toByteArray(datosFirmados.getHash().getInputStream()));
        }

        if (datosFirmados != null) {
          algoritmo = datosFirmados.getAlgoritmo();
        }

        return (ResultadoValidacionFirmaInfo) AfirmaModelToWsModelConverter
            .resultadoValidacionInfoAfirmaToResultadoValidacionInfoExt(
                afirmaService.validarFirmaInfo(idApp, firmaElectronica64, datos64, hash64,
                    algoritmo, tipoFirma, infoCertificados));

      }


    } catch (Exception e) {
      throw new EeutilException(ERROR_INESPERADO_AL_VALIDAR_FIRMA + " " + e.getMessage(), e);
    }



  }


  public ResultadoAmpliarFirma resellarFirmaA(String idApp, byte[] firma) throws EeutilException {
    logger.debug("Inicio resellarFirmaA");

    try {
      ConfiguracionAmpliarFirma confAmpliar = new ConfiguracionAmpliarFirma();
      confAmpliar.setCertificadosFirmantes(null);
      confAmpliar.setFormatoAmpliacion(DSSTiposFirmaConstantes.SIGNATURE_MODE_A);
      confAmpliar.setIgnorarPeriodoDeGracia(true);
      return this.ampliarFirma(idApp, firma, confAmpliar);

    } catch (Exception e) {
      throw new EeutilException("ERROR AL RESELLAR FIRMAA" + " " + e.getMessage(), e);
    }

  }

  public ResultadoAmpliarFirmaMtom resellarFirmaAMtom(String idApp, DataHandler firma)
      throws EeutilException {
    logger.debug("Inicio resellarFirmaA");

    ConfiguracionAmpliarFirma confAmpliar = new ConfiguracionAmpliarFirma();
    confAmpliar.setCertificadosFirmantes(null);
    confAmpliar.setFormatoAmpliacion(DSSTiposFirmaConstantes.SIGNATURE_MODE_A);
    confAmpliar.setIgnorarPeriodoDeGracia(true);
    return this.ampliarFirmaMtom(idApp, firma, confAmpliar);
  }

}
