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

import java.util.*;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author kwong
 *
 */
public class PromotionFactory 
{

    static private Context context_;
    static public String MODULE = "PromotionFactory";
    
    static public void initialize(Context ctx)
    {
        context_ = ctx;    
    } 
    
    /**
     * Generate Collection of Promotions based on spid and usage period
     * if isCronTask, spid specific generation day will determine if promotion is generated.
     * 
     * @param spid
     * @param isCronTask
     * @return collection, Collection of Promotion
     */
    static public Collection generateBySpid(int spid, Date genDate, boolean isCronTask)
        throws HomeException
    {                 
        return generateBySpid(findSPIDProperty(spid), genDate, isCronTask);
                    
    }

    /**
     * Generate Collection of Promotions based on spid and usage period
     * if isCronTask, spid specific generation day will determine if promotion is generated.
     * 
     * @param CRMSpid
     * @param isCronTask
     * @return collection, Collection of Promotion
     */
    static public Collection generateBySpid(CRMSpid property, Date genDate, boolean isCronTask)
        throws HomeException
    {
              
         PromotionSupport composites = new PromotionSupport();

         if (property.isWeeklyPromotion() &&
            (!isCronTask || CalendarSupportHelper.get(getContext()).isDayOfWeek(property.getWeeklyPromotionDay().getIndex())) )
         {              
             Date startDate = findPromotionStartDate(property, PeriodEnum.WEEKLY);
             if (startDate == null)
             {
                 new InfoLogMsg("Can't find a start time for promotion generation", null, null).log(getContext());
                 return new ArrayList();
             }
             if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(MODULE, "[PromotionFactory] Starting usage report process with weekly promotion on spid " + property.getSpid(), 
                                 null).log(getContext()); 
             }
             composites.add(generateByWeek(startDate, genDate,
                     new PromotionByNonZeroCharges(
                        new PromotionBySpid(property.getSpid(),             
                            new PromotionSearch(getContext())))));
                            
         }         
         if (property.isMonthlyPromotion() &&
            (!isCronTask || CalendarSupportHelper.get(getContext()).isDayOfMonth(property.getMonthlyPromotionDay())) )
         {            
             Date startDate = findPromotionStartDate(property, PeriodEnum.MONTHLY);
             if (startDate == null)
             {
                 new InfoLogMsg("Can't find a start time for promotion generation", null, null).log(getContext());
                 return new ArrayList();
             }
             if (LogSupport.isDebugEnabled(getContext()))
             {           
                new DebugLogMsg(MODULE, "[PromotionFactory] Starting usage report process with monthly promotion on spid "+ property.getSpid(), 
                                null).log(getContext()); 
             }
             composites.add(generateByMonth(startDate, genDate,
                    new PromotionByNonZeroCharges(
                        new PromotionBySpid(property.getSpid(), 
                            new PromotionSearch(getContext())))));
                            
         }
         
