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

package es.mpt.dsic.inside.security.util;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import es.mpt.dsic.inside.utils.exception.EeutilException;

/**
 * Utilidades asociadas a contexto de aplicacion.
 */
@Service("aplicationContextUtils")
public class AplicationContextUtils {


  // private static final String RUTA_LOGO_PARAM="rutaLogo";

  private static final String PREFIJO_RUTA_LOGO_ROTADO = "rotado_";


  // @Autowired
  // private AplicacionContext applicationContext;

  public String getrutaLogoByOrientation(String rutaLogoOriginal, boolean portrait)
      throws EeutilException {

    String rutaLogoOrientation = null;

    // rutaLogoOrientation =
    // applicationContext.getAplicacionInfo().getPropiedades().get(RUTA_LOGO_PARAM);
    rutaLogoOrientation = rutaLogoOriginal;


    // si es rotado.
    if (!portrait) {
      String rutaDirectorio = Paths.get(rutaLogoOrientation).getParent().toString();
      String rutaFichero = Paths.get(rutaLogoOrientation).getFileName().toString();
      rutaFichero = PREFIJO_RUTA_LOGO_ROTADO + rutaFichero;
      rutaLogoOrientation = rutaDirectorio + "/" + rutaFichero;
    }

    if (!Files.exists(Paths.get(rutaLogoOrientation))
        || Files.isDirectory(Paths.get(rutaLogoOrientation))) {
      throw new EeutilException("Error al procesar el logo, la ruta "
          + Paths.get(rutaLogoOrientation).toString() + "no existe o no es un fichero");
    }

    return rutaLogoOrientation;

  }

}
