package kr.co.aim.messolution.userprofile.management.data;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class UserInvisibleButton extends UdfAccessor implements DataInfo<UserInvisibleButtonKey> {

	private UserInvisibleButtonKey key;

	public UserInvisibleButtonKey getKey()
	{
		return key;
	}

	public void setKey(UserInvisibleButtonKey key)
	{
		this.key = key;
	}

}
