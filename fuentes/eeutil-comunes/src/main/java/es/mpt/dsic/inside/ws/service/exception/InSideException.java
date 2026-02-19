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

package es.mpt.dsic.inside.ws.service.exception;

import javax.xml.ws.WebFault;

import org.apache.log4j.MDC;

import es.mpt.dsic.inside.utils.exception.EeutilException;
import es.mpt.dsic.inside.ws.service.model.EstadoInfo;

@WebFault(faultBean = "es.mpt.dsic.inside.ws.bean.EstadoInfo", name = "ErrorTest")
public class InSideException extends Exception {
  private static final long serialVersionUID = 1L;

  private EstadoInfo estadoInfo;

  public InSideException(String message, EstadoInfo estadoInfo) {
    super(message);
    this.estadoInfo = estadoInfo;
  }

  public InSideException(EstadoInfo estadoInfo) {
    super();
    this.estadoInfo = estadoInfo;
  }

  public InSideException(String message, EstadoInfo estadoInfo, Throwable cause) {
    super(message, cause);
    this.estadoInfo = estadoInfo;
  }

  public InSideException(String message, Throwable cause) {
    super(message, cause);

    if (cause instanceof EeutilException) {
      EeutilException exception = (EeutilException) cause;
      if (exception.COD_AFIRMA != null) {
        String msg = exception.MSG_AFIRMA == null ? message : exception.MSG_AFIRMA;
        this.estadoInfo =
            new EstadoInfo("ERROR", exception.COD_AFIRMA, "UUID: " + MDC.get("uUId") + " " + msg);
      } else {
        this.estadoInfo =
            new EstadoInfo("ERROR", "ERROR", "UUID: " + MDC.get("uUId") + " " + message);
      }
    }


  }

  public void setEstadoInfo(EstadoInfo estadoInfo) {
    this.estadoInfo = estadoInfo;
  }

  public EstadoInfo getEstadoInfo() {
    return estadoInfo;
  }

  public EstadoInfo getFaultInfo() {
    return estadoInfo;
  }
}
