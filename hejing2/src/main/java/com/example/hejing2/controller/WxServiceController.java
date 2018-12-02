package com.example.hejing2.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WxServiceController {
	private  final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', 
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private Log log =LogFactory.getLog(getClass());
	@RequestMapping("/wx")
	public void home(HttpServletRequest request,HttpServletResponse response) {
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce =request.getParameter("nonce");
		
		String token = "first token";
		String echostr=request.getParameter("echostr");
		log.info("signature:"+signature+"  timestamp:"+timestamp+"  nonce :"+nonce+"  echostr:"+echostr);
		OutputStream os=null;
		try {
			os = response.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		List<String> list =new ArrayList<String>();
		list.add(token);
		list.add(nonce);
		list.add(timestamp);
		list.sort(new Comparator<String>() {

			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		try {
			MessageDigest md=MessageDigest.getInstance("SHA-1");
			byte[] ss=md.digest((list.get(0)+list.get(1)+list.get(2)).getBytes());
			char[] s=new char[ss.length*2];
			int index=0;
			for(byte a:ss) {
				s[index++]=HEX_CHAR[a>>>4&0xf];
				s[index++]=HEX_CHAR[a&0xf];
			}
			log.info("signature:"+signature +"   myDigest Message:"+String.valueOf(s));
			if(String.valueOf(s).equals(signature)) {
				if(echostr!=null) {
					os.write(echostr.getBytes());
				}else {
					os.write("".getBytes());
				}
				
			}else {
				log.info("认证失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
