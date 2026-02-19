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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import es.mpt.dsic.inside.exception.interfaz.IMDCAble;

public class UtilReflection {

  static UtilReflection laInstancia = null;


  public static UtilReflection getInstance() {
    if (laInstancia == null) {
      laInstancia = new UtilReflection();
    }

    return laInstancia;
  }

  public Map<String, String> extractDataPermitted(Object obj, Map<String, String> result)
      throws IOException {
    if (result == null) {
      result = new HashMap<String, String>();
    }
    if (obj == null) {
      return result;
    }

    boolean accesible = false;
    for (Field field : obj.getClass().getDeclaredFields()) {
      int mod = field.getModifiers();

      if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod)) {

        try {

          if (Modifier.isPrivate(mod)) {
            field.setAccessible(true);
            accesible = true;
          }

          try {
            Object objAux = field.get(obj);

            if (objAux == null) {
              return result;
            }

            if (objAux instanceof String) {

              if (((String) objAux).length() < 1000) {
                result.put(obj.getClass().getSimpleName() + "." + field.getName(), (String) objAux);
              } else {
                result.put(obj.getClass().getSimpleName() + "." + field.getName() + "_tamano",
                    "" + ((String) objAux).length() + " Kb.");
              }

            } else if (objAux instanceof byte[]) {

              byte[] b = (byte[]) objAux;
              String tamano = "" + b.length / 1000 + " Kb.";

              if (b.length < 1000) {
                tamano = "" + b.length + " bytes.";
              }

              result.put(obj.getClass().getSimpleName() + "." + field.getName() + "_tamano",
                  tamano);
            } else if (objAux instanceof DataHandler) {
              DataHandler dataH = (DataHandler) objAux;
              result.put(obj.getClass().getSimpleName() + "." + field.getName() + "_tamanoHandler",
                  "No se puede obtener la info");
            } else if (objAux instanceof Boolean || objAux instanceof Integer
                || objAux instanceof Double || objAux instanceof Float || objAux instanceof Long
                || field.getType().equals(boolean.class) || field.getType().equals(String.class)
                || field.getType().equals(int.class) || field.getType().equals(double.class)
                || field.getType().equals(float.class) || field.getType().equals(long.class)) {
              result.put(obj.getClass().getSimpleName() + "." + field.getName(), objAux.toString());
            } else if (objAux instanceof IMDCAble) {
              UtilReflection.getInstance().extractDataPermitted(objAux, result);
            } else {
              System.err.println("Campo no evaluado. " + field.getName() + " " + field.getType());
            }

          } catch (IllegalArgumentException | IllegalAccessException e) {
          }


        } finally {
          if (accesible) {
            field.setAccessible(false);
            accesible = false;
          }
        }

      }
    }
    return result;

  }



  public Map<String, String> extractMultipleDataPermitted(Map<String, String> result,
      Object[] objects, String[] nameParams) throws IOException {
    int contador = 0;
    for (Object obj : objects) {
      if (obj instanceof IMDCAble) {
        result = UtilReflection.getInstance().extractDataPermitted(obj, result);
      } else {
        result = UtilReflection.getInstance().extractSingleDataPermitted(obj, nameParams[contador],
            result);
      }
      contador++;
    }

    return result;
  }


  private Map<String, String> extractSingleDataPermitted(Object obj, String nameParam,
      Map<String, String> result) throws IOException {
    if (result == null) {
      result = new HashMap<String, String>();
    }
    if (obj == null) {
      return result;
    }


    try {

      if (obj instanceof String) {

        if (((String) obj).length() < 1000) {
          result.put(nameParam, (String) obj);
        } else {
          result.put(nameParam + "_tamano", "" + ((String) obj).length() + " Kb.");
        }
      } else if (obj instanceof byte[]) {

        byte[] b = (byte[]) obj;
        String tamano = "" + b.length / 1000 + " Kb.";

        if (b.length < 1000) {
          tamano = "" + b.length + " bytes.";
        }

        result.put(obj.getClass().getSimpleName() + "." + nameParam + "_tamano", tamano);
      } else if (obj instanceof DataHandler) {
        DataHandler dataH = (DataHandler) obj;
        result.put(obj.getClass().getSimpleName() + "." + nameParam + "_tamanoHandler",
            "No se puede obtener la info");
      } else if (obj instanceof Boolean || obj instanceof Integer || obj instanceof Double
          || obj instanceof Float || obj instanceof Long) {
        result.put(obj.getClass().getSimpleName() + "." + nameParam, obj.toString());
      } else {
        System.err.println("Campo no evaluado. " + nameParam + " " + obj.getClass().getName());
      }

    } catch (IllegalArgumentException e) {

    }
    return result;

  }


  /*
   * public String getTamanoDataHandler(DataHandler dataHandler) throws IOException { InputStream is
   * = null; long tamano = 0; if (dataHandler != null) { try { is =
   * dataHandler.getDataSource().getInputStream(); tamano = IOUtils.toByteArray(is).length; } catch
   * (IOException e) { tamano = -1; // recover by assuming length-0 data } finally { if(is!=null) {
   * is.close(); }
   * 
   * } }
   * 
   * if(tamano<1000) { return ""+tamano+" bytes."; } else { return ""+tamano/1000+" Kb."; } }
   */

}
