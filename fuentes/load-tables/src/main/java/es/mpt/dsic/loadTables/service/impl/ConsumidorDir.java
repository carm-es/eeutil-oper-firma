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

package es.mpt.dsic.loadTables.service.impl;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import es.mpt.dsic.inside.aop.AuditExternalServiceAnnotation;
import es.mpt.dsic.loadTables.service.model.FormatoFichero;
import es.mpt.dsic.loadTables.service.model.RespuestaWS;
import es.mpt.dsic.loadTables.service.model.SD01UNDescargaUnidades;
import es.mpt.dsic.loadTables.service.model.SD01UNDescargaUnidadesService;
import es.mpt.dsic.loadTables.service.model.TipoConsultaUO;
import es.mpt.dsic.loadTables.service.model.UnidadesWsVersion;
import es.mpt.dsic.loadTables.utils.Constantes;
import es.mpt.dsic.loadTables.utils.DateUtil;

public class ConsumidorDir {

  private String user;
  private String password;
  private String url;
  private SD01UNDescargaUnidades sd01;

  protected static final Log logger = LogFactory.getLog(ConsumidorDir.class);

  private Boolean configured = Boolean.FALSE;

  public Boolean configure() {
    if (!configured) {
      URL urlsc01 = null;
      try {
        logger.debug(String.format("El WS de Consumidor se encuentra en %s", url));
        urlsc01 = new URL(url);

        SD01UNDescargaUnidadesService service1 = new SD01UNDescargaUnidadesService(urlsc01);
        sd01 = service1.getSD01UNDescargaUnidades();
        // timeout 5 min
        setupTimeouts(ClientProxy.getClient(sd01));
        disableChunking(ClientProxy.getClient(sd01));

        configured = true;
      } catch (Exception me) {
        logger.error("No se puede crear la URL del servicio de DIR3 " + url, me);
      }
    }
    return configured;
  }

  @AuditExternalServiceAnnotation(nombreModulo = "load-tables")
  // vamos a volcar datos mes a mes desde la fecha indicada.
  public String volcadoDatosBasicos(Date fechaInicial) throws IOException {
    SimpleDateFormat format = new SimpleDateFormat(Constantes.WS_FORMATO_FECHA);
    UnidadesWsVersion unidadesRequest = new UnidadesWsVersion();
    unidadesRequest.setUsuario(user);
    unidadesRequest.setClave(password);
    unidadesRequest.setFormatoFichero(FormatoFichero.XML);
    unidadesRequest.setTipoConsulta(TipoConsultaUO.COMPLETO);
    unidadesRequest.setFechaInicio(format.format(fechaInicial));
    // sumamos 15 dias
    unidadesRequest.setFechaFin(format.format(DateUtil.sumarDiasDate(fechaInicial, 15)));


    RespuestaWS datos = sd01.exportarV3(unidadesRequest);
    return datos.getFichero();
  }


  @AuditExternalServiceAnnotation(nombreModulo = "load-tables")
  // vamos a volcar datos mes a mes desde la fecha indicada.
  public String volcadoDatosBasicos(String codigoUnidadOrganica) throws IOException {
    UnidadesWsVersion unidadesRequest = new UnidadesWsVersion();
    unidadesRequest.setUsuario(user);
    unidadesRequest.setClave(password);
    unidadesRequest.setFormatoFichero(FormatoFichero.XML);
    unidadesRequest.setTipoConsulta(TipoConsultaUO.COMPLETO);
    unidadesRequest.setCodigo(codigoUnidadOrganica);



    RespuestaWS datos = sd01.exportarV3(unidadesRequest);
    return datos.getFichero();
  }


  @AuditExternalServiceAnnotation(nombreModulo = "load-tables")
  public String incrementalDatosBasicos(Date fecha) throws IOException {
    SimpleDateFormat format = new SimpleDateFormat(Constantes.WS_FORMATO_FECHA);
    UnidadesWsVersion unidadesRequest = new UnidadesWsVersion();
    unidadesRequest.setUsuario(user);
    unidadesRequest.setClave(password);
    unidadesRequest.setFormatoFichero(FormatoFichero.XML);
    unidadesRequest.setTipoConsulta(TipoConsultaUO.COMPLETO);
    unidadesRequest.setFechaInicio(format.format(fecha));
    RespuestaWS datos = sd01.exportarV3(unidadesRequest);

    return datos.getFichero();
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }


  /** 5 minutos de timeout ***/
  private void setupTimeouts(Client client) {

    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy policy = httpConduit.getClient();

    // set time to wait for response in milliseconds. zero means unlimited

    policy.setReceiveTimeout(300000);
    policy.setConnectionTimeout(300000);

  }

  private void disableChunking(Client client) {
    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy policy = httpConduit.getClient();
    policy.setAllowChunking(false);
    policy.setChunkingThreshold(0);
    logger.debug("AllowChunking:" + policy.isAllowChunking());
    logger.debug("ChunkingThreshold:" + policy.getChunkingThreshold());
  }

}
