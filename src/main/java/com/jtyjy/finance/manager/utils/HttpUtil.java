package com.jtyjy.finance.manager.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @Auther: wujianlong
 * @Date: 2019/1/7 14:19
 * @Description:
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final RestTemplate restTemplate=new RestTemplate();
    /**
     * 功能描述: post请求
     * @param: url: 地址, jsonStr:json字符串
     * @return:
     * @auther: wujianlong
     * @date: 2018/12/25 9:16
     */
    public static String createHttpPost(String url, String jsonStr) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.addHeader("content-type", "application/json");
        StringEntity se = new StringEntity(jsonStr, "UTF-8" );
        post.setEntity(se);
        String res = null;
        try {
            CloseableHttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            res = EntityUtils.toString(entity);
            logger.info(res);
        } catch (IOException e) {
            logger.error("post请求失败",e);
        } finally {
            post.releaseConnection();
            try {
                client.close();
            } catch (IOException e) {
                logger.error("post请求关闭失败",e);
            }
        }
        return res;
    }

    /**
     * 功能描述: get请求
     * @param:
     * @return:
     * @auther: wujianlong
     * @date: 2019/1/7 14:25
     */
    public static String createHttpGet(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        String res = null;
        try {
            CloseableHttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            res = EntityUtils.toString(entity);
        } catch (IOException e) {
            logger.error("get请求失败",e);
        } finally {
            get.releaseConnection();
            try {
                client.close();
            } catch (IOException e) {
                logger.error("get请求关闭失败",e);
            }
        }

        return res;
    }

    public static String doGet(String url){
        String res = restTemplate.getForObject(url, String.class);
        return res;
    }

}
