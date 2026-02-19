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

package es.mpt.dsic.loadTables.schredule;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.mpt.dsic.loadTables.exception.UnidadOrganicaException;
import es.mpt.dsic.loadTables.handler.HandlerOficina;
import es.mpt.dsic.loadTables.handler.HandlerUnidadOrganica;
import es.mpt.dsic.loadTables.hibernate.service.impl.UnidadOrganicaServiceImpl;
import es.mpt.dsic.loadTables.objects.Organismo;
import es.mpt.dsic.loadTables.service.impl.ConsumidorDir;
import es.mpt.dsic.loadTables.service.impl.ConsumidorOficina;
import es.mpt.dsic.loadTables.utils.Constantes;
import es.mpt.dsic.loadTables.utils.DateUtil;
import es.mpt.dsic.loadTables.utils.Utils;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

@Component
public class UnidadOrganicaTask {

  private static final String ERROR_AL_CARGAR_DATOS = "Error al cargar datos: ";

  protected static final Log logger = LogFactory.getLog(UnidadOrganicaTask.class);

  private static final String strDateFormat = "dd/MM/yyyy";

  @Autowired
  private UnidadOrganicaServiceImpl unidadOrganicaService;

  @Autowired
  private ConsumidorDir consumerUnidades;

  @Autowired
  private ConsumidorOficina consumerOficinas;

  @Autowired
  ConfigurationShedlock configurationShedlock;

  @Value("${Path.temp}")
  private String rutaTemp;
  @Value("Organismo_temp.txt")
  private String ficheroTxtTemporal;
  @Value("Organismo_temp.zip")
  private String ficheroZipTemporal;
  @Value("organismo")
  private String pathDescompresion;
  @Value("Oficinas_temp.txt")
  private String ficheroOficinasTxtTemporal;
  @Value("Oficinas_temp.zip")
  private String ficheroOficinasZipTemporal;
  @Value("oficinas")
  private String pathDescompresionOficinas;


  // @Scheduled(cron = "0/5 * * * * ?") public void holaMundo() {
  // System.out.println("HOLAMUNDO"); }

  @Scheduled(cron = "${unidadorganica.cron.expression}")
  public void tarea() throws Exception {

    // Configuracion de shedlock//
    // To assert that the lock is held (prevents misconfiguration errors)
    LockingTaskExecutor executor = new DefaultLockingTaskExecutor(
        configurationShedlock.lockProvider(configurationShedlock.dataSource()));
    Runnable command = new MyDir3Task(this);

    // bloqueo durante 23 horas.
    Instant lockAtMostUntil = Instant.now().plusSeconds(Long.parseLong("82800"));
    Instant lockAtLeastUntil = lockAtMostUntil;
    LockConfiguration lockConfiguration =
        new LockConfiguration("lockDir3", lockAtMostUntil, lockAtLeastUntil);
    executor.executeWithLock(command, lockConfiguration);
    // Fin de configuracion de shedlock

  }



  public void cuerpoTareaDir3() {
    try {
      SimpleDateFormat objSDF = new SimpleDateFormat(strDateFormat);
      // traemos los ultimos 7 dias.
      Date dInicial = DateUtil.sumarDiasDate(new Date(), -7);

      loadUnidadOrganica(dInicial, null);
      loadOficinas(dInicial, null);

    } catch (Exception e) {
      logger.error(e.getMessage());
    }

  }

  public void loadUnidadOrganica(Date dInicial, Date dFinal) throws Exception {
    try {
      logger.info("Inicio Load Tablas UnidadOrganica");

      if (consumerUnidades.configure()) {
        // Comprueba que finalice en barra, si no, se la pone
        if (!rutaTemp.endsWith(Constantes.FILE_SEPARATOR))
          rutaTemp = rutaTemp + Constantes.FILE_SEPARATOR;

        // obtener fecha ultima sincronizacion
        // Date unidadOrganicaSyncDate = unidadOrganicaService.geLastSync();

        String base64 = "";


        String file = rutaTemp + pathDescompresion + Constantes.FILE_SEPARATOR + "Unidades.xml";
        // if (unidadOrganicaSyncDate == null)
        // {

        // la fecha limite sera la actual o la que marquemos segun precise
        Date dFechaLimite = dFinal != null ? dFinal : new Date();

        do {
          base64 = consumerUnidades.volcadoDatosBasicos(dInicial);

          if (!esVacioOrNull(base64)) {
            inserData(file, base64);
          }

          dInicial = DateUtil.sumarDiasDate(dInicial, 15);

        } while (dInicial.compareTo(dFechaLimite) <= 0);

      } else {
        logger.error("No se han podido actualizar las unidades organicas");
      }

      logger.info("Fin Load Tablas UnidadOrganica");

    } catch (FileNotFoundException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    } catch (IOException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    } catch (UnidadOrganicaException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    }
  }


