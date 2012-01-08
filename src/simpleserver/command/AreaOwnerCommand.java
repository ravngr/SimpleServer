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

import java.util.List;

import simpleserver.Color;
import simpleserver.Player;
import simpleserver.config.xml.Area;

public class AreaOwnerCommand extends AbstractCommand implements PlayerCommand {
  public AreaOwnerCommand() {
    super("areaowner [PLAYER]", "Change the owner of the current area");
  }

  public void execute(Player player, String message) {
    String arguments[] = extractArguments(message);

    if (arguments.length > 1) {
      player.addTCaptionedMessage("Usage", commandPrefix() + "areaowner [PLAYER]");
      return;
    }

    List<Area> areas = player.getServer().config.dimensions.areas(player.position());

    if (areas.isEmpty()) {
      player.addTMessage(Color.RED, "You are currently in no areas");
      return;
    }

    Area childArea = areas.get(areas.size() - 1);

    if (arguments.length == 1) {
      childArea.owner = arguments[0];
    } else {
      childArea.owner = null;
    }

    player.getServer().saveConfig();
  }
}
