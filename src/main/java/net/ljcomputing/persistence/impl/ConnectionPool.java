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
public class ConnectionPool {
  /** The Constant INSTANCE. */
  private static final ConnectionPool INSTANCE = new ConnectionPool();

  /** The properties. */
  private static Properties properties;

  /** The bds. */
  private final BasicDataSource bds = new BasicDataSource();

  /**
   * Instantiates a new connection pool.
   */
  private ConnectionPool() {
    if (null == properties) {
      properties = new Properties();

      try {
        properties.load(ConnectionPool.class.getClassLoader()
            .getResourceAsStream("database.properties"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    bds.setDriverClassName(properties.getProperty("cp.driver.class"));
    bds.setUrl(properties.getProperty("cp.db.url"));
    bds.setUsername(properties.getProperty("cp.db.user"));
    bds.setPassword(properties.getProperty("cp.db.password"));
    bds.setInitialSize(Integer.valueOf(properties.getProperty("cp.size")));
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
    return bds;
  }

  /**
   * Gets the connection.
   *
   * @return the connection
   * @throws PersistenceException the persistence exception
   */
  public Connection getConnection() throws PersistenceException {
    try {
      return bds.getConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }
}
