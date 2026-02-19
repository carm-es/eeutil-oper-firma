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

package es.mpt.dsic.inside.schredule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.security.auth.callback.PasswordCallback;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.mpt.dsic.eeutil.misc.web.controller.model.InfoCertModel;
import es.mpt.dsic.inside.dao.EeutilDao;
import es.mpt.dsic.inside.schredule.configuration.ConfigurationShedlockOther;
import es.mpt.dsic.inside.utils.exception.EeutilException;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;



/**
 * Clase que validara si del stack de certificados que tenemos instalados en bbdd alguno de ellos
 * esta a punto de caducar. Se enviara una notificacion via mail para avisar de dicho concepto.
 * 
 * @author mamoralf
 *
 */

@Component
public class CertificadoValidadorTask {


  private static final String LINE_SEPARATOR = "line.separator";

  public final static String EEUTIL_FIRMA = "EEUTIL-FIRMA";

  public final static String EEUTIL_UTIL_FIRMA = "EEUTIL-UTIL-FIRMA";

  public final static String EEUTIL_OPER_FIRMA = "EEUTIL-OPER-FIRMA";

  public final static String EEUTIL_VIS_DOCEXP = "EEUTIL-VIS-DOCEXP";

  public final static String EEUTIL_MISC = "EEUTIL-MISC";


  @Value("${eeutil.mail.active}")
  private String active;
  @Value("${eeutil.mail.entorno}")
  private String entorno;
  @Value("${eeutil.mail.smtp.host}")
  private String host;
  @Value("${eeutil.mail.smtp.port}")
  private String port;
  @Value("${eeutil.mail.smtp.auth}")
  private String auth;
  @Value("${eeutil.mail.smtp.starttls.enable}")
  private String starttls;
  @Value("${eeutil.mail.smtp.ssl.trust}")
  private String trust;
  @Value("${eeutil.mail.username}")
  private String username;
  @Value("${eeutil.mail.pwd}")
  private String pwd;
  @Value("${eeutil.mail.from}")
  private String from;
  @Value("${eeutil.mail.to}")
  private String to;



  @Autowired
  private EeutilDao eeutilDao;

  @Autowired
  public ConfigurationShedlockOther configurationShedlockOther;

  // @Scheduled(cron = "*/5 * * * * ?")
  // public void holaMundo() throws Exception
  // {
  // System.out.println("Hola mundo");
  // }



  protected static final Log logger = LogFactory.getLog(CertificadoValidadorTask.class);

  // every 5 minutes.
  // @Scheduled(cron = "0 */5 * * * ?")
  // at 01:16
  @Scheduled(cron = "0 16 1 * * ?")
  // @Scheduled(cron = "${cron.certificados.expression}")
  public void tarea() {

    // este codigo solo lo debe ejecutar firma aunque este instalado en todos los modulos
    // solo deberia acceder al shedlock en un firma (para tener disponibles la ruta de los
    // certificados).
    if (!EEUTIL_FIRMA.equals(getAplicacionGestionada())) {
      // salimos
      return;
    }

    logger.info("Ejecutamos HEALTHY CERTIFICADOS");

    // Configuracion de shedlock//
    // To assert that the lock is held (prevents misconfiguration errors)
    LockingTaskExecutor executor = new DefaultLockingTaskExecutor(
        configurationShedlockOther.lockProvider(configurationShedlockOther.dataSource()));
    Runnable command = new MyCertificadoValidador(this);

    // bloqueo durante 23 horas.
    Instant lockAtMostUntil = Instant.now().plusSeconds(Long.parseLong("82800"));
    Instant lockAtLeastUntil = lockAtMostUntil;
    LockConfiguration lockConfiguration =
        new LockConfiguration("lockValidadorCertificado", lockAtMostUntil, lockAtLeastUntil);
    executor.executeWithLock(command, lockConfiguration);
    // Fin de configuracion de shedlock
  }


