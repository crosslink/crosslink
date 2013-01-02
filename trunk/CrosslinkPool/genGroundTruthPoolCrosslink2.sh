path=`dirname $0`

topic_path=~/experiments/ntcir-10-crosslink/topics

for i in zh ja ko
do
#topic_path=`ls -d ~/experiments/ntcir-10-crosslink/topics/official/en*`
target_path=../CrosslinkAssessment/resources/GroundTruthPool/crosslink2/en-${i}
submission_path=~/experiments/ntcir-10-crosslink/submissions/GT/en-${i}
mkdir -p ${target_path}
${path}/genPool.sh -s -l en:${i} -p $topic_path -h /data/corpus/wikipedia/all/ -t ${target_path} ${submission_path}
done

for i in zh ja ko
do
#topic_path=`ls -d ~/experiments/ntcir-10-crosslink/topics/official/${i}*`
target_path=../CrosslinkAssessment/resources/GroundTruthPool/crosslink2/${i}-en
submission_path=~/experiments/ntcir-10-crosslink/submissions/GT/${i}-en
mkdir -p ${target_path}
${path}/genPool.sh -s -l ${i}:en -p $topic_path -h /data/corpus/wikipedia/all/ -t ${target_path} ${submission_path}
done

