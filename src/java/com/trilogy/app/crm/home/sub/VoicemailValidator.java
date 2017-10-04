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
import java.util.Iterator;
import java.util.Set;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;

/**
 * @author prasanna.kulkarni@redknee.com
 */
public final class VoicemailValidator implements Validator
{
    private static final VoicemailValidator INSTANCE = new VoicemailValidator();

    private VoicemailValidator()
    {
    }

    public static VoicemailValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Subscriber sub = (Subscriber) obj;
        final boolean oldAuxSvcFlag = isVMPresentInOldSubAuxSvc(ctx, sub);
        final boolean newAuxSvcFlag = isVMPresentInNewSubAuxSvc(ctx, sub);
        final boolean oldSvcFlag = isVMPresentInOldSubSvc(ctx);
        final boolean newSvcFlag = isVMPresentInNewSubSvc(ctx, sub);
        if ((oldAuxSvcFlag || newAuxSvcFlag) && (oldSvcFlag || newSvcFlag))
        {
            throw new IllegalStateException("Please do not try to add/remove voicemail service and auxiliary service "
                    + "AT THE SAME TIME.<br>Following actions are not supported:<br>1) Adding a voicemail service/aux."
                    + " servicee when voicemail auxiliary service/service is there.<br>2) At a time, removing "
                    + "a voicemail AUXILIARY SERVICE and adding a new voicemail SERVICE.<br>3) At a time, adding a "
                    + "new voicemail AUXILIARY SERVICE and removing a voicemail SERVICE.");
        }
    }

    private static boolean isVMPresentInNewSubAuxSvc(final Context ctx, final Subscriber sub)
    {
        SubscriberAuxiliaryService subAuxSvc = null;
        final Collection chosenServices = sub.getAuxiliaryServices(ctx);
        final Iterator auxSvcIterator = chosenServices.iterator();
        while (auxSvcIterator.hasNext())
        {
            subAuxSvc = (SubscriberAuxiliaryService) auxSvcIterator.next();
            if (subAuxSvc.getType(ctx) == AuxiliaryServiceTypeEnum.Voicemail)
            {
                return true;
            }
        }
        // TODO 2007-04-10 the following looks useless since the sub.getAuxiliaryServices() contains all
        // TODO aux services, future and current
        // no voicemail in active auxiliary services search for future services.
        final Collection chosenFutureServcies = sub.getFutureAuxiliaryServices();
        final Iterator futureAuxSvcIterator = chosenFutureServcies.iterator();
        while (futureAuxSvcIterator.hasNext())
        {
            subAuxSvc = (SubscriberAuxiliaryService) futureAuxSvcIterator.next();
            if (subAuxSvc.getType(ctx) == AuxiliaryServiceTypeEnum.Voicemail)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isVMPresentInOldSubAuxSvc(final Context ctx, final Subscriber sub)
    {
        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSubscriber == null)
        {
            return false;
        }
        final Collection allExistingAssociations = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx,
                oldSubscriber.getId());
        if (allExistingAssociations == null)
        {
            return false;
        }
        for (final Iterator i = allExistingAssociations.iterator(); i.hasNext();)
        {
            final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) i.next();
            if (service != null)
            {
                try
                {
                    final AuxiliaryService svc = service.getAuxiliaryService(ctx);
                    if (svc != null && svc.getType() == AuxiliaryServiceTypeEnum.Voicemail)
                    {
                        return true;
                    }
                }
                catch (HomeException he)
                {
                    //ignore
                    // TODO at least log
                }
            }
        }
        return false;
    }

    private boolean isVMPresentInOldSubSvc(final Context ctx)
    {
        /*
         * Instead of retrieving servcies from GUI, we retrieve the current services of the subscriber this will
         * return us a service which is unchecked on GUI. This is required because serviceProvision home comes after
         * auxiliary servcie provision home.
         * E.g.
         * If a subscriber has subscribed to a voicemail "SERVICE" and csr uncheks that services and before clicking
         * update csr adds an voicemail "AUXILIARY SERVICE" to the subscriber. This may seem valid but
         * provisioning/unprovisioning of auxiliary is done before the provisioning/unprovisioning of a service hence
         * in the above scenario the voicemailauxiliary service addition will fail
         * as Mpathix will not accept the addUser command because a user already exists in Mpathix with same userID.
         *
         */
        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSubscriber == null)
        {
            return false;
        }
        return isVMPresentInSubSvc(ctx, oldSubscriber);
    }

    private boolean isVMPresentInNewSubSvc(final Context ctx, final Subscriber sub)
    {
        /*
         * Instead of retrieving servcies from GUI, we retrieve the current services of the subscriber this will
         * return us a service which is unchecked on GUI. This is required because serviceProvision home comes after
         * auxiliary servcie provision home.
         * E.g.
         * If a subscriber has subscribed to a voicemail "SERVICE" and csr uncheks that services and before clicking
         * update csr adds an voicemail "AUXILIARY SERVICE" to the subscriber. This may seem valid but
         * provisioning/unprovisioning of auxiliary is done before the provisioning/unprovisioning of a service hence
         * in the above scenario the voicemailauxiliary service addition will fail
         * as Mpathix will not accept the addUser command because a user already exists in Mpathix with same userID.
         *
         */
        if (sub == null)
        {
            return false;
        }
        return isVMPresentInSubSvc(ctx, sub);
    }

    private boolean isVMPresentInSubSvc(final Context ctx, final Subscriber sub)
    {
        final Home svcHome = (Home) ctx.get(ServiceHome.class);
        if (svcHome == null)
        {
            new MajorLogMsg(this, "ServiceHome not found in context", null).log(ctx);
            return false;
        }
        final Set<ServiceFee2ID> svcIdSet = sub.getServices();
        if (svcIdSet == null)
        {
            return false;
        }
        
        final Iterator<ServiceFee2ID> svcIterator = svcIdSet.iterator();
        while (svcIterator.hasNext())
        {
            //final Long svcId = (Long) svcIterator.next();
        	long svcId = 0;
        	Object object = svcIterator.next();
			if (object instanceof ServiceFee2ID) {
				svcId = ((ServiceFee2ID) object).getServiceId();
			} else if (object instanceof Long) {
				svcId = (Long) object;
			}
            try
            {
                final Service svc = (Service) svcHome.find(svcId);
                if (svc != null && svc.getType() == ServiceTypeEnum.VOICEMAIL)
                {
                    return true;
                }
            }
            catch (HomeException he)
            {
                new MajorLogMsg(this, "Exception while searching Service for ID:" + svcId + " for subscriber:"
                        + sub.getId(), null).log(ctx);
            }
        }
        return false;
    }
}
