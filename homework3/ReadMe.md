# 作业三 组合服务开发与运行

SY2006127 张俊华

## 实验目标

1. 自己选择 Web 服务及流程的功能，开发 4 个及以上 Web 服务(RESTful/SOAP 可选)
2. 通过 BPEL/BPMN 流程进行上述服务的组合并进行执行； 

## 系统功能

本次作业实现了一个**身份证信息解析**的组合服务

- 该组合服务由 接口权限验证服务、身份证合法性检验服务、身份证签发省份计算、年龄计算、性别计算四个服务组合构成
- 最终实现的组合服务，首先对用户输入的接口调用秘钥进行验证，判断用户是否有权限使用该组合服务，若有权限，对传入的身份证号码使用身份证合法性检验服务进行校验，判断该身份证号码是否是一个合法的身份证号码，若合法，继续依次调用省份计算、年龄计算、性别计算三个服务。最后将各个服务的返回值进行整合，最终返回所有结果。

## 子服务功能

- 接口权限验证服务：

  - service 地址：http://localhost:8080/WebService/services/payService

  - 该服务包含一个 pay 接口，传入接口调用秘钥，验证对服务的调用权限，执行计费。返回一个 double 类型的账户余额数据，若返回值小于0，代表账户已欠费，无法完成服务调用请求

    ![image-20201223202059192](http://media.sumblog.cn/uPic/2020-12-23image-2020122320205919278qVcw.png)

- 身份证合法性检验服务：

  - 接口地址：http://localhost:8080/WebService/services/LegalService

  - 该服务包含一个 isLegal 接口，传入字符串类型的身份证号码，返回 int 类型，若返回值为 1，代表该身份证号码合法，否则表示身份证号码非法

    ![image-20201223202514480](http://media.sumblog.cn/uPic/2020-12-23image-20201223202514480U4TZoc.png)

- 身份证签发省份计算服务：

  - Service 地址：http://localhost:8080/WebService/services/ProvinceService

  - 该服务包含一个 `getProvince` 接口，接受字符串类型的身份证号码输入，返回字符串，表示该身份证的省份。

    ![image-20201223202709841](http://media.sumblog.cn/uPic/2020-12-23image-20201223202709841n9SHkr.png)

- 年龄和性别计算服务：

  - Service 地址：http://localhost:8080/WebService/services/AgeService 、http://localhost:8080/WebService/services/GenderService

  - 这两个服务通过传入的身份证号码，计算持证人的年龄和性别

    ![image-20201223202911880](http://media.sumblog.cn/uPic/2020-12-23image-20201223202911880haLvLm.png)

    ![image-20201223202924825](http://media.sumblog.cn/uPic/2020-12-23image-20201223202924825T5xwqG.png)

## BPEL 流程结构

<img src="http://media.sumblog.cn/uPic/2020-12-23join_%E6%9C%AA%E5%91%BD%E5%90%8D%20(1)ma4Mkt.jpg" alt="join_未命名 (1)" style="zoom:80%;" />

## 运行结果

<img src="http://media.sumblog.cn/uPic/2020-12-24image-20201224194035264Yv5IWi.png" alt="image-20201224194035264" style="zoom:67%;" />

- 可以看到，服务对输入的身份证号码进行了验证，返回归属地、性别、年龄等信息

## 实现过程：

### 1. 配置 Apache ODE

前往：http://ode.apache.org/getting-ode.html 下载 Apache ODE，目前的最新版本是 1.3.8。 下载 War 包，并部署在 Tomcat 中。

访问 127.0.0.1:8080/ode 可以看到 Apache ODE 运行界面

<img src="http://media.sumblog.cn/uPic/2020-12-15image-2020121517274309656zIvd.png" alt="image-20201215172743096" style="zoom:67%;" />

### 2. 安装 BPEL Designer 插件

Eclipise 进入 install wizard，选择安装 SOA Development 中的所有插件

<img src="http://media.sumblog.cn/uPic/2020-12-18image-20201218101749860rXqVU2.png" alt="image-20201218101749860" style="zoom:67%;" />

然而 安装完成后，发现仍然无法创建 BPEL project。经过排查，怀疑是 eclipse 版本问题

![image-20201218121656024](http://media.sumblog.cn/uPic/2020-12-18image-202012181216560248Tvqz2.png)

使用 2018-09 版本的 eclipse 尝试：

<img src="http://media.sumblog.cn/uPic/2020-12-18image-20201218122020318TQ7vXQ.png" alt="image-20201218122020318" style="zoom:67%;" />

该版本的 eclipse 配合 BPEL 工作正常。

### 3. 编写 Web 服务

编写进行身份证解析的相关 Web 服务

![image-20201224194916279](http://media.sumblog.cn/uPic/2020-12-24image-20201224194916279p70P3b.png)

并通过 Eclipse 创建 wsdl 文件。需要注意的是，这些服务需要位于不同的包中，否则在使用 BPEL 进行服务组合时，会导致命名中间出现冲突。

对发布的 web Service 进行测试：

<img src="http://media.sumblog.cn/uPic/2020-12-24image-20201224195206916A3Jsko.png" alt="image-20201224195206916" style="zoom:67%;" />

尝试调用，验证服务创建成功。

### 4. 创建 BPEL 工程：

<img src="http://media.sumblog.cn/uPic/2020-12-18image-20201218122247086S4SsOv.png" alt="image-20201218122247086" style="zoom:67%;" />

创建 BPEL 流程文件：

<img src="http://media.sumblog.cn/uPic/2020-12-18image-20201218122347579GCMiW5.png" alt="image-20201218122347579" style="zoom:67%;" />

在流程文件中依次将各子服务加入为 Partner Links，并按照流程依次添加Assign、Invoke、条件判断等环节，实现组合服务逻辑。

![image-20201224195422097](http://media.sumblog.cn/uPic/2020-12-24image-2020122419542209717pst7.png)

### 5. 创建 ode Server，发布组合服务

- 在 Eclipse 中创建一个 Ode Server，并完成配置

  ![image-20201224195702521](http://media.sumblog.cn/uPic/2020-12-24image-202012241957025215dAoNk.png)

- 然而发布后，尝试调用组合服务时，发现服务器返回 404 错误，经过排查，原因是 Eclipse 自动生成的组合服务 wsdl 中，服务 Address 有误，需要手动改正：

  ![image-20201224195842788](http://media.sumblog.cn/uPic/2020-12-24image-20201224195842788fUEslw.png)

- 前往 ode 面板，可以看到编写的组合服务已经发布成功：

  ![image-20201224195945727](http://media.sumblog.cn/uPic/2020-12-24image-20201224195945727hiUJ4R.png)

  ![image-20201224200009651](http://media.sumblog.cn/uPic/2020-12-24image-20201224200009651hh5WSY.png)

- 尝试调用，组合服务运行正常：

  ![image-20201224200044010](http://media.sumblog.cn/uPic/2020-12-24image-20201224200044010Efp2lX.png)