package com.trilogy.app.crm.ccr.corba;

import com.trilogy.app.crm.ccr.CreditCardRetrieval;
import com.trilogy.app.crm.ccr.CreditCardRetrievalHelper;
import com.trilogy.app.crm.ccr.CreditCardRetrievalPackage.CreditCardInfoHolder;
import com.trilogy.app.crm.provision.corba.api.ecareservices.AbstractCorbaClient;
import com.trilogy.app.crm.provision.corba.api.ecareservices.error.ErrorCode;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * 
 * 
 * @author dannyng
 *
 */
public class CreditCardRetrievalCorbaClient extends AbstractCorbaClient
{

    private static final String SERVICE_DESCRIPTION = "CORBA client for credit card retrieval";


    public CreditCardRetrievalCorbaClient(Context ctx)
    {
        super(ctx);
    }


    public CreditCardRetrievalCorbaClient(Context ctx, String key)
    {
        super(ctx, key);
    }


    @Override
    public synchronized org.omg.CORBA.Object getService()
    {
        org.omg.CORBA.Object objServant = super.getService();
        if (objServant == null)
        {
            invalidate(null);
            return null;
        }
        try
        {
            new InfoLogMsg(this, "About to narrow the Credit Card Retrieval Service", null).log(getContext());
            // attempt to derive CreditCardRetrieval
            service_ = CreditCardRetrievalHelper.narrow(objServant);
            return service_;
        }
        catch (Throwable th)
        {
            new InfoLogMsg(this, "Got an exception while narrowing the Credit Card Retrieval Service " + th.getMessage(), th)
                    .log(getContext());
            invalidate(th);
            return null;
        }
    }


    public int retrieveCreditCardInfo(String msisdn, CreditCardInfoHolder holder)
    {
        int ec = ErrorCode.COMMUNICATION_FAILURE;
        new InfoLogMsg(this, "get the Credit Card Retrieval Service", null).log(getContext());
        CreditCardRetrieval service = (CreditCardRetrieval) getService();
        if (service != null)
        {
            try
            {
                ec = service.retrieveCreditCardInfo(msisdn, holder);
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
            new InfoLogMsg(this, "Fail to the Credit Card Retrieval Service", null).log(getContext());
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


    @Override
    public boolean isAlive()
    {
        return false;
    }
}
