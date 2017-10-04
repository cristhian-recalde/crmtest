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
package com.trilogy.app.crm.writeoff;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.WriteOffConfig;
import com.trilogy.app.crm.bean.WriteOffConfigHome;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
public class WriteOffCronAgent implements ContextAgent
{

    public static String DATE_FORMAT_STRING = "yyyyMMdd";


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.
     * xhome.context.Context)
     */
    @SuppressWarnings("unchecked")
    public void execute(Context ctx) throws AgentException
    {
        String date = getDateString(ctx);
        Home home = (Home) ctx.get(WriteOffConfigHome.class);
        try
        {
            Collection<WriteOffConfig> configs = (Collection<WriteOffConfig>) home.selectAll(ctx);
            if (configs != null)
            {
                for (WriteOffConfig config : configs)
                {
                    new WriteOffAgent(ctx, date, config).execute(ctx);
                }
            }
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Exception encounted in Write-off process, process failed.", e);
        }
    }


    private String getDateString(Context ctx)
    {
        AgentEntry entry = (AgentEntry) ctx.get(AgentEntry.class);
        String strDate = entry.getTask().getParam0();
        if (strDate != null)
            strDate = strDate.trim();
        if (strDate == null || strDate.length() == 0)
        {
            Date date = WriteOffSupport.getDateWithNoTimeOfDay(new Date());
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING);
            strDate = format.format(date);
        }
        return strDate;
    }
}
