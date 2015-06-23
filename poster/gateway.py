#!/usr/bin/env python
#Derived from https://wiki.python.org/moin/BaseHttpServer#CA-86c92a7a96bb671a2cb7471059da597b6df2666f_1

from __future__ import print_function

import signal
import time
import Queue
import io
import os
import urllib2
import sys
import logging
from logging.handlers import WatchedFileHandler
from optparse import OptionParser, OptionGroup

from gateway_config import *


signal.signal(signal.SIGINT, signal.SIG_DFL)
LOG_LEVEL = None

# Function to process the queued data
def processQueue():
    opener = urllib2.build_opener(urllib2.HTTPHandler)
    logger = logging.getLogger("Gateway Queue Handler")
    logger.setLevel(LOG_LEVEL)
    message_printed = False
    queue = BASE_DIR + QUEUE_DIR
    archive = BASE_DIR + ARCHIVE_DIR
    while True:
        try:
            queue_length = len(os.listdir(queue))
            if queue_length > 0:
                logger.info("%i items in queue" % queue_length)
                filename = queue + "/" + os.listdir(queue)[0]
                logger.debug("Sending file %s" % filename)
                from_ip = filename.split("_")[1]
                fh = open(filename, 'r')
                data = fh.read()
                fh.close()
                request = urllib2.Request(NEXT_SERVER + "?ip=" + from_ip, data=data)
                url = opener.open(request)
                logger.info("Return Status Code = %s" % url.getcode())
                if int(url.getcode()/100) == 2:
                    new_filename = archive + "/" + filename.split("/")[-1]
                    logger.debug("Moving  %s to %s" % (filename, new_filename))
                    os.rename(filename, new_filename )
                else:
                    logger.error("Status code from next server was not success")
                message_printed = False
            else:
                if not message_printed:
                  logger.info("Nothing in the queue")
                message_printed = True
                time.sleep(SLEEP_TIME)
        except Exception, e:
            logger.error(str(e))
            time.sleep(SLEEP_TIME)

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
    PARSER.add_option("-f", "--file", dest="filename",
          help="Redirect all output to file")
    (OPTIONS, ARGS) = PARSER.parse_args()
    if OPTIONS.quiet:
        LOG_LEVEL = logging.CRITICAL
    elif OPTIONS.verbose:
        LOG_LEVEL = logging.DEBUG
    logging.basicConfig(
      format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    LOGGER = logging.getLogger("Gateway Main")
    LOGGER.setLevel(LOG_LEVEL)
    if OPTIONS.filename is not None:
        LF = WatchedFileHandler(OPTIONS.filename)
        FORMAT = logging.Formatter(
		'%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        LF.setFormatter(FORMAT)
        LOGGER.addHandler(LF)

    if not os.path.exists(BASE_DIR + QUEUE_DIR):
      os.makedirs(BASE_DIR + QUEUE_DIR)
      LOGGER.info("%s%s created" % (BASE_DIR, QUEUE_DIR))
    else:
      LOGGER.debug("%s%s exists" % (BASE_DIR, QUEUE_DIR))
    if not os.path.exists(BASE_DIR + ARCHIVE_DIR):
      os.makedirs(BASE_DIR + ARCHIVE_DIR)
      LOGGER.info("%s%s created" % (BASE_DIR, ARCHIVE_DIR))
    else:
      LOGGER.debug("%s%s exists" % (BASE_DIR, ARCHIVE_DIR))

    LOGGER.debug("Starting Process queue")
    processQueue();
    LOGGER.info("Server Stops - [%s]:%s" % (HOST_NAME, PORT_NUMBER))
