package com.jtyjy.finance.manager.oadao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.oapojo.OaUser;

import java.util.List;
import java.util.Map;

public interface OAMapper extends BaseMapper<OaUser> {
	List<Map<String, Object>> getSpecialPerson();
}
