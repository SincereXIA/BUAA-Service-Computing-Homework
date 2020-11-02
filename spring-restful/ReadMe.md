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