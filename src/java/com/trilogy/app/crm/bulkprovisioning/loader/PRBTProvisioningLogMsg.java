package com.trilogy.app.crm.bulkprovisioning.loader;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.format.LogMsgFormat;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;


/**
 * Extending SeverityLogMsg because, it has many of the basic components that make up a
 * useful message. Overwritting the
 * 
 * @author kumaran.sivasubramaniam
 * 
 */
public class PRBTProvisioningLogMsg extends SeverityLogMsg
{

    /**
         * 
         */
    private static final long serialVersionUID = 1L;


    public PRBTProvisioningLogMsg(Object src, String msg, Throwable t)
    {
        super(SeverityEnum.DEBUG, src.getClass().getName(), msg, t);
    }


    /**
     * Replacing the Common Redknee log message format
     * 
     * @param ctx
     *            Context Calling context with which to lookup LogMsgFormat
     * @return String Formatted string.
     * @since
     */
    public String toString(Context ctx)
    {
        if (text_ == null)
        {
            LogMsgFormat formatter = (LogMsgFormat) ctx.get(PRBTProvisioningLogMsgFormat.class);
            if (formatter != null)
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
