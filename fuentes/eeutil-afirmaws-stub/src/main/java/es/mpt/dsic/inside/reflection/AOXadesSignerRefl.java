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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.AOInvalidFormatException;
import es.gob.afirma.signers.xades.AOXAdESSigner;
import es.gob.afirma.signers.xml.Utils;
import es.gob.afirma.signers.xml.XMLConstants;

/**
 * @author miguel.moral Clase que soluciona el problema del getData para XadesManifest de miniapplet
 *         de afirma
 *
 */
public class AOXadesSignerRefl {

  /**
   * Recupera los datos originalmente firmados de una firma. En el caso de que la firma no contenga
   * los datos firmados, se devuelve <code>null</code>.
   * 
   * @param signDocument Documento XML de firma.
   * @return Datos originalmente firmados.
   * @throws es.gob.afirma.core.AOInvalidFormatException Si no se ha introducido un fichero de firma
   *         v&aacute;lido o no ha podido leerse la firma.
   */
  public static byte[] getData(final Document signDocument) throws AOInvalidFormatException {

    Element elementRes = null;

    try {

      // Comprueba que sea una documento de firma valido
      if (!AOXAdESSigner.isSign(signDocument)) {
        throw new AOInvalidFormatException("El documento no es un documento de firmas valido."); //$NON-NLS-1$
      }

      final Element signatureElement =
          XadesUtilRefl.getFirstSignatureElement(signDocument.getDocumentElement());

      // Si no se encuentran firmas, no se recuperan los datos
      if (signatureElement == null) {
        return null;
      }

      final List<Element> dataReferenceList =
          XadesUtilRefl.getSignatureDataReferenceList(signatureElement);

      try {
        // Obtiene la raiz del documento de firmas
        final Element docElement = signDocument.getDocumentElement();

        // Si la firma es externally detached o de tipo Manifest, consideramos que los
        // datos
        // son externos y no se devolveran. Esto se hace asi por seguridad, incluso si
        // se
        // pudiese acceder a los datos traves de URLs externas


        if (isSignatureElementExternallyDetached(dataReferenceList)) {
          elementRes = null;
        }

        // esto cambia con respecto a la version de miniapplet.
        else if (isSignatureWithManifest(dataReferenceList)) {

          // Node nodeXadesValue=XMLUtil.getNodeByXpathExpression(signDocument,
          // "//*/Manifest/Reference/DigestValue");

          NodeList lNodesSignatures = docElement.getChildNodes();

          for (int p = 0; p < lNodesSignatures.getLength(); p++) {

            if (!lNodesSignatures.item(p).getNodeName().equals("ds:Signature")) {
              continue;
            }

            NodeList lNodes = lNodesSignatures.item(p).getChildNodes();

            boolean buscarXadesValue = false;

            for (int i = 0; i < lNodes.getLength(); i++) {
              if (lNodes.item(i).getNodeName().equals("ds:Object")) {
                Element elemAuxNiv1 = (Element) lNodes.item(i).getFirstChild();
                if (elemAuxNiv1 != null && elemAuxNiv1.getNodeName().equals("ds:Manifest")) {
                  Element elemAuxNiv2 = (Element) elemAuxNiv1.getFirstChild();

                  if (elemAuxNiv2 != null && elemAuxNiv2.getNodeName().equals("ds:Reference")) {
                    NodeList nlElemAuxNiv3 = elemAuxNiv2.getChildNodes();

                    for (int k = 0; k < nlElemAuxNiv3.getLength(); k++) {
                      if (nlElemAuxNiv3.item(k).getNodeName().equals("ds:DigestValue")) {
                        elementRes = (Element) nlElemAuxNiv3.item(k);
                        buscarXadesValue = true;
                        break;
                      }
                    }

                    if (buscarXadesValue) {
                      break;
                    }

                  }

                }
              }
            }
          }

        }
        // Si es enveloped
        else if (isSignatureElementEnveloped(signatureElement, dataReferenceList)) {

          removeEnvelopedSignatures(docElement);


          elementRes = docElement;
        }


        // Si es internally detached
        else if (isSignatureElementInternallyDetached(docElement, dataReferenceList)) {

          final Element firstChild = (Element) docElement.getFirstChild();

          // Si el contenido firmado es un nodo de texto, lo extramos como tal
          if (firstChild.getFirstChild().getNodeType() == Node.TEXT_NODE) {
            // Si existe una transformacion Base64, la deshacemos


            return isBase64TransformationDeclared(docElement,
                firstChild.getAttribute(getFieldIDENTIFIER()))
                    ? new Base64().decode(firstChild.getTextContent())
                    : firstChild.getTextContent().getBytes();
          }
          // Si no era un nodo de texto, se considera que es XML
          elementRes = (Element) firstChild.getFirstChild();
        }

        // Si es enveloping y no es manifest (porque de estas ultimas no podemos extraer
        // los datos)
        else if (isSignatureElementEnveloping(signatureElement, dataReferenceList)) {

          // Obtiene el nodo Object de la primera firma
          final Element object =
              (Element) docElement.getElementsByTagNameNS(XMLConstants.DSIGNNS, "Object") //$NON-NLS-1$
                  .item(0);
          // Si el documento es un xml se extrae como tal
          if (object.getAttribute(getFieldXMLDSIG_ATTR_MIMETYPE_STR()).equals("text/xml")) { //$NON-NLS-1$
            elementRes = (Element) object.getFirstChild();
          }
          // Si el documento es binario se deshace la codificacion en
          // Base64 si y solo si esta declarada esta transformacion
          else {

            // Se deshace el Base64 si existe la transformacion Base64
            return isBase64TransformationDeclared(docElement,
                object.getAttribute(getFieldIDENTIFIER()))
                    ? new Base64().decode(object.getTextContent())
                    : object.getTextContent().getBytes();
          }
        }
        // No se ha podido identificar el tipo de firma
        else {
          elementRes = null;
        }
      } catch (final Exception ex) {
        throw new AOInvalidFormatException("Error al leer el fichero de firmas: " + ex, ex); //$NON-NLS-1$
      }

      // si no se ha recuperado ningun dato se devuelve null
      if (elementRes == null) {
        return null;
      }

    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new AOInvalidFormatException(e.getMessage(), e);
    }

    // convierte el documento obtenido en un array de bytes
    return Utils.writeXML(elementRes, null, null, null);

  }

