package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.WbUser;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Admin
 */
public interface WbUserMapper extends BaseMapper<WbUser> {
	
    List<WbUser> getUserPageInfo(Page pageCond, String displayName, String authSql);
    List<WbUser> getAllUserPageInfo(Page pageCond, String displayName, String authSql);

    List<WbUser> getUserPageInfoByUnit(Page pageCond, String displayName, String deptIds, String userIds, String authSql);

    WbUser selectUserByEmpNo(Long empNo);
}
