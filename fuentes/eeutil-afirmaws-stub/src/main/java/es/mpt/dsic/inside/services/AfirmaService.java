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

package es.mpt.dsic.inside.services;

import es.mpt.dsic.inside.model.ConfiguracionAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionFirmaFormatoAAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirma;
import es.mpt.dsic.inside.model.ResultadoValidacionInfoAfirmaExt;
import es.mpt.dsic.inside.model.ResultadoValidarCertificadoAfirma;
import es.mpt.dsic.inside.utils.exception.EeutilException;

/**
 * Interfaz ws de afirma en eeutils.
 * 
 * @author miguel.moral
 *
 */
public interface AfirmaService {

  /**
   * Valida firma en afirma.
   * 
   * @param aplicacion
   * @param firmaElectronica
   * @param datos
   * @param hash
   * @param algoritmo
   * @param tipoFirma
   * @return resultado de la validacion.
   * @throws EeutilException
   */
  ResultadoValidacionInfoAfirma validarFirma(String aplicacion, String firmaElectronica,
      String datos, String hash, String algoritmo, String tipoFirma) throws EeutilException;

  /**
   * Validacion de firma con informacion de certificados en afirma.
   * 
   * @param aplicacion
   * @param firmaElectronica
   * @param datos
   * @param hash
   * @param algoritmo
   * @param tipoFirma
   * @param infoCertificados
   * @return resultado de la validacion.
   * @throws EeutilException
   */
  ResultadoValidacionInfoAfirmaExt validarFirmaInfo(String aplicacion, String firmaElectronica,
      String datos, String hash, String algoritmo, String tipoFirma, boolean infoCertificados)
      throws EeutilException;

  /**
   * Validacion de certificado en afirma.
   * 
   * @param aplicacion
   * @param certificado
   * @param infAmpliada
   * @return resultado de la validacion.
   * @throws EeutilException
   */
  ResultadoValidarCertificadoAfirma validarCertificado(String aplicacion, String certificado,
      Boolean infAmpliada) throws EeutilException;

  /**
   * Obtencion de informacion de la firma.
   * 
   * @param aplicacion
   * @param firma
   * @param obtenerFirmantes
   * @param obtenerDatosFirmados
   * @param obtenerTipoFirma
   * @param content
   * @return informacion de la firma.
   * @throws EeutilException
   */
  InformacionFirmaAfirma obtenerInformacionFirma(String aplicacion, byte[] firma,
      boolean obtenerFirmantes, boolean obtenerDatosFirmados, boolean obtenerTipoFirma,
      byte[] content) throws EeutilException;

  /**
   * Metodo para ampliar una firma.
   * 
   * @param aplicacion
   * @param sign
   * @param configuracion
   * @return firma ampliada.
   * @throws EeutilException
   */
  ResultadoAmpliarFirmaAfirma ampliarFirma(String aplicacion, byte[] sign,
      ConfiguracionAmpliarFirmaAfirma configuracion) throws EeutilException;

  /**
   * Metodo para la obtencion de informacion de firma.
   * 
   * @param aplicacion
   * @param firma
   * @param content
   * @return informacion de la firma.
   * @throws EeutilException
   */
  String obtenerTipoFirmaDss(String aplicacion, byte[] firma, byte[] content)
      throws EeutilException;

  /**
   * Metodo para validar una firma en formato ampliado.
   * 
   * @param aplicacion
   * @param firmaElectronica
   * @param datos
   * @param hash
   * @param algoritmo
   * @param tipoFirma
   * @return resultado de la validacion.
   * @throws EeutilException
   */
  ResultadoValidacionFirmaFormatoAAfirma validarFirmaFormatoA(String aplicacion,
      String firmaElectronica, String datos, String hash, String algoritmo, String tipoFirma)
      throws EeutilException;

}
