/*
 * AuxiliaryServiceByIdentifier.java
 *
 * Author : victor.stratan@redknee.com
 * Date: Apr 21, 2006
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.app.crm.bean.AuxiliaryService;

/**
 * Predicate for serching AuxiliaryService objects by service id 
 */
public class AuxiliaryServiceByIdentifier implements Predicate
{
    private long id_;

    public AuxiliaryServiceByIdentifier(long id)
    {
        this.id_ = id;
    }

    public AuxiliaryServiceByIdentifier(AuxiliaryService service)
    {
        this.id_ = service.getIdentifier();
    }

    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        AuxiliaryService service = (AuxiliaryService) obj;
        return service.getIdentifier() == this.id_;
    }
}
