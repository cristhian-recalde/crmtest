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
package com.trilogy.app.crm.bulkloader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.app.crm.bean.GroupScreeningTemplateXInfo;
import com.trilogy.app.crm.bean.MarketingCampaignBeanXInfo;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bundle.DurationTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtension;
import com.trilogy.app.crm.extension.subscriber.FixedStopPricePlanSubExtensionXInfo;
import com.trilogy.app.crm.home.BypassCreatePipelineHome;
import com.trilogy.app.crm.home.BypassValidationHome;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

public class SubscriberCreateVisitor extends HomeVisitor
{
    public static final String PM_MODULE = AcctSubBulkLoadRequestServicer.class.getName();

    private int numberOfProcessedSubscribers_ = 0;

    private int numberOfSuccessfullyProcessedSubscribers_ = 0;

    private int numberOfPartialSuccessfullyProcessedSubscribers_ = 0;

    private Subscriber subscriberTemplate_;

    private PrintWriter subscriberWriter_;

    private PrintWriter subscriberErrWriter_;

    private final Date now_;
    private final Date futureEndDate_;
    private final ThreadLocalSimpleDateFormat dateFormatter_;

    public SubscriberCreateVisitor(final Context ctx,
                                   final Home home,
                                   final Subscriber template,
                                   final PrintWriter writer,
                                   final PrintWriter errWriter)
    {
        super(home);

        setSubscriberTemplate(template);
        setSubscriberWriter(writer);
        setSubscriberErrWriter(errWriter);

        now_ = new Date();
        futureEndDate_ = SubscriberSupport.getFutureEndDate(now_);
        dateFormatter_ = BulkLoadSubscriber.getDateFormat(ctx);
    }
    
    public synchronized void incrementNumberOfProcessedSubscribers_()
    {
    	numberOfProcessedSubscribers_ ++;
    }
    
    public synchronized void incrementNumberOfSuccessfullyProcessedSubscribers_() 
    {
    	numberOfSuccessfullyProcessedSubscribers_ ++;
    }
    
