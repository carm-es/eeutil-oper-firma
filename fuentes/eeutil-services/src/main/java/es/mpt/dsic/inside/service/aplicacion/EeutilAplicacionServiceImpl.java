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

package es.mpt.dsic.inside.service.aplicacion;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.mpt.dsic.inside.convert.ConvertDataEeutil;
import es.mpt.dsic.inside.dao.EeutilDao;
import es.mpt.dsic.inside.model.EeutilAplicacion;
import es.mpt.dsic.inside.model.EeutilAplicacionOperacion;
import es.mpt.dsic.inside.model.EeutilAplicacionPlantilla;
import es.mpt.dsic.inside.model.EeutilAplicacionPropiedad;
import es.mpt.dsic.inside.model.PeticionesPDFA;
import es.mpt.dsic.inside.model.aplicacion.AplicacionObject;
import es.mpt.dsic.inside.utils.exception.EeutilException;


@Service("eeutilAplicacionService")
public class EeutilAplicacionServiceImpl implements EeutilAplicacionService {

  protected static final Log logger = LogFactory.getLog(EeutilAplicacionServiceImpl.class);

  private static final String Eeutil = "Eeutil";

  // private AdminWebService adminInsideWS;

  // private Properties credentialProperties;

  // private Properties propertiesSecurity;

  // private Boolean connectedInside = Boolean.FALSE;


  @Autowired
  private EeutilDao eeutilDao;

  // @Autowired
  // private MessageSource messageSource;

  // @Autowired
  // ConsumeWSUtils consumeWSUtils;

  /*
   * public Properties getCredentialProperties() { return credentialProperties; }
   */
  /*
   * public void setCredentialProperties(Properties credentialProperties) {
   * this.credentialProperties = credentialProperties; }
   */
  /*
   * public Properties getPropertiesSecurity() { return propertiesSecurity; }
   * 
   * public void setPropertiesSecurity(Properties propertiesSecurity) { this.propertiesSecurity =
   * propertiesSecurity; }
   */
  /*
   * private void configureSystemTrustStore() { System.setProperty("javax.net.ssl.trustStore",
   * propertiesSecurity.getProperty("org.apache.ws.security.crypto.merlin.truststore.file"));
   * System.setProperty("javax.net.ssl.trustStorePassword",
   * propertiesSecurity.getProperty("org.apache.ws.security.crypto.merlin.truststore.password"));
   * System.setProperty("javax.net.ssl.trustStoreType",
   * propertiesSecurity.getProperty("org.apache.ws.security.crypto.merlin.truststore.type")); }
   */

  /*
   * public Boolean configureInside() { URL urlInside = null; String urlIns = null; try { if
   * (!connectedInside) { if (credentialProperties != null) { try { urlIns = credentialProperties
   * .getProperty("inside.admin.url"); logger.debug(String.format(
   * "El WS de Inside se encuentra en %s", urlIns)); urlInside = new URL(urlIns); } catch
   * (MalformedURLException me) { logger.error(
   * "No se puede crear la URL del servicio admin de Inside " + urlIns, me); }
   * 
   * AdminWS ssInside = new AdminWS(urlInside); adminInsideWS =
   * ssInside.getPort(AdminWebService.class); configureInside(urlIns); connectedInside =
   * Boolean.TRUE; } } } catch (Exception e) {
   * logger.error("No se puede conectar al servicio Admin de Inside " + urlIns, e); } return
   * connectedInside; }
   */

  /*
   * private void configureInside(String url) { configureSystemTrustStore();
   * 
   * final String passwordCertificado = propertiesSecurity .getProperty("passwordCertificado");
   * 
   * org.apache.cxf.endpoint.Client client = ClientProxy .getClient(adminInsideWS);
   * org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
   * 
   * // Disable Chucking consumeWSUtils.disableChunking(client);
   * 
   * Map<String, Object> outProps = new HashMap<String, Object>();
   * outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE);
   * outProps.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
   * 
   * // cargamos las properties de seguridad outProps.put("CryptoProperties", propertiesSecurity);
   * outProps.put("signaturePropRefId", "CryptoProperties");
   * 
   * outProps.put(WSHandlerConstants.USER,
   * propertiesSecurity.getProperty("WSHandlerConstants.USER"));
   * 
   * outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
   * outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {
   * 
   * @Override public void handle(final Callback[] callbacks) throws IOException,
   * UnsupportedCallbackException { final WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
   * pc.setPassword(passwordCertificado); } });
   * 
   * outProps.put(WSHandlerConstants.SIGNATURE_PARTS,
   * "{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body");
   * 
   * WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
   * cxfEndpoint.getOutInterceptors().add(wssOut);
   * 
   * BindingProvider bp = (BindingProvider) adminInsideWS;
   * bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url); }
   */

  @SuppressWarnings("unchecked")
  @Override
  public List<AplicacionObject> getAplicaciones(String app) throws EeutilException {
    List<AplicacionObject> retorno = null;
    if (app.equals(Eeutil)) {
      List<EeutilAplicacion> data = eeutilDao.getAllObjetos(EeutilAplicacion.class);
      retorno = ConvertDataEeutil.converDataEeutil(data);
    }
    return retorno;
  }


  public EeutilAplicacion getAplicacionEEUTIL(String idAplicacionEEUTIL) {
    return (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class, idAplicacionEEUTIL);
  }

  @Override
  public Map<String, String> getInfAdicional(String app) throws EeutilException {
    Map<String, String> retorno = new HashMap<>();
    if (app.equals(Eeutil)) {
      retorno = ConvertDataEeutil.getDefaultAdicional();
    }
    return retorno;
  }


