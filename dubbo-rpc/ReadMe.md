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

  ```java
  public class PersonImpl implements Person {
      String name;
      int age;
      boolean gender;
  
      public void setName(String name) {
          this.name = name;
      }
  
      public String getName() {
          return name;
      }
  
      public void setAge(int age) {
          this.age = age;
      }
  
      public int getAge() {
          return age;
      }
  
      public void setGender(boolean gender) {
          this.gender = gender;
      }
  
      public boolean getGender() {
          return gender;
      }
  
      public String sayHello()
      {
          return ("Hello world! " + this.name);
      }
  }
  ```

## 3.  Dubbo 服务端 实现

参考官方的示例代码，实现 Dubbo 服务端

### 安装 ZooKeeper

由于 dubbo 需要在 zookeeper 中完成注册，因此需要搭建 zookeeper 

zookeeper 下载地址：https://zookeeper.apache.org/  下载 ZooKeeper

```
wget https://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz
```

下载完后，解压至 /opt/zookeeper 目录下：

![RVfgNy](https://oss.sumblog.cn/uPic/RVfgNy.png)

- 将 zookeeper 的 bin 目录添加至 PATH 环境变量中：

  ![XDFH0G](https://oss.sumblog.cn/uPic/XDFH0G.png)

- 启动 zookeeper：

  使用前台方式启动 zookeeper：`zkServer.sh start-foreground` 便于观察输出

  ![SQekOZ](https://oss.sumblog.cn/uPic/SQekOZ.png)

  至此 Zookeeper 搭建完成

#### 编写 Dubbo Service 代码

1. 定义 zookeeper Host 地址：

   ```java
   private static String zookeeperHost = "172.16.114.128";
   ```

2. 编写 main 函数，先进行注册中心的配置：

   ```java
   ServiceConfig<Person> service = new ServiceConfig<Person>();
   service.setApplication(new ApplicationConfig("first-dubbo-provider"));
   service.setRegistry(new RegistryConfig("zookeeper://" + zookeeperHost + ":2181"));
   ```

3. 定义接口和具体实现：

   ```java
   service.setInterface(Person.class);
   service.setRef(new PersonImpl());
   ```

4. 启动 Dubbo Service：

   ```java
   service.export();
   
   System.out.println("dubbo service started");
   new CountDownLatch(1).await();
   ```

#### 遇到的问题：

启动服务端时，提示zookeeper 连接失败：`zookeeper not connected`

![XTV1nl](https://oss.sumblog.cn/uPic/XTV1nl.png)

经过排查，原因是在启动了 ipv6 的系统上，zookeeper 默认只监听 ipv6 地址：

![2020-10-02I4TRaUDlGYZS](http://media.sumblog.cn/uPic/2020-10-02I4TRaUDlGYZS.png)

服务器如果支持ipv6，安装zookeeper默认启动会监听ipv6地址，导致ipv4无法访问，需要配置开启 ipv4 访问。设置方法如下：

```
vim zookeeper-01/bin/zkServer.sh
```

查找nohup指令所在的代码行

```bash
"-Dzookeeper.log.file=${ZOO_LOG_FILE}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
-XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError='kill -9 %p' \
-cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

在以上脚本添加如下参数

```
-Djava.net.preferIPv4Stack=true
```

修改完之后

```bash
nohup "$JAVA" $ZOO_DATADIR_AUTOCREATE "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" \
    "-Dzookeeper.log.file=${ZOO_LOG_FILE}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
    -Djava.net.preferIPv4Stack=true -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError='kill -9 %p' \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &
```

重新启动 zookeeper，可以观察到 ipv4 地址正常监听：

![DnIvrp](https://oss.sumblog.cn/uPic/DnIvrp.png)





zookeeper 报错：

![2020-10-02IeidSrY3fN8d](http://media.sumblog.cn/uPic/2020-10-02IeidSrY3fN8d.png)

具体问题所在：

客户端连接Zookeeper时，配置的超时时长过短。致使Zookeeper还没有读完Consumer的数据，连接就被Consumer断开了。

解决方案：

初始化Zookeeper连接时，将接收超时参数值调整大一些即可（tickTime2000改为10000），默认是毫秒（ms）



完成 zookeeper 配置后，启动服务端，可以看到提示 dubbo server 成功启动：

![vR6cF7](https://oss.sumblog.cn/uPic/vR6cF7.png)



## 4. 编写 Dubbo 调用端，发起 rpc 调用：

```java

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

public class Caller {
    private static String zookeeperHost = "172.16.114.128";

    public static void main(String[] args) {
        ReferenceConfig<Person> reference = new ReferenceConfig<Person>();
        reference.setApplication(new ApplicationConfig("first-dubbo-consumer"));
        reference.setRegistry(new RegistryConfig("zookeeper://" + zookeeperHost + ":2181"));
        reference.setInterface(Person.class);
        Person service = reference.get();
        service.setName("Zhangjh");
        String message = service.sayHello();
        System.out.println(message);
    }
}
```

- 调用端首先通过 reference 获得远端对象 `service`
- 调用远端对象的 `setName` 方法，设置对象姓名
- 之后调用对象的 `sayHello` 方法，获得远端对象的返回值，并在终端打印：

![4pNccm](https://oss.sumblog.cn/uPic/4pNccm.png)

