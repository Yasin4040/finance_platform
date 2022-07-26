package com.jtyjy.finance.manager.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

import com.jtyjy.finance.manager.vo.SubjectInfoVO;

public class TreeData extends HashMap<String, Object>{
      private String idprefix;
      private String id;
      private String text;
      private String value;
      private String iconCls;
      private String pid;
      private String treetype = "extjs";
      private Boolean checked;
      private boolean leaf = true;
      private Boolean expanded;
      private int fitlerLevel = 1;
      private String fieldvalue;
      private String operator;
      private boolean addParentFilter = false;
      private String fieldName;
      private String moduleName;
      private String fieldtitle;
      private boolean isBaseField = false;
      private Integer count;
      
      public String getId()
      {
        return this.id;
      }
      
      public void setId(String id)
      {
        this.id = id;
      }
      
      public String getText()
      {
        return this.text;
      }
      
      public void setText(String text)
      {
        this.text = text;
      }
      
      public String getPid()
      {
        return this.pid;
      }
      
      public void setPid(String pid)
      {
        this.pid = pid;
      }
      
      public String getTreetype()
      {
        return this.treetype;
      }
      
      public void setTreetype(String treetype)
      {
        this.treetype = treetype;
      }
      
      public Boolean isChecked()
      {
        return this.checked;
      }
      
      public void setChecked(Boolean checked)
      {
        this.checked = checked;
      }
      
      public boolean isLeaf()
      {
        return this.leaf;
      }
      
      public void setLeaf(boolean leaf)
      {
        this.leaf = leaf;
      }
      
      public String getIdprefix()
      {
        return this.idprefix;
      }
      
      public void setIdprefix(String idprefix)
      {
        this.idprefix = idprefix;
      }
      
      public Boolean isExpanded()
      {
        return this.expanded;
      }
      
      public void setExpanded(Boolean expanded)
      {
        this.expanded = expanded;
      }
      
      public String getIconCls()
      {
        return this.iconCls;
      }
      
      public void setIconCls(String iconCls)
      {
        this.iconCls = iconCls;
      }
      
      public String getValue()
      {
        return this.value;
      }
      
      public void setValue(String value)
      {
        this.value = value;
      }
      
      public int getFitlerLevel()
      {
        return this.fitlerLevel;
      }
      
      public void setFitlerLevel(int fitlerLevel)
      {
        this.fitlerLevel = fitlerLevel;
      }
      
      public String getFieldvalue()
      {
        return this.fieldvalue;
      }
      
      public void setFieldvalue(String fieldvalue)
      {
        this.fieldvalue = fieldvalue;
      }
      
      public String getOperator()
      {
        return this.operator;
      }
      
      public void setOperator(String operator)
      {
        this.operator = operator;
      }
      
      public boolean isAddParentFilter()
      {
        return this.addParentFilter;
      }
      
      public void setAddParentFilter(boolean addParentFilter)
      {
        this.addParentFilter = addParentFilter;
      }
      
      public String getFieldName()
      {
        return this.fieldName;
      }
      
      public void setFieldName(String fieldName)
      {
        this.fieldName = fieldName;
      }
      
      private static String getEmptyStr(int level)
      {
        String emptyStr = "";
        for (int i = 0; i < level - 1; i++) {
          emptyStr = emptyStr + " ";
        }
        return emptyStr;
      }
      
