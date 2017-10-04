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

package com.trilogy.app.crm.web.border;

import java.util.Date;

import com.trilogy.app.crm.bean.BalanceHistorySearch;
import com.trilogy.app.crm.bean.BalanceHistoryXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.search.CallDetailSearchTypeEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.xdb.ByDate;


/**
 * An Custom Search Agent for Balance History information. This has to work together with
 * MergedBalanceHistoryHome. Any modification here has to be tested to work with
 * MergedBalanceHistoryHome because BalanceHistoryHome is not a real home.
 *
 * For now, this class is not used. It is checked in in case it will be needed in the future.
 *
 * @author victor.stratan@redknee.com
 **/
public class BalanceHistoryCustomSearchAgent extends ContextAgentProxy
{

    public void execute(Context ctx) throws AgentException
    {
        final BalanceHistorySearch search = (BalanceHistorySearch) SearchBorder.getCriteria(ctx);
        if (search != null)
        {
            final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            final String subID;
            if (sub != null)
            {
                subID = sub.getId();
            }
            else
            {
                subID = "";
            }

            final And condition = new And();
            condition.add(new EQ(BalanceHistoryXInfo.SUBSCRIBER_ID, subID));

            // startDate
            if (search.getStartDate() != null)
            {
                condition.add(new GTE(BalanceHistoryXInfo.KEY_DATE, search.getStartDate()));
            }

            // endDate
            if (search.getEndDate() != null)
            {
                final Date endofEndDate = CalendarSupportHelper.get(ctx).getEndOfDay(search.getEndDate());
                condition.add(new LTE(BalanceHistoryXInfo.KEY_DATE, endofEndDate));
            }

            // limit will not be applied here, it will be applied in MergedBalanceHistoryHome
            //condition.add(new Limit(search.getLimit()));

            SearchBorder.doSelect(ctx, condition);
        }

        super.execute(ctx);
    }
}
