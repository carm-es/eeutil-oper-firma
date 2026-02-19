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

package es.mpt.dsic.inside.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class BaseController {

  protected synchronized String getVersion() {
    String version = null;

    InputStream is = null;
    // try to load from maven properties first
    try {
      Properties p = new Properties();
      is = getClass().getResourceAsStream("/pom.properties");
      if (is != null) {
        p.load(is);
        version = p.getProperty("version", "");
      }
    } catch (Exception e) {
      // ignore
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    // fallback to using Java API
    if (version == null) {
      Package aPackage = getClass().getPackage();
      if (aPackage != null) {
        version = aPackage.getImplementationVersion();
        if (version == null) {
          version = aPackage.getSpecificationVersion();
        }
      }
    }

    if (version == null) {
      // we could not compute the version so use a blank
      version = "ERROR";
    }

    return version;
  }

}
