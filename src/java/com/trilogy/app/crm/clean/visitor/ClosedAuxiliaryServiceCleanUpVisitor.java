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
package com.trilogy.app.crm.clean.visitor;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.RemoveAllVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.visitor.AuxiliaryServiceCachingVisitor;


/**
 * This visitor will delete dependencies for closed auxiliary services.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ClosedAuxiliaryServiceCleanUpVisitor implements Visitor
{
    private static Visitor instance_ = null;
    public static Visitor instance()
    {
        if (instance_ == null)
        {
            instance_ = new ClosedAuxiliaryServiceCleanUpVisitor();
        }
        return instance_;
    }
    
    protected ClosedAuxiliaryServiceCleanUpVisitor()
    {   
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        AuxiliaryService service = (AuxiliaryService) obj;
        if (EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.CLOSED))
        {
            try
            {
                Home subAuxSvcHome = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);
                subAuxSvcHome.forEach(
                        ctx, 
                        new AuxiliaryServiceCachingVisitor(service, new RemoveAllVisitor(subAuxSvcHome)),
                        new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, service.getIdentifier()));
            }
            catch (HomeException e)
            {
                throw new AgentException("Error occurred removing subscription associations for closed auxiliary service " + service.getIdentifier(), e);
            }

            try
            {
                Home sctAuxSvcHome = (Home) ctx.get(SctAuxiliaryServiceHome.class);
                sctAuxSvcHome.removeAll(
                        ctx,
                        new EQ(SctAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, service.getIdentifier()));
            }
            catch (HomeException e)
            {
                throw new AgentException("Error occurred removing subscription creation template associations for closed auxiliary service " + service.getIdentifier(), e);
            }
        }
    }
    
}
