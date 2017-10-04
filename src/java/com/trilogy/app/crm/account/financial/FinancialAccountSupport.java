package com.trilogy.app.crm.account.financial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.PaymentMethodConstants;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TaxExemptionInclusion;
import com.trilogy.app.crm.bean.TaxExemptionInclusionHome;
import com.trilogy.app.crm.bean.TaxExemptionInclusionXInfo;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanXInfo;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.core.ruleengine.BusinessRule;
import com.trilogy.app.crm.dunning.DunningLevel;
import com.trilogy.app.crm.dunning.DunningLevelForecasting;
import com.trilogy.app.crm.dunning.DunningLevelHome;
import com.trilogy.app.crm.dunning.DunningLevelXInfo;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.dunning.DunningPolicyHome;
import com.trilogy.app.crm.dunning.DunningPolicyXInfo;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.dunning.Forecastable;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOptionHome;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOptionXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.AccountServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;

/**
 * @author kkadam
 *
 */
public class FinancialAccountSupport {

	private static List<Account> responsibleAccountList = null;
	private static Invoice lastInvoice = null;
	private static AccountFinancialInfoDunningResult isDebtOutsResult = null;
	private static int accountCount = 0;
	/**
	 * fetch current financial details for all the responsible accounts within a
	 * hierarchy.
	 * 
	 * @param ctx
	 * @param header
	 * @param rootAccount
	 * @param genericParameters
	 * @param limit 
	 * @param pagekey 
	 * @return list of financial details
	 * @throws FinancialAccountException
	 * @throws CRMExceptionFault
	 * @throws HomeException 
	 */
	public static List<FinancialAccount> getFinancialAccountInfo(Context ctx,
			CRMRequestHeader header, String rootAccount,
			GenericParameter[] genericParameters, String pagekey, int limit)
			throws FinancialAccountException, CRMExceptionFault, HomeException {
		accountCount = 0;
		responsibleAccountList = new ArrayList<Account>();
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFinancialAccountInfo] responsibleAccountList.size:"+responsibleAccountList.size()
		//					+", responsibleAccountList:"+responsibleAccountList.toString());
		List<FinancialAccount> financialAccountList = new ArrayList<FinancialAccount>();
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFinancialAccountInfo] financialAccountList.size:"+financialAccountList.size()
		//					+", financialAccountList:"+financialAccountList.toString());
		List<Account> accountList = getResponsibleAccount(ctx, rootAccount);
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFinancialAccountInfo] accountList.size:"+accountList.size()
		//		+", accountList:"+accountList.toString());
		accountList = sortResponsibleAccounts(ctx, accountList);
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFinancialAccountInfo] Sorted accountList.size:"+accountList.size()
		//		+", accountList:"+accountList.toString());
		List<Account> filterList = getFilterListWithPaging(ctx,rootAccount,accountList,pagekey,limit);
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFinancialAccountInfo] filterList.size:"+filterList.size()
		//		+", filterList:"+filterList.toString());
		for(Account account : filterList) {
			LogSupport.info(ctx, MODULE, "Fill Financial info in account "+ account.getBAN());
			financialAccountList.add(fillFinancialAccount(ctx, account, header,genericParameters));
		}
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFinancialAccountInfo] financialAccountList.size:"+financialAccountList.size()
		//		+", financialAccountList:"+financialAccountList.toString());
		return financialAccountList;
	}

	private static List<Account> getFilterListWithPaging(Context ctx, String rootAccount, List<Account> accountList, String pagekey, int limit) {
		List<Account> filterList = null;
		//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] rootAccount:"+rootAccount
		//							+", accountList.size():"+String.valueOf(accountList.size())+", pagekey:"+pagekey
		//							+", limit:"+String.valueOf(limit)+", accountList:"+accountList.toString());
		if(pagekey != null && !pagekey.isEmpty()){
			Iterator<Account> accountIterator = accountList.iterator();
			int index = 0;
			//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] accountIterator:"+accountIterator.toString());
			while (accountIterator.hasNext()) {
				index++;
				Account account = accountIterator.next();
				//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] index:"+String.valueOf(index)+", account.getBAN():"+account.getBAN());
				if(account.getBAN().equals(pagekey)){
					break;
				}//end if
			}//end while loop
			//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] out of while loop");
			if( index != 0 ){
				int lastIndex = index + limit;
				//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] lastIndex:"+lastIndex);
				if(accountList.size() < lastIndex){
					lastIndex = accountList.size();
					//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] Assign lastIndex with accountList.size():"+lastIndex);
				}
				filterList = accountList.subList(index,lastIndex);
				//LogSupport.info(ctx, FinancialAccountSupport.class, "[getFilterListWithPaging] filterList:"+filterList.toString());
			}else{
				LogSupport.minor(ctx, MODULE, "Page key is not found under root account "+ rootAccount);
			}
		}else{
			//without page key
			int lastIndex = limit;
			if(accountList.size() < limit){
				lastIndex = accountList.size();
			}
			filterList = accountList.subList(0,lastIndex);
		}
		return filterList;
	}

	/**
	 * Retrieve all Responsible Account for given root account
	 * 
	 * @param ctx
	 * @param rootAccount
	 * @return
	 * @throws FinancialAccountException
	 */
	public static List<Account> getResponsibleAccount(Context ctx,
			String rootAccount) throws FinancialAccountException {
		Account account = null;
		try {
			account = AccountSupport.getAccount(ctx, rootAccount);
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE, "Can not found account for "
					+ rootAccount);
			throw new FinancialAccountException("Can not found account for "
					+ rootAccount);
		}
		if (account != null) {
			if (account.getGroupType().equals(GroupTypeEnum.GROUP)) {
				addResponsibleAccount(ctx, account);
				LogSupport.info(ctx, MODULE,
						"Group Account " + account.getBAN());
				List<Account> chiledAccountList = getChildAccount(ctx,
						account.getBAN());
				LogSupport.info(ctx, MODULE,
						"Total child " + chiledAccountList.size()
								+ " account for " + account.getBAN()
								+ " Group account");
				for (Account chiledAccount : chiledAccountList) {
					LogSupport.info(ctx, MODULE, "Processing on "
							+ chiledAccount.getBAN() + " account");
					getResponsibleAccount(ctx, chiledAccount.getBAN());
				}
			} else {
				addResponsibleAccount(ctx, account);
			}
		} else {
			LogSupport.minor(ctx, MODULE, "Can not found account for "
					+ rootAccount);
			throw new FinancialAccountException("Can not found account for "
					+ rootAccount);
		}
		return responsibleAccountList;
	}

	/**
	 * Sort account with creation date
	 * 
	 * @param ctx
	 * @param responsibleAccountList
	 * @return
	 */
	public static List<Account> sortResponsibleAccounts(Context ctx,
			List<Account> responsibleAccountList) {
		Collections.sort(responsibleAccountList, new Comparator<Account>() {
			public int compare(Account account1, Account account2) {
				return account1.getCreationDate().compareTo(
						account2.getCreationDate());
			}
		});
		return responsibleAccountList;
	}

	/**
	 * Retrieve all Child account for root account
	 * 
	 * @param ctx
	 * @param ban
	 * @return list of child account
	 */
	private static List<Account> getChildAccount(Context ctx, String ban) {
		List<Account> chiledAccountList = new ArrayList<Account>();
		try {
			And filter = new And();
			filter.add(new EQ(AccountXInfo.PARENT_BAN, ban));
			chiledAccountList = (List<Account>) HomeSupportHelper.get(ctx)
					.getBeans(ctx, Account.class, filter);
		} catch (Exception e) {
			LogSupport.minor(ctx, MODULE, " Can not found child account for "
					+ ban);
		}
		return chiledAccountList;
	}

	/**
	 * Check given account is responsible if is responsible then add account
	 * list
	 * 
	 * @param ctx
	 * @param account
	 * @throws FinancialAccountException 
	 */
	private static void addResponsibleAccount(Context ctx, Account account) throws FinancialAccountException {
		if (account.getResponsible()) {
			accountCount++;
			responsibleAccountList.add(account);
			LogSupport.info(ctx, MODULE, account.getBAN()+ " is responsible account");
		} else {
			if(accountCount !=0 ){
				LogSupport.info(ctx, MODULE, account.getBAN()+ " is not responsible account");	
			}else{
				LogSupport.minor(ctx, MODULE, "Input Account "+ account.getBAN()+" is Non-Responsible Individual / Group Account with no Responsible Child");
				throw new FinancialAccountException("Input Account "+ account.getBAN()+" is Non-Responsible Individual / Group Account with no Responsible Child");
			}
			
		}
	}

	/**
	 * Fill or decorate Financial Account Object
	 * 
	 * @param ctx
	 * @param account
	 * @param header
	 * @param genericParameters
	 * @return decorated financial account bean
	 * @throws CRMExceptionFault
	 * @throws FinancialAccountException
	 * @throws HomeException 
	 */
	public static FinancialAccount fillFinancialAccount(Context ctx,
			Account account, CRMRequestHeader header,
			GenericParameter[] genericParameters) throws CRMExceptionFault,
			FinancialAccountException, HomeException {
		FinancialAccount financialAccount = new FinancialAccount();

		// Set account to financial account bean
		financialAccount.setAccount(account);

		String[] balanceTypes = { "LAST_INVOICE_AMOUNT", "BALANCE",
				"ADJUSTMENTS_SINCE_LAST_INVOICE", "PAYMENTS_SINCE_LAST_INVOICE" };

		financialAccount.setResponseParameter(executeAccountBalanceQuery(ctx,
				header, account.getBAN(), balanceTypes, genericParameters));

		// Payment due date
		financialAccount
				.setPaymentDueDate(getPastDueDate(ctx, account.getBAN()));

		GenericParameter[] responseParam = financialAccount.getResponseParameter();
		long paymentSinceLastInvoice = 0L;
		
		for(GenericParameter param: responseParam) {
			if(param.getName() == "PAYMENTS_SINCE_LAST_INVOICE") {
				paymentSinceLastInvoice = Long.parseLong( param.getValue().toString() );
			}
		}
		
		// Past Due Amount
		financialAccount.setPastDueAmount(getPastDueAmount(ctx, account, paymentSinceLastInvoice));

		// Payment Plan
		financialAccount.setPaymentPlan(getPaymentPlan(ctx, account));

		// Payment Plan Outstanding Amount
		financialAccount.setPaymentPlanOutstandingAmount(account
				.getPaymentPlanAmount());

		// Tax Class Name
		financialAccount.setTax(getTaxExemptionInclusion(ctx, account));

		// Is Exempt from Dunning Indicator/Flag
		financialAccount.setExemptFromDunning(account.getIsDunningExempted());

		// PAP Indicator/Flag - Set Default value for flag.
		if (account.getPaymentMethodType() == PaymentMethodConstants.PAYMENT_METHOD_DEBIT_CARD 
				|| account.getPaymentMethodType() == PaymentMethodConstants.PAYMENT_METHOD_CREDIT_CARD) { // (3 for Bank or 5 for credit card )
			financialAccount.setPapIndicator(true);
		} else {                                                                                          // (if Payment Method is Cash etc.)
			financialAccount.setPapIndicator(false);
		}
		
		// Invoice Delivery Method (Post, Email, etc)
		financialAccount.setInvoiceDeliveryMethod(getInvoiceDeliveryMethod(ctx,
				account));

		// Current Invoice
		if (lastInvoice != null)
			financialAccount.setCurrentInvoice(lastInvoice.getURL());

		// Next Invoice
		// financialAccount.setNextInvoice(getNextInvoice(ctx,
		// account.getBAN()));

		// Next Dunning Level Date
		financialAccount.setNextDunningLevelDate(getNextDunningLevelDate(ctx,
				account));
		
		// Current Debt Age
		int deptAge = getCurrentDebtAge(ctx, account);
		financialAccount.setDebtAge(deptAge);
		
		if(deptAge == 0){
			financialAccount.setNextDunningLevelDate(null);
		}
		
		return financialAccount;
	}
	
	 /**
     * Finds the next dunning level for the account 
     * @param ctx
     * @param account
     * @return
     * @throws HomeException
     */
	
    public static DunningLevel getForcastedDunningLevel(Context ctx, Account account){
    	DunningLevel nextLevel = null;
    	if ( account == null || account.getSystemType().equals(SubscriberTypeEnum.PREPAID)) {
			if (LogSupport.isEnabled(ctx, SeverityEnum.INFO)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Could not be discard dunning report records: Account '");
				sb.append(account.getBAN());
				sb.append("' not found or prepaid.");
				LogSupport.info(ctx, MODULE, sb.toString());
			}
			return nextLevel;
		}
		DunningPolicy dunningPolicy = getDunningPolicyOfAccount(ctx, account);
		List<AgedDebt> invoicedAgedDebts = getAgedDebts(ctx, account, dunningPolicy);
		Forecastable forecastable = new DunningLevelForecasting();
		List<AgedDebt> agedDebts = account.getAgedDebt(ctx, invoicedAgedDebts);
		final Currency currency = retrieveCurrency(ctx, account);
		nextLevel = forecastable.calculateForecastedLevel(ctx, account, agedDebts, currency, true, new Date(), dunningPolicy);

		isDebtOutsResult = (AccountFinancialInfoDunningResult) ctx.get("DEBT_OUTSTANDING_"+account.getBAN());
		return nextLevel;
    }
    
	private static Date getNextDunningLevelDate(Context ctx, Account account) throws HomeException, FinancialAccountException {
		Date calculatedDate = null;
		Date currentDate = new Date();
		if (account.getDunningPolicyId() != 0) {
			DunningLevel dunningLevel = getForcastedDunningLevel(ctx, account);
			
			
			if(dunningLevel != null){
			if (isDebtOutsResult.getAgedDebt() != null) {
				
				if(LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,"[getNextDunningLevelDate] Current Dunning Level:"+dunningLevel.getLevel());
					LogSupport.debug(ctx, MODULE,"[getNextDunningLevelDate] Due Date:"+isDebtOutsResult.getAgedDebt().getDueDate());
					LogSupport.debug(ctx, MODULE,"[getNextDunningLevelDate] Dunning Policy Id:"+account.getDunningPolicyId());
				}
				
				if (isDebtOutsResult.getAgedDebt().getDueDate().before(currentDate)) {
					if(dunningLevel!=null){

						Home policyhome = (Home) ctx.get(DunningPolicyHome.class);
						DunningPolicy policy = (DunningPolicy) policyhome.find(ctx,new EQ(DunningPolicyXInfo.DUNNING_POLICY_ID,account.getDunningPolicyId()));
						int maxLevels = policy.getAllLevels(ctx).size(); //Get the Count of Levels in the Dunning Policy

						if(LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(ctx, MODULE,"[getNextDunningLevelDate] Max Levels:"+maxLevels);
						}
						if(dunningLevel.getLevel() >= maxLevels) {
							calculatedDate = null;  //If the Customer is at last Dunning Level, Next Dunning Level Date should be null
						} else {
							DunningLevel nextDunningLevel = policy.getLevelAt(ctx,account.getLastDunningLevel()); //Fetching the next Dunning Level
							
							if(LogSupport.isDebugEnabled(ctx)) {
								LogSupport.debug(ctx, MODULE,"[getNextDunningLevelDate] Next Dunning Level:"+nextDunningLevel.getLevel());
								LogSupport.debug(ctx, MODULE,"[getNextDunningLevelDate] Next Dunning Grace days:"+nextDunningLevel.getGraceDays());
							}
							
							calculatedDate = CalendarSupportHelper.get(ctx).findDateDaysAfter(nextDunningLevel.getGraceDays(),isDebtOutsResult.getAgedDebt().getDueDate());
						}
					}
				}else{
				    calculatedDate = isDebtOutsResult.getAgedDebt().getDueDate();
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, MODULE,"Due Date is "+isDebtOutsResult.getAgedDebt().getDueDate()+" after current date "+ currentDate);
					}
				}
			  }else{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,"Unable to retrieve  aged debt entry for account "+ account.getBAN());
				}
			  }
			}else{
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,"Unable to retrieve  Dunning Level entry for account "+ account.getBAN());
				}
			}
			
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,"Calculated next dunning lavel date: " +calculatedDate+ " for account " + account.getBAN());
			}
		} else {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx,MODULE,"Dunning policy is not found for account"+ account.getBAN());
			}
		}
		return calculatedDate;
	}

	/**
	 * Get Debt age of the oldest unpaid invoice Debt age calculated in days
	 * 
	 * @param ctx
	 * @param account
	 * @return in
	 * @throws FinancialAccountException
	 */
	private static int getCurrentDebtAge(Context ctx, Account account)
			throws FinancialAccountException {
		int debtAge = 0;
		if(isDebtOutsResult!=null){
			if(isDebtOutsResult.isDebtOutstanding()){
				debtAge = (int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(isDebtOutsResult.getAgedDebt().getDueDate(), new Date());
			}
		}else{
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE, "Debt Outs Result is Null for account: " + account.getBAN());
			}
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE, "Calculate Current Debt Age : " + debtAge+ " for Account: " + account.getBAN());
		}
		return debtAge;
	}

	
	/**
	 * Calculate past due amount from invoice
	 * 
	 * @param ctx
	 * @param account
	 * @return
	 * @throws FinancialAccountException
	 */
	private static long getPastDueAmount(Context ctx, Account account, long paymentSinceLastInvoice)
			throws FinancialAccountException {
		if (lastInvoice != null) {
			Date currentDate = new Date();
			if (currentDate.before(lastInvoice.getDueDate())) {
				//Invoice preToLastInvoice = InvoiceSupport.getMostRecentInvoice(
				//		ctx, account.getBAN(), lastInvoice.getInvoiceDate());
				//if (preToLastInvoice != null) {
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, MODULE,
								"Past due amout for account" + account.getBAN()
										+ " Past Due Amount:"
										+ lastInvoice.getPreviousBalance()
										+ " + "
										+ lastInvoice.getPaymentAmount()
										+ " + "
										+ paymentSinceLastInvoice);
					}
					return lastInvoice.getPreviousBalance()
							+ lastInvoice.getPaymentAmount()
							+ paymentSinceLastInvoice;
				//}
			} else {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE, "Past due amout for account"
							+ account.getBAN() + " Past Due Amount:"
							+ lastInvoice.getTotalAmount()
							+ " + "
							+ paymentSinceLastInvoice);
				}
				return lastInvoice.getTotalAmount()
						+ paymentSinceLastInvoice;
			}
		}
		return 0;
	}

	
	
	protected static List<AgedDebt> getAgedDebts(final Context context, final Account account, final DunningPolicy policy){
	        Date oldestAgedDebtToLook = DunningProcessHelper.getOldestAgedDebtToLook(context, account, policy,new Date());
	        LogSupport.info(context, MODULE, "[getAgedDebts] oldestAgedDebtToLook:"+oldestAgedDebtToLook.toString());
	        return account.getInvoicedAgedDebt(context, oldestAgedDebtToLook, true);
	}
	 
	private static Currency retrieveCurrency(Context context, Account account) {
        Currency currency = (Currency) context.get(Currency.class);
        if (currency==null || !currency.getCode().equals(account.getCurrency()))
        {
            try
            {
                currency = HomeSupportHelper.get(context).findBean(context, Currency.class,
                        new EQ(CurrencyXInfo.CODE, account.getCurrency()));
            }
            catch (final Exception exception)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to retrieve currency '");
                cause.append(account.getCurrency());
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append(" for account '");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, MODULE, sb.toString(), exception);
            }
        }

        if (currency == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Currency '");
            cause.append(account.getCurrency());
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.minor(context, MODULE, sb.toString());
        }
        return currency;
    }

	private static DunningPolicy getDunningPolicyOfAccount(Context ctx,
			Account account) {
		try{
			return account.getDunningPolicy(ctx);
		}catch(HomeException he)
		{
			LogSupport.info(ctx, MODULE, "Exception while determining Dunning Policy for "+account.getBAN(),he);
		}
		return null;
	}

	/**
	 * Retrieve configured Invoice Delivery Option.
	 * 
	 * @param ctx
	 * @param account
	 * @return
	 */
	private static String getInvoiceDeliveryMethod(Context ctx, Account account) {
		InvoiceDeliveryOption invoiceDeliveryOption = null;
		try {
			final Home invoiceDeliveryOptionHome = (Home) ctx
					.get(InvoiceDeliveryOptionHome.class);
			invoiceDeliveryOption = (InvoiceDeliveryOption) invoiceDeliveryOptionHome
					.find(ctx,
							new EQ(InvoiceDeliveryOptionXInfo.ID, account
									.getInvoiceDeliveryOption()));
			if (invoiceDeliveryOption != null) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"Found Invoice Delivery Option "
									+ invoiceDeliveryOption.getId()
									+ " for Account " + account.getBAN());
				}
				return invoiceDeliveryOption.getDisplayName();
			}
		} catch (final HomeException hEx) {
			LogSupport.minor(ctx, MODULE,
					"Exception caught when looking up Invoice Delivery Option "
							+ account.getInvoiceDeliveryOption(), hEx);
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"Can not found Invoice Delivery Option for account "
							+ account.getBAN());
		}
		return null;
	}

	/**
	 * Get Tax Exemption Inclusion using identifier which is configure on
	 * account
	 * 
	 * @param ctx
	 * @param account
	 * @return
	 */
	private static String getTaxExemptionInclusion(Context ctx, Account account) {
		TaxExemptionInclusion taxExemptionInclusion = null;
		try {
			final Home taxExemptionInclusionHome = (Home) ctx
					.get(TaxExemptionInclusionHome.class);
			taxExemptionInclusion = (TaxExemptionInclusion) taxExemptionInclusionHome
					.find(ctx,
							new EQ(TaxExemptionInclusionXInfo.ID, account
									.getTEIC()));
			if (taxExemptionInclusion != null) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"Found Tax Exemption Inclusion "
									+ taxExemptionInclusion.getName()
									+ " for account " + account.getBAN());
				}
				return taxExemptionInclusion.getName();
			}
		} catch (final HomeException hEx) {
			LogSupport.minor(ctx, MODULE,
					"Exception caught when looking up Tax Exemption Inclusion for account "
							+ account.getBAN(), hEx);
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"Can not found Tax Exemption Inclusion " + account
							+ " for account " + account.getBAN());
		}
		return null;
	}

	/**
	 * Return Payment plan name using payment plan id
	 * 
	 * @param ctx
	 * @param account
	 * @return
	 */
	private static String getPaymentPlan(Context ctx, Account account) {
		PaymentPlan paymentPlan = null;
		try {
			final Home paymentPlanHome = (Home) ctx.get(PaymentPlanHome.class);
			paymentPlan = (PaymentPlan) paymentPlanHome.find(
					ctx,
					new EQ(PaymentPlanXInfo.ID, Long.valueOf(account
							.getPaymentPlan())));
			if (paymentPlan != null) {
				if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, MODULE,
							"Found payment Plan " + paymentPlan.getName()
									+ " for account " + account.getBAN());
				}
				return paymentPlan.getName();
			}
		} catch (final HomeException hEx) {
			LogSupport.minor(ctx, MODULE,
					"Exception caught when looking up payment plan for account "
							+ account.getBAN(), hEx);
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(
					ctx,
					MODULE,
					"Can not found Payment Plan for account "
							+ account.getBAN());
		}
		return null;
	}

	/**
	 * Return past due date of invoice
	 * 
	 * @param ctx
	 * @param accountID
	 * @return
	 */
	private static Date getPastDueDate(Context ctx, String accountID) {
		lastInvoice = InvoiceSupport.getMostRecentInvoice(ctx, accountID);
		if (lastInvoice != null) {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, MODULE,
						"Account " + accountID + " , Last Invoice Due Date "
								+ lastInvoice.getDueDate());
			}
			return lastInvoice.getDueDate();
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, MODULE,
					"Last invoice can not found for account " + accountID);
		}
		return null;
	}

	/**
	 * Execute Account Balance Query
	 * 
	 * @param ctx
	 * @param header
	 * @param accountID
	 * @param balanceTypes
	 * @param genericParameters
	 * @return
	 * @throws CRMExceptionFault
	 */
	public static GenericParameter[] executeAccountBalanceQuery(Context ctx,
			CRMRequestHeader header, String accountID, String[] balanceTypes,
			GenericParameter[] genericParameters) throws CRMExceptionFault {
		QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
		ExecuteResult result = executor.execute(ctx,
				AccountServiceSkeletonInterface.class.getSimpleName(),
				"executeAccountBalanceQuery", ExecuteResult.class, header,
				accountID, balanceTypes, genericParameters);
		GenericParameter[] parameters = result.getParameters();
		return parameters;
	}

	public static List<FinancialAccount> Main(Context ctx, String rootAccount,String pageKey,int limit)
			throws FinancialAccountException, CRMExceptionFault, HomeException {
		responsibleAccountList = new ArrayList<Account>();
		CRMRequestHeader header = new CRMRequestHeader();
		header.setPassword("rkadm");
		header.setUsername("rkadm");
		GenericParameter[] genericParameters = null;
		return getFinancialAccountInfo(ctx,header,rootAccount,genericParameters,pageKey,limit);
	}

	public static GenericParameter addGenericParameter(Context ctx, String key,
			Object value) {
		GenericParameter parameter = new GenericParameter();
		if (key != null) {
			parameter.setName(key);
			parameter.setValue(value);
		}
		return parameter;
	}

	public static Calendar toCalendar(Date paymentDueDate) {
		Calendar cal = null;
		if (paymentDueDate != null) {
			cal = Calendar.getInstance();
			cal.setTime(paymentDueDate);
		}
		return cal;
	}

	public static String getNextInvoice(Context ctx, String accountID) {
		// TODO Auto-generated method stub
		String nextInvoice = "";
		BusinessRule br = new BusinessRule();
		List actions = new ArrayList();
		Iterator<String> itr = actions.iterator();
		String actionElement;
		String directoryPattern;
		String fileNamePattern;
		if ((br.getEventType().toString().equalsIgnoreCase(Invoice_Event))
				&& (br.getInvoiceProcessingType().toString()
						.equalsIgnoreCase(Invoice_Preview))) {
			actions = br.getActions();
			while (itr.hasNext()) {
				actionElement = itr.next();
				if (actionElement.contains("directoryPattern")
						&& actionElement.contains("fileNamePattern")) {
					directoryPattern = actionElement.substring(
							actionElement.indexOf("directoryPattern"),
							actionElement.indexOf("/,") + 1).replace(
							"directoryPattern: ", "");
					fileNamePattern = actionElement.substring(
							actionElement.indexOf("fileNamePattern"),
							actionElement.indexOf(".pdf)") + 4).replace(
							"fileNamePattern: ", "");
					nextInvoice = directoryPattern + fileNamePattern;

				}
			}

		}
		return nextInvoice;
	}

	public final static String Invoice_Event = "Invoice Event";
	public final static String Invoice_Preview = "Invoice Preview";
	private static final String MODULE = FinancialAccountSupport.class
			.getName();
}