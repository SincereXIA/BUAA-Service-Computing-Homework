package buaa.zhangjh.restfuldemo;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/person/sayhello")
    public String sayHello() {
        return "Hello, I'm " + p.name;
    }

    @RequestMapping(value = "/person/age", method = RequestMethod.PUT)
    public boolean setAge(@RequestParam(value = "age")
                                   String age) {
        p.setAge(Integer.parseInt(age));
        return true;
    }


    @RequestMapping(value = "/person/gender", method = RequestMethod.PUT)
    public boolean setGender(@RequestParam(value = "gender")
                                   String gender) {
        p.setGender(Boolean.parseBoolean(gender));
        return true;
    }

    @RequestMapping(value = "/person/name", method = RequestMethod.PUT)
    public boolean setName(@RequestParam(value = "name")
                          String name) {
        p.setName(name);
        return true;
    }
}
