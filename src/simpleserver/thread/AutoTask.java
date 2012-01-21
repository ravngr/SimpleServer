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

import simpleserver.Server;

public abstract class AutoTask {
  protected static final long MILLISECONDS_PER_MINUTE = 1000 * 60;
  protected static final long MILLISECONDS_PER_HOUR = 1000 * 60 * 60;

  private final String name;

  protected final Server server;

  private final long interval;
  protected long lastRun;

  private Task task;
  private volatile boolean pauseRun = false;
  private volatile boolean forceRun = false;
  private volatile boolean run = true;

  protected AutoTask(Server server, String name, long interval, boolean initialRun) {
    this.server = server;
    this.name = name;
    this.interval = interval;

    if (initialRun) {
      lastRun = 0;
    } else {
      lastRun = System.currentTimeMillis();
    }

    task = new Task();
    task.start();
    task.setName(name);
  }

  public String getName() {
    return name;
  }

  public void forceRun() {
    forceRun = true;
  }

  public void setPaused(boolean pause) {
    pauseRun = pause;
  }

  public boolean isRunning() {
    return run;
  }

  public void stop() {
    run = false;
    task.interrupt();
  }

  protected boolean needsRun() {
    return isEnabled() && nextRun() <= System.currentTimeMillis();
  }

  abstract public boolean isEnabled();

  abstract public long nextRun();

  abstract protected void run();

  private final class Task extends Thread {
    @Override
    public void run() {
      while (run) {
        if (!pauseRun && (forceRun || needsRun())) {
          run();

          forceRun = false;

          lastRun = System.currentTimeMillis();
        }

        try {
          Thread.sleep(interval);
        } catch (InterruptedException e) {
        }
      }
    }
  }
}
