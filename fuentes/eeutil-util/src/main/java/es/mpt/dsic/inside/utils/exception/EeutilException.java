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

package es.mpt.dsic.inside.utils.exception;

import javax.xml.ws.WebServiceException;

import es.mpt.dsic.inside.exception.AfirmaException;

public class EeutilException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1585368938084545551L;

  public String getCOD_AFIRMA() {
    return COD_AFIRMA;
  }

  public String getMSG_AFIRMA() {
    return MSG_AFIRMA;
  }

  public String getCOD_ERROR_RUN() {
    return COD_ERROR_RUN;
  }

  public String getMSG_ERROR_RUN() {
    return MSG_ERROR_RUN;
  }

  public String COD_AFIRMA;

  public String MSG_AFIRMA;

  public String COD_ERROR_RUN;

  public String MSG_ERROR_RUN;

  public EeutilException() {
    super();
    // TODO Auto-generated constructor stub
  }

  public EeutilException(String message, Throwable cause) {
    super(message, cause);

    if (cause instanceof AfirmaException) {
      AfirmaException exception = (AfirmaException) cause;
      COD_AFIRMA = exception.getCode();
      MSG_AFIRMA = exception.getMessage();
    }
    // Especializacion para intentar quitarnos los mensajes de "Could not send message"
    else if (cause instanceof WebServiceException) {
      WebServiceException exception = (WebServiceException) cause;
      COD_AFIRMA = null;
      MSG_AFIRMA = message;
    }

    else if (cause instanceof EeutilException) {
      EeutilException exception = (EeutilException) cause;
      COD_AFIRMA = exception.COD_AFIRMA;
      MSG_AFIRMA = exception.MSG_AFIRMA;
    }
  }

  public EeutilException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }


  /**
   * 
   * @param codErrorRUN
   * @param msgErrorRun
   * @param distintintivoRun. Este parametro solo lo usamos para que no sea un constructor ambiguo
   */
  public EeutilException(String codErrorRUN, String msgErrorRun, boolean distintintivoRun) {
    super("codErrorRUN " + msgErrorRun);
    this.COD_ERROR_RUN = codErrorRUN;
    this.MSG_ERROR_RUN = msgErrorRun;
  }



  /*
   * public EeutilException(Throwable cause) { super(cause); // TODO Auto-generated constructor stub
   * }
   */



}
