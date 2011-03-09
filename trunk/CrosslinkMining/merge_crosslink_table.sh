#!/bin/bash

TABLE1=$1
TABLE2=$2

cat $TABLE1 $TABLE2 | awk -F ":" '{if ($2 != 0) {print $1"-"$2} }' | sort -n | uniq

