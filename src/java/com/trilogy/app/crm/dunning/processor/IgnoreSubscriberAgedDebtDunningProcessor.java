
package com.trilogy.app.crm.dunning.processor;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.dunning.DunningActionHolder;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningLevelForecasting;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.dunning.DunningReportGenerationHelper;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordAgedDebt;
import com.trilogy.app.crm.dunning.DunningReportRecordMatureStateEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordStatusEnum;
import com.trilogy.app.crm.dunning.DunningWaiver;
import com.trilogy.app.crm.dunning.DunningWaiverHome;
import com.trilogy.app.crm.dunning.DunningWaiverXInfo;
import com.trilogy.app.crm.dunning.Forecastable;
import com.trilogy.app.crm.dunning.OTGDunningStatusEnum;
import com.trilogy.app.crm.dunning.ReportForecastable;
import com.trilogy.app.crm.home.TransactionRedirectionHome;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.holder.ObjectHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.AbstractJDBCXDB;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

public class IgnoreSubscriberAgedDebtDunningProcessor extends AbstractDunningProcessor {

	private final Date runningDate_;

	public IgnoreSubscriberAgedDebtDunningProcessor(Date runningDate) {
		runningDate_ = runningDate;
	}

	@Override
	public void processAccount(Context context, Account account)
			throws DunningProcessException {

		DunningPolicy dunningPolicy = getDunningPolicyOfAccount(context,account);
		final Context subCtx = context.createSubContext();

		subCtx.put(DunningConstants.CONTEXT_KEY_IS_IN_DUNNING, true);
		// Set this to default fetch size.
		subCtx.put(AbstractJDBCXDB.FETCH_SIZE_KEY, AbstractJDBCXDB.FETCH_SIZE);
		updateAccountPTPTightened(subCtx, account);

		String calculationServiceSessionKey = null;

		try {
			if (!dunningPolicy.isDunningExempt(context)
					&& !DunningProcessHelper.isAccountTemporaryDunningExempt(subCtx, account, getRunningDate())) {

				if (checkForTemporaryDunningWavier(context, account)) {
					subCtx.put(DunningConstants.DUNNING_IS_OTG_APPLIED, true);

					StringBuilder sb = new StringBuilder();
					sb.append("Account '");
					sb.append(account.getBAN());
					sb.append("' is applicable for OTG ");
					LogSupport.debug(context, this, sb.toString());
				}

				final Currency currency = retrieveCurrency(subCtx, account);
				List<AgedDebt> invoicedAgedDebts = getAgedDebts(subCtx,	account, dunningPolicy);
				Forecastable forecastable = new DunningLevelForecasting();

				DunningLevel nextLevel = forecastable.calculateForecastedLevel(subCtx, account, invoicedAgedDebts, currency, false,
						getRunningDate(), dunningPolicy);

				if (!nextLevel.isLevelZero()) {
					if (LogSupport.isDebugEnabled(subCtx)) {
						StringBuilder sb = new StringBuilder();
						sb.append("Account :");
						sb.append(account.getBAN());
						sb.append(", Forecasted Level is not L0.Forecasting again , considering payments.");
						sb.append(" Running Date : ");
						sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
						LogSupport.debug(subCtx, this, sb.toString());
					}

					calculationServiceSessionKey = CalculationServiceSupport.createNewSession(subCtx);
					subCtx.put(Common.AGED_DEBT_CALCULATION_SESSION_KEY,calculationServiceSessionKey);

					List<AgedDebt> agedDebts = account.getAgedDebt(subCtx,invoicedAgedDebts);

					// TT#13031133022 - check flag in context to see if dunning
					// process is initiated by Transaction pipeline
					// If yes, set it to true as the debt is cleared at this
					// stage
					if (context.has(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION)) {
						subCtx.put(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION,Boolean.TRUE);
					}

					nextLevel = forecastable.calculateForecastedLevel(subCtx,account, agedDebts, currency, true,
							getRunningDate(), dunningPolicy);

				}

				updateDunningWaiverStatus(subCtx, account, false, nextLevel);
				DunningActionHolder actionHolder = new DunningActionHolder();
				Map<String, DunningLevel> subscribersNextLevel = null;
				if (!nextLevel.isLevelZero()) {
					addAccountActions(nextLevel, account, actionHolder);
					subscribersNextLevel = addSubscriptionActions(subCtx, nextLevel, account,actionHolder);
				} else {
					actionHolder.addAccountDunningActions(nextLevel.getAccountStepDownActions());
					subscribersNextLevel = addSubscriptionActions(subCtx, nextLevel, account,actionHolder);
					setSubscriberLevelsToZero(subCtx, nextLevel, account,subscribersNextLevel);
					updateDunningWaiverStatus(subCtx, account, true, nextLevel);
				}

				DunningActionProcessor actionProcessor = new DunningActionProcessor();
				actionProcessor.executeAllActions(subCtx, actionHolder,nextLevel, subscribersNextLevel, account,getRunningDate());

				subCtx.put(DunningConstants.SUBSCRIPTION_DUNNING_LEVEL_CHANGE,Boolean.TRUE);
				updateAccountAndSubscriberNextLevels(subCtx, account,nextLevel);
			}
		} finally {
			if (calculationServiceSessionKey != null) {
				CalculationServiceSupport.endSession(subCtx,calculationServiceSessionKey);
			}

			subCtx.remove(DunningConstants.DUNNING_IS_OTG_APPLIED);
		}

	}

