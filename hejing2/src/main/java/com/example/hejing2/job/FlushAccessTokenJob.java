package com.example.hejing2.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.example.hejing2.bo.AccessTokenService;
import com.example.hejing2.config.WxService;
import com.example.hejing2.vo.AccessTokenEntity;
/**
 * 定时刷新accestoken
 * @author 雷加伟
 *
 */
@Component
public class FlushAccessTokenJob {
	private Log log =LogFactory.getLog(getClass());
	@Autowired
	private AccessTokenService accessTokenBo;
	@Scheduled(fixedDelay=1000*60*10)
	protected void execute() {
		System.out.println("===============获取accesstoken job开始==============");
		RestTemplate rt=new RestTemplate();
		ResponseEntity<String> re=rt.getForEntity("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"+"&appid="+
		WxService.APPID+"&secret="+WxService.APPSECRET, String.class);
		System.out.println("====="+re.getBody());
		try {
			JSONObject object=new JSONObject(re.getBody());
			AccessTokenEntity entity=new AccessTokenEntity();
			if(object.has("errcode")&&!object.get("errcode").equals("0")) {
				entity.setResMessage(object.getString("errmsg"));
				entity.setResStatus(object.getString("errcode"));
			}else if(!object.has("errcode")||object.get("errcode").equals("0")){
				entity.setAccessToken(object.getString("access_token"));
				entity.setAppId(WxService.APPID);
				entity.setAppName("福建联通微信平台");
				entity.setAppSecrete(WxService.APPSECRET);
				entity.setResStatus("0");
				entity.setResMessage("获取成功！");
			}
//			AccessTokenEntity entity1=new AccessTokenEntity();
//			entity1.setAppId(WxService.APPID);
			Wrapper<AccessTokenEntity> ew= new EntityWrapper<AccessTokenEntity>();
			ew.eq("app_id", WxService.APPID);
			if(accessTokenBo.selectOne(ew)==null) {
				accessTokenBo.insert(entity);
			} else {
				accessTokenBo.update(entity, ew);
			}
			
			System.out.println("===============获取accesstoken job结束==============");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
