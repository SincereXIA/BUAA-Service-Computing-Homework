package travel;

public class payService {
	double money = 1000;
	public boolean pay(double cost) {
		if (cost <= money) {
			money = money-cost;
			return true;
		}
		else {
			return false;
		}
	}
}
