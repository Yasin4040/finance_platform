package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.DbInvokeRecord;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.mapper.InvokeRecordMapper;
import com.jtyjy.finance.manager.mapper.TabDmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-13 09:11
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InvokeRecordService extends DefaultBaseService<InvokeRecordMapper, DbInvokeRecord> {
    @Override
    public BaseMapper getLoggerMapper() {
        return null;
    }

    @Override
    public void setBaseLoggerBean() {

    }


}
