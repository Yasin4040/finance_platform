package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.log.DefaultChangeLog;
import com.jtyjy.core.log.LoggerAction;
import com.jtyjy.core.service.BaseService;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetCommonAttachment;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.dto.TabPayOrderVO;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.query.UploadQuery;
import com.jtyjy.finance.manager.utils.FileUtils;
import com.jtyjy.finance.manager.utils.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Date;
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
    @Autowired
    private StorageClient storageClient;
    @Autowired
    private BudgetCommonAttachmentService attachmentService;

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
     * @param fineCount 罚款原因
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
    /*
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
    @SneakyThrows
    public void uploadFile(UploadQuery query) {

        String loginUser = LoginThreadLocal.get().getEmpno();
        for (MultipartFile file : query.getFiles()) {
            String[] urls = storageClient.upload_file("group1", file.getSize(), new UploadCallback() {

                @Override
                public int send(OutputStream outputStream) throws IOException {
                    outputStream.write(file.getBytes());
                    return 0;
                }
            }, FileUtils.getFileType(file.getOriginalFilename()), null);

            String printUrl = StringUtils.join(urls, "/");
            BudgetCommonAttachment attachment = new BudgetCommonAttachment();
            attachment.setFileUrl(printUrl);
            attachment.setFileType(1);//默认1 普通文件
            attachment.setFileExtName(FileUtils.getFileType(file.getOriginalFilename()));
            attachment.setFileName(getFileNameNoEx(file.getOriginalFilename()));
            attachment.setContactId(query.getContactId());
            attachment.setCreator(loginUser);
            attachment.setCreateTime(new Date());
            attachmentService.save(attachment);
        }

    }

    public List<BudgetCommonAttachment> viewAttachment(String contactId) {
        return attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId,contactId).list();
    }

    public void delAttachment(String id) {
        attachmentService.removeById(id);
    }

    @SneakyThrows
    public String upload(CommonsMultipartFile file) {
        String[] urls = storageClient.upload_file("group1", file.getSize(), new UploadCallback() {
            @Override
            public int send(OutputStream outputStream) throws IOException {
                outputStream.write(file.getBytes());
                return 0;
            }
        }, FileUtils.getFileType(file.getOriginalFilename()), null);
        return StringUtils.join(urls, "/");

    }
}
