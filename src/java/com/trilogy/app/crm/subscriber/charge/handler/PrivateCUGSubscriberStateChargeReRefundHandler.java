package com.trilogy.app.crm.subscriber.charge.handler;

import java.util.Iterator;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.customize.PrivateCUGOwnerTransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.customize.TransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;


/**
 * this class is supposed to handle charge and refund during subscriber 
 * state change. if the pcug was unprovisioned and refund in cug pipe line,
 * then  ignore. 
 * 
 * some state change could trigger pcug remove. but never trigger creation.
 * 
 * 
 * @author lxia
 *
 */
public class PrivateCUGSubscriberStateChargeReRefundHandler 
implements ChargeRefundResultHandler, ChargingConstants
{
    
 
    public void handleTransaction(Context ctx,  ChargableItemResult ret)
    {
    	if (ret.getChargeResult() == TRANSACTION_REDIRECT_PCUG)
    	{
    		// first entry
    		createTransaction(ctx, ret);
    		
    	}  else 
    	{
    		handleSecondEntry(ctx, ret); 
    	}
    	
    }
    
    
    public void handleSecondEntry(Context ctx, ChargableItemResult ret)
    {

    	if (ret.getChargeResult() == TRANSACTION_SKIPPED_DUPLICATE_CHARGE || 
    			ret.getChargeResult() == TRANSACTION_SKIPPED_DUPLICATE_CHARGE )
    	{
    		// second entry
    		ret.setChargeResult(TRANSACTION_SUCCESS); 
    	}
    		// second entry
    	   
    	ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, null, ret);
    	        
    	    if ( realHandler != null )
    	    {    
    	       realHandler.handleTransaction(ctx, ret);
    	    }    

     }
   
    
    private void createTransaction(Context ctx, ChargableItemResult ret)
    {
    	final Subscriber sub = ret.getSubscriber();
    	try 
    	{
    		final SubscriberAuxiliaryService subService = (SubscriberAuxiliaryService)ret.getChargableObjectRef(); 
    		final AuxiliaryService service = (AuxiliaryService) ret.chargableObject; 
    		
    		ClosedUserGroup cug = getCUG(ctx, subService.getSecondaryIdentifier()); 
    		
    		if ( cug == null)
    		{
       			//AuxServiceChargingSupport.handleSingleAuxServiceTransaction(ctx,ret, this, null, null, null);  
    			new DebugLogMsg(this, "Won't create transaction, CUG was removed ", null).log(ctx); 
      			return; 
    		}
    		
    		Subscriber owner = null;
    		if(cug.getOwnerMSISDN() != null && cug.getOwnerMSISDN().length() > 0)
    		{
    			 owner = SubscriberSupport.lookupActiveSubscriberForMSISDN(ctx, cug.getOwnerMSISDN());
    		}    		
    		
    		if (owner == null)
    		{
    			ChargeRefundResultHandlerSupport.logErrorMsg(ctx, "fail to find owner subscriber of private cug " + cug.getID() + 
    					"when trying to charge for subscriber " + 
    					sub.getId(), TRANSACTION_FAIL_DATA_ERROR, null); 
    			return; 
    		} 
    		
    		TransactionCustomize  customizer = null; 
    		
    		if (owner.getId().equals(sub.getId()))
    		{	
    		    for (Iterator iterator = cug.getSubscribers().keySet().iterator(); iterator.hasNext(); )
    		    {
    		        String msisdn = (String) iterator.next();
    		        Subscriber cugSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
    		        if (cugSub!=null)
    		        {
                        SubscriberAuxiliaryService association = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx,cugSub.getId(), cug.getAuxiliaryService(ctx).getIdentifier(), cug.getID());
                        cug.getSubAuxServices().put(msisdn, association);
    		        }
    		    }
    		    
    		    cug.getSubAuxServices().put(sub.getMSISDN(), subService);
    		    
    		    customizer = new PrivateCUGOwnerTransactionCustomize(ctx, service, cug.getSubscribers().keySet(), cug);
                AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx,ret, new PCUGOwnerHandler(cug), null, customizer, null);                       
    	    } 

    	} 
    	catch (Throwable t)
    	{
    		ChargeRefundResultHandlerSupport.logErrorMsg(ctx, "fail to charge sub " + sub.getId() +
    				" for auxiliary service " + ret.getId(),
    				TRANSACTION_FAIL_DATA_ERROR, t); 
    	}
    }
    
    
    private static ClosedUserGroup getCUG(Context ctx, long cugId)
    {
    	try 
    	{
    		return ClosedUserGroupSupport.getCug(ctx, cugId); 

    	} catch (Exception e)
    	{
    		
    	}
    	return null; 
    }


	
	public void setDelegate(ChargeRefundResultHandler handler) {
	}
    

    
	   public static PrivateCUGSubscriberStateChargeReRefundHandler instance()
	    {
	        return INSTANCE;
	    }

	    private static final PrivateCUGSubscriberStateChargeReRefundHandler INSTANCE = new PrivateCUGSubscriberStateChargeReRefundHandler();
}   
