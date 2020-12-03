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
@XmlType(name="resultadoComprobarFirmaFormatoA", propOrder={"esFirmaA","fechaValidez"})
public class ResultadoComprobarFirmaFormatoA {

	@XmlElement(required=true,name="esFirmaA")
	private boolean esFirmaA;
	
	@XmlElement(required=false,name="fechaValidez")
	private String fechaValidez;

	public boolean isEsFirmaA() {
		return esFirmaA;
	}

	public void setEsFirmaA(boolean esFirmaA) {
		this.esFirmaA = esFirmaA;
	}

	public String getFechaValidez() {
		return fechaValidez;
	}

	public void setFechaValidez(String fechaValidez) {
		this.fechaValidez = fechaValidez;
	}

}