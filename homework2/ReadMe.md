# 服务计算 作业 2：服务 QoS 预测

SY2006127 张俊华

## 程序运行环境搭建

WS-DREAM 项目依赖的运行环境为：

- Python 2.7 ([https://www.python.org](https://www.python.org/))
- Cython 0.20.1 ([http://cython.org](http://cython.org/))
- numpy 1.8.1 ([http://www.scipy.org](http://www.scipy.org/))
- scipy 0.13.3 ([http://www.scipy.org](http://www.scipy.org/))
- AMF (https://github.com/wsdream/AMF)
- PPCF (https://github.com/wsdream/PPCF)

该项目使用 Python 2.7 版本，为了避免操作系统现在的 Python 版本对其干扰，使用 Anaconda 创建 python 版本为 2.7 的虚拟环境：

1. 使用anaconda，创建 python2.7 运行环境：

   <img src="http://media.sumblog.cn/uPic/2020-11-20jmCqyw4osvtd.png" alt="2020-11-20jmCqyw4osvtd" style="zoom: 50%;" />

2. 环境创建好后，先安装Python的基础依赖，该项目依赖的 Python 包写在项目根目录下的 `requirements.txt` 文件中，使用命令 `pip install -r requirements.txt` 完成安装

   ```
   numpy==1.8.1
   scipy==0.13.3
   -e git+https://github.com/wsdream/AMF.git#egg=AMF
   -e git+https://github.com/wsdream/PPCF.git#egg=PPCF
   ```

3. 安装 package:  使用命令`python setup.py install --user`

   该命令使用 cython，对 wsdream 目录下的各个模块完成编译

   <img src="http://media.sumblog.cn/uPic/2020-11-20xbwsHc2pn6I1.png" alt="2020-11-20xbwsHc2pn6I1" style="zoom:50%;" />

   编译完成后，自动将编译出的库文件拷贝到 python 的 site-packages 目录下，以便后续调用

   <img src="http://media.sumblog.cn/uPic/2020-11-20QPdD2pUePhQ3.png" alt="2020-11-20QPdD2pUePhQ3" style="zoom:50%;" />
   
   至此，运行环境搭建完成

## QoS 预测方法测试

### 1. PMF 方法：

PMF 为 Model-based 方法。该方法会根据可用的Web服务故障数据建立一个因子模型，并使用该因子模型进行进一步的可靠性预测。

**运行结果：**

<img src="http://media.sumblog.cn/uPic/2020-11-20mMZ2ExOJITqM.png" alt="2020-11-20mMZ2ExOJITqM" style="zoom:50%;" />

**方法原理**

1. 对 user-item 矩阵 P ，进行分解，分解为两个矩阵 W 和 H
   $$
   P \approx WH
   $$
   W 中的每一行是用户的特定系数，H 中的每一列是 Web 服务的因子向量

   例如对于矩阵 P：

   <img src="http://media.sumblog.cn/uPic/2020-11-20gY5xbuK4XUAN.png" alt="2020-11-20gY5xbuK4XUAN" style="zoom:50%;" />

   可以将其分解为：

   <img src="http://media.sumblog.cn/uPic/2020-11-20tOqDohcni5mU.png" alt="2020-11-20tOqDohcni5mU" style="zoom: 50%;" />

   通过矩阵 W 和 H 的乘积，计算 P 中空缺的部分，完成 Qos 预测。

   <img src="http://media.sumblog.cn/uPic/2020-11-20kcaOFreHWRpJ.png" alt="2020-11-20kcaOFreHWRpJ" style="zoom:50%;" />

   矩阵 W 和 H 通常不唯一，通过使 WH 和 P 之间距离最小化来确定，文章中使用平方误差的方法，计算 P 和 WH 的差异：

   <img src="http://media.sumblog.cn/uPic/2020-11-20zsE9BhfvLWlP.png" alt="2020-11-20zsE9BhfvLWlP" style="zoom:50%;" />

   但为了避免过拟合，需要增加惩罚函数，最终可以将优化问题归结为如下目标函数
   
   <img src="http://media.sumblog.cn/uPic/2020-11-20sF6SlcSxGsSR.png" alt="2020-11-20sF6SlcSxGsSR" style="zoom:50%;" />
   
2. 使用梯度下降的方法，找到目标函数的局部最小值，文章给出的算法流程为：

   <img src="http://media.sumblog.cn/uPic/2020-11-2016An3zZZeIrP.png" alt="2020-11-2016An3zZZeIrP" style="zoom:50%;" />

   其中，梯度可以通过下式得到：

   <img src="http://media.sumblog.cn/uPic/2020-11-20QTJkXPpwYdcc.png" alt="2020-11-20QTJkXPpwYdcc" style="zoom: 50%;" />

   首先用小的随机数初始化矩阵 W 和 H，之后通过梯度下降的方法，迭代更新矩阵 W 和 H，参数 $\alpha$  为学习率，控制迭代的速度。迭代停止时，计算出目标函数取得极小值处的 W 和 H，进行 QoS 预测

### 2. NTF 方法：

**运行结果**

![2020-11-21sORWGAklYUFj](http://media.sumblog.cn/uPic/2020-11-21sORWGAklYUFj.png)

**方法原理**

该文章将两个二维用户服务矩阵扩展为一个以三维张量表示的更复杂的用户服务时间三元关系，并提出了一种基于矩阵分解的广义张量因子分解（TF）。通过考虑不同时间QoS值的差异，使用 用户-服务-时间 关系替换用户-服务矩阵，来进行 QoS 预测。在真实世界中，QoS 值永远为非负数，因此，文章提出了一种非负张量因子分解（NTF）方法，以在考虑服务调用时间的情况下预测Web服务QoS值，并提出了一种基于时间QoS的Web服务推荐框架。

- 下图展示了如何将二维的矩阵，增加时间维度，构建 用户-服务-时间 矩阵：

  ![2020-11-210PLnge98haj4](http://media.sumblog.cn/uPic/2020-11-210PLnge98haj4.png)

  该三维矩阵的构造方法如下：

  ![2020-11-21pgO9Y7OOIajN](http://media.sumblog.cn/uPic/2020-11-21pgO9Y7OOIajN.png)

  

- NTF 方法流程如下：

  ![2020-11-21ttNCgNZHceJD](http://media.sumblog.cn/uPic/2020-11-21ttNCgNZHceJD.png)

  ![2020-11-213c1jMRwtkLUu](http://media.sumblog.cn/uPic/2020-11-213c1jMRwtkLUu.png)

  ![2020-11-21DPH4wgQKOb6r](http://media.sumblog.cn/uPic/2020-11-21DPH4wgQKOb6r.png)

  

  

### 3. CloudPred 方法

**运行结果**

![2020-11-20CmzJJrpTxpNm](http://media.sumblog.cn/uPic/2020-11-20CmzJJrpTxpNm.png)

![2020-11-20pCQKIiZOLCA6](http://media.sumblog.cn/uPic/2020-11-20pCQKIiZOLCA6.png)

**方法原理**

- CloudPred 是基于邻域的方法，CloudPred通过非负矩阵分解（NMF）了解用户的特征，并探索类似用户的QoS体验，以实现较高的QoS值预测准确性。

- 该方法将 QoS 矩阵分解为两个低秩矩阵 V 和 H，V 中的每一列代表用户的 l 维特征向量，H 中的每一列代表组件的特征向量。使用近似矩阵 $W\approx V^TH$ 来拟合

- 矩阵 V 和 H 是未知的，通过矩阵 W 中获得的 QoS 值学习，通过近似矩阵和原矩阵的距离，定义成本函数

  <img src="http://media.sumblog.cn/uPic/2020-11-20vlLGMxukhnkS.png" alt="2020-11-20vlLGMxukhnkS" style="zoom:50%;" />

- 文章中采用的目标函数为：

  <img src="http://media.sumblog.cn/uPic/2020-11-20BN1IXEQnNfz2.png" alt="2020-11-20BN1IXEQnNfz2" style="zoom:50%;" />

  为了使目标函数最小化，使用梯度下降的方法，计算局部最小值

  <img src="http://media.sumblog.cn/uPic/2020-11-20zJbUqV5eCAYv.png" alt="2020-11-20zJbUqV5eCAYv" style="zoom:50%;" />

  

  **相似度计算**

  得到用户和组件特征矩阵 V 和 H 后，使用Person 相关系数，计算不同用户和组件的邻域相似度，

  <img src="http://media.sumblog.cn/uPic/2020-11-21YFRhz8tGGbB6.png" alt="2020-11-21YFRhz8tGGbB6" style="zoom:50%;" />

  <img src="http://media.sumblog.cn/uPic/2020-11-21BPYvhsUbXVj3.png" alt="2020-11-21BPYvhsUbXVj3" style="zoom:50%;" />

**QoS 预测**

通过对相似度进行排序来识别当前用户的相似邻居。较少相似或不相似用户的QoS可能会大大降低预测准确性。 文章中，我们从相似的邻居集中排除PCC值为负的那些用户，而仅使用Top-K最大PCC值的的QoS来预测当前用户的QoS值。 

<img src="http://media.sumblog.cn/uPic/2020-11-21ro0H1SSi28uR.png" alt="2020-11-21ro0H1SSi28uR" style="zoom:50%;" />

<img src="http://media.sumblog.cn/uPic/2020-11-21ja0mFPAYdGgn.png" alt="2020-11-21ja0mFPAYdGgn" style="zoom:50%;" />

<img src="http://media.sumblog.cn/uPic/2020-11-217CDRWbTvbILs.png" alt="2020-11-217CDRWbTvbILs" style="zoom:50%;" />

CloudPred 预测算法，混合了基于用户的预测方法和基于组件的方法，其算法如下：

<img src="http://media.sumblog.cn/uPic/2020-11-21fYjxu8YFTzTc.png" alt="2020-11-21fYjxu8YFTzTc" style="zoom:50%;" />



### 4. UIPCC

**运行结果**

<img src="http://media.sumblog.cn/uPic/2020-11-21vqeon5Y2JE2R.png" alt="2020-11-21vqeon5Y2JE2R" style="zoom:50%;" />



**方法原理**

UIPCC 使用协作过滤的方法，利用服务用户的过去使用经验来预测 Web 服务的 QoS 值，进行服务推荐

1. 使用 Person 相关系数来计算两个服务用户 a、u 之间的相似度：

   <img src="http://media.sumblog.cn/uPic/2020-11-21NIVG0scXMwzy.png" alt="2020-11-21NIVG0scXMwzy" style="zoom:50%;" />

   计算两个 Web 服务之间的相似度：

   <img src="http://media.sumblog.cn/uPic/2020-11-21wF9Qnmh54CnI.png" alt="2020-11-21wF9Qnmh54CnI" style="zoom:50%;" />

   文章中认为 PCC 会高估一些实际上并不相似，但恰巧有相似 QoS 的 Web 服务的相似度，因此，文章提出了改进的 PCC 计算定义：

   <img src="http://media.sumblog.cn/uPic/2020-11-21WdGfbvaBdzel.png" alt="2020-11-21WdGfbvaBdzel" style="zoom:50%;" />

2. user-item 矩阵通常非常稀疏，这将极大的影响预测进度，文章提出了一种使得矩阵更密集的缺失值预测方法

   - 传统的 Top-K 算法根据 PCC 相似度对邻居进行排名，选择前 k 个相似的邻居进行缺失值预测，但实际上，用户项矩阵中，某些项可能邻居很少，甚至没有邻居，这将大大降低预测精度

   - 文章提出了增强的 Top K 算法，排除 PCC 相似度小于或等于 0 的邻居

     <img src="http://media.sumblog.cn/uPic/2020-11-21PYOcZSO10t14.png" alt="2020-11-21PYOcZSO10t14" style="zoom: 50%;" />

   - QoS 值的预测公式：

     - 基于用户的方法：

     <img src="http://media.sumblog.cn/uPic/2020-11-21j4ZKfOgGQXEk.png" alt="2020-11-21j4ZKfOgGQXEk" style="zoom:50%;" />

     - 基于项目的方法：

       <img src="http://media.sumblog.cn/uPic/2020-11-21gvLwa9oahAJj.png" alt="2020-11-21gvLwa9oahAJj" style="zoom:50%;" />

       

     - 两方法结合：

       <img src="http://media.sumblog.cn/uPic/2020-11-21kecPItkXIcrG.png" alt="2020-11-21kecPItkXIcrG" style="zoom:50%;" />

       <img src="http://media.sumblog.cn/uPic/2020-11-21oHJdQPJUwriB.png" alt="2020-11-21oHJdQPJUwriB" style="zoom:50%;" />

       <img src="http://media.sumblog.cn/uPic/2020-11-21Gqbd3EMKBtly.png" alt="2020-11-21Gqbd3EMKBtly" style="zoom:50%;" />

       $con_u$ 和 $con_i$ 可以自动平衡基于用户的预测和基于项目的预测，提高了方法适用于不同数据集的可行性。

###  5. EMF 方法

**运行结果**

![image-20201130111421588](http://media.sumblog.cn/uPic/2020-11-30image-20201130111421588HWVcue.png)

![image-20201130171541278](http://media.sumblog.cn/uPic/2020-11-30image-20201130171541278uVzXCp.png)

**方法原理**

文章中，定义 user-service 矩阵为 R，目标将矩阵 R 分解为 U 和 S 两个矩阵：
$$
R \approx U^TS
$$
目标使得估计的矩阵 R 更逼近原始矩阵，归结为最小化问题：

![2020-11-21MNWviPEXfmVK](http://media.sumblog.cn/uPic/2020-11-21MNWviPEXfmVK.png)

现实情况下，原始矩阵 R 是一个稀疏矩阵，使用 $I_{ij}$ 标记用户 $u_i$，与服务$s_j$ 是否进行了交互，原始问题修改为以下问题：

![2020-11-21F3Xzi4uEbesz](http://media.sumblog.cn/uPic/2020-11-21F3Xzi4uEbesz.png)

为了避免过拟合，进行正则化：

![2020-11-21ZO7mjS8LTnP0](http://media.sumblog.cn/uPic/2020-11-21ZO7mjS8LTnP0.png)

EMF 融合了 user 和 service 进行 QoS 预测，

![2020-11-21XF1WLwkdNgpU](http://media.sumblog.cn/uPic/2020-11-21XF1WLwkdNgpU.png)

之后使用梯度下降的方法，寻找局部最优解。









