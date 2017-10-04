/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.math.BigDecimal;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.config.CRMConfigInfoForVRA;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.app.crm.config.CRMConfigInfoForVRAHome;
import com.trilogy.framework.xhome.home.HomeInternalException;



/**
 * Provides support methods for dealing with VRA Transactions
 * 
 * @author vcheng
 * @author Asim Mahmood
 * @since 8.6
 */
public class VRASupport
{

    public static final int VRA_ER_EVENTTYPE_ZERO = 0;
    public static final int VRA_ER_EVENTTYPE_NORMAL = 1;
    public static final int VRA_ER_EVENTTYPE_OVERDRAFT = 2;

    public static final String VRAAGENT_CREDITVALUE = "CreditValue";
    
    /**
      * Return VRA config information based on spid <F-0001058 - Multi-tenancy Enhancements>
      * @since 9.6
      * @param ctx
      * @param spid
      * @return
      * @throws HomeException
      */
    public static CRMConfigInfoForVRA getCRMConfigInfoForVRA(Context ctx, int spid) throws HomeException
    {
    	Home home = (Home) ctx.get(CRMConfigInfoForVRAHome.class);
    	if(home==null){
    		throw new HomeException("CRMConfigInfoForVRA not present in home");
    	}

    	CRMConfigInfoForVRA config =(CRMConfigInfoForVRA)home.find(ctx,spid);
    	
    	// Added to provide the default VRA configuration if it is not defined to provide the fix for TT#13030643027
    	if(config == null){
    		config = new CRMConfigInfoForVRA();
    		config.setPostpaidAdjustmentType(VRA_ER_EVENTTYPE_NORMAL);
    		config.setPostpaidAdjustmentTypeCategory(VRA_ER_EVENTTYPE_NORMAL);
    		config.setPrepaidAdjustmentType(VRA_ER_EVENTTYPE_NORMAL);
    		config.setPrepaidAdjustmentTypeCategory(VRA_ER_EVENTTYPE_NORMAL);
    		config.setSpid(spid);
    		// Adding the log message for default VRA Configuration
    		if (LogSupport.isDebugEnabled(ctx)){
    			LogSupport.debug(ctx, VRASupport.class, "VRA configuration not defined and setting it to default configs.");
    		}
    	}
    	
    	return config;
    }
    /**
     * Determines if given adjustmentType is one of CRMConfigInfoForVRA configured adjustmentTypes.
     * @param ctx
     * @param adjType
     * @return true when
     * @throws HomeException 
     * @throws HomeInternalException 
     */
    public static boolean isVRAEvent(final Context ctx, final int adjType, int spid) throws HomeInternalException, HomeException
    {
        final CRMConfigInfoForVRA config = getCRMConfigInfoForVRA(ctx,spid);
        return isVRANormalTopUp(ctx, config, adjType) || isVRAEmergencyTopUp(ctx, config, adjType);
    }
    
    public static int getVRAEventTypeFromAdjustmentType(final Context ctx, final int adjType, final int spid) throws HomeInternalException, HomeException
    {
        final CRMConfigInfoForVRA config = getCRMConfigInfoForVRA(ctx,spid);
        if (isVRANormalTopUp(ctx, config, adjType))
        {
            return VRA_ER_EVENTTYPE_NORMAL;
        }
        if (isVRAEmergencyTopUp(ctx, config, adjType))
        {
            return VRA_ER_EVENTTYPE_OVERDRAFT;
        }
        return VRA_ER_EVENTTYPE_ZERO;
    }



