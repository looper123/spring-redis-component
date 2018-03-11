环境搭建
===
一共两台redis哨兵(30、32) 每一个哨兵各有三台redis实例（一主两从）
具体sentinel和master、slave 的配置 ：参考source/server_30/config & source/server_32/config
启动脚本：参考source/server_30/scripts & source/server_32/scripts   其中sentinel_start.sh 是整个sentinel环境的启动脚本
参照具体环境修改路径、和端口。。。
