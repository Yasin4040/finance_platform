package com.jtyjy.finance.manager.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.TabDm;

public interface TabDmMapper extends BaseMapper<TabDm> {

	@Select("SELECT COUNT(0) FROM tab_dm WHERE dm_type = #{type} AND dm = #{dm}")
	int selectSameRecords(@Param("type")String dmType, @Param("dm")String dm);

	@Select("SELECT * FROM tab_dm WHERE dm_type = #{type} AND dm_name = #{name}")
	TabDm selectSameRecordsByName(@Param("type")String dmType, @Param("name")String dmName);

	@Select("SELECT dm,dm_name AS dmName FROM tab_dm WHERE dm_type = 'PARENT'")
	List<Map<String, Object>> selectAllType();

	@Select("SELECT dm,dm_name AS dmName,dm_value as dmValue FROM tab_dm WHERE dm_status = 1 AND dm_type = #{type} order by dm_type,dm_order asc")
	List<TabDm> selectSubAllType(@Param("type") String type);
}
