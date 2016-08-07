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

package net.ljcomputing.repository.impl;

import net.ljcomputing.SqlUtils;
import net.ljcomputing.StringUtils;
import net.ljcomputing.exception.PersistenceException;
import net.ljcomputing.model.Model;
import net.ljcomputing.persistence.DataSourceTable;
import net.ljcomputing.persistence.EntityPopulator;
import net.ljcomputing.persistence.impl.ConnectionPool;
import net.ljcomputing.persistence.impl.EntityPopulatorImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract repository implementation.
 *
 * @author James G. Willmore
 * @param <T> the Model associated with the repository
 */
public abstract class AbstractRepository<T extends Model>
    implements ModelRepository<T> {
  
  /** The Constant LOGGER. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory
      .getLogger(AbstractRepository.class);

  /** The entity populator. */
  protected final EntityPopulator ep;

  /** The connection. */
  protected Connection conn;

  /** The prepared statement. */
  protected PreparedStatement ps;

  /** The associated data source table. */
  protected DataSourceTable table;

  /**
   * Instantiates a new abstract repository.
   *
   * @param table the table
   * @throws PersistenceException the persistence exception
   */
  public AbstractRepository(final DataSourceTable table)
          throws PersistenceException {
    this.table = table;
    this.ep = new EntityPopulatorImpl();
  }

  /**
   * Gets the constructor.
   *
   * @return the constructor
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  protected Constructor<T> getConstructor() throws Exception {
    return (Constructor<T>) table.getModel()
        .getConstructor(new Class[] { EntityPopulator.class, ResultSet.class });
  }

  /**
   * Obtain connection.
   *
   * @throws PersistenceException the persistence exception
   */
  protected void obtainConnection() throws PersistenceException {
    conn = ConnectionPool.getInstance().getConnection();
  }

  /**
   * Obtain prepared statement.
   *
   * @param sql the sql
   * @return the prepared statement
   * @throws PersistenceException the persistence exception
   */
  protected PreparedStatement obtainPreparedStatement(final String sql)
      throws PersistenceException {
    try {
      //verify we have a connection, and it is opened
      if(null == conn || conn.isClosed()) {
        obtainConnection();
      }
      
      return conn.prepareStatement(sql);
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Close prepared statement.
   */
  protected void closePreparedStatement() {
    if (null != ps) {
      try {
        ps.close();
      } catch (Exception exception) {
        // do nothing
      }
    }
  }

  /**
   * Close connection.
   */
  protected void closeConnection() {
    if (null != conn) {
      try {
        conn.close();
      } catch (Exception exception) {
        // do nothing
      }
    }
  }

  /**
   * @see net.ljcomputing.repository.impl.ModelRepository#create(net.ljcomputing.model.Model, java.lang.String[])
   */
  public void create(final T model, final String... columns)
      throws PersistenceException {
    ResultSet rs = null;
    int key = -1;

    try {
      obtainConnection();
      
      ps = conn.prepareStatement(
          SqlUtils.buildInsertStatement(table.getTableName(), columns),
          Statement.RETURN_GENERATED_KEYS);

      for (int c = 0; c < columns.length; c++) {
        try {
          ps.setObject(c + 1, getModelValue(model, columns[c]));
        } catch (Exception e) {
          throw new SQLException(e);
        }
      }

      ps.executeUpdate();

      rs = ps.getGeneratedKeys();

      if (rs.next()) {
        key = rs.getInt(1);
      }

      model.setId(key);
      closePreparedStatement();
      closeConnection();
    } catch (Exception exception) {
      throw new PersistenceException(exception);
    } finally {
      if (null != rs) {
        try {
          rs.close();
        } catch (SQLException e) {
          // do nothing
        }
      }
    }
  }

  /**
   * @see net.ljcomputing.repository.impl.ModelRepository#update(net.ljcomputing.model.Model, java.lang.String[])
   */
  public void update(final T model, final String... columns)
      throws PersistenceException {
    final String primaryKey = "id";
    final String sql = SqlUtils.buildUpdateStatement(table.getTableName(),
        primaryKey, columns);

    try {
      ps = obtainPreparedStatement(sql);

      int column = 1;
      for (Object value : getModelValues(model, columns)) {
        ps.setObject(column++, value);
      }

      ps.setObject(column, model.getId());

      ps.executeUpdate();
      closePreparedStatement();
      closeConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    } catch (Exception exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * @see net.ljcomputing.repository.impl.ModelRepository#delete(net.ljcomputing.model.Model)
   */
  public void delete(final T model) throws PersistenceException {
    delete(model.getId());
  }

  /**
   * @see net.ljcomputing.repository.impl.ModelRepository#delete(java.lang.Integer)
   */
  public void delete(final Integer id) throws PersistenceException {
    final String primaryKey = "id";
    final String sql = SqlUtils.buildDeleteStatement(table.getTableName(),
        primaryKey);

    try {
      ps = obtainPreparedStatement(sql);
      ps.setObject(1, id);
      ps.executeUpdate();
      closePreparedStatement();
      closeConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * @see net.ljcomputing.repository.impl.ModelRepository#readById(java.lang.Integer)
   */
  public T readById(final Integer id) throws PersistenceException {
    final String sql = "select * from " + table.getTableName() + " where id=?";
    T model = null;
    ResultSet rs = null;

    try {
      ps = obtainPreparedStatement(sql);

      ps.setObject(1, id);

      rs = ps.executeQuery();

      while (rs.next()) {
        model = getConstructor().newInstance(ep, rs);
      }

      closePreparedStatement();
      closeConnection();
    } catch (Exception exception) {
      throw new PersistenceException(exception);
    } finally {
      if (null != rs) {
        try {
          rs.close();
        } catch (SQLException e) {
          // do nothing
        }
      }
    }

    return model;
  }

  /**
   * @see net.ljcomputing.repository.impl.ModelRepository#readAll()
   */
  public List<T> readAll() throws PersistenceException {
    final String sql = "select * from " + table.getTableName();
    final List<T> list = new ArrayList<T>();
    ResultSet rs = null;

    try {
      ps = obtainPreparedStatement(sql);
      rs = ps.executeQuery();

      while (rs.next()) {
        list.add(getConstructor().newInstance(ep, rs));
      }

      closePreparedStatement();
      closeConnection();
    } catch (Exception exception) {
      throw new PersistenceException(exception);
    } finally {
      if (null != rs) {
        try {
          rs.close();
        } catch (SQLException e) {
          // do nothing
        }
      }
    }

    return list;
  }

  /**
   * Gets the model values.
   *
   * @param model the model
   * @param fieldNames the field names
   * @return the model values
   * @throws Exception the exception
   */
  protected Object[] getModelValues(final T model, final String... fieldNames)
      throws Exception {
    final Object[] values = new Object[fieldNames.length];

    for (int f = 0; f < fieldNames.length; f++) {
      values[f] = getModelValue(model, fieldNames[f]);
    }

    return values;
  }

  /**
   * Gets the model value.
   *
   * @param model the model
   * @param fieldName the field name
   * @return the model value
   * @throws Exception the exception
   */
  protected Object getModelValue(final T model, final String fieldName)
      throws Exception {
    final String newFieldName = StringUtils.toMemberCase(fieldName);
    final Field field = model.getClass().getDeclaredField(newFieldName);
    field.setAccessible(true);
    return field.get(model);
  }
}
