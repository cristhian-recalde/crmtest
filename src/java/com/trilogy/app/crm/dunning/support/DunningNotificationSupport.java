/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.support;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.DunningNotificationConfig;
import com.trilogy.app.crm.bean.DunningNotificationConfigHome;
import com.trilogy.app.crm.notification.generator.SimpleJasperMessageGenerator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * 
 * @author ptayde
 * @version 10.2.6
 */
public class DunningNotificationSupport {

	
    public static String getDunningNoticeFilePath(Context ctx, int spid, String accountCategory, String ban)
    {
        String path = null;
        try
        {
            Home home = (Home) ctx.get(DunningNotificationConfigHome.class);
            DunningNotificationConfig config = (DunningNotificationConfig) home.find(ctx, spid);
            if (config != null)
            {
                String noticeDate = yyyy_MM_dd.format(new Date());
                String baseDir = config.getArchiveDirectory() + "/" + SPID_ARCHIVE_PREFIX + spid + "/"
                        + DUNNING_NOT_REMINDER_ARCHIVE_PREFIX + noticeDate;
                StringBuilder pathBuilder = new StringBuilder();
                pathBuilder.append(baseDir);
                pathBuilder.append("/" + accountCategory);
                pathBuilder.append("/" + config.getFileNamePrefix() + ban + "-" + noticeDate + ".pdf");
                path = pathBuilder.toString();
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SimpleJasperMessageGenerator.class.getName(),
                            "Returning path for Dunning Notification Reminder, account : " + ban + ", path : " + path);
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SimpleJasperMessageGenerator.class.getName(), "Dunning Notification Reminder Config not present for spid : " + spid);
                }
            }
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, SimpleJasperMessageGenerator.class.getName(),
                    "Exception while fetching Payment Reminder Config for spid : " + spid, e);
        }
        return path;
    }
    
    private static final String SPID_ARCHIVE_PREFIX = "SPID";
    private static final String DUNNING_NOT_REMINDER_ARCHIVE_PREFIX = "DR";
    private static final SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyyMMdd");
	
	
}