	private void updateDunningWaiverStatus(Context subCtx, Account account,
			boolean flag, DunningLevel nextLevel)
			throws DunningProcessException {

		if (subCtx.has(DunningConstants.DUNNING_IS_OTG_APPLIED)
				&& subCtx.getBoolean(DunningConstants.DUNNING_IS_OTG_APPLIED)) {

			Home home = (Home) subCtx.get(DunningWaiverHome.class);
			And condition = new And();
			condition.add(new EQ(DunningWaiverXInfo.BAN, account.getBAN()));

			if (flag) {
				try {
					Collection<DunningWaiver> dw = home.select(subCtx,
							condition);
					if (dw != null && !dw.isEmpty()) {
						for (DunningWaiver d : dw) {
							d.setStatus(OTGDunningStatusEnum.EXPIRED_INDEX);
							home.store(subCtx, d);
						}
					}
				} catch (HomeException e) {
					throw new DunningProcessException(e);
				}
				return;
			}

			condition.add(new EQ(DunningWaiverXInfo.STATUS,OTGDunningStatusEnum.PENDING_INDEX));
			try {
				DunningWaiver dw = (DunningWaiver) home.find(subCtx, condition);
				if (dw != null) {
					if (nextLevel.getId() > account.getLastDunningLevel()) {

						if (LogSupport.isDebugEnabled(subCtx)) {
							StringBuilder sb = new StringBuilder();
							sb.append("Current dunning level");
							sb.append(nextLevel.getId());
							sb.append("Is greater than ");
							sb.append(", Last Duninng Level :");
							sb.append(account.getLastDunningLevel());
							sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
							LogSupport.debug(subCtx, this, sb.toString());
						}
						dw.setCurrentDunningLevel(nextLevel.getId());
						dw.setStatus(OTGDunningStatusEnum.APPLIED_INDEX);
					} else if (nextLevel.getId() < account
							.getLastDunningLevel()) {

						if (LogSupport.isDebugEnabled(subCtx)) {
							StringBuilder sb = new StringBuilder();
							sb.append("Current dunning level ");
							sb.append(nextLevel.getId());
							sb.append(" Is greater than ");
							sb.append(", Last Duninng Level : ");
							sb.append(account.getLastDunningLevel());
							sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
							LogSupport.debug(subCtx, this, sb.toString());
						}
						dw.setCurrentDunningLevel(nextLevel.getId());
						dw.setStatus(OTGDunningStatusEnum.EXPIRED_INDEX);
					}
					home.store(subCtx, dw);
				}
			} catch (HomeException e) {
				throw new DunningProcessException(e);
			}
			return;
		}

	}
	
	

