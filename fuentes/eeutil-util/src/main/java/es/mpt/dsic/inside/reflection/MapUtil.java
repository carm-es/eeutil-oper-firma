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

package es.mpt.dsic.inside.reflection;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class MapUtil {
  public static String mapToString(Map<String, String> map) {
    StringBuilder stringBuilder = new StringBuilder();

    for (String key : map.keySet()) {
      if (stringBuilder.length() > 0) {
        stringBuilder.append("#########");
      }
      String value = map.get(key);
      stringBuilder.append((key != null ? "[" + key + "]" : ""));
      stringBuilder.append("=");
      stringBuilder.append(value != null ? "(" + value + ")" : "");
    }

    return stringBuilder.toString();
  }

  public static Map<String, String> stringToMap(String input) {
    Map<String, String> map = new HashMap<String, String>();

    String[] nameValuePairs = input.split("&");
    for (String nameValuePair : nameValuePairs) {
      String[] nameValue = nameValuePair.split("=");
      try {
        map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
            nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("This method requires UTF-8 encoding support", e);
      }
    }

    return map;
  }
}
