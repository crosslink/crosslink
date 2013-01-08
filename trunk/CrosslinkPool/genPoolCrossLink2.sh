path=`dirname $0`

TOPIC_PATH=~/experiments/ntcir-10-crosslink/topics

for i in zh ja ko
do

target_path=../CrosslinkAssessment/resources/Pool/crosslink2/en-${i}
mkdir -p $target_path
${path}/genPool.sh -s -c -l en:${i} -p ${TOPIC_PATH} -h /data/corpus/wikipedia/all/ -t ${target_path} /media/hpchome/NTCIR-10-CROSSLINK/submissions/runs/E2CJK

target_path=../CrosslinkAssessment/resources/Pool/crosslink2/${i}-en
mkdir -p $target_path
${path}/genPool.sh -s -c -l ${i}:en -p ${TOPIC_PATH} -h /data/corpus/wikipedia/all/ -t ${target_path} /media/hpchome/NTCIR-10-CROSSLINK/submissions/runs/CJK2E

done

