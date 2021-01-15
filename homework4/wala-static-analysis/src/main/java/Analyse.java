import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.io.IOException;

public class Analyse {
    public static void main(String args[]) throws IOException, ClassHierarchyException, IllegalArgumentException, InvalidClassFileException, CancelException {
        String hbaseClient = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/hbase/hbase-client/target/hbase-client-3.0.0-SNAPSHOT.jar";
        String hbaseServer = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/hbase/hbase-server/target/hbase-server-3.0.0-SNAPSHOT.jar";
        String jarPath = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/cassandra/build/apache-cassandra-4.0-beta4-SNAPSHOT.jar";
        // 获得一个文件
        File exFile=new FileProvider().getFile("Java60RegressionExclusions.txt");

        // 将分析域存到文件中
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(hbaseServer, exFile);

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