	private boolean checkForTemporaryDunningWavier(Context context,Account account) throws DunningProcessException {

		And where = new And();
		where.add(new EQ(DunningWaiverXInfo.BAN, account.getBAN()));
		where.add(new NEQ(DunningWaiverXInfo.STATUS,
				OTGDunningStatusEnum.EXPIRED));
		DunningWaiver dw = null;
		try {
			dw = HomeSupportHelper.get(context).findBean(context,DunningWaiver.class, where);
		} catch (HomeException e) {
			throw new DunningProcessException(e);
		}
		return dw == null ? false : true;

	}
	

	private void setSubscriberLevelsToZero(Context subCtx,DunningLevel nextLevel, Account account,
			Map<String, DunningLevel> subscribersNextLevel) {

		subscribersNextLevel = new HashMap<String, DunningLevel>();
		try {
			Collection<Subscriber> allSubscribers = account.getSubscribers(subCtx);
			for (Subscriber sub : allSubscribers) {
				subscribersNextLevel.put(sub.getId(), nextLevel);
			}
		} catch (HomeException e) {
			LogSupport.minor(subCtx, this,"Problem setting level zero in subscriber level Map", e);
		}

	}

	
	private void addAccountActions(DunningLevel nextLevel, Account account,
			DunningActionHolder actionHolder) {

		if (nextLevel.compareTo(account.getLastDunningLevel()) == 1)
		{
			actionHolder.addAccountDunningActions(nextLevel.getAccountStepUpActions());
		}
		else if (nextLevel.compareTo(account.getLastDunningLevel()) == -1) 
		{
			actionHolder.addAccountDunningActions(nextLevel.getAccountStepDownActions());
		}

	}

	
	
