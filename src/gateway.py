#Derived from https://wiki.python.org/moin/BaseHttpServer#CA-86c92a7a96bb671a2cb7471059da597b6df2666f_1

import time
import BaseHTTPServer
import CGIHTTPServer
import Queue
import io
import urllib2
import thread
import random

HOST_NAME = 'localhost' # Change this!
PORT_NUMBER = 8080 # Change this!

POST_PROXY = 'http://localhost:9000/upload' # The address of the server after the proxy. Change this too!

queue = Queue.Queue(128)

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
        queue.put(s.rfile.read(int(s.headers.getheader('content-length'))))
        s.send_response(204)
        s.send_header("Content-type", "text/html")
        s.end_headers()
    def do_PUT(s):
	do_POST(s)

# Function to process the queued data
def processQueue():
    opener = urllib2.build_opener(urllib2.HTTPHandler)
    while True:
        element = queue.get()
        request = urllib2.Request(POST_PROXY, data=element)
        request.add_header('Content-Type', 'raw')
        request.get_method = lambda: 'PUT' # Hacky, but it works!
        url = opener.open(request)

if __name__ == '__main__':
    thread.start_new_thread(processQueue, ())
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)
