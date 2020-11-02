# 基于 Apache Axis2 提供 Web 服务，生成 WSDL 文件，调用服务





## 1. 安装 Tomcat

Axis 2 需要依赖 Tomcat 提供 Web 服务，因此需要先搭建 Tomcat 服务器。

从 Tomcat 官网下载 tomcat 9.0.38 版本安装包，解压后运行 startup.sh 脚本：

![IxNmY4](https://oss.sumblog.cn/uPic/IxNmY4.png)

服务启动成功后，访问 http://localhost:8080 ，可以看到 Apache Tomcat 的初始界面，tomcat 搭建成功

![9kaQk1](https://oss.sumblog.cn/uPic/9kaQk1.png)

## 2. 安装 axis2

目前，axis2 最新的版本为 1.7.9

下载 axis2-1.7.9-war.zip 包，解包后可以获得一个 war 包

<img src="https://oss.sumblog.cn/uPic/r29wGy.png" alt="r29wGy" style="zoom: 50%;" />

将该 war 包放置在  Tomcat 的 webapps 目录下，axis2 安装到 Tomcat 中。

重启 Tomcat 访问 http://127.0.0.1:8080/axis2/，可以看到 axis2 的默认页面，axis2 环境搭建成功。

![LWyhv7](https://oss.sumblog.cn/uPic/LWyhv7.png)



## 3. POJO 类实现

> 在Axis2中不需要进行任何的配置，就可以直接将一个简单的POJO发布成WebService。其中POJO中所有的public方法将被发布成WebService方法。

在 `<Tomcat安装目录>\webapps\axis2\WEB-INF\pojo`  目录中，建立 Person.java 文件，实现 Person 类：

```java
public class Person {
    String name;
    int age;
    boolean gender;

    public void setName(String name) {
        this.name = name;
    }

    public Person(String name, int age, boolean gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public Person() {
        this.name = "None";
        this.age = 0;
        this.gender = true;
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

    public boolean getGender(boolean gender) {
        return this.gender;
    }

    public String sayHello(){
        return "hello world!";
    }
}

```



## 4. 测试服务调用

现在成功将 Person 类发布成了 WebService，在浏览器访问 http://localhost:8080/axis2/services/listServices

![rWspKP](https://oss.sumblog.cn/uPic/rWspKP.png)

该 WebService 服务还生成了以下 wsdl 文件：

![YDWz3w](https://oss.sumblog.cn/uPic/YDWz3w.png)



## 5. 实现 WebService 客户端调用程序

Axis2提供了一个wsdl2java.sh 命令可以根据WSDL文件自动产生调用WebService的代码。wsdl2java.bat命令可以在**`<Axis2安装目录>/bin`**目录中找到。在使用wsdl2java.sh命令之前需要设置AXIS2_HOME环境变量，该变量值是**`<Axis2安装目录>`**

使用下面的命令行生成调用 WebService 的代码：

```
./wsdl2java.sh -uri http://localhost:8080/axis2/services/Person\?wsdl
```

其中-url参数指定了wsdl文件的路径，可以是本地路径，也可以是网络路径。在执行完上面的命令后，就会发现在当前目录下多了个stub目录，在 stub/src/client目录可以找到一个PersonStub.java文件，该文件负责调用WebService，可以在程序中直接使用这个类

![C6oku3](https://oss.sumblog.cn/uPic/C6oku3.png)

创建项目 wsdl-client，将生成的 PersonStub.java 以及 PersonCallbackHandler.java 文件导入工程中，并编写客户端测试代码，并测试：

可以看到，客户端发起调用成功，但存在问题，虽然已经使用了 setName 方法，设定了 Person 对象的姓名，但调用 SayHello 方法是，仍然无法获取到姓名。原因是默认的 Web服务是无状态的，需要保存会话状态，才能提供有状态的 Web 服务。

![BeQhAi](https://oss.sumblog.cn/uPic/BeQhAi.png)

## 6. 会话管理

使用Axis2来管理WebService的状态基本上对于开发人员是透明的。在WebService类需要使用org.apache.axis2.context.MessageContext和org.apache.axis2.context.ServiceContext类来保存与获得保存在服务端的状态信息，

实现同一个WebService的Session管理需要如下三步：

1. 使用MessageContext和**ServiceContext**获得与设置key-value对。

2. 在 service.xml 配置文件中为要进行Session管理的WebService类所对应的 \<service> 元素添加一个scope属性，并将该属性值设为**transportsession**。

3. 在客户端使用setManageSession(true)打开Session管理功能。

由于涉及到 xml 配置文件的修改，因此不能直接使用简单的pojo方式发布 WebService，需要通过 jar 包的方式发布。

要想将Person类发布成Web Service，需要一个services.xml文件，在WEB-INF下创建services文件夹然后创建**`-->conf-->META-INF-->services.xml`**，services.xml这个文件必须要放在META-INF目录中

- 在配置文件中为 WebService类对应的 \<service> 元素添加 scope 属性：

  修改后的配置文件如下：

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  
  <service name="PersonStateful" scop="application" targetNamespace="http://ws.apache.org/ax2">
      <description>有状态 Person</description>
      <!-- 服务全类名 -->
      <parameter name="ServiceClass">buaa.zhangjh.PersonStateful</parameter>
  
      <messageReceivers>
          <!-- 配置消息接收器，Axis2会自动选择 -->
          <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-out"
                           class="org.apache.axis2.rpc.receivers.RPCMessageReceiver" />
          <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-only"
                           class="org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver" />
      </messageReceivers>
  
  </service>
  ```

  

- 在服务端使用 key-value 对的方法，存储会话状态信息：

  修改后的服务端代码如下：

  ```java
  package buaa.zhangjh;
  
  import org.apache.axis2.context.MessageContext;
  import org.apache.axis2.context.ServiceGroupContext;
  
  public class PersonStateful {
      String name;
      int age;
      boolean gender;
      public PersonStateful() {
          this.name = "None";
          this.age = 0;
          this.gender = true;
      }
  
      public void setName(String name) {
          this.name = name;
          //  设置key-value对
          MessageContext mc = MessageContext.getCurrentMessageContext();
          ServiceGroupContext sgc = mc.getServiceGroupContext();
          sgc.setProperty("name", name);
      }
  
      public String getName() {
          //  获得key-value对中的value
          MessageContext mc = MessageContext.getCurrentMessageContext();
          ServiceGroupContext sgc =  mc.getServiceGroupContext();
          String name = (String) sgc.getProperty("name");
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
  
      public boolean getGender(boolean gender) {
          return this.gender;
      }
  
      public String sayHello(){
          //  获得key-value对中的value
          MessageContext mc = MessageContext.getCurrentMessageContext();
          ServiceGroupContext sgc =  mc.getServiceGroupContext();
          String name = (String) sgc.getProperty("name");
          String rs = String.format("Hello! %s", name);
          return  rs;
      }
  }
  
  ```

- 服务端打包并发布：

  打包 aar，控制台中进入ws目录，并输入如下的命令生成.aar文件.

  　　**`jar cvf person.aar .`**

  　　实际上，.jar文件也可以发布webservice，但axis2官方文档中建议使用.aar文件发布webservice.最后将ws.aar文件复制到**`<Tomcat安装目录>\webapps\axis2\WEB-INF\services`**目录中，启动Tomcat后，就可以调用这个WebService了。

  ![2hpsIY](https://oss.sumblog.cn/uPic/2hpsIY.png)

- 在客户端使用setManageSession(true)打开Session管理功能

  修改 PersonStatefulStub 类的构造方法：

  ```
  /**
   * Constructor that takes in a configContext  and useseperate listner
   */
  public PersonStatefulStub(
      org.apache.axis2.context.ConfigurationContext configurationContext,
      String targetEndpoint, boolean useSeparateListener)
      throws org.apache.axis2.AxisFault {
      //To populate AxisService
      populateAxisService();
      populateFaults();
  
      _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,
              _service);
  
      _serviceClient.getOptions()
                    .setTo(new org.apache.axis2.addressing.EndpointReference(
              targetEndpoint));
      _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
  
      //Set the soap version
      _serviceClient.getOptions()
                    .setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
  }
  ```

  在该方法中添加如下的代码：

  ```
  _serviceClient.getOptions().setManageSession(true);
  ```

  添加后的代码如下：

  ![xbjPFp](https://oss.sumblog.cn/uPic/xbjPFp.png)

## 7. 有状态的 WebService 客户端调用测试

编写客户端调用代码；

```java
public class Main {
    public static void main(String[] args) throws RemoteException {
        System.out.println("使用无状态的 axis2 服务测试");
        PersonStub person = new PersonStub();

        PersonStub.SayHello sayHello = new PersonStub.SayHello();
        PersonStub.SetName setName = new PersonStub.SetName();
        setName.setArgs0("Zhangjh");
        System.out.println("设置姓名完成");
        person.setName(setName);
        PersonStub.SayHelloResponse response = person.sayHello(sayHello);
        PersonStub.GetName getName = new PersonStub.GetName();
        PersonStub.GetNameResponse response1 = person.getName(getName);
        System.out.println("获取到的姓名为：" + response1.get_return());
        System.out.println(response.get_return() + response1.get_return());


        System.out.println("\n使用有状态的 axis2 服务");
        PersonStatefulStub personStateful = new PersonStatefulStub();
        PersonStatefulStub.SetName setnameState = new PersonStatefulStub.SetName();
        setnameState.setName("Zhangjh");
        personStateful.setName(setnameState);
        PersonStatefulStub.GetName getName1 = new PersonStatefulStub.GetName();
        PersonStatefulStub.GetNameResponse response3 =  personStateful.getName(getName1);
        String newName = response3.get_return();
        System.out.println("设置姓名成功，获取到的姓名为：" + newName);
        personStateful.setName(setnameState);
        PersonStatefulStub.SayHello sayHelloState = new PersonStatefulStub.SayHello();
        PersonStatefulStub.SayHelloResponse response2 = personStateful.sayHello(sayHelloState);
        System.out.println(response2.get_return());

    }
}
```

使用有状态的 WebService 调用，可以看到，成功的修改了 Person 对象的 Name 属性，并在会话的后续调用中，能返回正确的属性值：

![EKZ2xj](https://oss.sumblog.cn/uPic/EKZ2xj.png)