  @Override
  public AplicacionObject desactivar(String app, AplicacionObject aplicacion)
      throws EeutilException {
    AplicacionObject retorno = null;
    if (app.equals(Eeutil)) {
      EeutilAplicacion apli = (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class,
          aplicacion.getIdentificador());
      apli.setActiva(false);
      eeutilDao.update(apli);
    }
    return retorno;
  }


  @Override
  public AplicacionObject activar(String app, AplicacionObject aplicacion) throws EeutilException {
    AplicacionObject retorno = null;
    if (app.equals(Eeutil)) {
      EeutilAplicacion apli = (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class,
          aplicacion.getIdentificador());
      apli.setActiva(true);
      eeutilDao.update(apli);
    }
    return retorno;
  }


  @Override
  public void eliminarAplicacion(String app, AplicacionObject aplicacion, Locale locale)
      throws EeutilException {
    if (app.equals(Eeutil)) {
      removeAppEeutil(aplicacion.getIdentificador());
    }
  }


  @Override
  public AplicacionObject altaAplicacion(String app, AplicacionObject aplicacion)
      throws EeutilException {
    try {
      AplicacionObject retorno = null;
      if (app.equals(Eeutil)) {
        EeutilAplicacion apli = ConvertDataEeutil.converDataEeutil(aplicacion);
        apli.setActiva(true);
        Object dataBBDD = eeutilDao.getObjeto(EeutilAplicacion.class, apli.getIdaplicacion());
        if (dataBBDD != null) {
          if (StringUtils.isEmpty(aplicacion.getPassword())) {
            apli.setPassword(((EeutilAplicacion) dataBBDD).getPassword());
          }


          // cotejamos las propiedades de bbdd con las actuales, las que no aparezcan actuales
          // que aparezcan en bbdd se borran.
          EeutilAplicacion entity = (EeutilAplicacion) dataBBDD;

          Map<String, String> propBBDD = new HashMap<>();
          Map<String, String> propActual = new HashMap<>();
          if (CollectionUtils.isNotEmpty(entity.getPropiedades())) {
            for (EeutilAplicacionPropiedad propiedad : entity.getPropiedades()) {
              propBBDD.put(propiedad.getId().getPropiedad(), "OK");
            }
          }
          if (CollectionUtils.isNotEmpty(apli.getPropiedades())) {
            for (EeutilAplicacionPropiedad propiedad : apli.getPropiedades()) {
              propActual.put(propiedad.getId().getPropiedad(), "OK");
            }
          }
          // cotejamos ambas, si no estan en las actuales y estan en bbdd la borramos.
          Iterator<Map.Entry<String, String>> it = propBBDD.entrySet().iterator();

          Map<String, String> midBorrables = new HashMap<>();

          while (it.hasNext()) {
            Map.Entry<String, String> propiedadBaseDatos = it.next();

            // si no aparece en actual y aparece en bbdd se borra
            if (propActual.get(propiedadBaseDatos.getKey()) == null) {
              midBorrables.put(propiedadBaseDatos.getKey(), "BORRAR");
            }

          }

          // borramos las propiedades marcadas como borrables.
          if (CollectionUtils.isNotEmpty(entity.getPropiedades())) {
            for (EeutilAplicacionPropiedad propiedad : entity.getPropiedades()) {
              if (midBorrables.get(propiedad.getId().getPropiedad()) != null) {
                eeutilDao.remove(propiedad);
              }

            }
          }
          eeutilDao.update(apli);
        } else {
          eeutilDao.salvar(apli);
        }
      }
      return retorno;
    } catch (NoSuchAlgorithmException e) {
      throw new EeutilException(e.getMessage());
    } catch (UnsupportedEncodingException e) {
      throw new EeutilException(e.getMessage());
    }
  }

  private void removeAppEeutil(String identificador) {
    EeutilAplicacion appEntity =
        (EeutilAplicacion) eeutilDao.getObjeto(EeutilAplicacion.class, identificador);

    // peticiones pdfa
    List<Criterion> critPeticionesPdfA = new ArrayList<>();
    critPeticionesPdfA.add(Restrictions.eq("idAplicacion", identificador));
    List<PeticionesPDFA> peticionesPdfA =
        eeutilDao.findObjetos(PeticionesPDFA.class, critPeticionesPdfA);
    if (CollectionUtils.isNotEmpty(peticionesPdfA)) {
      for (PeticionesPDFA peticionPDFA : peticionesPdfA) {
        eeutilDao.remove(peticionPDFA);
      }
    }


    // aplicacion operacion
    List<Criterion> critAppOperacion = new ArrayList<>();
    critAppOperacion.add(Restrictions.eq("idAplicacion", identificador));
    List<EeutilAplicacionOperacion> appOperaciones =
        eeutilDao.findObjetos(EeutilAplicacionOperacion.class, critAppOperacion);
    if (CollectionUtils.isNotEmpty(appOperaciones)) {
      for (EeutilAplicacionOperacion appOperacion : appOperaciones) {
        eeutilDao.remove(appOperacion);
      }
    }

    // aplicaciones plantillas
    if (CollectionUtils.isNotEmpty(appEntity.getPlantillas())) {
      for (EeutilAplicacionPlantilla plantillApp : appEntity.getPlantillas()) {
        eeutilDao.remove(plantillApp);
      }
    }

    // propiedades aplicacion
    if (CollectionUtils.isNotEmpty(appEntity.getPropiedades())) {
      for (EeutilAplicacionPropiedad propiedadApp : appEntity.getPropiedades()) {
        eeutilDao.remove(propiedadApp);
      }
    }


    // aplicacion
    eeutilDao.remove(appEntity);
  }

}
