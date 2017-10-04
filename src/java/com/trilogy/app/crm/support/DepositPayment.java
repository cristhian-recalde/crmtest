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

public class DepositPayment implements ReleaseDeposit {

	/**
	 * Operation code to segregate different update logic,
	 * Release as payment when releasing from task & Release as per API call i.e.., release as payment
	 * In Release Deposit as Payment release deposit and calculate interest rate based on input parameter.
	 * Deposit and interest rate transaction created on Account and distribute on subscriber.
	 * 
	 * */
	@Override
	public int releaseDeposit(Context ctx, Map parameters,
			List<DepositStatusEnum> statusList, List<Deposit> depositList) throws DepositReleaseException, HomeException {
		
		int status = DepositConstants.depositNotFound;
		StringBuilder noteDesc = new StringBuilder();
		Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
		
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, MODULE, "Found total deposit records "+ depositList.size());
		}
		for (Deposit deposit : depositList) {
			long amount = DepositConstants.DefaultAmount;
			long interestRateReciptnumber = DepositConstants.DefaultInterestRateReciptnumber;
			if (statusList.contains(deposit.getStatus())){
				
				final String csrIdentifier = CoreTransactionSupportHelper.get(ctx).getCsrIdentifier(ctx);
				Account account = null;
				try {
					account = AccountSupport.getAccount(ctx, deposit.getBAN());
					noteDesc.append("Release deposit acccount "+deposit.getBAN());
				} catch (HomeException e) {
					LogSupport.minor(ctx,MODULE,"Error while getting Account for "+ deposit.getBAN());
					throw new DepositReleaseException("Error while getting Account for "+ deposit.getBAN());
				}
				AdjustmentType typeDeposit = DepositSupport.getAdjustmentType(ctx,AdjustmentTypeEnum.PaymentConvertedFromDeposit);
				Transaction depositAmountTransation = TransactionSupport.createAccountTransaction(ctx, account,deposit.getAmountHeld(), 0,typeDeposit, false, false, csrIdentifier,new Date(), new Date(),
						"Deposit interest payment", 0, "", 0,"default", "", "");

				depositAmountTransation = DepositCalculationSupport.createTransation(ctx,depositAmountTransation);
				LogSupport.info(ctx, MODULE,"Transation Created for Deposit Payment "+ depositAmountTransation);
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
						AdjustmentType type = DepositSupport.getAdjustmentType(ctx,AdjustmentTypeEnum.InterestPayment);
						Transaction transactionInterestPayment  = TransactionSupport.createAccountTransaction(ctx, account,amount, 0,type, false, false, csrIdentifier,new Date(), new Date(),
										"Deposit interest payment", 0, "", 0,"default", "", "");
						transactionInterestPayment = DepositCalculationSupport.createTransation(ctx, transactionInterestPayment);
						if(transactionInterestPayment != null){
							interestRateReciptnumber = transactionInterestPayment.getReceiptNum();
							LogSupport.info(ctx, MODULE,"Transation Created for Credit Deposit Interest Rate receipt number "+ transactionInterestPayment.getReceiptNum());
						}
					}
				}

				deposit = DepositSupport.setDepositInfo(ctx,deposit,DepositStatusEnum.RELEASED, deposit.getAmountHeld(),(double) amount, new Date(),interestRateReciptnumber,parameters.get("REASON_CODE"),
						ReleaseTypeEnum.PAYMENT,depositAmountTransation.getReceiptNum(), new Date());
				
				if(DepositSupport.updateDeposit(ctx, deposit)!=null){
					noteDesc.append(" release date "+deposit.getReleaseDate());
					com.redknee.app.crm.home.sub.SubscriberNoteSupport.createDepositRealeaseNote(ctx, MODULE,deposit,noteDesc);
					if(deposit.getProductID() >= 0){// update credit limit when deposit only for subscriber not for product
					Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,deposit.getSubscriptionID());
					if(subscriber!=null){
						try {
							SubscriberCreditLimitSupport.updateCreditLimit(ctx, account,subscriber,-depositAmountTransation.getAmount());
						} catch (Exception e) {
							LogSupport.minor(ctx,MODULE,"Error while update credit limit for "+ subscriber.getId());
							throw new DepositReleaseException("Error while update credit limit for "+ subscriber.getId());
						}
					  }
				   }// end credit limit
				 status = DepositConstants.depositReleaseSuccess;
				}
				
			}
		}
		return status;
	}
	private static final String MODULE = DepositPayment.class.getName();
}
