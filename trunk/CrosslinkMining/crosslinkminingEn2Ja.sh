#!/bin/bash

java -cp bin:lib/assessment-engine.jar crosslink.CrosslinkMining en:ja \
 ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all \
  ~/experiments/ntcir-10-crosslink/topics//en \
  ~/experiments/ntcir-10-crosslink/topics//ja
