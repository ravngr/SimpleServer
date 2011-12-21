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
package simpleserver.command;

import java.sql.SQLException;

import simpleserver.Color;
import simpleserver.Player;
import simpleserver.Server;
import simpleserver.database.DatabaseConnectionManager;

public class DatabaseCommand extends AbstractCommand implements PlayerCommand,
    ServerCommand {

  public DatabaseCommand() {
    super("db [open|close]", "Manage database connection");
  }

  public void execute(Server server, String message, CommandFeedback feedback) {
    DatabaseConnectionManager database = server.database;

    String arguments[] = extractArguments(message);

    if (arguments.length == 0) {
      feedback.send("Usage: db [open|close|status]");
      return;
    }

    try {
      if (arguments[0].equals("open")) {
        database.open();
        feedback.send("Database connection opened.");
      } else if (arguments[0].equals("close")) {
        database.close();
        feedback.send("Database connection closed.");
      } else if (arguments[0].equals("status")) {
        if (!database.isEnabled()) {
          feedback.send("Database disabled in configuration");
        } else if (database.isValid()) {
          feedback.send("Database connection open");
        } else {
          feedback.send("Database connection closed");
        }
      } else {
        feedback.send("Invalid argument!");
      }
    } catch (ClassNotFoundException e) {
      server.errorLog(e, "Failed to connect to database, missing driver.");
      feedback.send(e.getMessage());
    } catch (SQLException e) {
      server.errorLog(e, "Database error, unknown server excption.");
      feedback.send(e.getMessage());
    }
  }

  public void execute(Player player, String message) {
    Server server = player.getServer();

    DatabaseConnectionManager database = server.database;

    String arguments[] = extractArguments(message);

    if (arguments.length == 0) {
      player.addTCaptionedMessage("Usage", commandPrefix() + "db [open|close|status]");
      return;
    }

    try {
      if (arguments[0].equals("open")) {
        database.open();
        player.addTMessage("Database connection opened.");
      } else if (arguments[0].equals("close")) {
        database.close();
        player.addTMessage("Database connection closed.");
      } else if (arguments[0].equals("status")) {
        if (!database.isEnabled()) {
          player.addTMessage(Color.RED, "Database disabled");
        } else if (database.isValid()) {
          player.addTMessage(Color.GREEN, "Database connection open");
        } else {
          player.addTMessage(Color.RED, "Database connection closed");
        }
      } else {
        player.addTMessage(Color.RED, "Invalid argument!");
      }
    } catch (ClassNotFoundException e) {
      server.errorLog(e, "Failed to connect to database, missing driver.");
      player.addTMessage(Color.RED, e.getMessage());
    } catch (SQLException e) {
      server.errorLog(e, "Database error, unknown server excption.");
      player.addTMessage(Color.RED, e.getMessage());
    }
  }
}