  public void healthyCertificados() throws Exception {

    Map<String, InfoCertModel> mapResultados = healthyCertificadosBusiness();
    if (Boolean.parseBoolean(active)) {

      String bodyMessage = prepararMensajeCorreo(mapResultados, entorno);


      // si hay errores enviamos un correo.
      if (bodyMessage != null) {
        logger.error("HAY ERRORES O AVISOS SOBRE LOS CERTIFICADOS");
        enviarCorreo(mapResultados, entorno, bodyMessage.toString());
      } else {
        logger.info("NO HAY ERRORES NI AVISOS SOBRE LOS CERTIFICADOS");
      }
    }
    // return mapResultados;
  }


  public Map<String, InfoCertModel> healthyCertificadosBusiness() throws Exception {
    EntityManager em = null;
    Map<String, InfoCertModel> mapResultados = null;

    try {

      em = eeutilDao.getEntityManager();

      Query q = em.createNativeQuery(
          "select tabla1.rutaKS, tabla1.idaplicacion,tabla2.aliasCertificado,tabla3.passwordKS, tabla4.tipoKS from ("
              + " (select iap1.valor as rutaKS, iap1.idaplicacion  from inside_aplicaciones_propiedad iap1 inner join inside_aplicaciones ia1 on iap1.idaplicacion = ia1.idaplicacion and iap1.propiedad ='rutaKS')tabla1"
              + " inner join (select iap2.valor as aliasCertificado, iap2.idaplicacion  from inside_aplicaciones_propiedad iap2 inner join inside_aplicaciones ia2 on iap2.idaplicacion = ia2.idaplicacion and iap2.propiedad ='aliasCertificado')tabla2"
              + " on tabla1.idaplicacion = tabla2.idaplicacion"
              + " inner join (select iap3.valor as passwordKS, iap3.idaplicacion  from inside_aplicaciones_propiedad iap3 inner join inside_aplicaciones ia3 on iap3.idaplicacion = ia3.idaplicacion and iap3.propiedad ='passwordKS')tabla3"
              + " on tabla1.idaplicacion = tabla3.idaplicacion"
              + " inner join (select iap4.valor as tipoKS, iap4.idaplicacion  from inside_aplicaciones_propiedad iap4 inner join inside_aplicaciones ia4 on iap4.idaplicacion = ia4.idaplicacion and iap4.propiedad ='tipoKS')tabla4"
              + " on tabla1.idaplicacion = tabla4.idaplicacion" + " ) order by rutaKS");


      List aResultados = q.getResultList();
      mapResultados = new HashMap<>();

      for (Object objAux : aResultados) {
        Object[] obj = (Object[]) objAux;
        InfoCertModel infoModel =
            new InfoCertModel((String) obj[0], (String) obj[2], (String) obj[3], (String) obj[4]);
        String idAplicacion = (String) obj[1];
        infoModel.addAplicacion(idAplicacion);
        String rutaKS = (String) obj[0];

        if (mapResultados.get(rutaKS) != null) {
          InfoCertModel infoCertModelOld = mapResultados.get(rutaKS);
          infoCertModelOld.addAplicacion(idAplicacion);
          mapResultados.remove(rutaKS);
          mapResultados.put(rutaKS, infoCertModelOld);
        } else {
          mapResultados.put(rutaKS, infoModel);
        }

      }


      // para cada elemento
      for (Map.Entry<String, InfoCertModel> auxMap : mapResultados.entrySet()) {
        InfoCertModel infoCertModel = auxMap.getValue();
        infoCertModel.convertRutaCertificadoWithEnviromentVariable();

        File certfile = new File(infoCertModel.getRutaCertificado());

        if (!certfile.exists()) {
          infoCertModel.setError("No se ha encontrado el certificado en la ruta propuesta "
              + certfile.toPath().toString());
          continue;
        }

        String certificateStr = null;
        try {
          byte[] bytesCert = Files.readAllBytes(certfile.toPath());

          certificateStr = Base64.encodeBase64String(bytesCert);
        } catch (IOException e) {
          infoCertModel.setError("Error al extraer la informacion del certificado "
              + certfile.toPath().toString() + " " + e.getMessage());
        }

        // se va a realizar una validacion de certificado y obtencion de la informacion de validez
        // de los mismos.
        // Para poder hacer esto se necesita convertir el .p12 (privado) en un .cer o .pem (publico)
        // infoCertModel.setResultadoValidarCertificado(consumerEeutilOperFirmaImpl.validacionCertificado(idApp,
        // pwd, certificateStr));

        // Se obtendra la info de expiracion de certificado
        Object[] oInfoCertificado = null;
        try {
          oInfoCertificado = informacionCertificado(certfile, infoCertModel.getAliasCertificado(),
              infoCertModel.getPwdCertificado(), infoCertModel.getTipoAlmacen());
        } catch (EeutilException e) {
          infoCertModel
              .setError("Error al extraer la informacion del certificado " + e.getMessage());
          continue;
        }
        Date[] dateExpirty = new Date[2];

        dateExpirty[0] = (Date) oInfoCertificado[0];
        dateExpirty[1] = (Date) oInfoCertificado[1];
        String serialCert = oInfoCertificado[2].toString();
        String typeCert = (String) oInfoCertificado[3];
        infoCertModel.setDateExpiryCert(dateExpirty);
        infoCertModel.setSerialCert(serialCert);
        infoCertModel.setTypeCert(typeCert);


        logger.info(infoCertModel);

      }


    }

    finally {
      if (em != null)
        em.close();
    }

    return mapResultados;
  }


