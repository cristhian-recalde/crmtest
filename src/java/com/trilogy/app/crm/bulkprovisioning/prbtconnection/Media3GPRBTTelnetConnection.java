package com.trilogy.app.crm.bulkprovisioning.prbtconnection;

import java.io.IOException;
import java.util.regex.Pattern;

import com.trilogy.app.crm.bulkprovisioning.PRBTTelnetConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.telnet.TelnetWrapper2;

/**
 * Telnet connection for 3G Media server..
 * @author ksivasubramaniam
 *
 */
public class Media3GPRBTTelnetConnection implements PRBTTelnetConnection
{

    public Media3GPRBTTelnetConnection()
    {
    }


    public Media3GPRBTTelnetConnection(Context ctx, String hostName, String port, long timeout)
    {
        this.ctx_ = ctx;
        this.hostName_ = hostName;
        this.portNum_ = port;
        this.timeout_ = timeout * 1000;
    }


    /** * HlrPhysicalLink method */
    public void forceReconnect()
    {
        close();
        open();
    }


    /** * open hlr connection and log in, in case failure, * */
    public boolean open()
    {
        return openSession();
    }


    public boolean openSession()
    {
        int retry = 0;
        final PMLogMsg pmLogMsgTelnet = new PMLogMsg("3GMediaTelnetSession", "Hlr telnet login");
        tryToOpen();
        if (!isConnected())
        {
            close();
            return false;
        }
        else
        {
            pmLogMsgTelnet.log(ctx_);
        }
        return true;
    }


    public void close()
    {
        closeSession();
    }


    /** * close connection, log out first if necessary * */
    public synchronized void closeSession()
    {
        if (!isConnected())
        {
            return;
        }
        telnetConnection.disconnect();
        connected_ = false;
    }


    /** * open a telnet connection, login unix box if necessary * */
    private void tryToOpen()
    {
        if (isConnected())
        {
            return;
        }
        telnetConnection = new TelnetWrapper2(ctx_);
        telnetConnection.setNewLine(getNewLineSperator());
        try
        {
            telnetConnection.connect(hostName_, Integer.valueOf(portNum_));
            connected_ = true;
            if (LogSupport.isDebugEnabled(ctx_))
            {
                debugMsg("No authenciation required for 3G Media telnet server", null);
            }
        }
        catch (Exception e)
        {
            new InfoLogMsg(this, "Can not establish connection with HLR.", null).log(ctx_);
        }
        return;
    }


    private String getNewLineSperator()
    {
        String newLineSperator = null;
        if (null == newLineSperator || newLineSperator.isEmpty())
        {
            return System.getProperty("line.separator");
        }
        return newLineSperator;
    }


    public String getCommandResponsePattern()
    {
        final String commandPattern = null;
        if (null == commandPattern || commandPattern.isEmpty())
        {
            return PRBTTelnetConstants.DEFAULT_COMMAND_RESPONSE_PATTERN;
        }
        return commandPattern;
    }


    private void debugMsg(String msg, Exception e)
    {
        if (LogSupport.isDebugEnabled(ctx_))
        {
            new DebugLogMsg(this, msg, e).log(getContext());
        }
    }


    /**
     * * send command no matter log in or not * * @param request * @return * @throws
     * IOException * @throws ApgSyncException * @throws ApgTimeOutException
     */
    public String send(final String command) throws IOException
    {
        new InfoLogMsg(this, "  3G Media Server: sending=>{" + command + "}", null).log(getContext());
        synchronized (messageLock)
        {
            telnetConnection.send(command);
            String result = waitForReply(command);
            connected_ = false;
            return result;
        }
    }


    private String waitForReply(String command) throws IOException
    {
        final Pattern commandResponsePattern;
        {
            commandResponsePattern = Pattern.compile(getCommandResponsePattern(), Pattern.DOTALL);
        }
        long endTime = System.currentTimeMillis() + timeout_;
        StringBuffer response;
        do
        {
            response = new StringBuffer();
            if (!telnetConnection.waitfor(commandResponsePattern, response, timeout_))
            {
                new InfoLogMsg(this, "Action Response time out, will try to synchronize", null).log(getContext());
            }
            else
            {
                return response.toString();
            }
        }
        while (System.currentTimeMillis() < endTime);
        return null;
    }


    public boolean isConnected()
    {
        return connected_;
    }


    private Context getContext()
    {
        return ctx_;
    }

    private TelnetWrapper2 telnetConnection;
    protected boolean connected_ = false;
    private String portNum_;
    private String hostName_;
    private long timeout_;
    private Context ctx_;
    private Object messageLock = new Object();
}
