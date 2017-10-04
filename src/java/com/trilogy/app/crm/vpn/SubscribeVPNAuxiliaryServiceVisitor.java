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
package com.trilogy.app.crm.vpn;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.AbstractSubscriberCharger;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.util.snippet.log.Logger;

/**
 * 
 *
 * @author victor.stratan@redknee.com
 */
public class SubscribeVPNAuxiliaryServiceVisitor implements Visitor
{
    protected String ignoreID;
    protected SubscriberAuxiliaryService subAux;

    /**
     * @param subAux
     * @param ignoreID
     */
    public SubscribeVPNAuxiliaryServiceVisitor(final SubscriberAuxiliaryService subAux, final String ignoreID)
    {
        super();
        this.subAux = subAux;
        this.ignoreID = ignoreID;
    }

    /**
     *
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj) throws AgentException, AbortVisitException
    {
        final Context subCtx = context.createSubContext();
        final Subscriber sub = (Subscriber) obj;
        final String subscriberID = sub.getId();
        if (!ignoreID.equals(subscriberID))
        {
            try
            {
                SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) subAux.deepClone();
                service.setIdentifier(-1);
                service.setSubscriberIdentifier(subscriberID);
                // set this for optimization
                service.setSubscriber(sub);
                service = (SubscriberAuxiliaryService) HomeSupportHelper.get(subCtx).createBean(subCtx, service);

                CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(sub, service);
                subCtx.put(Subscriber.class, sub);
                subCtx.put(AbstractSubscriberCharger.class, charger);
                charger.charge(subCtx, null);
            }
            catch (HomeException e)
            {
                throw new AgentException("Cannot Provision Auxiliary Service " + subAux.getAuxiliaryServiceIdentifier()
                        + " to subscriber " + subscriberID + " : " + e.getMessage(), e);
            }
            catch (CloneNotSupportedException e)
            {
                // this should not happen
                Logger.crit(subCtx, this, "Unable to clone SubscriberAuxiliaryService", e);
                throw new AgentException("Cannot Provision Auxiliary Service " + subAux.getAuxiliaryServiceIdentifier()
                        + " to subscriber " + subscriberID + " : " + e.getMessage(), e);
            }
        }
    }

}