      public static List<Map<String, Object>> list2listtree(List<Map<String, Object>> datas, String pid, int level)
      {
        List<Map<String, Object>> tds = new ArrayList();
        for (Map<String, Object> data : datas)
        {
          TreeData td = new TreeData();
          if (data.containsKey("realid")) {
            td.put("realid", data.get("realid"));
          }
          String pid_ = null;
          if (null != data.get("pid")) {
            pid_ = data.get("pid").toString();
          } else if (null != data.get("pId")) {
            pid_ = data.get("pId").toString();
          } else if (null != data.get("parentid")) {
            pid_ = data.get("parentid").toString();
          } else if (null != data.get("parentId")) {
            pid_ = data.get("parentId").toString();
          }
          td.setPid(pid_);
          if (StringUtils.isEmpty(pid)) {
            pid = pid_;
          }
          String text_ = null;
          if (null != data.get("text")) {
            text_ = data.get("text").toString();
          } else if (null != data.get("name")) {
            text_ = data.get("name").toString();
          }
          td.setText(text_);
          
          String emptyStr = getEmptyStr(level);
          td.put("listtext", emptyStr + text_);
          
          String value_ = null;
          if (null != data.get("value")) {
            value_ = data.get("value").toString();
          }
          td.setValue(value_);
          String iconCls_ = null;
          if (null != data.get("iconCls")) {
            iconCls_ = data.get("iconCls").toString();
          }
          td.setIconCls(iconCls_);
          boolean checked_ = ((Boolean)data.getOrDefault("checked", Boolean.valueOf(false))).booleanValue();
          td.setChecked(Boolean.valueOf(checked_));
          boolean expanded_ = ((Boolean)data.getOrDefault("expanded", Boolean.valueOf(false))).booleanValue();
          td.setExpanded(Boolean.valueOf(expanded_));
          String fieldvalue_ = (String)data.getOrDefault("fieldvalue", "");
          if (StringUtils.isEmpty(fieldvalue_)) {
            fieldvalue_ = value_;
          }
          td.setFieldvalue(fieldvalue_);
          String fieldName_ = (String)data.getOrDefault("fieldName", "");
          td.setFieldName(fieldName_);
          String operator_ = (String)data.getOrDefault("operator", "=");
          td.setOperator(operator_);
          
          String moduleName_ = (String)data.getOrDefault("moduleName", "");
          td.setModuleName(moduleName_);
          String fieldtitle_ = (String)data.getOrDefault("fieldtitle", "");
          td.setFieldtitle(fieldtitle_);
          Boolean isBaseField_ = (Boolean)data.getOrDefault("isBaseField", Boolean.valueOf(false));
          td.setIsBaseField(isBaseField_.booleanValue());
          Integer count_ = (Integer)data.getOrDefault("count", null);
          td.setCount(count_);
          
          String id = data.get("id").toString();
          td.setId(id);
          if (((StringUtils.isEmpty(pid)) && (StringUtils.isEmpty(pid_))) || 
            (pid.equals(pid_)))
          {
            td.putAll(data);
            td.put("level", Integer.valueOf(level));
            
            List<Map<String, Object>> datas_ = list2listtree(datas, id, level + 1);
            td.setLeaf(true);
            td.setExpanded(Boolean.valueOf(true));
            
            tds.add(td.me2map());
            tds.addAll(datas_);
          }
        }
        return tds;
      }
      
      public void list(List<SubjectInfoVO> datas) {
          int currentLevel = 1;
          int currentParentIndex = 0;
          List<SubjectInfoVO> newList = new ArrayList<>();
          for(SubjectInfoVO vo : datas) {
              if ( 0 == vo.getLevel()) {
                  newList.add(vo);
              }else if (currentLevel == vo.getLevel()) {
                  if (newList.get(currentParentIndex).getId().equals(vo.getParentid())) {
                      currentParentIndex ++;
                      newList.add(currentParentIndex, vo);
                  }else {
                      for(int i = 0; i < newList.size(); i++) {
                          if(newList.get(i).getId().equals(vo.getParentid())) {
                              currentParentIndex = i;
                              newList.add(currentParentIndex, vo);
                              break;
                          }
                      }
                  }
              }else {
                  currentLevel = vo.getLevel();
                  for(int i = 0;i < newList.size(); i++) {
                      if(newList.get(i).getId().equals(vo.getParentid())) {
                          currentParentIndex = i;
                          newList.add(currentParentIndex, vo);
                          break;
                      }
                  }
              }
          }
      }
      
