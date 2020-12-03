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

package es.mpt.dsic.inside.converter;

import es.mpt.dsic.inside.dssprocessing.constantes.DSSTiposFirmaConstantes;

public class AfirmaValidationConverterType {
	
	public static final String XADES_A = "XADES-A";
	public static final String CADES_A = "CADES-A";

	public static String tipoFirmaAToTipoFirmaValidacion(String tipo) {
		if (DSSTiposFirmaConstantes.XADES_TYPES.contains(tipo)) {
			return XADES_A;
		}
		if (DSSTiposFirmaConstantes.CADES_TYPES.contains(tipo)) {
			return CADES_A;
		}
		return "";
	}
	
}
