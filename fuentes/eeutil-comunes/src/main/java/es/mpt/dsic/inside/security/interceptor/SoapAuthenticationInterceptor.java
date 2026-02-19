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

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package es.mpt.dsic.inside.security.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class SoapAuthenticationInterceptor extends AbstractPhaseInterceptor<SoapMessage>
    implements InitializingBean {

  private AuthenticationManager authenticationManager;
  private boolean authenticationRequired = true;

  public SoapAuthenticationInterceptor() {
    super(Phase.RECEIVE);
  }

  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  public void setAuthenticationRequired(boolean authenticationRequired) {
    this.authenticationRequired = authenticationRequired;
  }

  public void afterPropertiesSet() throws Exception {
    if (authenticationManager == null) {
      throw new IllegalStateException("No authentication manager has been configured");
    }
  }

  public void handleMessage(SoapMessage message) throws Fault {
    try {
      Authentication authentication = message.getExchange().get(Authentication.class);
      if (authentication != null) {
        try {
          authentication = authenticationManager.authenticate(authentication);
          message.getExchange().put(Authentication.class, authentication);
        } catch (AuthenticationException ex) {
          throw new SoapFault("Bad credentials", message.getVersion().getSender());
        }
      } else if (authenticationRequired) {
        throw new SoapFault("Authentication required", message.getVersion().getSender());
      }
    } finally {
      String[] aInfoClient = getInfoClient(message);
      MDC.put("ipClient", aInfoClient[0]);
      MDC.put("clientHost", aInfoClient[1]);
      MDC.put("clientURI", aInfoClient[2]);
      MDC.put("contentLengh", aInfoClient[3]);
    }
  }


  private String[] getInfoClient(Message message) {
    String[] aInfoClient = new String[4];
    String clientIP = null;
    String hostClient = null;
    String uriClient = null;
    String contentLength = null;

    try {
      HttpServletRequest request =
          (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
      // mostrarTodasCabeceras(request);
      clientIP = getClientIpAddr(request);
      hostClient = request.getRemoteHost();
      uriClient = obtenerTipoEndpointURI(request.getRequestURI());
      contentLength = mostrarContentLength(request);

    } catch (Exception e) {
      clientIP = "UKNOWN";
      hostClient = "UKNOWN";
      uriClient = "UKNOWN";
    }

    aInfoClient[0] = clientIP;
    aInfoClient[1] = hostClient;
    aInfoClient[2] = uriClient;
    aInfoClient[3] = contentLength;


    return aInfoClient;
  }


  private String getClientIpAddr(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if (ip == null) {
      ip = "unknown";
    }
    return ip;
  }


  private String obtenerTipoEndpointURI(String uri) {
    StringBuilder str = new StringBuilder("");
    if (uri != null && uri.indexOf('/') != -1) {
      str.append(uri.substring(uri.lastIndexOf('/'), uri.length()));
      return str.toString();
    }

    else
      return uri;
  }

  private String mostrarContentLength(HttpServletRequest request) {
    String tamanoPeticion = request.getHeader("content-length");
    if (tamanoPeticion == null || tamanoPeticion.length() == 0
        || "unknown".equalsIgnoreCase(tamanoPeticion)) {
      tamanoPeticion = "unknown";
    }
    return tamanoPeticion;
  }

  private void mostrarTodasCabeceras(HttpServletRequest request) {
    Enumeration<String> aHeaders = request.getHeaderNames();

    while (aHeaders.hasMoreElements()) {
      System.out.println("CABECERAS");
      String header = aHeaders.nextElement();
      System.out.println("CABECERA " + header + "VALOR " + request.getHeader(header));
    }
  }

}