    /**
     * Applies VRA logic on transaction, based on the VRA eventType (Normal or Overdraft). This method does not apply
     * the clawback.
     *  Transaction.adjustmentType = VRA Config (eventType)
     *  Transaction.ammount =  voucherValue * (1 + tax)
     * @param ctx
     * @param transaction
     * @param subscriber
     * @param eventType
     * @throws HomeException
     */
    public static void processVRAEventType(final Context ctx, final com.redknee.app.crm.bean.Transaction transaction, 
            final Subscriber subscriber, final int eventType, final long creditValue, final long voucherValue) 
        throws IllegalArgumentException, HomeException
    {
        final CRMConfigInfoForVRA config = getCRMConfigInfoForVRA(ctx,subscriber.getSpid());
        // apply tax
        final long voucherTax = calculateVoucherTax(config, subscriber, voucherValue);
        transaction.setAmount(creditValue + voucherTax);
        
        // apply adjustment type based on eventType
        int adjustmentType = retrieveNormalVoucherAdjustmentType(ctx, config, subscriber);
        if (eventType != -1)
        {
            if (eventType == VRA_ER_EVENTTYPE_OVERDRAFT)
            {
                // HLD OID 37067
                // Use adjustment type from "Overdraft Adjustment Type"
                adjustmentType = config.getOverdraftAdjustmentType();
            }
            else if (eventType == VRA_ER_EVENTTYPE_NORMAL)
            {
                // default value
            }
            else
            {
                throw new IllegalArgumentException("VRA transaction event type " + eventType
                        + ". Failed to process VRA ER properly.");                
            }
        }
        AdjustmentType vraAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx,
                adjustmentType);
        if (vraAdjustmentType == null)
        {
            new InfoLogMsg(VRASupport.class, "Can't find correct AdjustmentType " + adjustmentType
                    + " for VRA transaction. Failed to process VRA ER properly.", null).log(ctx);
            return;
        }
        transaction.setAdjustmentType(vraAdjustmentType.getCode());
        
        ctx.put(VRAAGENT_CREDITVALUE, creditValue);
        
