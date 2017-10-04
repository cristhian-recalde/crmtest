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
package com.trilogy.app.crm.bean;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.omg.CORBA.LongHolder;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bas.recharge.SubscriberSuspendedEntities;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.bean.usage.BalanceUsage;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.contract.SubscriptionContractSupport;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionBANAdapter;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.auxiliaryservice.core.DiscountAuxSvcExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberAdvancedFeatures;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.ff.BlacklistWhitelistPLPSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.refactoring.ServiceRefactoring_RefactoringClass;
import com.trilogy.app.crm.resource.ResourceDevice;
import com.trilogy.app.crm.resource.ResourceDeviceXInfo;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.support.TopologySupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.app.crm.web.control.BlacklistWhitelistEntriesWebControl;
import com.trilogy.app.crm.xhome.CustomEnumCollection;
import com.trilogy.app.crm.xhome.auth.PrincipalAware;
import com.trilogy.product.s2100.ErrorCode;
import com.trilogy.util.partitioning.xhome.support.MsisdnAware;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.writeoff.WriteOffSupport;
/**
 * Removed custom bean code from the Subscriber entity.
 * 
 * @author paul.sperneac@redknee.com
 */
public class Subscriber extends AbstractSubscriber implements PrincipalAware, LazyLoadBean, MsisdnAware, SupplementaryDataAware
{
	private int clctopertionType;
	private Map<Long, Properties> removedServiceListOnPPChange = null;
	private Map<Long, Properties> replaceIdForPPChange = null;
	
	/**
	 * Returns the service id which is replacing the service id passesed as argument
	 * @param key : Service id
	 * @return
	 */
	public Properties getReplacePropForPPChange(Long key) {
		if(null != replaceIdForPPChange){
			return replaceIdForPPChange.get(key);
		}
		return null;
	}
	
    public int getCLCTOpertionType() {
		return clctopertionType;
	}

	public void setCLCTOpertionType(int clctopertionType) {
		this.clctopertionType = clctopertionType;
	}

	public Subscriber()
    {

    }

    @Override
    public Context getContext()
    {
        if (context_ == null)
        {
            return ContextLocator.locate();
        }
        return context_;
    }

    public Context getContextInternal()
    {
        return context_;
    }

    @Override
    public void setContext(final Context context)
    {
        context_ = context;

        // This is so that ContextFactories will have
        // access to this Subscriber.
        // getContext().put(Subscriber.class, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lazyLoad(Context ctx, PropertyInfo property)
    {
        if (property != null)
        {
            if (isFrozen())
            {
                new InfoLogMsg(this, "Unable to lazy-load " + property.getBeanClass().getSimpleName() + "."
                    + property.getName() + " because subscription " + this.getId() + " is frozen.", null).log(ctx);
            }
            else if (property.getBeanClass().isAssignableFrom(this.getClass()))
            {
                PMLogMsg pm = new PMLogMsg(LazyLoadBean.class.getName(), this.getClass().getSimpleName() + ".lazyLoad("
                    + property.getName() + ")");
                try
                {
                    property.get(this);
                    return true;
                }
                catch (Throwable t)
                {
                    ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                    if (el != null)
                    {
                        el.thrown(new IllegalPropertyArgumentException(property, t.getMessage()));
                    }
                    new MinorLogMsg(this, "Error occured lazy-loading " + property.getBeanClass().getSimpleName() + "."
                        + property.getName() + ": " + t.getMessage(), t).log(ctx);
                }
                finally
                {
                    pm.log(ctx);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lazyLoadAllProperties(Context ctx)
    {
        PMLogMsg pm = new PMLogMsg(LazyLoadBean.class.getName(), Subscriber.class.getSimpleName()
            + ".lazyLoadAllProperties()");
        
        Context sCtx = ctx.createSubContext();
        
        String sessionKey = CalculationServiceSupport.createNewSession(sCtx);
        try
        {
            lazyLoad(sCtx, SubscriberXInfo.SUB_EXTENSIONS);
            lazyLoad(sCtx, SubscriberXInfo.REAL_TIME_BALANCE);
            lazyLoad(sCtx, SubscriberXInfo.INTENT_TO_PROVISION_SERVICES);
            lazyLoad(sCtx, SubscriberXInfo.MONTH_TO_DATE_BALANCE);
            lazyLoad(sCtx, SubscriberXInfo.MONTH_TO_DATE_BALANCE_WITHOUT_PAYMENT_PLAN);
            lazyLoad(sCtx, SubscriberXInfo.AMOUNT_OWING);
            lazyLoad(sCtx, SubscriberXInfo.AMOUNT_OWING_WITHOUT_PAYMENT_PLAN);
            lazyLoad(sCtx, SubscriberXInfo.LAST_INVOICE_AMOUNT);
            lazyLoad(sCtx, SubscriberXInfo.ADJUSTMENTS_SINCE_LAST_INVOICE);
            lazyLoad(sCtx, SubscriberXInfo.PAYMENT_SINCE_LAST_INVOICE);
            lazyLoad(sCtx, SubscriberXInfo.ABM_CREDIT_LIMIT);
            lazyLoad(sCtx, SubscriberXInfo.BALANCE_REMAINING);
            lazyLoad(sCtx, SubscriberXInfo.OVERDRAFT_BALANCE);
            lazyLoad(sCtx, SubscriberXInfo.BLOCKED_BALANCE);
            lazyLoad(sCtx, SubscriberXInfo.SUBSCRIPTION_CONTRACT);

            /*
             * The following are lazy loaded but do not use the property's
             * setter, so they are allowed to change a frozen bean. This is
             * probably not good, and should be fixed once we have a solid
             * lazy-load strategy in general.
             */
            lazyLoad(sCtx, SubscriberXInfo.CURRENCY);
            lazyLoad(sCtx, SubscriberXInfo.DEPOSIT);
            lazyLoad(sCtx, SubscriberXInfo.CREDIT_LIMIT);
            lazyLoad(sCtx, SubscriberXInfo.AUXILIARY_SERVICES);
            lazyLoad(sCtx, SubscriberXInfo.FUTURE_AUXILIARY_SERVICES);
            lazyLoad(sCtx, SubscriberXInfo.PERSONAL_LIST_PLAN_ENTRIES);
            lazyLoad(sCtx, SubscriberXInfo.SUSPENDED_SERVICES);
            lazyLoad(sCtx, SubscriberXInfo.SUSPENDED_BUNDLES);
            lazyLoad(sCtx, SubscriberXInfo.SUSPENDED_AUX_SERVICES);
            lazyLoad(sCtx, SubscriberXInfo.SUSPENDED_PACKAGES);

            /*
             * The following are lazy-loaded, but not using a model defined
             * property (i.e. local variable) These changes are also not
             * prevented when the bean is frozen. Again, probably not good and
             * should be fixed once we have a solid lazy-load strategy.
             */
            this.getServices(sCtx);
            this.getGroupMSISDN(sCtx);
            //this.getPoolID(sCtx);
            this.getRawPricePlanVersion(sCtx);
            this.getServicesBackup(sCtx);
            this.getElegibleForProvision(sCtx);

            Iterator<ServiceTypeEnum> iterator = ServiceTypeEnum.COLLECTION.iterator();
            while (iterator.hasNext())
            {
                ServiceTypeEnum serviceType = iterator.next();
                this.getServices(sCtx, serviceType);
                this.getIntentToProvisionServices(sCtx, serviceType);
            }

            return true;
        }
        catch (Throwable t)
        {
            ExceptionListener el = (ExceptionListener) sCtx.get(ExceptionListener.class);
            if (el != null)
            {
                el.thrown(t);
            }
            new MinorLogMsg(this, "Error occured lazy-loading properties for subscription " + this.getId() + ": "
                + t.getMessage(), t).log(sCtx);
        }
        finally
        {
            CalculationServiceSupport.endSession(sCtx, sessionKey);
            pm.log(sCtx);
        }

        return false;
    }

    @Override
    public void setParent(final Object parent)
    {
        setBAN((String) parent);
    }

    @Override
    public Object getParent()
    {
        return getBAN();
    }

    public SubscriberStateEnum getStateWithExpired()
    {
        SubscriberStateEnum state = super.getState();
        if (isPrepaid() && SubscriberStateEnum.ACTIVE_INDEX == state.getIndex()
                && getExpiryDate().after(NEVER_EXPIRE_CUTOFF_DATE)
                && getExpiryDate().before(new Date()))
        {
            state = SubscriberStateEnum.EXPIRED;
        }

        return state;
    }

    public void setExceptionList(List<Throwable> exceptionList)
    {
        exceptionList_ = exceptionList;
    }

    public List<Throwable> getExceptionList()
    {
        return exceptionList_;
    }

    public void setExceptionListener(HTMLExceptionListener exceptionListener)
    {
        exceptionListener_ = exceptionListener;
    }

    public HTMLExceptionListener getExceptionListener()
    {
        return exceptionListener_;
    }

    public boolean isSubscriberIdSet()
    {
        boolean isDefaultValue = (id_ == null || id_.trim().length() == 0 || id_.equals(AbstractSubscriber.DEFAULT_ID) || id_
            .startsWith(MoveConstants.DEFAULT_MOVE_PREFIX));
        return !isDefaultValue;
    }

    public synchronized String getPoolID(final Context ctx)
    {
        if (poolID_ == null)
        {
            final Account account = SubscriberSupport.lookupAccount(ctx, this.getBAN());

            if (account != null && account.isIndividual(ctx) && !account.isResponsible())
            {
                poolID_ = account.getPoolID(ctx, this.getSubscriptionType());
            }
            else
            {
                /*
                 * ABM now requires that the group MSISDN be blank for
                 * non-pooled accounts Old behavior: groupMSISDN =
                 * account.getMSISDN();
                 */
                poolID_ = "";
            }
        }

        return poolID_;
    }

    
    /**
     * This bean field is auto-set and persisted
     */
    @Override
    public String getPoolID()
    {
        final Context ctx = ContextLocator.locate();
        if (null != ctx && (!ctx.getBoolean(Common.DURING_MIGRATION, false)))
        {
            if (null != ctx)
            {
                poolID_ = getPoolID(ctx);
            }
        }
        return poolID_;
    }
    
    /**
     * This bean field is auto-set and persisted. Setter just would not set anything unless it is a migration
     */
    @Override
    public void setPoolID(String poolID)
    {
        
        final Context ctx = ContextLocator.locate();
        if (null != ctx && ctx.getBoolean(Common.DURING_MIGRATION, false) )
        {
            //only for migration should this be set
            poolID_ = poolID;                   
        }
        // this needs to be done because fw-XDB does not differentiate between "" and null
        // when it comes to bean String fields. I reads both from VARCHAR2 as "" into
        // strings
    }
    
    
    
    public void resetPoolID()
    {
        poolID_ = null;
    }
    
    
    
    

    public synchronized String getGroupMSISDN(final Context ctx)
    {
        if (poolMSISDN_ == null && this.getBAN() != null && this.getBAN().length() > 0)
        {
            final Account account = SubscriberSupport.lookupAccount(ctx, this.getBAN());

            if (account != null && account.isIndividual(ctx) && !account.isResponsible())
            {
                poolMSISDN_ = account.getGroupMSISDN(ctx, this.getSubscriptionType());
            }
            else
            {
                // this probably should be set to subscription MSISDN to
                // accommodate for ECP, SMSB and others
                poolMSISDN_ = "";
            }
        }

        return poolMSISDN_;
    }

    public synchronized void clearGroupMSISDN()
    {
        poolID_ = null;
        poolMSISDN_ = null;
    }

    public String getSupportMSISDN(Context ctx)
    {
        String result = "";
        if (ctx != null)
        {
            if (super.getSubExtensions()==null)
            {
                if (this.getId()!=null && !this.getId().isEmpty())
                {
                    try
                    {
                        PPSMSupporteeSubExtension ppsmSupportee = PPSMSupporteeSubExtension.getPPSMSupporteeSubscriberExtension(ctx, this.getId());
                        if (ppsmSupportee!=null)
                        {
                            result = ppsmSupportee.getSupportMSISDN();
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Unable to retrieve PPSM Supportee extension: " + e.getMessage(), e);
                    }
                }
            }
            else
            {
                List<ExtensionHolder> subExtensions = getSubExtensions(ctx);
                for (ExtensionHolder extensionHolder : subExtensions)
                {
                    if (extensionHolder != null && extensionHolder.getExtension() instanceof PPSMSupporteeSubExtension)
                    {
                        result = ((PPSMSupporteeSubExtension) extensionHolder.getExtension()).getSupportMSISDN();
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    public String getSupportMSISDN()
    {
        return getSupportMSISDN(getContext());
    }

    /**
     * Check if the HTTP request is a result of a price plan selection preview
     * page reload
     * 
     * @return true if screen refresh is due to a price plan related change in
     *         GUI.
     */
    public static boolean isFromWebNewOrPreviewOnPriceplan(final Context ctx)
    {
        if (ctx != null)
        {
            final HttpServletRequest req = (HttpServletRequest) ctx.get(HttpServletRequest.class);

            // If a preview is occurring as a result of a price plan selection
            // set the deposit and credit limit to that of the price plan
            if (req != null
                && (WebController.isCmd("New", req) || WebController.isCmd("Preview", req)
                    && req.getParameter("PreviewButtonSrc") != null
                    && (req.getParameter("PreviewButtonSrc").indexOf(".pricePlan") != -1 || req.getParameter(
                        "PreviewButtonSrc").indexOf(".satId") != -1)))
            {
                // change of SAT causes change of Price Plan. We have to react
                // to this as well.
                return true;
            }
        }

        return false;
    }

    /**
     * Adds currently subscribed auxiliary bundles to a provided Map.
     * 
     * @param ctx
     * @param bundles
     */
    public void fillInSubscribedAuxiliaryBundlesOnly(final Context ctx, final Map bundles)
    {
        final Map allBundles = SubscriberBundleSupport.getAllBundles(ctx, this);
        final Iterator it = allBundles.entrySet().iterator();
        while (it.hasNext())
        {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object key = entry.getKey();
            final BundleFee bundle = (BundleFee) entry.getValue();
            if (bundle.isAuxiliarySource())
            {
                bundles.put(key, bundle);
            }
        }
    }

    /**
     * Selects the mandatory and Default bundles from the current Price Plan.
     * Adds auxiliary bundles provided in a Map to currently subscribed
     * auxiliary. The provided Map is cleared of Bundles that are now in the
     * Price Plan. Used in tandem with fillInSubscribedAuxiliaryBundlesOnly()
     * 
     * @param ctx
     *            the operating context
     * @param auxBundles
     */
    public void selectDefaultAndMandatoryBundlesAndAddAuxiliary(final Context ctx, final Map auxBundles)
    {
        if (pricePlan_ != DEFAULT_PRICEPLAN)
        {
            // clear out bundles that are no longer in the PP and are viewed as
            // auxiliary
            final Map newAux = new HashMap();
            fillInSubscribedAuxiliaryBundlesOnly(ctx, newAux);
            bundles_.keySet().removeAll(newAux.keySet());

            // clear remembered auxiliary bundles of bundles which are now in
            // the PP
            final Map ppBundles = SubscriberBundleSupport.getPricePlanBundles(ctx, this);
            auxBundles.keySet().removeAll(ppBundles.keySet());

            // clear bundles in Subscriber bundles that are not in the new Price
            // Plan
            // auxiliary bundles should be passed in the auxBundles parameter
            for (final Iterator iter = bundles_.keySet().iterator(); iter.hasNext();)
            {
                final Object key = iter.next();
                if (!ppBundles.containsKey(key))
                {
                    iter.remove();
                }
            }

            final Iterator it = ppBundles.entrySet().iterator();
            while (it.hasNext())
            {
                final Map.Entry entry = (Map.Entry) it.next();
                final Object key = entry.getKey();
                final BundleFee bundle = (BundleFee) entry.getValue();
                final ServicePreferenceEnum mode = bundle.getServicePreference();
                if (mode == ServicePreferenceEnum.MANDATORY || mode == ServicePreferenceEnum.DEFAULT)
                {
                    bundles_.put(key, bundle);
                }
            }

            getBundles().putAll(auxBundles);
        }
    }

    @Override
    public void setQuotaType(final QuotaTypeEnum quotaType) throws IllegalArgumentException
    {
        super.setQuotaType(quotaType);
        if (!SafetyUtil.safeEquals(QuotaTypeEnum.UNLIMITED_QUOTA, quotaType) && super.getQuotaLimit() == -1)
        {
            // Ensure that we don't accidentally allow quota limit = -1 when the
            // type is
            // not unlimited
            super.setQuotaLimit(0);
        }
    }

    @Override
    public long getQuotaLimit()
    {
        // We override this to make ABM provisioning easier.
        // ABM uses the -1 quota limit as a flag for unlimited.
        if (QuotaTypeEnum.UNLIMITED_QUOTA.equals(getQuotaType()))
        {
            return -1;
        }
        return super.getQuotaLimit();
    }

    /**
	  * Create a set SubscriberServices from the Mandatory Services selected in
	  * the raw Price Plan for the given subscriber.
	  * 
	  * @param ctx
	  *            The operating context.
	  * @param s
	  *            The subscriber being examined.
	  * @return A set of SubscriberServices the subscriber has subscribed to.
	  */
	 public Set<SubscriberServices> populateMandatoryServicesForProvisioning(final Context ctx)
	 {
		 // these are the existing services
		 //final Set<Long> services = this.getServices(ctx);
		 final Set<ServiceFee2ID> services = this.getServices(ctx);
		 //final Set<Long> mandatoryServices = new HashSet<Long>();
		 final Set<ServiceFee2ID> mandatoryServices = new HashSet<ServiceFee2ID>();

		 // applies the mandatory price plan services
		 try
		 {
			 final PricePlanVersion pp = this.getRawPricePlanVersion(ctx);
			 final Map c = pp.getServiceFees(ctx);

			 for (final Iterator it = c.keySet().iterator(); it.hasNext();)
			 {
				 final ServiceFee2 service = (ServiceFee2) c.get(it.next());
				 if (service != null && service.getServicePreference() == ServicePreferenceEnum.MANDATORY)
				 {
					 //services.add(Long.valueOf(service.getServiceId()));
					 //mandatoryServices.add(Long.valueOf(service.getServiceId()));
					 services.add(new ServiceFee2ID(service.getServiceId(), service.getPath()));
					 mandatoryServices.add(new ServiceFee2ID(service.getServiceId(), service.getPath()));
				 }
			 }

		 }
		 catch (final HomeException e)
		 {
			 if (LogSupport.isDebugEnabled(ctx))
			 {
				 new DebugLogMsg("[Subscriber.populateMandatoryServicesProvisioning]", e.getMessage(), e).log(ctx);
			 }
		 }

		 final Set<SubscriberServices> intentToProvisionServices = new HashSet<SubscriberServices>();
		 // converst the services into Display services
		 for (final Iterator<ServiceFee2ID> i = services.iterator(); i.hasNext();)
		 {
			 final ServiceFee2ID key = i.next();

			 final SubscriberServices ss = new SubscriberServices();
			 ss.setServiceId(key.getServiceId());
			 ss.setSubscriberId(getId());
			 ss.setPath(key.getPath());

			 if (mandatoryServices.contains(key))
			 {
				 ss.setMandatory(true);
			 }
			 else
			 {
				 ss.setMandatory(false);
			 }

			 ctx.put(Lookup.SPID, this.getSpid());
			 ss.setSubscriberServiceDates(ctx, new Date());
			 /*
			  * TT 5082122967: Feature does not send any HLR provisioning
			  * commands The Fix: In the past the Bulk Loader, the Bulk Create,
			  * and the Audi Tool were not required to send HLR commands. When
			  * creating subs manually, the DisplaySubscriberServicesWebControl
			  * usually takes care of setting the state of the service, as all
			  * services are defaulted to the Pending state. The state of the
			  * service is to change to Active on its StartDate. Since we have
			  * defaulted the start date to today, we should also default the
			  * state to Active. HLR provisioning commands are only sent out for
			  * provisioned services in the Active state. -Angie Li
			  */
			 ss.setProvisionedState(ServiceStateEnum.PROVISIONED);

			 intentToProvisionServices.add(ss);
		 }
		 setIntentToProvisionServices(intentToProvisionServices);
		 return intentToProvisionServices;
	 }

	 /**
	  * @param context
	  * @return
	  */
	 public Set<SubscriberServices> getUpdatedSubscriberServicesForIntentToProvisionSet(final Context context)
	{
		// service fees for the new price plan version
		final Map serviceFees = SubscriberServicesSupport.getServiceFees(context, this);
		final Map subscribed = SubscriberServicesSupport.getSubscribersServices(context, this.getId());
		final Set<ServiceFee2ID> serviceIds = this.getServices(context);
		final Set<SubscriberServices> subscriberServices = new HashSet<SubscriberServices>();

		for (final Iterator<ServiceFee2ID> iter = serviceIds.iterator(); iter.hasNext();) {
			final ServiceFee2ID key = iter.next();
			final ServiceFee2 fee = (ServiceFee2) serviceFees.get(key);

			// I have no idea why the subscriberservice should be over write
			// if it is mandatory. it is pretty tricky and doesn't make sense to
			// me.
			final SubscriberServices subService = (SubscriberServices) subscribed.get(key);

			if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY)
					|| (fee.getServicePreference() == ServicePreferenceEnum.DEFAULT
							&& fee.getServicePeriod() != ServicePeriodEnum.ONE_TIME)) {
				// if it's mandatory then we only worry about the id
				final SubscriberServices bean = new SubscriberServices();
				bean.setSubscriberId(this.getId());
				bean.setServiceId(fee.getServiceId());
				bean.setPath(fee.getPath());
				bean.setMandatory(true);
				if (subService != null) {
					bean.setProvisionedState(subService.getProvisionedState());
				} else {
					bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
				}

				subscriberServices.add(bean);
			} else {
				// since it's not mandatory we have to first check if it's a
				// service
				// being subscribed to in the previous price plan
				// if so we have to get the start and end date
				if (subService != null) {
					subService.setMandatory(false);
					subscriberServices.add(subService);
				}

			}
		}
		setIntentToProvisionServices(subscriberServices);
		return subscriberServices;
	}
	 
    public void populateSubscriberServices(final Context ctx, final boolean addDefault)
    {
        final Map serviceFees = SubscriberServicesSupport.getServiceFees(ctx, this);
        final Map subscribed = SubscriberServicesSupport.getSubscribersServices(ctx, this.getId());

        final Set subscriberServices = new HashSet();
        final Set subscribersServicesIdSet = new HashSet();
        for (final Iterator iter = serviceFees.entrySet().iterator(); iter.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iter.next();
            final Object key = entry.getKey();
            final ServiceFee2 fee = (ServiceFee2) entry.getValue();

            SubscriberServices bean = (SubscriberServices) subscribed.get(key);
            if (bean != null)
            {
                if (bean.isMandatory())
                {
                    // reset the end date for services that were mandatory.
                    // Mandatory did not hold the end date (bug)
                    bean.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE,
                        bean.getStartDate()));
                }

                bean.setMandatory(fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY));
                bean.setServicePeriod(fee.getServicePeriod());
                subscriberServices.add(bean);
                subscribersServicesIdSet.add(key);
            }
            else if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY)
                || (addDefault && fee.getServicePreference().equals(ServicePreferenceEnum.DEFAULT)))
            {
                // we only subscribe the mandatory services
                // now we have to subscribe default services as well
                bean = new SubscriberServices();
                bean.setSubscriberId(this.getId());
                bean.setServiceId(fee.getServiceId());
                bean.setProvisionedState(ServiceStateEnum.PROVISIONED);

                bean.setMandatory(fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY));
                bean.setSubscriberServiceDates(ctx, new Date());
                bean.setServicePeriod(fee.getServicePeriod());
                bean.setPath(fee.getPath());
                subscriberServices.add(bean);
                subscribersServicesIdSet.add(key);
            }
        }
        // It's important to sync the services set as well
        this.setServices(subscribersServicesIdSet);
        this.setIntentToProvisionServices(subscriberServices);
    }

    public void switchPricePlan(final Context ctx, final long pricePlan, final int version)
    {
        final Map bundles = new HashMap();
        this.fillInSubscribedAuxiliaryBundlesOnly(ctx, bundles);
        this.getBundles().keySet().removeAll(bundles.keySet());
        super.setPricePlan(pricePlan);
        setPricePlanVersion(version);

        this.populateSubscriberServices(ctx, true);
        this.selectDefaultAndMandatoryBundlesAndAddAuxiliary(ctx, bundles);
    }

    public void switchPricePlan(final Context ctx, final long pricePlan)
    {
        switchPricePlan(ctx, pricePlan, DEFAULT_PRICEPLANVERSION);
    }

    /**
     * Returns a PricePlanVersion object that corresponds to the subscribers
     * selected price plan version. If a specific version is not selected then
     * the current versoin of the price plan is returned. The object returned
     * can be a cached object so DO NOT modify the returned object. If you need
     * a PricePlanVersion that you can modify, try getPricePlan(). May return
     * null if no price plan is selected.
     * 
     * @param ctx
     *            The operation Context
     * @return PricePlanVersion is a price plan is selected, null otherwise.
     * @throws HomeException
     */
    public synchronized PricePlanVersion getRawPricePlanVersion(final Context ctx) throws HomeException
    {
        // reset the cachedPlanVersion_ if a different price plan is selected
        if (cachedPlanVersion_ == null || cachedPlanVersion_.getId() != getPricePlan()
            || cachedPlanVersion_.getId() == getPricePlan() && cachedPlanVersion_.getVersion() != getPricePlanVersion())
        {
            if (cachedPlanVersion_ != null && cachedPlanVersion_.getId() != getPricePlan())
            {
                // reset the version because a different price plan is selected
                setPricePlanVersion(AbstractSubscriber.DEFAULT_PRICEPLANVERSION);
            }

            if (getPricePlanVersion() == AbstractSubscriber.DEFAULT_PRICEPLANVERSION
                && getPricePlan() != AbstractSubscriber.DEFAULT_PRICEPLAN)
            {
                final Home home = (Home) ctx.get(PricePlanHome.class);
                if (home != null)
                {
                    PricePlan plan = (PricePlan) home.find(ctx, Long.valueOf(getPricePlan()));

                    if (plan == null)
                    {
                        // this means data integrity error. please do not guess
                        // or return a different price plan
                        // version, just let the caller deal with this ERROR!
                        return null;
                    }
                    else
                    {
                        setPricePlanVersion(plan.getCurrentVersion());
                    }
                }
            }

            final Home pricePlanVersionHome = (Home) ctx.get(PricePlanVersionHome.class);

            if (pricePlanVersionHome != null)
            {
                cachedPlanVersion_ = (PricePlanVersion) pricePlanVersionHome.find(ctx, new PricePlanVersionID(
                    getPricePlan(), getPricePlanVersion()));
            }
        }

        return cachedPlanVersion_;
    }

    /**
     * returns a cloned PricePlanVersion object (deep cloned from raw PricePlan Object) 
     * with user selected services, plus the mandatory services.
     * 
     * @param ctx
     * @return
     * @throws HomeException
     */
    public synchronized PricePlanVersion getPricePlan(final Context ctx) throws HomeException
    {
        PricePlanVersion plan = null;
        try
        {
            final PricePlanVersion rawPlan = getRawPricePlanVersion(ctx);

            if (rawPlan != null)
            {
                plan = (PricePlanVersion) rawPlan.deepClone();
            }
        }
        catch (final Exception cloneEx)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Failed to clone raw PricePlan.", cloneEx).log(ctx);
            }

            plan = getRawPricePlanVersion(ctx);
        }

        // null is normal, it means that no price plan is selected.
        // Or it means data integrity error
        if (plan == null)
        {
            return null;
        }

        final Map map = new HashMap();

        // TODO plan.getServiceFees(ctx) returns the services in the PP Version
        // and
        // Service Packages from PPV
        // TODO this implementation does handle properly services in mandatory
        // packages
        final Map fees = plan.getServiceFees(ctx);
        if (fees != null)
        {
        	for (final Iterator i = fees.values().iterator(); i.hasNext();)
			 {
				 final ServiceFee2 fee = (ServiceFee2) i.next();
				 final ServiceFee2ID serviceFee2ID = new ServiceFee2ID(fee.getServiceId(), fee.getPath());

				 if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY || getServices(ctx).contains(serviceFee2ID))
				 {
					 map.put(serviceFee2ID, fee);
				 }
			 }
        }
        else
        {
            // assign empty map.
        }

        plan.setServiceFees(map);

        return plan;
    }

