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
package com.trilogy.app.crm.home.account;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * The Payment Plan feature only allows an Account to enter into a 
 * Payment Plan a maximum of maxNumOfPaymentPlans (See SPID) over a 
 * time interval defined by paymentPlanInterval (See SPID) from the 
 * time of the Accounts first Payment Plan. 
 * 
 * An account's paymentPlanStartDate is set upon enrolling in a Payment
 * Plan, however, it is not reset again until the paymentPlanInterval 
 * has expired.
 * 
 * This check is performed if the Payment Plan History Feature is enabled.
 * For details, see PaymentPlanSupport class.
 * @author Angie Li 
 */
public class AccountPaymentPlanIntervalValidador implements Validator {

    protected static AccountPaymentPlanIntervalValidador instance__=null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    
    public static AccountPaymentPlanIntervalValidador instance()
    {
        if(instance__==null)
        {
            instance__ = new AccountPaymentPlanIntervalValidador();
        }
        return instance__;
    }
    
    /**
     * If the Payment Plan License is enabled, validate the Payment Plan Interval date.
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException 
    {
        if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
        {
            CompoundIllegalStateException el = new CompoundIllegalStateException();
            Account account = (Account)obj;
            
            try
            {
                if (AccountSupport.hasPaymentPlanChanged(ctx, account) &&
                        PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, account.getPaymentPlan()) &&
                        PaymentPlanSupportHelper.get(ctx).isHistoryEnabled(ctx))
                {
                    /* Throw an error if the Account has exceeded the maxNumOfPaymentPlans (See SPID)
                     * during the paymentPlanInterval. */
                    CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
                    int maxNumOfAllowedEnrollments = spid.getMaxNumOfPaymentPlans();
                    int monthsInInterval = spid.getPaymentPlanInterval();
                    Date endOfInterval = new Date();
                    // Find date X months ago, where X is the Intervals configured in the SPID.
                    Date startOfInterval = CalendarSupportHelper.get(ctx).findDateMonthsAfter(-monthsInInterval, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(endOfInterval));
                    
                    //Count number of time the account has entered Payment Plan during that period.
                    long numEnrollments = PaymentPlanSupportHelper.get(ctx).countPaymentPlanEnrollments(ctx, account.getBAN(), startOfInterval, endOfInterval);
                    
                    if (numEnrollments < maxNumOfAllowedEnrollments)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Validation Passed. Account " + account.getBAN() + 
                                    " can join Payment Plan " + account.getPaymentPlan() + ".", null).log(ctx);
                        }
                    }
                    else
                    {
                        /* the account has reached the maximum number of Payment Plan enrollments during the 
                         * allotted interval.  Log error. */
                        final MessageMgr msg_mgr = new MessageMgr(ctx, this);
                        // List ordered by most recent Record to the furthest in the past. Descending chronological order.
                        Collection<PaymentPlanHistory> list = PaymentPlanSupportHelper.get(ctx).getLastEnrollments(ctx, account.getBAN(), 
                                maxNumOfAllowedEnrollments, new Date());
                        PaymentPlanHistory oldestEnrollment = null;
                        for (PaymentPlanHistory current : list)
                        {
                            oldestEnrollment = current;
                        }
                        Date nextEnrollmentDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(monthsInInterval, oldestEnrollment.getRecordDate());
                        
                        // TODO 2010-10-01 DateFormat access needs synchronization
                        el.thrown(new IllegalPropertyArgumentException(
                                msg_mgr.get("Account.paymentPlan.Label", AccountXInfo.PAYMENT_PLAN.getLabel(ctx)),
                                "This account has reached the Maximum Number of Payment Plans enrollments allowed <br/>" +
                                "for this Service Provider. <br/>This account can re-apply for a Payment Plan Loan after " + 
                                dateFormat.format(nextEnrollmentDate)+ "."));
                    }
                }
            }
            catch (HomeException hEx)
            {
                el.thrown(new IllegalStateException("Error during Account Payment Plan Validation.", hEx));
            }
            
            el.throwAll();
        }
    }
}
