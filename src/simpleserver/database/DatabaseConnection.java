/*
 * Copyright (c) 2010 SimpleServer authors (see CONTRIBUTORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package simpleserver.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import simpleserver.Server;

public abstract class DatabaseConnection {
  private static final int DEFAULT_TIMEOUT = 10;

  protected final Server server;

  protected Connection connection = null;
  protected Statement statement = null;

  private final String name;

  public DatabaseConnection(String name, Server server) {
    this.name = name;
    this.server = server;
  }

  abstract public void open() throws ClassNotFoundException, SQLException;

  public boolean isValid(int timeout) throws SQLException {
    return connection != null && connection.isValid(timeout) && statement != null && !statement.isClosed();
  }

  public boolean isValid() throws SQLException {
    return isValid(DEFAULT_TIMEOUT);
  }

  public void close() throws SQLException {
    try {
      if (statement != null) {
        statement.close();
      }

      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      statement = null;
      connection = null;
    }
  }

  public synchronized final boolean execute(String sql) throws SQLException {
    if (connection == null || statement == null) {
      throw new SQLException("No open connection.");
    }

    return statement.execute(sql);
  }

  public synchronized final ResultSet executeQuery(String sql) throws SQLException {
    if (connection == null || statement == null) {
      throw new SQLException("No open connection.");
    }

    return statement.executeQuery(sql);
  }

  public String getName() {
    return name;
  }
}
