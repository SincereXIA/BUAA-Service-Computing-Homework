package travel;

public class payService {
	public double pay(String key) {
		if (key.equals("password")) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
