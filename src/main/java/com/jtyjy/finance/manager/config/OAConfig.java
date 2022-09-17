package com.jtyjy.finance.manager.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iamxiongx.util.message.DateUtil;
import com.jtyjy.api.OAServiceProxy;
import com.jtyjy.ecology.webservice.workflow.EcologyWorkflowClient;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.dto.commission.OAApplicationDTO;
import com.jtyjy.finance.manager.dto.commission.OAApplicationDetailDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author User
 */
@Configuration
public class OAConfig {

    @Value("${oa.service.endpoint}")
    private String endpoint;

    @Value("${oa.ecology.workflow.url}")
    private String workflowUrl;

    /**
     * oa 服务
     */
    @Bean
    public OAServiceProxy oaServiceProxy() {
        return new OAServiceProxy(this.endpoint);
    }

    /**
     * oa 工作流
     */
    @Bean
    public EcologyWorkflowClient ecologyworkflowclient() {
        return new EcologyWorkflowClient(this.workflowUrl);
    }


    public static void main(String[] args) {
//        OAServiceProxy oaServiceProxy = new OAServiceProxy("http://api.jtyjy.com/services/OAService?wsdl");
//        String result = null;
//        try {
//            result = oaServiceProxy.getOAUserinfo("17474");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        System.out.println(result);
        WorkflowInfo wi = new WorkflowInfo();
        wi.setCreatorId("5001");
        wi.setRequestLevel("0");
        wi.setRequestName("提成申请单流程--" + 5001);
        OAApplicationDTO oaDTO = new OAApplicationDTO();
        oaDTO.setSqr("17474");
//        oaDTO.setBm("义务教育事业群");
        oaDTO.setBm("200");
        oaDTO.setZbrq("2022-09-16");
        oaDTO.setZfsy("支付21届1月第2批提成");
        oaDTO.setBz("备注");
        oaDTO.setBh("TC20220916023");
        //附件可以用, 隔开
        oaDTO.setFj("457,458");
        List<OAApplicationDetailDTO> oaDetailList = new ArrayList<>();
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(oaDTO);
        List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.toJSON(oaDetailList);
        System.out.println(JSONObject.toJSON(wi));
//       http://192.168.4.63/workflow/workflow/addwf0.jsp?ajax=1&src=editwf&wfid=5263&isTemplate=0
        EcologyWorkflowClient ecologyWorkflowClient = new EcologyWorkflowClient("http://192.168.4.63/services/WorkflowService");
        String requestId = ecologyWorkflowClient.createWorkflow(wi, "5263", main, list);
//        String requestId =  oaService.createWorkflow(wi, tcWorkFlowId, main, list);
        if (requestId == null || Integer.parseInt(requestId) < 0) {
            System.out.println(requestId+"???");
            throw new RuntimeException("提交失败，oa系统未找到你的上级人员，请联系oa管理员。");
        }
    }
}
