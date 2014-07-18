0. Introduction
	This script provides proxy functionality, allowing files from 
one network to be sent to a high-latency network without the sender 
having to wait for the transfer to complete.

1. Config
	Please change the host IP address, port number and the next 
server's address. The host IP address should be the IP address of the 
machine this script is running on. The next server's address should be 
the address of the next server where the stored files will go.

2. Under the hood
	The files send to this gateway are stored on disk in a queue, 
allowing for recovery in the event of a system outage. Files are sent in 
order received (FIFO). Files are only removed from the queue once they 
have been sent to the next server.

3. Usage
	One configured, just run with:
	python gateway.py
