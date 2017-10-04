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
package com.trilogy.app.crm.subscriber.provision;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * This listener will call the switch priceplan logic whenever needed.
 *
 * @author victor.stratan@redknee.com
 */
public class PricePlanSwitchFromWebListener implements PropertyChangeListener
{
    public void propertyChange(final PropertyChangeEvent evt)
    {
        final Context ctx = (Context) evt.getSource();
        final Subscriber oldSub = (Subscriber) evt.getOldValue();
        final Subscriber newSub = (Subscriber) evt.getNewValue();

        if (Subscriber.isFromWebNewOrPreviewOnPriceplan(ctx))
        {
            long oldPPid = -1;
            if (oldSub != null)
            {
                oldPPid = oldSub.getPricePlan();
            }

            long newPPid = newSub.getPricePlan();

            if (newSub.getSatId() != newSub.getLastSatId()
                    && newSub.getSatId() != Subscriber.DEFAULT_SATID)
            {
                applyServiceActivationTemplate(ctx, newSub, newSub.getSatId());
                newSub.setLastSatId(newSub.getSatId());

                if (oldPPid != newSub.getPricePlan()
                        && SafetyUtil.safeEquals(Subscriber.DEFAULT_ID, newSub.getId()))
                {
                    // deposit and credit limit set logic for new subscribers
                    newSub.setDeposit(Subscriber.DEFAULT_DEPOSIT);
                    newSub.setCreditLimit(Subscriber.DEFAULT_CREDITLIMIT);
                }
            }
            else if (newSub.getSatId() == Subscriber.DEFAULT_SATID
                    && newSub.getLastSatId() != Subscriber.DEFAULT_SATID)
            {
                newSub.setLastSatId(newSub.getSatId());
            }
            else if (oldPPid != newPPid)
            {
                // we have to revert to the old PP id to have the propper Price Plan switch
                newSub.setPricePlan(oldPPid);

                newSub.switchPricePlan(ctx, newPPid);

                if (oldPPid != newSub.getPricePlan()
                        && SafetyUtil.safeEquals(Subscriber.DEFAULT_ID, newSub.getId()))
                {
                    // deposit and credit limit set logic for new subscribers
                    newSub.setDeposit(Subscriber.DEFAULT_DEPOSIT);
                    newSub.setCreditLimit(Subscriber.DEFAULT_CREDITLIMIT);
                }
            }
            else
            {
				// TT#12022146028: Need to call switch price plan even if the
				// old price plan equals the new one, so that the version will
				// be properly updated if the CSR had previously selected
				// another price plan.
                newSub.switchPricePlan(ctx, newPPid);
            }
        }
    }

    /**
     * Apply values in SAT.
     *
     * @param ctx the operating context
     * @param sub subscriber to modify with the template
     * @param satId id of the template to apply to the subscriber
     */
    public void applyServiceActivationTemplate(final Context ctx, final Subscriber sub, final long satId)
    {
        try
        {
            SubscriberSupport.applySubServiceActivationTemplate(ctx, sub, satId);
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Failed to apply subscriber creation template [" + satId + "]", e).log(ctx);
        }
    }
}
