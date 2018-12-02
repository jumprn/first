package com.example.hejing2.controller;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.example.hejing2.bo.UserService;
import com.example.hejing2.config.WxService;
import com.example.hejing2.utils.cache.RedisUtils;
import com.example.hejing2.vo.UserEntity;
import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping("/wxauth/redirectUrl")
public class LoginController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private RedisUtils redisUtil;
	/**
	 * 获取网页code并利用所获取的code获取用户openid
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unused")
	@RequestMapping("/codeControl")
	public void getWxPageCode(HttpServletRequest request,HttpServletResponse response) {
		HttpSession session =request.getSession();
		RestTemplate rt =new RestTemplate();
		rt.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		String openid=redisUtil.getStringBykey("openid");
		String session_token =redisUtil.getStringBykey("session_token");
		String fresh_access_token =redisUtil.getStringBykey("fresh_access_token");
		System.out.println("++++++++++++++openid:  "+openid);
		Map<String,String> map =null;
		if(openid!=null) {
			map=redisUtil.getHashMapByKey(openid);
		}
		if(map!=null) {
			UserEntity userEntity=new UserEntity();
			try {
				BeanUtils.populate(userEntity, map);
				System.out.println("此用户尚且在redis中："+JSONObject.toJSON(userEntity).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}else {  
			if(StringUtils.isNullOrEmpty(openid)||StringUtils.isNullOrEmpty(session_token)){//判断session中是否有access_token
				String code = request.getParameter("code");
				String state = request.getParameter("state");
				System.out.println(request.getAttribute("code")+"=========code:"+code+"===========state:"+state);
				ResponseEntity<String> re= rt.getForEntity("https://api.weixin.qq.com/sns/oauth2/access_token?"
						+ "appid="+WxService.APPID+"&secret="+WxService.APPSECRET+"&code="+code+"&grant_type=authorization_code", String.class);
				JSONObject json=JSONObject.parseObject(re.getBody());
				System.out.println(json);
				openid=json.getString("openid");
				session_token=json.getString("access_token");
				fresh_access_token=json.getString("refresh_token");
				if(session_token!=null&&openid!=null&&fresh_access_token!=null) {
					redisUtil.setStringByKey("access_token", session_token);//往session中塞access_token和openid,refresh_token
					redisUtil.setStringByKey("openid", openid);
					redisUtil.setStringByKey("fresh_access_token", fresh_access_token);
					System.out.println("+++++++++++++++access_token和openid已经被存入redis");
				}
			}
			ResponseEntity<String> re3 =rt.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token="
					+ session_token
					+ "&openid="
					+ openid
					+ "&lang=zh_CN", String.class);
			JSONObject json3=JSONObject.parseObject(re3.getBody());
			if(json3.containsKey("errcode")) {//当前access_token已经过期，需使用refresh_token重新获取access_token
				ResponseEntity<String> re4 = rt.getForEntity("https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
						+ WxService.APPID
						+ "&grant_type="
						+ fresh_access_token
						+ "&refresh_token=REFRESH_TOKEN", String.class);
				JSONObject refreshJson =JSONObject.parseObject(re4.getBody());
				System.out.println("refresh_token获取的access_token:  "+refreshJson);
				if(!refreshJson.containsKey("access_token")) {
					System.out.println("=============通过refresh_token获取access_token出错=============");
				}else {
					session_token =refreshJson.getString("access_token");
					redisUtil.setStringByKey("access_token", session_token);
					redisUtil.setStringByKey("openid", openid);
					System.out.println("access_token和openid已经被存入redis");
				}
				ResponseEntity<String> re5 =rt.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token="
						+ session_token
						+ "&openid="
						+ openid
						+ "&lang=zh_CN", String.class);
				JSONObject freshJson = JSONObject.parseObject(re5.getBody());
				System.out.println("通过refresh token后获取的用户json："+freshJson);
			}else {
				System.out.println("=======通过session中存放的access_token获取的用户json信息："+json3);
				UserEntity user =new UserEntity();
				user.setCity(json3.getString("city"));
				user.setCountry(json3.getString("country"));
				user.setHeadimgurl(json3.getString("headimgurl"));
				user.setLanguage(json3.getString("language"));
				user.setNickName(json3.getString("nickname"));
				user.setOpenId(json3.getString("openid"));
				user.setPrivilege("");
				user.setProvince(json3.getString("province"));
				user.setSex(json3.getInteger("sex")==1 ? "男" : "女");
				user.setUserId(UUID.randomUUID().toString());
				EntityWrapper<UserEntity> ew =new EntityWrapper<>();
				ew.eq("open_id", json3.get("openid"));
				if(userService.selectOne(ew)==null) {
					userService.insert(user);
				}else {
					System.out.println("用户："+openid+"已经存在了");
				}
				try {
					redisUtil.setHashByKeyAndValue(openid, (JSONObject) JSONObject.toJSON(user));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
		
		
		//获取refresh_token，先判断access_token是否过期，过期则使用refresh_token获取access_token,refresh_token过期则使用code重新获取
		
//		System.out.println(json3);
		
//		request.getSession().setAttribute("", value);
//		System.out.println(json.toString());
//		String access_token=json.getString("access_token");
//		Integer expires_in =json.getInt("expires_in");
		
	}
}
