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

import com.trilogy.app.crm.bean.HomezoneCount;
import com.trilogy.app.crm.bean.HomezoneCountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * This home is to assign homezonecount 0 initially to each subscriber created. The
 * homezone count is not made property of subscriber purposefully because we need to
 * update this count while provisioning/unprovisioning homezone auxiliary services through
 * cron task, so it becomes much more easier and faster to update the count in this home
 * each time we update it, instead of directly firing query to subscriber table or calling
 * the store on subscriberhome at that time.
 * TODO 2009-04-03 rethink this implementation. Most subscribers do NOT have homezone,
 * yet will have a HZCounter record for all of them.
 *
 * @author Prasanna.Kulkarni@redknee.com
 */
public class SubscriberHomezoneCountCreationHome extends HomeProxy
{

    /**
     * Creates a new SubscriberHomezoneCountCreationHome.
     * 
     * @param delegate The Home to which we delegate.
     */
    public SubscriberHomezoneCountCreationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public Object create(Context ctx, final Object obj) throws HomeException
    {
        Home hzCountHome = (Home) ctx.get(HomezoneCountHome.class);
        if (hzCountHome == null)
        {
            new MajorLogMsg(this, "HomezoneCountHome not in the context", null).log(ctx);
            throw new HomeException("HomezoneCountHome not in the context");
        }

        final Subscriber sub = (Subscriber) super.create(ctx, obj);

        if (sub != null)
        {
            // TODO 2009-07-09 create HomezoneCount records only for subscribers that subscribe to homezone.
            HomezoneCount hzCntBean = new HomezoneCount();
            hzCntBean.setSubscriberIdentifier(sub.getId());
            hzCntBean.setHzcount(0);
            hzCountHome.create(ctx, hzCntBean);
        }

        return sub;
    }
    
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        //lets remove important things first
        super.remove(ctx, obj);
        try
        {
            Home hzCountHome = (Home) ctx.get(HomezoneCountHome.class);
            if (hzCountHome == null)
            {
                new MajorLogMsg(this, "HomezoneCountHome not in the context", null).log(ctx);
                throw new HomeException("HomezoneCountHome not in the context");
            }
            Subscriber sub = (Subscriber) obj;
            if (sub != null)
            {
                HomezoneCount hzCntBean = new HomezoneCount();
                hzCntBean.setSubscriberIdentifier(sub.getId());
                hzCountHome.remove(ctx, hzCntBean);
            }            
        }
        catch(HomeException he)
        {
            // TODO 2007-3-13 improve error handling. At least log the event
            //ignore 
        }
        
    }

    private static final long serialVersionUID = 1L;
}
