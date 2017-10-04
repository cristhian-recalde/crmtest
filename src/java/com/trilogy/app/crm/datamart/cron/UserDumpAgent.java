/*
 * Created on Jul 6, 2005
 *
 * Created for DataMart mining.
 */
package com.trilogy.app.crm.datamart.cron;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.AndHome;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * UserDumpAgent dumps all relevant User information from CRM.
 * Datamart requires all of the currently used User ID / Names in the system
 *
 * @author victor.stratan@redknee.com
 * @author ali
 */
public class UserDumpAgent implements ContextAgent
{
    public static final String DATE_FORMAT_STRING = "yyyyMMdd";
    private static final char delimiter_ = ',';
    private static int sequence = 1;

    public void execute(Context ctx) throws AgentException
    {
        Home home = getUserHome(ctx);
        if (home == null)
        {
            new MinorLogMsg(this, "Failed to write user to User Dump File", null).log(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Failed to write user to User Dump File: Unable to locate local UserHome",
                        null).log(ctx);
            }
            return;
        }

        try
        {
            home.forEach(ctx, new UserDumpVisitor(this));
            sequence++;
        }
        catch (Throwable t)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Failed to write user to User Dump File: " + t.getMessage(), t).log(ctx);
            }
        }
    }

    /**
     * Get UserHome from the context. Walk the gelegates chain and try to obtail the local home behind the
     * RMIClusteredHome
     *
     * @return local UserHome
     */
    private Home getUserHome(Context ctx)
    {
        Home home = (Home) ctx.get(UserHome.class);
        while (home != null)
        {
            if (home instanceof RMIClusteredHome)
            {
                home = ((HomeProxy) home).getDelegate();
                if (home instanceof AndHome)
                {
                    home = ((HomeProxy) home).getDelegate();
                }
                else
                {
                    home = null;
                }
                break;
            }
            else if (home instanceof HomeProxy)
            {
                home = ((HomeProxy) home).getDelegate();
            }
            else
            {
                home = null;
            }
        }

        return home;
    }

    /**
     * Return string of fields in CSV format
     *
     * @param fields
     */
    protected String format(String[] fields)
    {
        StringBuilder msgBuf = new StringBuilder();

        for (int i = 0; i < fields.length; i++)
        {
            if (i != 0)
            {
                msgBuf.append(delimiter_);
            }
            msgBuf.append(fields[i]);
        }
        msgBuf.append("\n");

        return msgBuf.toString();
    }

    /**
     * @param msg
     */
    protected synchronized void writeToFile(Context ctx, String msg)
    {
        try
        {
            getUserWriter().print(msg);
            getUserWriter().flush();
        }
        catch (Throwable tt)
        {
            //Can't write to log file
            new InfoLogMsg(this, "Unable to write to Subscriber log file: " + msg, tt).log(ctx);
        }
    }

    /**
     * @return Returns the userWriter_.
     */
    protected PrintWriter getUserWriter()
    {
        return userWriter_;
    }

    /**
     * @param userWriter The userWriter_ to set.
     */
    protected void setUserWriter(PrintWriter userWriter)
    {
        this.userWriter_ = userWriter;
    }

    /**
     * The Agent is set to dump every night.  A new file is created every time
     * this agent is run.
     *
     * @param ctx
     * @throws IOException
     */
    protected void initUserWriter(Context ctx) throws IOException
    {
        GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
        FileWriter userFileWr = null;
        PrintWriter userPrintWr = null;

        final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_STRING);

        String date = dateFormatter.format(new Date());

        String path = config.getDataMartDumpDir();

        File dir = new File(path);

        if (!dir.exists())
        {
            dir.mkdir();
        }

        File subFile = new File(path + File.separator + "CRMUser" + date + "." + sequence);

        if (!subFile.exists())
        {
            subFile.createNewFile();
        }

        userFileWr = new FileWriter(subFile, true);
        userPrintWr = new PrintWriter(new BufferedWriter(userFileWr));
        setUserWriter(userPrintWr);
    }

    protected void closeUserWriter()
    {
        getUserWriter().close();
    }

    private PrintWriter userWriter_;
}
