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

import net.ljcomputing.model.Person;
import net.ljcomputing.persistence.DataSourceTable;

/**
 * Test tables enumeration.
 * 
 * @author James G. Willmore
 */
public enum TestTables implements DataSourceTable {
  Person("person", DdlStatements.PERSON_DDL, Person.class);
  
  /** The table name. */
  private String tableName;
  
  /** The ddl. */
  private String ddl;
  
  /** The model class. */
  private Class<?> modelClass;
  
  /**
   * Instantiates a new test tables.
   *
   * @param tableName the table name
   * @param ddl the ddl
   * @param modelClass the model class
   */
  private TestTables(String tableName, String ddl, Class<?> modelClass) {
    this.tableName = tableName;
    this.ddl = ddl;
    this.modelClass = modelClass;
  }

  /**
   * Gets the table name.
   *
   * @return the table name
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Gets the SQL DDL statement.
   *
   * @return the DDL
   */
  public String getDDl() {
    return ddl;
  }

  /**
   * Gets the model.
   *
   * @return the model
   */
  public Class<?> getModel() {
    return modelClass;
  }

}
