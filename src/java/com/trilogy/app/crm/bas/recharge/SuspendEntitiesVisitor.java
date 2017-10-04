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
package com.trilogy.app.crm.bas.recharge;

import java.util.HashMap;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Suspend provided Entities (Services, AuxiliaryServices, Bundles) for the given
 * subscriber.
 *
 * @author victor.stratan@redknee.com
 */
public class SuspendEntitiesVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the PM module.
     */
    public static final String PM_MODULE = SuspendEntitiesVisitor.class.getName();


    /**
     * Create a new instance of <code>SuspendEntitiesVisitor</code>.
     */
    public SuspendEntitiesVisitor()
    {
        // do nothing
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
    {
        final Subscriber subscriber = (Subscriber) obj;
        StringBuilder details = new StringBuilder();
        details.append("SubscriberId='");
        details.append(subscriber.getId());
        details.append("'");

        SubscriberSuspendedEntities subSuspended = subscriber.getNewlySuspendedEntities();
        if (subSuspended==null) 
            return;
        
            if (subSuspended.hasSuspensionInThisCharge())
            {
                final PMLogMsg pmLogMsg = new PMLogMsg(SuspensionSupport.PM_MODULE, ": Suspending services", details.toString());

                try
                {
                    HashMap suspendedPackages = subSuspended.getSuspendedPackages();
                    HashMap suspendedServices = subSuspended.getSuspendedServices();
                    HashMap suspendedBundles = subSuspended.getSuspendedBundles();
                    HashMap suspendedAuxServices = subSuspended.getSuspendedAuxServices();
                    
                    final PMLogMsg smsPm = new PMLogMsg(SuspensionSupport.PM_MODULE, ": Sending suspension SMS", details.toString());
                    SubscriptionNotificationSupport.sendSuspendNotification(ctx, subscriber, suspendedPackages, suspendedServices,
                            suspendedBundles, suspendedAuxServices, true);
                    smsPm.log(ctx);
    
                    SuspensionSupport.suspendPackages(ctx, subscriber, suspendedPackages, true);
                    SuspensionSupport.suspendServices(ctx, subscriber, suspendedServices, true);
                    SuspensionSupport.suspendBundles(ctx, subscriber, suspendedBundles, true);
                    SuspensionSupport.suspendAuxServices(ctx, subscriber, suspendedAuxServices, true, this);
                }
                catch (final Throwable throwable)
                {
                    new MajorLogMsg(this, "Problem occurred while suspending entities for" + " Prepaid subscriber "
                        + subscriber.getId() + " with insufficient balance.", throwable).log(ctx);
                }
                pmLogMsg.log(ctx);
            }
            else
            {
                
            }
    }

/*
    private static void suspendVoiceMail(Context ctx, boolean suspend, Subscriber sub)
    {
        try
        {
            MpathixService mpathixVMService = VoicemailSupport.getVMService(ctx);
            VoicemailServiceConfig configBean = VoicemailSupport.getVMConfig(ctx);
            if (suspend)
            {
                mpathixVMService.deactivateAccount(sub.getMSISDN(), configBean.getModifyUserWait(),
                        configBean.getModifyUserTimeOut());
            }
            else
            {
                mpathixVMService.activateAccount(sub.getMSISDN(), configBean.getModifyUserWait(),
                        configBean.getModifyUserTimeOut());
            }
        }
        catch (HomeException e)
        {
            new MajorLogMsg(SuspendEntitiesVisitor.class.getName(),
                    "Problem occurred while suspending Voicemail service for"
                    + " Prepaid subscriber " + sub.getId()
                    + " with insufficient balance.",
                    e).log(ctx);
        }
    }
*/
}
