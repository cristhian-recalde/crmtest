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
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SysFeatureCfg;

/**
 * Validates the presence of Identification Number for Postpaid subscribers.
 * For actual Identification Number value validation
 * @see com.redknee.app.crm.web.border.SubscriberIdentificationValidator
 *
 * @author paul.sperneac@redknee.com
 */
public final class IdNumber1Validator extends AbstractSubscriberValidator
{
    private static final IdNumber1Validator INSTANCE = new IdNumber1Validator();

    /**
     * Prevents initialization
     */
    private IdNumber1Validator()
    {
    }

    public static IdNumber1Validator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
	final Subscriber sub = (Subscriber) obj;           
        if (sub.getSubscriberType() != SubscriberTypeEnum.PREPAID)
        {
            final CompoundIllegalStateException el = new CompoundIllegalStateException();
            validIdTypeSelected(el, obj);
     
            final SysFeatureCfg cfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        
            if (cfg.isSubscriberIdValidate())
            {
           
                // TODO 2008-08-22 no longer part of Subscriber
                // class to be removed
                //required(el, sub.getIdNumber1(), SubscriberXInfo.ID_NUMBER1);
           
            }
            el.throwAll();
        }
    }

    private void validIdTypeSelected( final ExceptionListener el, final Object obj)
    {
        final Subscriber sub = (Subscriber) obj;
        
        // TODO 2008-08-22 no longer part of Subscriber
        //if ( sub.getIdType1() == AbstractSubscriber.DEFAULT_IDTYPE1 )
        //{
        //    el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.ID_TYPE1, "Valid Id Type required."));
        //}
    }
}
