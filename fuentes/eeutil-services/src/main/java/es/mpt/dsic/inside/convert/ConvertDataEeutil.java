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

package es.mpt.dsic.inside.convert;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import es.mpt.dsic.inside.model.EeutilAplicacion;
import es.mpt.dsic.inside.model.EeutilAplicacionPropiedad;
import es.mpt.dsic.inside.model.EeutilAplicacionPropiedadId;
import es.mpt.dsic.inside.model.aplicacion.AplicacionObject;

public class ConvertDataEeutil {


  public static final String FIRMA = "firma";
  public static final String SELLO = "sello";
  public static final String TRAMITAR = "tramitar";

  public static List<AplicacionObject> converDataEeutil(List<EeutilAplicacion> data) {
    List<AplicacionObject> retorno = new ArrayList<>();

    // agrupaci�n de datos
    Map<String, EeutilAplicacion> agrupados = new HashMap<>();
    if (CollectionUtils.isNotEmpty(data)) {
      for (EeutilAplicacion app : data) {
        if (!agrupados.containsKey(app.getIdaplicacion())) {
          agrupados.put(app.getIdaplicacion(), app);
        }
      }
    }

    if (CollectionUtils.isNotEmpty(agrupados.entrySet())) {
      for (Entry<String, EeutilAplicacion> app : agrupados.entrySet()) {
        retorno.add(converDataEeutil(app.getValue()));
      }
    }
    return retorno;
  }

  public static AplicacionObject converDataEeutil(EeutilAplicacion data) {
    AplicacionObject retorno = new AplicacionObject();
    retorno.setIdentificador(data.getIdaplicacion());
    retorno.setActivo(data.isActiva());
    retorno.setAditionalData(convertAditionalData(data));
    retorno.setEmail(data.getEmail());
    retorno.setResponsable(data.getResponsable());
    retorno.setTelefono(data.getTelefono());
    retorno.setCodigoUnidadOrganica(data.getUnidad());
    return retorno;
  }

  public static EeutilAplicacion converDataEeutil(AplicacionObject data)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    EeutilAplicacion retorno = new EeutilAplicacion();
    retorno.setActiva(data.isActivo());
    retorno.setEmail(data.getEmail());
    retorno.setResponsable(data.getResponsable());
    retorno.setIdaplicacion(data.getIdentificador());
    retorno.setDescripcion(data.getIdentificador());
    retorno.setTelefono(data.getTelefono());

    if (StringUtils.isNotEmpty(data.getPassword())) {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(data.getPassword().getBytes(StandardCharsets.UTF_8));
      byte[] digest = md.digest();
      // convert the byte to hex format method 1
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < digest.length; i++) {
        sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
      }
      retorno.setPassword(sb.toString());
    }

    if (data.getAditionalData() != null) {
      if (data.getAditionalData().containsKey(FIRMA)
          && data.getAditionalData().get(FIRMA).equalsIgnoreCase("S")) {
        retorno.setFirma(true);
      } else {
        retorno.setFirma(false);
      }
      data.getAditionalData().remove(FIRMA);
    }

    if (data.getAditionalData() != null) {
      if (data.getAditionalData().containsKey(SELLO)
          && data.getAditionalData().get(SELLO).equalsIgnoreCase("S")) {
        retorno.setSello(true);
      } else {
        retorno.setSello(false);
      }
      data.getAditionalData().remove(SELLO);
    }

    if (data.getAditionalData() != null) {
      if (data.getAditionalData().containsKey(TRAMITAR)
          && data.getAditionalData().get(TRAMITAR).equalsIgnoreCase("S")) {
        retorno.setTramitar(true);
      } else {
        retorno.setTramitar(false);
      }
      data.getAditionalData().remove(TRAMITAR);
    }

    if (data.getAditionalData() != null) {
      retorno.setPropiedades(convertPropiedades(data.getIdentificador(), data.getAditionalData()));
    }
    retorno.setUnidad(data.getCodigoUnidadOrganica());

    return retorno;
  }

  public static Set<EeutilAplicacionPropiedad> convertPropiedades(String app,
      Map<String, String> data) {
    Set<EeutilAplicacionPropiedad> retorno = new HashSet<>();



    if (data != null) {
      Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, String> datoIterado = it.next();
        String clave = datoIterado.getKey();
        if (StringUtils.isNotEmpty(clave) && StringUtils.isNotEmpty(data.get(clave))) {
          retorno.add(new EeutilAplicacionPropiedad(new EeutilAplicacionPropiedadId(app, clave),
              data.get(clave)));
        }
      }
    }

    return retorno;
  }

  public static Map<String, String> convertAditionalData(EeutilAplicacion data) {
    Map<String, String> retorno = new HashMap<>();
    retorno.put(FIRMA, data.isFirma() ? "S" : "N");
    retorno.put(SELLO, data.isSello() ? "S" : "N");
    retorno.put(TRAMITAR, data.isTramitar() ? "S" : "N");
    if (CollectionUtils.isNotEmpty(data.getPropiedades())) {
      for (EeutilAplicacionPropiedad propiedad : data.getPropiedades()) {
        if (!retorno.containsKey(propiedad.getId().getPropiedad())) {
          retorno.put(propiedad.getId().getPropiedad(), propiedad.getValor());
        }
      }
    }
    return retorno;
  }

  public static Map<String, String> getDefaultAdicional() {
    Map<String, String> retorno = new HashMap<>();
    retorno.put(FIRMA, "S");
    retorno.put(SELLO, "S");
    retorno.put(TRAMITAR, "S");
    retorno.put("algoritmoFirmaDefecto", "SHA256withRSA");
    retorno.put("aliasCertificado", "");
    retorno.put("formatoFirmaDefecto", "Adobe PDF");
    retorno.put("modoFirmaDefecto", "implicit");
    retorno.put("passwordCertificado", "<noaplica>");
    retorno.put("passwordKS", "");
    retorno.put("rutaKS", "");
    retorno.put("tipoKS", "");
    retorno.put("ip.openoffice", "");
    retorno.put("port.openoffice", "");
    retorno.put("rutaLogo", "${local_home_app}/sgtic/conf/escudo.jpg");
    retorno.put("escalaLogoX", "");
    retorno.put("escalaLogoY", "");
    return retorno;
  }



}
