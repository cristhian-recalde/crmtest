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

package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseConfigurationEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteriaHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreateDepositResponse;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositDetails;
import com.trilogy.app.crm.bean.DepositReasonCode;
import com.trilogy.app.crm.bean.DepositReasonCodeXInfo;
import com.trilogy.app.crm.bean.DepositReference;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.app.crm.bean.DepositSubscriberLevelConfig;
import com.trilogy.app.crm.bean.DepositType;
import com.trilogy.app.crm.bean.DepositTypeXInfo;
import com.trilogy.app.crm.bean.DepositXInfo;
import com.trilogy.app.crm.bean.ReleaseTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.deposit.PercentageWithMinimumReleaseCalculation;
import com.trilogy.app.crm.deposit.ReleaseCalculation;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

/**
 * Support class for deposit calculations.
 *
 * @author cindy.wong@redknee.com
 */
public final class DepositSupport
{
    /**
     * License key for auto deposit release.
     */
    public static final String AUTO_DEPOSIT_RELEASE_LICENSE_KEY = "AUTO_DEPOSIT_RELEASE_LICENSE";

    /**
     * Creates an instance of <code>DepositSupport</code>.
     */
    private DepositSupport()
    {
        // empty for utility class
    }

    /**
     * Retrieves the release calculation strategy applicable to the service provider specified.
     *
     * @param context
     *            The operating context.
     * @param serviceProvider
     *            The service provider of interest.
     * @return The release calculation strategy applicable to <code>serviceProvider</code>.
     */
    public static ReleaseCalculation getReleaseCalculation(final Context context, final CRMSpid serviceProvider)
    {
        return PercentageWithMinimumReleaseCalculation.getInstance();
    }

    /**
     * Checks if auto deposit release is enabled for a specific service provider.
     *
     * @param context
     *            The operating context.
     * @param serviceProvider
     *            The service provider to inquire about.
     * @return Whether auto deposit release license is enabled.
     */
    public static boolean isAutoDepositReleaseEnabled(final Context context, final CRMSpid serviceProvider)
    {
        final LicenseMgr licenseManager = (LicenseMgr) context.get(LicenseMgr.class);
        boolean result = licenseManager.isLicensed(context, AUTO_DEPOSIT_RELEASE_LICENSE_KEY);
        if (serviceProvider != null)
        {
            result &= serviceProvider.getUseAutoDepositRelease().equals(AutoDepositReleaseConfigurationEnum.YES);
        }
        return result;
    }

    /**
     * Retrieves the auto deposit release criteria set for a service provider.
     *
     * @param context
     *            The operating context.
     * @param serviceProvider
     *            The service provider to look up.
     * @return The auto deposit release criteria set for serviceProvider.
     * @throws HomeException
     *             Thrown if there are problems retrieving the criteria.
     */
    public static AutoDepositReleaseCriteria getServiceProviderCriteria(final Context context,
        final CRMSpid serviceProvider) throws HomeException
    {
        final Home home = (Home) context.get(AutoDepositReleaseCriteriaHome.class);
        if (home == null)
        {
            new DebugLogMsg(DepositSupport.class, "Auto Deposit Release Criteria Home missing", null);
            throw new HomeException("AutoDepositReleaseCriteriaHome does not exist!");
        }
        return (AutoDepositReleaseCriteria) home.find(context, Long.valueOf(serviceProvider.getAutoDepositReleaseCriteria()));
    }