    public synchronized void incrementNumberOfPartialSuccessfullyProcessedSubscribers_()
    {
    	numberOfPartialSuccessfullyProcessedSubscribers_ ++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(final Context ctx, final Object obj)
        throws AgentException, AbortVisitException
    {
        // Next Subscriber
        final BulkLoadSubscriber bs = (BulkLoadSubscriber) obj;

        final PMLogMsg createSubscriberPM = new PMLogMsg(PM_MODULE, "Create Subscriber", bs.getBAN());
        final PMLogMsg createSubscriberPMSuccess = new PMLogMsg(PM_MODULE, "Create Subscriber (SUCCESS)", bs.getBAN());
        final PMLogMsg createSubscriberPMError = new PMLogMsg(PM_MODULE, "Create Subscriber (ERROR)", bs.getBAN());

        try
        {
            incrementNumberOfProcessedSubscribers_();

            final Subscriber s = getSubscriber(ctx, bs);

            processSubscriber(ctx, s, bs);

            if (s.getLastExp()==null)
            {
            	incrementNumberOfSuccessfullyProcessedSubscribers_();
            }
            else
            {
            	incrementNumberOfPartialSuccessfullyProcessedSubscribers_();

            }
            createSubscriberPMSuccess.log(ctx);
        }
        catch (Throwable t)
        {
            String msg;
            if ("".equals(bs.getSubscriptionID()))
            {
                msg = bs.getBAN() + "-1";
            }
            else
            {
                msg = bs.getSubscriptionID();
            }

            // An error occured trying to add this subscriber
            try
            {
                msg = msg + "-Failure Adding Subscriber-" + t.getMessage() + "\n";
                // Write to log and error files: the error, the
                // BulkLoadSubscriber that erred
                logErrorToFiles(ctx, msg, bs);
                t.printStackTrace(getSubscriberWriter());
            }
            catch (Throwable tt)
            {
                // Can't write to log file
                new InfoLogMsg(this, "Unable to write to Subscriber log file: " + msg, tt).log(ctx);
            }
            createSubscriberPMError.log(ctx);
        }
        finally
        {
            createSubscriberPM.log(ctx);
        }
    }


    /**
     *
     */
    private void processSubscriber(final Context ctx,
            Subscriber subscriber,
            final BulkLoadSubscriber bs)
        throws HomeException, AgentException
    {
        final SubscriberStateEnum currState = subscriber.getState();

        
        if ( !(Boolean) ctx.get("CONSIDER_SUSPENDED_AND_PENDING_FOR_SUBSCRIBER_BULKLOAD", false) && (currState == SubscriberStateEnum.SUSPENDED || currState == SubscriberStateEnum.PENDING))
        {
            final String msg = "Didn't load subscriber " + subscriber.getMSISDN()
                    + " because it is SUSPENDED or PENDING";
            new MinorLogMsg(this, msg, null).log(ctx);

            // Write to log and error files: the error, the BulkLoadSubscriber
            // that erred
            logErrorToFiles(ctx, msg, bs);
        }
        else
        {
            // Modify to pending state for create
            subscriber.setState(SubscriberStateEnum.PENDING);

            subscriber = (Subscriber) getHome().create(ctx, subscriber);

            
            final String id = subscriber.getId();

            // for some reason there are a lot of problems if you reuse the same
            // subscriber
            // so we have to find it again
            
            
            if(!ctx.getBoolean(BypassCreatePipelineHome.BYPASS_SUBSCRIBER_PIPELINE, false))
            {
	            subscriber = (Subscriber) getHome().find(ctx, id);
	
	            if (subscriber == null)
	            {
	                final String msg = "Cannot find the subscriber we just added: " + id;
	
	                // Write to log and error files: the error, the
	                // BulkLoadSubscriber that erred
	                logErrorToFiles(ctx, msg, bs);
	
	                throw new AgentException(msg);
	            }
	
	            if (subscriber.getState() != currState)
	            {
	                subscriber.setState(currState);
	
	                // this will make the bean think it is another provisioning
	                // operation
	                subscriber.resetTransientProvisionedServices();
	
	                getHome().store(ctx, subscriber);
	            }
            }            
        }
    }

    /**
     * Creates a subscriber out of the bulk load subscriber and the subscriber
     * template.
     *
     * @param ctx registry
     * @param bs bulk load subscriber
     * @return new subscriber
     */
    protected Subscriber getSubscriber(final Context parentCtx, final BulkLoadSubscriber bs)
        throws AgentException
    {
        Subscriber subscriber = null;
        Context ctx = parentCtx.createSubContext();
        
        //TT#13041719005 Fixed. Setting the correct SPID in context when subscriber validations are taking place.
        if(bs!= null)
        {
        	MSP.setBeanSpid(ctx , Integer.parseInt(bs.getSpid()));
        }

        if (getSubscriberTemplate() != null)
        {
        	PMLogMsg cloneMsg = new PMLogMsg(PM_MODULE, "Subscriber Clone: ", "deepClone");
            try
            {
                subscriber = (Subscriber) getSubscriberTemplate().deepClone();
                subscriber.setDateCreated(now_);
            }
            catch (final CloneNotSupportedException e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Cannot clone subscriber template.", e).log(ctx);
                }
            }
            finally
            {
            	cloneMsg.log(parentCtx);
            }
        }

        if (subscriber == null)
        {
            try
            {
                subscriber = (Subscriber) XBeans.instantiate(Subscriber.class, ctx);
            }
            catch (final Exception e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Cannot instantiate subscriber class.", e).log(ctx);
                }
                subscriber = new Subscriber();
            }
        }

        subscriber.setContext(ctx);

        validateMandatoryFields(ctx, bs);

        copyBulkLoadValue(ctx, subscriber, bs);

        validateLicenses(ctx, subscriber);

