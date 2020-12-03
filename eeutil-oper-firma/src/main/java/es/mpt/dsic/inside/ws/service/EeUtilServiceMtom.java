/* Copyright (C) 2012-13 MINHAP, Gobierno de España
   This program is licensed and may be used, modified and redistributed under the terms
   of the European Public License (EUPL), either version 1.1 or (at your
   option) any later version as soon as they are approved by the European Commission.
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
   or implied. See the License for the specific language governing permissions and
   more details.
   You should have received a copy of the EUPL1.1 license
   along with this program; if not, you may find it at
   http://joinup.ec.europa.eu/software/page/eupl/licence-eupl */

package es.mpt.dsic.inside.ws.service;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;

import es.mpt.dsic.inside.security.model.ApplicationLogin;
import es.mpt.dsic.inside.ws.service.exception.InSideException;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.DatosFirmadosMtom;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.OpcionesObtenerInformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirmaMtom;
import es.mpt.dsic.inside.ws.service.model.ResultadoComprobarFirmaFormatoA;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;

@WebService
public interface EeUtilServiceMtom {

	@WebMethod(operationName = "obtenerFirmantes", action = "urn:obtenerFirmantes")
	@WebResult(name = "ListaFirmantes", partName = "ListaFirmantes")
	public ListaFirmaInfo obtenerFirmantes(
			@WebParam(name = "aplicacionInfo") @XmlElement(required = true, name = "aplicacionInfo") ApplicationLogin info,
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "FirmaMtom") @XmlMimeType("application/octet-stream") DataHandler firma,
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "FirmaMtom") @XmlMimeType("application/octet-stream") DataHandler datos,
			@WebParam(name = "TipoFirma") @XmlElement(name = "TipoFirma") String tipoFirma)
			throws InSideException;

	@WebMethod(operationName = "validacionFirma", action = "urn:validacionFirma")
	@WebResult(name = "ResultadoValidacionInfo", partName = "ResultadoValidacionInfo")
	public ResultadoValidacionInfo validacionFirma(
			@WebParam(name = "aplicacionInfo") @XmlElement(required = true, name = "aplicacionInfo") ApplicationLogin info,
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "Firma") @XmlMimeType("application/octet-stream") DataHandler firma,
			@WebParam(name = "TipoFirma") @XmlElement(required = false, name = "TipoFirma") String tipoFirma,
			@WebParam(name = "DatosFirmadosMtom") @XmlElement(required = false, name = "DatosFirmadosMtom") DatosFirmadosMtom datosFirmadosMtom)
			throws InSideException;

	@WebMethod(operationName = "obtenerInformacionFirma", action = "urn:obtenerInformacionFirma")
	@WebResult(name = "resultadoObtenerInformacionFirma", partName = "resultadoObtenerInformacionFirma")
	public InformacionFirma obtenerInformacionFirma(
			@WebParam(name = "aplicacionInfo") @XmlElement(required = true, name = "aplicacionInfo") ApplicationLogin info,
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "Firma") @XmlMimeType("application/octet-stream") DataHandler firma,
			@WebParam(name = "opcionesObtenerInformacionFirma") @XmlElement(required = true, name = "opcionesObtenerInformacionFirma") OpcionesObtenerInformacionFirma opciones,
			@WebParam(name = "ContenidoMtom") @XmlElement(required = false, name = "ContenidoMtom") @XmlMimeType("application/octet-stream") DataHandler contenido)
			throws InSideException;

	@WebMethod(operationName = "ampliarFirma", action = "urn:ampliarFirma")
	@WebResult(name = "resultadoAmpliarFirmaMtom", partName = "resultadoAmpliarFirmaMtom")
	public ResultadoAmpliarFirmaMtom ampliarFirma(
			@WebParam(name = "aplicacionInfo") @XmlElement(required = true, name = "aplicacionInfo") ApplicationLogin info,
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "FirmaMtom") @XmlMimeType("application/octet-stream") DataHandler firma,
			@WebParam(name = "configuracionAmpliarFirma") @XmlElement(required = true, name = "configuracionAmpliarFirma") ConfiguracionAmpliarFirma configuracion)
			throws InSideException;

	@WebMethod(operationName = "postProcesarFirma", action = "urn:postProcesarFirma")
	@WebResult(name = "FirmaProcesada", partName = "FirmaProcesada")
	public DataHandler postProcesarFirma(
			@WebParam(name = "aplicacionInfo") @XmlElement(required = true, name = "aplicacionInfo") ApplicationLogin info,
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "FirmaMtom") @XmlMimeType("application/octet-stream") DataHandler firma)
			throws InSideException;
	
	@WebMethod(operationName = "validarCertificado", action = "urn:validarCertificado")
	@WebResult(name = "resultadoValidarCertificado", partName = "resultadoValidarCertificado")
	public ResultadoValidarCertificado  validarCertificado(@WebParam(name = "aplicacionInfo")  @XmlElement(required=true,name="aplicacionInfo") ApplicationLogin info,	
			@WebParam(name = "certificate")  @XmlElement(required=true,name="certificate") String certificate
			)  throws InSideException;
	
	@WebMethod(operationName = "getInfoCertificado", action = "urn:getInfoCertificado")
	@WebResult(name = "resultadoGetInfoCertificado", partName = "resultadoGetInfoCertificado")
	public InfoCertificado  getInfoCertificado(@WebParam(name = "aplicacionInfo")  @XmlElement(required=true,name="aplicacionInfo") ApplicationLogin info,	
			@WebParam(name = "certificate")  @XmlElement(required=true,name="certificate") String certificate
			)  throws InSideException;
	
	@WebMethod(operationName = "comprobarFirmaFormatoA", action = "urn:comprobarFirmaFormatoA")
	@WebResult(name = "resultadoComprobarFirmaFormatoA", partName = "resultadoComprobarFirmaFormatoA")
	public ResultadoComprobarFirmaFormatoA  comprobarFirmaFormatoA(@WebParam(name = "aplicacionInfo")  @XmlElement(required=true,name="aplicacionInfo") ApplicationLogin info,	
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "FirmaMtom") @XmlMimeType("application/octet-stream") DataHandler firma
			)  throws InSideException;
	
	@WebMethod(operationName = "resellarFirmaA", action = "urn:resellarFirmaA")
	public ResultadoAmpliarFirmaMtom  resellarFirmaA(@WebParam(name = "aplicacionInfo")  @XmlElement(required=true,name="aplicacionInfo") ApplicationLogin info,	
			@WebParam(name = "FirmaMtom") @XmlElement(required = true, name = "FirmaMtom") @XmlMimeType("application/octet-stream") DataHandler firma
			)  throws InSideException;

}
