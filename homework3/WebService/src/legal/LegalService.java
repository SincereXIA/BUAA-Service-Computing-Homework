package legal;

import travel.IdcardUtils;

public class LegalService{
	public int isLegal(String id) {
		IdcardUtils utils = new IdcardUtils();
		if(utils.validateCard(id)) {
			return 1;
		}
		return -1;
	}
}
