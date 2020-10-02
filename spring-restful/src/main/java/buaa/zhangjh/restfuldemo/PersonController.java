package buaa.zhangjh.restfuldemo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.TrustAnchor;

@RestController
public class PersonController {

    @GetMapping("/person")
    public Person person(@RequestParam(value="name", defaultValue = "Zhangjh")
                         String name,
                         @RequestParam(value="age", defaultValue = "22")
                         int age,
                         @RequestParam(value="gender", defaultValue = "1")
                         boolean gender){
        return new Person(name, age, gender);
    }
}
