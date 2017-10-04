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

package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.urcs.PromotionManagementClientV2;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.extension.spid.PricePlanSwitchLimitSpidExtension;
import com.trilogy.app.crm.extension.spid.PricePlanSwitchLimitSpidExtensionXInfo;
import com.trilogy.app.urcs.promotion.v2_0.Counter;
import com.trilogy.app.urcs.promotion.v2_0.CounterDelta;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Support class for Price Plan Switch Limit 
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 */
public class PricePlanSwitchLimitSupport
{
    
    /**
     * Get PricePlanSwitchLimit SPID extension, if configured.
     * 
     * @param ctx
     * @param spid
     * @return
     */
    public static PricePlanSwitchLimitSpidExtension getPricePlanSwitchLimitSpidExtension(final Context ctx, final int spid)
    {
        PricePlanSwitchLimitSpidExtension ext = null;
        
        try
        {
            final EQ filter = new EQ(PricePlanSwitchLimitSpidExtensionXInfo.SPID, spid);
            ext = HomeSupportHelper.get(ctx).findBean(ctx, PricePlanSwitchLimitSpidExtension.class, filter);
        }
        catch (HomeException exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(PricePlanSwitchLimitSupport.class,
                        "No PricePlanSwitchLimit configuration found for SPID " + spid, exception).log(ctx);                
            }
        }

        return ext;
    }
    
    /**
     * Validates of PricePlanSwitch count limit has reached the threshold for subscription. Retrieves
     * counter from URCS for subscription and compares against the SPID config, throws
     * IllegalStateException if count has reached the threshold. 
     *  
     * @param ctx
     * @param sub
     * @throws IllegalStateException
     * @see SgR.CRM.1620
     */
    public static void validatePricePlanSwitchThreshold(final Context ctx, final Subscriber sub) throws IllegalStateException
    {
        if (sub == null)
        {
            return;
        }
        PricePlanSwitchLimitSpidExtension ext = getPricePlanSwitchLimitSpidExtension(ctx, sub.getSpid());
        if (ext == null || !ext.getEnabled())
        {
            return;
        }
        try
        {
            final PromotionManagementClientV2 client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_MANAGEMENT_CLIENT_V2_KEY);
            Counter counter = client.retrieveCounterForSub(ctx, sub.getMsisdn(), (int) sub.getSubscriptionType(), ext.getCounterProfileId());

            if (counter != null && counter.value >= ext.getThreshold())
            {
                String msg = "Price plan switch limit has reached for subcription=%s, msisdn=%s, subscriptionType=%d, count=%d, threshold=%d.";
                msg = String.format(msg, sub.getId(), sub.getMsisdn(), sub.getSubscriptionType(), counter.value, ext.getThreshold());
            
                throw new IllegalStateException(msg);
            }
        }
        catch (IllegalStateException ise)
        {
            throw ise;
        }
        catch (Exception e)
        {
            new MinorLogMsg(PricePlanSwitchLimitSupport.class, 
                    "Error occurred when validating PriceplanSwitch threshold has not reached for sub=" + sub.getId(), e).log(ctx);
        }
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(PricePlanSwitchLimitSupport.class, "validatePricePlanSwitchThreshold() passed for sub="
                    + sub.getId(), null).log(ctx);
        }
    }
    
    /**
     * Increments PricePlanSwitch counter for given subscription on URCS, using
     * PricePlanSwitchLimit config in SPID
     * 
     * @param ctx
     * @param sub
     * @return Counter Non-null counter instance with updated count if successful
     */
    public static Counter incrementPricePlanSwitchCounter(final Context ctx, final Subscriber sub)
    {
        Counter counter = null;
        
        if (sub == null)
        {
            return null;
        }
        PricePlanSwitchLimitSpidExtension ext = getPricePlanSwitchLimitSpidExtension(ctx, sub.getSpid());
        if (ext == null || !ext.getEnabled())
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                final String msg = "PricePlanSwitchLimit feature is either not configured or enabled for sub="+sub.getId()+",spid="+sub.getSpid();
                new DebugLogMsg(PricePlanSwitchLimitSupport.class, msg, null).log(ctx);
            }
            return null;
        }
        try
        {
            final List<CounterDelta> deltas = new ArrayList<CounterDelta>();
            final CounterDelta delta = new CounterDelta();
            Collection<Counter> updated;
            
            delta.counterId = ext.getCounterProfileId();
            delta.delta = 1;
            deltas.add(delta);

            final PromotionManagementClientV2 client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_MANAGEMENT_CLIENT_V2_KEY);
            updated = client.updateCounters(ctx, sub.getMSISDN(), (int) sub.getSubscriptionType(), deltas);

            //get Counter
            if (updated != null && updated.size() > 0)
            {
                counter = updated.iterator().next();
            
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(PricePlanSwitchLimitSupport.class, "PricePlanSwitch count increment successful for sub="
                            + sub.getId() + ", count=" + counter.value, null).log(ctx);
                }

            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(PricePlanSwitchLimitSupport.class, 
                    "Error occurred when updating PricePlanSwitch counter for sub=" + sub.getId(), e).log(ctx);
        }
        
        return counter;
    }
}
