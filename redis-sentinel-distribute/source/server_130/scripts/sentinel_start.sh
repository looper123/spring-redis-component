#!/bin/bash
base_dir=/home/lzp/softwares/redis-4.0.6

$base_dir/sentinel_conf/clean_data.sh

sleep 1

$base_dir/sentinel_conf/start_nodes.sh

sleep 1

#local sentinel start 
nohup  $base_dir/src/redis-server $base_dir/sentinel_conf/sentinel_130.conf --sentinel >/dev/null 2>&1 &


#remote sentinel start 
ssh root@192.168.194.132  "nohup  $base_dir/src/redis-server $base_dir/sentinel_conf/sentinel_132.conf --sentinel >/dev/null 2>&1 &"




