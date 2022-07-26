package com.jtyjy.finance.manager.listener;

import com.alibaba.fastjson.JSON;
import com.jtyjy.finance.manager.bean.BudgetBankAccount;
import com.jtyjy.finance.manager.bean.DbInvokeRecord;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.event.InvokeRecordEvent;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetBankAccountMapper;
import com.jtyjy.finance.manager.mapper.InvokeRecordMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-13 08:55
 */
@Component
@AllArgsConstructor
public class InvokeRecordListener {

    private final BudgetBankAccountMapper budgetBankAccountMapper;
    private final InvokeRecordMapper invokeRecordMapper;

    @Async
    @Order
    @EventListener(InvokeRecordEvent.class)
    public void saveInvokeRecord(InvokeRecordEvent event) {
        System.out.println("测试事件监听");
        BudgetBankAccount record = (BudgetBankAccount) event.getSource();
        long id = record.getId();
        String preBody = "";
        String postBody = JSON.toJSONString(record);
        if(!Objects.isNull(id)){
            BudgetBankAccount preRecord = budgetBankAccountMapper.selectById(id);
            if(!Objects.isNull(preRecord)){
                preBody = JSON.toJSONString(preRecord);
            }
        }
        WbUser wbUser = event.getWbUser();
        DbInvokeRecord invokeRecord = new DbInvokeRecord();
        invokeRecord.setCreateTime(new Date());
        invokeRecord.setBankId(record.getId());
        invokeRecord.setPreBody(preBody);
        invokeRecord.setPostBody(postBody);
        invokeRecord.setCreator(wbUser.getDisplayName());
        invokeRecord.setCreateOid(wbUser.getUserName());
        invokeRecordMapper.insert(invokeRecord);
    }
}
