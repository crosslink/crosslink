path=`dirname $0`

for i in zh ja ko
do
topic_path=`ls -d ~/experiments/ntcir-10-crosslink/topics/official/en*`
target_path=~/experiments/ntcir-10-crosslink/submissions/GT/en-${i}
mkdir -p ${target_path}
${path}/genPool.sh -s -l en:${i} -p $topic_path ${target_path}
done

for i in zh ja ko
do
topic_path=`ls -d ~/experiments/ntcir-10-crosslink/topics/official/${i}*`
target_path=~/experiments/ntcir-10-crosslink/submissions/GT/${i}-en
mkdir -p ${target_path}
${path}/genPool.sh -s -l ${i}:en -p $topic_path ${target_path}
done

