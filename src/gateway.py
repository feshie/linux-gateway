#Derived from https://wiki.python.org/moin/BaseHttpServer#CA-86c92a7a96bb671a2cb7471059da597b6df2666f_1

import time
import BaseHTTPServer
import CGIHTTPServer
import Queue
import io
import urllib2
import thread
import socket

HOST_NAME = '0:0:0:0:0:0:0:1' # Change this!
PORT_NUMBER = 8080 # Change this!

NEXT_SERVER = 'http://localhost:9000/upload' # The address of the server after the proxy. Change this too!

class MyHandler(CGIHTTPServer.CGIHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()
    def do_GET(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()
        s.wfile.write("<html><head><title>%s</title></head>" % (s.path))
        s.wfile.write("<body><p>PUT/POST Proxy</p>")

    def do_POST(s):
        #queue.put(s.rfile.read(int(s.headers.getheader('content-length'))))
        s.send_response(204)
        s.send_header("Content-type", "text/html")
        s.end_headers()
    def do_PUT(s):
	do_POST(s)

# Function to process the queued data
def processQueue():
    opener = urllib2.build_opener(urllib2.HTTPHandler)
    while True:
        try:
            #element = queue.get()
            #request = urllib2.Request(NEXT_SERVER, data=element)
            #request.add_header('Content-Type', 'raw')
            #request.get_method = lambda: 'PUT' # Hacky, but it works!
            #url = opener.open(request)
            pass
        except Exception, e:
            print e
            #queue.put(element)
            time.sleep(1)

class HTTPServerV6(BaseHTTPServer.HTTPServer):
    address_family = socket.AF_INET6

if __name__ == '__main__':
    thread.start_new_thread(processQueue, ())
    httpd = HTTPServerV6((HOST_NAME, PORT_NUMBER), MyHandler)
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)
