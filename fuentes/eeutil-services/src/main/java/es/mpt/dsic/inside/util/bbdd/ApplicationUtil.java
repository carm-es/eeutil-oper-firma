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

package es.mpt.dsic.inside.util.bbdd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApplicationUtil {

  protected final static Log logger = LogFactory.getLog(ApplicationUtil.class);



  public final static String EEUTIL_FIRMA = "EEUTIL-FIRMA";

  public final static String EEUTIL_UTIL_FIRMA = "EEUTIL-UTIL-FIRMA";

  public final static String EEUTIL_OPER_FIRMA = "EEUTIL-OPER-FIRMA";

  public final static String EEUTIL_VIS_DOCEXP = "EEUTIL-VIS-DOCEXP";

  public final static String EEUTIL_MISC = "EEUTIL-MISC";


  /**
   * Carga los modulos que hay en la VM.
   * 
   * @return
   */
  /*
   * public static List<String> getAplicacionesGestionadasServidor() {
   * 
   * 
   * 
   * 
   * 
   * 
   * List<String> aAplicacionesGestionadas=new ArrayList<>();
   * 
   * if (System.getProperty("eeutil-firma.config.path") != null) {
   * aAplicacionesGestionadas.add(EEUTIL_FIRMA); } if
   * (System.getProperty("eeutil-util-firma.config.path") != null) {
   * aAplicacionesGestionadas.add(EEUTIL_UTIL_FIRMA); } if
   * (System.getProperty("eeutil-oper-firma.config.path") != null) {
   * aAplicacionesGestionadas.add(EEUTIL_OPER_FIRMA); } if
   * (System.getProperty("eeutil-vis-docexp.config.path") != null) {
   * aAplicacionesGestionadas.add(EEUTIL_VIS_DOCEXP); } if
   * (System.getProperty("eeutil-misc.config.path") != null) {
   * aAplicacionesGestionadas.add(EEUTIL_MISC); }
   * 
   * if(aAplicacionesGestionadas.isEmpty()) {
   * logger.error("Error al obtener el parametro [MODULO_EEUTIL].config.path de la VM"); throw new
   * RuntimeException("Error al obtener el parametro [MODULO_EEUTIL].config.path de la VM"); }
   * 
   * return aAplicacionesGestionadas; }
   */

  public static String getAplicacionGestionada() {
    // Intentamos instanciar con reflection una clase de cada uno de los modulos.

    try {
      Class.forName("es.mpt.dsic.inside.util.SecurityUtils");
      return EEUTIL_FIRMA;
    } catch (ClassNotFoundException e) {
      // si no esta instanciada una clase de firma.
      try {
        // util-firma
        Class.forName("es.mpt.dsic.inside.util.SignedData");
        return EEUTIL_UTIL_FIRMA;
      } catch (ClassNotFoundException e1) {
        // oper-firma
        try {
          Class.forName("es.mpt.dsic.inside.ws.service.postprocess.PostProcessUtil");
          return EEUTIL_OPER_FIRMA;
        } catch (ClassNotFoundException e2) {
          // vis-doc-exp
          try {
            Class.forName("es.mpt.dsic.inside.visualizacion.PieAdder");
            return EEUTIL_VIS_DOCEXP;
          } catch (ClassNotFoundException e3) {
            // misc
            try {
              Class.forName("es.mpt.dsic.eeutil.misc.web.util.WebConstants");
              return EEUTIL_MISC;
            } catch (ClassNotFoundException e4) {
              logger.error("Error, no se ha podido identificar el modulo de la aplicacion");
            }

          }

        }

      }

    }


    return null;
  }



}
