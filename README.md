# Linux-gateway

This repo contains the various scripts / programs
used to receive data from the MountainSensing nodes.

### fetcher

The fetcher is responsible for interfacing with the nodes.
As such, it handles getting samples, deleting samples,
setting the configuration and other node related things.
It also supports decoding protocol-buffer encoded samples and configurations.

### poster

The poster is responsible for interfacing with the MountainSensing backed.
A directory is used as a FIFO queue for samples,
which are posted to the database.

### scripts

Assortment of various scripts used, such as init scripts to connect
the border router and so on.
