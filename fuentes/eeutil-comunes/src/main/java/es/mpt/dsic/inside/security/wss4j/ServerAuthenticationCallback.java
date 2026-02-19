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

package es.mpt.dsic.inside.security.wss4j;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.beans.factory.annotation.Autowired;

import es.mpt.dsic.inside.security.model.AppInfo;
import es.mpt.dsic.inside.security.service.AplicacionInfoService;
import es.mpt.dsic.inside.utils.exception.EeutilException;

public class ServerAuthenticationCallback implements CallbackHandler {

  protected static final Log logger = LogFactory.getLog(ServerAuthenticationCallback.class);

  @Autowired
  private AplicacionInfoService aplicacionInfoService;

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    logger.debug("Inicio ServerCallback");


    try {

      for (int i = 0; i < callbacks.length; i++) {
        WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
        String id = pwcb.getIdentifier();
        int usage = pwcb.getUsage();

        switch (usage) {
          case WSPasswordCallback.UNKNOWN:
            logger.debug("ServerCallback UNKNOWN");
            break;
          case WSPasswordCallback.DECRYPT:
            logger.debug("ServerCallback DECRYPT");
            break;
          case WSPasswordCallback.SIGNATURE:
            logger.debug("ServerCallback SIGNATURE");
            break;
          case WSPasswordCallback.USERNAME_TOKEN:
            logger.debug("ServerCallback USERNAME_TOKEN");
            AppInfo app = aplicacionInfoService.getAplicacionInfo(id);
            pwcb.setPassword(app.getPassword());
            break;
        }
      }
      logger.debug("Fin ServerCallback");

    } catch (EeutilException e) {
      logger.error(e.getMessage(), e);
      throw new IOException(e.getMessage(), e);
    }

  }
}
