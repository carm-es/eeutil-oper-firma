/* Copyright (C) 2012-13 MINHAP, Gobierno de EspaÃ±a
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

package es.mpt.dsic.inside.ws.service.impl;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.gob.afirma.core.signers.AOSignerFactory;
import es.mpt.dsic.inside.converter.AfirmaModelToWsModelConverter;
import es.mpt.dsic.inside.converter.AfirmaValidationConverterType;
import es.mpt.dsic.inside.exception.AfirmaException;
import es.mpt.dsic.inside.model.InformacionFirmaAfirma;
import es.mpt.dsic.inside.model.ResultadoAmpliarFirmaAfirma;
import es.mpt.dsic.inside.security.context.AplicacionContext;
import es.mpt.dsic.inside.security.model.ApplicationLogin;
import es.mpt.dsic.inside.services.AfirmaService;
import es.mpt.dsic.inside.utils.xml.XMLUtil;
import es.mpt.dsic.inside.ws.service.EeUtilService;
import es.mpt.dsic.inside.ws.service.exception.InSideException;
import es.mpt.dsic.inside.ws.service.model.ConfiguracionAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.DatosFirmados;
import es.mpt.dsic.inside.ws.service.model.EstadoInfo;
import es.mpt.dsic.inside.ws.service.model.InfoCertificado;
import es.mpt.dsic.inside.ws.service.model.InformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ListaFirmaInfo;
import es.mpt.dsic.inside.ws.service.model.OpcionesObtenerInformacionFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoAmpliarFirma;
import es.mpt.dsic.inside.ws.service.model.ResultadoComprobarFirmaFormatoA;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionFormatoAInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidacionInfo;
import es.mpt.dsic.inside.ws.service.model.ResultadoValidarCertificado;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessException;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessFactory;
import es.mpt.dsic.inside.ws.service.postprocess.PostProcessor;

@Service("eeUtilService")
@WebService(endpointInterface = "es.mpt.dsic.inside.ws.service.EeUtilService")
@SOAPBinding(style = Style.RPC, parameterStyle = ParameterStyle.BARE, use = Use.LITERAL)
public class EeUtilServiceImpl implements EeUtilService {

	protected final static Log logger = LogFactory
			.getLog(EeUtilServiceImpl.class);

	@Autowired(required = false)
	private AplicacionContext aplicacionContext;

	@Autowired
	private AfirmaService afirmaService;

	public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	@Override
	public ResultadoValidacionInfo validacionFirma(ApplicationLogin info,
			byte[] firma, String tipoFirma, DatosFirmados datosFirmados)
			throws InSideException {

		if (firma == null) {
			EstadoInfo estadoInfo = new EstadoInfo();
			throw new InSideException("La firma no puede ser nula", estadoInfo,
					new IllegalArgumentException());
		}

		logger.debug("Tipo de firma: " + tipoFirma);
		String firmaElectronica64 = Base64.encodeBase64String(firma);

		String datos64 = null;
		String hash64 = null;
		String algoritmo = null;

		if (datosFirmados != null && datosFirmados.getDocumento() != null) {
			datos64 = Base64.encodeBase64String(datosFirmados.getDocumento());
		}

		if (datosFirmados != null && datosFirmados.getHash() != null) {
			hash64 = Base64.encodeBase64String(datosFirmados.getHash());
		}

		if (datosFirmados != null) {
			algoritmo = datosFirmados.getAlgoritmo();
		}

		ResultadoValidacionInfo validaFirma = AfirmaModelToWsModelConverter
				.resultadoValidacionInfoAfirmaToResultadoValidacionInfo(afirmaService
						.validarFirma(info.getIdApplicacion(),
								firmaElectronica64, datos64, hash64, algoritmo,
								tipoFirma));

		return validaFirma;

	}

	/**
	 * Obtiene la información de una firma, a partir de una llamada a los WS DSS
	 * de Afirma
	 * 
	 * @param info
	 *            informacion de la aplicación
	 * @param firma
	 *            bytes de la firma
	 * @return
	 * @throws InSideException
	 */
	@Override
	public InformacionFirma obtenerInformacionFirma(
			ApplicationLogin aplicacion, byte[] firma,
			OpcionesObtenerInformacionFirma opciones, byte[] content)
			throws InSideException {
		InformacionFirma info = null;
		try {
			InformacionFirmaAfirma infoAfirma = afirmaService
					.obtenerInformacionFirma(aplicacion.getIdApplicacion(),
							firma, opciones.isObtenerFirmantes(),
							opciones.isObtenerDatosFirmados(),
							opciones.isObtenerTipoFirma(), content);
			info = AfirmaModelToWsModelConverter
					.informacionFirmaAfirmaToInformacionFirma(infoAfirma);
		} catch (AfirmaException e) {
			logger.error("Error en obtenerInformacionFirma", e);
			EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(),
					e.getMessage());
			throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
		} catch (Throwable t) {
			logger.error("Error en obtenerInformacionFirma", t);
			EstadoInfo estadoInfo = new EstadoInfo("ERROR",
					"ERROR INESPERADO AL OBTENER INFORMACION DE FIRMA", null);
			throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
		}

		return info;

	}

	/**
	 * Obtiene el objeto InfoCertificadoInfo que corresponde al certificado
	 * pasado por parámetro
	 * 
	 * @param cert
	 *            Certificado del que se quiere buscar la información en la
	 *            lista.
	 * @param lista
	 *            Lista de información de certificados en la que queremos buscar
	 * @return El objeto InfoCertificadoInfo que se corresponda con el
	 *         certificado pasado como parámetro. Se considera que se
	 *         corresponden cuando coincide el número de serie.
	 */
	/*
	 * private InfoCertificadoInfo getCertificado (X509Certificate cert,
	 * List<InfoCertificadoInfo> lista) {
	 * logger.debug("Certificado: Número de serie " +
	 * cert.getSerialNumber().toString());
	 * logger.debug("Certificado: Issuer Principal, Campo O= " +
	 * MiscUtil.getOrganizationIssuerX500Principal
	 * (cert.getIssuerX500Principal().getName()));
	 * logger.debug("Certificado: Issuer Principal: " +
	 * cert.getIssuerX500Principal().getName());
	 * 
	 * InfoCertificadoInfo infoCerti = null; boolean encontrado = false; int
	 * i=0; while (i < lista.size() && !encontrado) { InfoCertificadoInfo info =
	 * lista.get(i); List<Campo> campos = info.getCampo();
	 * 
	 * boolean encontradoNumSerie = false; boolean encontradoIdEmisor = false;
	 * 
	 * String numSerie = ""; String idEmisor = ""; int j = 0;
	 * 
	 * while (j < campos.size() && (!encontradoNumSerie || !encontradoIdEmisor))
	 * { logger.debug("IdCampo " + campos.get(j).getIdCampo());
	 * logger.debug("ValorCampo " + campos.get(j).getValorCampo()); if
	 * (campos.get(j).getIdCampo().contentEquals("numeroSerie")) {
	 * encontradoNumSerie = true; numSerie = campos.get(j).getValorCampo(); }
	 * else if (campos.get(j).getIdCampo().contentEquals("idEmisor")) {
	 * encontradoIdEmisor = true; idEmisor = campos.get(j).getValorCampo(); }
	 * j++; }
	 * 
	 * if (numSerie.contentEquals(cert.getSerialNumber().toString())) {
	 * logger.debug("Encontrado número de serie coincidente"); if
	 * (idEmisor.contentEquals(cert.getIssuerX500Principal().getName()) ||
	 * idEmisor.contentEquals(MiscUtil.getOrganizationIssuerX500Principal(cert.
	 * getIssuerX500Principal().getName()))) {
	 * logger.debug("Encontrado emisor coincidente"); } else {
	 * logger.info("NO se ha encontrado emisor coincidente"); } infoCerti =
	 * info; }
	 * 
	 * i++; }
	 * 
	 * return infoCerti;
	 * 
	 * }
	 */

	@Override
	public ListaFirmaInfo obtenerFirmantes(ApplicationLogin info, byte[] firma,
			byte[] datos, String tipoFirma) throws InSideException {
		OpcionesObtenerInformacionFirma opciones = new OpcionesObtenerInformacionFirma();
		opciones.setObtenerDatosFirmados(false);
		opciones.setObtenerFirmantes(true);
		opciones.setObtenerTipoFirma(false);

		InformacionFirma informacionFirma = this.obtenerInformacionFirma(info,
				firma, opciones, null);

		return informacionFirma.getFirmantes();
	}

	/**
	 * Obtiene el resultado de la validaci?n de un certificado
	 */
	@Override
	public ResultadoValidarCertificado validarCertificado(
			ApplicationLogin info, String certificate) throws InSideException {
		return AfirmaModelToWsModelConverter
				.resultadoValidarCertificadoAfirmaToResultadoValidarCertificado(afirmaService
						.validarCertificado(info.getIdApplicacion(),
								certificate, false));
	}

	/**
	 * Obtiene el resultado de la validaci?n de un certificado
	 */
	@Override
	public InfoCertificado getInfoCertificado(ApplicationLogin info,
			String certificate) throws InSideException {
		return AfirmaModelToWsModelConverter
				.resultadoValidarCertificadoAfirmaToResultadoValidarCertificadoAmpliado(afirmaService
						.validarCertificado(info.getIdApplicacion(),
								certificate, true));
	}

	/**
	 * Recibe una firma y una configuraci?n de ampliaci?n y devuelve la firma
	 * con el upgrade apropiado
	 */
	@Override
	public ResultadoAmpliarFirma ampliarFirma(ApplicationLogin info,
			byte[] firma, ConfiguracionAmpliarFirma configuracion)
			throws InSideException {

		ResultadoAmpliarFirma resultadoAmpliarFirma;

		try {

			ResultadoAmpliarFirmaAfirma resAfirma = afirmaService
					.ampliarFirma(
							info.getIdApplicacion(),
							firma,
							AfirmaModelToWsModelConverter
									.configuracionAmpliarFirmaToConfiguracionAmpliarFirmaAfirma(configuracion));
			resultadoAmpliarFirma = AfirmaModelToWsModelConverter
					.resultadoAmpliarFirmaAfirmaToResultadoAmpliarFirma(resAfirma);
		} catch (AfirmaException e) {
			logger.error("Error en ampliarFirma", e);
			EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(),
					e.getMessage());
			throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
		} catch (Throwable t) {
			logger.error("Error en ampliarFirma", t);
			EstadoInfo estadoInfo = new EstadoInfo("ERROR",
					"ERROR INESPERADO AL INTENTAR AMPLIAR LA FIRMA", null);
			throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
		}

		return resultadoAmpliarFirma;
	}

	@Override
	public byte[] postProcesarFirma(ApplicationLogin info, byte[] firma)
			throws InSideException {
		byte[] firmaPost = null;
		try {
			PostProcessFactory factory = PostProcessFactory.getInstance();
			PostProcessor proc = factory.getPostProcessor(AOSignerFactory
					.getSigner(firma));

			if (proc == null) {
				EstadoInfo estadoInfo = new EstadoInfo();
				throw new InSideException(
						"No existe postProcessador para este tipo de firma",
						estadoInfo);
			}

			firmaPost = proc.postProcessSign(firma);
		} catch (IOException ioe) {
			logger.error("Error postprocesando firma", ioe);
			EstadoInfo estadoInfo = new EstadoInfo();
			throw new InSideException("No se puede leer la firma: "
					+ ioe.getMessage(), estadoInfo);
		} catch (PostProcessException pe) {
			logger.error("Error postprocesando firma", pe);
			EstadoInfo estadoInfo = new EstadoInfo();
			throw new InSideException("No se puede procesar la firma: "
					+ pe.getMessage(), estadoInfo);
		} catch (Exception e) {
			logger.error("Error postprocesando firma", e);
			EstadoInfo estadoInfo = new EstadoInfo();
			throw new InSideException("Error inesperado al procesar la firma: "
					+ e.getMessage(), estadoInfo);
		}

		return firmaPost;
	}

	@Override
	public ResultadoComprobarFirmaFormatoA comprobarFirmaFormatoA(
			ApplicationLogin info, byte[] firma) throws InSideException {
		try {
			ResultadoComprobarFirmaFormatoA retorno = new ResultadoComprobarFirmaFormatoA();

			// obtenemos el tipo de firma DSS
			String tipoFirmaDss = afirmaService.obtenerTipoFirmaDss(
					info.getIdApplicacion(), firma, null);

			// realizamos la conversion para validar la firma al formato
			// correspondiente
			String formatoValidacion = AfirmaValidationConverterType
					.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

			// comprobamos si la firma viene en base64
			byte[] firmaValidar = Base64.isBase64(firma) ? firma : Base64
					.encodeBase64(firma);

			// compobamos si se tiene q hacer la ampliacion de formato
			ResultadoValidacionFormatoAInfo resultadoValidar = AfirmaModelToWsModelConverter
					.resultadoValidacionFirmaFormatoAAfirmaToResultadoValidacionFormatoAInfo(afirmaService
							.validarFirmaFormatoA(info.getIdApplicacion(),
									XMLUtil.decodeUTF8(firmaValidar), null,
									null, null, formatoValidacion));
			if (resultadoValidar.isEstado()) {
				retorno.setEsFirmaA(true);
				retorno.setFechaValidez(resultadoValidar
						.getFechaValidezCertificadoTSA());
			} else {
				retorno.setEsFirmaA(false);
			}
			return retorno;
		} catch (AfirmaException e) {
			logger.error("Error en comprobarFirmaFormatoA", e);
			EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(),
					e.getMessage());
			throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
		}
	}

	@Override
	public ResultadoAmpliarFirma resellarFirmaA(ApplicationLogin info,
			byte[] firma) throws InSideException {
		try {
			logger.debug("Inicio resellarFirmaA");

			// obtenemos el tipo de firma DSS
			String tipoFirmaDss = afirmaService.obtenerTipoFirmaDss(
					info.getIdApplicacion(), firma, null);

			// realizamos la conversion para validar la firma al formato
			// correspondiente
			String formatoAmpliar = AfirmaValidationConverterType
					.tipoFirmaAToTipoFirmaValidacion(tipoFirmaDss);

			ConfiguracionAmpliarFirma confAmpliar = new ConfiguracionAmpliarFirma();
			confAmpliar.setCertificadosFirmantes(null);
			confAmpliar.setFormatoAmpliacion(formatoAmpliar);
			confAmpliar.setIgnorarPeriodoDeGracia(true);
			return this.ampliarFirma(info, firma, confAmpliar);
		} catch (AfirmaException e) {
			logger.error("Error en resellarFirmaA", e);
			EstadoInfo estadoInfo = new EstadoInfo("ERROR", e.getCode(),
					e.getMessage());
			throw new InSideException(estadoInfo.getDescripcion(), estadoInfo);
		}
	}

}
