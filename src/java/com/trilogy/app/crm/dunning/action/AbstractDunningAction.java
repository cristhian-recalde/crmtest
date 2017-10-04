package com.trilogy.app.crm.dunning.action;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.dunning.DunningLevel;

public abstract class AbstractDunningAction extends com.redknee.framework.xhome.beans.AbstractBean
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Account account;
	private Subscriber subscriber;
	private DunningLevel nextLevel;
	private Date runningDate;
	
	public Account getAccount() {
		return account;
	}
	public Subscriber getSubscriber() {
		return subscriber;
	}
	
	
	public DunningLevel getNextLevel() {
		return nextLevel;
	}
	
	public Date getRunningDate() {
		return runningDate;
	}
	public void populateActionWithDunningData(Account account , Subscriber subscriber , 
			DunningLevel nextLevel,Date runningDate )
			 
	{
		this.account = account;
		this.subscriber = subscriber;
		this.nextLevel = nextLevel;
		this.runningDate = runningDate;		
	}
}
