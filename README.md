ZipFileServer

An http server to serve contents of zip file. The project was meant to be very minimal and not use
libraries outside the JVM. It does use HttpServer from the java library which is restricted and may
not be available on JVMs. This server is not meant to be production quality, but just for users to
be able to read contents of a zipfile without having to extract it. It is especially useful to read
html documentation from a zip file without exploding the zipfile.
