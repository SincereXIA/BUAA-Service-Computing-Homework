package age;

import travel.IdcardUtils;

public class AgeService {
	public int getAge(String cardID) {
		return IdcardUtils.getAgeByIdCard(cardID);
	}
}