        return subscriber;
    }

    /**
     * Applies given SCT to a Subscription.
     * Should be called only for Prepaind Subscriptions, no validation done.
     */
    private ServiceActivationTemplate applySCTtoSubscriber(final Context context, final Subscriber subscriber,
            final long satId) throws AgentException
    {
        try
        {
            return SubscriberSupport.applySubServiceActivationTemplate(context, subscriber, satId);
        }
        catch (HomeException he)
        {
            final String msg = "Error applying SCT: " + satId + " to Subscriber.";
            getSubscriberErrWriter().println(msg);
            throw new AgentException(msg, he);
        }
    }

    private void applySubscriptionClassToSubscriber(final Context ctx, final Subscriber subscriber)
        throws AgentException
    {
        final SubscriptionClass subClass = subscriber.getSubscriptionClass(ctx);
        if (subClass == null)
        {
            throw new AgentException("Subscription Class field cannot be parsed.");
        }
        subscriber.setSubscriptionType(subClass.getSubscriptionType());
        subscriber.setTechnology(TechnologyEnum.get((short)(subClass.getTechnologyType())));
    }

    /**
     * Copies the values from the bulk load subscriber to the subscriber
     *
     * @param ctx the operating context
     * @param s subscriber to configure
     * @param bs bulk load record
     * @throws com.redknee.framework.xhome.context.AgentException thrown due to validation errors
     */
    protected void copyBulkLoadValue(final Context ctx, final Subscriber s, final BulkLoadSubscriber bs)
        throws AgentException
    {
        s.setBAN(bs.getBAN());
        s.setSpid(SubscriberCreateVisitor.getMandatoryInt(ctx, bs.getSpid(), SubscriberXInfo.SPID));
        if (bs.getSubscriptionID() != null && !bs.getSubscriptionID().equals(""))
        {
            s.setId(bs.getSubscriptionID());
        }
        boolean satSet = false;
        if (bs.getBillingType() == SubscriberTypeEnum.PREPAID)
        {
            s.setSatId(SubscriberCreateVisitor.getLong(bs.getSatId(), s.getSatId()));
            satSet = s.getSatId() != AbstractSubscriber.DEFAULT_SATID;
        }

        if (satSet)
        {
            final ServiceActivationTemplate sat = applySCTtoSubscriber(ctx, s, s.getSatId());
            s.setSubscriptionClass(sat.getSubscriptionClass());
            applySubscriptionClassToSubscriber(ctx, s);
            s.setTechnology(sat.getTechnology());
        }
        else
        {
            s.setSubscriptionClass(SubscriberCreateVisitor.getMandatoryLong(ctx, bs.getSubscriptionClass(),
                    SubscriberXInfo.SUBSCRIPTION_CLASS));
            applySubscriptionClassToSubscriber(ctx, s);

            if (s.getTechnology() == TechnologyEnum.ANY)
            {
                if (bs.getTechnology() != TechnologyEnum.ANY)
                {
                    s.setTechnology(bs.getTechnology());
                }
                else
                {
                    throw new AgentException(SubscriberXInfo.TECHNOLOGY.getLabel(ctx)
                            + " is Mandatory for this SubscriptionClass.");
                }
            }
        }

        if (bs.getBillingType() != SubscriberTypeEnum.HYBRID)
        {
            s.setSubscriberType(bs.getBillingType());
        }
        else
        {
            throw new AgentException("Mandatory field " + SubscriberXInfo.SUBSCRIBER_TYPE.getLabel(ctx) + " not set.");
        }

        s.setMSISDN(bs.getMSISDN());
        s.setFaxMSISDN(bs.getFaxMSISDN());
        s.setDataMSISDN(bs.getDataMSISDN());
        // state set moved to the if block bellow because of the type dependent default
        
        Date defaultActivationDate = now_;
        if (s.isPrepaid())
        {
            SubscriberStateEnum prepaidState = SubscriberCreateVisitor.getStateEnum(bs.getState(), SubscriberStateEnum.AVAILABLE);
            if (prepaidState.equals(SubscriberStateEnum.AVAILABLE) || prepaidState.equals(SubscriberStateEnum.PENDING))
            {
                defaultActivationDate = null;
            }
        }
        
        s.setStartDate(getDate(ctx, bs.getStartDate(), defaultActivationDate, SubscriberXInfo.START_DATE));
        s.setEndDate(getDate(ctx, bs.getEndDate(), null, SubscriberXInfo.END_DATE));

        final long ppID;
        if (satSet)
        {
            ppID = SubscriberCreateVisitor.getLong(bs.getPricePlan(), s.getPricePlan());
        }
        else
        {
            ppID = SubscriberCreateVisitor.getMandatoryLong(ctx, bs.getPricePlan(), SubscriberXInfo.PRICE_PLAN);
        }
        validatePricePlan(ctx, ppID, SubscriberXInfo.PRICE_PLAN);

        // price plan should be set before deposit field to avoid overriding values
        selectPricePlanServicesAndBundles(ctx, s, ppID, bs.getServices(), bs.getBundles());

        if (s.isPostpaid())
        {
            if (s.getStartDate().after(now_))
            {
                s.setState(SubscriberCreateVisitor.getStateEnum(bs.getState(), SubscriberStateEnum.PENDING));
            }
            else
            {
                s.setState(SubscriberCreateVisitor.getStateEnum(bs.getState(), SubscriberStateEnum.ACTIVE));
            }
            
            s.setDeposit(SubscriberCreateVisitor.getMandatoryLong(ctx, bs.getDeposit(), SubscriberXInfo.DEPOSIT));
            s.setDepositDate(getMandatoryDate(ctx, bs.getLastDepositDate(), SubscriberXInfo.DEPOSIT_DATE));
            s.setCreditLimit(SubscriberCreateVisitor.getMandatoryLong(ctx, bs.getCreditLimit(),
                    SubscriberXInfo.CREDIT_LIMIT));
            s.setMaxRecharge(AbstractSubscriber.DEFAULT_MAXRECHARGE);
            s.setReactivationFee(AbstractSubscriber.DEFAULT_REACTIVATIONFEE);
            s.setExpiryDate((Date) SubscriberXInfo.EXPIRY_DATE.getDefault());

            s.setDiscountClass(SubscriberCreateVisitor.getMandatoryInt(ctx, bs.getDiscountClass(),
                    SubscriberXInfo.DISCOUNT_CLASS));
            s.setInitialBalance(AbstractSubscriber.DEFAULT_INITIALBALANCE);

            if (bs.getBillingOption() != null)
            {
                s.setBillingOption(bs.getBillingOption());
            }
        }
        else
        {
            s.setState(SubscriberCreateVisitor.getStateEnum(bs.getState(), SubscriberStateEnum.AVAILABLE));
            s.setDeposit(0);
            if (SubscriberXInfo.DEPOSIT_DATE.getDefault()!=null)
            {
                s.setDepositDate((Date) SubscriberXInfo.DEPOSIT_DATE.getDefault());
            }
            else
            {
                s.setDepositDate(new Date());
            }
            s.setCreditLimit(0);
            s.setMaxRecharge(SubscriberCreateVisitor.getLong(bs.getMaxRecharge(), s.getMaxRecharge()));
            s.setReactivationFee(SubscriberCreateVisitor.getLong(bs.getReactivationFee(), s.getReactivationFee()));
            //s.setSupportMSISDN(bs.getSupportMSISDN());
            //s.setChargePpsm(bs.getChargePpsm());
            s.setExpiryDate(getDate(ctx, bs.getExpiryDate(), s.getExpiryDate(), SubscriberXInfo.EXPIRY_DATE));

            s.setDiscountClass(AbstractSubscriber.DEFAULT_DISCOUNTCLASS);
            s.setInitialBalance(SubscriberCreateVisitor.getLong(bs.getInitialBalance(), s.getInitialBalance()));            
        }

        validateState(ctx, s, SubscriberXInfo.STATE);


        SubscriptionType subscriptionType = s.getSubscriptionType(ctx);
        if (s.getTechnology().isPackageAware())
        {
            if (bs.getPackageId() == null || bs.getPackageId().trim().length() == 0)
            {
                throw new AgentException("Mandatory field PackageID not set.");
            }
            s.setPackageId(bs.getPackageId());
        }
        else if (bs.getPackageId() != null && bs.getPackageId().trim().length() > 0)
        {
            throw new AgentException("PackageID can be set only for package aware subscription types.");
        }

        s.setDealerCode(bs.getDealerCode());
        s.setReasonCode(bs.getReasonCode());
        s.setQuotaType(bs.getQuoteType());
        s.setQuotaLimit(bs.getQuotaLimit());

        long secPPid = SubscriberCreateVisitor.getLong(bs.getSecondaryPricePlan(), s.getSecondaryPricePlan());
        if (secPPid != Subscriber.DEFAULT_SECONDARYPRICEPLAN)
        {
            validatePricePlan(ctx, secPPid, SubscriberXInfo.SECONDARY_PRICE_PLAN);
            s.setSecondaryPricePlan(secPPid);
        }

        s.setSecondaryPricePlanStartDate(getDate(ctx, bs.getSecondaryPricePlanStartDate(), null,
                SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE));
        s.setSecondaryPricePlanEndDate(getDate(ctx, bs.getSecondaryPricePlanEndDate(), null,
                SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE));

        if (bs.getAuxiliaryServices().size() > 0 && !subscriptionType.isService())
        {
            throw new AgentException("Auxiliary Services can be added only for service-based subscriptions.");
        }
        selectAuxiliaryServices(ctx, s, bs.getAuxiliaryServices());

        s.setSubscriberCategory(bs.getSubscriptionCategory());
        s.getMarketingCampaignBean().setMarketingId(bs.getMarketingCampaignId());
        s.getMarketingCampaignBean().setStartDate(getDate(ctx, bs.getMarketingCampaignStartDate(), now_,
                MarketingCampaignBeanXInfo.START_DATE));
        s.getMarketingCampaignBean().setEndDate(getDate(ctx, bs.getMarketingCampaignEndDate(), now_,
                MarketingCampaignBeanXInfo.END_DATE));
        s.setBillingLanguage(bs.getLanguage());

		s.setSubscriptionContract(getLong(bs.getContractId(),
		    Subscriber.DEFAULT_SUBSCRIPTIONCONTRACT));
		s.setStartDate(getDate(ctx, bs.getStartDate(), null,
		    SubscriberXInfo.SUBSCRIPTION_CONTRACT_START_DATE));
		
		s.setDeviceTypeId(bs.getDeviceTypeId());
		
		s.setDeviceName(bs.getDeviceName());
		// TODO additional DCRM parameters
		
		if (bs.getGroupScreeningTemplateId() != -1)
		{
			long count = validateGroupScreeningTemplate(ctx, bs);
		    if (count > 0)
		    {
		    	s.setGroupScreeningTemplateId(bs.getGroupScreeningTemplateId());
		    }
		    else
		    {
		    	throw new AgentException("Group screening template does not exist with id : " + bs.getGroupScreeningTemplateId());
		    }
		}
		else
		{
			s.setGroupScreeningTemplateId(bs.getGroupScreeningTemplateId());
		}
		
		if (bs.getOverdraftBalanceLimit()>=0)
		{
            s.setOverdraftBalanceLimit(ctx, bs.getOverdraftBalanceLimit());
		}
		
		if (!bs.getPrimaryPricePlanEndDate().isEmpty())
		{
		    s.setFixedStopPricePlanDate(ctx, getDate(ctx, bs.getPrimaryPricePlanEndDate(), null,
                FixedStopPricePlanSubExtensionXInfo.END_DATE));
		}
		
    }

    private void selectPricePlanServicesAndBundles(final Context ctx, final Subscriber s, final long ppID,
            final Set<ServiceFee2ID> servicesAdded, final Set bundlesAdded) throws AgentException
    {
        s.switchPricePlan(ctx, ppID);

        // handle services
        final PricePlanVersion ppv;
        try
        {
            ppv = s.getRawPricePlanVersion(ctx);
        }
        catch (HomeException e)
        {
            throw new AgentException("Unable to retreive Price Plan information", e);
        }
        final Map serviceFees = ppv.getServicePackageVersion().getServiceFees();
        final Set subscribedSrvs = s.getAllNonUnprovisionedStateServices();

        for (final Iterator<ServiceFee2ID> srvIt = servicesAdded.iterator(); srvIt.hasNext();)
        {
            final ServiceFee2ID srvKey = (ServiceFee2ID) srvIt.next();
            final long srvID = srvKey.getServiceId();
            final String path = srvKey.getPath();
            final ServiceFee2 serviceFee = (ServiceFee2) serviceFees.get(srvKey);
            if (serviceFee == null)
            {
                throw new AgentException("Service [" + srvID + "] is not part of the Price Plan [" + ppID +"].");
            }

            SubscriberServices bean = null;
			for (final Iterator iterator = subscribedSrvs.iterator(); iterator.hasNext();) {
				final SubscriberServices subService = (SubscriberServices) iterator.next();
				if (subService.getServiceId() == srvID && subService.getPath().equals(path)) {
					bean = subService;
					break;
				}
			}

			if (bean == null) {
				bean = new SubscriberServices();
				bean.setSubscriberId(s.getId());
				bean.setServiceId(srvID);
				bean.setPath(path);
				bean.setMandatory(false);
				bean.setServicePeriod(serviceFee.getServicePeriod());
				// bean.setState(ServiceStateEnum.ACTIVE);
				bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
				subscribedSrvs.add(bean);
			}

			s.getServices().add(new ServiceFee2ID(srvID, path));
        }

        // handle bundles
        Map bundles = s.getBundles();
        final Map ppBundles = SubscriberBundleSupport.getPricePlanBundles(ctx, s);

        for (final Iterator bndIt = bundlesAdded.iterator(); bndIt.hasNext();)
        {            final Long key = (Long) bndIt.next();
            final BundleFee bundle = (BundleFee) ppBundles.get(key);
            if (bundle != null)
            {
                bundles.put(key, bundle);
                bndIt.remove();
            }
            else
            {
                try
                {
                    BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, key);
    
                    if (bundleProfile != null && bundleProfile.isAuxiliary()
                            && (!bundleProfile.getRecurrenceScheme().isOneTime()
                                    || bundleProfile.getEndDate() == null
                            || !bundleProfile.getEndDate().before(new Date())
                            || bundleProfile.getEndDate().equals(new Date(0))))
                    {
                        final BundleFee fee = new BundleFee();
                        fee.setId(key.longValue());
                        fee.setSource(BundleFee.AUXILIARY);
                        fee.setFee(bundleProfile.getAuxiliaryServiceCharge());
                        fee.setServicePreference(ServicePreferenceEnum.OPTIONAL);
                        final Date now = new Date();
                        Date endDate = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, now);
                        if (bundleProfile.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL))
                        {
                            if (bundleProfile.getInterval() == DurationTypeEnum.MONTH_INDEX)
                            {
                                endDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(bundleProfile.getValidity(), now);
                            }
                            else
                            {
                                endDate = CalendarSupportHelper.get(ctx).findDateDaysAfter(bundleProfile.getValidity(), now);
                            }
                        }
                        fee.setStartDate(now);
                        fee.setEndDate(endDate);

                        bundles.put(key, fee);
                    }
                }
                catch (Exception e)
                {
                    throw new AgentException("Unable to bulkload subscription with MSISDN='" + s.getMsisdn()
                            + "': Bundle Profile " + key + " does not exist.");
                }
            }
        }

        // this removes any provisioned services that might have been set using the template
        s.resetTransientProvisionedServices();
    }

    private void selectAuxiliaryServices(final Context ctx, final Subscriber s, final Set auxServicesAdded)
    {
        List subAuxSrvs = s.getAuxiliaryServices(ctx);
        for (final Iterator it = auxServicesAdded.iterator(); it.hasNext();)
        {
            final Long auxSrvID = (Long) it.next();
            final SubscriberAuxiliaryService bean = new SubscriberAuxiliaryService();
            bean.setContext(ctx);
            bean.setAuxiliaryServiceIdentifier(auxSrvID.longValue());

            subAuxSrvs.add(bean);
        }
    }
    
    protected void validateState(final Context ctx, final Subscriber s, final PropertyInfo info) throws AgentException
    {
    	if(ctx.getBoolean(BypassValidationHome.FLAG, false))
    	{
    		return;
    	}
    	
        if (s.getStartDate() == null || s.getStartDate().after(now_))
        {
            if (s.isPostpaid() && !SubscriberStateEnum.PENDING.equals(s.getState()))
            {
                throw new AgentException("Invalid " + info.getLabel(ctx) + " [" + s.getState()
                        + "] for postpaid subscription with future activation.");
            }
            else if (s.isPrepaid() && !SubscriberStateEnum.AVAILABLE.equals(s.getState()))
            {
                throw new AgentException("Invalid " + info.getLabel(ctx) + " [" + s.getState()
                        + "] for prepaid subscription with future activation.");
            }
        }
    }
    
    protected long validateGroupScreeningTemplate(final Context ctx, final BulkLoadSubscriber bs) throws AgentException
    {
    	long count =0;
    	
    	And and = new And();
    	and.add(new EQ(GroupScreeningTemplateXInfo.IDENTIFIER, bs.getGroupScreeningTemplateId()));
    	and.add(new EQ(GroupScreeningTemplateXInfo.ACTIVE, true));
    	try {
			count = HomeSupportHelper.get(ctx).getBeanCount(ctx, 
					GroupScreeningTemplate.class, and);
		} catch (HomeException e) {
			throw new AgentException("Unable to find Group Screening Template Id with id : " + bs.getGroupScreeningTemplateId());
		}
    	return count;
    }

    protected void validatePricePlan(final Context ctx, final long pricePlan, final PropertyInfo info)
        throws AgentException
    {
    	if(ctx.getBoolean(BypassValidationHome.FLAG, false))
    	{
    		return;
    	}
    	
        if (pricePlan <= 0L)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Invalid " + label + " [" + pricePlan + "]");
        }

        try
        {
            final Home h = (Home) ctx.get(PricePlanHome.class);

            final PricePlan pp = (PricePlan) h.find(ctx, Long.valueOf(pricePlan));

            if (pp != null)
            {
                return;
            }
            else
            {
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "PP found null : PricePlan ID : " + pricePlan, null).log(ctx);
                }
            }
        }
        catch (HomeException hex)
        {
            new MinorLogMsg(this, "HomeException Occured while trying to fetch Price Plan ID : " + pricePlan, hex).log(ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "HomeException Occured while trying to fetch Price Plan ID : " + pricePlan, e).log(ctx);
        }


        final String label = info.getLabel(ctx);
        throw new AgentException("Invalid " + label + " [" + pricePlan + "]");
    }

    /**
     * Calculates the number of days that the expiryDate is ahead of the date
     *
     * @param expiryDate
     * @param date
     * @return
     */
    private int getNumberOfDays(final Date expiryDate, final Date date)
    {
        final long tExp = expiryDate.getTime();
        final long tDate = date.getTime();

        if (tExp < tDate)
        {
            return 0;
        }

        return (int) ((tExp - tDate) / (24 * 60 * 60 * 1000) + 1);
    }

    public static SubscriberStateEnum getStateEnum(final String str, final SubscriberStateEnum default_)
        throws AgentException
    {
        if (str == null || str.length() == 0)
        {
            return default_;
        }

        final short stateId = (short) SubscriberCreateVisitor.getInt(str, default_.getIndex());
        SubscriberStateEnum state = null;

        for (short i = 0; i < SubscriberStateEnum.COLLECTION.getSize(); i++)
        {
            SubscriberStateEnum aState = (SubscriberStateEnum) SubscriberStateEnum.COLLECTION.get(i);
            if (aState.getIndex() == stateId)
            {
                state = aState;
                break;
            }
        }

        if (state == null)
        {
            throw new AgentException("Invalid State ID.");
        }

        return state;
    }

    /**
     * Converts a string to an integer. Not using the StringUtil.getInt() because it relies on thrown exceptions for
     * parsing an empty string and creates an extra Integer object.
     * @param str string containing an integer
     * @param default_ default value, if error
     * @return integer or default_ value on error
     */
    public static int getInt(final String str, final int default_)
    {
        if (str == null || str.length() == 0)
        {
            return default_;
        }
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException f)
        {
            return default_;
        }
    }

    /**
     * Converts a string to an integer. Throws AgentException if value cannot be converted or missing.
     *
     * @param ctx the operating context
     * @param str string containing an integer
     * @param info field PropertyInfo for error reporting purposes
     * @return integer
     * @throws com.redknee.framework.xhome.context.AgentException thrown if string parsing fails
     */
    public static int getMandatoryInt(final Context ctx, final String str, final PropertyInfo info)
        throws AgentException
    {
        if (str == null || str.length() == 0)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Mandatory field " + label + " not set.");
        }
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException e)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Mandatory field " + label + " with value [" + str + "] cannot be parsed.", e);
        }
    }

    /**
     * Converts a string to a long. Not using the StringUtil.getLong() because it relies on thrown exceptions for
     * parsing an empty string and creates an extra Long object.
     * @param str string containing a long
     * @param default_ default value, if error
     * @return long or default_ value on error
     */
    public static long getLong(final String str, final long default_)
    {
        if (str == null || str.length() == 0)
        {
            return default_;
        }
        try
        {
            return Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            return default_;
        }
    }

    /**
     * Converts a string to a long. Throws AgentException if value cannot be converted or missing.
     *
     * @param ctx the operating context
     * @param str string containing a long
     * @param info filed PropertyInfo for error reporting purposes
     * @return long
     * @throws com.redknee.framework.xhome.context.AgentException thrown if string parsing fails
     */
    public static long getMandatoryLong(final Context ctx, final String str, final PropertyInfo info)
        throws AgentException
    {
        if (str == null || str.length() == 0)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Mandatory field " + label + " not set.");
        }
        try
        {
            return Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            final String label = info.getLabel(ctx);
            throw new AgentException("Mandatory field " + label + " with value [" + str + "] cannot be parsed.", e);
        }
    }

    /**
     * Converts a string to a Date. returns the default_ value if parsing fails.
     *
     * @param ctx the operating context
     * @param str string containing an integer
     * @param default_ default value, if error
     * @param info property info for the field that is being set
     * @return integer or default_ value on error
     * @throws com.redknee.framework.xhome.context.AgentException if date cannot be parsed
     */
    public Date getDate(final Context ctx, final String str, final Date default_, final PropertyInfo info)
        throws AgentException
    {
        return BulkLoadSupport.getDate(ctx, str, default_, dateFormatter_, info);
    }

    /**
     * Converts a string to a Date. Throws exception if string is empty.
     *
     * @param ctx the operating context
     * @param str string containing an integer
     * @param info property info for the field that is being set
     * @return integer or default_ value on error
     * @throws com.redknee.framework.xhome.context.AgentException if date cannot be parsed or string is empty
     */
    public Date getMandatoryDate(final Context ctx, final String str, final PropertyInfo info)
        throws AgentException
    {
        return BulkLoadSupport.getMandatoryDate(ctx, str, dateFormatter_, info);
    }

    /**
     * Checks if bulk load Subscriber record has all mandatory fields set.
     *
     * @param ctx the operting context
     * @param bs bean that containg the record values
     * @throws com.redknee.framework.xhome.context.AgentException if mandatory fields are empty 
     */
    private void validateMandatoryFields(final Context ctx, final BulkLoadSubscriber bs) throws AgentException
    {
    	if(ctx.getBoolean(BypassValidationHome.FLAG, false))
    	{
    		return;
    	}
        if (bs.getBAN() == null || bs.getBAN().trim().length() == 0)
        {
            throw new AgentException("Mandatory field BAN not set.");
        }
        else if (bs.getMSISDN() == null || bs.getMSISDN().trim().length() == 0)
        {
            throw new AgentException("Mandatory field MISIDN not set.");
        }
    }

    /**
     * Checks Subscriber Technology License
     * @param ctx the operating context
     * @param subscriber License check will be done on the technology used by this subscriber
     * @throws com.redknee.framework.xhome.context.AgentException thrown if used technology is not licensed
     */
    private void validateLicenses(final Context ctx, final Subscriber subscriber) throws AgentException
    {
    	if(ctx.getBoolean(BypassValidationHome.FLAG, false))
    	{
    		return;
    	}
    	
        final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);

        final TechnologyEnum techEnum = subscriber.getTechnology();

        if (techEnum == null || techEnum == TechnologyEnum.ANY)
        {
            throw new AgentException("Invalid Subscriber Technology Type");
        }
        else if (techEnum == TechnologyEnum.GSM)
        {
            if (lMgr != null && !lMgr.isLicensed(ctx, LicenseConstants.GSM_LICENSE_KEY))
            {
                throw new AgentException("Invalid Subscriber Technology Type [GSM Licesnse valdiation failed]");
            }
        }
        else if (techEnum == TechnologyEnum.CDMA || techEnum == TechnologyEnum.TDMA)
        {
            if (lMgr != null && !lMgr.isLicensed(ctx, LicenseConstants.TDMA_CDMA_LICENSE_KEY))
            {
                throw new AgentException("Invalid Subscriber Technology Type. [CDMA-TDMA Licesnse valdiation failed]");
            }
        }
        else if (techEnum == TechnologyEnum.VSAT_PSTN)
        {
            if (lMgr != null && !lMgr.isLicensed(ctx, LicenseConstants.VSAT_PSTN_LICENSE_KEY))
            {
                throw new AgentException("Invalid Subscriber Technology Type. [VSAT-PSTN Licesnse valdiation failed]");
            }
        }
        else if (techEnum != TechnologyEnum.NO_TECH)
        {
            throw new AgentException("Invalid Subscriber Technology Type");
        }
    }

    /**
     * @return Returns the numberOfProcessedSubscribers.
     */
    public int getNumberOfProcessedSubscribers()
    {
        return numberOfProcessedSubscribers_;
    }

    /**
     * @param numberOfProcessedSubscribers The numberOfProcessedSubscribers to set.
     */
    public void setNumberOfProcessedSubscribers(final int numberOfProcessedSubscribers)
    {
        this.numberOfProcessedSubscribers_ = numberOfProcessedSubscribers;
    }

    /**
     * @return Returns the numberOfSuccessfullyProcessedSubscribers.
     */
    public int getNumberOfSuccessfullyProcessedSubscribers()
    {
        return numberOfSuccessfullyProcessedSubscribers_;
    }

    /**
     * @return Returns the numberOfSuccessfullyProcessedSubscribers.
     */
    public int getNumberOfPartialSuccessfullyProcessedSubscribers()
    {
        return numberOfPartialSuccessfullyProcessedSubscribers_;
    }

    /**
     * @param numberOfSuccessfullyProcessedSubscribers The numberOfSuccessfullyProcessedSubscribers to set.
     */
    public void setNumberOfSuccessfullyProcessedSubscribers(final int numberOfSuccessfullyProcessedSubscribers)
    {
        this.numberOfSuccessfullyProcessedSubscribers_ = numberOfSuccessfullyProcessedSubscribers;
    }

    /**
     * @return Returns the subscriberTemplate.
     */
    public Subscriber getSubscriberTemplate()
    {
        return subscriberTemplate_;
    }

    /**
     * @param subscriberTemplate The subscriberTemplate to set.
     */
    public void setSubscriberTemplate(final Subscriber subscriberTemplate)
    {
        this.subscriberTemplate_ = subscriberTemplate;
    }

    /**
     * @return Returns the subscriberWriter.
     */
    public PrintWriter getSubscriberWriter()
    {
        return subscriberWriter_;
    }

    /**
     * @param subscriberWriter The subscriberWriter to set.
     */
    public void setSubscriberWriter(final PrintWriter subscriberWriter)
    {
        this.subscriberWriter_ = subscriberWriter;
    }

    /**
     * @return Returns the subscriberErrWriter.
     */
    public PrintWriter getSubscriberErrWriter()
    {
        return subscriberErrWriter_;
    }

    /**
     * @param subscriberErrWriter The subscriberErrWriter to set.
     */
    public void setSubscriberErrWriter(final PrintWriter subscriberErrWriter)
    {
        this.subscriberErrWriter_ = subscriberErrWriter;
    }

    public void logErrorToFiles(final Context ctx, final String msg, final BulkLoadSubscriber bs)
    {
        // Write to log file: the error
        getSubscriberWriter().print(msg);

        // Write to error file: the error, the line that erred
        getSubscriberErrWriter().print(msg);
        getSubscriberErrWriter().println("   " + BulkLoadSubscriberCSVSupport.instance().toString(ctx, bs));
    }

}
