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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bas.promotion.processor.ERHandler;
import com.trilogy.app.crm.bas.promotion.processor.PromotionProcessor;
import com.trilogy.app.crm.bas.promotion.summary.SummaryBundle;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author kwong
 *
 */
public class ReportCronAgent 
    implements ContextAgent 
{
    /**
     * Each Spid will generate an ER per usage period and msisdn
     */
	public void execute(Context context) throws AgentException 
    {
        try{     
            
            if (LogSupport.isDebugEnabled(context))
            {       
                new DebugLogMsg(this, "[ReportCronAgent] Starting usage report process"
                             , null).log(context);  
            }
            final Collection spids =
                ((Home)context.get(CRMSpidHome.class)).selectAll(context);
            
            final Iterator iter = spids.iterator();

            Collection summaries = null;
            
            //for each spid
            while (iter.hasNext())
            {
                final CRMSpid spid = (CRMSpid)iter.next(); 

                Collection promotionBys = PromotionFactory.generateBySpid(spid);  
                // generate usage report summary for desired spid
                summaries = new PromotionAgent(promotionBys).summarize();
            
                if (summaries == null)
                {
                    throw new PromotionException("Operation Error: collection summaries is null!");
                }
                // for each usage period and spid
                for (Iterator sumIter = summaries.iterator(); sumIter.hasNext();)
                {
                    SummaryBundle sbundle = (SummaryBundle)sumIter.next();
                    new PromotionProcessor(context, sbundle, 
                        new ERHandler(context))
                                .execute();                    
                }  
                updateHistory(context, spid); 
         
            }
  
//          synchronizeHistory(context, spids); 
        }
        catch (HomeException e)
        {   
            new EntryLogMsg(10950, this, "",  null,
                     new String[] { e.getMessage() } , e).log(context);       
        }
        catch (Exception e)
        {
            throw new AgentException(e);
        }
    }
    
    protected void updateHistory(Context context, final CRMSpid spid) throws HomeException
    {   
        
        Home historyHome = (Home)context.get(PromotionCronTaskHistoryHome.class);
        Date startDate;
        Date endDate;
        
        if (spid.isWeeklyPromotion() &&
                CalendarSupportHelper.get(context).isDayOfWeek(spid.getWeeklyPromotionDay().getIndex())) 
        {   
            endDate =   CalendarSupportHelper.get(context).findFirstDayOfWeek(new Date());
            startDate = CalendarSupportHelper.get(context).findPreviousFirstDayOfWeek(new Date());
            
            PromotionCronTaskHistory history = createHistory(context, spid.getSpid(), 
                                                            PeriodEnum.WEEKLY,
                                                            startDate,
                                                            endDate                                                    
                                                            );
            try{
                historyHome.create(context,history);
            }
            catch(HomeException e)
            {
                historyHome.store(context,history);                       
            }
           
        }
        if (spid.isMonthlyPromotion() &&
                CalendarSupportHelper.get(context).isDayOfMonth(spid.getMonthlyPromotionDay())) 
        {    
            endDate =   CalendarSupportHelper.get(context).findFirstDayOfMonth(new Date());
            startDate = CalendarSupportHelper.get(context).findPreviousFirstDayOfMonth(new Date());
            
            PromotionCronTaskHistory history = createHistory(context, spid.getSpid(), 
                                                            PeriodEnum.MONTHLY,
                                                            startDate,
                                                            endDate                                                    
                                                            );
            try{
                historyHome.create(context,history);
            }
            catch(HomeException e)
            {
                historyHome.store(context,history);                       
            }
        }
    }
    
    protected PromotionCronTaskHistory createHistory(Context ctx, int spid, PeriodEnum period, Date start, Date end)
       {
           PromotionCronTaskHistory history = new PromotionCronTaskHistory();
           history.setPeriod(period);
           history.setSpid(spid);
           history.setStartDate(start);
           history.setEndDate(end);
           history.setGeneration(new Date());
           if (LogSupport.isDebugEnabled(ctx))
           {   
               new DebugLogMsg(this, "history: " +  history, null).log(ctx);        
           }
           return history;
       }
    
    
    protected void synchronizeHistory(Context context, Collection spids) throws AgentException
    {
        final Iterator iter = spids.iterator();

        while (iter.hasNext()) 
        {
            final CRMSpid spid = (CRMSpid)iter.next();
            
            if (spid.isWeeklyPromotion() && CalendarSupportHelper.get(context).isDayOfWeek(spid.getWeeklyPromotionDay().getIndex())) 
            {   
            	new HistorySynchronizer(spid.getSpid(), PeriodEnum.WEEKLY).execute(context);
            }
            if (spid.isMonthlyPromotion() && CalendarSupportHelper.get(context).isDayOfMonth(spid.getMonthlyPromotionDay())) 
            {    
            	new HistorySynchronizer(spid.getSpid(), PeriodEnum.MONTHLY).execute(context);
            }
        
        }
        
    }
}
