package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

public class PCUGMemberHandler 
extends GenericHandler
{
    public PCUGMemberHandler(Subscriber member)
    { 
        this.memberSub = member;       
    }

   
    @Override
    protected void handleSuccess(Context ctx, ChargableItemResult ret)
    {
    	 ChargeRefundResultHandlerSupport.logDebugMsg(ctx, ret); 
    }
    
    @Override
    protected void handleError(Context ctx, ChargableItemResult ret)
    {
    	switch(ret.getChargeResult())
    	{
    	case TRANSACTION_SKIPPED_DUPLICATE_CHARGE:
    	case TRANSACTION_SKIPPED_DUPLICATE_REFUND:
    		redoTransaction(ctx, ret); 
    	}
    	
    	if (ret.getChargeResult() == TRANSACTION_SUCCESS)
    	{
    		handleSuccess(ctx, ret);
    		
    	}else
    	{
    		logError(ctx, ret);
    	}
    }
 
    
    public void redoTransaction(Context ctx,ChargableItemResult ret)
    {
          Transaction tran = ret.getTrans(); 
        
        
        if ( tran != null)
        {
           try {
                ret.trans = CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, ret.getTrans());
                ret.setChargeResult( TRANSACTION_SUCCESS);
           } catch (OcgTransactionException e )
           {
                ret.chargeResult = TRANSACTION_FAIL_OCG;  
           }
           catch ( Throwable t )
           {
                ret.chargeResult = TRANSACTION_FAIL_UNKNOWN; 
                ret.thrownObject = t; 
           }   
        }
    }
    
    public void logError(Context ctx,  ChargableItemResult ret)
    {
        String msg = "Fail to " + ACTION_TYPE_NAMES.get(new Integer(ret.getAction())) + 
        " to subscriber " + 
        memberSub.getId()+ 
        " for " + CHARGABLE_ITEM_NAMES[ret.getChargableItemType()] + " " + 
        ret.getId() + 
        " error = "  + ((null == ret.getThrownObject())? "" : ret.getThrownObject().getMessage()) +
        " retcode = " + TRANSACTION_RETURN_NAMES.get( new Integer(ret.getChargeResult())); 
        
        ChargeRefundResultHandlerSupport.logErroMsg(ctx, msg, ret);
    }
        
    @Override
    public void setDelegate(ChargeRefundResultHandler handler)
    {
    
    }
    
    @Override
    protected ChargeRefundResultHandler getDelegate()
    {
        return null; 
    }


    Subscriber memberSub; 
}