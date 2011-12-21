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

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import simpleserver.Server;

public class DatabaseConnectionManager {
  private final Server server;

  private final Map<String, DatabaseConnection> connectors;

  public DatabaseConnection connection;

  public DatabaseConnectionManager(Server server) {
    this.server = server;

    connectors = new HashMap<String, DatabaseConnection>();

    Reflections r = new Reflections("simpleserver", new SubTypesScanner());
    Set<Class<? extends DatabaseConnection>> types = r.getSubTypesOf(DatabaseConnection.class);

    for (Class<? extends DatabaseConnection> connectorType : types) {
      if (Modifier.isAbstract(connectorType.getModifiers())) {
        continue;
      }

      DatabaseConnection connector;

      try {
        connector = connectorType.getConstructor(new Class[] { Server.class }).newInstance(new Object[] { server });
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Unexpected exception. Skipping connector "
            + connectorType.getName());
        continue;
      }

      connectors.put(connector.getName(), connector);
    }
  }

  public void open() throws ClassNotFoundException, SQLException {
    if (!server.config.properties.getBoolean("dbEnabled")) {
      // Force closure when disabled
      try {
        close();
      } catch (SQLException e) {

      }

      throw new SQLException("Database disabled in configuration");
    }

    boolean connect = false;

    try {
      if (connection == null) {
        connect = true;
      } else if (!connection.isValid(30)) {
        connect = true;
        connection.close();
      }
    } catch (SQLException e) {
      // Connection error on checking validity
      connect = true;
      connection.close();
    }

    if (connect) {
      connection = connectors.get(server.config.properties.get("dbConnector"));

      if (connection == null) {
        throw new SQLException("Invalid or missing database connector");
      }
    }

    connection.open();
  }

  public void close() throws SQLException {
    if (connection == null) {
      return;
    }

    connection.close();
    connection = null;
  }

  public boolean isEnabled() {
    return server.config.properties.getBoolean("dbEnabled");
  }

  public boolean isValid() throws SQLException {
    if (connection == null) {
      return false;
    }

    return connection.isValid();
  }
}
