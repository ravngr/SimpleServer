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

public class AutoRun {
  private static final long MILLISECONDS_PER_MINUTE = 1000 * 60;

  private final Server server;
  private final String title;
  private final String propertyEnable;
  private final String propertyAnnounce;
  private final String propertyCommand;
  private final String propertyInterval;
  private final Runner runner;

  private long lastRun;
  private volatile boolean run = true;

  public AutoRun(Server server, String title, String propertyEnable, String propertyAnnounce, String propertyCommand, String propertyInterval) {
    this.server = server;
    this.title = title;
    this.propertyEnable = propertyEnable;
    this.propertyAnnounce = propertyAnnounce;
    this.propertyCommand = propertyCommand;
    this.propertyInterval = propertyInterval;

    lastRun = System.currentTimeMillis();

    runner = new Runner();
    runner.start();
    runner.setName("AutoRun");
  }

  public void stop() {
    run = false;
    runner.interrupt();
  }

  private boolean needsRun() {
    long maxAge = System.currentTimeMillis() - MILLISECONDS_PER_MINUTE
        * server.config.properties.getInt(propertyInterval);
    return server.config.properties.getBoolean(propertyEnable) && maxAge > lastRun;
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

  public void announce(String message) {
    if (server.config.properties.getBoolean(propertyAnnounce)) {
      server.runCommand("say", message);
    }
  }

  private final class Runner extends Thread {
    @Override
    public void run() {
      while (run) {
        if (needsRun()) {
          try {
            server.saveLock.acquire();
          } catch (InterruptedException e) {
            continue;
          }

          announce(t(title + " started!"));

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
              process = Runtime.getRuntime().exec(server.config.properties.get(propertyCommand));
            } catch (IOException e) {
              announce(t(title + " failed!"));
              System.out.println("[SimpleServer] " + e);
              System.out.println("[SimpleServer] Cron Failed! Bad Command!");
              server.errorLog(e, "AutoRun Failure");
              continue;
            }

            new OutputConsumer(process.getInputStream()).start();
            new OutputConsumer(process.getErrorStream()).start();

            int exitCode;
            while (true) {
              try {
                exitCode = process.waitFor();
                break;
              } catch (InterruptedException e) {
                if (!run) {
                  process.destroy();
                }
              }
            }

            if (exitCode < 0) {
              System.out.println("[SimpleServer] " + title + " Failed! Exited with code "
                  + exitCode + "!");
              announce(t(title + " failed!"));
            } else {
              announce(t(title + " complete!"));
            }
          } finally {
            server.runCommand("save-on", null);
            server.saveLock.release();
          }
        }

        try {
          Thread.sleep(60000);
        } catch (InterruptedException e) {
        }
      }
    }
  }
}
