# 使用 Apache Dubbo 对外提供 RPC 服务

## 0. 运行环境

- OS：64bit Mac OS X 10.15.7 19H2
- Kernel：x86_64 Darwin 19.6.0

## 1. 配置 Dubbo 开发环境

1. 安装 JDK 环境

   目前，Oracle 官网最新的 JDK 版本为 15，但出于兼容性考虑，下载安装 JDK8 环境，下载地址：https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html

   在下载页面中，找到对应的平台进行下载，并完成安装

   ![dyC3sO](https://oss.sumblog.cn/uPic/dyC3sO.png)

   安装之后在终端检查 java 版本：

   ![DORhuT](https://oss.sumblog.cn/uPic/DORhuT.png)

2. 创建 Dubbo 工程项目

   - 使用 Jetbrains IDEA 作为集成开发环境，新建 Java 项目

     ![nrDeU8](https://oss.sumblog.cn/uPic/nrDeU8.png)

   - 前往 dubbo 官方 github 仓库：https://github.com/apache/dubbo，按照 ReadMe 中的 Getting started 说明，引入 dubbo 依赖：

     在项目根目录下的 pom.xml 文件中加入以下依赖项：

     ```xml
     <properties>
         <dubbo.version>2.7.8</dubbo.version>
     </properties>
         
     <dependencies>
         <dependency>
             <groupId>org.apache.dubbo</groupId>
             <artifactId>dubbo</artifactId>
             <version>${dubbo.version}</version>
         </dependency>
         <dependency>
             <groupId>org.apache.dubbo</groupId>
             <artifactId>dubbo-dependencies-zookeeper</artifactId>
             <version>${dubbo.version}</version>
             <type>pom</type>
         </dependency>
     </dependencies>
     ```

   - 点击 Maven Reload 按钮，下载安装所需依赖：

     ![asxdze](https://oss.sumblog.cn/uPic/asxdze.png)

## 2. 服务接口定义、实现

- 创建文件 Person.java 定义 Person 接口：

  ```java
  public interface Person {
  
      public void setName(String name);
  
      public String getName();
  
      public void setAge(int age);
  
      public int getAge();
  
      public void setGender(boolean gender);
  
      public boolean getGender();
  
      public String sayHello();
  
  }
  ```

- 创建文件 PersonImpl.java，对定义的 Person 接口完成实现：

  ```
  
  ```

  

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

