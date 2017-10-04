package com.trilogy.app.crm.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.snippet.log.Logger;


public class AccountSpidSwitchFromWebListener implements PropertyChangeListener
{
    public void propertyChange(final PropertyChangeEvent evt)
    {
        final Context ctx = (Context) evt.getSource();
        final Account oldAccount = (Account) evt.getOldValue();
        final Account newAccount = (Account) evt.getNewValue();

        if (Account.isFromWebNewOrPreviewOnSpid(ctx))
        {
            long oldSpid = -1;
            if (oldAccount != null)
            {
                oldSpid = oldAccount.getSpid();
            }

            long newSpid = newAccount.getSpid();

            if (newSpid != oldSpid)
            {
                try
                {
                    // Setting default values based on spid configuration
                    CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, newAccount.getSpid());

                    if (crmSpid.getBillCycle()!= CRMSpid.DEFAULT_BILLCYCLE)
                    {
                        newAccount.setBillCycleID(crmSpid.getBillCycle());
                    }
                    
                    if (crmSpid.getTaxAuthority()!= CRMSpid.DEFAULT_TAXAUTHORITY)
                    {
                        newAccount.setTaxAuthority(crmSpid.getTaxAuthority());
                    }

                    if (crmSpid.getDealer()!= CRMSpid.DEFAULT_DEALER)
                    {
                        newAccount.setDealerCode(crmSpid.getDealer());
                    }
                    
                    if (crmSpid.getDefaultCreditCategory()!= CRMSpid.DEFAULT_DEFAULTCREDITCATEGORY)
                    {
                        newAccount.setCreditCategory(crmSpid.getDefaultCreditCategory());
                    }

                    if (crmSpid.getCurrency()!= CRMSpid.DEFAULT_CURRENCY)
                    {
                        newAccount.setCurrency(crmSpid.getCurrency());
                    }
                }
                catch (Throwable t)
                {
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "Error setting default SPID values in Account initialization", t);
                    }
                }
            }
        }
    }
}