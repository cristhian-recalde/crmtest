package com.trilogy.app.crm.bean.deposit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.app.crm.bean.DepositXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.DepositConstants;
import com.trilogy.app.crm.support.DepositReleaseException;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class DepositReleaseProcessingAgent extends LifecycleAgentScheduledTask {
	private static final String MODULE = DepositReleaseProcessingAgent.class.getName();
	public DepositReleaseProcessingAgent(Context ctx, String agentId) throws AgentException {
		super(ctx, agentId);
	}

	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException {
		LogSupport.debug(ctx, this,
				">>> Auto deposit release as per Credit Category configuration (asynchronous) - ENTERED");
		try{
			setValidDepositsToAutoRelease(ctx);
		}catch(Exception e){
			LogSupport.debug(ctx, this,
					">>> Release Deposits: Exception occured while releasing deposits >>> " + e.getMessage());
		}
		LogSupport.debug(ctx, this,
				">>> Auto deposit release as per Credit Category configuration (asynchronous) - EXITED");
	}
	
	public void setValidDepositsToAutoRelease(Context ctx){
		List<Deposit> depositReleaseList = new ArrayList<Deposit>();
		Map parameters = new HashMap();
		List<Deposit> deposits = new ArrayList<Deposit>();
		try {
			deposits = getDepositsForRelease(ctx);
		} catch (DepositReleaseException e) {
			LogSupport.debug(ctx, this,
					">>> Release Deposits: Exception occured while retrieving deposits >>> " + e.getMessage());
		}
		if (!(DepositSupport.isNull(deposits) || deposits.isEmpty())) {
			ListIterator<Deposit> depositItr = deposits.listIterator();
			while (depositItr.hasNext()) {
				depositReleaseList = new ArrayList<Deposit>();
				Deposit deposit = depositItr.next();
				try {
					LogSupport.debug(ctx, this,
							">>> Release Deposits: Processing Deposit Id and Subscription Id >>> " + deposit.getDepositID()+" : "+deposit.getSubscriptionID()+" : "+deposit.getBAN());
					if(DepositSupport.isNotNull(deposit) && DepositSupport.isNotNull(deposit.getSubscriptionID()) && (!DepositConstants.EMPTY_STRING.equals(deposit.getSubscriptionID()))){
					Account account = AccountSupport.getAccount(ctx, deposit.getBAN());
					CreditCategory creditCategory = CreditCategorySupport.findCreditCategory(ctx, account.getCreditCategory());
					if (creditCategory == null) {
						LogSupport.debug(ctx, this, ">>> Credit Category not found with id "
								+ account.getCreditCategory() + " for Account " + account.getBAN());
						throw new HomeException("Credit Category not found with id " + account.getCreditCategory()
								+ " for Account " + account.getBAN());
					} else {
						depositReleaseList.add(deposit);
						DepositSupport.addQualifiedDepositsToListForRelease(ctx, depositReleaseList, parameters,
								deposit, account, creditCategory);
					}
					}
				} catch (HomeException e) {
					LogSupport.debug(ctx, this,
							">>> Release Deposits: Exception occured while retrieving Account/Credit Category >>> "
									+ e.getMessage());
				} catch (DepositReleaseException e) {
					LogSupport.debug(ctx, this,
							">>> Exception occured while releasing deposits >>> " + e.getMessage());
				}

			}
		}else{
			LogSupport.debug(ctx, this,
					">>> Release Deposits: No Deposits found for release >>> ");
		}
	}
	
	private List<Deposit> getDepositsForRelease(Context ctx) throws DepositReleaseException{
		return DepositSupport.getDeposits(ctx, createFilterForDepositRelease(ctx));
	}
	
	private And createFilterForDepositRelease(Context ctx){
		And filter = new And();
		Date expReleseDate = new Date();
		
		if(getParameter2(ctx, String.class)!=null){
			if(StringUtils.isNumeric(getParameter2(ctx, String.class))){
				expReleseDate = new Date(Long.valueOf(getParameter2(ctx, String.class)));
			}else{
				LogSupport.debug(ctx, this,
						">>> Release Deposits: Date passed in the parameter 2 is not numeric. Please enter the number of milliseconds >>> received value is "+getParameter2(ctx, String.class));
			}
		}
		filter.add(new EQ(DepositXInfo.STATUS,DepositStatusEnum.ACTIVE_INDEX));
		filter.add(new LTE(DepositXInfo.EXPECTED_RELEASE_DATE,expReleseDate));
		return filter;
	}
	
}
