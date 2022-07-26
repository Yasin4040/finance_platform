package com.jtyjy.finance.manager.event;

import com.jtyjy.finance.manager.bean.BudgetBankAccount;
import com.jtyjy.finance.manager.bean.WbUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-13 08:48
 */
public class InvokeRecordEvent extends ApplicationEvent {

    private WbUser wbUser;

    public WbUser getWbUser() {
        return wbUser;
    }

    public void setWbUser(WbUser wbUser) {
        this.wbUser = wbUser;
    }

    public InvokeRecordEvent(BudgetBankAccount source, WbUser wbUser) {
        super(source);
        this.wbUser = wbUser;
    }
}
