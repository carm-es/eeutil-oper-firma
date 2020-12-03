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

package es.mpt.dsic.inside.ws.service.postprocess;

//import es.gob.afirma.signers.AOSigner;
//import es.gob.afirma.signers.AOXAdESSigner;

//import es.gob.afirma.core.signers.AOSigner;
//import es.gob.afirma.signers.xades.AOXAdESSigner;

import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.signers.xades.AOXAdESSigner;
import es.mpt.dsic.inside.ws.service.postprocess.impl.XAdESPostProcessor;

public class PostProcessFactory {
	
	private static PostProcessFactory instance = null;
	
	private PostProcessFactory() {
		
	}
		
	public static PostProcessFactory getInstance() {
		if (instance == null) {
			instance = new PostProcessFactory();
		}
		return instance;
	}
	
	public PostProcessor getPostProcessor(AOSigner signer) {
		PostProcessor processor = null;
		if (signer instanceof AOXAdESSigner) {
			return new XAdESPostProcessor();
		}
		return processor;
	}
	
	

}
