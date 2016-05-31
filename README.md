# Linux-gateway

This repo contains the various scripts / programs
used to get/receive data from the MountainSensing nodes. 
See mountainsensing.org by the University of Southampton.

### fetcher

The fetcher is responsible for interfacing with the nodes.
As such, it handles getting samples, deleting samples,
setting the configuration and other node related things.
It also supports decoding protocol-buffer encoded samples and configurations.
Designed to run on the linux border router system, using the border-router node.
It uses the Californium CoAP library (see http://www.eclipse.org/californium/)

### poster

The poster is responsible for interfacing with the MountainSensing backed.
A directory is used as a FIFO queue for samples,
which are posted to the remote server database.

### scripts

Assortment of various scripts used, such as init scripts to connect
the border router and so on.
