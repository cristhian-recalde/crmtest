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
package com.trilogy.app.crm.bas.recharge;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

import com.trilogy.app.crm.bean.RecurringChargeErrorReport;
import com.trilogy.app.crm.bean.RecurringChargeErrorReportXInfo;
import com.trilogy.app.crm.bean.RecurringChargeReportSearch;
import com.trilogy.app.crm.bean.RecurringChargeReportSearchWebControl;
import com.trilogy.app.crm.bean.RecurringChargeReportSearchXInfo;


/**
 * RecurringChargeErrorReport searching border, supporting search by BAN, MSISDN and SPID.
 *
 * @author larry.xia@redknee.com
 */
public class RecurringChargeReportSearchBorder extends SearchBorder
{

    /**
     * Create a new instance of <code>RecurringChargeReportSearchBorder</code>.
     *
     * @param context
     *            The operating context.
     */
    public RecurringChargeReportSearchBorder(final Context context)
    {
        super(context, RecurringChargeErrorReport.class, new RecurringChargeReportSearchWebControl());

        // BAN
        addAgent(new SelectSearchAgent(RecurringChargeErrorReportXInfo.BAN,RecurringChargeReportSearchXInfo.BAN, false)
        {

            public String getCriteria(final Object criteria)
            {
                return ((RecurringChargeReportSearch) criteria).getBAN();
            }


            public String getField(final Object bean)
            {
                return ((RecurringChargeErrorReport) bean).getBAN();
            }
        });

        // MSISDN
        addAgent(new SelectSearchAgent(RecurringChargeErrorReportXInfo.MSISDN,RecurringChargeReportSearchXInfo.MSISDN, false)
        {

            public String getCriteria(final Object criteria)
            {
                return ((RecurringChargeReportSearch) criteria).getMSISDN();
            }


            public String getField(final Object bean)
            {
                return ((RecurringChargeErrorReport) bean).getMSISDN();
            }
        });

        // SPID
        addAgent(new ContextAgentProxy()
        {

            public void execute(final Context ctx) throws AgentException
            {
                final RecurringChargeReportSearch criteria = (RecurringChargeReportSearch) getCriteria(ctx);

                if (criteria.getSpid() != -1)
                {
                    SearchBorder.doSelect(ctx, new EQ(RecurringChargeErrorReportXInfo.SPID, Integer.valueOf(criteria
                        .getSpid())));

                }
                delegate(ctx);
            }
        });
        // Limit
        addAgent(new LimitSearchAgent(RecurringChargeReportSearchXInfo.LIMIT));

    }

}