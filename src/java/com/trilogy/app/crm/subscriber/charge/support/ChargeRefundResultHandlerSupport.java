package com.trilogy.app.crm.subscriber.charge.support;

import java.util.Date;
import java.util.StringTokenizer;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandlerFactory;
import com.trilogy.app.crm.subscriber.charge.handler.HandlerConstants;
import com.trilogy.app.crm.subscriber.charge.handler.PrivateCUGSubscriberStateChargeReRefundHandler;
import com.trilogy.app.crm.subscriber.charge.handler.RedirectOneTimeChargeHandler;
import com.trilogy.app.crm.subscriber.charge.handler.VPNGroupChargeHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;

public abstract class ChargeRefundResultHandlerSupport 
{

    public static ChargeRefundResultHandler getHandler(Context ctx, ChargableItemResult source)
    {
        ChargeRefundResultHandlerFactory factory = (ChargeRefundResultHandlerFactory) ctx.get(ChargeRefundResultHandlerFactory.class); 
        return factory.create(ctx, source); 
    }

    public static ChargeRefundResultHandler getHandler(Context ctx, ChargeRefundResultHandler handler, ChargableItemResult source)
    {
        if(handler != null )
        {
            return handler;
        }
        
        switch ( source.getChargeResult())
        {		
        	case ChargingConstants.TRANSACTION_REDIRECT_ONE_TIME:
        		return RedirectOneTimeChargeHandler.instance(); 
        	case ChargingConstants.TRANSACTION_REDIRECT_VPN_GROUP:
        		return VPNGroupChargeHandler.instance(); 
        	case ChargingConstants.TRANSACTION_REDIRECT_PCUG:
        		return PrivateCUGSubscriberStateChargeReRefundHandler.instance(); 
        }
        
        return getHandler(ctx, source); 
        
    }
    
    
    protected static ChargeRefundResultHandler createHandlerPipeLine( String handlers)
    throws Exception
    {
        ChargeRefundResultHandler handler = null; 
        StringTokenizer tokenizer = new StringTokenizer(handlers, ",");
        while(tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken(); 
            int handlerType = Integer.parseInt(token); 
            ChargeRefundResultHandler delegate = createHandlerByType(handlerType); 
            if( handler != null )
            {
               delegate.setDelegate(handler); 
            }
              handler = delegate; 
                    
         }
         return handler; 
    }
    
    
    protected static ChargeRefundResultHandler createHandlerByType(int handlerType)
    throws Exception 
    {
        return createHandler( HandlerConstants.HANDLER_CLASSES[handlerType]); 
    }
    
    
    private static ChargeRefundResultHandler createHandler(Class handlerClass)
    throws Exception 
    {
        return (ChargeRefundResultHandler)handlerClass.newInstance();
    }
    
    
    public static void logDebugMsg(Context ctx,  ChargableItemResult ret)
    {
        new DebugLogMsg(ChargeRefundResultHandlerSupport.class, 
                "Successfully " + ChargingConstants.ACTION_TYPE_NAMES.get(new Integer(ret.getAction())) + 
                 " to subscriber " + 
                ret.getSubscriber().getId()+ 
                " for " + ChargingConstants.CHARGABLE_ITEM_NAMES[ret.getChargableItemType()] + " " + 
                ret.getId() +
                " error = "  + ((null == ret.getThrownObject())? "" : ret.getThrownObject().getMessage()) +
                " retcode = " + ChargingConstants.TRANSACTION_RETURN_NAMES.get( new Integer(ret.getChargeResult())), ret.thrownObject).log(ctx);         
    }
    
    
    public static void logErroMsg(Context ctx, String msg, ChargableItemResult ret)
    {
          logErrorMsg(ctx, msg, ret.getChargeResult(), ret.thrownObject);
    }
    
