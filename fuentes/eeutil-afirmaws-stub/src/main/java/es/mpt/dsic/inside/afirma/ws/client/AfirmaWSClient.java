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

package es.mpt.dsic.inside.afirma.ws.client;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import afirmaws.services.dss.oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.core.schema.DSSSignature;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.core.schema.ResponseBaseType;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.core.schema.UpdatedSignatureType;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.core.schema.VerifyRequest;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.profiles.verificationreport.schema.CertificateValidityType;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.profiles.verificationreport.schema.DetailedReportType;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.profiles.verificationreport.schema.IndividualSignatureReportType;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.profiles.verificationreport.schema.TimeStampValidityType;
import afirmaws.services.dss.oasis.names.tc.dss._1_0.profiles.verificationreport.schema.VerificationReportType;
import afirmaws.services.nativos.model.validarcertificado.InfoCertificadoInfo.Campo;
import afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta;
import afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.ResultadoProcesamiento;
import afirmaws.services.nativos.model.validarfirma.ValidacionFirmaElectronica;
import afirmaws.services.nativos.ws.validarcertificado.Validacion;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.signers.pades.AOPDFSigner;
import es.mpt.dsic.inside.aop.AuditExternalServiceAnnotation;
import es.mpt.dsic.inside.dssprocessing.DSSSignerProcessor;
import es.mpt.dsic.inside.dssprocessing.DSSSignerProcessorException;
import es.mpt.dsic.inside.dssprocessing.DSSSignerProcessorFactory;
import es.mpt.dsic.inside.dssprocessing.DSSUtil;
import es.mpt.dsic.inside.dssprocessing.constantes.DSSResultConstantes;
import es.mpt.dsic.inside.dssprocessing.impl.XMLDetachedDSSSignerProcessor;
import es.mpt.dsic.inside.dssprocessing.impl.XMLEnvelopedDSSSignerProcessor;
import es.mpt.dsic.inside.dssprocessing.impl.XMLEnvelopingDSSSignerProcessor;
import es.mpt.dsic.inside.exception.AfirmaException;
import es.mpt.dsic.inside.model.ContenidoInfoAfirma;
import es.mpt.dsic.inside.model.FirmaInfoAfirma;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.RequestAmpliarFirmaDSS;
import es.mpt.dsic.inside.model.RequestConfigAfirma;
import es.mpt.dsic.inside.model.RequestObtenerInformacionFirma;
import es.mpt.dsic.inside.model.RequestValidarCertificado;
import es.mpt.dsic.inside.model.RequestValidarFirma;
import es.mpt.dsic.inside.model.RequestValidarFirmaDSS;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionFirmaFormatoAAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirmaExt;
import es.mpt.dsic.inside.model.ResultadoValidarCertificadoAfirma;
import es.mpt.dsic.inside.model.TipoDeFirmaAfirma;
import es.mpt.dsic.inside.obtenerinformacionfirma.ContenidoFirmado;
import es.mpt.dsic.inside.obtenerinformacionfirma.ContentNotExtractedException;
import es.mpt.dsic.inside.obtenerinformacionfirma.ObtenerInformacionFirmaUtil;
import es.mpt.dsic.inside.util.AOUtilExt;
import es.mpt.dsic.inside.util.CodigosError;
import es.mpt.dsic.inside.util.InformacionFirmante;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.mime.MimeUtil;
import es.mpt.dsic.inside.utils.pdf.PdfEncr;
import es.mpt.dsic.inside.wrapper.AOSignerWrapperEeutils;

/**
 * Cliente para la llamada a @firma.
 * 
 * @author miguel.moral
 *
 */
public class AfirmaWSClient {

  /**
   * Cadena para error en afirma.
   */
  private static final String ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA =
      "Error de conexion con los servicios web de @Firma.";

  /**
   * Cadena para error en afirma.
   */
  private static final String ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY =
      "Error al realizar la peticion a DSSAfirmaVerify ";

  /**
   * Cadena para error en afirma.
   */
  private static final String ERROR_AL_REALIZAR_LA_PETICION_A_DSS_PROCESSOR_FACTORY =
      "Error al realizar la peticion a DSSProcessorFactory";

  /**
   * Cadena para error en afirma.
   */
  // private static final String ERROR_INESPERADO_AL_INTENTAR_OBTENER_INFORMACION_DE_LA_FIRMA =
  // "Error inesperado al intentar obtener informacion de la firma, ";

  /**
   * Cadena para error en afirma.
   */
  // private static final String ERROR_EN_LA_LLAMADA_A_FIRMA = "Error en la llamada a @firma";

  /**
   * Cadena para error en afirma.
   */
  private static final String ERROR_XML_NO_FIRMA_XML = "El formato de la firma no es v�lido"; // El
                                                                                              // documento
                                                                                              // es
                                                                                              // un
                                                                                              // XML,
                                                                                              // pero
                                                                                              // no
                                                                                              // es
                                                                                              // una
                                                                                              // Firma
                                                                                              // XML:
                                                                                              // p.e.
                                                                                              // un
                                                                                              // documento
                                                                                              // ENI

  /**
   * Cadena para indicar que la firma es valida.
   */
  private static final String LA_FIRMA_ES_VALIDA = "La firma es valida";

  /**
   * Cadena definicion de informacion.
   */
  private static final String DETALLE = "Detalle: ";
  /**
   * Cadena definicion de informacion.
   */
  private static final String PROCESO = "Proceso: ";
  /**
   * Cadena definicion de informacion.
   */
  private static final String CONCLUSION = "Conclusion: ";
  /**
   * Cadena definicion de informacion.
   */
  private static final String RESULTADO = "Resultado: ";
  /**
   * Cadena definicion de informacion.
   */
  private static final String EXCEPCION_ASOCIADA = "ExcepcionAsociada: ";
  /**
   * Cadena definicion de informacion.
   */
  private static final String CODIGO_ERROR = "CodigoError: ";
  /**
   * Cadena definicion de informacion.
   */
  private static final String DESCRIPCION = "Descripcion: ";

  /**
   * Cadena para error en afirma.
   */
  private static final String CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS =
      "Codigos de respuesta de Afirma inesperados: ";

  public static final String JAVAX_NET_SSL_TRUSTSTORE = "javax.net.ssl.trustStore";

  public static final String JAVAX_NET_SSL_TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";

  protected static final Log logger = LogFactory.getLog(AfirmaWSClient.class);

  // @Autowired
  // @Qualifier("firma")
  // private Firma firma;

  // @Autowired
  // @Qualifier("marshallerMvalidafirma")
  private Jaxb2Marshaller marshallerfirma;

  // @Autowired
  // @Qualifier("marshallerMvalidacert")
  private Jaxb2Marshaller marshallercert;

  private Jaxb2Marshaller marshallerdssverify;

  // @Autowired
  // @Qualifier("valCertificado")
  private Validacion valCert;

  private DSSSignature dssAfirmaVerify;

  private DSSSignerProcessorFactory dssProcessorFactory;

  private LoggingOutInterceptor logOut;

  private LoggingInInterceptor logIn;

  private long connectionTimeOut;

  private long receiveTimeOut;

  @PostConstruct
  private void configureEntorno() {

    setupTimeouts(ClientProxy.getClient(dssAfirmaVerify));
    disableChunking(ClientProxy.getClient(dssAfirmaVerify));

    setupTimeouts(ClientProxy.getClient(valCert));
    disableChunking(ClientProxy.getClient(valCert));

    if (logOut != null) {
      logOut.setLimit(-1);
      logOut.setPrettyLogging(Boolean.TRUE);
    } else {
      logger.warn("No se ha configurado el interceptor de salida");
    }
    if (logIn != null) {
      logIn.setLimit(-1);
      logIn.setPrettyLogging(Boolean.TRUE);
    } else {
      logger.warn("No se ha configurado el interceptor de entrada");
    }
  }

  private void setupTimeouts(Client client) {

    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy policy = httpConduit.getClient();

    // set time to wait for response in milliseconds. zero means unlimited

    policy.setReceiveTimeout(receiveTimeOut);
    policy.setConnectionTimeout(connectionTimeOut);

  }

  private void disableChunking(Client client) {
    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy policy = httpConduit.getClient();
    policy.setAllowChunking(false);
    policy.setChunkingThreshold(0);
    logger.debug("AllowChunking:" + policy.isAllowChunking());
    logger.debug("ChunkingThreshold:" + policy.getChunkingThreshold());
  }


