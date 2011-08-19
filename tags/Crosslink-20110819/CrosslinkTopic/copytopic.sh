#!/bin/bash

cat topic-ids.txt | while read line; do topic=~/corpus/wikipedia/all/en/`~/corpus/wikipedia/all/id2path.sh $line`; cp -v $topic en; done
