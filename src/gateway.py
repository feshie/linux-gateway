#Derived from https://wiki.python.org/moin/BaseHttpServer#CA-86c92a7a96bb671a2cb7471059da597b6df2666f_1

from __future__ import print_function

import time
import BaseHTTPServer
import CGIHTTPServer
import Queue
import io
import os
import urllib2
import thread
import socket
import sys
import SocketServer

HOST_NAME = '0:0:0:0:0:0:0:1' # Change this!
PORT_NUMBER = 8080 # Change this!

NEXT_SERVER = 'http://localhost:9000/upload' # The address of the server after the proxy. Change this too!

class MyHandler(CGIHTTPServer.CGIHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(204)
        s.end_headers()
    def do_GET(s):
        s.send_response(204)
        s.end_headers()
    def do_POST(s):
        # Queue the file on disk
        filename = "queue/" + str(int(time.time() * 1000000)) + "_" + s.client_address[0]
        if not os.path.exists(os.path.dirname(filename)):
            os.makedirs(os.path.dirname(filename))
        data = s.rfile.read(int(s.headers.getheader('content-length')))
        fh = open(filename, 'w')
        fh.write(data.decode())
        s.send_response(204)
        s.end_headers()

    def do_PUT(s):
	do_POST(s)

# Function to process the queued data
def processQueue():
    opener = urllib2.build_opener(urllib2.HTTPHandler)
    while True:
        try:
            if len(os.listdir("queue")) > 0:
                filename = "queue/" + os.listdir("queue")[0]
                fh = open(filename, 'r')
                data = fh.read()
                request = urllib2.Request(NEXT_SERVER, data=data)
                request.add_header('Content-Type', 'raw')
                url = opener.open(request)
                print("Status = %s" % url.getcode())
                if int(url.getcode()/100) == 2:
                    os.remove(filename)
                else:
                    print("Status code from next server was not success", file=sys.stderr)
            else:
                time.sleep(1)
        except Exception, e:
            print(e, file=sys.stderr)
            time.sleep(1)

class HTTPServerV6(SocketServer.ThreadingMixIn, BaseHTTPServer.HTTPServer):
    address_family = socket.AF_INET6

if __name__ == '__main__':
    thread.start_new_thread(processQueue, ())
    httpd = HTTPServerV6((HOST_NAME, PORT_NUMBER), MyHandler)
    print(time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER))
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print(time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER))
