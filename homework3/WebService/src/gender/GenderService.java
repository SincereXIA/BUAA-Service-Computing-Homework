package gender;

import travel.IdcardUtils;

public class GenderService {
	public String getSex(String id) {
		return IdcardUtils.getGenderByIdCard(id);
	}
}
