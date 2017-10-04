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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Validates the subscriber's MSISDN is in the same technology as the subscriber's technology.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberMsisdnTechnologyValidator implements Validator
{

    /**
     * Singleton instance.
     */
    private static Validator instance = new SubscriberMsisdnTechnologyValidator();

    /**
     * Creates a new <code>SubscriberMsisdnTechnologyValidator</code>.
     */
    protected SubscriberMsisdnTechnologyValidator()
    {
        // empty constructor to protect singleton
    }

    /**
     * Get an instance of this validator.
     *
     * @return An instance of this validator.
     */
    public static Validator getInstance()
    {
        return instance;
    }

    /**
     * Validates the subscriber's MSISDN is in the same technology as the subscriber's technology.
     *
     * @param context The operating context
     * @param object The subscriber being updated
     * @throws IllegalStateException Thrown if the MSISDN's technology and the subscriber's technology don't match
     */
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Subscriber subscriber = (Subscriber) object;
        final TechnologyEnum subTechEnum = subscriber.getTechnology();
        if (null != subTechEnum && TechnologyEnum.NO_TECH != subTechEnum && TechnologyEnum.ANY != subTechEnum)
        {
            final CompoundIllegalStateException compoundException = new CompoundIllegalStateException();
            try
            {
                final Msisdn msisdn = MsisdnSupport.getMsisdn(context, subscriber.getMSISDN());
                if (msisdn != null)
                {
                    // the correctness of the MSISDN is checked in
                    // VoiceMsisdnHomeValidator
                    // TODO 2007-06-27 merge this check with the
                    // AbstractMsisdnHomeValidator to avoid multiple lookup
                    final TechnologyEnum technology = msisdn.getTechnology();
                    if (!technology.equals(TechnologyEnum.ANY) && !technology.equals(subTechEnum))
                    {
                        compoundException.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.MSISDN,
                                "MSISDN does not belong to the technology specified for this subscriber"));
                    }
                }
            }
            catch (Throwable exception)
            {
                compoundException.thrown(new IllegalStateException("Unable to retrieve MSISDN", exception));
            }
            compoundException.throwAll();
        }
    }

}
