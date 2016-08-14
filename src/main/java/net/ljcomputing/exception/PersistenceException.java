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

package net.ljcomputing.exception;

/**
 * Exception representing a persistence exception.
 *
 * @author James G. Willmore
 */
public class PersistenceException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 455963116296709978L;

  /**
   * Instantiates a new persistence exception.
   */
  public PersistenceException() {
    super();
  }

  /**
   * Instantiates a new persistence exception.
   *
   * @param message the message
   */
  public PersistenceException(final String message) {
    super(message);
  }

  /**
   * Instantiates a new persistence exception.
   *
   * @param cause the cause
   */
  public PersistenceException(final Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new persistence exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public PersistenceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new persistence exception.
   *
   * @param message the message
   * @param cause the cause
   * @param suppression the suppression
   * @param stackTrace the stack trace
   */
  public PersistenceException(final String message, final Throwable cause,
      final boolean suppression, final boolean stackTrace) {
    super(message, cause, suppression, stackTrace);
  }
}
