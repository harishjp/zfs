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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Abstract class to around HttpHandler, only forward http GET requests to handleInternal.
 */
abstract class ZFSHttpHandler implements HttpHandler {
    private static final Logger log = Logger.getLogger(ZFSHttpHandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      final String path = httpExchange.getRequestURI().getPath();
      try {
        log.info("Got request for path: " + path);
        try (ZFSExchange exch = new ZFSExchange(httpExchange)) {
          if ("GET".equals(httpExchange.getRequestMethod()) && path.startsWith("/")) {
            handleInternal(path, exch);
          } else {
            log.warning("Invalid request, method: " + httpExchange.getRequestMethod() + ", path: " + path);
            exch.getResponseBody(405, -1);
          }
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error processing request: " + path + " error: " + e.getMessage(), e);
      } finally {
        httpExchange.close();
      }
    }

    abstract void handleInternal(String path, ZFSExchange req) throws IOException;
}
