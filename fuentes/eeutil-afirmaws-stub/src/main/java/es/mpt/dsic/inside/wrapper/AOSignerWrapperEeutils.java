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

package es.mpt.dsic.inside.wrapper;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.core.signers.AOSignerFactory;
import es.mpt.dsic.inside.utils.exception.EeutilException;

/**
 * WRAPPER DEL ACCESO PARA SABER SI UN CONTENIDO ES UNA FIRMA EN LOS MODULOS PRINCIPALES DE EEUTILS
 * (5)
 * 
 * @author miguel.moral
 *
 */

public class AOSignerWrapperEeutils {

  private static final Log logger = LogFactory.getLog(AOSignerWrapperEeutils.class);

  public AOSignerWrapperEeutils() {
    super();
  }

  public AOSigner wrapperGetSigner(byte[] firma) throws EeutilException {

    try {
      Long millisIni = new Date().getTime();
      AOSigner signer = AOSignerFactory.getSigner(firma);
      Long millisFin = new Date().getTime();
      if (millisFin - millisIni > 20000l) {
        logger.error("El tiempo de llamada a la validacion si es una firma es excesivo "
            + ((millisFin - millisIni) / 1000) + " .sg. Tamano del fichero:" + (firma.length / 1000)
            + "KB.");
      }
      return signer;
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }

  }

  public AOSigner wrapperGetSigner(byte[] firma, String mime) throws EeutilException {

    try {
      Long millisIni = new Date().getTime();
      AOSigner signer = AOSignerFactory.getSigner(firma);
      Long millisFin = new Date().getTime();
      if (millisFin - millisIni > 20000l) {
        logger.error("El tiempo de llamada a la validacion si es una firma es excesivo "
            + ((millisFin - millisIni) / 1000) + " .sg. Tamano del fichero:" + (firma.length / 1000)
            + "KB. Mime: " + mime);
      }
      return signer;
    } catch (IOException e) {
      throw new EeutilException(e.getMessage(), e);
    }

  }

}
