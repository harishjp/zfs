/*
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
*/
package org.chaoticmind.zfs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpServer;

/**
 * The ZipFileServer class, it uses an internal HttpServer component of Java to
 * serve requests. This may not work on other JVMs, and may not be production
 * ready. This was meant to be used by an end user, hence frontended by a GUI.
 */
class ZipFileServer {
  private static final Logger log = Logger.getLogger(ZipFileServer.class.getName());
  private final HashMap<String, ZipFileHttpHandler> handlers = new HashMap<>();
  private final ZFSHttpHandler rootHandler = new ZFSHttpHandler() {
    @Override
    public void handleInternal(String path, ZFSExchange req) throws IOException {
      if (path.equals("/")) {
        OutputStreamWriter writer = new OutputStreamWriter(req.getResponseBody(200, 0));
        try {
          writer.write("<html><head><title>List of archives on ZipFileServer</title></head><body><hr>\n");
          if (handlers.isEmpty()) {
            writer.write("No files on ZipFileServer");
          }
          for (String key : handlers.keySet()) {
            writer.write("<a href='" + key + "/'>" + key + "</a><br>\n");
          }
          writer.write("<hr></body></html>");
        } finally {
          writer.flush();
        }
      }
    }
  };

  private HttpServer server;

  void start(int port) throws IOException {
    if (server == null) {
      log.info("Starting server.");
      server = HttpServer.create(new InetSocketAddress(port), 5);
      server.createContext("/", rootHandler);
      for (Entry<String, ZipFileHttpHandler> entry : handlers.entrySet()) {
        server.createContext("/" + entry.getKey(), entry.getValue());
      }
      server.setExecutor(Executors.newCachedThreadPool());
      server.start();
      log.info("Started server.");
    }
  }

  void stop() {
    if (server != null) {
      log.info("Stopping server.");
      server.stop(2);
      server = null;
      log.info("Stopped server.");
    }
  }

  boolean addFile(String name, String filePath) throws IOException {
    if (!handlers.containsKey(name)) {
      ZipFileHttpHandler handler = new ZipFileHttpHandler(filePath);
      handlers.put(name, handler);
      if (server != null) {
        server.createContext("/" + name + "/", handler);
      }
      return true;
    }
    return false;
  }

  void removeFile(String name) throws IOException {
    ZipFileHttpHandler handler = handlers.remove(name);
    if (handler != null) {
      if (server != null) {
        server.removeContext("/" + name);
      }
      handler.close();
    }
  }
}
