package com.jtyjy.finance.manager.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iamxiongx.util.http.HttpUtil;
import com.jtyjy.finance.manager.bean.BudgetReimburmentTimedetail;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderDetail;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitMapper;
import com.jtyjy.finance.manager.mapper.BudgetReimburmentTimedetailMapper;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderDetailMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.easyexcel.BxTimeDetailExcelData;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.core.tools.HttpClientTool;

import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimburmentTimedetailService extends DefaultBaseService<BudgetReimburmentTimedetailMapper, BudgetReimburmentTimedetail> {

	private final TabChangeLogMapper loggerMapper;

	private final BudgetReimburmentTimedetailMapper mapper;
	
	private final BudgetReimbursementorderDetailMapper orderDetailMapper;
    
	private final BudgetBillingUnitMapper billUnitMapper;
	
	private final BudgetUnitService unitService;
	
	private final BudgetReimbursementorderFdtaskService fdtaskService;
	
	private final BudgetYearPeriodService yearService;
	
	private final static String GETDAYS_BYSTARTTIMEANDENDTIME_URL = "http://api.jtyjy.com/ecology/flow/getHrInterfaceAction?starttime=%s&endtime=%s&start=%s&end=%s&empno=%s";
    //上班时间
    private final static String WORK_TIMEFORHOUR = " 08:00";
    //下班时间
    private final static String OFFWORK_TIMEFORHOUR = " 18:00";
    
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimburment_timedetail"));
	}

    public void createBudgetReimbursentTimeDetail(Date startDate,Date endDate,String reimcode,String empno,Integer type){
        String starttime = "";
        String endtime = null == endDate ? null : Constants.FORMAT_10.format(endDate);
        QueryWrapper<BudgetReimburmentTimedetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reimcode", reimcode);
        queryWrapper.eq("iseffective", 1);
        queryWrapper.eq("type", type);
        BudgetReimburmentTimedetail timedetail = this.getOne(queryWrapper);
        if(timedetail == null){
            if (null == startDate) {
                startDate = new Date();
            }
            starttime = Constants.FORMAT_10.format(startDate);
            timedetail = new BudgetReimburmentTimedetail();
            timedetail.setId(null);
            timedetail.setReimcode(reimcode);
            timedetail.setStarttime(startDate);
            timedetail.setEndtime(endDate);
            timedetail.setType(type);
            timedetail.setIseffective(1);
            timedetail.setEmpno(empno);
            if(endtime == null){
                timedetail.setDays(null);
            }else if(starttime.equals(endtime)){
                timedetail.setDays(new BigDecimal("1"));
            }else{
                BigDecimal days = getWorkTimeByStarttimeAndEndTime(starttime,endtime,empno);
                if(empno.equals("admin")){
                    timedetail.setDays(BigDecimal.ZERO);
                }else{
                    timedetail.setDays(days);
                }
            }           
            timedetail.setCreattime(new Date());
            this.save(timedetail);
        }else{
            if (null == startDate) {
                starttime = Constants.FORMAT_10.format(timedetail.getStarttime());
            }else {
                starttime = Constants.FORMAT_10.format(startDate);
            }
            if (null == endDate) {
                endDate = new Date();
            }
            endtime = Constants.FORMAT_10.format(endDate);
            
            if(starttime.equals(endtime)){
                timedetail.setDays(new BigDecimal("1"));
            }else{
                BigDecimal days = getWorkTimeByStarttimeAndEndTime(starttime,endtime,empno);
                timedetail.setDays(days);
            }
            timedetail.setEndtime(endDate);
            this.updateById(timedetail);
        }
    }	
    
    public BigDecimal getWorkTimeByStarttimeAndEndTime(String starttime,String endtime,String empno){
        String url = String.format(GETDAYS_BYSTARTTIMEANDENDTIME_URL, "08:00", "18:00", starttime, endtime, empno);
                
        //GETDAYS_BYSTARTTIMEANDENDTIME_URL+"?starttime="+WORK_TIMEFORHOUR+"&endtime="+OFFWORK_TIMEFORHOUR+"&start="+starttime+"&end="+endtime+"&empno="+empno;
        
        String result = "";
        Map<String,Object> map = null;
        try{
            result =  HttpClientTool.getRequest(url);               
        }catch(Exception e){
            e.printStackTrace();
            return new BigDecimal(1);
        }
        map = (Map<String, Object>) JSON.parse(result);
        //总天数
        BigDecimal days = BigDecimal.ZERO;
        try{
            if(("admin").equals(empno)){
                days = new BigDecimal("0");                             
            }else{
                try{
                    days = new BigDecimal(map.get("data").toString());                      
                }catch(Exception e){
                    days = new BigDecimal("-1");
                }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
        return days;
    }

    public void exportBxTime(Long yearId, Long monthId, String authSql, HttpServletResponse response) throws Exception{
        List<BudgetReimburmentTimedetail> retList = this.mapper.getTimeDetail(yearId, monthId, authSql);
        
        List<BxTimeDetailExcelData> datas = new ArrayList<>();
        FastDateFormat sdf = Constants.NOSS_FORMAT;
        if(null != retList && !retList.isEmpty()){
            List<Long> bxids = retList.stream().map(e->e.getOrderId()).collect(Collectors.toList());
            List<BudgetReimbursementorderDetail> alldetail = this.orderDetailMapper.selectList(new QueryWrapper<BudgetReimbursementorderDetail>().in("reimbursementid", bxids));
            for(BudgetReimburmentTimedetail timeDetail : retList){
                BxTimeDetailExcelData data;
                String reimcode = timeDetail.getReimcode();
                boolean addFlag = true;
                if (datas.size() > 0) {
                    //同一单号
                    if (reimcode.equals(datas.get(datas.size() - 1).getReimcode())) {
                        data = datas.get(datas.size() - 1);
                        addFlag = false;
                    }else {
                        data = new BxTimeDetailExcelData();
                    }
                }else {
                    data = new BxTimeDetailExcelData();
                }
                
                
                if(1 == timeDetail.getType()){
                    data.setT1(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    data.setTt1(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));
                }else if(2 == timeDetail.getType()){
                    if(timeDetail.getDays().compareTo(BigDecimal.ZERO) == 0){
                        //兼容票面审核没有接收直接审核的情况
                        data.setT2(BigDecimal.ZERO);                     
                    }else{
                        data.setT2(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    }
                    data.setTt2(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));                                                       
                }else if(3 == timeDetail.getType()){                  
                    data.setT3(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    data.setTt3(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));
                }else if(4 == timeDetail.getType()){
                    data.setT4(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    data.setTt4(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));
                    //是否全是无票
                    if(StringUtils.isBlank(timeDetail.getFdUnitName())){
                        data.setBunitname("都是无票");
                    }else{
                        data.setBunitname(timeDetail.getFdUnitName());
                    }
                }else if(5 == timeDetail.getType()){
                    data.setT5(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    data.setTt5(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));                                     
                }else if(6 == timeDetail.getType()){
                    data.setT6(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    data.setTt6(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));                                       
                }else if(7 == timeDetail.getType()){
                    data.setT7(getHourByStarttimeAndEndTime1(timeDetail.getStarttime(),timeDetail.getEndtime(),timeDetail.getDays()));
                    data.setTt7(null == timeDetail.getEndtime() ? "" : sdf.format(timeDetail.getEndtime()));                                       
                }               
                if (addFlag) {
                    if(timeDetail.getSubmitTime()==null) throw new RuntimeException("导出失败!报销单【"+reimcode+"】提交时间为NULL！");
                    String submittime = sdf.format(timeDetail.getSubmitTime());
                    data.setReimcode(reimcode);
                    data.setUnitname(timeDetail.getUnitName());             
                    data.setBxje(timeDetail.getReimMoney());
                    List<BudgetReimbursementorderDetail> details = alldetail.stream().filter(e->e.getReimbursementid().equals(timeDetail.getOrderId())).collect(Collectors.toList());
                    String subjectnames = details.stream().map(e->e.getSubjectname()).distinct().collect(Collectors.joining(","));
                    data.setSubjectnames(subjectnames);
                    data.setSubmittime(submittime);
                    data.setBunitname("未分单");
                    datas.add(data); 
                }
            }
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/bxtimeTemplate.xlsx");
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("导出报销时间节点表", response), BxTimeDetailExcelData.class).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("时间节点");
            Map<String, String> heads = new HashMap<>();
            String exportDate = "";
            if (null != yearId) {
                exportDate += yearService.getById(yearId).getPeriod();
            }
            if (null != monthId) {
                exportDate += monthId + "月";
            }
            heads.put("exportDate", exportDate);
            workBook.fill(heads, sheet);
            workBook.fill(datas, sheet);
            workBook.finish();
        }
                            
        
    }
    
    private static BigDecimal getHourByStarttimeAndEndTime1(Date starttime,Date endTime,BigDecimal days) throws ParseException{     
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        //if(StringUtils.isEmpty(empno) 
        //      || Long.compare(starttime.getTime(), endTime.getTime())>=0) return getHour(calendar, starttime, endTime);
        if (null == endTime) {
            return null;
        }
        FastDateFormat sdf = Constants.NOSS_FORMAT;
        String startDate = sdf.format(starttime).substring(0, 10);
        String endDate = sdf.format(endTime).substring(0, 10);
        FastDateFormat sdf1 = Constants.FORMAT_10;
        
        Date date1_Start = sdf.parse(startDate+WORK_TIMEFORHOUR);
        Date date1_End = sdf.parse(startDate+OFFWORK_TIMEFORHOUR);          
        Date date2_Start = sdf.parse(endDate+WORK_TIMEFORHOUR);
        Date date2_End = sdf.parse(endDate+OFFWORK_TIMEFORHOUR);
            
        if(startDate.equals(endDate)){
            //同一天  
            /**
             * 如果开始时间在上班前，取上班时间。如果结束时间在下班后，取下班时间
             * 如果开始时间在下班后。时间为0
             */
            if(Long.compare(starttime.getTime(), date1_End.getTime())>=0)return BigDecimal.ZERO;
            return getHour(calendar,
                           Long.compare(starttime.getTime(), date1_Start.getTime())==-1?date1_Start:starttime,
                           Long.compare(endTime.getTime(), date1_End.getTime())>=0?date1_End:endTime);
        }else{
            //不是同一天             
            Date _date1 = sdf1.parse(startDate);
            Date _date2 = sdf1.parse(endDate);
            long betweenDate = (_date2.getTime() - _date1.getTime())/(60*60*24*1000);
            BigDecimal dec_date = new BigDecimal(betweenDate).add(new BigDecimal("1"));     
            if(betweenDate >0 && dec_date.compareTo(new BigDecimal(2)) ==0 && days.compareTo(new BigDecimal(1)) ==0 && dec_date.compareTo(days)>0){
                days = dec_date;
            }
            //总小时
            BigDecimal hours = days.multiply(new BigDecimal(24));
            /**
             * 总小时 - 无效小时   
             * 接口返回的天数*24 - 开始当天的无效小时 - 结束当天的无效小时 - (接口返回的天数-2)*15  ==(9小时为上班小时)
             */
            //1:开始当天的无效小时
            BigDecimal invalidHourForStartTime;
            if(Long.compare(starttime.getTime(), date1_Start.getTime())==-1){
                //开始时间在上班时间之前
                invalidHourForStartTime = new BigDecimal(24).subtract(getHour(calendar, date1_Start, date1_End));
            }else if(Long.compare(starttime.getTime(), date1_End.getTime())>=0){
                //开始时间在下班时间之后
                invalidHourForStartTime = new BigDecimal(24);
            }else{
                //开始时间在上班时间
                BigDecimal t = getHour(calendar, starttime, date1_End);
                invalidHourForStartTime = new BigDecimal(24).subtract(t);
            }
            //2:结束当天的无效小时
            BigDecimal invalidHourForEndTime;
            if(Long.compare(endTime.getTime(), date2_Start.getTime())==-1){
                //结束时间在上班时间之前
                invalidHourForEndTime = new BigDecimal(24);
            }else if(Long.compare(endTime.getTime(), date2_End.getTime())>=0){
                //结束时间在下班时间之后
                invalidHourForEndTime = new BigDecimal(24).subtract(getHour(calendar, date2_Start, date2_End));
            }else{
                //结束时间在上班时间
                BigDecimal t = getHour(calendar, date2_Start, endTime);
                invalidHourForEndTime = new BigDecimal(24).subtract(t);
            }
            BigDecimal tt1 = hours.subtract(invalidHourForStartTime).subtract(invalidHourForEndTime);
            BigDecimal tt2 = (new BigDecimal(24).subtract(getHour(calendar, date1_Start, date1_End))).multiply(days.subtract(new BigDecimal(2)));
            return tt1.subtract(tt2);
        }
    }
    
    private static BigDecimal getHour(Calendar calendar,Date t1,Date t2){
        calendar.setTime(t1);
        long timeInMillis1 = calendar.getTimeInMillis();
        calendar.setTime(t2);
        long timeInMillis2 = calendar.getTimeInMillis();
        BigDecimal betweenHours =  new BigDecimal(timeInMillis2 - timeInMillis1).divide(new BigDecimal(1000L*3600L),2,BigDecimal.ROUND_HALF_DOWN);
        return betweenHours;
    }
}
