package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplication;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.bean.BudgetExtractFeePayDetailBeforeCal;
import com.jtyjy.finance.manager.bean.BudgetExtractsum;
import com.jtyjy.finance.manager.dto.commission.FeeImportErrorDTO;
import com.jtyjy.finance.manager.dto.commission.IndividualIssueExportDTO;
import com.jtyjy.finance.manager.query.commission.FeeQuery;
import com.jtyjy.finance.manager.vo.application.BudgetSubjectVO;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoUpdateVO;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
* @author User
* @description 针对表【budget_extract_commission_application(提成支付申请单  主表 )】的数据库操作Service
* @createDate 2022-08-26 11:08:05
*/
public interface BudgetExtractCommissionApplicationService extends IService<BudgetExtractCommissionApplication> {

    void importIndividual(MultipartFile multipartFile);

    void saveEntity(BudgetExtractsum extract, String badDebt,Object... params);

    void saveExtractImportDetails(Map<Integer, String> detailMap, BudgetExtractsum extractSum);

    CommissionApplicationInfoVO getApplicationInfo(String sumId);

    void updateStatusBySumId(String sumId, Integer status);

    void generateReimbursement(Long sumId,BudgetExtractsum extractsum);

    Optional<BudgetExtractCommissionApplication> getApplicationBySumId(String sumId);

    void updateApplicationInfo(CommissionApplicationInfoUpdateVO updateVO);

    PageResult<BudgetSubjectVO> listSubjectMonthAgent(HashMap<String, Object> paramMap, Integer page, Integer rows);

    List<IndividualIssueExportDTO> exportIssuedTemplate(String extractMonth);

    List<FeeImportErrorDTO> importFeeTemplate(MultipartFile multipartFile,String extractMonth);

    IPage<BudgetExtractFeePayDetailBeforeCal> selectFeePage(FeeQuery query);

    void validateApplication(BudgetExtractsum extractSum);

    void uploadOA(BudgetExtractCommissionApplication application);
}
