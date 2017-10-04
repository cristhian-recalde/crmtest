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

import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This classes validates that only one birthday plan auxiliary service is assigned to the subscriber.
 * 
 * @author marcio.marques@redknee.com
 *
 */
public class SubscriberBirthdayPlanValidator implements Validator
{
    private static SubscriberBirthdayPlanValidator INSTANCE = new SubscriberBirthdayPlanValidator();
    
    public static SubscriberBirthdayPlanValidator instance()
    {
        return INSTANCE;
    }

    private SubscriberBirthdayPlanValidator()
    {
        
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        Subscriber subscriber = (Subscriber) obj;
        Account account = (Account) ctx.get(Lookup.ACCOUNT);
        
        try
        {
            CompoundIllegalStateException exception = new CompoundIllegalStateException();
            int entries = countNumberOfBPAuxiliaryServices(ctx, subscriber);
            if (entries>1)
            {
                exception.thrown(new IllegalStateException("Subscriber cannot have more than 1 birthday plan auxiliary service."));
            }
            if (entries>0 && account.getDateOfBirth()==null)
            {
                exception.thrown(new IllegalStateException("Birthday plan auxiliary service cannot be selected if subscriber's date of birth is not set."));
            }
            exception.throwAll();
        }
        catch (HomeException e)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Failed to retrieve subscriber auxiliary services. SubscriberID='");
            sb.append(subscriber.getId());
            sb.append("', MSISDN='");
            sb.append(subscriber.getMSISDN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
            throw new IllegalStateException("Failed to retrieve subscriber auxiliary services. Birthday plan number validation cannot be performed.");
        }
    }

    private int countNumberOfBPAuxiliaryServices(Context ctx, Subscriber subscriber) throws HomeException
    {
        int plpEntries = 0;
        
        // Counting auxiliary services
        plpEntries = countBPAuxiliaryServices(ctx, subscriber.getAuxiliaryServices(ctx));

        // Adding future auxiliary services to count
        plpEntries += countBPAuxiliaryServices(ctx, subscriber.getFutureAuxiliaryServices(ctx));

        return plpEntries;
    }
    
    private int countBPAuxiliaryServices(Context ctx, List<SubscriberAuxiliaryService> subscriberAuxiliaryServices) throws HomeException
    {
        int count = 0;
        final Collection<AuxiliaryService> services = SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(ctx, subscriberAuxiliaryServices, null);
        for (final AuxiliaryService service : services)
        {
            if (service.isBirthdayPlan(ctx))
            {
                count++;
            }
        }
        return count;
    }
    }
