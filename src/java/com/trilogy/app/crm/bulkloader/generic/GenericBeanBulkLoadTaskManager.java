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
package com.trilogy.app.crm.bulkloader.generic;

import java.io.File;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.PPMLogMsg;


/**
 * This class is a variant of @Link(GenericBeanBulkloadManager) that adjusts the loggers
 * and progress monitors to that of Generic Bulk Load Task
 * 
 * @author simar.singh@redknee.com
 * 
 * @since 8.4
 */
public class GenericBeanBulkLoadTaskManager extends GenericBeanBulkloadManager
{

    @Override
    protected PPMLogMsg createProgressMonitor(Context ctx)
    {
        PPMLogMsg ppm = (PPMLogMsg) ctx.get(PPMLogMsg.class);
        if (null == ppm)
        {
            return super.createProgressMonitor(ctx);
        }
        else
        {
            return ppm;
        }
    }



    private String getFileNameFromPath(String path)
    {
        int indexOfPathSeparator = path.lastIndexOf(File.separatorChar);
        if (indexOfPathSeparator > 0)
        {
            return path.substring(indexOfPathSeparator + 1);
        }
        else
        {
            return null;
        }
    }
}
