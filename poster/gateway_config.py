from logging import ERROR

HOST_NAME = '2a01:348:24b:2::1'
PORT_NUMBER = 8081 # Change this!

NEXT_SERVER = 'http://data.mountainsensing.org/feshie/reciever/upload.php' # The address of pthe server after the proxy. Change this too!
SLEEP_TIME = 5
DEFAULT_LOG_LEVEL = ERROR

BASE_DIR = "/ms"
QUEUE_DIR = "/queue"
ARCHIVE_DIR = "/archive"