  public void loadUnidadOrganica(String codigoUnidadOrganica) throws Exception {
    try {
      logger.info("Inicio Load Tablas UnidadOrganica");

      if (consumerUnidades.configure()) {
        // Comprueba que finalice en barra, si no, se la pone
        if (!rutaTemp.endsWith(Constantes.FILE_SEPARATOR))
          rutaTemp = rutaTemp + Constantes.FILE_SEPARATOR;

        String base64 = "";
        String file = rutaTemp + pathDescompresion + Constantes.FILE_SEPARATOR + "Unidades.xml";



        base64 = consumerUnidades.volcadoDatosBasicos(codigoUnidadOrganica);

        if (!esVacioOrNull(base64)) {
          inserData(file, base64);
        }

      } else {
        logger.error("No se han podido actualizar la unidad organica: " + codigoUnidadOrganica);
      }

      logger.info("Fin Load Tablas UnidadOrganica");

    } catch (FileNotFoundException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    } catch (IOException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    } catch (UnidadOrganicaException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    }
  }

  public void loadOficinas(Date dInicial, Date dFinal) throws Exception {

    try {

      logger.info("Inicio Load las oficinas en la Tabla UnidadOrganica");

      if (consumerOficinas.configure()) {
        // Comprueba que finalice en barra, si no, se la pone
        if (!rutaTemp.endsWith(Constantes.FILE_SEPARATOR))
          rutaTemp = rutaTemp + Constantes.FILE_SEPARATOR;

        String base64 = "";
        String file =
            rutaTemp + pathDescompresionOficinas + Constantes.FILE_SEPARATOR + "Oficinas.xml";

        // la fecha limite sera la actual o la que marquemos segun precise
        Date dFechaLimite = dFinal != null ? dFinal : new Date();



        do {
          base64 = consumerOficinas.volcadoDatosBasicos(dInicial);

          if (!esVacioOrNull(base64)) {
            inserData(file, base64);
          }

          dInicial = DateUtil.sumarDiasDate(dInicial, 15);

        } while (dInicial.compareTo(dFechaLimite) <= 0);

      } else {
        logger.error("No se han podido actualizar las oficinas");
      }

      logger.info("Fin Load las oficinas en la Tabla UnidadOrganica");

    } catch (FileNotFoundException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    } catch (IOException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    } catch (UnidadOrganicaException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      logger.error(ERROR_AL_CARGAR_DATOS + dInicial + " " + e.getMessage(), e);
      throw e;
    }
  }


  public void loadOficinas(String codigoUnidadOrganica) throws Exception {

    try {

      logger.info("Inicio Load las oficinas en la Tabla UnidadOrganica");

      if (consumerOficinas.configure()) {
        // Comprueba que finalice en barra, si no, se la pone
        if (!rutaTemp.endsWith(Constantes.FILE_SEPARATOR))
          rutaTemp = rutaTemp + Constantes.FILE_SEPARATOR;

        String base64 = "";
        String file =
            rutaTemp + pathDescompresionOficinas + Constantes.FILE_SEPARATOR + "Oficinas.xml";

        base64 = consumerOficinas.volcadoDatosBasicos(codigoUnidadOrganica);

        if (!esVacioOrNull(base64)) {
          inserData(file, base64);
        }

      } else {
        logger.error("No se han podido actualizar la oficina: " + codigoUnidadOrganica);
      }

      logger.info("Fin Load las oficinas en la Tabla UnidadOrganica");

    } catch (FileNotFoundException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    } catch (IOException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    } catch (UnidadOrganicaException e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      logger.error(ERROR_AL_CARGAR_DATOS + codigoUnidadOrganica + " " + e.getMessage(), e);
      throw e;
    }
  }


