/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordSearch;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportRecordSearchWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;

/**
 * @since 10.3
 * @author shyamrag.charuvil@redknee.com
 */
public class DunningRecordSearchBorder extends SearchBorder{

	 public DunningRecordSearchBorder(Context ctx)
	    {
	        super(ctx, DunningReportRecord.class, new DunningReportRecordSearchWebControl());

	 	    addAgent(new ContextAgentProxy()
	        {
	            @Override
	            public void execute(Context ctx) throws AgentException
	            {
	            	DunningReportRecordSearch criteria = (DunningReportRecordSearch) getCriteria(ctx);

	                if (criteria.getBan() != null && !criteria.getBan().trim().isEmpty())
	                {
	                    doSelect(ctx, new EQ(DunningReportRecordXInfo.SPID, criteria.getSpid()));
	                    doSelect(ctx, new EQ(DunningReportRecordXInfo.BAN, criteria.getBan()));
	                }
	                delegate(ctx);
	            }
	        });

	        addAgent(new ContextAgentProxy()
	        {
	            @Override
	            public void execute(Context ctx) throws AgentException
	            {
	            	DunningReportRecordSearch criteria = (DunningReportRecordSearch) getCriteria(ctx);

	                if (criteria.getReportStartDate() != null)
	                {
	                    doSelect(ctx, new GTE(DunningReportRecordXInfo.REPORT_DATE, criteria.getReportStartDate()));
	                }
	                delegate(ctx);
	            }
	        });

	        addAgent(new ContextAgentProxy()
	        {
	            @Override
	            public void execute(Context ctx) throws AgentException
	            {
	            	DunningReportRecordSearch criteria = (DunningReportRecordSearch) getCriteria(ctx);

	                if (criteria.getReportEndDate() != null)
	                {
	                    doSelect(ctx, new LTE(DunningReportRecordXInfo.REPORT_DATE, criteria.getReportEndDate()));
	                }
	                delegate(ctx);
	            }
	        });
	    }
}
