/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;


/**
 * Predicate to verify whether or not a dunning report can be deleted.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class EditableDunningReportPredicate extends SimpleDeepClone implements Predicate
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new EditableDunningReportPredicate predicate.
     */
    public EditableDunningReportPredicate()
    {
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        DunningReport report = (DunningReport) obj;
        
        if (report!=null)
        {
            return DunningReportStatusEnum.PENDING_INDEX == report.getStatus();
        }
        
        return false;
    }
}
