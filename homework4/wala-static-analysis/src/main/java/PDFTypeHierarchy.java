import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.examples.properties.WalaExamplesProperties;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.PDFViewUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * This simple example WALA application builds a TypeHierarchy and fires off ghostview to viz a DOT
 * representation.
 *
 * @author sfink
 */
public class PDFTypeHierarchy {
    private static final String EXCLUSIONS = "java\\/awt\\/.*\n" +
            "javax\\/swing\\/.*\n" +
            "sun\\/awt\\/.*\n" +
            "sun\\/swing\\/.*\n" +
            "com\\/sun\\/.*\n" +
            "sun\\/.*\n" +
            "org\\/netbeans\\/.*\n" +
            "org\\/openide\\/.*\n" +
            "com\\/ibm\\/crypto\\/.*\n" +
            "com\\/ibm\\/security\\/.*\n" +
            "org\\/apache\\/xerces\\/.*\n" +
            "java\\/security\\/.*\n" +
            "java\\/util\\/.*\n" +
            "java\\/io\\/.*\n" +
            "java\\/lang\\/.*\n" +
            "";

    // This example takes one command-line argument, so args[1] should be the "-classpath" parameter
    static final int CLASSPATH_INDEX = 1;

    public static final String DOT_FILE = "hbaseServer.dt";

    private static final String PDF_FILE = "hbaseServer.pdf";

    public static Properties p;

    static {
        try {
            p = WalaProperties.loadProperties();
            p.putAll(WalaExamplesProperties.loadProperties());
        } catch (WalaException e) {
            e.printStackTrace();
            Assertions.UNREACHABLE();
        }
    }

    public static void main(String[] args) throws IOException {
        run(args);
    }

    public static Process run(String[] args) throws IOException {
        String OUTPUT_DIR = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/output";
        try {
            String hbaseClient = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/hbase/hbase-client/target/hbase-client-3.0.0-SNAPSHOT.jar";
            String hbaseServer = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/hbase/hbase-server/target/hbase-server-3.0.0-SNAPSHOT.jar";
            String classpath = "/Users/zhangjh/workspace/homework/BUAA-Service-Computing-Homework/homework4/cassandra/build/apache-cassandra-4.0-beta4-SNAPSHOT.jar";
            AnalysisScope scope =
                    AnalysisScopeReader.makeJavaBinaryAnalysisScope(
                            hbaseServer, (new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

           // scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes()))); // 排除部分类
            // invoke WALA to build a class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);

            Graph<IClass> g = typeHierarchy2Graph(cha);

            g = pruneForAppLoader(g);
            String dotFile = OUTPUT_DIR + File.separatorChar + DOT_FILE;
            String pdfFile = OUTPUT_DIR + File.separatorChar + PDF_FILE;
            String dotExe = "/usr/local/bin/dot";
            String gvExe = p.getProperty(WalaExamplesProperties.PDFVIEW_EXE);
            DotUtil.dotify(g, null, dotFile, pdfFile, dotExe);
            return PDFViewUtil.launchPDFView(pdfFile, gvExe);

        } catch (WalaException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> Graph<T> pruneGraph(Graph<T> g, Predicate<T> f) {
        Collection<T> slice = GraphSlicer.slice(g, f);
        return GraphSlicer.prune(g, new CollectionFilter<>(slice));
    }

    /** Restrict g to nodes from the Application loader */
    public static Graph<IClass> pruneForAppLoader(Graph<IClass> g) {
        Predicate<IClass> f =
                c -> (c.getClassLoader().getReference().equals(ClassLoaderReference.Application));
        return pruneGraph(g, f);
    }

    /**
     * Validate that the command-line arguments obey the expected usage.
     *
     * <p>Usage: args[0] : "-classpath" args[1] : String, a ";"-delimited class path
     *
     * @throws UnsupportedOperationException if command-line is malformed.
     */
    public static void validateCommandLine(String[] args) {
        if (args.length < 2) {
            throw new UnsupportedOperationException("must have at least 2 command-line arguments");
        }
        if (!args[0].equals("-classpath")) {
            throw new UnsupportedOperationException(
                    "invalid command-line, args[0] should be -classpath, but is " + args[0]);
        }
    }
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
}