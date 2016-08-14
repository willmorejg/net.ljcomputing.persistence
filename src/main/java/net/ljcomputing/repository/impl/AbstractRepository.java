/**
           Copyright 2016, James G. Willmore

   Licensed under the Apache License, VeresultSetion 2.0 (the "License");
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
import net.ljcomputing.persistence.Entity;
import net.ljcomputing.persistence.EntityPopulator;
import net.ljcomputing.persistence.impl.ConnectionPool;
import net.ljcomputing.persistence.impl.EntityPopulatorImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * @param <T> the Model associated with the repository
 */
public abstract class AbstractRepository<T extends Model>
    implements ModelRepository<T> {

 /** The SLF4J logger. */
 @SuppressWarnings("unused")
 private static final Logger LOGGER = 
   LoggerFactory.getLogger(AbstractRepository.class);

  /** The primary key field. */
  private static final String PRIMARY_KEY = "id";
  
  /** The entity populator. */
  protected transient final EntityPopulator entityPopulator;
  
  /** The connection. */
  protected transient Connection connection;
  
  /** The prepared statement. */
  protected transient PreparedStatement preparedStatement;
  
  /** The associated data source table. */
  protected transient DataSourceTable table;

  /**
   * Instantiates a new abstract repository.
   *
   * @param table the table
   * @throws PersistenceException the persistence exception
   */
  public AbstractRepository(final DataSourceTable table)
      throws PersistenceException {
    this.table = table;
    this.entityPopulator = new EntityPopulatorImpl();
  }

  /**
   * Gets the model instance.
   *
   * @return the model instance
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @SuppressWarnings("unchecked")
  protected T getModelInstance() throws NoSuchMethodException,
      SecurityException, InstantiationException, IllegalAccessException {
    return (T) table.getModel().newInstance();
  }

  /**
   * Obtain connection.
   *
   * @throws PersistenceException the persistence exception
   */
  protected void obtainConnection() throws PersistenceException {
    connection = ConnectionPool.getInstance().getConnection();
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
      // verify we have a connection, and it is opened
      if (null == connection || connection.isClosed()) {
        obtainConnection();
      }

      return connection.prepareStatement(sql);
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Close prepared statement.
   */
  protected void closePreparedStatement() {
    if (null != preparedStatement) {
      try {
        preparedStatement.close();
      } catch (SQLException exception) {
        // do nothing
      }
    }
  }

  /**
   * Close connection.
   */
  protected void closeConnection() {
    if (null != connection) {
      try {
        connection.close();
      } catch (SQLException exception) {
        // do nothing
      }
    }
  }

  /**
   * Creates the.
   *
   * @param model the model
   * @param columns the columns
   * @throws PersistenceException the persistence exception
   * @see net.ljcomputing.repository.impl.ModelRepository#create(net.ljcomputing.model.Model, java.lang.String[])
   */
  public void create(final T model, final String... columns)
      throws PersistenceException {
    ResultSet resultSet = null;
    int key = -1;

    try {
      obtainConnection();

      preparedStatement = connection.prepareStatement(
          SqlUtils.buildInsertStatement(table.getTableName(), columns),
          Statement.RETURN_GENERATED_KEYS);

      for (int c = 0; c < columns.length; c++) {
        preparedStatement.setObject(c + 1, getModelValue(model, columns[c]));
      }

      preparedStatement.executeUpdate();

      resultSet = preparedStatement.getGeneratedKeys();

      if (resultSet.next()) {
        key = resultSet.getInt(1);
      }

      model.setId(key);
      closePreparedStatement();
      closeConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    } finally {
      if (null != resultSet) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          // do nothing
        }
      }
    }
  }

  /**
   * Update.
   *
   * @param model the model
   * @param columns the columns
   * @throws PersistenceException the persistence exception
   * @see net.ljcomputing.repository.impl.ModelRepository#update(net.ljcomputing.model.Model, java.lang.String[])
   */
  public void update(final T model, final String... columns)
      throws PersistenceException {
    final String sql = SqlUtils.buildUpdateStatement(table.getTableName(),
        PRIMARY_KEY, columns);

    try {
      preparedStatement = obtainPreparedStatement(sql);

      int column = 1;
      for (final Object value : getModelValues(model, columns)) {
        preparedStatement.setObject(column++, value);
      }

      preparedStatement.setObject(column, model.getId());

      preparedStatement.executeUpdate();
      closePreparedStatement();
      closeConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Delete.
   *
   * @param model the model
   * @throws PersistenceException the persistence exception
   * @see net.ljcomputing.repository.impl.ModelRepository#delete(net.ljcomputing.model.Model)
   */
  public void delete(final T model) throws PersistenceException {
    delete(model.getId());
  }

  /**
   * Delete.
   *
   * @param id the id
   * @throws PersistenceException the persistence exception
   * @see net.ljcomputing.repository.impl.ModelRepository#delete(java.lang.Integer)
   */
  public void delete(final Integer id) throws PersistenceException {
    final String sql = SqlUtils.buildDeleteStatement(table.getTableName(),
        PRIMARY_KEY);

    try {
      preparedStatement = obtainPreparedStatement(sql);
      preparedStatement.setObject(1, id);
      preparedStatement.executeUpdate();
      closePreparedStatement();
      closeConnection();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Read by id.
   *
   * @param id the id
   * @return the t
   * @throws PersistenceException the persistence exception
   * @see net.ljcomputing.repository.impl.ModelRepository#readById(java.lang.Integer)
   */
  public T readById(final Integer id) throws PersistenceException {
    final String sql = "select * from " + table.getTableName() + " where "
        + PRIMARY_KEY + " =?";
    T model = null;
    ResultSet resultSet = null;

    try {
      preparedStatement = obtainPreparedStatement(sql);

      preparedStatement.setObject(1, id);

      resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        model = getPersistedModel(resultSet);
      }

      closePreparedStatement();
      closeConnection();
    } catch (SQLException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | NoSuchMethodException
        | SecurityException exception) {
      throw new PersistenceException(exception);
    } finally {
      if (null != resultSet) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          // do nothing
        }
      }
    }

    return model;
  }

  /**
   * Read all.
   *
   * @return the list
   * @throws PersistenceException the persistence exception
   * @see net.ljcomputing.repository.impl.ModelRepository#readAll()
   */
  public List<T> readAll() throws PersistenceException {
    final String sql = "select * from " + table.getTableName();
    final List<T> list = new ArrayList<T>();
    ResultSet resultSet = null;

    try {
      preparedStatement = obtainPreparedStatement(sql);
      resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        list.add(getPersistedModel(resultSet));
      }

      closePreparedStatement();
      closeConnection();
    } catch (SQLException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | NoSuchMethodException
        | SecurityException exception) {
      throw new PersistenceException(exception);
    } finally {
      if (null != resultSet) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          // do nothing
        }
      }
    }

    return list;
  }
  
  /**
   * Gets the persisted model.
   *
   * @param resultSet the result set
   * @return the persisted model
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws PersistenceException the persistence exception
   */
  @SuppressWarnings("unchecked")
  private T getPersistedModel(ResultSet resultSet) 
      throws NoSuchMethodException, SecurityException, InstantiationException, 
        IllegalAccessException, PersistenceException {
    final Entity entity = (Entity) getModelInstance();
    entity.populate(entityPopulator, resultSet);
    return (T) entity;    
  }

  /**
   * Gets the model values.
   *
   * @param model the model
   * @param fieldNames the field names
   * @return the model values
   * @throws PersistenceException the persistence exception
   */
  protected Object[] getModelValues(final T model, final String... fieldNames)
      throws PersistenceException {
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
   * @throws PersistenceException the persistence exception
   */
  protected Object getModelValue(final T model, final String fieldName)
      throws PersistenceException {
    try {
      final String newFieldName = StringUtils.toMemberCase(fieldName);
      final Class<? extends Model> klass = model.getClass();
      final Field field = klass.getDeclaredField(newFieldName);
      field.setAccessible(true);
      return field.get(model);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException exception) {
      throw new PersistenceException(exception);
    }
  }
}
