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

package es.mpt.dsic.inside.pdf.converter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirma;
import es.mpt.dsic.inside.pdf.converter.utils.RestIgaeServiceB;
import es.mpt.dsic.inside.pdf.rest.libreoffice.ClientSecureRestLibreofficeConv;
import es.mpt.dsic.inside.pdf.rest.libreoffice.ConfigureRestInfoConv;
import es.mpt.dsic.inside.services.AfirmaService;
import es.mpt.dsic.inside.util.CodigosError;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.utils.file.FileUtil;
import es.mpt.dsic.inside.utils.io.IOUtil;
import es.mpt.dsic.inside.utils.mime.MimeUtil;
import es.mpt.dsic.inside.utils.string.StringUtil;

@Component
public class PdfConverter {

  private static final String STATUS_CONST = " status:";

  private static final String ERROR_AL_ACCEDER_AL_SERVICIO_DE_IGAE =
      "ERROR AL ACCEDER AL SERVICIO DE IGAE ";

  private static final String ERROR_AL_ACCEDER_AL_SERVICIO_DE_LIBREOFFICE =
      "ERROR_AL_ACCEDER_AL_SERVICIO_DE_LIBREOFFICE";

  private static final String GENERIC_PREFIX = "generic";

  protected final static Log logger = LogFactory.getLog(PdfConverter.class);

  @Autowired
  OfficeToPdfConverter officeToPdfConverter;

  // @Autowired
  // TiffToPdfConverter tiffToPdfConverter;

  @Autowired
  GenericPdfConverter genericPdfConverter;

  // @Autowired
  // XmlToPdfConverter xmlToPdfConverter;

  @Autowired
  AfirmaService afirmaService;

  @Autowired
  RestIgaeServiceB restIgaeServiceB;

  @Autowired
  ClientSecureRestLibreofficeConv clientSecureRestLibreofficeConv;

