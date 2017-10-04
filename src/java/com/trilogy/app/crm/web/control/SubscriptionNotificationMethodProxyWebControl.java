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

package com.trilogy.app.crm.web.control;

import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumIndexWebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.NotificationMethodProperty;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtension;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtensionXInfo;
import com.trilogy.app.crm.filter.NotificationMethodNoEmailPredicate;
import com.trilogy.app.crm.filter.NotificationMethodNoSmsPredicate;
import com.trilogy.app.crm.support.ExtensionSupportHelper;

/**
 * Web control for limiting the choices available for subscription notification
 * method based on SPID configuration.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class SubscriptionNotificationMethodProxyWebControl extends EnumIndexWebControl
{

    public SubscriptionNotificationMethodProxyWebControl(EnumCollection _enum)
    {
        super(_enum);
    }
    
    public SubscriptionNotificationMethodProxyWebControl(EnumCollection _enum, int enumSize)
    {
        super(_enum, enumSize);
    }

    public SubscriptionNotificationMethodProxyWebControl(EnumCollection _enum, boolean autoPreview)
    {
        super(_enum, autoPreview);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public EnumCollection getEnumCollection(Context ctx)
    {
        EnumCollection enumCollection = super.getEnumCollection(ctx);
        if (enumCollection == null || enumCollection.size() == 0)
        {
            return enumCollection;
        }
        
        Predicate filter = null;

        Object webBean = ctx.get(AbstractWebControl.BEAN);
        if (webBean instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) webBean;
            
            Long subType = Long.valueOf(sub.getSubscriptionType());
            List<NotificationMethodSpidExtension> notificationExtensions = ExtensionSupportHelper.get(ctx).getExtensions(
                    ctx, 
                    NotificationMethodSpidExtension.class, 
                    new EQ(NotificationMethodSpidExtensionXInfo.SPID, sub.getSpid()));
            if (notificationExtensions != null)
            {
                for (NotificationMethodSpidExtension ext : notificationExtensions)
                {
                    Map<Object, NotificationMethodProperty> notificationMethods = ext.getNotificationMethods();
                    NotificationMethodProperty p = notificationMethods.get(subType);
                    if (p != null)
                    {
                        boolean smsAllowed = p.getSmsAllowed();
                        boolean emailAllowed = p.getEmailAllowed();
                        if (smsAllowed && emailAllowed)
                        {
                            filter = BOTH_ALLOWED_PREDICATE;
                        }
                        else if (smsAllowed)
                        {
                            filter = ONLY_SMS_ALLOWED_PREDICATE;
                        }
                        else if (emailAllowed)
                        {
                            filter = ONLY_EMAIL_ALLOWED_PREDICATE;
                        }
                        else
                        {
                            filter = NONE_ALLOWED_PREDICATE;
                        }
                    }
                }
            }
        }
        
        return enumCollection.where(ctx, filter);
    }

    private static final Predicate BOTH_ALLOWED_PREDICATE = True.instance();
    private static final Predicate ONLY_SMS_ALLOWED_PREDICATE = new NotificationMethodNoEmailPredicate();
    private static final Predicate ONLY_EMAIL_ALLOWED_PREDICATE = new NotificationMethodNoSmsPredicate();
    private static final Predicate NONE_ALLOWED_PREDICATE = new And().add(
            new NotificationMethodNoSmsPredicate()).add(
            new NotificationMethodNoEmailPredicate());
}
