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
package com.trilogy.app.crm.priceplan;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.webcontrol.OptionalLongWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AbstractPricePlan;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Validate name and rate plan de-selections.  Also validate restriction parameters.
 *
 * @author victor.stratan@redknee.com
 */
public class PricePlanValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final PricePlan pricePlan = (PricePlan) obj;
        final RethrowExceptionListener el = new RethrowExceptionListener();

        if (pricePlan.getName() == null || pricePlan.getName().trim().equals(""))
        {
            el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.NAME, "Price plan name cannot be empty"));
        }
        
        if (pricePlan.isApplyContractDurationCriteria())
        {
            long minDuration = pricePlan.getMinContractDuration();
            long maxDuration = pricePlan.getMaxContractDuration();
            
            if (minDuration == OptionalLongWebControl.DEFAULT_VALUE 
                    && maxDuration == OptionalLongWebControl.DEFAULT_VALUE)
            {
                el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.MIN_CONTRACT_DURATION, "Contract duration required (at least 1 of min or max)"));
                el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.MAX_CONTRACT_DURATION, "Contract duration required (at least 1 of min or max)"));
            }
            
            if (maxDuration != OptionalLongWebControl.DEFAULT_VALUE
                    && minDuration > maxDuration)
            {
                el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.MIN_CONTRACT_DURATION, "Minimum contract duration must be less than or equal to maximum contract duration."));
            }
        }

        if (HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
        {
            PricePlan oldPlan = null;
            try
            {
                oldPlan = PricePlanSupport.getPlan(ctx, pricePlan.getId());
            }
            catch (HomeException e)
            {
                final String msg = "Exception occured when trying to retrieve the curent Price Plan: "
                        + pricePlan.getId() + ". " + e.getMessage();
                LogSupport.minor(ctx, this, msg, e);
                throw new IllegalStateException(msg);
            }

            if (oldPlan == null)
            {
                final String msg = "Unable to retrieve the curent Price Plan: "
                        + pricePlan.getId() + ".";
                LogSupport.minor(ctx, this, msg, null);
                throw new IllegalStateException(msg);
            }

            if (oldPlan.getNextVersion() != PricePlan.DEFAULT_NEXTVERSION)
            {
                // these checks should be done only if there is already a price plan version
                if (!oldPlan.getVoiceRatePlan().equals(AbstractPricePlan.DEFAULT_VOICERATEPLAN)
                        && pricePlan.getVoiceRatePlan().equals(AbstractPricePlan.DEFAULT_VOICERATEPLAN))
                {
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.VOICE_RATE_PLAN,
                            "It is not allowed to switch to blank rate plan if versions already exist"));
                }

                if (!oldPlan.getSMSRatePlan().equals(AbstractPricePlan.DEFAULT_SMSRATEPLAN)
                        && pricePlan.getSMSRatePlan().equals(AbstractPricePlan.DEFAULT_SMSRATEPLAN))
                {
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.SMSRATE_PLAN,
                            "It is not allowed to switch to blank rate plan if versions already exist"));
                }

                if (!oldPlan.getDataRatePlan().equals(AbstractPricePlan.DEFAULT_DATARATEPLAN)
                        && pricePlan.getDataRatePlan().equals(AbstractPricePlan.DEFAULT_DATARATEPLAN))
                {
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.DATA_RATE_PLAN,
                            "It is not allowed to switch to blank rate plan if versions already exist"));
                }
            }
        }

        el.throwAllAsCompoundException();
    }
}
