package com.jtyjy.finance.manager.config;

import com.jtyjy.api.OAServiceProxy;
import com.jtyjy.ecology.webservice.workflow.EcologyWorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.RemoteException;

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
        OAServiceProxy oaServiceProxy = new OAServiceProxy("http://api.jtyjy.com/services/OAService?wsdl");
        String result = null;
        try {
            result = oaServiceProxy.getOAUserinfo("17474");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }
}
