## 安装 ZooKeeper

https://zookeeper.apache.org/

下载

![2020-10-01hL7L77T5dWTo](http://media.sumblog.cn/uPic/2020-10-01hL7L77T5dWTo.png)

zookeeper 连接失败

默认监听 ipv4 地址

![2020-10-02I4TRaUDlGYZS](http://media.sumblog.cn/uPic/2020-10-02I4TRaUDlGYZS.png)

服务器如果支持ipv6，安装zookeeper默认启动会监听ipv6地址，导致ipv4无法访问，设置方法如下：

```
vim zookeeper-01/bin/zkServer.sh
```

查找nohup代码行，vim中键入 `/nohup` 找到如下

```nohup
    "-Dzookeeper.log.file=${ZOO_LOG_FILE}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
    -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError='kill -9 %p' \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

在以上脚本添加如下参数

```
-Djava.net.preferIPv4Stack=true
```

修改完之后

```
nohup "$JAVA" $ZOO_DATADIR_AUTOCREATE "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" \
    "-Dzookeeper.log.file=${ZOO_LOG_FILE}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
    -Djava.net.preferIPv4Stack=true -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError='kill -9 %p' \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

zookeeper 报错：

![2020-10-02IeidSrY3fN8d](http://media.sumblog.cn/uPic/2020-10-02IeidSrY3fN8d.png)

具体问题所在：

客户端连接Zookeeper时，配置的超时时长过短。致使Zookeeper还没有读完Consumer的数据，连接就被Consumer断开了。

解决方案：

初始化Zookeeper连接时，将接收超时参数值调整大一些即可（tickTime2000改为10000），默认是毫秒（ms）

![vR6cF7](https://oss.sumblog.cn/uPic/vR6cF7.png)

![4pNccm](https://oss.sumblog.cn/uPic/4pNccm.png)

