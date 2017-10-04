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
package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.ContextAgent;

/**
 * 
 * @author simar.singh@redknee.com
 *
 * @param <BEAN> - It is home-bean (having a primary key attribute)
 */
public class IdentifiableBackgroundTaskExecutor<BEAN extends AbstractBean & Identifiable & ContextAgent> extends IdentifiableBackgroundTask<BEAN>
{

    public IdentifiableBackgroundTaskExecutor(BEAN task)
    {
        super(task, task);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    
}
