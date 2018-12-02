/**
 * 
 */
package com.example.hejing2.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.EntityWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.example.hejing2.bo.AccessTokenService;
import com.example.hejing2.bo.CompanyUserService;
import com.example.hejing2.utils.java.com.qq.weixin.mp.aes.AesException;
import com.example.hejing2.utils.java.com.qq.weixin.mp.aes.WXBizMsgCrypt;
import com.example.hejing2.vo.CompanyUserEntity;
import com.mysql.jdbc.StringUtils;

/**
 * @author 雷加伟
 *
 */
@Controller
@RequestMapping("/handCommunication")
public class CommunicationListController {
	private String sToken="7e1GaXin6mgxxtiARJcxMsoM";
	private String sCorpID="wwab8a4ff5b65e63b4";
	private String sEncodingAESKey="aoIj935yqJ1iUbbV7T9EsI5SIgBri4z50RF6ecXhYyq";
	private String sScret="uNxG1mb5uurP2IKrY2UK8EH4dq8ICg9iePwLw-ZnrgQ";
	@Autowired
	private CompanyUserService service;
	String cropId="wwab8a4ff5b65e63b4";
	@Autowired
	private AccessTokenService tokenService;
	private Log log =LogFactory.getLog(this.getClass());
	@RequestMapping("/handleChange")
	public void handleChange(HttpServletRequest request,HttpServletResponse response) {
		try {
			WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
			// String sReqMsgSig = HttpUtils.ParseUrl("msg_signature");
			String sReqMsgSig = request.getParameter("msg_signature");
			// String sReqTimeStamp = HttpUtils.ParseUrl("timestamp");
			String sReqTimeStamp = request.getParameter("timestamp");
			// String sReqNonce = HttpUtils.ParseUrl("nonce");
			String sReqNonce = request.getParameter("nonce");
			// post请求的密文数据
			// sReqData = HttpUtils.PostData();
			BufferedReader br =new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line=null;
			StringBuffer sb=new StringBuffer();
			while((line=br.readLine())!=null) {
				sb.append(line);
			}
			String sReqData=sb.toString();
				String sMsg = wxcpt.DecryptMsg(sReqMsgSig, sReqTimeStamp, sReqNonce, sReqData);
				System.out.println("after decrypt msg: " + sMsg);
				// TODO: 解析出明文xml标签的内容进行处理
				// For example:
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				StringReader sr = new StringReader(sMsg);
				InputSource is = new InputSource(sr);
				Document document = db.parse(is);
				
				Element root = document.getDocumentElement();
				NodeList rootChild=root.getChildNodes();
				String msgType="";
				for(int i=0 ; i< rootChild.getLength(); i++) {
					if(rootChild.item(i).getNodeName().equals("ChangeType")) {
						msgType=rootChild.item(i).getTextContent();
					}
				}
				if(msgType.equals("update_user")) {
					dealWithUpdateUser(root);
				}else if(msgType.equals("create_user")) {
					dealWithCreateUser(root);
				}else if(msgType.equals("delete_user")) {
					dealWithDeleteUser(root);
				}else {
					log.error("===============无法处理的通讯录变更消息===============");
					throw new Exception();
				}
			} catch (Exception e) {
				// TODO
				// 解密失败，失败原因请查看异常
				e.printStackTrace();
			}
	}
	
	private CompanyUserEntity getUserByRoot(Element root) {
		NodeList rootChild=root.getChildNodes();
		String ToUserName =null;
		String FromUserName =null;
		String CreateTime =null;
		String MsgType =null;
		String Event =null;
		String UserID =null;
		String NewUserID =null;
		String Name =null;
		String Department =null;
		String Mobile =null;
		String Position =null;
		String Gender =null;
		String Email =null;
		String Status =null;
		String Avatar =null;
		String EnglishName =null;
		String IsLeader =null;
		String Telephone =null;
		String ExtAttr =null;
		for(int i=0 ; i< rootChild.getLength(); i++) {
			if(rootChild.item(i).getNodeName().equals("ToUserName")) {
				ToUserName=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("FromUserName")) {
				FromUserName=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("CreateTime")) {
				CreateTime=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("MsgType")) {
				MsgType=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Event")) {
				Event=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("UserID")) {
				UserID=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("NewUserID")) {
				NewUserID=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Name")) {
				Name=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Department")) {
				Department=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Mobile")) {
				Mobile=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Position")) {
				Position=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Gender")) {
				Gender=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Email")) {
				Email=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Status")) {
				Status=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Avatar")) {
				Avatar=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("EnglishName")) {
				EnglishName=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("IsLeader")) {
				IsLeader=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("Telephone")) {
				Telephone=rootChild.item(i).getTextContent();
			}else if(rootChild.item(i).getNodeName().equals("ExtAttr")) {
				ExtAttr=rootChild.item(i).getTextContent();
			}
		}
		
		CompanyUserEntity entity=new CompanyUserEntity();
		entity.setAvatar(Avatar);
		entity.setCreateTime(CreateTime);
		entity.setDepartment(Department);
		entity.setEmail(Email);
		entity.setToUserName(ToUserName);
		entity.setEnglishName(EnglishName);
		entity.setEvent(Event);
		entity.setExrAttr(ExtAttr);
		entity.setTelephone(Telephone);
		entity.setFromUserName(FromUserName);
		entity.setGender(Gender);
		entity.setIsLeader(IsLeader);
		entity.setMobile(Mobile);
		entity.setMsgType(MsgType);
		entity.setName(Name);
		entity.setPosition(Position);
		entity.setStatus(Status);
		if(!StringUtils.isNullOrEmpty(NewUserID)&&MsgType.equals("update_user")) {
			entity.setUserId(NewUserID);
			entity.setOldUserId(UserID);
		}else {
			entity.setUserId(UserID);
		}
		return entity;
	}
	private void dealWithUpdateUser(Element root) {
		CompanyUserEntity entity=getUserByRoot(root);
		Wrapper<CompanyUserEntity> ew=new EntityWrapper<CompanyUserEntity>(entity);
		service.update(entity, ew);
	}
	private void dealWithCreateUser(Element root) {
		CompanyUserEntity entity=getUserByRoot(root);
		service.insert(entity);
	}
	
	private void dealWithDeleteUser(Element root) {
		CompanyUserEntity entity=getUserByRoot(root);
		EntityWrapper<CompanyUserEntity> ew=new EntityWrapper<>();
		ew.eq("user_id", entity.getUserId());
		if(service.selectOne(ew)!=null) {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("user_id", entity.getUserId());
			service.delete(ew);
		}else {
			log.debug("数据库并不存在此用户");
		}
		
	}
}
