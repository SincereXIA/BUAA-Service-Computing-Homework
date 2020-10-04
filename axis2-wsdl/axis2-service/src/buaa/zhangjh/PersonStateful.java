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
        //  第1步：设置key-value对
        MessageContext mc = MessageContext.getCurrentMessageContext();
        ServiceGroupContext sgc = mc.getServiceGroupContext();
        sgc.setProperty("name", name);
    }

    public String getName() {
        //  第1步：获得key-value对中的value
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
        //  第1步：获得key-value对中的value
        MessageContext mc = MessageContext.getCurrentMessageContext();
        ServiceGroupContext sgc =  mc.getServiceGroupContext();
        String name = (String) sgc.getProperty("name");
        String rs = String.format("Hello! %s", name);
        return  rs;
    }
}
