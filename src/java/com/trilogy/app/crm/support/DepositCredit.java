package com.trilogy.app.crm.support;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.DepositStatusEnum;
import com.trilogy.app.crm.bean.ReleaseTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class DepositCredit implements ReleaseDeposit {

	
	/**
	 * Deposit release could be done by API,Task,Subscriber State change,Operation code to segregate different update logic.
	 * Conditional as received input from API i.e Release as per API call, release as payment.
	 * In Release Deposit as Credit release deposit and calculate interest rate based on input parameter.
	 * Deposit and interest rate transaction created on subscriber.
	 * 
	 */
	@Override
	public int releaseDeposit(Context ctx, Map parameters,
			List<DepositStatusEnum> statusList, List<Deposit> depositList) throws DepositReleaseException, HomeException  {
		
		Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
		int status = DepositConstants.depositNotFound;
		StringBuilder noteDesc = new StringBuilder();
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, MODULE, "Found total deposit records "+ depositList.size());
		}
		for (Deposit deposit : depositList) {
			long amount = DepositConstants.DefaultAmount;
			long interestRateReciptnumber = DepositConstants.DefaultInterestRateReciptnumber;
			noteDesc.append("Release deposit acccount "+deposit.getBAN());
			if (statusList.contains(deposit.getStatus())) {
				Subscriber subscriber = null;
				try {
					subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,deposit.getSubscriptionID());
				} catch (HomeException e) {
					LogSupport.minor(ctx,MODULE,"Error while getting subscriber for "+ deposit.getSubscriptionID() + e, e);
					throw new DepositReleaseException("Error while getting subscriber for "+ deposit.getSubscriptionID());
				}
				if(subscriber == null){
					LogSupport.minor(ctx,MODULE,"Can not find subscriber while getting "+deposit.getSubscriptionID());
					throw new DepositReleaseException("Can not find subscriber while getting "+deposit.getSubscriptionID());
				}
				AdjustmentType depositType = DepositSupport.getAdjustmentType(ctx,AdjustmentTypeEnum.DepositReleaseCredit);
				Transaction depositAmountTransation = TransactionSupport.createTransaction(ctx, subscriber,deposit.getAmountHeld(), depositType);
				if(depositAmountTransation == null)
					throw new DepositReleaseException("Error while creating trasaction on " + subscriber.getBAN());
				LogSupport.info(ctx, MODULE,"Transation Created for Deposit Credit receipt number: "+ depositAmountTransation.getReceiptNum());
				status = DepositConstants.depositReleasePartial;
				
				if(deposit.getProductID() >= 0){
					noteDesc.append(" for Product "+deposit.getProductID());
				 }else{
					 noteDesc.append(" for Subscriber "+deposit.getSubscriptionID());
				 }

				noteDesc.append(" release amount "+CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,currency, deposit.getAmountHeld()));
				if (Boolean.parseBoolean(String.valueOf(parameters.get("CALCULATE_INTREST")))) {
					amount = DepositCalculationSupport.getInterest(ctx,deposit.getDepositType(), deposit.getDepositDate(),new Date(), deposit.getAmountHeld());
					if (amount > DepositConstants.DefaultAmount) {
						noteDesc.append(" and Interest amount "+CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,currency,amount));
						AdjustmentType interestRateType = DepositSupport.getAdjustmentType(ctx,com.redknee.app.crm.bean.AdjustmentTypeEnum.CreditDepositInterest);
						Transaction transactionInterestPayment = TransactionSupport.createTransaction(ctx, subscriber, amount,interestRateType);
						if(transactionInterestPayment!=null){
							interestRateReciptnumber = transactionInterestPayment.getReceiptNum();
							LogSupport.info(ctx, MODULE,"Transation Created for Credit Deposit Interest Rate receipt number "+ transactionInterestPayment.getReceiptNum());
						}
					}
				}
				
				deposit = DepositSupport.setDepositInfo(ctx,deposit,DepositStatusEnum.RELEASED, deposit.getAmountHeld(),(double) amount, new Date(),interestRateReciptnumber,
						parameters.get("REASON_CODE"),ReleaseTypeEnum.CREDIT,depositAmountTransation.getReceiptNum(), new Date());
				if(DepositSupport.updateDeposit(ctx, deposit)!=null){
					noteDesc.append(" release date "+deposit.getReleaseDate());
					com.redknee.app.crm.home.sub.SubscriberNoteSupport.createDepositRealeaseNote(ctx, MODULE,deposit,noteDesc);
					 if(deposit.getProductID() >= 0){// update credit limit when deposit only for subscriber not for product
					  Account account = AccountSupport.getAccount(ctx, deposit.getBAN());
					  if(account!=null){
						  try{
						  SubscriberCreditLimitSupport.updateCreditLimit(ctx, account,subscriber,-depositAmountTransation.getAmount());
						  }catch (Exception e) {
						   LogSupport.minor(ctx,MODULE,"Error while update credit limit for "+ subscriber.getId());
						   throw new DepositReleaseException("Error while update credit limit for "+ subscriber.getId());
						}
					  }
					}
					status = DepositConstants.depositReleaseSuccess;	
				}
			}
		}
		return status;
	}
	
	private static final String MODULE = DepositCredit.class.getName();
	
}
