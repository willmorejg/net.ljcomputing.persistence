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

import net.ljcomputing.StringUtils;
import net.ljcomputing.exception.PersistenceException;
import net.ljcomputing.persistence.EntityPopulator;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Implementation of an entity populator, which populates entities from a result
 * set.
 * 
 * @author James G. Willmore
 *
 */
public class EntityPopulatorImpl implements EntityPopulator {

  /**
   * @see net.ljcomputing.persistence.EntityPopulator#populate(java.lang.Object, java.sql.ResultSet)
   */
  public void populate(final Object entity, final ResultSet resultSet)
      throws PersistenceException {
    try {
      final int columnCount = getResultSetMetaData(resultSet).getColumnCount()
          + 1;

      for (int column = 1; column < columnCount; column++) {
        populateMember(entity, resultSet, column);
      }
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Gets the result set meta data.
   *
   * @param resultSet the result set
   * @return the result set meta data
   * @throws PersistenceException the persistence exception
   */
  private ResultSetMetaData getResultSetMetaData(final ResultSet resultSet)
      throws PersistenceException {
    try {
      return resultSet.getMetaData();
    } catch (SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Populate the entity member.
   *
   * @param entity the entity
   * @param resultSet the result set
   * @param column the column
   * @throws PersistenceException the persistence exception
   */
  private void populateMember(final Object entity, final ResultSet resultSet,
      final int column) throws PersistenceException {
    try {
      final ResultSetMetaData rsmd = getResultSetMetaData(resultSet);
      final String columnName = rsmd.getColumnName(column);
      final String fieldName = StringUtils.toMemberCase(columnName);
      final Class<?> entityClass = entity.getClass();
      final Field member = getEntityMember(entityClass, fieldName);

      member.setAccessible(true);
      member.set(entity, resultSet.getObject(column));
    } catch (SecurityException | IllegalArgumentException
        | IllegalAccessException | SQLException exception) {
      throw new PersistenceException(exception);
    }
  }

  /**
   * Gets the entity member.
   *
   * @param entityClass the entity class
   * @param columnMember the column member
   * @return the entity member
   * @throws PersistenceException the persistence exception
   */
  private Field getEntityMember(final Class<?> entityClass,
      final String columnMember) throws PersistenceException {
    Field field = null;

    for (int f = 0; f < entityClass.getSuperclass()
        .getDeclaredFields().length; f++) {
      if (null != field) {
        break;
      }

      if (columnMember.equals(
          entityClass.getSuperclass().getDeclaredFields()[f].getName())) {
        field = entityClass.getSuperclass().getDeclaredFields()[f];
      }
    }

    for (int f = 0; f < entityClass.getDeclaredFields().length; f++) {
      if (null != field) {
        break;
      }

      if (columnMember.equals(entityClass.getDeclaredFields()[f].getName())) {
        field = entityClass.getDeclaredFields()[f];
      }
    }

    return field;
  }
}
