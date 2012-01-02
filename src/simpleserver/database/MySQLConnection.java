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
import java.sql.DriverManager;
import java.sql.SQLException;

import simpleserver.Server;

public class MySQLConnection extends DatabaseConnection {
  public MySQLConnection(Server server) {
    super("mysql");
  }

  @Override
  public Connection newConnection(Server server) throws DatabaseConfigurationException, ClassNotFoundException, SQLException {
    Class.forName("com.mysql.jdbc.Driver");

    String dbHost = server.config.properties.get("dbMySQLHostname");
    String dbName = server.config.properties.get("dbMySQLDatabase");
    String dbUsername = server.config.properties.get("dbMySQLUsername");
    String dbPassword = server.config.properties.get("dbMySQLPassword");

    if (dbHost == null) {
      dbHost = "localhost";
    }

    if (dbName == null) {
      throw new DatabaseConfigurationException("Missing MySQL database name");
    }

    if (dbUsername == null && dbPassword == null) {
      return DriverManager.getConnection(String.format("jdbc:mysql://%1$s/%2$s", dbHost, dbName));
    } else if (dbPassword == null) {
      return DriverManager.getConnection(String.format("jdbc:mysql://%1$s/%2$s?user=%3$s", dbHost, dbName, dbUsername));
    } else {
      return DriverManager.getConnection(String.format("jdbc:mysql://%1$s/%2$s?user=%3$s&password=%4$s", dbHost, dbName, dbUsername, dbPassword));
    }
  }
}
