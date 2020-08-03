package cn.xww.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import cn.xww.miaosha.dao.MiaoshaUserDao;
import cn.xww.miaosha.domain.MiaoshaUser;
import cn.xww.miaosha.exception.GlobalException;
import cn.xww.miaosha.redis.MiaoshaUserKey;
import cn.xww.miaosha.redis.RedisService;
import cn.xww.miaosha.result.CodeMsg;
import cn.xww.miaosha.util.MD5Util;
import cn.xww.miaosha.util.UUIDUtil;
import cn.xww.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class MiaoshaUserService {
	
	
	public static final String COOKI_NAME_TOKEN = "token";
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;
	
//	public MiaoshaUser getById(long id) {
//		return miaoshaUserDao.getById(id);
//	}
public MiaoshaUser getById(long id) {
	//1.取缓存	---先根据id来取得缓存
	MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
	//缓存非空直接给客户端
	if(user != null) {
		return user;
	}
	//缓存为空，取数据库
	user = miaoshaUserDao.getById(id);
	if(user != null) {
		redisService.set(MiaoshaUserKey.getById, ""+id, user);
	}
	return user;
}
	// http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323

	/**
	 * 更新用户密码：更新数据库与缓存，一定保证数据一致性，
	 * 修改token关联的对象以及id关联的对象，先更新数据库后删除缓存，
	 * 不能直接删除token，删除之后就不能登录了，再将token以及对应的用户信息一起再写回缓存里面去
	 * @param
	 * @param token
	 * @return
	 */
	public boolean updatePassword(String token, long id, String formPass) {
		//取user
		MiaoshaUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		miaoshaUserDao.update(toBeUpdate);
		//3.更新数据库与缓存，数据库更新缓存一定也要更新，一定保证数据一致性，修改token关联的对象以及id关联的对象
		redisService.delete(MiaoshaUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		//不能直接删除token，删除之后就不能登录了
		redisService.set(MiaoshaUserKey.token, token, user);
		return true;
	}
	

	public MiaoshaUser getByToken(HttpServletResponse response,String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user =  redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
	

	public String login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		System.out.println(dbPass);
		String saltDB = user.getSalt();
		System.out.println(saltDB);
		//之前在login.html的doLogin()已经加密了一次
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		System.out.println(calcPass);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//登录成功后，给用户生成一个token标识此用户写入cookie传递给客户端，客户端在随后的访问中在
		// cookie中上传此token，服务端根据此token取用户的session信息
		// 生成cookie
		String token= UUIDUtil.uuid(); //随机
		//1.将token与其对应用户房间第三方缓存redis；2.写入cookie
		addCookie(response, token, user);
		return token;

	}
	
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
		//cookie有效期
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		cookie.setPath("/");
		//写入response
		response.addCookie(cookie);
	}

}
