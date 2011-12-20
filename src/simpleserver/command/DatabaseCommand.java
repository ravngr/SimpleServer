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
import simpleserver.database.DatabaseConnection;

public class DatabaseCommand extends AbstractCommand implements PlayerCommand,
    ServerCommand {

  protected DatabaseCommand() {
    super("db [open|close]", "Manage database connection");
  }

  public void execute(Server server, String message, CommandFeedback feedback) {
    DatabaseConnection connection = server.database;

    String arguments[] = extractArguments(message);

    if (arguments.length == 0) {
      feedback.send("Usage: db [open|close]");
      return;
    }

    if (arguments[0].equals("open")) {
      try {
        connection.open();
        feedback.send("Database connection opened.");
      } catch (ClassNotFoundException e) {
        server.errorLog(e, "Failed to open database connection, missing driver.");
        feedback.send("Failed to open database connection!");
      } catch (SQLException e) {
        server.errorLog(e, "Failed to open database connection, server error.");
        feedback.send("Failed to open database connection!");
      }
    } else if (arguments[0].equals("close")) {
      try {
        connection.close();
      } catch (SQLException e) {
        server.errorLog(e, "Failed to close database connection, server error.");
        feedback.send("Failed to close database connection!");
      }
    } else {
      feedback.send("Invalid argument!");
    }
  }

  public void execute(Player player, String message) {
    Server server = player.getServer();

    DatabaseConnection connection = server.database;

    String arguments[] = extractArguments(message);

    if (arguments.length == 0) {
      player.addTCaptionedMessage("Usage", commandPrefix() + "db [open|close]");
      return;
    }

    if (arguments[0].equals("open")) {
      try {
        connection.open();
        player.addTMessage(Color.RED, "Database connection opened.");
      } catch (ClassNotFoundException e) {
        server.errorLog(e, "Failed to open database connection, missing driver.");
        player.addTMessage(Color.RED, "Failed to open database connection!");
      } catch (SQLException e) {
        server.errorLog(e, "Failed to open database connection, server error.");
        player.addTMessage(Color.RED, "Failed to open database connection!");
      }
    } else if (arguments[0].equals("close")) {
      try {
        connection.close();
      } catch (SQLException e) {
        server.errorLog(e, "Failed to close database connection, server error.");
        player.addTMessage(Color.RED, "Failed to close database connection!");
      }
    } else {
      player.addTMessage(Color.RED, "Invalid argument!");
    }
  }
}
