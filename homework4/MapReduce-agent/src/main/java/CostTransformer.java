import javassist.*;
import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

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
                method.addLocalVariable("pid", CtClass.longType);
                method.insertBefore("start = System.currentTimeMillis(); " +
                        "pid = Thread.currentThread().getId();");
                String methodName = method.getLongName();
                method.insertAfter("System.err.println(\"" + methodName + " , \" + (System" +
                        ".currentTimeMillis() - start) + \" , \" + start + \" ,  \" + pid);");
            }

            byte[] transformed = cl.toBytecode();
            return transformed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}