    public static void logErrorMsg(Context ctx, String msg, int result, Throwable t)
    {
           ExceptionListener el= (ExceptionListener)ctx.get(ExceptionListener.class); 
          
          switch (result)
          {
                case ChargingConstants.TRANSACTION_FAIL_UNKNOWN: 
                case ChargingConstants.TRANSACTION_FAIL_OCG:
                case ChargingConstants.TRANSACTION_FAIL_DATA_ERROR:     
                	new MinorLogMsg(ChargeRefundResultHandler.class, msg, t).log(ctx); 
                case ChargingConstants.TRANSACTION_SUCCESS: 
                case ChargingConstants.TRANSACTION_VALIDATION_SUCCESS:
                case ChargingConstants.TRANSACTION_SKIPPED_SUSPEND:
             
                	if ( el != null )
                	{
                		el.thrown(new Exception(msg, t));
                	}  
                	break; 
                case ChargingConstants.TRANSACTION_SKIPPED_UNSUPPORTED_TYPE: 
                case ChargingConstants.TRANSACTION_SKIPPED_DUPLICATE_CHARGE:
                case ChargingConstants.TRANSACTION_SKIPPED_DUPLICATE_REFUND:
                case ChargingConstants.TRANSACTION_SKIPPED_NO_CHARGE_IN_BILLING_CYCLE:
                case ChargingConstants.TRANSACTION_REDIRECT_ONE_TIME:
                case ChargingConstants.TRANSACTION_SKIPPED_IN_PACKAGE:
                case ChargingConstants.TRANSACTION_SKIPPED_OVERUSAGE:
                case ChargingConstants.TRANSACTION_REDIRECT_PCUG:
                default:	
                	new DebugLogMsg(ChargeRefundResultHandler.class, msg, t).log(ctx);     	   
           }
          
          
        }

    
    static public  void createSubscriptionHistory(Context ctx, ChargableItemResult ret)
    {
    	long fullCharge = ret.trans.getFullCharge(); 
    	if (ret.getAction() >= ChargingConstants.ACTION_PROVISIONING_REFUND )
    	{
    		fullCharge = fullCharge * -1; 
    	}
    	Object item = ret.getChargableObject();
    	if (item instanceof AuxiliaryService)
    	{
    	    item = ret.getChargableObjectRef();
    	}
    	Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
      	SubscriberSubscriptionHistorySupport.addChargingHistory(
    			ctx, item, ret.subscriber, getHistoryEventType(ret), 
                getChargedItemType(ret), ret.trans.getAmount(), fullCharge , 
                ret.trans, runningDate);
    }
    
   
    static public HistoryEventTypeEnum getHistoryEventType(ChargableItemResult ret)
    {
    	if ( ret.getAction() < ChargingConstants.ACTION_PROVISIONING_REFUND)
    	{
    		return HistoryEventTypeEnum.CHARGE;
    	} else 
    	{
    		return HistoryEventTypeEnum.REFUND; 
    	}
    	
    }
    
    /**
     * Should be replaced when common chargeable interface is created and implemented by 
     * all types. 
     * @param ret
     * @return
     */
   static public ChargedItemTypeEnum getChargedItemType( ChargableItemResult ret)
    {
    	switch (ret.chargableItemType)
    	{
    	case ChargingConstants.CHARGABLE_ITEM_SERVICE: 
    		return ChargedItemTypeEnum.SERVICE; 
    	case ChargingConstants.CHARGABLE_ITEM_BUNDLE:
            BundleFee fee  = (BundleFee) ret.getChargableObject();
            if (fee.isAuxiliarySource())
            {
                return ChargedItemTypeEnum.AUXBUNDLE;
            }
            else
            {
                return ChargedItemTypeEnum.BUNDLE;
            }
    	case ChargingConstants.CHARGABLE_ITEM_AUX_SERVICE:
    		return ChargedItemTypeEnum.AUXSERVICE; 
    	case ChargingConstants.CHARGABLE_ITEM_PACKAGE:
    		return ChargedItemTypeEnum.SERVICEPACKAGE; 
    	}
    	
    	return null; 
    }
    
    
}
