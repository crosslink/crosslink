#!/bin/bash

java -cp bin:lib/assessment-engine.jar crosslink.CrosslinkMining zh:en \
 ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all \
  ~/experiments/c2e-ld/topics/zh-topics \
 ~/experiments/c2e-ld/topics/en-topics 
