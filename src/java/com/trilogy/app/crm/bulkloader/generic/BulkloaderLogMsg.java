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
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

/**
 * Extending SeverityLogMsg because, it has many of the basic components that make up a useful message.
 * Overwritting the 
 * @author angie.li@redknee.com
 *
 */
public class BulkloaderLogMsg extends SeverityLogMsg 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public BulkloaderLogMsg(
            Object src,
            String msg,
            Throwable t)
    {
        super(SeverityEnum.DEBUG, src.getClass().getName(), msg, t);
    }
    
    
    /**
     *  Replacing the Common Redknee log message format
     *
     * @param ctx Context Calling context with which to lookup LogMsgFormat
     * @return String Formatted string.
     * @since
     */
    public String toString(Context ctx)
    {
        if (text_ == null)
        {
            LogMsgFormat formatter  = (LogMsgFormat) ctx.get(BulkloaderLogMsgFormat.class);

            if ( formatter != null )
            {
                text_ = formatter.format(this);
            }
            else
            {
                text_ = toString();
            }
        }

        return text_;
    }
}
