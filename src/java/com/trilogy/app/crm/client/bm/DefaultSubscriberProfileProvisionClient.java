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
package com.trilogy.app.crm.client.bm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.NotificationMethodProperty;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.config.AppEcpClientConfig;
import com.trilogy.app.crm.config.ProductAbmClientConfig;
import com.trilogy.app.crm.extension.spid.NotificationMethodSpidExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtensionXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.bundle.manager.provision.profile.error.ErrorCode;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.BCDChangeRequestReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberProfile;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberRemovalReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriberReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionProfile;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionState;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.SubscriptionsForSubscriberReturnParam;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;


/**
 * Provides a layer of indirection between the CRM+ business logic and the
 * underlying CORBA API.
 *
 * @author gary.anderson@redknee.com
  * 
 * Support clustered corba client
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 29, 2009 
*/
class DefaultSubscriberProfileProvisionClient implements SubscriberProfileProvisionClient
{
    /**
     * Creates a new client for interacting with Bundle Manager subscriber and
     * subscription profiles.
     *
     * @param context The operating context.
     */
    public DefaultSubscriberProfileProvisionClient(final Context context)
    {
        client_ = new SubscriberProfileProvisionCorbaClient(context);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addSubscriberAccountProfile(final Context context, final Account subscriberAccount)
        throws HomeException, SubscriberProfileProvisionException
    {
    	Parameter[] inParamSet = null;
        Parameters param = new Parameters();
        final SubscriberProfile subscriberProfile;

        final String subscriberID = subscriberAccount.getBAN();

        {
            final CRMSpid spid = SpidSupport.getCRMSpid(context, subscriberAccount.getSpid());
            final String tzOffset = spid.getTimezone();
            final int billingID = getBillingDay(context, subscriberAccount);
            // TODO -- creationDate is explicitly set for purposes of syncing
            // creation date from CRM+. We need to potentially add a "created"
            // date to the account profile.
            final long creationDate = System.currentTimeMillis();
            final boolean enableNotification = isNotificationEnabled(context, subscriberAccount);

            param.birthDay(subscriberAccount.getDateOfBirth());
            param.email(subscriberAccount.getEmailID());
            
            inParamSet	= param.end();
            subscriberProfile =
                new SubscriberProfile(subscriberID, spid.getId(), tzOffset, billingID, creationDate,
                    enableNotification);
        }

        final SubscriberReturnParam results = client_.createSubscriberProfile(context, subscriberProfile, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure during subscriber account profile creation for BAN " + subscriberID);
        }
    }


    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Parameters querySubscriberAccountProfile(final Context context, final Account subscriberAccount)
        throws SubscriberProfileProvisionException
    {
        final Parameter[] inParamSet = new Parameter[0];

        final String subscriberID = subscriberAccount.getBAN();
        
        Context subCtx = context.createSubContext();        
        MSP.setBeanSpid(subCtx, subscriberAccount.getSpid());

        final SubscriberReturnParam results =
            client_.getSubscriberProfile(subCtx, subscriberID, inParamSet);

        final Parameters parameters;

        if (!isErrorCode(context, results.resultCode))
        {
            parameters = new Parameters(results.outParamSet);
            copy(results.outSubscriberProfile, parameters);
        }
        else
        {
            if (results.resultCode != ErrorCode.RECORD_NOT_FOUND)
            {
                throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failed to query profile for subscriber account " + subscriberID);
            }

            parameters = null;
        }

        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Parameters> queryAllSubscriptionsForSubscriber(final Context context, final String subscriberID)
        throws SubscriberProfileProvisionException, HomeException
    {
        final Parameter[] inParamSet = new Parameter[0];

        final SubscriptionsForSubscriberReturnParam results =
            client_.getAllSubscriptionsForSubscriber(context, subscriberID, inParamSet);

        List<Parameters> subscriptions = new ArrayList<Parameters>();

        if (!isErrorCode(context, results.resultCode))
        {
            for (SubscriptionProfile subscription : results.subscriptionProfiles)
            {
                Parameters parameters = new Parameters();
                copy(subscription, parameters);
                parameters.spid(results.suscriberProfile.spid);
                parameters.creationDate(new Date(results.suscriberProfile.creationDate));
                subscriptions.add(parameters);
            }
        }
        else
        {
            if (results.resultCode != ErrorCode.RECORD_NOT_FOUND)
            {
                throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failed to query profile for subscriber account " + subscriberID);
            }
        }

        return subscriptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSubscriberAccountProfile(final Context context, final Account subscriberAccount)
        throws SubscriberProfileProvisionException
    {
        final Parameter[] inParamSet = new Parameter[0];

        final String subscriberID = subscriberAccount.getBAN();

        final SubscriberRemovalReturnParam results =
            client_.removeSubscriberProfile(context, subscriberID, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failure during subscriber account profile deletion for BAN " + subscriberID);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Parameters removeSubscriptionProfile(final Context context, final Subscriber subscription)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int) subscription.getSubscriptionType();
        final Parameter[] inParamSet = new Parameter[0];
        final SubscriptionReturnParam results = client_.removeSubscriptionProfile(context, subscription.getMSISDN(),
                subscriptionType, inParamSet);
        final Parameters parameters;
        if (!isErrorCode(context, results.resultCode))
        {
            parameters = new Parameters(results.outParamSet);
            copy(results.outSubscriptionProfile, parameters);
        }
        else
        {
            if (results.resultCode != ErrorCode.RECORD_NOT_FOUND)
            {
                throw new SubscriberProfileProvisionException(results.resultCode,
                        "Failed to query profile for subscription " + subscription.getId() + ", MSISDN "
                                + subscription.getMSISDN() + ", subscription type " + subscriptionType);
            }
            parameters = null;
        }
        return parameters;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void addSubscriptionProfile(final Context context, final Subscriber subscription)
        throws HomeException, SubscriberProfileProvisionException
    {
        final Parameters parameters = new Parameters().pricePlan(subscription.getPricePlan());

        final Account subscriberAccount = subscription.getAccount(context);
        final String subscriberID = subscriberAccount.getBAN();

        long monthlySpendLimit = -1;
        //Reason: This check is for hidden subscription we have for group pooled account.
        //Hidden subscription don't have subscriberAccount, it is always under the Pooled Account
		if (subscriberAccount.isPooled(context)
                && subscriberAccount.getOwnerMSISDN() != null
                && subscriberAccount.getOwnerMSISDN().length() > 0)
        {
            parameters.poolGroupOwner(subscriberAccount.getOwnerMSISDN());
        }
        
		if (subscription.isPostpaid() && (!subscriberAccount.isPooled(context)))
        {
            String groupMSISDN = "";
            
			if (subscriberAccount != null
			    && subscriberAccount.isIndividual(context)
			    && !subscriberAccount.isResponsible())
            {
                groupMSISDN = subscriberAccount.getGroupMSISDN(context, subscription.getSubscriptionType());
            }
            if ( groupMSISDN == null || groupMSISDN.length() <= 0)
            {
                monthlySpendLimit = subscription.getMonthlySpendLimit();
            }
        }
        
        parameters.monthlySpendLimit(monthlySpendLimit); 
        
        parameters.billingType(subscription.getSubscriberType().getIndex());
        
        if (subscription.getBillingLanguage() != null && !subscription.getBillingLanguage().isEmpty())
        {
            parameters.billingLanguage(subscription.getBillingLanguage());
        }
        
        // Multiplay support, for one account - multiple subscribers with same subscription type
        if(allowMultiSubPerAccount(context, subscriberAccount))
        {
        	parameters.allowMultiSub(Boolean.TRUE);
        }

        final int bmState;
        if (subscription.isPrepaid())
        {
           
			 bmState = SubscriptionState.PENDING_ACTIVATION;
        }
        else
        {
            bmState = SubscriptionState.ACTIVE;
        }

        final int subscriptionType = (int)subscription.getSubscriptionType();
        final int subscriptionLevel = getSubscriptionLevelID(context, subscription);
        final String msisdn = subscription.getMSISDN();

        final String groupID = subscription.getPoolID(context);

        final String currency = subscriberAccount.getCurrency();
        final long creditLimit = subscription.getCreditLimit(context);

        final long balance = subscription.getInitialBalance();

        long overDraftBalanceLimit = 0;
        
        if (subscription.isPrepaid() && !subscription.isPooled(context)
                && subscription.getSubscriptionType(context).getType() == SubscriptionTypeEnum.AIRTIME_INDEX)
        {
            OverdraftBalanceSpidExtension extension = HomeSupportHelper.get(context).findBean(context,
                    OverdraftBalanceSpidExtension.class,
                    new EQ(OverdraftBalanceSpidExtensionXInfo.SPID, Integer.valueOf(subscription.getSpid())));
    
            if (extension!=null)
            {
                overDraftBalanceLimit = extension.getLimit();
                parameters.overdraftBalanceLimit(overDraftBalanceLimit);
            }
        }
        
        final long expiryDate = Parameters.NO_DATE;
        // TODO -- I assume this to be start date, but is that correct?
        final long activationDate;
        if (subscription.getStartDate() != null)
        {
            activationDate = subscription.getStartDate().getTime();
        }
        else
        {
            activationDate = 0L;
        }
        
        parameters.IMSI(subscription.getIMSI());
        
        if(subscription.getTechnology() == TechnologyEnum.CDMA)
	    {
	    	And and = new And();
	      	and.add(new EQ(TDMAPackageXInfo.PACK_ID, subscription.getPackageId()));
	        and.add(new EQ(TDMAPackageXInfo.SPID, subscription.getSpid()));
	        and.add(new EQ(TDMAPackageXInfo.TECHNOLOGY ,TechnologyEnum.CDMA));
	        final TDMAPackage cdmaCard = HomeSupportHelper.get(context).findBean(context, TDMAPackage.class, and);
	        if(cdmaCard != null)
	        {
	        	parameters.setImsi(subscription.getIMSI());
	        	String esn = cdmaCard.getESN();
	        	if(esn != null && esn.trim().length()>0)
		        {
	        		parameters.ESN(esn);
		        }
	        	
	        	String msid = cdmaCard.getExternalMSID();
	        	if(msid != null && msid.trim().length()>0)
		        {
	        		parameters.MSID(msid);
		        }
	        	LogSupport.debug(context, this, " parameters - ESN:-"  + esn + " MSID :-" + msid);
	        }
	    }               
        
        AppEcpClientConfig config = (AppEcpClientConfig)context.get(AppEcpClientConfig.class);
        
        parameters.classOfService(config.getClassOfService(context, subscriberAccount.getSpid(), subscriberAccount
                .getAccountCategory().getIdentifier(), subscription.getSubscriberType()));
        
        parameters.setBalanceThreshold(subscription.getAtuBalanceThreshold());
		parameters.setGroupScreeningTemplateld(subscription.getGroupScreeningTemplateId());
        
        NotificationMethodEnum notificationMethod = getNotificationMethod(context, subscription);
        int prefferedNotifyMethod;
        if(notificationMethod.getIndex()==NotificationMethodEnum.DEFAULT_INDEX)
        {
        	prefferedNotifyMethod= NotificationMethodEnum.SMS_INDEX;        	
        }
        else
        {
        	prefferedNotifyMethod = notificationMethod.getIndex();
        }
		parameters.notificationType(prefferedNotifyMethod);
		final Parameter[] inParamSet = parameters.end();

        final int state = bmState;
        long groupQuota = subscription.getQuotaLimit();
        final long groupUsage = 0;

        if(subscription.isPrepaid())
        {
        	groupQuota = 0L;
        }
        
        final SubscriptionProfile subscriberProfile =
            new SubscriptionProfile(subscriberID, subscriptionType, subscriptionLevel, msisdn, groupID, currency,
                creditLimit, balance, overDraftBalanceLimit, expiryDate, activationDate, state, groupQuota,
                groupUsage);

        final SubscriptionReturnParam results =
            client_.createSubscriptionProfile(context, subscriberProfile, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure during subscription creation for ID " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }


	public void updateGroupScreeningTemplateId(Context context,
			Subscriber subscription, long groupScreeningTemplateId)
			throws HomeException, SubscriberProfileProvisionException
	{
		final int subscriptionType = (int) subscription.getSubscriptionType();

		final Parameters parameters = new Parameters().setGroupScreeningTemplateld(groupScreeningTemplateId);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	        
		final SubscriptionReturnParam results = client_
				.updateSubscriptionProfile(context, subscription.getMSISDN(),
						subscriptionType, inParamSet);

		if (isErrorCode(context, results.resultCode)) 
		{
			throw new SubscriberProfileProvisionException(results.resultCode,
					"Failure updating groupScreeningTemplateId for subscription profile "
							+ subscription.getId() + ", MSISDN "
							+ subscription.getMSISDN() + ", subscription type "
							+ subscriptionType);
		}
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Parameters querySubscriptionProfile(final Context context, final Subscriber subscription)
        throws SubscriberProfileProvisionException
    {
        final Parameter[] inParamSet = new Parameter[0];

        final int subscriptionType = (int)subscription.getSubscriptionType();

        final SubscriptionReturnParam results =
            client_.getSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        final Parameters parameters;

        if (!isErrorCode(context, results.resultCode))
        {
            parameters = new Parameters(results.outParamSet);
            copy(results.outSubscriptionProfile, parameters);
        }
        else
        {
            if (results.resultCode != ErrorCode.RECORD_NOT_FOUND)
            {
                throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failed to query profile for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
            }

            parameters = null;
        }

        return parameters;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSubscriptionProfile(final Context context, final Subscriber subscription)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();
        final Parameter[] inParamSet = new Parameter[0];
        final SubscriptionReturnParam results =
            client_.removeSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failure deleting subscription ID " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePooledGroupID(final Context context, final Subscriber subscription, final String identifier,
        final boolean clearQuota)
        throws SubscriberProfileProvisionException
    {
        final Parameter[] inParamSet;
        {
            Parameters builder = new Parameters().poolGroupID(identifier);
            if (clearQuota)
            {
                builder = builder.groupUsage(0).groupQuota(0);
            }

            if(subscription.isPrepaid())
            {
            	builder.groupQuota(0L);
            }
    	    
            inParamSet = builder.end();
        }

        final int subscriptionType = (int)subscription.getSubscriptionType();

        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating pooled group ID for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePooledGroupOwner(
        final Context context,
        final Subscriber subscription,
        final String ownerIdentifier)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().poolGroupOwner(ownerIdentifier);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating pooled group Owner for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePricePlan(
        final Context context,
        final Subscriber subscription,
        final long pricePlan)
        throws SubscriberProfileProvisionException, HomeException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();
        final int subscriptionLevel = getSubscriptionLevelID(context, subscription);
        
        final Parameters parameters = new Parameters().pricePlan(pricePlan).subscriptionLevel(subscriptionLevel);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating price plan for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }

    }

    /**
     * The underlying CORBA client.
     */
    private final SubscriberProfileProvisionCorbaClient client_;

    /**
     * {@inheritDoc}
     */
    public void updateBillingLanguage(Context context, Subscriber subscription, String lang) throws HomeException,
            SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().billingLanguage(lang);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
        final Parameter[] inParamSet = parameters.end();
        
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating language for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }

    }

    public void updateBalanceThresholdAmount(Context context, Subscriber subscription, long balanceThreshold) throws HomeException,
            SubscriberProfileProvisionException
    {
        final int subscriptionType = (int) subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().setBalanceThreshold(balanceThreshold);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
        final Parameter[] inParamSet = parameters.end();
        
        final SubscriptionReturnParam results = client_.updateSubscriptionProfile(context, subscription.getMSISDN(),
                subscriptionType, inParamSet);
        
        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failure updating balanceThreshold for subscription profile " + subscription.getId() + ", MSISDN "
                            + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }

    @Override
    public void updateOverdraftBalanceLimit(Context context, Subscriber subscription, long limit)
            throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int) subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().overdraftBalanceLimit(limit);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	        
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating overdraft balance limit for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
     
    }

    
    @Override
    public void updateDualBalance(Context context, Subscriber subscription, boolean status)
            throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().dualBalanceStatus(status);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
        final Parameter[] inParamSet = parameters.end();
        
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating dual balance status for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
     
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBAN(final Context context, final Subscriber subscription, final String newBan)
        throws HomeException, SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().subscriberID(newBan);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
        final Parameter[] inParamSet = parameters.end();
        
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating BAN for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }

    /**
     * 
     */
	@Override
	public void updateStateAndExpiryDate(Context context,
			Subscriber subscription, int bmState, Date expiryDate)
			throws HomeException, SubscriberProfileProvisionException 
	{

        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().expiryDate(expiryDate).state(bmState);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating expiry date and state for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    
		
	}
	

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateExpiryDate(final Context context, final Subscriber subscription, final Date expiryDate)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().expiryDate(expiryDate);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
        final Parameter[] inParamSet = parameters.end();
        
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating expiry date for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBalance(final Context context, final Subscriber subscription, final long balance)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().balance(balance);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
        final Parameter[] inParamSet = parameters.end();

        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating the balance of subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateState(final Context context, final Subscriber subscription, final int bmState)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int) subscription.getSubscriptionType();
        
        final Parameters parameters = new Parameters().state(bmState);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
	    
        final SubscriptionReturnParam results = client_.updateSubscriptionProfile(context, subscription.getMSISDN(),
                subscriptionType, inParamSet);
        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failure updating the state of subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBillingDay(final Context context, final Account subscriberAccount, final int day)
        throws SubscriberProfileProvisionException
    {
        final Parameter[] inParamSet = new Parameters().billCycleDay(day).end();

        final String subscriberID = subscriberAccount.getBAN();
        
        Context subCtx = context.createSubContext();        
        MSP.setBeanSpid(subCtx, subscriberAccount.getSpid());

        final SubscriberReturnParam results =
            client_.updateSubscriberProfile(subCtx, subscriberID, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating the billing day of subscriber account " + subscriberID);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBirthDay(final Context context, final Account subscriberAccount)
        throws SubscriberProfileProvisionException
    {
        final Parameter[] inParamSet = new Parameters().birthDay(subscriberAccount.getDateOfBirth()).end();

        final String subscriberID = subscriberAccount.getBAN();
        
        Context subCtx = context.createSubContext();        
        MSP.setBeanSpid(subCtx, subscriberAccount.getSpid());

        final SubscriberReturnParam results =
            client_.updateSubscriberProfile(subCtx, subscriberID, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating the birth day of subscriber account " + subscriberID);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMobileNumber(final Context context, final Subscriber subscription, final String oldMobileNumber)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().msisdn(subscription.getMSISDN());

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	        
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, oldMobileNumber, subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating the mobile number of subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void updateIMSI(Context context, Subscriber subscription, String IMSI) throws HomeException,
            SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();
        
        Parameter[] inParamSet = null;
        Parameters param = new Parameters();
        
        if(subscription.isPrepaid())
        {
        	param.groupQuota(0L);
        }
	   	    
        param = param.IMSI(subscription.getIMSI());
        if(subscription.getTechnology() == TechnologyEnum.CDMA)
        {
        	And and = new And();
        	and.add(new EQ(TDMAPackageXInfo.PACK_ID, subscription.getPackageId()));
        	and.add(new EQ(TDMAPackageXInfo.SPID, subscription.getSpid()));
	        and.add(new EQ(TDMAPackageXInfo.TECHNOLOGY ,TechnologyEnum.CDMA));
        	final TDMAPackage cdmaCard = HomeSupportHelper.get(context).findBean(context, TDMAPackage.class, and);
        	if(cdmaCard != null)
 	        {
        		param.setImsi(subscription.getIMSI());
 	        	String esn = cdmaCard.getESN();
 	        	if(esn != null && esn.trim().length()>0)
 		        {
 	        		param.ESN(esn);
 		        }
 	        	
 	        	String msid = cdmaCard.getExternalMSID();
 	        	if(msid != null && msid.trim().length()>0)
 		        {
 	        		param.MSID(msid);
 		        }
 	        	LogSupport.debug(context, this, " parameters - ESN:-"  + esn + " MSID :-" + msid);
 	        }

        }
        inParamSet = param.end();
                

        
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMsisdn(), subscriptionType, inParamSet);
    
        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating the IMSI of subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
        
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSubscriptionQuotaLimit(final Context context, final Subscriber subscription, final long quota)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().groupQuota(quota);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating quota limit for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSubscriptionMonthlySpendLimit(final Context context, final Subscriber subscription,
            final long spendLimit) throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().monthlySpendLimit(spendLimit);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating monthly spend limit for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatSubscriptionPPSMSupporter(final Context context, final Subscriber subscription,
            final String ppsmSupporterBan, final long ppsmScreeningTemplate)
        throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();
        final Parameters parameters = new Parameters();
        if (ppsmSupporterBan!=null && !ppsmSupporterBan.isEmpty())
        {
            parameters.ppsmSupporter(ppsmSupporterBan);
            parameters.ppsmScreeningTemplate(ppsmScreeningTemplate);
        }
        else
        {
            parameters.ppsmSupporter("-1");
            parameters.ppsmScreeningTemplate(ppsmScreeningTemplate);
        }

        final Parameter[] inParamSet = parameters.end();
        
        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating PPSM information for subscription " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public long adjustCreditLimit(final Context context, final Subscriber subscription, final long amount,
        final String erReference)
        throws SubscriberProfileProvisionException
    {
        final String msisdn = subscription.getMSISDN();

        final int subscriptionType = (int)subscription.getSubscriptionType();

        final LongHolder creditLimit = new LongHolder();

        final short result;
        if (amount > 0)
        {
            result = client_.incCreditLimit(context, msisdn, subscriptionType, amount, erReference, creditLimit);
        }
        else
        {
            result = client_.decCreditLimit(context, msisdn, subscriptionType, -amount, erReference, creditLimit);
        }

        if (isErrorCode(context, result))
        {
            throw new SubscriberProfileProvisionException(result,
                    "Failure adjusting credit limit for subscription " + subscription.getId()
                            + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
        return creditLimit.value;
    }
    
    /**
     * 
     */
    public long adjustCreditLimit(final Context context, final Subscriber subscription, final long newCreditLimit, long oldCreditLimit,final String erReference) throws SubscriberProfileProvisionException
    {
        
        long amount = (newCreditLimit - oldCreditLimit);
        long creditLimit = adjustCreditLimit(context, subscription, amount, erReference);
        if(creditLimit != newCreditLimit)
        {
            new InfoLogMsg(this, "Credit limit out of synch. Attempting to synchronize. Expected Credit Limit ["+ newCreditLimit +"] : Existing Credit Limit ["+ creditLimit + "] ", null).log(context);
            amount = newCreditLimit - creditLimit;
            creditLimit = adjustCreditLimit(context, subscription, amount, erReference);
        }
        if(creditLimit != newCreditLimit)
        {
            String message = "Could not synchroize credit limit. Expected Credit Limit ["+ newCreditLimit +"] : Existing Credit Limit ["+ creditLimit + "] "; 
            ExceptionListener excl = (ExceptionListener)context.get(ExceptionListener.class);
            if(null != excl)
            {
                excl.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.CREDIT_LIMIT, message));
            }
            new MinorLogMsg(this, message, null).log(context);
        } else
        {
            new InfoLogMsg(this, "Credit limit successfully syncrhonized. Expected Credit Limit ["+ newCreditLimit +"] : Existing Credit Limit ["+ creditLimit + "] ", null).log(context);
        }
        return creditLimit;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void adjustGroupQuota(final Context context, final Subscriber subscription, final long amount,
        final String erReference)
        throws SubscriberProfileProvisionException
    {
        final String msisdn = subscription.getMSISDN();

        final int subscriptionType = (int)subscription.getSubscriptionType();

        final LongHolder groupQuota = new LongHolder();

        final short result;
        if (amount > 0)
        {
            result = client_.incGroupQuota(context, msisdn, subscriptionType, amount, erReference, groupQuota);
        }
        else
        {
            result = client_.decGroupQuota(context, msisdn, subscriptionType, -amount, erReference, groupQuota);
        }

        if (isErrorCode(context, result))
        {
            throw new SubscriberProfileProvisionException(result,
                    "Failure adjusting group quota for subscription " + subscription.getId()
                            + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
    }

    @Override
    public void resetMonthlySpendUsage(Context context, Subscriber subscription)
            throws  SubscriberProfileProvisionException
    {
        final String msisdn = subscription.getMSISDN();
        final int subscriptionType = (int) subscription.getSubscriptionType();
        final short result = client_.resetMonthlySpendUsage(context, msisdn, subscriptionType);
        if (isErrorCode(context, result))
        {
            throw new SubscriberProfileProvisionException(result, "Failure reset Monthly spend usage for subscription "
                    + subscription.getId() + ", MSISDN " + subscription.getMSISDN() + ", subscription type "
                    + subscriptionType);
        }
    }


    @Override
    public void resetGroupUsage(Context context, Subscriber subscription)
            throws SubscriberProfileProvisionException
    {
        final String msisdn = subscription.getMSISDN();
        final int subscriptionType = (int) subscription.getSubscriptionType();
        final short result = client_.resetGroupUsage(context, msisdn, subscriptionType);
        if (isErrorCode(context, result))
        {
            throw new SubscriberProfileProvisionException(result, "Failure reset group quota for subscription "
                    + subscription.getId() + ", MSISDN " + subscription.getMSISDN() + ", subscription type "
                    + subscriptionType);
        }
    }


    /**
     * {@inheritDoc}
     */
    public BCDChangeRequestReturnParam[] addUpdateBcdChangeRequest(Context context, String[] subscriptionIDs,
            int newBillCycleDay) throws SubscriberProfileProvisionCorbaException
    {
        return client_.addUpdateBcdChangeRequest(context, subscriptionIDs, newBillCycleDay);
    }


    /**
     * {@inheritDoc}
     */
    public BCDChangeRequestReturnParam[] removeBcdChangeRequest(Context context, String[] subscriptionIDs)
            throws SubscriberProfileProvisionCorbaException
    {
        return client_.removeBcdChangeRequest(context, subscriptionIDs);
    }

    /**
     * Copies the values of subscriber profile to the more generic list of
     * parameters.
     *
     * @param subscriberProfile The subscriber account profile for which to copy
     * parameter values.
     * @param parameters The Parameters object into which to copy parameter
     * values.
     */
    private void copy(final SubscriberProfile subscriberProfile, final Parameters parameters)
    {
        parameters
            .subscriberID(subscriberProfile.subscriberId)
            .spid(subscriberProfile.spid)
            .timezoneOffset(subscriberProfile.tzOffset)
            .billCycleDay(subscriberProfile.billingID)
            // .creationDate(subscriberProfile.creationDate)
            .enableNotification(subscriberProfile.enableNotification);
    }


    /**
     * Copies the values of subscription profile to the more generic list of
     * parameters.
     *
     * @param subscriptionProfile The subscription profile for which to copy
     * parameter values.
     * @param parameters The Parameters object into which to copy parameter
     * values.
     */
    private void copy(final SubscriptionProfile subscriptionProfile, final Parameters parameters)
    {
        parameters
            .subscriberID(subscriptionProfile.subscriberId)
            .subscriptionType(subscriptionProfile.subscriptionType)
            .subscriptionLevel(subscriptionProfile.subscriptionLevel)
            .msisdn(subscriptionProfile.msisdn)
            .poolGroupID(subscriptionProfile.groupId)
            .currency(subscriptionProfile.currency)
            .creditLimit(subscriptionProfile.creditLimit)
            .balance(subscriptionProfile.balance)
            .overdraftBalanceLimit(subscriptionProfile.overDraftBalanceLimit)
            .expiryDate(new Date(subscriptionProfile.expiryDate))
            .activationDate(new Date(subscriptionProfile.activationDate))
            .state(subscriptionProfile.state)
            .groupQuota(subscriptionProfile.groupQuota)
            .groupUsage(subscriptionProfile.groupUsage);
    }


    /**
     * @param context The operating context.
     * @param resultCode The result code to check as an error code.
     * @return True if the given result code is an error code; false otherwise.
     */
    private boolean isErrorCode(final Context context, final short resultCode)
    {
        return resultCode != ErrorCode.SUCCESS;
    }


    /**
     * Gets the billing day of the given subscriber Account.
     *
     * @param context The operating context.
     * @param subscriberAccount The account for which to get the billing day.
     * @return The billing date of the given subscriber Account.
     * @throws HomeException Thrown if there is a problem accessing bill cycle
     * information.
     */
    private int getBillingDay(final Context context, final Account subscriberAccount)
        throws HomeException
    {
        final ProductAbmClientConfig abmClientConfig =
            (ProductAbmClientConfig)context.get(ProductAbmClientConfig.class);

        if (abmClientConfig.getSpBillingCycleEnable())
        {
            return 0;
        }

        final int billingDay;

        final BillCycle billCycle = subscriberAccount.getBillCycle(context);
        if (billCycle != null)
        {
            billingDay = billCycle.getDayOfMonth();
        }
        else
        {
            throw new HomeException("Failed to find a bill cycle for ID " + subscriberAccount.getBillCycleID()
                + " referenced by account " + subscriberAccount.getBAN());
        }

        return billingDay;
    }

    /**
     * Gets the ID of the subscription level of the given subscription.
     *
     * @param context The operating context.
     * @param subscription The subscription for which to get the level ID.
     * @return The level ID.
     * @throws HomeException Thrown if there are problems accessing Home data in the context.
     */
    private int getSubscriptionLevelID(final Context context, final Subscriber subscription)
        throws HomeException
    {
        final long planID = subscription.getPricePlan();
        final Home home = (Home)context.get(PricePlanHome.class);

        final PricePlan plan = (PricePlan)home.find(context, Long.valueOf(planID));
        return (int)plan.getSubscriptionLevel();
    }


    /**
     * Indicates whether or not notification is enabled for the given subscriber account.
     *
     * @param context The operating context.
     * @param subscriberAccount The subscriber account for which to check.
     * @return True if notification is enabled; false otherwise.
     * @throws HomeException Thrown if there are problems accessing Home data in the context.
     */
    private boolean isNotificationEnabled(final Context context, final Account subscriberAccount)
        throws HomeException
    {
        // Notification is currently specified at the service provider level.
        final int spid = subscriberAccount.getSpid();

        final Home home = (Home)context.get(CRMSpidHome.class);
        final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
        if (serviceProvider == null)
        {
            throw new HomeException(
                "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
        }

        return serviceProvider.isEnableSMSOnBundleExpiry();
    }
    
    private boolean allowMultiSubPerAccount(final Context context, final Account subscriberAccount)
    throws HomeException
	{
	    // Notification is currently specified at the service provider level.
	    final int spid = subscriberAccount.getSpid();
	
	    final Home home = (Home)context.get(CRMSpidHome.class);
	    final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
	    if (serviceProvider == null)
	    {
	        throw new HomeException(
	            "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
	    }
	
	    return serviceProvider.isAllowMultiSubForOneAccount();
	}


    @Override
    public void updatePromOptOut(Context context, Subscriber subscription, boolean status)
            throws SubscriberProfileProvisionException
    {
        final int subscriptionType = (int)subscription.getSubscriptionType();

        final Parameters parameters = new Parameters().promotionalSmsOptOut(status);

        if(subscription.isPrepaid())
        {
        	parameters.groupQuota(0L);
        }
	    final Parameter[] inParamSet = parameters.end();
	    
        final SubscriptionReturnParam results =
            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

        if (isErrorCode(context, results.resultCode))
        {
            throw new SubscriberProfileProvisionException(results.resultCode,
                "Failure updating promotional sms opt-out status for subscription profile " + subscription.getId()
                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
        }
     
    }

	@Override
	public Parameters getSubscriptionProfile(Context context, String msisdn, int subscriptionType, Parameter[] inParamSet) 
		throws HomeException, SubscriberProfileProvisionException 
	{
        final SubscriptionReturnParam results =
            client_.getSubscriptionProfile(context, msisdn, subscriptionType, inParamSet);

        final Parameters parameters;

        if (!isErrorCode(context, results.resultCode))
        {
            parameters = new Parameters(results.outParamSet);
            copy(results.outSubscriptionProfile, parameters);
        }
        else
        {
            if (results.resultCode != ErrorCode.RECORD_NOT_FOUND)
            {
                throw new SubscriberProfileProvisionException(results.resultCode,
                    "Failed to query profile for msisdn " + msisdn);
            }

            parameters = null;
        }

        return parameters;
	}
	 private  NotificationMethodEnum getNotificationMethod(Context ctx, Subscriber sub)
	    {
		 	
	        if (sub.getNotificationMethod() != NotificationMethodEnum.DEFAULT_INDEX)
	        {
	            return NotificationMethodEnum.get((short) sub.getNotificationMethod());
	        }
	        else
	        {
	            try
	            {
	                CRMSpid spid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
	                for (Object obj : spid.getExtensions())
	                {
	                    if (obj instanceof NotificationMethodSpidExtension)
	                    {
	                        NotificationMethodSpidExtension ext = (NotificationMethodSpidExtension) obj;
	                        Long key = Long.valueOf(sub.getSubscriptionType());
	                        if (ext.getNotificationMethods().containsKey(key) &&(((NotificationMethodProperty) ext
	                                            .getNotificationMethods().get(key))
	                                            .getDefaultMethod()!= NotificationMethodEnum.DEFAULT_INDEX))
	                        {
	                            return NotificationMethodEnum
	                                    .get((short) ((NotificationMethodProperty) ext
	                                            .getNotificationMethods().get(key))
	                                            .getDefaultMethod());
	                        }
	                        return NotificationMethodEnum.get((short) ext.getDefaultMethod());
	                    }
	                }
	            }
	            catch (HomeException exception)
	            {
	                new MinorLogMsg(SubscriptionNotificationSupport.class,
	                        "Cannot retrieve SPID", exception).log(ctx);
	            }
	            return NotificationMethodEnum.SMS;
	        }
	    }
	 /**
	     * {@inheritDoc}
	     */
	    public void updateEmailId(final Context context, final Account subscriberAccount, final String email)
	        throws SubscriberProfileProvisionException
	    {
	        final Parameter[] inParamSet = new Parameters().email(email).end();

	        final String subscriberID = subscriberAccount.getBAN();

	        final SubscriberReturnParam results =
	            client_.updateSubscriberProfile(context, subscriberID, inParamSet);

	        if (isErrorCode(context, results.resultCode))
	        {
	            throw new SubscriberProfileProvisionException(results.resultCode,
	                "Failure updating email Id of subscriber account " + subscriberID);
	        }
	    }
	    
	    @Override
	    public void updateNotificationType(Context context, Subscriber subscription, int notificationType)
	            throws SubscriberProfileProvisionException
	    {
	        final int subscriptionType = (int)subscription.getSubscriptionType();
	        NotificationMethodEnum notificationMethod = getNotificationMethod(context, subscription);
	        if(notificationMethod == NotificationMethodEnum.DEFAULT)
	        {
	        	notificationType = NotificationMethodEnum.SMS_INDEX;
	        }
	        else
	        {
	        	notificationType = notificationMethod.getIndex();
	        }
	        
	        final Parameters parameters = new Parameters().notificationType(notificationType);

	        if(subscription.isPrepaid())
	        {
	        	parameters.groupQuota(0L);
	        }
		    final Parameter[] inParamSet = parameters.end();
		        
		    
	        final SubscriptionReturnParam results =
	            client_.updateSubscriptionProfile(context, subscription.getMSISDN(), subscriptionType, inParamSet);

	        if (isErrorCode(context, results.resultCode))
	        {
	            throw new SubscriberProfileProvisionException(results.resultCode,
	                "Failure updating notification Type " + subscription.getId()
	                        + ", MSISDN " + subscription.getMSISDN() + ", subscription type " + subscriptionType);
	        }
	     
	    }

}
