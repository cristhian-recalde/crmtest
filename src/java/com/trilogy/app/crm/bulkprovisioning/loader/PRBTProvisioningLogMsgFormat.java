package com.trilogy.app.crm.bulkprovisioning.loader;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.format.LogMsgFormat;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.logger.LoggerSupport;


/**
 * Trimmed down Logging format for the Generic Bean Bulkloader
 * 
 * @author kumaran.sivasubramaniam
 * 
 */
public class PRBTProvisioningLogMsgFormat implements LogMsgFormat
{

    protected StringBuilder sb_ = new StringBuilder();
    


    public String format(LogMsg logMsg)
    {
        sb_.setLength(0);
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
