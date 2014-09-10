#!/usr/bin/env python
#Derived from https://wiki.python.org/moin/BaseHttpServer#CA-86c92a7a96bb671a2cb7471059da597b6df2666f_1

from __future__ import print_function

import signal
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
import logging
from optparse import OptionParser, OptionGroup

from gateway_config import *


signal.signal(signal.SIGINT, signal.SIG_DFL)
LOG_LEVEL = None

class MyHandler(CGIHTTPServer.CGIHTTPRequestHandler):
    def __init__(self):
      self.logger = logging.getLogger("Gateway Post Handler")
      self.logger.setLevel(LOG_LEVEL)
      

    def do_HEAD(s):
        s.send_response(204)
        s.end_headers()
    def do_GET(s):
        s.send_response(204)
        s.end_headers()
    def do_POST(s):
        # Queue the file on disk
        filename = "queue/" + str(int(time.time() * 1000000)) + "_" + s.client_address[0]
        s.logger.info("Saving to %s" % filename)
        if not os.path.exists(os.path.dirname(filename)):
            os.makedirs(os.path.dirname(filename))
        data = s.rfile.read(int(s.headers.getheader('content-length')))
        fh = open(filename, 'w')
        fh.write(data)
        s.send_response(204)
        s.end_headers()

    def do_PUT(s):
	do_POST(s)

# Function to process the queued data
def processQueue():
    opener = urllib2.build_opener(urllib2.HTTPHandler)
    logger = logging.getLogger("Gateway Queue Handler")
    logger.setLevel(LOG_LEVEL)
    message_printed = False
    while True:
        try:
            queue_length = len(os.listdir("queue"))
            if queue_length > 0:
                logger.info("%d items in queue")
                filename = "queue/" + os.listdir("queue")[0]
                logger.info("Sending file %s" % filename)
                from_ip = filename.split("_")[1]
                fh = open(filename, 'r')
                data = fh.read()
                request = urllib2.Request(NEXT_SERVER + "?ip=" + from_ip, data=data)
                url = opener.open(request)
                print("Status = %s" % url.getcode())
                if int(url.getcode()/100) == 2:
                    logger.info("Deleting %s" % filename)
                    os.remove(filename)
                else:
                    logger.error("Status code from next server was not success")
                message_printed = False
            else:
                if not message_printed:
                  logger.info("Nothing in the queue")
                message_printed = True
                time.sleep(1)
        except Exception, e:
            logger.error(str(e))
            time.sleep(1)

class HTTPServerV6(SocketServer.ThreadingMixIn, BaseHTTPServer.HTTPServer):
    address_family = socket.AF_INET6

if __name__ == '__main__':
    LOG_LEVEL = DEFAULT_LOG_LEVEL
    PARSER = OptionParser()
    GROUP = OptionGroup(PARSER, "Verbosity Options",
        "Options to change the level of output")
    GROUP.add_option("-q", "--quiet", action="store_true",
          dest="quiet", default=False,
          help="Supress all but critical errors")
    GROUP.add_option("-v", "--verbose", action="store_true",
          dest="verbose", default=False,
          help="Print all information available")
    PARSER.add_option_group(GROUP)
    (OPTIONS, ARGS) = PARSER.parse_args()
    if OPTIONS.quiet:
        LOG_LEVEL = logging.CRITICAL
    elif OPTIONS.verbose:
        LOG_LEVEL = logging.DEBUG
    logging.basicConfig(
      format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    LOGGER = logging.getLogger("Gateway Main")
    LOGGER.setLevel(LOG_LEVEL)
    LOGGER.debug("Starting Process thread")
    thread.start_new_thread(processQueue, ())
    httpd = HTTPServerV6((HOST_NAME, PORT_NUMBER), MyHandler)
    LOGGER.info("Listening on - [%s]:%s" % (HOST_NAME, PORT_NUMBER))
    LOGGER.info("Next Server: %s" % NEXT_SERVER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    LOGGER.info("Server Stops - [%s]:%s" % (HOST_NAME, PORT_NUMBER))
