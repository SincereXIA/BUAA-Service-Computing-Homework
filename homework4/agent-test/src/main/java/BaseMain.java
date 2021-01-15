public class BaseMain {
    public static void main(String[] args) throws InterruptedException {
        DemoClz demoClz = new DemoClz();
        int cnt = 0;
        for (int i = 0; i < 20; i++) {
            if (++cnt % 2 == 0) {
                i = demoClz.print(i);
            } else {
                i = demoClz.count(i);
            }
        }
    }
}
