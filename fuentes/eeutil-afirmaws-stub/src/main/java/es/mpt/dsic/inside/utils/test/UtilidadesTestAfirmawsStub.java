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

package es.mpt.dsic.inside.utils.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SLF4JLocationAwareLog;

public class UtilidadesTestAfirmawsStub implements IPruebaTraza {
  protected static final Log logger = LogFactory.getLog(UtilidadesTestAfirmawsStub.class);

  public String testTrazaImp() {
    boolean bImpSLF4JLocationAwareLog = false;

    if (logger instanceof SLF4JLocationAwareLog) {
      bImpSLF4JLocationAwareLog = true;
    }
    logger.error("Is implementation SLF4JLocationAwareLog " + bImpSLF4JLocationAwareLog
        + " Is error enabled: " + logger.isErrorEnabled()
        + " Verificacion de que la traza en eeutil-afirmaws-stub es correcta");

    return "Is implementation SLF4JLocationAwareLog " + bImpSLF4JLocationAwareLog
        + " Is error enabled: " + logger.isErrorEnabled()
        + " Verificacion de que la traza en eeutil-afirmaws-stub es correcta";
  }
}
