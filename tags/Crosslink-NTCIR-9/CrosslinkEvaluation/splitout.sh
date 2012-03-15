#!/bin/bash

cat uniq.txt | while read line; do if [ "a${line:0:1}" == 'a#' ]; then file=`echo $line|cut -f 2 -d "-"`; else echo $line >>${file}.txt; fi; done
