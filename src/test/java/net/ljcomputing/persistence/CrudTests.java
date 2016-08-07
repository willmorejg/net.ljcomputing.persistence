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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.ljcomputing.exception.PersistenceException;
import net.ljcomputing.model.Person;
import net.ljcomputing.persistence.impl.ConnectionPool;
import net.ljcomputing.repository.impl.ModelRepository;
import net.ljcomputing.repository.impl.PersonRepositoryImpl;
import net.ljcomputing.repository.impl.TestTables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Persistence JUnit CRUD tests.
 * 
 * @author James G. Willmore
 */
public class CrudTests {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(CrudTests.class);

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ConnectionPool cp = ConnectionPool.getInstance();

    try {
      LOGGER.debug(" ... initialize tables");
      
      initTables(cp.getConnection());
      
      LOGGER.debug("COMPLETED ... initialize tables");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Inits the tables.
   *
   * @param conn the conn
   * @throws Exception the exception
   */
  private static void initTables(Connection conn) throws Exception {
    for (DataSourceTable table : TestTables.values()) {
      if (!exists(conn, table)) {
        LOGGER.debug(" ... table " + table.getTableName()
            + " does not exist ... creating");
        
        createTable(conn, table);
        
        LOGGER.debug("CREATED ... " + table.getTableName());
      }
    }

    LOGGER.debug("COMPLETED ... adding items");
  }

  /**
   * Exists.
   *
   * @param conn the conn
   * @param table the table
   * @return true, if successful
   * @throws Exception the exception
   */
  private static boolean exists(Connection conn, DataSourceTable table)
      throws Exception {
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt
          .executeQuery("select * from " + table.getTableName() + " where 1=0");

      rs.close();
      stmt.close();
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * Creates the table.
   *
   * @param conn the conn
   * @param table the table
   * @throws Exception the exception
   */
  private static void createTable(Connection conn, DataSourceTable table)
      throws Exception {
    Statement stmt = conn.createStatement();
    stmt.executeUpdate(table.getDDl());
    stmt.close();
  }
  
  /**
   * Gets a list of people used in the tests.
   *
   * @return the people
   */
  private static List<Person> getPeople() {
    Person person1 = new Person("alice");
    Person person2 = new Person("bob");
    Person person3 = new Person("charlie");
    Person person4 = new Person("david");
    Person person5 = new Person("eve");
    
    List<Person> people = new ArrayList<Person>();
    
    people.add(person1);
    people.add(person2);
    people.add(person3);
    people.add(person4);
    people.add(person5);
    
    return people;
  }

  /**
   * Test CRUD functionality.
   */
  @Test
  public void test() {
    List<Person> people = getPeople();
    
    try {
      ModelRepository<Person> repository = new PersonRepositoryImpl();

      for(Person person : people) {
        repository.create(person, "name");
        Person createdPerson = repository.readById(person.getId());
        assertNotNull(person.getId());
        LOGGER.info("created: {}", createdPerson);
      }

      for(Person person : people) {
        Person newPerson = new Person(person.getId(), person.getName().toUpperCase());
        repository.update(newPerson, "name");
      }

      for(Person person : repository.readAll()) {
        LOGGER.info("updated: {}", person.toString());
      }

      for(Person person : people) {
        repository.delete(person);
        assertNull(repository.readById(person.getId()));
      }

      //this should only log orphaned records
      //if the previous tests were successful, 
      //and no other records exists, nothing will
      //be logged
      for(Person person : repository.readAll()) {
        LOGGER.info("found: {}", person.toString());
      }
      
    } catch (PersistenceException e) {
      LOGGER.error("Repository test failure: ", e);
      fail("Repository tests FAILED");
    }
    
    assertTrue(true);
  }

}