  /**
   * @return
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  private static String getFieldXMLDSIG_ATTR_MIMETYPE_STR()
      throws NoSuchFieldException, IllegalAccessException {
    Field field_XMLDSIG_ATTR_MIMETYPE_STR =
        AOXAdESSigner.class.getDeclaredField("XMLDSIG_ATTR_MIMETYPE_STR");
    field_XMLDSIG_ATTR_MIMETYPE_STR.setAccessible(true);
    final String str_field_XMLDSIG_ATTR_MIMETYPE_STR =
        (String) field_XMLDSIG_ATTR_MIMETYPE_STR.get(null);
    field_XMLDSIG_ATTR_MIMETYPE_STR.setAccessible(false);
    return str_field_XMLDSIG_ATTR_MIMETYPE_STR;
  }

  /**
   * @return
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  private static String getFieldIDENTIFIER() throws NoSuchFieldException, IllegalAccessException {
    Field field_IDENTIFIER = AOXAdESSigner.class.getDeclaredField("ID_IDENTIFIER");
    field_IDENTIFIER.setAccessible(true);
    final String strField_Identifier = (String) field_IDENTIFIER.get(null);
    field_IDENTIFIER.setAccessible(false);
    return strField_Identifier;
  }

  /**
   * @param docElement
   * @param strField_Identifier
   * @param firstChild
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchFieldException
   * @throws IllegalArgumentException
   */
  private static boolean isBase64TransformationDeclared(final Element docElement,
      final String identifier)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdisBase64TransformationDeclared = AOXAdESSigner.class
        .getDeclaredMethod("isBase64TransformationDeclared", Element.class, String.class);
    methdisBase64TransformationDeclared.setAccessible(true);
    boolean isBase64TransformationDeclared =
        (boolean) methdisBase64TransformationDeclared.invoke(null, docElement, identifier);
    methdisBase64TransformationDeclared.setAccessible(false);
    return isBase64TransformationDeclared;
  }

  /**
   * @param docElement
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private static void removeEnvelopedSignatures(final Element docElement)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdremoveEnvelopedSignatures =
        AOXAdESSigner.class.getDeclaredMethod("removeEnvelopedSignatures", Element.class);
    methdremoveEnvelopedSignatures.setAccessible(true);
    methdremoveEnvelopedSignatures.invoke(null, docElement);
    methdremoveEnvelopedSignatures.setAccessible(false);
  }

  /**
   * @param signatureElement
   * @param dataReferenceList
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private static boolean isSignatureElementEnveloping(final Element signatureElement,
      final List<Element> dataReferenceList)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdisSignatureElementEnveloping = AOXAdESSigner.class
        .getDeclaredMethod("isSignatureElementEnveloping", Element.class, List.class);
    methdisSignatureElementEnveloping.setAccessible(true);
    boolean isSignatureElementEnveloping = (boolean) methdisSignatureElementEnveloping.invoke(null,
        signatureElement, dataReferenceList);
    methdisSignatureElementEnveloping.setAccessible(false);
    return isSignatureElementEnveloping;
  }

  /**
   * @param dataReferenceList
   * @param docElement
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private static boolean isSignatureElementInternallyDetached(final Element docElement,
      final List<Element> dataReferenceList)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdisSignatureElementInternallyDetached = AOXAdESSigner.class
        .getDeclaredMethod("isSignatureElementInternallyDetached", Element.class, List.class);
    methdisSignatureElementInternallyDetached.setAccessible(true);
    boolean isSignatureElementInternallyDetached =
        (boolean) methdisSignatureElementInternallyDetached.invoke(null, docElement,
            dataReferenceList);
    methdisSignatureElementInternallyDetached.setAccessible(false);
    return isSignatureElementInternallyDetached;
  }

  /**
   * @param signatureElement
   * @param dataReferenceList
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private static boolean isSignatureElementEnveloped(final Element signatureElement,
      final List<Element> dataReferenceList)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdisSignatureElementEnveloped = AOXAdESSigner.class
        .getDeclaredMethod("isSignatureElementEnveloped", Element.class, List.class);
    methdisSignatureElementEnveloped.setAccessible(true);
    boolean isSignatureElementEnveloped = (boolean) methdisSignatureElementEnveloped.invoke(null,
        signatureElement, dataReferenceList);
    methdisSignatureElementEnveloped.setAccessible(false);
    return isSignatureElementEnveloped;
  }

  /**
   * @param dataReferenceList
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private static boolean isSignatureWithManifest(final List<Element> dataReferenceList)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdisSignatureWithManifest =
        AOXAdESSigner.class.getDeclaredMethod("isSignatureWithManifest", List.class);
    methdisSignatureWithManifest.setAccessible(true);
    boolean isSignatureWithManifest =
        (boolean) methdisSignatureWithManifest.invoke(null, dataReferenceList);
    methdisSignatureWithManifest.setAccessible(false);
    return isSignatureWithManifest;
  }

  /**
   * @param dataReferenceList
   * @return
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private static boolean isSignatureElementExternallyDetached(final List<Element> dataReferenceList)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method methdisSignatureElementExternallyDetached =
        AOXAdESSigner.class.getDeclaredMethod("isSignatureElementExternallyDetached", List.class);
    methdisSignatureElementExternallyDetached.setAccessible(true);
    boolean isSignatureElementExternallyDetached =
        (boolean) methdisSignatureElementExternallyDetached.invoke(null, dataReferenceList);
    methdisSignatureElementExternallyDetached.setAccessible(false);
    return isSignatureElementExternallyDetached;
  }

  public static byte[] getData(final byte[] sign) throws AOInvalidFormatException {

    // Construimos el arbol DOM
    Document doc;
    byte[] bGetData = null;
    try (InputStream bis = new ByteArrayInputStream(sign)) {
      doc = Utils.getNewDocumentBuilder().parse(bis);
      bGetData = getData(doc);

    } catch (final Exception ex) {
      throw new AOInvalidFormatException("Error al leer el fichero de firmas: " + ex, ex); //$NON-NLS-1$
    }

    return bGetData;

  }

}