        sendVRANotification(ctx, transaction, subscriber);
    }


    /**
     * Creates a transaction, where adjustment type is mapped from from {@link eventType}
     * and applies any taxes on voucher.
     * 
     * @param ctx
     * @param transDate
     *            Transaction date
     * @param voucherValue
     *            VRA Voucher value
     * @param newBalance
     * @param creditValue
     * @param extensionDays
     * @param subscriber
     * @param eventType
     *            VRA event type (Normal, Overdraft, Clawback, etc.)
     * @param csrInput
     * @param externalID 
     * @param adjType 
     * @throws HomeException 
     */
    public static void createVRATransaction(final Context ctx, final Date transDate, long voucherValue,
            long newBalance, long creditValue, int extensionDays, Subscriber subscriber, String csrInput, int eventType, String externalID)
        throws HomeException
    {
        final Transaction transaction = createTransactionObject(ctx, transDate, voucherValue, newBalance,
                extensionDays, subscriber, csrInput, new AdjustmentType(), externalID);
        processVRAEventType(ctx, transaction, subscriber, eventType, voucherValue, creditValue);
        TransactionSupport.createTransactionRecord(ctx, subscriber, transaction);
    }


    /**
     * Create a second transaction using the adjustment type from
     * "Cleared Overdraft Adjustment Type" using the ER1050.clearedOverdraftAmount value
     * as the transaction amount. HLD OID 37067
     * 
     * @param ctx
     * @param transDate
     *            Transaction date
     * @param clearedOverdraftAmount
     *            Transaction ammount
     * @param newBalance
     *            Transaction date
     * @param creditValue
     * @param extensionDays
     * @param subscriber
     * @param csrInput
     * @throws HomeException
     */
    public static void createClawbackTransaction(Context ctx, final Date transDate, final long clearedOverdraftAmount,
            final long newBalance, final long creditValue, int extensionDays, Subscriber subscriber, String csrInput)
            throws HomeException
    {
        //TODO 2010/11/17: What is creditValue used for?
        if (clearedOverdraftAmount > 0)
        {
            // Get Clawback adjustment type
            CRMConfigInfoForVRA config = getCRMConfigInfoForVRA(ctx,subscriber.getSpid());
            int adjustmentType = config.getClearedOverdraftAmountAdjustmentType();
            AdjustmentType vraClearedODAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(
                    ctx, adjustmentType);
            // Create transaction
            final Transaction transaction = createTransactionObject(ctx, transDate, clearedOverdraftAmount, newBalance,
                    extensionDays, subscriber, csrInput, vraClearedODAdjustmentType, "");
            TransactionSupport.createTransactionRecord(ctx, subscriber, transaction);
            
            ctx.put(VRAAGENT_CREDITVALUE, creditValue);
            sendVRANotification(ctx, transaction, subscriber);
        }
    }


    
    
    /**
     * Verifies whether polled ER represents a VRA normal top up.
     * @param config 
     * 
     * @param config
     * @param adjType
     * @return
     */
    private static boolean isVRANormalTopUp(final Context ctx, CRMConfigInfoForVRA config, final int adjType)
    {
        
        return (adjType == config.getPrepaidAdjustmentType() || adjType == config.getPostpaidAdjustmentType());
    }
    
    /**
     * Verifies whether polled ER represents a VRA ETU top up.
     * @param config 
     * 
     * @param config
     * @param adjType
     * @return
     */
    private static boolean isVRAEmergencyTopUp(final Context ctx, CRMConfigInfoForVRA config, final int adjType)
    {
        return (adjType == config.getOverdraftAdjustmentType());
    }
    
    private static Transaction createTransactionObject(Context ctx, final Date transDate, final long amount,
            final long newBalance, int extensionDays, Subscriber subscriber, String csrInput,
            AdjustmentType adjustmentType, String externalID) throws HomeException
    {
        final boolean prorate = false;
        final boolean limitException = true;
        
        final Transaction transaction = TransactionSupport.createSubscriberTransactionObject(ctx, subscriber, amount,
                newBalance, adjustmentType, prorate, limitException, 
                CoreTransactionSupportHelper.get(ctx).getCsrIdentifier(ctx), transDate, new Date(), csrInput, extensionDays);
        transaction.setFromVRAPoller(true);
        transaction.setExtTransactionId(externalID);
        return transaction;
    }


    private static void sendVRANotification(final Context ctx, final com.redknee.app.crm.bean.Transaction transaction,
            final Subscriber subscriber)
    {
        // the reason to set RemainingBalance here instead of using AppOcgClient to get
        // the same value
        // from OCG in SmsSupport class is that somehow in SmsSupport class, OCG always
        // return 0
        // because of some params format error which I don't have enough time to dig
        // deeply into.
        subscriber.setBalanceRemaining(transaction.getBalance());
        // Remove it from the queue if the subscriber is set for queueSubPreExpiryMsg - TT
        // #5062720238
        try
        {
            SubscriptionNotificationSupport.removePendingNotifications(ctx, subscriber.getId(),
                    NotificationTypeEnum.PRE_EXPIRY);
            
            Date startDate = subscriber.getDateCreated();
            int thresholdInHrs = ctx.getInt(ACTIVATION_TIME_SMS_SUPPRESSION_THRESHOLD, 24);
            Date stareDateAfterThresholdTime = CalendarSupportHelper.get(ctx).getHoursAfter(startDate, thresholdInHrs);
            Date currentDate = new Date();
            
            //HACK: To disable sms notification on subscription activitation
            if (subscriber.getState() != SubscriberStateEnum.AVAILABLE
                    && currentDate.after(stareDateAfterThresholdTime))
            {
            	// TT#12090730043: Priceplan Renewal message on Every Voucher Recharge
                SubscriptionNotificationSupport.sendVoucherTopupNotification(ctx, subscriber, transaction);  // instead use VRA notification, send a param saying VRA type of notific
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(VRASupport.class,
                    "An error occured during VRA transaction processing, SMS notification failed.", e).log(ctx);
        }

        
    }


    private static long calculateVoucherTax(final CRMConfigInfoForVRA config, final Subscriber subscriber, long voucherValue)
    {
    	long voucherTax = 0L;
        if ((config.getApplyVoucherTax() == true) && (subscriber.isPostpaid()))
        {
            float percentage = (BigDecimal.valueOf(config.getVoucherTax()).divide(BigDecimal.valueOf(100), 3, BigDecimal.ROUND_FLOOR)).floatValue();
            
            boolean isCreditVoucher = false;
     	 		            
            if(voucherValue < 0)
            {
                isCreditVoucher = true;
            }
            
            voucherTax = (long) (Math.round(Math.abs(voucherValue) * percentage));
            
            if(isCreditVoucher)
            {
            	voucherTax = -voucherTax;
            }
        }
        return voucherTax;
    }


    private static int retrieveNormalVoucherAdjustmentType(final Context ctx, final CRMConfigInfoForVRA config,
            final Subscriber subscriber)
    {
        int adjustmentType;
        // Get "Normal Voucher Adjustment Type"
        if (subscriber.isPrepaid())
        {
            adjustmentType = config.getPrepaidAdjustmentType();
        }
        else
        {
            adjustmentType = config.getPostpaidAdjustmentType();
        }
        return adjustmentType;
    }
    
    public static final String ACTIVATION_TIME_SMS_SUPPRESSION_THRESHOLD = "ACTIVATION_TIME_SMS_SUPPRESSION_THRESHOLD";
}
