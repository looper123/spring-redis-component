redis+spring 实现消息的发布订阅
===
问题:当发布消息后 所有集群服务中的所有服务都会受会通过监听触发回调动作 ，但是真实情况是只要触发一次回调
--
解决方案：使用Jedis类中的set(String, String, String, String, long)  方法  只有当该key 不存在时 才能set成功并返回OK
否则set会失败并且放回null 。 因为该指令是redis中的原生command 所以 能保证原子性 （即同一时间只能有一个当前cammond在redis中执
行，当该command执行完成后才会 执行下一个）