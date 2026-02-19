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

package es.mpt.dsic.inside.security.interceptor;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;


public class CustomLogginOUTInterceptor extends LoggingOutInterceptor {

  private static Logger logger = Logger.getLogger(CustomLogginOUTInterceptor.class);

  public CustomLogginOUTInterceptor() {
    super(Phase.PRE_STREAM);
  }

  @Override
  public void handleMessage(Message message) {
    CacheAndWriteOutputStream newOut = null;
    newOut = new CacheAndWriteOutputStream(message.getContent(OutputStream.class));
    message.setContent(OutputStream.class, newOut);
    newOut.registerCallback(new LoggingCallback());
    try {
      if (newOut != null)
        newOut.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }



  }

  public class LoggingCallback implements CachedOutputStreamCallback {
    public void onFlush(CachedOutputStream cos) {}

    public void onClose(CachedOutputStream cos) {
      try {
        StringBuilder builder = new StringBuilder();
        cos.writeCacheTo(builder, limit);
        // String soapXml = builder.toString();

        // guardar en base datos

      } catch (Exception e) {
        logger.error("Error al ejecutar el metodo onClose del interceptor de salida", e);
      } finally {
        if (cos != null) {
          try {
            cos.close();
          } catch (IOException e) {
            logger.error("Error al cerrar el descriptor de fichero", e);
          }
        }
      }

    }
  }
}