  /**
   * Metodo para obtener la informacion de validacion de firma, llama a verify de afirma.
   * 
   * @param requestValidarFirma datos de entrada para validacion de firma.
   * @param infoCertificados true si qeremos informacion de certificados, false en caso contrario.
   * @return resultado de la validacion.
   * @throws EeutilException excepcion generica de eeutils.
   */

  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public ResultadoValidacionInfoAfirmaExt validarFirmaInfo(RequestValidarFirma requestValidarFirma,
      boolean infoCertificados) throws EeutilException {
    // setupTimeouts(ClientProxy.getClient(firma));
    // disableChunking(ClientProxy.getClient(firma));

    // System.setProperty(JAVAX_NET_SSL_TRUSTSTORE,
    // requestValidarFirma.getTruststore());
    // System.setProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD,
    // requestValidarFirma.getPassTruststore());

    ResultadoValidacionInfoAfirmaExt resultado = new ResultadoValidacionInfoAfirmaExt();

    // String peticion = crearPeticionValidar(requestValidarFirma);
    // logger.debug("Peticion a Afirma validar firma:\n" + peticion);

    // String respuesta = null;
    // MensajeSalida msg = null;


    try {

      RequestValidarFirmaDSS requestValidarFirmaDSS =
          generateRequestValidarFirmaDSS(requestValidarFirma);

      if (requestValidarFirmaDSS.getProcessor() == null) {
        resultado.setEstado(false);
        resultado.setDetalle(ERROR_XML_NO_FIRMA_XML);
      } else {

        ResponseBaseType verifyResponse = validarFirmaDSS(requestValidarFirmaDSS);

        // Comprobamos que el formato de la firma es un formato valido
        if (verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
            && verifyResponse.getResult().getResultMinor()
                .contentEquals(DSSResultConstantes.DSS_MINOR_INCORRECTFORMAT)) {
          // logger.info("Los servicios de Afirma responden IncorrectFormat");
          StringBuilder sb = new StringBuilder(CodigosError.MSJ_0003);
          sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
          // logger.error(sb.toString());
          throw new AfirmaException(CodigosError.COD_0003, sb.toString());

          // Admitimos la respuesta
          // "Firma valida, firma no valida, y warning. Para el resto lanzamos una excepcion"

        } else if (!verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_VALIDSIGNATURE)
            && !verifyResponse.getResult().getResultMajor()
                .contentEquals(DSSResultConstantes.DSS_MAJOR_INVALIDSIGNATURE)
            && !verifyResponse.getResult().getResultMajor()
                .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
          StringBuilder sb = new StringBuilder(CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS);
          sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
          // logger.error(sb.toString());
          throw new AfirmaException(CodigosError.COD_0004,
              CodigosError.MSJ_0004 + " " + sb.toString());

        } else

          // obtenemos resultado b�sico: firma v�lida y detalle
          resultado =
              (ResultadoValidacionInfoAfirmaExt) obtainBasicResultadoValidacionAfirma(resultado,
                  verifyResponse);

        if (!verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)) {
          // obtenemos resultado detallado seg�n ws
          resultado =
              obtainDetailedResultadoValidacionAfirma(resultado, verifyResponse, infoCertificados);
        }
      }

    } catch (Exception e) {
      // logger.error(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + e.getMessage(), e);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " "
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    }

    // msg = procesarSalidaValidar(respuesta);

    /*
     * } catch (WebServiceException e) {
     * //logger.error(ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA+" "+ e.getMessage()); msg =
     * this.generarRespuestaError(e); }
     */
    // logger.debug("Respuesta a Afirma de validar firma:\n" + respuesta);

    // String detalle = "";
    //
    // if (msg.getRespuesta().getRespuesta() != null && msg.getRespuesta().getRespuesta().isEstado()
    // )
    // {
    // detalle = LA_FIRMA_ES_VALIDA;
    // resultado.setEstado(true);
    // resultado.setDetalle(detalle);
    //
    // List<Object> content = msg.getRespuesta().getRespuesta().getDescripcion().getContent();
    // ValidacionFirmaElectronica validacionFirmaElectronica = (ValidacionFirmaElectronica)
    // content.get(0);
    //
    // List<Firmante> firmantes =
    // validacionFirmaElectronica.getInformacionAdicional().getFirmante();
    // if ( firmantes != null && firmantes.size() > 0 ) {
    // int numFirmantes = validacionFirmaElectronica.getInformacionAdicional().getFirmante().size();
    // resultado.setNumeroFirmantes(numFirmantes);
    // if ( infoCertificados ) {
    // List<String> certificados = new ArrayList<>();
    // for ( Firmante firmante : firmantes ) {
    // certificados.add( firmante.getCertificado() );
    // }
    // resultado.setCertificados(certificados);
    // }
    // }
    // }
    // else
    // {
    // resultado.setEstado(false);
    // detalle += construyeDetalleRespuesta(msg.getRespuesta().getExcepcion());
    // resultado.setDetalle(detalle);
    // }

    return resultado;
  }


  /**
   * Metodo para obtener la informacion de validacion de firma, llama a verify de afirma.
   * 
   * @param requestValidarFirma datos firma a validar.
   * @return resultado de la validacion.
   * @throws EeutilException excepcion generica de eeutils.
   */
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public ResultadoValidacionInfoAfirma validarFirma(RequestValidarFirma requestValidarFirma)
      throws EeutilException {
    // setupTimeouts(ClientProxy.getClient(firma));
    // disableChunking(ClientProxy.getClient(firma));

    // System.setProperty(JAVAX_NET_SSL_TRUSTSTORE,
    // requestValidarFirma.getTruststore());
    // System.setProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD,
    // requestValidarFirma.getPassTruststore());

    ResultadoValidacionInfoAfirma resultado = new ResultadoValidacionInfoAfirma();

    // String peticion = crearPeticionValidar(requestValidarFirma);
    // // logger.debug("Petici�n a Afirma validar firma:\n" + peticion);
    //
    // String respuesta = null;
    // MensajeSalida msg = null;

    try {
      RequestValidarFirmaDSS requestValidarFirmaDSS =
          generateRequestValidarFirmaDSS(requestValidarFirma);

      if (requestValidarFirmaDSS.getProcessor() == null) {
        resultado.setEstado(false);
        resultado.setDetalle(ERROR_XML_NO_FIRMA_XML);
      } else {

        ResponseBaseType verifyResponse = validarFirmaDSS(requestValidarFirmaDSS);

        // Comprobamos que el formato de la firma es un formato valido
        if (verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
            && verifyResponse.getResult().getResultMinor()
                .contentEquals(DSSResultConstantes.DSS_MINOR_INCORRECTFORMAT)) {
          // logger.info("Los servicios de Afirma responden IncorrectFormat");
          StringBuilder sb = new StringBuilder(CodigosError.MSJ_0003);
          sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
          // logger.error(sb.toString());
          throw new AfirmaException(CodigosError.COD_0003, sb.toString());

          // Admitimos la respuesta
          // "Firma valida, firma no valida, y warning. Para el resto lanzamos una excepcion"

        } else if (!verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_VALIDSIGNATURE)
            && !verifyResponse.getResult().getResultMajor()
                .contentEquals(DSSResultConstantes.DSS_MAJOR_INVALIDSIGNATURE)
            && !verifyResponse.getResult().getResultMajor()
                .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
          StringBuilder sb = new StringBuilder(CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS);
          sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
          // logger.error(sb.toString());
          throw new AfirmaException(CodigosError.COD_0004,
              CodigosError.MSJ_0004 + " " + sb.toString());

        } else

          // obtenemos resultado b�sico: firma v�lida y detalle
          resultado =
              (ResultadoValidacionInfoAfirma) obtainBasicResultadoValidacionAfirma(resultado,
                  verifyResponse);
      }


    } catch (Exception e) {
      if (e instanceof AfirmaException && ((AfirmaException) e).getCode() != null) {
        throw new EeutilException(
            ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " " + e.getMessage(), e);
      } else if (e instanceof WebServiceException) {
        throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " "
            + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
      } else {
        throw new EeutilException(
            ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " " + e.getMessage(), e);
      }

      // logger.error(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + e.getMessage(), e);

    }

    // logger.debug("Respuesta a Afirma de validar firma:\n" + respuesta);

    // String detalle = "";
    //
    //
    // if (msg.getRespuesta().getRespuesta() != null && msg.getRespuesta().getRespuesta().isEstado()
    // )
    // {
    // detalle = LA_FIRMA_ES_VALIDA;
    // resultado.setEstado(true);
    // resultado.setDetalle(detalle);
    //
    // }
    // else
    // {
    // resultado.setEstado(false);
    // detalle += construyeDetalleRespuesta(msg.getRespuesta().getExcepcion());
    // resultado.setDetalle(detalle);
    // }

    return resultado;


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // COMENTO ESTA LOCURA DE CODIGO PORQUE NO TIENE SENTIDO!!!! SI MANDO A AFIRMA UNA FIRMA PARA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// VALIDARLA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// Y
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// YA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ME
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// RESPONDE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// QUE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ES
    // VALIDA/O INVALIDA... A QUE MIRO ESTADO DE LOS CERTIFICADOS!!!!!!! AFIRMA YA HABRA HECHO TODAS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// LAS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// COMPROBACIONES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// NECESARIAS
    // PARA AFIRMAR QUE LA FIRMA ES VALIDA O INVALIDA.
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // boolean firmaCorrecta = true;
    // boolean certCorrectos = true;
    //
    // // Si la respuesta no es nula, se ha podido validar la firma y
    // // comprobamos el resultado.
    // if (msg.getRespuesta().getRespuesta() != null) {
    //
    // List<Object> lista = msg.getRespuesta().getRespuesta()
    // .getDescripcion().getContent();
    // for (Object obj : lista) {
    // ValidacionFirmaElectronica val = (ValidacionFirmaElectronica) obj;
    //
    // // Firma correcta, validamos certificados.
    // if (msg.getRespuesta().getRespuesta().isEstado()) {
    //
    // List<InformacionAdicional.Firmante> firmantes = val
    // .getInformacionAdicional().getFirmante();
    // Iterator<InformacionAdicional.Firmante> itFirmantes = firmantes
    // .iterator();
    //
    // while (itFirmantes.hasNext() && certCorrectos) {
    // InformacionAdicional.Firmante firmante = itFirmantes
    // .next();
    // if ("XADES-A".equalsIgnoreCase(requestValidarFirma.getTipoFirma())
    // || "CADES-A".equalsIgnoreCase(requestValidarFirma.getTipoFirma())) {
    // //validamos el certificado TSA
    // RequestValidarCertificado requestValidarCertificado = new RequestValidarCertificado(
    // new RequestConfigAfirma(requestValidarFirma
    // .getIdAplicacion(), requestValidarFirma
    // .getTruststore(), requestValidarFirma
    // .getPassTruststore()),
    // Base64
    // .encodeBase64String(firmante
    // .getCertificadoTSA()), null, 3, true);
    // Respuesta valCertTsa = this
    // .crearPeticionValidarCertificado(requestValidarCertificado);
    //
    // if (!"0".equalsIgnoreCase(valCertTsa
    // .getResultadoProcesamiento()
    // .getResultadoValidacion().getResultado())) {
    // certCorrectos = false;
    // detalle += construyeDetalleRespuesta(valCertTsa
    // .getResultadoProcesamiento()
    // .getResultadoValidacion().getDescripcion());
    // }
    // } else {
    // //validamos el certificado de la firma
    // RequestValidarCertificado requestValidarCertificado = new RequestValidarCertificado(
    // new RequestConfigAfirma(requestValidarFirma
    // .getIdAplicacion(), requestValidarFirma
    // .getTruststore(), requestValidarFirma
    // .getPassTruststore()),
    // firmante.getCertificado(), false, 3, true);
    // Respuesta valCert = this
    // .crearPeticionValidarCertificado(requestValidarCertificado);
    // // Validacion de certificado correcta
    // if (!"0".equalsIgnoreCase(valCert
    // .getResultadoProcesamiento()
    // .getResultadoValidacion().getResultado())) {
    // certCorrectos = false;
    // detalle += construyeDetalleRespuesta(valCert
    // .getResultadoProcesamiento()
    // .getResultadoValidacion().getDescripcion());
    // }
    // }
    // }
    //
    // // Firma no correcta
    // } else {
    // firmaCorrecta = false;
    // detalle += construyeDetalleRespuesta(val);
    // }
    //
    // }
    //
    // // La respuesta es nula porque no se ha podido validar la firma.
    // } else {
    // firmaCorrecta = false;
    // detalle += construyeDetalleRespuesta(msg.getRespuesta()
    // .getExcepcion());
    // }
    //
    // if (firmaCorrecta && certCorrectos) {
    // detalle = "La firma es v�lida";
    // }
    //
    // resultado.setEstado(firmaCorrecta && certCorrectos);
    // resultado.setDetalle(detalle);

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// fin comentario ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  }

  /*
   * private MensajeSalida generarRespuestaError(Exception e) { MensajeSalida msg; msg = new
   * MensajeSalida(); RespuestaX respuestaError = new RespuestaX();
   * afirmaws.services.nativos.model.validarfirma.MensajeSalida.RespuestaX.Respuesta resp = new
   * afirmaws.services.nativos.model.validarfirma.MensajeSalida.RespuestaX.Respuesta();
   * resp.setEstado(false); Excepcion exp = new Excepcion();
   * exp.setCodigoError(CodigosError.COD_0010); exp.setDescripcion(CodigosError.MSJ_0010+" "+
   * e.getMessage()); respuestaError.setExcepcion(exp); respuestaError.setRespuesta(resp);
   * msg.setRespuesta(respuestaError); return msg; }
   */

  /**
   * metodo para realizar la validacion de un certificado en @firma.
   * 
   * @param requestValidarCertificado datos del certificado.
   * @return resultado de la validacion.
   * @throws EeutilException excepcion generica de eeutils.
   */
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public ResultadoValidarCertificadoAfirma validarCertificado(
      RequestValidarCertificado requestValidarCertificado) throws EeutilException {
    ResultadoValidarCertificadoAfirma resultado = new ResultadoValidarCertificadoAfirma();

    try {
      boolean validado = false;
      String idUsuario = null;
      String numeroSerie = null;
      String detalleValidacion = "";

      Respuesta valCerti = this.crearPeticionValidarCertificado(requestValidarCertificado);


      Map<String, String> resultInfoCertificado = new HashMap<>();

      // Validacion de certificado. Si no ha habido error en afirma no devuelve campo exception
      // relleno
      if (valCerti.getExcepcion() == null) {
        validado = true;
        List<Campo> campos = valCerti.getResultadoProcesamiento().getInfoCertificado().getCampo();
        for (Campo campo : campos) {
          if (campo.getIdCampo().equals("NIFResponsable")) {
            idUsuario = campo.getValorCampo();
          } else if (campo.getIdCampo().equals("numeroSerie")) {
            numeroSerie = campo.getValorCampo();
          } else if (campo.getIdCampo().equals("NIF-CIF") && idUsuario == null) {
            idUsuario = campo.getValorCampo();
          }

          if (Boolean.TRUE.equals(requestValidarCertificado.getInfAmpliada())) {
            resultInfoCertificado.put(campo.getIdCampo(), campo.getValorCampo());
          }
        }

        detalleValidacion += construyeDetalleRespuesta(
            valCerti.getResultadoProcesamiento().getResultadoValidacion().getDescripcion());
      } else {
        detalleValidacion += construyeDetalleRespuesta(valCerti.getExcepcion());
        idUsuario = "";
        resultInfoCertificado.put("Afirma", "Error en Validaci�n");
      }


      if (idUsuario == null) {
        validado = false;
        detalleValidacion =
            "El NIF del responsable no se encuentra en la informaci�n del certificado";
      }

      resultado.setValidado(validado);
      resultado.setIdUsuario(idUsuario);
      resultado.setNumeroSerie(numeroSerie);
      resultado.setDetalleValidacion(detalleValidacion);
      resultado.setInfoCertificado(resultInfoCertificado);
    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

    return resultado;
  }


  /**
   * Metodo para realizar una ampliacion sobre una firma.
   * 
   * @param requestAmpliarFirmaDSS firma a ampliar.
   * @return firma ampliada.
   * @throws EeutilException excepcion generica de eeutils.
   */
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public ResponseBaseType ampliarFirmaDSS(RequestAmpliarFirmaDSS requestAmpliarFirmaDSS)
      throws EeutilException {
    ResponseBaseType vresponse = null;

    System.setProperty(JAVAX_NET_SSL_TRUSTSTORE, requestAmpliarFirmaDSS.getTruststore());
    System.setProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD,
        requestAmpliarFirmaDSS.getPassTruststore());

    // setupTimeouts(ClientProxy.getClient(dssAfirmaVerify));
    // disableChunking(ClientProxy.getClient(dssAfirmaVerify));

    try {

      VerifyRequest vrequest = requestAmpliarFirmaDSS.getProcessor().buildVerifyRequestToUpgrade(
          requestAmpliarFirmaDSS.getIdAplicacion(), requestAmpliarFirmaDSS.getSign(),
          requestAmpliarFirmaDSS.getConfiguracion(), null);

      StringWriter sw = new StringWriter();
      marshallerdssverify.marshal(vrequest, new StreamResult(sw));

      logger.debug("Request @firma:" + sw.toString());


      String sResult = dssAfirmaVerify.verify(sw.toString());

      vresponse = (ResponseBaseType) ((JAXBElement<?>) marshallerdssverify
          .unmarshal(new StreamSource(new StringReader(sResult)))).getValue();

      // si es una firma xades-enveloping traemos la firma de otra manera distinta.
      if (requestAmpliarFirmaDSS.getProcessor() instanceof XMLEnvelopingDSSSignerProcessor) {
        Base64Signature base64Signature = new Base64Signature();
        base64Signature.setValue(getSignAmpXssEnveloping(sResult));
        UpdatedSignatureType updatedSignatureType = (UpdatedSignatureType) DSSUtil
            .getObjectByClass(UpdatedSignatureType.class, vresponse.getOptionalOutputs().getAny());
        updatedSignatureType.getSignatureObject().setBase64Signature(base64Signature);
      }

    } catch (XmlMappingException e) {
      // logger.error("Error al hacer marshall o unmarshall" + e.getMessage(), e);
      throw new EeutilException("Error al hacer marshall o unmarshall "
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    } catch (WebServiceException e) {
      // logger.error(ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA);
      // logger.error(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + e.getMessage(), e);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    } catch (Exception t) {
      // logger.error(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + t.getMessage(), t);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
          + (t.getCause() != null ? t.getCause().getLocalizedMessage() : t.getMessage()), t);
    } catch (Throwable t2) {
      // logger.error(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + t.getMessage(), t);
      throw new EeutilException(
          ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
              + (t2.getCause() != null ? t2.getCause().getLocalizedMessage() : t2.getMessage()),
          t2);
    }
    return vresponse;
  }

  private String construyeDetalleRespuesta(Object obj) {
    final String ln = System.getProperty("line.separator");

    StringBuilder mensaje = new StringBuilder("");
    if (obj instanceof afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion) {
      afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion e =
          (afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion) obj;
      mensaje.append(CODIGO_ERROR + e.getCodigoError() + ln);
      mensaje.append(DESCRIPCION + e.getDescripcion() + ln);
      mensaje.append(EXCEPCION_ASOCIADA + e.getExcepcionAsociada() + ln);
    } else if (obj instanceof afirmaws.services.nativos.model.validarfirma.Excepcion) {
      afirmaws.services.nativos.model.validarfirma.Excepcion e =
          (afirmaws.services.nativos.model.validarfirma.Excepcion) obj;
      mensaje.append(CODIGO_ERROR + e.getCodigoError() + ln);
      mensaje.append(DESCRIPCION + e.getDescripcion() + ln);
      mensaje.append(EXCEPCION_ASOCIADA + e.getExcepcionAsociada() + ln);
    } else if (obj instanceof ValidacionFirmaElectronica) {
      ValidacionFirmaElectronica val = (ValidacionFirmaElectronica) obj;
      mensaje.append(PROCESO + val.getProceso() + ln);
      mensaje.append(DETALLE + val.getDetalle() + ln);
      mensaje.append(CONCLUSION + val.getConclusion() + ln);
    } else if (obj instanceof ResultadoProcesamiento) {
      ResultadoProcesamiento res = (ResultadoProcesamiento) obj;
      mensaje.append(RESULTADO + res.getResultadoValidacion().getResultado() + ln);
      mensaje.append(DESCRIPCION + res.getResultadoValidacion().getDescripcion() + ln);
    } else if (obj instanceof String) {
      String s = (String) obj;
      mensaje.append(s + ln);
    }

    return mensaje.toString();
  }

  /*
   * private MensajeSalida procesarSalidaValidar(String entrada) { StreamSource source = new
   * StreamSource(new StringReader(entrada));
   * 
   * Object obj = marshallerfirma.unmarshal(source); logger.debug(obj); return (MensajeSalida) obj;
   * }
   */

  /*
   * private String crearPeticionValidar(RequestValidarFirma requestValidarFirma) {
   * 
   * MensajeEntrada entrada = new MensajeEntrada();
   * entrada.setPeticion(AfirmaConstantes.OP_VALIDAR_FIRMA);
   * entrada.setVersionMsg(AfirmaConstantes.VERSION_1_0);
   * 
   * Parametros parametros = new Parametros();
   * parametros.setIdAplicacion(requestValidarFirma.getIdAplicacion());
   * parametros.setFirmaElectronica(requestValidarFirma .getFirmaElectronica());
   * 
   * if (requestValidarFirma.getTipoFirma() != null) {
   * parametros.setFormatoFirma(requestValidarFirma.getTipoFirma()); }
   * 
   * if (requestValidarFirma.getDatos() != null) {
   * parametros.setDatos(requestValidarFirma.getDatos()); }
   * 
   * if (requestValidarFirma.getHash() != null) {
   * parametros.setHash(requestValidarFirma.getHash().getBytes()); }
   * 
   * if (requestValidarFirma.getAlgoritmo() != null) {
   * parametros.setAlgoritmoHash(requestValidarFirma.getAlgoritmo()); } //
   * entrada.setParametros(parametros); StringWriter sw = new StringWriter();
   * marshallerfirma.marshal(entrada, new StreamResult(sw));
   * 
   * return sw.toString(); }
   */

  private Respuesta crearPeticionValidarCertificado(
      RequestValidarCertificado requestValidarCertificado) {

    // setupTimeouts(ClientProxy.getClient(valCert));
    // disableChunking(ClientProxy.getClient(valCert));

    System.setProperty(JAVAX_NET_SSL_TRUSTSTORE, requestValidarCertificado.getTruststore());
    System.setProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD,
        requestValidarCertificado.getPassTruststore());

    afirmaws.services.nativos.model.validarcertificado.MensajeEntrada entrada =
        new afirmaws.services.nativos.model.validarcertificado.MensajeEntrada();
    entrada.setPeticion(AfirmaConstantes.OP_VALIDAR_CERTIFICADO);
    entrada.setVersionMsg(AfirmaConstantes.VERSION_1_0);

    afirmaws.services.nativos.model.validarcertificado.MensajeEntrada.Parametros parametros =
        new afirmaws.services.nativos.model.validarcertificado.MensajeEntrada.Parametros();

    parametros.setIdAplicacion(requestValidarCertificado.getIdAplicacion());
    parametros.setModoValidacion(requestValidarCertificado.getModoValidacion());
    parametros.setObtenerInfo(requestValidarCertificado.isObtenerInfo());
    parametros.setCertificado(requestValidarCertificado.getCertificado());

    entrada.setParametros(parametros);
    StringWriter sw = new StringWriter();
    marshallercert.marshal(entrada, new StreamResult(sw));

    String salidaCert = null;
    afirmaws.services.nativos.model.validarcertificado.MensajeSalida salidaCertM = null;

    try {
      salidaCert = valCert.validarCertificado(sw.toString());

      salidaCertM = getSalidaCert(salidaCert);

    } catch (WebServiceException e) {
      // logger.error(ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA+" "+e.getMessage());
      salidaCertM = new afirmaws.services.nativos.model.validarcertificado.MensajeSalida();
      Respuesta resp = new Respuesta();
      afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion exp =
          new afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion();
      exp.setCodigoError(CodigosError.COD_0010);
      exp.setDescripcion(CodigosError.MSJ_0010 + " " + e.getMessage());
      resp.setExcepcion(exp);
      salidaCertM.setRespuesta(resp);
    }

    catch (Exception e) {
      // logger.error(ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA+" "+e.getMessage());
      salidaCertM = new afirmaws.services.nativos.model.validarcertificado.MensajeSalida();
      Respuesta resp = new Respuesta();
      afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion exp =
          new afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion();
      exp.setCodigoError(CodigosError.COD_0000);
      exp.setDescripcion(CodigosError.MSJ_0000 + " " + e.getMessage());
      resp.setExcepcion(exp);
      salidaCertM.setRespuesta(resp);
    }

    catch (Throwable e) {
      // logger.error(ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA+" "+e.getMessage());
      salidaCertM = new afirmaws.services.nativos.model.validarcertificado.MensajeSalida();
      Respuesta resp = new Respuesta();
      afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion exp =
          new afirmaws.services.nativos.model.validarcertificado.MensajeSalida.Respuesta.Excepcion();
      exp.setCodigoError(CodigosError.COD_0000);
      exp.setDescripcion(CodigosError.MSJ_0000 + " " + e.getMessage());
      resp.setExcepcion(exp);
      salidaCertM.setRespuesta(resp);
    }

    return salidaCertM.getRespuesta();
  }

  private afirmaws.services.nativos.model.validarcertificado.MensajeSalida getSalidaCert(
      String entrada)

  {
    StreamSource source = new StreamSource(new StringReader(entrada));

    Object obj = marshallercert.unmarshal(source);
    logger.debug(obj);
    return (afirmaws.services.nativos.model.validarcertificado.MensajeSalida) obj;
  }

  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  private ResponseBaseType validarFirmaDSS(RequestValidarFirmaDSS requestValidarFirmaDSS)
      throws EeutilException {

    // setupTimeouts(ClientProxy.getClient(dssAfirmaVerify));
    // disableChunking(ClientProxy.getClient(dssAfirmaVerify));

    System.setProperty(JAVAX_NET_SSL_TRUSTSTORE, requestValidarFirmaDSS.getTruststore());
    System.setProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD,
        requestValidarFirmaDSS.getPassTruststore());

    ResponseBaseType vresponse = null;

    try {

      VerifyRequest vrequest =
          requestValidarFirmaDSS.getProcessor().buildVerifyRequest(requestValidarFirmaDSS.getSign(),
              requestValidarFirmaDSS.getIdAplicacion(), requestValidarFirmaDSS.getContent());

      StringWriter sw = new StringWriter();
      marshallerdssverify.marshal(vrequest, new StreamResult(sw));

      String sResult = dssAfirmaVerify.verify(sw.toString());

      vresponse = (ResponseBaseType) ((JAXBElement<?>) marshallerdssverify
          .unmarshal(new StreamSource(new StringReader(sResult)))).getValue();

    } catch (XmlMappingException e) {
      // logger.error( "Error al hacer marshall o unmarshall" + e.getMessage(), e);
      throw new EeutilException("Error al hacer marshall o unmarshall "
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    } catch (WebServiceException e) {
      // logger.error(ERROR_DE_CONEXION_CON_LOS_SERVICIOS_WEB_DE_FIRMA);
      // logger.error( ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + e.getMessage(), e);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    } catch (Exception t) {
      // logger.error( ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + t.getMessage(), t);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
          + (t.getCause() != null ? t.getCause().getLocalizedMessage() : t.getMessage()), t);
    } catch (Throwable t2) {
      throw new EeutilException(
          ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
              + (t2.getCause() != null ? t2.getCause().getLocalizedMessage() : t2.getMessage()),
          t2);
    }

    return vresponse;

  }

  /**
   * Metodo para obtenerInformacion de firma de afirma, llama a verify de afirma.
   * 
   * @param requestObtenerInformacionFirma datos de la firma.
   * @return resultado informacion de la firma.
   * @throws AfirmaException excepcion de afirma.
   */
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public InformacionFirmaAfirma obtenerInformacionFirma(
      RequestObtenerInformacionFirma requestObtenerInformacionFirma) throws AfirmaException {

    InformacionFirmaAfirma infoFirma = new InformacionFirmaAfirma();
    try {

      // Obtenemos el objeto que construira la peticion a enviar a los
      // WS de Afirma.
      DSSSignerProcessor processor =
          dssProcessorFactory.getDSSSignerProcessor(requestObtenerInformacionFirma.getFirma());
      if (processor == null) {
        throw new AfirmaException(CodigosError.COD_0001, CodigosError.MSJ_0001);
      }
      /*
       * else if (processor instanceof XMLEnvelopingDSSSignerProcessor) { throw new
       * AfirmaException(CodigosError.COD_0002, CodigosError.MSJ_0002); }
       */

      // Hacemos la llamada a los WS de Afirma
      RequestValidarFirmaDSS requestValidarFirmaDSS = new RequestValidarFirmaDSS(
          new RequestConfigAfirma(requestObtenerInformacionFirma.getIdAplicacion(),
              requestObtenerInformacionFirma.getTruststore(),
              requestObtenerInformacionFirma.getPassTruststore()),
          requestObtenerInformacionFirma.getFirma(), processor,
          requestObtenerInformacionFirma.getContent());
      ResponseBaseType verifyResponse = validarFirmaDSS(requestValidarFirmaDSS);

      // Comprobamos que el formato de la firma es un formato valido
      if (verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
          && verifyResponse.getResult().getResultMinor()
              .contentEquals(DSSResultConstantes.DSS_MINOR_INCORRECTFORMAT)) {
        logger.info("Los servicios de Afirma responden IncorrectFormat");
        StringBuilder sb = new StringBuilder(CodigosError.MSJ_0003);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0003, sb.toString());

        // Admitimos la respuesta
        // "Firma valida, firma no valida, y warning. Para el resto lanzamos una excepcion"

      } else if (!verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_VALIDSIGNATURE)
          && !verifyResponse.getResult().getResultMajor()
              .contentEquals(DSSResultConstantes.DSS_MAJOR_INVALIDSIGNATURE)
          && !verifyResponse.getResult().getResultMajor()
              .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
        StringBuilder sb = new StringBuilder(CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0004,
            CodigosError.MSJ_0004 + " " + sb.toString());

      }

      if (requestObtenerInformacionFirma.isObtenerFirmantes()) {
        // Obtenemos alguna informacion de los firmantes mediante las
        // librerias de Afirma.
        AOSigner signer = new AOSignerWrapperEeutils()
            .wrapperGetSigner(requestObtenerInformacionFirma.getFirma());
        if (signer == null) {
          throw new AfirmaException(CodigosError.COD_0001, CodigosError.MSJ_0001);
        }
        List<InformacionFirmante> firmantes = AOUtilExt.getTreeAsInformacionFirmantes(
            signer.getSignersStructure(requestObtenerInformacionFirma.getFirma(), true));

        // Obtenemos mas informacion de los firmantes, mezclando la
        // informacion que ya teniamos con la que hemos obtenido de
        // los WS de Afirma
        // List<FirmaInfo> listaFirmaInfo =
        // DSSUtil.mergeInfoFirmantes(firmantes,
        // DSSUtil.getReadableCertificateInfoList(verifyResponse));
        List<FirmaInfoAfirma> listaFirmaInfo = DSSUtil.mergeInfoFirmantes(firmantes,
            DSSUtil.getReadableCertificateInfoAndTimeStampContentList(verifyResponse));
        /*
         * ListaFirmaInfo lista = new ListaFirmaInfo (); lista.setInformacionFirmas(listaFirmaInfo);
         * infoFirma.setFirmantes(lista);
         */
        infoFirma.setFirmantes(listaFirmaInfo);
      }

      if (requestObtenerInformacionFirma.isObtenerDatosFirmados()) {
        if (requestObtenerInformacionFirma.getContent() == null) {
          // Obtenemos el contenido firmado
          ContenidoFirmado contenidoFirmado =
              processor.getSignedData(verifyResponse, requestObtenerInformacionFirma.getFirma());

          if (contenidoFirmado.getBytesDocumento() != null) {
            ContenidoInfoAfirma documentoFirmado = new ContenidoInfoAfirma();
            documentoFirmado.setContenido(contenidoFirmado.getBytesDocumento());
            documentoFirmado.setTipoMIME(contenidoFirmado.getMimeDocumento());
            infoFirma.setDocumentoFirmado(documentoFirmado);
          } else if (contenidoFirmado.getHash() != null) {
            infoFirma.setHashFirmado(new String(contenidoFirmado.getHash()));
            infoFirma.setAlgoritmoHashFirmado(contenidoFirmado.getAlgoritmoHash());
          }
        } else {
          ContenidoInfoAfirma documentoFirmado = new ContenidoInfoAfirma();
          documentoFirmado.setContenido(requestObtenerInformacionFirma.getContent());
          documentoFirmado
              .setTipoMIME(MimeUtil.getMimeType(requestObtenerInformacionFirma.getContent()));
          infoFirma.setDocumentoFirmado(documentoFirmado);
        }
      }

      if (requestObtenerInformacionFirma.isObtenerTipoFirma()) {
        // Obtenemos el tipo de firma
        String tipoFirmaDSS = DSSUtil.getSignatureType(verifyResponse);

        String sufijo = "";
        if (processor instanceof XMLDetachedDSSSignerProcessor) {
          sufijo = " DETACHED";
        } else if (processor instanceof XMLEnvelopedDSSSignerProcessor) {
          sufijo = " ENVELOPED";
          // Aunque ahora no admitimos enveloping lo dejamos preparado
        } else if (processor instanceof XMLEnvelopingDSSSignerProcessor) {
          sufijo = " ENVELOPING";
        }

        String tipoFirma = ObtenerInformacionFirmaUtil.getTipoFirma(tipoFirmaDSS) + sufijo;
        // De momento el modo lo dejamos vacio.
        TipoDeFirmaAfirma tipoDeFirma = new TipoDeFirmaAfirma();
        tipoDeFirma.setTipoFirma(tipoFirma);
        infoFirma.setTipoDeFirma(tipoDeFirma);
      }

      infoFirma.setEsFirma(true);

    } catch (ContentNotExtractedException e) {
      // logger.error(ERROR_EN_LA_LLAMADA_A_FIRMA, e);
      throw new AfirmaException(CodigosError.COD_0011, CodigosError.MSJ_0011 + " " + e.getMessage(),
          e);
    } catch (DSSSignerProcessorException e) {
      // logger.error(ERROR_EN_LA_LLAMADA_A_FIRMA, e);
      throw new AfirmaException(CodigosError.COD_0009, CodigosError.MSJ_0009 + " " + e.getMessage(),
          e);
    } catch (AfirmaException e) {
      throw e;
    } catch (Exception t) {
      // logger.error(
      // ERROR_INESPERADO_AL_INTENTAR_OBTENER_INFORMACION_DE_LA_FIRMA,
      // t);
      throw new AfirmaException(CodigosError.COD_0000,
          ERROR_AL_REALIZAR_LA_PETICION_A_DSS_PROCESSOR_FACTORY + " " + t.getMessage(), t);
    } catch (Throwable t) {
      // logger.error(
      // ERROR_INESPERADO_AL_INTENTAR_OBTENER_INFORMACION_DE_LA_FIRMA,
      // t);
      throw new AfirmaException(CodigosError.COD_0000,
          ERROR_AL_REALIZAR_LA_PETICION_A_DSS_PROCESSOR_FACTORY + " " + t.getMessage(), t);
    }

    return infoFirma;
  }


  /**
   * Recibe una firma y una configuracion de ampliacion y devuelve la firma con el upgrade apropiado
   * 
   * @param requestAmpliarFirmaDSS firma y configuracion.
   * @return amplicacion asociada.
   * @throws AfirmaException excepcion de afirma.
   */
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public ResultadoAmpliarFirmaAfirma ampliarFirma(RequestAmpliarFirmaDSS requestAmpliarFirmaDSS)
      throws AfirmaException {

    ResultadoAmpliarFirmaAfirma resultadoAmpliarFirma = new ResultadoAmpliarFirmaAfirma();

    try {

      // Si la firma es PADES y se quiere a�adir sello de tiempo
      // devolvemos un error //
      if (esPADES(requestAmpliarFirmaDSS.getSign()) && AfirmaConstantes.UPGRADE_TIMESTAMP
          .equals(requestAmpliarFirmaDSS.getConfiguracion().getFormatoAmpliacion())) {
        requestAmpliarFirmaDSS.getConfiguracion()
            .setFormatoAmpliacion(AfirmaConstantes.UPGRADE_TIMESTAMP_PDF);
      }

      // Obtenemos el objeto que construir� la petici�n a enviar a los
      // WS de Afirma.
      DSSSignerProcessor processor =
          dssProcessorFactory.getDSSSignerProcessor(requestAmpliarFirmaDSS.getSign());
      if (processor == null) {
        throw new AfirmaException(CodigosError.COD_0001, CodigosError.MSJ_0001);
      } /*
         * else if (processor instanceof XMLEnvelopingDSSSignerProcessor) { throw new
         * AfirmaException(CodigosError.COD_0002, CodigosError.MSJ_0002); }
         */

      // Hacemos la llamada a los WS de Afirma
      requestAmpliarFirmaDSS.setProcessor(processor);
      ResponseBaseType verifyResponse = ampliarFirmaDSS(requestAmpliarFirmaDSS);

      // Comprobamos si se ha realizado la operaci�n correctamente
      if (verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
          && verifyResponse.getResult().getResultMinor()
              .contentEquals(DSSResultConstantes.DSS_MINOR_INCOMPLETEUPGRADEOP)) {
        // logger.error("Los servicios de Afirma responden IncompleteUpgradeOperation para
        // operacion: "
        // + requestAmpliarFirmaDSS.getConfiguracion()
        // .getFormatoAmpliacion());
        StringBuilder sb = new StringBuilder(CodigosError.COD_0005);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0005,
            CodigosError.MSJ_0005 + " " + sb.toString());

        // Comprobamos si el tipo de amplicaci�n introducido es
        // correcto
      } else if (verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
          && verifyResponse.getResult().getResultMinor()
              .contentEquals(DSSResultConstantes.DSS_MINOR_INCORRECTRETURNUPDATEDSIGNTYPE)) {
        // logger.error("Los servicios de Afirma responden IncompleteReturnUpdatedSignatureType para
        // operaci�n: "
        // + requestAmpliarFirmaDSS.getConfiguracion()
        // .getFormatoAmpliacion());
        StringBuilder sb = new StringBuilder(CodigosError.COD_0006);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0006,
            CodigosError.MSJ_0006 + " " + sb.toString());

        // Comprobamos si el tipo de amplicaci�n ha sido introducido
      } else if (verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
          && verifyResponse.getResult().getResultMinor()
              .contentEquals(DSSResultConstantes.DSS_MINOR_UPDATEDSIGNTYPENOTPROVIDED)) {
        // logger.error("Los servicios de Afirma responden UpdatedSignatureTypeNotProvided para
        // operaci�n: "
        // + requestAmpliarFirmaDSS.getConfiguracion()
        // .getFormatoAmpliacion());
        StringBuilder sb = new StringBuilder(CodigosError.COD_0007);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0007,
            CodigosError.MSJ_0007 + " " + sb.toString());

        // Admitimos la respuesta "�xito",
        // "Pendiente de periodo de gracia" y "warning". Para el resto
        // lanzamos una excepci�n"
      } else if (!verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_SUCCESS)
          && !verifyResponse.getResult().getResultMajor()
              .contentEquals(DSSResultConstantes.DSS_MAJOR_PENDING)
          && !verifyResponse.getResult().getResultMajor()
              .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
        StringBuilder sb = new StringBuilder(CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0004,
            CodigosError.MSJ_0004 + " " + sb.toString());
      }

      // Recogemos la firma ampliada //
      byte[] firmaAmpliada = processor.getUpgradedSignature(verifyResponse);
      resultadoAmpliarFirma.setFirma(firmaAmpliada);

    } catch (DSSSignerProcessorException e) {
      throw new AfirmaException(CodigosError.COD_0009, CodigosError.MSJ_0009 + " " + e.getMessage(),
          e);
    } catch (AfirmaException e) {
      throw e;
    } catch (Exception t) {
      // logger.error("Error inesperado al intentar ampliar la firma, ", t);
      throw new AfirmaException(CodigosError.COD_0000, CodigosError.MSJ_0000 + " " + t.getMessage(),
          t);
    } catch (Throwable t2) {
      // logger.error("Error inesperado al intentar ampliar la firma, ", t);
      throw new AfirmaException(CodigosError.COD_0000,
          CodigosError.MSJ_0000 + " " + t2.getMessage(), t2);
    }

    return resultadoAmpliarFirma;
  }

  /**
   * Metodo que comprueba si una firma es en formato PADES
   * 
   * @throws IOException
   */
  private boolean esPADES(byte[] firma) throws EeutilException {
    boolean esPADES = false;

    try {

      // si el documento esta protegido, error
      if (PdfEncr.isProtectedPdf(firma)) {
        throw new EeutilException("El fichero pdf tiene contrase�a y no se puede procesar");
      }

      AOSigner pdfSigner = new AOPDFSigner();

      if (pdfSigner.isSign(firma)) {
        esPADES = true;
      }

    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }



    return esPADES;
  }

  /*
   * public Firma getFirma() { return firma; }
   * 
   * public void setFirma(Firma firma) { this.firma = firma; }
   */

  /**
   * 
   * @return marshallerfirma
   */
  public Jaxb2Marshaller getMarshallerfirma() {
    return marshallerfirma;
  }

  /**
   * 
   * @param marshallerfirma
   */
  public void setMarshallerfirma(Jaxb2Marshaller marshallerfirma) {
    this.marshallerfirma = marshallerfirma;
  }

  /**
   * 
   * @return marshallercert
   */
  public Jaxb2Marshaller getMarshallercert() {
    return marshallercert;
  }

  /**
   * 
   * @param marshallercert
   */
  public void setMarshallercert(Jaxb2Marshaller marshallercert) {
    this.marshallercert = marshallercert;
  }

  /**
   * 
   * @return valCert
   */
  public Validacion getValCert() {
    return valCert;
  }

  /**
   * 
   * @param valCert
   */
  public void setValCert(Validacion valCert) {
    this.valCert = valCert;
  }

  /**
   * 
   * @return marshallerdssverify
   */
  public Jaxb2Marshaller getMarshallerdssverify() {
    return marshallerdssverify;
  }

  /**
   * 
   * @param marshallerdssverify
   */
  public void setMarshallerdssverify(Jaxb2Marshaller marshallerdssverify) {
    this.marshallerdssverify = marshallerdssverify;
  }

  /**
   * 
   * @return dssAfirmaVerify
   */
  public DSSSignature getDssAfirmaVerify() {
    return dssAfirmaVerify;
  }

  /**
   * 
   * @param dssAfirmaVerify
   */
  public void setDssAfirmaVerify(DSSSignature dssAfirmaVerify) {
    this.dssAfirmaVerify = dssAfirmaVerify;
  }

  /**
   * 
   * @return dssProcessorFactory
   */
  public DSSSignerProcessorFactory getDssProcessorFactory() {
    return dssProcessorFactory;
  }

  /**
   * 
   * @param dssProcessorFactory
   */
  public void setDssProcessorFactory(DSSSignerProcessorFactory dssProcessorFactory) {
    this.dssProcessorFactory = dssProcessorFactory;
  }

  /**
   * 
   * @return logOut
   */
  public LoggingOutInterceptor getLogOut() {
    return logOut;
  }

  /**
   * 
   * @param logOut
   */
  public void setLogOut(LoggingOutInterceptor logOut) {
    this.logOut = logOut;
  }

  /**
   * 
   * @return logIn
   */
  public LoggingInInterceptor getLogIn() {
    return logIn;
  }

  /**
   * 
   * @param logIn
   */
  public void setLogIn(LoggingInInterceptor logIn) {
    this.logIn = logIn;
  }

  /**
   * Metodo para obtener el tipo de firma, llama a verify de afirma.
   * 
   * @param requestObtenerInformacionFirma firma.
   * @return informacion de la firma.
   * @throws AfirmaException excecpion de afirma.
   */
  public String obtenerTipoFirmaDss(RequestObtenerInformacionFirma requestObtenerInformacionFirma)
      throws AfirmaException {
    String tipoFirmaDSS = "";

    try {
      // Obtenemos el objeto que construira la peticion a enviar a los
      // WS de Afirma.
      DSSSignerProcessor processor =
          dssProcessorFactory.getDSSSignerProcessor(requestObtenerInformacionFirma.getFirma());
      if (processor == null) {
        throw new AfirmaException(CodigosError.COD_0001, CodigosError.MSJ_0001);
      }
      /*
       * else if (processor instanceof XMLEnvelopingDSSSignerProcessor) { throw new
       * AfirmaException(CodigosError.COD_0002, CodigosError.MSJ_0002); }
       */

      // Hacemos la llamada a los WS de Afirma
      RequestValidarFirmaDSS requestValidarFirmaDSS = new RequestValidarFirmaDSS(
          new RequestConfigAfirma(requestObtenerInformacionFirma.getIdAplicacion(),
              requestObtenerInformacionFirma.getTruststore(),
              requestObtenerInformacionFirma.getPassTruststore()),
          requestObtenerInformacionFirma.getFirma(), processor,
          requestObtenerInformacionFirma.getContent());
      ResponseBaseType verifyResponse = validarFirmaDSS(requestValidarFirmaDSS);

      // Comprobamos que el formato de la firma es un formato valido
      if (verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
          && verifyResponse.getResult().getResultMinor()
              .contentEquals(DSSResultConstantes.DSS_MINOR_INCORRECTFORMAT)) {
        // logger.error("Los servicios de Afirma responden IncorrectFormat");
        StringBuilder sb = new StringBuilder(CodigosError.MSJ_0003);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.error(sb.toString());
        throw new AfirmaException(CodigosError.COD_0003, sb.toString());


        // Admitimos la respuesta
        // "Firma valida, firma no valida, y warning. Para el resto lanzamos una excepcion"

      } else if (!verifyResponse.getResult().getResultMajor()
          .contentEquals(DSSResultConstantes.DSS_MAJOR_VALIDSIGNATURE)
          && !verifyResponse.getResult().getResultMajor()
              .contentEquals(DSSResultConstantes.DSS_MAJOR_INVALIDSIGNATURE)
          && !verifyResponse.getResult().getResultMajor()
              .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
        StringBuilder sb = new StringBuilder(CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS);
        sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
        // logger.info(sb.toString());
        throw new AfirmaException(CodigosError.COD_0004,
            CodigosError.MSJ_0004 + " " + sb.toString());

      }

      tipoFirmaDSS = DSSUtil.getSignatureType(verifyResponse);
    } catch (DSSSignerProcessorException e) {
      // logger.error(ERROR_EN_LA_LLAMADA_A_FIRMA, e);
      throw new AfirmaException(CodigosError.COD_0009, CodigosError.MSJ_0009 + " " + e.getMessage(),
          e);
    } catch (AfirmaException e) {
      throw e;
    } catch (Exception e) {
      // logger.error(ERROR_EN_LA_LLAMADA_A_FIRMA, e);
      throw new AfirmaException(CodigosError.COD_0010, CodigosError.MSJ_0010 + " " + e.getMessage(),
          e);
    } catch (Throwable t) {
      // logger.error(ERROR_EN_LA_LLAMADA_A_FIRMA, e);
      throw new AfirmaException(CodigosError.COD_0010, CodigosError.MSJ_0010 + " " + t.getMessage(),
          t);
    }
    return tipoFirmaDSS;
  }

  /**
   * metodo para validar una firma ampliada.
   * 
   * @param requestValidarFirma
   * @return resultado de la validacion.
   * @throws EeutilException
   */
  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-afirmaws-stub")
  public ResultadoValidacionFirmaFormatoAAfirma validarFirmaFormatoA(
      RequestValidarFirma requestValidarFirma) throws EeutilException {
    // setupTimeouts(ClientProxy.getClient(firma));
    // disableChunking(ClientProxy.getClient(firma));

    // System.setProperty(JAVAX_NET_SSL_TRUSTSTORE,
    // requestValidarFirma.getTruststore());
    // System.setProperty(JAVAX_NET_SSL_TRUSTSTOREPASSWORD,
    // requestValidarFirma.getPassTruststore());

    ResultadoValidacionFirmaFormatoAAfirma resultado = new ResultadoValidacionFirmaFormatoAAfirma();

    // String peticion = crearPeticionValidar(requestValidarFirma);
    // logger.debug("Petici�n a Afirma validar firma:\n" + peticion);

    // String respuesta = null;
    // MensajeSalida msg = null;
    //
    // String detalle = "";
    // boolean firmaCorrecta = true;
    // boolean certCorrectos = true;

    try {
      RequestValidarFirmaDSS requestValidarFirmaDSS =
          generateRequestValidarFirmaDSS(requestValidarFirma);

      if (requestValidarFirmaDSS.getProcessor() == null) {
        resultado.setEstado(false);
        resultado.setDetalle(ERROR_XML_NO_FIRMA_XML);
      } else {
        ResponseBaseType verifyResponse = validarFirmaDSS(requestValidarFirmaDSS);

        // Comprobamos que el formato de la firma es un formato valido
        if (verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
            && verifyResponse.getResult().getResultMinor()
                .contentEquals(DSSResultConstantes.DSS_MINOR_INCORRECTFORMAT)) {
          // logger.info("Los servicios de Afirma responden IncorrectFormat");
          StringBuilder sb = new StringBuilder(CodigosError.MSJ_0003);
          sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
          // logger.error(sb.toString());
          throw new AfirmaException(CodigosError.COD_0003, sb.toString());

          // Admitimos la respuesta
          // "Firma valida, firma no valida, y warning. Para el resto lanzamos una excepcion"

        } else if (!verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_VALIDSIGNATURE)
            && !verifyResponse.getResult().getResultMajor()
                .contentEquals(DSSResultConstantes.DSS_MAJOR_INVALIDSIGNATURE)
            && !verifyResponse.getResult().getResultMajor()
                .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
          StringBuilder sb = new StringBuilder(CODIGOS_DE_RESPUESTA_DE_AFIRMA_INESPERADOS);
          sb.append(DSSUtil.getInfoResult(verifyResponse.getResult()));
          // logger.error(sb.toString());
          throw new AfirmaException(CodigosError.COD_0004,
              CodigosError.MSJ_0004 + " " + sb.toString());

        } else {

          // obtenemos resultado b�sico: firma v�lida y detalle
          resultado = (ResultadoValidacionFirmaFormatoAAfirma) obtainBasicResultadoValidacionAfirma(
              resultado, verifyResponse);

          // da igual si la firma devuelta es inv�lida
          // (para que TF04 sin especificar datos originales lo devuelva bien)

          // obtenemos resultado detallado seg�n ws
          resultado = obtainDetailedResultadoValidacionAfirma(resultado, verifyResponse,
              requestValidarFirma);
        }
      }
    } catch (EeutilException e) {
      // logger.error(
      // ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
      // + e.getMessage(), e);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " "
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    } catch (AfirmaException e) {
      // logger.error(
      // ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
      // + e.getMessage(), e);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " "
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    } catch (Exception e) {
      // logger.error(
      // ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY
      // + t.getMessage(), t);
      throw new EeutilException(ERROR_AL_REALIZAR_LA_PETICION_A_DSS_AFIRMA_VERIFY + " "
          + (e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage()), e);
    }

    // logger.debug("Respuesta a Afirma de validar firma:\n" + respuesta);


    // Si la respuesta no es nula, se ha podido validar la firma y
    // comprobamos el resultado.
    // if (msg.getRespuesta().getRespuesta() != null) {
    //
    // List<Object> lista = msg.getRespuesta().getRespuesta()
    // .getDescripcion().getContent();
    // for (Object obj : lista) {
    // ValidacionFirmaElectronica val = (ValidacionFirmaElectronica) obj;
    //
    // // Firma correcta, validamos certificados.
    // if (msg.getRespuesta().getRespuesta().isEstado()) {
    //
    // List<InformacionAdicional.Firmante> firmantes = val
    // .getInformacionAdicional().getFirmante();
    // Iterator<InformacionAdicional.Firmante> itFirmantes = firmantes
    // .iterator();
    //
    // while (itFirmantes.hasNext() && certCorrectos) {
    // InformacionAdicional.Firmante firmante = itFirmantes
    // .next();
    // RequestValidarCertificado requestValidarCertificado = new RequestValidarCertificado(
    // new RequestConfigAfirma(requestValidarFirma
    // .getIdAplicacion(), requestValidarFirma
    // .getTruststore(), requestValidarFirma
    // .getPassTruststore()),
    // Base64
    // .encodeBase64String(firmante
    // .getCertificadoTSA()), null, 3, true);
    //
    // logger.debug("Certificado TSA:"
    // + Base64.encodeBase64String(firmante
    // .getCertificadoTSA()));
    // Respuesta valCertTsa = this
    // .crearPeticionValidarCertificado(requestValidarCertificado);
    // // Validacion de certificado correcta
    // if (!"0".equalsIgnoreCase(valCertTsa
    // .getResultadoProcesamiento()
    // .getResultadoValidacion().getResultado())) {
    // certCorrectos = false;
    // detalle += construyeDetalleRespuesta(valCertTsa
    // .getResultadoProcesamiento()
    // .getResultadoValidacion().getDescripcion());
    // } else {
    // List<Campo> campos = valCertTsa
    // .getResultadoProcesamiento()
    // .getInfoCertificado().getCampo();
    // for (Campo campo : campos) {
    // if (campo.getIdCampo().equals("validoHasta")) {
    // logger.debug("Fecha Validez TSA:"
    // + campo.getValorCampo());
    // resultado
    // .setFechaValidezCertificadoTSA(campo
    // .getValorCampo());
    // }
    // }
    // }
    // }
    //
    // // Firma no correcta
    // } else {
    // firmaCorrecta = false;
    // detalle += construyeDetalleRespuesta(val);
    // }
    //
    // }
    //
    // // La respuesta es nula porque no se ha podido validar la firma.
    // } else {
    // firmaCorrecta = false;
    // detalle += construyeDetalleRespuesta(msg.getRespuesta()
    // .getExcepcion());
    // }
    //
    // if (firmaCorrecta && certCorrectos) {
    // detalle = LA_FIRMA_ES_VALIDA;
    // }
    //
    // resultado.setEstado(firmaCorrecta && certCorrectos);
    // resultado.setDetalle(detalle);

    return resultado;

  }

  /**
   * 
   * @return connectionTimeOut
   */
  public long getConnectionTimeOut() {
    return connectionTimeOut;
  }

  /**
   * 
   * @param connectionTimeOut
   */
  public void setConnectionTimeOut(long connectionTimeOut) {
    this.connectionTimeOut = connectionTimeOut;
  }

  /**
   * 
   * @return receiveTimeOut
   */
  public long getReceiveTimeOut() {
    return receiveTimeOut;
  }

  /**
   * 
   * @param receiveTimeOut
   */
  public void setReceiveTimeOut(long receiveTimeOut) {
    this.receiveTimeOut = receiveTimeOut;
  }


  private RequestValidarFirmaDSS generateRequestValidarFirmaDSS(
      RequestValidarFirma requestValidarFirma) throws EeutilException {
    RequestValidarFirmaDSS requestValidarFirmaDSS = null;
    try {
      DSSSignerProcessor processor = dssProcessorFactory.getDSSSignerProcessor(
          Base64.decodeBase64(requestValidarFirma.getFirmaElectronica().getBytes()));

      RequestConfigAfirma reqAfirma = new RequestConfigAfirma(requestValidarFirma.getIdAplicacion(),
          requestValidarFirma.getTruststore(), requestValidarFirma.getPassTruststore());

      requestValidarFirmaDSS = new RequestValidarFirmaDSS(reqAfirma,
          Base64.decodeBase64(requestValidarFirma.getFirmaElectronica().getBytes()), processor,
          requestValidarFirma.getDatos() == null ? null
              : Base64.decodeBase64(requestValidarFirma.getDatos().getBytes()));

      requestValidarFirmaDSS.setTruststore(requestValidarFirma.getTruststore());
      requestValidarFirmaDSS.setPassTruststore(requestValidarFirma.getPassTruststore());
    } catch (DSSSignerProcessorException e) {
      throw new EeutilException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilException(CodigosError.COD_0000 + " "
          + ERROR_AL_REALIZAR_LA_PETICION_A_DSS_PROCESSOR_FACTORY + " " + e.getMessage(), e);
    } catch (Throwable t) {
      throw new EeutilException(CodigosError.COD_0000 + " "
          + ERROR_AL_REALIZAR_LA_PETICION_A_DSS_PROCESSOR_FACTORY + " " + t.getMessage(), t);
    }

    return requestValidarFirmaDSS;
  }

  private Object obtainBasicResultadoValidacionAfirma(Object respuesta,
      ResponseBaseType verifyResponse) {
    boolean estado = false;
    String detalle = null;
    if (verifyResponse.getResult().getResultMajor()
        .contentEquals(DSSResultConstantes.DSS_MAJOR_INVALIDSIGNATURE)
        || verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_INSUFFICIENTINFORMATION)
        || verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_PENDING)
        || verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_REQUESTERERROR)
        || verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_RESPONDERERROR)
        || verifyResponse.getResult().getResultMajor()
            .contentEquals(DSSResultConstantes.DSS_MAJOR_WARNING)) {
      estado = false;
      detalle = verifyResponse.getResult().getResultMessage().getValue();
    } else if (verifyResponse.getResult().getResultMajor()
        .contentEquals(DSSResultConstantes.DSS_MAJOR_VALIDSIGNATURE)) {
      estado = true;
      detalle = LA_FIRMA_ES_VALIDA;
    }


    if (respuesta instanceof ResultadoValidacionInfoAfirma) {// validarFirma
      ((ResultadoValidacionInfoAfirma) respuesta).setEstado(estado);
      ((ResultadoValidacionInfoAfirma) respuesta).setDetalle(detalle);
    } else if (respuesta instanceof ResultadoValidacionInfoAfirmaExt) {// validarFirmaInfo
      ((ResultadoValidacionInfoAfirmaExt) respuesta).setEstado(estado);
      ((ResultadoValidacionInfoAfirmaExt) respuesta).setDetalle(detalle);
    } else if (respuesta instanceof ResultadoValidacionFirmaFormatoAAfirma) {// validarFirmaFormatoA
      ((ResultadoValidacionFirmaFormatoAAfirma) respuesta).setEstado(estado);
      ((ResultadoValidacionFirmaFormatoAAfirma) respuesta).setDetalle(detalle);
    }

    return respuesta;
  }

  private ResultadoValidacionInfoAfirmaExt obtainDetailedResultadoValidacionAfirma(
      ResultadoValidacionInfoAfirmaExt resultado, ResponseBaseType verifyResponse,
      boolean infoCertificados) {

    // obtenemos numero de firmantes
    List<Object> optionalOutputs = verifyResponse.getOptionalOutputs().getAny();
    VerificationReportType verificationReport = null;
    for (int i = 0; verificationReport == null && i < optionalOutputs.size(); i++) {
      Object opOut = optionalOutputs.get(i);
      if (opOut instanceof JAXBElement) {
        String jAXBElementClassName = ((JAXBElement) opOut).getDeclaredType().getName();
        String verificationReportTypeClassName = VerificationReportType.class.getName();
        if (jAXBElementClassName.equals(verificationReportTypeClassName)) {
          verificationReport = (VerificationReportType) ((JAXBElement) opOut).getValue();
        }
      }
    }
    if (verificationReport != null) {
      resultado.setNumeroFirmantes(verificationReport.getIndividualSignatureReport().size());
    }

    // si se ha solicitado informaci�n adicional
    if (infoCertificados && verificationReport != null) {
      List<String> certificados = new ArrayList<>();
      for (IndividualSignatureReportType firma : verificationReport
          .getIndividualSignatureReport()) {
        List<Object> detallesFirma = firma.getDetails().getAny();
        DetailedReportType informeDetalleFirma = null;
        for (int i = 0; informeDetalleFirma == null && i < detallesFirma.size(); i++) {
          Object detalleObj = detallesFirma.get(i);
          if (detalleObj instanceof JAXBElement) {
            String jAXBElementClassName = ((JAXBElement) detalleObj).getDeclaredType().getName();
            String detailedReportTypeClassName = DetailedReportType.class.getName();
            if (jAXBElementClassName.equals(detailedReportTypeClassName)) {
              informeDetalleFirma = (DetailedReportType) ((JAXBElement) detalleObj).getValue();
            }
          }
        }
        if (informeDetalleFirma != null) {
          certificados
              .add(new String(Base64.encodeBase64(informeDetalleFirma.getCertificatePathValidity()
                  .getPathValidityDetail().getCertificateValidity().get(0).getCertificateValue())));
        }
      }
      resultado.setCertificados(certificados);
    }

    return resultado;
  }

  private ResultadoValidacionFirmaFormatoAAfirma obtainDetailedResultadoValidacionAfirma(
      ResultadoValidacionFirmaFormatoAAfirma resultado, ResponseBaseType verifyResponse,
      RequestValidarFirma requestValidarFirma) {

    // obtenemos el informe de firmas
    List<Object> optionalOutputs = verifyResponse.getOptionalOutputs().getAny();
    VerificationReportType verificationReport = null;
    for (int i = 0; verificationReport == null && i < optionalOutputs.size(); i++) {
      Object opOut = optionalOutputs.get(i);
      if (opOut instanceof JAXBElement) {
        String jAXBElementClassName = ((JAXBElement) opOut).getDeclaredType().getName();
        String verificationReportTypeClassName = VerificationReportType.class.getName();
        if (jAXBElementClassName.equals(verificationReportTypeClassName)) {
          verificationReport = (VerificationReportType) ((JAXBElement) opOut).getValue();
        }
      }
    }

    if (verificationReport != null) {
      List<String> certificados = new ArrayList<>();

      // obtenemos los certificados de los sellos de tiempo
      for (IndividualSignatureReportType firma : verificationReport
          .getIndividualSignatureReport()) {
        List<Object> detallesFirma = firma.getDetails().getAny();
        DetailedReportType informeDetalleFirma = null;

        // obtenemos el informe de detalle de la firma
        for (int i = 0; informeDetalleFirma == null && i < detallesFirma.size(); i++) {
          Object detalleObj = detallesFirma.get(i);
          if (detalleObj instanceof JAXBElement) {
            String jAXBElementClassName = ((JAXBElement) detalleObj).getDeclaredType().getName();
            String detailedReportTypeClassName = DetailedReportType.class.getName();
            if (jAXBElementClassName.equals(detailedReportTypeClassName)) {
              informeDetalleFirma = (DetailedReportType) ((JAXBElement) detalleObj).getValue();
            }
          }
        }

        if (informeDetalleFirma != null) {
          if (informeDetalleFirma.getProperties() != null
              && informeDetalleFirma.getProperties().getUnsignedProperties() != null
              && informeDetalleFirma.getProperties().getUnsignedProperties()
                  .getUnsignedSignatureProperties() != null
              && informeDetalleFirma.getProperties().getUnsignedProperties()
                  .getUnsignedSignatureProperties()
                  .getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs() != null) {
            TimeStampValidityType validacionSelloTiempo = null;

            // obtenemos la validaci�n del sello de tiempo
            for (int i = 0; validacionSelloTiempo == null && i < informeDetalleFirma.getProperties()
                .getUnsignedProperties().getUnsignedSignatureProperties()
                .getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs().size(); i++) {
              Object counterSigObj = informeDetalleFirma.getProperties().getUnsignedProperties()
                  .getUnsignedSignatureProperties()
                  .getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs().get(i);
              if (counterSigObj instanceof JAXBElement) {
                String jAXBElementClassName =
                    ((JAXBElement) counterSigObj).getDeclaredType().getName();
                String timeStampValidityTypeClassName = TimeStampValidityType.class.getName();
                if (jAXBElementClassName.equals(timeStampValidityTypeClassName)) {
                  validacionSelloTiempo =
                      (TimeStampValidityType) ((JAXBElement) counterSigObj).getValue();
                }
              }
            }

            if (validacionSelloTiempo != null) {
              if (validacionSelloTiempo.getCertificatePathValidity() != null
                  && validacionSelloTiempo.getCertificatePathValidity()
                      .getPathValidityDetail() != null
                  && validacionSelloTiempo.getCertificatePathValidity().getPathValidityDetail()
                      .getCertificateValidity() != null) {

                // obtenemos los certificados de los sellos de tiempo
                for (CertificateValidityType validezCertificado : validacionSelloTiempo
                    .getCertificatePathValidity().getPathValidityDetail()
                    .getCertificateValidity()) {
                  certificados.add(
                      new String(Base64.encodeBase64(validezCertificado.getCertificateValue())));
                }

              }
            }
          }
        }
      }

      // recorremos los certificados de los sellos de tiempo para validarlos
      for (String certificado : certificados) {
        // preparamos petici�n para validar el certificado
        RequestValidarCertificado requestValidarCertificado = new RequestValidarCertificado(
            new RequestConfigAfirma(requestValidarFirma.getIdAplicacion(),
                requestValidarFirma.getTruststore(), requestValidarFirma.getPassTruststore()),
            certificado, null, 3, true);

        // validamos el certificado
        Respuesta valCertTsa = this.crearPeticionValidarCertificado(requestValidarCertificado);

        if ("0".equalsIgnoreCase(
            valCertTsa.getResultadoProcesamiento().getResultadoValidacion().getResultado())) {
          // si la validaci�n es correcta buscamos la propiedad donde informa de la fecha de validez
          List<Campo> campos =
              valCertTsa.getResultadoProcesamiento().getInfoCertificado().getCampo();
          for (int i = 0; resultado.getFechaValidezCertificadoTSA() == null
              && i < campos.size(); i++) {
            Campo campo = campos.get(i);
            if (campo.getIdCampo().equals("validoHasta")) {
              logger.debug("Fecha Validez TSA:" + campo.getValorCampo());

              resultado.setEstado(true);
              resultado.setDetalle("Es FirmaA");
              resultado.setFechaValidezCertificadoTSA(campo.getValorCampo());
            }
          }
        }
      }

      // si no se ha rellenado la fecha de validez del certificado TSA es porque
      // no se ha encontrado ninguna ampliaci�n
      // o las ampliaciones encontradas no son validas
      if (resultado.getFechaValidezCertificadoTSA() == null) {
        resultado.setEstado(false);
        resultado.setDetalle("No es FirmaA o no tiene validez");
      }
    }

    return resultado;
  }

  private static byte[] getSignAmpXssEnveloping(String sResultDSS) throws IOException {
    // System.out.println(content);
    int indexIni = sResultDSS.indexOf("ds:Signature");
    int indexFin = sResultDSS.lastIndexOf("ds:Signature");

    if (indexIni == -1 || indexFin == -1 || (indexIni == indexFin)) {
      return null;
    }

    String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + sResultDSS.substring(indexIni - 1, indexFin + 13);
    return content.getBytes(StandardCharsets.UTF_8);

  }


  /*
   * public static void main(String [] args) throws EeutilException, IOException,
   * InterruptedException {
   * 
   * for(int i=0;i<20000;i++) {
   * 
   * System.out.println(i); File f=new File("E:/Downloads/kk.xml"); byte
   * []bb=FileUtils.readFileToByteArray(f);
   * 
   * AfirmaWSClient af=new AfirmaWSClient();
   * 
   * af.esPADES(bb);
   * 
   * }
   * 
   * int i=0; do {
   * 
   * System.out.println("Stopped "+i++);
   * 
   * }while(true);
   * 
   * 
   * 
   * }
   * 
   */

  public static void main(String[] args) throws EeutilException, IOException {

    boolean esPADES = false;

    // esPADES=PdfEncr.isProtectedPdf(FileUtils.readFileToByteArray(new
    // File("e:/Downloads/resres.pdf")));

    // boolean esPADES=false;
    /*
     * AOSigner pdfSigner = AOSignerFactory.getSigner(FileUtils.readFileToByteArray(new
     * File("e:/Downloads/kk35pag-protected.pdf")));
     */
    AOSigner pdfSigner = new AOPDFSigner();
    if (pdfSigner
        .isSign(FileUtils.readFileToByteArray(new File("e:/Downloads/kk35pag-protected.pdf")))) {
      esPADES = true;
    }

    System.out.println(esPADES);
  }

}
