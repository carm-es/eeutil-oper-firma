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

package es.mpt.dsic.inside.ws.service.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InformacionFirma", propOrder = { "esFirma","tipoDeFirma","documentoFirmado",
		"hashFirmado","algoritmoHashFirmado","firmantes"})
public class InformacionFirma {

	@XmlElement(required = true, name = "esFirma")
	private boolean esFirma;
	@XmlElement(required = false, name = "tipoDeFirma")
	private TipoDeFirma tipoDeFirma;
	@XmlElement(required = false, name = "documentoFirmado")
	private ContenidoInfo documentoFirmado;
	@XmlElement(required = false, name = "hashFirmado")
	private String hashFirmado;
	@XmlElement(required = false, name = "algoritmoHashFirmado")
	private String algoritmoHashFirmado;
	@XmlElement(required = false, name = "firmantes")
	private ListaFirmaInfo firmantes;
	
	
	public boolean isEsFirma() {
		return esFirma;
	}
	public void setEsFirma(boolean esFirma) {
		this.esFirma = esFirma;
	}
	public TipoDeFirma getTipoDeFirma() {
		return tipoDeFirma;
	}
	public void setTipoDeFirma(TipoDeFirma tipoDeFirma) {
		this.tipoDeFirma = tipoDeFirma;
	}	

	public ContenidoInfo getDocumentoFirmado() {
		return documentoFirmado;
	}
	public void setDocumentoFirmado(ContenidoInfo documentoFirmado) {
		this.documentoFirmado = documentoFirmado;
	}
	public String getHashFirmado() {
		return hashFirmado;
	}
	public void setHashFirmado(String hashFirmado) {
		this.hashFirmado = hashFirmado;
	}
	public String getAlgoritmoHashFirmado() {
		return algoritmoHashFirmado;
	}
	public void setAlgoritmoHashFirmado(String algoritmoHashFirmado) {
		this.algoritmoHashFirmado = algoritmoHashFirmado;
	}
	public ListaFirmaInfo getFirmantes() {
		return firmantes;
	}
	public void setFirmantes(ListaFirmaInfo firmantes) {
		this.firmantes = firmantes;
	}
	
	
	
}
