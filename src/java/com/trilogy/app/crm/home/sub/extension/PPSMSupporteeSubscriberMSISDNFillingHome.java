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
package com.trilogy.app.crm.home.sub.extension;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Fills in the MSISDN field during extension creation. 
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporteeSubscriberMSISDNFillingHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PPSMSupporteeSubscriberMSISDNFillingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        PPSMSupporteeSubExtension extension = (PPSMSupporteeSubExtension) obj;
        Subscriber sub = (Subscriber) ExtensionSupportHelper.get(ctx).getParentBean(ctx);
        if (sub!=null)
        {
            extension.setMSISDN(sub.getMSISDN());
        }
        return super.create(ctx, obj);
    }
    
    public Object store(Context ctx, Object obj) throws HomeException
    {
        PPSMSupporteeSubExtension extension = (PPSMSupporteeSubExtension) obj;
        Subscriber sub = (Subscriber) ExtensionSupportHelper.get(ctx).getParentBean(ctx);
        if (sub!=null)
        {
            extension.setMSISDN(sub.getMSISDN());
        }
        return super.store(ctx, obj);
    }

}
