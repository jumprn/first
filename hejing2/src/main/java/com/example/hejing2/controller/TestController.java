package com.example.hejing2.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.example.hejing2.bo.CompanyUserService;
import com.example.hejing2.utils.Fire.LimitProcessEnum;
import com.example.hejing2.utils.Fire.RewardEnum;
import com.example.hejing2.utils.cache.RedisUtils;
import com.example.hejing2.vo.CompanyUserEntity;

@Controller
@RequestMapping("/test")
public class TestController {

	@Autowired
	private CompanyUserService userService;
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private RedisUtils redisUtil;
	@RequestMapping("/testCompanyUser")
	public void testCompanyUser() {
		System.out.println("=============");
		CompanyUserEntity entity=new CompanyUserEntity();
		entity.setUserId(UUID.randomUUID().toString());
		userService.insert(entity);
	}
	@RequestMapping("/testSession")
	public void testSessionInRedis(HttpServletRequest request,HttpServletResponse response) {
		System.out.println("==========");
		HttpSession session=request.getSession();
		System.out.println("sessionId:"+session.getId());
		if(session.getAttribute("name")==null) {
			session.setAttribute("name", "hellow");;
		}else {
			System.out.println("======name:"+session.getAttribute("name"));
		}
		try {
			response.getOutputStream().println((String) session.getAttribute("name"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/testRedis")
	public void testRedis(HttpServletRequest request,HttpServletResponse response) {
		System.out.println("=======++++++++++"+redisTemplate.getHashValueSerializer());
		CompanyUserEntity cut= new CompanyUserEntity();
		cut.setAvatar("avatar");
		cut.setCreateTime("123456");
		cut.setDepartment("信息化部");
		cut.setEmail("dasdasdad");
		Map<String,String> map=new HashMap<String,String>();
		try {
			map=BeanUtils.describe(cut);
			System.out.println(map);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		redisTemplate.opsForHash().putAll("cumpanyUser", map);
		redisTemplate.opsForValue().set("this", cut, 0);
		CompanyUserEntity user =(CompanyUserEntity) redisTemplate.opsForValue().get("this");
		List<String> array= new ArrayList<String>();
		array.add("avatar");
		array.add("department");
		System.out.println("+++++++++++++++++mapUser"+redisTemplate.opsForHash().multiGet("cumpanyUser", array));
		System.out.println(user+"++++++++++++++++++++++entityUser"+user.getAvatar()+"  "+user.getDepartment());
		System.out.println("++++store  successfully!++++++");
		
	}
	/**
	 * 测试奖池抢券操作
	 * @param request
	 * @param response
	 */
	@RequestMapping("/testList")
	public void testListRedis(HttpServletRequest request,HttpServletResponse response) {
		String userId =request.getParameter("userId");
		System.out.println("用户id是:"+userId);
		String rewardId = redisUtil.partiReward(userId, RewardEnum.sourceKey, RewardEnum.desKey);
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		if(rewardId.equals("fail")) {
			try {
				response.getWriter().write("对不起，奖券池的奖券已经被一扫而空了！下次来早点==");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			try {
				response.getWriter().write("恭喜您!!!!!!您成功获得奖券且券号为："+rewardId+".别忘了即使兑换奥!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 初始化奖池
	 * @param response
	 */
	@RequestMapping("/initialize")
	public void initializeTheRewardPool(HttpServletResponse response) {
		response.setHeader("Content-type", "text/html;charset=utf-8");
		for(int i=0; i<20;i++) {
			String id =UUID.randomUUID().toString();
			redisUtil.setListByKeyAndValue(RewardEnum.sourceKey, id);
			System.out.println("券号为："+id+"的券被添加成功!");
		}
		try {
			response.getWriter().write("添加券成功!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 测试限流操作
	 */
	@RequestMapping("/limitProcess")
	public void limitProcess(HttpServletRequest request , HttpServletResponse response) {
		String userId = request.getParameter("userId");
		String String = request.getParameter("order");
		response.setHeader("Content-type", "text/html;charset=utf-8");
		try {
			String mess = redisUtil.limitMessageToProcess(LimitProcessEnum.sourceKey, 
					LimitProcessEnum.descKey, LimitProcessEnum.channel, userId, 100 ,String);
			
			if(mess.equals("success")) {
				response.getWriter().write("下单完成!请等待具体通知!");
			}else {
				response.getWriter().write("当前服务器压力过大,请稍等一段时间在下单!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			try {
				response.getWriter().write("对不起，下单时处理出错，请联系管理员!");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println(e);
		}
	}
	/**
	 * 订阅服务器redis处理订阅
	 */
	@RequestMapping("/beginSub")
	public void beginSub(HttpServletResponse response) {
		
		try {
			redisUtil.subscribeSub(LimitProcessEnum.channel);
			response.setHeader("Content-type", "text/html;charset=utf-8")
			;
			response.getWriter().write("正在监听当中。。。。。。");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
