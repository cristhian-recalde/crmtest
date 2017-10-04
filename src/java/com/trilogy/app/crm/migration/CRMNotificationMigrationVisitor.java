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
package com.trilogy.app.crm.migration;

import com.trilogy.app.crm.bean.SpidLangMsgConfig;
import com.trilogy.app.crm.bean.StateNotificationMsg;
import com.trilogy.app.crm.bean.SubServiceSuspendMsg;
import com.trilogy.app.crm.delivery.email.CRMEmailTemplate;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.notification.template.EmailNotificationTemplate;
import com.trilogy.app.crm.notification.template.SmsNotificationTemplate;
import com.trilogy.app.crm.notification.template.SubscriptionServiceSuspendSmsNotificationTemplate;
import com.trilogy.app.crm.notification.template.SubscriptionStateChangeEmailNotificationTemplate;
import com.trilogy.app.crm.notification.template.SubscriptionStateChangeSmsNotificationTemplate;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMNotificationMigrationVisitor extends NotificationMigrationVisitor
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected SmsNotificationTemplate createSmsTemplate(SpidLangMsgConfig template)
    {
        if (template instanceof StateNotificationMsg)
        {
            StateNotificationMsg stateTmpl = (StateNotificationMsg) template; 
            
            SubscriptionStateChangeSmsNotificationTemplate result = new SubscriptionStateChangeSmsNotificationTemplate();
            
            result.setPreviousState(stateTmpl.getPreviousState());
            result.setNewState(stateTmpl.getNewState());
            
            return result;
        }
        else if (template instanceof SubServiceSuspendMsg)
        {
            SubServiceSuspendMsg suspTmpl = (SubServiceSuspendMsg) template;
            
            SubscriptionServiceSuspendSmsNotificationTemplate result = new SubscriptionServiceSuspendSmsNotificationTemplate();
            
            result.setFooter(suspTmpl.getSuspendFooter());

            result.setUnprovisionFooter(suspTmpl.getUnprovisionExtraLine());
            
            return result;
        }
        else
        {
            return super.createSmsTemplate(template);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EmailNotificationTemplate createEmailTemplate(CRMEmailTemplate template, NotificationTypeEnum type)
    {
        if (NotificationTypeEnum.STATE_CHANGE.equals(type))
        {
            SubscriptionStateChangeEmailNotificationTemplate result = new SubscriptionStateChangeEmailNotificationTemplate();
            
            result.setPreviousState(template.getPreviousState());
            result.setNewState(template.getNewState());
            
            return result;
        }
        else
        {
            return super.createEmailTemplate(template, type);
        }
    }

}
