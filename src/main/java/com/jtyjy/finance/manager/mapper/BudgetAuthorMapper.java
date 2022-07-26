package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetAuthor;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.vo.BudgetAuthorVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetAuthorMapper extends BaseMapper<BudgetAuthor> {

    Map<String, String> getAuthorInfo(@Param("name")String name, @Param("numb") String number);

    /**
     *  根据稿件作者获取银行信息
     * @param inSql
     * @return
     */
	List<BankInfo> getBankInfoByAuthorCode(@Param("inSql")String inSql);
	
	/**
	 * 分页查询稿费作者
	 * @param pageCond 分页信息
	 * @param conditionMap 条件
	 * @param authSql
	 * @return
	 */
	List<BudgetAuthorVO> queryAuthorPageInfo(Page<BudgetAuthorVO> pageCond, Map<String, Object> conditionMap, String authSql);
}
