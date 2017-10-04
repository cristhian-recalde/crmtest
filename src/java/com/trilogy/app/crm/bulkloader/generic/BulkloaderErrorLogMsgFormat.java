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
package com.trilogy.app.crm.bulkloader.generic;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.format.LogMsgFormat;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.logger.LoggerSupport;

/**
 * Trimmed down Error Record Logging format for the Generic Bean Bulkloader
 * @author angie.li@redknee.com
 *
 */
public class BulkloaderErrorLogMsgFormat implements LogMsgFormat
{
    
    /**
     * Only print out the message in the LogMsg
     */
    public String format(LogMsg logMsg) 
    {
        sb_.setLength(0);
        
        sb_.append(logMsg.getMessage());
        sb_.append(LoggerSupport.newline());

        return sb_.toString();
    }
    

    protected StringBuilder sb_ = new StringBuilder();


    @Override
    public void format(LogMsg logMsg, StringBuilder b)
    {
        
    }


    @Override
    public LogMsg parseLogMsg(Context ctx, String str)
    {
        throw new UnsupportedOperationException();
    }
}
