#!/bin/bash

java -cp bin:lib/assessment-engine.jar crosslink.CrosslinkMining en:ko \
 ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all \
  ~/experiments/ntcir-9-clld/topics/test/en \
  ~/experiments/ntcir-9-clld/topics/test/ko