	@Override
	public DunningReportRecord generateReportRecord(Context context,
			Account account) throws DunningProcessException {

		DunningPolicy dunningPolicy = getDunningPolicyOfAccount(context,
				account);
		StringBuilder accountDetails = new StringBuilder();
		accountDetails.append("Account BAN = '");
		accountDetails.append(account.getBAN());
		accountDetails.append("'");
		String details = accountDetails.toString();
		final PMLogMsg pmLogMsg = new PMLogMsg("Generate Report Record",
				"PolicyBasedDunningProcessor", details);
		try {

			DunningReportRecord record = null;
			if (dunningPolicy.isDunningExempt(context) || DunningProcessHelper.isAccountTemporaryDunningExempt(
							context, account, getRunningDate()))

			{
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(context,this,"Account : "+ account.getBAN()+ " is exempted from dunning. Keeping account in Active state. Running Date : "
											+ CoreERLogger.formatERDateDayOnly(runningDate_));
				}

				record = null;
			} else {
				record = new DunningReportRecord();
				record.setBAN(account.getBAN());
				record.setCurrentLevel(getLevelInfoId(context, dunningPolicy,
						account.getLastDunningLevel()));
				// for TT ITSC-4196
				// record.setCreditCategoryId(account.getCreditCategory());
				record.setAccountType(account.getType());
				record.setReportDate(CalendarSupportHelper.get(context)
						.getDateWithNoTimeOfDay(runningDate_));
				record.setStatus(DunningReportRecordStatusEnum.PENDING_INDEX);
				record.setSpid(account.getSpid());
				record.setLastInvoiceDate(null);
				record.setRecordMaturity(DunningReportRecordMatureStateEnum.PENDING_INDEX);
				record.setAgedDebt(DunningReportGenerationHelper
						.createDunningReportRecordAgedDebtList(context,
								account.getSpid()));
				record.setPaymentMethod(account.getPaymentMethodType());
				final Currency currency = retrieveCurrency(context, account);

				if (checkForTemporaryDunningWavier(context, account)) {
					context.put(DunningConstants.DUNNING_IS_OTG_APPLIED, true);

					StringBuilder sb = new StringBuilder();
					sb.append("Account '");
					sb.append(account.getBAN());
					sb.append("' is applicable for OTG");
					LogSupport.debug(context, this, sb.toString());
				}

				List<AgedDebt> invoicedAgedDebts = getAgedDebts(context,account, dunningPolicy);

				if (invoicedAgedDebts.size() > 0) {
					Date lastInvoiceDebtDate = invoicedAgedDebts.iterator().next().getDebtDate();
					record.setLastInvoiceDate(lastInvoiceDebtDate);
					if (LogSupport.isDebugEnabled(context)) {
						StringBuilder sb = new StringBuilder();
						sb.append("Got InvoicedAgedDebts for Account :");
						sb.append(account.getBAN());
						sb.append(", lastInvoiceDebtDate : ");
						sb.append(CoreERLogger.formatERDateDayOnly(lastInvoiceDebtDate));
						sb.append(" Running Date : ");
						sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
						LogSupport.debug(context, this, sb.toString());
					}
				}
				ReportForecastable forecastable = new DunningLevelForecasting();
				LongHolder dunningAmount = new LongHolder(0);
				ObjectHolder dunnedAgedDebt = new ObjectHolder(0, null);
				DunningLevel nextLevel = forecastable.calculateForecastedLevel(context, account, invoicedAgedDebts, currency,record.getAgedDebt(), false, dunningAmount,
						dunnedAgedDebt, getRunningDate(), dunningPolicy);

				if (!nextLevel.isLevelZero()&& nextLevel.isAfterLevel(account.getLastDunningLevel())) {
					List<AgedDebt> agedDebts = null;

					if (LogSupport.isDebugEnabled(context)) {
						StringBuilder sb = new StringBuilder();
						sb.append("Account :");
						sb.append(account.getBAN());
						sb.append(", ForecastedState is Not Active. Calculating Forecasted date again.");
						sb.append(" Running Date : ");
						sb.append(CoreERLogger.formatERDateDayOnly(runningDate_));
						LogSupport.debug(context, this, sb.toString());
					}

					agedDebts = account.getAgedDebt(context, invoicedAgedDebts);

					// Cleaning the record's aged debt values. and recalculating
					// the next state
					for (DunningReportRecordAgedDebt agedDebt : (List<DunningReportRecordAgedDebt>) record
							.getAgedDebt()) {
						agedDebt.setValue(0);
					}

					// TT#13031133022 - check flag in context to see if dunning
					// process is initiated by Transaction pipeline
					// If yes, set it to true as the debt is cleared at this
					// stage
					if (context.has(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION)) {
						context.put(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION,Boolean.TRUE);
					}

					nextLevel = forecastable.calculateForecastedLevel(context,account, agedDebts, currency, record.getAgedDebt(),true, dunningAmount, dunnedAgedDebt,
							getRunningDate(), dunningPolicy);
					if (!nextLevel.isLevelZero()&& nextLevel.isAfterLevel(account.getLastDunningLevel())) {
						LogSupport.debug(context, this, "Going to next level-"+ nextLevel.getId());
						populateDRRAgedDebt(context, account, record,agedDebts, getRunningDate());
						record.setForecastedLevel(nextLevel.getLevel());
						record.setDunningAmount(dunningAmount.getValue());
						record.setDunnedAgedDebt((AgedDebt) dunnedAgedDebt.getObj());
						if (!nextLevel.getIsApproved()) {
							record.setRecordMaturity(DunningReportRecordMatureStateEnum.APPROVED_INDEX);
						}

					} else
						record = null;
				} else
					record = null;
			}

			return record;
		} finally {
			pmLogMsg.log(context);
			context.remove(DunningConstants.DUNNING_IS_OTG_APPLIED);
		}

	}
	

	private void updateAccountPTPTightened(Context context, Account account) {

		/* setting PTPTermsTightened to false and storing it */
		if (account.getPtpTermsTightened()) {
			account.setPtpTermsTightened(false);
			final Home home = (Home) context.get(AccountHome.class);
			try {
				home.store(context, account);
			} catch (final Exception exception) {
				StringBuilder sb = new StringBuilder();
				sb.append("Unable to update account PTPTermsTightened for account '");
				sb.append(account.getBAN());
				sb.append("': ");
				sb.append(exception.getMessage());
				LogSupport.minor(context, this, sb.toString(), exception);
			}
		}

	}

	

	private Map<String,DunningLevel>  addSubscriptionActions(Context subCtx, DunningLevel nextLevel,
			Account account, DunningActionHolder actionsHolder) {
		
		Map<String,DunningLevel> subNextLevels = new HashMap<String, DunningLevel>();
		Collection<Subscriber> allSubscribers = null;
		try {
			allSubscribers = account.getSubscribers(subCtx);
		} catch (HomeException e) {
			e.printStackTrace();
		}
		for (Subscriber sub : allSubscribers) 
		{
			subNextLevels.put(sub.getId(), nextLevel);
			addSubscriptionAction(nextLevel, sub.getSubscriptionType(), sub,actionsHolder);

		}
		return subNextLevels;
	}

	private void addSubscriptionAction(DunningLevel subNextLevel,
			long subscriptionType, Subscriber sub,
			DunningActionHolder actionsHolder) {

		if(subNextLevel.compareTo(sub.getLastDunningLevel()) == 1)
        {
            actionsHolder.addSubscriberDunningAction(sub.getId(),subNextLevel.getSubscriberStepUpActions(subscriptionType));
        }
		else if(subNextLevel.compareTo(sub.getLastDunningLevel()) == -1)
        {
            actionsHolder.addSubscriberDunningAction(sub.getId(),subNextLevel.getSubscriberStepDownActions(subscriptionType));            
        }

	}

	
	
	
	
	private void populateDRRAgedDebt(Context context, Account account,
			DunningReportRecord record, List<AgedDebt> agedDebts,
			Date runningDate) {
		List<Date> agedDebtDates = DunningReportGenerationHelper.createAgedDebtDatesList(context, account.getSpid(),
						runningDate);
		if (agedDebts != null) {
			for (AgedDebt agedDebt : agedDebts) {
				DunningReportGenerationHelper.populateRecordAgedDebt(context,agedDebt, record.getAgedDebt(), agedDebtDates, true);
			}
		}
	}
	

	private void updateAccountAndSubscriberNextLevels(Context subCtx,Account account, DunningLevel acccountNextLevel) {

		try {
			if (account.getLastDunningLevel() != acccountNextLevel.getId()) {
				account.setLastDunningLevel(acccountNextLevel.getId());
				HomeSupportHelper.get(subCtx).storeBean(subCtx, account);
			}
		} catch (HomeException e) {
			LogSupport.minor(subCtx, this,"Problem updating Account next Dunning Level", e);
		}

		try {
				Collection<Subscriber> allSubscribers = account.getSubscribers(subCtx);

				for (Subscriber sub : allSubscribers) {

						if (acccountNextLevel.getId() != sub.getLastDunningLevel()) {
							sub.setLastDunningLevel(acccountNextLevel.getId());
							HomeSupportHelper.get(subCtx).storeBean(subCtx, sub);
						}
					
				}
		} catch (HomeException e) {
			LogSupport.minor(subCtx, this,"Problem updating Subsriber next Dunning Level", e);
		}

	}
	
	
	
	
	private int getLevelInfoId(final Context ctx, final DunningPolicy policy,
			int levelId) {
		if (levelId == 0) {
			return levelId;
		}
		DunningLevel level = policy.getLevel(ctx, levelId);
		return level.getLevel();
	}

	@Override
	public Date getRunningDate() {
		return runningDate_;
	}
}
