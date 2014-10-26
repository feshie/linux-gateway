#!/bin/bash
cd /home/mountainsensing/linux-gateway/scripts
echo "Starting border router"
./border-router
pid=$1
echo "sleeping"
sleep 540
echo "killing"
kill -9 $pid