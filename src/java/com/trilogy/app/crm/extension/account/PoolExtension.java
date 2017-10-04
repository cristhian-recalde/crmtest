/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.extension.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.QuotaTypeEnum;
import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This account extension allows to specify additional properties needed to devine Pools
 * in the Mobile Money CRM.
 * 
 * @author victor.stratan@redknee.com
 * @author simar.singh@redknee.com
 */
public class PoolExtension extends AbstractPoolExtension implements ContextAware
{

   
    private static final long serialVersionUID = 1L;
    private static Object lock = new Object();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary(final Context ctx)
    {
        return PoolExtensionXInfo.POOL_MSISDN.getLabel() + "=" + this.getPoolMSISDN();
    }


    @Override
    public void validate(final Context ctx) throws IllegalStateException
    {
        final CompoundIllegalStateException exception = new CompoundIllegalStateException();
        
        ExtensionAware parentBean = this.getParentBean(ctx);
        
		GroupTypeEnum type = null;
        if( parentBean instanceof Account )
        {
			type = ((Account) parentBean).getGroupType();
        }
        else if( parentBean instanceof AccountCreationTemplate )
        {
			type = ((AccountCreationTemplate) parentBean).getGroupType();
        }
        else if( parentBean instanceof ConvertAccountGroupTypeRequest )
        {
            type = ((ConvertAccountGroupTypeRequest) parentBean).getGroupType();
        }
		if (type != null && !type.equals(GroupTypeEnum.GROUP_POOLED))
        {
            exception.thrown(new IllegalPropertyArgumentException(parentBean.getExtensionHolderProperty(), this.getName(ctx) + " extension only allowed for pooled account types."));
        }
        
        exception.throwAll();
    }
   


    @Override
    public void install(final Context ctx) throws ExtensionInstallationException
    {
        final Account account = getAccount(ctx);
        if (account == null)
        {
            throw new ExtensionInstallationException("Unable to install " + this.getName(ctx)
                    + " extension.  No account found with BAN=" + this.getBAN(), false);
        }
        synchronized (lock) 
        {
        	LogSupport.debug(ctx, this, "Sunchronized the block to get free available MSISDN for account : " + account.getBAN());
        	acquireAndSetPoolMsisdn(ctx);
		}
        
        createAllPoolSubscriptions(ctx, account, getSubscriptionPoolProperties(), false);
        LogSupport.debug(ctx, this, this.getName(ctx) + " installed on account " + account.getBAN() + " successfully.");
    }
    
    @Override
    public String getPoolMSISDN()
    {
        Context ctx = getContext();
        
        if (ctx==null)
        {
            ctx = ContextLocator.locate();
        }

        if (null != ctx && !(ExtensionSupportHelper.get(ctx).getParentBean(ctx) instanceof AccountCreationTemplate))
        {
            try
            {
                poolMSISDN_ = getPoolMSISDN(ctx);
            }
            catch (Throwable t)
            {
                handleGenericError(ctx, "Could not Find Pool MSISDN", t);
            }
        }
        else
        {
            poolMSISDN_ = super.getPoolMSISDN();
        }
        return ((null == poolMSISDN_) ? "" : poolMSISDN_);
    }
    
    @Override
    public void setPoolMSISDN(String poolMSISDN)
    {
       //pool MSISDN is auto; we should not set it.
    }
    

    /**
     * Do not use this method. This method has been added for convenience of incidental
     * tasks like migration where somebody might want to execute a migration step low level
     * enough to skip auto-pool-msisdn setting
     * 
     * @param poolMSISDN
     */
    public void setPoolMsisdnByForce(String poolMSISDN)
    {
        poolMSISDN_ = poolMSISDN;
    }
   
    public String getPoolMSISDN(Context ctx) throws HomeException
    {
        if(null == poolMSISDN_ || poolMSISDN_.isEmpty())
        {
            synchronized(this)
            {
                if(null == poolMSISDN_ || poolMSISDN_.isEmpty())
                {
                    
                    poolMSISDN_ = findPoolMsisdn(ctx);
                }
                if(null == poolMSISDN_ || poolMSISDN_.isEmpty())
                {
                    
                    poolMSISDN_ = getFreePoolMsisdn(ctx);
                }
            }
        }
        return poolMSISDN_;
    }
    
    
    private void acquireAndSetPoolMsisdn(final Context ctx) throws ExtensionInstallationException
    {
        try
        {
            String msisdn = findPoolMsisdn(ctx);
            if (null == msisdn || msisdn.isEmpty())
            {
                msisdn = getFreePoolMsisdn(ctx);
            }
            MsisdnManagement.claimMsisdn(ctx, msisdn, getAccount(ctx), false, "Claiming POOL MSISDN for Account");
            poolMSISDN_ = msisdn;
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            new DebugLogMsg(this,
                    "No need to do anything if MSISDN is already acquired. This exception can be ignored.", null)
                    .log(ctx);
        }
        catch (Throwable t)
        {
            throw new ExtensionInstallationException("Could not acquire and claim MSISDN for Pool. Error ["
                    + t.getMessage() + "]", t, false, true);
        }
    }
    
