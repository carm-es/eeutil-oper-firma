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

package es.mpt.dsic.inside.pdf.converter.utilsigae;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.stereotype.Service;

import es.mpt.dsic.inside.aop.AuditExternalServiceAnnotation;
import es.mpt.dsic.inside.utils.exception.EeutilException;

@Service
public class RestIgaeServiceA {

  private org.apache.http.ssl.SSLContextBuilder sslContextBuilder;
  private SSLContext sslContext;
  private org.apache.http.conn.ssl.SSLConnectionSocketFactory sslSocketFactory;
  private HttpClientBuilder httpClientBuilder;

  @AuditExternalServiceAnnotation(nombreModulo = "eeutil-pdf-conversion-igae")
  public CloseableHttpResponse getResponseClientIgae(CloseableHttpClient httpClient, String url,
      byte[] bContenido, String tokenNameIgae, String tokenValueIgae) throws EeutilException,
      IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    try {

      if (sslContextBuilder == null) {
        {
          sslContextBuilder = SSLContextBuilder.create();
          sslContextBuilder
              .loadTrustMaterial(new org.apache.http.conn.ssl.TrustSelfSignedStrategy());
        }
        if (sslContext == null) {
          sslContext = sslContextBuilder.build();
        }
        if (sslSocketFactory == null) {
          sslSocketFactory =
              new SSLConnectionSocketFactory(sslContext, new String[] {"TLSv1.1", "TLSv1.2"}, null,
                  new org.apache.http.conn.ssl.DefaultHostnameVerifier());
        }
        if (httpClientBuilder == null) {
          httpClientBuilder = HttpClients.custom().setSSLSocketFactory(sslSocketFactory);
        }

      }
      httpClient = httpClientBuilder.build();

      // httpClient = HttpClients.createDefault();

      HttpPost httpPost = new HttpPost(url);

      // StringEntity entity = new StringEntity(json,StandardCharsets.UTF_8);
      String json = Base64.encodeBase64String(bContenido);
      StringEntity entity = new StringEntity(json);
      // chunked a false
      entity.setChunked(false);
      httpPost.setEntity(entity);

      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");
      httpPost.addHeader(tokenNameIgae, tokenValueIgae);

      return httpClient.execute(httpPost);

    } finally {
    }

  }

  public void cerrarRequestResponse(CloseableHttpClient httpClient, CloseableHttpResponse response)
      throws IOException {
    if (response != null) {
      response.close();
    }
    if (httpClient != null) {
      httpClient.close();
    }
  }

}
