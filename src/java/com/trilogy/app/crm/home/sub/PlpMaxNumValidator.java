/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;

/**
 * @author rchen
 *
 */
public class PlpMaxNumValidator implements Validator
{
    private static PlpMaxNumValidator INSTANCE = new PlpMaxNumValidator();
    
    public static PlpMaxNumValidator instance()
    {
        return INSTANCE;
    }

    private PlpMaxNumValidator(){}

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        Subscriber subscriber = (Subscriber) obj;
        int maxPlpNum = retrieveMaxPLPNumber(ctx, subscriber.getSpid());
        
        try
        {
            if (!validNumberOfPLPAuxiliaryServices(ctx, subscriber, maxPlpNum))
            {
                throw new IllegalStateException("Subscriber cannot have more than " + maxPlpNum + " Personal Lists.");
            }

        } 
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to retrieve subscriber auxiliary services. SubscriberID='");
            sb.append(subscriber.getId());
            sb.append("', MSISDN='");
            sb.append(subscriber.getMSISDN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
            throw new IllegalStateException("Failed to retrieve subscriber auxiliary services. PLP number validation cannot be performed.");
        }
    }
    
    private boolean validNumberOfPLPAuxiliaryServices(Context ctx, Subscriber subscriber, int maxPlpNum) throws HomeException
    {
        int plpEntries = 0;
        
        // Counting auxiliary services
        plpEntries = countPLPAuxiliaryServices(ctx, subscriber.getAuxiliaryServices(ctx));
        if (plpEntries>maxPlpNum)
        {
            return false;
        }

        // Adding future auxiliary services to count
        plpEntries += countPLPAuxiliaryServices(ctx, subscriber.getFutureAuxiliaryServices(ctx));
        if (plpEntries>maxPlpNum)
        {
            return false;
        }

        return true;
    }
    
    private int countPLPAuxiliaryServices(Context ctx, List<SubscriberAuxiliaryService> subscriberAuxiliaryServices) throws HomeException
    {
        int count = 0;
        final Collection<AuxiliaryService> services = SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(ctx, subscriberAuxiliaryServices, null);
        for (final AuxiliaryService service : services)
        {
            if (service.isPLP(ctx))
            {
                count++;
            }
        }
        return count;
    }
    
    private int retrieveMaxPLPNumber(Context ctx, int spid) throws IllegalStateException
    {
        int result = 0;
        Home spHome = (Home) ctx.get(CRMSpidHome.class);
        try
        {
            CRMSpid sp = (CRMSpid) spHome.find(ctx, Integer.valueOf(spid));
            result = sp.getMaxPlpPerSubscriber();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to get the Max PLP Number Per Subscriber. PLP Number validation cannot be performed.");
        }
        return result;
    }

}