    public String findPoolMsisdn(final Context ctx) throws HomeException
    {

        final Subscriber subscriber = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, new EQ(SubscriberXInfo.BAN,this.getBAN()));
        if(null != subscriber && subscriber.isPooledGroupLeader(ctx))
        {
            return subscriber.getMSISDN();
        }
        return null;
    }
    
    public String getFreePoolMsisdn(Context ctx) throws HomeException
    {
        int msisdnGroupId = getMSISDNGroup(ctx);
        String msisdn = MsisdnSupport.getFreeMsisdnFromGroup(ctx, getSpid(), msisdnGroupId, SubscriberTypeEnum.HYBRID);
        if (null == msisdn || msisdn.isEmpty())
        {
            final MsisdnGroup msisdnGroup = HomeSupportHelper.get(ctx).findBean(ctx, MsisdnGroup.class, msisdnGroupId);
            if (null != msisdnGroup)
            {
                throw new HomeException("No free Charging-ID/MSISDNs exist in Pool MSISDN Group ID ["
                        + msisdnGroup.getName() + "], NAME [" + msisdnGroup.getName() + "]");
            }
            else
            {
                throw new HomeException("CPool MSISDN Group ID  has not been configured for Pool's SPID ["
                        + this.getSpid() + "]");
            }
        }
        return msisdn;
    }


    @Override
    public void update(final Context ctx) throws ExtensionInstallationException
    {
        final Account account = getAccount(ctx);
        if (account == null)
        {
            throw new ExtensionInstallationException("Unable to update " + this.getName(ctx)
                    + " extension.  No account found with BAN=" + this.getBAN(), false);
        }
        updatePoolSubscriptions(ctx, account);
        LogSupport.debug(ctx, this, this.getName(ctx) + " updated on account " + account.getBAN() + " successfully.");
    }


    @Override
    public void move(final Context ctx, Object newContainer) throws ExtensionInstallationException
    {
        if (newContainer instanceof Account)
        {
            // TODO Implement this
        }
    }


    @Override
    public void uninstall(final Context ctx) throws ExtensionInstallationException
    {
        final Account account = getAccount(ctx);
        if (account == null)
        {
            throw new ExtensionInstallationException("Unable to uninstall " + this.getName(ctx)
                    + " extension.  No account found with BAN=" + this.getBAN(), false);
        }
        removeAllPoolSubscriptions(ctx, account, this.getSubscriptionPoolProperties());
        LogSupport.debug(ctx, this, this.getName(ctx) + " uninstalled from account " + account.getBAN()
                + " successfully.");
    }


    /**
     * This Method oveerrides the bean method to lazy-load the bundles for the
     * Pool-Subscription This method is designed to use the ThreadLocal context
     * [ContextLocator] in web-controls
     */
    @Override
    public Map getPoolBundles()
    {
        final Context ctx = ContextLocator.locate();
        if (null == ctx)
        {
            return new HashMap();
        }
        try
        {
            // return the bundles as the exist in the PooledSubscription
            return getPoolBundles(ctx);
        }
        catch (HomeException e)
        {
            // cannot afford the exception to be rethrown;
            // it's a bean-field getter; it would go to the GUI.
            // gracefully log it to exception listener
            // TODO Auto-generated catch block
            final String message = "Could not get bundles for Pooled Account '" + this.getBAN()
                    + "': " + e.getMessage();
            new DebugLogMsg(this, "Could not get bundles for Pooled Account [" + this.getBAN() + "]", e).log(ctx);
            handleGenericError(ctx, message, e);
            return new HashMap();
        }
    }

    /**
     * This Method oveerrides the bean method to lazy-load the bundles for the
     * Pool-Subscription This method is designed to use the ThreadLocal context
     * [ContextLocator] in web-controls
     */
    @Override
    public void setPoolBundles(Map poolBudles)
    {
        super.setPoolBundles(poolBudles);
        isPoolBundleMapSetDirty_ = true;
    }
    
    
    @Override
    public Map getSubscriptionPoolProperties()
    {
        Context ctx = ContextLocator.locate();
        if(null != ctx)
        {
            try
            {
                return getSubscriptionPoolProperties(ctx);
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Could query pool leader subscriptions", t);
            }
            
        }
        return super.subscriptionPoolProperties_;
    }
    
    
    public Map getSubscriptionPoolProperties(Context ctx) throws HomeException
    {
        if(null == super.subscriptionPoolProperties_)
        {
            subscriptionPoolProperties_ = new HashMap();
            for(Subscriber sub : getPoolSubscriptions(ctx) )
            {
              SubscriptionPoolProperty property = (SubscriptionPoolProperty)    subscriptionPoolProperties_.get(sub.getSubscriptionType());
              if(null == property)
              {
                  property = new SubscriptionPoolProperty();
                  property.setSubscriptionType(sub.getSubscriptionType());
                  subscriptionPoolProperties_.put(sub.getSubscriptionType(), property);
              }
              property.setProvisioned((sub.getState() == SubscriberStateEnum.PENDING) || (sub.getState() == SubscriberStateEnum.AVAILABLE)? ServiceProvisionStatusEnum.PROVISIONINGFAILED_INDEX : ServiceProvisionStatusEnum.PROVISIONED_INDEX);
              property.setInitialPoolBalance(sub.getSubscriberType() == SubscriberTypeEnum.PREPAID? sub.getInitialBalance() : sub.getCreditLimit(ctx));
            }
        }
        return super.subscriptionPoolProperties_;
    }

    
