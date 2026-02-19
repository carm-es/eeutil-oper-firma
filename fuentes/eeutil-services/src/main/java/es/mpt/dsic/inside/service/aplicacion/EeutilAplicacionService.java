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

package es.mpt.dsic.inside.service.aplicacion;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.mpt.dsic.inside.model.EeutilAplicacion;
import es.mpt.dsic.inside.model.aplicacion.AplicacionObject;
import es.mpt.dsic.inside.utils.exception.EeutilException;

public interface EeutilAplicacionService {

  List<AplicacionObject> getAplicaciones(String app) throws EeutilException;

  AplicacionObject desactivar(String app, AplicacionObject aplicacion) throws EeutilException;

  AplicacionObject activar(String app, AplicacionObject aplicacion) throws EeutilException;

  void eliminarAplicacion(String app, AplicacionObject aplicacion, Locale locale)
      throws EeutilException;

  AplicacionObject altaAplicacion(String app, AplicacionObject aplicacion) throws EeutilException;

  Map<String, String> getInfAdicional(String app) throws EeutilException;

  public EeutilAplicacion getAplicacionEEUTIL(String idAplicacionEEUTIL);
}
