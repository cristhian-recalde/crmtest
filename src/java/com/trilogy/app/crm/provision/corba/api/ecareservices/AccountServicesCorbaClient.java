package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountParamSetHolder;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountServices;
import com.trilogy.app.crm.provision.corba.api.ecareservices.acctmgmt.AccountServicesHelper;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;

public class AccountServicesCorbaClient extends AbstractCorbaClient
{

    private static final String SERVICE_DESCRIPTION = "CORBA client for account services";

    public AccountServicesCorbaClient(Context ctx)
    {
        super(ctx);      
    }

    public AccountServicesCorbaClient(Context ctx, String key)
    {
        super(ctx, key);
    }

    @Override
    public synchronized org.omg.CORBA.Object getService()
    {
        org.omg.CORBA.Object objServant = super.getService();
        
        if ( objServant == null )
        {
            invalidate(null);
            return null;
        }
        
        try
        {
            new InfoLogMsg(this, "About to narrow the Account Services", null).log(getContext());
            // attempt to derive SubProvision
            service_ = AccountServicesHelper.narrow(objServant);
            return service_;
        }
        catch (Throwable th)
        {
            new InfoLogMsg(this, "Got an exception while narrowing the Account Services "+th.getMessage(), th).log(getContext());
            invalidate(th);
            return null;
        }        
    }
    
    public int getAccountInfoByMsisdn(
            String msisdn, 
            AccountParamID[] reqSet, 
            AccountParamSetHolder outputSet)
    {
        int ec = ErrorCode.COMMUNICATION_FAILURE;
        new InfoLogMsg(this, "get the Account Services", null).log(getContext());
        AccountServices service = (AccountServices)getService();
        
       
        if ( service != null)
        {
            try 
            {
                ec = service.getAccountInfoByMsisdn(msisdn, reqSet, outputSet);
            }
            catch (org.omg.CORBA.COMM_FAILURE cf)
            {
                new InfoLogMsg(this, cf.getMessage(), cf).log(getContext());
                invalidate(cf);
            }
            catch (Exception ex)
            {
                new InfoLogMsg(this, ex.getMessage(), ex).log(getContext());
                ec = ErrorCode.INTERNAL_ERROR;
            }
        }
        else
        {
            new InfoLogMsg(this, "Fail to the Account Services", null).log(getContext());
        }
        
        return ec;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }
   
}
