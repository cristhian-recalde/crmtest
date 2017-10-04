package com.trilogy.app.crm.subscriber.charge.customize;

import java.util.Date;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.SubscriberChargingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class PrivateCUGMemberTransactionCustomize 
implements TransactionCustomize
{

	public PrivateCUGMemberTransactionCustomize(Context ctx, Subscriber sub, AuxiliaryService service, Subscriber owner, SubscriberAuxiliaryService subAuxService, int action, int chargingCycleType, ClosedUserGroup cug)
	{
        long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
        long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
        long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
        
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                service, CallingGroupAuxSvcExtension.class);
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
                            + "' for auxiliary service " + service.getIdentifier());
        }
        
	    this.cug_ = cug;
		this.supportedSubscriberId = sub.getId(); 
		this.defaultCharge = service.getCharge(); 
		if (sub.getSpid() == cug_.getSpid())
		{
    		if (sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
    		{
    		    this.realCharge = serviceChargePostpaid; 
    		} else 
    		{
    		    this.realCharge = serviceChargePrepaid; 
    		}
		}
		else
		{
		    this.realCharge = serviceChargeExternal; 
		}
		this.action_ = action;
		this.chargingCycleType_ = chargingCycleType;
		this.subAuxService_ = subAuxService;
		
		this.owner = owner; 
	}

	public PrivateCUGMemberTransactionCustomize(Context ctx, String msisdn, AuxiliaryService service, Subscriber owner, SubscriberAuxiliaryService subAuxService, int action, int chargingCycleType, ClosedUserGroup cug)
	{
        long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
        
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            serviceChargeExternal = callingGroupAuxSvcExtension.getServiceChargeExternal();
        }
        
        this.cug_ = cug;
		this.supportedSubscriberId = msisdn;
		this.defaultCharge = service.getCharge(); 
		this.realCharge = serviceChargeExternal; 
		this.owner = owner; 
        this.action_ = action;
        this.chargingCycleType_ = chargingCycleType;
        this.subAuxService_ = subAuxService;
	}
	
	public void setDelegate(TransactionCustomize delegate)
    {
    	
    }


	public Transaction customize(Context ctx, Transaction trans)
	{
        ServicePeriodHandler handler = SubscriberChargingSupport.getServicePeriodHandler(ctx, chargingCycleType_);
        
        try
        {
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            
            final double ratio;
            if (SubscriberChargingSupport.isRefund(action_))
            {
                ratio = handler.calculateRefundRate(ctx, runningDate, 
                    SubscriberSupport.getBillCycleDay(ctx, owner), 
                    owner.getSpid(), 
                    owner.getId(), subAuxService_);
            }
            else
            {
                ratio = handler.calculateRate(ctx, runningDate, 
                        SubscriberSupport.getBillCycleDay(ctx, owner), 
                        owner.getSpid(), 
                        owner.getId(), subAuxService_);
            }
            trans.setRatio(ratio);
            
            trans.setServiceEndDate(handler.calculateCycleEndDate(ctx, runningDate, 
                    SubscriberSupport.getBillCycleDay(ctx, owner), 
                    owner.getSpid(), 
                    owner.getId(), subAuxService_));  
            
            if ( chargingCycleType_ == ChargingConstants.CHARGING_CYCLE_ONETIME ) 
            {
                    // changed for Dory, but supposed to be accepted by all customers. 
                   trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
                } else
                {
                    trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
                }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to calculate ratio based on PCUG owner: " + e.getMessage(), e);
        }

        trans.setAmount( Math.round(realCharge * trans.getRatio())); 
        trans.setFullCharge(realCharge); 
		trans.setSupportedSubscriberID(supportedSubscriberId);

		trans.setBAN(owner.getBAN());
	    trans.setSpid(owner.getSpid());
	    trans.setMSISDN(owner.getMSISDN());
	    trans.setSubscriberID(owner.getId());

		return trans; 
	}


	String supportedSubscriberId; 
	long defaultCharge; 
	long realCharge;
	int chargingCycleType_;
	int action_;
	ClosedUserGroup cug_;
	SubscriberAuxiliaryService subAuxService_;
	Subscriber owner; 
}
