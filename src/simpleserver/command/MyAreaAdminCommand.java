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

import org.xml.sax.SAXException;

import simpleserver.Color;
import simpleserver.Player;
import simpleserver.config.xml.Chests;
import simpleserver.config.xml.Config;
import simpleserver.config.xml.Config.AreaStoragePair;
import simpleserver.config.xml.Permission;

public class MyAreaAdminCommand extends AbstractCommand implements PlayerCommand {
  public MyAreaAdminCommand() {
    super("myareaadmin [+|-][place|destroy|use|chest] [PLAYER]", "Change permissions on a myarea");
  }

  public void execute(Player player, String message) {
    Config config = player.getServer().config;
    String arguments[] = extractArguments(message);

    if (arguments.length == 0 || arguments.length > 2) {
      player.addTCaptionedMessage("Usage", commandPrefix() + "myareaadmin [-][place|destroy|use|chest] [PLAYER]");
      return;
    }

    AreaStoragePair area = config.playerArea(player);
    if (area == null) {
      player.addTMessage(Color.RED, "You currently have no personal area which can be managed!");
      return;
    }

    if (!(arguments[0].contains("place") || arguments[0].contains("destroy")
        || arguments[0].contains("use") || arguments[0].contains("chest"))) {
      player.addTMessage(Color.RED, "Invalid premission specified");
      return;
    }

    String targetPlayer = null;

    if (arguments.length == 2) {
      targetPlayer = arguments[1].toLowerCase();

      if (targetPlayer.equalsIgnoreCase(player.getName())) {
        player.addTMessage(Color.RED, "You already have all permissions");
        return;
      }
    }

    Permission permission = null;
    String permissionString = null;

    if (targetPlayer == null) {
      if (arguments[0].startsWith("-")) {
        // Forbid globally (allow only player)
        permission = new Permission(player);
      }
    } else {
      if (arguments[0].contains("place")) {
        permission = area.area.allblocks.blocks.place;
      } else if (arguments[0].contains("destroy")) {
        permission = area.area.allblocks.blocks.destroy;
      } else if (arguments[0].contains("use")) {
        permission = area.area.allblocks.blocks.use;
      } else if (arguments[0].contains("chest") && area.area.chests.chests != null) {

      }

      if (arguments[0].startsWith("-") && permission != null) {
        permissionString = permission.toString().replace(targetPlayer, "");
      } else {
        permissionString = permission.toString() + "," + targetPlayer.toLowerCase();
      }
    }

    if (permissionString != null) {
      if (permissionString.equalsIgnoreCase(permission.toString())) {
        player.addTMessage(Color.RED, "No changes made");
        return;
      }

      try {
        permission = new Permission(permissionString);
      } catch (SAXException e) {
        player.getServer().errorLog(e, "Error during addition to permissions");
        player.addTMessage(Color.RED, "Unexpected error");
        return;
      }
    }

    if (arguments[0].contains("place")) {
      area.area.allblocks.blocks.place = permission;
    } else if (arguments[0].contains("destroy")) {
      area.area.allblocks.blocks.destroy = permission;
    } else if (arguments[0].contains("use")) {
      area.area.allblocks.blocks.use = permission;
    } else if (arguments[0].contains("chest")) {
      if (permission == null) {
        area.area.chests.chests = null;
      } else {
        area.area.chests.chests = new Chests(permission);
      }
    }

    player.addTMessage(Color.GRAY, "Permissions updated!");

    player.getServer().saveConfig();
  }
}