         return composites.getPromotions();
              
    }

    

    /**
     * Generate Collection of Promotions based on spid and usage period
     * Default all spid reports will be generated upto the current day
     * 
     * @param spid
     * @return Collection, collection of promotion
     * @throws HomeException
     */
    static public Collection generateBySpid(CRMSpid spid)
        throws HomeException
    {
        return generateBySpid(spid, new Date(), true);
    }

    /**
     * Generate Collection of Promotions based of subscriber id and usage period
     * 
     * @param subID
     * @return collection, collection of promotion
     */
    static public Collection generateBySubID(String subID, Date genDate)
        throws HomeException 
    {        
        PromotionSupport composites = new PromotionSupport();
        
        // find start date of this usage period
        CRMSpid property = null;

        property = findSPIDProperty(lookupSpidBySubscriberID(subID));
 
     
        if (property.isWeeklyPromotion())
        {
            Date startDate = findPromotionStartDate(subID, PeriodEnum.WEEKLY);
            if (startDate == null)
            {
                new InfoLogMsg("Can't find a start time for promotion generation", null, null).log(getContext());
                return new ArrayList();
            }
            composites.add(generateByWeek(startDate, genDate,
                new PromotionByNonZeroCharges(
                    new PromotionBySubID(subID,     
                        new PromotionSearch(getContext())))));
        }
        if (property.isMonthlyPromotion())
        {
            Date startDate = findPromotionStartDate(subID, PeriodEnum.MONTHLY);
            if (startDate == null)
            {
                new InfoLogMsg("Can't find a start time for promotion generation", null, null).log(getContext());
                return new ArrayList();
            }
            composites.add(generateByMonth(startDate, genDate,
                new PromotionByNonZeroCharges(
                    new PromotionBySubID(subID,    
                        new PromotionSearch(getContext())))));
        }
                
        return composites.getPromotions();
        
    }
    
    
    static public int lookupSpidBySubscriberID(String subID)
        throws HomeException
    {     
        try{
        
            Home subHome = (Home) getContext().get(SubscriberHome.class);
            Subscriber sub = (Subscriber)subHome.find(getContext(),subID);
            return sub.getSpid();
        }catch(NullPointerException e)
        {
            throw new IllegalArgumentException("Subscriber "+subID+ " not found!");
        }
    }
    
    
    /**
     * 
     * @param startDate
     * @param promotion
     * @return Collection, collection of promotion to be generated
     */
    static public Collection generateByWeek(final Date startDate, final Date endDate, final PromotionBy promotion)
    {
        PromotionSupport composites = new PromotionSupport();
    
        for (Date start = startDate, end = CalendarSupportHelper.get().findNextFirstDayOfWeek(startDate)
            ; end.before(endDate); start = end, end = CalendarSupportHelper.get().findNextFirstDayOfWeek(end)  )
        {
            composites.add (new PromotionByWeek(getContext(), promotion, start, end));
        }
    
        return composites.getPromotions();
        
    }

    /**
     * 
     * @param startDate
     * @param promotion
     * @return Collection, collection of promotion to be generated
     */
    static public Collection generateByMonth(final Date startDate, final Date endDate, final PromotionBy promotion)
    {
        PromotionSupport composites = new PromotionSupport();
        
        for (Date start = startDate, end = CalendarSupportHelper.get().findNextFirstDayOfMonth(startDate);
             end.before(endDate); start = end, end = CalendarSupportHelper.get().findNextFirstDayOfMonth(end)  )
        {
            composites.add (new PromotionByMonth(getContext(), promotion, start, end));
        }
        
        return composites.getPromotions();
    }
    


    /**
     * 
     * @param spid
     * @return Date, the start date of this promotion generation
     * @throws HomeException
     */
    static private Date findPromotionStartDate(Object obj, PeriodEnum period)
        throws HomeException
    {
        
        Date effDate;
        Date historyDate;
        
        if (obj instanceof CRMSpid)
        {
            CRMSpid spid = (CRMSpid)obj;
            effDate = spid.getEffectiveDate();

            historyDate = findLastEndDate(spid.getSpid(), period);
        }
        else if (obj instanceof String)
        {
            String subID = (String)obj;
            CRMSpid spid = findSPIDProperty(lookupSpidBySubscriberID(subID));
            if (spid == null)
            {
                throw new HomeException("Can't identify the spid of subscriber:"+subID);
            }
            effDate = spid.getEffectiveDate();
            historyDate = findLastEndDate(subID, period);
        }
        else
        {
            return null;
        }
        
        
        if (effDate == null)
        {
            // both dates are null 
            if (historyDate == null)
            {
                return null;
            }       
            return historyDate;
        }
        else if (historyDate == null)
        {
            return effDate;
        }
        //both dates are not null;
        else if (effDate.after(historyDate))
        {
            return effDate;
        }
        else 
        {
            return historyDate;
        }
    }


    /**
     * 
     * @param spid
     * @return CRMSpid, the property of SPID Setting
     * @throws HomeException
     */
    static CRMSpid findSPIDProperty(int spid)
        throws HomeException
    {
        Home spidHome = (Home)getContext().get(CRMSpidHome.class);
        return (CRMSpid)spidHome.find(getContext(), Integer.valueOf(spid));
    }

    
    /**
     * 
     * @param where, filter for promotion history table
     * @return Date, Date of the earliest found end date in promotion history table
     */
    static Date findLastEndDate(Context ctx,Object where, Home historyHome)
        throws HomeException
    {

         Collection history = historyHome.select(ctx,where);
         if (history == null)
         {
             return null;
         }
         Date earliestDate = null;
     
         for (Iterator iter = history.iterator(); iter.hasNext();)
         {
             Date endDate =  ((HandsetPromotionHistory) iter.next()).getEndDate();
             if (earliestDate == null || endDate.before(earliestDate))
             {
                 earliestDate = endDate;
             }
         }
         return earliestDate;

    }
    
    
    
    /**
     * Find Earliest Last Date in the promotion history by subscriber ID
     * 
     * @param subID, subscriber ID
     * @return Date, the end date of last promotion period
     */    
    static Date findLastEndDate(String subID, PeriodEnum period)
        throws HomeException
    {
        Home historyHome = (Home)getContext().get(HandsetPromotionHistoryHome.class);
        HandsetPromotionHistory history = (HandsetPromotionHistory)historyHome
            .find(getContext(),new HandsetPromotionHistoryID(subID, period));
        if (history == null)
        {
            return null;    
        }
		return history.getEndDate();
    }
    
    
    /**
     * Find Earliest Last Date in the promotion history by subscriber ID
     * 
     * @param subID, subscriber ID
     * @return Date, the end date of last promotion period
     */    
    static Date findLastEndDate(int spid, PeriodEnum period)
        throws HomeException
    {
        Home  historyHome = (Home)getContext().get(PromotionCronTaskHistoryHome.class);
        PromotionCronTaskHistory history = (PromotionCronTaskHistory)historyHome
            .find(getContext(),new PromotionCronTaskHistoryID(spid, period));
        if (history == null)
        {
            return null;    
        }
		return history.getEndDate();
    }
        
    
    static Context getContext()
    {
        return context_;
    }
    
//    static Home getHistoryHome()
//    {
//        return historyHome_;
//    }
//
//    static Home getSpidHome()
//    {
//        return spidHome_;
//    }
}
