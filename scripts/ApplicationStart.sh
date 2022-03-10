#!/bin/bash

cd /home/ubuntu
sudo nohup java -jar target/demo-0.0.1-SNAPSHOT.jar >> /opt/csye6225.log 2>&1 &