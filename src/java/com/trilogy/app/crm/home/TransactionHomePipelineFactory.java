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
package com.trilogy.app.crm.home;

import java.rmi.RemoteException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bas.directDebit.UpdatingDirectDebitRecordForPaymentHome;
import com.trilogy.app.crm.bas.recharge.PostpaidRetryRecurRechargeVisitor;
import com.trilogy.app.crm.bas.recharge.PrepaidRetryRecurRechargeVisitor;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.home.core.CoreTransactionHomePipelineFactory;
import com.trilogy.app.crm.home.transaction.TransactionSAPFieldsSettingHome;
import com.trilogy.app.crm.home.transaction.TransactionTaxAuthoritySettingHome;
import com.trilogy.app.crm.home.transaction.TransactionUnifiedReceiptSettingHome;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.transaction.TransactionAdjustmentTypePermissionValidator;
import com.trilogy.app.crm.transaction.TransactionDateComparator;
import com.trilogy.app.crm.transaction.TransactionDatesValidator;
import com.trilogy.app.crm.web.control.AcctNumMsisdnValidator;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.app.crm.xhome.home.GLCodeObtainHome;
import com.trilogy.app.crm.xhome.home.TransactionOwnerTypeSettingHome;
import com.trilogy.app.crm.xhome.home.UserAgentHome;
import com.trilogy.app.crm.xhome.validator.TransactionAdjustmentTypeValidator;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.RMIHomeServer;
import com.trilogy.framework.xhome.home.ReadOnlyHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.writeoff.PaymentToWrittenOffAccountTransactionHome;
import com.trilogy.app.crm.writeoff.PostWrittenOffSubPaymentTransactionHome;

/**
 * Provides a class from which to create the pipeline of Home decorators that process a
 * Transaction travelling between the application and the given delegate.
 *
 * @author gary.anderson@redknee.com
 */
public final class TransactionHomePipelineFactory extends CoreTransactionHomePipelineFactory
{

    /**
     * Private constructor to discourage instantiation.
     */
    public TransactionHomePipelineFactory()
    {
        // Empty
    }


