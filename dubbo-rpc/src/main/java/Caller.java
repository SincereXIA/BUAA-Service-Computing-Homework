

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