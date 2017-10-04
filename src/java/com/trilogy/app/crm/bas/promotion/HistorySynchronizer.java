/*
 * Created on Apr 1, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.bas.promotion;

import java.util.Date;

import com.trilogy.app.crm.bas.promotion.home.SyncHistoryCmd;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryHome;
import com.trilogy.app.crm.bean.PeriodEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Sychronize start date, end date and generation date in promotion history with the same spid and period 
 * 
 * @author kwong
 *
 */
public class HistorySynchronizer implements ContextAgent
{

    private int spid_;
    private PeriodEnum period_;
    
	public HistorySynchronizer(int spid, PeriodEnum period) 
	{
        spid_ = spid;
        period_ = period;
	}
    
    public void execute(Context ctx) throws AgentException
    {
        try{
            Date startDate;
            Date endDate;
            
            if (period_.equals(PeriodEnum.WEEKLY))
            {  
                endDate = CalendarSupportHelper.get(ctx).findFirstDayOfWeek(new Date());
                startDate = CalendarSupportHelper.get(ctx).findPreviousFirstDayOfWeek(endDate);
            }
            else if (period_.equals(PeriodEnum.MONTHLY))
            {          
                endDate = CalendarSupportHelper.get(ctx).findFirstDayOfMonth(new Date());
                startDate = CalendarSupportHelper.get(ctx).findPreviousFirstDayOfMonth(endDate);
            }
            else 
            {
                throw new IllegalStateException("Unknown usage period choice:"+period_);  
            }
                 
            
            Home historyHome = (Home) ctx.get(HandsetPromotionHistoryHome.class);
            historyHome.cmd(ctx,new SyncHistoryCmd(startDate, endDate,
                   "spid = " + spid_ + " and period = " +period_.getIndex()));
        }
        catch(HomeException e)
        {
            new MajorLogMsg(this, "Fail to sync promotion history table:"+e.getMessage(), e).log(ctx);
            throw new AgentException(e);
        }
    }
    
    
    public void setSpid(int spid)
    {
        spid_ = spid;
    }

    public int getSpid()
    {
        return spid_;
    }

}
