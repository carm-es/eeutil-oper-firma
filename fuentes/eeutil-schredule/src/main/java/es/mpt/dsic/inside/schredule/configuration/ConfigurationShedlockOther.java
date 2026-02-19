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

package es.mpt.dsic.inside.schredule.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import es.mpt.dsic.inside.config.EeutilApplicationDataConfig;
import es.mpt.dsic.inside.config.EeutilConfigPath;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;


// @PropertySource("file:${eeutil-misc.config.path}/database.properties")
@Configuration
public class ConfigurationShedlockOther {

  protected static final Log logger = LogFactory.getLog(ConfigurationShedlockOther.class);

  private static final String APPLICATION_NAME = "application.name";
  private static final String APPLICATION_PROPERTIES = "application.properties";

  @Autowired
  private DataSource dataSource;

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  // @Value("${databaseGen.username}")
  private String databaseUsername;

  // @Value("${databaseGen.password}")
  private String databasePassword;

  // @Value("${database.driverClassName}")
  private String databaseDriver;

  // @Value("${databaseGen.url}")
  private String databaseUrl;



  @Bean
  @Primary
  public DataSource dataSource() {

    Properties propiedades = new Properties();
    Properties props = EeutilApplicationDataConfig.loadProperties(APPLICATION_PROPERTIES);

    /**
     * String ruta = System.getProperty("config.path") + "/textos.errores.firma.properties";
     */
    String ruta = null;
    if (System.getenv(
        props.getProperty(APPLICATION_NAME) + "." + EeutilConfigPath.CONFIG_PATH_VAR) != null) {
      ruta = System
          .getenv(props.getProperty(APPLICATION_NAME) + "." + EeutilConfigPath.CONFIG_PATH_VAR)
          + "/database.properties";
    } else {
      ruta = System
          .getProperty(props.getProperty(APPLICATION_NAME) + "." + EeutilConfigPath.CONFIG_PATH_VAR)
          + "/database.properties";
    }


    try (FileInputStream fin = new FileInputStream(ruta);) {
      propiedades.load(fin);

      databaseUsername = propiedades.getProperty("databaseGen.username");
      databaseDriver = propiedades.getProperty("database.driverClassName");
      databasePassword = propiedades.getProperty("databaseGen.password");
      databaseUrl = propiedades.getProperty("databaseGen.url");

    } catch (IOException e) {
      logger.error("Error al obtener las propiedades de base de datos " + e.getMessage(), e);
    }



    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
    driverManagerDataSource.setUsername(databaseUsername);
    driverManagerDataSource.setDriverClassName(databaseDriver);
    driverManagerDataSource.setPassword(databasePassword);
    driverManagerDataSource.setUrl(databaseUrl);

    return driverManagerDataSource;
  }

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
        .withJdbcTemplate(new JdbcTemplate(dataSource)).build());
  }

}
