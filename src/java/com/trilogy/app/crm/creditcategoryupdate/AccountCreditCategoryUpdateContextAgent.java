package com.trilogy.app.crm.creditcategoryupdate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.account.filter.ResponsibleAccountPredicate;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositDetails;
import com.trilogy.app.crm.bean.DepositHome;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.app.crm.bean.DepositSubscriberLevelConfig;
import com.trilogy.app.crm.bean.DepositXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleAndActionOutput;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleEngineUtility;
import com.trilogy.app.crm.core.ruleengine.EventTypeEnum;
import com.trilogy.app.crm.core.ruleengine.actions.ChangeCreditCategoryOutputAction;
import com.trilogy.app.crm.core.ruleengine.actions.ifc.ActionOutputIfc;
import com.trilogy.app.crm.core.ruleengine.actions.param.ActionParameter;
import com.trilogy.app.crm.core.ruleengine.actions.param.ActionParameterValue;
import com.trilogy.app.crm.core.ruleengine.counters.CounterData;
import com.trilogy.app.crm.core.ruleengine.util.RuleEngineConstants;
import com.trilogy.app.crm.counter.Counter;
import com.trilogy.app.crm.counter.CounterUtils;
import com.trilogy.app.crm.counter.CountersQuery;
import com.trilogy.app.crm.creditcategoryupdate.visitor.accountprocessing.AccountCreditCategoryUpdateAssignmentVisitor;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class AccountCreditCategoryUpdateContextAgent implements ContextAgent {
	
	private List<String> failedBANs_ = new ArrayList<String>();
	private AccountCreditCategoryUpdateAssignmentVisitor accountCreditCategoryUpdatePredicateVisitor_;
	private static Map<Integer,Object> prameterMap_ = new HashMap<Integer, Object>();//Taking Static Instance as we don't need to pass anything for now
	public static String CREDITCATEGORYUPDATE_ACCOUNT = "CreditCategoryUpdate";
	
	
	public AccountCreditCategoryUpdateContextAgent(AccountCreditCategoryUpdateAssignmentVisitor accountCreditCategoryUpdateAssignmentVisitor)
	{
		accountCreditCategoryUpdatePredicateVisitor_ = accountCreditCategoryUpdateAssignmentVisitor;
	}
	
 public void execute(Context context)
 {
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport.debug(context, this, "AccountCreditCategoryUpdateContextAgent started!!!! ");
		}
		final Account account = (Account) context.get(CREDITCATEGORYUPDATE_ACCOUNT);

		if (account != null) {
			try {
				Subscriber subscriber = account.getSubscriber();
				Collection<Subscriber> subList = account.getAllSubscribers(context);
				List<SubscriberIfc> subscriberList = new ArrayList<SubscriberIfc>();
				Set<Long> subscriptionTypes = new HashSet<Long>();
				Set<String> subscriberIds = new HashSet<String>();
				Set<Integer> creditCategorySet = new HashSet<Integer>();
				for (Subscriber sub : subList) {
					subscriberList.add(sub);
					subscriberIds.add(sub.getId());
					subscriptionTypes.add(sub.getSubscriptionType());
				}
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(context, this, "AccountCreditCategoryUpdateContextAgent Subscriber:: " + subscriber
							+ " and SubscriberList:: " + subscriberList);
				}
				creditCategorySet.add(account.getCreditCategory());
				prameterMap_.put(RuleEngineConstants.CREDIT_CATEGORY, creditCategorySet);
				prameterMap_.put(RuleEngineConstants.ACCOUNT_TYPE_LIST, account.getType());
				prameterMap_.put(RuleEngineConstants.SUBSCRIPTION_TYPE, subscriptionTypes);
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(context, this,
							"AccountCreditCategoryUpdateContextAgent creditCategorySet:: " + creditCategorySet
									+ " account type:: " + account.getType() + " and list of subscriptionTypes:: "
									+ subscriptionTypes + " for BAN:: " + account.getBAN());
				}
				
				context = context.createSubContext();
				
				Counter[] allcounters = ((CountersQuery)context.get(CountersQuery.class)).getAllCounters(context, account.getSpid(), account.getBAN(), subscriberIds);
				CounterData counterData = CounterUtils.createCounterData(allcounters);
				context.put(CounterData.class, counterData);
				
				BusinessRuleAndActionOutput output = BusinessRuleEngineUtility.evaluateRuleExecuteAction(context,
						EventTypeEnum.CREDIT_CATEGORY_UPDATE, account, subscriber, subscriberList, prameterMap_,
						new ActionParameter[] {});

				List<ActionOutputIfc> actionOutputList = output.getActionOutputList();
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(context, this,
							"AccountCreditCategoryUpdateContextAgent after Rule evaluation" + actionOutputList);
				}
				ActionParameterValue actionParameterValue;
				int newCreditCategory = 0;
				for (ActionOutputIfc actionOutput : actionOutputList) {
					if (LogSupport.isDebugEnabled(context)) {
						LogSupport.debug(context, this,
								"Him ActionName.getName():: " + ChangeCreditCategoryOutputAction.ACTION_NAME
										+ " actionOutput.getActionName():: " + actionOutput.getActionName()
										+ " ActionOutput.getResultCode():: " + actionOutput.getResultCode());
					}
					if (ChangeCreditCategoryOutputAction.ACTION_NAME.equals(actionOutput.getActionName())
							&& actionOutput.getResultCode() == ActionOutputIfc.RESULT_CODE_PASS) {
						ActionParameter actionParameter[] = actionOutput.getActionOutPram();
						if (null != actionParameter) {
							for (ActionParameter obj : actionParameter) {
								actionParameterValue = obj.value;
								newCreditCategory = actionParameterValue.intValue();
							}
							Home accountHome = (Home) context.get(AccountHome.class);
							if (LogSupport.isDebugEnabled(context)) {
								LogSupport.debug(context, this,
										"Value of Action parameter Array new Credit Category:: " + newCreditCategory+" for BAN:: "+account.getBAN());
							}
							try {
								account.setCreditCategory(newCreditCategory);
								accountHome.store(account);
								if (account.getGroupType() == GroupTypeEnum.GROUP) {
									Collection<Account> getGroupChildrenAccount = AccountSupport.getTopologyEx(context, account, new ResponsibleAccountPredicate(false), null, true, false, null, false);
									if (null != getGroupChildrenAccount) {
										if (LogSupport.isDebugEnabled(context)) {
											LogSupport.debug(context, this,
													"For Group parent BAN:: " + account.getBAN() + " has Total account:: "
															+ getGroupChildrenAccount.size() + " responsible:: "
															+ account.getResponsible());
										}
										for (Account act : getGroupChildrenAccount) {
											if (act.getSystemType() == SubscriberTypeEnum.POSTPAID
													|| act.getSystemType() == SubscriberTypeEnum.HYBRID) {
												act.setCreditCategory(newCreditCategory);
												accountHome.store(act);
												if (LogSupport.isDebugEnabled(context)) {
													LogSupport.debug(context, this, "For children account:: " + act.getBAN()
															+ " has Individual flag is:: " + act.isIndividual(context)
															+ " and responsible:: " + act.getResponsible());
												}
												if (act.isIndividual(context)) {
													updateSubscriberCreditLimitAndDepositReleaseDate(context, act);
												}
											}
										}
									}
								} else {
									updateSubscriberCreditLimitAndDepositReleaseDate(context, account);
								}
							} catch (Exception e) {
								if (LogSupport.isDebugEnabled(context)) {
									LogSupport.debug(context, this,
											"Rule has been matched but failed to set new credit category for account"
													+ account.getBAN() + " :: " + e.getMessage());
									addFailedAssignedBAN(account.getBAN());
								}
							}
						}
					}
				}
			} catch (Exception e) {
				new MinorLogMsg(this, "AccountCreditCategoryUpdate process failed for account '" + account.getBAN()
						+ "': " + e.getMessage(), e).log(context);
				addFailedAssignedBAN(account.getBAN());

			}
		} else {
			if (LogSupport.isDebugEnabled(context)) {
				LogSupport.debug(context, this, "No account found in AccountCreditCategoryUpdateContextAgent");
			}
		}
		if (LogSupport.isDebugEnabled(context)) {
			LogSupport.debug(context, this, "AccountCreditCategoryUpdateContextAgent Ended!!!! ");
		}
	}
	    
    private synchronized void addFailedAssignedBAN(String BAN)
    {
        this.failedBANs_.add(BAN);
    }
    

    public synchronized List<String> getFailedAssignedBANs()
    {
        return failedBANs_;
    }

	
	
		
	public Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	
	
	public void updateSubscriberCreditLimitAndDepositReleaseDate(Context ctx, Account account) {
		try {
			Home creditCategoryHome = (Home) ctx.get(com.redknee.app.crm.bean.CreditCategoryHome.class);
			Home subscriberHome = (Home) ctx.get(SubscriberHome.class);
			Home depositHome = (Home) ctx.get(DepositHome.class);
			CreditCategory userBean = (CreditCategory) creditCategoryHome.find(ctx, account.getCreditCategory());
				if (null != userBean) {
					if (LogSupport.isDebugEnabled(ctx)){
						LogSupport.debug(ctx, this,"In updateSubscriberCreditLimitAndDepositReleaseDate userBean is not null");
					}
					Map depositDetailMap = userBean.getSubscriptionDepositDetails();
					Set keySet = depositDetailMap.keySet();
					Iterator keySetIterator = keySet.iterator();
					long subscriptionType = 0l;
					while (keySetIterator.hasNext()) {
						subscriptionType = (Long) keySetIterator.next();
						Object value = depositDetailMap.get(subscriptionType);
						if (LogSupport.isDebugEnabled(ctx)){
							LogSupport.debug(ctx, this, "In updateExistingCreditLimit value " + value+" and subscription type:: " + subscriptionType);
						}
						if (value instanceof DepositDetails) {
							if (null != value) {
								long creditLimit = ((DepositDetails) value).getCreditLimit();
								if (LogSupport.isDebugEnabled(ctx)){
									LogSupport.debug(ctx, this, "In updateExistingCreditLimit creditLimit:: " + creditLimit);
								}
								Collection<Subscriber> sub = account.getSubscriptionsBySubscriptionType(ctx,
										subscriptionType);
								if (null != sub) {
									for (Subscriber subscriber : sub) {
										if (subscriber.getState() != SubscriberStateEnum.INACTIVE) {
											try {
												updateExistingCreditLimit(ctx, subscriber, creditLimit, subscriberHome);
												updateDepositReleaseDate(ctx, subscriber, value, depositHome,account);
											} catch (Exception e) {
												new MinorLogMsg(this,"AccountCreditCategoryUpdate process failed for updating the Subscriber '"
														+ subscriber.getId() + "': " + e.getMessage(),e).log(ctx);
											}
										}
									}

								}
							}
						}
					}
				}
		} catch (HomeException e) {
			new MinorLogMsg(this, "AccountCreditCategoryUpdate process failed for account '" + account.getBAN() + "' during updateSubscriberCreditLimitAndDepositReleaseDate method: "
					+ e.getMessage(), e).log(ctx);
		}
	}
	
	
	/*
	 * This method is used to update the credit Limit of non-inactive Subscriber on the basis of matched rule.
	 */
	public boolean updateExistingCreditLimit(Context ctx, Subscriber subscriber, long newCreditLimit,Home subscriberHome) throws HomeException{
		boolean isCreditLimitUpdate = false;
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, this, "Before In updateExistingCreditLimit creditLimit:: " + newCreditLimit);
		}
		if (newCreditLimit > 0l) {
			subscriber.setCreditLimit(newCreditLimit);
			subscriberHome.store(subscriber);
			if (LogSupport.isDebugEnabled(ctx)){
				LogSupport.debug(ctx, this, "After In updateExistingCreditLimit creditLimit:: " + subscriber.getCreditLimit());
			}
			isCreditLimitUpdate = true;
		}
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, this, "For Subscriber:: " + subscriber.getId() + "creditLimit is :: " + newCreditLimit+" isCreditLimitUpdate:: "+isCreditLimitUpdate);
		}
		return isCreditLimitUpdate;
	}

	/*
	 *  This method is used to update the expected release date of deposit on the basis of matched rule and 
	 *  deposit whose status is not released. 	
	 */	
	public boolean updateDepositReleaseDate(Context ctx, Subscriber sub, Object value, Home depositHome,
			Account account)throws HomeException {
		boolean isDepositReleaseDate = false;
		Date previousDepositDate = null;
		Date expectedNewReleaseDate = null;
		And filter = new And();
		int depositHoldDuration = 0;
		List list = ((DepositDetails) value).getDepositSubscriberLevelConfig();
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, this,
				"In updateDepositReleaseDate List:: " + list + " Value in Subscriber, sub.getBAN():: " + sub.getBAN()
						+ " sub.getId():: " + sub.getId() + " sub.getSubscriptionType():: "
						+ sub.getSubscriptionType());
		}
		if (!list.isEmpty()) {
			filter.add(new EQ(DepositXInfo.SPID, account.getSpid()));
			filter.add(new EQ(DepositXInfo.BAN, sub.getBAN()));
			filter.add(new EQ(DepositXInfo.SUBSCRIPTION_TYPE, sub.getSubscriptionType()));
			filter.add(new NEQ(DepositXInfo.STATUS, DepositStatusEnum.RELEASED_INDEX));
			Collection<Deposit> depositDetails = HomeSupportHelper.get(ctx).getBeans(ctx, Deposit.class, filter);
			if (null != depositDetails) {
				for (Deposit deposit : depositDetails) {
					for (int i = 0; i < list.size(); i++) {
						if (LogSupport.isDebugEnabled(ctx)){
							LogSupport.debug(ctx, this,
								"In updateDepositReleaseDate deposit type from home:: " + deposit.getDepositType()
										+ " Confiured on GUI:: "
										+ ((DepositSubscriberLevelConfig) list.get(i)).getDepositType());
						}
						if (deposit.getDepositType() == ((DepositSubscriberLevelConfig) list.get(i)).getDepositType()) {
							if (LogSupport.isDebugEnabled(ctx)){
								LogSupport.debug(ctx, this, "Deposit Hold duration from GUI:: "
									+ ((DepositSubscriberLevelConfig) list.get(i)).getDepositHoldDuration());
							}
							if (((DepositSubscriberLevelConfig) list.get(i)).getDepositHoldDuration() > 0) {
								depositHoldDuration = ((DepositSubscriberLevelConfig) list.get(i))
										.getDepositHoldDuration();
								previousDepositDate = deposit.getDepositDate();
								expectedNewReleaseDate = addDays(previousDepositDate, depositHoldDuration);
								deposit.setExpectedReleaseDate(expectedNewReleaseDate);
								if (LogSupport.isDebugEnabled(ctx)){
									LogSupport.debug(ctx, this,
										"Before depositHoldDuration:: " + depositHoldDuration
												+ " previousDepositDate:: " + previousDepositDate
												+ " expectedNewReleaseDate:: " + expectedNewReleaseDate);
								}
								depositHome.store(deposit);
								if (LogSupport.isDebugEnabled(ctx)){
									LogSupport.debug(ctx, this,
										"After depositHoldDuration:: " + depositHoldDuration + " previousDepositDate:: "
												+ previousDepositDate + " expectedNewReleaseDate:: "
												+ deposit.getExpectedReleaseDate());
								}
								isDepositReleaseDate = true;
							}
						}
					}
				}
			}

		}
		if(LogSupport.isDebugEnabled(ctx)){
		LogSupport.debug(ctx, this,
				"For Account:: " + sub.getBAN() + "depositRelease Date is updated:: " + isDepositReleaseDate);
		}
		return isDepositReleaseDate;
	}
	
}
