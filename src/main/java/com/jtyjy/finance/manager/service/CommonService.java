package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.log.DefaultChangeLog;
import com.jtyjy.core.log.LoggerAction;
import com.jtyjy.core.service.BaseService;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.dto.TabPayOrderVO;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.utils.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@JdbcSelector(value = "defaultJdbcTemplateService")
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonService extends DefaultBaseService<WbBanksMapper, WbBanks> {

    @Autowired
    private WbBanksMapper banksMapper;

    @Autowired
    private TabDmService dmService;

    @Value("${sunpay.add.url}")
    private String sunPayUrl;



    @Override
    public void doLog(LoggerAction loggerAction, DefaultChangeLog changeLog) throws Exception {

    }

    @Override
    public BaseMapper getLoggerMapper() {
        return null;
    }

    @Override
    public void setBaseLoggerBean() {

    }

    /*
     * Author: ldw
     * Description: 获取系统内所有的银行类型，如：中国银行，招商银行等
     * Date: 2021/4/23 15:00
     */
    public List<String> getDistinctBankTypes() {
        /*QueryWrapper<WbBanks>  wrapper = new QueryWrapper<>();
        wrapper.*/
        String sql = "SELECT DISCINCT bank_name FROM wb_banks";
        System.err.println(this.jdbcTemplateService);
        List<String> query = this.jdbcTemplateService.getColumnValue(String.class,"wb_banks","bank_name",null);
        HashSet set = new HashSet<String>(query);
        query.clear();
        query.addAll(set);
        return query;
    }

    /**
     * <p></p>
     * @author minzhq
     * @date 2022/8/6 10:15
     * @param opt 1 追加  2 拆借
     * @param remark 罚款原因
     */
    public void createBudgetFine(int opt,int fineCount,String empNo,String creator){
        try{

            TabDm dm = dmService.getByPrimaryKey("is_test_fine", "is_test_fine");
            String fineEmpNo = empNo;
            if("1".equals(dm.getDmValue())){
                TabDm dm1 = dmService.getByPrimaryKey("test_fine_notice", "test_fine_notice");
                fineEmpNo = dm1.getDmValue();
            }
            HttpUtil.doGet(sunPayUrl+"?empNo="+fineEmpNo+"&opt="+opt+"&count="+fineCount+"&creator="+creator);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
