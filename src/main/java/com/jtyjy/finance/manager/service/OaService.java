package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.api.OAServiceProxy;
import com.jtyjy.ecology.webservice.workflow.EcologyWorkflowClient;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.bean.BudgetYearAgentlendDetail;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.mapper.BudgetUnitMapper;
import com.jtyjy.finance.manager.mapper.TabDmMapper;
import com.jtyjy.finance.manager.utils.AesUtil;
import com.jtyjy.finance.manager.ws.BudgetYearAgentLending;
import com.jtyjy.finance.manager.ws.BudgetYearAgentLendingDetail;
import localhost.services.DocService.DocServiceLocator;
import localhost.services.DocService.DocServicePortType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import weaver.docs.webservices.DocAttachment;
import weaver.docs.webservices.DocInfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author User
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OaService {

    @Value("${fastdfs.url}")
    public String fastDfsUrl;

    @Value("${oa.service.doc.url}")
    public String docServiceUrl;

    private final TabDmMapper dmMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final OAServiceProxy oaServiceProxy;
    private final EcologyWorkflowClient ecologyWorkflowClient;



    /**
     * 通过工号获取oa用户id
     */
    public String getOaUserId(String empNo,List<Map<String,Object>> list) {
        try {
            /**
             * update by minzhq
             * 修改成直接用oa获取
             */
            Map<String, Object> map = list.stream().filter(e -> empNo.equals(e.get("EMPNO").toString())).findFirst().orElse(null);
            if(map!=null && map.get("USERMSGS")!=null){
                return map.get("USERMSGS").toString();
            }
            //TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "BUDGET_OA").eq("dm",empNo));
            // 判断是否存在对应的工号
            //if (Objects.nonNull(dm)) {
            //    return dm.getDmValue().replace("-", ",");
            //}
            // oa系统实时查询工号对应的用户Id和部门Id
            String result = this.oaServiceProxy.getOAUserinfo(empNo);
            if (StringUtils.isNotBlank(result)) {
                List<Map> users = JSON.parseArray(result, Map.class);
                if (users != null && !users.isEmpty()) {
                    return users.get(0).get("ID").toString() + "," + users.get(0).get("DEPARTMENTID").toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0,0";
    }

    /**
     * 是否省区预算单位
     */
    public Boolean isProvinceUnit(Long unitId) {
        // 除了自营办
        // 54 直营总公司基础预算单位id
        // 29  直营办基础预算单位id
        Integer count = this.budgetUnitMapper.isProvinceUnit((long) 54, (long) 29, unitId);
        return count > 0;
    }

    /**
     * 删除工作流
     */
    public boolean deleteRequest(String requestId, String userId) {
        return this.ecologyWorkflowClient.deleteRequest(Integer.parseInt(requestId), Integer.parseInt(userId));
    }

    /**
     * 创建工作流
     */
    public String createWorkflow(WorkflowInfo wi, String wfId, Map<String, Object> main, List<Map<String, Object>> details) {
        return this.ecologyWorkflowClient.createWorkflow(wi, wfId, main, details);
    }

    /**
     * 创建附件文档对象
     */
    public int createDoc(String empNo, String password, InputStream input, String filename, String fileUrl, String docName) {
        DocServicePortType service = null;
        String session = null;
        try {
            service = new DocServiceLocator().getDocServiceHttpPort(new URL(docServiceUrl));
            String decrypt = AesUtil.decrypt(password);
            session = service.login(empNo, decrypt, 0, "127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringUtils.isEmpty(session)) {
            throw new RuntimeException("提交审核失败!oa认证失败!");
        }
        DocInfo doc = new DocInfo();
        byte[] content = null;
        try {
            int byteRead;
            byte[] data = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((byteRead = input.read(data)) != -1) {
                out.write(data, 0, byteRead);
                out.flush();
            }
            content = out.toByteArray();
            input.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DocAttachment da = new DocAttachment();
        da.setDocid(0);
        da.setImagefileid(0);
        da.setFilecontent(Base64.encodeBase64String(content));
        da.setFilerealpath(fileUrl);
        da.setFilename(filename);
        da.setIszip(1);

        doc.setId(0);
        doc.setDocSubject(filename);
        doc.setDoccontent(docName);
        doc.setMaincategory(-1);
        doc.setSeccategory(59);
        doc.setSubcategory(43);
        doc.setDoccreatername(empNo);
        doc.setAttachments(new DocAttachment[]{da});
        int i = -1;
        try {
            i = service.createDoc(doc, session);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }
    
    public static void main(String[] args) throws RemoteException {
//    	OAServiceProxy proxy = new OAServiceProxy("http://api.jtyjy.com/services/OAService?wsdl");
//    	String result = proxy.getOAUserinfo("17474");
//    	System.out.println(result);

        EcologyWorkflowClient ecologyWorkflowClient = new EcologyWorkflowClient("http://192.168.4.63/services/WorkflowService");
        BudgetYearAgentLending yearLending = new BudgetYearAgentLending();
        yearLending.setFj("-1");
        yearLending.setSsbm("200");
        yearLending.setSqr("5001");
        yearLending.setYsjb("fff");
        // 申请日期
        yearLending.setSqrq("2022-08-08");
        yearLending.setCjcs(1);

        yearLending.setCjje(new BigDecimal("5"));
        yearLending.setWfid("2783");
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(yearLending);
        List<BudgetYearAgentLendingDetail> workflowDetails = new ArrayList<>();
        BudgetYearAgentLendingDetail detail = new BudgetYearAgentLendingDetail();
        detail.setCjysdw("bbb");
        detail.setCjkm("aaa");
        detail.setCjdy("dfff");
        detail.setCjhndys(new BigDecimal("1"));
        detail.setCcyysdw("ffffff");
        detail.setCckm("dfsdfsadf");
        detail.setCcdy("dsfdf");
        detail.setCjhndysu(new BigDecimal("12332"));
        detail.setCjje(new BigDecimal("12323"));
        detail.setCjyy("dsfsdf");
        detail.setSfsqmf("0");
        detail.setMflysm("sdfsdsd");
        workflowDetails.add(detail);
        List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.toJSON(workflowDetails);
        WorkflowInfo wi = new WorkflowInfo();
        wi.setCreatorId("5001");
        wi.setRequestLevel("0");
        wi.setRequestName("年度预算拆借--aaaa");
        String workflow = ecologyWorkflowClient.createWorkflow(wi, "2783", main, list);
        System.out.println(workflow);
    }
}
