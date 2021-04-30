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



# 使用 Java Spring 提供 restful 服务

### 1. 创建 Spring 环境

- 启动 IDEA , New Project ，使用 Spring Initializer，初始化 Spring 环境：

![l7ymMH](https://oss.sumblog.cn/uPic/l7ymMH.png)

- 在依赖中勾选 Spring Web

![29nf2U](https://oss.sumblog.cn/uPic/29nf2U.png)

- 创建完成后，构建工程，并启动，控制台打印出 spring 的初始化过程，由于 Spring 中内嵌了 Apache Tomcat 服务器，因此可以不必单独部署 Tomcat

![f8n146](https://oss.sumblog.cn/uPic/f8n146.png)

- 打开浏览器 http://127.0.0.1:8080 默认 Spring 显示一个路径错误页面，至此， Spring 搭建成功。

![HmkQdv](https://oss.sumblog.cn/uPic/HmkQdv.png)

### 2. 创建资源表示形式类

创建如下 Person 类：

![w2ieYM](https://oss.sumblog.cn/uPic/w2ieYM.png)

### 3. 创建资源控制器

创建 `PersonController` 类:

```java
package buaa.zhangjh.restfuldemo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.TrustAnchor;

@RestController
public class PersonController {
 		Person p;
    @GetMapping("/person")
    public Person person(@RequestParam(value="name", defaultValue = "Zhangjh")
                         String name,
                         @RequestParam(value="age", defaultValue = "22")
                         int age,
                         @RequestParam(value="gender", defaultValue = "1")
                         boolean gender){
        p = new Person(name, age, gender);
        return p;
    }
}

```

使用 `@GetMapping` 修饰器，为 person 方法注册路由。`Person` 方法接受三个参数，使用`@RequestParam`，从客户端的请求中读取参数，并设置默认参数值。

之后，使用客户端传入的参数，构造一个新的 Person 对象，并将该对象返回。

类似的，构建 `sayHello` 方法，以及修改属性的 `setage`、`setGender`、`setName` 方法。

```java
    @GetMapping("/person/sayhello")
    public String sayHello() {
        return "Hello, I'm " + p.name;
    }

    @GetMapping("/person/setage")
    public boolean setAge(@RequestParam(value = "age")
                                   String age) {
        p.setAge(Integer.parseInt(age));
        return true;
    }
    @GetMapping("/person/setgender")
    public boolean setGender(@RequestParam(value = "gender")
                                   String gender) {
        p.setGender(Boolean.parseBoolean(gender));
        return true;
    }
    @GetMapping("/person/setName")
    public boolean setName(@RequestParam(value = "name")
                          String name) {
        p.setName(name);
        return true;
    }
```

### 4. 完成 Restful 调用

在浏览器中，对 url：http://127.0.0.1:8080/person 进行调用，可以看到直接返回默认的 Person 对象

![Ft4EXe](https://oss.sumblog.cn/uPic/Ft4EXe.png)

传入 name、age、gender 参数，构建自定义的 Person 对象

![OxvITn](https://oss.sumblog.cn/uPic/OxvITn.png)

调用 http://127.0.0.1:8080/person/sayhello ，显示 sayHello 方法的返回值

![sN45iU](https://oss.sumblog.cn/uPic/sN45iU.png)





