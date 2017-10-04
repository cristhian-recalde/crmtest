package com.trilogy.app.crm.support;

import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.InterestRate;
import com.trilogy.app.crm.bean.InterestRateXInfo;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.IsNull;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class DepositCalculationSupport{

	public static long getInterest(Context ctx, long depositType, Date depositDate,
			Date releaseDate, double amount) {
		double totalAmount = 0;
		try {
			// Home home = (Home) ctx.get(InterestRateHome.class);
			And filter = new And();
			filter.add(new EQ(InterestRateXInfo.DEPOSIT_TYPE, depositType));
			//filter.add(new GTE(InterestRateXInfo.EXPIRATION_DATE, depositDate));
			Or orFilter = new Or();
				orFilter.add(new GTE(InterestRateXInfo.EXPIRATION_DATE, depositDate));// (exprirationDate >= depositDate) OR (exprirationDate == null)
				orFilter.add(new IsNull(InterestRateXInfo.EXPIRATION_DATE));
			filter.add(orFilter);
			List<InterestRate> interestRateBeans = (List<InterestRate>) HomeSupportHelper.get(ctx).getBeans(ctx, InterestRate.class, filter);

			LogSupport.debug(ctx, MODULE, "Found "+ interestRateBeans.size()+ " interest rate slab");
			
			//for (InterestRate interestRate : interestRateBeans) {
			for (int iterationCount = 0; iterationCount < interestRateBeans.size(); iterationCount++){
				
				InterestRate interestRate = interestRateBeans.get(iterationCount);
				
				if (interestRate.getExpirationDate() == null) {
					interestRate.setExpirationDate(new Date());
				}

				Date startDate = null, endDate = null;

				if (interestRate.getEffectiveDate().getTime() <= depositDate.getTime()&& depositDate.getTime() <= interestRate.getExpirationDate().getTime()) {
					startDate = depositDate;
				} else {
					if (interestRate.getEffectiveDate().getTime() < releaseDate.getTime())
						startDate = interestRate.getEffectiveDate();
				}

				if (interestRate.getEffectiveDate().getTime() <= releaseDate.getTime() && releaseDate.getTime() <= interestRate.getExpirationDate().getTime()) {
					endDate = releaseDate;
				} else {
					if (interestRate.getEffectiveDate().getTime() < releaseDate.getTime())
						endDate = interestRate.getExpirationDate();
				}

				if (startDate != null && endDate != null) {
					
					long days = CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(startDate, endDate);
					
					if(interestRateBeans.size() > 1 && iterationCount != interestRateBeans.size()-1){
						
						/* When multiple slab for interest calculation that time
							getNumberOfDaysBetween(date,date) this function calculate days -1 thats why we need add +1 day
							ex: date1 = "Oct/1/2016", date2 = "Oct/15/2016"; 
							Result:getNumberOfDaysBetween(date1,date2) = 14
							but expected result when multiple slab is 15*/
						
						/*
						 * when only one slab for interest  getNumberOfDaysBetween(date,date) works fine, as we expect */
						
						days = days + 1;
						
					}
					
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, MODULE, "StartDate "+ startDate+" EndDate "+endDate);
						LogSupport.debug(ctx, MODULE, "Interest calculation for "+ days+" days");
					}
					totalAmount = totalAmount + calculateInterest(interestRate.getInterestRate(),days, amount);
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, MODULE, "slab interest rate calculated amount "+totalAmount);
					}
					
				}
			}
			LogSupport.info(ctx, MODULE, "Calculated total interest amount: " + totalAmount);
		} catch (Exception e) {
			LogSupport.minor(ctx, MODULE, "Error while getting Interest rate"
					+ e, e);
		}
		return Math.round(totalAmount);
	}

	public static Transaction createTransation(Context ctx, Transaction transaction)
			throws DepositReleaseException {
		final Home home = (Home) ctx.get(TransactionHome.class);
		if (home == null) {
			throw new DepositReleaseException(
					"System error: no TransactionHome found in context.");
		}
		try {
			transaction = (Transaction) home.create(ctx, transaction);
		} catch (HomeException e) {
			throw new DepositReleaseException(
					"Exception when create new Transaction");
		}
		return transaction;
	}

	private static double calculateInterest(double interestRate, long days,
			double amount) {
		final double interestPerDay = interestRate / 100 / NUMBER_OF_DAYS;
		return amount * interestPerDay * days;
	}

	private static final double NUMBER_OF_DAYS = 365;
	private static final String MODULE = DepositCalculationSupport.class
			.getName();
}