  private Object[] informacionCertificado(File certfile, String aliasCertificado,
      String passwCertificado, String tipoAlmacen) throws Exception {
    // File certfile=null;

    Object[] resultado = new Object[4];
    X509Certificate certificate = null;

    try {

      if (!certfile.exists()) {
        throw new IOException("Certificado no encontrado");
      }
      logger.debug("Encontrado almacen de certificados");

      PasswordCallback pc = new PasswordCallback(">", false);
      pc.setPassword(passwCertificado.toCharArray());


      AOKeyStoreManager ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
          AOKeyStore.valueOf(tipoAlmacen), certfile.getAbsolutePath(), null, pc, null);

      if (ksm == null) {
        throw new IOException("ksm valor nulo");
      }

      logger.debug("Obteniendo keystoreManager...: end");

      String alias = aliasCertificado;
      PrivateKeyEntry pek = ksm.getKeyEntry(alias);

      if (pek == null) {
        pek = ksm.getKeyEntry(alias.toLowerCase());
        if (pek == null) {
          pek = ksm.getKeyEntry(alias.toUpperCase());
        }
        if (pek == null) {

          if (ksm.getAliases() != null && ksm.getAliases().length == 1) {
            pek = ksm.getKeyEntry(ksm.getAliases()[0]);
          }

          if (pek == null) {
            throw new Exception("Error al obtener la PrivateKeyEntry del certificado");
          }
        }
      }

      certificate = (X509Certificate) pek.getCertificateChain()[0];

      logger.debug(" Validate To Date. " + certificate.getNotAfter());
      logger.debug("Validate Before Date. " + certificate.getNotBefore());
      logger.debug("Serial number certificate. " + certificate.getSerialNumber());
      logger.debug("Tipo de certificado. " + certificate.getType());


    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

    resultado[0] = (certificate.getNotBefore());
    resultado[1] = (certificate.getNotAfter());
    resultado[2] = (certificate.getSerialNumber());
    resultado[3] = (certificate.getType());

    return resultado;
  }

