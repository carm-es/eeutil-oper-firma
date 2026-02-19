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
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;

public class CustomLogginInInterceptor extends LoggingInInterceptor {



  protected final static Log logger = LogFactory.getLog(CustomLogginInInterceptor.class);

  private long limite = ((1024 * 1024) * 1024);

  @Override
  public void handleMessage(Message message) throws Fault {
    try {
      super.handleMessage(message);
      try (InputStream is = message.getContent(InputStream.class);
          CachedOutputStream os = new CachedOutputStream(limite);) {
        IOUtils.copy(is, os);
        os.flush();
        message.setContent(InputStream.class, os.getInputStream());



        String msg = IOUtils.toString(os.getInputStream());
        // llamada web service, habilitar interceptores en xx-ws.xml para que funcione
        logger.debug("LLAMADA de WS (Habilitar interc" + msg);

        message.put("xmlRequest", msg.getBytes(Charset.forName("UTF-8")));
      }
    } catch (IOException | Fault e) {
      logger.error("Exception interceptando mensaje: ", e);
    } catch (Exception e) {
      logger.error("Exception interceptando mensaje: ", e);
    }

  }

}
