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

package net.ljcomputing.persistence;

import net.ljcomputing.exception.PersistenceException;

import java.sql.ResultSet;

/**
 * Interface to an entity.
 * 
 * @author James G. Willmore
 *
 */
public interface Entity {

  /**
   * Populate.
   *
   * @param entityPopulator the entity populator
   * @param resultSet the result set
   * @throws PersistenceException the persistence exception
   */
  void populate(EntityPopulator entityPopulator, ResultSet resultSet)
      throws PersistenceException;
}
