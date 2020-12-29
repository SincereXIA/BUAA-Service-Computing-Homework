package province;

import travel.IdcardUtils;

public class ProvinceService {
	public String getProvince(String id) {
		return IdcardUtils.getProvinceByIdCard(id);
	}
}
