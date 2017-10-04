package com.trilogy.app.crm.subscriber.charge.customize;


import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class PrivateCUGOwnerTransactionCustomize implements TransactionCustomize
{

	public PrivateCUGOwnerTransactionCustomize(Context ctx, AuxiliaryService service, Collection<String> msisdns, ClosedUserGroup cug)
	{
		this.auxiliaryService_ = service;  
        this.postpaidCount_ = 0;
        this.prepaidCount_ = 0;
        this.externalCount_ = 0;
        
        for(Iterator<String> i = msisdns.iterator(); i.hasNext(); )
        {
            String number = i.next(); 
            
            try
            {
                Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, number); 
                if (msisdn == null || msisdn.getSpid() != cug.getSpid())
                {
                    externalCount_++; 
                } 
                else if (msisdn.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
                {
                    postpaidCount_++; 
                } 
                else 
                {
                    prepaidCount_++; 
                }
            } catch (Exception e)
            {
                new MinorLogMsg(this, "Fail to find MSISDN " + number, e).log(ctx); 
            }
        }
	}

	public Transaction customize(Context ctx, Transaction trans)
	{		
        double ratio = trans.getRatio();
        
        long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
        long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
        long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
        
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                auxiliaryService_, CallingGroupAuxSvcExtension.class);
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
                            + "' for auxiliary service " + auxiliaryService_.getIdentifier());
        }
        
        // Use the absolute ratio before rounding, to avoid -27.5 to be rounded to -27 instead of -28
        long postpaidAmount = postpaidCount_ * Math.round(Math.abs(serviceChargePostpaid * ratio));
        long prepaidAmount = prepaidCount_ * Math.round(Math.abs(serviceChargePrepaid * ratio));
        long externalAmount = externalCount_ * Math.round(Math.abs(serviceChargeExternal * ratio));
        long chargedAmount = postpaidAmount + prepaidAmount + externalAmount;

        // If negative ratio, multiply charged amount by -1.
        if (Math.abs(ratio)!=ratio)
        {
            chargedAmount = -1 * chargedAmount;
        }
        
        long fullCharge = serviceChargePostpaid * postpaidCount_
                + serviceChargePrepaid * prepaidCount_
                + serviceChargeExternal * externalCount_;
        
        trans.setAmount(chargedAmount);
		trans.setFullCharge(Math.abs(fullCharge)); 
		return trans; 
	}

	public void setDelegate(TransactionCustomize delegate)
    {
    	this.delegate_ = delegate; 
    }


	private int postpaidCount_;
	private int prepaidCount_;
	private int externalCount_;
	private AuxiliaryService auxiliaryService_;
	private TransactionCustomize delegate_; 

}