      public static List<Map<String, Object>> list2Multipletree(List<Map<String, Object>> datas, String pid, int level)
      {
        List<Map<String, Object>> tds = new ArrayList();
        for (Map<String, Object> data : datas)
        {
          TreeData td = new TreeData();
          
          String pid_ = null;
          if (null != data.get("pid")) {
            pid_ = data.get("pid").toString();
          } else if (null != data.get("pId")) {
            pid_ = data.get("pId").toString();
          } else if (null != data.get("parentid")) {
            pid_ = data.get("parentid").toString();
          } else if (null != data.get("parentId")) {
            pid_ = data.get("parentId").toString();
          }
          td.setPid(pid_);
          if (StringUtils.isEmpty(pid)) {
            pid = pid_;
          }
          String text_ = null;
          if (null != data.get("text")) {
            text_ = data.get("text").toString();
          } else if (null != data.get("name")) {
            text_ = data.get("name").toString();
          }
          td.setText(text_);
          String value_ = null;
          if (null != data.get("value")) {
            value_ = data.get("value").toString();
          }
          td.setValue(value_);
          String iconCls_ = null;
          if (null != data.get("iconCls")) {
            iconCls_ = data.get("iconCls").toString();
          }
          td.setIconCls(iconCls_);
          
          boolean expanded_ = ((Boolean)data.getOrDefault("expanded", Boolean.valueOf(false))).booleanValue();
          td.setExpanded(Boolean.valueOf(expanded_));
          String fieldvalue_ = (String)data.getOrDefault("fieldvalue", "");
          if (StringUtils.isEmpty(fieldvalue_)) {
            fieldvalue_ = value_;
          }
          td.setFieldvalue(fieldvalue_);
          String fieldName_ = (String)data.getOrDefault("fieldName", "");
          td.setFieldName(fieldName_);
          String operator_ = (String)data.getOrDefault("operator", "=");
          td.setOperator(operator_);
          
          String moduleName_ = (String)data.getOrDefault("moduleName", "");
          td.setModuleName(moduleName_);
          String fieldtitle_ = (String)data.getOrDefault("fieldtitle", "");
          td.setFieldtitle(fieldtitle_);
          Boolean isBaseField_ = (Boolean)data.getOrDefault("isBaseField", Boolean.valueOf(false));
          td.setIsBaseField(isBaseField_.booleanValue());
          Integer count_ = (Integer)data.getOrDefault("count", null);
          td.setCount(count_);
          
          String id = data.get("id").toString();
          td.setId(id);
          if (((StringUtils.isEmpty(pid)) && (StringUtils.isEmpty(pid_))) || 
            (pid.equals(pid_)))
          {
            td.putAll(data);
            td.put("level", Integer.valueOf(level));
            List<Map<String, Object>> datas_ = list2tree(datas, id, level + 1);
            if ((null != datas_) && (datas_.size() > 0))
            {
              td.put("children", datas_);
              td.setExpanded(Boolean.valueOf(true));
            }
            else
            {
              td.setExpanded(Boolean.valueOf(false));
            }
            td.setLeaf(false);
            
            tds.add(td.me2map());
          }
        }
        return tds;
      }
      
      public static List<Map<String, Object>> list2tree(List<Map<String, Object>> datas, String pid, int level, boolean checkflag, boolean expandflag, boolean totop)
      {
        Set<String> ids;
        if (true == totop)
        {
          ids = new HashSet();
          for (Map<String, Object> data : datas)
          {
            String id = data.get("id").toString();
            ids.add(id);
          }
          for (Map<String, Object> data : datas)
          {
            String pid_ = null;
            if (null != data.get("pid")) {
              pid_ = data.get("pid").toString();
            } else if (null != data.get("pId")) {
              pid_ = data.get("pId").toString();
            } else if (null != data.get("parentid")) {
              pid_ = data.get("parentid").toString();
            } else if (null != data.get("parentId")) {
              pid_ = data.get("parentId").toString();
            }
            if ((StringUtils.isNotEmpty(pid_)) && (!ids.contains(pid_))) {
              data.put("pid", "0");
            }
          }
        }
        return list2tree(datas, pid, level, checkflag, expandflag);
      }
      
