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
package com.trilogy.app.crm.subscriber.provision;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.pipe.SubscriberCreditLimitUpdateAgent;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.QuotaTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.core.TDMAPackage;
import com.trilogy.app.crm.client.AbmResultCode;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.ClientException;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.config.ProductAbmClientConfig;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.home.OcgForwardTransactionHome;
import com.trilogy.app.crm.home.validator.AbstractValidatorHome;
import com.trilogy.app.crm.home.validator.ExternalProvisioningException;
import com.trilogy.app.crm.home.validator.HomeValidator;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.subscriber.filter.SubscriberUseExpiryPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.service.ReactivateSubscriberRequestServicer;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionState;
import com.trilogy.product.s2100.ErrorCode;

/**
 * <p>
 * Allowed to add/remove profile at ups at any time, in response to subscriber state
 * change, but whenever we provision files to ups, we need to provision every parameters,
 * so another updates, will need to detect needsAddProfile() first.
 * </p>
 * <p>
 * Larry: all the logic should be move to service, such home should be removed from
 * subscriber provision pipeline.
 * </p>
 *
 * @author joe.chen@redknee.com
 */
public class SubscriberProvisionBMHome extends AbstractValidatorHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * If a subscriber is in any of these states, expiry is ignored.
     */
    public static final Collection<SubscriberStateEnum> NO_EXPIRY_STATES =
            Arrays.asList(
                    SubscriberStateEnum.AVAILABLE,
                    SubscriberStateEnum.INACTIVE);

    /**
     * If a subscriber is entering one of these states, the subscriber is unprovisioned from BM.
     */
    protected static final Collection<SubscriberStateEnum> ABM_UNPROVISION_STATES =
            Arrays.asList(
                    SubscriberStateEnum.PENDING,
                    SubscriberStateEnum.INACTIVE);

    protected static final Collection<SubscriberStateEnum> CLOSED_STATES =
            Arrays.asList(
                    SubscriberStateEnum.INACTIVE);

    protected static final Collection<SubscriberStateEnum> DORMANT_STATES =
            Arrays.asList(
                    SubscriberStateEnum.DORMANT);

    protected static final Collection<SubscriberStateEnum> ACTIVATED_STATES =
            Arrays.asList(
                    SubscriberStateEnum.ACTIVE);

    /**
     * Create a new instance of <code>SubscriberProvisionBMHome</code>.
     *
     * @param ctx      The operating context.
     * @param delegate The delegate of this home.
     */
    public SubscriberProvisionBMHome(final Context ctx, final HomeValidator delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber newSub = (Subscriber) obj;

        if (newSub != null && newSub.getUpdateReason() == UpdateReasonEnum.CONVERSION)
        {
            onConversion(ctx, null, newSub);
        }

        /*
         * Upon Subscriber conversion from Postpaid to Prepaid subscriber type, the newly
         * Postpaid subscriber is Deactivated and a new Prepaid subscriber is created.
         * During the creation of this prepaid subscriber the BM credit limit must be
         * updated to match the subscriber's new credit limit.
         */

        /*
         * Since the TT5100525051 fix, during post->pre conversion, ups file will be
         * removed. not necessary to change creditlimt.
         */

        /*
         * Pre-creating the subscriber in CRM has been moved outside from the External
         * Application Provisioning pipeline. It will be installed in a new decorator home
         * to the Home Validator installation. See the class
         * com.redknee.app.crm.subscriber.provision.SubscriberPreCreateHome.
         */

        onSubscriberStateChange(ctx, null, newSub);

        // we will safely redirect to store in later
        final Subscriber resultSub = (Subscriber) super.create(ctx, newSub);

        /*
         * The credit limit of the subscriber could be modified further down the
         * subscriber pipeline (i.e. by SubscriberPaymentPlanCreditLimitUpdateHome) and
         * will need to update the BM with the updated value.
         */
        if (hasCreditLimitUpdated(ctx, newSub, resultSub))
        {
            try
            {
                updateBMCreditLimit(ctx, newSub.getCreditLimit(ctx), resultSub.getCreditLimit(ctx), newSub);
            }
            catch (final HomeException exp)
            {
                final int result = -1;
                SubscriberProvisionResultCode.setProvisionCreditLimitResultCode(ctx, result);
                SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, result);
                SubscriberProvisionResultCode.addException(ctx, "Failed to update subcription credit limit.", exp, newSub,
                        resultSub);
            }
        }

        return resultSub;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) obj;
        super.remove(ctx, obj);
        // don't touch hybrid prepaid sub
        if (sub.isPostpaid() || sub.isPrepaid())
        {
            Subscriber existingSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, sub.getMSISDN(), sub
                    .getSubscriptionType(), new Date());
            if ((null != existingSubscriber) && (sub.getId().equals(existingSubscriber.getId())))
            {
                try
                {
                    if (hasProfile(ctx, sub))
                    {
                        new InfoLogMsg(this, "BM Profile exists for Subscription [ " + sub.getId()
                                + "]. It should get deleted on Subscriber deactivation. Deleting it from BM now.", null)
                                .log(ctx);
                        final SubscriberProfileProvisionClient client;
                        client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
                        client.deleteSubscriptionProfile(ctx, sub);
                    }
                }
                catch (final Exception e)
                {
                    logBMException(ctx, null, sub, e);
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "No need to finalize BM profile for Subscriber [" + sub.getId()
                            + "] as the profile for the Subscription's MSISDN [" + sub.getMSISDN()
                            + "] either does not exist or is in use for another Subscriber", null).log(ctx);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Object ret = null;
	
	        try
        {
            if (needsRefreshBalanceRemainingFromOcg(ctx, oldSub, newSub))
            {
                setSubscriberBalanceRemainingFromOcg(ctx, newSub);
            }
            if (needsUpdateQuotaLimit(newSub, oldSub))
            {
                onUpdateQuotaLimit(ctx, oldSub, newSub);
            }
            if (needsUpdateMonthlySpendLimit(newSub, oldSub))
            {
                onUpdateMonthlySpendLimit(ctx, oldSub,newSub);
            }
            if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
            {
                onChangeMsisdn(ctx, oldSub, newSub);
            }
            if (!SafetyUtil.safeEquals(oldSub.getIMSI(), newSub.getIMSI()))
            {
            	onChangeImsi(ctx, oldSub, newSub);
            }
            if (!SafetyUtil.safeEquals(oldSub.getPackageId(), newSub.getPackageId()))
            {
            	final String pkgIMSI = retrieveIMSIbyPackageId(ctx, newSub);
            	newSub.setIMSI(pkgIMSI);
            	onChangeImsi(ctx, oldSub, newSub);
            }
            onSubscriberStateChange(ctx, oldSub, newSub);
            if (needsPricePlanCreditLimitChange(ctx, oldSub, newSub))
            {
                changeCreditLimit(ctx, oldSub, newSub);
            }
            if (needsPricePlanChange(ctx, oldSub, newSub))
            {
                updateBMPricePlan(ctx, newSub);
            }
            if (needsNotificationTypeUpdate(ctx, oldSub, newSub))
            {
                updateNotificationType(ctx, newSub);
            }
            if (!SafetyUtil.safeEquals(oldSub.getBillingLanguage(), newSub.getBillingLanguage()))
            {
                updateBMBillingLanguage(ctx, newSub);
            }
            if (!SafetyUtil.safeEquals(oldSub.getAtuBalanceThreshold(), newSub.getAtuBalanceThreshold()))
            {
            	if(oldSub.getSubscriptionType(ctx).getType() != SubscriptionTypeEnum.BROADBAND_INDEX)
                {
            		updateBalanceThresholdAmount(ctx, newSub);
                }
            }
            if (!SafetyUtil.safeEquals(oldSub.getGroupScreeningTemplateId(), newSub.getGroupScreeningTemplateId()))
            {
                    updateGroupScreeningTemplateId(ctx, newSub);
            }
        }
        catch (final ProvisioningHomeException exception)
        {
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, exception.getResultCode());

            /*
             * Cindy Wong: TT 6121242579 Add this exception to the parent context to make
             * sure the transaction fails.
             */
            final Context parent = (Context) ctx.get("..");
            if (parent != null)
            {
                parent.put(Exception.class, exception);
            }
            throw exception;
        }
        catch (final Exception exp)
        {
            logBMException(ctx, newSub, oldSub, exp);
        }

        ret = super.store(ctx, newSub);
        
        onSubscriberStateChangePostStore(ctx, oldSub, newSub);
        if (needsRemoveProfile(ctx, oldSub, newSub))
        {
            /*
             * Make the BM profile removing as the last action Many actions in the
             * pipeline may need the profile. It is not in scope to roll-back the intent
             * to close If it has reached this point, close the state whatever happens
             */
            
            final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            if(null==client)
            {
                throw new HomeException("final remaining balance write-off  for the Subscription [" + newSub.getId()+ "] failed due to unavailability of Provisioning Service");
            }
            Parameters subscriptionParameters = null;
            try
            {
                subscriptionParameters = client.removeSubscriptionProfile(ctx, oldSub);
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                logBMException(ctx, newSub, oldSub, exception);
                throw new HomeException(
                        "Unable to delete Subscriber from BM , Return Code:" + exception.getErrorCode(), exception);
            }
            if (subscriptionParameters != null)
            {
                if (oldSub.isPrepaid() && SystemSupport.supportsAllowWriteOffForPrepaidSubscription(ctx) )
                {
                    if (oldSub.isPooledGroupLeader(ctx))
                    {
                        /*
                         * if the subscription is a pooled leader, we should not do a
                         * write-off. Transactions must be recorded against a visible
                         * subscription (MSISDN). We just record the balance and
                         * group-usage in account notes when a member is being closed.
                         */
                        NoteSupportHelper.get(ctx).addAccountNote(ctx, oldSub.getAccount(ctx).getBAN(),
                                "Subsription Pool Removed: Pool-ID|Group-MSISDN [" + oldSub.getMSISDN()
                                        + ", [Subscription-Type=" + subscriptionParameters.getSubscriptionType()
                                        + "], [Pool-Balance=" + subscriptionParameters.getBalance() + "]"
                                        + ", [Pool-Lmit=" + subscriptionParameters.getCreditLimit()
                                        + "], [Pool-Usage=" + subscriptionParameters.getGroupUsage() + " ]",
                                SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.ACCACTIVE);
                    }
                    else
                    {
                        /*
                         * Balance should not be written off when subscriber is
                         * moving from available or pending to deactivated.
                         */
                        final long balance = subscriptionParameters.getBalance();
                        if (balance > 0 && !SafetyUtil.safeEquals(oldSub.getState(), SubscriberStateEnum.AVAILABLE)
                                && !SafetyUtil.safeEquals(oldSub.getState(), SubscriberStateEnum.PENDING))
                        {
                            Context subCtx = ctx.createSubContext();
                            subCtx.put(OcgForwardTransactionHome.IS_IGNORE_OCG_FW, true);
                            final AdjustmentTypeEnum adjustmentTypeEnum = SubscriberSupport
                                    .getWriteoffSubscriptionBalanceAdjustment(subCtx, oldSub);
                            final AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(subCtx).getAdjustmentType(subCtx,
                                    adjustmentTypeEnum);
                            if (adjustmentType != null)
                            {
                                TransactionSupport.createTransaction(subCtx, oldSub, balance, 0, adjustmentType, false,
                                        new Date(), "");
                            }
                            else
                            {
                                final String errorMessage = "Can not write-off remaining balance on the Subscription ["
                                    + oldSub.getId() + "]. Required adjustment type not available [ ("
                                    + adjustmentTypeEnum.getDescription() + ") ( " + adjustmentTypeEnum.getIndex()
                                    + ") ]" ;
                                new MajorLogMsg(this,errorMessage , null).log(subCtx);
                                FrameworkSupportHelper.get(subCtx).notifyExceptionListener(subCtx, new IllegalStateException(errorMessage));
                            }
                        }
                    }
                }
            }
            else
            {
                // there is no use in failing the action
                // un-provisioning and profile removal has already been done
                new MajorLogMsg(this, "BM did not return the final subscription profile on remove action", null)
                        .log(ctx);
            }
        }
        return ret;
    }

    /**
     * @param ctx
     * @param newSub
     */
    private void updateGroupScreeningTemplateId(Context ctx, Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateGroupScreeningTemplateId(ctx, newSub, newSub.getGroupScreeningTemplateId());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "Failed to update subscription [" + newSub.getId()
                    + "] GroupScreeningTemplateId on BM due to BM error [" + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }

    private void updateBMBillingLanguage(Context ctx, Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateBillingLanguage(ctx, newSub, newSub.getBillingLanguage());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "provisioning result 3008: failed to update subscription [" + newSub.getId()
                    + "] billing language on BM due to BM error [" + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }
    
    private void updateBalanceThresholdAmount(Context ctx, Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateBalanceThresholdAmount(ctx, newSub, newSub.getAtuBalanceThreshold());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "Failed to update subscription [" + newSub.getId()
                    + "] balanceThreshold on BM due to BM error [" + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }
    
    /**
     * Operations needed on BM when IMSI  is updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if the there are problems updating BM.
     */
    public void onChangeImsi(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        if (!needsAddProfile(ctx, oldSub, newSub) && !needsRemoveProfile(ctx, oldSub, newSub))
        {
        	final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            try
            {
                client.updateIMSI(ctx, newSub, oldSub.getMSISDN());
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                final short result = exception.getErrorCode();
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                final String err = "provisioning result 3008: failed to provision BM due to BM error ["
                        + result + "]";
                throw new ProvisioningHomeException(err, result, Common.OM_UPS_ERROR, exception);
            }
            catch (final HomeException exception)
            {
                final short result = -1;
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                final String err = "provisioning result 3008: failed to provision BM due error.";
                throw new ProvisioningHomeException(err, result, Common.OM_UPS_ERROR, exception);
            }
        }
    }

    
    /**
     * Determines whether quota limit needs to be updated.
     *
     * @param newSub New subscriber.
     * @param oldSub Old subscriber.
     * @return Whether quota limit needs to be updated.
     */
    private boolean needsUpdateQuotaLimit(final Subscriber newSub, final Subscriber oldSub)
    {
        boolean needs = newSub != null;
        if (oldSub != null && newSub != null)
        {
            needs &= oldSub.getQuotaLimit() != newSub.getQuotaLimit();
        }
        return needs;
    }

    
    /**
     * Determines whether monthly spend limit needs to be updated.
     *
     * @param newSub New subscriber.
     * @param oldSub Old subscriber.
     * @return Whether quota limit needs to be updated.
     */
    private boolean needsUpdateMonthlySpendLimit(final Subscriber newSub, final Subscriber oldSub)
    {
        boolean needs = newSub != null;
        if (oldSub != null && newSub != null)
        {
            needs &= oldSub.getMonthlySpendLimit() != newSub.getMonthlySpendLimit();
        }
        return needs;
    }

    
    /**
     * Logs the exception, if applicable, before throwing it.
     *
     * @param ctx    The operating context.
     * @param newSub New subscriber.
     * @param oldSub Old subscriber.
     * @param exp    Exception being thrown.
     * @throws HomeException Exception being thrown.
     */
    private void logBMException(final Context ctx, final Subscriber newSub, final Subscriber oldSub,
            final Exception exp) throws HomeException
    {
        // If its insufficient balance on BM, we want to stop the update
        if (SubscriberProvisionResultCode.getProvisionUpsErrorCode(ctx) == AbmResultCode.NOT_ENOUGH_BAL)
        {
            throw (HomeException) exp;
        }

        // If its insufficient balance on BM, we want to stop the update
        if (SubscriberProvisionResultCode.getProvisionUpsErrorCode(ctx) == AbmResultCode.MAX_AMT_EXCEEDED)
        {
            throw (HomeException) exp;
        }

        final String module = Common.OM_UPS_ERROR;
        new OMLogMsg(Common.OM_MODULE, module).log(ctx);

        if (exp instanceof ProvisioningHomeException)
        {
            final ProvisioningHomeException phe = (ProvisioningHomeException) exp;
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, phe.getResultCode());
        }
        SubscriberProvisionResultCode.addException(ctx, exp.getMessage(), exp, oldSub, newSub);
    }

    /**
     * Operations needed on BM when quota limit is updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if there are problems updating the quota limit on BM.
     */
    public void onUpdateQuotaLimit(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateSubscriptionQuotaLimit(ctx, newSub, newSub.getQuotaLimit());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "provisioning result 3008: failed to provision BM due to BM error ["
                    + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }

    
    /**
     * Operations needed on BM when quota limit is updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if there are problems updating the quota limit on BM.
     */
    public void onUpdateMonthlySpendLimit(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateSubscriptionMonthlySpendLimit(ctx, newSub, newSub.getMonthlySpendLimit());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "provisioning result 3008: failed to provision BM due to BM error ["
                    + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }
    
    /**
     * Operations needed on BM when MSISDN is updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if the there are problems updating BM.
     */
    public void onChangeMsisdn(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        if (!needsAddProfile(ctx, oldSub, newSub) && !needsRemoveProfile(ctx, oldSub, newSub))
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            try
            {
                client.updateMobileNumber(ctx, newSub, oldSub.getMSISDN());
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                final short result = exception.getErrorCode();
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                final String err = "provisioning result 3008: failed to provision BM due to BM error ["
                        + result + "]";
                throw new ProvisioningHomeException(err, result, Common.OM_UPS_ERROR, exception);
            }
            catch (final HomeException exception)
            {
                final short result = -1;
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                final String err = "provisioning result 3008: failed to provision BM due error.";
                throw new ProvisioningHomeException(err, result, Common.OM_UPS_ERROR, exception);
            }
        }
    }

    /**
     * Operations needed on BM when subscriber type is updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if there are problems updating BM.
     */
    public void onConversion(final Context ctx, final Subscriber oldSub, final Subscriber newSub) throws HomeException
    {

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Delete old subscriber profile from BM for converted subscriber "
                    + (newSub != null ? newSub.getId() : ""), null).log(ctx);
        }

        final SubscriberProfileProvisionClient client;
        client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);

        try
        {
            client.deleteSubscriptionProfile(ctx, newSub);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short result = exception.getErrorCode();

            if (result != AbmResultCode.RECORD_NOT_FOUND)
            {
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, result);
                throw new HomeException("Fail to delete subscriber's BM profile, msisdn=" + newSub.getMSISDN()
                        + ", BM result [" + result + "]", exception);
            }
        }
    }

    /**
     * Operations needed on BM when subscriber state is updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if there are problems updating BM.
     */
    public void onSubscriberStateChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        int result = AbmResultCode.SUCCESS;
        String action = "";

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "onSubscriberStateChange oldSub " + oldSub + ",newSub=" + newSub, null).log(ctx);
        }

        if ( needsAddProfile(ctx, oldSub, newSub))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "needs to Add Profile ", null).log(ctx);
            }

            // We don't need to check if BM has profile, if this balance is held externally
            // Since urcs will check first on external system and create on URCS if it is required
            // That's why it should avoid hasProfile check.  If PrepaidPeningState is not selected, 
            // then we are assuming the balance is held externally
            if ((!SystemSupport.supportsPrepaidPendingState(ctx) && newSub != null && newSub.isPrepaid()) ||
                    (!hasProfile(ctx, newSub)))
            {
                // if this is a re-activation, then set sub's initial balance to zero
                if (newSub.isPooledMemberSubscriber(ctx))
                {
                    newSub.setCreditLimit(0);
                    if (newSub.isPrepaid() && AccountSupport.getAccount(ctx, newSub.getPoolID(ctx)).isHybrid())
                    {
                        // prepaid type should never be able to use postpaid type pool
                        newSub.setQuotaType(QuotaTypeEnum.LIMITED_QUOTA);
                        newSub.setQuotaLimit(0);
                    }
                    else
                    {
                        newSub.setInitialBalance(0);
                    }
                }

                try
                {
                    final SubscriberProfileProvisionClient client = BalanceManagementSupport
                            .getSubscriberProfileProvisionClient(ctx);
                    if (null == client)
                    {
                        throw new HomeException("Unable to create Profile for Subscriber [" + newSub.getId()
                                + "] because Subscriber-Profile-Provision-Client serive is not available");
                    }
                    Account account = (Account) ctx.get(Account.class);
                    if (account==null || account.getBAN().equals(newSub.getBAN()))
                    {
                        account = newSub.getAccount(ctx);
                    }
                    // it is possible that account creation may have also failed at BM
                    // we do not create pending state accounts
                    // to ensure that an account exists before a subscription is created 
                    // query the account profile, if it does not exist, create it first
                    final Parameters profile = client.querySubscriberAccountProfile(ctx, account);
                    if (profile == null)
                    {
                        client.addSubscriberAccountProfile(ctx, account);
                    }
                    
                    if (newSub.isPrepaid())
                    {
                        newSub.setMonthlySpendLimit(-1);
                    }                    
                    client.addSubscriptionProfile(ctx, newSub);
                }
                catch (final SubscriberProfileProvisionException exception)
                {
    
                    if (!SystemSupport.supportsPrepaidPendingState(ctx))
                    {
                        // We want to throw exception so that it doesn't create subscriber on when it fails to Create on URCS

                        // error code already captured.
                        ERLogger.logActivationER(ctx, newSub);
                        throw new ProvisioningHomeException("OCG error " + exception.getMessage(), -1,
                                Common.OM_UPS_ERROR, exception);
                    }
                    else
                    {
                        throw new HomeException("Unable to create Profile for Subscriber [" + newSub.getId()
                                + "] on Balance Manager [Result Code(" + exception.getErrorCode() + ")]", exception);
                    }
                }

                try
                {
                    // Create a new AdjustmentType that is type of Credit Balance
                    AdjustmentType creditBalanceAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                            AdjustmentTypeEnum.InitialBalance);
                    
                    if(newSub.isInitialBalanceByCreditCard())
                    {
                        CRMSpid spid = SpidSupport.getCRMSpid(ctx, newSub.getSpid());
                        if(spid != null)
                        {
                            creditBalanceAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, spid.getCreditCardPaymentAdjustmentID());  
                        }
                    }
                    if(creditBalanceAdjustmentType != null)
                    {
                        createTransactionRecord(ctx, newSub, creditBalanceAdjustmentType, newSub.getInitialBalance(),
                                newSub.getInitialBalance());
                    }
                }
                catch (final HomeException e)
                {
                    /*
                     * It shouldn't stop the reactivation, if the transaction cannot be
                     * recorded.
                     */
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "The Reactivation Fee Adjustment Type cannot be found.", null).log(ctx);
                    }
                }
            }
            /* TODO we should add all other parameters, or implemented in addSubscriber */
        }

        if (result == AbmResultCode.SUCCESS && needsSuspensionForMoneyWallet(ctx, oldSub, newSub))
        {
            suspendSubscriptionBalance(ctx, newSub);
        }
        else if (result == AbmResultCode.SUCCESS && 
                (needsReactivationForMoneyWallet(ctx, oldSub, newSub) || needsReactivationFixedStopPricePlan(ctx, oldSub, newSub)))
        {
            reactivateSubscriptionBalance(ctx, newSub);
        }
        
        if (result == AbmResultCode.SUCCESS && needsActivateForPrepaid(ctx, oldSub, newSub))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Activating the subscriber with MSISDN " + newSub.getMSISDN(), null).log(ctx);
            }
            result = activateForPrepaid(ctx, newSub);
            action = "activate subscriber";
        }

        if (result == AbmResultCode.SUCCESS)
        {

            if (!SubscriberUseExpiryPredicate.instance().f(ctx, newSub))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Need to ignore the expiry date", null).log(ctx);
                }

                newSub.setExpiryDate(new Date(0));
                SubscriberSupport.updateExpiryOnCrmAbmBM(ctx, newSub);
            }
            else if (needsUpdateOcgExpiryDateForPrepaid(ctx, oldSub, newSub))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Needs to update the expiry date", null).log(ctx);
                }

                updateOcgExpiryDateForPrepaid(ctx, newSub);
            }
            else
            {
                setExpiryDate(ctx, oldSub, newSub);
            }

            /* charge anyway, if we don't charge, how can csr remember to charge? */
            /* Larry: why and how to charge if subscriber already deleted from BM? */
            if (!needsRemoveProfile(ctx, oldSub, newSub)
                    && needsDebitReactivationFeeInBMForSubscriber(ctx, oldSub, newSub))
            {
                action = "debit reactivaton fees to OCG";
                result = debitReactivationFeeInBMForSubscriber(ctx, newSub);
            }
        }

        logBMError(ctx, oldSub, newSub, result, action);
    }

    protected void onSubscriberStateChangePostStore(Context ctx, Subscriber oldSub, Subscriber newSub)
    throws HomeException
    {
        if (needsSuspensionForFixedStopPricePlan(ctx, oldSub, newSub))
        {
            suspendSubscriptionBalance(ctx, newSub);
        }
    }

    private void reactivateSubscriptionBalance(final Context ctx, final Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateState(ctx, newSub, SubscriptionState.ACTIVE);
        }
        catch (SubscriberProfileProvisionException e)
        {
            final String message = "Could not activate balance on BM for Subscriber:" + newSub.getId()
                    + " BM result code" + e.getErrorCode();
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, message, e).log(ctx);
            }
            throw new ProvisioningHomeException(message, e);
        }
    }

    private void suspendSubscriptionBalance(final Context ctx, final Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateState(ctx, newSub, SubscriptionState.SUSPENDED);
        }
        catch (SubscriberProfileProvisionException e)
        {
            final String message = "Could not suspend balance for Subscriber: " + newSub.getId()
                    + " BM result code: " + e.getErrorCode();
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, message, e).log(ctx);
            }
            throw new ProvisioningHomeException(message, e);
        }
    }

    /**
     * Logs BM error.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @param result BM result code.
     * @throws HomeException Thrown with the appropriate error message if the BM operation was not successful.
     */
    protected void logBMError(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub, final int result, final String action) throws HomeException
    {
        if (result != AbmResultCode.SUCCESS)
        {
            // error logs
            HomeException he = null;
            if (result == AbmResultCode.NOT_ENOUGH_BAL)
            {
                SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
                SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, result);
                he = new HomeException("Insufficient balance.  Failed to " + action);
                throw he;
            }
            final int lastResult = 3008;
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, result);
            SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, lastResult);
            final Subscriber sub;
            if (newSub == null)
            {
                sub = oldSub;
            }
            else
            {
                sub = newSub;
            }
            he = new HomeException("provisioning result 3008: failed to " + action + " due to BM error ("
                    + AbmResultCode.toString((short) result) + ":" + result + ") for subscription: "
                    + sub.getId() + " msisdn: " + sub.getMSISDN());
            // generate subscriber activate failed - out of sync alarm
            ERLogger.logOutOfSync10339(ctx, sub, null, this, 3008);

            SubscriberProvisionResultCode.addException(ctx, "", he, oldSub, newSub);

            // we don't throw exception, so we need manually OM log
            new OMLogMsg(Common.OM_MODULE, Common.OM_UPS_ERROR).log(ctx);
        }
    }

    /**
     * Sets the subscriber expiry date on CRM to be the same as the one on BM.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     */
    public void setExpiryDate(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        final SubscriberProfileProvisionClient client;
        client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        try
        {
            final Parameters profile2 = client.querySubscriptionProfile(ctx, newSub);
            if (profile2 != null)
            {
                newSub.setExpiryDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(profile2.getExpiryDate(), newSub.getTimeZone(ctx)));
            }
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(this, "Failed to query BM for subscription " + newSub.getId(), exception).log(ctx);
        }
        catch (SubscriberProfileProvisionException exception)
        {
            new MinorLogMsg(this, "Failed to query BM for subscription " + newSub.getId(), exception).log(ctx);
        }
    }

    /**
     * Determines whether the subscriber profile needs to be added to BM.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Whether subscriber profile needs to be added to BM.
     */
    public boolean needsAddProfile(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        /*
         * We are request to follow Buzzard prepaid mode. so add profile for prepaid for
         * non pending state Although Ibis does not require to do so.
         */
        boolean needs = false;
        if (oldSub == null)
        {
            if (!EnumStateSupportHelper.get(ctx).isOneOfStates(newSub, ABM_UNPROVISION_STATES))
            {
                needs = true;
            }
        }
        else if (newSub != null && !EnumStateSupportHelper.get(ctx).isOneOfStates(newSub, ABM_UNPROVISION_STATES)
                && EnumStateSupportHelper.get(ctx).isOneOfStates(oldSub, ABM_UNPROVISION_STATES))
        {
            needs = true;
        }
        return needs;
    }

    /**
     * Determines whether the subscriber exists on BM.
     *
     * @param ctx The operating context.
     * @param sub Subscriber being examined.
     * @return Returns <code>true</code> if the subscriber profile already exists on
     *         BM, <code>false</code> otherwise.
     */
    protected boolean hasProfile(final Context ctx, final Subscriber sub)
    {
        boolean result = false;
        if (sub == null)
        {
            return false;
        }

        final SubscriberProfileProvisionClient client;
        client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);

        try
        {
            final Parameters results = client.querySubscriptionProfile(ctx, sub);
            //Checks the URCS subscriber Id with subscription's BAN
            result = results != null && (results.getSubscriberID().equals(sub.getBAN()));
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, exception.getMessage(), exception).log(ctx);
            }
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, exception.getMessage(), exception).log(ctx);
            }
        }

        return result;
    }

    /**
     * Determines whether the subscriber profile should be removed from BM.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns <code>true</code> if the subscriber profile should be removed
     *         from BM, <code>false</code> otherwise.
     */
    public boolean needsRemoveProfile(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (newSub == null)
        {
            needs = true;
        }
        else if (oldSub == null)
        {
            needs = false;
        }
        else
        {
            needs = EnumStateSupportHelper.get(ctx).isOneOfStates(newSub, ABM_UNPROVISION_STATES)
                    && !EnumStateSupportHelper.get(ctx).isOneOfStates(oldSub, ABM_UNPROVISION_STATES);
        }
        return needs;
    }

    /**
     * Determines whether the subscriber's credit limit has changed.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns <code>true</code> if the subscriber's credit limit has changed,
     *         <code>false</code> otherwise.
     */
    boolean needsPricePlanCreditLimitChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub != null && newSub != null && !needsAddProfile(ctx, oldSub, newSub)
                && !needsRemoveProfile(ctx, oldSub, newSub))
        {
            /*
             * Cindy: this method used to call Subscriber.getPricePlan(Context) to check
             * the credit limit. However, getPricePlan(Context) actually calls
             * getRawPricePlanVersion(Context) to get the actual price plan, and then override
             * the credit limit in the price plan with the value retrieved from
             * Subscriber.getCreditLimit(). In turn, Subscriber.getCreditLimit() actually
             * calls getRawPricePlanVersion() AGAIN to determine the credit limit -- which was
             * totally messed up. Armed with that knowledge, I'm changing the code to use
             * Subscriber.getCreditLimit().
             */
            needs = oldSub.getCreditLimit(ctx) != newSub.getCreditLimit(ctx);
        }
        return needs;
    }

    /**
     * Determines whether the subscriber's price plan has changed.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns <code>true</code> if the subscriber's credit limit has changed,
     *         <code>false</code> otherwise.
     */
    boolean needsPricePlanChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub != null && newSub != null && !needsAddProfile(ctx, oldSub, newSub)
                && !needsRemoveProfile(ctx, oldSub, newSub))
        {
            needs = oldSub.getPricePlan() != newSub.getPricePlan();
        }
        return needs;
    }

    /**
     * Determines whether credit limit has been updated.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns TRUE if the credit limit is different between the two accounts.
     *         Otherwise, returns false.
     */
    boolean hasCreditLimitUpdated(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        final boolean needs = false;
        if (oldSub != null && newSub != null)
        {
            return oldSub.getCreditLimit(ctx) != newSub.getCreditLimit(ctx);
        }
        return needs;
    }

    /**
     * Change credit limit for sub, it will not cause exception if BM error.
     * But exception will be thrown if credit limit exceed max.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @throws HomeException Thrown if updating BM failed.
     */
    void changeCreditLimit(final Context ctx, final Subscriber oldSub, final Subscriber newSub) throws HomeException
    {
        /*
         * Update profile in BM. We have to assume at this point that the update is
         * successful.
         */
        int result = AbmResultCode.SUCCESS;
        Exception exp = null;

        try
        {
            final long oldCreditLimit = oldSub.getCreditLimit(ctx);
            final long newCreditLimit = newSub.getCreditLimit(ctx);

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Modifying credit limit of subscriber " + newSub.getId() + " from "
                        + oldCreditLimit + " to " + newCreditLimit, null).log(ctx);
            }

            try
            {
                updateBMCreditLimit(ctx, newCreditLimit, oldCreditLimit, newSub);
            }
            catch (final HomeException exception)
            {
                result = -1;
                exp = exception;
            }
        }
        catch (final Exception e)
        {
            result = 3013;

            final IllegalStateException newException = new IllegalStateException("Result: " + result
                    + ".  Attempt to change credit limit of subscriber " + newSub.getId() + " has failed. Cause:" + e);

            newException.initCause(e);

            new MinorLogMsg(this, "", newException).log(ctx);

            exp = newException;
        }

        if (result != AbmResultCode.SUCCESS)
        {
            SubscriberProvisionResultCode.setProvisionCreditLimitResultCode(ctx, result);
            SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, result);
        }

        if (exp != null)
        {
            SubscriberProvisionResultCode.addException(ctx, "Failed to update subscription credit limit.", exp, oldSub, newSub);
        }
    }

    /**
     * Update credit limit on BM.
     *
     * @param ctx            The operating context.
     * @param newCreditLimit The new credit limit.
     * @param oldCreditLimit The old credit limit.
     * @param newSub         The subscriber being updated.
     * @return BM result code of the update credit limit on BM.
     * @throws AgentException Thrown if there are problems executing the credit limit update.
     */
    public int updateBMCreditLimit(final Context ctx, final long newCreditLimit, final long oldCreditLimit,
            final Subscriber newSub) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_CREDIT_LIMIT_CHANGE).log(ctx);
        
        final int result = AbmResultCode.SUCCESS;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final long oldDeposit;
        if (oldSub==null)
        {
            oldDeposit = 0;
        }
        else
        {
            oldDeposit = oldSub.getDeposit(ctx);
        }

        final ERLogMsg er = ERLogger.getCreditLimitResetER(ctx, newSub, oldDeposit, newSub.getDeposit(ctx),
                newCreditLimit, result);
        
        try
        {

            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.adjustCreditLimit(ctx, newSub, newCreditLimit, oldCreditLimit, "");
            
        }
        catch (final SubscriberProfileProvisionException provEx)
        {
            new MinorLogMsg(
                    this,
                    "Failed to communicate with BM to change credit limit for \""
                    + newSub.getMSISDN() 
                    + "\" to \"" 
                    + newCreditLimit
                    + "\".",
                    null).log(ctx);
            final short resultBM = provEx.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "provisioning result 3008: failed to update subscription [" + newSub.getId()
                    + "] credit limit on URCS due to URCSerror [" + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, provEx);
        }
        finally
        {
            er.setTimestamp(System.currentTimeMillis());
            final String[] fields = er.getFields();
            /*
             * As above, the result code is set to RESULT_CODE_UPS_FAILS if not zero. The
             * result is always the last entry.
             */
            fields[fields.length - 2] = String.valueOf(result);
            if (result == 0)
            {
                fields[fields.length - 1] = "0";
            }
            else
            {
                fields[fields.length - 1] = String.valueOf(3008);
            }
            er.log(ctx);
        }
        return result;
    }

    /**
     * Update price plan on BM.
     *
     * @param ctx            The operating context.
     * @param newSub         The subscriber being updated.
     * @return BM result code of the update credit limit on BM.
     * @throws HomeException Thrown if there are problems executing the price plan update.
     */
    public void updateBMPricePlan(final Context ctx, final Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updatePricePlan(ctx, newSub, newSub.getPricePlan());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "provisioning result 3008: failed to update subscription [" + newSub.getId()
                    + "] price plan on BM due to BM error [" + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }

    /**
     * <p>
     * This methods checks the new and old sub need the initial OCG expiry date extension.
     * If it needs it updates the expDateExt with the amount in initialExpDateExt There
     * are only four cases that we need update expiry date on BM.
     * </p>
     * <ul>
     * <li>available to active, with ABM pre 8_0_59</li>
     * <li>inactive to active</li>
     * <li>convert postpaid to prepaid.</li>
     * <li>move prepaid subscriber from pooled to non-pooled account</li>
     * </ul>
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns <code>true</code> if the OCG expiry date needs to be updated for
     *         prepaid subscriber, <code>false</code> otherwise.
     */
    protected boolean needsUpdateOcgExpiryDateForPrepaid(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub)
    {
        // TODO do we need to do same thing for Hybrid prepaid
        if (newSub == null || oldSub == null || (newSub.isPrepaid() && SystemSupport.supportsUnExpirablePrepaidSubscription(ctx)))
        {
            return false;
        }

        // no need to update expiry date if it is is first activation.
        if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.AVAILABLE,
                SubscriberStateEnum.ACTIVE)
                && !ctx.getBoolean(ReactivateSubscriberRequestServicer.REACTIVATION, false))
        {
            return false;
        }

        // conversion
        if (!oldSub.isPrepaid() && EnumStateSupportHelper.get(ctx).isNotOneOfStates(oldSub, NO_EXPIRY_STATES))
        {
            // initially we have to extend by the initial amount.
            newSub.setExpDateExt(newSub.getExpDateExt());
            return true;
        }

        if (oldSub.isPrepaid())
        {
            if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.AVAILABLE,
                    SubscriberStateEnum.ACTIVE))
            {
                // state change
                // initially we have to extend by the initial amount.
                newSub.setExpDateExt(newSub.getExpDateExt());
                return true;
            }

            try
            {
                final Account oldAccount = oldSub.getAccount(ctx);
                if (oldAccount != null && oldAccount.isPooled(ctx))
                {
                    final Account newAccount = newSub.getAccount(ctx);
                    if (newAccount != null && !newAccount.isPooled(ctx))
                    {
                        // Old account is pooled, new account is non-pooled
                        return true;
                    }
                }
            }
            catch (final HomeException he)
            {
                new MinorLogMsg(this, "Unable to get account type. Expiry will not be updated on BM.", he).log(ctx);
            }
        }

        return false;
    }

    /**
     * This methods checks if BM activation call is needed updates the expDateExt with
     * the amount in initialExpDateExt.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Whether prepaid subscriber should be activated.
     */
    boolean needsActivateForPrepaid(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        final ProductAbmClientConfig abmClientConfig =
                (ProductAbmClientConfig) ctx.get(ProductAbmClientConfig.class);

        boolean needs = false;
        final boolean isReactivation = ctx.getBoolean(ReactivateSubscriberRequestServicer.REACTIVATION, false);
        if (newSub.isPrepaid()&& !newSub.isFirstActivation())
       {
                needs = (oldSub == null || oldSub.getState().equals(SubscriberStateEnum.AVAILABLE))
                        && newSub.getState().equals(SubscriberStateEnum.ACTIVE) && (!isReactivation);

                        // On deactivation, if the "isRemovePrepaidSubscriberOnDeactivation" is false,
                        // then the BM profile will not be removed and it stays as active.
                        // When reactivating the subscriber, no need to activate BM profile in this case
                        // as the subscriber may be expired already
                        
        }

        return needs;
        
        
    }

    /**
         * This methods checks if subscription suspension is needed
         *
         * @param ctx    The operating context.
         * @param oldSub The old subscriber.
         * @param newSub The new subscriber.
         * @return Whether prepaid subscriber's money should be suspended.
         * @throws HomeException for data problems
         */
        boolean needsSuspensionForMoneyWallet(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        boolean needs = false;
        if (oldSub != null && newSub != null && newSub.isPrepaid())
        {
            SubscriptionType subscriptionType = newSub.getSubscriptionType(ctx);
            if (subscriptionType != null
                    && subscriptionType.isWallet())
            {
                needs = oldSub.getState().equals(SubscriberStateEnum.ACTIVE)
                        && newSub.getState().equals(SubscriberStateEnum.DORMANT);
            }
        }
        return needs;
    }

    boolean needsReactivationForMoneyWallet(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        boolean needs = false;
        if (oldSub != null && newSub != null && newSub.isPrepaid())            
        {
            SubscriptionType subscriptionType = newSub.getSubscriptionType(ctx);
            if (subscriptionType != null
                    && subscriptionType.isWallet())
            {
                needs = oldSub.getState().equals(SubscriberStateEnum.DORMANT)
                        && newSub.getState().equals(SubscriberStateEnum.ACTIVE);
            }
        }
        return needs;
    }

    /**
     * This methods checks if subscription suspension is needed
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Whether prepaid subscriber's money should be suspended.
     * @throws HomeException for data problems
     */
    boolean needsSuspensionForFixedStopPricePlan(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        boolean needs = false;
        if (oldSub != null && newSub != null && newSub.isPrepaid())
        {
            needs = ctx.getBoolean(FixedStopPricePlanSubExtension.FIXED_STOP_PRICEPLAN_SUB_SUSPEND, false);    
        }
        return needs;
    }

    boolean needsReactivationFixedStopPricePlan(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
        throws HomeException
    {
        boolean needs = false;
        if (oldSub != null && newSub != null && newSub.isPrepaid())            
        {
                needs = oldSub.getState().equals(SubscriberStateEnum.LOCKED)
                        && newSub.getState().equals(SubscriberStateEnum.ACTIVE);
        }
        return needs;
    }
    /**
     * Activate prepaid subscriber.
     *
     * @param ctx The operating context.
     * @param sub The subscriber being activated.
     * @return Returns OCG result code.
     */
    public int activateForPrepaid(final Context ctx, final Subscriber sub)
    {
        int result = ErrorCode.NO_ERROR;
        final LongHolder outputBalance = new LongHolder();
        final AppOcgClient client = (AppOcgClient) ctx.get(AppOcgClient.class);
        try
        {
            result = client.activateSubscriber(sub.getMSISDN(), sub.getSubscriberType(), sub.getCurrency(ctx),
                    false, "", outputBalance, sub.getSubscriptionType(ctx).getId());
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Error trying to get subscription type.", e).log(ctx);
            return ErrorCode.UNKNOWN_ERROR;
        }

        return result;
    }

    /**
     * Updates OCG expiry date for prepaid subscriber.
     *
     * @param ctx    The operating context.
     * @param newSub The subscriber whose OCG expiry date is being updated.
     * @throws HomeException Thrown if there are problems updating OCG.
     */
    void updateOcgExpiryDateForPrepaid(final Context ctx, final Subscriber newSub) throws HomeException
    {
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Calling  AppOcgClient.updateExpiryDateForSubscriber with Expiry extension "
                        + newSub.getExpDateExt(), null).log(ctx);
            }

            // Create a new string to store the new expiry date returned from BM
            final StringBuilder newExpDate = new StringBuilder();

            short extension = 0;

            extension = (short) newSub.getExpDateExt();

            SubscriptionType subscriptionType = newSub.getSubscriptionType(ctx);
            if (subscriptionType == null)
            {
                throw new HomeException("Subscription Type " + newSub.getSubscriptionType() + " not found for subscription " + newSub.getId());
            }
            
            AppOcgClient ocgClient = (AppOcgClient) ctx.get(AppOcgClient.class);
            ocgClient.updateExpiryDateForSubscriber(ctx, newSub, extension,
                    subscriptionType.getId(), newExpDate);

            // Set the sub's expiry date to the new expiry date we get back from BM
            if (newExpDate.length() > 0)
            {
                // Parse returning date into a Date
                final String dateString = newExpDate.toString();
                TimeZone t = TimeZone.getTimeZone(newSub.getTimeZone(ctx));
                final Calendar calendar = Calendar.getInstance(t);

                // 0-4: year
                // 4-6: month (have to subtract 1 because of months go from 0 to 11 in
                // calendar class
                // 6-8: day
                calendar.set(Integer.parseInt(dateString.substring(0, 4)),
                        Integer.parseInt(dateString.substring(4, 6)) - 1, Integer.parseInt(dateString.substring(6, 8)));

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "New Expiry date found setting it with a value of " + calendar.getTime(),
                            null).log(ctx);
                }

                newSub.setExpiryDate(calendar.getTime());
            }
        }
        catch (final ClientException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Client exception : " + e.getMessage(), e).log(ctx);
            }

            throw new ProvisioningHomeException("Failed to transition from Avaiable to Active state", -1,
                    Common.OM_OCG_ERROR, e);
        }
    }

    /**
     * Determines whether the reactivation fee needs to be debited in BM.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns <code>true</code> if the reactivation fee should be debited in
     *         BM, <code>false</code> otherwise.
     */
    protected boolean needsDebitReactivationFeeInBMForSubscriber(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub)
    {
        return oldSub != null
                && newSub != null
                && EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.AVAILABLE,
                SubscriberStateEnum.ACTIVE)
                && ctx.getBoolean(ReactivateSubscriberRequestServicer.REACTIVATION, false)
                && newSub.getReactivationFee() != 0;
    }

    /**
     * We use this function for two purposes, for checking prepaid precreated, and get
     * balance remaining.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Determines whether the balance remaining should be refreshed from OCG.
     */
    protected boolean needsRefreshBalanceRemainingFromOcg(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub)
    {
        boolean needs = false;
        needs = needsRefreshBalanceRemainingFromOcgForStateTransition(ctx, oldSub, newSub);

        // converting also needs
        if (!needs && oldSub != null && newSub != null)
        {
            if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
            {
                needs = true;
            }
        }
        return needs;
    }

    /**
     * Determines whether balance remaining needs to be refreshed from OCG for state
     * transition.
     *
     * @param ctx    The operating context.
     * @param oldSub The old subscriber.
     * @param newSub The new subscriber.
     * @return Returns <code>true</code> if the balance remaining should be refreshed
     *         from OCG, <code>false</code> otherwise.
     */
    protected boolean needsRefreshBalanceRemainingFromOcgForStateTransition(final Context ctx, final Subscriber oldSub,
            final Subscriber newSub)
    {
        boolean needs = false;
        if (newSub != null && newSub.isPrepaid())
        {
            if (!SystemSupport.supportsPrepaidPendingState(ctx))
            {
                /**
                 * TT5061519591, when a prepaid subscriber leaving pending state, we do
                 * not need to refresh balance remaining from OCG because no transaction
                 * happen during pending state, balance should be the same.
                 */
                final Collection<SubscriberStateEnum> states = Arrays.asList(SubscriberStateEnum.PENDING,
                        SubscriberStateEnum.INACTIVE);
                /*
                 * Note that these isOneOfStates() checks are not equivalent to any
                 * combination of isEnteringState()/isLeavingState() checks provided by
                 * EnumStateSupport. The possiblity of the old subscriber being null is
                 * what complicates it.
                 */
                if ((oldSub == null || EnumStateSupportHelper.get(ctx).isOneOfStates(oldSub, states))
                        && !EnumStateSupportHelper.get(ctx).isOneOfStates(newSub, states))
                {
                    needs = true;
                }
            }
        }
        return needs;
    }


    /**
     * Debit reactivation fee in BM for the given subscriber.
     *
     * @param ctx        The operating context.
     * @param subscriber The given subscriber.
     * @return Result code from OCG.
     */
    private int debitReactivationFeeInBMForSubscriber(final Context ctx, final Subscriber subscriber)
    {
        final AppOcgClient ocgClient = (AppOcgClient) ctx.get(AppOcgClient.class);

        final Account account = (Account) ctx.get(Lookup.ACCOUNT);

        final LongHolder balance = new LongHolder();

        final String erReference = "AppCrm-" + subscriber.getMSISDN();

        SubscriptionType subscriptionType = subscriber.getSubscriptionType(ctx);
        if (subscriptionType == null)
        {
            new MajorLogMsg(this, "Subscription type not found: " + subscriber.getSubscriptionType(), null).log(ctx);
            return AbmResultCode.UNKNOWN_ERROR;
        }

        final int result = ocgClient.requestDebit(subscriber.getMSISDN(), subscriber.getSubscriberType(),
                subscriber.getReactivationFee(), account.getCurrency(),
                // Don't allow balance to go below zero, TT 5072521732
                true, erReference, subscriptionType.getId(),
                // the subscriber's Balance after the transaction
                balance);

        try
        {
            if (result == ErrorCode.NO_ERROR)
            {
                // Create a new AdjustmentType that is type of Reactivation Fee
                final AdjustmentType reactivationFeeAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                        AdjustmentTypeEnum.ReactivationFee);

                createTransactionRecord(ctx, subscriber, reactivationFeeAdjustmentType, subscriber.getReactivationFee(),
                        balance.value);
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Receive the OCG error when applying the reactivation fees " + result,
                            null).log(ctx);
                }
            }
        }
        catch (final HomeException e)
        {
            // It shouldn't stop the reactivation, if the transaction cannot be recorded.
            new MinorLogMsg(this, "Exception when applying the reactivation transaction ", e).log(ctx);
        }

        return result;
    }

    /**
     * Set subscriber balance from OCG.
     *
     * @param ctx        The operating context.
     * @param subscriber The subscriber being updated.
     * @throws HomeException Thrown if there are problems updating OCG.
     */
    private void setSubscriberBalanceRemainingFromOcg(final Context ctx, final Subscriber subscriber)
        throws HomeException
    {
        final int errorCodeForBalQuery = 3009;

        final AppOcgClient client = (AppOcgClient) ctx.get(AppOcgClient.class);
        final Account account = (Account) ctx.get(Lookup.ACCOUNT);

        final LongHolder outputBalance = new LongHolder();
        final LongHolder outputOverdraftBalance = new LongHolder();
        final LongHolder outputOverdraftDate = new LongHolder();

        int rc = ErrorCode.NO_ERROR;
        HomeException exp = null;
        try
        {
            rc = client.requestBalance(subscriber.getMSISDN(), subscriber.getSubscriberType(), account.getCurrency(),
                    // sendExpiry
                    false,
                    // erReference
                    "", subscriber.getSubscriptionType(ctx).getId(), outputBalance,
                    outputOverdraftBalance, outputOverdraftDate);
        }
        catch (final Exception e)
        {
            exp = new ProvisioningHomeException(
                    "Failed to query balance remaining for the prepaid subscriber from OCG, sub=" + subscriber.getId()
                            + ",rc=" + rc, errorCodeForBalQuery, Common.OM_CRM_PROV_ERROR, e);
        }
        if (rc != ErrorCode.NO_ERROR && rc != ErrorCode.BAL_EXPIRED)
        {
            /**
             * TT5072021543, ignore RC 208. BM returns 208 due to its expiry date <
             * current date. Even though the
             */
            // TODO, ocg error or crm error, in Ibis it is crm
            exp = new ProvisioningHomeException(
                    "Failed to query balance remaining for the prepaid subscriber from OCG, sub=" + subscriber.getId()
                            + ", rc=" + rc, errorCodeForBalQuery, Common.OM_CRM_PROV_ERROR);
            ERLogger.logOutOfSync10339(ctx, subscriber, exp, this, 3009);
        }
        else
        {
            subscriber.setBalanceRemaining(outputBalance.value);
            subscriber.setOverdraftBalance(outputOverdraftBalance.value);
            subscriber.setOverdraftDate(outputOverdraftDate.value);
        }

        if (exp != null)
        {
            SubscriberProvisionResultCode.setProvisionCrmResultCode(ctx, errorCodeForBalQuery);
            SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, errorCodeForBalQuery);
            throw exp;
        }
    }


    /**
     * Creates a transaction RECORD with the following parameters (as opposed to
     * transaction which includes balance adjustments). I have forcibly restricted the
     * types of adjustments to only ReactivationFee and InitialBalance. The reason behind
     * this is that these are two transactions that will not be forwarded to OCG (they
     * won't adjust the BM balance).
     *
     * @param ctx            The operating context.
     * @param subscriber     the subscriber involved in the transaction
     * @param adjustmentType the adjustment type
     * @param transAmount    the transaction charge amount
     * @param transBalance   the Balance after the transaction has been done
     */
    private void createTransactionRecord(final Context ctx, final Subscriber subscriber,
            final AdjustmentType adjustmentType, final long transAmount, final long transBalance)
    {
        try
        {
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
            AdjustmentType CCPaymentAdjType =  AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, spid.getCreditCardPaymentAdjustmentID());
            boolean allowCreateTxn = false;
            if(subscriber.isInitialBalanceByCreditCard() && CCPaymentAdjType != null
                    && CCPaymentAdjType.getCode() == adjustmentType.getCode())
            {
                allowCreateTxn = true;
            }
            
            if (adjustmentType.isInCategory(ctx, AdjustmentTypeEnum.ReactivationFee)
                    || adjustmentType.isInCategory(ctx, AdjustmentTypeEnum.InitialBalance)
                    || allowCreateTxn)
            {
                final Context subCtx = ctx.createSubContext();
                subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);

                /*
                 * The transaction will not be forwarded to the OCG because the call to
                 * requestDebit has already been done (see
                 * OcgForwardTransactionHome.create method)
                 */
                TransactionSupport.createTransaction(subCtx, subscriber, transAmount, transBalance, adjustmentType,
                        false, new Date(), "");
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Transaction record creation not valid for Transaction Adjusment Type "
                            + adjustmentType.getName(), null).log(ctx);
                }
            }
        }
        catch (final HomeException e)
        {
            // It shouldn't stop the reactivation, if the transaction cannot be recorded.
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Failed to create Debit Reactivation Fee transaction record for subscriber "
                        + subscriber.getId(), null).log(ctx);
            }
        }
    }

    /**
     * Validate for the existence of a profile on the client application. Throw an error
     * if the profile exists.
     *
     * @param ctx The operating context.
     * @param obj Subscriber being created.
     * @param el  Exception listener.
     * @throws IllegalStateException Thrown if the subscriber is not valid.
     */
    @Override
    public void validateCreate(final Context ctx, final Object obj, final ExternalProvisioningException el)
        throws IllegalStateException
    {
        final Subscriber sub = (Subscriber) obj;

        if (sub.getUpdateReason() != UpdateReasonEnum.CONVERSION && hasProfile(ctx, sub))
        {
            el.thrown(new IllegalStateException("The profile for Msisdn " + sub.getMSISDN()
                    + " already exists in the BM."), SubscriberProvisionBMHome.class.getName());
        }
        super.validateCreate(ctx, obj, el);
    }

    /**
     * Delete the existing profile on the client application, so the creation of the new
     * profile can continue.
     *
     * @param ctx The operating context.
     * @param obj Subscriber being resolved.
     * @param el  Exception listener.
     * @throws HomeException         Thrown if there are problems resolving the conflict.
     */
    @Override
    public void resolveCreateConflict(final Context ctx, final Object obj, final ExternalProvisioningException el)
        throws HomeException
    {
        if (el.isSourceOfError(this.getClass().getName()))
        {
            final Subscriber sub = (Subscriber) obj;
            try
            {
                final SubscriberProfileProvisionClient client;
                client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
                client.deleteSubscriptionProfile(ctx, sub);
            }
            catch (final Exception e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Failed to delete previously existing profile in the BM. "
                            + e.getMessage());
                }
            }
        }
        super.resolveCreateConflict(ctx, obj, el);
    }

    /**
     * Since the resolution to the missing external profile is to do nothing, it would be
     * easy to simply omit the validateRemove and resolveRemoveConflict methods. However,
     * it is best to note that we log the out of sync issue. In the future, if we want to
     * actively deal with any validation issue, we would do it here.
     *
     * @param context The operating context.
     * @param obj     The subscriber being removed.
     * @param el      Exception listener.
     * @throws HomeException Thrown if there are problems resolving the conflict.
     */
    @Override
    public void resolveRemoveConflict(final Context context, final Object obj, final ExternalProvisioningException el)
        throws HomeException
    {
        // Nothing to be done. The profile will only be removed if it exists.

        // Continue with the rest of the pipeline
        super.resolveRemoveConflict(context, obj, el);
    }

    /**
     * Update to the BM is done through an Add/Update command. If the profile being
     * stored does not exist on the BM, then it is added. The only scenario we have to
     * beware of is Changing the MSISDN, when the new MSISDN already has an existing
     * profile in the BM. For the change MSISDN action, validate for existence of the
     * external profile using the new MSISDN, if it exists, throw error.
     *
     * @param context The operating context.
     * @param obj     Subscriber being updated.
     * @param el      Exception listener.
     */
    @Override
    public void validateStore(final Context context, final Object obj, final ExternalProvisioningException el)
    {
        // Change MSISDN
        final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);

        if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(context);

            try
            {
                Parameters profile = client.querySubscriptionProfile(context, newSub);
                
                if (profile != null)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Profile with MSISDN '");
                    sb.append(newSub.getMSISDN());
                    sb.append("' already exists in BM. BSS will automatically overwrite old profile. ");
                    sb.append("Profile info: Subscriber ID = ");
                    sb.append(profile.getSubscriberID());
                    sb.append(", MSISDN = ");
                    sb.append(profile.getMsisdn());
                    sb.append(", Billing type = ");
                    sb.append(profile.getBillingType());
                    LogSupport.minor(context, this, sb.toString());
                    
                    client.removeSubscriptionProfile(context, newSub);
                }
            }
            catch (final SubscriberProfileProvisionException exception)
            {
                el.thrown(new IllegalStateException("Failure detected while communicating with BM for subcription "
                        + newSub.getId(), exception), SubscriberProvisionBMHome.class.getName());
            }
            catch (HomeException exception)
            {
                el.thrown(new IllegalStateException("Failure detected while communicating with BM for subcription "
                        + newSub.getId(), exception), SubscriberProvisionBMHome.class.getName());
            }
        }
        super.validateStore(context, obj, el);
    }

    /**
     * Only supports the Change MSISDN exception resolution. Delete the existing profile
     * that is using the new (destination) MSISDN, so that the
     * ProductUpsClient.changeMSISDN call will work.
     *
     * @param context The operating context.
     * @param obj     Subscriber with conflict.
     * @param el      Exception listener.
     */
    @Override
    public void resolveStoreConflict(final Context context, final Object obj, final ExternalProvisioningException el)
    {
        // Until the time we support another error, we don't have to check for
        // onChangeMsisdn.
        if (el.isSourceOfError(this.getClass().getName()))
        {
            final Subscriber newSub = (Subscriber) obj;
            try
            {
                final SubscriberProfileProvisionClient client;
                client = BalanceManagementSupport.getSubscriberProfileProvisionClient(context);
                client.deleteSubscriptionProfile(context, newSub);
            }
            catch (final Exception e)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, this,
                            "Failed to delete previously existing profile in the BM for MSISDN=" + newSub.getMSISDN()
                                    + " " + e.getMessage());
                }
            }
        }
    }
    /**
     * 
     * @param ctx
     * @param oldSub
     * @param newSub
     * @return
     */
    boolean needsNotificationTypeUpdate(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        boolean needs = false;
        if (oldSub != null && newSub != null )
        {
            needs = oldSub.getNotificationMethod() != newSub.getNotificationMethod();
        }
        return needs;
    }
    /**
     * 
     * @param ctx
     * @param newSub
     * @throws HomeException
     */
    private void updateNotificationType(Context ctx, Subscriber newSub) throws HomeException
    {
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updateNotificationType(ctx, newSub, newSub.getNotificationMethod());
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "provisioning result 3008: failed to update subscription [" + newSub.getId()
                    + "] Notification Type on BM due to BM error [" + resultBM + "]";
            throw new ProvisioningHomeException(err, resultBM, Common.OM_UPS_ERROR, exception);
        }
    }
    
    /**
     * As a fix of USACLD-432, on DeviceSwap, BSS was making call to CPS to update correct IMSI in CPS.
     * 
     * However, DeviceSwap includes change in PackageId on Subscriber.
     * This was causing another call from BSS to CPS.
     * During this call, IMSI value from the updated subscriber used to pass to CPS.
     * 
     * Since, Subscriber was not fully modified till that point, BSS used to send old IMSI value to CPS.
     * 
     * This method takes care of that scenario.
     * It fetches correct IMSI by PackageId and not the one on Subscriber.
     * Subscriber.PackageId --> PkgId of PrimaryPackage
     * PrimaryPackage.SerialNo --> PkgId of SecondaryPackage
     * SecondaryPackage.Min --> IMSI
     * 
     * This is applicable only for CDMA / TDMA Packages.
     *  
     * @param ctx
     * @param subscriber
     * @return imsi
     * @see SwapPackageSupport.attachPackage() for PrimaryPackage.SerialNo SecondaryPackage.PkgId mapping.
     * 
     * @author kashyap.deshpande
     * @since 9_9_tcb FixForBugId: USACLD-432 (http://jira01.bln1.bf.nsn-intra.net/jira/browse/USACLD-432)
     */
    private String retrieveIMSIbyPackageId(final Context ctx, final Subscriber subscriber)
    {
    	
    	String imsi = subscriber.getIMSI();
    	
    	if (subscriber.getTechnology() != TechnologyEnum.CDMA && subscriber.getTechnology() != TechnologyEnum.TDMA)
    	{
    		if (LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, this, "Subscriber technology [" + subscriber.getTechnology() + "] is "
    					+ "neither CDMA nor TDMA, no need to proceed for IMSI retrieval by packageId");
    		}
    		return imsi;
    	}
    	
		try {
			final EQ predicatePrimaryPkg = new EQ(TDMAPackageXInfo.PACK_ID, subscriber.getPackageId());
			final TDMAPackage tdmaPriamryPackage = HomeSupportHelper.get(ctx).findBean(ctx, TDMAPackage.class, predicatePrimaryPkg);
			if (tdmaPriamryPackage == null  || tdmaPriamryPackage.getPackId().trim().isEmpty())
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "No IMSI found for TDMA PrimaryPackageId:" + subscriber.getPackageId());
				}
				return imsi;
			}
			
			if (tdmaPriamryPackage.getMin() != null && !tdmaPriamryPackage.getMin().trim().isEmpty())
			{
				imsi = tdmaPriamryPackage.getMin();
			}
			
			final EQ predicateSecondaryPkg = new EQ(TDMAPackageXInfo.PACK_ID, tdmaPriamryPackage.getSerialNo());
			final TDMAPackage tdmaSecondaryPackage = HomeSupportHelper.get(ctx).findBean(ctx, TDMAPackage.class, predicateSecondaryPkg);
			if (tdmaSecondaryPackage == null || tdmaSecondaryPackage.getPackId().trim().isEmpty()
					|| tdmaSecondaryPackage.getMin() == null || tdmaSecondaryPackage.getMin().trim().isEmpty())
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "No IMSI found for TDMA SecondaryPackageId:" + tdmaPriamryPackage.getSerialNo());
				}
				return imsi;
			}
			
			if (tdmaSecondaryPackage.getMin() != null && !tdmaSecondaryPackage.getMin().trim().isEmpty())
			{
				imsi = tdmaSecondaryPackage.getMin();
			}
			
		} catch (HomeException e) {
			LogSupport.minor(ctx, this, "HomeException occurred whlie retrieving IMSI value by PackageId");
		}
    	
		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "IMSI for Subscriber [" + subscriber + "] "
					+ "by PackageId [" + subscriber.getPackageId() + "] is [" + imsi + "]");
		}
		
    	return imsi;
    }
}
