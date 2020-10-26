package buaa.zhangjh.wsdldemo;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(targetNamespace = "buaa.zhangjh.wsdldemo", name = "CalculatorPortType")
public interface CalculatorService {
    @WebMethod(operationName = "add")
    int add(@WebParam(name = "a") int a, @WebParam(name = "b") int b);
}
