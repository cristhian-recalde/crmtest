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
package com.trilogy.app.crm.visitor;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.VisitorProxy;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;


/**
 * This visitor caches an instance of an auxiliary service and sets the
 * auxiliary service field of subscription associations to this cached
 * value.  It is useful for bulk operations on subscription associations
 * belonging to the same auxiliary service because it avoids excessive
 * lazy loading of the same auxiliary service.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AuxiliaryServiceCachingVisitor extends VisitorProxy
{
    private static final long serialVersionUID = 1L;
    
    private AuxiliaryService service_ = null;

    public AuxiliaryServiceCachingVisitor(AuxiliaryService service, Visitor delegate)
    {
        super(delegate);
        service_ = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        if (obj instanceof SubscriberAuxiliaryService)
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
            association.setAuxiliaryService(service_);
        }
        super.visit(ctx, obj);
    }
}
