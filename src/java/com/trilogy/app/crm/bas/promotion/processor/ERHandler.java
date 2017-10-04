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

import java.util.Date;

import com.trilogy.app.crm.bean.HandsetPromotionHistory;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryHome;
import com.trilogy.app.crm.bean.HandsetPromotionHistoryID;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Generate ER per usage report and update promotion history
 * 
 * @author kwong
 *
 */
public class ERHandler 
    extends ReportHandler
{
    
    Home historyHome_;
    
    public ERHandler(Context ctx){
        setContext(ctx) ;     
        historyHome_ = (Home)getContext().get(HandsetPromotionHistoryHome.class);
    }
    
    public ERHandler(Context ctx, ReportHandler handler)
    {
        setDelegate(handler);
        setContext(ctx);
        historyHome_ = (Home)getContext().get(HandsetPromotionHistoryHome.class);
    }
    
    protected void handling() 
        throws HandlerException, HomeException
    {
        if (LogSupport.isDebugEnabled(getContext()))
        {   
            new DebugLogMsg(this, "generating ER from " +  getReport(), null).log(getContext());
        }       
        //TODO: to speed up the process, cache the history locally; but require synchronization         
        if (getReport() == null)
        {
            new MinorLogMsg(this, "No Report to process!", null).log(getContext());
        }
        
        
        try{
               
            HandsetPromotionHistory history = (HandsetPromotionHistory) historyHome_.find(getContext(),
                                                new HandsetPromotionHistoryID(getReport().getSubscriberID(), 
                                                                                getReport().getUsagePeriod()));
            if (history == null)
            {
                UsageER.log(getContext(), getReport());
                if (LogSupport.isDebugEnabled(getContext()))
                {   
                    new DebugLogMsg(this, "creating..", null).log(getContext()); 
                }
                historyHome_.create(getContext(),createHistory());
            }
            else if (getReport().getEndDate().after(history.getEndDate())){                                                                  
                UsageER.log(getContext(), getReport());
                if (LogSupport.isDebugEnabled(getContext()))
                {   
                    new DebugLogMsg(this, "updating..", null).log(getContext()); 
                }
                historyHome_.store(getContext(),createHistory());
            }
            else 
            {
                new InfoLogMsg(this, "ER has already generated from " +  getReport(), null).log(getContext());
                //do nothing: ER report already exists
            }
            
        }catch (HomeException he)
        {
            new MinorLogMsg(this, "Database failure in generating ER report for subscriber:"
                                           + getReport().getSubscriberID()+" with period:"
                                           + getReport().getUsagePeriod()+ " Home Exception:"+he.getMessage(), he).log(getContext());
            throw he;
        }catch (Exception e)
        {
            new MajorLogMsg(this, "Fail generating ER report for subscriber:"
                                + getReport().getSubscriberID()+" with period:"
                                + getReport().getUsagePeriod()+ " Exception:"+e.getMessage(), e).log(getContext());

            throw new HandlerException(e);
        }
        
    }
    
    protected HandsetPromotionHistory createHistory()
    {
        HandsetPromotionHistory history = new HandsetPromotionHistory();
        history.setSubscriber(getReport().getSubscriberID());
        history.setPeriod(getReport().getUsagePeriod());
        history.setSpid(getReport().getSpid());
        history.setStartDate(getReport().getStartDate());
        history.setEndDate(getReport().getEndDate());
        history.setGeneration(new Date());
        if (LogSupport.isDebugEnabled(getContext()))
        {   
            new DebugLogMsg(this, "history: " +  history, null).log(getContext());        
        }
        return history;
    }
    
}
