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
package com.trilogy.app.crm.dunning.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.DunningReportRecordMatureStateEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * 
 * @author abhijit.mokashi
 * @since 10.2.1
 * 
 * This visitor thread identifies the Dunning Report Record that needs to be discarded.
 * Those ban who is present in low level dunning report with status Approved/Accepted will be discarded.
 * 
 */
public class DunningUnapprovedLastLevelRecordCleanupVisitor  implements Visitor{

	private static final long serialVersionUID = 1L;

	// one day in milliseconds (#hours * #minute * #seconds * 1000)
	final static long DAY_IN_MILLS = 24*60*60*1000L;

	/**
	 *
	 */
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
	AbortVisitException {

		PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(),
				"DunningUnapprovedLastLevelRecordCleanup");

		LogSupport.debug(ctx, this,
				"inside DunningUnapprovedLastLevelRecordCleanupVisitor visit method");

		try {
			// we need to fetch the records which are approved/accepted
			final Or orCondition = new Or();
			orCondition.add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY, DunningReportRecordMatureStateEnum.APPROVED_INDEX));
			orCondition.add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY, DunningReportRecordMatureStateEnum.ACCEPTED_INDEX));
			
			// need to order by Ban and then by forcasted level
			final OrderBy order1 = new OrderBy(DunningReportRecordXInfo.BAN, true);
			final OrderBy order2 = new OrderBy(DunningReportRecordXInfo.FORECASTED_LEVEL, true);
			final OrderBy order3 = new OrderBy(DunningReportRecordXInfo.REPORT_DATE, true);
			 
			// do not change the order of below lines, else the algorithm doesn't work.
			Home home1 = (Home)ctx.get(DunningReportRecordHome.class);
			Home orderedHome = new OrderByHome(ctx, order3, home1 );
			orderedHome = new OrderByHome(ctx, order2, home1 );
			orderedHome = new OrderByHome(ctx, order1, orderedHome );
			
			Collection<DunningReportRecord> dunnningRecords = (Collection<DunningReportRecord>) orderedHome.select(ctx, orCondition);
			if(null != dunnningRecords && !dunnningRecords.isEmpty()){
				List<DunningReportRecord> recordsToBeDiscarded = new ArrayList<DunningReportRecord>();
				/**
				 * Iterate the list and find the records to be discarded.
				 * Records to be Discarded is identified if any higher level 
				 * Dunning Record for the same ban is present
				 * 
				 * 
				 */
				Iterator<DunningReportRecord> itr = dunnningRecords.iterator();
				DunningReportRecord previousRecord = itr.next();
				while(itr.hasNext()){
					DunningReportRecord currentRecord = itr.next();
					
					if(previousRecord.getBAN().equals(currentRecord.getBAN()) &&
							previousRecord.getForecastedLevel() < currentRecord.getForecastedLevel()){
						recordsToBeDiscarded.add(previousRecord);
					}
					previousRecord = currentRecord;
				}
				
				// mark the records to be discarded 
				if(!recordsToBeDiscarded.isEmpty()){
					Home home = (Home)ctx.get(DunningReportRecordHome.class);
					for(DunningReportRecord record : recordsToBeDiscarded){
						record.setRecordMaturity(DunningReportRecordMatureStateEnum.DISCARDED_INDEX);;
						home.store(record);
					}
				}
			}
		} catch (HomeInternalException ex) {
			LogSupport.minor(ctx,this,"Failed to discard the dunning report.",ex);
		} catch (HomeException ex) {
			LogSupport.minor(ctx,this,"Failed to discard the dunning report.",ex);
		}

		pm.log(ctx);
	}
}