  /**
   * Convierte a PDF
   */
  public File convertir(String ipOO, String portOO, File contenido, String mimeType, String idApp,
      String baseUrlIgae, String tokenNameRestIgae, String tokenValueRestIgae)
      throws EeutilException {
    String mime = mimeType;



    File pdf = null;

    logger.debug("Mime recibido de la peticion : " + mimeType);

    try {

      if (mime == null) {
        mime = MimeUtil.getMimeNotNull(IOUtil.getBytesFromObject(contenido));
      }

      if (mime.contentEquals("text-tcn/html") || mime.contentEquals("text/tcn")) {

        // pdf = tcnToPdfConverter.convertTCNToPdf(IOUtil
        // .getBytesFromObject(contenido));

        byte[] bContenido = IOUtil.getBytesFromObject(contenido);

        // pdf = tcnToPdfConverter.convertTCNToPdf(bContenido);


        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;

        try {
          // org.apache.http.ssl.SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
          // sslContextBuilder.loadTrustMaterial(new
          // org.apache.http.conn.ssl.TrustSelfSignedStrategy());
          // SSLContext sslContext = sslContextBuilder.build();
          // org.apache.http.conn.ssl.SSLConnectionSocketFactory sslSocketFactory = new
          // SSLConnectionSocketFactory(
          // sslContext, new org.apache.http.conn.ssl.DefaultHostnameVerifier());
          //
          // HttpClientBuilder httpClientBuilder =
          // HttpClients.custom().setSSLSocketFactory(sslSocketFactory);
          // httpClient = httpClientBuilder.build();
          //
          //
          // HttpPost httpPost = new HttpPost(baseUrlIgae+"/api/convertirTCN");
          //
          // String json = Base64.encodeBase64String(bContenido);
          // StringEntity entity = new StringEntity(json);
          // //chunked a false
          // entity.setChunked(false);
          // httpPost.setEntity(entity);
          //
          // httpPost.setHeader("Accept", "application/json");
          // httpPost.setHeader("Content-type", "application/json");
          // httpPost.addHeader(tokenNameRestIgae,tokenValueRestIgae);
          //
          // response = httpClient.execute(httpPost);

          response = restIgaeServiceB.getResponseClientIgae(httpClient,
              baseUrlIgae + "/api/convertirTCN", bContenido, tokenNameRestIgae, tokenValueRestIgae);

          HttpEntity entityResponse = response.getEntity();

          if (response.getStatusLine().getStatusCode() != 200) {
            throw new EeutilException(ERROR_AL_ACCEDER_AL_SERVICIO_DE_IGAE + baseUrlIgae
                + STATUS_CONST + response.getStatusLine().getStatusCode());
          } else {
            String filePath = FileUtil.createFilePath(GENERIC_PREFIX);
            FileUtils.writeByteArrayToFile(new File(filePath),
                Base64.decodeBase64(IOUtils.toByteArray(entityResponse.getContent())));
            pdf = new File(filePath);

          }

        } catch (Exception e) {
          throw new EeutilException(e.getMessage(), e);
        } finally {
          restIgaeServiceB.cerrarRequestResponse(httpClient, response);
        }

      } else if (StringUtil.contiene(mime, "pdf")) {

        pdf = contenido;

      } else if (mime.equals("image/tiff")) {
        // con la version 2.1.7 de itext no funciona.
        // pdf = tiffToPdfConverter.convertTiffToPDF(contenido);
        throw new EeutilException("No se puede convertir desde este formato image/tiff");

      } else if (StringUtil.contiene(mime, "jpg") || StringUtil.contiene(mime, "BMP")
          || StringUtil.contiene(mime, "image") || StringUtil.contiene(mime, "jpeg")

          || StringUtil.contiene(mime, "gif") || StringUtil.contiene(mime, "bmp")
          || StringUtil.contiene(mime, "png")) {
        pdf = genericPdfConverter.convertPdfGeneric(contenido, true);
      }

      else if (mime.equals("application/xml") || mime.equals("text/xml")
          || mime.equals("image/svg+xml") || mime.equals("application/svg+xml")) {

        try {
          try {
            // pdf = xmlToPdfConverter.convertXMLToPdf(IOUtil
            // .getBytesFromObject(contenido));


            byte[] bContenido = IOUtil.getBytesFromObject(contenido);


            CloseableHttpClient httpClient = null;
            CloseableHttpResponse response = null;

            try {
              // org.apache.http.ssl.SSLContextBuilder sslContextBuilder =
              // SSLContextBuilder.create();
              // sslContextBuilder.loadTrustMaterial(new
              // org.apache.http.conn.ssl.TrustSelfSignedStrategy());
              // SSLContext sslContext = sslContextBuilder.build();
              // org.apache.http.conn.ssl.SSLConnectionSocketFactory sslSocketFactory = new
              // SSLConnectionSocketFactory(
              // sslContext, new org.apache.http.conn.ssl.DefaultHostnameVerifier());
              //
              // HttpClientBuilder httpClientBuilder =
              // HttpClients.custom().setSSLSocketFactory(sslSocketFactory);
              // httpClient = httpClientBuilder.build();
              //
              // HttpPost httpPost = new HttpPost(baseUrlIgae+"/api/convertirXML");
              //
              // String json = Base64.encodeBase64String(bContenido);
              // StringEntity entity = new StringEntity(json);
              // httpPost.setEntity(entity);
              //
              // httpPost.setHeader("Accept", "application/json");
              // httpPost.setHeader("Content-type", "application/json");
              // httpPost.addHeader(tokenNameRestIgae,tokenValueRestIgae);
              //
              // response = httpClient.execute(httpPost);

              response = restIgaeServiceB.getResponseClientIgae(httpClient,
                  baseUrlIgae + "/api/convertirXML", bContenido, tokenNameRestIgae,
                  tokenValueRestIgae);

              HttpEntity entityResponse = response.getEntity();

              if (response.getStatusLine().getStatusCode() != 200) {
                throw new EeutilException(ERROR_AL_ACCEDER_AL_SERVICIO_DE_IGAE + baseUrlIgae
                    + STATUS_CONST + response.getStatusLine().getStatusCode());
              } else {
                String filePath = FileUtil.createFilePath(GENERIC_PREFIX);
                FileUtils.writeByteArrayToFile(new File(filePath),
                    Base64.decodeBase64(IOUtils.toByteArray(entityResponse.getContent())));
                pdf = new File(filePath);

              }

            } catch (Exception e) {
              throw new EeutilException(e.getMessage(), e);
            } finally {
              restIgaeServiceB.cerrarRequestResponse(httpClient, response);
            }
          } catch (Exception e) {
            // Si da excepcion intentamos sacar el contenido de la firma

            byte[] byteContenido = FileUtils.readFileToByteArray(contenido);
            // comprobamos si el contenido es una firma o no. Esto es nuevo y habra que validarlo
            // si el contenido de la firma no es null sacamos el contenido.
            // AOSigner aoSigner=new AOSignerWrapperEeutils().wrapperGetSigner(byteContenido,mime);

            ResultadoValidacionInfoAfirma validacionFirmaInfo = null;
            boolean esFirma = false;

            try {
              validacionFirmaInfo = afirmaService.validarFirma(idApp,
                  Base64.encodeBase64String(byteContenido), null, null, null, null);
              esFirma = validacionFirmaInfo.isEstado();
              // no es una firma y se procesa como no firma
              if (!esFirma && validacionFirmaInfo.getDetalle() != null
                  && validacionFirmaInfo.getDetalle().contains("El formato de la firma no es")) {
                esFirma = false;
              }
              // en cualquier otro caso se cuenta como una firma.
              else {
                esFirma = true;
              }
            }
            // si lanza una excepcion
            catch (EeutilException t) {
              // es un formato no valido de firma (no se reconoce como firma)
              // Error al realizar la peticion a DSSAfirmaVerify HTTP response '413: Request Entity
              // Too Large' when communicating with
              // https://des-afirma.redsara.es/afirmaws/services/DSSAfirmaVerify
              // hemos hecho una personalizacion de excepciones en EEutilException para las
              // excepciones de tipo WebServiceException
              if ((t.getCOD_AFIRMA() != null && t.getCOD_AFIRMA().equals(CodigosError.COD_0003))) {
                esFirma = false;
              } else if ((t.getMSG_AFIRMA() != null
                  ? t.getMSG_AFIRMA().indexOf("Request Entity Too Large") != -1
                  : false)) {
                // evaluamos si es una posible firma mirando los primeros bytes
                if (IOUtil.esPosibleFormatoFirmaCadesXadesAnalyzeBytes(byteContenido)) {
                  // ERROR es una posible firma
                  throw t;
                } else {
                  esFirma = false;
                }

              } else {
                esFirma = true;
              }
            }



            // validamos si es firma correcta
            if (esFirma) {
              byte[] cont = null;
              InformacionFirmaAfirma info = afirmaService.obtenerInformacionFirma(idApp,
                  byteContenido, false, true, false, cont);
              // sobreescrimos en el fichero contenido el contenido de la firma
              FileUtils.writeByteArrayToFile(contenido, info.getDocumentoFirmado().getContenido());


              try {

                // pdf = xmlToPdfConverter.convertXMLToPdf(IOUtil
                // .getBytesFromObject(contenido));


                byte[] bContenido = IOUtil.getBytesFromObject(contenido);


                CloseableHttpClient httpClient = null;
                CloseableHttpResponse response = null;

                try {
                  // org.apache.http.ssl.SSLContextBuilder sslContextBuilder =
                  // SSLContextBuilder.create();
                  // sslContextBuilder.loadTrustMaterial(new
                  // org.apache.http.conn.ssl.TrustSelfSignedStrategy());
                  // SSLContext sslContext = sslContextBuilder.build();
                  // org.apache.http.conn.ssl.SSLConnectionSocketFactory sslSocketFactory = new
                  // SSLConnectionSocketFactory(
                  // sslContext, new org.apache.http.conn.ssl.DefaultHostnameVerifier());
                  //
                  // HttpClientBuilder httpClientBuilder =
                  // HttpClients.custom().setSSLSocketFactory(sslSocketFactory);
                  // httpClient = httpClientBuilder.build();
                  //
                  // HttpPost httpPost = new HttpPost(baseUrlIgae+"/api/convertirXML");
                  //
                  // String json = Base64.encodeBase64String(bContenido);
                  // StringEntity entity = new StringEntity(json);
                  // httpPost.setEntity(entity);
                  //
                  // httpPost.setHeader("Accept", "application/json");
                  // httpPost.setHeader("Content-type", "application/json");
                  // httpPost.addHeader(tokenNameRestIgae,tokenValueRestIgae);
                  //
                  // response = httpClient.execute(httpPost);

                  response = restIgaeServiceB.getResponseClientIgae(httpClient,
                      baseUrlIgae + "/api/convertirXML", bContenido, tokenNameRestIgae,
                      tokenValueRestIgae);

                  HttpEntity entityResponse = response.getEntity();

                  if (response.getStatusLine().getStatusCode() != 200) {
                    throw new EeutilException(ERROR_AL_ACCEDER_AL_SERVICIO_DE_IGAE + baseUrlIgae
                        + STATUS_CONST + response.getStatusLine().getStatusCode());
                  } else {
                    String filePath = FileUtil.createFilePath(GENERIC_PREFIX);
                    FileUtils.writeByteArrayToFile(new File(filePath),
                        Base64.decodeBase64(IOUtils.toByteArray(entityResponse.getContent())));
                    pdf = new File(filePath);

                  }

                } catch (Exception e2) {
                  throw new EeutilException(e.getMessage(), e2);
                } finally {
                  restIgaeServiceB.cerrarRequestResponse(httpClient, response);
                }
              } catch (Exception e1) {

                // si da error, no es de la igae y lo intentamos imprimir intentando sacar el mime.
                mime = MimeUtil.getMimeType(info.getDocumentoFirmado().getContenido());
                // DocumentFormat inFormat = registry.getFormatByMimeType(fileExtension) si le pasas
                // aplication/xml devuelve null
                if (mime.equals("application/xml") || mime.equals("text/xml")) {
                  mime = "text/plain";
                }
                // si es una firma y el contenido de la firma no es xml
                else {
                  return convertir(ipOO, portOO, contenido, mime, idApp, baseUrlIgae,
                      tokenNameRestIgae, tokenValueRestIgae);
                }
                throw new EeutilException(
                    "si da error, suponemos que no es de la igae y lo intentamos imprimir como intentando sacar el mime");
              }


            }
            // si no es una firma, intentaremos sacarlo como text plain
            else {
              mime = "text/plain";
              throw new EeutilException("Sacamos el contenido como application/xml normal");
            }

          }
        }
        // si falla la conversion por igae hacemos la conversion normal
        catch (EeutilException e) {



          if (!ConfigureRestInfoConv.isActiveNewLibreoffice()) {
            // ojo no a�adir log.error
            pdf = officeToPdfConverter.convertOfficeToPDFByMIME(contenido, mime, ipOO, portOO);
          } else {
            pdf = convertirRest(contenido, mime);
          }
        }
      }
      // cualquiera menos imagenes, xml, tcn
      else {
        /*
         * if(mime.equals("application/rtf")) { mime = "text/rtf";
         * 
         * }
         */
        if (!ConfigureRestInfoConv.isActiveNewLibreoffice()) {
          // ojo no a�adir log.error
          pdf = officeToPdfConverter.convertOfficeToPDFByMIME(contenido, mime, ipOO, portOO);
        } else {
          pdf = convertirRest(contenido, mime);
        }


      }



      /*
       * if (mime.contentEquals(
       * "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
       * 
       * logger.debug("INTENTANDO CONVERTIR DOCX A PDF LLAMANDO A OPENOFFICE O LIBREOFFICE");
       * logger.debug("IP  : "+ipOO); logger.debug("PORT: "+portOO);
       * 
       * pdf = officeToPdfConverter.convertOfficeToPDFByMIME(contenido,mime, ipOO, portOO);
       * 
       * } else if (StringUtil.contiene(mime, "text") || StringUtil.contiene(mime, "doc") ||
       * StringUtil.contiene(mime, "ppt") || StringUtil.contiene(mime, "ms") ||
       * StringUtil.contiene(mime, "msword") || StringUtil.contiene(mime, "xls") ||
       * StringUtil.contiene(mime, "rtf") || StringUtil.contiene(mime, "wpd")) {
       * 
       * if (StringUtil.contiene(mime, "xml")) mime = "text/plain";
       * 
       * pdf = officeToPdfConverter.convertOfficeToPDFByMIME(contenido,mime, ipOO, portOO);
       * 
       * 
       * } else if (StringUtil.contiene(mime, "tiff") || StringUtil.contiene(mime, "jpg") ||
       * StringUtil.contiene(mime, "BMP") || StringUtil.contiene(mime, "image") ||
       * StringUtil.contiene(mime, "jpeg") || StringUtil.contiene(mime, "tif") ||
       * StringUtil.contiene(mime, "gif") || StringUtil.contiene(mime, "bmp") ||
       * StringUtil.contiene(mime, "gif") || StringUtil.contiene(mime, "png")) {
       * 
       * else { pdf = genericPdfConverter .convertPdfGeneric(contenido, true); } } else { pdf =
       * genericPdfConverter.convertPdfGeneric(contenido, false); }
       */
      return pdf;
    } catch (EeutilException e) {
      throw new EeutilException(
          "Error en metodo convertir, el mime proporcionado es: Valor: " + mime == null ? "nulo"
              : mime + " " + e.getMessage(),
          e);
    } catch (Exception t) {
      throw new EeutilException(
          "Error en metodo convertir, el mime proporcionado es: Valor: " + mime == null ? "nulo"
              : mime + " " + t.getMessage(),
          t);
    }

  }



