package com.jtyjy.finance.manager.constants;

/**
 * 状态常量
 * @author User
 *
 */
public class StatusConstants {
	
	/*****************************************报销***********************************/

	/**
	 * 报销单：审核通过状态
	 */
	public static final Integer BX_PASS = 2;
	
	/**
	 * 报销单：保存状态
	 */
	public static final Integer BX_SAVE = 0;
	
	/**
	 * 报销单：退回状态
	 */
	public static final Integer BX_BACK = -1;
	
	/**
	 * 报销单：退回纸质状态
	 */
	public static final Integer BX_BACK_PAPER = -2;
	
	
	/**
	 * 报销单：全部退回
	 */
	public static final Integer BX_BACK_ALL = -3;
	
	/**
	 * 报销单：已提交状态
	 */
	public static final Integer BX_SUBMIT = 1;
	
	/*****************************************紧急程度***********************************/
	
	/**
	 * 紧急程度：0级
	 */
	public static final Integer SERIOUS_ZERO = 0;
	
	/**
	 * 报销接收模式
	 */
	public static final Integer BX_RECEIVE_MODEL = 1;
	
	/**
	 * 报销审核模式
	 */
	public static final Integer BX_CHECK_MODEL = 2;
}
