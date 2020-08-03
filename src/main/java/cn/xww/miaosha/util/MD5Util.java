package cn.xww.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	
	public static String md5(String src) {
		//做一次MD5
		return DigestUtils.md5Hex(src);
	}
	
	private static final String salt = "1a2b3c4d";

	//第一次，加密用户输入的密码，给form表单
	public static String inputPassToFormPass(String inputPass) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
		System.out.println(str);
		return md5(str);
	}
	//将form传过来的再MD5给数据库
	public static String formPassToDBPass(String formPass, String salt) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);//拼完再MD5
	}
	//用户输入的密码转换成数据库密码
	public static String inputPassToDbPass(String inputPass, String saltDB) {
		String formPass = inputPassToFormPass(inputPass);
		String dbPass = formPassToDBPass(formPass, saltDB);
		return dbPass;
	}
	
	public static void main(String[] args) {
		//System.out.println(inputPassToFormPass("123456"));//d3b1294a61a07da9b49b6e22b2cbd7f9
		//System.out.println(formPassToDBPass(inputPassToFormPass("c38dc3dcb8f0b43ac8ea6a70b5ec7648"), "1a2b3c4d"));
		System.out.println(inputPassToDbPass("123456", "1a2b3c4d"));//b7797cce01b4b131b433b6acf4add449
		System.out.println(inputPassToDbPass("123123", "1a2b3c4d"));//77efb622dd4b2893e9979da5b629d529
	}
	
}
