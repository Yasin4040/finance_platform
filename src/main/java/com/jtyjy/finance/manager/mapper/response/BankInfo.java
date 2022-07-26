package com.jtyjy.finance.manager.mapper.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 银行信息
 * @author User
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BankInfo {
	/**
	 * 银行名称
	 */
	private String bankName;
	
	/**
	 * 银行账号
	 */
	private String bankAccount;
	
	/**
	 * 银联码
	 */
	private String bankCode;
	
	/**
	 * 开户名称
	 */
	private String accountName;
	
	/**
	 * 开户行
	 */
	private String openBank;
	
	/**
	 * 稿费作者编码
	 */
	private String authorCode;
}
