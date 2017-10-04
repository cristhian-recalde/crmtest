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
package com.trilogy.app.crm.poller;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.service.poller.nbio.PollerLogger;
import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.MountStatusEnum;

/**
 * An instance of this is given to every Poller class. This Logger class will receive all the logging requests from the
 * poller.
 *
 * @author psperneac
 */
public class PollerLoggerImpl extends ContextAwareSupport implements PollerLogger
{
    protected MountStatusEnum mountStatus = MountStatusEnum.NA;

    /**
     * Constructor
     *
     * @param ctx
     *            the context used by the Logger to find the system loggers
     */
    public PollerLoggerImpl(Context ctx)
    {
        setContext(ctx);
    }

    /**
     * Parses the errorType and informs the outside world of what happens inside the poller.
     *
     * @param errorType
     *            the error code
     * @param module
     *            the module ??
     * @param component
     *            the component ??
     * @param msgs
     *            other messages passed by the poller. Depending on the error type some of theses messages have meaning.
     * @param t
     *            an exception, if any was generated
     * 
     * @see com.redknee.service.poller.nbio.PollerLogger#error(int, java.lang.String, java.lang.String,
     *      java.lang.Object[], java.lang.Throwable)
     */
    public void error(int errorType, String module, String component, Object[] msgs, Throwable t)
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_POLLER_MOUNT_ERROR, 1).log(getContext());

        // REVIEW(traceability): errors should have different severity instead of all minor.
        // Should raise SNMP alarm is applicable (e.g. ERROR_POLLER_INIT_FAILED)
        switch (errorType)
        {
            case ERROR_POLLER_INIT_FAILED:
                Logger.minor(getContext(), this, "Poller Init Failed" + getMessages(msgs), t);
                break;
            case ERROR_HANDLER_NOT_LOADED:
                Logger.minor(getContext(), this, "Handler not loaded" + getMessages(msgs), t);
                break;
            case ERROR_TIMESTAMP_PARSE:
                Logger.minor(getContext(), this, "Timestamp Parse Error" + getMessages(msgs), t);
                break;
            case ERROR_DATE_FORMAT:
                Logger.minor(getContext(), this, "Date Format Error" + getMessages(msgs), t);
                break;
            case ERROR_ER_MALFORM:
                Logger.minor(getContext(), this, "Malformed ER" + getMessages(msgs), t);
                break;
            case ERROR_ER_FILE_NOT_OPENED:
                Logger.minor(getContext(), this, "ER File not opened" + getMessages(msgs), t);
                break;
            case ERROR_LAST_READ_POSITION_NOT_FOUND:
                Logger.minor(getContext(), this, "Last Read Position not found" + getMessages(msgs), t);
                break;
            case ERROR_ER_FILE_NOT_CLOSED:
                Logger.minor(getContext(), this, "ER File not closed" + getMessages(msgs), t);
                break;
            case ERROR_FILE_NOT_FILTERED:
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(getContext(), this, "Cannot filter " + ((String) msgs[0])
                            + " in directory <" + ((String) msgs[1]) + ">", t);
                }
                break;
            case ERROR_FILE_NOT_INTERRUPTED:
                Logger.minor(getContext(), this, "File not interrupted" + getMessages(msgs), t);
                break;
            case ERROR_LAST_BYTE_FILE_NOT_FOUND:
                Logger.minor(getContext(), this, "Last byte file was not found" + getMessages(msgs), t);
                break;
            case ERROR_LAST_READ_POSITION_FAILED_COPIED:
                Logger.minor(getContext(), this, "Last read position failed copied" + getMessages(msgs), t);
                break;
            case ERROR_LAST_READ_POSITION_FAILED_WRITTEN:
                Logger.minor(getContext(), this, "Last read position failed written" + getMessages(msgs), t);
                break;
            case ERROR_POLLING_FILE_EXCEPTION:
                Logger.minor(getContext(), this, "Polling file exception" + getMessages(msgs), t);
                break;
            case ERROR_NFS_MOUNT_DOWN:
                doMountDown(module, component, msgs, t);
                break;
            case ERROR_NFS_MOUNT_UP:
                doMountUp(module, component, msgs, t);
                break;
        }

    }

    /**
     * This is processing done if the NFS mount went up
     * 
     * @param module
     * @param component
     * @param msgs
     * @param t
     */
    private void doMountUp(String module, String component, Object[] msgs, Throwable t)
    {
        Logger.minor(getContext(), this, "NFS mount up" + getMessages(msgs), t);

        // trigger if is already down, and coming up.
        if (mountStatus.getIndex() == MountStatusEnum.DOWN_INDEX)
        {
            new EntryLogMsg(10389, this, "", "", new String[] {getMessages(msgs)}, t).log(getContext());
        }

        mountStatus = MountStatusEnum.UP;
    }

    /**
     * This is processing done if the NFS mount went down. When the nfs link goes down (ER files cannot be found) it
     * generates the 10388 SNMP trap and an OM. The trap is generated only once.
     * 
     * @param module
     * @param component
     * @param msgs
     * @param t
     */
    private void doMountDown(String module, String component, Object[] msgs, Throwable t)
    {
        Logger.minor(getContext(), this, "NFS mount down" + getMessages(msgs), t);

        // trigger if it's not set or up and going down.
        if (mountStatus.getIndex() != MountStatusEnum.DOWN_INDEX)
        {
            new EntryLogMsg(10388, this, "", "", new String[] {getMessages(msgs)}, t).log(getContext());

            new OMLogMsg(Common.OM_MODULE, Common.OM_POLLER_MOUNT_CONN_LOST, 1).log(getContext());
        }

        mountStatus = MountStatusEnum.DOWN;
    }

    /**
     * Turns an array of messages into a single string (concatenates). Uses a StringBuilder for better memory management.
     * Uses a single space as a delimiter.
     *
     * @param messages
     *            the array of objects (messages) * @return
     * @return the concatenated string.
     */
    protected String getMessages(Object[] messages)
    {
        if (messages == null || messages.length == 0)
        {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (int i = 0; i < messages.length; i++)
        {
            if (messages[i] != null)
            {
                if (i != 0)
                {
                    buf.append(",");
                }
                buf.append(messages[i].toString());
            }
        }
        buf.append("]");

        return buf.toString();
    }

    /**
     * @see com.redknee.service.poller.nbio.PollerLogger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(String msg, Throwable th)
    {
        if (Logger.isDebugEnabled())
        {
            Logger.debug(getContext(), this, msg, th);
        }
    }

    /**
     * @see com.redknee.service.poller.nbio.PollerLogger#info(java.lang.String, java.lang.Throwable)
     */
    public void info(String msg, Throwable th)
    {
        if (Logger.isInfoEnabled())
        {
            Logger.info(getContext(), this, msg, th);
        }
    }

    /**
     * @see com.redknee.service.poller.nbio.PollerLogger#minor(java.lang.String, java.lang.Throwable)
     */
    public void minor(String msg, Throwable th)
    {
        Logger.minor(getContext(), this, msg, th);
    }

    /**
     * @see com.redknee.service.poller.nbio.PollerLogger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return Logger.isDebugEnabled();
    }

    /**
     * @see com.redknee.service.poller.nbio.PollerLogger#setDebugEnabled(boolean)
     */
    public void setDebugEnabled(boolean debug)
    {
        LogSupport.setSeverityThreshold(getContext(), debug ? SeverityEnum.DEBUG : SeverityEnum.INFO);
    }

    /**
     * This is called whenever an ER file is opened for parsing. (start of processing)
     *
     * @see com.redknee.service.poller.nbio.PollerLogger#erFileOpenedLog(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void erFileOpenedLog(String arg0, String arg1, String arg2, String arg3)
    {
        if (Logger.isDebugEnabled())
        {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("hh:mm:ss:SS");
            String date = formatter.format(new java.util.Date());

            Logger.debug(getContext(), this, "Opening ER file: " + arg0 + arg1 + " " + arg2 + " " + arg3 + " " + date);
        }
    }

    /**
     * This is called whenever an ER file is closed. (end of processing)
     *
     * @see com.redknee.service.poller.nbio.PollerLogger#erFileClosedLog(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, long, long, long)
     */
    public void erFileClosedLog(String arg0, String arg1, String arg2, String arg3, long arg4, long arg5, long arg6)
    {
        if (Logger.isDebugEnabled())
        {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("hh:mm:ss:SS");
            String date = formatter.format(new java.util.Date());
            Logger.debug(getContext(), this, "Closing ER file: " + arg0 + arg1 + " " + arg2 + " " + arg3 + " " + date
                    + " " + arg4 + " " + arg5 + " " + arg6);
        }
    }
}
