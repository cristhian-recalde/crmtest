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

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.support.Lookup;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class MultiSimMsisdnChangeHome extends HomeProxy
{

    public MultiSimMsisdnChangeHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public MultiSimMsisdnChangeHome(Context ctx)
    {
        super(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        String oldMsisdn = null;
        if (oldSub != null)
        {
            oldMsisdn = oldSub.getMSISDN();
        }
        String newMsisdn = newSub.getMSISDN();
        if (oldMsisdn != null && !SafetyUtil.safeEquals(oldMsisdn, newMsisdn))
        {
            Collection<Extension> extensions = newSub.getExtensions();
            if (extensions != null)
            {
                for (Extension ext : extensions)
                {
                    if (ext instanceof MultiSimSubExtension)
                    {
                        ((MultiSimSubExtension) ext).changeMainMsisdn(ctx, oldMsisdn, newMsisdn);
                    }
                }
            }
        }
        
        return super.store(ctx, obj);
    }

}
