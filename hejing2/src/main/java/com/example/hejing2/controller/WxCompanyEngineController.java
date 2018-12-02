package com.example.hejing2.controller;

import java.net.URLDecoder;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.example.hejing2.bo.AccessTokenService;
import com.example.hejing2.utils.java.com.qq.weixin.mp.aes.AesException;
import com.example.hejing2.utils.java.com.qq.weixin.mp.aes.WXBizMsgCrypt;
import com.example.hejing2.vo.AccessTokenEntity;


@Controller
@RequestMapping("/testHanding")
public class WxCompanyEngineController {
	private String sToken="rl4q4YdOP47OQuls9ydFbeE5J2Cg";
	private String sCorpID="wwab8a4ff5b65e63b4";
	private String sEncodingAESKey="uI3znBBvL0DUQl34CMMDyS9SMFewm25fzviHrPHbAJr";
	private String sScret="hpDMkycbhVhulhs9TirKSWNFjxMbA-YmrnKL9_bjzWo";
	String cropId="wwab8a4ff5b65e63b4";
	@Autowired
	private AccessTokenService tokenService;
	private Log log =LogFactory.getLog(this.getClass());
	@RequestMapping("/handingResponseMessage")
	public void handingTheToken(HttpServletRequest request,HttpServletResponse response) {
		try {
			WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
			String msg_signature=request.getParameter("msg_signature");
			String timestamp=request.getParameter("timestamp");
			String nonce=request.getParameter("nonce");
			String echostr=request.getParameter("echostr");
			@SuppressWarnings("deprecation")
			String sVerifyMsgSig =URLDecoder.decode(msg_signature);
			@SuppressWarnings("deprecation")
			String sVerifyTimeStamp=URLDecoder.decode(timestamp);
			@SuppressWarnings("deprecation")
			String sVerifyNonce=URLDecoder.decode(nonce);
		    @SuppressWarnings("deprecation")
			String sVerifyEchoStr = URLDecoder.decode(echostr);
		    log.info("解码前echostr:"+echostr+"解码后的echostr："+sVerifyEchoStr);
		    log.info("解码前sVerifyTimeStamp:"+timestamp+"解码后的sVerifyTimeStamp："+sVerifyTimeStamp);
		    String sEchoStr;
		    try {
				sEchoStr = wxcpt.VerifyURL(sVerifyMsgSig, sVerifyTimeStamp,
						sVerifyNonce, sVerifyEchoStr);
				log.info("verifyurl echostr: " + sEchoStr);
				response.getOutputStream().write(sEchoStr.getBytes("utf-8"));
				// 验证URL成功，将sEchoStr返回
				// HttpUtils.SetResponse(sEchoStr);
			} catch (Exception e) {
				//验证URL失败，错误原因请查看异常
				e.printStackTrace();
			}
		} catch (AesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/getAccessToken")
	public void getToken(HttpServletRequest request,HttpServletResponse response) {
		System.out.println("================");
		try {
			RestTemplate rt= new RestTemplate();
//			String httpEntity="corpid="+URLEncoder.encode(cropId, "utf-8")+"&corpsecret="+URLEncoder.encode(sScret, "utf-8");
//			HttpHeaders header=new HttpHeaders();
//			header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//			
//			LinkedMultiValueMap map=new LinkedMultiValueMap<>();
//			map.add("corpid", cropId);
//			map.add("corpsecret", sScret);
//			HttpEntity entity=new HttpEntity<>(httpEntity, header);
//			ResponseEntity<String> re=rt.exchange("https://qyapi.weixin.qq.com/cgi-bin/gettoken",
//					HttpMethod.GET, entity, String.class);
			ResponseEntity<String> res1=rt.getForEntity("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wwab8a4ff5b65e63b4&corpsecret=uNxG1mb5uurP2IKrY2UK8EH4dq8ICg9iePwLw-ZnrgQ",
					String.class);
//			System.out.println(entity.getBody());
//			System.out.println(re.getBody());
//			System.out.println(re.getStatusCodeValue());
			System.out.println(res1);
			System.out.println("===============下面是json格式============");
//			JSONObject res1=rt.getForObject("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wwab8a4ff5b65e63b4&corpsecret=uNxG1mb5uurP2IKrY2UK8EH4dq8ICg9iePwLw-ZnrgQ",
//					JSONObject.class);
//			System.out.println(res1);
			JSONObject res=new JSONObject(res1.getBody());
			AccessTokenEntity ate=new AccessTokenEntity();
			ate.setAppId(UUID.randomUUID().toString());
			ate.setAccessToken(res.getString("access_token"));
			ate.setAppName("测试");
			ate.setAppSecrete("uNxG1mb5uurP2IKrY2UK8EH4dq8ICg9iePwLw-ZnrgQ");
			ate.setResMessage(res.getString("errmsg"));
			ate.setResStatus(res.getString("errcode"));
			tokenService.insert(ate);
		} catch (Exception e) {
			log.error("==========获取access_token时出错=============",e);
		}
	}
}
