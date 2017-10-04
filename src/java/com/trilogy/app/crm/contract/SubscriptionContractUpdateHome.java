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
package com.trilogy.app.crm.contract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonHome;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;


/**
 * Creates new adjustment types for new contracts, updates and look-ups existing
 * adjustment types for existing contracts.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class SubscriptionContractUpdateHome extends HomeProxy
{
	private static final SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("yyyyMMdd");

    /**
     * Creates a new SubscriptionContractUpdateHome.
     * 
     * @param delegate
     *            The home to which we delegate.
     */
    public SubscriptionContractUpdateHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * Creates a new SubscriptionContractUpdateHome.
     * 
     * @param context
     *            The operating context.
     * @param delegate
     *            The home to which we delegate.
     */
    public SubscriptionContractUpdateHome(final Context context, final Home delegate)
    {
        super(delegate);
        setContext(context);
    }


    // ////////////////////////////////////// SPI Impl
    /**
     * @param ctx
     * @param obj
     * @return Object
     * @exception HomeException
     * @exception HomeInternalException
     */
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        new InfoLogMsg(this, "Starting contract update CREATE", null).log(ctx);
        Subscriber sub = (Subscriber) obj;
        long contract = sub.getSubscriptionContract(ctx);
        if (contract != SubscriberSupport.INVALID_VALUE
                && contract != SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT
                && contract != SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED)
        {
            Home home = (Home) ctx.get(SubscriptionContractHome.class);
            Date startDate = new Date();
            SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContractTerm.class,
                    new EQ(SubscriptionContractTermXInfo.ID, sub.getSubscriptionContract(ctx)));
            if (term != null)
            {
                com.redknee.app.crm.contract.SubscriptionContract newContract = new com.redknee.app.crm.contract.core.SubscriptionContract(
                        sub.getId(), term, startDate);
                
                Long subsidyAmount = ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM);
                Long penaltyFeePerMonth = ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM);
                Long deviceProductId = ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM);
                
                if (subsidyAmount != null)
                	newContract.setSubsidyAmount(ConvertToCurrency(ctx, subsidyAmount));
                if (penaltyFeePerMonth != null)
                	newContract.setPenaltyFeePerMonth(ConvertToCurrency(ctx, penaltyFeePerMonth));
                if (deviceProductId != null)
                	newContract.setDeviceProductID(deviceProductId);
                
                home.create(ctx, newContract);
                sub.setSubscriptionContractEndDate(newContract.getContractEndDate());
                sub.setSubscriptionContractStartDate(newContract.getContractStartDate());
                sub.setSubscriptionContract(newContract.getContractId());
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Contract created for subscriber " + sub.getId() + " with contract Id : " + newContract.getContractId());
                }
            }
            else
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Unable to find contract for subscriber " + sub.getId());
                }
                throw new HomeException("Unable to find contract Id " + sub.getSubscriptionContract(ctx));
            }
        }
        new InfoLogMsg(this, "Completed  contract update CREATE", null).log(ctx);
        return getDelegate(ctx).create(ctx, obj);
    }


    /**
     * write to data store
     * 
     * @param ctx
     * @param obj
     * @exception HomeException
     * @exception HomeInternalException
     */
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        new InfoLogMsg(this, "Starting contract update STORE", null).log(ctx);
        final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        long newContractId = newSub.getSubscriptionContract(ctx);
        long oldContractId = oldSub.getSubscriptionContract(ctx);
        long newPricePlanId = newSub.getPricePlan();
        long oldPricePlanId = oldSub.getPricePlan();
        Home crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
        CRMSpid crmSpidBean = (CRMSpid) crmSpidHome.find(ctx, oldSub.getSpid());
        
        boolean enteringToInactive = EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub);
        if ((needToTerminateContractOnDeactivation(ctx, oldSub, newSub)) || (oldContractId != newContractId)
                || (newPricePlanId != oldPricePlanId && crmSpidBean.getUseContractPricePlan()) || (enteringToInactive))
        {
            Home home = (Home) ctx.get(SubscriptionContractHome.class);
            SubscriptionContract contract = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContract.class,
                    new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, newSub.getId()));
            SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx,
                    SubscriptionContractTerm.class,
                    new EQ(SubscriptionContractTermXInfo.ID, newSub.getSubscriptionContract(ctx)));
            if (contract != null)
            {
                home.remove(ctx, contract);
            }
            // Resettign contract empty, if no new contract is selected
            if (((newPricePlanId != oldPricePlanId) && (oldContractId == newContractId))
                    || (newSub.getState() == SubscriberStateEnum.INACTIVE))
            {
                if(newSub.getState() == SubscriberStateEnum.INACTIVE)
                {
                    newContractId = SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT;
                    newSub.setSubscriptionContract(SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT);
                }
                
                if (crmSpidBean.getUseContractPricePlan())
                {
                    newContractId = SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT;
                    newSub.setSubscriptionContract(SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT);
                    newSub.setSubscriptionContractEndDate(new Date());
                    newSub.setSubscriptionContractStartDate(new Date());
                }
                else if (!crmSpidBean.getUseContractPricePlan() && term != null)
                {
                    newSub.setSubscriptionContractEndDate(contract.getContractEndDate());
                    newSub.setSubscriptionContractStartDate(contract.getContractStartDate());
                    newSub.setDaysRemainingInTerm(CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(new Date(), contract.getContractEndDate()));
                    newSub.setCurrentCancellationCharges(SubscriptionContractSupport.getCurrentPenaltyFee(ctx, newSub, contract, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()), term.getProrateCancellationFees()));
                }
            }
            
            if (!enteringToInactive && newContractId != SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED
                    && newContractId != SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT
                    && newContractId != SubscriberSupport.INVALID_VALUE)
            {
                Date startDate = new Date();
                if (term != null)
                {
                    SubscriptionContract newContractTerm = new com.redknee.app.crm.contract.core.SubscriptionContract(
                            										newSub.getId(), term, startDate);
                    
                    Long subsidyAmount = ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM);
                    Long penaltyFeePerMonth = ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM);
                    Long deviceProductId = ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM);

                    if (subsidyAmount != null)
                    	newContractTerm.setSubsidyAmount(ConvertToCurrency(ctx, subsidyAmount));
                    if (penaltyFeePerMonth != null)
                    	newContractTerm.setPenaltyFeePerMonth(ConvertToCurrency(ctx, penaltyFeePerMonth));
                    if (deviceProductId != null)
                    	newContractTerm.setDeviceProductID(deviceProductId);

                    
                    home.create(ctx, newContractTerm);
                    newSub.setSubscriptionContractEndDate(newContractTerm.getContractEndDate());
                    newSub.setSubscriptionContractStartDate(newContractTerm.getContractStartDate());
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Subscriber " + newSub.getId() + " updated with contract Id : " + newContractId);
                    }
                }
                else
                {
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Unable to find contract for subscriber " + newSub.getId());
                    }
                    throw new HomeException("Unable to find contract Id " + newSub.getSubscriptionContract(ctx));
                }
            }
        }
        final CRMSpid crmspid = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, newSub.getSpid());
        if(crmspid != null && crmspid.isEnableSubscriberContractHistory()){
	        boolean mbgRenewal = ctx.get(Constants.NON_STANDARD_RENEWAL) == null? false: (Boolean)ctx.get(Constants.NON_STANDARD_RENEWAL);
	        Home subscriptionContractHistoryhome = (Home) ctx.get(SubscriptionContractHistoryHome.class);
	        List<SubscriptionContractHistory> preContractHistoryList = 
	        		(List<SubscriptionContractHistory>)HomeSupportHelper.get(ctx).getBeans(ctx, SubscriptionContractHistory.class,
	                											new EQ(SubscriptionContractHistoryXInfo.SUBSCRIPTION_ID, newSub.getId()));
	        if(LogSupport.isDebugEnabled(ctx)){
	        	 LogSupport.debug(ctx, this, 
	        			 "Subscriber [" + newSub.getId() + "], MBG Renewal flag:[" + mbgRenewal + "], existing history is empty:[" + 
	        			 ((preContractHistoryList == null || preContractHistoryList.size() == 0) ? true:false) +
	        			 "], subscription new state:[" + newSub.getState() + "], old contract:[" + oldContractId
	        			 + "], new contract:[" + newContractId + "]");
	        }
	        if(preContractHistoryList == null || preContractHistoryList.size() == 0){
	        	//indicates first time record creation.
	        	subscriptionContractHistoryhome.create(ctx, createNewSubscriptionContractHistory(ctx, newSub, 0));
	        }
	        else if(oldContractId != newContractId){
	        	Collections.sort(preContractHistoryList);//Order by record id, so the last element is the latest
	        	//Get the last element in listDirectDebitOverTimeUpdate
	        	SubscriptionContractHistory lastInsertedContractHistory = preContractHistoryList.get(preContractHistoryList.size() -1);
	        	/**
	        	 * During deactivation of subscriber, if exception occurs in homes below this home, then the transactions done by this home
	        	 * are not rolled over. To handle this behavior, below checks are required.
	        	 */
	        	if((lastInsertedContractHistory.getContractId() != newContractId) || !newSub.getState().equals(SubscriberStateEnum.ACTIVE)){
	        		//Either its normal contract change or subscriber is deactivating.
	        		
		        	lastInsertedContractHistory.setNonStandardRenewal(mbgRenewal);
		    		lastInsertedContractHistory.setContractStatus(Constants.CONTRACT_INACTIVE);
		    		lastInsertedContractHistory.setRecordModifyDate(new Date(System.currentTimeMillis()));
		    		subscriptionContractHistoryhome.store(ctx, lastInsertedContractHistory);
		        	if(newSub.getState().equals(SubscriberStateEnum.ACTIVE)){
		        		//Create a new record
		        		subscriptionContractHistoryhome.create(ctx, 
		        				createNewSubscriptionContractHistory(ctx, newSub, lastInsertedContractHistory.getIdentifier()));
		        	}
	        	}
	        	else{
	        		//Indicates a case where inactivation of subscriber had failed in below homes, and so it is again being reactivated
	        		//with same contract id as before
		    		lastInsertedContractHistory.setContractStatus(Constants.CONTRACT_ACTIVE);
		    		lastInsertedContractHistory.setRecordModifyDate(new Date(System.currentTimeMillis()));
		    		subscriptionContractHistoryhome.store(ctx, lastInsertedContractHistory);
	        	}
	        	
	        }
	        //END
	        }
        
        //Adding suspended period to the SubscriptionContract entity after that updating SubscriptionContractHistory entity
        this.updateSubscriptionContractWithSuspensionPeriod(ctx, obj);        
        
        new InfoLogMsg(this, "Completed contract update STORE", null).log(ctx);
        return getDelegate(ctx).store(ctx, obj);
    }


	/**
	 * Adding SuspensionPeriod, ContractEndDate to the SubscriptionContract entity 
	 * after that updating SubscriptionContractHistory entity's ContractEndDate
	 * @param ctx
	 * @param obj
	 * @throws HomeException
	 * @throws HomeInternalException
	 */
    private void updateSubscriptionContractWithSuspensionPeriod(Context ctx, Object obj)
			throws HomeException, HomeInternalException {

		new InfoLogMsg(this, "Starting SubscriptionContractUpdateHome::updateSubscriptionContractWithSuspensionPeriod",
				null).log(ctx);
		final Subscriber newSub = (Subscriber) obj;
		final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

		if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.ACTIVE)) {

			Home subscriptionContractHome = (Home) ctx.get(SubscriptionContractHome.class);
			SubscriptionContract subscriptionContract = HomeSupportHelper.get(ctx).findBean(ctx,
					SubscriptionContract.class, new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, oldSub.getId()));

			Home subscriptionSuspensionReasonHome = (Home) ctx.get(SubscriptionSuspensionReasonHome.class);

			And filter = new And();
			filter.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, oldSub.getSpid()));
			filter.add(new EQ(SubscriptionSuspensionReasonXInfo.REASONCODE, oldSub.getSuspensionReason()));

			SubscriptionSuspensionReason subscriptionSuspensionReason = HomeSupportHelper.get(ctx).findBean(ctx,
					SubscriptionSuspensionReason.class, filter);

			if ((subscriptionContract != null
					&& (oldSub.getSuspensionDate().before(subscriptionContract.getContractEndDate())
							|| oldSub.getSuspensionDate().equals(subscriptionContract.getContractEndDate())))
					&& subscriptionSuspensionReason != null
					&& subscriptionSuspensionReason.isContractperiodmodification()) {

				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, this,
							"In updateSubscriptionContractWithSuspensionPeriod Subscription SuspensionDate "
									+ oldSub.getSuspensionDate() + ", subscriptionContract previous ContractEndDate was "
									+ subscriptionContract.getContractEndDate());
				}
				
				String suspensionPeriodWithDateFormat = "";

				long tempTotalSuspendedDays = (long) subscriptionContract.getTotalSuspendedDays();

				long totalSuspendedDaysSum = CalendarSupportHelper.get(ctx)
						.getNumberOfDaysBetween(oldSub.getSuspensionDate(), newSub.getResumedDate());

				final Calendar calendar = Calendar.getInstance();
				calendar.setTime(subscriptionContract.getContractEndDate());
				calendar.add(Calendar.DATE, (int) totalSuspendedDaysSum);
				Date endDate = calendar.getTime();

				subscriptionContract.setContractEndDate(endDate);
				
				// UMP-5053 Adding logic for calculating TotalSuspendedDays, in case
				// of subscriber activation after subscription contract end date				
				if (newSub.getResumedDate().after(oldSub.getSubscriptionContractEndDate())
						|| newSub.getResumedDate().equals(oldSub.getSubscriptionContractEndDate()))
				{
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, this,
								"Subscription ResumeDate " + newSub.getResumedDate()
										+ " is after Subscription Contract End Date "
										+ oldSub.getSubscriptionContractEndDate());
					}					
					
					totalSuspendedDaysSum = CalendarSupportHelper.get(ctx)
							.getNumberOfDaysBetween(oldSub.getSuspensionDate(), oldSub.getSubscriptionContractEndDate());
					
					totalSuspendedDaysSum = totalSuspendedDaysSum + tempTotalSuspendedDays;
					
					
					suspensionPeriodWithDateFormat = this.suspensionPeriodWithDateFormat(
							subscriptionContract.getSuspensionPeriod(), oldSub.getSuspensionDate(),
							oldSub.getSubscriptionContractEndDate());

					subscriptionContract.setSuspensionPeriod(suspensionPeriodWithDateFormat);					
					subscriptionContract.setTotalSuspendedDays(totalSuspendedDaysSum);				
					
				}
				else
				{					
					totalSuspendedDaysSum = totalSuspendedDaysSum + tempTotalSuspendedDays;
					
					suspensionPeriodWithDateFormat = this.suspensionPeriodWithDateFormat(
							subscriptionContract.getSuspensionPeriod(), oldSub.getSuspensionDate(),
							newSub.getResumedDate());

					subscriptionContract.setSuspensionPeriod(suspensionPeriodWithDateFormat);
					subscriptionContract.setTotalSuspendedDays(totalSuspendedDaysSum);
				}
				
                Long subsidyAmount = ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM);
                Long penaltyFeePerMonth = ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM);
                Long deviceProductId = ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM);

                if (subsidyAmount != null)
                	subscriptionContract.setSubsidyAmount(ConvertToCurrency(ctx, subsidyAmount));
                if (penaltyFeePerMonth != null)
                	subscriptionContract.setPenaltyFeePerMonth(ConvertToCurrency(ctx, penaltyFeePerMonth));
                if (deviceProductId != null)
                	subscriptionContract.setDeviceProductID(deviceProductId);



				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, this,
							"In SubscriptionContractUpdateHome::updateSubscriptionContractWithSuspensionPeriod "
									+ " SubscriptionContract TotalSuspendedDays "
									+ subscriptionContract.getTotalSuspendedDays()
									+ " ,SubscriptionContract ContractEndDate updated to " + endDate.toString());
				}

				subscriptionContractHome.store(ctx, subscriptionContract);

				// Updating SUBSCRIPTIONCONTRACTHISTORY table with ContractEndDate where contractstatus=Active
				this.updateSubscriptionContractHistoryWithContractEndDate(ctx, obj, endDate);
			}

		}

	}
    
    /**
     * Format suspensionPeriod with Date as YYYYMMDD-YYYYMMDD;...YYYYMMDD-YYYYMMDD
     * @param oldSuspensionPeriod
     * @param oldSubSuspensionDate
     * @param newSubSuspensionDate
     * @return formatted suspensionPeriod
     */
	private String suspensionPeriodWithDateFormat(final String oldSuspensionPeriod, final Date oldSubSuspensionDate,
			final Date newSubSuspensionDate) {
		
		final StringBuffer suspensionPeriod = new StringBuffer();

		if (oldSuspensionPeriod == null || oldSuspensionPeriod.isEmpty()) 
		{
			suspensionPeriod.append(DATE_FORMAT_STRING.format(oldSubSuspensionDate));
			suspensionPeriod.append("-");
			suspensionPeriod.append(DATE_FORMAT_STRING.format(newSubSuspensionDate));
		} 
		else 
		{			
			suspensionPeriod.append(oldSuspensionPeriod);
			suspensionPeriod.append(";");
			suspensionPeriod.append(DATE_FORMAT_STRING.format(oldSubSuspensionDate));
			suspensionPeriod.append("-");
			suspensionPeriod.append(DATE_FORMAT_STRING.format(newSubSuspensionDate));
		}
		
		return suspensionPeriod.toString();
	}
    
	
	/**
	 * Updating SUBSCRIPTIONCONTRACTHISTORY table with ContractEndDate where contractstatus=Active
	 * @param ctx
	 * @param obj
	 * @param endDate
	 * @throws HomeException
	 * @throws HomeInternalException
	 */
	private void updateSubscriptionContractHistoryWithContractEndDate(Context ctx, Object obj, Date endDate)
			throws HomeException, HomeInternalException {

		final Subscriber newSub = (Subscriber) obj;
		Home subscriptionContractHistoryhome = (Home) ctx.get(SubscriptionContractHistoryHome.class);

		And filter = new And();
		filter.add(new EQ(SubscriptionContractHistoryXInfo.SUBSCRIPTION_ID, newSub.getId()));
		filter.add(new EQ(SubscriptionContractHistoryXInfo.CONTRACT_STATUS, Constants.CONTRACT_ACTIVE));

		SubscriptionContractHistory contractHistory = HomeSupportHelper.get(ctx).findBean(ctx,
				SubscriptionContractHistory.class, filter);

		if (contractHistory != null) {
			contractHistory.setContractEndDate(endDate);

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, this,
						"In updateSubscriptionContractHistoryWithContractEndDate updating ContractEndDate with "
								+ contractHistory.getContractEndDate());
			}

			
            Long subsidyAmount = ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM);
            Long penaltyFeePerMonth = ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM);
            Long deviceProductId = ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM);
            
            if (subsidyAmount != null)
            	contractHistory.setSubsidyAmount(ConvertToCurrency(ctx, subsidyAmount));
            if (penaltyFeePerMonth != null)
            	contractHistory.setPenaltyFeePerMonth(ConvertToCurrency(ctx, penaltyFeePerMonth));
            if (deviceProductId != null)
            	contractHistory.setDeviceProductID(deviceProductId);

			
			subscriptionContractHistoryhome.store(ctx, contractHistory);
		}

	}

	private boolean needToTerminateContractOnDeactivation(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub)
    {
        boolean result = false;
        if (newSub.isPostpaid()
                && (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.INACTIVE)))
        {
            long contractId = oldSub.getSubscriptionContract(ctx);
            result = (contractId != SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT)
                    && (contractId != SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED);
        }
        return result;
    }


    /**
     * remove from data store
     * 
     * @param ctx
     * @param obj
     * @exception HomeException
     * @exception HomeInternalException
     */
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        final Subscriber newSub = (Subscriber) obj;
        Home home = (Home) ctx.get(SubscriptionContractHome.class);
        home.removeAll(ctx, new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, newSub.getId()));
        getDelegate(ctx).remove(ctx, obj);
    }
    
    private SubscriptionContractHistory createNewSubscriptionContractHistory(Context ctx, Subscriber newSub, long prevRecordId){
    	
    	String dealerCode = "";
        String userId = "";
        
        CRMRequestHeader header = (CRMRequestHeader)ctx.get(CRMRequestHeader.class);
        
        if(header != null){
        	dealerCode = header.getAgentID();
        	userId = header.getUsername();
        }
        
        
    	SubscriptionContractHistory contractHistory = new SubscriptionContractHistory();
    	contractHistory.setSubscriptionId(newSub.getId());
    	contractHistory.setDealerCode(dealerCode);
    	contractHistory.setUserId(userId);
    	contractHistory.setDeviceType(newSub.getDeviceTypeId());
    	contractHistory.setDeviceModel(newSub.getDeviceName());
    	contractHistory.setContractId(newSub.getSubscriptionContract(ctx));
    	contractHistory.setContractStartDate(newSub.getSubscriptionContractStartDate());
    	contractHistory.setContractEndDate(newSub.getSubscriptionContractEndDate());
    	contractHistory.setContractStatus(newSub.getState().equals(
    										SubscriberStateEnum.INACTIVE) ? Constants.CONTRACT_INACTIVE  : Constants.CONTRACT_ACTIVE);
    	contractHistory.setNonStandardRenewal(false);//by default false for first record
    	contractHistory.setPreviousRecordId(prevRecordId);
    	contractHistory.setRecordCreateDate(new Date(System.currentTimeMillis()));
    	contractHistory.setRecordModifyDate(null);
    	contractHistory.setIdentifier(0);
    	contractHistory.setSpid(newSub.getSpid());

        Long subsidyAmount = ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM);
        Long penaltyFeePerMonth = ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM);
        Long deviceProductId = ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM) == null? null: (Long)ctx.get(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM);
        
        if (subsidyAmount != null)
        	contractHistory.setSubsidyAmount(ConvertToCurrency(ctx, subsidyAmount));
        if (penaltyFeePerMonth != null)
        	contractHistory.setPenaltyFeePerMonth(ConvertToCurrency(ctx, penaltyFeePerMonth));
        if (deviceProductId != null)
        	contractHistory.setDeviceProductID(deviceProductId);

    	return contractHistory;
    }
    
    private long ConvertToCurrency(Context ctx, Long value)
    {
    	Currency currency = (Currency)ctx.get(Currency.class, Currency.DEFAULT);
//    	Long l = 100;
    	     try {
				return currency.parse(value.toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return 0;
			}

    }
}
