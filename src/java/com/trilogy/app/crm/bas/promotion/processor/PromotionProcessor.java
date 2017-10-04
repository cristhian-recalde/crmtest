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
package com.trilogy.app.crm.bas.promotion.processor;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bas.promotion.summary.PromotionCalculation;
import com.trilogy.app.crm.bas.promotion.summary.SummaryBundle;
import com.trilogy.app.crm.bean.HandsetPromotionHistory;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryHome;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryID;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryTransientHome;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryXInfo;
import com.trilogy.app.crm.bean.PeriodEnum;
import com.trilogy.app.crm.bean.UsageReport;
import com.trilogy.app.crm.bean.core.CallDetailSummary;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Process usage summary with give report handler
 * 
 * @author kwong
 *
 */
public class PromotionProcessor 
    extends ContextAwareSupport
{
    
    private SummaryBundle    summaryBundle_;
    private ReportHandler    handler_;
    private Home    historyHome_;


	public PromotionProcessor(Context ctx, SummaryBundle summaryBundle, ReportHandler handler)
        throws HomeException
    {
       summaryBundle_ = summaryBundle;
	   handler_ = handler;       
       setContext(ctx);
       // cache the history table
       cacheHistory(ctx, summaryBundle);
       
    }

    /**
     * Generate usage report with PromotionCalculation
     * and process usage reports with given report handler
     * The summary has to be group by Msisdn
     */
    public void execute() throws HomeException, HandlerException
    {
        
        String              curMsisdn    = null;
        CallDetailSummary   summary      = null;
        PromotionCalculation calculation = null;
        
        printDebugLog(); 
                           
        for (Iterator iter = summaryBundle_.getCollection().iterator(); iter.hasNext();)
        {
            summary = (CallDetailSummary) iter.next();
            if (curMsisdn != null)
            {
                // same msisdn --> same usage report;
                if (summary.getChargedMSISDN().equals(curMsisdn) )
                {
                    calculation.add(summary);
                    continue;      
                }
                
				processReport(calculation);
            }
            calculation = new PromotionCalculation(summary.getSpid(),
                                 summary.getSubscriberID(), summary.getChargedMSISDN(),
                                    summary.getBAN(), summaryBundle_.getStartDate(),
                                    summaryBundle_.getEndDate(), summaryBundle_.getPeriod());
            calculation.add(summary);
            curMsisdn = summary.getChargedMSISDN();
        } 
        // generating the last report
        if (calculation != null)
        {
            processReport(calculation);
        }
    }

    
    /**
     * Only create usage report for those not already exist in the history table
     * 
     * @param calculation
     * @throws HandlerException
     * @throws HomeException
     */
    protected void processReport(PromotionCalculation calculation)
        throws HandlerException, HomeException
    {
        if (!existInHistory(calculation.getSubscriberID(), calculation.getPeriod(), calculation.getEndDate()))
        {         
            final UsageReport report = calculation.generateReport();
            // process report;
            handler_.setReport(report);
            handler_.process();
        }
    }
    
    
    /**
     * If history table contains a more recent history than the one is being generated. 
     * Generation should be stopped to avoid duplicated usage report
     * 
     * @param subID
     * @param period
     * @param endDate
     * @return
     * @throws HomeException
     */
    protected boolean existInHistory(String subID, PeriodEnum period, Date endDate)
        throws HomeException
    {
        HandsetPromotionHistory history = (HandsetPromotionHistory)getHistory()
                            .find(getContext(),new HandsetPromotionHistoryID(subID, period));
        if (history == null)
        {
            return false;
        }
        
		return history.getEndDate().after(endDate);
    }


    private void cacheHistory(Context ctx, SummaryBundle summaryBundle)
        throws HomeException
    {      
        Home cacheHome = new HandsetPromotionHistoryTransientHome(ctx);
        Home historyHome = (Home)ctx.get(HandsetPromotionHistoryHome.class);
        
        Iterator it = summaryBundle.getCollection().iterator();
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "caching history ... ",null).log(ctx);
        } 

        if (it.hasNext()){
            int spid = ((CallDetailSummary)it.next()).getSpid();
            
            Collection history = historyHome
               .where(ctx, new EQ(HandsetPromotionHistoryXInfo.SPID, Integer.valueOf(spid)))
               .where(ctx, new EQ(HandsetPromotionHistoryXInfo.PERIOD, summaryBundle.getPeriod()))
               .selectAll(ctx);
            
    //        selectAll();
            for (Iterator iter = history.iterator(); iter.hasNext();)
            {
                cacheHome.create(ctx,iter.next());
            }
        }
        historyHome_ = cacheHome;      
    }
    
    protected Home getHistory()
    {
        return historyHome_;
    }
    
    
    private void printDebugLog()
    {
        if (LogSupport.isDebugEnabled(getContext()))
        {    
            new DebugLogMsg(this, "Generating "+ summaryBundle_.getCollection().size()+" " 
                                +summaryBundle_.getPeriod()+" reports", null).log(getContext());
            for (Iterator iter = summaryBundle_.getCollection().iterator(); iter.hasNext();)
            {
                  new DebugLogMsg(this, "summary:"+iter.next().toString(), null).log(getContext());
            }
        }
    }
    
}