  private void inserData(String file, String base64) throws IOException, UnidadOrganicaException {


    List<Organismo> organismos = null;

    if (file.contains("ficina")) {
      organismos = createFileOficinas(file, base64);
    } else {
      organismos = createFileUnidadesOrganica(file, base64);
    }



    // guardado de datos
    unidadOrganicaService.saveList(organismos, new Date());
  }

  /**
   * @param file
   * @param base64
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnidadOrganicaException
   */
  private List<Organismo> createFileUnidadesOrganica(String file, String base64)
      throws IOException, UnidadOrganicaException {

    FileInputStream fis = null;

    try {

      // borrado de datos anteriores
      Utils.deleteBefore(rutaTemp, ficheroTxtTemporal, ficheroZipTemporal, pathDescompresion);

      Path path = Paths.get(rutaTemp + ficheroTxtTemporal);
      byte[] strToBytes = base64.getBytes();

      Files.write(path, strToBytes);

      // relleno fichero txt con base 64
      // Utils.writeDataBase64(base64, rutaTemp, ficheroTxtTemporal);

      fis = new FileInputStream(rutaTemp + ficheroTxtTemporal);

      // convertir fichero txt base64 a zip
      Utils.getUnZippedFile(fis, rutaTemp + ficheroZipTemporal);

      // descomprimir fichero zip
      Utils.unZipFile(rutaTemp + ficheroZipTemporal, rutaTemp + pathDescompresion);

      logger.debug("Fichero a cargar: " + file);

      // parseo de ficheros
      HandlerUnidadOrganica handler = new HandlerUnidadOrganica(file);

      List<Organismo> aOrganismos = handler.getOrganimos();

      Collections.sort(aOrganismos, new Comparator<Organismo>() {
        public int compare(Organismo o1, Organismo o2) {

          if (o1.getDatosIdentificativos().getVersion() == null
              && o2.getDatosIdentificativos().getVersion() == null)
            return 0;
          else if (o1.getDatosIdentificativos().getVersion() == null)
            return -1;
          else if (o2.getDatosIdentificativos().getVersion() == null)
            return 1;
          else
            return o1.getDatosIdentificativos().getVersion()
                .compareTo(o2.getDatosIdentificativos().getVersion());
        }
      });


      return aOrganismos;

    } finally {
      if (fis != null)
        fis.close();
    }
  }


  /**
   * @param file
   * @param base64
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnidadOrganicaException
   */
  private List<Organismo> createFileOficinas(String file, String base64)
      throws IOException, UnidadOrganicaException {

    FileInputStream fis = null;

    try {

      // borrado de datos anteriores
      Utils.deleteBefore(rutaTemp, ficheroOficinasTxtTemporal, ficheroOficinasZipTemporal,
          pathDescompresionOficinas);

      Path path = Paths.get(rutaTemp + ficheroOficinasTxtTemporal);
      byte[] strToBytes = base64.getBytes();

      Files.write(path, strToBytes);

      // relleno fichero txt con base 64
      // Utils.writeDataBase64(base64, rutaTemp, ficheroTxtTemporal);

      fis = new FileInputStream(rutaTemp + ficheroOficinasTxtTemporal);

      // convertir fichero txt base64 a zip
      Utils.getUnZippedFile(fis, rutaTemp + ficheroOficinasZipTemporal);

      // descomprimir fichero zip
      Utils.unZipFile(rutaTemp + ficheroOficinasZipTemporal, rutaTemp + pathDescompresionOficinas);

      logger.debug("Fichero a cargar: " + file);

      // parseo de ficheros
      HandlerOficina handler = new HandlerOficina(file);

      List<Organismo> aOrganismos = handler.getOrganimos();

      Collections.sort(aOrganismos, new Comparator<Organismo>() {
        public int compare(Organismo o1, Organismo o2) {

          if (o1.getDatosIdentificativos().getVersion() == null
              && o2.getDatosIdentificativos().getVersion() == null)
            return 0;
          else if (o1.getDatosIdentificativos().getVersion() == null)
            return -1;
          else if (o2.getDatosIdentificativos().getVersion() == null)
            return 1;
          else
            return o1.getDatosIdentificativos().getVersion()
                .compareTo(o2.getDatosIdentificativos().getVersion());
        }
      });

      return aOrganismos;

    } finally {
      if (fis != null)
        fis.close();
    }
  }


