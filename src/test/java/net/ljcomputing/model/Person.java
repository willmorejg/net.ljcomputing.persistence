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

package net.ljcomputing.model;

import net.ljcomputing.exception.PersistenceException;
import net.ljcomputing.persistence.Entity;
import net.ljcomputing.persistence.EntityPopulator;

import java.sql.ResultSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Person model.
 * 
 * @author James G. Willmore
 */
public class Person extends AbstractModel implements Model, Entity {

  /** The name. */
  private String name;

  /**
   * Instantiates a new person.
   *
   * @param name the name
   */
  public Person(String name) {
    this(null, name);
  }

  /**
   * Instantiates a new person.
   *
   * @param id the id
   * @param name the name
   */
  public Person(Integer id, String name) {
    setId(id);
    this.name = name;
  }

  /**
   * Instantiates a new person.
   *
   * @param ep the ep
   * @param rs the rs
   * @throws PersistenceException the persistence exception
   */
  public Person(EntityPopulator ep, ResultSet rs) throws PersistenceException {
    populate(ep, rs);
  }

  /**
   * @see net.ljcomputing.persistence.Entity#populate(net.ljcomputing.persistence.EntityPopulator, java.sql.ResultSet)
   */
  public void populate(EntityPopulator ep, ResultSet rs)
      throws PersistenceException {
    ep.populate(this, rs);
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this,
        ToStringStyle.MULTI_LINE_STYLE);
  }
}