      public static List<Map<String, Object>> list2tree(List<Map<String, Object>> datas, String pid, int level, boolean checkflag, boolean expandflag)
      {
        List<Map<String, Object>> tds = new ArrayList();
        for (Map<String, Object> data : datas)
        {
          TreeData td = new TreeData();
          
          String pid_ = null;
          if (null != data.get("pid")) {
            pid_ = data.get("pid").toString();
          } else if (null != data.get("pId")) {
            pid_ = data.get("pId").toString();
          } else if (null != data.get("parentid")) {
            pid_ = data.get("parentid").toString();
          } else if (null != data.get("parentId")) {
            pid_ = data.get("parentId").toString();
          }
          td.setPid(pid_);
          if (StringUtils.isEmpty(pid)) {
            pid = pid_;
          }
          String text_ = null;
          if (null != data.get("text")) {
            text_ = data.get("text").toString();
          } else if (null != data.get("name")) {
            text_ = data.get("name").toString();
          }
          td.setText(text_);
          String value_ = null;
          if (null != data.get("value")) {
            value_ = data.get("value").toString();
          }
          td.setValue(value_);
          String iconCls_ = null;
          if (null != data.get("iconCls")) {
            iconCls_ = data.get("iconCls").toString();
          }
          td.setIconCls(iconCls_);
          boolean checked_ = ((Boolean)data.getOrDefault("checked", Boolean.valueOf(false))).booleanValue();
          td.setChecked(Boolean.valueOf(checked_));
          boolean expanded_ = ((Boolean)data.getOrDefault("expanded", Boolean.valueOf(false))).booleanValue();
          td.setExpanded(Boolean.valueOf(expanded_));
          String fieldvalue_ = data.getOrDefault("fieldvalue", "").toString();
          if (StringUtils.isEmpty(fieldvalue_)) {
            fieldvalue_ = value_;
          }
          td.setFieldvalue(fieldvalue_);
          String fieldName_ = (String)data.getOrDefault("fieldName", "");
          td.setFieldName(fieldName_);
          String operator_ = (String)data.getOrDefault("operator", "=");
          td.setOperator(operator_);
          
          String moduleName_ = (String)data.getOrDefault("moduleName", "");
          td.setModuleName(moduleName_);
          String fieldtitle_ = (String)data.getOrDefault("fieldtitle", "");
          td.setFieldtitle(fieldtitle_);
          Boolean isBaseField_ = (Boolean)data.getOrDefault("isBaseField", Boolean.valueOf(false));
          td.setIsBaseField(isBaseField_.booleanValue());
          Integer count_ = (Integer)data.getOrDefault("count", null);
          td.setCount(count_);
          
          String id = data.get("id").toString();
          td.setId(id);
          if (((StringUtils.isEmpty(pid)) && (StringUtils.isEmpty(pid_))) || 
            (pid.equals(pid_)))
          {
            td.putAll(data);
            td.put("level", Integer.valueOf(level));
            List<Map<String, Object>> datas_ = list2tree(datas, id, level + 1, checkflag, expandflag);
            td.put("children", datas_);
            td.setExpanded(Boolean.valueOf((null != datas_) && (datas_.size() != 0)));
            td.setLeaf((null == datas_) || (datas_.size() == 0));
            
            Map map = td.me2map();
            if (!checkflag) {
              map.remove("checked");
            }
            if (!expandflag) {
              map.remove("expanded");
            }
            tds.add(map);
          }
        }
        return tds;
      }
      
      public static List<Map<String, Object>> list2tree(List<Map<String, Object>> datas, String pid, int level)
      {
        return list2tree(datas, pid, level, true, true);
      }
      
      public static Map<String, Object> createTree(List<Map<String, Object>> tree, String rootname)
      {
        Map<String, Object> root = new HashMap();
        root.put("children", tree);
        root.put("text", rootname);
        return root;
      }
      
      public static Map<String, Object> createTree(List<Map<String, Object>> tree)
      {
        return createTree(tree, "root");
      }
      
      public Map<String, Object> me2map()
      {
        Map<String, Object> dd = this;
        String idprefix = "";
        if (!StringUtils.isEmpty(idprefix)) {
          idprefix = idprefix + "_";
        }
        dd.put("id", idprefix + this.id);
        if (dd.containsKey("realid")) {
          dd.put("id", dd.get("realid"));
        }
        dd.put("text", this.text);
        dd.put("iconCls", this.iconCls);
        dd.put("pid", idprefix + this.pid);
        dd.put("checked", this.checked);
        dd.put("leaf", Boolean.valueOf(this.leaf));
        dd.put("expanded", this.expanded);
        dd.put("fieldName", this.fieldName);
        dd.put("addParentFilter", Boolean.valueOf(this.addParentFilter));
        dd.put("operator", this.operator);
        
        dd.put("fitlerLevel", Integer.valueOf(this.fitlerLevel));
        dd.put("fieldvalue", this.fieldvalue);
        dd.put("value", this.value);
        return dd;
      }
      
      public String getModuleName()
      {
        return this.moduleName;
      }
      
      public void setModuleName(String moduleName)
      {
        this.moduleName = moduleName;
      }
      
      public String getFieldtitle()
      {
        return this.fieldtitle;
      }
      
      public void setFieldtitle(String fieldtitle)
      {
        this.fieldtitle = fieldtitle;
      }
      
      public boolean getIsBaseField()
      {
        return this.isBaseField;
      }
      
      public void setIsBaseField(boolean isBaseField)
      {
        this.isBaseField = isBaseField;
      }
      
      public Integer getCount()
      {
        return this.count;
      }
      
      public void setCount(Integer count)
      {
        this.count = count;
      }
}
