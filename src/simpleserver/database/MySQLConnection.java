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

import java.sql.DriverManager;
import java.sql.SQLException;

import simpleserver.Server;

public class MySQLConnection extends DatabaseConnection {
  private final Server server;

  public MySQLConnection(Server server) {
    this.server = server;
  }

  @Override
  public void open() throws ClassNotFoundException, SQLException {
    Class.forName("com.mysql.jdbc.Driver");

    String dbHost = server.config.properties.get("dbHost");
    String dbName = server.config.properties.get("dbName");
    String dbUsername = server.config.properties.get("dbUsername");
    String dbPassword = server.config.properties.get("dbPassword");

    if (dbHost == null || dbName == null || dbUsername == null || dbPassword == null) {
      throw new SQLException("Missing database connection configuration");
    }

    connection = DriverManager.getConnection(String.format("jdbc:mysql://%1$s/%2$s?user=%3$s&password=%4$s", dbHost, dbName, dbUsername, dbPassword));

    statement = connection.createStatement();
  }

  @Override
  public void close() {
    try {
      if (statement != null) {
        statement.close();
      }

      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {

    }
  }
}
