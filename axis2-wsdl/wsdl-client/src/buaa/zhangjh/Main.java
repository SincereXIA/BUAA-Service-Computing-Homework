package buaa.zhangjh;
import org.apache.axis2.AxisFault;
import org.apache.ws.ax2.PersonStatefulStub;
import org.apache.ws.axis2.PersonStub;

import java.rmi.RemoteException;

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



	// write your code here
    }
}