  // private void inserDataOficinas(String file, String base64) throws IOException,
  // UnidadOrganicaException
  // {
  //
  // FileInputStream fis=null;
  //
  // try
  // {
  // fis= new FileInputStream(rutaTemp + ficheroOficinasTxtTemporal);
  // // borrado de datos anteriores
  // Utils.deleteBefore(rutaTemp, ficheroOficinasTxtTemporal, ficheroOficinasZipTemporal,
  // pathDescompresionOficinas);
  //
  // // relleno fichero txt con base 64
  // Utils.writeDataBase64(base64, rutaTemp, ficheroOficinasTxtTemporal);
  //
  // // convertir fichero txt base64 a zip
  // Utils.getUnZippedFile(
  // fis, rutaTemp
  // + ficheroOficinasZipTemporal);
  //
  // // descomprimir fichero zip
  // Utils.unZipFile(rutaTemp + ficheroOficinasZipTemporal, rutaTemp
  // + pathDescompresionOficinas);
  //
  // logger.debug("Fichero a cargar: " + file);
  //
  //
  // //se hace un handleroficinas igual que el handlerunidadesorganicas y simulando un organismo
  // pero metiendo los datos de oficina para
  // //poder cargarlo en la misma tabla porque solo queremos para validar, es decir, saber que
  // existe
  //
  // HandlerOficina handler = new HandlerOficina(file);
  // List<Organismo> organismos = handler.getOrganimos();
  //
  // // guardado de datos
  // unidadOrganicaService.saveList(organismos, new Date());
  // }
  // finally
  // {
  // if(fis!=null)
  // fis.close();
  // }
  //
  // }

  public UnidadOrganicaServiceImpl getUnidadOrganicaService() {
    return unidadOrganicaService;
  }

  public void setUnidadOrganicaService(UnidadOrganicaServiceImpl unidadOrganicaService) {
    this.unidadOrganicaService = unidadOrganicaService;
  }

  public String getRutaTemp() {
    return rutaTemp;
  }

  public void setRutaTemp(String rutaTemp) {
    this.rutaTemp = rutaTemp;
  }

  public String getFicheroTxtTemporal() {
    return ficheroTxtTemporal;
  }

  public void setFicheroTxtTemporal(String ficheroTxtTemporal) {
    this.ficheroTxtTemporal = ficheroTxtTemporal;
  }

  public String getFicheroZipTemporal() {
    return ficheroZipTemporal;
  }

  public void setFicheroZipTemporal(String ficheroZipTemporal) {
    this.ficheroZipTemporal = ficheroZipTemporal;
  }

  public String getPathDescompresion() {
    return pathDescompresion;
  }

  public void setPathDescompresion(String pathDescompresion) {
    this.pathDescompresion = pathDescompresion;
  }

  public String getFicheroOficinasTxtTemporal() {
    return ficheroOficinasTxtTemporal;
  }

  public void setFicheroOficinasTxtTemporal(String ficheroOficinasTxtTemporal) {
    this.ficheroOficinasTxtTemporal = ficheroOficinasTxtTemporal;
  }

  public String getFicheroOficinasZipTemporal() {
    return ficheroOficinasZipTemporal;
  }

  public void setFicheroOficinasZipTemporal(String ficheroOficinasZipTemporal) {
    this.ficheroOficinasZipTemporal = ficheroOficinasZipTemporal;
  }

  public String getPathDescompresionOficinas() {
    return pathDescompresionOficinas;
  }

  public void setPathDescompresionOficinas(String pathDescompresionOficinas) {
    this.pathDescompresionOficinas = pathDescompresionOficinas;
  }

  public ConsumidorDir getConsumerUnidades() {
    return consumerUnidades;
  }

  public void setConsumerUnidades(ConsumidorDir consumerUnidades) {
    this.consumerUnidades = consumerUnidades;
  }

  public ConsumidorOficina getConsumerOficinas() {
    return consumerOficinas;
  }

  public void setConsumerOficinas(ConsumidorOficina consumerOficinas) {
    this.consumerOficinas = consumerOficinas;
  }

  public static void main(String args[]) throws Exception {
    SimpleDateFormat objSDF = new SimpleDateFormat(strDateFormat);
    Date dInicial = objSDF.parse("01/01/2001");
  }

  private static boolean esVacioOrNull(String str) {
    if ("".equals(str) || str == null)
      return true;

    return false;
  }

}
