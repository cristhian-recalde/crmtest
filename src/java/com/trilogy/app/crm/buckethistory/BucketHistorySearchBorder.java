// INSPECTED: 26/09/2003 LZ


/*
 *  TransactionSearchBorder
 *
 *  Author : Kevin Greer
 *  Date   : Sept 24, 2003
 *
 *  Copyright (c) 2003, Redknee
 *  All rights reserved.
 */

package com.trilogy.app.crm.buckethistory;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

import com.trilogy.app.crm.bean.BucketHistoryXInfo;
import com.trilogy.app.crm.bean.BucketHistory;
import com.trilogy.app.crm.bean.BucketHistorySearch;
import com.trilogy.app.crm.bean.BucketHistorySearchWebControl;
import com.trilogy.app.crm.bean.BucketHistorySearchXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;


/**
 * A Custom SearchBorder for Transactions.
 *
 * This will be generated from an XGen template in the future but for now
 * I'm still experimenting with the design.  Also, some common helper classes
 * will be created for each Search type.
 *
 * Add this Border before the WebController, not as one of either its
 * Summary or Detail borders.
 *
 * @author     kgreer
 **/
public class BucketHistorySearchBorder
   extends SearchBorder
{

   public BucketHistorySearchBorder(Context context)
   {
      super(context, BucketHistory.class, new BucketHistorySearchWebControl());

      // BAN
      addAgent(new SelectSearchAgent(BucketHistoryXInfo.BAN, BucketHistorySearchXInfo.BAN));

      // MSISDN
      

      // Subscriber ID
      addAgent(new SelectSearchAgent(BucketHistoryXInfo.SUBSCRIPTION_ID, BucketHistorySearchXInfo.SUBSCRIBER_ID));

      // startDate
      addAgent(new ContextAgentProxy() {
         @Override
        public void execute(Context ctx)
            throws AgentException
         {
        	 BucketHistorySearch criteria = (BucketHistorySearch) getCriteria(ctx);

            if ( criteria.getStartDate() != null )
            {
                Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(criteria.getStartDate());
               doSelect(ctx, new GTE(BucketHistoryXInfo.ADJUSTMENT_DATE, startDate));
            }

            delegate(ctx);
         }
      });

      // endDate
      addAgent(new ContextAgentProxy() {
         @Override
        public void execute(Context ctx)
            throws AgentException
         {
        	 BucketHistorySearch criteria = (BucketHistorySearch) getCriteria(ctx);

            if ( criteria.getEndDate() != null )
            {
                Date endDate = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(criteria.getEndDate());
               doSelect(ctx, new LTE(BucketHistoryXInfo.ADJUSTMENT_DATE, endDate));
            }

            delegate(ctx);
         }
      });

      // Limit
      addAgent(new LimitSearchAgent(BucketHistorySearchXInfo.LIMIT));

   }

}

