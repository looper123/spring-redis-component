#!/bin/bash
base_dir=/home/lzp/softwares/redis-4.0.6
port_array=("6382" "6383" "6384" "26380")

#stop all nodes
for i  in  ${port_array[@]}
  do 
   node_pid=`netstat -tunlp|grep $i | awk '{print $7}'| awk -F'/' '{print $1}'  | uniq`
  if [ ! $node_pid ];  then 
	echo 'port'$i 'is not exist'
  else 
        echo 'stopping....' $node_pid 
        kill $node_pid
  fi
done

sleep 2

#delete data record files
find -name '*.aof' | xargs  rm -rf 
find -name '*.rdb' | xargs  rm -rf
find -name 'node_700*.conf' | xargs  rm -rf

sleep 2



