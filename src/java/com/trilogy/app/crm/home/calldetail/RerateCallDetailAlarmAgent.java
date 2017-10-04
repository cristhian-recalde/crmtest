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
package com.trilogy.app.crm.home.calldetail;

import java.util.Calendar;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bean.RerateAlarmConfig;
import com.trilogy.app.crm.bean.RerateCallDetailAlarmTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetail;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailHome;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailXInfo;

/**
 * An agent that generates an alarm if there are unreceived call details
 * extracted before a configurable amount of time.
 * 
 * @author dannyng
 *
 */
public class RerateCallDetailAlarmAgent implements ContextAgent
{

    public RerateCallDetailAlarmAgent()
    {
        super();
    }


    public void execute(Context ctx) throws AgentException
    {
        RerateAlarmConfig config = (RerateAlarmConfig) ctx.get(RerateAlarmConfig.class);
        
        Calendar cal = Calendar.getInstance();
        
        // Get the number of hours or days to check
        if (config.getTimeUnit() == RerateCallDetailAlarmTypeEnum.DAYS)
        {
            cal.add(Calendar.DATE, -1 * config.getNumDays());
        }
        else if (config.getTimeUnit() == RerateCallDetailAlarmTypeEnum.HOURS)
        {
            cal.add(Calendar.HOUR_OF_DAY, -1 * config.getNumHours());
        }
        
        And condition = new And();
        condition.add(new EQ(RerateCallDetailXInfo.RECEIVED, False.instance()));
        condition.add(new LTE(RerateCallDetailXInfo.EXTRACT_DATE, cal.getTime()));
        
        Home rcdHome = (Home) ctx.get(RerateCallDetailHome.class);
        try
        {
            /*
             *  TODO figure out if we suppose to generate alarm
             *  per unrecieved call detail or just generate
             *  one alarm if there are unreceived call details
             */
            
            RerateCallDetail rcd = (RerateCallDetail) rcdHome.find(ctx, condition);
            
            // There is at least one call detail that was
            // extracted X hours/days ago, generate an alarm
            if (rcd != null)
            {
                // Generate alarm
                new EntryLogMsg(12742 , this, "execute", null, new String[] {"execute"}, null).log(ctx);
            }
        }
        catch (HomeInternalException e)
        {
            e.printStackTrace();
        }
        catch (HomeException e)
        {
            e.printStackTrace();
        }
        
    }
}
