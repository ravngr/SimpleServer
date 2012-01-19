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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import simpleserver.Server;

public class DatabaseConnectionManager {
  private final Server server;

  private Map<String, DatabaseConnection> connectors;

  public DatabaseConnectionManager(Server server) {
    this.server = server;

    reloadConnectors();
  }

  public void reloadConnectors() {
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

  public Connection getConnector() throws ClassNotFoundException, DatabaseConfigurationException, SQLException {
    String connector = server.config.properties.get("dbConnector");

    if (connector == null) {
      throw new DatabaseConfigurationException("Missing dbConnector from configuration");
    }

    DatabaseConnection c = connectors.get(connector);

    if (c == null) {
      throw new DatabaseConfigurationException("Specified database connector " + connector + " was not found");
    }

    return c.newConnection(server);
  }
}
