package com.trilogy.app.crm.bulkprovisioning.prbtconnection;

/*
 *  * This code is a protected work and subject to domestic and international copyright *
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * * source code is, by its very nature, confidential information and inextricably
 * contains * trade secrets and other information proprietary, valuable and sensitive to
 * Redknee. No * unauthorized use, disclosure, manipulation or otherwise is permitted, and
 * may only be used in accordance with the terms of the license agreement entered into
 * with Redknee * Inc. and/or its subsidiaries. * * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its
 * subsidiaries. All Rights Reserved.
 */
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.regex.Pattern;

import com.trilogy.app.crm.bulkprovisioning.PRBTBulkProvisioningConfig;
import com.trilogy.app.crm.bulkprovisioning.PRBTTelnetConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.telnet.TelnetWrapper2;



/** * @author kumaran.sivasubramaniam@redknee.com 
 * 
 * @author ksivasubramaniam
 *
 */
public class GreencityPRBTTelnetConnection implements PRBTTelnetConstants, PRBTTelnetConnection
{

    public GreencityPRBTTelnetConnection()
    {
    }

    public GreencityPRBTTelnetConnection(final Context ctx, PRBTBulkProvisioningConfig config)
    {
        this(ctx, config.getHostname(), config.getPortNumber(), config.getUserId(), config.getPassword(), config.getTimeoutTelnetServer() );
    }

    public GreencityPRBTTelnetConnection(Context ctx, final String hostName, final String port, final String userName, final String password,
            final long timeout)
    {
        this.ctx_ = ctx;
        this.hostName_ = hostName;
        this.portNum_ = port;
        this.userName_ = userName;
        this.password_ = password;
        this.timeout_ = timeout *1000;
        
    }


    /** * HlrPhysicalLink method */
    public void forceReconnect()
    {
        close();
        open();
    }

    public boolean open()
    {
        return openSession();
    }
    
