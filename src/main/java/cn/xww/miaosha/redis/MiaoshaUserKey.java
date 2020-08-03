package cn.xww.miaosha.redis;

public class MiaoshaUserKey extends BasePrefix{
//有效期
	public static final int TOKEN_EXPIRE = 3600*24 * 2;
	private MiaoshaUserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");
	//0,表示永久有效
	public static MiaoshaUserKey getById = new MiaoshaUserKey(0, "id");
}
