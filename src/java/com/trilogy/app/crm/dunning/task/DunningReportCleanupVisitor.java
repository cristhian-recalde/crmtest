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
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * 
 * @author abhijit.mokashi
 * @since 10.2.1
 * 
 * This visitor thread identifies the report that needs to be cleaned up depending on the 
 * number of days configured at spid level.
 */
public class DunningReportCleanupVisitor  implements Visitor{

	private static final long serialVersionUID = 1L;

	// one day in milliseconds (#hours * #minute * #seconds * 1000)
	final static long DAY_IN_MILLS = 24*60*60*1000L;

	/**
	 * We walk through all the spids and for each spid we query the dunning report table with the
     * spid specific criteria for the clean up and then removes the identified reports.     
	 */
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
	AbortVisitException {

		PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(),
				"DunningReportCleanup");

		LogSupport.debug(ctx, this,
				"inside DunningReportCleanupVisitor visit method");

		//Retrieve the spid home
		Home spidHome = (Home) ctx.get(CRMSpidHome.class);
		if (spidHome == null){
			throw new AgentException("System error: CRMSpidHome not found in context");
		}
		//Retrieve the DunningReportHome home
		final Home dunningReportHome = ( Home ) ctx.get(DunningReportHome.class);
		if ( dunningReportHome == null ){
			throw new AgentException("System error: DunningReportHome not found in context");
		}

		Collection spids;
		try {
			// if spid is specified use that spid only
			if(null != obj && obj instanceof String){
				spids = new ArrayList<CRMSpid>();
				CRMSpid spid = (CRMSpid)spidHome.find(obj);
				spids.add(spid);
			}else{
				spids = spidHome.selectAll();
			}
			
			if(null == spids){
				LogSupport.minor(ctx,this,"Unable to retrive spids, the cleanup task will exit now.");
			}
			
			final Iterator iter = spids.iterator();
			//for each spid
			while (iter.hasNext()) {
				final CRMSpid spid = (CRMSpid)iter.next();
				try {
					// Expire date will be ( current time - number of configured days in milliseconds )
					long expiryDate = new Date().getTime() - spid.getDunningReportCleanupDays()*DAY_IN_MILLS;
					
					// the task will be deleting report before the 00.00hours of the expiry day, 
					// thus need to exclude the hours and sec part of the date object
					expiryDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date(expiryDate)).getTime();
					
					// we need to delete all report for the spid which are generated before expiry date
					String whereClauseSQL = "SPID=" + spid.getId() + " and REPORTDATE<" + expiryDate;
	
					dunningReportHome.where(ctx, new SimpleXStatement(whereClauseSQL))
						.forEach(ctx, new CloneingVisitor(new HomeVisitor(dunningReportHome) {
							private static final long serialVersionUID = 1L;
							/**
							 * This visitor removes the dunning report found
							 */
							public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException {
								DunningReport dunningReport = (DunningReport)obj;
								try{
									getHome().remove(ctx,dunningReport);
								} catch (Exception e) {
									LogSupport.minor(ctx,this,"Couldn't remove dunning report generated on " + dunningReport.getReportDate(),e);
								}
							}
						}
					));
				} catch(Exception ex) {
					LogSupport.minor(ctx,this,"Failed to remove the dunning report for the spid: " + spid.getId(),ex);
				}
			}
		} catch (HomeInternalException e) {
			LogSupport.minor(ctx,this,"Failed to while running the Dunning report cleanup.",e);
		} catch (UnsupportedOperationException e) {
			LogSupport.minor(ctx,this,"Failed to while running the Dunning report cleanup.",e);
		} catch (HomeException e) {
			LogSupport.minor(ctx,this,"Failed to while running the Dunning report cleanup.",e);
		}

		pm.log(ctx);
	}
}
