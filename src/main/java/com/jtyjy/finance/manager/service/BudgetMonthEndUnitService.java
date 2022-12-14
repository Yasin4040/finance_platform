package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.dto.BudgetAuditDTO;
import com.jtyjy.finance.manager.dto.BudgetDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthEndUnitService extends DefaultBaseService<BudgetMonthEndUnitMapper, BudgetMonthEndUnit> {

    private final TabChangeLogMapper loggerMapper;

    private final BudgetMonthStartupMapper bmsMapper;

    private final BudgetMonthEndUnitMapper mapper;

    private final WbUserMapper wbUserMapper;

    private final MessageSender messageSender;

    private final BudgetUnitMapper budgetUnitMapper;

    private final BudgetYearPeriodMapper budgetYearPeriodMapper;

    private final BudgetMonthEndUnitLogMapper bmeulMapper;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_month_end_unit"));
    }

    /**
     * ??????????????????
     *
     * @param yearId
     * @param monthStartId
     * @return
     */
    public List<BudgetUnitVO> getEndMonthUnit(Long yearId, Long monthStartId) {
        BudgetMonthStartup budgetMonthStartup = this.bmsMapper.selectById(monthStartId);
        return this.mapper.getUnitMonthEndTime(yearId, budgetMonthStartup.getMonthid());
    }

    /**
     * ??????????????????
     *
     * @param periodId ??????id
     * @param unitIds  ????????????ids????????????,?????????
     */
    public void setEndMonthUnit(String periodId, String unitIds) {
        if (!periodId.contains("-")) {
            return;
        }
        Long yearId = Long.parseLong(periodId.split("-")[0]);
        Long monthId = Long.parseLong(periodId.split("-")[1]);
        BudgetMonthEndUnit updateEntity = new BudgetMonthEndUnit();
        updateEntity.setMonthendflag(false);
        UpdateWrapper<BudgetMonthEndUnit> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("requeststatus", 2);
        updateWrapper.eq("yearid", yearId);
        updateWrapper.eq("monthid", monthId);
        this.mapper.update(updateEntity, updateWrapper);

        Date nowDate = new Date();
        if (StringUtils.isNotBlank(unitIds)) {
            updateEntity = new BudgetMonthEndUnit();
            updateEntity.setMonthendflag(true);
            updateEntity.setMonthendtime(nowDate);
            updateEntity.setPremonthendtime(nowDate);
            List<Long> unitIdList = new ArrayList<>();
            for (String unitId : unitIds.split(",")) {
                unitIdList.add(Long.valueOf(unitId));
            }
            updateWrapper.in("unitid", unitIdList);
            this.mapper.update(updateEntity, updateWrapper);
        }
        BudgetMonthEndUnitLog bean = new BudgetMonthEndUnitLog();
        bean.setYearid(yearId);
        bean.setMonthid(monthId);
        bean.setUnitids(unitIds);
        String allunitids = this.mapper.getUnitGroup(yearId);
        bean.setUnitids(allunitids);
        bean.setCreatetime(nowDate);
        bean.setCreater(UserThreadLocal.get().getUserId());
        bean.setCreatename(UserThreadLocal.get().getDisplayName());
        this.bmeulMapper.insert(bean);

    }

    /**
     * ????????????????????????????????????
     */
    public PageResult<BudgetMonthEndUnit> listMonthAuditPage(Integer page, Integer rows, Long yearId, Long budgetUnitId, Long monthId, String name) {
        Page<BudgetMonthEndUnit> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("yearId", yearId);
        paramMap.put("budgetUnitId", budgetUnitId);
        paramMap.put("monthId", monthId);
        paramMap.put("name", name);

        List<BudgetMonthEndUnit> resultList = this.mapper.listMonthAuditPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ??????????????????
     *
     * @param budgetUnitId ????????????Id
     * @param monthId      ??????Id
     * @param remark       ????????????
     * @param type         1?????? 2?????? 3????????????
     */
    public void monthBudgetAudit(Long budgetUnitId, Long monthId, String remark, int type,WbUser user) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????Id??????");
        }
        BudgetMonthEndUnit monthEndUnit = this.mapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
                .eq("unitid", budgetUnitId)
                .eq("monthid", monthId));
        if (monthEndUnit == null) {
            throw new RuntimeException("???????????????" + budgetUnit.getName() + "??????" + monthId + "?????????????????????");
        }
        if (type == 1 && monthEndUnit.getRequeststatus() < 1) {
            throw new RuntimeException("??????????????????????????????" + budgetUnit.getName() + "???" + monthId + "?????????????????????");
        } else if (type == 1 && monthEndUnit.getRequeststatus() == 2) {
            throw new RuntimeException("??????????????????????????????" + budgetUnit.getName() + "???" + monthId + "??????????????????");
        } else if (type == 2 && monthEndUnit.getRequeststatus() < 1) {
            throw new RuntimeException("??????????????????????????????" + budgetUnit.getName() + "???" + monthId + "?????????????????????");
        } else if (type == 2 && monthEndUnit.getRequeststatus() == 2) {
            throw new RuntimeException("??????????????????????????????" + budgetUnit.getName() + "???" + monthId + "??????????????????");
        } else if (type == 3 && monthEndUnit.getRequeststatus() != 2) {
            throw new RuntimeException("????????????????????????????????????" + budgetUnit.getName() + "???" + monthId + "????????????????????????");
        } else if (type == 2 || type == 3) {
            if (monthEndUnit.getMonthendflag()) {
                throw new RuntimeException(type == 2 ? "" : "??????" + "??????????????????????????????" + budgetUnit.getName() + "???" + monthId + "??????????????????");
            } else if (monthEndUnit.getRequeststatus() == -1) {
                throw new RuntimeException(type == 2 ? "" : "??????" + "??????????????????????????????" + budgetUnit.getName() + "???" + monthId + "??????????????????");
            }
        }

        // ??????????????????????????????
        BudgetMonthEndUnit updateMonthEndUnit = new BudgetMonthEndUnit();
        updateMonthEndUnit.setVerifyorid(user.getUserId());
        updateMonthEndUnit.setVerifyorname(user.getDisplayName());
        updateMonthEndUnit.setVerifytime(new Date());
        if (type == 1) {
            // ??????
            updateMonthEndUnit.setRequeststatus(2);
            updateMonthEndUnit.setVerifystr("");
        } else {
            // ?????? or ????????????
            updateMonthEndUnit.setVerifystr(remark);
            updateMonthEndUnit.setRequeststatus(-1);
        }
        this.mapper.update(updateMonthEndUnit, new QueryWrapper<BudgetMonthEndUnit>()
                .eq("unitid", budgetUnitId)
                .eq("monthid", monthId));

        // ????????????????????????
        String managers = budgetUnit.getManagers();
        if (StringUtils.isNotBlank(managers)) {
            String userId = managers.split(",")[0];

            WbUser sendUser = this.wbUserMapper.selectById(userId);
            if (sendUser != null) {
                String period = this.budgetYearPeriodMapper.selectById(budgetUnit.getYearid()).getPeriod();
                String message = "???" + period + "??????????????????" + budgetUnit.getName() + "???" + monthId;
                switch (type) {
                    case 1:
                        this.messageSender.sendQywxMsg(new QywxTextMsg(sendUser.getUserName(), null, null, 0, message + "????????????????????????", 0));
                        break;
                    case 2:
                        this.messageSender.sendQywxMsg(new QywxTextMsg(sendUser.getUserName(), null, null, 0, message + "?????????????????????, ???????????????" + remark, 0));
                        break;
                    case 3:
                        this.messageSender.sendQywxMsg(new QywxTextMsg(sendUser.getUserName(), null, null, 0, message + "???????????????????????????", 0));
                        break;
                    default:
                }
            }
        }

    }

    public String batchMonthBudgetAudit(BudgetAuditDTO dto, int type) {
        if(Objects.isNull(dto.getBudgetDto())){
            throw new RuntimeException("????????????????????????");
        }
        StringBuilder builder = new StringBuilder();
        List<CompletableFuture> futures = new ArrayList<>();
        WbUser user = UserThreadLocal.get();
        for(BudgetDTO budgetDTO: dto.getBudgetDto()){
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    this.monthBudgetAudit(budgetDTO.getBudgetUnitId(),budgetDTO.getMonthId(), dto.getRemark(), type,user);
                }catch (Exception e){
                    builder.append(e.getMessage()).append("\n");
                }
                return 1;
            }, BudgetUnitService.executor);
            futures.add(future);
        }
        for(CompletableFuture future:futures){
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(builder.length()>0){
            return "???????????????????????????????????????\n"+builder.toString();
        }
        return "";
    }
}
