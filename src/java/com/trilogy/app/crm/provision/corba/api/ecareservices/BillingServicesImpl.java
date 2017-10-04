package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamSetHolder;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingServicesPOA;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.InfoLogMsg;

public class BillingServicesImpl extends BillingServicesPOA implements ContextAware, Common
{
    public BillingServicesImpl(Context ctx)
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
    
    public int getUsageByAcctId(String acctId, BillingParamID[] reqSet, BillingParamSetHolder outputSet)
    {
        BillingServicesFacade service = (BillingServicesFacade) getContext().get(BillingServicesFacade.class);
        
        if ( service == null )
        {
            new InfoLogMsg(this, "Billing Service Not Found", null).log(getContext());
            return ErrorCode.SERVICE_NOT_FOUND;
        }
        return service.getUsageByAcctId(getContext(), acctId, reqSet, outputSet); 
        
    }

    public int getUsageByMSISDN(String msisdn, BillingParamID[] reqSet, BillingParamSetHolder outputSet)
    {
        BillingServicesFacade service = (BillingServicesFacade) getContext().get(BillingServicesFacade.class);
        
        if ( service == null )
        {
            new InfoLogMsg(this, "Billing Service Not Found", null).log(getContext());
            return ErrorCode.SERVICE_NOT_FOUND;
        }
        return service.getUsageByMSISDN(getContext(), msisdn, reqSet, outputSet); 
        
    }

    

    protected Context ctx_;
   
}
