#!/bin/bash

for i in `ls *.log`; do grep error $i > /dev/null; if [ $? -eq 0 ]; then echo $i >> errors.log; fi; done

