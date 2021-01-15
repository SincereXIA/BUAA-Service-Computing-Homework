public class DemoClz {
    public int print(int i) {
        System.out.println("i: " + i);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return i + 2;
    }

    public int count(int i) {
        System.out.println("cnt: " + i);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return i + 1;
    }
}
