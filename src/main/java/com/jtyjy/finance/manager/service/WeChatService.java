package com.jtyjy.finance.manager.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.tools.HttpClientTool;

/**
 * 微信信息
 * @author User
 *
 */
@Service
public class WeChatService {
	
	@Autowired
	private RedisClient redis;
	@Value("${eweixin.corpid}")
	private String corpId;
	@Value("${eweixin.corpsecret}")
	private String corpSecret;
	
	private static final String REDIS_TOKEN_KEY = "ACCESS_TOKEN_KEY";
	private static final String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=#corpid&corpsecret=#corpsecret";
	public static final String USERID_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=#token&code=#code";
	private static final Integer TTL = 7100;
	
	
	/**
     * <p>获取企业微信access_token</p>
     * 作者 konglingcheng
     * date 2020年6月19日
     * <p>@param serverId
     * <p>@return</p>
     * @throws Exception 
     */
	public String getAccessToken() throws Exception {
		String token = this.redis.get(REDIS_TOKEN_KEY);
		if(StringUtils.isBlank(token)) {
			//获取，并保存到redis
			token = this.getAndSaveToken();
			if(StringUtils.isBlank(token)) {
				throw new Exception("获取企业微信access_token失败");
			}
		}
		return token;
	}
	
	
	/**
     * <p>根据code和access_token获取用户工号</p>
     * 作者 konglingcheng
     * date 2020年6月19日
     * <p>@param accessToken
     * <p>@param code
     * <p>@return</p>
     * @throws Exception 
     */
    public String getEmpNo(String accessToken, String code) throws Exception {
		String url = USERID_URL;
		url = url.replace("#token", accessToken).replace("#code", code);
		String json = HttpClientTool.getHttps(url);
		if(!StringUtils.isEmpty(json)) {
			JSONObject object = JSONObject.parseObject(json);
			if(object.getInteger("errcode") == 0) {
				return object.getString("UserId");
			}else{
				throw new Exception(object.getString("errmsg"));
			}
		}
		return null;
	}
	
	
	private String getAndSaveToken() throws Exception {
		String url = ACCESS_TOKEN_URL.replace("#corpid", corpId).replace("#corpsecret", corpSecret);
		String json = HttpClientTool.getHttps(url);
		if(!StringUtils.isEmpty(json)) {
			JSONObject object = JSONObject.parseObject(json);
			if(object.getInteger("errcode") == 0) {
				String token = object.getString("access_token");
				//缓存到redis
				this.redis.set(REDIS_TOKEN_KEY, token, TTL);
				return token;
			}
		}
		return null;
	}

}