    /**
     * Creates and installs pipelines for Transaction.
     *
     * @param context
     *            The application context.
     * @param serverContext
     *            The context used for remote services.
     * @return The Home representing the head of the pipeline.
     * @exception AgentException
     *                Thrown if there are any problems creating the pipeline.
     */
    @Override
    public Home createPipeline(final Context context, final Context serverContext) throws AgentException
    {
        Home home = null;

        try
        {
            // Get basic pipeline installed by AppCrmCore
            home = new ContextRedirectingHome(context, CoreCrmConstants.DISCOUNT_PLAIN_TXN_HOME);

            home = new TransactionTaxAuthoritySettingHome(context, home);
            home = new PMHome(context, TransactionHome.class.getName() + ".factorybefore.04.Before.UpsOcgForward", home);
            home = new OcgForwardTransactionHome(home);
            
            home = new ExtendSubscriberExpiryHome(home);
            home = new UpdateNextRecurringChargeDateHome(home);
            home = new TopUpScheduleUpdatingOnPlanChangeHome(home);

            home = new UpsForwardTransactionHome(home);
            home = new PMHome(context, TransactionHome.class.getName() + ".factorybefore.05.After.UpsOcgForward", home);
            
            home = new TransactionDebtCollectionAgencyPaymentHome(home);

            home = new TransactionMonitorPostpaidCreditHome(home, new PostpaidRetryRecurRechargeVisitor());
            home = new TransactionMonitorPrepaidCreditHome(home, new PrepaidRetryRecurRechargeVisitor());
            home = new PMHome(context, TransactionHome.class.getName() + ".factorybefore.06.AfterRetryRecurRecharge", home);

            // redirect prepaid to postpaid support
            home = new PostpaidSupportMsisdnTransHome(home);

            home = new BalanceTransferTransactionHome(home);

            home = new TransactionSetResponsibleBANHome(home);

            home = new TransactionSetSubTypeHome(home);

            home = new TransactionSAPFieldsSettingHome(home); 
                        
            home = new GLCodeObtainHome(home);

            home = new AdjustmentLimitTransactionHome(home);

            home = new LookupSpidTransactionHome(home);

            home = new DropZeroAmountTransactionHome(home);
            home = new PaymentToWrittenOffAccountTransactionHome(home);

           
            
            final CompoundValidator validators = new CompoundValidator();
            validators.add(new AcctNumMsisdnValidator(context));
            validators.add(TransactionDatesValidator.instance());
            home = new ValidatingHome(validators, home);


            // For unit test purposes, if you see this in svn please delete it
            // home = new FakeFailedTransactionHome(home);
            // Payment related decorators

            // Checks if this payment may go to the wrong subscriber
            home = new TransactionInMultipleSubscribersHome(home);
            home = new PMHome(context, TransactionHome.class.getName() + ".factorybefore.07.AfterMultipleSubscribers", home);


            home = new SubscriberIdentifierSettingHome(home);


            // if something happens to the payment this decorator will report an ER
            home = new PaymentFailureERHome(home);
            home = new PMHome(context, TransactionHome.class.getName() + ".factorybefore.08.AfterPaymentFailure", home);

            home = new PostWrittenOffSubPaymentTransactionHome(home);
            // Larry: do not move this home after validating home, otherwise,
            // account level payment doesn't work
            home = new TransactionRedirectionHome(home);

            //This home needs to go before transactionRedirectionHome so that all sub transaction will get same unified receipt id
            home = new TransactionUnifiedReceiptSettingHome(home);
            
            home = new UpdatingDirectDebitRecordForPaymentHome(home); 
            /*
             * [2007-01-17] Cindy Wong: This validator must happen before
             * TransactionRedirectionHome in the pipeline to prevent NPE.
             */
            home = new TransactionNonApplicableSubscrberHome(context, home);

            home = new TransactionOwnerTypeSettingHome(context, home);
/*
            home = new ValidatingHome(TransactionAccountValidator.instance(), home);
            home = new ValidatingHome(TransactionAdjustmentTypeLimitValidator.instance(), home);
            home = new ValidatingHome(TransactionUserGroupAdjustmentLimitValidator.instance(), home);
*/
			/*
			 * [2010-09-09] Cindy Wong TT#10030232032: Permission should be
			 * validated before limit.
			 */
			home =
			    new ValidatingHome(
			        TransactionAdjustmentTypePermissionValidator.instance(),
			        home);

            home = new ValidatingHome(TransactionAdjustmentTypeValidator.instance(), home);

            home = new SpidAwareHome(context, home);

            home = new NoSelectAllHome(home);

            home = new UserAgentHome(context, home);

            home = new SortingHome(home, new TransactionDateComparator(true));
            home = new PMHome(context, TransactionHome.class.getName() + ".factorybefore.09.TotalTime", home);

            context.put(TransactionHome.class, home);

            /*
             * [2008-04-04] Cindy Wong: the full home is put in context so that payment
             * plan loan reversal can be properly created when an account on payment plan
             * is dunned when making a payment.
             */
           context.put(Common.FULL_TRANSACTION_HOME, home);

            // [jhughes] remote for Selfcare
            if (DeploymentTypeSupportHelper.get(context).isBas(context) || DeploymentTypeSupportHelper.get(context).isSingle(context))
            {
                home = new ReadOnlyHome(home);
                home = new PMHome(serverContext, TransactionHome.class.getName() + ".rmiserver", home);

                new RMIHomeServer(serverContext, home, TransactionHome.class.getName()).register();
            }
        }
        catch (final RemoteException exception)
        {
            throw new AgentException("Failed to create a remote home for Transaction.", exception);
        }
        catch (final Exception e)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(TransactionHomePipelineFactory.class, e.getMessage(), e).log(context);
            }
            throw new AgentException(e);
        }

        return home;
    }

} // class