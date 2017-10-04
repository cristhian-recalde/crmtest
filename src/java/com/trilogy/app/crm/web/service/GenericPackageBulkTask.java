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
package com.trilogy.app.crm.web.service;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

import com.trilogy.app.crm.bean.GenericPackageImportCSVSupport;
import com.trilogy.app.crm.bean.IdentifiableBackgroundTask;
import com.trilogy.app.crm.bean.PackageBulkTask;

/**
 * A background task to feed GenericPackageImort
 *
 * @author simar.singh@redknee.com
 * 
 */
public class GenericPackageBulkTask extends IdentifiableBackgroundTask<PackageBulkTask>
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GenericPackageBulkTask(PackageBulkTask taskOwner)
    {
        super(taskOwner);
        packageTask_ = taskOwner;
    }

    @Override
    public ContextAgent getTaskExecutor(Context ctx)
    {
		return new GenericPackageBulkLoader(ctx,
		    packageTask_.getFileLocation(),
		    packageTask_.getBatchId());
    }
    
    private final PackageBulkTask packageTask_;
    
    
}
