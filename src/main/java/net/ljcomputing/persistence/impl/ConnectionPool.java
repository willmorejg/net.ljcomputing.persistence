/**
           Copyright 2016, James G. Willmore

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package net.ljcomputing.persistence.impl;

import net.ljcomputing.exception.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Connection pool.
 *
 * @author James G. Willmore
 */
public final class ConnectionPool {
  /** SLF4J logger. */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ConnectionPool.class);

  /** The instance of the connection pool. */
  private static final ConnectionPool INSTANCE = new ConnectionPool();

  /** The properties. */
  private transient Properties properties;

  /** The Basic Data Source. */
  private transient final BasicDataSource BDS = new BasicDataSource();

  /**
   * Instantiates a new connection pool.
   */
  private ConnectionPool() {
    if (null == properties) {
      properties = new Properties();
    }

    try {
      properties.load(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("database.properties"));

      BDS.setDriverClassName(properties.getProperty("cp.driver.class"));
      BDS.setUrl(properties.getProperty("cp.db.url"));
      BDS.setUsername(properties.getProperty("cp.db.user"));
      BDS.setPassword(properties.getProperty("cp.db.password"));
      BDS.setInitialSize(Integer.valueOf(properties.getProperty("cp.size")));
    } catch (IOException exception) {
      LOGGER.error("Cannot instniate Connection Pool: ", exception);
    }
  }

  /**
   * Gets the single instance of ConnectionPool.
   *
   * @return single instance of ConnectionPool
   */
  public static ConnectionPool getInstance() {
    return INSTANCE;
  }

  /**
   * Data source.
   *
   * @return the data source
   */
  public DataSource dataSource() {
    return BDS;
  }

  /**
   * Gets the connection.
   *
   * @return the connection
   * @throws PersistenceException the persistence exception
   */
  public Connection getConnection() throws PersistenceException {
    try {
      return BDS.getConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }
}
