package com.trilogy.app.crm.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.DiscountActivityTrigger;
import com.trilogy.app.crm.bean.DiscountActivityTriggerHome;
import com.trilogy.app.crm.bean.DiscountActivityTriggerXInfo;
import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.bean.DiscountCriteriaEnum;
import com.trilogy.app.crm.bean.DiscountEvaluationStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityHome;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventXInfo;
import com.trilogy.app.crm.bean.DiscountStrategyEnum;
import com.trilogy.app.crm.bean.DiscountTransactionHist;
import com.trilogy.app.crm.bean.DiscountTransactionHistHome;
import com.trilogy.app.crm.bean.DiscountTransactionHistXInfo;
//import com.trilogy.app.crm.bean.PaymentOptionEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.contract.AccountContract;
import com.trilogy.app.crm.contract.AccountContractXInfo;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.core.ruleengine.DiscountPriorityTypeEnum;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.discount.CombinationDiscountHandler;
import com.trilogy.app.crm.discount.ContractDiscountHandler;
import com.trilogy.app.crm.discount.CrossSubscriptionDiscountHandler;
import com.trilogy.app.crm.discount.DiscountActivityUtils;
import com.trilogy.app.crm.discount.DiscountHandler;
import com.trilogy.app.crm.discount.FirstDeviceDiscountHandler;
import com.trilogy.app.crm.discount.MasterPackDiscountHandler;
import com.trilogy.app.crm.discount.PairedDiscountHandler;
import com.trilogy.app.crm.discount.SecondDeviceDiscountHandler;
import com.trilogy.app.crm.service.MultiMonthlyPeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.util.MathSupportUtil;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Support methods for discount feature.
 * 
 * @author shailesh.makhijani
 * @since 10.2
 * 
 */

public class DiscountSupportImpl implements DiscountSupport {

	private static final String MODULE = DiscountSupportImpl.class.getName();
	private static final String CHARGEAMOUNT="ChargeAmount";
	private static final String REMAININGAMOUNT="RemainingAmount";

	public static final String DISCOUNT_GRADE ="DiscountGrade";
	
	static int DEFAULT_EXPIRYDAY_EXT = 0;


