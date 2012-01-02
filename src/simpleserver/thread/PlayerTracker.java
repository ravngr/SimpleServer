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
package simpleserver.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import simpleserver.Coordinate.Dimension;
import simpleserver.Player;
import simpleserver.Server;
import simpleserver.database.DatabaseConfigurationException;

public class PlayerTracker {
  private static final String CLEAR_QUERY = "TRUNCATE player_tracker";
  private static final String INSERT_QUERY = "INSERT INTO player_tracker VALUES (?, ?, ?, ?, ?, ?, ?)";

  private final Server server;
  private final Tracker tracker;

  private long lastUpdate;
  private volatile boolean run = true;

  public PlayerTracker(Server server) {
    this.server = server;

    lastUpdate = 0;

    tracker = new Tracker();
    tracker.start();
    tracker.setName("PlayerTracker");
  }

  public void stop() {
    run = false;
    tracker.interrupt();
  }

  private boolean needsUpdate() {
    long maxAge = System.currentTimeMillis() - 1000 * server.config.properties.getInt("playerTrackerSecs");
    return server.config.properties.getBoolean("playerTracker") && maxAge > lastUpdate;
  }

  private void logError(Exception e, String message) {
    server.errorLog(e, message);
    System.out.println("[SimpleServer] " + e);
    System.out.println("[SimpleServer] " + message);
  }

  private final class Tracker extends Thread {
    @Override
    public void run() {
      while (run) {
        if (needsUpdate()) {

          Connection databaseConnection = null;

          try {
            databaseConnection = server.databaseManager.getConnector();

            PreparedStatement statementClear = databaseConnection.prepareStatement(CLEAR_QUERY);
            statementClear.executeUpdate();

            PreparedStatement statementInsert = databaseConnection.prepareStatement(INSERT_QUERY);

            for (Player player : server.playerList.getArray()) {
              if (player.getDimension().equals(Dimension.EARTH) && player.getDimension().equals(Dimension.NETHER)) {
                continue;
              }

              // Clamp y position, boats/carts make y value invalid
              int y = player.position().y();

              if (y < 0) {
                y = 0;
              }

              statementInsert.setString(1, player.getName());
              statementInsert.setString(2, player.getGroup().name);
              statementInsert.setInt(3, player.getDimension().index());
              statementInsert.setInt(4, player.position().x());
              statementInsert.setInt(5, y);
              statementInsert.setInt(6, player.position().z());
              statementInsert.setBoolean(7, player.isHidden());

              statementInsert.executeUpdate();
            }
          } catch (ClassNotFoundException e) {
            logError(e, "Failed to load database driver");
          } catch (DatabaseConfigurationException e) {
            logError(e, "Database configuration invalid or missing");
          } catch (SQLException e) {
            logError(e, "Database update failed");
          } finally {
            if (databaseConnection != null) {
              try {
                databaseConnection.close();
              } catch (SQLException e) {
                logError(e, "Failed to properly close database connection");
              }
            }
          }

          lastUpdate = System.currentTimeMillis();
        }

        try {
          Thread.sleep(60000);
        } catch (InterruptedException e) {
        }
      }
    }
  }
}
