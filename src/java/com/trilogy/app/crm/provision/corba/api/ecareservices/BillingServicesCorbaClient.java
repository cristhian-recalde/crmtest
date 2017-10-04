package com.trilogy.app.crm.provision.corba.api.ecareservices;

import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamID;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingParamSetHolder;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingServices;
import com.trilogy.app.crm.provision.corba.api.ecareservices.billingmgmt.BillingServicesHelper;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.framework.xhome.context.Context;

public class BillingServicesCorbaClient extends AbstractCorbaClient
{

    private static final String SERVICE_DESCRIPTION = "CORBA client for billing services";

    public BillingServicesCorbaClient(Context ctx)
    {
        super(ctx);      
    }

    public BillingServicesCorbaClient(Context ctx, String key)
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
            // attempt to derive SubProvision
            service_ = BillingServicesHelper.narrow(objServant);
            return service_;
        }
        catch (Throwable th)
        {
            invalidate(th);
            return null;
        }        
    }
    
    public int getUsageByAcctId(
            String msisdn, 
            BillingParamID[] reqSet, 
            BillingParamSetHolder outputSet)
    {
        int ec = ErrorCode.COMMUNICATION_FAILURE;
        BillingServices service = (BillingServices)getService();
        
        if ( service != null)
        {
            try 
            {
                ec = service.getUsageByAcctId(msisdn, reqSet, outputSet);
            }
            catch (org.omg.CORBA.COMM_FAILURE cf)
            {
                
                invalidate(cf);
            }
            catch (Exception ex)
            {
                ec = ErrorCode.INTERNAL_ERROR;
            }
        }
        
        return ec;
    }

    public int getUsageByMSISDN(
            String msisdn, 
            BillingParamID[] reqSet, 
            BillingParamSetHolder outputSet)
    {
        int ec = ErrorCode.COMMUNICATION_FAILURE;
        BillingServices service = (BillingServices)getService();
        
        if ( service != null)
        {
            try 
            {
                ec = service.getUsageByMSISDN(msisdn, reqSet, outputSet);
            }
            catch (org.omg.CORBA.COMM_FAILURE cf)
            {
                
                invalidate(cf);
            }
            catch (Exception ex)
            {
                ec = ErrorCode.INTERNAL_ERROR;
            }
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
