/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 *
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.home.account.ContractEndDateSettingHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.Lookup;

/**
 * Calculates & renders the Contract's End date value for the account based upon
 *     *Contract's Start Date chosen on the account
 *  *Current configuration for the selected Contract of the account viz Duration,Frequency
 *  
 * @author abaid
 * @author cindy.wong@redknee.com
 */

public class ContractEndDateWebControl extends ProxyWebControl 
{
    
    public ContractEndDateWebControl() 
    {
        super(new DateWebControl(15));
    }
    
    @Override
    public void toWeb(
            Context context,
            final PrintWriter out,
            final String name,
            final Object obj)
    {
        Context subCtx = context.createSubContext();
        subCtx.setName(this.getClass().getName());

        Account account = (Account)subCtx.get(AbstractWebControl.BEAN);
        Account dbAccount = (Account) subCtx.get(Lookup.ACCOUNT);
        
        Date date = (Date)obj;
        Contract contract = null;
        
        if (date != null)
        { 
            LogSupport.debug(subCtx, this, "Account to display: [BAN=" + account.getBAN() + ", contract=" + account.getContract() + ", contractStartDate=" + account.getContractStartDate() + ", contractEndDate=" + account.getContractEndDate());

            if (dbAccount == null)
            {
                try
                {
                    dbAccount = AccountSupport.getAccount(subCtx, account.getBAN());
                }
                catch (HomeException exception)
                {
                    LogSupport.info(subCtx, this, "Exception caught while retrieving account " + account.getBAN(), exception);
                }
            }

            if (dbAccount != null)
            {
                LogSupport.debug(subCtx, this, "Account in context: [BAN=" + dbAccount.getBAN() + ", contract=" + dbAccount.getContract() + ", contractStartDate=" + dbAccount.getContractStartDate() + ", contractEndDate=" + dbAccount.getContractEndDate());
            }

            boolean lookup = true;
            /*
             * [Cindy Wong] 2010-08-06: don't calculate new end date if contract
             * and start date hasn't changed -- end date should remaing the same
             * even if the duration in the contract was modified.
             */
            if (dbAccount != null &&
                SafetyUtil.safeEquals(dbAccount.getBAN(), account.getBAN()))
            {
                lookup = dbAccount.getContract() != account.getContract() || 
                    !SafetyUtil.safeEquals(dbAccount.getContractStartDate(),
                        account.getContractStartDate());
            }

            LogSupport.debug(subCtx, this, "lookup = " + lookup);

            if (lookup)
            {
                contract = ContractEndDateSettingHome.getContract(subCtx, account); 
                if(contract != null)
                {
                    Date calculatedContractEndDate = 
                        ContractEndDateSettingHome.calculateContractEndDate(contract, account);
                    date.setTime(calculatedContractEndDate.getTime());
                }
            }
        }
        super.toWeb(context, out, name, obj);
    }
}
