package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.framework.xhome.context.Context;

public abstract class AbstractFilterHandler 
extends GenericHandler 
{

    
    public void handleTransaction(Context ctx,  ChargableItemResult ret)
    {
        if (delegate_ != null)
        {    
            if(  !isRejected(ctx,ret) )
            {      
                getDelegate().handleTransaction(ctx, ret);
            }
            else 
            {
                ChargeRefundResultHandlerSupport.logDebugMsg(ctx, ret); 

            }
        }    

    }    
    
    abstract protected boolean isRejected(Context ctx, ChargableItemResult ret); 
 }