    /** * open hlr connection and log in, in case failure, * */
    private synchronized boolean openSession()
    {
        final PMLogMsg pmLogMsgTelnet = new PMLogMsg("GreencityPRBTTelnetConnection", "PRBT telnet login");
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
    private synchronized void closeSession()
    {
        if (!isConnected())
        {
            return;
        }
        try
        {
            telnetConnection.send(getExitCommandString());
        }
        catch (Throwable t)
        {
            new InfoLogMsg(this, "fail to logout, but will close the connection", null).log(ctx_);
        }
        finally
        {
            telnetConnection.disconnect();
            connected_ = false;
        }
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
            telnetConnection.connect(hostName_, Integer.valueOf(portNum_), (int) timeout_);
            connected_ = telnetLogin();
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Can not establish connection with HLR.", e).log(ctx_);
        }
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


    protected String getSyncCommand()
    {
        String cmd = null;
        if (cmd == null || cmd.trim().length() < 1)
        {
            cmd = DEFAULT_HEART_BEATING_CMD;
        }
        return cmd;
    }


    protected String getSyncReplyPattern()
    {
        String response = null;
        if (response == null || response.trim().length() < 1)
        {
            response = DEFAULT_HEART_BEATING_REPLY_PATTERN;
        }
        return response;
    }


    protected String getPromptPattern()
    {
        String promptPattern = null;
        if (promptPattern == null || promptPattern.isEmpty())
        {
            promptPattern = DEFAULT_PRBT_PROMPT_PATTERN;
        }
        return promptPattern;
    }


    protected String getCommandConfirmString()
    {
        return DEFAULT_CONFIRM_COMMAND;
    }


    protected String getPasswordPromptPattern()
    {
        return DEFAULT_HLR_PASSWORD_PROMPT_PATTERN;
    }


    protected String getHeartbeatPromptPattern()
    {
        String commandString = null;
        MessageMgr mmgr = new MessageMgr(getContext(), this);
        commandString = mmgr.get("PRBT_TELNET_CUSTOM_NEWLINE_FEE", "\r\n");
        if (null == commandString || commandString.isEmpty())
        {
            commandString = DEFAULT_HEART_BEATING_CMD;
        }
        return commandString;
    }


    protected String getDomainPromotPattern()
    {
        return DEFAULT_HLR_DOMAIN_PROMPT_PATTERN;
    }


    public String getCommandResponsePattern()
    {
        final String commandPattern = null;
        if (null == commandPattern || commandPattern.isEmpty())
        {
            return DEFAULT_COMMAND_RESPONSE_PATTERN;
        }
        return commandPattern;
    }


    protected String getExitCommandString()
    {
        final String commandString = null;
        if (null == commandString || commandString.isEmpty())
        {
            return DEFAULT_EXIT_COMMAND;
        }
        else
            return commandString;
    }


    /** * login Unix box * * @return */
    private boolean telnetLogin()
    {
        StringBuffer response = new StringBuffer();
        try
        {
            if (userName_ != null && (!userName_.isEmpty()))
            {
                debugMsg("Telnet Authentication required.  Trying to login...", null);
                Pattern login_pattern = Pattern.compile(DEFAULT_TELNET_LOGIN_PATTERN, Pattern.DOTALL);
                waitfor(login_pattern, response, timeout_);
                debugMsg("Sending Telnet username...", null);
                telnetConnection.send(userName_);
                response.delete(0, response.length());
                Pattern password_pattern = Pattern.compile(DEFAULT_TELNET_LOGIN_PASSWORD_PATTERN, Pattern.DOTALL);               
                waitfor(password_pattern, response, timeout_);                
                debugMsg("Sending Telnet password...", null);
                telnetConnection.send(password_);
                
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "fail at telnet login HLR box", e).log(ctx_);
            return false;
        }
        return true;
    }


    private void debugMsg(String msg, Exception e)
    {
        if (LogSupport.isDebugEnabled(ctx_))
        {
            new DebugLogMsg(this, msg, e).log(getContext());
        }
    }


    /**
     * * send command no matter log in or not * * 
     * @param request * 
     * @return * 
     * @throws IOException 
     */
    @Override
    public String send(final String command) throws IOException
    {
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, getLogHeader() + " - Greencity Telnet server: sending=>{" + command + "}", null).log(getContext());
        }
        synchronized (messageLock)
        {
            //Check for connection
            String heartbeat = getHeartbeatPromptPattern();
            try
            {
                telnetConnection.setPrompt(DEFAULT_PRBT_PROMPT_PATTERN);
                String result = telnetConnection.send(heartbeat);
            }
            catch (Exception ex)
            {
                new InfoLogMsg(this, getLogHeader() + " - Resetting the PRBT connection.  Due connection failure with previous connection", null).log(getContext());
                close() ;
                tryToOpen();
                if (! isConnected())
                {
                    throw new IOException(" Unable to connect PRBT Server");
                }
                telnetConnection.setPrompt(DEFAULT_PRBT_PROMPT_PATTERN);
            }            
            
            try
            {
                return telnetConnection.send(command);
            }
            catch (IOException ex)
            {
                close();
                throw ex;
            }
        }
    }


    private String getLogHeader()
    {
        return new String();
    }

    
    

    private void waitfor(Pattern keys, StringBuffer response, long timeout) throws IOException
    {
        if (!telnetConnection.waitfor(keys, response, timeout))
        {
            throw new InterruptedIOException("Action Response Timeout");
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "Response = " + response.toString(), null).log(getContext());
        }
    }


    private String waitForReply() throws IOException
    {
        final Pattern commandResponsePattern;
        final Pattern promptPattern;
        {
            commandResponsePattern = Pattern.compile(getCommandResponsePattern(), Pattern.DOTALL);
            promptPattern = Pattern.compile(getPromptPattern(), Pattern.DOTALL);
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
                String retVal = response.toString();
                new InfoLogMsg(this, getLogHeader() + " - PRBT: response=>{" + retVal + "}", null).log(getContext());
                if (retVal != null)
                {
                    if (commandResponsePattern.matcher(response).matches())
                    {
                        return retVal;
                    }
                    else
                    {
                        debugMsg(getLogHeader() + " - PRBT: response=>{" + retVal + "} was echo. Sending confirmation.",
                                null);
                        response.delete(0, response.length());
                        telnetConnection.send(getCommandConfirmString());
                        telnetConnection.waitfor(promptPattern, response, 100);
                        debugMsg(getLogHeader() + " - PRBT: response=>{" + retVal + "} received after confrimation.",
                                null);
                        retVal = response.toString();
                        return retVal;
                    }
                }
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
    protected boolean loggedIn = false;
    protected boolean connected_ = false;
    private String portNum_;
    private String hostName_;
    private String userName_;
    private String password_;
    private long timeout_;
    private Context ctx_;
    private Object messageLock = new Object();


}
