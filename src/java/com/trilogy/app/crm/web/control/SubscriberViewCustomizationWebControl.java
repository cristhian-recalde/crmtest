/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.PermissionSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.WebControlSupportHelper;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;

/**
 * Performes Subscriber View customizaton.
 *
 * TODO Replace this file using field level predicates on the Subscriber XMenus.
 *
 * @author Aaron Gourley
 * @since 7.5
 */
public class SubscriberViewCustomizationWebControl extends ProxyWebControl
{
	public SubscriberViewCustomizationWebControl(WebControl delegate)
    {
        super(delegate);
    }
	
	/*
	 * (non-Javadoc)
	 * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
	 */
	@Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
	    Context subCtx = ctx.createSubContext();

	    if( obj instanceof Subscriber )
	    {
	        com.redknee.framework.xhome.msp.MSP.setBeanSpid(subCtx,((AbstractSubscriber) obj).getSpid());
	        configureSubscriberView(subCtx, (Subscriber)obj);
	    }
		super.toWeb(subCtx, out, name, obj);
	}

    
	protected void configureSubscriberView(Context ctx, Subscriber subscriber)
    {
	    // Putting subscribers time zone in the context.
        ctx.put(TimeZone.class, TimeZone.getTimeZone(subscriber.getTimeZone(ctx)));

        if (!needsMergeView(ctx))
        {
            hideMergeViewProperties(ctx);
        }
        
        if (SubscriberTypeEnum.POSTPAID.equals(subscriber.getSubscriberType()))
        {
            hidePrepaidProperties(ctx);
            if(!allowCreditLimitBarring(ctx))
            {
                WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, SubscriberXInfo.ABOVE_CREDIT_LIMIT);
            }
        }
        else if (SubscriberTypeEnum.PREPAID.equals(subscriber.getSubscriberType()))
        {
            hidePostpaidProperties(ctx);
            if (SystemSupport.supportsUnExpirablePrepaidSubscription(ctx))
            {
                hideExpiryDataProperties(ctx);
                
	            // hybrid prepaid needs to get balance from ocg when creating
                //WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, SubscriberXInfo.INITIAL_BALANCE);
            }
        }

        if (subscriber.getId().length() > 0)
        {
            // apply only to already created subscriber, which already have an ID
            syncScheduledStartEndDateWithState(ctx, subscriber);
            readOnlyContract(ctx,subscriber);
        }

        try
        {
            final Account subscriberAccount = subscriber.getAccount(ctx);
            
            final SubscriptionType subType = SubscriptionClass.getSubscriptionTypeForClass(
                    SubscriptionClass.filterHomeOnBillingType(ctx, subscriberAccount.getSystemType()), 
                    subscriber.getSubscriptionClass(), 
                    AbstractSubscriber.DEFAULT_SUBSCRIPTIONCLASS);
            
            String groupMSISDN = "";
			if (subscriberAccount != null
			    && subscriberAccount.isIndividual(ctx)
			    && !subscriberAccount.isResponsible())
            {
                groupMSISDN = subscriberAccount.getGroupMSISDN(ctx, subType.getId());
            }
            boolean isPooledMember = false;
            if (groupMSISDN != null && groupMSISDN.length() > 0)
            {
                isPooledMember = true;
            }
            
            if (subType != null)
            {
                hidePropertiesForSubscriptionType(ctx, subscriber,  subType);
            }
            
            if (!isPooledMember)
            {
                hideQuotaProperties(ctx);
            }
            else 
            {
                // a prepaid member under a post-paid pool is not allowed to use
                // Pool's
                // balance
                // since it still is a member, it can use pool bundle buckets.
                hideNonPoolProperties(ctx);
                if (subscriber.isPrepaid())
                {
                    Account pooledAccount = AccountSupport.getAccount(ctx, subscriber.getPoolID(ctx));
                    if (pooledAccount != null && pooledAccount.isHybrid())
                    {
                        hideQuotaProperties(ctx);
                    }
                }
            }
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this, HomeException.class.getSimpleName() + " occurred in SubscriberViewCustomizationWebControl.configureSubscriberView(): " + e.getMessage(), e).log(ctx);
        }

        //customizeStartEndStates(ctx, subscriber);
    }

	protected void hideMergeViewProperties(Context ctx)
    {
        final PropertyInfo mergeViewProps[] = new PropertyInfo[]
        {
            //SubscriberXInfo.BILLING_LANGUAGE, /* TT5060619176 get rid of billing language show up on crmadmin */
        	SubscriberXInfo.CURRENCY,
        	SubscriberXInfo.SPID,
        	SubscriberXInfo.BAN,
        	//SubscriberXInfo.LANGUAGE
        	//SubscriberXInfo.DEALER_CODE
        };

        WebControlSupportHelper.get(ctx).hideProperties(ctx, mergeViewProps);
    }

	public final static SimplePermission MERGEVIEW_PERMISSION = new SimplePermission("app.crm.mergeview");
    public final static SimplePermission READ_CHARGING_ID = new SimplePermission("special.app.crm.subsriber.charging-id.read");
    public final static SimplePermission EDIT_CHARGING_ID = new SimplePermission("special.app.crm.subsriber.charging-id.read");
    public final static SimplePermission CREDIT_LMIT_BARRED_PERMISSION = new SimplePermission("special.app.crm.subsriber.abovecreditlimit.write");
    boolean needsMergeView(Context ctx)
	{
        return FrameworkSupportHelper.get(ctx).hasPermission(ctx, MERGEVIEW_PERMISSION);
	}
    
    boolean allowCreditLimitBarring(Context ctx)
    {
        return FrameworkSupportHelper.get(ctx).hasPermission(ctx, MERGEVIEW_PERMISSION);
    }

	protected Context syncScheduledStartEndDateWithState(final Context ctx, final Subscriber sub)
	{
		if (sub.isInFinalState())
		{
			WebControlSupportHelper.get(ctx).setPropertiesReadOnly(ctx, new PropertyInfo[] {
			        SubscriberXInfo.START_DATE,
			        SubscriberXInfo.END_DATE});
		}
        else if (SubscriberStateEnum.PENDING.equals(sub.getState())
                || SubscriberStateEnum.AVAILABLE.equals(sub.getState()))
		{
			//Both fields editable
		}
		else
		{
			WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, SubscriberXInfo.START_DATE);
		}
		return ctx;
	}
	
	private void readOnlyContract(final Context ctx, final Subscriber sub)
	{
	    if (sub.getState() == SubscriberStateEnum.SUSPENDED)
	    {
	           WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, SubscriberXInfo.SUBSCRIPTION_CONTRACT);
	    }
	}
	protected void hideAccountRelatedProperties(Context ctx)
	{
        // Hide properties that should not be visible from within the account view
        final PropertyInfo accountHiddenProps[] = new PropertyInfo[]
        {
            SubscriberXInfo.BAN,
            SubscriberXInfo.ID,
            SubscriberXInfo.SPID,
            SubscriberXInfo.DATE_CREATED,
            SubscriberXInfo.LAST_MODIFIED,
            SubscriberXInfo.SUBSCRIBER_TYPE,
            SubscriberXInfo.DISCOUNT_CLASS
        };

        WebControlSupportHelper.get(ctx).hideProperties(ctx, accountHiddenProps);
	}
	
	protected void hidePrepaidProperties(Context ctx)
    {
        //those properties are for prepaid user only
        final PropertyInfo prepaidOnlyProps[] = new PropertyInfo[]
        {
            SubscriberXInfo.BALANCE_REMAINING,
            SubscriberXInfo.OVERDRAFT_BALANCE,
            SubscriberXInfo.OVERDRAFT_DATE,
            SubscriberXInfo.SAT_ID,
            SubscriberXInfo.LAST_SAT_ID,
            SubscriberXInfo.CHARGE_PPSM,

            //service activation template
            SubscriberXInfo.INITIAL_BALANCE,
            SubscriberXInfo.EXP_DATE_EXT,
            SubscriberXInfo.MAX_BALANCE,
            SubscriberXInfo.MAX_RECHARGE,
            SubscriberXInfo.REACTIVATION_FEE,
            SubscriberXInfo.PRE_EXPIRY_SMS_SENT         
        };

        WebControlSupportHelper.get(ctx).hideProperties(ctx, prepaidOnlyProps);
    }
    
	protected void hideExpiryDataProperties(Context ctx)
    {
        //those properties are for prepaid user only
        //additional fields 
        final PropertyInfo prepaidOnlyProps[] = new PropertyInfo[]
        {
            SubscriberXInfo.SAT_ID,
            SubscriberXInfo.LAST_SAT_ID,
            SubscriberXInfo.INITIAL_BALANCE,
            SubscriberXInfo.EXP_DATE_EXT,
            SubscriberXInfo.PRE_EXPIRY_SMS_SENT,
            SubscriberXInfo.EXPIRY_DATE
        };

        WebControlSupportHelper.get(ctx).hideProperties(ctx, prepaidOnlyProps);
    }
    
	protected void hidePostpaidProperties(Context ctx)
    {        
        //those properties are for postpaid user only
        final PropertyInfo postPaidOnlyProps[] = new PropertyInfo[]
        {
            SubscriberXInfo.FAX_MSISDN_ENTRY_TYPE,
            SubscriberXInfo.FAX_MSISDN,
            SubscriberXInfo.DATA_MSISDN_ENTRY_TYPE,
            SubscriberXInfo.DATA_MSISDN,
            SubscriberXInfo.MONTH_TO_DATE_BALANCE,
            //"Subscriber.onDemandMonthToDateBalance",
            SubscriberXInfo.DEPOSIT,
            SubscriberXInfo.CREDIT_LIMIT,
            SubscriberXInfo.MONTHLY_SPEND_LIMIT,
            SubscriberXInfo.ABM_CREDIT_LIMIT,
            //SubscriberXInfo.ON_DEMAND_ABM_CREDIT_LIMIT,
            SubscriberXInfo.AMOUNT_OWING,
            //SubscriberXInfo.ABOVE_CREDIT_LIMIT,
            //"Subscriber.onDemandAmountOwing",
            SubscriberXInfo.REAL_TIME_BALANCE,
            //"Subscriber.onDemandAbmBalance",
            SubscriberXInfo.PAYMENT_SINCE_LAST_INVOICE,
            SubscriberXInfo.ADJUSTMENTS_SINCE_LAST_INVOICE,
            SubscriberXInfo.SUBSCRIPTION_CONTRACT,
            SubscriberXInfo.SUBSCRIPTION_CONTRACT_END_DATE,
            SubscriberXInfo.SUBSCRIPTION_CONTRACT_START_DATE,  
        };

        WebControlSupportHelper.get(ctx).hideProperties(ctx, postPaidOnlyProps);
    }


    protected void hidePropertiesForSubscriptionType(Context ctx, Subscriber subscriber, SubscriptionType subType)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        
        Set<PropertyInfo> hideProps = new HashSet<PropertyInfo>();
        Set<PropertyInfo> readOnlyProps = new HashSet<PropertyInfo>();

        if (subType.isOfType(
                SubscriptionTypeEnum.MOBILE_WALLET, 
                SubscriptionTypeEnum.NETWORK_WALLET))
        {
            hideProps.addAll(Arrays.asList(
                    SubscriberXInfo.FAX_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.FAX_MSISDN,
                    SubscriberXInfo.DATA_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.DATA_MSISDN,
                    SubscriberXInfo.IMSI,
                    SubscriberXInfo.PACKAGE_ID,
                    SubscriberXInfo.HLR_ID,
                    SubscriberXInfo.DEPOSIT,
                    SubscriberXInfo.NEXT_DEPOSIT_RELEASE_DATE,
                    SubscriberXInfo.CREDIT_LIMIT,
                    SubscriberXInfo.ABOVE_CREDIT_LIMIT,
                    SubscriberXInfo.DEPOSIT_DATE,
                    SubscriberXInfo.EXP_DATE_EXT,
                    //SubscriberXInfo.MAX_BALANCE,/* TODO: Confirm with SDA */
                    //SubscriberXInfo.MAX_RECHARGE,/* TODO: Confirm with SDA */
                    //SubscriberXInfo.REACTIVATION_FEE,/* TODO: Confirm with SDA */
                    //SubscriberXInfo.SUPPORT_MSISDN,/* TODO: Confirm with SDA */
                    //SubscriberXInfo.CHARGE_PPSM,/* TODO: Confirm with SDA */
                    //SubscriberXInfo.EXPIRY_DATE,/* TODO: Confirm with SDA */
                    //SubscriberXInfo.PRE_EXPIRY_SMS_SENT,/* TODO: Confirm with SDA */
                    SubscriberXInfo.VOICE_PRICE_PLAN,
                    SubscriberXInfo.SMSPRICE_PLAN,
                    SubscriberXInfo.DATA_PRICE_PLAN,
                    SubscriberXInfo.INTENT_TO_PROVISION_SERVICES,
                    SubscriberXInfo.BUNDLES,
                    SubscriberXInfo.AUXILIARY_SERVICES,/* TODO: Confirm with SDA */
                    SubscriberXInfo.POINTS_BUNDLES,
                    SubscriberXInfo.SUBSCRIBER_CATEGORY,/* TODO: Confirm with SDA */
                    SubscriberXInfo.MARKETING_CAMPAIGN_BEAN,/* TODO: Confirm with SDA */
                    SubscriberXInfo.PERSONAL_LIST_PLAN_ENTRIES));
        }

        if (subType.isOfType(SubscriptionTypeEnum.AIRTIME))
        {
            hideProps.addAll(Arrays.asList(
                    SubscriberXInfo.REACTIVATION_FEE));
        }

        if (subType.isOfType(SubscriptionTypeEnum.WIRE_LINE))
        {
            hideProps.addAll(Arrays.asList(
                    SubscriberXInfo.SMSPRICE_PLAN,
                    SubscriberXInfo.DATA_PRICE_PLAN,
                    SubscriberXInfo.FAX_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.FAX_MSISDN,
                    SubscriberXInfo.DATA_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.DATA_MSISDN,
                //    SubscriberXInfo.BUNDLES,
                //    SubscriberXInfo.POINTS_BUNDLES,
                    SubscriberXInfo.IMSI,
                    SubscriberXInfo.HLR_ID,
                    SubscriberXInfo.REACTIVATION_FEE));
        }

        if (subType.isOfType(SubscriptionTypeEnum.BROADBAND))
        {
            hideProps.addAll(Arrays.asList(
                    SubscriberXInfo.VRA_FRAUD_PROFILE,
                  // SubscriberXInfo.MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.FAX_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.FAX_MSISDN,
                    SubscriberXInfo.DATA_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.DATA_MSISDN,
                    SubscriberXInfo.IMSI,
                    SubscriberXInfo.PACKAGE_ID,
                    SubscriberXInfo.INITIAL_BALANCE,
                    SubscriberXInfo.EXP_DATE_EXT,
                    SubscriberXInfo.MAX_BALANCE,
                    SubscriberXInfo.REACTIVATION_FEE,
                    SubscriberXInfo.CHARGE_PPSM,
                    SubscriberXInfo.EXPIRY_DATE,
                    SubscriberXInfo.PRE_EXPIRY_SMS_SENT,
                    SubscriberXInfo.VOICE_PRICE_PLAN,
                    SubscriberXInfo.SMSPRICE_PLAN,
                 //   SubscriberXInfo.DATA_PRICE_PLAN,
                 //   SubscriberXInfo.BUNDLES,
                 //   SubscriberXInfo.POINTS_BUNDLES,
                    SubscriberXInfo.HLR_ID));
            
          /*  if (ApiSupport.authorizeUser(ctx, READ_CHARGING_ID))
            {
                // only special (example rkadm) can see the MSISDN serving as charging ID
                readOnlyProps.add(SubscriberXInfo.MSISDN);
            }
            else
            {
                hideProps.add(SubscriberXInfo.MSISDN);
            }*/
            //This is not done at MSISDN WebControl level, since it shared
            if (subscriber.getId().isEmpty() && !subscriber.getMSISDN().isEmpty())
            {
                subscriber.setMSISDN(com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl.DEFAULT_OPTIONAL_VALUE);
            }
            
        }
        
        if (subType.isOfType(SubscriptionTypeEnum.PREPAID_CALLING_CARD))
        {
            hideProps.addAll(Arrays.asList(
                    SubscriberXInfo.ID,
                    SubscriberXInfo.SUBSCRIPTION_CLASS,
                    SubscriberXInfo.SUBSCRIPTION_TYPE,
                    SubscriberXInfo.SAT_ID,
                    SubscriberXInfo.LAST_SAT_ID,
                    SubscriberXInfo.LAST_MODIFIED,
                    SubscriberXInfo.REASON_CODE,
                    SubscriberXInfo.VRA_FRAUD_PROFILE,
                    SubscriberXInfo.QUOTA_TYPE,
                    SubscriberXInfo.QUOTA_LIMIT,
                    SubscriberXInfo.MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.MSISDN,
                    SubscriberXInfo.FAX_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.FAX_MSISDN,
                    SubscriberXInfo.DATA_MSISDN_ENTRY_TYPE,
                    SubscriberXInfo.DATA_MSISDN,
                    SubscriberXInfo.IMSI,
                    SubscriberXInfo.PACKAGE_ID,
                    SubscriberXInfo.HLR_ID,
                    SubscriberXInfo.STATE,
                    SubscriberXInfo.DEACTIVE_STATE_ADVISE_MESSAGE,
                    SubscriberXInfo.START_DATE,
                    SubscriberXInfo.END_DATE,
                    SubscriberXInfo.DEALER_CODE,
                    SubscriberXInfo.BILLING_OPTION,
                    SubscriberXInfo.DEPOSIT,
                    SubscriberXInfo.NEXT_DEPOSIT_RELEASE_DATE,
                    SubscriberXInfo.CREDIT_LIMIT,
                    SubscriberXInfo.ABOVE_CREDIT_LIMIT,
                    SubscriberXInfo.DEPOSIT_DATE,
                    SubscriberXInfo.INITIAL_BALANCE,
                    SubscriberXInfo.EXP_DATE_EXT,
                    SubscriberXInfo.MAX_BALANCE,
                    SubscriberXInfo.REACTIVATION_FEE,
                    SubscriberXInfo.CHARGE_PPSM,
                    SubscriberXInfo.PRE_EXPIRY_SMS_SENT,
                    SubscriberXInfo.DEACTIVATION_DATE,
                    SubscriberXInfo.VOICE_PRICE_PLAN,
                    SubscriberXInfo.SMSPRICE_PLAN,
                    SubscriberXInfo.DATA_PRICE_PLAN,
                    SubscriberXInfo.SECONDARY_PRICE_PLAN,
                    SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE,
                    SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE,
                    SubscriberXInfo.INTENT_TO_PROVISION_SERVICES,
                    SubscriberXInfo.BUNDLES,
                    SubscriberXInfo.AUXILIARY_SERVICES,
                    SubscriberXInfo.POINTS_BUNDLES,
                    SubscriberXInfo.SUBSCRIBER_CATEGORY,
                    SubscriberXInfo.MARKETING_CAMPAIGN_BEAN,
                    SubscriberXInfo.PERSONAL_LIST_PLAN_ENTRIES));
        }

        WebControlSupportHelper.get(ctx).hideProperties(ctx, hideProps.toArray(new PropertyInfo[]{}));
        WebControlSupportHelper.get(ctx).setPropertiesReadOnly(ctx, readOnlyProps.toArray(new PropertyInfo[]{}));
    }

	protected void hideQuotaProperties(Context ctx)
    {
        
        final PropertyInfo pooledOnlyProps[] = new PropertyInfo[]
        {
            SubscriberXInfo.QUOTA_TYPE,
            SubscriberXInfo.QUOTA_LIMIT
        };

        WebControlSupportHelper.get(ctx).hideProperties(ctx, pooledOnlyProps);
    }
	
	protected void hideNonPoolProperties(Context ctx)
    {
        // those properties are for non-pooled only user only
        final PropertyInfo pooledOnlyProps[] = new PropertyInfo[]
            {
                SubscriberXInfo.CREDIT_LIMIT, 
                SubscriberXInfo.MONTHLY_SPEND_LIMIT,
                SubscriberXInfo.ABM_CREDIT_LIMIT
            };
        WebControlSupportHelper.get(ctx).hideProperties(ctx, pooledOnlyProps);
    }


}