    /**
     * Determines the set of IDs of bill cycles which are eligible for deposit release.
     *
     * @param context
     *            The operating context.
     * @param serviceProvider
     *            The service provider.
     * @param criteria
     *            The criteria for determining eligibility.
     * @param activeDate
     *            The date to act upon
     * @return A set of bill cycle IDs (Integer) which are eligible for deposit release.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public static Set getEligibleBillCycleIds(final Context context, final CRMSpid serviceProvider,
        final AutoDepositReleaseCriteria criteria, final Calendar activeDate) throws HomeException
    {
        // determine the day of month a bill cycle has to match
        final Calendar targetDate = (Calendar) activeDate.clone();

        targetDate.add(Calendar.DAY_OF_MONTH, criteria.getReleaseSchedule());

        // retrieves all bill cycles whose day of month matches
        final And and = new And();
        and.add(new EQ(BillCycleXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())));
        and.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, Integer.valueOf(targetDate.get(Calendar.DAY_OF_MONTH))));
        Collection billCycles;
        billCycles = ((Home) context.get(BillCycleHome.class)).select(context, and);
        final Set<Integer> billCycleIds = new TreeSet<Integer>();
        for (Object object : billCycles)
        {
            final BillCycle billCycle = (BillCycle) object;
            billCycleIds.add(Integer.valueOf(billCycle.getBillCycleID()));
        }
        return billCycleIds;
    }
    
    /**
	 * Get list of deposits using optional and required parameters.
	 *  
	 * @param ctx
	 * @param parameters
	 * @return
	 * @throws DepositReleaseException
	 */
	public static List<Deposit> getDeposits(Context ctx, Map parameters)
			throws DepositReleaseException {
		
		String ban = String.valueOf(parameters.get("BAN"));
		DepositSupport.validateMandatoryObject(ctx, ban, "BAN");

		boolean status = (Boolean) parameters.get("CALCULATE_INTREST");
		DepositSupport.validateMandatoryObject(ctx, status, "CalculateInterest");
		
		List<Deposit> depositBean = null;
		try {
			And filter = new And();
			filter.add(new EQ(DepositXInfo.BAN, ban));
			if (parameters.get("DEPOSIT_ID") != null)
				filter.add(new EQ(DepositXInfo.DEPOSIT_ID, parameters.get("DEPOSIT_ID")));
			if (parameters.get("SUBSCRIPTION_ID") != null && parameters.get("SUBSCRIPTION_ID") != "")
				filter.add(new EQ(DepositXInfo.SUBSCRIPTION_ID, parameters.get("SUBSCRIPTION_ID")));
			if (parameters.get("PRODUCT_ID") != null) {
				DepositSupport.validateMandatoryObject(ctx, parameters.get("SUBSCRIPTION_ID"),"SUBSCRIPTION_ID");
				filter.add(new EQ(DepositXInfo.PRODUCT_ID, parameters.get("PRODUCT_ID")));
			}
			
			depositBean = (List<Deposit>) HomeSupportHelper.get(ctx).getBeans(
					ctx, Deposit.class, filter);
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE, "Error while getting Deposit" + e, e);
			throw new DepositReleaseException("Error while getting Deposit");
		}
		return depositBean;
	}
	
	public static List<Deposit> getDeposits(Context ctx, And filter) throws DepositReleaseException {
		try {
			return (List<Deposit>) HomeSupportHelper.get(ctx).getBeans(ctx, Deposit.class, filter);
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE, "Error while getting Deposit" + e, e);
			throw new DepositReleaseException("Error while getting Deposit");
		}
	}
	
	/**
	 * @param ctx
	 * @param value
	 * @param label
	 * @throws NullPointerException
	 */
	public static boolean validateMandatoryObject(Context ctx, final Object value,
			final String label) throws NullPointerException {
		boolean status = false;
		if (value == null) {
			final String msg = label + " is null. " + label
					+ " is Mandatory and cannot be null";
			LogSupport.minor(ctx, MODULE, msg);
			throw new NullPointerException(msg);
			
		}else{
			status = true;
		}
		return status;
	}
	
	/**
	 * Update subscriber id of deposit using required parameters.
	 * 
	 * @param ctx
	 * @param parameters
	 * @return
	 * @throws DepositReleaseException
	 */
	public static int updateSubscriptionId(Context ctx, Map parameters)
			throws DepositReleaseException {
		int status = DepositConstants.depositReleasefail;
		DepositSupport.validateMandatoryObject(ctx, parameters.get("BAN"),"BAN");
		DepositSupport.validateMandatoryObject(ctx,	parameters.get("DEPOSIT_ID"), "depositID");
		DepositSupport.validateMandatoryObject(ctx,parameters.get("SUBSCRIPTION_ID"), "SUBSCRIPTION_ID");
		try {
			And filter = new And();
			filter.add(new EQ(DepositXInfo.DEPOSIT_ID,parameters.get("DEPOSIT_ID")));
			filter.add(new EQ(DepositXInfo.BAN,parameters.get("BAN")));
			Deposit depositBean  = (Deposit) HomeSupportHelper.get(ctx).findBean(ctx, Deposit.class, filter);
			if (depositBean != null) {
				//if(depositBean.getSubscriptionID() != String.valueOf(parameters.get("SUBSCRIPTION_ID"))){
					And andFilter = new And();
						andFilter.add(new EQ(SubscriberXInfo.ID, parameters.get("SUBSCRIPTION_ID")));
						andFilter.add(new EQ(SubscriberXInfo.BAN, parameters.get("BAN")));
					Subscriber subscriber = (Subscriber) HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, andFilter);
					if(null != subscriber){
						if(subscriber.getState() != SubscriberStateEnum.INACTIVE){
							depositBean.setSubscriptionID(String.valueOf(parameters.get("SUBSCRIPTION_ID")));
							depositBean = updateDeposit(ctx, depositBean);
							LogSupport.info(ctx, MODULE, "Update Subscripton ID "+ depositBean.getSubscriptionID() + " for Deposit ID "+ depositBean.getDepositID());
							if(depositBean.getProductID() >= 0){// update credit limit when deposit only for subscriber not for product
							Account account = AccountSupport.getAccount(ctx, depositBean.getBAN());
							try{
								 SubscriberCreditLimitSupport.updateCreditLimit(ctx, account, subscriber, depositBean.getAmountHeld());
							 }catch (Exception e) {
							   LogSupport.minor(ctx,MODULE,"Error while update credit limit for "+ subscriber.getId());
							   throw new DepositReleaseException("Error while update credit limit for "+ subscriber.getId());
							}
						   }
						 status = DepositConstants.depositReleaseSuccess;
					    }else{
							LogSupport.minor(ctx, MODULE,"Given subscriber "+parameters.get("SUBSCRIPTION_ID")+" is de-activated");
							throw new DepositReleaseException("Given subscriber "+parameters.get("SUBSCRIPTION_ID")+" is de-activated");
						}
					}else{
						LogSupport.minor(ctx, MODULE,"Given Subscriber "+parameters.get("SUBSCRIPTION_ID") +" not fount Under Account "+parameters.get("BAN"));
						throw new DepositReleaseException("Given Subscriber "+parameters.get("SUBSCRIPTION_ID") +" not fount Under Account "+parameters.get("BAN"));
					}
				/*}else{
					LogSupport.minor(ctx, MODULE,"Given subscriber "+String.valueOf(parameters.get("SUBSCRIPTION_ID")) +" already existing with deposit entry"));
					throw new DepositReleaseException("Given subscriber "+String.valueOf(parameters.get("SUBSCRIPTION_ID")) +" already existing with deposit entry"));
				}*/
			} else {
				LogSupport.minor(ctx, MODULE,"Can not find Deposit record for Deposit ID "+ parameters.get("DEPOSIT_ID") + " and BAN "+ parameters.get("BAN"));
				throw new DepositReleaseException("Can not find Deposit record for Deposit ID "+ parameters.get("DEPOSIT_ID") + " and BAN "+ parameters.get("BAN"));
			}
		} catch (Exception e) {
			LogSupport.minor(ctx, MODULE,"Error while Updating Deposit Subscripton ID" + e, e);
			throw new DepositReleaseException("Error while Updating Deposit Subscripton ID");
		}
		return status;
	}
	
	/**
	 * set/update info of deposit bean
	 * 
	 * @param ctx
	 * @param objDeposit
	 * @param released
	 * @param totalReleasedAmount
	 * @param interestReleased
	 * @param releaseDate
	 * @param receiptNumInterest
	 * @param reasonCode
	 * @param payment
	 * @param receiptNum
	 * @param systemUpdateDate
	 * @return
	 * @throws DepositReleaseException
	 */
	static Deposit setDepositInfo(Context ctx, Deposit objDeposit,
			DepositStatusEnum released, double totalReleasedAmount,
			double interestReleased, Date releaseDate, long receiptNumInterest,
			Object reasonCode, ReleaseTypeEnum releaseType, long receiptNum,
			Date systemUpdateDate) throws DepositReleaseException {
		Deposit deposit = null;
		try {
			deposit = (Deposit) objDeposit.clone();
			deposit.setStatus(DepositStatusEnum.RELEASED);
			deposit.setReleasedAmount(totalReleasedAmount);
			deposit.setInterestReleased(interestReleased);
			deposit.setReleaseDate(new Date());
			deposit.setInterestTransactionID(receiptNumInterest);
			if(reasonCode != null)
				deposit.setReasonCode(Integer.parseInt(String.valueOf(reasonCode)));
			deposit.setReleaseType(releaseType);
			deposit.setTransactionId(receiptNum);
			deposit.setSystemUpdateDate(new Date());
		} catch (CloneNotSupportedException e) {
			LogSupport.info(ctx, MODULE,"Error while set info of Deposit bean");
			throw new DepositReleaseException("Error while set info of Deposit bean");
		}
		return deposit;
	}
	
	/**
	 * Update deposit in Database
	 * @param ctx
	 * @param deposit
	 * @return
	 * @throws DepositReleaseException
	 */
	static Deposit updateDeposit(Context ctx, Deposit deposit)
			throws DepositReleaseException {
		if (deposit != null) {
			try {
				deposit = (Deposit) HomeSupportHelper.get(ctx).storeBean(ctx,
						deposit);
			} catch (HomeException e) {
				LogSupport.minor(ctx, MODULE, "Fail to update Deposit" + e, e);
				throw new DepositReleaseException("Fail to update Deposit"
						+ deposit.getDepositID());
			}
		}
		return deposit;
	}
	
	/**
	 * Get adjustment type bean.
	 * 
	 * @param ctx
	 * @param typeEnum
	 * @return adjustment type
	 * @throws DepositReleaseException
	 */
	static AdjustmentType getAdjustmentType(Context ctx,
			AdjustmentTypeEnum typeEnum) throws DepositReleaseException {
		AdjustmentType type = null;
		try {
			type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
					typeEnum);
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE,
					"Error while getting Adjustment Type " + e, e);
			throw new DepositReleaseException(
					"Error while getting Adjustment Type " + typeEnum);
		}
		return type;
	}
	
	//Convert Date to Calendar
  	public static Calendar dateToCalendarConverter(Date date) {

  		Calendar calendar = Calendar.getInstance();
  		if(date!=null){
  			calendar.setTime(date);
  		}else{
  			calendar = null;
  		}
  		return calendar;

  	}
  	
  	public static Integer getValidValue(Integer value){
		if(value!=null && Integer.MIN_VALUE==value){//if(value<0){
			return null;
		}
		return value;
	}
	
	public static String getValidValue(String value){
		if(value.isEmpty()){
			return null;
		}
		return value;
	}
	
	/**
	 * BSS should expose deposit history API , for clients to fetch all deposit
	 * of a BAN or all deposits of subscription or deposits based on status.
	 * 
	 * @param ctx
	 * @param spid
	 * @param ban
	 * @param subscriptionID
	 * @param status
	 * @return collection of Deposit
	 * @throws NullPointerException
	 *             when Mandatory parameter is null";
	 * 
	 */
	public static List<Deposit> getDepositHistory(Context ctx, Integer spid,
			String ban, String subscriptionID, Integer status)
			throws NullPointerException,CRMExceptionFault {
		String msg = DepositConstants.EMPTY_STRING;
		validateMandatoryObject(ctx, spid, "SPID");
		validateMandatoryObject(ctx, ban, "BAN");
		List<Deposit> depositList = null;
		try {
			And filter = new And();
			filter.add(new EQ(DepositXInfo.SPID, spid));
			filter.add(new EQ(DepositXInfo.BAN, ban));

			if (subscriptionID != null && !subscriptionID.isEmpty())
				filter.add(new EQ(DepositXInfo.SUBSCRIPTION_ID, subscriptionID));

			if (!isNull(status) && status>=0 && status<=DepositStatusEnum.COLLECTION.getSize()) {
				filter.add(new EQ(DepositXInfo.STATUS, status));
			}else if(!isNull(status) && status>DepositStatusEnum.COLLECTION.getSize() && status<0){
				msg = "Deposit status does not match with Enum index. Please enter valid status.";
				logAndThrowException(ctx, msg);
			}
			
			depositList = (List<Deposit>) HomeSupportHelper.get(ctx)
					.getBeans(ctx, Deposit.class, filter);
			
			if(DepositSupport.isNull(depositList) || depositList.isEmpty()){
				msg = "No Records Found";
				logAndThrowException(ctx, msg);
			}

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE, "Getting Deposit for " + ban
						+ " Found: " + depositList.size() + " records");
			}
		} catch(CRMExceptionFault e){
			logAndThrowException(ctx, e.getMessage());
		} catch (Exception e) {
			logAndThrowException(ctx, e.getMessage());
		}
		return depositList;
	}
	
	public static boolean isNull(Object obj){
		if(obj==null){
			return true;
		}
		return false;
	}
	
	public static boolean isNotNull(Object obj){
		if(obj!=null){
			return true;
		}
		return false;
	}
	
	private static void logAndThrowException(Context ctx, String msg) throws CRMExceptionFault {
		LogSupport.minor(ctx, MODULE, msg);
		throw new CRMExceptionFault(msg);
	}
	
	private static long getReasonCodeForDepositRelease(Context ctx, int spid) {
    	CRMSpid spidBean = null;
		try {
			spidBean = SpidSupport.getCRMSpid(ctx, spid);
		} catch (Exception e) {
			LogSupport.minor(ctx, DepositSupport.class, "Cannot find Spid " + e);
		}
		if (spidBean != null) {
			DepositReasonCode depositReasonCodeXDB = null;
			try {
				And filter = new And();
				filter.add(new EQ(DepositReasonCodeXInfo.SPID, spid));
				filter.add(new EQ(DepositReasonCodeXInfo.IDENTIFIER, spidBean.getReasonCodeAutoDepositRelease()));
				depositReasonCodeXDB = (DepositReasonCode) HomeSupportHelper.get(ctx).findBean(ctx,
						DepositReasonCode.class, filter);
				if(depositReasonCodeXDB==null){
					return Long.valueOf("-2");
				}
			} catch (Exception e) {
				LogSupport.minor(ctx, DepositSupport.class, "Cannot find DepositReasonCode " + e);
				return Long.valueOf("-2");
			}
			return depositReasonCodeXDB.getDepositReasonCode();
		}

		return Long.valueOf("-2");
	}
	
	private static void setParametersForDepositRelease(Context ctx, Map parameters,List<DepositStatusEnum> statusList, int spId){
		statusList.add(DepositStatusEnum.ACTIVE);
		parameters.put(DepositConstants.CALCULATE_INTREST, true);
		parameters.put(DepositConstants.REASON_CODE, getReasonCodeForDepositRelease(ctx,spId));
	}
	
	public static void addQualifiedDepositsToListForRelease(Context ctx, List<Deposit> depositReleaseList, Map parameters, Deposit deposit, Account account, CreditCategory creditCategory)
			throws HomeException,DepositReleaseException {
		Map<Long, DepositDetails> subscriptionDepDetailsMap = creditCategory.getSubscriptionDepositDetails();
		if (subscriptionDepDetailsMap.containsKey(deposit.getSubscriptionType())) {
			DepositDetails depositDetails = subscriptionDepDetailsMap.get(deposit.getSubscriptionType());
			if (isNotNull(depositDetails)) {
				List<DepositSubscriberLevelConfig> configValList = depositDetails.getDepositSubscriberLevelConfig();
				if (isNull(configValList) || configValList.isEmpty()) {
					LogSupport.debug(ctx, DepositSupport.class,
							"Unable to find Subscriber level Deposit config in Credit Category "
									+ account.getCreditCategory() + " for Subscription Type "
									+ deposit.getSubscriptionType());
					throw new HomeException("Unable to find Subscriber level Deposit config in Credit Category "
							+ account.getCreditCategory() + " for Subscription Type "
							+ deposit.getSubscriptionType());

				} else {
					ListIterator<DepositSubscriberLevelConfig> configIterator = configValList.listIterator();
					while (configIterator.hasNext()) {
						List<DepositStatusEnum> statusList = new ArrayList<DepositStatusEnum>();
						setParametersForDepositRelease(ctx,parameters, statusList,deposit.getSpid());
						DepositSubscriberLevelConfig config = configIterator.next();
						LogSupport.debug(ctx, DepositSupport.class,
								">>> Deposit subscriber level config - " + config.getDepositType() + " > "
										+ config.getDepositHoldDuration() + " > "
										+ config.isConvertToPayment());
						if (deposit.getDepositType() == config.getDepositType()) {
							ReleaseDeposit releaseDep;
							if (config.isConvertToPayment()) {
								LogSupport.debug(ctx, DepositSupport.class, ">>> Deposit id "
										+ deposit.getDepositID() + " can be converted to the payment and is added for release.");
								releaseDep = new DepositPayment();
							} else {
								LogSupport.debug(ctx, DepositSupport.class,
										">>> Deposit id " + deposit.getDepositID()
												+ " can not be converted to the payment but can be credited and is added for release.");
								releaseDep = new DepositCredit();
							}
							releaseDep.releaseDeposit(ctx, parameters, statusList, depositReleaseList);
						} // end of deposit type check
					} // end of config iterator

				} // end of sub level config check
			} else {
				LogSupport.minor(ctx, DepositSupport.class,
						"Unable to find Deposit Details configured for subscription type "
								+ deposit.getSubscriptionType() + " in CreditCategory "
								+ account.getCreditCategory());
				throw new HomeException("Unable to find Deposit Details configured for subscription type "
						+ deposit.getSubscriptionType() + " in CreditCategory " + account.getCreditCategory());
			} // end of deposit details check
		} // end of subscriber type check in map
	}
	
	/**
	 * Method that allows creation of deposits, returns array of deposits
	 * containing correlation id of input
	 * 
	 * @param context
	 * @param depositRefs
	 * @return
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	public static CreateDepositResponse[] createDeposit(final Context context,
			final DepositReference depositRefs[], String agentID) throws HomeInternalException,
			HomeException {
		CreateDepositResponse[] createDepositResponses = null;
		Collection<CreateDepositResponse> response = new ArrayList<CreateDepositResponse>();

		for (DepositReference depositRef : depositRefs) {
			Deposit deposit = null;
			CreateDepositResponse createDepositResponse = null;

			try {
				deposit = constructDeposit(context, depositRef);
				deposit.setUserId(agentID);
				deposit = createDepositBean(context, deposit);
				
				try {
					createDepositResponse = (CreateDepositResponse) XBeans
							.instantiate(CreateDepositResponse.class, context);
				} catch (Exception e) {
					LogSupport
							.minor(context,
									DepositSupport.class,
									"Error instantiating new DepositResponse.  Using default constructor.",
									e);
					createDepositResponse = new CreateDepositResponse();
				}

				createDepositResponse.setCorrelationID(depositRef
						.getCorrelationID());
				createDepositResponse.setDepositID(deposit.getDepositID());

				response.add(createDepositResponse);
			} catch (Exception e) {
				LogSupport.major(context, DepositSupport.class,e.getMessage(), e);
				throw new HomeException(e.getMessage(), e);
			}
		}

		createDepositResponses = response
				.toArray(new CreateDepositResponse[response.size()]);
		return createDepositResponses;
	}
	
	/**
	 * Constructs deposit bean
	 * 
	 * @param context
	 * @param depositRef
	 * @return
	 * @throws HomeInternalException
	 * @throws HomeException
	 */

	public static Deposit constructDeposit(Context context,
			final DepositReference depositRef) throws HomeInternalException,
			HomeException {
		Deposit deposit = null;

		try {
			deposit = (Deposit) XBeans.instantiate(Deposit.class, context);
		} catch (Exception e) {
			LogSupport
					.minor(context,
							DepositSupport.class,
							"Error instantiating new deposit.  Using default constructor.",
							e);
			deposit = new Deposit();
		}

		deposit = fillInDepositData(context, deposit, depositRef);

		return deposit;
	}

	/**
	 * Helps fill in the deposit entity related data by mapping it from request
	 * validates the request input
	 * 
	 * @param ctx
	 * @param deposit
	 * @param depositRef
	 * @return
	 */
	public static Deposit fillInDepositData(Context ctx, Deposit deposit,
			final DepositReference depositRef) throws HomeException {
		if (depositRef.getCorrelationID() == null || depositRef.getCorrelationID().trim().isEmpty()) {
			throw new HomeException(
					"Correlation id is mandatory for deposit creation!");
		}

		deposit.setSpid(depositRef.getSpid());

		Account account = null;
		String ban = depositRef.getBan();
		if (ban != null) {
			account = AccountSupport.getAccount(ctx, ban);
			if (account == null) {
				throw new HomeException("Account does not exist for BAN = "
						+ ban);
			}
			
			if(account.getState()==AccountStateEnum.INACTIVE){
				throw new HomeException("Account is in-active");
			}

			deposit.setBAN(depositRef.getBan());
		} else {
			throw new HomeException("BAN can not be empty or null.");
		}

		String subscriberId = depositRef.getSubscriptionID();
		if (subscriberId != null && !(subscriberId.trim().isEmpty())) // optional
		{
			Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(
					ctx, subscriberId);
			if (subscriber == null) {
				throw new HomeException("Subscriber with identifier "
						+ subscriberId + " does not exist.");
			}

			deposit.setSubscriptionID(subscriberId);
		}

		SubscriptionType subscriptionType = null;
		try {
			subscriptionType = (SubscriptionType) HomeSupportHelper.get(ctx)
					.findBean(
							ctx,
							SubscriptionType.class,
							new EQ(SubscriptionTypeXInfo.ID, depositRef
									.getSubscriptionType()));

			if (subscriptionType == null) {
				throw new HomeException("Invalid input Subscription Type "
						+ depositRef.getSubscriptionType());
			} else {
				deposit.setSubscriptionType(depositRef.getSubscriptionType());
			}
		} catch (HomeException he) {
			throw new HomeException(he.getMessage());
		}

		if(depositRef.getProductID() >= 0){
			Service service = (Service) HomeSupportHelper.get(ctx).findBean(ctx,Service.class,
					new EQ(ServiceXInfo.ID, depositRef.getProductID()));
			
			if(service!=null){
				deposit.setProductID(depositRef.getProductID());
			} else {
				throw new HomeException(
						"Product ID does not exist");
			}
		}
		
		int depositType = depositRef.getDepositType();
		DepositType depositTypeBean = null;
		try {
			depositTypeBean = HomeSupportHelper.get(ctx).findBean(ctx,
					DepositType.class,
					new EQ(DepositTypeXInfo.IDENTIFIER, depositType));
			if (depositTypeBean == null) {
				throw new HomeException(
						"Invalid Deposit type identifier, No Deposit Type found.");
			} else {
				deposit.setDepositType(depositType);
				// derive adjustment type and GL code from depositType
				// configuration
				deposit.setAdjustmentType(depositTypeBean.getAdjustmentType());
				deposit.setGlCode(depositTypeBean.getGlCode());
			}
		} catch (HomeException he) {
			throw new HomeException(he.getMessage(), he);
		}

		if (depositRef.getDepositAmount() <= 0) {
			throw new HomeException("Invalid Deposit amount requested.");
		} else {
			deposit.setAmountHeld(depositRef.getDepositAmount());
		}

		if(depositRef.getReasonCode()!=Integer.MIN_VALUE){
			DepositReasonCode bean = HomeSupportHelper.get(ctx).findBean(ctx, DepositReasonCode.class, new EQ(DepositReasonCodeXInfo.DEPOSIT_REASON_CODE, depositRef.getReasonCode()));
			
			if(bean!=null)
				deposit.setReasonCode(depositRef.getReasonCode());
			else
				throw new HomeException("Deposit Reason code "+depositRef.getReasonCode()+" does not exist");			
		}
		
		if((depositRef.getExternalTransactionID()==null) || (depositRef.getExternalTransactionID().trim().isEmpty()))
			throw new HomeException("External Transaction id is mandatory for deposit creation!");
		else
			deposit.setExternalTransactionID(depositRef.getExternalTransactionID());

		long longDateValue = depositRef.getDepositDate();
		Long newDateValue = new Long (longDateValue);
		if (newDateValue != 0 && newDateValue != -9223372036854775808L)//date should not be blank or null
			deposit.setDepositDate(new Date(depositRef.getDepositDate()));
		else
			throw new HomeException("Deposit date is mandatory for deposit creation!");

		// account, subscriptionType, depositType will never be null at this
		// stage
		// and deposit start date is today
		Date releaseDate = calculateDepositReleaseDate(ctx, account,new Date(depositRef.getDepositDate()), subscriptionType, depositTypeBean);
		deposit.setExpectedReleaseDate(releaseDate);
		deposit.setStatus(DepositStatusEnum.ACTIVE); // default at entity
		deposit.setReleaseDate(null);
		return deposit;
	}

	/**
	 * Method to calculate deposit release date based on credit category
	 * configuration related to the account
	 * 
	 * @param ctx
	 * @param account
	 * @param depositStartDate
	 * @param subscriptionType
	 * @param depositType
	 * @return
	 * @throws HomeException
	 */

	public static Date calculateDepositReleaseDate(Context ctx,
			Account account, Date depositStartDate,
			SubscriptionType subscriptionType, DepositType depositType)
			throws HomeException {
		CreditCategory creditCategory = CreditCategorySupport
				.findCreditCategory(ctx, account.getCreditCategory());
		if (creditCategory == null) {
			throw new HomeException("Credit Category not found with id "
					+ account.getCreditCategory() + " for Account "
					+ account.getBAN());
		}

		int depositHoldDuration = -9999;
		Date depositReleaseDate = null;

		Map depositDetails = creditCategory.getSubscriptionDepositDetails();

		// depositDetails.values()

		if (depositDetails.containsKey(subscriptionType.getId())) {
			DepositDetails detail = (DepositDetails) depositDetails
					.get(subscriptionType.getId());
			List<DepositSubscriberLevelConfig> subLevelConfig = detail
					.getDepositSubscriberLevelConfig();

			if (subLevelConfig != null) {
				Iterator<DepositSubscriberLevelConfig> j = subLevelConfig
						.iterator();
				while (j.hasNext()) {
					DepositSubscriberLevelConfig subLevelDepositConfig = j
							.next();
					if (subLevelDepositConfig.getDepositType() == depositType
							.getIdentifier()) {
						depositHoldDuration = subLevelDepositConfig
								.getDepositHoldDuration();
						break;
					}
				}
			} else {
				LogSupport.minor(
						ctx,
						DepositSupport.class,
						"Unable to find Subscriber level Deposit config in Credit Category "
								+ account.getCreditCategory()
								+ " for Subscription Type "
								+ subscriptionType.getId());
				throw new HomeException(
						"Unable to find Subscriber level Deposit config in Credit Category "
								+ account.getCreditCategory()
								+ " for Subscription Type "
								+ subscriptionType.getId());
			}

		} else {
			LogSupport.minor(ctx, DepositSupport.class,
					"Unable to find Deposit Details configured for subscription type "
							+ subscriptionType.getId() + " in CreditCategory "
							+ account.getCreditCategory());
			throw new HomeException(
					"Unable to find Deposit Details configured for subscription type "
							+ subscriptionType.getId() + " in CreditCategory "
							+ account.getCreditCategory());
		}

		if(depositHoldDuration == 0){
			depositReleaseDate = null;
		}else if (depositHoldDuration != -9999) {
			// for deposit creation, pass deposit start date as today
			depositReleaseDate = CalendarSupportHelper.get(ctx)
					.findDateDaysAfter(depositHoldDuration, depositStartDate);
		}else{
			throw new HomeException(
					"Unable to calculate Deposit release date, please check credit category configuration");
		}
		// check if deposit release date is returned null at caller
		return depositReleaseDate;
	}
	
	/**
	 * Create single deposit entity in storage (database)
	 * 
	 * @param context
	 * @param deposit
	 * @return deposit created
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	public static Deposit createDepositBean(final Context context,
			Deposit deposit) throws HomeInternalException, HomeException {
		deposit = HomeSupportHelper.get(context).createBean(context, deposit);
		return deposit;
	}
	private static final String MODULE = DepositSupport.class.getName();

}
