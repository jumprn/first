/**
 * 
 */
package com.example.hejing2.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.example.hejing2.bo.AccessTokenService;
import com.example.hejing2.config.WxService;
import com.example.hejing2.vo.AccessTokenEntity;

/**
 * @author 雷加伟
 *
 */
@Controller
@RequestMapping("/wx/menu")
public class MenuButtonController {

	private Log logger =LogFactory.getLog(getClass());
	
	@Autowired
	private AccessTokenService accessTokenService;
	@SuppressWarnings("deprecation")
	@RequestMapping("/createMenu")
	@ResponseBody
	public String createMenu(HttpServletRequest request,HttpServletResponse response) {
		
		try {
			RestTemplate rt =new RestTemplate();
			rt.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
			JSONObject jsonObject =new JSONObject();
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject1 =new JSONObject();
			jsonObject1.put("type", "view");
			jsonObject1.put("name", "跳到登陆");
			jsonObject1.put("url", "https://open.weixin.qq.com/connect/oauth2/authorize"
					+ "?appid="+WxService.APPID+"&redirect_uri="+URLEncoder.encode("http://jumprn.picp.io/wxauth/redirectUrl/codeControl")+
					"&response_type=open&scope="+"snsapi_userinfo"+"&state="+"11111"+"#wechat_redirect");
			jsonArray.put(jsonObject1);
			jsonObject.put("button", jsonArray);
			logger.info(jsonObject.toString());
			EntityWrapper<AccessTokenEntity> ew=new EntityWrapper<>();
			ew.eq("app_id", "wx34b44bf164beda5b");
//			System.out.println(accessTokenService.selectOne(ew).getAccessToken());
			ResponseEntity<String> re=rt.postForEntity("https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+
			accessTokenService.selectOne(ew).getAccessToken(), jsonObject.toString(),String.class);
			JSONObject object=new JSONObject(re.getBody());
			logger.info("=============返回body信息==========="+re.getBody());
			if(((Integer)object.get("errcode"))==0) {
				return "处理成功!";
			}else {
				return "处理失败";
			}
		} catch (Exception e) {
			logger.error("=======create menu error========", e);
			return "处理失败";
		}
		
	}
}
