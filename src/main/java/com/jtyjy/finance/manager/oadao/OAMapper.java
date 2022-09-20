package com.jtyjy.finance.manager.oadao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.oapojo.OaUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OAMapper extends BaseMapper<OaUser> {
	List<Map<String, Object>> getSpecialPerson();

    List<Map<String, String>> getNodeList(@Param("values") List<String> values);

    String getOaUserId(@Param("empNo") String empNo);
    String getDeptId(@Param("deptName") String deptName);
    String getNodeName(int nodeId);

    String getLogStatus(@Param("requestId") String requestId,@Param("nodeId") int nodeId);
}
