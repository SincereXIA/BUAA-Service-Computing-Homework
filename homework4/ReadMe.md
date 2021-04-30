# 服务计算作业 4 服务质量保障

张俊华 SY2006127

[TOC]

## 0. 题目要求与完成情况

> 1. 代码静态分析
>    - 下载 HBase 和 Cassandra 项目的最新版本源代码， 使用 WALA 工具统计含有 Replica/Replication 关键字的类个数，并分析这些类之间的调用依赖；
>    - 选择自己之前作业中开发的某个服务，使用 LLVM 进行符号执行分析，并进行自动 化的用例生成和测试；
>
> 2. 代码动态分析
>
>    下载 Map Reduce 项目的最新版本源代码，编译运行自带的 word count 例子。 基于 Java agent机制和javassist工具生成word count例子运行过程中Map Reduce产生的trace， 包括进入和退出每个方法的时间戳、线程号等，分析其中可能的性能瓶颈。

**完成情况**

1. 代码静态分析
   - WALA 静态分析
     -  :white_check_mark:完成了 HBase、Cassandra 项目最新版本源代码下载
     -  :white_check_mark:使用 mvn、ant 对两个项目进行了编译，生成了待分析的 Jar 包
     -  :white_check_mark:建立 WALA 工程，使用 WALA 工具，构建ClassHierarchy，循环遍历每一个类，检查类名，并统计了包含关键字的类个数
     -  :white_check_mark:以 WALA 官方提供的 PDFTypeHierarchy 样例为基础，分析了包含关键字的类之间的调用依赖，生成了调用依赖图
   - LLVM 符号执行分析
     -   使用在 LLVM 编译器基础结构之上的 KLEE 符号执行引擎进行 c++ 程序的符号执行分析
     -  :white_check_mark:从源码安装约束求解器z3
     -  :white_check_mark:编译 klee-uclibc，支持对 使用了POSIX库的 c 程序的分析
     -  :white_check_mark:编译了libc++，实现了对 c++ 程序分析的支持
     -  :white_check_mark:从源码编译安装 KLEE
     -  :white_check_mark:对示例 c 代码进行了静态分析和测试用例的生成
2. 代码动态分析
   -  :white_check_mark:从源码编译安装 Hadoop
   -  :white_check_mark:编写了 Java agent ，使用 javassist 工具，为每个方法注入统计代码
   -  :white_check_mark:使用环境变量的方式，设置 Hadoop 启用自己编写的 Java agent
   -  :white_check_mark:生成了进入每个方法的时间戳，方法运行时间，以及线程号，对输出结果进行分析

# 1. WALA 静态分析

> **1.1** **下载 HBase 和 Cassandra 项目的最新版本源代码，使用 WALA 工具统计含有 Replica/Replication 关键字的类个数，并分析这些类之间的调用依赖；** 

## 1.1 源代码下载