  /**
   * Metodo donde se prepara el body del mensaje, si esta vacio de errores y avisos se devolvera
   * null.
   */
  private String prepararMensajeCorreo(Map<String, InfoCertModel> mapResultados, String entorno) {

    StringBuilder messageBody = new StringBuilder("");

    Calendar calendarNext1Month = Calendar.getInstance();
    calendarNext1Month.setTime(new Date());
    // le sumamos un mes
    calendarNext1Month.add(Calendar.MONTH, 1);

    Calendar calendarToday = Calendar.getInstance();
    calendarToday.setTime(new Date());

    for (Map.Entry<String, InfoCertModel> mapAux : mapResultados.entrySet()) {
      InfoCertModel modelAux = mapAux.getValue();

      Date[] afechasExpiry = modelAux.getDateExpiryCert();

      if (modelAux.getError() != null) {
        // certificado con incidencia
        messageBody.append("* Certificado con incidencia: ").append(modelAux.toString())
            .append(System.getProperty(LINE_SEPARATOR)).append(System.getProperty(LINE_SEPARATOR));
      }

      if (afechasExpiry == null || afechasExpiry.length == 0) {
        continue;
      }

      Calendar calendarExpiry = Calendar.getInstance();
      // fecha de expiracion del certificado.
      calendarExpiry.setTime(afechasExpiry[1]);


      if (calendarExpiry.compareTo(calendarToday) < 0
          || calendarExpiry.compareTo(calendarToday) == 0) {
        // certificado caducado
        messageBody.append("* Certificado caducado: ").append(modelAux.toString())
            .append(System.getProperty(LINE_SEPARATOR)).append(System.getProperty(LINE_SEPARATOR));
      }

      else if (calendarExpiry.compareTo(calendarNext1Month) < 0) {
        // queda menos de un mes para que expire el certificado.
        messageBody.append("* Certificado a punto de caducar: ").append(modelAux.toString())
            .append(System.getProperty(LINE_SEPARATOR)).append(System.getProperty(LINE_SEPARATOR));
      }

      //

    }

    if ("".equals(messageBody.toString())) {
      return null;
    }

    return "Incidencias en los certificados del entorno " + entorno
        + System.getProperty(LINE_SEPARATOR) + System.getProperty(LINE_SEPARATOR)
        + messageBody.toString();

  }

  private void enviarCorreo(Map<String, InfoCertModel> mapResultados, String entorno,
      String bodyMessage) throws EeutilException {


    // System.setProperty("javax.net.ssl.trustStore",
    // System.getProperty("javax.net.ssl.trustStore"));
    // System.setProperty("javax.net.ssl.trustStorePassword",
    // System.getProperty("javax.net.ssl.trustStorePassword"));
    Properties prop = new Properties();
    prop.put("mail.smtp.host", host);
    prop.put("mail.smtp.port", port);
    prop.put("mail.smtp.auth", auth);
    prop.put("mail.smtp.starttls.enable", starttls); // TLS
    prop.put("mail.smtp.ssl.trust", trust);

    Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, pwd);
      }
    });

    try {

      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.setRecipients(Message.RecipientType.TO,
          // InternetAddress.parse("noreply.inside.gestion@correo.gob.es,
          // noreply.inside.gestion@correo.gob.es")
          InternetAddress.parse(to));
      message.setSubject("Gestion de certificados de eeutils. Entorno:" + entorno);
      message.setText(bodyMessage);

      Transport.send(message);

    } catch (Exception e) {
      throw new EeutilException(e.getMessage(), e);
    }

  }


  public String getAplicacionGestionada() {
    // Intentamos instanciar con reflection una clase de cada uno de los modulos.

    try {
      Class.forName("es.mpt.dsic.inside.util.SecurityUtils");
      return EEUTIL_FIRMA;
    } catch (ClassNotFoundException e) {
      // si no esta instanciada una clase de firma.
      try {
        // util-firma
        Class.forName("es.mpt.dsic.inside.util.SignedData");
        return EEUTIL_UTIL_FIRMA;
      } catch (ClassNotFoundException e1) {
        // oper-firma
        try {
          Class.forName("es.mpt.dsic.inside.ws.service.postprocess.PostProcessUtil");
          return EEUTIL_OPER_FIRMA;
        } catch (ClassNotFoundException e2) {
          // vis-doc-exp
          try {
            Class.forName("es.mpt.dsic.inside.visualizacion.PieAdder");
            return EEUTIL_VIS_DOCEXP;
          } catch (ClassNotFoundException e3) {
            // misc
            try {
              Class.forName("es.mpt.dsic.eeutil.misc.web.util.WebConstants");
              return EEUTIL_MISC;
            } catch (ClassNotFoundException e4) {
              logger.error("Error, no se ha podido identificar el modulo de la aplicacion");
            }

          }

        }

      }

    }


    return null;
  }


}