    @Override
    public SubscriberAdvancedFeatures getAdvancedFeatures()
    {
        if (advancedFeatures_ == null)
        {
            advancedFeatures_ = new SubscriberAdvancedFeatures();
            advancedFeatures_.setSpid(this.getSpid());
            advancedFeatures_.setBAN(this.getBAN());
            advancedFeatures_.setSubId(this.getId());
            advancedFeatures_.setContext(this.getContext());
        }
        return advancedFeatures_;
    }

    @Override
    public PropertyInfo getExtensionHolderProperty()
    {
        return SubscriberXInfo.SUB_EXTENSIONS;
    }

    @Override
    public Collection<Extension> getExtensions()
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>) getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get(getContext()).unwrapExtensions(holders);
    }


    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        return SubscriberSupport.getExtensionTypes(ctx, this.getSubscriberType());
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getSubExtensions(Context)
     *             } if possible
     */
    @Deprecated
    @Override
    public List getSubExtensions()
    {
        return getSubExtensions(getContext());
    }

    /**
     * Lazy loading extensions. {@inheritDoc}
     */
    public List getSubExtensions(Context ctx)
    {
        synchronized (this)
        {
            if (super.getSubExtensions() == null)
            {
                try
                {
                    // To avoid deadlock, use a subscription
                    // "with extensions loaded" along with extension loading
                    // adapter.
                    Subscriber subCopy = (Subscriber) this.clone();
                    subCopy.setSubExtensions(new ArrayList());

                    subCopy = (Subscriber) new ExtensionLoadingAdapter<SubscriberExtension>(SubscriberExtension.class,
                        SubscriberExtensionXInfo.SUB_ID).adapt(ctx, subCopy);
                    subCopy = (Subscriber) new ExtensionBANAdapter().adapt(ctx, subCopy);
                    subCopy = (Subscriber) new ExtensionSpidAdapter().adapt(ctx, subCopy);

                    this.setSubExtensions(subCopy.getSubExtensions(ctx));
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
        }

        return super.getSubExtensions();
    }

    public int compareTo(final Object obj)
    {
        final Subscriber other = (Subscriber) obj;

        return getId().compareTo(other.getId());
    }

    /**
     * Work around to prevent computed fields from being computed.
     * 
     * @param o
     * @return
     */
    @Override
    public boolean transientEquals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (o == null)
        {
            return false;
        }

        if (o.getClass() != getClass())
        {
            return false;
        }

        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        // XGen generated clone() does almost all that needs to be done, almost.
        final Subscriber cln = (Subscriber) super.clone();
        // clean the context because it may be a stale o wrong context
        cln.setContext(null);
        // we need a copy the Bundles Map in order to have 2 different data
        // structures
        // for 2 different subscriber objects
        cln.setBundles(deepCloneBundles(cln.getBundles()));
        // these values control lazy loading of language
        // the clone() should lazy load the language irrespective of what the
        // original had
        // it is very imp for FW-caching which returns a shallow clone on a hit
        cln.languageAsLastRetrieved_ = null;
        cln.isLanguageSetByAccessor_ = false;
        cln.resetBackupServices();
        cloneSubscriberExtensionList(cln);
        return cln;
    }

    private Map deepCloneBundles(final Map origBundles) throws CloneNotSupportedException
    {
        final HashMap newMap = new HashMap();
        for (final Iterator i = origBundles.values().iterator(); i.hasNext();)
        {
            final BundleFee origBundle = (BundleFee) i.next();
            final BundleFee newBundleFee = (BundleFee) origBundle.clone();
            newMap.put(Long.valueOf(newBundleFee.getId()), newBundleFee);
        }
        return newMap;
    }

    private Subscriber cloneSubscriberExtensionList(final Subscriber clone) throws CloneNotSupportedException
    {
        List subExtensions = super.getSubExtensions();
        if (subExtensions != null)
        {
            final List extentionList = new ArrayList(subExtensions.size());
            clone.setSubExtensions(extentionList);
            for (final Iterator it = subExtensions.iterator(); it.hasNext();)
            {
                extentionList.add(safeClone((XCloneable) it.next()));
            }
        }
        return clone;
    }

    /**
     * Return the OICK mapping for this subscriber.
     * 
     * @param ctx
     *            The operating context.
     * @param oickCommandIndex
     *            The index indicating whether a provision or an unprovision
     *            OICK command is expected.
     * @return String The OICK mapping.
     */
    private String getOICK(final Context ctx, final boolean needProvisionCommand)
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException("Could not find object.  Context parameter is null.");
        }

        String defaultOICK = null;
        String oick = null;

        Iterator mappingItr = null;
        try
        {
            mappingItr = HomeSupportHelper.get(ctx).getBeans(ctx, 
                    OICKMapping.class, 
                    new EQ(OICKMappingXInfo.SUBSCRIBER_TYPE, getSubscriberType()),
                    true, OICKMappingXInfo.OICKMAPPING_ID).iterator();
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(this, "Failed to get data from OICKMappingHome [" + e.getMessage() + "].", e).log(ctx);
            return "";
        }

        int maxLengthMatched = 0;

        while (mappingItr.hasNext())
        {
            final OICKMapping mapping = (OICKMapping) mappingItr.next();

            final String msisdnMap = mapping.getMSISDNMap();
            if (needProvisionCommand)
            {
                if (defaultOICK == null && "*".equals(msisdnMap))
                {
                    defaultOICK = mapping.getProvisionOICK();
                }
                else if (msisdnMap.length() > maxLengthMatched && getMSISDN().startsWith(msisdnMap))
                {
                    oick = mapping.getProvisionOICK();
                    maxLengthMatched = msisdnMap.length();
                }
            }
            else
            {
                if (defaultOICK == null && "*".equals(msisdnMap))
                {
                    defaultOICK = mapping.getUnprovisionOICK();
                }
                else if (msisdnMap.length() > maxLengthMatched && getMSISDN().startsWith(msisdnMap))
                {
                    oick = mapping.getUnprovisionOICK();
                    maxLengthMatched = msisdnMap.length();
                }
            }
        }

        if (oick != null)
        {
            return oick;
        }
        else if (defaultOICK != null)
        {
            return defaultOICK;
        }
        else
        {
            return "";
        }
    }

    /**
     * Return the provision OICK mapping for this subscriber.
     * 
     * @param ctx
     *            The operating context.
     * @return String The provision OICK mapping.
     */
    public String getProvisionOICK(final Context ctx)
    {
        return getOICK(ctx, true);
    }

    /**
     * Return the unprovision OICK mapping for this subscriber.
     * 
     * @param ctx
     *            The operating context.
     * @return String The unprovision OICK mapping.
     */
    public String getUnprovisionOICK(final Context ctx)
    {
        return getOICK(ctx, false);
    }

    public void serviceProvisioned(Context ctx, final Service svc)
    {
        getProvisionedServices(ctx).add(svc.ID());
        getTransientProvisionedServices().add(svc.ID());

    }

    public void serviceUnProvisioned(Context ctx, final Service svc)
    {
        // TODO 2008-09-10 this does not work any more. removeal from
        // provisioned services needs to happen onthe table
        getProvisionedServices(ctx).remove(svc.ID());
        getTransientProvisionedServices().remove(svc.ID());
    }

    public boolean isServiceProvisioned(Context ctx, final long id)
    {
        return getProvisionedServices(ctx).contains(Long.valueOf(id));
    }

    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.isServiceSuspended(
     *             Context, id)} if possible
     * Returns TRUE if the given Service (id) is a suspended service for this
     * subscriber.
     * 
     * @param id
     *            Service Identifier
     * @return
     */
    @Deprecated
    public boolean isServiceSuspended(final long id)
    {
        return getSuspendedServices().get(Long.valueOf(id)) != null;
    }

    /**
     * Returns TRUE if the given Service (id) is a suspended service for this subscriber.
     * 
     * @param id
     *            Service Identifier
     * @return
     */
    public boolean isServiceSuspended(final Context ctx, final long id)
    {
        return getSuspendedServices(ctx).get(Long.valueOf(id)) != null;
    }

    public boolean isBundleProvisioned(final long id)
    {
        return getProvisionedBundles().contains(Long.valueOf(id));
    }

    public void bundleProvisioned(final BundleFee svc)
    {
        getProvisionedBundles().add(svc.ID());
    }

    public void bundleProvisioned(final Collection ids)
    {
        getProvisionedBundles().addAll(ids);
    }

    public void bundleUnProvisioned(final Collection ids)
    {
        getProvisionedBundles().removeAll(ids);
    }

    public void bundleUnProvisioned(final BundleFee svc)
    {
        getProvisionedBundles().remove(svc.ID());
    }

    public void resetProvisionedBundles()
    {
        provisionedBundles_ = new HashSet();
    }

    /**
     * Get the services expecting to be provisioned based on the price plan
     * selection, clct, service start date and end date TT5091424064: Mandatory
     * services are exempted from CLCT feature.
     * 
     * @param ctx
     * @return the service id collection
     */
    public Collection getProvisionedServicesExpected(final Context ctx) throws HomeException
    {
        return ServiceSupport.transformServiceObjectToIds(getProvisionSubServicesExpected(ctx));
    }


    public Collection<SubscriberServices> getProvisionSubServicesExpected(final Context ctx) throws HomeException
    {
        Collection<SubscriberServices> selectedServices = getElegibleForProvision(ctx);
        return computeProvisionServices(ctx, selectedServices);
    }

    /**
     * Compute the services that need to be provisioned. Takes into account
     * CLTC.
     * 
     * @param ctx
     * @param selectedServices
     * @return
     */
    public Collection<SubscriberServices> computeProvisionServices(final Context ctx,
            final Collection<SubscriberServices> selectedServices)
    {
        // if subscriber crosses its threshold then find out the clct services which are
        // below threshold and provision them
        if (isClctChange())
        {
            // If subscriber is below credit limit, exclude CLTC enabled services from the
            // services expected to be provisioned.
            Collection<SubscriberServices> clctServicesNotToProvision = SubscriberServicesSupport
                    .getCLCTServicesWithThresholdAboveBalance(ctx, selectedServices, getSubNewBalance());
            // Exclude CLCT services which have threshold greater than balance
            for (SubscriberServices service : clctServicesNotToProvision)
            {
                selectedServices.remove(service);
            }
        }
        else
        {
            // if subscriber threshold is not crossed, do not consider suspended clct
            // services for provisioning
            boolean isCLTCEnabled = true;
            Collection<SubscriberServices> clctServices = SubscriberServicesSupport.findSuspendedWithinListByCLTC(ctx,
                    selectedServices, isCLTCEnabled);
            // Exclude suspended CLCT services which have threshold greater than balance
            for (SubscriberServices service : clctServices)
            {
                selectedServices.remove(service);
            }
        }
        return selectedServices;
    }


    /**
     * Return the eligibible services in addition to the services selected (the intent) of
     * this subscriber.
     */
    public Collection<SubscriberServices> getElegibleForProvision(final Context ctx) throws HomeException
    {
        Collection<SubscriberServices> services = new ArrayList<SubscriberServices>();
        // This services plus the ones on the services for display
        Iterator<SubscriberServices> svcForDisplay = this.getIntentToProvisionServices(ctx).iterator();
        final long nowMillis = new Date().getTime();
        while (svcForDisplay.hasNext())
        {
            SubscriberServices svc = svcForDisplay.next();
            if (!services.contains(svc.getServiceId()) && svc.getStartDate().getTime() <= nowMillis)
            {
                services.add(svc);
            }
        }
        return services;
    }

    

    /**
     * This method returns all the subscriber's services that are CLTC enabled,
     * be they mandatory or non-mandatory. Note that as per HLD obj11833,
     * "Mandatory Services are exempt from the CLCT feature." As of 09/16/2005
     * this method was not used anywhere in CRM, so I didn't change this method
     * to reflect the HLD. -Angie Li
     * 
     * @param ctx
     * @return
     * @throws HomeException
     * @deprecated due to Service Refactoring changing the Service Suspension
     *             behavior
     */
    @Deprecated
    public Collection getClctAffectedServices(final Context ctx) throws HomeException
    {
        Collection list;
        if (this.isAboveCreditLimit())
        {
            final Collection selectedServices = ServiceSupport.transformServiceIdToObjects(ctx, getServices(ctx));
            list = CollectionSupportHelper.get(ctx).findAll(ctx, selectedServices,
                new EQ(ServiceXInfo.ENABLE_CLTC, Boolean.TRUE));
            list = ServiceSupport.transformServiceObjectToIds(list);
        }
        else
        {
            list = new ArrayList();
        }
        return list;
    }

    /**
     * Determine a service provisioning status
     * 
     * @param ctx
     * @param serviceId
     * @return
     * @throws HomeException
     */
    public ServiceProvisionStatusEnum getServiceStatus(final Context ctx, final Long serviceId) throws HomeException
    {
        ServiceProvisionStatusEnum status;

        SubscriberServices service = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, getId(), serviceId, SubscriberServicesUtil.DEFAULT_PATH);
        return getServiceStatus(ctx, service);
    }

    /**
     * Determine a service provisioning status
     * 
     * @param ctx
     * @param serviceId
     * @return
     * @throws HomeException
     * @TODO this method should be moved subscriber services
     */
    public ServiceProvisionStatusEnum getServiceStatus(final Context ctx, final SubscriberServices service)
        throws HomeException
    {
        ServiceProvisionStatusEnum status;

        if (service != null)
        {
            switch (service.getProvisionedState().getIndex())
            {
                case ServiceStateEnum.PROVISIONED_INDEX:
                    status = ServiceProvisionStatusEnum.PROVISIONED;
                    ServiceRefactoring_RefactoringClass.implementRealSubscriberServiceSuspendedDueToCLTC();
                    break;
                case ServiceStateEnum.PROVISIONEDWITHERRORS_INDEX:
                    status = ServiceProvisionStatusEnum.PROVISIONINGFAILED;
                    ServiceRefactoring_RefactoringClass.implementRealSubscriberServiceSuspendedDueToCLTC();
                    break;
                case ServiceStateEnum.UNPROVISIONEDWITHERRORS_INDEX:
                    status = ServiceProvisionStatusEnum.UNPROVISIONINGFAILED;
                    break;
                case ServiceStateEnum.SUSPENDED_INDEX:
                    status = getReasonForSuspension(service.getProvisionedState(), service.getSuspendReason());
                    break;
                case ServiceStateEnum.SUSPENDEDWITHERRORS_INDEX:
                    status = getReasonForSuspension(service.getProvisionedState(), service.getSuspendReason());
                    break;
                default: // Pending
                    status = ServiceProvisionStatusEnum.UNPROVISIONEDOK;
            }

        }
        else
        {
            status = ServiceProvisionStatusEnum.UNPROVISIONEDOK;
        }

        return status;
    }

    public boolean hasSuspended(Context ctx)
    {
        return (getSuspendedBundles(ctx).size() + getSuspendedServices(ctx).size() + getSuspendedAuxServices(ctx).size() + getSuspendedPackages(ctx)
            .size()) > 0;
    }

    /**
     * Returns the ServiceProvisionStatusEnum associated with the given
     * SuspendReasonEnum.
     * 
     * @param reason
     * @return
     */
    private ServiceProvisionStatusEnum getReasonForSuspension(ServiceStateEnum state, SuspendReasonEnum reason)
    {
        ServiceProvisionStatusEnum status = ServiceProvisionStatusEnum.SUSPENDED;
        if (state.equals(ServiceStateEnum.SUSPENDEDWITHERRORS))
        {
            status = ServiceProvisionStatusEnum.SUSPENDEDWITHERRORS;
            if (reason.equals(SuspendReasonEnum.CLCT))
            {
                status = ServiceProvisionStatusEnum.SUSPENDEDDUETOCLCTWITHERRORS;
            }
        }
        else if (reason.equals(SuspendReasonEnum.CLCT))
        {
            status = ServiceProvisionStatusEnum.SUSPENDEDDUETOCLCT;
        }
        return status;
    }

    public void resetBundles()
    {
        bundles_ = new HashMap();
    }

    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.isAccountIndividual(
     *             Context)} if possible
     */
    @Deprecated
    public boolean isAccountIndividual()
    {
        return isAccountIndividual(getContext());
    }

    /**
     * Checks if the associated account is individual. If the account could not
     * be found, returns false.
     * 
     * @param ctx
     * @return true if subscriber is in an individual account
     */
    public boolean isAccountIndividual(final Context ctx)
    {
        final Account account = SubscriberSupport.lookupAccount(ctx, this);
        if (account != null)
        {
            return account.isIndividual(ctx);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getCurrency(Context)} if
     *             possible
     */
    @Deprecated
    @Override
    public String getCurrency()
    {
        return getCurrency(getContext());
    }

    public String getCurrency(final Context ctx)
    {
        if (currency_ == null)
        {
            final Account account = SubscriberSupport.lookupAccount(ctx, this);
            if (account != null)
            {
                currency_ = account.getCurrency();
            }
        }
        return currency_;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getMonthToDateBalance(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public long getMonthToDateBalance()
    {
        return getMonthToDateBalance(getContext());
    }

    public long getMonthToDateBalance(Context ctx)
    {
        synchronized (this)
        {
            if (monthToDateBalance_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }
                
                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    monthToDateBalance_ = service.getSubscriberMDBalance(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching monthToDateBalance for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
                
            }
        }

        return monthToDateBalance_;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getBlockedBalance(Context
     *             )} if possible
     */
    @Deprecated
    @Override
    public long getBlockedBalance()
    {
        return getBlockedBalance(getContext());
    }

    public long getBlockedBalance(Context ctx)
    {
        synchronized (this)
        {
            if (blockedBalance_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                SubscriberSupport.updateSubscriptionBlockedBalance(ctx, this);
            }
        }

        return blockedBalance_;
    }
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getBlockedBalance(Context
     *             )} if possible
     */
    @Deprecated
    @Override
    public long getSubscriptionContract()
    {
        return getSubscriptionContract(getContext());

    }
    
    public long getSubscriptionContract(Context ctx)
    {
        synchronized (this)
        {
            if ((this.getId() != this.DEFAULT_ID)
                    && (super.getSubscriptionContract() == SubscriberSupport.INVALID_VALUE || super
                            .getSubscriptionContract() == -1))
            {
                this.subscriptionContract_ = -1;
                try
                {
                    final com.redknee.app.crm.contract.SubscriptionContract contract = HomeSupportHelper.get(ctx)
                            .findBean(
                                    ctx,
                                    com.redknee.app.crm.contract.SubscriptionContract.class,
                                    new EQ(com.redknee.app.crm.contract.SubscriptionContractXInfo.SUBSCRIPTION_ID, this
                                            .getId()));
                    if (contract != null)
                    {
                        SubscriptionContractTerm term = SubscriptionContractSupport.getSubscriptionContractTerm(ctx,
                                contract);
                        this.setSubscriptionContract(contract.getContractId());
                        this.setSubscriptionContractEndDate(contract.getContractEndDate());
                        this.setSubscriptionContractStartDate(contract.getContractStartDate());
                        this.setDaysRemainingInTerm(CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(new Date(),
                                contract.getContractEndDate()));
                        this.setCurrentCancellationCharges(SubscriptionContractSupport.getCurrentPenaltyFee(ctx, this,
                                contract, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()), term.getProrateCancellationFees()));
                    }
                }
                catch (HomeException homeEx)
                {
                    new MinorLogMsg(this, "Unable to find subcription contract ", homeEx).log(ctx);
                }
            }
        }
        return super.getSubscriptionContract();
    }

    public Date getSubscriptionContractStartDate()
    {
        getSubscriptionContract();
        return super.getSubscriptionContractStartDate();
    }


    public Date getSubscriptionContractEndDate()
    {
        getSubscriptionContract();
        return super.getSubscriptionContractEndDate();
    }

    
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link com.redknee.app.crm.bean.Subscriber.
     *             getMonthToDateBalanceWithoutPaymentPlan(Context)} if possible
     */
    @Deprecated
    @Override
    public long getMonthToDateBalanceWithoutPaymentPlan()
    {
        return getMonthToDateBalanceWithoutPaymentPlan(getContext());
    }

    public long getMonthToDateBalanceWithoutPaymentPlan(Context ctx)
    {
        synchronized (this)
        {
            if (monthToDateBalanceWithoutPaymentPlan_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    monthToDateBalanceWithoutPaymentPlan_ = service.getSubscriberMDBalanceWithoutPaymentPlan(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching monthToDateBalanceWithoutPaymentPlan for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
            }
        }

        return monthToDateBalanceWithoutPaymentPlan_;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link getAccumulatedPayment(Context)}
     */
    @Deprecated
    @Override
    public long getPaymentSinceLastInvoice()
    {
        return getPaymentSinceLastInvoice(getContext());
    }

    /**
     * Returns the accumulated payment of the subscriber since the last invoice.
     * 
     * @param ctx
     *            The operating context.
     * @return The accumulated payment amount of the subscriber since the last
     *         invoice.
     */
    public long getPaymentSinceLastInvoice(Context ctx)
    {
        synchronized (this)
        {
            if (paymentSinceLastInvoice_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    paymentSinceLastInvoice_ = service.getSubscriberPaymentSinceLastInvoice(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching monthToDateBalanceWithoutPaymentPlan for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
            }
        }

        return paymentSinceLastInvoice_;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link getAccumulatedOtherAdjustments(Context)}
     */
    @Deprecated
    @Override
    public long getAdjustmentsSinceLastInvoice()
    {
        return getAdjustmentsSinceLastInvoice(getContext());
    }

    /**
     * Returns the accumulated other adjustments of the subscriber since the
     * last invoice.
     * 
     * @param ctx
     *            The operating context.
     * @return The accumulated other adjustments (i.e. all adjustments other
     *         than payments) of the subscriber since the last invoice.
     */
    public long getAdjustmentsSinceLastInvoice(Context ctx)
    {
        synchronized (this)
        {
            if (adjustmentsSinceLastInvoice_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    adjustmentsSinceLastInvoice_ = service.getSubscriberAdjustmentsSinceLastInvoice(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching adjustmentsSinceLastInvoice for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
            }
        }

        return adjustmentsSinceLastInvoice_;
    }
    
    
    public List<SubscriberAgedDebt> getSubscriberInvoicedAgedDebt(Context ctx, Date maxDueDate, boolean stopOnNonAccumulatedDebt)
	 {
		 List<SubscriberAgedDebt> result = new ArrayList<SubscriberAgedDebt>();
		 try
		 {
			 And predicate = new And();
			 predicate.add(new EQ(SubscriberAgedDebtXInfo.SUBSCRIBER_ID, getId()));
			 predicate.add(new GTE(SubscriberAgedDebtXInfo.DUE_DATE, maxDueDate));
			 if (stopOnNonAccumulatedDebt)
			 {
				 predicate.add(new GT(SubscriberAgedDebtXInfo.ACCUMULATED_DEBT, Long.valueOf(0)));
			 }

			 result.addAll(HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberAgedDebt.class,
					 predicate, false, SubscriberAgedDebtXInfo.DEBT_DATE));
		 }
		 catch (HomeException e)
		 {
			 LogSupport
			 .info(ctx, this,
					 "Cannot retrieve subscriber invoiced aged debt for account "
							 + getBAN() + " subscriber " + getMsisdn(), e);
		 }
		 return result;
	 }

	 public List<SubscriberAgedDebt> getSubscriberAgedDebts(Context ctx, List<SubscriberAgedDebt> subAgedDebts)
	 {
		 List<SubscriberAgedDebt> result = new ArrayList<SubscriberAgedDebt>(subAgedDebts.size());

		 // reset accumulated payment to not use the cached value.
		 long accumulatedPayment_ = SubscriberSupport.INVALID_VALUE;

		 String sessionKey = (String)ctx.get(Common.AGED_DEBT_CALCULATION_SESSION_KEY);
		 long newPayment = this.getAccumulatedPayment(ctx, sessionKey, accumulatedPayment_);

		 if (newPayment != SubscriberSupport.INVALID_VALUE && subAgedDebts!=null)
		 {
			 // this is a payment and there is accumulated debt
			 if ( newPayment <= 0 && subAgedDebts.size() > 0 && subAgedDebts.iterator().next().getAccumulatedDebt() > 0 )
			 {
				 Collections.reverse(subAgedDebts);

				 boolean first = true;
				 long accumulatedPayment = 0;

				 for (SubscriberAgedDebt subAgedDebt : subAgedDebts)
				 {
					 // Taking care of first aged debt with accumulated value.
					 if (first)
					 {
						 first = false;
						 if (subAgedDebt.getDebt() != subAgedDebt.getAccumulatedDebt())
						 {
							 long previousDebt = subAgedDebt.getAccumulatedDebt() - subAgedDebt.getDebt();
							 long agedDebtPayment = Math.max(newPayment, -previousDebt);
							 accumulatedPayment += agedDebtPayment;
							 newPayment -= agedDebtPayment;
						 }
					 }

					 long currentDebt = subAgedDebt.getDebt();
					 long currentAccumulatedDebt = subAgedDebt.getAccumulatedDebt() + accumulatedPayment;
					 long currentPayment = subAgedDebt.getInvoicedPayment();

					 if (currentDebt > 0 && newPayment < 0)
					 {
						 long agedDebtPayment = Math.max(newPayment, -currentDebt);
						 currentDebt += agedDebtPayment;
						 currentPayment += agedDebtPayment;
						 currentAccumulatedDebt += agedDebtPayment;
						 accumulatedPayment += agedDebtPayment;
						 newPayment -= agedDebtPayment;
					 }

					 subAgedDebt.setCurrentDebt(currentDebt);
					 subAgedDebt.setCurrentPayment(currentPayment);
					 subAgedDebt.setCurrentAccumulatedDebt(currentAccumulatedDebt);
					 result.add(subAgedDebt);
				 }

				 Collections.reverse(subAgedDebts);
				 Collections.reverse(result);
			 }            

			 // this is somehow a debt
			 else if (newPayment > 0)
			 {
				 long accumulatedDebt = newPayment;

				 for (SubscriberAgedDebt agedDebt : subAgedDebts)
				 {
					 boolean first = true;

					 if (first)
					 {
						 first = false;
						 // If there is an overpayment
						 if (agedDebt.getAccumulatedTotalAmount()<0)
						 {
							 newPayment = newPayment + agedDebt.getAccumulatedTotalAmount();
							 newPayment = Math.max(newPayment, 0);
							 accumulatedDebt = newPayment;
						 }
					 }

					 long currentDebt = agedDebt.getDebt();
					 long currentPayment = agedDebt.getInvoicedPayment();
					 long currentAccumulatedDebt = agedDebt.getAccumulatedDebt() + accumulatedDebt;
					 if (currentPayment < 0 && newPayment > 0)
					 {
						 long unpaying = Math.min(newPayment, -currentPayment);
						 currentDebt += unpaying;
						 currentPayment += unpaying;
						 accumulatedDebt -= unpaying;
						 newPayment -= unpaying;
					 }

					 agedDebt.setCurrentDebt(currentDebt);
					 agedDebt.setCurrentPayment(currentPayment);
					 agedDebt.setCurrentAccumulatedDebt(currentAccumulatedDebt);

					 result.add(agedDebt);
				 }
			 }
			 else
			 {
				 result.addAll(subAgedDebts);
			 }
		 }else
			 result = subAgedDebts;
		 return result;
	 }
	 
	 public long getAccumulatedPayment(final Context ctx, String sessionKey, long accumulatedPayment)
	 {
		 synchronized (this)
		 {
			 if (accumulatedPayment == SubscriberSupport.INVALID_VALUE)
			 {
				 Context myCtx = ctx;
				 String mySessionKey = sessionKey;
				 if (sessionKey == null)
				 {
					 myCtx = ctx.createSubContext();
					 mySessionKey = CalculationServiceSupport.createNewSession(myCtx);
				 }
				 CalculationService service = (CalculationService) ctx.get(CalculationService.class);
				 try
				 {
					 accumulatedPayment = service.getSubscriberPaymentsReceived(myCtx, mySessionKey, this.getId(),
							 CalendarSupportHelper.get(myCtx).getRunningDate(myCtx));
				 }
				 catch (CalculationServiceException e)
				 {
					 LogSupport.minor(myCtx, this, "Failed to fetch due amount for subscriber.", e);
				 }
				 finally
				 {
					 // Session should be invalidated only if it is created by the method.
					 // Not otherwise.
					 if (sessionKey == null)
					 {
						 CalculationServiceSupport.endSession(myCtx, mySessionKey);
					 }
				 }
			 }
			 return accumulatedPayment;
		 }
	 }

    /**
     * This method should only be used in the web control. Others should call
     * the method with context parameter.
     * 
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getDeposit()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getCreditLimit(Context)}
     *             if possible
     */
    @Deprecated
    @Override
    public long getDeposit()
    {
        return getDeposit(getContext());
    }

    public long getDeposit(final Context ctx)
    {
        if (deposit_ == DEFAULT_DEPOSIT && ctx != null)
        {
            try
            {
                final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
                if (ppv != null)
                {
                    deposit_ = ppv.getDeposit();
                }
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Failed to determine default deposit.", exception).log(ctx);
            }
        }
        return deposit_;
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getCreditLimit()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getCreditLimit(Context)}
     *             if possible
     */
    @Deprecated
    @Override
    public long getCreditLimit()
    {
        return getCreditLimit(getContext());
    }

    /**
     * This method should only be used in the web control. Others should call
     * the method with context parameter.
     */
    public long getCreditLimit(final Context ctx)
    {
        if (creditLimit_ == DEFAULT_CREDITLIMIT && ctx != null)
        {
            try
            {
                final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
                if (ppv != null)
                {
                    creditLimit_ = ppv.getCreditLimit();
                }
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Failed to determine default credit limit.", exception).log(ctx);
            }
        }
        return creditLimit_;
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getAbmCreditLimit()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getAbmCreditLimit(Context
     *             )} if possible
     */
    @Deprecated
    @Override
    public long getAbmCreditLimit()
    {
        return getAbmCreditLimit(getContext());
    }

    public long getAbmCreditLimit(Context ctx)
    {
        synchronized (this)
        {
            if (abmCreditLimit_ == SubscriberSupport.INVALID_VALUE)
            {
                SubscriberSupport.updateSubscriberSummaryABM(ctx, this);
            }
        }

        return abmCreditLimit_;
    }

    /**
     * @deprecated Use {@link getAmountDue(Context)}
     */
    @Deprecated
    @Override
    public long getLastInvoiceAmount()
    {
        return getLastInvoiceAmount(getContext());
    }

    public long getLastInvoiceAmount(Context ctx)
    {
        synchronized (this)
        {
            if (lastInvoiceAmount_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    lastInvoiceAmount_ = service.getLastInvoiceAmountForSubscriber(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching lastInvoiceAmount for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
            }
        }

        return lastInvoiceAmount_;

    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getAmountOwing()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getAmountOwing(Context)}
     *             if possible
     */
    @Deprecated
    @Override
    public long getAmountOwing()
    {
        return getAmountOwing(getContext());
    }

    public long getAmountOwing(Context ctx)
    {
        synchronized (this)
        {
            if (amountOwing_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    amountOwing_ = service.getAmountOwedBySubscriber(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching amountOwing for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
            }
        }

        return amountOwing_;
    }

    /**
     * @deprecated Use {@link 
     *             com.redknee.app.crm.bean.Subscriber.getAmountOwingWithoutPaymentPlan
     *             (Context)} if possible
     */
    @Deprecated
    @Override
    public long getAmountOwingWithoutPaymentPlan()
    {
        return getAmountOwingWithoutPaymentPlan(getContext());
    }

    /**
     * Updates the subscriber with the amount owing excluding the payment plan
     * amounts.
     * 
     * @return the amount owing excluding the payment plan amounts
     */
    public long getAmountOwingWithoutPaymentPlan(Context ctx)
    {
        synchronized (this)
        {
            if (amountOwingWithoutPaymentPlan_ == SubscriberSupport.INVALID_VALUE)
            {
                final Account account = SubscriberSupport.lookupAccount(ctx, this);

                if (account == null)
                {
                    return SubscriberSupport.INVALID_VALUE;
                }

                CalculationService service = (CalculationService) getContext().get(CalculationService.class);
                try
                {
                    amountOwingWithoutPaymentPlan_ = service.getAmountOwedBySubscriberWithoutPaymentPlan(ctx, this.getId());
                }
                catch (CalculationServiceException e)
                {
                    new MinorLogMsg(this, "Exception while fetching amountOwingWithoutPaymentPlan for subscriber", e);
                    return SubscriberSupport.INVALID_VALUE;
                }
            }
        }

        return amountOwingWithoutPaymentPlan_;
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getRealTimeBalance()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getAbmBalance(Context)}
     *             if possible
     */
    @Deprecated
    @Override
    public long getRealTimeBalance()
    {
        return getRealTimeBalance(getContext());
    }

    public long getRealTimeBalance(Context ctx)
    {
        synchronized (this)
        {
            if (realTimeBalance_ == SubscriberSupport.INVALID_VALUE)
            {
                SubscriberSupport.updateSubscriberSummaryABM(ctx, this);
            }
        }

        return realTimeBalance_;
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getBalanceRemaining()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getBalanceRemaining(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public long getBalanceRemaining()
    {
        return getBalanceRemaining(getContext());
    }
    
    
    public long getBalanceRemaining(Context ctx)
    {
        return getBalanceRemaining(ctx, false);
    }

    public long getBalanceRemaining(Context ctx, boolean sendExpiry)
    {
        if (getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {
            if (balanceRemaining_ == DEFAULT_BALANCEREMAINING)
            {
                final AppOcgClient client = (AppOcgClient) ctx.get(AppOcgClient.class);
                if (client == null)
                {
                    setBalanceRemaining(0);
                }
                else
                {
                    final Account account = SubscriberSupport.lookupAccount(ctx, this);

                    final LongHolder outputBalance = new LongHolder();
                    int rc = 0;
                    if (account == null)
                    {
                        setBalanceRemaining(0);
                    }
                    else
                    {
                        try
                        {
                            SubscriptionType subscriptionType = getSubscriptionType(ctx);
                            if (null == subscriptionType)
                            {
                                setBalanceRemaining(0);
                            }
                            else
                            {
                                rc = client.requestBalance(getMSISDN(), getSubscriberType(), account.getCurrency(),
                                    sendExpiry, "", subscriptionType.getId(), outputBalance, new LongHolder(),
                                    new LongHolder());
                            }
                        }
                        catch (Exception e)
                        {
                            setBalanceRemaining(0);
                        }
                    }

                    if (rc != ErrorCode.NO_ERROR && rc != ErrorCode.BAL_EXPIRED)
                    {
                        setBalanceRemaining(0);
                    }
                    else
                    {
                        setBalanceRemaining(outputBalance.value);
                    }
                }
            }
        }
        return balanceRemaining_;
    }

    /**
     * See the language as set by users
     * 
     * @return - Language as set from accessor - like the one as set from
     *         WebControl.fromWeb. It returns null if the language has not been
     *         set on the bean by accessor
     */
    public String getLanguageAsSetByAccessor()
    {
        if (isLanguageSetByAccessor_)
        {
           // return language_;
        	return billingLanguage_;
        }
        else
        {
            return null;
        }
    }

   
   
    /**
     * Get most recently fetched value of language from the Service. This is
     * lazy loading of the language
     * 
     * @return the language as fetched last or null if not known or could not be
     *         fetched
     */
    public String getLanguageAsLastRetrieved(Context ctx)
    {
        if (null == languageAsLastRetrieved_)
        {
            // the lanaguageAsLastRetrieved_ get's updated when getLangauage()
            // fetches
            // language from service
        	return getBillingLanguage();
        }
        return languageAsLastRetrieved_;
    }

  
    @Override
    public void setMSISDN(String MSISDN) throws IllegalArgumentException
    {
        // TODO Auto-generated method stub
        super.setMSISDN(MSISDN);
        // this is important because language is lazy loaded
        languageAsLastRetrieved_ = null;
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getOverdraftBalance()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getOverdraftBalance(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public long getOverdraftBalance()
    {
        return getOverdraftBalance(getContext());
    }

    @Override
    public long getOverdraftDate()
    {
        return getOverdraftDate(getContext());
    }
    
    public long getOverdraftDate(Context ctx)
    {
        return getOverdraftBalance(ctx, false);
    }

    public long getOverdraftDate(Context ctx, boolean sendExpiry)
    {
        getOverdraftBalance(ctx, sendExpiry);
        return super.getOverdraftDate();
    }
    
    public long getOverdraftBalance(Context ctx)
    {
        return getOverdraftBalance(ctx, false);
    }
    
    public long getOverdraftBalance(Context ctx, boolean sendExpiry)
    {
        if (getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {
            if (overdraftBalance_ == DEFAULT_OVERDRAFTBALANCE)
            {
                final AppOcgClient client = (AppOcgClient) ctx.get(AppOcgClient.class);
                if (client == null)
                {
                    setOverdraftBalance(0);
                    setOverdraftDate(0);
                }
                else
                {
                    final Account account = SubscriberSupport.lookupAccount(ctx, this);

                    final LongHolder outputOverdraftBalance = new LongHolder();
                    final LongHolder outputOverdraftDate = new LongHolder();
                    int rc = 0;
                    if (account == null)
                    {
                        setOverdraftBalance(0);
                        setOverdraftDate(0);
                    }
                    else
                    {
                        try
                        {
                            SubscriptionType subscriptionType = getSubscriptionType(ctx);
                            if (null == subscriptionType)
                            {
                                setOverdraftBalance(0);
                                setOverdraftDate(0);
                            }
                            else
                            {
                                rc = client.requestBalance(getMSISDN(), getSubscriberType(), account.getCurrency(),
                                    sendExpiry, "", subscriptionType.getId(), new LongHolder(), outputOverdraftBalance,
                                    outputOverdraftDate);
                            }
                        }
                        catch (Exception e)
                        {
                            setOverdraftBalance(0);
                            setOverdraftDate(0);
                        }
                    }

                    if (rc != ErrorCode.NO_ERROR && rc != ErrorCode.BAL_EXPIRED)
                    {
                        setOverdraftBalance(0);
                        setOverdraftDate(0);
                    }
                    else
                    {
                        setOverdraftBalance(outputOverdraftBalance.value);
                        setOverdraftDate(outputOverdraftDate.value);
                    }
                }
            }
        }
        return overdraftBalance_;
    }

    /**
     * @return usage
     */
    public BalanceUsage getBalanceUsage(Context ctx)
    {
        ctx = ctx.createSubContext();
        String sessionKey = CalculationServiceSupport.createNewSession(ctx);
        try
        {
            BalanceUsage usage = null;
            try
            {
                usage = (BalanceUsage) XBeans.instantiate(BalanceUsage.class, ctx);
            }
            catch (Exception e)
            {
                usage = new BalanceUsage();
            }

            amountOwing_ = SubscriberSupport.INVALID_VALUE;
            amountOwingWithoutPaymentPlan_ = SubscriberSupport.INVALID_VALUE;
            abmCreditLimit_ = SubscriberSupport.INVALID_VALUE;
            balanceRemaining_ = DEFAULT_BALANCEREMAINING;
            overdraftBalance_ = DEFAULT_OVERDRAFTBALANCE;
            monthToDateBalance_ = SubscriberSupport.INVALID_VALUE;
            blockedBalance_ = SubscriberSupport.INVALID_VALUE;
            monthlySpendAmount_ = SubscriberSupport.INVALID_VALUE;
            lastInvoiceAmount_ = SubscriberSupport.INVALID_VALUE;
            paymentSinceLastInvoice_ = SubscriberSupport.INVALID_VALUE;
            adjustmentsSinceLastInvoice_ = SubscriberSupport.INVALID_VALUE;
            urcsOverdraftBalanceLimit_ = SubscriberSupport.INVALID_VALUE;
            
            usage.setSubscriberIdentifier(getId());
            usage.setSubscriberType(getSubscriberType());
            usage.setSpid(getSpid());
            usage.setBlockedBalance(getBlockedBalance(ctx));
            
            if (getWrittenOff())
            {
                usage.setIsWrittenOff(true);
                usage.setWriteOffAmount(WriteOffSupport.getTotalWriteOffAmountForSub(getContext(), this));
            }
            
            boolean isPooledSub = this.isPooledMemberSubscriber(ctx);
            try
            {
                usage.setAccountType(getAccount(ctx).getType());
                usage.setPooledMember(isPooledSub);
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(this, "Unable to find accountType for sub " + getId(), homeEx).log(ctx);
            }
            try
            {
                Parameters parameters = SubscriberSupport.updateSubscriberSummaryABMReturnParameterList(ctx, this);

                if (parameters != null && isPooledSub)
                {
                    usage.setGroupUsageQuota(parameters.getGroupQuota());
                    usage.setGroupUsage(parameters.getGroupUsage());
                }
                usage.setScreeningTemplateId(this.getGroupScreeningTemplateId());
                if(parameters != null && this.getGroupScreeningTemplateId() != -1 ){
                	usage.setVoiceCap(parameters.getVoiceCap());
                	usage.setDataCap(parameters.getDataCap());
                	usage.setMessageCap(parameters.getMessageCap());
                	usage.setVoiceUsage(parameters.getVoiceUsage());
                	usage.setDataUsage(parameters.getDataUsage());
                	usage.setMessageUsage(parameters.getMessageUsage());
                }
                if (this.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
                {
                    usage.setCreditLimit(getAbmCreditLimit(ctx));
                    usage.setRealTimeBalance(getRealTimeBalance(ctx));
                    usage.setMonthToDateBalance(getMonthToDateBalance(ctx));
                    usage.setAmountOwing(getAmountOwing(ctx));
                    usage.setLastInvoiceAmount(getLastInvoiceAmount(ctx));
                    usage.setPaymentSinceLastInvoice(getPaymentSinceLastInvoice(ctx));
                    usage.setAdjustmentsSinceLastInvoice(getAdjustmentsSinceLastInvoice(ctx));
                    if (!isPooledSub)
                    {
                        usage.setMonthlySpendLimit(getMonthlySpendLimit());
                        usage.setMonthlySpendAmount(getMonthlySpendAmount(ctx));
                    }
                }
                else
                {
                    SysFeatureCfg sysFeatureCfg = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);                    
                    boolean sendExpiry = sysFeatureCfg.getEnableBalanceForExpiredSub();
                    usage.setBalanceRemaining(getBalanceRemaining(ctx, sendExpiry));
                    usage.setOverdraftBalanceLimit(getURCSOverdraftBalanceLimit(ctx));
                    usage.setOverdraftBalance(getOverdraftBalance(ctx, sendExpiry));
                    usage.setOverdraftDate(getOverdraftDate(ctx, sendExpiry));
                }
            }
            catch (IllegalArgumentException iEx)
            {
            }
            return usage;
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey);
        }
    }

    public void setMonthlySpendAmount(long value)
    {
        monthlySpendAmount_ = value;
    }

    public long getMonthlySpendAmount(Context ctx)
    {
        synchronized (this)
        {
            if (monthlySpendAmount_ == SubscriberSupport.INVALID_VALUE)
            {
                SubscriberSupport.updateSubscriberSummaryABM(ctx, this);
            }
        }
        return monthlySpendAmount_;
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getDeactivationDate()
     */
    @Override
    public Date getDeactivationDate()
    {
        try
        {
            return getDeactivationDate(getContext());
        }
        catch (final Throwable t)
        {
            return new Date();
        }

    }


    @Override
    public String getDealerCode()
    {
        final TechnologyEnum technology = this.getTechnology();
        if (technology == null)
        {
            return super.getDealerCode();
        }
        Context ctx = ContextLocator.locate();
        if (!technology.isPackageAware())
        {
            return super.getDealerCode();
        }
        else
        {
            try
            {
                Account account = this.getAccount(ctx);
                if (account != null && account.isPooled(ctx))
                {
                    // ignoring fake subscription that is created for Pooled acounts
                    return super.getDealerCode();
                }
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(this, "Unable to find the account " + this.getBAN(), homeEx).log(ctx);
            }
        }
        try
        {
            final GenericPackage techPackage = PackageSupportHelper.get(ctx).getPackage(ctx, this.getTechnology(),
                    this.getPackageId(), this.getSpid());
            if ( techPackage != null)
            {
                return techPackage.getDealer();
            }
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(this, "Unable to find the package " + this.getPackageId(), homeEx).log(ctx);
        }
        return null;
    }
    
    public Date getDeactivationDate(final Context ctx)
    {
        CRMSpid serviceProvider = null;
        try
        {
            serviceProvider = SubscriberSupport.getServiceProvider(ctx, this);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, "Unable to determine deactivation date.", e).log(ctx);
        }

        Date dateRemaining = new Date();
        if (serviceProvider != null)
        {
            if (getState() == SubscriberStateEnum.AVAILABLE)
            {
                // daysRemaining = serviceProvider.getSubAvailableTimer() -
                // getAvailableTimer();
                dateRemaining = getDate(serviceProvider.getSubAvailableTimer(), getDateCreated());
            }
            else if (getState() == SubscriberStateEnum.EXPIRED)
            {
                // daysRemaining = serviceProvider.getSubExpiryTimer() -
                // getExpiryTimer();
                dateRemaining = getDate(serviceProvider.getSubExpiryTimer(), getExpiryDate());
            }
        }

        /*
         * Calendar calendar = Calendar.getInstance();
         * calendar.add(Calendar.DAY_OF_MONTH, daysRemaining);
         */

        return CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(dateRemaining);
    }

    /**
     * Returns the day that is 'days' days in advance
     * 
     * @param days
     * @param date
     * @return Date object that is 'days' days after date parameter
     */
    private Date getDate(final int days, final Date date)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, day + days);
        return cal.getTime();
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getVoicePricePlan()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getVoicePricePlan(Context
     *             )} if possible
     */
    @Deprecated
    @Override
    public String getVoicePricePlan()
    {
        return getVoicePricePlan(getContext());
    }

    public String getVoicePricePlan(Context ctx)
    {
        try
        {
            // As of CRM 8.2, the Rate Plan information is stored in Price Plan
            final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
            if (ppv == null)
            {
                return AbstractSubscriber.DEFAULT_VOICEPRICEPLAN;
            }
            final PricePlan rateplanRef = ppv.getPricePlan(ctx);
            if (rateplanRef == null)
            {
                return AbstractSubscriber.DEFAULT_VOICEPRICEPLAN;
            }

            return rateplanRef.getVoiceRatePlan();
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(this, "Failed to retrieve the voice rate plan ID from the price plan.", exception).log(ctx);
            return AbstractSubscriber.DEFAULT_VOICEPRICEPLAN;
        }
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getSMSPricePlan()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getSMSPricePlan(Context)}
     *             if possible
     */
    @Deprecated
    @Override
    public String getSMSPricePlan()
    {
        return getSMSPricePlan(getContext());
    }

    public String getSMSPricePlan(Context ctx)
    {
        try
        {
            // As of CRM 8.2, the Rate Plan information is stored in Price Plan
            final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
            if (ppv == null)
            {
                return AbstractSubscriber.DEFAULT_SMSPRICEPLAN;
            }
            final PricePlan rateplanRef = ppv.getPricePlan(ctx);
            if (rateplanRef == null)
            {
                return AbstractSubscriber.DEFAULT_SMSPRICEPLAN;
            }

            return rateplanRef.getSMSRatePlan();
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(this, "Failed to retrieve the SMS rate plan ID from the price plan.", exception).log(ctx);
            return AbstractSubscriber.DEFAULT_SMSPRICEPLAN;
        }
    }

    /**
     * @see com.redknee.app.crm.bean.AbstractSubscriber#getDataPricePlan()
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getDataPricePlan(Context)
     *             } if possible
     */
    @Deprecated
    @Override
    public String getDataPricePlan()
    {
        return getDataPricePlan(getContext());
    }

    public String getDataPricePlan(Context ctx)
    {
        try
        {
            // As of CRM 8.2, the Rate Plan information is stored in Price Plan
            final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
            if (ppv == null)
            {
                return AbstractSubscriber.DEFAULT_DATAPRICEPLAN;
            }
            final PricePlan rateplanRef = ppv.getPricePlan(ctx);
            if (rateplanRef == null)
            {
                return AbstractSubscriber.DEFAULT_DATAPRICEPLAN;
            }

            return rateplanRef.getDataRatePlan();
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(this, "Failed to retrieve the SMS rate plan ID from the price plan.", exception).log(ctx);
            return AbstractSubscriber.DEFAULT_DATAPRICEPLAN;
        }
    }

    /**
     * Return all of the Subscriber Services in the "provisioned" state. The
     * Services actually provisioned on the backend.
     */
    public Set getProvisionedServices(Context ctx)
    {
        /*
         * Previous to version 8_0, CRM used to return all the Services from the
         * RAW Price Plan in case the Subscriber was coming from CLTC
         * suspension. However, I didn't find any instance of
         * setFirstInitCltc(true) which would trigger that code. Since Service
         * Refactoring in version 8_0, the SubscriberServices records remain
         * even during CLTC suspension. So replace the RAW Price Plan look up
         * with the actual Provisioned Services.
         */
        return (Set) SubscriberServicesSupport.getProvisionedServices(ctx, getId());
    }

    /**
     * Return all of the Subscriber Services in the "provisioned" state. The
     * Services actually provisioned on the backend.
     */
    public Set<SubscriberServices> getProvisionedSubscriberServices(Context ctx)
    {
        /*
         * Previous to version 8_0, CRM used to return all the Services from the
         * RAW Price Plan in case the Subscriber was coming from CLTC
         * suspension. However, I didn't find any instance of
         * setFirstInitCltc(true) which would trigger that code. Since Service
         * Refactoring in version 8_0, the SubscriberServices records remain
         * even during CLTC suspension. So replace the RAW Price Plan look up
         * with the actual Provisioned Services.
         */
        return (Set<SubscriberServices>) SubscriberServicesSupport.getProvisionedSubscriberServices(ctx, getId());
    }


    /**
     * Adds a provisioned service to the cached transient set and modifies the
     * SubscriberService to Provisioned
     * 
     * @param ctx
     *            the operating context
     * @param serviceId
     *            the service id to add
     * @throws HomeException
     *             this exception occurs if the service update fails
     */
    public void addProvisionService(Context ctx, long serviceId) throws HomeException
    {
        SubscriberServicesSupport.createOrModifySubcriberService(ctx, this, serviceId, ServiceStateEnum.PROVISIONED);

        if (!getTransientProvisionedServices().contains(serviceId))
        {
            getTransientProvisionedServices().add(serviceId);
        }
    }

    /**
     * Adds a provisioned service to the cached transient set and modifies the
     * SubscriberService to the given state
     * 
     * @param ctx
     *            the operating context
     * @param serviceId
     *            the service id to add
     * @param state
     *            the state needed to store in the DB
     * @throws HomeException
     *             this exception occurs if the service update fails
     */
    public void removeProvisionService(Context ctx, long serviceId, ServiceStateEnum state, String path) throws HomeException
    {
        if (SubscriberServicesSupport.existsInServicesForIntentProvision(ctx, this, serviceId))
        {
            SubscriberServicesSupport.createOrModifySubcriberService(ctx, this, serviceId, state);
        }
        else
        {
            /*
             * This is a failed to provision service that needs to be deleted
             * from the table
             */

            if (SubscriberServicesSupport.isProvisionedWithErrors(ctx, serviceId, getId()))
            {
                SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx, getId(), serviceId, path);
            }
            else
            {
                SubscriberServicesSupport.createOrModifySubcriberService(ctx, this, serviceId, state);
            }
        }
    }

    
    /**
     * Adds a provisioned service to the cached transient set and modifies the
     * SubscriberService to the given state
     * 
     * @param ctx
     *            the operating context
     * @param serviceId
     *            the service id to add
     * @param state
     *            the state needed to store in the DB
     * @throws HomeException
     *             this exception occurs if the service update fails
     */
    public void updateSubscriberService(Context ctx, SubscriberServices subService, ServiceStateEnum state) throws HomeException
    {
        if (SubscriberServicesSupport.existsInServicesForIntentProvision(ctx, this, subService.getServiceId()))
        {
            SubscriberServicesSupport.createOrModifySubcriberService(ctx, this, subService.getServiceId(), state, 
                    (subService != null ? subService.getSuspendReason() : SuspendReasonEnum.NONE),subService);
        }
        else
        {
            /*
             * This is a failed to provision service that needs to be deleted
             * from the table
             */

            if (SubscriberServicesSupport.isProvisionedWithErrors(ctx, subService.getServiceId(), getId()))
            {
                SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx, getId(), subService.getServiceId(), subService.getPath());
            }
            else
            {
                SubscriberServicesSupport.createOrModifySubcriberService(ctx, this, subService.getServiceId(), state, 
                        (subService != null ? subService.getSuspendReason() : SuspendReasonEnum.NONE),subService);
            }
        }
    }
    
    
    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getServices(Context)} if
     *             possible
     */
    @Deprecated
    public Set<ServiceFee2ID> getServices()
    {
        return getServices(getContext());
    }

    /**
	  * @return Returns all service selected + mandatory services in the price
	  *         plan
	  */
	 public Set<ServiceFee2ID> getServices(Context ctx)
	 {
		 if (services_ == null)
		 {
			 services_ = new HashSet<ServiceFee2ID>();
			 Map<ServiceFee2ID, SubscriberServices> services = SubscriberServicesSupport.getSubscribersServices(ctx, getId());
			 
			 services_.addAll(services.keySet());
			 try
			 {
				 // Add the priceplan version's mandatory services
				 final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
				 if (ppv != null)
				 {
					 Map<ServiceFee2ID, ServiceFee2> serviceFees = ppv.getServiceFees(ctx);

					 Iterator i = serviceFees.keySet().iterator();
					 while (i.hasNext())
					 {
						 final ServiceFee2 sf2 = serviceFees.get(i.next());
						 if (sf2.getServicePreference().equals(ServicePreferenceEnum.MANDATORY)  && !services_.contains(new ServiceFee2ID(sf2.getServiceId(), sf2.getPath())))
						 {
							 services_.add(new ServiceFee2ID(sf2.getServiceId(), sf2.getPath()));
						 }
					 }
				 }
			 }
			 catch (HomeException e)
			 {
				 LogSupport.minor(ctx, this, "Error while trying to add Mandatory Services from the Price Plan.");
			 }
		 }
		 return services_;
	 }

    /**
     * @return Returns all service selected + mandatory services in the price
     *         plan
     */
	 public Set<ServiceFee2ID> getServices(Context ctx, Date nextRecurringChargeDate)
	 {
		 if (services_ == null)
		 {
			 services_ = new HashSet();

			 //Map<Long, SubscriberServices> services = SubscriberServicesSupport.getAllSubscribersServices(ctx, getId(), false, nextRecurringChargeDate);
			 Map<ServiceFee2ID, SubscriberServices> services = SubscriberServicesSupport.getAllSubscribersServices(ctx, getId(), false, nextRecurringChargeDate);
			 services_.addAll(services.keySet());
			 try
			 {
				 // Add the priceplan version's mandatory services
				 final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
				 if (ppv != null)
				 {
					 //Map<Long, ServiceFee2> serviceFees = ppv.getServiceFees(ctx);
					 Map<ServiceFee2ID, ServiceFee2> serviceFees = ppv.getServiceFees(ctx);

					 Iterator i = serviceFees.keySet().iterator();
					 while (i.hasNext())
					 {
						 final ServiceFee2 sf2 = serviceFees.get(i.next());

						 //if (sf2.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && !services_.contains(sf2.getServiceId()))
						 if (sf2.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && !services_.contains(new ServiceFee2ID(sf2.getServiceId(), sf2.getPath())))
						 {
							 //services_.add(sf2.getServiceId());
							 services_.add(new ServiceFee2ID(sf2.getServiceId(), sf2.getPath()));
						 }
					 }
				 }
			 }
			 catch (HomeException e)
			 {
				 LogSupport.minor(ctx, this, "Error while trying to add Mandatory Services from the Price Plan.");
			 }
		 }
		 return services_;
	 }    
    

    public Set<ServiceFee2ID> getServices(Context ctx, ServiceTypeEnum serviceType) throws HomeException
	 {
		 if (servicesByType_ == null)
		 {
			 servicesByType_ = new HashMap<ServiceTypeEnum, Set<ServiceFee2ID>>();
		 }

		 if (servicesByType_.get(serviceType) == null && ctx != null)
		 {
			 lazyLoadServicesOfType(ctx, getServices(ctx), servicesByType_, serviceType);
		 }

		 Set<ServiceFee2ID> result = servicesByType_.get(serviceType);
		 if (result == null)
		 {
			 return new HashSet<ServiceFee2ID>();
		 }

		 return result;
	 }

    public boolean hasServiceOfType(Context ctx, ServiceTypeEnum serviceType) throws HomeException
	 {
		 if (servicesByType_ == null)
		 {
			 servicesByType_ = new HashMap<ServiceTypeEnum, Set<ServiceFee2ID>>();
		 }

		 Set<ServiceFee2ID> svcs = servicesByType_.get(serviceType);
		 if (svcs != null)
		 {
			 return svcs.size() > 0;
		 }
		 Collection<ServiceFee2ID> services = getServices(ctx);
		 
		 List<Long> serviceIds = new ArrayList<Long>();
		 for (ServiceFee2ID service : services) {
			serviceIds.add(service.getServiceId());
		}
		 return ServiceSupport.hasServiceOfType(ctx, serviceIds, serviceType);
	 }

    public void setServices(Set services) throws IllegalArgumentException
    {
        services_ = services;
        if (servicesByType_ != null)
        {
            servicesByType_.clear();
        }
    }

    private void lazyLoadServicesOfType(Context ctx, Collection<ServiceFee2ID> services,
			 Map<ServiceTypeEnum, Set<ServiceFee2ID>> destination, ServiceTypeEnum serviceType) throws HomeException
	 {
		 List<Long> serviceIds = new ArrayList<Long>();
		 for (ServiceFee2ID service : services) {
			serviceIds.add(service.getServiceId());
		}
		 Collection<Service> filteredServices = ServiceSupport.filterServicesByType(ctx, serviceIds, serviceType);

		 Set<ServiceFee2ID> result = new HashSet<ServiceFee2ID>();
		 if (filteredServices != null)
		 {
			 for (Service svc : filteredServices)
			 {
				 result.add(new ServiceFee2ID(svc.getID(),SubscriberServicesUtil.DEFAULT_PATH));
			 }
		 }
		 destination.put(serviceType, Collections.unmodifiableSet(result));
	 }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getAuxiliaryServices(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public List getAuxiliaryServices()
    {
        return getAuxiliaryServices(getContext());
    }

    public List<SubscriberAuxiliaryService> getAuxiliaryServices(final Context ctx)
    {
        if (auxiliaryServices_ == null && ctx != null)
        {
            final String identifier = this.getId();

            if (identifier == null || identifier.length() == 0)
            {
                return new ArrayList<SubscriberAuxiliaryService>();
            }

            auxiliaryServices_ = new ArrayList<SubscriberAuxiliaryService>(
                SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx, identifier));
        }

        return auxiliaryServices_;
    }

    public Set<Long> getAuxiliaryServiceIds(Context ctx)
    {
        List<SubscriberAuxiliaryService> auxServices = getAuxiliaryServices(ctx);
        Set<Long> auxServiceIds = new HashSet<Long>();
        for (Iterator<SubscriberAuxiliaryService> iter = auxServices.iterator(); iter.hasNext();)
        {
            auxServiceIds.add(Long.valueOf(iter.next().getAuxiliaryServiceIdentifier()));
        }
        return auxServiceIds;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link 
     *             com.redknee.app.crm.bean.Subscriber.getFutureAuxiliaryServices
     *             (Context)} if possible
     */
    @Deprecated
    @Override
    public List getFutureAuxiliaryServices()
    {
        if (getContext() != null)
        {
            return getFutureAuxiliaryServices(getContext());
        }
        else
        {
            return futureAuxiliaryServices_;
        }
    }

    public List<SubscriberAuxiliaryService> getFutureAuxiliaryServices(Context ctx)
    {
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        return getFutureAuxiliaryServices(ctx, runningDate);
    }

    public List<SubscriberAuxiliaryService> getFutureAuxiliaryServices(Context ctx, Date runningDate)
    {
        if (futureAuxiliaryServices_ == null && ctx != null)
        {
            final And filter = new And();
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, this.getId()));
            filter.add(new GT(SubscriberAuxiliaryServiceXInfo.START_DATE, CalendarSupportHelper.get(ctx).getEndOfDay(
                runningDate)));

            Collection<SubscriberAuxiliaryService> collection;
            try
            {
                collection = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberAuxiliaryService.class, filter);
                collection = SubscriberAuxiliaryServiceSupport.setTypeForSubscriberAuxiliaryServiceCollection(ctx,
                    collection);
                futureAuxiliaryServices_ = new ArrayList<SubscriberAuxiliaryService>(collection);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Unable to retrieve subscriber's future Aux.Svc contents.", e).log(ctx);
            }

        }

        return futureAuxiliaryServices_;
    }

    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getPersonalListPlan(
     *             Context)} if possible
     */
    @Deprecated
    public Collection getPersonalListPlan()
    {
        return getPersonalListPlan(getContext());
    }

    public Collection getPersonalListPlan(Context ctx)
    {
        if (ctx != null)
        {
            try
            {
                return PersonalListPlanSupport.getPersonalListPlan(ctx, getAuxiliaryServices(ctx));
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to look-up personal list plan for subscriber " + getId(), exception)
                    .log(ctx);

                return null;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link 
     *             com.redknee.app.crm.bean.Subscriber.getPersonalListPlanEntries
     *             (Context)} if possible
     */
    @Deprecated
    @Override
    public Map getPersonalListPlanEntries()
    {
        return getPersonalListPlanEntries(getContext());
    }

    public Map getPersonalListPlanEntries(final Context ctx)
    {
        if (personalListPlanEntries_ == null && ctx != null)
        {
            if (this.getPersonalListPlan(ctx).isEmpty())
            {
                personalListPlanEntries_ = new HashMap();
            }
            else
            {
                try
                {
                    personalListPlanEntries_ = PersonalListPlanSupport
                        .getPersonalListPlanMSISDNs(ctx, this.getMSISDN());
                }
                catch (FFEcareException e)
                {
                    ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                    if (el != null)
                    {
                        el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PERSONAL_LIST_PLAN_ENTRIES, e
                            .getMessage()));
                    }
                    new MinorLogMsg(this, e.getMessage(), e).log(ctx);
                    // prevent NullPointerException and force try again next
                    // time
                    // the method is called.
                    return new HashMap();
                }

                for (Iterator it = this.getPersonalListPlan().iterator(); it.hasNext();)
                {
                    Long plpid = (Long) it.next();
                    if (personalListPlanEntries_.get(plpid) == null)
                    {
                        personalListPlanEntries_.put(plpid, new TreeSet());
                    }
                }
            }
        }

        return personalListPlanEntries_;
    }

    /**
     * Returns the Birthday Plan associated with this subscriber or null if no
     * birthday plan is associated.
     * 
     * @param ctx
     *            the current context
     * @return the birthday plan for this subscriber or null
     */
    public BirthdayPlan getBirthdayPlan(final Context ctx)
    {
        if (ctx != null)
        {
            try
            {
                return SubscriberAuxiliaryServiceSupport.getBirthdayPlan(ctx, getAuxiliaryServices(ctx));
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to look-up birthday plan for subscriber " + getId(), exception).log(ctx);

                return null;
            }
        }

        return null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Map getBlacklistWhitelistPlanEntries()
    {
        return getBlacklistWhitelistPlanEntries(getContext());
    }


    public  Map<BlacklistWhitelistTemplate, Set<String>> getBlacklistWhitelistPlanEntries(final Context ctx)
    {
        if (blacklistWhitelistPlanEntries_.size() ==0 && ctx != null)
        {
            /*
             * don't make service call if there is no Blacklist Whitelist provisioned service
             */
            List<Long> serviceList = this.getBlacklistWhitelistIdentifier(ctx);
            if (serviceList != null && serviceList.isEmpty())
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, Subscriber.class,
                        "Blacklist Whitelist PLP service ID list empty, user has not subscribed BLWL PLP services.");
                }
                blacklistWhitelistPlanEntries_ = new HashMap<BlacklistWhitelistTemplate, Set<String>>();
            }
            // if null(home exception has occured) or subscriberService(serviceList != null) exists fetch list from FfeRmi service
            else
            {
                try
                {
                    blacklistWhitelistPlanEntries_ = BlacklistWhitelistPLPSupport.getBlacklistWhitelistPlpMsisdnList(
                            ctx, this.getMSISDN());
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, Subscriber.class,
                            "Blacklist Whitelist PLP details fetched from service, received list size : " + blacklistWhitelistPlanEntries_.size());
                    }
                }
                catch (FFEcareException e)
                {
                    ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                    if (el != null)
                    {
                        el.thrown(new IllegalPropertyArgumentException(
                                SubscriberXInfo.BLACKLIST_WHITELIST_PLAN_ENTRIES, e.getMessage()));
                    }
                    new MinorLogMsg(this, e.getMessage(), e).log(ctx);
                    return new HashMap<BlacklistWhitelistTemplate, Set<String>>();
                }
            }
        }

        return blacklistWhitelistPlanEntries_;
    }


    public List<Long> getBlacklistWhitelistIdentifier(Context ctx)
    {
        if (ctx != null)
        {
            try
            {
                return BlacklistWhitelistPLPSupport.getBlacklistWhitelistIdFromSubscriberServices(ctx,
                        SubscriberServicesSupport.getProvisionedSubscriberServices(ctx, getId()));
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to look-up Blacklist Whitelist PLP service for subscriber " + getId(),
                        exception).log(ctx);

                return null;
            }
        }

        return null;
    }
    /*
     * (non-Javadoc)
     * @see com.redknee.app.crm.state.StateAware#getAbstractState()
     */
    @Override
    public AbstractEnum getAbstractState()
    {
        return getState();
    }

    /*
     * (non-Javadoc)
     * @see
     * com.redknee.app.crm.state.StateAware#setAbstractState(com.redknee.framework
     * .xhome.xenum.AbstractEnum)
     */
    @Override
    public void setAbstractState(final AbstractEnum state)
    {
        setState((SubscriberStateEnum) state);
    }

    @Override
    public Collection<? extends Enum> getFinalStates()
    {
        return FINAL_STATES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Enum> boolean isFinalState(T state)
    {
        Collection<? extends Enum> finalStates = getFinalStates();
        return EnumStateSupportHelper.get(getContext()).isOneOfStates(state, finalStates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInFinalState()
    {
        Collection<? extends Enum> finalStates = getFinalStates();
        return EnumStateSupportHelper.get(getContext()).isOneOfStates(this, finalStates);
    }

    /**
     * Gets direct parent account object, Throws exception if BAN is null or
     * empty TODO Can we cache the account, instead of DB query (given we don't
     * use Caching home).
     * 
     * @return
     * @throws HomeException
     */
    public Account getAccount(final Context ctx) throws HomeException
    {
        return AccountSupport.getAccount(ctx, getBAN());
    }

    /**
     * Gets its responsible ancestor account object, may not be the direct
     * parent account
     * 
     * @return
     * @throws HomeException
     */
    public Account getResponsibleParentAccount(final Context ctx) throws HomeException
    {
        Account parent = getAccount(ctx);
        parent = parent.getResponsibleParentAccount(ctx);
        return parent;
    }

    public Account getRootAccount(final Context ctx) throws HomeException
    {
        Account parent = getAccount(ctx);
        parent = parent.getRootAccount(ctx);
        return parent;
    }

    // /**
    // * @return Returns the hlrProfileRemoved_.
    // */
    // public boolean isHlrProfileRemoved() {
    // return hlrProfileRemoved_;
    // }
    // /**
    // * @param hlrProfileRemoved_ The hlrProfileRemoved_ to set.
    // */
    // public void setHlrProfileRemoved(boolean hlrProfileRemoved) {
    // this.hlrProfileRemoved_ = hlrProfileRemoved;
    // }
    //
    // /**
    // * If hlr profile is removed, we don't need to send hlr command for each
    // * individual service for unprovisioning
    // * so adding this transient field for tracking, JoeC Mar 22, 2005
    // */
    // private boolean hlrProfileRemoved_ = false;

    // unlike the the Set ProvisionedServices_, which is persistented, used to
    // track
    // servies provioned at backend.
    // This one is used to track transiently, provioned changes.
    // It is primarily to use to track ECP and Smsb profile adding, since during
    // provisioning
    // we will add all properties, but in the subscriber pipe line, we have no
    // way to know
    // should we need to update other parameters to ECP, SMSB if they changed
    // JoeC Mar 24, 2005.

    // TODO, now I added getProvisionedServicesBackup, these transiend set can
    // be removed.

    public Set getTransientProvisionedServices()
    {
        return transientProvisionedServices_;
    }

    public void resetTransientProvisionedServices()
    {
        transientProvisionedServices_.clear();
    }

    public Collection getServicePackageIds(final Context ctx) throws HomeException
    {
        Collection arr;

        final PricePlanVersion version = this.getRawPricePlanVersion(ctx);

        if (version != null && version.getServicePackageVersion() != null)
        {
            arr = ServiceSupport.transformServiceObjectToIds(version.getServicePackageVersion().getPackageFees()
                .values());
        }
        else
        {
            arr = new ArrayList();
        }

        return arr;
    }

    public Collection<SubscriberAuxiliaryService> getProvisionedAuxiliaryServices(Context ctx)
    {
        Collection<SubscriberAuxiliaryService> ret = new HashSet();

        CollectionSupportHelper.get(ctx).findAll(ctx, this.getAuxiliaryServices(ctx),
            new EQ(SubscriberAuxiliaryServiceXInfo.PROVISIONED, Boolean.TRUE), ret);
        return ret;
    }


    public Collection getProvisionedAuxServiceIdsBackup(final Context ctx)
    {
        if (provisionedAuxServiceIdsBackup_  == null)
        {
            provisionedAuxServiceIdsBackup_ = new ArrayList();
            try
            {
            provisionedAuxServiceIdsBackup_.addAll(SubscriberAuxiliaryServiceSupport.getSubscriberProvisionedAuxiliaryServiceIDs(ctx, this));
            }
            catch(HomeException homeEx)
            {
                new MinorLogMsg(this, "Unable to load provisioned auxiliary services", homeEx).log(ctx);
                
            }
        }
        return provisionedAuxServiceIdsBackup_;
    }

    public Collection getProvisionedAuxServiceBackup(final Context ctx)
    {
        if (provisionedAuxServiceBackup_  == null)
        {
            provisionedAuxServiceBackup_ = new ArrayList();
            provisionedAuxServiceBackup_.addAll(getAuxiliaryServices(ctx));
        }
        return provisionedAuxServiceBackup_;
    }

    public void resetProvisionedAuxServiceIdsBackup()
    {
        provisionedAuxServiceIdsBackup_ = null;
    }
    
    public void resetProvisionedAuxServiceBackup()
    {
        provisionedAuxServiceBackup_ = null;
    }
    
    public Collection getProvisionedBundleIdsBackup()
    {
        return provisionedBundleIdsBackup_;
    }

    public Collection getProvisionedPackageIdsBackup()
    {
        return provisionedPackageIdsBackup_;
    }
    
    public void resetProvisionedServiceBackup()
    {
    	provisionedServices = null;
    }

    

    public Map<com.redknee.app.crm.bean.service.ServiceStateEnum, Map<Long, SubscriberServices>> getServicesBackup(final Context ctx)
    {
        if (servicesBackup_ == null)
        {
            servicesBackup_ = new HashMap<com.redknee.app.crm.bean.service.ServiceStateEnum, Map<Long, SubscriberServices>>();
            Map<ServiceFee2ID, SubscriberServices> allSubServicesMap = SubscriberServicesSupport.getAllSubscribersServices(ctx,
                    getId(), true);
            for (SubscriberServices subService : allSubServicesMap.values())
            {
                Map<Long, SubscriberServices> serviceByState = servicesBackup_.get(subService.getProvisionedState());
                if (serviceByState == null)
                {
                    serviceByState = new HashMap<Long, SubscriberServices>();
                    servicesBackup_.put(subService.getProvisionedState(), serviceByState);
                }
                serviceByState.put(Long.valueOf(subService.getServiceId()), subService);
            }
        }
        return servicesBackup_;
    }

    private Map<Long, SubscriberServices> getSubscriberServicesByState(final Context ctx, final com.redknee.app.crm.bean.service.ServiceStateEnum state)
    {
        Map<com.redknee.app.crm.bean.service.ServiceStateEnum, Map<Long, SubscriberServices>> allServicesMap = getServicesBackup(ctx);
        return allServicesMap.get(state);
    }
    
    
    
    public Map<Long, SubscriberServices> getProvisionedServicesBackup(final Context ctx)
    {
    	if(provisionedServices == null)
    	{
    		provisionedServices = getSubscriberServicesByState(ctx,
    				com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONED);
    		if (provisionedServices == null)
    		{
    			provisionedServices = new HashMap<Long, SubscriberServices>();
    		}
    	}
        return provisionedServices;
    }
    
    public Map<Long, SubscriberServices> getUnProvisionedErrorServicesBackup(final Context ctx)
    {
        Map<Long, SubscriberServices> unProvWithError = getSubscriberServicesByState(ctx,
                com.redknee.app.crm.bean.service.ServiceStateEnum.UNPROVISIONEDWITHERRORS);
        
        if (unProvWithError == null)
        {
            unProvWithError = new HashMap<Long, SubscriberServices>();
        }
        return unProvWithError;
    }
    
    public Map<Long, SubscriberServices> getProvisionedErrorServicesBackup(final Context ctx)
    {
        Map<Long, SubscriberServices> provWithError = getSubscriberServicesByState(ctx,
                com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONEDWITHERRORS);
        
        if (provWithError == null)
        {
        	provWithError = new HashMap<Long, SubscriberServices>();
        }
        return provWithError;
    }
    
    public Map<Long, SubscriberServices> getCLCTSuspendedServicesBackup(final Context ctx)
    {
        Map<Long, SubscriberServices> suspendedServices = getSubscriberServicesByState(ctx,
                com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDED);
        Map<Long, SubscriberServices> clctSuspendedServices = new HashMap<Long, SubscriberServices>();
        if (suspendedServices == null)
        {
            suspendedServices = new HashMap<Long, SubscriberServices>();
        }
        for (SubscriberServices subService : suspendedServices.values())
        {
            if (SuspendReasonEnum.CLCT.equals(subService.getSuspendReason()))
            {
                clctSuspendedServices.put(subService.getServiceId(), subService);
            }
        }
        return suspendedServices;
    }
    
    public Map<Long, SubscriberServices> getSuspendedStateServicesBackup(final Context ctx)
    {
        Map<Long, SubscriberServices> suspendedServices = getSubscriberServicesByState(ctx,
                com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDED);
        Map<Long, SubscriberServices> SuspendedServices = new HashMap<Long, SubscriberServices>();
        if (suspendedServices == null)
        {
            suspendedServices = new HashMap<Long, SubscriberServices>();
        }
        for (SubscriberServices subService : suspendedServices.values())
        {
            if (SuspendReasonEnum.SUBSCRIBERSUSPENSION.equals(subService.getSuspendReason()))
            {
                SuspendedServices.put(subService.getServiceId(), subService);
            }
        }
        return suspendedServices;
    }


    public void resetBackupServices()
    {
        servicesBackup_ = null;
    }


    /**
     * Set of service Ids newly selected by the subscriber.
     * 
     * @return
     */
    public Set<ServiceFee2ID> getIntentToProvisionServiceIds()
	  {
		 //HashSet<Long> selectedServices = new HashSet<Long>();
		 HashSet<ServiceFee2ID> selectedServices = new HashSet<ServiceFee2ID>();
		 Collection<SubscriberServices> col = this.getIntentToProvisionServices();
		 for (SubscriberServices ss : col)
		 {
			 //selectedServices.add(Long.valueOf(ss.getServiceId()));
			 selectedServices.add(new ServiceFee2ID(ss.getServiceId(), ss.getPath()));
		 }
		 return selectedServices;
	  }

    /**
     * @deprecated Use {@link 
     *             com.redknee.app.crm.bean.Subscriber.getIntentToProvisionServices
     *             (Context)} if possible
     */
    @Deprecated
    @Override
    public Set getIntentToProvisionServices()
    {
        return getIntentToProvisionServices(getContext());
    }

    /**
     * All set of subscriber services that should be provisioned. If it is null,
     * then it would be all non unprovisioned subscriber services
     * 
     * @return set => set of subscriberservices
     */
    public Set<SubscriberServices> getIntentToProvisionServices(Context ctx)
    {
        if (ctx == null)
        {
            return intentToProvisionServices_;
        }
        
        if (intentToProvisionServices_ == null)
        {
            final Map<ServiceFee2ID, SubscriberServices> provisionedServices = SubscriberServicesSupport.getSubscribersServices(ctx, this.getId());
            Set<SubscriberServices> services = new HashSet<SubscriberServices>();
            Iterator<Entry<ServiceFee2ID, SubscriberServices>>  iter = provisionedServices.entrySet().iterator();

            while (iter.hasNext())
            {
                final Map.Entry<ServiceFee2ID, SubscriberServices> entry = iter.next();
                services.add(entry.getValue());
            }
            this.setIntentToProvisionServices(services);
        }

        return intentToProvisionServices_;
    }

    public Set<ServiceFee2ID> getIntentToProvisionServices(Context ctx, ServiceTypeEnum serviceType) throws HomeException
	  {
		  if (intentToProvisionServicesByType_ == null)
		  {
			  intentToProvisionServicesByType_ = new HashMap<ServiceTypeEnum, Set<ServiceFee2ID>>();
		  }

		  if (intentToProvisionServicesByType_.get(serviceType) == null && ctx != null)
		  {
			  lazyLoadServicesOfType(ctx, getIntentToProvisionServiceIds(), intentToProvisionServicesByType_, serviceType);
		  }

		  Set<ServiceFee2ID> result = intentToProvisionServicesByType_.get(serviceType);
		  if (result == null)
		  {
			  return new HashSet<ServiceFee2ID>();
		  }

		  return result;
	  }


    public boolean hasIntentToProvisionServiceOfType(Context ctx, ServiceTypeEnum serviceType) throws HomeException
	  {
		  if (intentToProvisionServicesByType_ == null)
		  {
			  intentToProvisionServicesByType_ = new HashMap<ServiceTypeEnum, Set<ServiceFee2ID>>();
		  }

		  Set<ServiceFee2ID> svcs = intentToProvisionServicesByType_.get(serviceType);
		  if (svcs != null)
		  {
			  return svcs.size() > 0;
		  }

		  Collection<ServiceFee2ID> services = getIntentToProvisionServiceIds();
		  
		  List<Long> serviceIds = new ArrayList<Long>();
		  for (ServiceFee2ID service : services) {
			Long serviceId = service.getServiceId();
			serviceIds.add(serviceId);
		}
		  
		  return ServiceSupport.hasServiceOfType(ctx, serviceIds, serviceType);
	  }
    @Override
    public void setIntentToProvisionServices(Set intentToProvisionServices) throws IllegalArgumentException
    {
        super.setIntentToProvisionServices(intentToProvisionServices);
        if (intentToProvisionServicesByType_ != null)
        {
            intentToProvisionServicesByType_.clear();
        }
    }

    public void addServiceToIntentToProvisionService(Context ctx, SubscriberServices service) throws IllegalArgumentException
    {
        Set intentToProvisionServices = getIntentToProvisionServices(ctx);
        if (intentToProvisionServices != null)
        {
            intentToProvisionServices.add(service);
        }
    }

    public void removeServiceFromIntentToProvisionServices(Context ctx, long subServiceId) throws IllegalArgumentException
    {
        Set<SubscriberServices> intentToProvisionServices = getIntentToProvisionServices(ctx);
        Set<SubscriberServices> modified = new HashSet<SubscriberServices>();
        for(SubscriberServices subService: intentToProvisionServices)
        {
            if(subService.getServiceId() == subServiceId)
                continue;
            modified.add(subService);
        }
        setIntentToProvisionServices(modified);
    }
    
    /**
     * 
     * @param ctx
     * @param service
     * @throws IllegalArgumentException
     * @Deprecated Use the other method {@link #removeServiceFromIntentToProvisionServices(Context, long)}
     */
    @Deprecated 
    public void removeServiceFromIntentToProvisionServices(Context ctx, SubscriberServices service) throws IllegalArgumentException
    {
        Set intentToProvisionServices = getIntentToProvisionServices(ctx);
        if (intentToProvisionServices != null)
        {
            intentToProvisionServices.remove(service);
        }
    }

    /**
     * It will be return a set of all non unprovisioned state subscriber
     * services This method should be used by any type of tasks that try update
     * sub. services which doesn't get initiated by the UI
     * 
     * @return
     */
    @Deprecated
    public Set getAllNonUnprovisionedStateServices()
    {

        return this.getIntentToProvisionServices();
    }

    public boolean isPostpaid()
    {
        return SubscriberTypeEnum.POSTPAID.equals(getSubscriberType());
    }

    public boolean isPrepaid()
    {
        return SubscriberTypeEnum.PREPAID.equals(getSubscriberType());
    }

    public boolean isPooled(final Context ctx)
    {
        final String groupMSISDN = getGroupMSISDN(ctx);
        return groupMSISDN != null && groupMSISDN.length() > 0;
    }

    public boolean isPooledGroupLeader(final Context ctx)
    {
        boolean result;
        final Account account = SubscriberSupport.lookupAccount(ctx, this.getBAN());
        // new Pool leader subscriptions are in the group account
        result = account.isPooled(ctx) && !account.isIndividual(ctx);
        return result;
    }

    public boolean isPooledMemberSubscriber(final Context ctx)
    {
        boolean result;
        final Account account = SubscriberSupport.lookupAccount(ctx, this.getBAN());
        // new Pool members subscriptions are in the Subscriber Account
        result = isPooled(ctx) && account.isIndividual(ctx);
        return result;
    }

    public boolean isGroupOrGroupPooledMemberSubscriber(final Context ctx) throws HomeException
    {
        boolean result;
        final Account account = SubscriberSupport.lookupAccount(ctx, this.getBAN());
        if (account.isIndividual(ctx) && !account.getParentBAN().equals(Account.DEFAULT_PARENTBAN))
        {
            return true;
        }

        return false;
    }
    
    public boolean isConversion(final Subscriber otherSub)
    {
        boolean conversion = true;
        if (otherSub != null && otherSub.getSubscriberType().getIndex() == getSubscriberType().getIndex())
        {
            conversion = false;
        }
        return conversion;
    }

    public boolean isSameSubscriberType(final Subscriber otherSub)
    {
        return !isConversion(otherSub);
    }

    /**
     * 
     * @param ctx
     * @param fees
     * @param nextRecurringChargeDate
     * @return
     */
    public Collection<ServiceFee2> getChargeableFees(Context ctx, Map<ServiceFee2ID, ServiceFee2> fees, Date nextRecurringChargeDate)
    {
        Collection<ServiceFee2ID> chargableServices = getChargableServices(ctx,nextRecurringChargeDate);
        Collection<ServiceFee2> result = new ArrayList<ServiceFee2>();
        for (ServiceFee2ID key : chargableServices)
        {
            if (fees.containsKey(key))
            {
                result.add(fees.get(key));
            }
        }

        return result;
    }

    
    public Collection<ServiceFee2> getChargeableFees(Context ctx, Map<ServiceFee2ID, ServiceFee2> fees)
    {
    	return getChargeableFees(ctx, fees, null);
    }

    public Collection<ServiceFee2ID> getChargableServices(Context ctx, Date nextRecurringChargeDate )
	   {
		   // this inlcudes both mandatory and optional services selected
		   //final Collection<Long> selectedServices = getServices(ctx,nextRecurringChargeDate);
		   final Collection<ServiceFee2ID> selectedServices = getServices(ctx,nextRecurringChargeDate);
		   // this includes optional services that are not chargable due to being
		   // outside the
		   // start/end date
		   final Collection<Long> nonChargableServices = SubscriberServicesSupport
				   .getSubscriberNonChargableOptionalServices(ctx, getId());

		   selectedServices.removeAll(nonChargableServices);

		   return selectedServices;
	   }

	   public Collection<ServiceFee2ID> getChargableServices(Context ctx)
	   {
		   return getChargableServices(ctx,null);
	   }

    /**
     * @deprecated, by introducing PrincipalAware interface
     */
    @Override
    public String getAgent()
    {
        return getUser();
    }

    /**
     * @deprecated, by introducing PrincipalAware interface
     */
    @Override
    public void setAgent(final String usr)
    {
        setUser(usr);
    }

    /*
     * (non-Javadoc)
     * @see com.redknee.app.crm.bean.PrincipalAware#getPrincipal()
     */
    @Override
    public Principal getPrincipal()
    {
        return principal_;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.redknee.app.crm.bean.PrincipalAware#setPrincipal(java.security.Principal
     * )
     */
    @Override
    public void setPrincipal(final Principal usr)
    {
        principal_ = usr;
        if (usr != null)
        {
            final String userName = usr.getName();
            if (userName != null)
            {
                setAgent(userName);
            }
        }
    }

    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getSuspendedServices(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public Map getSuspendedServices()
    {
        return getSuspendedServices(getContext());
    }

    /**
     * Return a map of <ServiceId, ServiceFee> for all Suspended services for
     * this Subscriber.
     */
    public Map getSuspendedServices(Context ctx)
    {
        Collection coll = null;
        
        if (ctx == null)
        {
            return suspendedServices_;
        }
        
        if (suspendedServices_ == null)
        {
            coll = SubscriberServicesSupport.getSuspendedServices(ctx, getId());
            suspendedServices_ = new HashMap<Long, SubscriberServices>();

            try
            {
                final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
                if (ppv!=null)
                {
                    final Map<ServiceFee2ID, ServiceFee2> serviceFees = ppv.getServiceFees(ctx);
                    final Iterator iter = coll.iterator();
                    SubscriberServices entity = null;
                    while (iter.hasNext())
                    {
                        entity = (SubscriberServices) iter.next();
                        final Long id = Long.valueOf(entity.getServiceId());
                        final ServiceFee2 sf2 = (ServiceFee2) serviceFees.get(id);

                        if (!suspendedServices_.containsKey(sf2))
                        {
                            suspendedServices_.put(id, sf2);
                        }

                    }
                }
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, "Error Trying to get the suspended services for sub " + getId(), e).log(ctx);
            }
        }

        return suspendedServices_;
    }

    /**
     * Return a map of <ServiceId, ServiceFee> for all Suspended services for
     * this Subscriber.
     */
    public Map<Long, SubscriberServices> getSuspendedSubscriberServices(Context ctx)
    {
        Collection coll = null;
        
        coll = SubscriberServicesSupport.getSuspendedServices(ctx, getId());
        HashMap<Long, SubscriberServices> suspendedServices = new HashMap<Long, SubscriberServices>();

        try
        {
            final PricePlanVersion ppv = getRawPricePlanVersion(ctx);
            if (ppv!=null)
            {
                final Map serviceFees = ppv.getServiceFees(ctx);
                final Iterator iter = coll.iterator();
                SubscriberServices entity = null;
                while (iter.hasNext())
                {
                    entity = (SubscriberServices) iter.next();
                    final Long id = Long.valueOf(entity.getServiceId());
                    final ServiceFee2 sf2 = (ServiceFee2) serviceFees.get(id);

                    if (!suspendedServices.containsKey(id))
                    {
                        suspendedServices.put(id, entity);
                    }

                }
            }
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Error Trying to get the suspended services for sub " + getId(), e).log(ctx);
        }

        return suspendedServices;
    }
    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getSuspendedBundles(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public Map getSuspendedBundles()
    {
        return getSuspendedBundles(getContext());
    }

    public Map getSuspendedBundles(Context ctx)
    {
        Collection coll = null;
        
        if (ctx == null)
        {
            return suspendedBundles_;
        }

        if (suspendedBundles_ == null)
        {
            coll = SuspendedEntitySupport.getSuspendedEntities(getContext(), getId(), BundleFee.class);
            suspendedBundles_ = new HashMap();

            Map bundleFees = null;
            final Iterator iter = coll.iterator();
            SuspendedEntity entity = null;
            while (iter.hasNext())
            {
                try
                {
                    entity = (SuspendedEntity) iter.next();
                    final Long id = Long.valueOf(entity.getIdentifier());
                    if (bundleFees == null)
                    {
                        bundleFees = SubscriberBundleSupport.getAllBundles(ctx, this);
                    }

                    final BundleFee bundle = (BundleFee) bundleFees.get(id);

                    if (!suspendedBundles_.containsKey(id))
                    {
                        suspendedBundles_.put(id, bundle);
                    }
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, "Error Trying to get the BundleFee " + entity + ", sub " + getId(), e)
                        .log(getContext());
                }
            }
        }

        return suspendedBundles_;
    }

    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getSuspendedAuxServices(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public Map<Long, Map<Long, SubscriberAuxiliaryService>> getSuspendedAuxServices()
    {
        return getSuspendedAuxServices(getContext());
    }

    public Set<SubscriberAuxiliaryService> getSuspendedAuxServicesList(Context ctx)
    {
        Set<SubscriberAuxiliaryService> services = new HashSet<SubscriberAuxiliaryService>();
        
        for (Long id : getSuspendedAuxServices(ctx).keySet())
        {
            services.addAll(getSuspendedAuxServices(ctx).get(id).values());
        }
        return services;
    }

    public Map<Long, Map<Long, SubscriberAuxiliaryService>> getSuspendedAuxServices(Context ctx)
    {
        Collection coll = null;
        
        if (ctx == null)
        {
            return suspendedAuxServices_;    
        }
        
        if (suspendedAuxServices_ == null)
        {
            coll = SuspendedEntitySupport.getSuspendedEntities(ctx, getId(), AuxiliaryService.class);
            suspendedAuxServices_ = new HashMap<Long, HashMap<Long, SubscriberAuxiliaryService>>();
            final Iterator iter = coll.iterator();
            SuspendedEntity entity = null;
            while (iter.hasNext())
            {
                try
                {
                    entity = (SuspendedEntity) iter.next();
                    final Long id = Long.valueOf(entity.getIdentifier());

                    if (!suspendedAuxServices_.containsKey(id))
                    {
                        suspendedAuxServices_.put(id, new HashMap<Long, SubscriberAuxiliaryService>());
                    }
                    SubscriberAuxiliaryService association = SubscriberAuxiliaryServiceSupport
                        .getSubscriberAuxiliaryService(ctx, getId(), entity.getIdentifier(),
                            entity.getSecondaryIdentifier());
                    if (association == null)
                    {
                        association = new SubscriberAuxiliaryService();
                        association.setAuxiliaryServiceIdentifier(entity.getIdentifier());
                        association.setSecondaryIdentifier(entity.getSecondaryIdentifier());
                        association.setSubscriberIdentifier(entity.getSubscriberId());
                        association.setProvisioned(true);
                        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryServicById(ctx,
                            entity.getIdentifier());
                        association.setType(service.getType());
                    }
                    ((HashMap<Long, SubscriberAuxiliaryService>) suspendedAuxServices_.get(id)).put(
                        Long.valueOf(entity.getSecondaryIdentifier()), association);
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, "Error Trying to get the AuxiliaryService " + entity + ", sub " + getId(), e)
                        .log(ctx);
                }
            }

        }

        return suspendedAuxServices_;
    }

    public int getNumSuspendedAuxService(Context ctx)
    {
        int result = 0;
        if (getSuspendedAuxServices(ctx) != null)
        {
            for (final Map<Long, SubscriberAuxiliaryService> associations : getSuspendedAuxServices(ctx).values())
            {
                result += associations.size();
            }
        }
        return result;
    }

    /**
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getSuspendedPackages(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public Map getSuspendedPackages()
    {
        return getSuspendedPackages(getContext());
    }

    public Map getSuspendedPackages(Context ctx)
    {
        Collection coll = null;
        
        if (ctx == null)
        {
            return suspendedPackages_;    
        }
        
        if (suspendedPackages_ == null)
        {
            coll = SuspendedEntitySupport.getSuspendedEntities(ctx, getId(), ServicePackage.class);
            suspendedPackages_ = new HashMap();
            final Iterator iter = coll.iterator();
            SuspendedEntity entity = null;
            while (iter.hasNext())
            {
                try
                {
                    entity = (SuspendedEntity) iter.next();
                    final Integer id = Integer.valueOf((int) entity.getIdentifier());

                    final Home packHome = (Home) ctx.get(ServicePackageHome.class);

                    final ServicePackage svc = (ServicePackage) packHome.find(ctx, id);

                    if (!suspendedPackages_.containsKey(id))
                    {
                        suspendedPackages_.put(id, svc);
                    }
                }
                catch (final Exception e)
                {
                    new MajorLogMsg(this, "Error Trying to get the ServicePackage " + entity + ", sub " + getId(), e)
                        .log(ctx);
                }
            }
        }

        return suspendedPackages_;
    }

    public SubscriberSuspendedEntities getNewlySuspendedEntities()
    {
        if (newlySuspendedEntities_ == null)
        {
            newlySuspendedEntities_ = new SubscriberSuspendedEntities();
        }
        
        return newlySuspendedEntities_; 
    }

    public void insertSuspendedAuxService(final Context ctx, final SubscriberAuxiliaryService serv)
        throws HomeException
    {
        final Long id = Long.valueOf(serv.getAuxiliaryServiceIdentifier());
        

        if (!getSuspendedAuxServices(ctx).containsKey(id))
        {
            getSuspendedAuxServices(ctx).put(id, new HashMap<Long, SubscriberAuxiliaryService>());
            getNewlySuspendedEntities().getSuspendedAuxServices().put(id, new HashMap<Long, SubscriberAuxiliaryService>());
        }

        if (!getSuspendedAuxServices(ctx).get(id).containsKey(serv.getSecondaryIdentifier()))
        {
            
            SuspendedEntitySupport.createSuspendedEntity(ctx, getId(), serv.getAuxiliaryServiceIdentifier(),
                    serv.getSecondaryIdentifier(), AuxiliaryService.class);

           
            ((Map<Long, SubscriberAuxiliaryService>) getNewlySuspendedEntities().getSuspendedAuxServices().get(id)).put(serv.getSecondaryIdentifier(), serv);
        }

        final Map<Long, SubscriberAuxiliaryService> associations = getSuspendedAuxServices(ctx).get(id);
        associations.put(Long.valueOf(serv.getSecondaryIdentifier()), serv);
    }

    public void insertSuspendedPackage(final Context ctx, final ServicePackage pack) throws HomeException
    {
        SuspendedEntitySupport.createSuspendedEntity(ctx, getId(), pack.getIdentifier(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServicePackage.class);
        getSuspendedPackages(ctx).put(XBeans.getIdentifier(pack), pack);

        getNewlySuspendedEntities().getSuspendedPackages().put(XBeans.getIdentifier(pack), pack);
    }

    public void insertSuspendedBundles(final Context ctx, final BundleFee bundle) throws HomeException
    {
        SuspendedEntitySupport.createSuspendedEntity(ctx, getId(), bundle.getId(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, BundleFee.class);
        getSuspendedBundles(ctx).put(XBeans.getIdentifier(bundle), bundle);

        getNewlySuspendedEntities().getSuspendedBundles().put(XBeans.getIdentifier(bundle), bundle);
    }

    /**
     * Inserts a service in the suspended services collection and Modifies the
     * service state to suspended on the SubscriberServices table.
     * 
     * @param ctx
     *            the operating context
     * @param svcId
     *            the identifier for the service fee to add
     * @param suspendReason
     *            the reason for suspension
     * @throws HomeException
     *             in case there is a problem trying to get the subscriber
     *             service or modifying it.
     */
    public void insertSuspendedService(final Context ctx, final Long svcId, SuspendReasonEnum suspendReason)
        throws HomeException
    {
        Map newPricePlanFees = getPricePlan(ctx).getServiceFees(ctx);
        ServiceFee2 fee = (ServiceFee2) newPricePlanFees.get(svcId);
        if (fee != null)
        {
            insertSuspendedService(ctx, fee, suspendReason);
        }
        else
        {
            LogSupport.debug(ctx, this, "The Service with identifier " + svcId
                + " was not found in set of Subscriber's price plan, for sub=" + getId()
                + ". Will abort suspending this Service.");
        }
    }

    /**
     * Inserts a service in the suspended services collection and Modifies the
     * service state to suspended on the SubscriberServices table.
     * 
     * @param ctx
     *            the operating context
     * @param svc
     *            the service fee to add
     * @throws HomeException
     *             in case there is a problem trying to get the subscriber
     *             service or modifying it.
     */
    public void insertSuspendedService(final Context ctx, final ServiceFee2 svc, SuspendReasonEnum suspendReason)
        throws HomeException
    {

        SuspendedEntitySupport.createSuspendedEntity(ctx, getId(), svc.getServiceId(),
                SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class);
        final SubscriberServices subscriberService = SubscriberServicesSupport.createOrModifySubcriberService(ctx,
            this, svc.getServiceId(), ServiceStateEnum.SUSPENDED, suspendReason);

        if (subscriberService != null)
        {
            getSuspendedServices(ctx).put(XBeans.getIdentifier(svc), svc);
        }

        getNewlySuspendedEntities().getSuspendedServices().put(XBeans.getIdentifier(svc), svc);
    }

    public void removeSuspendedAuxService(final Context ctx, final SubscriberAuxiliaryService serv)
        throws HomeException
    {
        SuspendedEntitySupport.removeSuspendedEntity(ctx, getId(), serv.getAuxiliaryServiceIdentifier(),
            serv.getSecondaryIdentifier(), AuxiliaryService.class);
        final Map<Long, SubscriberAuxiliaryService> associations = getSuspendedAuxServices(ctx).get(
            Long.valueOf(serv.getAuxiliaryServiceIdentifier()));
        if(associations != null)
        {
        	associations.remove(Long.valueOf(serv.getSecondaryIdentifier()));
        }
    }

    public void removeSuspendedPackage(final Context ctx, final ServicePackage pack) throws HomeException
    {
        SuspendedEntitySupport.removeSuspendedEntity(ctx, getId(), pack.getIdentifier(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServicePackage.class);
        getSuspendedPackages(ctx).remove(XBeans.getIdentifier(pack));
    }

    public void removeSuspendedBundles(final Context ctx, final BundleFee bundle) throws HomeException
    {
        SuspendedEntitySupport.removeSuspendedEntity(ctx, getId(), bundle.getId(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, BundleFee.class);
        getSuspendedBundles(ctx).remove(XBeans.getIdentifier(bundle));
    }

    /**
     * Removes a service from the suspended services collection and Modifies the
     * service state to PROVISIONED on the SubscriberServices table.
     * 
     * @param ctx
     *            the operating context
     * @param svc
     *            the service fee to add
     * @throws HomeException
     *             in case there is a problem tryng to get the subcriber service
     *             or modifying it.
     */
    public void removeSuspendedService(final Context ctx, final Service svc) throws HomeException
    {
        final SubscriberServices subscriberService = SubscriberServicesSupport.createOrModifySubcriberService(ctx,
            this, svc.getID(), ServiceStateEnum.PROVISIONED, SuspendReasonEnum.NONE);

        if (subscriberService != null)
        {
            getSuspendedServices(ctx).remove(XBeans.getIdentifier(svc));
        }
        // Removing it suspend enity table
        SuspendedEntitySupport.removeSuspendedEntity(ctx, getId(), svc.getID(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class);

        // The old implementation (prior to SR) wanted this to mean "activate"
        // Services
        ServiceRefactoring_RefactoringClass.doesRemoveSuspendedServiceMeanDeleteFromSuspendedEntityOrActivate(true);
    }

    /**
     * Removes a service from the suspended services collection and Modifies the
     * service state to PROVISIONED on the SubscriberServices table.
     * 
     * @param ctx
     *            the operating context
     * @param svc
     *            the service fee to add
     * @throws HomeException
     *             in case there is a problem tryng to get the subcriber service
     *             or modifying it.
     */
    public void removeSuspendedService(final Context ctx, final ServiceFee2 svc) throws HomeException
    {
        final SubscriberServices subscriberService = SubscriberServicesSupport.createOrModifySubcriberService(ctx,
            this, svc.getServiceId(), ServiceStateEnum.PROVISIONED, SuspendReasonEnum.NONE);

        if (subscriberService != null)
        {
            getSuspendedServices(ctx).remove(XBeans.getIdentifier(svc));
        }
        // Removing it suspend enity table
        SuspendedEntitySupport.removeSuspendedEntity(ctx, getId(), svc.getServiceId(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class);

        // The old implementation (prior to SR) wanted this to mean "activate"
        // Services
        ServiceRefactoring_RefactoringClass.doesRemoveSuspendedServiceMeanDeleteFromSuspendedEntityOrActivate(true);
    }

    /**
     * Removes a service from the suspended services collection and removes the
     * service from the SubscriberServices table.
     * 
     * @param ctx
     *            the operating context
     * @param svc
     *            the service fee to add
     * @throws HomeException
     *             in case there is a problem trying to get the subscriber
     *             service or modifying it.
     */
    public void deleteSuspendedService(final Context ctx, final ServiceFee2 svc) throws HomeException
    {
        int result = SubscriberServicesSupport.deleteSubscriberServiceRecord(ctx, getId(), svc.getServiceId(), svc.getPath());

        if (result != -1)
        {
            getSuspendedServices(ctx).remove(XBeans.getIdentifier(svc));
        }
        // Removing it suspend enity table
        SuspendedEntitySupport.removeSuspendedEntity(ctx, getId(), svc.getServiceId(),
            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, ServiceFee2.class);
        // Use this method to mean "delete all traces of this service". But
        // perhaps this would be better done in a blanket call
        // instead of one only for the suspended services?
        ServiceRefactoring_RefactoringClass.doesRemoveSuspendedServiceMeanDeleteFromSuspendedEntityOrActivate(false);
    }

    /**
     * @return Returns the pricePlanBackup_.
     */
    public long getPricePlanBackup()
    {
        return pricePlanBackup_;
    }

    /**
     * @param pricePlanBackup
     *            The pricePlanBackup_ to set.
     */
    public void setPricePlanBackup(final long pricePlanBackup)
    {
        this.pricePlanBackup_ = pricePlanBackup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionType getSubscriptionType(final Context ctx)
    {
        return SubscriptionType.getSubscriptionType(ctx, getSubscriptionType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionClass getSubscriptionClass(Context ctx)
    {
        return SubscriptionClass.getSubscriptionClass(ctx, getSubscriptionClass());
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getMsisdnEntryType(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public int getMsisdnEntryType()
    {
        return getMsisdnEntryType(getContext());
    }

    public int getMsisdnEntryType(Context ctx)
    {
        int entryType = super.getMsisdnEntryType();
        if (entryType == AbstractSubscriber.DEFAULT_MSISDNENTRYTYPE)
        {
            String msisdn = getMSISDN();
            entryType = lookupMsisdnEntryType(ctx, msisdn, MsisdnEntryTypeEnum.MSISDN_GROUP_INDEX);
        }
        return entryType;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getDataMsisdnEntryType(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public int getDataMsisdnEntryType()
    {
        return getDataMsisdnEntryType(getContext());
    }

    public int getDataMsisdnEntryType(Context ctx)
    {
        int entryType = super.getDataMsisdnEntryType();
        if (entryType == AbstractSubscriber.DEFAULT_DATAMSISDNENTRYTYPE)
        {
            String msisdn = getDataMSISDN();
            entryType = lookupMsisdnEntryType(ctx, msisdn, MsisdnEntryTypeEnum.MSISDN_GROUP_INDEX);
        }
        return entryType;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link
     *             com.redknee.app.crm.bean.Subscriber.getFaxMsisdnEntryType(
     *             Context)} if possible
     */
    @Deprecated
    @Override
    public int getFaxMsisdnEntryType()
    {
        return getFaxMsisdnEntryType(getContext());
    }

    public int getFaxMsisdnEntryType(Context ctx)
    {
        int entryType = super.getFaxMsisdnEntryType();
        if (entryType == AbstractSubscriber.DEFAULT_FAXMSISDNENTRYTYPE)
        {
            String msisdn = getFaxMSISDN();
            entryType = lookupMsisdnEntryType(ctx, msisdn, MsisdnEntryTypeEnum.MSISDN_GROUP_INDEX);
        }
        return entryType;
    }

    private int lookupMsisdnEntryType(Context ctx, String msisdn, int defaultEntryType)
    {
        if (msisdn != null && msisdn.length() > 0)
        {
            try
            {
                Msisdn msisdnRecord = MsisdnSupport.getMsisdn(ctx, msisdn);
                if (msisdnRecord == null)
                {
                    // MSISDN doesn't exist, could be intended to be external or
                    // custom but we will assume custom
                    return MsisdnEntryTypeEnum.CUSTOM_ENTRY_INDEX;
                }
                else
                {
                    if (msisdnRecord.getBAN().equals(getBAN()))
                    {
                        return MsisdnEntryTypeEnum.AQUIRED_MSISDNS_INDEX;
                    }
                    if (msisdnRecord.isExternal())
                    {
                        return MsisdnEntryTypeEnum.EXTERNAL_INDEX;
                    }
                    else
                    {
                        return MsisdnEntryTypeEnum.MSISDN_GROUP_INDEX;
                    }
                }
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, "Encountered a HomeException while trying to retreive Msisdn record for [msisdn="
                    + msisdn + "]", e).log(ctx);
                return defaultEntryType;
            }
        }
        else
        {
            return defaultEntryType;
        }
    }

    public String getResourceID(final Context ctx)
    {
        if (super.getResourceID() == null)
        {
            if (this.getId() == null || this.getId().length() == 0)
            {
                super.setResourceID("");
            }
            else
            {
                try
                {
                    final EQ condition = new EQ(ResourceDeviceXInfo.SUBSCRIPTION_ID, this.getId());
                    final ResourceDevice resource = HomeSupportHelper.get(ctx).findBean(ctx, ResourceDevice.class,
                        condition);
                    if (resource != null)
                    {
                        super.setResourceID(resource.getResourceID());
                    }
                    else
                    {
                        super.setResourceID("");
                    }
                }
                catch (HomeException e)
                {
                    Logger.minor(ctx, this,
                        "Unable to locate ResourceDevice corresponding to subscription [" + this.getId() + "]", e);
                }
            }
        }

        return super.getResourceID();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated use method with Context parameter. This method is only for
     *             auto-generated WebControl
     */
    @Deprecated
    @Override
    public String getResourceID()
    {
        return getResourceID(getContext());
    }

    /**
     * Return the Identifier of the Group Account to which this Subscription
     * belongs.
     * 
     * @param ctx
     * @return Could return Null if errors while searching for Group Account
     */
    public String getGroupAccountBAN(Context ctx)
    {
        // TODO victor: check this out
        return TopologySupport.findGroupAccountId(ctx, this);
    }

    /**
     * Return the Pool Subscriber (bean) for this subscriber if one exists, and
     * returns null otherwise.
     * 
     * @param context
     *            The operating context.
     * @return The group pool Subscriber of the subscriber if one exists; null
     *         otherwise.
     */
    public Subscriber getPoolSubscription(Context ctx) throws HomeException
    {
        Subscriber poolSubscriber = null;
        try
        {
            String poolMsisdn = getAccount(ctx).getPoolMSISDN();
            new DebugLogMsg(this, "Retrieving the Pool Subscriber to Subscriber id=" + getId(), null).log(ctx);
            poolSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, poolMsisdn, getSubscriptionType(),
                new Date());
        }
        catch (HomeException e)
        {
            throw new HomeException("Failed to look up the Pool Subscription for Subscription id=" + getId(), e);
        }
        return poolSubscriber;
    }

    public void setBundleOverUsage(Map map)
    {
        this.bundleOverUsage = map;
    }

    public Map getBundleOverUsage()
    {
        return this.bundleOverUsage;
    }

    public Set<Discount> getDiscounts(Context ctx) throws HomeException
    {
        try
        {
            final Set<Discount> discountSet = new HashSet<Discount>();
            Collection<SubscriberAuxiliaryService> auxillaryServices = getAuxiliaryServices(ctx);
            Visitors.forEach(ctx, auxillaryServices, (new Visitor() {

                @Override
                public void visit(Context ctx, Object bean) throws AgentException, AbortVisitException
                {
                    try
                    {
                        final SubscriberAuxiliaryService subAxuService = (SubscriberAuxiliaryService) bean;
                        final Date now = new Date();
                        final Date serviceStartDate;
                        final Date serviceEndDate;
                        {
                            CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
                            serviceStartDate = calendarSupport.getDateWithNoTimeOfDay(subAxuService.getStartDate());
                            serviceEndDate = calendarSupport.getDateWithLastSecondofDay(subAxuService.getEndDate());
                        }
                        if (subAxuService.isProvisioned() && now.after(serviceStartDate) && now.before(serviceEndDate))
                        {
                            final AuxiliaryService auxService = subAxuService
                                .getAuxiliaryService(ctx);
                            if (AuxiliaryServiceTypeEnum.Discount == auxService.getType())
                            {
                                boolean isEnableThreshold = DiscountAuxSvcExtension.DEFAULT_ENABLETHRESHOLD;
                                double discountPercentage = DiscountAuxSvcExtension.DEFAULT_DISCOUNTPERCENTAGE;
                                long minimumTotalChargeThreshold = DiscountAuxSvcExtension.DEFAULT_MINIMUMTOTALCHARGETHRESHOLD;

                                DiscountAuxSvcExtension discountAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxService, DiscountAuxSvcExtension.class);
                                if (discountAuxSvcExtension!=null)
                                {
                                    isEnableThreshold = discountAuxSvcExtension.isEnableThreshold();
                                    discountPercentage = discountAuxSvcExtension.getDiscountPercentage();
                                    minimumTotalChargeThreshold = discountAuxSvcExtension.getMinimumTotalChargeThreshold();
                                }
                                
                                Discount discount;
                                try
                                {
                                    discount = (Discount) XBeans.instantiate(Discount.class, ctx);
                                }
                                catch (Throwable t)
                                {
                                    discount = new Discount();
                                }
                                discount.setName(auxService.getName());
                                discount.setAdjustmentType(auxService.getAdjustmentType());
                                discount.setDiscountPercentage(discountPercentage);
                                discount.setEnableThreshold(isEnableThreshold);
                                discount.setMinimumTotalChargeThreshold(minimumTotalChargeThreshold);

                                discountSet.add(discount);
                            }
                        }
                    }
                    catch (Throwable t)
                    {
                        throw new AgentException(t.getMessage(), t);
                    }
                }
            }));
            return discountSet;
        }
        catch (Throwable t)
        {
            throw new HomeException("Could not return discounts for Subcriber[" + getId() + "]. Error ["
                + t.getMessage() + "]", t);
        }
    }

    
     public Collection<SubscriberServices> getCLTCSubscriberServices(Context ctx)
     {
         Collection subscriberCLTCServices = null;
         if (cltcServices_ == null)
         {
              cltcServices_ = new HashSet();

             try
             {
                 subscriberCLTCServices = SubscriberServicesSupport.getCLTCServices(ctx, getId());
                 final Iterator iter = subscriberCLTCServices.iterator();
                 SubscriberServices entity = null;
                 while (iter.hasNext())
                 {
                     entity = (SubscriberServices) iter.next();
                     cltcServices_.add(entity);
                 }
             }
             catch (HomeException e)
             {
                 new MajorLogMsg(this, "Error Trying to get the CLTC services for sub " + getId(), e).log(ctx);
             }
         }

         return cltcServices_;     
     }

     public Collection getCLTCServices(Context ctx)
     {
            Set<Long> cltcServices = new HashSet<Long>();
            final Iterator iter = getCLTCSubscriberServices(ctx).iterator();
            SubscriberServices entity = null;
            while (iter.hasNext())
            {
                entity = (SubscriberServices) iter.next();
                cltcServices.add( Long.valueOf(entity.getServiceId()));
            }

        return cltcServices;
    }
     
     public long getOverdraftBalanceLimit(Context ctx)
     {
         long result = 0;
         boolean found = false;
         
         OverdraftBalanceSubExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx, this, OverdraftBalanceSubExtension.class);
         
         if (extension!=null)
         {
             result = extension.getLimit(ctx);
         }
         else
         {
             try
             {
                 OverdraftBalanceSpidExtension spidExtension = HomeSupportHelper.get(ctx).findBean(ctx,
                         OverdraftBalanceSpidExtension.class,
                         new EQ(OverdraftBalanceSpidExtensionXInfo.SPID, Integer.valueOf(getSpid())));
                 if (spidExtension!=null)
                 {
                     result = spidExtension.getLimit();
                 }
             }
             catch (HomeException e)
             {
                 LogSupport.minor(ctx, this, "Unable to retrieve Overdraft Balance Limit SPID extension for SPID "
                         + getSpid() + ": " + e.getMessage(), e);
             }
         }
         
         return result;
     }
     
     
     public void setURCSOverdraftBalanceLimit(long limit)
     {
         urcsOverdraftBalanceLimit_ = limit;
     }
     
     public long getURCSOverdraftBalanceLimit(Context ctx)
     {
         synchronized (this)
         {
             if (urcsOverdraftBalanceLimit_ == SubscriberSupport.INVALID_VALUE)
             {
                 SubscriberSupport.updateSubscriberSummaryABM(ctx, this);
             }
         }
         return urcsOverdraftBalanceLimit_;
     }

     public void setOverdraftBalanceLimit(Context ctx, long limit)
    {
        OverdraftBalanceSubExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx, this, OverdraftBalanceSubExtension.class);
        Collection<Extension> extensions = getExtensions();
        
        if (extension!=null)
        {
            extension.setNewLimit(limit);
        }
        else
        {
            try
            {
                extension = (OverdraftBalanceSubExtension) XBeans.instantiate(OverdraftBalanceSubExtension.class, ctx);
            }
            catch (final Exception e)
            {
                extension = new OverdraftBalanceSubExtension();
                LogSupport
                        .minor(ctx, this, "Exception while XBeans.instantiate(OverdraftBalanceSubExtension.class)", e);
            }

            extension.setBAN(getBAN());
            extension.setSubId(getId());
            extension.setSpid(getSpid());
            extension.setNewLimit(limit);
            extensions.add(extension);
        }

        setSubExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));
    }

    public void resetMonthlySpendLimit(Context ctx) throws HomeException
    {
        final long currentUsage = this.getMonthlySpendAmount(ctx);
        final SubscriberProfileProvisionClient client;
        client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        if (client != null)
        {
            try
            {
                client.resetMonthlySpendUsage(ctx, this);
                this.setMonthlySpendAmount(SubscriberSupport.INVALID_VALUE);
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, this.getId(),
                        "Monthly usage reset. Old usage = " + formatAmount(ctx, currentUsage, this),
                        SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.USAGE_RESET);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this,
                        "Unable to add subscriber note for monthly spend limit reset for subscriber '" + this.getId()
                                + "': " + e.getMessage(), e);
            }
            catch (SubscriberProfileProvisionException exception)
            {
                final short resultBM = exception.getErrorCode();
                if (SubscriberProvisionResultCode.getProvisionResultCode(ctx)!=null)
                {
                    SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
                }
                final String err = "Unable to reset Monthly Usage due to an error on URCS: ErrorCode = " + resultBM;
                throw new HomeException(err);
            }
        }
        else
        {
            final String err = "Unable to reset Monthly Usage: SubscriberProfileProvisionClient not installed.";
            LogSupport.minor(ctx, this, err);
            throw new HomeException(err);
        }
    }


    public void resetGroupUsage(Context ctx) throws HomeException
    {
        final SubscriberProfileProvisionClient client;
        client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        if (client!=null)
        {
            try
            {
                client.resetGroupUsage(ctx, this);
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, this.getId(), "Group usage reset.",
                        SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.USAGE_RESET);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this,
                        "Unable to add subscriber note for group reset for subscriber '" + this.getId()
                                + "': " + e.getMessage(), e);
            }
            catch (SubscriberProfileProvisionException exception)
            {
                final short resultBM = exception.getErrorCode();
                if (SubscriberProvisionResultCode.getProvisionResultCode(ctx)!=null)
                {
                    SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
                }
                final String err = "Unable to reset Group Usage due to an error on URCS: ErrorCode = " + resultBM;
                throw new HomeException(err);
            }
        }
        else
        {
            final String err = "Unable to reset Group Usage: SubscriberProfileProvisionClient not installed.";
            LogSupport.minor(ctx, this, err);
            throw new HomeException(err);
        }
    }
    
    private String formatAmount(Context ctx, long amount, Subscriber sub)
    {
        String result = String.valueOf(amount);
        try 
        {
                Currency currency = (Currency) ((Home) ctx.get(CurrencyHome.class)).find(sub.getCurrency(ctx));
                if (currency != null)
                {
                    result = currency.formatValue(amount);
                }
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Failed to look up this subscribers's currency. Subscriber=" + sub.getId(), e).log(ctx);
        }
        return result;
    }

     
    public Collection getAuxServiceToBeReprovisioned() {
        return auxServiceToBeReprovisioned;
    }

    public void setAuxServiceToBeReprovisioned(
            Collection auxServiceToBeReprovisioned) {
        this.auxServiceToBeReprovisioned = auxServiceToBeReprovisioned;
    }   
     
    
    
    /***
     * 
     */
    public Date getFixedStopPricePlanDate(Context ctx)
    {
        Date result = null;
        boolean found = false;
        
        FixedStopPricePlanSubExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx, this, FixedStopPricePlanSubExtension.class);
        
        if (extension!=null)
        {
            result = extension.getEndDate();
        }
        
        return result;
    }
    
    public void setFixedStopPricePlanDate(Context ctx, Date endDate)
   {
       FixedStopPricePlanSubExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx, this, FixedStopPricePlanSubExtension.class);
       Collection<Extension> extensions = new ArrayList<Extension>(getExtensions());
       
       if (extension!=null)
       {
           extension.setEndDate(endDate);
       }
       else
       {
           try
           {
               extension = (FixedStopPricePlanSubExtension) XBeans.instantiate(FixedStopPricePlanSubExtension.class, ctx);
           }
           catch (final Exception e)
           {
               extension = new FixedStopPricePlanSubExtension();
               LogSupport
                       .minor(ctx, this, "Exception while XBeans.instantiate(OverdraftBalanceSubExtension.class)", e);
           }

           extension.setBAN(getBAN());
           extension.setSubId(getId());
           extension.setSpid(getSpid());
           extension.setEndDate(endDate);
           extensions.add(extension);
       }

       
       setSubExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));
   }
    
    /**
     * 
     * 
     */
    private SubscriberSuspendedEntities newlySuspendedEntities_ = null;

    private SubscriberAdvancedFeatures advancedFeatures_ = null;
    
    public static final long serialVersionUID = 2370116152981095408L;
    public static final EnumCollection SUBSCRIBERTYPE_COLLECTION = new CustomEnumCollection(
        SubscriberTypeEnum.POSTPAID, SubscriberTypeEnum.PREPAID);

    private static final Collection<SubscriberStateEnum> FINAL_STATES = Arrays.asList(SubscriberStateEnum.INACTIVE);

    /**
     * The operating context.
     */
    private transient Context context_;

    protected PricePlanVersion cachedPlanVersion_ = null;

    protected List<Throwable> exceptionList_ = new ArrayList<Throwable>();
    protected HTMLExceptionListener exceptionListener_;

    protected Collection provisionedAuxServiceIdsBackup_ = new HashSet();

    protected Collection<SubscriberAuxiliaryService> provisionedAuxServiceBackup_ = new HashSet<SubscriberAuxiliaryService>();
    
    protected Map<Long, SubscriberServices> provisionedServices;   

    protected Collection provisionedBundleIdsBackup_ = new HashSet();

    protected Collection provisionedPackageIdsBackup_ = new HashSet();

    /**
     * This two collection will be copied from services_ & provisionedServices_
     * when we load & store from db. it is acting as a backup for oldSubscriber.
     * To further clarify all these services sets: 1. getServices(), returns all
     * service selected + mandatory services in the price plan 2. Get
     * EligibleServices() returns sub set of getServices for startDate&EndDate
     * checking 3. provisionedServices, services actually provisioned on the
     * backend
     */
    protected transient Map<com.redknee.app.crm.bean.service.ServiceStateEnum, Map<Long, SubscriberServices>> servicesBackup_ = null;
    protected Set transientProvisionedServices_ = new HashSet();
    protected transient Set<ServiceFee2ID> services_ = null;
    protected transient Map<ServiceTypeEnum, Set<ServiceFee2ID>> servicesByType_ = null;
    protected transient Map<ServiceTypeEnum, Set<ServiceFee2ID>> intentToProvisionServicesByType_ = null;        

    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getMsisdn()
    {
        return getMSISDN();
    }
    
    public String getTimeZone()
    {
        if (getContext()!=null)
        {
            return getTimeZone(getContext());
        }
        else
        {
            return getTimeZone(ContextLocator.locate());
        }
    }

    public String getTimeZone(Context ctx)
    {
        try
        {
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, this.getSpid());
            return spid.getTimezone();
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve SPID. Returning default timezone: " + t.getMessage(), t);
            return TimeZone.getDefault().getID();
        }
    }

    public List getSupplementaryDataList()
    {
        return getSupplementaryDataList(getContext());
    }
    
    public List<SupplementaryData> getSupplementaryDataList(Context ctx)
    {
        if (supplementaryDataList_ == null || supplementaryDataSpid_ != getSpid())
        {
            if (ctx == null)
            {
                ctx = ContextLocator.locate();
            }
            
            try
            {
                supplementaryDataSpid_ = getSpid();
                List<SupplementaryData> result = new ArrayList<SupplementaryData>(); 
                
                And filter = new And();
                filter.add(new EQ(SupplementaryDataReqFieldsXInfo.SPID, Integer.valueOf(this.getSpid())));
                filter.add(new EQ(SupplementaryDataReqFieldsXInfo.ENTITY, Integer.valueOf(SupplementaryDataEntityEnum.SUBSCRIPTION_INDEX)));
                SupplementaryDataReqFields fields = HomeSupportHelper.get(ctx).findBean(ctx, SupplementaryDataReqFields.class, filter);
                if (fields!=null && fields.getFields()!=null && fields.getFields().size()>0)
                {
                    Map<String, SupplementaryData> supplementaryDataMap = new HashMap<String, SupplementaryData>();
                    List<SupplementaryData> supplementaryData = new ArrayList<SupplementaryData>();
                    
                    // Retrieving existing supplementary data
                    if (!AbstractSubscriber.DEFAULT_ID.equals(this.getId()))
                    {
                        supplementaryData = new ArrayList<SupplementaryData>(getSupplementaryData(ctx));
                    }

                    // Populating map with existing supplementary data
                    for (SupplementaryData data : supplementaryData)
                    {
                        supplementaryDataMap.put(data.getKey(), data);   
                    }
                    
                    // Adding required supplementary data to result
                    for (SupplementaryDataField field : (List<SupplementaryDataField>) fields.getFields())
                    {
                        SupplementaryData data = new SupplementaryData();
                        data.setEntity(SupplementaryDataEntityEnum.SUBSCRIPTION_INDEX);
                        data.setIdentifier(this.getId());
                        data.setKey(field.getName());
                        
                        // Populating supplementary data value if it exists
                        if (supplementaryDataMap.get(field.getName())!=null)
                        {
                            SupplementaryData found = supplementaryDataMap.get(field.getName());
                            data.setValue(found.getValue());
                            supplementaryDataMap.remove(field.getName());
                        }
                        
                        result.add(data);
                    }

                    // Adding extra supplementary data to result
                    for (SupplementaryData data : (Collection<SupplementaryData>) supplementaryDataMap.values())
                    {
                        result.add(data);
                    }
                }
                supplementaryDataList_ = result;
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to retrieve supplementary data", e);
            }
        }
        
        return supplementaryDataList_;
    }
    
    
    public boolean isClctChange()
    {
        return clctChange_;
    }


    public long getSubNewBalance()
    {
        return subNewBalance_;
    }


    public long getSubOldBalance()
    {
        return subOldBalance_;
    }
    
    public void setClctChange(boolean clctChange)
    {
        clctChange_ = clctChange;
    }


    public void setSubNewBalance(long newBalance)
    {
        subNewBalance_ = newBalance;
    }


    public void setSubOldBalance(long oldBalance)
    {
        subOldBalance_ = oldBalance;
    }
    

    /**
     * Return all the supplementary data for this subscription
     * @param context
     * @return
     * @throws HomeException
     */
    public Collection<SupplementaryData> getSupplementaryData(Context context) throws HomeException
    {
        return SupplementaryDataSupportHelper.get(context).getSupplementaryData(context,
                SupplementaryDataEntityEnum.SUBSCRIPTION, this.getId());
    }

    /**
     * Return a given supplementary data for this subscription
     * @param context
     * @param key
     * @return
     * @throws HomeException
     */
    public SupplementaryData getSupplementaryData(Context context, String key) throws HomeException
    {
        return SupplementaryDataSupportHelper.get(context).getSupplementaryData(context,
                SupplementaryDataEntityEnum.SUBSCRIPTION, this.getId(), key);
    }
    
    /**
     * Remove all the supplementary data for this bean
     * @param context
     * @throws HomeException
     */
    public void removeAllSupplementaryData(Context context) throws HomeException
    {
        SupplementaryDataSupportHelper.get(context).removeAllSupplementaryData(context,
                SupplementaryDataEntityEnum.SUBSCRIPTION, this.getId());
    }
    
    /**
	 * Add the remove id for service that are getting removed by old pp
	 * @param key : Service id which is getting replace 
	 * @param value : Properties of the service which is a removed
	 */
	public void addToOldPPServiceRemovePropOnPPChange(Long key, Properties value) {
		if(null == this.removedServiceListOnPPChange){
			this.removedServiceListOnPPChange = new HashMap<Long, Properties>();
		}
		this.removedServiceListOnPPChange.put(key, value);
	}
	
    
    public Properties getOldPPServiceRemovePropOnPPChange(Long key) {
		if(null != removedServiceListOnPPChange){
			return removedServiceListOnPPChange.get(key);
		}
		return null;
	}
    
    private boolean clctChange_ = false;
    
    private long subNewBalance_ = SubscriberSupport.INVALID_VALUE;
    
    private long subOldBalance_ = SubscriberSupport.INVALID_VALUE;
    
    private Map bundleOverUsage;

    /**
     * The ID of the Pool for the Pooled Group account.
     */
    //protected String poolID_ = null;

    /**
     * The MSISDN of the Pool for the Pooled Group account.
     */
    protected String poolMSISDN_ = null;

    protected long monthlySpendAmount_ = Subscriber.DEFAULT_MONTHLYSPENDLIMIT;

    protected long urcsOverdraftBalanceLimit_ = SubscriberSupport.INVALID_VALUE;

    /**
     * The subscriber language; lazy loaded.
     */
    private volatile String languageAsLastRetrieved_ = null;

    /**
     * Indicates if subscriber's language was set.
     */
    private volatile boolean isLanguageSetByAccessor_ = false;

    protected Principal principal_ = null;

    /**
     * This value will be set when existing subscriber loaded/saved.
     */
    protected long pricePlanBackup_ = -1;

    private Collection cltcServices_; 
    private Collection auxServiceToBeReprovisioned;

    /**
     * This is needed because of different time zones result in different numeric representation of the epoch
     * calendar date.
     */
    public static final Date NEVER_EXPIRE_CUTOFF_DATE;

    static
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 2);
        NEVER_EXPIRE_CUTOFF_DATE = calendar.getTime();
    }
}
