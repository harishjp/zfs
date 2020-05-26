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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

/**
 * Class to enscapsulate a request to the http server.
 * If a response body was accessed then it will setup the headers
 * and return the underlying OutputStream. The response status cannot
 * be changed once headers are accessed and will be ignored.
 * If it is closed without accessing a response it will return 404.
 */
class ZFSExchange implements Closeable {
  private final HttpExchange exchange;
  private boolean headersSent = false;

  ZFSExchange(HttpExchange exchange) {
    this.exchange = exchange;
  }

  public OutputStream getResponseBody(int code, int len) throws IOException {
    if (!headersSent) {
      headersSent = true;
      exchange.sendResponseHeaders(code, len);
    }
    return exchange.getResponseBody();
  }

  public void close() throws IOException {
    getResponseBody(404, -1).close();
  }
}