package buaa.zhangjh.wsdldemo;

import javax.jws.WebService;

@WebService (
        targetNamespace = "buaa.zhangjh.wsdldemo",
        name= "CalculatorPortType",
        serviceName = "calculatorService",
        portName = "calculatorPortName",
        endpointInterface = "buaa.zhangjh.wsdldemo.CalculatorService"
)
public class CalculatorServiceImpl  implements CalculatorService{
    @Override
    public int add(int a, int b) {
        return a+b;
    }
}
