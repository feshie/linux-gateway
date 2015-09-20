# Poster

### Config
Please change the host IP address, port number and the next
server's address. The host IP address should be the IP address of the
machine this script is running on. The next server's address should be
the address of the next server where the stored files will go.

### Under the hood
The files send to this gateway are stored on disk in a queue,
allowing for recovery in the event of a system outage. Files are sent in
order received (FIFO). Files are only removed from the queue once they
have been sent to the next server.

### Usage
One configured, just run with:
python gateway.py

Any files added to the queue directory configured will be posted to
the next server.
