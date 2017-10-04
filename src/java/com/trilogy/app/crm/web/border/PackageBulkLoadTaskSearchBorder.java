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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.FileBulkLoadTask;
import com.trilogy.app.crm.bean.PackageBulkLoadTaskSearchWebControl;
import com.trilogy.app.crm.bean.PackageBulkLoadTaskSearchXInfo;
import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageBulkTaskXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.FindSearchAgent;


/**
 * A Generic SearchBorder for all File Bulk Loading Task Homes.
 * 
 * Add this Border before the WebController, not as one of either its Summary or Detail
 * borders.
 * 
 * @author simar.singh@redknee.com
 **/
public class PackageBulkLoadTaskSearchBorder<BEAN extends FileBulkLoadTask> extends FileLoadingTypeBulkTaskSearchBorder<PackageBulkTask>
{

    public PackageBulkLoadTaskSearchBorder(final Context context)
    {
        super(context, PackageBulkTask.class, new PackageBulkLoadTaskSearchWebControl());
        addAgent(new FindSearchAgent(PackageBulkTaskXInfo.BATCH_ID, PackageBulkLoadTaskSearchXInfo.BATCH_ID, false));
    }
}
