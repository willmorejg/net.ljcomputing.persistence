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

import net.ljcomputing.exception.PersistenceException;
import net.ljcomputing.model.Model;

import java.util.List;

/**
 * Model repository interface.
 *
 * @author James G. Willmore
 * @param <T> the Model associated with the repository
 */
public interface ModelRepository<T extends Model> {

  /**
   * Creates the given model.
   *
   * @param model the model
   * @param columns the columns
   * @throws PersistenceException the persistence exception
   */
  void create(T model, String... columns) throws PersistenceException;

  /**
   * Update the given model.
   *
   * @param model the model
   * @param columns the columns
   * @throws PersistenceException the persistence exception
   */
  void update(T model, String... columns) throws PersistenceException;

  /**
   * Delete the model with the given id.
   *
   * @param id the id
   * @throws PersistenceException the persistence exception
   */
  void delete(Integer id) throws PersistenceException;

  /**
   * Delete the given model.
   *
   * @param model the model
   * @throws PersistenceException the persistence exception
   */
  void delete(T model) throws PersistenceException;

  /**
   * Read the model by id.
   *
   * @param id the id
   * @return the t
   * @throws PersistenceException the persistence exception
   */
  T readById(Integer id) throws PersistenceException;

  /**
   * Read all models.
   *
   * @return the list
   * @throws PersistenceException the persistence exception
   */
  List<T> readAll() throws PersistenceException;
}
