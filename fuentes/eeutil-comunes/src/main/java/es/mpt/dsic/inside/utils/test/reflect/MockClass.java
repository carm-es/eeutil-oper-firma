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

package es.mpt.dsic.inside.utils.test.reflect;

public class MockClass {

  private final static String CINCO = "5";

  public String devuelveAlgo(String a) {
    String resultado = "";

    if ("5".equals(a)) {
      resultado = "hola";
    } else if ("6".equals(a)) {
      resultado = "adios";
    }

    return resultado;

  }

  private String devuelveAlgoSinParam() {
    return "hola";
  }

  private String devuelveAlgoDosParam(String a, Boolean activo) {

    if (activo == null || !activo)
      return "sal";

    String resultado = null;

    if (CINCO.equals(a)) {
      resultado = "hola";
    } else if ("6".equals(a)) {
      resultado = "adios";
    }
    return resultado;
  }

}