//    /**
//     * @Link(getMSISDNGroup(ctx))
//     */R
//    @Deprecated
//    @Override
//    public int getMSISDNGroup()
//    {   
//        final Context ctx = ContextLocator.locate();
//        if( null != ctx)
//        {
//            return getMSISDNGroup(ctx);
//        } else
//        {
//            return MSISDNGroup_;
//        }
//        
//    }
//    
//    @Deprecated
//    @Override
//    public void setMSISDNGroup(int msisdnGroup)
//    {
//       // don't set anything
//    }
    
    
    public int getMSISDNGroup(Context ctx)
    {   
        return  SpidSupport.getGroupPooledMsisdnGroup(ctx, getSpid());
        
    }
    
    
    public SubscriberTypeEnum getSubscriberType(Context ctx)
    {
    	com.redknee.app.crm.bean.Account acct = getAccount(ctx);
    	if(acct == null)
    	{
    		return null;
    	}
    	if(acct.isPrepaid())
    	{
    		return SubscriberTypeEnum.PREPAID;
    	}
    	else if (acct.isPostpaid())
    	{
    		return SubscriberTypeEnum.POSTPAID;
    	}
    	else if (acct.isHybrid())
    	{
    		return SubscriberTypeEnum.HYBRID;
    	}
        return null;
    }
    

    public Map getPoolBundles(Context ctx) throws HomeException
    {
//        if (AbstractAccount.DEFAULT_BAN.equals(getBAN()))
//        {
//            // most likely, this case occours when account has not been created yet
//            return new HashMap();
//        }
//        else
        {
            if (poolBundles_ != null && !poolBundles_.isEmpty())
            {
                return poolBundles_;
            }
            else
            {
                Subscriber poolSub = getPoolSubscription(ctx, SubscriptionTypeEnum.AIRTIME);
                if (null != poolSub)
                {
                    return poolSub.getBundles() != null ? poolSub.getBundles() : new HashMap();
                }
                else
                {
                    return new HashMap();
                }
            }
        }
    }


    private boolean createAllPoolSubscriptions(final Context ctx, final Account account,
            final Map<Long, SubscriptionPoolProperty> propertiesMap, boolean recreate) throws ExtensionInstallationException
    {
        boolean isExtensionUpdated = false;
        
        List<Long> failedList = null;
        for (Map.Entry<Long, SubscriptionPoolProperty> entry : propertiesMap.entrySet())
        {
            final SubscriptionPoolProperty poolProp = entry.getValue();
            try
            {
                createPoolSubscription(ctx, account, poolProp, recreate);
                poolSubscriptions(ctx, account, entry.getKey());
                poolProp.setProvisioned(ServiceProvisionStatusEnum.PROVISIONED_INDEX);
                isExtensionUpdated = true;
            }
            catch (HomeException e)
            {
                final String errorMessage;
                if (e.getCause() instanceof CompoundIllegalStateException && ((CompoundIllegalStateException) e.getCause()).getSize() == 1)
                {
                    CompoundIllegalStateException cise = (CompoundIllegalStateException) e.getCause();
                    HTMLExceptionListener el = new HTMLExceptionListener(new MessageMgr(ctx, this));
                    cise.rethrow(el);
                    errorMessage = FAILED_MSG + "with subscription type " + entry.getKey() + ": " + ((Throwable) el.getExceptions().iterator().next()).getMessage();
                }
                else
                {
                    errorMessage = FAILED_MSG + "with subscription type " + entry.getKey() + ": " + e.getMessage();
                }
                handleGenericError(ctx, errorMessage, e);
                if (failedList == null)
                {
                    failedList = new ArrayList<Long>(propertiesMap.size());
                }
                failedList.add(entry.getKey());
            }
            catch (Exception e)
            {
                final String errorMessage = FAILED_MSG + "with subscription type " + entry.getKey() + ": " + e.getMessage();
                handleGenericError(ctx, errorMessage, e);
                if (failedList == null)
                {
                    failedList = new ArrayList<Long>(propertiesMap.size());
                }
                failedList.add(entry.getKey());
            }
        }
        if (failedList != null && failedList.size()>0)
        {
            final StringBuilder msg = new StringBuilder(FAILED_MSG);
            msg.append("for the following subscription types: ");
            Iterator<Long> iter = failedList.iterator();
            msg.append(iter.next());
            while (iter.hasNext())
            {
                Long id = iter.next();
                msg.append(", ");
                msg.append(id);
            }
            msg.append(".");
            throw new ExtensionInstallationException(msg.toString(), isExtensionUpdated);
        }
        
        return isExtensionUpdated;
    }


    private void createPoolSubscription(final Context ctx, final Account account, final SubscriptionPoolProperty prop, final boolean recreate)
            throws HomeException
    {
        final SubscriberTypeEnum systemType = (account.isPrepaid())
                ? (SubscriberTypeEnum.PREPAID)
                : (SubscriberTypeEnum.POSTPAID);
        final SubscriptionType subType = prop.getSubscriptionType(ctx);
        final PricePlan plan = PricePlanSupport.getPoolPricePlan(ctx, account.getSpid(), prop.getSubscriptionType(),
                systemType);
        Subscriber poolSub = FrameworkSupportHelper.get(ctx).instantiateBean(ctx, Subscriber.class);
        poolSub.setBAN(account.getBAN());
        poolSub.setSpid(account.getSpid());
        poolSub.setSubscriptionClass(Subscriber.DEFAULT_SUBSCRIPTIONCLASS);
        poolSub.setSubscriptionType(prop.getSubscriptionType());
        LogSupport.debug(ctx, this, "Setting Pool Msisdn : " + this.getPoolMSISDN() + "for account : " + account.getBAN());
        poolSub.setMSISDN(this.getPoolMSISDN());
        poolSub.setPricePlan(plan.getId());
        poolSub.setQuotaType(QuotaTypeEnum.UNLIMITED_QUOTA); // quota of a group is always
                                                             // unlimited
        poolSub.setDealerCode(account.getDealerCode());
        if (SubscriberTypeEnum.PREPAID.equals(systemType))
        {
            poolSub.setSubscriberType(SubscriberTypeEnum.PREPAID);
            poolSub.setInitialBalance(prop.getInitialPoolBalance());
        }
        else
        {
            poolSub.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            poolSub.setCreditLimit(prop.getInitialPoolBalance());
            poolSub.setDeposit(0);
            poolSub.setDepositDate(new Date());
        }
        {
            // only airtime and wireline pools can be Postpaid
            if (subType.isWallet())
            {
                if (SubscriberTypeEnum.POSTPAID == systemType)
                {
                    throw new HomeException("Pool Type '" + subType.getName()
                            + "' can only be creted under True Pre-Paid Pooled Account.");
                }
            }
            else
            {
                poolSub.setBundles(poolBundles_);
            }
        }
        if (recreate)
        {
            poolSub = HomeSupportHelper.get(ctx).storeBean(ctx, poolSub);
        }
        else
        {
            poolSub = HomeSupportHelper.get(ctx).createBean(ctx, poolSub);
        }
        
        if (SubscriberTypeEnum.PREPAID.equals(poolSub.getSubscriberType()))
        {
            poolSub = activatePoolSubscription(ctx, poolSub, prop);
        }
    }


    private Subscriber activatePoolSubscription(final Context ctx, final Subscriber poolSub,
            final SubscriptionPoolProperty prop) throws HomeException
    {
        prop.setProvisioned(ServiceProvisionStatusEnum.PROVISIONINGFAILED_INDEX);
        // automaticaly activate the pool subsription
        poolSub.setStartDate(new Date());
        poolSub.setState(SubscriberStateEnum.ACTIVE);
        return HomeSupportHelper.get(ctx).storeBean(ctx, poolSub);
    }


    private boolean diffSubscriptionTypes(final Context ctx, final Map<Long, SubscriptionPoolProperty> toCreate, final Map<Long, SubscriptionPoolProperty> toRecreate,
            final Map<Long, SubscriptionPoolProperty> toRemove, final Map<Long, SubscriptionPoolProperty> toActivate,
            final Map<Long, SubscriptionPoolProperty> toAdjust)
    {
        // This variable should be set if any of the SubscriptionPoolProperty objects are changed
        boolean isExtensionUpdated = false;
        
        toCreate.putAll(getSubscriptionPoolProperties());
        toRemove.putAll(getOldSubscriptionPoolProperties(ctx));
        for (Iterator<Map.Entry<Long, SubscriptionPoolProperty>> it = toRemove.entrySet().iterator(); it.hasNext();)
        {
            final Map.Entry<Long, SubscriptionPoolProperty> entry = it.next();
            final Long id = entry.getKey();
            final SubscriptionPoolProperty newProp = toCreate.remove(id);
            if (newProp != null)
            {
                final SubscriptionPoolProperty poolProperty = entry.getValue();
                if (poolProperty.getProvisioned() == ServiceProvisionStatusEnum.PROVISIONED_INDEX)
                {
                    it.remove();
                    if (newProp.getInitialPoolBalance() != poolProperty.getInitialPoolBalance())
                    {
                        toAdjust.put(id, newProp);
                    }
                }
                else if (poolProperty.getProvisioned() == ServiceProvisionStatusEnum.PROVISIONINGFAILED_INDEX)
                {
                    it.remove();
                    toActivate.put(id, poolProperty);
                }
                else if (poolProperty.getProvisioned() == ServiceProvisionStatusEnum.UNPROVISIONEDOK_INDEX)
                {
                    it.remove();
                    // even though the record is there, it is not provisioned
                    toRecreate.put(id, newProp);
                }
                else if (poolProperty.getProvisioned() == ServiceProvisionStatusEnum.UNPROVISIONINGFAILED_INDEX)
                {
                    // we want to finish removing, and attempt to add it back
                    toCreate.put(id, newProp);
                }
            }
        }
        
        return isExtensionUpdated;
    }


    private void updatePoolSubscriptions(final Context ctx, final Account account)
            throws ExtensionInstallationException
    {
        final int currentSize = getSubscriptionPoolProperties().size();
        Map<Long, SubscriptionPoolProperty> toCreate = new HashMap<Long, SubscriptionPoolProperty>(currentSize);
        Map<Long, SubscriptionPoolProperty> toRecreate = new HashMap<Long, SubscriptionPoolProperty>(currentSize);
        Map<Long, SubscriptionPoolProperty> toRemove = new HashMap<Long, SubscriptionPoolProperty>(currentSize);
        Map<Long, SubscriptionPoolProperty> toActivate = new HashMap<Long, SubscriptionPoolProperty>(currentSize);
        Map<Long, SubscriptionPoolProperty> toAdjust = new HashMap<Long, SubscriptionPoolProperty>(currentSize);
        
        boolean extensionUpdated = false;
        try
        {
            extensionUpdated = diffSubscriptionTypes(ctx, toCreate, toRecreate, toRemove, toActivate, toAdjust);
            extensionUpdated = activateAllPoolSubscriptions(ctx, account, toActivate) || extensionUpdated;
            extensionUpdated = createAllPoolSubscriptions(ctx, account, toCreate, false) || extensionUpdated;
            extensionUpdated = createAllPoolSubscriptions(ctx, account, toRecreate, true) || extensionUpdated;
            extensionUpdated = removeAllPoolSubscriptions(ctx, account, toRemove) || extensionUpdated;
            extensionUpdated = adjustAllPoolSubscriptions(ctx, account, toAdjust) || extensionUpdated;
            syncronizeServiceBundles(ctx, account);
        }
        catch (ExtensionInstallationException e)
        {
            e.setExtensionUpdated(extensionUpdated);
            throw e;
        }
    }


    private boolean activateAllPoolSubscriptions(final Context ctx, final Account account,
            final Map<Long, SubscriptionPoolProperty> subscriptionTypes) throws ExtensionInstallationException
    {
        // This variable should be set if any of the SubscriptionPoolProperty objects are changed
        boolean isExtensionUpdated = false;
        if (null == subscriptionTypes || subscriptionTypes.isEmpty())
        {
            return isExtensionUpdated;
        }
        
        final And condition = new And();
        condition.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
        condition.add(new In(SubscriberXInfo.SUBSCRIPTION_TYPE, subscriptionTypes.keySet()));
        condition.add(new In(SubscriberXInfo.STATE, STATES));
        List<Long> failedList = null;
        try
        {
            Collection<Subscriber> poolSubscriptions = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, condition);
            for (Subscriber sub : poolSubscriptions)
            {
                final Long subTypeID = Long.valueOf(sub.getSubscriptionType());
                try
                {
                    if (!EnumStateSupportHelper.get(ctx).stateEquals(sub, SubscriberStateEnum.ACTIVE))
                    {
                        SubscriptionPoolProperty prop = subscriptionTypes.get(subTypeID);
                        isExtensionUpdated = true;
                        sub = activatePoolSubscription(ctx, sub, prop);
                    }
                    poolSubscriptions(ctx, account, subTypeID);
                }
                catch (Exception e)
                {
                    final String errorMessage = FAILED_ACTIVATION_MSG + sub.getId() + ": " + e.getMessage();
                    handleGenericError(ctx, errorMessage, e);
                    if (failedList == null)
                    {
                        failedList = new ArrayList<Long>(subscriptionTypes.size());
                    }
                    failedList.add(subTypeID);
                }
            }
        }
        catch (HomeException e)
        {
            final String errorMessage = "Failed to retrieve pool Subscriptions: " + e.getMessage();
            handleGenericError(ctx, errorMessage, e);
            failedList = new ArrayList<Long>(subscriptionTypes.keySet());
        }
        if (failedList != null)
        {
            final StringBuilder msg = new StringBuilder(FAILED_ACTIVATION_MSG);
            msg.append("for the following subscription types: ");
            Iterator<Long> iter = failedList.iterator();
            msg.append(iter.next());
            while (iter.hasNext())
            {
                Long id = iter.next();
                msg.append(", ");
                msg.append(id);
            }
            msg.append(".");
            throw new ExtensionInstallationException(msg.toString(), isExtensionUpdated);
        
        }        
        return isExtensionUpdated;
    }


    private boolean adjustAllPoolSubscriptions(final Context ctx, final Account account,
            final Map<Long, SubscriptionPoolProperty> subscriptionTypes) throws ExtensionInstallationException
    {
        // This variable should be set if any of the SubscriptionPoolProperty objects are changed
        boolean isExtensionUpdated = false;
        if (null == subscriptionTypes || subscriptionTypes.isEmpty())
        {
            return isExtensionUpdated;
        }
        
        final And condition = new And();
        condition.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
        condition.add(new In(SubscriberXInfo.SUBSCRIPTION_TYPE, subscriptionTypes.keySet()));
        condition.add(new In(SubscriberXInfo.STATE, STATES));
        condition.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        condition.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.ACTIVE));
        List<Long> failedList = null;
        try
        {
            final SubscriberProfileProvisionClient bmClient_ = BalanceManagementSupport
            .getSubscriberProfileProvisionClient(ctx);
            if (bmClient_ == null)
            {
                throw new ExtensionInstallationException("Could not get reference to getSubscriberProfileProvision BM Service", isExtensionUpdated);
            }
            
            Collection<Subscriber> poolSubscriptions = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, condition);
            for (Subscriber sub : poolSubscriptions)
            {
                try
                {                    
                    final long newBalanceLimit = subscriptionTypes.get(sub.getSubscriptionType())
                    .getInitialPoolBalance();
                    sub.setCreditLimit(newBalanceLimit);
                    sub = HomeSupportHelper.get(ctx).storeBean(ctx, sub);                    
                }
                catch (Exception e)
                {
                    final String message = FAILED_UDPATE_MSG + sub.getId() + ": " + e.getMessage();
                    handleGenericError(ctx, message, e);
                    if (failedList == null)
                    {
                        failedList = new ArrayList<Long>(subscriptionTypes.size());
                    }
                    failedList.add(sub.getSubscriptionType());
                }
            }
        }
        catch (HomeException e)
        {
            final String message = "Failed to retrieve pool subscriptions: " + e.getMessage();
            handleGenericError(ctx, message, e);
            failedList = new ArrayList<Long>(subscriptionTypes.keySet());
        }
        if (failedList != null)
        {
            final StringBuilder msg = new StringBuilder(FAILED_ACTIVATION_MSG);
            msg.append("for the following subscription types: ");
            Iterator<Long> iter = failedList.iterator();
            msg.append(iter.next());
            while (iter.hasNext())
            {
                Long id = iter.next();
                msg.append(", ");
                msg.append(id);
            }
            msg.append(".");
            throw new ExtensionInstallationException(msg.toString(), isExtensionUpdated);
        }
        
        return isExtensionUpdated;
    }


    private boolean removeAllPoolSubscriptions(final Context ctx, final Account account,
            final Map<Long, SubscriptionPoolProperty> subscriptionTypes) throws ExtensionInstallationException
    {
        // This variable should be set if any of the SubscriptionPoolProperty objects are changed
        boolean isExtensionUpdated = false;
        if (null == subscriptionTypes || subscriptionTypes.isEmpty())
        {
            return isExtensionUpdated;
        }
        
        DefaultExceptionListener exceptionListner = new DefaultExceptionListener();
        try
        {
            final And condition = new And();
            condition.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
            condition.add(new In(SubscriberXInfo.SUBSCRIPTION_TYPE, subscriptionTypes.keySet()));
            condition.add(new In(SubscriberXInfo.STATE, STATES));
            final Home subHome = (Home) ctx.get(SubscriberHome.class);
            final Home accountHome = (Home) ctx.get(AccountHome.class);
            final SubscriberProfileProvisionClient bmClient_ = BalanceManagementSupport
                    .getSubscriberProfileProvisionClient(ctx);
            if (bmClient_ == null || subHome == null || accountHome == null)
            {
                throw new ExtensionInstallationException(
                        "Can not perform removal of Pool. Check service and store status", isExtensionUpdated);
            }
            subHome.forEach(ctx, new RemoveSubscriptionPoolVisitor(subHome, accountHome, bmClient_, exceptionListner),
                    condition);
        }
        catch (HomeException e)
        {
            // TODO Auto-generated catch block
            final String errorMessage = "Pool Removal Failed. Could not fetch subscription pools under Account with BAN="
                    + account.getBAN() + " becsuse of the following error: " + e.getMessage();
            handleGenericError(ctx, errorMessage, e);
        }
        finally
        {
            handleExceptionList(ctx, exceptionListner.getExceptions(), "Remove Subscription(s) operation failed");
        }
        
        return isExtensionUpdated;
    }


    private void poolSubscriptions(final Context ctx, final Account account, final Long subscriptionType)
    {
        SubscriberProfileProvisionClient bmClient_ = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        DefaultExceptionListener exceptionListner = new DefaultExceptionListener();
        PoolSubscriberAccountsVisitor visitor = new PoolSubscriberAccountsVisitor(bmClient_, subscriptionType, account
                .getBAN(), exceptionListner);
        final Home accountHome = (Home) ctx.get(AccountHome.class);
        try
        {
            final And condition = new And();
            condition.add(new EQ(AccountXInfo.PARENT_BAN, account.getBAN()));
            condition.add(new EQ(AccountXInfo.RESPONSIBLE, Boolean.FALSE));
            condition.add(new Not(new In(AccountXInfo.STATE, PoolSubscriberAccountsVisitor.DISABLED_STATES)));
            condition.add(new EQ(AccountXInfo.GROUP_TYPE, GroupTypeEnum.SUBSCRIBER));
            visitor = (PoolSubscriberAccountsVisitor) accountHome.forEach(ctx, visitor, condition);
        }
        catch (HomeException e)
        {
            final String errorMessage = "Unable to pool all subscriptions under Account '" + account.getBAN()
                    + "' for subscription type '" + subscriptionType + "' becsuse of the following error: " + e.getMessage();
            handleGenericError(ctx, errorMessage, e);
        }
        finally
        {
            handleExceptionList(ctx, exceptionListner.getExceptions(), "Pool Subscription(s) Operation failed: ");
        }
        if (visitor.getAcumulatedBalance() > 0)
        {
            String poolMSISDN = account.getPoolMSISDN();
            try
            {
                Subscriber poolSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, poolMSISDN, subscriptionType,
                        new Date());
                Parameters subscription = BalanceManagementSupport.getSubscription(ctx, this, bmClient_, poolSub);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Increasing Pool Balance for " + poolSub.getId() + "from "
                            + subscription.getBalance() + " by " + visitor.getAcumulatedBalance());
                }
                bmClient_.updateBalance(ctx, poolSub, subscription.getBalance() + visitor.getAcumulatedBalance());
            }
            catch (Exception e)
            {
                final String errorMessage = "Unable to credit Pool Subscription '" + poolMSISDN
                        + "': " + e.getMessage();
                handleGenericError(ctx, errorMessage, e);
            }
        }
    }


    private void syncronizeServiceBundles(Context ctx, Account account)
    {
        if (!isPoolBundleMapSetDirty_)
        {
            new DebugLogMsg(this, "No pool bundles are there to be provisioned", null).log(ctx);
            return;
        }
        else
        {
            isPoolBundleMapSetDirty_ = false;
            // the thread that sets this to false will take on the responsibility to
            // synchrionize
            try
            {
                final Home subHome = (Home) ctx.get(SubscriberHome.class);
                Subscriber airtimePoolSub = getPoolSubscription(ctx, SubscriptionTypeEnum.AIRTIME);
                if (null == airtimePoolSub)
                {
                    final String message = "Could not provision airtime Bundles. Please create a valid Airtime Pool under Account '"
                            + this.getBAN() + "' before bundles can be provisioned.";
                    handleGenericError(ctx, message, null);
                }
                else
                {
                    // forget about the delta of bundles to be provisioned
                    // the Subscriber Pipeline will handle
                    airtimePoolSub.setBundles(poolBundles_);
                    subHome.store(ctx, airtimePoolSub);
                }
            }
            catch (Exception e)
            {
                final String message = "Could not provision Bundles to Airtime Pool under Account [%s]. Error [%s]";
                handleGenericError(ctx, String.format(message, this.getBAN(), e.getMessage()), e);
            }
        }
    }


    private Subscriber getPoolSubscription(Context ctx, SubscriptionTypeEnum subscriptionType) throws HomeException
    {
        final And condition = new And();
        final long airtimeSubscriptionId = SubscriptionType.getSubscriptionType(ctx, subscriptionType).getId();
        condition.add(new EQ(SubscriberXInfo.BAN, this.getBAN()));
        condition.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, airtimeSubscriptionId));
        condition.add(new In(SubscriberXInfo.STATE, STATES));
        return (Subscriber) ((Home) ctx.get(SubscriberHome.class)).find(ctx, condition);
    }
    
    
    private Collection<Subscriber> getPoolSubscriptions(Context ctx) throws HomeException
    {
        final And condition = new And();
        condition.add(new EQ(SubscriberXInfo.BAN, this.getBAN()));
        condition.add(new In(SubscriberXInfo.STATE, STATES));
        return ((Home) ctx.get(SubscriberHome.class)).select(ctx, condition);
    }


    private Map<Long, SubscriptionPoolProperty> getOldSubscriptionPoolProperties(final Context ctx)
    {
        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        final PoolExtension oldExt = (PoolExtension) oldAccount.getFirstAccountExtensionOfType(PoolExtension.class);
        return oldExt.getSubscriptionPoolProperties();
    }


    private List<PoolPropertyExeption> handleExceptionList(Context ctx, List errors, String module)
    {
        List<PoolPropertyExeption> poolPropertyExceptionList = new ArrayList<PoolPropertyExeption>();
        for (Object t : errors)
        {
            if (t instanceof PoolPropertyExeption)
            {
                poolPropertyExceptionList.add((PoolPropertyExeption) t);
            }
            if (t instanceof Throwable)
            {
                handleGenericError(ctx, module, (Throwable) t);
            }
            else
            {
                FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new IllegalStateException(
                        "Unkown Pool Installation error of type [" + t.getClass().getName() + "]"));
            }
        }
        return poolPropertyExceptionList;
    }


    private void handleGenericError(Context ctx, String message, Throwable cause)
    {
        String errorMessage = new StringBuilder().append(((message == null) ? "" : message)).append(
                ". Reason-or-Cause [").append((cause == null) ? "Unknown / Not-Applicable" : cause.getMessage())
                .append("]").toString();
        LogSupport.minor(ctx, this, errorMessage);
        LogSupport.debug(ctx, this, errorMessage, cause);
        FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new IllegalStateException(message, cause));
    }

    class PoolPropertyExeption extends IllegalStateException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;


        PoolPropertyExeption(String message, Subscriber pool, SubscriptionPoolProperty poolProperty, Throwable cause)
        {
            super(message, cause);
            pool_ = pool;
            poolProperty_ = poolProperty;
        }


        Subscriber getPool()
        {
            return pool_;
        }


        SubscriptionPoolProperty getSubsriptionPoolProperty()
        {
            return poolProperty_;
        }

        SubscriptionPoolProperty poolProperty_;
        Subscriber pool_;
    }

    public static final Set<SubscriberStateEnum> STATES = Collections.unmodifiableSet(new HashSet<SubscriberStateEnum>(
            Arrays.asList(
                    SubscriberStateEnum.PENDING, 
                    SubscriberStateEnum.ACTIVE, 
                    SubscriberStateEnum.AVAILABLE,
                    SubscriberStateEnum.SUSPENDED,
                    SubscriberStateEnum.LOCKED,
                    SubscriberStateEnum.EXPIRED,
                    SubscriberStateEnum.NON_PAYMENT_WARN,
                    SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                    SubscriberStateEnum.PROMISE_TO_PAY,
                    SubscriberStateEnum.IN_ARREARS,
                    SubscriberStateEnum.IN_COLLECTION,
                    SubscriberStateEnum.DORMANT     
            )
    ));


    @Override
    public Object clone() throws CloneNotSupportedException
    {
        // TODO Auto-generated method stub
        PoolExtension cln = (PoolExtension) super.clone();
        // it is important that clone does not treat the bunleMapDirty
        // Otherwise if the bean get's stuck in cache, all the hits will show as if they
        // are dirty
        cln.isPoolBundleMapSetDirty_ = true;
        cln.subscriptionPoolProperties_ = null;
        return cln;
    }
    
    public static BundleFee transformBundle(Context ctx, BundleProfile bundleProfile)
    {
        final BundleFee poolBundleFee;
        {
            poolBundleFee = new BundleFee();
            poolBundleFee.setBundleProfile(bundleProfile);
            poolBundleFee.setServicePeriod(bundleProfile.getChargingRecurrenceScheme());
            poolBundleFee.setId(bundleProfile.getBundleId());
            poolBundleFee.setStartDate(new Date());
            
        }
        return poolBundleFee;
    }

    
    public static Set<BundleFee> transformBundles(Context ctx, Collection<BundleProfile> bundleProfiles)
    {
        final Set<BundleFee> poolBundles = new HashSet<BundleFee>();
        for (BundleProfile bundleProfile : bundleProfiles)
        {
            poolBundles.add(transformBundle(ctx, bundleProfile));
        }
        return poolBundles;
    }

    
    public static Map<Long,BundleFee> transformBundles(Context ctx,ExceptionListener excl, Long[] bundleIDs)
    {
        final HomeSupport homeSupport = HomeSupportHelper.get(ctx);
        excl = (null ==excl )? (ExceptionListener)ctx.get(ExceptionListener.class) : excl;
        final Map <Long, BundleFee> poolBundles;
        {
            poolBundles  = new HashMap<Long, BundleFee>();
            for(long bundleID : bundleIDs )
            {
                try
                {
                    BundleProfile bundleProfile = homeSupport.findBean(ctx, BundleProfile.class, bundleID);
                    if(null != bundleProfile)
                    {
                        poolBundles.put(bundleID, PoolExtension.transformBundle(ctx, bundleProfile));
                    }
                    
                }catch(Throwable t)
                {
                    if(excl != null)
                    {
                        excl.thrown(new IllegalStateException("Bundle ID [" + bundleID + "]. Error ["+ t.getMessage()+ "]",t));
                        if(LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(PoolExtension.class, "Exception on processing bundles", t).log(ctx);
                        }
                    }
                }
                
            }
        }
        return poolBundles;
    }
    
    @Override
    public Context getContext()
    {
        return ctx_;
    }


    @Override
    public void setContext(Context context)
    {
        ctx_ = context;
    }



    public static final String FAILED_MSG = "Unable to create pool subscription ";
    public static final String FAILED_ACTIVATION_MSG = "Unable to activate pool subscription ";
    public static final String FAILED_UDPATE_MSG = "Unable to update pool subscription ";
    private volatile boolean isPoolBundleMapSetDirty_ = false;
    private Context ctx_;
    
}
