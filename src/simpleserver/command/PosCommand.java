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

import java.io.IOException;

import simpleserver.Color;
import simpleserver.Coordinate;
import simpleserver.Player;
import simpleserver.bot.BotController.ConnectException;

public class PosCommand extends AbstractCommand implements PlayerCommand {
  public PosCommand() {
    super("pos X Y Z", "Teleport to specified position");
  }

  public void execute(Player player, String message) {
    String arguments[] = extractArguments(message);

    if (arguments.length != 3) {
      player.addTCaptionedMessage("Usage", commandPrefix() + "pos X Y Z");
      return;
    }

    try {
      int x = Integer.parseInt(arguments[0]);
      int y = Integer.parseInt(arguments[1]);
      int z = Integer.parseInt(arguments[2]);

      Coordinate target = new Coordinate(x, y, z);
      player.teleport(target);

      player.addTMessage(Color.GRAY, "Teleporting to %d, %d, %d", x, y, z);
    } catch (NumberFormatException e) {
      player.getServer().errorLog(e, "Failed to parse input values " + arguments.toString());
      player.addTMessage(Color.RED, "Arguments must be whole numbers!");
      return;
    } catch (ConnectException e) {
      player.getServer().errorLog(e, "Failed to create teleport bot");
      player.addTMessage(Color.RED, "Unexpected error!");
      return;
    } catch (IOException e) {
      player.getServer().errorLog(e, "Failed to create teleport bot");
      player.addTMessage(Color.RED, "Unexpected error!");
      return;
    }
  }
}
