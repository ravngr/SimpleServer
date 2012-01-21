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

import static simpleserver.lang.Translations.t;

import java.io.IOException;
import java.io.InputStream;

import simpleserver.Server;

public class AutoMap extends AutoTask {
  public AutoMap(Server server) {
    super(server, "AutoMap", 1000, false);
  }

  @Override
  public long nextRun() {
    return lastRun + MILLISECONDS_PER_MINUTE * server.config.properties.getInt("autoMapMins");
  }

  public void announce(String message) {
    if (server.config.properties.getBoolean("announceMap")) {
      server.runCommand("say", message);
    }
  }

  @Override
  protected void run() {
    try {
      server.saveLock.acquire();
    } catch (InterruptedException e) {
      return;
    }

    announce(t("Mapping started!"));

    server.runCommand("save-all", null);

    while (server.isSaving()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }

    server.runCommand("save-off", null);

    lastRun = System.currentTimeMillis();
    try {
      Process process;
      try {
        process = Runtime.getRuntime().exec(server.config.properties.get("autoMapCmd"));
      } catch (IOException e) {
        announce(t("Mapping failed!"));
        System.out.println("[SimpleServer] " + e);
        System.out.println("[SimpleServer] Cron Failed! Bad Command!");
        server.errorLog(e, "AutoRun Failure");
        return;
      }

      new OutputConsumer(process.getInputStream()).start();
      new OutputConsumer(process.getErrorStream()).start();

      int exitCode;
      while (true) {
        try {
          exitCode = process.waitFor();
          break;
        } catch (InterruptedException e) {
          if (!isRunning()) {
            process.destroy();
          }
        }
      }

      if (exitCode < 0) {
        System.out.println("[SimpleServer] Mapping Failed! Exited with code "
            + exitCode + "!");
        announce(t("Mapping failed!"));
      } else {
        announce(t("Mapping complete!"));
      }
    } finally {
      server.runCommand("save-on", null);
      server.saveLock.release();
    }
  }

  private static final class OutputConsumer extends Thread {
    private final InputStream in;

    private OutputConsumer(InputStream in) {
      this.in = in;
    }

    @Override
    public void run() {
      byte[] buf = new byte[256];
      try {
        while (in.read(buf) >= 0) {
        }
      } catch (IOException e) {
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return server.config.properties.getBoolean("autoMap");
  }
}