	/**
	 * This method returns collection of DiscountEvent Beans for specified
	 * states
	 * 
	 * @param ctx
	 * @param state
	 * @return
	 * @throws HomeException
	 */
	public static Collection<DiscountEventActivity> filterDiscountEventbyState(
			final Context ctx, final Collection<DiscountEventStatusEnum> state,
			String ban) throws HomeException {
		final And condition = new And();
		if (state instanceof Set) {
			condition.add(new In(DiscountEventXInfo.STATE, (Set) state));
		} else {
			condition.add(new In(DiscountEventXInfo.STATE, new HashSet(state)));
		}
		condition.add(new EQ(DiscountEventXInfo.BAN, ban));
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "Discount events fetched for "
					+ state.toString());
		}

		ArrayList<DiscountEventActivity> discountEventList = (ArrayList<DiscountEventActivity>) HomeSupportHelper
				.get(ctx).getBeans(ctx, DiscountEventActivity.class, condition);
		Collections.sort(discountEventList,
				new DiscountEventActivityComparator());

		return discountEventList;
	}

	/**
	 * This method will calculate the discount amount of which the transaction
	 * will be generated
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param chargingHistForRemainingAmmount
	 * @param discountType
	 * @param amount
	 * @return
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private static long calculateDiscountAmount(Context ctx,
			DiscountEventActivity discEvent, DiscountClass discountClass,
			long ammountForCalc, long ammountForCapping, int billCycleDay,
			Service service, AuxiliaryService auxService, Subscriber payerSub,
			Date serviceStartDate,
			SubscriberSubscriptionHistory chargingHistForRemainingAmmount,
			final boolean isMultiMonthly,final boolean isPayInAdvance)
			throws HomeInternalException, HomeException {
		if (discountClass == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateDiscountAmount:No discount class found for discount class id "
								+ discEvent.getDiscountClass());
			}
			return 0;
		}

		if (discountClass.isEnableThreshold()
				&& (discountClass.getMinimumTotalChargeThreshold() > ammountForCalc)) {

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"calculateDiscountAmount:For discount class :"
								+ discEvent.getDiscountClass()
								+ " Threshold amount :"
								+ discountClass
										.getMinimumTotalChargeThreshold()
								+ " id greater than charged amount :"
								+ ammountForCalc
								+ "  Hence 0 amount discount will be given");
			}
			return 0;
		}

		if (ActivationFeeModeEnum.FULL.equals(discountClass
				.getDiscountCalculationType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateDiscountAmount:Discount class has activation fee code is Full "
								+ discEvent.getDiscountClass());
				LogSupport.debug(ctx, MODULE,
						"calculateDiscountAmount:Before calling the  calculateFullDiscountAmmount for BAN " + discEvent.getBan());
			}
			return calculateFullDiscountAmmount(ctx, discountClass,
					ammountForCalc, ammountForCapping);
		} else if (ActivationFeeModeEnum.PRORATE.equals(discountClass
				.getDiscountCalculationType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateDiscountAmount:Discount class has activation fee code is ProRate "
								+ discEvent.getDiscountClass());
			}
			return calculateProrateDiscountAmmount(ctx, discEvent,
					discountClass, ammountForCalc, ammountForCapping,
					auxService, chargingHistForRemainingAmmount,isMultiMonthly,isPayInAdvance,billCycleDay);
		} else {
			return 0;
		}
	}


	/**
	 * This method will calculate the discount amount of which the transaction
	 * will be generated
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param chargingHistForRemainingAmmount
	 * @param discountType
	 * @param amount
	 * @return
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private static long calculateReverseDiscountAmount(Context ctx,
			Account account, DiscountEventActivity discEvent,
			DiscountClass discountClass,
			SubscriberSubscriptionHistory chargingHistForChargingAmmount,
			SubscriberSubscriptionHistory chargingHistForRemainingAmmount,
			int billCycleDay, Service service, AuxiliaryService auxService,
			Subscriber payerSub,final boolean isMultiMonthly) throws HomeInternalException, HomeException {
		if (discountClass == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateReverseDiscountAmount:No discount class found for discount class id "
								+ discEvent.getDiscountClass());
			}
			return 0;
		}

		long ammountForCalc = getAmountToBeUsedForDiscountCalc(chargingHistForChargingAmmount);
		long ammountForCapping = getAmountToBeUsedForDiscountCapping(chargingHistForRemainingAmmount);

		if (discountClass.isEnableThreshold()
				&& (discountClass.getMinimumTotalChargeThreshold() > ammountForCalc)) {

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"calculateReverseDiscountAmount:For discount class :"
								+ discEvent.getDiscountClass()
								+ " Threshold amount :"
								+ discountClass
										.getMinimumTotalChargeThreshold()
								+ " id greater than charged amount :"
								+ ammountForCalc
								+ "  Hence 0 amount discount will be given");
			}
			return 0;
		}

		if (ActivationFeeModeEnum.FULL.equals(discountClass
				.getDiscountCalculationType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateReverseDiscountAmount:Discount class has activation fee code is Full "
										+ discEvent.getDiscountClass());
			}
			return calculateFullReverseDiscountAmmount(ctx, discountClass,
					ammountForCalc, ammountForCapping, discEvent);
		} else if (ActivationFeeModeEnum.PRORATE.equals(discountClass
				.getDiscountCalculationType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateReverseDiscountAmount:Discount class has activation fee code is ProRate "
										+ discEvent.getDiscountClass());
			}
			return calculateProrateReverseDiscountAmmount(ctx, account,
					discEvent, discountClass, chargingHistForRemainingAmmount,
					ammountForCapping,billCycleDay,auxService,service,isMultiMonthly);
		} else {
			return 0;
		}
	}

	/**
	 * This method returns calculated Full Discount Amount
	 * and returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @return 
	
	 */
	private static long calculateFullDiscountAmmount(final Context ctx,
			final DiscountClass discountClass, long ammountForCalc,
			long ammountForCapping) {
		
		LogSupport.debug(ctx, MODULE,
				"calculateFullDiscountAmmount:Calculating the full disount-->" + discountClass.getDiscountType());

		if (DiscountCriteriaEnum.FLAT.equals(discountClass.getDiscountType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateFullDiscountAmmount:Discount type is Flat "
								+ "amount for Capping is : "
								+ ammountForCapping
								+ "Flat amount at discount class is"
								+ discountClass.getDiscountFlat());
			}
			return ammountForCapping < discountClass.getDiscountFlat() ? ammountForCapping
					: discountClass.getDiscountFlat();
		} else if (DiscountCriteriaEnum.PERCENTAGE.equals(discountClass
				.getDiscountType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateFullDiscountAmmount:Discount type is Percentage "
								+ "amount for Capping is : "
								+ ammountForCapping + "Percentage is : "
								+ discountClass.getDiscountPercentage()
								+ "Charged amount for the service is : "
								+ ammountForCalc);
			}
			long percentageDiscount = (long) ((discountClass
					.getDiscountPercentage() * ammountForCalc) / 100);
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateFullDiscountAmmount:Percentage discount amount is : "
								+ percentageDiscount);
			}
			return ammountForCapping < percentageDiscount ? ammountForCapping
					: percentageDiscount;
		} else {
			
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateFullDiscountAmmount:Returning the default discount value : 0");
			}
			return 0;
		}
	}

	/**
	 * This method returns calculated Full Discount Amount
	 * in case of cancellation of Discount and returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @return 
	
	 */
	private static long calculateFullReverseDiscountAmmount(final Context ctx,
			final DiscountClass discountClass, final long ammountForCalc,
			final long ammountForCapping,
			final DiscountEventActivity discEventActivity) {

		if (isChargeApplicable(discEventActivity)) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateFullReverseDiscountAmmount:Calculating charge discount amount for discount event activity whose discount transaction is not created"
										+ "And discount is discontinued");
			}
			if (DiscountCriteriaEnum.FLAT.equals(discountClass
					.getDiscountType())) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"calculateFullReverseDiscountAmmount:Discount type is Flat "
											+ "amount for Capping for refund discount is : "
											+ ammountForCapping
											+ "Flat amount at discount class for refund discount is"
											+ discountClass.getDiscountFlat());
				}
				return ammountForCapping < discountClass.getDiscountFlat() ? ammountForCapping
						: discountClass.getDiscountFlat();
			} else if (DiscountCriteriaEnum.PERCENTAGE.equals(discountClass
					.getDiscountType())) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateFullReverseDiscountAmmount:Discount type is Percentage "
									+ "amount for Capping is : "
									+ ammountForCapping + "Percentage is : "
									+ discountClass.getDiscountPercentage()
									+ "Charged amount for the service is : "
									+ ammountForCalc);
				}
				long percentageDiscount = (long) ((discountClass
						.getDiscountPercentage() * ammountForCalc) / 100);

				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateFullReverseDiscountAmmount:Percentage Reverse discount amount is : "
									+ percentageDiscount);
				}
				return ammountForCapping < percentageDiscount ? ammountForCapping
						: percentageDiscount;
			}
		} else if (isRefundApplicable(discEventActivity)) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateFullReverseDiscountAmmount:Calculating Refund discount amount for discount event activity whose discount transaction is  created"
										+ "And discount is discontinued");
			}
			return getRefundDiscountAmountFromDiscountHistory(ctx,
					discEventActivity);
		}
		return 0;
	}


	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static long calculateProrateDiscountAmmount(final Context ctx,
			final DiscountEventActivity discEventActivity,
			final DiscountClass discountClass, final long ammountForCalc,
			final long ammountForCapping, final AuxiliaryService auxService,
			SubscriberSubscriptionHistory chargingHistForRemainingAmmount,
			final boolean isMultiMonthly,final boolean isPayInAdvance,final int billCycleDay) {
		long totalFee;
		And where;
		SubscriberAuxiliaryService sAS=null;
		SubscriberServices ss=null;
		try {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateDiscountAmmount:Calculating Prorated discount amount for discount event activity whose discount transaction is  not created");
			}
			if (discEventActivity.getDiscountType().equals(
					DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT)) {
				where = new And();
				where.add(new EQ(
						SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER,
						discEventActivity.getSubId()));
				where.add(new EQ(
						SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER,
						discEventActivity.getServiceId()));
				where.add(new EQ(
						SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER,
						discEventActivity.getServiceInstance()));
				sAS = HomeSupportHelper.get(ctx)
						.findBean(ctx, SubscriberAuxiliaryService.class, where);
				if (null != sAS && sAS.getIsfeePersonalizationApplied()) {
					totalFee = sAS.getPersonalizedFee();
				} else {
					totalFee = auxService.getCharge();
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateDiscountAmmount:Fetching Subscriber Auxiliary service's fee"
									+ totalFee);
				}

			} else {
				where = new And();
				where.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID,
						discEventActivity.getSubId()));
				where.add(new EQ(SubscriberServicesXInfo.SERVICE_ID,
						discEventActivity.getServiceId()));
				ss = HomeSupportHelper.get(ctx).findBean(
						ctx, SubscriberServices.class, where);
				if (null != ss && ss.getIsfeePersonalizationApplied()) {
					totalFee = ss.getPersonalizedFee();
				} else {
					PricePlanVersion version = PricePlanSupport
							.getCurrentVersion(ctx,
									discEventActivity.getPricePlanId());
					// Assumption there will be only 1 mandatory service in PP
					ServiceFee2 serviceFee = version.getMandatoryService(ctx);
					totalFee = serviceFee.getFee();
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateDiscountAmmount:Fetching Subscriber Service's fee"
									+ totalFee);
				}
			}
			Date toDate=null;
			if(isMultiMonthly)
			{
				if (discEventActivity.getDiscountType().equals(
						DiscountEventActivityTypeEnum.MASTER_PACK_DISCOUNT)) {
					
				toDate=MultiMonthlyPeriodHandler.instance()
					.calculateCycleEndDate(ctx,
							discEventActivity.getDiscountEffectiveDate(),
							billCycleDay, discEventActivity.getSpid(),
							discEventActivity.getSubId(), sAS);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateDiscountAmmount:IsMultiMonthly Aux service");
									
				}
					
				}else
				{
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, MODULE,
								"calculateProrateDiscountAmmount:IsMultiMonthly Subscriber service");
										
					}
					toDate=	MultiMonthlyPeriodHandler.instance()
							.calculateCycleEndDate(ctx,
									discEventActivity.getDiscountEffectiveDate(),
									billCycleDay, discEventActivity.getSpid(),
									discEventActivity.getSubId(), ss);
					
				}
			    
			}else{
				
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateDiscountAmmount:IsMonthly service");
									
				}
				toDate= BillCycleSupport.getDateForBillCycleEnd(
						ctx,
						chargingHistForRemainingAmmount.getTimestamp_(),
						BillCycleSupport.getBillCycleForBan(ctx,
								discEventActivity.getBan()).getDayOfMonth());
			}
			

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateDiscountAmmount:Todate calculated for discount amount calculation : "
										+ toDate);
			}
			Date endDateForDiscount = discEventActivity
					.getDiscountExpirationDate();
			if (endDateForDiscount == null) {
				// if expiration is null, we need to calculate the discount till
				// end of the month
				endDateForDiscount = toDate;
			}
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateDiscountAmmount:End date calculated for discount amount calculation"
										+ endDateForDiscount);
			}
			Date fromDate = null;
			if (null == discEventActivity.getDiscountAppliedTillDate()
					|| 0 == discEventActivity.getDiscountAppliedTillDate()
							.getTime()
					|| discEventActivity.getDiscountAppliedTillDate().before(
							discEventActivity.getDiscountEffectiveDate())) {
				fromDate = discEventActivity.getDiscountEffectiveDate();
			} else {
				fromDate = CalendarSupportHelper.get(ctx).getDaysAfter(discEventActivity.getDiscountAppliedTillDate(),1);
			}
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateDiscountAmmount:From date calculated for discount amount calculation : "
										+ fromDate);
			}
			double noOfApplicableDaysForDiscount = getNoOfDaysBetweenDates(ctx,
					fromDate, endDateForDiscount);

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateDiscountAmmount:No. of ApplicableDaysForDiscount calculated for discount amount calculation : "
										+ noOfApplicableDaysForDiscount);
			}
			double noOfChargeDates = getNoOfDaysBetweenDates(ctx,
					chargingHistForRemainingAmmount.getTimestamp_(), toDate);

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateDiscountAmmount:No. of ChargeDays calculated for discount amount calculation : "
										+ noOfChargeDates);
			}
			double amount = noOfApplicableDaysForDiscount
					/ noOfChargeDates * ammountForCalc;
 
			BigDecimal decimalAmountChargedForTheDiscountedPeriod = BigDecimal.valueOf(amount); 
			final long ammountChargedForTheDiscountedPeriod;

	        // Always round the absolute value to get the correct rounding.
			ammountChargedForTheDiscountedPeriod = MathSupportUtil.round(ctx, discEventActivity.getSpid(), decimalAmountChargedForTheDiscountedPeriod);
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateProrateDiscountAmmount:amount calculated from charge : "
								+ ammountChargedForTheDiscountedPeriod);
			}
			if (DiscountCriteriaEnum.FLAT.equals(discountClass
					.getDiscountType())) {
				long discamount = (ammountChargedForTheDiscountedPeriod * discountClass
						.getDiscountFlat()) / totalFee;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateDiscountAmmount: Discount class type is flat"
									+ " total fee is : " + totalFee
									+ " discount amount is : " + discamount
									+ " Amount capping is : "
									+ ammountForCapping
									+ " Discount flat amount is : "
									+ discountClass.getDiscountFlat());

				}
				return ammountForCapping < discamount ? ammountForCapping
						: discamount;
			} else if (DiscountCriteriaEnum.PERCENTAGE.equals(discountClass
					.getDiscountType())) {
				long percentageDiscount = (long) ((discountClass
						.getDiscountPercentage() * ammountChargedForTheDiscountedPeriod) / 100);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateDiscountAmmount: Discount class type is flat"
									+ " total fee is : " + totalFee
									+ " percentage discount amount is : "
									+ percentageDiscount
									+ " Amount capping is : "
									+ ammountForCapping);

				}
				return ammountForCapping < percentageDiscount ? ammountForCapping
						: percentageDiscount;
			} else {
				return 0;
			}
		} catch (HomeInternalException e) {
			return 0;
		} catch (HomeException e) {
			return 0;
		}
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 * in case of cancellation of discount and  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param account
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param billCycleDay
	 * @param service
	 * @return 
	 * @throws HomeException
	 */
	private static long calculateProrateReverseDiscountAmmount(
			final Context ctx,
			final Account account,
			final DiscountEventActivity discEventActivity,
			DiscountClass discountClass,
			final SubscriberSubscriptionHistory chargingHistForRemainingAmmount,
			long ammountForCapping,
			final int billCycleDay,
			final AuxiliaryService auxService,
			final Service service,
			final boolean isMultiMonthly) throws HomeException {

		if (isChargeApplicable(discEventActivity)) {
			// calc no of applicable days from (till date/effective date to exp
			// date)
			// get charge history between till date and exp date, if till date
			// is null, get latest charge before exp date
			// calc no of days for which the charge is applied
			// noOfApplicableDays/noOfChargeDays * calculated discount ammount
			// (this ammount is for the complete charge period)
			// chargingHistForRemainingAmmount =

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateReverseDiscountAmmount: Calculating discount amount for the activity whose transaction is not created"
										+ " and disount is discontinued");
			}
			Date fromDate = null;
			if (null == discEventActivity.getDiscountAppliedTillDate()
					|| 0 == discEventActivity.getDiscountAppliedTillDate()
							.getTime()
					|| discEventActivity.getDiscountAppliedTillDate().before(
							discEventActivity.getDiscountEffectiveDate())) {
				fromDate = discEventActivity.getDiscountEffectiveDate();

			} else {
				fromDate = CalendarSupportHelper.get(ctx).getDaysAfter(discEventActivity.getDiscountAppliedTillDate(),1);
			}

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateReverseDiscountAmmount:Fromdate calculated for discount amount calculation"
										+ fromDate);
			}
			double noOfApplicableDaysForDiscount = getNoOfDaysBetweenDates(ctx,
					fromDate, discEventActivity.getDiscountExpirationDate());

			// TODO: toDate calculation will change for multimonthly
			Date toDate = null;
			if (isMultiMonthly) {
				// in case of multi-monthly service, the charge's end date can be two or more months after
				//the current date.
				if(null!=service)
				{
					try
					{
					Service serviceObj = (Service)service.deepClone();
					serviceObj.setStartDate(discEventActivity.getDiscountEffectiveDate());
				    toDate = MultiMonthlyPeriodHandler.instance().calculateCycleEndDate(ctx, chargingHistForRemainingAmmount.getTimestamp_(), 
				        account.getBillCycleDay(ctx), account.getSpid(),discEventActivity.getSubId(),serviceObj);
					}catch(Exception ex)
					{
						LogSupport.info(ctx,MODULE,"Exception occured while calculating cycle end date so setting cycle end date as Current month's end date");
						toDate = BillCycleSupport.getDateForBillCycleEnd(ctx,
								chargingHistForRemainingAmmount.getTimestamp_(),
								BillCycleSupport.getBillCycleForBan(ctx, account.getBAN())
										.getDayOfMonth());
					}
				}else if(null!=auxService)
				{
					try
					{
					AuxiliaryService auxServiceObj = (AuxiliaryService)auxService.deepClone();
					auxServiceObj.setStartDate(discEventActivity.getDiscountEffectiveDate());
					toDate = MultiMonthlyPeriodHandler.instance().calculateCycleEndDate(ctx, chargingHistForRemainingAmmount.getTimestamp_(), 
					        account.getBillCycleDay(ctx), account.getSpid(),discEventActivity.getSubId(),auxServiceObj);
					}catch(Exception ex)
					{
						LogSupport.info(ctx,MODULE,"Exception occured while calculating cycle end date so setting cycle end date as Current month's end date");
						toDate = BillCycleSupport.getDateForBillCycleEnd(ctx,
								chargingHistForRemainingAmmount.getTimestamp_(),
								BillCycleSupport.getBillCycleForBan(ctx, account.getBAN())
										.getDayOfMonth());
					}
				}
						
			}else{
				toDate =BillCycleSupport.getDateForBillCycleEnd(ctx,
					chargingHistForRemainingAmmount.getTimestamp_(),
					BillCycleSupport.getBillCycleForBan(ctx, account.getBAN())
							.getDayOfMonth());
			}
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateReverseDiscountAmmount:toDate calculated for discount amount calculation"
										+ toDate);
			}
			double noOfChargeDates = getNoOfDaysBetweenDates(ctx,
					chargingHistForRemainingAmmount.getTimestamp_(), toDate);

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateReverseDiscountAmmount:No Of Charge Date calculated for discount amount calculation"
										+ noOfChargeDates);
			}
			double amount = noOfApplicableDaysForDiscount
					/ noOfChargeDates
					* chargingHistForRemainingAmmount.getChargedAmount();
			BigDecimal decimalAmount = BigDecimal.valueOf(amount); 
			final long ammountChargedForTheDiscountedPeriod;

		        // Always round the absolute value to get the correct rounding.
			 ammountChargedForTheDiscountedPeriod = MathSupportUtil.round(ctx, discEventActivity.getSpid(), decimalAmount);

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateProrateReverseDiscountAmmount:amount calculated from charge"
								+ ammountChargedForTheDiscountedPeriod);
			}
			long discountAmmount = 0;
			if (DiscountCriteriaEnum.FLAT.equals(discountClass
					.getDiscountType())) {
				// in case of flat-prorated discount class, the flat discount
				// defined on discount class needs to be calculated
				// on proration basis
				double flatDiscountApplicable = noOfApplicableDaysForDiscount
						/ noOfChargeDates * discountClass.getDiscountFlat();
				BigDecimal decimalAmountForApplicableDiscount = BigDecimal.valueOf(flatDiscountApplicable); 
				final long flatDiscountApplicableForPeriod;

			        // Always round the absolute value to get the correct rounding.
				flatDiscountApplicableForPeriod = MathSupportUtil.round(ctx, discEventActivity.getSpid(), decimalAmountForApplicableDiscount);

				long minValue = ammountChargedForTheDiscountedPeriod < flatDiscountApplicableForPeriod ? ammountChargedForTheDiscountedPeriod
						: flatDiscountApplicableForPeriod;
				// need to cap this minValue of discount
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"calculateProrateReverseDiscountAmmount: Discount class type is flat"
											+ " Minimum value between flat amount and calculated discount amount is : "
											+ minValue
											+ " discount amount is : "
											+ flatDiscountApplicableForPeriod
											+ " Amount capping is : "
											+ ammountForCapping
											+ " Discount flat amount is : "
											+ discountClass.getDiscountFlat());

				}
				discountAmmount = ammountForCapping < minValue ? ammountForCapping
						: minValue;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateReverseDiscountAmmount:amount calculated for discount is"
									+ discountAmmount);
				}
			} else if (DiscountCriteriaEnum.PERCENTAGE.equals(discountClass
					.getDiscountType())) {
				long percentageDiscount = (long) ((discountClass
						.getDiscountPercentage() * ammountChargedForTheDiscountedPeriod) / 100);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"calculateProrateReverseDiscountAmmount: Discount class type is percentage"
											+ " discount amount for chanrged days is :  "
											+ ammountChargedForTheDiscountedPeriod
											+ " percentage discount amount is : "
											+ percentageDiscount
											+ " Amount capping is : "
											+ ammountForCapping);

				}
				discountAmmount = ammountForCapping < percentageDiscount ? ammountForCapping
						: percentageDiscount;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"calculateProrateReverseDiscountAmmount:amount calculated for discount is"
									+ discountAmmount);
				}
			}
			return discountAmmount;

		} else if (isRefundApplicable(discEventActivity)) {

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"calculateProrateReverseDiscountAmmount: Calculating discount amount for the activity whose transaction is created"
										+ " and discount is discontinued");
			}
			double daysApplicableForRefund = getNoOfApplicableDaysForRefund(ctx,
					discEventActivity);
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateProrateReverseDiscountAmmount:Days applicable for refund is : "
								+ daysApplicableForRefund);
			}
			double daysForDiscountGiven = getNoOfDaysBetweenDates(ctx,
					discEventActivity.getDiscountAppliedFromDate(),
					discEventActivity.getDiscountAppliedTillDate());
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"calculateProrateReverseDiscountAmmount:Days for discount given is is : "
								+ daysForDiscountGiven);
			}
			double ammountCharged = daysApplicableForRefund
					/ daysForDiscountGiven
					* getRefundDiscountAmountFromDiscountHistory(ctx,
							discEventActivity);
			BigDecimal decimalAmountChargedForTheDiscountedPeriod = BigDecimal.valueOf(ammountCharged); 
			final long ammountChargedForTheDiscountedPeriod;
           
	        // Always round the absolute value to get the correct rounding.
			ammountChargedForTheDiscountedPeriod = MathSupportUtil.round(ctx, discEventActivity.getSpid(), decimalAmountChargedForTheDiscountedPeriod);

			//As below code is calculating on the reverse amount for discount and thus
			//giving wrong value
			/*long discountAmmount = 0;
			if (DiscountCriteriaEnum.FLAT.equals(discountClass
					.getDiscountType())) {
				discountAmmount = ammountChargedForTheDiscountedPeriod < discountClass
						.getDiscountFlat() ? ammountChargedForTheDiscountedPeriod
						: discountClass.getDiscountFlat();
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"calculateProrateReverseDiscountAmmount: Discount class type is flat"
											+ " amount charged for discounting period : "
											+ ammountChargedForTheDiscountedPeriod
											+ " discount amount is : "
											+ discountAmmount
											+ " Amount capping is : "
											+ ammountForCapping
											+ " Discount flat amount is : "
											+ discountClass.getDiscountFlat());

				}
			} else if (DiscountCriteriaEnum.PERCENTAGE.equals(discountClass
					.getDiscountType())) {
				discountAmmount = (long) ((discountClass
						.getDiscountPercentage() * ammountChargedForTheDiscountedPeriod) / 100);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"calculateProrateReverseDiscountAmmount: Discount class type is percentage"
											+ " discount amount for chanrged days is :  "
											+ ammountChargedForTheDiscountedPeriod
											+ " percentage discount amount is : "
											+ discountAmmount
											+ " Amount capping is : "
											+ ammountForCapping);

				}
			}
			return discountAmmount;
			*/
			return ammountChargedForTheDiscountedPeriod;
		}
		return 0;
	}

	/**
	 * This method returns calculated refund Discount Amount
	 * from DiscountTransactionHist  in case of cancellation
	 * of discount and  returns the amount
	 * 
	 * @param ctx
	 * @param discEventActivity
	 * @return 
	 *
	 */
	private static long getRefundDiscountAmountFromDiscountHistory(
			final Context ctx, final DiscountEventActivity discEventActivity) {
		long discAmount = 0;
		try {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"getRefundDiscountAmountFromDiscountHistory:Getting Discount transaction hist for the discount event activity"
										+ discEventActivity.getId());

			}
			final Home home = (Home) ctx.get(DiscountTransactionHistHome.class);
			And condition = new And();
			condition.add(new EQ(
					DiscountTransactionHistXInfo.EVENT_ACTIVITY_ID,
					discEventActivity.getId()));
			condition.add(new GTE(
					DiscountTransactionHistXInfo.CREATION_TIME_STAMP,
					discEventActivity.getDiscountAppliedFromDate().getTime()));
			condition.add(new LTE(
					DiscountTransactionHistXInfo.CREATION_TIME_STAMP,
					CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(discEventActivity.getDiscountAppliedTillDate()).getTime()));

			DiscountTransactionHist discTranHist = (DiscountTransactionHist) home
					.find(ctx, condition);
			if (null != discTranHist) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"getRefundDiscountAmountFromDiscountHistory:Getting Matched Discount transaction hist for the discount event activity"
											+ discEventActivity.getId()
											+ " and amount is : "
											+ discTranHist.getAmount());

				}
				discAmount = discTranHist.getAmount();
			}

		} catch (Exception ex) {
			LogSupport
					.minor(ctx,
							MODULE,
							"getRefundDiscountAmountFromDiscountHistory:Exception Occured while fetching amount from DiscountTransactionHis"
									+ ex.getMessage());
		}
		return discAmount;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static long getBillCycleDate(final Context ctx, String ban)
			throws HomeException {
		Account account = HomeSupportHelper.get(ctx).findBean(ctx,
				Account.class, ban);
		final Calendar transactionDate = Calendar.getInstance();
		transactionDate
				.set(Calendar.DAY_OF_MONTH, account.getBillCycleDay(ctx));
		Date bcd_Date = transactionDate.getTime();
		long bcd_Long = bcd_Date.getTime();
		return bcd_Long;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static void generateDiscountTransctionForAccount(Context ctx,
			String ban) throws HomeException {
		Set enumValues = new HashSet();
		enumValues.add(DiscountEventActivityStatusEnum.ACTIVE);
		enumValues.add(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);

		ArrayList<DiscountEventActivity> discountEventList = (ArrayList<DiscountEventActivity>) DiscountSupportImpl
				.filterDiscountEventbyState(ctx, enumValues, ban);
		String subcriberID = null;
		for (DiscountEventActivity discEvent : discountEventList) {
			/*
			 * Added for Contracct Discount applicability as per spid config
			 * 
			 * if (discEvent.getDiscountType().equals(
			 * DiscountEventActivityTypeEnum.CONTRACT_DISCOUNT) &&
			 * !discEvent.getState().equals(
			 * DiscountEventActivityStatusEnum.CANCELLATION_PENDING)) { Home
			 * subscriberHome = (Home) ctx.get(SubscriberHome.class); Subscriber
			 * subobj = (Subscriber) subscriberHome.find(ctx,
			 * discEvent.getSubId());
			 * 
			 * 
			 * if (SubscriptionSuspensionReasonMappingSupport
			 * .isDiscountNotApplicable(ctx, subobj)) { //
			 * changeDiscountCheck(ctx
			 * ,discEvent.getId(),discEvent.getContractId()); continue; } else {
			 * discEvent.setState(DiscountEventActivityStatusEnum.ACTIVE); } }
			 */
			/*
			 *
			 */
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"Verfying if Discount Already given for discounEvent bean and Generating Transactions if not present : "
										+ discEvent.toString());
			}

			long billCycleDate = DiscountSupportImpl.getBillCycleDate(ctx,
					discEvent.getBan());
			if (discEvent.getDiscountAppliedTillDate().getTime() == 0
					|| billCycleDate > discEvent.getDiscountAppliedTillDate()
							.getTime()) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"Applying Discount for Account: " + ban
									+ "  And discount event "
									+ discEvent.getId());
				}
				if (subcriberID == null) {
					subcriberID = discEvent.getSubId();
				}
				if (subcriberID.equals(discEvent.getSubId())) {
					if (DiscountEventTypeEnum.MASTER_PACK_DISCOUNT
							.equals(discEvent.getDiscountType())) {
						// In case of master pack discount, for each fee
						// transaction there can be only one discount applicable
						// thus, no need to manage discountGiven counter for
						// multiple discounts
						DiscountSupportImpl.generateDiscountTransactions(ctx,
								discEvent);
					} else {
						DiscountSupportImpl.generateDiscountTransactions(ctx,
								discEvent);
					}
				} else {
					subcriberID = discEvent.getSubId();
					DiscountSupportImpl.generateDiscountTransactions(ctx,
							discEvent);
				}
			} else {
				LogSupport.info(ctx, MODULE, "Discount Already Given For"
						+ discEvent.toString());
				/*
				 * Commenting as the below line would throw Concurrent
				 * Modification exception
				 */
				// discountEventList.remove(discEvent);
			}
		}
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static Transaction createDiscountTransaction(Context ctx,
			Subscriber sub, String supportedSubscriberID, long discAmount,
			int adjustmenttype, int taxAuthority,
			AdjustmentTypeActionEnum action, DiscountEventActivity discEvent,
			final SubscriberSubscriptionHistory histForTheDiscount)
			throws HomeException {
		Transaction returnTrans = null;

		if (discEvent.getDiscountType().equals(
				DiscountEventActivityTypeEnum.FIRST_DEVICE)) {
			Service associatedService = ServiceSupport.getService(ctx,
					discEvent.getServiceId());
			int linkedAdjustment = -1;
			if (associatedService != null) {
				linkedAdjustment = associatedService.getAdjustmentType();
			}
			returnTrans = TransactionSupport.createLinkedTransaction(ctx, sub,
					supportedSubscriberID, discAmount, (long) 0,
					new DefaultAdjustmentTypeSupport().getAdjustmentType(ctx,
							adjustmenttype), taxAuthority, false, false, "",
					new Date(), getRecieveDate(ctx, histForTheDiscount), "", action, linkedAdjustment);
		} else {

			returnTrans = TransactionSupport.createTransaction(ctx, sub,
					supportedSubscriberID, discAmount, (long) 0,
					new DefaultAdjustmentTypeSupport().getAdjustmentType(ctx,
							adjustmenttype), taxAuthority, false, false, "",
					new Date(), getRecieveDate(ctx, histForTheDiscount), "", DEFAULT_EXPIRYDAY_EXT,
					action);
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"createDiscountTransaction : Discount transaction has been created");

		}

		if (null != returnTrans) {
			// create discount transaction history
			DiscountTransactionHist discountTransHist = new DiscountTransactionHist();
			discountTransHist.setServiceId(discEvent.getServiceId());
			discountTransHist.setDiscountServiceType(discEvent
					.getDiscountServiceType());
			discountTransHist.setCreationTimeStamp(new Date().getTime());
			discountTransHist.setDiscountAdjustmentType(adjustmenttype);
			discountTransHist.setReceiptNum(returnTrans.getReceiptNum());
			discountTransHist.setDiscountReceiveDate(returnTrans
					.getReceiveDate().getTime());
			discountTransHist.setBan(discEvent.getBan());
			discountTransHist.setSubId(discEvent.getSubId());
			if (null != supportedSubscriberID) {
				// if supported subscriber is not null, the sub passed to the
				// method is
				discountTransHist.setPayerId(sub.getBAN());
			}
			discountTransHist.setAmount(discAmount);
			discountTransHist.setEventActivityId(discEvent.getId());
			final Home home = (Home) ctx.get(DiscountTransactionHistHome.class);
			home.create(discountTransHist);
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"createDiscountTransaction : Discount transaction History has been created");
			}
		}

		return returnTrans;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static Transaction createReverseDiscountTransaction(Context ctx,
			Subscriber sub, String supportedSubscriberID, long discAmount,
			int adjustmenttype, int taxAuthority,
			AdjustmentTypeActionEnum action, DiscountEventActivity discEvent,
			final SubscriberSubscriptionHistory histForTheDiscount,final Account ban)
			throws HomeException {
		Transaction returnTrans = null;

		Date transactionReceivedDate = new Date();
		long receivedDateOfLastTransaction = getDiscountTransactionReciptFromDiscountHistory(ctx, discEvent);
		if(!isTransactionInvoiced(ctx,receivedDateOfLastTransaction, ban))
		{
			transactionReceivedDate = new Date(getTransactionReceivedDate(ctx, receivedDateOfLastTransaction, ban));
		}
		if (discEvent.getDiscountType().equals(
				DiscountEventActivityTypeEnum.FIRST_DEVICE)) {
			Service associatedService = ServiceSupport.getService(ctx,
					discEvent.getServiceId());
			int linkedAdjustment = -1;
			if (associatedService != null) {
				linkedAdjustment = associatedService.getAdjustmentType();
			}
			returnTrans = TransactionSupport.createLinkedTransaction(ctx, sub,
					supportedSubscriberID, discAmount, (long) 0,
					new DefaultAdjustmentTypeSupport().getAdjustmentType(ctx,
							adjustmenttype), taxAuthority, false, false, "",
					new Date(), transactionReceivedDate, "", action, linkedAdjustment);
		} else {

			returnTrans = TransactionSupport.createTransaction(ctx, sub,
					supportedSubscriberID, discAmount, (long) 0,
					new DefaultAdjustmentTypeSupport().getAdjustmentType(ctx,
							adjustmenttype), taxAuthority, false, false, "",
					new Date(), transactionReceivedDate, "", DEFAULT_EXPIRYDAY_EXT,
					action);
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"createReverseDiscountTransaction : Discount transaction has been created");

		}

		if (null != returnTrans) {
			// create discount transaction history
			DiscountTransactionHist discountTransHist = new DiscountTransactionHist();
			discountTransHist.setServiceId(discEvent.getServiceId());
			discountTransHist.setDiscountServiceType(discEvent
					.getDiscountServiceType());
			discountTransHist.setCreationTimeStamp(new Date().getTime());
			discountTransHist.setDiscountAdjustmentType(adjustmenttype);
			discountTransHist.setReceiptNum(returnTrans.getReceiptNum());
			discountTransHist.setDiscountReceiveDate(returnTrans
					.getReceiveDate().getTime());
			discountTransHist.setBan(discEvent.getBan());
			discountTransHist.setSubId(discEvent.getSubId());
			if (null != supportedSubscriberID) {
				// if supported subscriber is not null, the sub passed to the
				// method is
				discountTransHist.setPayerId(sub.getBAN());
			}
			discountTransHist.setAmount(discAmount);
			discountTransHist.setEventActivityId(discEvent.getId());
			final Home home = (Home) ctx.get(DiscountTransactionHistHome.class);
			home.store(discountTransHist);
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"createReverseDiscountTransaction : Discount transaction History has been created");
			}
		}

		return returnTrans;
	}
	
	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static Date getRecieveDate(final Context ctx,
			final SubscriberSubscriptionHistory chargeHistForTheDiscount) {

		String transactionId = chargeHistForTheDiscount.getTransactionId();
		final And condition = new And();
		condition.add(new EQ(TransactionXInfo.RECEIPT_NUM, transactionId));

		try {
			Transaction transaction = (Transaction) HomeSupportHelper.get(ctx)
					.findBean(ctx, Transaction.class, condition);
			return null == transaction ? new Date() : transaction
					.getReceiveDate();
		} catch (HomeInternalException e) {
			LogSupport.minor(ctx, MODULE,"getRecieveDate : Exception Occured while fetching transaction from charge history" + e.getMessage());
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE,"getRecieveDate : Exception Occured while fetching transaction from charge history" + e.getMessage());
		}
		return new Date();
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static void updateContractDiscountCount(Context ctx,
			DiscountEventActivity discEventActivity) {
		try{
			And cnd = new And();
			cnd.add(new EQ(AccountContractXInfo.CONTRACT_ID, discEventActivity
					.getContractId()));
			cnd.add(new EQ(AccountContractXInfo.BAN, discEventActivity.getBan()));
			AccountContract act = HomeSupportHelper.get(ctx).findBean(ctx,
					AccountContract.class, cnd);
			if (null != act) {
				act.setDiscountAppliedCount(act.getDiscountAppliedCount() + 1);
				HomeSupportHelper.get(ctx).storeBean(ctx, act);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"updateContractDiscountCount : Discount applied count is incremented for BAN "
									+ discEventActivity.getBan());
				}
			}
		}catch(Exception e)
		{
			LogSupport.minor(ctx, MODULE,"Exception Occured while updating discount count" + e.getMessage());
		}
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static Subscriber getPayerSubForDiscount(final Context ctx,
			final Subscriber sub, final Service service,
			final SubscriberServices subService, final boolean isMultiMontly,
			final boolean isMontlyPayInAdvance) throws HomeException {
		Subscriber payerSub = sub;
		Subscriber genericSubForPayer;
		String calcPayerBan = "";

		// Check if payer exits and if yes create discounts for Payer.
		if (isMultiMontly || isMontlyPayInAdvance) {
			//Not supporting the payerPayee feature
			/*calcPayerBan = PayerPayeeUtil.getPayerBanForServiceChargeDate(ctx,
					sub, service, subService);*/
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForDiscount : Payer for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" is : " + calcPayerBan);
			}
			if (calcPayerBan == null || calcPayerBan.isEmpty()
					|| sub.getBAN().equals(calcPayerBan)) {
				// No payer found, discount will be created against the current
				// subscriber
				payerSub = sub;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForDiscount : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" is : " + payerSub);
				}
			} /*else {
				// found the payer of service, discount transaction will be
				// created for it in case of refund..
				genericSubForPayer = SubscriberSupport.getGenericSubscriber(
						ctx, calcPayerBan);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForDiscount : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" is : " + genericSubForPayer);
				}
				payerSub = genericSubForPayer;
			}*/
		} /*else if (subService.getPayer() == null
				|| subService.getPayer().isEmpty()
				|| subService.getPayer().equals(sub.getBAN())) {
			
			payerSub = sub;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForDiscount : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" is : " + payerSub);
			}
		} else {
			genericSubForPayer = SubscriberSupport.getGenericSubscriber(ctx,
					subService.getPayer());
			payerSub = genericSubForPayer;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForDiscount : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" is : " + genericSubForPayer);
			}
		}*/
		return payerSub;

	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	/*private static Subscriber getPayerSubForAuxServiceDiscount(
			final Context ctx, final Subscriber sub,
			final AuxiliaryService auxService,
			final SubscriberAuxiliaryService subAuxService,
			final boolean isMultiMontly, final boolean isMontlyPayInAdvance)
			throws HomeException {
		Subscriber payerSub;
		Subscriber genericSubForPayer;
		String calcPayerBan = "";

		// Check if payer exits and if yes create discounts for Payer.
		if (isMultiMontly || isMontlyPayInAdvance) {
			//Not supporting the PayerPayee feature
			/*calcPayerBan = PayerPayeeUtil.getPayerBanForAuxServiceChargeDate(
					ctx, sub, auxService, subAuxService);*/
		/*	if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForAuxServiceDiscount : Payer for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" for Aux Service is : " + calcPayerBan);
			}
			if (calcPayerBan == null || calcPayerBan.isEmpty()
					|| sub.getBAN().equals(calcPayerBan)) {
				// No payer found, discount will be created against the current
				// subscriber
				payerSub = sub;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForAuxServiceDiscount : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" for Aux Service is : " + payerSub);
				}
			} else {
				// found the payer of service, discount transaction will be
				// created for it in case of refund..
				genericSubForPayer = SubscriberSupport.getGenericSubscriber(
						ctx, calcPayerBan);
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForAuxServiceDiscount : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" for Aux Service is : " + genericSubForPayer);
				}
				payerSub = genericSubForPayer;
			}
		} else if (subAuxService.getPayer() == null
				|| subAuxService.getPayer().isEmpty()
				|| subAuxService.getPayer().equals(sub.getBAN())) {
			payerSub = sub;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForAuxServiceDiscount : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" for Aux Service is : " + payerSub);
			}
		} else {
			genericSubForPayer = SubscriberSupport.getGenericSubscriber(ctx,
					subAuxService.getPayer());
			payerSub = genericSubForPayer;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForAuxServiceDiscount : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" for Aux Service is : " + genericSubForPayer);
			}
		}
		return payerSub;

	}*/

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	//For 9x payerpayee is not present
	/*private static Subscriber getPayerSubForReverseDiscount(final Context ctx,
			final Subscriber sub, final Service service,
			final DiscountEventActivity discEventActivity,
			final boolean isMultiMontly, final boolean isMontlyPayInAdvance,
			final boolean isInvoiceGenerated) throws HomeException {
		Subscriber payerSub;
		Subscriber genericSubForPayer;
		String calcPayerBan = "";
		Date serviceStartDate = null;
		
		/*String existingPayer = PayerPayeeUtil.getPayerBanForDate(ctx, sub,
				null, discEventActivity.getServiceId(), serviceStartDate,
				new Date());*/

		// Check if payer exits and if yes create discounts for Payer.
		/*if (isMultiMontly || (isInvoiceGenerated && isMontlyPayInAdvance)) {
			// calculate start date from provisioning history , latest
			// provisioning time.
			And andSubProvHist = new And();
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.SUBSCRIBER_ID, sub
							.getId()));
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.ITEM_IDENTIFIER,
					service.getID()));
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.SERVICE_STATE,
					ServiceStateEnum.PROVISIONED));
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.ITEM_TYPE,
					ChargedItemTypeEnum.SERVICE_INDEX));
			andSubProvHist.add(new Order().add(
					SubscriptionProvisioningHistoryXInfo.TIMESTAMP_, false));

			SubscriptionProvisioningHistory subProvhist = HomeSupportHelper
					.get(ctx).findBean(ctx,
							SubscriptionProvisioningHistory.class,
							andSubProvHist);

			serviceStartDate = subProvhist.getTimestamp_();
			/*existingPayer = PayerPayeeUtil.getPayerBanForDate(ctx, sub, null,
					discEventActivity.getServiceId(), serviceStartDate,
					new Date());*/

			/*calcPayerBan = PayerPayeeUtil.getPayerBanForServiceChargeDate(ctx,
					sub, service, subProvhist.getTimestamp_(), existingPayer);*/

			
			/*if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForReverseDiscount : Payer Ban for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" for Service is : " + calcPayerBan);
			}
			if (calcPayerBan == null || calcPayerBan.isEmpty()
					|| sub.getBAN().equals(calcPayerBan)) {
				// No payer found, discount will be created against the current
				// subscriber
				payerSub = sub;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForReverseDiscount : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Service is : " + payerSub);
				}
			} /*else {
				// found the payer of service, discount transaction will be
				// created for it in case of refund..
				genericSubForPayer = SubscriberSupport.getGenericSubscriber(
						ctx, calcPayerBan);
				payerSub = genericSubForPayer;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForReverseDiscount : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Service is : " + payerSub);
				}
			}*/
	/*	} else if (existingPayer == null || existingPayer.isEmpty()
				|| existingPayer.equals(sub.getBAN())) {
			payerSub = sub;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForReverseDiscount : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Service is : " + payerSub);
			}
		} else {
			genericSubForPayer = SubscriberSupport.getGenericSubscriber(ctx,
					existingPayer);
			payerSub = genericSubForPayer;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForReverseDiscount : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Service is : " + payerSub);
			}
		}

		return payerSub;
	}*/

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	//For 9x the payerpayee is not present
	/*private static Subscriber getPayerSubForReverseDiscountForAuxService(
			final Context ctx, final Subscriber sub,
			final AuxiliaryService auxService,
			final DiscountEventActivity discEventActivity,
			final boolean isMultiMontly, final boolean isMontlyPayInAdvance,
			final boolean isInvoiceGenerated) throws HomeException {
		Subscriber payerSub;
		Subscriber genericSubForPayer;
		String calcPayerBan = "";
		Date serviceStartDate = null;
		/*String existingPayer = PayerPayeeUtil.getPayerBanForDate(ctx, sub,
				null, discEventActivity.getServiceId(), serviceStartDate,
				new Date());*/

		// Check if payer exits and if yes create discounts for Payer.
		/*if (isMultiMontly || (isInvoiceGenerated && isMontlyPayInAdvance)) {
			// calculate start date from provisioning history , latest
			// provisioning time.
			And andSubProvHist = new And();
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.SUBSCRIBER_ID, sub
							.getId()));
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.ITEM_IDENTIFIER,
					auxService.getID()));
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.SERVICE_STATE,
					ServiceStateEnum.PROVISIONED));
			andSubProvHist.add(new EQ(
					SubscriptionProvisioningHistoryXInfo.ITEM_TYPE,
					ChargedItemTypeEnum.AUXSERVICE_INDEX));
			andSubProvHist.add(new Order().add(
					SubscriptionProvisioningHistoryXInfo.TIMESTAMP_, false));

			SubscriptionProvisioningHistory subProvhist = HomeSupportHelper
					.get(ctx).findBean(ctx,
							SubscriptionProvisioningHistory.class,
							andSubProvHist);

			serviceStartDate = subProvhist.getTimestamp_();
			/*existingPayer = PayerPayeeUtil.getPayerBanForDate(ctx, sub, null,
					discEventActivity.getServiceId(), serviceStartDate,
					new Date());*/

			/*calcPayerBan = PayerPayeeUtil.getPayerBanForAuxServiceChargeDate(
					ctx, sub, auxService, subProvhist.getTimestamp_(),
					existingPayer);*/

			/*if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForReverseDiscountForAuxService: Payer for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" and IsInvoiceGenerated : " + isInvoiceGenerated + " for Aux Service is : " + calcPayerBan);
			}
			if (calcPayerBan == null || calcPayerBan.isEmpty()
					|| sub.getBAN().equals(calcPayerBan)) {
				// No payer found, discount will be created against the current
				// subscriber
				payerSub = sub;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForReverseDiscountForAuxService : Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Aux Service is : " + payerSub);
				}
			} else {
				// found the payer of service, discount transaction will be
				// created for it in case of refund..
				genericSubForPayer = SubscriberSupport.getGenericSubscriber(
						ctx, calcPayerBan);
				payerSub = genericSubForPayer;
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"getPayerSubForReverseDiscountForAuxService : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
							+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Aux Service is : " + payerSub);
				}
			}
		} else if (existingPayer == null || existingPayer.isEmpty()
				|| existingPayer.equals(sub.getBAN())) {
			payerSub = sub;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForReverseDiscountForAuxService :Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Aux Service is : " + payerSub);
			}
		} else {
			genericSubForPayer = SubscriberSupport.getGenericSubscriber(ctx,
					existingPayer);
			payerSub = genericSubForPayer;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"getPayerSubForReverseDiscountForAuxService : Generic Payer Subscription for IsMultiMonthly :" + isMultiMontly + " or isMonthlyPayInAdvance : " + isMontlyPayInAdvance
						+" IsInvoiceGenerated "+ isInvoiceGenerated +" for Aux Service is : " + payerSub);
			}
		}

		return payerSub;
	}*/

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static Transaction generateDiscountTransactions(Context ctx,
			DiscountEventActivity discEventActivity) throws HomeException {
		boolean isMultiMontly = false;
		boolean isPayInAdvance = false;
		int chargeHistCount=0;
		Transaction transcation = null;

		// fetch the subscriber
		Subscriber sub = null;
		try{
			sub = SubscriberSupport.getSubscriber(ctx,
				discEventActivity.getSubId());
			
		}catch(Exception ex){
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : Exception while fetching subscriber with id : "
								+ discEventActivity.getSubId() + "exception: " + ex.getMessage());
			}
		}
		if (sub == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : Failed to fetch the subscriber with id : "
								+ discEventActivity.getSubId());
			}
			return transcation;
		}
		
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
		if (spid == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : Failed to fetch the service provider with id : "
								+ discEventActivity.getSubId());
			}
			return transcation;
		}

		/* Added for Generating refund transaction for old payer */
		Account parentAccount = sub.getAccount(ctx);
		
		// fetch the service object
		Service service = null;
		try{
			service = ServiceSupport.getService(ctx,
				discEventActivity.getServiceId());
		}catch(Exception ex){
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : Exception while fetching service for serviceId : "
								+ discEventActivity.getServiceId() + "exception: " + ex.getMessage());
			}
		}
		if (service == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : No mandatory service found for discountEvent : "
								+ discEventActivity.getId());
			}
			return transcation;
		}
		//Fetching the service record of the subscriber
		SubscriberServices subService = null;
		try{
			subService = SubscriberServicesSupport
				.getSubscriberServiceRecord(ctx, sub.getId(), service.getID(), SubscriberServicesUtil.DEFAULT_PATH);
		}catch(Exception ex){
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : Exception while fetching SubscriberServices for serviceId : "
								+ service.getID() + "exception: " + ex.getMessage());
			}
		}
		if (subService == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(
						ctx,
						MODULE,
						"generateDiscountTransactions : No subscriber service found for service id  : "
								+ service.getID() + " and subscriber: "
								+ sub.getId());
			}
			return transcation;
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "generateDiscountTransactions : Adjustment type for the service: "
					+ service.getAdjustmentType());
		}
		int billCycleDay = BillCycleSupport.getBillCycleForBan(ctx,
				discEventActivity.getBan()).getDayOfMonth();

		if (service.getChargeScheme() == ServicePeriodEnum.MULTIMONTHLY) {
			isMultiMontly = true;
		}
		/*
		if (PaymentOptionEnum.PAY_IN_ADVANCE.equals(service.getPaymentOption())
				|| PaymentOptionEnum.STRICTLY_PAY_IN_ADVANCE.equals(service
						.getPaymentOption())) 
		*/
		//'Bill in Advance' checked at SPID level, so setting PAY_IN_ADVANCE
		if(spid.isPrebilledRecurringChargeEnabled())
		{
			isPayInAdvance = true;
		}
		/*
		 * Check is placed here to see if the service is of type Multi-Monthly and
		 * Pay -inAdvance as other multi-monthly services are not supported for 
		 * discounting
		 */
		if (isMultiMontly && !isPayInAdvance) {
			// we do not support multi monthly service apart from Pay In Advance
			LogSupport
					.minor(ctx,
							MODULE,
							"generateDiscountTransactions : Only Multi Monthly Pay In Advance services are supported by Dynamic Billing. The current discount event entry will be skipped/not applicable.");
			
			markDiscountEventActivityAsNotApplicable(ctx, discEventActivity);
			return null;
		}

		//Fetching payer details of the service
		/*Subscriber payerSub = getPayerSubForDiscount(ctx, sub, service,
				subService, isMultiMontly, isPayInAdvance);*/
		Subscriber payerSub = sub;
		Date serviceStartDate = null;

		//Setting service start date for the multi-monthly or payInAdvance service
		if (isMultiMontly || isPayInAdvance) {
			serviceStartDate = subService.getStartDate();
		}
        //Fetching Discount class
		And where = new And();
		where.add(new EQ(DiscountClassXInfo.ID, discEventActivity
				.getDiscountClass()));

		DiscountClass discountClass = HomeSupportHelper.get(ctx).findBean(ctx,
				DiscountClass.class, where);

		if (discountClass == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions : Unable to find Discount Class for id  : "
								+ discEventActivity.getDiscountClass());
			}
			return transcation;
		}
 
		Collection<SubscriberSubscriptionHistory> chargingHistList = new ArrayList<SubscriberSubscriptionHistory>();
        /*
         * Discount event is being analyzed to check whether the discount is applicable from the last month(Prorated),
         * or it is new discount started on BCD or discount is continued.
         */
		Date startDateForChrgHistory = null;
		Date endDateForChrgHistory = null;

		if (isNewProratedDiscountOfLastMonth(ctx, parentAccount,
					discEventActivity)) {
				if (isMultiMontly) {
					// in case of multi-monthly service, the charge before effective
					// date could be from multiple months back,
					// not necessarily last month, so need to calculate charge cycle
					// start date
					startDateForChrgHistory = MultiMonthlyPeriodHandler.instance()
							.calculateCycleStartDate(ctx,
									discEventActivity.getDiscountEffectiveDate(),
									billCycleDay, discEventActivity.getSpid(),
									discEventActivity.getSubId(), subService);

				} else {
					// in case of monthly service we need to get history of at max
					// last month
					startDateForChrgHistory = CalendarSupportHelper.get(ctx)
							.getMonthsBefore(
									BillCycleSupport.getDateForBillCycleStart(
											ctx,
											new Date(),
											BillCycleSupport.getBillCycleForBan(
													ctx, parentAccount.getBAN())
													.getDayOfMonth()), 1);
				}
				endDateForChrgHistory = new Date();
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"generateDiscountTransactions:Prorated Charge : Start Date for fetching charging history  : "
											+ startDateForChrgHistory
											+ " End date for fetching charging history is : "
											+ endDateForChrgHistory);
				}
				/*
				 * Here last month's charging history is fetched for prorated service charge
				 * so as to give pro rated discount for the last month. Service can have charging hist previous to effective date 
				 * of discount so end date to fetch hist is passed as effective date
				 */
				SubscriberSubscriptionHistory history = SubscriberSubscriptionHistorySupport
						.getLastChargingEventBetween(ctx,
								discEventActivity.getSubId(),
								ChargedItemTypeEnum.SERVICE,
								service, startDateForChrgHistory,
								discEventActivity.getDiscountEffectiveDate());
				if (null != history) {
					chargingHistList.add(history);
				}

				// Pull charging history records created after the discount is
				// effective,
				// mostly this will return the current month charging history
				Collection<SubscriberSubscriptionHistory> listOfHistory = findChargingHistoryRecordForService(
						ctx, sub, service,
						CalendarSupportHelper.get(ctx).getDayAfter(discEventActivity.getDiscountEffectiveDate()),
						endDateForChrgHistory);
				if (null != listOfHistory) {
					chargingHistList.addAll(listOfHistory);
				}
			} else if (isNewDiscountFromCurrentMonth(ctx, parentAccount,
					discEventActivity)) {
				if (isMultiMontly) {
					// in case of multi-monthly service, the charge before effective
					// date could be from multiple months back,
					// not necessarily last month, so need to calculate charge cycle
					// start date
					startDateForChrgHistory = MultiMonthlyPeriodHandler.instance()
							.calculateCycleStartDate(ctx,
									discEventActivity.getDiscountEffectiveDate(),
									billCycleDay, discEventActivity.getSpid(),
									discEventActivity.getSubId(), subService);
				} else {
					// in case of monthly service we need to get history of at max
					// last month
					startDateForChrgHistory = BillCycleSupport
							.getDateForBillCycleStart(
									ctx,
									discEventActivity.getDiscountEffectiveDate(),
									BillCycleSupport.getBillCycleForBan(ctx,
											parentAccount.getBAN()).getDayOfMonth());
				}
				endDateForChrgHistory = new Date();
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"generateDiscountTransactions:New Charge : Start Date for fetching charging history  : "
											+ startDateForChrgHistory
											+ " End date for fetching charging history is : "
											+ endDateForChrgHistory);
				}
				// Pull charging history records created after the discount is
				// effective
				Collection<SubscriberSubscriptionHistory> listOfHistory = findChargingHistoryRecordForService(
						ctx, sub, service, startDateForChrgHistory,
						endDateForChrgHistory);
				if (null != listOfHistory) {
					chargingHistList.addAll(listOfHistory);
				}
			} else if (isContinuedDiscountFromLastMonth(ctx, parentAccount,
					discEventActivity)) {
				if (isMultiMontly) {
					// in case of multi-monthly service, the charge before effective
					// date could be from multiple months back,
					// not necessarily last month, so need to calculate charge cycle
					// start date
					startDateForChrgHistory = MultiMonthlyPeriodHandler.instance()
							.calculateCycleStartDate(
									ctx,
									CalendarSupportHelper.get(ctx).getDayAfter(
											discEventActivity
													.getDiscountAppliedTillDate()),
									billCycleDay, discEventActivity.getSpid(),
									discEventActivity.getSubId(), subService);
				} else {
					startDateForChrgHistory = CalendarSupportHelper.get(ctx)
							.getDayAfter(
									discEventActivity.getDiscountAppliedTillDate());
				}
				endDateForChrgHistory = new Date();
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"generateDiscountTransactions:Continued Charge : Start Date for fetching charging history  : "
											+ startDateForChrgHistory
											+ " End date for fetching charging history is : "
											+ endDateForChrgHistory);
				}
				// Pull charging history records created after the discount is
				// effective
				Collection<SubscriberSubscriptionHistory> listOfHistory = findChargingHistoryRecordForService(
						ctx, sub, service, startDateForChrgHistory,
						endDateForChrgHistory);
				if (null != listOfHistory) {
					chargingHistList.addAll(listOfHistory);
				}
			  }
		   
		   if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateDiscountTransactions:Continued Charge : Fetched charge history is : "
								+ chargingHistList);
			}
		     
			for (SubscriberSubscriptionHistory chargingHistRecord : chargingHistList) {
				SubscriberSubscriptionHistory chargingHistRecordForRemainingAmmount = chargingHistRecord;

				/*
				 * Capping is the amount maintained at charging hist as remaininng amount field
				 * It is used to limit the discount given to the sub.
				 */
				long ammountForCapping = getAmountToBeUsedForDiscountCapping(chargingHistRecordForRemainingAmmount);
				long ammountForCalc = getAmountToBeUsedForDiscountCalc(chargingHistRecord);

				long discAmount = calculateDiscountAmount(ctx, discEventActivity,
						discountClass, ammountForCalc, ammountForCapping,
						billCycleDay, service, null, payerSub, serviceStartDate,
						chargingHistRecord,isMultiMontly,isPayInAdvance);

				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"Discount amount calculated is : " + discAmount);
					LogSupport.debug(ctx, MODULE,
							"Discount transction is getting created");
				}
	            //It is to allow 0 amount discount transaction to be created 
				/*if (discAmount == 0) {
					LogSupport
							.debug(ctx, MODULE,
									"Calculated discount ammount is zero, so no transaction will be generated.");
					return transcation;
				}*/
				AdjustmentTypeActionEnum actionType = HistoryEventTypeEnum.CHARGE
						.equals(chargingHistRecord.getEventType()) ? AdjustmentTypeActionEnum.CREDIT
						: AdjustmentTypeActionEnum.DEBIT;
				
				if (payerSub.getId().equals(sub.getId())) {
					// if payerSub id and sub id is same, there is no
					// payer attached to service
					// here we cannot relay on DiscountEvent.getPayer as
					// in case of multimonthly service
					// its not necessary that the current payer is
					// actual payer for multimonthly service
					transcation = createDiscountTransaction(ctx, payerSub, null,
							discAmount, discountClass.getAdjustmentType(),
							service.getTaxAuthority(), actionType,
							discEventActivity, chargingHistRecord);
				} else {
					transcation = createDiscountTransaction(ctx, payerSub,
							sub.getId(), discAmount,
							discountClass.getAdjustmentType(),
							service.getTaxAuthority(), actionType,
							discEventActivity, chargingHistRecord);
				}
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"generateDiscountTransactions : Discount transaction is generated for Subscription"
									+ sub.getId() + " Amount " + discAmount);
				}
				discEventActivity.setDiscountAppliedFromDate(
						DiscountActivityUtils.getDiscountAppliedPeriodStartDate(ctx,discEventActivity,parentAccount,chargingHistRecord.getTimestamp_()));
				discEventActivity.setDiscountAppliedTillDate(DiscountActivityUtils.getDiscountAppliedPeriodEndDate(ctx,discEventActivity,parentAccount,chargingHistRecord.getTimestamp_()));

				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"generateDiscountTransactions:Setting up DiscountAppliedTillDate" + " " +
											discEventActivity.getDiscountAppliedTillDate() + "and DiscountStartDate "
											+ discEventActivity.getDiscountAppliedFromDate());
				}
				// update charging history record
				// chargingHistRecordForRemainingAmmount
				
				
					updateRemainingAmount(ctx, transcation,
							chargingHistRecordForRemainingAmmount);
				
			
		
		 }
		
		return transcation;

	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static void markDiscountEventActivityAsNotApplicable(Context ctx,
			DiscountEventActivity discEventActivity) {
		discEventActivity.setState(DiscountEventActivityStatusEnum.NOT_APPLICABLE);
		DiscountActivityUtils.saveDiscountEventActivity(ctx, discEventActivity);
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean isNewProratedDiscountOfLastMonth(final Context ctx,
			final Account account, final DiscountEventActivity discEventActivity) {
		Date billCycleStartDate;
		Date lasMonthBillCycleStartDate;
		try {
			billCycleStartDate = BillCycleSupport.getDateForBillCycleStart(ctx,
					new Date(),
					BillCycleSupport.getBillCycleForBan(ctx, account.getBAN())
							.getDayOfMonth());
			lasMonthBillCycleStartDate = CalendarSupportHelper.get(ctx)
					.getMonthsBefore(billCycleStartDate, 1);

			if (null != discEventActivity
					&& (discEventActivity.getDiscountAppliedFromDate() == null || 0 == discEventActivity
							.getDiscountAppliedFromDate().getTime())
					&& (discEventActivity.getDiscountAppliedTillDate() == null || 0 == discEventActivity
							.getDiscountAppliedTillDate().getTime())
					&& (discEventActivity.getDiscountExpirationDate() == null || 0 == discEventActivity
							.getDiscountExpirationDate().getTime())
					&& null != discEventActivity.getDiscountEffectiveDate()
					&& null != lasMonthBillCycleStartDate
					&& null != billCycleStartDate
					&& discEventActivity.getDiscountEffectiveDate().getTime() >= lasMonthBillCycleStartDate
							.getTime()
					&& discEventActivity.getDiscountEffectiveDate().getTime() < billCycleStartDate
							.getTime()) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"Prorated Discount : Current Bill Cycle Date is greater than Discount's effective date. Hence fetching charges from the month of Discount's effective date "
											+ discEventActivity.getSubId());
				}
				return true;
			}
		} catch (HomeException e) {
			LogSupport
					.minor(ctx,
							MODULE,
							"Failed to identify the discount event as new prorated discount from last month: "
									+ discEventActivity);
		}
		return false;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean isContinuedDiscountFromLastMonth(final Context ctx,
			final Account account, final DiscountEventActivity discEventActivity) {
		Date billCycleStartDate;
		try {
			billCycleStartDate = BillCycleSupport.getDateForBillCycleStart(ctx,
					new Date(),
					BillCycleSupport.getBillCycleForBan(ctx, account.getBAN())
							.getDayOfMonth());
			if (null != discEventActivity
					&& (discEventActivity.getDiscountAppliedFromDate() != null && 0 > discEventActivity
							.getDiscountAppliedFromDate().getTime())
					&& (discEventActivity.getDiscountAppliedTillDate() != null && 0 >= discEventActivity
							.getDiscountAppliedTillDate().getTime())
					&& (discEventActivity.getDiscountExpirationDate() == null || 0 == discEventActivity
							.getDiscountExpirationDate().getTime())
					&& null != billCycleStartDate
					&& discEventActivity.getDiscountAppliedFromDate().getTime() < billCycleStartDate
							.getTime()
					&& discEventActivity.getDiscountAppliedTillDate().getTime() < billCycleStartDate
							.getTime()) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"Prorated Discount : Current Bill Cycle Date is greater than Discount's effective date. Hence fetching charges from the month of Discount's effective date "
											+ discEventActivity.getSubId());
				}
				return true;
			}
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE,
					"Failed to identify the discount event as continued discount from last month: "
							+ discEventActivity);
		}
		return false;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean isNewDiscountFromCurrentMonth(final Context ctx,
			final Account account, final DiscountEventActivity discEventActivity) {
		Date billCycleStartDate;
		try {
			billCycleStartDate = BillCycleSupport.getDateForBillCycleStart(ctx,
					new Date(),
					BillCycleSupport.getBillCycleForBan(ctx, account.getBAN())
							.getDayOfMonth());

			if (null != discEventActivity
					&& (discEventActivity.getDiscountAppliedFromDate() == null || 0 == discEventActivity
							.getDiscountAppliedFromDate().getTime())
					&& (discEventActivity.getDiscountAppliedTillDate() == null || 0 == discEventActivity
							.getDiscountAppliedTillDate().getTime())
					&& (discEventActivity.getDiscountExpirationDate() == null || 0 == discEventActivity
							.getDiscountExpirationDate().getTime())
					&& null != discEventActivity.getDiscountEffectiveDate()
					&& null != billCycleStartDate
					&& discEventActivity.getDiscountEffectiveDate().getTime() >= billCycleStartDate
							.getTime()
					&& discEventActivity.getDiscountEffectiveDate().getTime() <= (new Date()
							.getTime())) {
				return true;
			}
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE,
					"Failed to identify the discount event as new for the current month: "
							+ discEventActivity);
		}
		return false;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean isChargeApplicable(
			final DiscountEventActivity discEventActivity) {
		if (null != discEventActivity
				&& (((discEventActivity.getDiscountAppliedTillDate() != null && 0 < discEventActivity
						.getDiscountAppliedTillDate().getTime()) && discEventActivity
						.getDiscountAppliedTillDate().before(
								discEventActivity.getDiscountExpirationDate())) || ((discEventActivity
						.getDiscountAppliedTillDate() == null || 0 == discEventActivity
						.getDiscountAppliedTillDate().getTime()) && (discEventActivity
						.getDiscountExpirationDate() != null && 0 < discEventActivity
						.getDiscountExpirationDate().getTime())))) {
			return true;
		}
		return false;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean isRefundApplicable(
			final DiscountEventActivity discEventActivity) {
		if (null != discEventActivity
				&& (discEventActivity.getDiscountAppliedTillDate() != null && 0 < discEventActivity
						.getDiscountAppliedTillDate().getTime())
				&& (discEventActivity.getDiscountExpirationDate() != null && 0 < discEventActivity
						.getDiscountExpirationDate().getTime())
				&& discEventActivity.getDiscountAppliedTillDate().after(
						discEventActivity.getDiscountExpirationDate())) {
			return true;
		}
		return false;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static Transaction generateReverseDiscountTransactions(Context ctx,
			DiscountEventActivity discEventActivity) throws HomeException {
		boolean isMultiMontly = false;
		boolean isPayInAdvance = false;
		Transaction transaction = null;

		// fetch the subscriber
		Subscriber sub = SubscriberSupport.getSubscriber(ctx,
				discEventActivity.getSubId());
		if (sub == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactions:Failed to fetch the subscriber with id : "
								+ discEventActivity.getSubId());
			}
			return transaction;
		}
		
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
		if (spid == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactions:Failed to fetch the service provider with id : "
								+ discEventActivity.getSubId());
			}
			return transaction;
		}

		/* Added for Generating refund transaction for old payer */
		Account parentAccount = sub.getAccount(ctx);
		SubscriberSubscriptionHistory latestRefundchargeHistory = null;

		// fetch the service object
		Service service = ServiceSupport.getService(ctx,
				discEventActivity.getServiceId());
		if (service == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"No mandatory service found for discountEvent : "
								+ discEventActivity.getId());
			}
			return transaction;
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "Adjustment type for the service: "
					+ service.getAdjustmentType());
		}
		int billCycleDay = BillCycleSupport.getBillCycleForBan(ctx,
				discEventActivity.getBan()).getDayOfMonth();

		if (service.getChargeScheme() == ServicePeriodEnum.MULTIMONTHLY)
			isMultiMontly = true;
		
		/*
		if (PaymentOptionEnum.PAY_IN_ADVANCE.equals(service.getPaymentOption())
				|| PaymentOptionEnum.STRICTLY_PAY_IN_ADVANCE.equals(service
						.getPaymentOption())) 
		*/
		//'Bill in Advance' checked at SPID level, so setting PAY_IN_ADVANCE
		if(spid.isPrebilledRecurringChargeEnabled())		
		{
			isPayInAdvance = true;
			latestRefundchargeHistory = SubscriberSubscriptionHistorySupport
					.getLatestHistoryForItem(ctx, sub.getId(),
							ChargedItemTypeEnum.SERVICE,
							service.getIdentifier(),
							HistoryEventTypeEnum.REFUND, -1,
							ServiceStateEnum.UNPROVISIONED);
		}
		if (isMultiMontly && !isPayInAdvance) {
			// we do not support multi monthly service apart from Pay In Advance
			LogSupport
					.minor(ctx,
							MODULE,
							"Only Multi Monthly Pay In Advance services are supported by Dynamic Billing. The current discount event entry will be skipped/not applicable.");
			
			markDiscountEventActivityAsNotApplicable(ctx, discEventActivity);
			return null;
		}

		boolean isInvoiceGenerated = false;
		if (latestRefundchargeHistory != null) {
			// Charge date can be calculated by service start date + billing
			// month
			And andTrans = new And();
			andTrans.add(new EQ(TransactionXInfo.RECEIPT_NUM,
					latestRefundchargeHistory.getTransactionId()));
			Transaction txn = HomeSupportHelper.get(ctx).findBean(ctx,
					Transaction.class, andTrans);
			Date serviceChargeDate = txn.getReceiveDate();
			/*isInvoiceGenerated = PayerPayeeUtil.isInvoiceForRefundNotGenerated(
					ctx, parentAccount, serviceChargeDate);*/
		}
		/*Subscriber payerSub = getPayerSubForReverseDiscount(ctx, sub, service,
				discEventActivity, isMultiMontly, isPayInAdvance,
				isInvoiceGenerated);*/
		//Considering that the payerpayee is not needed
		Subscriber payerSub = sub;
		
		SubscriberSubscriptionHistory chargingHistForRemainingAmmount=null;
		boolean feePersonalizeForService = checkFeePersonalization(ctx,discEventActivity);
		
		//Added to check for Fee Personalization
		if(feePersonalizeForService)
		{
			//the code to fetch the last refund hist
			chargingHistForRemainingAmmount= getChargeRefundHistoryForFeePersonalization(
							ctx,
							sub,
							service,
							sub.getStartDate(),
							CalendarSupportHelper.get(ctx)
									.getDateWithLastSecondofDay(
											discEventActivity
													.getDiscountExpirationDate()));
		}else{

		// fetch the charging history for the remaining amount calculations
			chargingHistForRemainingAmmount= SubscriberSubscriptionHistorySupport
				.getLastChargingEventBetween(
						ctx,
						sub.getId(),
						ChargedItemTypeEnum.SERVICE,
						service,
						sub.getStartDate(),
						CalendarSupportHelper.get(ctx)
								.getDateWithLastSecondofDay(
										discEventActivity
												.getDiscountExpirationDate()));
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactions:Fetched the latest subscribersubscription history  : "
									+ chargingHistForRemainingAmmount);
		}
		AdjustmentTypeActionEnum actionType = null;

		SubscriberSubscriptionHistory chargingHistForChargingAmmount = null;
		// check if the history is of refund, if yes fetch the latest charge and
		// use both
		if (null != chargingHistForRemainingAmmount
				&& HistoryEventTypeEnum.CHARGE
						.equals(chargingHistForRemainingAmmount.getEventType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactions:Fetched the latest subscribersubscription history is of charge : "
										+ chargingHistForRemainingAmmount);
			}
			chargingHistForChargingAmmount = chargingHistForRemainingAmmount;
		} else {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactions:Fetched the latest subscribersubscription is of refund so fetching latest charge history "
										+ chargingHistForRemainingAmmount);
			}
			if(feePersonalizeForService)
			{
				//to change the code to fetch the charge hist for refund charge
				chargingHistForChargingAmmount = getChargeHistForRefundHist(ctx,chargingHistForRemainingAmmount,sub,
								service,sub.getStartDate(),
								CalendarSupportHelper.get(ctx)
								.getDateWithLastSecondofDay(
										discEventActivity
												.getDiscountExpirationDate()));
			}else
			{
				chargingHistForChargingAmmount = SubscriberSubscriptionHistorySupport
						.getLatestHistoryForItem(ctx, sub.getId(),
								ChargedItemTypeEnum.SERVICE, service.getID(),
								HistoryEventTypeEnum.CHARGE, -1);
			}
			
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactions:Fetched the latest subscribersubscription history  of charge : "
										+ chargingHistForChargingAmmount);
			}
		}

	

		And where = new And();
		where.add(new EQ(DiscountClassXInfo.ID, discEventActivity
				.getDiscountClass()));

		DiscountClass discountClass = HomeSupportHelper.get(ctx).findBean(ctx,
				DiscountClass.class, where);

		if (discountClass == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactions:Unable to find Discount Class for id  : "
								+ discEventActivity.getDiscountClass());
			}
			return transaction;
		}

		long discAmount = calculateReverseDiscountAmount(ctx, parentAccount,
				discEventActivity, discountClass,
				chargingHistForChargingAmmount,
				chargingHistForRemainingAmmount, billCycleDay, service, null,
				payerSub,isMultiMontly);

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"generateReverseDiscountTransactions:Reverse Discount amount calculated is : "
							+ discAmount);
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactions:Reverse Discount transction is getting created");
		}

		/*if (discAmount == 0) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactions:Calculated reverse discount ammount is zero, so no transaction will be generated.");
			return transaction;
		}*/

		/*
		 * It is to check whether discount transaction needs to be created or 
		 * refund discount has to be given.
		 */
		if (isChargeApplicable(discEventActivity)) {
			
			actionType = AdjustmentTypeActionEnum.CREDIT;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactions:action type is "
								+ actionType);
			}
			if (payerSub.getId().equals(sub.getId())) {
				// if payerSub id and sub id is same, there is no
				// payer attached to service
				// here we cannot relay on DiscountEvent.getPayer as
				// in case of multimonthly service
				// its not necessary that the current payer is
				// actual payer for multimonthly service
				transaction = createDiscountTransaction(ctx, payerSub, null,
						discAmount, discountClass.getAdjustmentType(),
						service.getTaxAuthority(), actionType, discEventActivity,
						chargingHistForRemainingAmmount);
			} else {
				transaction = createDiscountTransaction(ctx, payerSub, sub.getId(),
						discAmount, discountClass.getAdjustmentType(),
						service.getTaxAuthority(), actionType, discEventActivity,
						chargingHistForRemainingAmmount);
			}
		} else if (isRefundApplicable(discEventActivity)) {
			
			actionType = AdjustmentTypeActionEnum.DEBIT;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactions:action type is "
								+ actionType);
			}
			
			if (payerSub.getId().equals(sub.getId())) {
				// if payerSub id and sub id is same, there is no
				// payer attached to service
				// here we cannot relay on DiscountEvent.getPayer as
				// in case of multimonthly service
				// its not necessary that the current payer is
				// actual payer for multimonthly service
				transaction = createReverseDiscountTransaction(ctx, payerSub, null,
						discAmount, discountClass.getAdjustmentType(),
						service.getTaxAuthority(), actionType, discEventActivity,
						chargingHistForRemainingAmmount,parentAccount);
			} else {
				transaction = createReverseDiscountTransaction(ctx, payerSub, sub.getId(),
						discAmount, discountClass.getAdjustmentType(),
						service.getTaxAuthority(), actionType, discEventActivity,
						chargingHistForRemainingAmmount,parentAccount);
			}
		}
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactions:Discount transaction is generated for Subscription"
									+ sub.getId()
									+ " Amount "
									+ discAmount
									+ "Adjustment Type is : " + actionType);
		}
		if(null==discEventActivity.getDiscountAppliedFromDate() ||
				0>=discEventActivity.getDiscountAppliedFromDate().getTime())
		{
			discEventActivity.setDiscountAppliedFromDate(discEventActivity.getDiscountEffectiveDate());
		}
		updateRemainingAmount(ctx, transaction, chargingHistForRemainingAmmount);

		return transaction;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static Transaction generateAuxServiceDiscountTransactions(
			Context ctx, DiscountEventActivity discEventActivity)
			throws HomeException {

		AuxiliaryService auxService = null;
		boolean isMultiMontly = false;
		boolean isPayInAdvance = false;
		Transaction transaction = null;
		// fetch the subscriber
		Subscriber sub = SubscriberSupport.getSubscriber(ctx,
				discEventActivity.getSubId());
		if (sub == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions:Failed to fetch the subscriber with id : "
										+ discEventActivity.getSubId());
			}
			return transaction;
		}
		
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
		if (spid == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateAuxServiceDiscountTransactions : Failed to fetch the service provider with id : "
								+ discEventActivity.getSubId());
			}
			return transaction;
		}		
		
		int billCycleDay = BillCycleSupport.getBillCycleForBan(ctx,
				discEventActivity.getBan()).getDayOfMonth();

		/* Added for Generating refund transaction for old payer */
		Account parentAccount = sub.getAccount(ctx);

		auxService = AuxiliaryServiceSupport.getAuxiliaryService(ctx,
				discEventActivity.getServiceId());
		if (auxService == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions:No auxiliary service found for discountEvent : "
										+ discEventActivity.getId());
			}
			return transaction;
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateAuxServiceDiscountTransactions:Adjustment type for the auxiliary service: "
									+ auxService.getAdjustmentType());
		}

		SubscriberAuxiliaryService subAuxService = SubscriberAuxiliaryServiceSupport
				.getSubAuxServBySubIdAuxIdAndSecondaryId(ctx,
						discEventActivity.getSubId(),
						discEventActivity.getServiceId(),
						discEventActivity.getServiceInstance());
		if (subAuxService == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions: No subscriber service found for service id  : "
										+ auxService.getID()
										+ " and subscriber: " + sub.getId());
			}
			return transaction;
		}

		// Check if payer exits and if yes create discounts for Payer.
		if (auxService.getChargingModeType() == ServicePeriodEnum.MULTIMONTHLY)
			isMultiMontly = true;
		/*
		if (PaymentOptionEnum.PAY_IN_ADVANCE.equals(auxService
				.getPaymentOption())
				|| PaymentOptionEnum.STRICTLY_PAY_IN_ADVANCE.equals(auxService
						.getPaymentOption()))
		*/
		//'Bill in Advance' checked at SPID level, so setting PAY_IN_ADVANCE
		if(spid.isPrebilledRecurringChargeEnabled())
		{
			isPayInAdvance = true;
		}
		if (isMultiMontly && !isPayInAdvance) {
			// we do not support multi monthly service apart from Pay In Advance
			LogSupport
					.minor(ctx,
							MODULE,
							"Only Multi Monthly Pay In Advance services are supported by Dynamic Billing. The current discount event entry will be skipped/not applicable.");
			
			markDiscountEventActivityAsNotApplicable(ctx, discEventActivity);
			return null;
		}

		/*Subscriber payerSub = getPayerSubForAuxServiceDiscount(ctx, sub,
				auxService, subAuxService, isMultiMontly, isPayInAdvance);*/
				Subscriber payerSub = sub;
		Date serviceStartDate = null;

		// Check if payer exits and if yes create discounts for Payer.
		if (isMultiMontly || isPayInAdvance) {
			serviceStartDate = subAuxService.getStartDate();
		}
		And where = new And();
		where.add(new EQ(DiscountClassXInfo.ID, discEventActivity
				.getDiscountClass()));

		DiscountClass discountClass = HomeSupportHelper.get(ctx).findBean(ctx,
				DiscountClass.class, where);

		if (discountClass == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions:Unable to find Discount Class for id  : "
										+ discEventActivity.getDiscountClass());
			}
			return transaction;
		}

		Collection<SubscriberSubscriptionHistory> chargingHistList = new ArrayList<SubscriberSubscriptionHistory>();

		Date startDateForChrgHistory = null;
		Date endDateForChrgHistory = null;
		if (isNewProratedDiscountOfLastMonth(ctx, parentAccount,
				discEventActivity)) {
			if (isMultiMontly) {
				// in case of multi-monthly service, the charge before effective
				// date could be from multiple months back,
				// not necessarily last month, so need to calculate charge cycle
				// start date
				startDateForChrgHistory = MultiMonthlyPeriodHandler.instance()
						.calculateCycleStartDate(ctx,
								discEventActivity.getDiscountEffectiveDate(),
								billCycleDay, discEventActivity.getSpid(),
								discEventActivity.getSubId(), subAuxService);
			} else {
				startDateForChrgHistory = CalendarSupportHelper.get(ctx)
						.getMonthsBefore(
								BillCycleSupport.getDateForBillCycleStart(
										ctx,
										new Date(),
										BillCycleSupport.getBillCycleForBan(
												ctx, parentAccount.getBAN())
												.getDayOfMonth()), 1);
			}
			endDateForChrgHistory = new Date();
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions:Prorated Charge : Start Date for fetching charging history  : "
										+ startDateForChrgHistory
										+ " End date for fetching charging history is : "
										+ endDateForChrgHistory);
			}
			SubscriberSubscriptionHistory history = SubscriberSubscriptionHistorySupport
					.getLastChargingEventBetween(ctx,
							discEventActivity.getSubId(),
							ChargedItemTypeEnum.AUXSERVICE, subAuxService,
							startDateForChrgHistory,
							discEventActivity.getDiscountEffectiveDate());
			if (null != history) {
				chargingHistList.add(history);

			}
			// Pull charging history records created after the discount is
			// effective, mostly this will return the current month charging
			// history
			Collection<SubscriberSubscriptionHistory> listOfHistory = findChargingHistoryRecordForAuxService(
					ctx, sub, CalendarSupportHelper.get(ctx).getDayAfter(discEventActivity.getDiscountEffectiveDate()),
					endDateForChrgHistory, auxService,discEventActivity.getServiceInstance());
			if (null != listOfHistory) {
				chargingHistList.addAll(listOfHistory);
			}

		} else if (isNewDiscountFromCurrentMonth(ctx, parentAccount,
				discEventActivity)) {

			if (isMultiMontly) {
				// in case of multi-monthly service, the charge before effective
				// date could be from multiple months back,
				// not necessarily last month, so need to calculate charge cycle
				// start date
				startDateForChrgHistory = MultiMonthlyPeriodHandler.instance()
						.calculateCycleStartDate(ctx,
								discEventActivity.getDiscountEffectiveDate(),
								billCycleDay, discEventActivity.getSpid(),
								discEventActivity.getSubId(), subAuxService);
			} else {
				startDateForChrgHistory = BillCycleSupport
						.getDateForBillCycleStart(
								ctx,
								discEventActivity.getDiscountEffectiveDate(),
								BillCycleSupport.getBillCycleForBan(ctx,
										parentAccount.getBAN()).getDayOfMonth());
			}
			endDateForChrgHistory = new Date();

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions:New Charge : Start Date for fetching charging history  : "
										+ startDateForChrgHistory
										+ " End date for fetching charging history is : "
										+ endDateForChrgHistory);
			}
			// Pull charging history records created after the discount is
			// effective, mostly this will return the current month charging
			// history
			Collection<SubscriberSubscriptionHistory> listOfHistory = findChargingHistoryRecordForAuxService(
					ctx, sub, startDateForChrgHistory,
					endDateForChrgHistory, auxService,discEventActivity.getServiceInstance());
			if (null != listOfHistory) {
				chargingHistList.addAll(listOfHistory);
			}
		} else if (isContinuedDiscountFromLastMonth(ctx, parentAccount,
				discEventActivity)) {
			if (isMultiMontly) {
				// in case of multi-monthly service, the charge before effective
				// date could be from multiple months back,
				// not necessarily last month, so need to calculate charge cycle
				// start date
				startDateForChrgHistory = MultiMonthlyPeriodHandler.instance()
						.calculateCycleStartDate(
								ctx,
								CalendarSupportHelper.get(ctx).getDayAfter(
										discEventActivity
												.getDiscountAppliedTillDate()),
								billCycleDay, discEventActivity.getSpid(),
								discEventActivity.getSubId(), subAuxService);
			} else {
				startDateForChrgHistory = CalendarSupportHelper.get(ctx)
						.getDayAfter(
								discEventActivity.getDiscountAppliedTillDate());
			}
			endDateForChrgHistory = new Date();
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateAuxServiceDiscountTransactions:Continued Charge : Start Date for fetching charging history  : "
										+ startDateForChrgHistory
										+ " End date for fetching charging history is : "
										+ endDateForChrgHistory);
			}

			// Pull charging history records created after the discount is
			// effective, mostly this will return the current month charging
			// history
			Collection<SubscriberSubscriptionHistory> listOfHistory = findChargingHistoryRecordForAuxService(
					ctx, sub, startDateForChrgHistory,
					endDateForChrgHistory,auxService,discEventActivity.getServiceInstance());
			if (null != listOfHistory) {
				chargingHistList.addAll(listOfHistory);
			}
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateAuxServiceDiscountTransactions:Continued Charge : Fetched charge history is : "
									+ chargingHistList);
		}
		for (SubscriberSubscriptionHistory chargingHistRecord : chargingHistList) {

			SubscriberSubscriptionHistory chargingHistRecordForRemainingAmmount = chargingHistRecord;

			long ammountForCapping = getAmountToBeUsedForDiscountCapping(chargingHistRecordForRemainingAmmount);
			long ammountForCalc = getAmountToBeUsedForDiscountCalc(chargingHistRecord);

			long discAmount = calculateDiscountAmount(ctx, discEventActivity,
					discountClass, ammountForCalc, ammountForCapping,
					billCycleDay, null, auxService, payerSub, serviceStartDate,
					chargingHistRecord,isMultiMontly,isPayInAdvance);

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateAuxServiceDiscountTransactions : Discount amount calculated is : " + discAmount);
				LogSupport.debug(ctx, MODULE,
						"generateAuxServiceDiscountTransactions : Discount transction is getting created");

			}

			/*if (discAmount == 0) {
				LogSupport
						.debug(ctx, MODULE,
								"Calculated discount ammount is zero, so no transaction will be generated.");
				return transaction;
			}*/
			AdjustmentTypeActionEnum actionType = HistoryEventTypeEnum.CHARGE
					.equals(chargingHistRecord.getEventType()) ? AdjustmentTypeActionEnum.CREDIT
					: AdjustmentTypeActionEnum.DEBIT;

			if (payerSub.getId().equals(sub.getId())) {
				// if payerSub id and sub id is same, there is no
				// payer attached to service
				// here we cannot relay on DiscountEvent.getPayer as
				// in case of multimonthly service
				// its not necessary that the current payer is
				// actual payer for multimonthly service
				transaction = createDiscountTransaction(ctx, payerSub, null,
						discAmount, discountClass.getAdjustmentType(),
						auxService.getTaxAuthority(), actionType,
						discEventActivity, chargingHistRecord);
			} else {
				transaction = createDiscountTransaction(ctx, payerSub,
						sub.getId(), discAmount,
						discountClass.getAdjustmentType(),
						auxService.getTaxAuthority(), actionType,
						discEventActivity, chargingHistRecord);
			}
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateAuxServiceDiscountTransactions : Discount transaction is generated for Subscription"
								+ sub.getId() + " Amount " + discAmount);
			}
			discEventActivity.setDiscountAppliedFromDate(
					DiscountActivityUtils.getDiscountAppliedPeriodStartDate(ctx,discEventActivity,parentAccount,chargingHistRecord.getTimestamp_()));
			discEventActivity.setDiscountAppliedTillDate(DiscountActivityUtils.getDiscountAppliedPeriodEndDate(ctx,discEventActivity,parentAccount,chargingHistRecord.getTimestamp_()));

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateDiscountTransactions:Setting up DiscountAppliedTillDate" + " " +
										discEventActivity.getDiscountAppliedTillDate() + "and DiscountStartDate "
										+ discEventActivity.getDiscountAppliedFromDate());
			}

			// update charging history record
			// chargingHistRecordForRemainingAmmount
			updateRemainingAmount(ctx, transaction,
					chargingHistRecordForRemainingAmmount);
		}
		return transaction;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static Transaction generateReverseDiscountTransactionsForAux(
			Context ctx, DiscountEventActivity discEventActivity)
			throws HomeException {
		AuxiliaryService auxService = null;
		boolean isMultiMontly = false;
		boolean isPayInAdvance = false;
		Transaction transaction = null;

		SubscriberSubscriptionHistory chargeHistory = null;
		// fetch the subscriber
		Subscriber sub = SubscriberSupport.getSubscriber(ctx,
				discEventActivity.getSubId());
		if (sub == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactionsForAux:Failed to fetch the subscriber with id : "
										+ discEventActivity.getSubId());
			}
			return transaction;
		}
		
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
		if (spid == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactionsForAux : Failed to fetch the service provider with id : "
								+ discEventActivity.getSubId());
			}
			return transaction;
		}
				
		int billCycleDay = BillCycleSupport.getBillCycleForBan(ctx,
				discEventActivity.getBan()).getDayOfMonth();

		/* Added for Generating refund transaction for old payer */
		Account parentAccount = sub.getAccount(ctx);

		auxService = AuxiliaryServiceSupport.getAuxiliaryService(ctx,
				discEventActivity.getServiceId());
		if (auxService == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactionsForAux:No auxiliary service found for discountEvent : "
										+ discEventActivity.getId());
			}
			return transaction;
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactionsForAux:Adjustment type for the auxiliary service: "
									+ auxService.getAdjustmentType());
		}

		if (auxService.getChargingModeType() == ServicePeriodEnum.MULTIMONTHLY)
			isMultiMontly = true;
		
		/*
		if (PaymentOptionEnum.PAY_IN_ADVANCE.equals(auxService
				.getPaymentOption())
				|| PaymentOptionEnum.STRICTLY_PAY_IN_ADVANCE.equals(auxService
						.getPaymentOption())) 
		*/
		//'Bill in Advance' checked at SPID level, so setting PAY_IN_ADVANCE
		if(spid.isPrebilledRecurringChargeEnabled())
		{
			isPayInAdvance = true;
			chargeHistory = SubscriberSubscriptionHistorySupport
					.getLatestHistoryForItem(ctx, sub.getId(),
							ChargedItemTypeEnum.AUXSERVICE,
							auxService.getIdentifier(),
							HistoryEventTypeEnum.REFUND,
							discEventActivity.getServiceInstance(),
							ServiceStateEnum.UNPROVISIONED);
		}
		if (isMultiMontly && !isPayInAdvance) {
			// we do not support multi monthly service apart from Pay In Advance
			LogSupport
					.minor(ctx,
							MODULE,
							"Only Multi Monthly Pay In Advance services are supported by Dynamic Billing. The current discount event entry will be skipped/not applicable.");
			
			markDiscountEventActivityAsNotApplicable(ctx, discEventActivity);
			return null;
		}

		boolean isInvoiceGenerated = false;
		if (chargeHistory != null) {
			// Charge date can be calculated by service start date + billing
			// month
			And andTrans = new And();
			andTrans.add(new EQ(TransactionXInfo.RECEIPT_NUM, chargeHistory
					.getTransactionId()));
			Transaction txn = HomeSupportHelper.get(ctx).findBean(ctx,
					Transaction.class, andTrans);
			Date serviceChargeDate = txn.getReceiveDate();
			/*isInvoiceGenerated = PayerPayeeUtil.isInvoiceForRefundNotGenerated(
					ctx, parentAccount, serviceChargeDate);*/
		}
		/*Subscriber payerSub = getPayerSubForReverseDiscountForAuxService(ctx,
				sub, auxService, discEventActivity, isMultiMontly,
				isPayInAdvance, isInvoiceGenerated);*/

		Subscriber payerSub = sub;
		SubscriberSubscriptionHistory chargingHistForRemainingAmmount=null;
		boolean feePersonalizeForService = checkFeePersonalizationForAuxService(ctx,discEventActivity);
		
		if(feePersonalizeForService)
		{
			chargingHistForRemainingAmmount = getChargeRefundHistoryForFeePersonalizationForAux(ctx,
					sub,
					auxService,
					sub.getStartDate(),
					CalendarSupportHelper.get(ctx)
							.getDateWithLastSecondofDay(
									discEventActivity
											.getDiscountExpirationDate()),
					discEventActivity.getServiceInstance());
		}else{
		// fetch the charging history for the remaining amount calculations
			 chargingHistForRemainingAmmount = SubscriberSubscriptionHistorySupport
					.getLastChargingEventBetweenForAux(
							ctx,
							sub.getId(),
							ChargedItemTypeEnum.AUXSERVICE,
							auxService,
							sub.getStartDate(),
							CalendarSupportHelper.get(ctx)
									.getDateWithLastSecondofDay(
											discEventActivity
													.getDiscountExpirationDate()),
							discEventActivity.getServiceInstance());
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactionsForAux:Fetched the latest subscribersubscription history  : "
									+ chargingHistForRemainingAmmount);
		}
		AdjustmentTypeActionEnum actionType = null;

		SubscriberSubscriptionHistory chargingHistForChargingAmmount = null;
		// check if the history is of refund, if yes fetch the latest charge and
		// use both
		if (null != chargingHistForRemainingAmmount
				&& HistoryEventTypeEnum.CHARGE
						.equals(chargingHistForRemainingAmmount.getEventType())) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactionsForAux:Fetched the latest subscribersubscription history is of charge : "
										+ chargingHistForRemainingAmmount);
			}
			chargingHistForChargingAmmount = chargingHistForRemainingAmmount;
		} else {
			
			if(feePersonalizeForService)
			{
				chargingHistForChargingAmmount = getChargeHistForRefundHistForAux(ctx, chargingHistForRemainingAmmount
						          , sub, auxService, sub.getStartDate(),
							CalendarSupportHelper.get(ctx)
									.getDateWithLastSecondofDay(
											discEventActivity
													.getDiscountExpirationDate()),discEventActivity.getServiceInstance());
			}else{
				chargingHistForChargingAmmount = SubscriberSubscriptionHistorySupport
						.getLatestHistoryForItem(ctx, sub.getId(),
								ChargedItemTypeEnum.AUXSERVICE, auxService.getID(),
								HistoryEventTypeEnum.CHARGE,
								discEventActivity.getServiceInstance());
			}
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"generateReverseDiscountTransactionsForAux:Fetched the latest subscribersubscription history for refund : "
										+ chargingHistForChargingAmmount);
			}
		}

		

		And where = new And();
		where.add(new EQ(DiscountClassXInfo.ID, discEventActivity
				.getDiscountClass()));

		DiscountClass discountClass = HomeSupportHelper.get(ctx).findBean(ctx,
				DiscountClass.class, where);

		if (discountClass == null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"Unable to find Discount Class for id  : "
								+ discEventActivity.getDiscountClass());
			}
			return transaction;
		}
		long discAmount = calculateReverseDiscountAmount(ctx, parentAccount,
				discEventActivity, discountClass,
				chargingHistForChargingAmmount,
				chargingHistForRemainingAmmount, billCycleDay, null,
				auxService, payerSub,isMultiMontly);

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"generateReverseDiscountTransactionsForAux:Discount amount calculated is : "
							+ discAmount);
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactionsForAux:Discount transction is getting created");
		}

		/*if (discAmount == 0) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactionsForAux:Calculated discount ammount is zero, so no transaction will be generated.");
			return transaction;
		}*/
		
		if (isChargeApplicable(discEventActivity)) {
			actionType = AdjustmentTypeActionEnum.DEBIT;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactionsForAux:action type is "
								+ actionType);
			}
			if (payerSub.getId().equals(sub.getId())) {
				// if payerSub id and sub id is same, there is no
				// payer attached to service
				// here we cannot relay on DiscountEvent.getPayer as
				// in case of multimonthly service
				// its not necessary that the current payer is
				// actual payer for multimonthly service
				transaction = createDiscountTransaction(ctx, payerSub, null,
						discAmount, discountClass.getAdjustmentType(),
						auxService.getTaxAuthority(), actionType,
						discEventActivity, chargingHistForRemainingAmmount);
			} else {
				transaction = createDiscountTransaction(ctx, payerSub, sub.getId(),
						discAmount, discountClass.getAdjustmentType(),
						auxService.getTaxAuthority(), actionType,
						discEventActivity, chargingHistForRemainingAmmount);
			}
		} else if (isRefundApplicable(discEventActivity)) {
			actionType = AdjustmentTypeActionEnum.CREDIT;
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"generateReverseDiscountTransactionsForAux:action type is "
								+ actionType);
			}
			if (payerSub.getId().equals(sub.getId())) {
				// if payerSub id and sub id is same, there is no
				// payer attached to service
				// here we cannot relay on DiscountEvent.getPayer as
				// in case of multimonthly service
				// its not necessary that the current payer is
				// actual payer for multimonthly service
				transaction = createReverseDiscountTransaction(ctx, payerSub, null,
						discAmount, discountClass.getAdjustmentType(),
						auxService.getTaxAuthority(), actionType,
						discEventActivity, chargingHistForRemainingAmmount,parentAccount);
			} else {
				transaction = createReverseDiscountTransaction(ctx, payerSub, sub.getId(),
						discAmount, discountClass.getAdjustmentType(),
						auxService.getTaxAuthority(), actionType,
						discEventActivity, chargingHistForRemainingAmmount,parentAccount);
			}
		}

		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport
					.debug(ctx,
							MODULE,
							"generateReverseDiscountTransactionsForAux:Discount transaction is generated for Subscription"
									+ sub.getId() + " Amount " + discAmount);
		}
		if(null==discEventActivity.getDiscountAppliedFromDate() ||
				0>=discEventActivity.getDiscountAppliedFromDate().getTime())
		{
			discEventActivity.setDiscountAppliedFromDate(discEventActivity.getDiscountEffectiveDate());
		}
		updateRemainingAmount(ctx, transaction, chargingHistForRemainingAmmount);
		return transaction;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static long getAmountToBeUsedForDiscountCapping(
			SubscriberSubscriptionHistory chargingHistForRemainingAmmount) {
		if (null != chargingHistForRemainingAmmount) {
			return chargingHistForRemainingAmmount.getRemainingAmount();
		}
		return 0;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static long getAmountToBeUsedForDiscountCalc(
			SubscriberSubscriptionHistory chargingHistForRemainingAmmount) {
		if (null != chargingHistForRemainingAmmount) {
			return chargingHistForRemainingAmmount.getChargedAmount();
		}
		return 0;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	public static Collection<SubscriberSubscriptionHistory> findChargingHistoryRecordForService(
			final Context ctx, final Subscriber sub, final Service service,
			final Date startDateForChrgHistory, final Date endDateForChrgHistory) {
		try {
			final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
			eventTypes.add(HistoryEventTypeEnum.CHARGE);
			eventTypes.add(HistoryEventTypeEnum.REFUND);

			final Set<ChargedItemTypeEnum> itemTypes = new HashSet<ChargedItemTypeEnum>();
			itemTypes.add(ChargedItemTypeEnum.SERVICE);

			return SubscriberSubscriptionHistorySupport.getEventsBetween(ctx,
					sub.getId(), itemTypes, startDateForChrgHistory,
					endDateForChrgHistory, eventTypes,service);
		} catch (HomeException e) {
			
				LogSupport.info(
						ctx,
						MODULE,
						"findChargingHistoryRecordForService : 	Failed find charging history record for service :"
								+ service.getID() + " for subscriber :"
								+ sub.getId(), e);
			
			return null;
		}
	}
	
	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static Collection<SubscriberSubscriptionHistory> findChargingHistoryRecordForAuxService(
			final Context ctx, final Subscriber sub,
			final Date startDateForChrgHistory,
			final Date endDateForChrgHistory,
			final AuxiliaryService auxService,
			final long secondarIdentifier) {
		try {
			final Set<HistoryEventTypeEnum> eventTypes = new HashSet<HistoryEventTypeEnum>();
			eventTypes.add(HistoryEventTypeEnum.CHARGE);
			eventTypes.add(HistoryEventTypeEnum.REFUND);

			final Set<ChargedItemTypeEnum> itemTypes = new HashSet<ChargedItemTypeEnum>();
			itemTypes.add(ChargedItemTypeEnum.AUXSERVICE);

			return SubscriberSubscriptionHistorySupport
					.getEventsBetweenForAuxService(ctx, sub.getId(), itemTypes,
							startDateForChrgHistory, endDateForChrgHistory,
							eventTypes, auxService,secondarIdentifier);
		} catch (HomeException e) {
			
				LogSupport.info(ctx, MODULE,
						"findChargingHistoryRecordForAuxService : Failed find charging history record for aux service :"
								+ secondarIdentifier
								+ " for subscriber :" + sub.getId(), e);
			
			return null;
		}
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static double getNoOfApplicableDaysForRefund(final Context context,
			final DiscountEventActivity discEventActivity) {

		double noOfDays = 1.0;
		noOfDays = CalendarSupportHelper.get(context).getNumberOfDaysBetween(
				CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(discEventActivity.getDiscountExpirationDate()),
						CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(discEventActivity.getDiscountAppliedTillDate()));

		return noOfDays;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static double getNoOfDaysBetweenDates(final Context context,
			final Date fromDate, final Date toDate) {

		double noOfDays = 1.0;
		if(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(fromDate).getTime()
				!= CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(toDate).getTime())
		{
			noOfDays = CalendarSupportHelper.get(context).getNumberOfDaysBetween(
					CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
							fromDate),
					CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(
							CalendarSupportHelper.get(context).getDaysAfter(toDate,1)));
		}

		return noOfDays;
	}

	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean updateRemainingAmount(
			final Context ctx,
			final Transaction transaction,
			final SubscriberSubscriptionHistory chargingHistoryForRemainingAmount) {
		boolean retValue = false;
		if (null != transaction) {
			try {
				long remainingAmount = chargingHistoryForRemainingAmount
						.getRemainingAmount();
				if (AdjustmentTypeActionEnum.CREDIT.equals((transaction
						.getAction()))) {
					// if discount transaction is credit, we need to subtract
					// the discount amount from remaining amount
					remainingAmount -= Math.abs(transaction.getAmount());
				} else {
					// if discount transaction is debit, we need to add the
					// discount amount from remaining amount
					remainingAmount += Math.abs(transaction.getAmount());
				}

				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"updateRemainingAmount : Updating the remaining amount of charging history of Subscriber:"
									+ chargingHistoryForRemainingAmount
											.getSubscriberId()
									+ " for service: "
									+ chargingHistoryForRemainingAmount
											.getItemIdentifier()
									+ " from :"
									+ chargingHistoryForRemainingAmount
											.getRemainingAmount() + " to :"
									+ remainingAmount);
				}

				chargingHistoryForRemainingAmount
						.setRemainingAmount(remainingAmount);
				SubscriberSubscriptionHistorySupport
						.updateSubscriberSubscriptionHistoryEvent(ctx,
								chargingHistoryForRemainingAmount);
			} catch (Exception ex) {
				
					LogSupport.info(
							ctx,
							MODULE,
							"updateRemainingAmount : Failed to update remaing ammount on charging history record of Subscriber:"
									+ chargingHistoryForRemainingAmount
											.getSubscriberId()
									+ " for service: "
									+ chargingHistoryForRemainingAmount
											.getItemIdentifier(), ex);
				
			}
		}
		return retValue;
	}
	
	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean updateRemainingAmountForMultipleChargeHist(
			final Context ctx,
			final long discAmount,
			final Collection<SubscriberSubscriptionHistory> chargingHistList) {
		    
		   boolean retValue = false;
		   SubscriberSubscriptionHistory chargingHistoryForRemainingAmount=null;
		   try{
			   long remainingDiscAmount = discAmount;
                List<SubscriberSubscriptionHistory> chargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargingHistList);
			    for(int i=chargeHistList.size();i>=0;i--)
			    {
			    	chargingHistoryForRemainingAmount = chargeHistList.get(i);
			    	long remainingAmount = chargingHistoryForRemainingAmount
							.getRemainingAmount();
			    	//If remaining amount is less than discount amount then we will take the remaining discount amount
			    	//after deducting remaining amount from discount amount and would deduct it from the previous charge hist record
			    	long discountAmountToBeDeducted = remainingAmount<=remainingDiscAmount?remainingAmount:remainingDiscAmount;
			    	remainingDiscAmount-=discountAmountToBeDeducted;
			    	if(remainingDiscAmount>=0)
			    	{
			    		
			    		if(chargingHistoryForRemainingAmount.getEventType().equals(HistoryEventTypeEnum.CHARGE))
			    		{
			    			remainingAmount -= Math.abs(discountAmountToBeDeducted);
			    		}else
			    		{
			    			remainingAmount += Math.abs(discountAmountToBeDeducted);
			    			i--;
			    		}
			    		if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(
									ctx,
									MODULE,
									"updateRemainingAmountForMultipleChargeHist : Updating the remaining amount of charging history of Subscriber:"
											+ chargingHistoryForRemainingAmount
													.getSubscriberId()
											+ " for service: "
											+ chargingHistoryForRemainingAmount
													.getItemIdentifier()
											+ " from :"
											+ chargingHistoryForRemainingAmount
													.getRemainingAmount() + " to :"
											+ remainingAmount);
						}
			    		chargingHistoryForRemainingAmount
						               .setRemainingAmount(remainingAmount);
				        SubscriberSubscriptionHistorySupport
						               .updateSubscriberSubscriptionHistoryEvent(ctx,
								                                    chargingHistoryForRemainingAmount);
			    	}else
			    	{
			    		break;
			    	}
			    }
			
			
		}catch(Exception ex)
		{
			
				LogSupport.info(
						ctx,
						MODULE,
						"updateRemainingAmountForMultipleChargeHist : Failed to update remaing ammount on charging history record of Subscriber:"
								+ chargingHistoryForRemainingAmount
										.getSubscriberId()
								+ " for service: "
								+ chargingHistoryForRemainingAmount
										.getItemIdentifier(), ex);
			
		}
		return retValue;
	}
	
	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static long getDiscountTransactionReciptFromDiscountHistory(
			final Context ctx, final DiscountEventActivity discEventActivity) {
		long transReceipt = 0L;
		try {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport
						.debug(ctx,
								MODULE,
								"getDiscountTransactionReciptFromDiscountHistory: Getting Discount transaction hist for the discount event activity"
										+ discEventActivity.getId());

			}
			final Home home = (Home) ctx.get(DiscountTransactionHistHome.class);
			And condition = new And();
			condition.add(new EQ(
					DiscountTransactionHistXInfo.EVENT_ACTIVITY_ID,
					discEventActivity.getId()));
			condition.add(new GTE(
					DiscountTransactionHistXInfo.CREATION_TIME_STAMP,
					discEventActivity.getDiscountAppliedFromDate().getTime()));
			condition.add(new LTE(
					DiscountTransactionHistXInfo.CREATION_TIME_STAMP,
					CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(discEventActivity.getDiscountAppliedTillDate()).getTime()));

			DiscountTransactionHist discTranHist = (DiscountTransactionHist) home
					.find(ctx, condition);
			if (null != discTranHist) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport
							.debug(ctx,
									MODULE,
									"getDiscountTransactionReciptFromDiscountHistory:Getting Matched Discount transaction hist for the discount event activity"
											+ discEventActivity.getId()
											+ " and Transaction Receipt Number is : "
											+ discTranHist.getReceiptNum());

				}
				transReceipt = discTranHist.getReceiptNum();
			}

		} catch (Exception ex) {
			LogSupport
					.minor(ctx,
							MODULE,
							"getDiscountTransactionReciptFromDiscountHistory:Exception Occured while fetching amount from DiscountTransactionHis"
									+ ex.getMessage());
		}
		return transReceipt;
	}
	
	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static boolean isTransactionInvoiced(final Context ctx, final long receiptNum,final Account parentAccount)
	{
		
		boolean isInvoiceGenerated=true;
		if(receiptNum != 0)
		{
			try{
			And andTrans = new And();
			andTrans.add(new EQ(TransactionXInfo.RECEIPT_NUM,
					receiptNum));
			Transaction txn = HomeSupportHelper.get(ctx).findBean(ctx,
					Transaction.class, andTrans);
			Date serviceChargeDate = txn.getReceiveDate();
			/*isInvoiceGenerated = PayerPayeeUtil.isInvoiceForRefundNotGenerated(
					ctx, parentAccount, serviceChargeDate);*/
			}catch(Exception ex)
			{
				LogSupport
				.minor(ctx,
						MODULE,
						"isTransactionInvoiced:Exception Occured while fetching Transaction"
								+ ex.getMessage());
			}
		}
		return isInvoiceGenerated;
	}
	
	/**
	 * This method returns calculated ProRated Discount Amount
	 *  returns the amount
	 * 
	 * @param ctx
	 * @param discountClass
	 * @param ammountForCalc
	 * @param ammountForCapping
	 * @param discEventActivity
	 * @param auxService
	 * @param chargingHistForRemainingAmmount
	 * @param isMultiMonthly
	 * @param isPayInAdvance
	 * @param billCycleDay
	 * @return 
	 * @throws HomeException
	 */
	private static long getTransactionReceivedDate(final Context ctx, final long receiptNum,final Account parentAccount)
	{
		
		Date receivedDate=new Date();
		if(receiptNum != 0)
		{
			try{
			And andTrans = new And();
			andTrans.add(new EQ(TransactionXInfo.RECEIPT_NUM,
					receiptNum));
			Transaction txn = HomeSupportHelper.get(ctx).findBean(ctx,
					Transaction.class, andTrans);
			
			receivedDate=txn.getReceiveDate();
			}catch(Exception ex)
			{
				LogSupport
				.minor(ctx,
						MODULE,
						"isTransactionInvoiced:Exception Occured while fetching Transaction"
								+ ex.getMessage());
			}
		}
		return receivedDate.getTime();
	}
	
	/*private static Map<Integer,List<SubscriberSubscriptionHistory>> groupChargeRefundHistory(final Context ctx, final Collection<SubscriberSubscriptionHistory> chargeHistList)
	{
		Map<Integer,List<SubscriberSubscriptionHistory>> mapOfChargeRefundSubcriptionHist=null;
		List<SubscriberSubscriptionHistory> referenceChargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargeHistList);
		List<SubscriberSubscriptionHistory> pairedList = new ArrayList<SubscriberSubscriptionHistory>();
		int count=0;
		try
		{
			mapOfChargeRefundSubcriptionHist = new HashMap<Integer,List<SubscriberSubscriptionHistory>>();
			for(int i=referenceChargeHistList.size();i>=0;i--)
			{
				count++;
				if(referenceChargeHistList.get(i).getEventType().equals(HistoryEventTypeEnum.CHARGE))
				{
					pairedList.add(referenceChargeHistList.get(i));
					mapOfChargeRefundSubcriptionHist.put(count,pairedList);	
				}else
				{
					pairedList.clear();
					if(referenceChargeHistList.get(i-1).getEventType().equals(HistoryEventTypeEnum.CHARGE))
					{
						pairedList.add(referenceChargeHistList.get(i));
						pairedList.add(referenceChargeHistList.get(i-1));
						mapOfChargeRefundSubcriptionHist.put(count,pairedList);
						i--;
					}
				}
			}
		}catch(Exception e)
		{
			LogSupport.info(ctx,MODULE,"Exception occured while grouping Charge History" + e.getMessage());
		}
		return mapOfChargeRefundSubcriptionHist;
	}
	
	private static Map<String,Long> getChargeAndRemainingFromAllChargeRefundHist(final Context ctx, final Collection<SubscriberSubscriptionHistory> chargeHistList)
	{
		long totalChargeAmount=0l;
		long totalRemainingAmount =0l;
		Map<String,Long> mapOfChargeAndRemainingAmount=null;
		List<SubscriberSubscriptionHistory> referenceChargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargeHistList);
	
		
		try
		{
			mapOfChargeAndRemainingAmount = new HashMap<String,Long>();
			for(int i=referenceChargeHistList.size();i>=0;i--)
			{
				
				if(referenceChargeHistList.get(i).getEventType().equals(HistoryEventTypeEnum.CHARGE))
				{
					totalChargeAmount -=Math.abs(referenceChargeHistList.get(i).getChargedAmount());
					totalRemainingAmount +=Math.abs(referenceChargeHistList.get(i).getRemainingAmount());
				}else
				{
					
					totalChargeAmount +=Math.abs(referenceChargeHistList.get(i).getChargedAmount());
					if(referenceChargeHistList.get(i-1).getEventType().equals(HistoryEventTypeEnum.CHARGE))
					{
						totalChargeAmount -=Math.abs(referenceChargeHistList.get(i-1).getChargedAmount());
						totalRemainingAmount +=Math.abs(referenceChargeHistList.get(i-1).getRemainingAmount());
						i--;
					}
				}
			}
			mapOfChargeAndRemainingAmount.put(CHARGEAMOUNT,new Long(totalChargeAmount));
			mapOfChargeAndRemainingAmount.put(REMAININGAMOUNT,new Long(totalRemainingAmount));
		}catch(Exception e)
		{
			LogSupport.info(ctx,MODULE,"Exception occured while grouping Charge History" + e.getMessage());
		}
		return mapOfChargeAndRemainingAmount;
	}
	
	private static long calculateDiscountAmountForFeePersonalizeChargeHist(final Context ctx,final DiscountEventActivity discEventActivity
			,final DiscountClass discountClass,final int billCycleDay,final Service service,final Subscriber payerSub,final Date serviceStartDate
			,final SubscriberSubscriptionHistory chargingHistRecord,final boolean isMultiMontly,final boolean isPayInAdvance
			,final Collection<SubscriberSubscriptionHistory> chargeHistList)
	{
		long discAmount=0l;
		try
		{
		
		 * Capping is the amount maintained at charging hist as remaininng amount field
		 * It is used to limit the discount given to the sub.
		 
			long ammountForCapping =0l;
			long ammountForCalc =0l;
			Map<String,Long> chargeAndRemainingAmountMap = getChargeAndRemainingFromAllChargeRefundHist(ctx,chargeHistList);
			if(null!=chargeAndRemainingAmountMap && !chargeAndRemainingAmountMap.isEmpty())
			{
				ammountForCapping = chargeAndRemainingAmountMap.get(REMAININGAMOUNT);
				ammountForCalc = chargeAndRemainingAmountMap.get(CHARGEAMOUNT);
			}
				
			discAmount = calculateDiscountAmount(ctx, discEventActivity,
					discountClass, ammountForCalc, ammountForCapping,
					billCycleDay, service, null, payerSub, serviceStartDate,
					chargingHistRecord,isMultiMontly,isPayInAdvance);
			
		}catch(Exception ex)
		{
			LogSupport.info(ctx,MODULE,"calculateDiscountAmountForFeePersonalizeChargeHist : Exception occured while calculating discount amount"
					+ ex.getMessage());
		}
       return discAmount;
	}*/
	
	 private static boolean isFeePersonalizedProcessedForSubscriptionService(final Context ctx,final DiscountEventActivity discEventActivity
			 , final Date toActivityTimeStamp,final Date fromActivityTimeStamp)
		{
			try
			{
				And and = new And();
				and.add(new EQ(DiscountActivityTriggerXInfo.BAN,discEventActivity.getBan()));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_ACTIVITY_TYPE,DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_EVALUATION_STATUS,DiscountEvaluationStatusEnum.PROCESSED_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.SUBSCRIPTION_ID,discEventActivity.getSubId()));
				and.add(new EQ(DiscountActivityTriggerXInfo.SERVICE_ID,discEventActivity.getServiceId()));
				and.add(new LTE(DiscountActivityTriggerXInfo.ACTIVITY_TIME_STAMP,CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(toActivityTimeStamp)));
				and.add(new GTE(DiscountActivityTriggerXInfo.ACTIVITY_TIME_STAMP,fromActivityTimeStamp));
				
				Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
				if(null!=home)
				{
					DiscountActivityTrigger discActivityTrigger= (DiscountActivityTrigger)home.find(ctx,and);
					if(null!=discActivityTrigger)
					{
						return true;
					}else
					{
						return false;
					}
				}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE,"isFeePersonalizedProcessedForSubscriptionService : Exception occured while fetching record for discountactivity trigger for Fee personaliize"
						+ e.getMessage());
			}
			
			return true;
		}
	 
	 private static boolean isFeePersonalizedProcessedForSubscriptionAuxService(final Context ctx,final DiscountEventActivity discEventActivity,
			 final Date toActivityTimeStamp,final Date fromActivityTimeStamp)
		{
			try
			{
				And and = new And();
				and.add(new EQ(DiscountActivityTriggerXInfo.BAN,discEventActivity.getBan()));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_ACTIVITY_TYPE,DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.DISCOUNT_EVALUATION_STATUS,DiscountEvaluationStatusEnum.PROCESSED_INDEX));
				and.add(new EQ(DiscountActivityTriggerXInfo.SUBSCRIPTION_ID,discEventActivity.getSubId()));
				and.add(new EQ(DiscountActivityTriggerXInfo.SERVICE_ID,discEventActivity.getServiceId()));
				and.add(new LTE(DiscountActivityTriggerXInfo.ACTIVITY_TIME_STAMP,CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(toActivityTimeStamp)));
				and.add(new GTE(DiscountActivityTriggerXInfo.ACTIVITY_TIME_STAMP,fromActivityTimeStamp));
				Home home = (Home) ctx.get(DiscountActivityTriggerHome.class);
				if(null!=home)
				{
					DiscountActivityTrigger discActivityTrigger= (DiscountActivityTrigger)home.find(ctx,and);
					if(null!=discActivityTrigger)
					{
						return true;
					}else
					{
						return false;
					}
				}
			}catch(Exception e)
			{
				LogSupport.info(ctx,MODULE,"isFeePersonalizedProcessedForSubscriptionAuxService : Exception occured while fetching record for discountactivity trigger for Fee personaliize" + e.getMessage());
			}
			
			return true;
		}
	 
	 private static boolean checkFeePersonalization(final Context ctx,final DiscountEventActivity discEventActivity)
	 {
		 Date toActivityTimeStamp=null;
		 Date fromActivityTimeStamp=null;
		try
		{
			 if(isChargeApplicable(discEventActivity))
			 {
				 fromActivityTimeStamp=discEventActivity.getDiscountEffectiveDate();
				 toActivityTimeStamp = discEventActivity.getDiscountExpirationDate();
				 
			 }else if(isRefundApplicable(discEventActivity))
			 {
				 fromActivityTimeStamp=discEventActivity.getDiscountAppliedFromDate();
				 toActivityTimeStamp = discEventActivity.getDiscountExpirationDate();
			 }
			 if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"checkFeePersonalization : Dates to check Fee personalize processed events for service from start date : " + fromActivityTimeStamp
							+" and end date : " + toActivityTimeStamp );
			 }
		 }
		 catch(Exception ex)
		 {
			 LogSupport.info(ctx,MODULE,"checkFeePersonalization : Exception occured while setting toActivityTimeStamp and fromActivityTimeStamp" + discEventActivity.getId());
			 //setting dates for checking the fee personalize event and range of date is from
			 // effective date to expiry date
			 fromActivityTimeStamp=discEventActivity.getDiscountEffectiveDate();
			 toActivityTimeStamp = discEventActivity.getDiscountExpirationDate();
		 }
		
		 return isFeePersonalizedProcessedForSubscriptionService(ctx, discEventActivity, toActivityTimeStamp, fromActivityTimeStamp);
	 }
	 
	 private static boolean checkFeePersonalizationForAuxService(final Context ctx,final DiscountEventActivity discEventActivity)
	 {
		 Date toActivityTimeStamp=null;
		 Date fromActivityTimeStamp=null;
		 try
		 {
			 if(isChargeApplicable(discEventActivity))
			 {
				 fromActivityTimeStamp=discEventActivity.getDiscountEffectiveDate();
				 toActivityTimeStamp = discEventActivity.getDiscountExpirationDate();
				 
			 }else if(isRefundApplicable(discEventActivity))
			 {
				 fromActivityTimeStamp=discEventActivity.getDiscountAppliedFromDate();
				 toActivityTimeStamp = discEventActivity.getDiscountExpirationDate();
			 }
			 if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(
							ctx,
							MODULE,
							"getChargeRefundHistoryForFeePersonalization : Dates to check Fee personalize processed events for Aux service from start date : " + fromActivityTimeStamp
							+" and end date : " + toActivityTimeStamp );
			 }
		 }catch(Exception ex)
		 {
			 LogSupport.info(ctx,MODULE,"checkFeePersonalizationForAuxService : Exception occured while setting toActivityTimeStamp and fromActivityTimeStamp" + discEventActivity.getId());
			 //setting dates for checking the fee personalize event and range of date is from
			 // effective date to expiry date
			 fromActivityTimeStamp=discEventActivity.getDiscountEffectiveDate();
			 toActivityTimeStamp = discEventActivity.getDiscountExpirationDate();
		 }
		 
		 return isFeePersonalizedProcessedForSubscriptionAuxService(ctx, discEventActivity, toActivityTimeStamp, fromActivityTimeStamp);
	 }
	 
	 private static SubscriberSubscriptionHistory getChargeRefundHistoryForFeePersonalization(final Context ctx,final Subscriber sub
			 ,final Service service,final Date startDateForChrgHistory,final Date endDateForChrgHistory)
	 {
		 SubscriberSubscriptionHistory chargeHistForRemaining = null;
		 try
		 {
			 Collection<SubscriberSubscriptionHistory> chargeHistCollection=findChargingHistoryRecordForService(ctx, sub, service, startDateForChrgHistory, endDateForChrgHistory);
			 List<SubscriberSubscriptionHistory> chargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargeHistCollection);
			 if(null!=chargeHistList && !chargeHistList.isEmpty())
			 {
				 for(int i=chargeHistList.size();i>=0;i--)
				 {
					 if(chargeHistList.get(i).getEventType().equals(HistoryEventTypeEnum.REFUND))
					 {
						 chargeHistForRemaining = chargeHistList.get(i);
						 if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,
										"getChargeRefundHistoryForFeePersonalization : RefundHist fetched is " + chargeHistForRemaining);
						 }
						 break;
					 }
				 }
			 }else
			 {
				 if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getChargeRefundHistoryForFeePersonalization : No ChargeEvents are found for the subscriber between startdate " + startDateForChrgHistory
								+" and end date : " + endDateForChrgHistory );
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 LogSupport.info(ctx,MODULE,"getChargeRefundHistoryForFeePersonalization : Exception occured while getting last refund event for subscriber " + sub.getId() + " " +ex.getMessage());
		 }
		 
		 return chargeHistForRemaining;
		 
	 }
	 
	 
	 private static SubscriberSubscriptionHistory getChargeRefundHistoryForFeePersonalizationForAux(final Context ctx,final Subscriber sub
			 ,final AuxiliaryService auxService,final Date startDateForChrgHistory,final Date endDateForChrgHistory, final long secondaryIdentifier)
	 {
		 SubscriberSubscriptionHistory chargeHistForRemaining = null;
		 try
		 {
			 Collection<SubscriberSubscriptionHistory> chargeHistCollection=findChargingHistoryRecordForAuxService(ctx, sub, startDateForChrgHistory
					                                                                                                   , endDateForChrgHistory,auxService,secondaryIdentifier);
			 List<SubscriberSubscriptionHistory> chargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargeHistCollection);
			 if(null!=chargeHistList && !chargeHistList.isEmpty())
			 {
				 for(int i=chargeHistList.size();i>=0;i--)
				 {
					 if(chargeHistList.get(i).getEventType().equals(HistoryEventTypeEnum.REFUND))
					 {
						 chargeHistForRemaining = chargeHistList.get(i);
						 if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,
										"getChargeRefundHistoryForFeePersonalizationForAux : RefundHist fetched is " + chargeHistForRemaining);
						 }
						 break;
					 }
				 }
			 }else
			 {
				 if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getChargeRefundHistoryForFeePersonalizationForAux : No ChargeEvents are found for the subscriber between startdate " + startDateForChrgHistory
								+" and end date : " + endDateForChrgHistory );
				 }
			 }
		 }
		 catch(Exception ex)
		 {
			 LogSupport.info(ctx,MODULE,"getChargeRefundHistoryForFeePersonalizationForAux : Exception occured while getting last refund event for subscriber " + sub.getId() + " " +ex.getMessage());
		 }
		 return chargeHistForRemaining;
		 
	 }
	 
	 private static SubscriberSubscriptionHistory getChargeHistForRefundHist(final Context ctx, SubscriberSubscriptionHistory refundHist
			 ,final Subscriber sub,final Service service, final Date startDateForChrgHistory,final Date endDateForChrgHistory)
	{
		 SubscriberSubscriptionHistory chargeHistForChargingAmount = null;
		 try
		 {
			 Collection<SubscriberSubscriptionHistory> chargeHistCollection=findChargingHistoryRecordForService(ctx, sub, service, startDateForChrgHistory, endDateForChrgHistory);
			 List<SubscriberSubscriptionHistory> chargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargeHistCollection);
			 if(null!=chargeHistList && !chargeHistList.isEmpty())
			 {
				 for(int i=chargeHistList.size();i>=0;i--)
				 {
					 if(chargeHistList.get(i).getEventType().equals(HistoryEventTypeEnum.CHARGE) &&
							 chargeHistList.get(i).getTimestamp_().getTime() < refundHist.getTimestamp_().getTime() )
					 {
						 chargeHistForChargingAmount = chargeHistList.get(i);
						 if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,
										"getChargeHistForRefundHist : ChargeHist fetched is " + chargeHistForChargingAmount);
						 }
						 break;
					 }
				 }
			 }else
			 {
				 if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getChargeHistForRefundHist : No ChargeEvents are found for the subscriber between startdate " + startDateForChrgHistory
								+" and end date : " + endDateForChrgHistory );
				 }
			 }
	     }
		 catch(Exception ex)
		 {
			 LogSupport.info(ctx,MODULE,"getChargeHistForRefundHist : Exception occured while getting last charge event for subscriber " + sub.getId() + " " +ex.getMessage());
		 }
		 return chargeHistForChargingAmount;
	}
	 
	 private static SubscriberSubscriptionHistory getChargeHistForRefundHistForAux(final Context ctx, SubscriberSubscriptionHistory refundHist
			 ,final Subscriber sub,final AuxiliaryService auxService, final Date startDateForChrgHistory,final Date endDateForChrgHistory, final long secondaryIdentifier)
	{
		 SubscriberSubscriptionHistory chargeHistForChargingAmount = null;
		 try
		 {
			 Collection<SubscriberSubscriptionHistory> chargeHistCollection=findChargingHistoryRecordForAuxService(ctx, sub, startDateForChrgHistory, endDateForChrgHistory,auxService,secondaryIdentifier);
			 List<SubscriberSubscriptionHistory> chargeHistList = new ArrayList<SubscriberSubscriptionHistory>(chargeHistCollection);
			 if(null!=chargeHistList && !chargeHistList.isEmpty())
			 {
				 for(int i=chargeHistList.size();i>=0;i--)
				 {
					 if(chargeHistList.get(i).getEventType().equals(HistoryEventTypeEnum.CHARGE) &&
							 chargeHistList.get(i).getTimestamp_().getTime() < refundHist.getTimestamp_().getTime() )
					 {
						 chargeHistForChargingAmount = chargeHistList.get(i);
						 if (LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(
										ctx,
										MODULE,
										"getChargeHistForRefundHistForAux : ChargeHist fetched is " + chargeHistForChargingAmount);
						 }
						 break;
					 }
				 }
			 }else
			 {
				 if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								ctx,
								MODULE,
								"getChargeHistForRefundHistForAux : No ChargeEvents are found for the subscriber between startdate " + startDateForChrgHistory
								+" and end date : " + endDateForChrgHistory );
				 }
			 }
		 }catch(Exception ex)
		 {
			 LogSupport.info(ctx,MODULE,"getChargeHistForRefundHistForAux : Exception occured while getting last charge event for subscriber " + sub.getId() + " " +ex.getMessage());
		 }
		 
		 return chargeHistForChargingAmount;
	}
	 
	 
	 /**
		 * This method is used to find the discounted data against the input BAN in the discountEventActivity table
		 * Depending on the Strategy the BAN to search in the discountEventActivity table is decided
		 * ROOT strategy: All the child  are passed as a predicate 
		 * SELF strategy: The passed BAN itself is used for the predicate  
		 * @param account
		 * @return
		 */
		
	public static Collection<DiscountEventActivity> findExistingDiscountEventActivity(Context ctx, 
			Account account) throws HomeException
	{
		String predicateString = null;
		String filter = account.getBAN();
		final Home home = (Home) ctx.get(AccountHome.class);
		Home spHome = (Home)ctx.get(CRMSpidHome.class);
		CRMSpid instance = (CRMSpid)spHome.find(ctx,account.getSpid());
		if(instance.getDiscountStrategy() == DiscountStrategyEnum.ROOT){
			new DebugLogMsg(MODULE, "DiscountClassContextAgent: findExistingDiscountEventActivity: Fetching the child account for the BAN [" + account.getBAN()+"] with SPID level strategy as [" + instance.getDiscountStrategy() + "]").log(ctx);
			StringBuffer strBuff = new StringBuffer();
			Home subAcctHome = account.getImmediateChildrenAccountHome(ctx);
			//subAcctHome.where(ctx, new Not(new EQ(AccountXInfo.STATE, AccountStateEnum.INACTIVE)));
			Collection<Account> list = subAcctHome.selectAll(ctx);
			
			if(list!=null && list.size()>0){
			Iterator it = list.iterator();
				while(it.hasNext()){
					strBuff.append(((Account)it.next()).getBAN() + ",");
				}
			filter = strBuff.toString(); 
			filter = filter.substring(0, filter.length()-1); 
			} 
			new DebugLogMsg(MODULE, "DiscountClassContextAgent: findExistingDiscountEventActivity: Child BANs to be searched are [" + filter + "]").log(ctx);
		}
		predicateString = "BAN in (" + filter + ")";
		new DebugLogMsg(MODULE, "DiscountClassContextAgent: findExistingDiscountEventActivity: predicateString is [" + predicateString + "]").log(ctx);
		SimpleXStatement predicate = new SimpleXStatement(predicateString );
		final Home discountEventActivityHome =  (Home) ctx.get(DiscountEventActivityHome.class);
		Collection<DiscountEventActivity> existingDiscountEventActivity = discountEventActivityHome.where(ctx, predicate).selectAll(ctx);
		return existingDiscountEventActivity;
	}

	/**
	 * Initializes the discount evaluation order list. 
	 * This will decide the order in which the discounts are evaluated for any account
	 * @param context
	 * @param account
	 * @param discountPriorityList 
	 * @return
	 */
	public static boolean initializeDiscountEvaluationOrder(final Context context, final Account account,
			List<DiscountHandler> discountEvaluationOrder, List<DiscountPriority> discountPriorityList){
		CRMSpid crmSpid = null;
		discountEvaluationOrder.clear();
		discountEvaluationOrder.add(new MasterPackDiscountHandler());

		discountPriorityList.addAll(getDiscountPriorityList(context, account));
		for(DiscountPriority discObj:discountPriorityList)
		{
			switch (discObj.getDiscountPriority().getIndex()){
			case DiscountPriorityTypeEnum.CONTRACT_DISCOUNT_INDEX :
				discountEvaluationOrder.add(new ContractDiscountHandler());
				break;
			case DiscountPriorityTypeEnum.CROSS_SUBSCRIPTION_DISCOUNT_INDEX :
				discountEvaluationOrder.add(new CrossSubscriptionDiscountHandler());
				break;
			case DiscountPriorityTypeEnum.FIRST_DEVICE_INDEX :
				discountEvaluationOrder.add(new FirstDeviceDiscountHandler());
				break;
			case DiscountPriorityTypeEnum.PAIRED_DISCOUNT_INDEX :
				discountEvaluationOrder.add(new PairedDiscountHandler());
				break;
			case DiscountPriorityTypeEnum.SECONDARY_DEVICE_DISCOUNT_INDEX :
				discountEvaluationOrder.add(new SecondDeviceDiscountHandler());
				break;
			case DiscountPriorityTypeEnum.COMBINATION_DISCOUNT_INDEX :
				discountEvaluationOrder.add(new CombinationDiscountHandler());
				break;
			default:
				break;
			}
		}
		if(LogSupport.isDebugEnabled(context)){
			LogSupport.debug(context, MODULE, "Discount Evaluation order for ban["+account.getBAN()+"] is " + discountEvaluationOrder.toString());
		}

		return true;		
	}
	
	public static List<DiscountPriority> getDiscountPriorityList(Context context, Account account){
		
		CRMSpid crmSpid;
		List<DiscountPriority> discountPriorityList = null;
		try {
			crmSpid = HomeSupportHelper.get(context).findBean(context,  CRMSpid.class, account.getSpid());
			if(crmSpid!=null) {
				discountPriorityList = crmSpid.getDiscountPriority();
			}else{
				LogSupport.minor(context, MODULE, "Failed to fetch Spid for Ban[" + account.getBAN() +"]");
			}
		} catch (HomeInternalException e) {
			LogSupport.minor(context, MODULE, "Failed to fetch Spid for Ban[" + account.getBAN() +"]  "+e.getMessage());
		} catch (HomeException e) {
			LogSupport.minor(context, MODULE, "Failed to fetch Spid for Ban[" + account.getBAN() +"]  "+e.getMessage());
		}
		
		return discountPriorityList;
		
	}

}
