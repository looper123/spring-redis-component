#!/bin/bash
base_dir=/home/lzp/softwares/redis-4.0.6
m_7000=sentinel_conf/master_6379.conf
s_7001=sentinel_conf/slave_6380.conf
s_7002=sentinel_conf/slave_6381.conf


conf_array=($m_7000 $s_7001 $s_7002)
for i in ${conf_array[@]}
  do
   port=`echo $i | awk -F'_'  '{print $3}' | awk -F'.' '{print $1}' `
   echo 'starting...'  $port 
   nohup ${base_dir}/src/redis-server ${base_dir}/$i >/dev/null 2>&1 &
   pid=`netstat -tunlp|grep  $port |  awk '{print $7}'| awk -F'/' '{print $1}'  | uniq`
   if [ ! $pid ] ; then
        echo $port 'start failed..'
   else
        echo 'start success..'
   fi
done
sleep 2

ssh root@192.168.194.132  "/home/lzp/softwares/redis-4.0.6/sentinel_conf/start_nodes.sh"



 
