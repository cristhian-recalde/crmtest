package com.trilogy.app.crm.contract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;


import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoTimeOfDayCalendarSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


public class SubscriptionContractSupport
{
    public final static int SUBSCRIPTION_CONTRACT_NOT_INTIALIZED= -1;
    public final static int SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT = -2;
    public final static int DUMMY_SUBSCRIPITON_CONTRACT_LENGTH = 0;

    public static long applyFirstMonthCredit(Context ctx, Subscriber sub, SubscriptionContract contract,
            Date startDate, boolean isProrated) throws HomeException
    {
        long firstMonthCredit = 0L;
        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            double rate = 1.0;
            Date lastDayOfBC = startDate;
            if (isProrated)
            {
                Account account = sub.getAccount(ctx);
                int billCycleDay = account.getBillCycleDay(ctx);
                Date startBillCycleDate = calculateCycleStartDate(ctx, startDate, billCycleDay);
                final Calendar billingDateCalendar = Calendar.getInstance();
                billingDateCalendar.setTime(startBillCycleDate);
                int daysInMonth = CalendarSupportHelper.get(ctx).getNumberOfDaysInMonth(
                        billingDateCalendar.get(Calendar.MONTH), billingDateCalendar.get(Calendar.YEAR));
                lastDayOfBC = BillCycleSupport.getDateOfBillCycleLastDay(billCycleDay, startDate);
                
                int numOfDaysLeftOver =  getNumberofDaysInBetween(ctx,startDate, lastDayOfBC);
                rate = (double) numOfDaysLeftOver / daysInMonth;
            }
            SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
            long monthlyCredit = getMonthlyCreditAmount(term.getPrepaymentAmount(), term.getPrePaymentLength());
            firstMonthCredit = Math.round(rate * monthlyCredit);
            
            //TT#13011421012
            if( !(SpidSupport.getCRMSpid(ctx, sub.getSpid()).isIgnoreChargeForZeroMontlyContractCredit()
                    && firstMonthCredit == 0)
                    )
            {
                AdjustmentType adj = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                        (int) term.getContractAdjustmentTypeId());
            	// TCBSUP-1089 check if "ignoreAdjustmentLimitValidation" flag is set and set limitExemption flag accordingly.
            	boolean ignoreAdjLimit = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getIgnoreAdjustmentLimitValidation();
            	if(ignoreAdjLimit)
            	{
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "ignoreAdjustmentLimitValidation flag set at SPID level. Ignoring CSR daily adjustment limit validation check" + sub.getId());
                    }
                    createTransactionForSubContract(ctx, sub, -firstMonthCredit, 0, adj, false, true, "system", startDate,
                            startDate, "", lastDayOfBC, lastDayOfBC);
            	}
            	else
            	{
                    createTransactionForSubContract(ctx, sub, -firstMonthCredit, 0, adj, false, false, "system", startDate,
                            startDate, "", lastDayOfBC, lastDayOfBC);
            	}


            }
        }
        
        return -firstMonthCredit;
    }


    public static void applyPrePaymentCharge(Context ctx, Subscriber sub, SubscriptionContract contract, Date startDate)
            throws HomeException
    {
        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
            AdjustmentType adj = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                    (int) term.getContractAdjustmentTypeId());
            
        	// TCBSUP-1089 check if "ignoreAdjustmentLimitValidation" flag is set and set limitExemption flag accordingly.
        	boolean ignoreAdjLimit = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getIgnoreAdjustmentLimitValidation();
        	if(ignoreAdjLimit)
        	{
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "ignoreAdjustmentLimitValidation flag set at SPID level. Ignoring CSR daily adjustment limit validation check" + sub.getId());
                }
                createTransactionForSubContract(ctx, sub, term.getPrepaymentAmount(), 0, adj, false, true, "system",
                        startDate, startDate, "", contract.getContractEndDate(), contract.getContractEndDate());
        	}
        	else
        	{
                createTransactionForSubContract(ctx, sub, term.getPrepaymentAmount(), 0, adj, false, false, "system",
                        startDate, startDate, "", contract.getContractEndDate(), contract.getContractEndDate());
        	}

        }
    }


    public static void applySubscriptionContractRefund(Context ctx, Subscriber sub, SubscriptionContract contract,
            Date cancelDate) throws HomeException
    {
        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
            if (term.isPrepaymentRefund())
            {
                AdjustmentType adj = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                        (int) term.getContractAdjustmentTypeId());
                
            	// TCBSUP-1089 check if "ignoreAdjustmentLimitValidation" flag is set and set limitExemption flag accordingly.
            	boolean ignoreAdjLimit = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getIgnoreAdjustmentLimitValidation();
            	if(ignoreAdjLimit)
            	{
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "ignoreAdjustmentLimitValidation flag set at SPID level. Ignoring CSR daily adjustment limit validation check" + sub.getId());
                    }
                    createTransactionForSubContract(ctx, sub, -contract.getBalancePaymentAmount(), 0, adj, false, true,
                            "system", cancelDate, cancelDate, "", cancelDate, cancelDate);
            	}
            	else
            	{
                    createTransactionForSubContract(ctx, sub, -contract.getBalancePaymentAmount(), 0, adj, false, false,
                            "system", cancelDate, cancelDate, "", cancelDate, cancelDate);
            	}

            }  
        }
    }


    public static void applyEarlyTerminationPenalty(Context ctx, Subscriber sub, SubscriptionContract contract,
            Date penaltyDate, boolean isProrated) throws HomeException
    {
        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            Account account = sub.getAccount(ctx);
            SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
         
            // set flag for trail period if trial period then no cancellation charges else calculate**
            int  trail_days = term.getTrialPeriod();
     
            Calendar c = Calendar.getInstance();   
            c.setTime(contract.getContractStartDate());
            c.add(Calendar.DATE, trail_days);
            Date trialPeriodEndDate  =  c.getTime();
//            trialPeriodEndDate  =  CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(trialPeriodEndDate);
            if (LogSupport.isDebugEnabled(ctx))
            {
            	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "trail_days:" + trail_days + ", trialPeriodEndDate:" + trialPeriodEndDate
            			+ ", penaltyDate:" + penaltyDate );
            }
            
           if (penaltyDate.after(trialPeriodEndDate) || penaltyDate.equals(trialPeriodEndDate)){
            
            if (contract.getContractEndDate().after(penaltyDate))
              {
               // SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
                
                //UMP-604 - PenaltyFeePerMonth should be checked first from SubscriptionContract and if blank then from ContractTerm 
//                SubscriptionContract  subContract = getPenaltyPerMonth(ctx,contract.getContractId());
//                		Long monthlyPenaltObj	= subContract.getPenaltyFeePerMonth();
                		Long monthlyPenaltObj	= contract.getPenaltyFeePerMonth();
                		long monthlyPenalty = 0l;
                 	if(monthlyPenaltObj != null && monthlyPenaltObj > 0 ){
                 		 monthlyPenalty = monthlyPenaltObj.longValue();
                 	}else 
                 		 monthlyPenalty = term.getPenaltyFeePerMonth();
                
                 	if (LogSupport.isDebugEnabled(ctx))
                 	{
                 		LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "monthlyPenalty:" + monthlyPenalty);
                 	}

                 	
                if (term != null)
                {
                    AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                            AdjustmentTypeEnum.PenaltyFee);
                    long maxPenaltyFee = term.getFlatPenaltyFee();
                  //  long monthlyPenalty = term.getPenaltyFeePerMonth();
                    int billCycleDay = account.getBillCycleDay(ctx);
                    CRMSpid spid = SubscriberSupport.getServiceProvider(ctx, sub);
                    boolean isBCDCycleEnabled = spid.getCurrentCancellationCharges();
                    long monthlyPenaltyFee;
                    if(isBCDCycleEnabled){
                     monthlyPenaltyFee = calculateMonthlyPenaltyFee(ctx, contract, monthlyPenalty, billCycleDay,
                            penaltyDate, contract.getContractStartDate(), contract.getContractEndDate(), isProrated);
                    }
                    else{
                     monthlyPenaltyFee = calculatePenaltyOnContractMonthlyCycle(ctx, contract, monthlyPenalty, billCycleDay,
                                penaltyDate, contract.getContractStartDate(), contract.getContractEndDate(), isProrated);
                    }
                    
                    
                    long maximumPenaltyFee = Math.max(maxPenaltyFee, monthlyPenaltyFee);
                    
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                    	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "monthlyPenaltyFee:" + monthlyPenaltyFee);
                    	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "maximumPenaltyFee:" + maximumPenaltyFee);
                    }
                    
                	// TCBSUP-1089 check if "ignoreAdjustmentLimitValidation" flag is set and set limitExemption flag accordingly.
                	boolean ignoreAdjLimit = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getIgnoreAdjustmentLimitValidation();
                	if(ignoreAdjLimit)
                	{
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "ignoreAdjustmentLimitValidation flag set at SPID level. Ignoring CSR daily adjustment limit validation check" + sub.getId());
                        }
                        createTransactionForSubContract(ctx, sub, maximumPenaltyFee, 0, type, false, true, "system",
                                penaltyDate, penaltyDate, "", penaltyDate,penaltyDate);
                	}
                	else
                	{
                        createTransactionForSubContract(ctx, sub, maximumPenaltyFee, 0, type, false, false, "system",
                                penaltyDate, penaltyDate, "", penaltyDate,penaltyDate);
                	}

                }
            }
          } 
        }
    }


    public static SubscriptionContractTerm getSubscriptionContractTerm(Context ctx, SubscriptionContract contract)
            throws HomeException
    {
        return getSubscriptionContractTerm(ctx, contract.getContractId());
    }


    public static SubscriptionContractTerm getSubscriptionContractTerm(Context ctx, long contractId)
            throws HomeException
    {
        SubscriptionContractTerm term = (SubscriptionContractTerm) ctx.get(SubscriptionContractTerm.class);
        if (term == null || term.getId() != contractId)
        {
            term = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContractTerm.class,
                    new EQ(SubscriptionContractTermXInfo.ID, contractId));
            ctx.put(SubscriptionContractTerm.class, term);
        }
        return term;
    }
    	
    public static SubscriptionContract getPenaltyPerMonth(Context ctx, long contractId)
            throws HomeException
    {
        
    	SubscriptionContract subContract = HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContract.class,
                    new EQ(SubscriptionContractXInfo.CONTRACT_ID, contractId));
            ctx.put(SubscriptionContract.class, subContract);
        
        return subContract;
    }
    
    public static boolean isDummyContract(Context ctx, long contractId) throws HomeException
    {
        SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contractId);
        if(term != null && term.getContractLength() == DUMMY_SUBSCRIPITON_CONTRACT_LENGTH)
        {
            return true;
        }
        return false;
    }

    public static long getMonthlyCreditAmount(long totalPrePaymentAmount, int numOfMonths)
    {
        if (numOfMonths > 0)
        {
            return Math.round(totalPrePaymentAmount / numOfMonths);
        }
        return 0L;
    }


    public static long calculateMonthlyPenaltyFee(Context ctx, SubscriptionContract contract, long monthlyPenalty,
            int billCycleDay, Date cancelDate, Date startDate, Date endDate, boolean isProrated) throws HomeException
    {
        long monthlyPenaltySum = 0L;
        
            if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
            {
                ArrayList<Date> canceledBillCycleDates = new ArrayList<Date>();
                if(isProrated)
                {
                    canceledBillCycleDates = getAllBillCyclesInBetweenForProration(ctx, billCycleDay, cancelDate, endDate);
                }
                else
                {
                    canceledBillCycleDates = getAllBillCyclesInBetween(ctx, billCycleDay, cancelDate, endDate);
                }
                Date startBillCycleDate = calculateCycleStartDate(ctx, cancelDate, billCycleDay);
                Date endBillCycleDate = calculateCycleEndDate(ctx, endDate, billCycleDay);
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "canceledBillCycleDates:" + canceledBillCycleDates);
                	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), ", startBillCycleDate:" + startBillCycleDate
                			+ ", endBillCycleDate:" + endBillCycleDate );
                	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "isProrated:" + isProrated);
                }
                
                if (isProrated)
                {
                    Date lastBillCycleDate = endDate; // If cancel period happens on the last
                                                      // billcycle of the contract
                    if (!canceledBillCycleDates.isEmpty())
                    {
                        // special prorated cacluation is required last billcycle
                        lastBillCycleDate = canceledBillCycleDates.get(canceledBillCycleDates.size() - 1);
                        // canceledBillCycleDates.remove(canceledBillCycleDates.size()-1);
                        for (Iterator<Date> iter = canceledBillCycleDates.iterator(); iter.hasNext();)
                        {
                            Date curBillDate = iter.next();
                            if (!lastBillCycleDate.equals(curBillDate) && (!curBillDate.before(cancelDate)))
                            {
                                monthlyPenaltySum += monthlyPenalty;
                            }
                        }
                    }
                    monthlyPenaltySum += calculateFirstMonthPenalty(ctx, monthlyPenalty, startBillCycleDate, endDate,
                            lastBillCycleDate, cancelDate, billCycleDay);
                    monthlyPenaltySum += calculateLastMonthPenalty(ctx, monthlyPenalty, startBillCycleDate, endDate,
                            lastBillCycleDate);
                }
                else
                {// If we are paying full amount, no proration
                    int addCurrentBillCycle = 0;
                    
                    int leadingNumOfDays = (int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(startBillCycleDate, cancelDate);
                    int precedingNumOfDays = (int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(endBillCycleDate, endDate);

                    if (LogSupport.isDebugEnabled(ctx))
                    {
                    	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "leadingNumOfDays:" + leadingNumOfDays 
                    			+ ", precedingNumOfDays:" + precedingNumOfDays);
                    }

                    ArrayList<Date> canceledBillCycles = getAllBillCyclesInBetween(ctx, billCycleDay, startDate, cancelDate);
                    
                    SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
                    boolean isPartiallyElapsed = term.isPartialMonthElapsed();
                  
                    if((leadingNumOfDays+precedingNumOfDays) > 31 && !canceledBillCycles.isEmpty())
                    {
                        addCurrentBillCycle = 1;
                    }
                    else if (startBillCycleDate.equals(cancelDate) || (leadingNumOfDays+precedingNumOfDays) < 31)
                    {
                        // if start Bill Cycle is day as cancelDate, we don't need to add
                        addCurrentBillCycle = 0;
                    }
                    
                    if(isPartiallyElapsed){
                    	canceledBillCycleDates.remove(0); 
                     }
                    
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                    	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "canceledBillCycleDates.size():" + canceledBillCycleDates.size() 
                    	+ ", addCurrentBillCycle:" + addCurrentBillCycle);
                    }
                                      
                    monthlyPenaltySum = (canceledBillCycleDates.size() + addCurrentBillCycle) * monthlyPenalty;   
                }
            }
        
        
        return monthlyPenaltySum;
    }


    public static long calculateFirstMonthPenalty(Context ctx, long monthlyCredit, Date startBillCycleDate,
            Date endDate, Date lastBillCycleDate, Date cancelDate, int billCycleDay)
    {
        final Calendar billingDateCalendar = Calendar.getInstance();
        billingDateCalendar.setTime(startBillCycleDate);
        int billingMonth = billingDateCalendar.get(Calendar.MONTH);
        int daysInMonth = CalendarSupportHelper.get(ctx).getNumberOfDaysInMonth(billingMonth,
                billingDateCalendar.get(Calendar.YEAR));
        double rate = 0;
        
        Date endBillCycleDate = BillCycleSupport.getDateOfBillCycleLastDay(billCycleDay, startBillCycleDate);
        int numOfDays = getNumberofDaysInBetween(ctx, cancelDate, endBillCycleDate);
        if (lastBillCycleDate.equals(endDate))
        {
            numOfDays = (int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(cancelDate, endDate);
        }

        if (daysInMonth > 0)
        {
            rate = (double) numOfDays / daysInMonth;
        }
        long firstMonthPenalty = Math.round(rate * monthlyCredit);
        return firstMonthPenalty;
    }


    public static long calculateLastMonthPenalty(Context ctx, long monthlyCredit, Date startBillCycleDate,
            Date endDate, Date lastBillCycleDate)
    {
        final Calendar billingDateCalendar = Calendar.getInstance();
        billingDateCalendar.setTime(lastBillCycleDate);
        int billingMonth = billingDateCalendar.get(Calendar.MONTH);
        int daysInMonth = CalendarSupportHelper.get(ctx).getNumberOfDaysInMonth(billingMonth,
                billingDateCalendar.get(Calendar.YEAR));
        double rate = 0;
        if (!lastBillCycleDate.equals(endDate))
        {
            int numOfDays = (int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(lastBillCycleDate, endDate);
            
            if (daysInMonth > 0)
            {
                rate = (double) numOfDays / daysInMonth;
            }
        }
        long lastMonthPenalty = Math.round(rate * monthlyCredit);
        return lastMonthPenalty;
    }

    public static ArrayList<Date> getAllBillCyclesInBetween(Context ctx, int billCycleDay, Date startDate, Date endDate)
    {
        ArrayList<Date> list = new ArrayList<Date>();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.DAY_OF_MONTH, billCycleDay);
        Date curDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(calendar.getTime());
        if(curDate.equals(startDate) || startDate.before(curDate))
        {
        	list.add(curDate);
        }
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);
        
        Date updatedDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(calendar.getTime());
        while(updatedDate.before(endDate) || updatedDate.equals(endDate))
        {
        	list.add(updatedDate);
        	calendar.setTime(updatedDate);
            calendar.add(Calendar.MONTH, 1);
            updatedDate = calendar.getTime();
        }
        
        return list;
    }
    
    //System would calculate Contract Monthly cycle and not Billing cycle.
    public static long calculatePenaltyOnContractMonthlyCycle(Context ctx, SubscriptionContract contract, long monthlyPenalty,
            int billCycleDay, Date cancelDate, Date startDate, Date endDate, boolean isProrated) throws HomeException
    {
        long monthlyPenaltySum = 0L;
        
            if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
            {
                ArrayList<Date> canceledBillCycleDates = new ArrayList<Date>();
                ArrayList<Date> canceledBillCycleDatesNonProrated = new ArrayList<Date>();
                if(isProrated)
                {
                    canceledBillCycleDates = getAllBillCyclesInBetweenForProration(ctx, billCycleDay, cancelDate, endDate);
                }
                else
                {
                    canceledBillCycleDatesNonProrated = getAllCancelledBillCycles(ctx, billCycleDay, cancelDate, endDate);
                }
                Date startBillCycleDate = calculateCycleStartDate(ctx, cancelDate, billCycleDay);
                Date endBillCycleDate = calculateCycleEndDate(ctx, endDate, billCycleDay);
                
                
                if (isProrated)
                {
                    Date lastBillCycleDate = endDate; // If cancel period happens on the last
                                                      // billcycle of the contract
                    if (!canceledBillCycleDates.isEmpty())
                    {
                        // special prorated cacluation is required last billcycle
                        lastBillCycleDate = canceledBillCycleDates.get(canceledBillCycleDates.size() - 1);
                        // canceledBillCycleDates.remove(canceledBillCycleDates.size()-1);
                        for (Iterator<Date> iter = canceledBillCycleDates.iterator(); iter.hasNext();)
                        {
                            Date curBillDate = iter.next();
                            if (!lastBillCycleDate.equals(curBillDate) && (!curBillDate.before(cancelDate)))
                            {
                                monthlyPenaltySum += monthlyPenalty;
                            }
                        }
                    }
                    monthlyPenaltySum += calculateFirstMonthPenalty(ctx, monthlyPenalty, startBillCycleDate, endDate,
                            lastBillCycleDate, cancelDate, billCycleDay);
                    monthlyPenaltySum += calculateLastMonthPenalty(ctx, monthlyPenalty, startBillCycleDate, endDate,
                            lastBillCycleDate);
                }
                else
                {// If we are paying full amount, no proration
                    int addCurrentBillCycle = 0;
                    
                    SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
                    boolean isPartiallyElapsed = term.isPartialMonthElapsed();
                    
                    if(canceledBillCycleDatesNonProrated.size()>0){
                    int leadingNumOfDays = calculateLeadingNumberOfDays(ctx, cancelDate, billCycleDay);
                    int trailingNumOfDays = calculateTrailingNumberOfDays(ctx, endDate, billCycleDay);
                                        
                    if((leadingNumOfDays+trailingNumOfDays) > 31)
                     {
                        addCurrentBillCycle = 1;
                     }
                    if (isPartiallyElapsed){
           	                 canceledBillCycleDatesNonProrated.remove(0);
                        }
                    }else{
                    	Date contractCancelDt = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(cancelDate);
                        Date contractEndDt = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(endDate);
                    	if(contractCancelDt.before(contractEndDt)){
                    		  addCurrentBillCycle = 1;
                    		}
                    	if (isPartiallyElapsed){
           	                 canceledBillCycleDatesNonProrated.remove(0);
                            }
                    }
                    monthlyPenaltySum = (canceledBillCycleDatesNonProrated.size() + addCurrentBillCycle) * monthlyPenalty;
                }
            }
        
        
        return monthlyPenaltySum;
    }
    
    
    private static ArrayList<Date> getAllCancelledBillCycles(Context ctx, int billCycleDay, Date contractCancelDateTime, Date contractEndDateTime)
    {
        ArrayList<Date> list = new ArrayList<Date>();
        final Calendar calendar = Calendar.getInstance();
        Date contractCancelDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(contractCancelDateTime);
        Date contractEndDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(contractEndDateTime);
        calendar.setTime(contractCancelDate);
        calendar.set(Calendar.DAY_OF_MONTH, billCycleDay);
        Date billCycleDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(calendar.getTime());
        if(contractCancelDate.before(billCycleDate) && billCycleDate.before(contractEndDate))
        {
        	list.add(billCycleDate);
        }
        calendar.setTime(billCycleDate);
        calendar.add(Calendar.MONTH, 1);
        
        Date updatedDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(calendar.getTime());
        while(updatedDate.before(contractEndDate))
        {
        	list.add(updatedDate);
        	calendar.setTime(updatedDate);
            calendar.add(Calendar.MONTH, 1);
            updatedDate = calendar.getTime();
        }
        
        return list;
    }
    
    private static int calculateLeadingNumberOfDays (final Context context, final Date contractCancelDateTime, final int billingCycleDay)
    {
        if (billingCycleDay < CoreCrmConstants.MIN_BILL_CYCLE_DAY
                || billingCycleDay > CoreCrmConstants.MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalArgumentException("BillCycle day must be between 1 and 28");
        }
        Date contractCancelDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(contractCancelDateTime);
        final Calendar cal = Calendar.getInstance();
        int adjustment = 0;
        cal.setTime(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(contractCancelDate));
        cal.set(Calendar.DAY_OF_MONTH, billingCycleDay);
        if (contractCancelDate.before(cal.getTime()))
        {
        }
        else {
        	adjustment = 31 - cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        	cal.add(Calendar.MONTH, 1);
        	
        }
        return (int) CalendarSupportHelper.get(context).getNumberOfDaysBetween(contractCancelDate, cal.getTime()) + adjustment;
    }
    
    private static int calculateTrailingNumberOfDays (final Context context, final Date contractEndDateTime, final int billingCycleDay)
    {
        if (billingCycleDay < CoreCrmConstants.MIN_BILL_CYCLE_DAY
                || billingCycleDay > CoreCrmConstants.MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalArgumentException("BillCycle day must be between 1 and 28");
        }
        Date contractEndDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(contractEndDateTime);
        final Calendar cal = Calendar.getInstance();
        int adjustment = 0;
        cal.setTime(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(contractEndDate));
        cal.set(Calendar.DAY_OF_MONTH, billingCycleDay);
        if (contractEndDate.after(cal.getTime()))
        {
        }
        else {
        	cal.add(Calendar.MONTH, -1);
        	adjustment = 31 - cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        return (int) CalendarSupportHelper.get(context).getNumberOfDaysBetween(cal.getTime(), contractEndDate) + adjustment;
    }
    


    public static Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay)
    {
        if (billingCycleDay < CoreCrmConstants.MIN_BILL_CYCLE_DAY
                || billingCycleDay > CoreCrmConstants.MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalArgumentException("BillCycle day must be between 1 and 28");
        }
        final Calendar cycleStartCalendar = Calendar.getInstance();
        cycleStartCalendar.setTime(CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(billingDate));
        cycleStartCalendar.set(Calendar.DAY_OF_MONTH, billingCycleDay);
        if (cycleStartCalendar.getTime().after(billingDate))
        {
            cycleStartCalendar.add(Calendar.MONTH, -1);
        }
        return CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(cycleStartCalendar.getTime());
    }


    public static Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay)
    {
        if (billingCycleDay < CoreCrmConstants.MIN_BILL_CYCLE_DAY
                || billingCycleDay > CoreCrmConstants.MAX_BILL_CYCLE_DAY)
        {
            throw new IllegalStateException("BillCycle day must be between 1 and 28");
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(billingDate);
        final int dayOfBillingDate = cal.get(Calendar.DAY_OF_MONTH);
        if (dayOfBillingDate >= billingCycleDay)
        {
            cal.add(Calendar.MONTH, 1);
        }
        cal.set(Calendar.DAY_OF_MONTH, billingCycleDay);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return CalendarSupportHelper.get(context).getDateWithLastSecondofDay(cal.getTime());
    }


    public static Subscriber getSubscriber(Context ctx, SubscriptionContract contract) throws HomeException
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null || sub.getId().equals(contract.getSubscriptionId()))
        {
            sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class,
                    new EQ(SubscriberXInfo.ID, contract.getSubscriptionId()));
            ctx.put(Subscriber.class, sub);
        }
        return sub;
    }
    public static int getNumberofDaysInBetween(final Context ctx, final Date startDate, final Date endDate)
    {
        Date startDateWithNoMins = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate);
        // If billcycle is 1, then it midnight of next billcycle
        Date endDateWithNoMins = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getDaysAfter(endDate, 1));
        //This will now include activation date as well
        
        /**
         * NoTimeOfDayCalendarSupport.java has been introduced while fixing TT#13100551001.
         * We have implemented only one method in this class - "getNumberOfDaysBetween" which will
         * take care of time zone as well.
         * Right now we have made use of its direct reference instead of installing it in subCtx because 
         * this is the only use case which is using this method.
         * Eventually , we can make use of subCtx if required at some other location.
         */
        int numOfDaysLeftOver = (int)NoTimeOfDayCalendarSupport.instance().getNumberOfDaysBetween(startDateWithNoMins, endDateWithNoMins);
        return numOfDaysLeftOver;
    }
    
    private static Transaction createTransactionForSubContract(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean prorated, final boolean limitExemption,
            final String csrIdentifier, final Date billingDate, final Date receivingDate, final String csrInput, final Date serviceRevenueRecognDate , final Date serviceEndDate) throws HomeException
    {
        final int expiryDaysExt = 0;
        Transaction transaction = com.redknee.app.crm.support.TransactionSupport.createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, prorated, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        
        transaction.setServiceRevenueRecognizedDate(CalendarSupportHelper.get(context).getDateWithLastSecondofDay(serviceRevenueRecognDate));
        transaction.setServiceEndDate(serviceEndDate);
        transaction = CoreTransactionSupportHelper.get(context).createTransaction(context, transaction, true);
        return transaction;

    }
    
    public static void applyContractRenewalCredit(Context ctx, Subscriber sub, SubscriptionContract contract, Date startDate)
            throws HomeException
    {
        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
            AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                    AdjustmentTypeEnum.RenewalCredit);
            
        	// TCBSUP-1089 check if "ignoreAdjustmentLimitValidation" flag is set and set limitExemption flag accordingly.
        	boolean ignoreAdjLimit = SpidSupport.getCRMSpid(ctx, sub.getSpid()).getIgnoreAdjustmentLimitValidation();
        	if(ignoreAdjLimit)
        	{
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "ignoreAdjustmentLimitValidation flag set at SPID level. Ignoring CSR daily adjustment limit validation check" + sub.getId());
                }
                createTransactionForRenewalCredit(ctx, sub, term.getRenewalCreditAmount(), 0, type, false, true, "system",
                        startDate, startDate, "", contract.getContractEndDate(), contract.getContractEndDate());
        	}
        	else
        	{
                createTransactionForRenewalCredit(ctx, sub, term.getRenewalCreditAmount(), 0, type, false, false, "system",
                        startDate, startDate, "", contract.getContractEndDate(), contract.getContractEndDate());
        	}

        }
    }
    
    private static Transaction createTransactionForRenewalCredit(final Context context, final Subscriber subscriber, final long amount,
            final long newBalance, final AdjustmentType type, final boolean prorated, final boolean limitExemption,
            final String csrIdentifier, final Date billingDate, final Date receivingDate, final String csrInput, final Date serviceRevenueRecognDate , final Date serviceEndDate) throws HomeException
    {
        final int expiryDaysExt = 0;
        Transaction transaction = com.redknee.app.crm.support.TransactionSupport.createSubscriberTransactionObject(context, subscriber, amount, newBalance,
                type, prorated, limitExemption, csrIdentifier, billingDate, receivingDate, csrInput,
                expiryDaysExt);
        
        transaction.setServiceRevenueRecognizedDate(CalendarSupportHelper.get(context).getDateWithLastSecondofDay(serviceRevenueRecognDate));
        transaction.setServiceEndDate(serviceEndDate);
        transaction = CoreTransactionSupportHelper.get(context).createTransaction(context, transaction, true);
        return transaction;

    }
    
    public static long getCurrentPenaltyFee(Context ctx, Subscriber sub, SubscriptionContract contract,
            Date penaltyDate, boolean isProrated) throws HomeException
    {
        long finalPenaltyFee = 0l;
        if (LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "SubContract:" + contract);
        }

        if(!SubscriptionContractSupport.isDummyContract(ctx, contract.getContractId()))
        {
            Account account = sub.getAccount(ctx);
            SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
       
            // set flag for trail period if trial period then no cancellation charges else calculate**
            int  trail_days = term.getTrialPeriod();
            	
            Calendar c = Calendar.getInstance();    
            c.setTime(contract.getContractStartDate());
            c.add(Calendar.DATE, trail_days);           
            Date trialPeriodEndDate  =  c.getTime();
            trialPeriodEndDate  =  CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(trialPeriodEndDate);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
            	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "trail_days:" + trail_days + ", trialPeriodEndDate:" + trialPeriodEndDate
            			+ ", penaltyDate:" + penaltyDate );
            }
           
            if (penaltyDate.after(trialPeriodEndDate) || penaltyDate.equals(trialPeriodEndDate)){
            		
            if (contract.getContractEndDate().after(penaltyDate))
            {
            	 // SubscriptionContractTerm term = getSubscriptionContractTerm(ctx, contract);
                     
           //UMP-604 - PenaltyFeePerMonth should be checked first from SubscriptionContract and if blank then from ContractTerm 
//                SubscriptionContract  subContract = getPenaltyPerMonth(ctx,contract.getContractId());
//                		Long monthlyPenaltObj	= subContract.getPenaltyFeePerMonth();
        		Long monthlyPenaltObj	= contract.getPenaltyFeePerMonth();
                		long monthlyPenalty = 0l;
                 	if(monthlyPenaltObj != null && monthlyPenaltObj > 0 ){
                 		 monthlyPenalty = monthlyPenaltObj.longValue();
                 	}else 
                 		 monthlyPenalty = term.getPenaltyFeePerMonth();
                 	
                 	if (LogSupport.isDebugEnabled(ctx))
                 	{
                 		LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "monthlyPenalty:" + monthlyPenalty);
                 	}

                   long maxPenaltyFee = term.getFlatPenaltyFee();
                 //   long monthlyPenalty = term.getPenaltyFeePerMonth();
                    int billCycleDay = account.getBillCycleDay(ctx);
                    CRMSpid spid = SubscriberSupport.getServiceProvider(ctx, sub);
                    boolean isBCDCycleEnabled = spid.getCurrentCancellationCharges();
                    long monthlyPenaltyFee;
                    if(isBCDCycleEnabled){
                     monthlyPenaltyFee = calculateMonthlyPenaltyFee(ctx, contract, monthlyPenalty, billCycleDay,
                            penaltyDate, contract.getContractStartDate(), contract.getContractEndDate(), isProrated);
                    }
                    else{
                     monthlyPenaltyFee = calculatePenaltyOnContractMonthlyCycle(ctx, contract, monthlyPenalty, billCycleDay,
                                penaltyDate, contract.getContractStartDate(), contract.getContractEndDate(), isProrated);
                    }
                    	
                    long maximumPenaltyFee = Math.max(maxPenaltyFee, monthlyPenaltyFee);
                    finalPenaltyFee = maximumPenaltyFee;
                
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                    	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "monthlyPenaltyFee:" + monthlyPenaltyFee);

                    	LogSupport.debug(ctx, SubscriptionContractSupport.class.getName(), "finalPenaltyFee:" + finalPenaltyFee);
                    }

            }
          } 
        }
        return finalPenaltyFee;
    }

    /**
     * Added during 9.5.5 TT fix TT#13080221005 .
     * 
     * Gives all bill cycle in between two dates.
     * The only difference between getAllBillCyclesInBetweenForProration and getAllBillCyclesInBetween
     * is that if a contract is being canceled on bill cycle day itself and contract cancellation fees is to
     * be  "prorated" then, we must not consider that bill cycle day .
     *
     * The original method (getAllBillCyclesInBetween) was modified while enhancing feature Subscription Contract Support for 9.5.0.
     * During this feature we introduced non-proration calculation of contract cancellation fees .
     * While we modified this method to achieve expected results for non-proration, the existing behavior for
     * "proration" logic got broken .
     * Now, At this stage when the updated method is being used for "non-proration" calculations ,
     * its not a good idea to make changes to it as It would add up a lot of regression 
     * as there are a lot of use cases for both "proration" and "no-proration" .
     * This method will take care of legacy logic for "prorated" calculation of contract cancellation fees .
     * The logic used here the same as that of method which was used before
     * any changes made for "non proration" TT fixes. Reference to original version can be found here:
     * http://floyd/repos/AppCrm/branch/9_5_0/src/java/com/redknee/app/crm/contract/SubscriptionContractSupport.java  
     * 
     * @param ctx
     * @param billCycleDay
     * @param startDate
     * @param endDate
     * @return list of bill cycles
     */
    public static ArrayList<Date> getAllBillCyclesInBetweenForProration(Context ctx, int billCycleDay, Date startDate, Date endDate)
    {
        ArrayList<Date> list = new ArrayList<Date>();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, 1);
        Date curDate = calendar.getTime();
        Date billCycleDate = calculateCycleStartDate(ctx, curDate, billCycleDay);
        while (billCycleDate.before(endDate))
        {
            list.add(billCycleDate);
            calendar.setTime(billCycleDate);
            calendar.add(Calendar.MONTH, 1);
            billCycleDate = calendar.getTime();
        }
        return list;
    }
}
