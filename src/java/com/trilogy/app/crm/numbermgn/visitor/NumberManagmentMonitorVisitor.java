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
package com.trilogy.app.crm.numbermgn.visitor;

import java.util.Map;
import java.util.TreeMap;

import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.numbermgn.MobileNumGrpMonitorAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * For each NumberGroup depending on the threshold, it will dispaly an SNMP 
 * @author amedina
 *
 */
public class NumberManagmentMonitorVisitor implements Visitor
{


	private NumberManagmentMonitorVisitor() 
	{
		super();
		
		raisedAlarm_ = new TreeMap<Integer, Boolean>();
		raisedClearAlarm_ = new TreeMap<Integer, Boolean>();
	}

	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException
	{
		MsisdnGroup group = (MsisdnGroup) obj;		
		
		if (group.getAvailableMsisdns() < group.getMinSize())
		{
			if (!raisedAnAlarm(group))
			{
    			new EntryLogMsg(13188L, this,this.toString(), null,
    					new String[]{group.getName()}, null).log(ctx);
    			new InfoLogMsg(MobileNumGrpMonitorAgent.class,"Raised the SNMP Alarm Id 13188 for Mobile " +
    					"numbers fallen below threshold",null).log(ctx);
    			
    			setRaisedAlarm(group);

			}
		}
		else
		{
			if (!raisedAClearAlarm(group))
			{
				new EntryLogMsg(13189L, this, this.toString(), null, new String[]{group.getName()}, null).log(ctx);
				new InfoLogMsg(MobileNumGrpMonitorAgent.class,"Cleared the SNMP Alarm Id 13189",null).log(ctx);
				setRaisedClearAlarm(group);
			}
		}

	}
	
	
	/**
	 * Flags a raised alarm
	 * @param group
	 */
	private void setRaisedClearAlarm(MsisdnGroup group) 
	{
		raisedAlarm_.put(group.getId(), false);
		raisedClearAlarm_.put(group.getId(), true);
		
	}


	/**
	 * Flags a clear alarm
	 * @param group
	 */
	private void setRaisedAlarm(MsisdnGroup group) 
	{
		raisedAlarm_.put(group.getId(), true);
		raisedClearAlarm_.put(group.getId(), false);
		
	}

	/**
	 * Verifies if this particular group already flagged a clear SNMP alarm
	 * @param group
	 * @return
	 */
	private boolean raisedAClearAlarm(MsisdnGroup group) 
	{
		boolean raised = false;
		
		if (raisedClearAlarm_.containsKey(group.getId()))
		{
			raised = raisedClearAlarm_.get(group.getId());
		}
		
		return raised;
	}

	/**
	 * Verifies if this particular group already flagged a clear SNMP alarm
	 * @param group
	 * @return
	 */
	private boolean raisedAnAlarm(MsisdnGroup group) 
	{
		boolean raised = false;
		
		if (raisedAlarm_.containsKey(group.getId()))
		{
			raised = raisedAlarm_.get(group.getId());
		}
		
		return raised;
	}
	
	public static NumberManagmentMonitorVisitor instance()
	{
		return instance_;
	}

	/**
	 * Serail version UID 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Map<Integer, Boolean> raisedAlarm_;
	protected Map<Integer, Boolean> raisedClearAlarm_;
	private static NumberManagmentMonitorVisitor instance_ = new NumberManagmentMonitorVisitor(); 

}
