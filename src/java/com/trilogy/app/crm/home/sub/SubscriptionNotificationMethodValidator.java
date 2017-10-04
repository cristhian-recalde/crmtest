/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.NotificationMethodProperty;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtension;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class SubscriptionNotificationMethodValidator extends
        AbstractSubscriberValidator
{
    /**
     * Singleton instance.
     */
    private static SubscriptionNotificationMethodValidator instance;

    public SubscriptionNotificationMethodValidator()
    {
        // empty
    }

    /**
     * Returns an instance of
     * <code>SubscriptionNotificationMethodValidator</code>.
     * 
     * @return An instance of
     *         <code>SubscriptionNotificationMethodValidator</code>.
     */
    public static SubscriptionNotificationMethodValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionNotificationMethodValidator();
        }

        return instance;
    }

    /**
     * @see com.redknee.app.crm.home.sub.AbstractSubscriberValidator#validate(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        final Subscriber sub = (Subscriber) obj;

        if (NotificationMethodEnum.SMS_INDEX == sub.getNotificationMethod()
                || NotificationMethodEnum.EMAIL_INDEX == sub
                        .getNotificationMethod()
                || NotificationMethodEnum.BOTH_INDEX == sub
                        .getNotificationMethod())
        {

            CRMSpid spid = null;
            try
            {
                spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
                spid.setContext(ctx);
            }
            catch (HomeException exception)
            {
                exceptions.thrown(new IllegalStateException(
                        "Subscription SPID cannot be found."));
            }

            Long subType = Long.valueOf(sub.getSubscriptionType());
            if (spid != null)
            {
                for (Object o : spid.getExtensions())
                {
                    Extension extension = (Extension) o;
                    if (extension instanceof NotificationMethodSpidExtension)
                    {
                        NotificationMethodSpidExtension ext = (NotificationMethodSpidExtension) extension;
                        if (ext.getNotificationMethods().containsKey(subType))
                        {
                            NotificationMethodProperty p = (NotificationMethodProperty) ext
                                    .getNotificationMethods().get(subType);
                            if (!p.getSmsAllowed()
                                    && (NotificationMethodEnum.SMS_INDEX == sub
                                            .getNotificationMethod() || NotificationMethodEnum.BOTH_INDEX == sub
                                            .getNotificationMethod()))
                            {
                                exceptions
                                        .thrown(new IllegalPropertyArgumentException(
                                                SubscriberXInfo.NOTIFICATION_METHOD,
                                                "SMS notification is not allowed for this subscription type"));
                            }
                            else if (!p.getEmailAllowed()
                                    && (NotificationMethodEnum.EMAIL_INDEX == sub
                                            .getNotificationMethod() || NotificationMethodEnum.BOTH_INDEX == sub
                                            .getNotificationMethod()))
                            {
                                exceptions
                                        .thrown(new IllegalPropertyArgumentException(
                                                SubscriberXInfo.NOTIFICATION_METHOD,
                                                "E-mail notification is not allowed for this subscription type"));
                            }
                        }
                    }
                }
            }
        }
        exceptions.throwAllAsCompoundException();
    }

}