- Hbase 源码位于：https://hbase.apache.org/source-repository.html

  ![image-20201226202135711](http://media.sumblog.cn/uPic/2020-12-26image-20201226202135711CG2FlJ.png)

- 克隆代码到本地：

```
  git clone https://gitbox.apache.org/repos/asf/hbase.git
```

​	官方给出的代码无法克隆，使用 github 仓库代替：

![image-20201226203513832](http://media.sumblog.cn/uPic/2020-12-26image-20201226203513832PD5NwI.png)

- cassandra 源码下载地址： https://cassandra.apache.org/download/

  ![image-20201226202525560](http://media.sumblog.cn/uPic/2020-12-26image-20201226202525560Ml44Xb.png)

  

  使用命令

  ```
  git clone https://gitbox.apache.org/repos/asf/cassandra.git
  ```

  克隆仓库到本地

  ![image-20201226202908876](http://media.sumblog.cn/uPic/2020-12-26image-20201226202908876enwdDc.png)



## 1.2  编译

- 进入 hbase 工程目录下，使用命令：`mvn package -DskipTests` 编译 jar 包

  ![image-20210113083641295](http://media.sumblog.cn/uPic/2021-01-13image-20210113083641295Dcw09Q.png)

  编译完成：

  ![image-20210113084443385](http://media.sumblog.cn/uPic/2021-01-13image-20210113084443385EquZPB.png)

- 进入 cassandra 工程目录下，使用命令：`ant` 编译工程，生成 jar 包：

  ![image-20210113084405880](http://media.sumblog.cn/uPic/2021-01-13image-20210113084405880sqqX3Z.png)

  编译完成：

  ![image-20210113084502670](http://media.sumblog.cn/uPic/2021-01-13image-20210113084502670PFuiIK.png)

  生成的 jar 包位于工程的 build 目录下：

  ![image-20210113085346968](http://media.sumblog.cn/uPic/2021-01-13image-20210113085346968tJvloP.png)

## 1.3 使用 WALA 进行分析

- 新建工程项目，使用 Gradle 进行依赖管理，编辑 `build.gradle` 文件，加入 `wala` 依赖：

  ![image-20210113090740250](http://media.sumblog.cn/uPic/2021-01-13image-20210113090740250L0ZNTJ.png)

### 1.3.1 统计含有 Replica/Replication 关键字的类个数

- 编写代码，解析编译好的 jar 包，构建ClassHierarchy，循环遍历每一个类，检查类名，并统计包含关键字的类个数：

  ```java
  public class Analyse {
      public static void main(String args[]) throws IOException, ClassHierarchyException, IllegalArgumentException, InvalidClassFileException, CancelException {
          String jarPath = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/cassandra/build/apache-cassandra-4.0-beta4-SNAPSHOT.jar";
          // 获得一个文件
          File exFile=new FileProvider().getFile("Java60RegressionExclusions.txt");
  
          // 将分析域存到文件中
          AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarPath, exFile);
  
          // 构建ClassHierarchy，相当与类的一个层级结构
          ClassHierarchy cha = ClassHierarchyFactory.make(scope);
  
          // 循环遍历每一个类
          int num = 0;
          for(IClass klass : cha) {
              // 打印类名
              if (klass.getName().toString().contains("Replic")) {
                  System.out.println(klass.getName().toString());
                  num += 1;
              }
          }
          System.out.println("Class Name Contain Replica/Replication num: " + num);
      }
  }
  
  ```

- 对于 cassandra，有 69 个类含有 Replica/Replication 关键字

  ![image-20210113161329922](http://media.sumblog.cn/uPic/2021-01-13image-20210113161329922jRxw24.png)

- 对于 hbase client，有 21 个类含有 Replica/Replication 关键字

  ![image-20210114094146046](http://media.sumblog.cn/uPic/2021-01-14image-20210114094146046TfU32K.png)

- 对于 hbase server，有 72 个类含有 Replica/Replication 关键字：

  ![image-20210114094433281](http://media.sumblog.cn/uPic/2021-01-14image-20210114094433281vfTiek.png)



### 1.3.2 分析类之间的调用依赖

- wala 官方给出了一个构建类之间层次结构，并使用 graphviz 工具，将类之间关系可视化的样例代码

  > ![image-20210113161919372](http://media.sumblog.cn/uPic/2021-01-13image-20210113161919372NJeC6E.png)

  但该代码无法筛选出我们需要的包含关键词的类，需要对其代码进行修改：

  - 在构建关系图时，对欲加入图中的每个类进行筛选，包含关键词再进行构建，修改后的部分代码如下：

    ```java
        public static boolean checkName(String name) {
            return name.contains("Replica");
        }
    
        /**
         * Return a view of an {@link IClassHierarchy} as a {@link Graph}, with edges from classes to
         * immediate subtypes
         */
        public static Graph<IClass> typeHierarchy2Graph(IClassHierarchy cha) {
            Graph<IClass> result = SlowSparseNumberedGraph.make();
            for (IClass c : cha) {
                if (checkName(c.getName().toString()))
                    result.addNode(c);
            }
            for (IClass c : cha) {
                if (checkName(c.getName().toString())){
                    for (IClass x : cha.getImmediateSubclasses(c)) {
                        if (checkName(x.getName().toString()))
                            result.addEdge(c, x);
                    }
                    if (c.isInterface()) {
                        for (IClass x : cha.getImplementors(c.getReference())) {
                            if (checkName(x.getName().toString()))
                                result.addEdge(c, x);
                        }
                    }
                }
            }
            return result;
        }
    ```

- 对于 cassandra 项目，构建出的类调用依赖如图所示：

  ![image-20210113162307891](http://media.sumblog.cn/uPic/2021-01-13image-20210113162307891F2R9t9.png)

- 对于 hbase client，包含关键词的类调用依赖：

  ![image-20210114100537663](http://media.sumblog.cn/uPic/2021-01-14image-20210114100537663IoENPf.png)

- 对于 hbase Server，包含关键词的类调用依赖：

  ![image-20210114100842783](http://media.sumblog.cn/uPic/2021-01-14image-20210114100842783zqy99K.png)

# 2 LLVM 静态分析

> 选择自己之前作业中开发的某个服务，使用 LLVM 进行符号执行分析，并进行自动化的用例生成和测试

- KLEE 是建立在 LLVM 编译器基础结构之上的符号执行引擎，从 2008 年提出以来，已经在学术界和工业界得到了广泛的研究和应用。

## 2.1 KLEE 编译

- KLEE 给出了在 LLVM9 上的编译步骤：http://klee.github.io/build-llvm9/，依据官方文档编译安装 KLEE 

- 安装编译依赖：`sudo apt-get install build-essential curl libcap-dev git cmake libncurses5-dev python-minimal python-pip unzip libtcmalloc-minimal4 libgoogle-perftools-dev libsqlite3-dev doxygen`

- 安装 lit、tabulate、wllvm ：`pip3 install lit tabulate wllvm`

- 安装 LLVM9：`sudo apt-get install clang-9 llvm-9 llvm-9-dev llvm-9-tools  `

- 安装约束求解器：z3

  ```
  $ wget https://github.com/Z3Prover/z3/archive/z3-4.8.9.zip
  $ unzip z3-4.8.9.zip
  $ cd z3-4.8.9
  $ mkdir build
  $ cd build
  $ cmake .. -DCMAKE_C_COMPILER=clang-9 -DCMAKE_CXX_COMPILER=clang++-9
  $ make -j4
  ```

  ![image-20210113170024666](http://media.sumblog.cn/uPic/2021-01-13image-20210113170024666sVwrVm.png)

  之后，使用 `make install ` a安装 z3

  ![image-20210113171846606](http://media.sumblog.cn/uPic/2021-01-13image-20210113171846606P7iLxZ.png)

- 构建 uClibc 和 POSIX 环境模型：默认  KLEE 不支持使用外部库，如果要使用 KLEE 分析实际程序，需要启用 KLEE POSIX Runtime 

  ```
  $ git clone https://github.com/klee/klee-uclibc.git  
  $ cd klee-uclibc  
  $ ./configure --make-llvm-lib  
  $ ./configure --make-llvm-lib --with-llvm-config=/usr/bin/llvm-config-9 --with-cc=/usr/bin/clang-9
  $ make -j2  
  $ cd .. 
  ```

  ![image-20210113170421956](http://media.sumblog.cn/uPic/2021-01-13image-20210113170421956iyiTGA.png)

- 编译 KLEE

  **Get KLEE source:**

  ```
  $ git clone https://github.com/klee/klee.git
  ```

  **Build libc++:** To be able to run C++ code, you also need to enable support for the C++ standard library.

  ```
  $ LLVM_VERSION=9 SANITIZER_BUILD= BASE=<LIBCXX_DIR> REQUIRES_RTTI=1 DISABLE_ASSERTIONS=1 ENABLE_DEBUG=0 ENABLE_OPTIMIZED=1 ./scripts/build/build.sh libcxx
  ```

  where `<LIBCXX_DIR>` is the absolute path where libc++ should be cloned and built.

  会提示找不到刚编译出的 z3 的动态链接库，运行 `ldconfig` 命令，创建出动态装入程序(ld.so)所需的连接和缓存文件

  To tell KLEE to use libc++, pass the following flags to CMake when you configure KLEE in step 8:

  ```
  -DENABLE_KLEE_LIBCXX=ON -DKLEE_LIBCXX_DIR=<LIBCXX_DIR>/libc++-install-9/ -DKLEE_LIBCXX_INCLUDE_DIR=<LIBCXX_DIR>/libc++-install-9/include/c++/v1/
  ```

  To additionally enable KLEE’s exception handling support for C++, pass the following flags to CMake when you configure KLEE in step 8:

  ```
  -DENABLE_KLEE_EH_CXX=ON -DKLEE_LIBCXXABI_SRC_DIR=<LIBCXX_DIR>/llvm-9/libcxxabi/
  ```

  Note that `<LIBCXX_DIR>` must currently be an absolute path. Note that if you want to build libc++ in your user home path, that in some environments (such as Ubuntu 18.04), `~` may not be an absolute path, but you can use `$HOME` instead.

  运行下面的指令编译 KLEE

  ```
  cmake \
    -DENABLE_SOLVER_Z3=ON \
    -DENABLE_POSIX_RUNTIME=ON \
    -DENABLE_KLEE_UCLIBC=ON \
    -DKLEE_UCLIBC_PATH=/home/zhangjh/download/src/klee-uclibc \
    -DLLVM_CONFIG_BINARY=/usr/bin/llvm-config-9 \
    -DLLVMCC=/usr/bin/clang-9 \
    -DLLVMCXX=/usr/bin/clang++-9 \
    -DENABLE_KLEE_LIBCXX=ON\
    -DKLEE_LIBCXX_DIR=/home/zhangjh/download/src/libcxx/libc++-install-90/ \
    -DKLEE_LIBCXX_INCLUDE_DIR=/home/zhangjh/download/src/libcxx/libc++-install-90/include/c++/v1/ \
    -DENABLE_KLEE_EH_CXX=ON \
    -DKLEE_LIBCXXABI_SRC_DIR=/home/zhangjh/download/src/libcxx/llvm-90/libcxxabi/ \
    ..
  ```

  **编译成功：**

  ![image-20210113184704959](http://media.sumblog.cn/uPic/2021-01-13image-20210113184704959NKGuWg.png)

  之后使用 命令 `make install` 安装编译好的 klee，安装完成后，检测版本：

  ![image-20210113190245117](http://media.sumblog.cn/uPic/2021-01-13image-20210113190245117jgZTMq.png)

## 2.2 使用 KLEE 进行符号测试

编写一段测试代码，该段代码对输入的符号 x，进行判断，比较其和 0 的大小：

```
int get_sign(int x) {
  if (x == 0)
     return 0;

  if (x < 0)
     return -1;
  else
     return 1;
}

int main() {
  int a;
  klee_make_symbolic(&a, sizeof(a), "a");
  return get_sign(a);
}
```

为了使用 KLEE 进行测试，使用klee_make_symbolic()函数，对需要测试的变量进行标记。

- 之后，使用 LLVM 将源代码编译成 LLVM bitcode

  ```
  clang -I ../../include -emit-llvm -c -g -O0 -Xclang -disable-O0-optnone get_sign.c
  ```

- 然后使用 klee 进行测试：

  ```
  klee get_sign.bc
  ```

  ![image-20210113194434782](http://media.sumblog.cn/uPic/2021-01-13image-20210113194434782iqXGTQ.png)

  可以看到，KLEE 判断出程序存在三条路径，生成了 3 个测试用例

  可以在 klee-last 目录下，看到生成的测试集：

  ![image-20210113194554572](http://media.sumblog.cn/uPic/2021-01-13image-20210113194554572y7cMQH.png)



# 3. 代码动态分析



## 3.1 Hadoop 编译安装

- 下载 Hadoop 源代码：

  ![image-20210114091346521](http://media.sumblog.cn/uPic/2021-01-14image-20210114091346521my0i0F.png)

  这里选择下载 Hadoop 3.3.0 版本源代码：`wget https://apache.claz.org/hadoop/common/hadoop-3.3.0/hadoop-3.3.0-src.tar.gz`

- 解包，编译安装，按照源码包中的 BUILDING.txt 说明进行编译：

  ```
  Build instructions for Hadoop
  
  ----------------------------------------------------------------------------------
  Requirements:
  
  * Unix System
  * JDK 1.8
  * Maven 3.3 or later
  * Protocol Buffers 3.7.1 (if compiling native code)
  * CMake 3.1 or newer (if compiling native code)
  * Zlib devel (if compiling native code)
  * Cyrus SASL devel (if compiling native code)
  * One of the compilers that support thread_local storage: GCC 4.8.1 or later, Visual Studio,
    Clang (community version), Clang (version for iOS 9 and later) (if compiling native code)
  * openssl devel (if compiling native hadoop-pipes and to get the best HDFS encryption performance)
  * Linux FUSE (Filesystem in Userspace) version 2.6 or above (if compiling fuse_dfs)
  * Doxygen ( if compiling libhdfspp and generating the documents )
  * Internet connection for first build (to fetch all Maven and Hadoop dependencies)
  * python (for releasedocs)
  * bats (for shell code testing)
  * Node.js / bower / Ember-cli (for YARN UI v2 building)
  
  ```

  由于编译环境要求较为复杂，从 Hadoop 3.0 开始，官方建议通过配置好编译环境的 docker 进行编译

  部署 docker 容器， `./start_build_env.sh`  脚本会从Docker hub网站拉取hadoop-build镜像，然后在镜像中添加本地用户信息和环境变量等，最后运行Docker镜像并挂载本地目录。

- Docker 编译环境部署好之后，进入编译环境中：

  ![image-20210114095225167](http://media.sumblog.cn/uPic/2021-01-14image-20210114095225167VXWv5H.png)

  之后进行编译：

  > Create binary distribution with native code and with documentation:
  >
  >   $ mvn package -Pdist,native,docs -DskipTests -Dtar

  - 报错，提示：[ERROR] Could not create local repository at /home/zhangjh/.m2/repository -> [Help 1]

  - 排查是 `.m2` 目录权限问题，修改目录所有者为当前用户：

    ![image-20210114095924396](http://media.sumblog.cn/uPic/2021-01-14image-20210114095924396CdUNDm.png)

- 编译成功：

  ![image-20210115090031095](http://media.sumblog.cn/uPic/2021-01-15image-20210115090031095f0enfX.png)

   编译后生成的文件位于 `~/hadoop/hadoop-dist/target/hadoop-3.3.0`

  ![image-20210115090427183](http://media.sumblog.cn/uPic/2021-01-15image-20210115090427183pwOQN3.png)

- 安装：

  1. NameNode 数据存放目录： /usr/local/data/hadoop/name
  2. SecondaryNameNode 数据存放目录： /usr/local/data/hadoop/secondary
  3. DataNode 数据存放目录： /usr/local/data/hadoop/data
  4. 临时数据存放目录： /usr/local/data/hadoop/tmp

  ```
  mkdir -p /usr/local/data/hadoop/name
  mkdir -p /usr/local/data/hadoop/secondary
  mkdir -p /usr/local/data/hadoop/data
  mkdir -p /usr/local/data/hadoop/tmp
  ```

## 3.2 Hadoop 配置

格式化hadoop文件系统

```
cd /usr/local/hadoop
./bin/hdfs namenode -format
复制代码
```

启动 dfs

```
./sbin/start-dfs.sh
```

全部启动：

```
./sbin/start-all.sh
```

![image-20210114165535799](http://media.sumblog.cn/uPic/2021-01-14image-20210114165535799LNiPlv.png)

- 创建 HDFS 数据目录：

  -  创建一个目录，用于保存MapReduce任务的输入文件：

    ```
    hadoop fs -mkdir -p /data/wordcount
    ```

  - 创建目录，保存 MapReduce 任务输出文件

    ```
    hadoop fs -mkdir /output
    ```

  - 上传单词文件到 HDFS

    ```bash
    hadoop fs -put myword.txt /data/wordcount
    ```

- 运行 mapreduce wordcount 实例程序：

  ```
  hadoop jar ./hadoop-mapreduce-examples-3.3.0.jar wordcount /data/wordcount /output/wordcount
  ```

  可以看到，直接运行原始 jar 包，可以运行成功：

  ![image-20210114170924654](http://media.sumblog.cn/uPic/2021-01-14image-20210114170924654m3NIfO.png)

## 3.3 java agent 编写

- 创建一个新的 MAVEN 项目

- 编写 Agent 类：

  ```java
  public class Agent {
      /**
       * jvm 参数形式启动，运行此方法
       *
       * @param agentArgs
       * @param inst
       */
      public static void premain(String agentArgs, Instrumentation inst) {
          System.out.println("premain");
          customLogic(inst);
      }
  
      /**
       * 动态 attach 方式启动，运行此方法
       *
       * @param agentArgs
       * @param inst
       */
      public static void agentmain(String agentArgs, Instrumentation inst) {
          System.out.println("agentmain");
          customLogic(inst);
      }
      /**
       * 统计方法耗时
       *
       * @param inst
       */
      private static void customLogic(Instrumentation inst) {
          inst.addTransformer(new CostTransformer(), true);
      }
  
  }
  ```

- 编写 CostTransformer 类：

  ```java
  public class CostTransformer implements ClassFileTransformer {
      @Override
      public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain, byte[] classfileBuffer) {
          // 只针对目标包下进行耗时统计
          if (!className.startsWith("org/apache/hadoop")) {
              return classfileBuffer;
          }
  
          CtClass cl = null;
          try {
              ClassPool classPool = ClassPool.getDefault();
              cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
  
              for (CtMethod method : cl.getDeclaredMethods()) {
                  // 所有方法，统计耗时；请注意，需要通过`addLocalVariable`来声明局部变量
                  method.addLocalVariable("start", CtClass.longType);
                  method.insertBefore("start = System.currentTimeMillis();");
                  String methodName = method.getLongName();
                  method.insertAfter("System.out.println(\"" + methodName + " cost: \" + (System" +
                          ".currentTimeMillis() - start));");
              }
  
              byte[] transformed = cl.toBytecode();
              return transformed;
          } catch (Exception e) {
              e.printStackTrace();
          }
          return classfileBuffer;
      }
  }
  ```

- 通过 maven 插件，可以比较简单的输出一个合规的 java agent 包，修改 pom.xml 文件如下：

  ```xml
  <build>
          <plugins>
              <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-assembly-plugin</artifactId>
                  <configuration>
                      <descriptorRefs>
                          <descriptorRef>jar-with-dependencies</descriptorRef>
                      </descriptorRefs>
                      <archive>
                          <manifestEntries>
                              <Premain-Class>Agent</Premain-Class>
                              <Agent-Class>Agent</Agent-Class>
                              <Can-Redefine-Classes>true</Can-Redefine-Classes>
                              <Can-Retransform-Classes>true</Can-Retransform-Classes>
                          </manifestEntries>
                      </archive>
                  </configuration>
  
                  <executions>
                      <execution>
                          <goals>
                              <goal>attached</goal>
                          </goals>
                          <phase>package</phase>
                      </execution>
                  </executions>
              </plugin>
              <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-compiler-plugin</artifactId>
                  <configuration>
                      <source>6</source>
                      <target>6</target>
                  </configuration>
              </plugin>
          </plugins>
      </build>
  ```

- 然后通过 `mvn assembly:assembly` 命令打包，在`target`目录下，可以看到一个后缀为`jar-with-dependencies`的 jar 包，就是我们的目标

## 3.4 启用 Java agent 对 word count 例子进行分析

**设置环境变量：**

```
export HADOOP_OPTS='-javaagent:/home/zhangjh/MapReduce-agent-1.0-SNAPSHOT-jar-with-dependencies.jar'
```

- 运行 mapreduce wordcount 实例程序：

  ```
  hadoop jar ./hadoop-mapreduce-examples-3.3.0.jar wordcount /data/wordcount /output/wordcount
  ```

- 在启用环境变量之后，向 hadoop 提交任务，可以看到输出了方法名，执行时间，开始时间戳，线程号：

  ![image-20210114185703778](/Users/zhangjh/Library/Application Support/typora-user-images/image-20210114185703778.png)

- 对输出内容进行分析，使用 Excel 对方法耗时进行排序，部分结果如下：

  ![image-20210114190612240](http://media.sumblog.cn/uPic/2021-01-14image-20210114190612240rjHrwe.png)

  可以看到，耗时较长的方法有 `waitForCompletion(boolean)`、`Connection.waitForWork()`、`mapreduce.Job.connect()` 等等，其中`Connection.waitForWork` 调用次数较多

  - 只有方法名，方法耗时，时间戳 信息，较难分析出性能瓶颈，如果能分析出各方法的调用关系，能够更好的进行分析



