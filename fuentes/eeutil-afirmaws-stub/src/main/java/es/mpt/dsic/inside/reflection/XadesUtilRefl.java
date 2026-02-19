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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.w3c.dom.Element;

import es.gob.afirma.signers.xades.XAdESUtil;

public class XadesUtilRefl {


  /**
   * @param signDocument
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  static Element getFirstSignatureElement(final Element documentElement)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdgetFirstSignatureElement =
        XAdESUtil.class.getDeclaredMethod("getFirstSignatureElement", Element.class);
    methdgetFirstSignatureElement.setAccessible(true);
    final Element signatureElement =
        (Element) methdgetFirstSignatureElement.invoke(null, documentElement);
    methdgetFirstSignatureElement.setAccessible(false);
    return signatureElement;
  }


  /**
   * @param signatureElement
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  static List<Element> getSignatureDataReferenceList(final Element signatureElement)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdgetSignatureDataReferenceList =
        XAdESUtil.class.getDeclaredMethod("getSignatureDataReferenceList", Element.class);
    methdgetSignatureDataReferenceList.setAccessible(true);
    final List<Element> dataReferenceList =
        (List<Element>) methdgetSignatureDataReferenceList.invoke(null, signatureElement);
    methdgetSignatureDataReferenceList.setAccessible(false);
    return dataReferenceList;
  }

}
