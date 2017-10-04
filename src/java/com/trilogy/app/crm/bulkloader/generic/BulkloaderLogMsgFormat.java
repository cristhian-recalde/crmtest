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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.format.LogMsgFormat;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.logger.LoggerSupport;

/**
 * Trimmed down Logging format for the Generic Bean Bulkloader
 * @author angie.li@redknee.com
 *
 */
public class BulkloaderLogMsgFormat implements LogMsgFormat 
{
    
    protected StringBuilder sb_ = new StringBuilder();
    protected static final char DEFAULT_DELIMITER = ',';
    protected char delimiter_ = DEFAULT_DELIMITER;
    // Cache last timestamp string for performance
    protected long   lastTime_  = -1;
    protected String lastDate_  = "";
    protected Date   date_      = new Date();

    protected final SimpleDateFormat dateFormat_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
       
    public String format(LogMsg logMsg) 
    {
        sb_.setLength(0);
        
        if ( logMsg.getTimestamp() != lastTime_ )
          {
             lastTime_ = logMsg.getTimestamp();
             date_.setTime(lastTime_);
             // TODO 2010-10-01 DateFormat access needs synchronization
             lastDate_ = dateFormat_.format(date_);
          }
        
        sb_.append(lastDate_);
        sb_.append(delimiter_);
        sb_.append(logMsg.getSeverity().getDescription());
        sb_.append(delimiter_);
        sb_.append(logMsg.getMessage());

        sb_.append(LoggerSupport.newline());

        return sb_.toString();
    }

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
