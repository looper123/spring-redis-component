#!/bin/bash
base_dir=/home/lzp/softwares/redis-4.0.6
s_7003=sentinel_conf/master_6382.conf
s_7004=sentinel_conf/slave_6383.conf
s_7005=sentinel_conf/slave_6384.conf


conf_array=($s_7003 $s_7004 $s_7005)
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
