package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


public class DebtCollectionAgencySpidSupportAccountRequiredFieldPredicate implements AccountRequiredFieldPredicate
{

    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        boolean result = false;
        Account account = (Account) obj;
        try
        {
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
 
            if (spid != null)
            {
                result = spid.isEnableDebtCollectionAgencies();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve spid " + account.getSpid() + ": " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * {@inheritDoc}
     */
    public Object deepClone() throws CloneNotSupportedException
    {
        return clone();
    }
}
