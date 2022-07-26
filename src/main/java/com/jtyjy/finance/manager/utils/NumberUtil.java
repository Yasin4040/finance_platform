package com.jtyjy.finance.manager.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * <p>Description:TODO(用一句话描述这个变量表示什么)</p>
 * @author <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a>
 * @version 1.0
 * @Note
 * <p>ProjectName: iamxiongx-util-message</p>
 * <p>PackageName: com.iamxiongx.util.message</p>
 * <p>ClassName: NumberUtil</p>
 * <p>Date: 2019年4月3日 上午8:21:20</p>
 */
public class NumberUtil {
	  /** 定义数组存放数字对应的大写 */  
	  private final static String[] STR_NUMBER = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };  
	  
	  /** 定义数组存放位数的大写 */  
	  private final static String[] STR_MODIFY = { "", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟" };  
	  /** 
	   * 判断传入的字符串中是否有.如果有则返回点 
	   *  
	   * @param tempString 
	   * @return 
	   */  
	  private static String getDot(String tempString) {  
	    return tempString.indexOf(".") != -1 ? "点" : "";  
	  }  
	  
	  /** 
	   * 判断传入的字符串中是否有-如果有则返回负 
	   *  
	   * @param tempString 
	   * @return 
	   */  
	  private static String getSign(String tempString) {  
	    return tempString.indexOf("-") != -1 ? "负" : "";  
	  }
	  /** 
	   * 转化小数部分 例：输入22.34返回叁肆 
	   *  
	   * @param tempString 
	   * @return 
	   */  
	  private static String getFraction(String tempString) {  
	    String strFraction = null;  
	    int intDotPos = tempString.indexOf(".");  
	    /** 没有点说明没有小数，直接返回 */  
	    if (intDotPos == -1)  
	      return "";  
	    strFraction = tempString.substring(intDotPos + 1);  
	    StringBuffer sbResult = new StringBuffer(strFraction.length());  
	    for (int i = 0; i < strFraction.length(); i++) {  
	      sbResult.append(STR_NUMBER[strFraction.charAt(i) - 48]);  
	    }  
	    return sbResult.toString();  
	  }
	  /** 
	   * 替代字符 
	   *  
	   * @param pValue 
	   * @param pSource 
	   * @param pDest 
	   */  
	  private static void replace(StringBuffer pValue, String pSource, String pDest) {  
	    if (pValue == null || pSource == null || pDest == null)  
	      return;  
	    /** 记录pSource在pValue中的位置 */  
	    int intPos = 0;  
	    do {  
	      intPos = pValue.toString().indexOf(pSource);  
	      /** 没有找到pSource */  
	      if (intPos == -1)  
	        break;  
	      pValue.delete(intPos, intPos + pSource.length());  
	      pValue.insert(intPos, pDest);  
	    } while (true);  
	  }
	  /** 
	   * 转化整数部分 
	   *  
	   * @param tempString 
	   * @return 返回整数部分 
	   */  
	  private static String getInteger(String tempString) {  
	    /** 用来保存整数部分数字串 */  
	    String strInteger = null;//    
	    /** 记录"."所在位置 */  
	    int intDotPos = tempString.indexOf(".");  
	    int intSignPos = tempString.indexOf("-");  
	    if (intDotPos == -1)  
	      intDotPos = tempString.length();  
	    /** 取出整数部分 */  
	    strInteger = tempString.substring(intSignPos + 1, intDotPos);  
	    strInteger = new StringBuffer(strInteger).reverse().toString();  
	    StringBuffer sbResult = new StringBuffer();  
	    for (int i = 0; i < strInteger.length(); i++) {  
	      sbResult.append(STR_MODIFY[i]);  
	      sbResult.append(STR_NUMBER[strInteger.charAt(i) - 48]);  
	    }  
	      
	    sbResult = sbResult.reverse();  
	    replace(sbResult, "零拾", "零");  
	    replace(sbResult, "零佰", "零");  
	    replace(sbResult, "零仟", "零");  
	    replace(sbResult, "零万", "万");  
	    replace(sbResult, "零亿", "亿");  
	    replace(sbResult, "零零", "零");  
	    replace(sbResult, "零零零", "零");  
	    /** 这两句不能颠倒顺序 */  
	    replace(sbResult, "零零零零万", "");  
	    replace(sbResult, "零零零零", "");  
	    /** 这样读起来更习惯. */  
	    replace(sbResult, "壹拾亿", "拾亿");  
	    replace(sbResult, "壹拾万", "拾万");  
	    /** 删除个位上的零 */  
	    if (sbResult.charAt(sbResult.length() - 1) == '零' && sbResult.length() != 1)  
	      sbResult.deleteCharAt(sbResult.length() - 1);  
	    if (strInteger.length() == 2) {  
	      replace(sbResult, "壹拾", "拾");  
	    }  
	    /** 将结果反转回来. */  
	    return sbResult.toString();  
	  }
	  /**
	   * 
	   * <p>Description:阿拉伯数字变成中文数字</p>
	   * @param tempNumber
	   * @return 
	   * @return String
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午8:30:45</p>
	   * <p>Version: 1.0</p>
	   */
	  public static String numberToChinese(BigDecimal tempNumber) {  
		   java.text.DecimalFormat df = new java.text.DecimalFormat("#.#########");  
		   String pTemp = String.valueOf(df.format(tempNumber));  
		   StringBuffer sbResult = new StringBuffer(getSign(pTemp) + getInteger(pTemp) + getDot(pTemp) + getFraction(pTemp));  
		   return sbResult.toString();
	  }
	  public static String jenumberToChinese(BigDecimal tempNumber) {
		  java.text.DecimalFormat df = new java.text.DecimalFormat("#.#########");  
		   String pTemp = String.valueOf(df.format(tempNumber));
		   String part = getSign(pTemp) + getInteger(pTemp) + "元";
		   String dot = getDot(pTemp);
		   String fraction = getFraction(pTemp);
		   //part += dot;
		   if(StringUtils.isEmpty(dot)) {
			   part += "整";
		   }else {
			   for(int i=0;i<fraction.length();i++) {
				   if(i>1) {
					   break;
				   }
				   String tmp = fraction.charAt(i)+"";
				   if(i==0) {
					   tmp += "角";
				   }else if(i==1) {
					   tmp += "分";
				   }
				   part += tmp;
			   }
		   }
		   
		   StringBuffer sbResult = new StringBuffer(part);  
		   return sbResult.toString();
	  }
	  private static boolean istrue(String str,String reg) {
		  Boolean result = false;
		  try {
			  java.util.regex.Pattern pattern=java.util.regex.Pattern.compile(reg); // 判断小数点后2位的数字的正则表达式
		      java.util.regex.Matcher match=pattern.matcher(str); 
		      result = match.matches();
		  }catch(Exception e) {}
		  return result;
	  }
	  public static String str2numberstr(String str) {
		  //str = str.trim().r
		  if(StringUtils.isEmpty(str)) {
			  return "0";
		  }
		  str = str.trim().replace(" ", "");
		  if(!str.startsWith(",") && !str.endsWith(",")) {
			  str = str.replace(",", "");
		  }
		  return str;
	  }
	  /**
	   * 
	   * <p>Description:数字判断</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:22:14</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNumeric(String str) {
		  String tmp = str;
		  if(str.startsWith("-") && !str.startsWith("--")) {
			  tmp = str.replace("-", "");
		  }
	      return istrue(tmp,"^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,20})?$");
	  }
	  /**
	   * 
	   * <p>Description:纯数字判断</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:11:32</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isAllNumeric(String str) {
          return istrue(str,"[0-9]*");
	  }
	  /**
	   * 
	   * <p>Description:非负整数（正整数   +   0）</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:23:44</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNonnegativeInteger(String str) {
          return istrue(str,"^\\d+$");
	  }
	  /**
	   * 
	   * <p>Description:正整数   </p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:32:16</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isPositiveInteger(String str) {
		 return istrue(str, "^[1-9]\\d*$");
	  }
	  /**
	   * 
	   * <p>Description:非正整数（负整数   +   0） </p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:34:54</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNonpositiveInteger(String str) {
		  return istrue(str,"\"^((-\\\\d+)|(0+))$\"");
	  }
	  /**
	   * 
	   * <p>Description:负整数 </p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:35:57</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNegativeInteger(String str) {
		  return istrue(str,"\"^((-\\\\d+)|(0+))$\"");
	  }
	  /**
	   * 
	   * <p>Description:整数</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:36:51</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isInteger(String str) {
		  //return istrue(str,"\"^((-\\\\d+)|(0+))$\"");
		  return istrue(str,"^-?[1-9]\\d*$");
	  }
	  /**
	   * 
	   * <p>Description:非负浮点数（正浮点数   +   0）</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:38:14</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNonnegativeFloatingPointNumbers(String str) {
		  return istrue(str,"\"^\\\\d+(\\\\.\\\\d+)?$\"");
	  }
	  /**
	   * 
	   * <p>Description:正浮点数  </p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:39:19</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isPositiveFloatingPoint(String str) {
		  return isFloatingNumber(str) && !str.startsWith("-");
	  }
	  /**
	   * 
	   * <p>Description:非正浮点数（负浮点数   +   0）  </p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:40:32</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNonPositiveFloatingPointNumber(String str) {
		  return (isNumeric(str) && str.contains(".") && !str.startsWith("-")) || "0".equals(str);
	  }
	  /**
	   * 
	   * <p>Description:负浮点数</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:41:40</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isNegativeFloatingPointNumber(String str) {
		  return isNumeric(str) && str.contains(".") && str.startsWith("-") && !str.startsWith("--");
	  }
	  /**
	   * 
	   * <p>Description:浮点数</p>
	   * @param str
	   * @return 
	   * @return Boolean
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年4月3日 上午9:42:44</p>
	   * <p>Version: 1.0</p>
	   */
	  public static Boolean isFloatingNumber(String str) {
		  return isNumeric(str) && str.contains(".");
	  }
	  /**
	   * 
	   * <p>Description:去掉数字后面多余的0</p>
	   * @param number
	   * @return 
	   * @return BigDecimal
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年5月8日 下午3:45:27</p>
	   * <p>Version: 1.0</p>
	   */
	  public static BigDecimal subZeroAndDot(BigDecimal number){  
		  String s = number.toString();
		  if(s.indexOf(".") > 0){  
	            s = s.replaceAll("0+?$", "");//去掉多余的0  
	            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉  
	      }  
	      return new BigDecimal(s);  
	  }
	  /**
	   * 
	   * <p>Description:百分号转数字  15.6%  =  0.156</p>
	   * @param percentchar
	   * @return 
	   * @return String
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年7月5日 下午5:07:16</p>
	   * <p>Version: 1.0</p>
	   */
	  public static String percentchar2num(String percentchar) {
		  if(StringUtils.isEmpty(percentchar)) {
			  return percentchar;
		  }
		  if(!percentchar.endsWith("%") && !(percentchar.length()>1)) {
			  return percentchar;
		  }
		  String tmp = percentchar.substring(0, percentchar.length()-1);
		  if(!isNumeric(tmp)) {
			  return percentchar;
		  }
		  return new BigDecimal(tmp).divide(new BigDecimal(100),4,RoundingMode.HALF_UP).toString();
	  }
	  /**
	   * 
	   * <p>Description:1,000 转成 1000</p>
	   * @param commachar
	   * @return 
	   * @return String
	   * @Note
	   * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
	   * <p>Date: 2019年7月5日 下午3:25:00</p>
	   * <p>Version: 1.0</p>
	   */
	  public static String commachar2num(String commachar) {
		  if(StringUtils.isEmpty(commachar)) {
			  return commachar;
		  }
		  String tmp = commachar.replaceAll(",", "");//替换英文逗号
	      tmp = tmp.replace("，", "");//替换中文逗号
	      if(!isNumeric(tmp)) {
	    	  tmp = commachar;
	      }
	      return tmp;
	  }
	  public static void main(String[] args) {
		System.out.println(NumberUtil.jenumberToChinese(new BigDecimal("23.4555")));
		  Boolean integer =
				  NumberUtil.isInteger("100.0");
		  System.out.println(integer);
	  }

//	public static Boolean isInteger(String str){
//		String[] split = str.split("\\.");
//		if(split.length>=2 && Integer.valueOf(split[1])>0){
//			return false;
//		}
//		return true;
//	}
}
