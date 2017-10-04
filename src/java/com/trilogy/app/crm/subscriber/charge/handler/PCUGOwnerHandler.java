package com.trilogy.app.crm.subscriber.charge.handler;

import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.provision.corba.SubscriberTypeEnum;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

public class PCUGOwnerHandler extends GenericHandler
{
    
    public PCUGOwnerHandler(ClosedUserGroup cug)
    {
    	this.cug = cug; 
    }

    @Override
    public void handleError(Context ctx,  ChargableItemResult ret)
    {
    	if(ret.getTrans() != null)
    	{
    		if(ret.getTrans().getSupportedSubscriberID()!= null)
    		{
    			if(!(ret.getTrans().getSubscriberID().equals(ret.getTrans().getSupportedSubscriberID())))
    			{
    				logError(ctx, ret);
    			}
    		}
    	}	
    }
    
    
    @Override
    public void handleSuccess(Context ctx,  ChargableItemResult ret)
    {
    	Collection <SubscriberAuxiliaryService > subServices = cug.getSubAuxServices().values(); 
    	long ownerFee = ret.getTrans().getFullCharge();
    	long ownerAmount = ret.getTrans().getAmount();
    	SubscriberAuxiliaryService ownerService = null;
    	
    	for (SubscriberAuxiliaryService subService : subServices)
    	{
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            
    		try 
    		{
    	        AuxiliaryService auxService = cug.getAuxiliaryService(ctx); 
    			Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subService.getSubscriberIdentifier()); 
    			
                if (!sub.getMSISDN().equals(cug.getOwnerMSISDN()))
                {
                    long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
                    long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
                    long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
                    
                    CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                            auxService, CallingGroupAuxSvcExtension.class);
                    if (callingGroupAuxSvcExtension!=null)
                    {
                        serviceChargeExternal = callingGroupAuxSvcExtension.getServiceChargeExternal();
                        serviceChargePostpaid = callingGroupAuxSvcExtension.getServiceChargePostpaid();
                        serviceChargePrepaid = callingGroupAuxSvcExtension.getServiceChargePrepaid();
                    }
                    else
                    {
                        LogSupport.minor(ctx, this,
                                "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                        + "' for auxiliary service " + auxService.getIdentifier());
                    }
                    
                    long memberFee;
                    if (cug.getSpid()!=sub.getSpid())
                    {
                        memberFee = serviceChargeExternal;
                    }
                    else if (sub.isPostpaid())
                    {
                        memberFee = serviceChargePostpaid;
                    }
                    else
                    {
                        memberFee = serviceChargePrepaid;
                    }
                    long memberAmount = Math.round(memberFee * ret.getTrans().getRatio());
                    SubscriberSubscriptionHistorySupport.addChargingHistory(
    					ctx, subService, sub, 
    					ChargeRefundResultHandlerSupport.getHistoryEventType(ret), 
    					ChargeRefundResultHandlerSupport.getChargedItemType(ret), 
    					memberAmount,
    					memberFee, 
    					ret.trans, runningDate);
                    ownerFee -= memberFee;
                    ownerAmount -= memberAmount;
                }
                else
                {
                    ownerService = subService;
                }
    		} 
    		catch (HomeException e)
    		{
    			new MinorLogMsg(this, "failed to create subscriber subscription history for " + subService.getSubscriberIdentifier()
    					+ " " + subService.getAuxiliaryServiceIdentifier() + " " + ret.getAction(), e).log(ctx); 
    		}
    	}
    	
        if (ownerService != null)
        {
            try
            {
                SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, ownerService, cug.getOwner(ctx),
                        ChargeRefundResultHandlerSupport.getHistoryEventType(ret), ChargeRefundResultHandlerSupport
                                .getChargedItemType(ret), ownerAmount, ownerFee, ret.trans, ret.trans.getTransDate());
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "failed to create subscriber subscription history for "
                        + ownerService.getSubscriberIdentifier() + " " + ownerService.getAuxiliaryServiceIdentifier()
                        + " " + ret.getAction(), e).log(ctx);
            }
        }
    }
    
    
    public void logError(Context ctx,  ChargableItemResult ret)
    {
        String msg = "Fail to " + ACTION_TYPE_NAMES.get(new Integer(ret.getAction())) + 
        " to subscriber " + 
        cug.getOwnerMSISDN()+ 
        " for " + CHARGABLE_ITEM_NAMES[ret.getChargableItemType()] + " " + 
        ret.getId() +
        " error = "  + ((null == ret.getThrownObject())? "" : ret.getThrownObject().getMessage()) +
        " retcode = " + TRANSACTION_RETURN_NAMES.get( new Integer(ret.getChargeResult())); 
        
        ChargeRefundResultHandlerSupport.logErroMsg(ctx, msg, ret);
    }
    
    private long getFullCharge(Context ctx, Subscriber sub, AuxiliaryService service)
    {
        long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
        long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
        
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            serviceChargePostpaid = callingGroupAuxSvcExtension.getServiceChargePostpaid();
            serviceChargePrepaid = callingGroupAuxSvcExtension.getServiceChargePrepaid();
        }
        else
        {
            LogSupport.minor(ctx, this,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        
   	if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
    	{
    		return serviceChargePostpaid; 
    	} else
    	{
    		return serviceChargePrepaid; 
    	}
    }
    
    ClosedUserGroup cug; 

}
    