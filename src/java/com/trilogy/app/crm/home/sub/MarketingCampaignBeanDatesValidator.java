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

package com.trilogy.app.crm.home.sub;

import java.util.Calendar;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.MarketingCampaignBean;
import com.trilogy.app.crm.bean.MarketingCampaignBeanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.DateValidator;
import com.trilogy.app.crm.support.Lookup;

/**
 * Validates the dates in a {@link MarketingCampaignBean}.
 *
 * @author cindy.wong@redknee.com
 */
public class MarketingCampaignBeanDatesValidator extends DateValidator
{
    /**
     * Create a new instance of <code>MarketingCampaignBeanDatesValidator</code>.
     */
    protected MarketingCampaignBeanDatesValidator()
    {
        super();
    }

    /**
     * Returns an instance of <code>MarketingCampaignBeanDatesValidator</code>.
     *
     * @return An instance of <code>MarketingCampaignBeanDatesValidator</code>.
     */
    public static MarketingCampaignBeanDatesValidator instance()
    {
        if (instance == null)
        {
            instance = new MarketingCampaignBeanDatesValidator();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber newSubscriber = (Subscriber) object;
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);

        final MarketingCampaignBean newBean = newSubscriber.getMarketingCampaignBean();
        final MarketingCampaignBean oldBean;

        if (oldSubscriber == null)
        {
            oldBean = null;
        }
        else
        {
            oldBean = oldSubscriber.getMarketingCampaignBean();
        }

        final GeneralConfig config = (GeneralConfig) context.get(GeneralConfig.class);
        if (config == null)
        {
            final IllegalStateException exception = new IllegalStateException(
                "System Error: GeneralConfig does not exist in context");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        try
        {
			validatePrior(context, oldBean, newBean,
			    MarketingCampaignBeanXInfo.START_DATE, config);

            // protect against really, really off dates.
			validBefore(context, oldBean, newBean,
			    MarketingCampaignBeanXInfo.START_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        try
        {
			validatePrior(context, oldBean, newBean,
			    MarketingCampaignBeanXInfo.END_DATE, config);

            // protect against really, really off dates.
			validBefore(context, oldBean, newBean,
			    MarketingCampaignBeanXInfo.END_DATE, Calendar.YEAR, 100);
        }
        catch (IllegalPropertyArgumentException exception)
        {
            exceptions.thrown(exception);
        }

        exceptions.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static MarketingCampaignBeanDatesValidator instance;
}
