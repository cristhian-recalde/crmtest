/*
 * Created on April 11, 2006
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED
 * BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.*;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * @author kso
 * 
 */
public class AccountServicesImpl extends AccountServicesPOA implements ContextAware
{
    public AccountServicesImpl(Context ctx)
    {
        super();
        setContext(ctx);
    }
    
    public Context getContext()
    {        
        return ctx_;
    }

    public void setContext(Context ctx)
    {
        this.ctx_ = ctx;
        
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountServicesOperations#getAccountInfoByMsisdn(java.lang.String, com.redknee.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamID[], com.redknee.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamSetHolder)
     * 
     *  To retrieve the account.
     *  @param msisdn
     *  @param reqSet
     *  @param outputSet
     */
    public int getAccountInfoByMsisdn(String msisdn, AccountParamID[] reqSet, AccountParamSetHolder outputSet)
    {
        AccountServicesFacade service = (AccountServicesFacade)getContext().get(AccountServicesFacade.class);
        
        if ( service == null )
        {
            new InfoLogMsg(this, "Account Service Not Found", null).log(getContext());
            return ErrorCode.SERVICE_NOT_FOUND;
        }
        return service.getAccountInfoByMsisdn(getContext(), msisdn, reqSet, outputSet);
        
    }

    public boolean attemptRate()
    {
        
        LicenseMgr lMgr = (LicenseMgr)ctx_.get(LicenseMgr.class);
        
        return lMgr.attemptRate(ctx_, LicenseConstants.ACCT_SVC_LICENSE_KEY);
    }

    protected Context ctx_;
}