  private File convertirRest(File contenido, String mimeType) throws EeutilException {

    CloseableHttpClient httpClient = null;
    CloseableHttpResponse response = null;
    File pdf = null;
    try {

      byte[] bytesEntrada = FileUtils.readFileToByteArray(contenido);

      String urlBaseRestLibreoffice = ConfigureRestInfoConv.getBaseUrlSecureLibreoffice();
      String nameTokenLibreoffice = ConfigureRestInfoConv.getTokenNameLibreoffice();
      String valueTokenLibreoffice = ConfigureRestInfoConv.getTokenValueLibreoffice();

      response = clientSecureRestLibreofficeConv.getResponseClientLibreoffice(httpClient,
          urlBaseRestLibreoffice + "/api/convertirPdfLibreOffice", mimeType, bytesEntrada,
          nameTokenLibreoffice, valueTokenLibreoffice);

      HttpEntity entityResponse = response.getEntity();

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new EeutilException(ERROR_AL_ACCEDER_AL_SERVICIO_DE_LIBREOFFICE
            + urlBaseRestLibreoffice + " STATUS " + response.getStatusLine().getStatusCode());
      } else {
        String filePath = FileUtil.createFilePath(GenericPdfConverter.GENERIC_PREFIX);
        FileUtils.writeByteArrayToFile(new File(filePath),
            Base64.decodeBase64(IOUtils.toByteArray(entityResponse.getContent())));
        pdf = new File(filePath);
        return pdf;

      }

    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    } finally {
      try {
        clientSecureRestLibreofficeConv.cerrarRequestResponse(httpClient, response);
      } catch (IOException e) {
        throw new EeutilException(e.getMessage(), e);
      }
    }

  }



}
