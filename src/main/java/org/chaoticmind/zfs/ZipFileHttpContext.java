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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A HttpHandler to handle requests for a zip file.
 */
class ZipFileHttpHandler extends ZFSHttpHandler implements Closeable {
  private static final Logger log = Logger.getLogger(ZipFileHttpHandler.class.getName());
  private final ZipFile zipFile;

  ZipFileHttpHandler(String file) throws IOException {
    this.zipFile = new ZipFile(file);
  }

  @Override
  public void handleInternal(String path, ZFSExchange req) throws IOException {
    int ind = path.indexOf('/', 1);
    path = ind != -1 ? path.substring(ind + 1) : "";
    log.fine("Processing zipfile path: " + path);
    if (path.isEmpty() || path.endsWith("/")) {
      listDirectory(path, req.getResponseBody(200, 0));
    } else {
      ZipEntry ze = zipFile.getEntry(path);
      if (ze == null) {
        return;
      } else if (ze.isDirectory()) {
        listDirectory(path, req.getResponseBody(200, 0));
      } else {
        copyFile(ze, req.getResponseBody(200, 0));
      }
    }
  }

  private void writeHtmlEscaped(Writer writer, String str) throws IOException {
    for (char ch : str.toCharArray()) {
      switch (ch) {
        case '<': writer.write("&lt;"); break;
        case '>': writer.write("&gt;"); break;
        case '"': writer.write("&quot;"); break;
        case '&': writer.write("&amp;"); break;
        case '\'': writer.write("&#39;"); break;
        default: writer.write(ch);
      }
    }
  }

  private void copyFile(ZipEntry entry, OutputStream out) throws IOException {
    try (InputStream is = zipFile.getInputStream(entry)) {
      is.transferTo(out);
    }
  }

  private void listDirectory(String dir, OutputStream out) throws IOException {
    int pLen = dir.length();
    TreeSet<String> links = new TreeSet<>();
    for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
      ZipEntry ze = e.nextElement();
      String zname = ze.getName();
      if (zname.startsWith(dir)) {
        int sIndex = zname.indexOf('/', pLen);
        String link = zname.substring(pLen, sIndex == -1 ? zname.length() : sIndex + 1);
        links.add(link);
      }
    }
    Writer writer =  new OutputStreamWriter(out);
    writer.write("<html><head><title>Directory List</title></head><body><hr>\n");
    writer.write("<a href='..'>..</a><br>\n");
    for (String link : links) {
      writer.write("<a href='./");
      writeHtmlEscaped(writer, link);
      writer.write("'>");
      writeHtmlEscaped(writer, link);
      writer.write("</a><br>\n");
    }
    if (links.isEmpty()) {
      writer.write("ZipFile does not have any contents in directory");
    }
    writer.write("<hr></body></html>");
    writer.flush();
  }

  @Override
  public void close() throws IOException {
    zipFile.close();
  }
}
