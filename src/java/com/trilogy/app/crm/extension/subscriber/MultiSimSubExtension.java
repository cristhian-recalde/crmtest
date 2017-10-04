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
package com.trilogy.app.crm.extension.subscriber;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.log.MultiSimProvisioningTypeEnum;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.support.SystemSupport;

/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class MultiSimSubExtension extends AbstractMultiSimSubExtension {

    private static final DecimalFormat ZERO_LED_DECIMAL_FORMAT = new DecimalFormat("00");
    private static final Collection<Long> AVAILABLE_IDS;
    static
    {
        Collection<Long> availableIds = new ArrayList<Long>(99);
        for (int i=1; i<100; i++)
        {
            availableIds.add(Long.valueOf(i));
        }
        AVAILABLE_IDS = Collections.unmodifiableCollection(availableIds);
    }
    
    /**
     * {@inheritDoc}
     */
    public void install(Context ctx) throws ExtensionInstallationException
    {
        // NOP
    }

    /**
     * {@inheritDoc}
     */
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        Context sCtx = ctx.createSubContext();
        
        DefaultExceptionListener el = new DefaultExceptionListener();
        sCtx.put(ExceptionListener.class, el);
     
        // Remove all SIM level services
        setSims(new ArrayList<SubscriberAuxiliaryService>());
        update(sCtx);
        
        if (el.hasErrors())
        {
            int numOfErrors = el.numOfErrors();
            String msg = "Error(s) occurred uninstalling individual SIMs.  [Count=" + numOfErrors + "].";
            new MinorLogMsg(this, msg + "  See DEBUG logs for error details.", null).log(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                int i=1;
                for (Throwable t : (List<Throwable>) el.getExceptions())
                {
                    new DebugLogMsg(this, "Error occurred uninstalling individual SIM (Error " + i + "/" + numOfErrors + ")", t).log(ctx);
                }
            }
            throw new ExtensionInstallationException(msg, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update(Context ctx) throws ExtensionInstallationException
    {
        ctx = ctx.createSubContext();
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Updating Multi-SIM extension for subscription " + this.getSubId() + " and auxiliary service ID " + this.getAuxSvcId(), null).log(ctx);
        }
        
        try
        {
            And associationExistanceFilter = new And();
            associationExistanceFilter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, this.getSubId()));
            associationExistanceFilter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, this.getAuxSvcId()));
            associationExistanceFilter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER));
            if (!HomeSupportHelper.get(ctx).hasBeans(ctx, SubscriberAuxiliaryService.class, associationExistanceFilter))
            {
                throw new ExtensionInstallationException(
                        "Unable to add SIMs to auxiliary service " + this.getAuxSvcId()
                        + " for subscription " + this.getSubId() + " because the subscription does not have that service.", false);
            }
        }
        catch (HomeException e)
        {
            throw new ExtensionInstallationException(
                    "Unable to verify existance of association of auxiliary service " + this.getAuxSvcId()
                    + " to subscription " + this.getSubId(), false);
        }
        
        // Put subscriber in the context so that downstream pipelines can retrieve it
        Subscriber sub = getSubscriber(ctx);
        ctx.put(Subscriber.class, sub);
        
        List<MultiSimRecordHolder> sims = getSims();
        if (sims == null)
        {
            sims = new ArrayList<MultiSimRecordHolder>();
            setSims(sims);
        }
        
        AuxiliaryService auxSvc = this.getAuxiliaryService(ctx);
        long maxNumSims = MultiSimAuxSvcExtension.DEFAULT_MAXNUMSIMS;
        MultiSimAuxSvcExtension multiSimAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSvc, MultiSimAuxSvcExtension.class);
        if (multiSimAuxSvcExtension != null)
        {
            maxNumSims = multiSimAuxSvcExtension.getMaxNumSIMs();
        }
        else 
        {
            LogSupport.minor(ctx, this,
                    "Unable to find required extension of type '" + MultiSimAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + auxSvc.getIdentifier());
        }

        if (sims.size() > maxNumSims)
        {
            throw new ExtensionInstallationException("Too many SIMs configured (" + sims.size() 
                    + ").  Maximum number of SIMs for auxiliary service " + auxSvc.getIdentifier()
                    + " is " + maxNumSims, false);
        }

        Queue<Long> freeSecondaryIds = new PriorityQueue<Long>(AVAILABLE_IDS);
        
        boolean simUpdateOccurred = false;
        
        Map<String, SubscriberAuxiliaryService> existingSimMap = new HashMap<String, SubscriberAuxiliaryService>();
        Collection<SubscriberAuxiliaryService> existingSims = getSimSpecificAuxiliaryService(ctx);
        if (existingSims != null)
        {
            for (SubscriberAuxiliaryService sim : existingSims)
            {
                if (sim != null)
                {
                    freeSecondaryIds.remove(sim.getSecondaryIdentifier());
                    existingSimMap.put(sim.getMultiSimPackage(), sim);
                }
            }
        }

        And filter = new And();
        filter.add(new EQ(MultiSimSubExtensionXInfo.SUB_ID, this.getSubId()));
        filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, this.getAuxSvcId()));
        
        List<MultiSimSubExtension> existingExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);
        if (existingExtensions != null && existingExtensions.size() > 0)
        {
            MultiSimSubExtension oldExtension = existingExtensions.get(0);
            if (oldExtension != null)
            {
                // Lazy-load old SIM information
                oldExtension.getSims();
            }
            ctx.put(Lookup.OLD_MULTISIM_SUB_EXTENSION, oldExtension);
        }
        
        Iterator<MultiSimRecordHolder> iter = sims.iterator();
        while (iter.hasNext())
        {
            MultiSimRecordHolder sim = iter.next();
            SubscriberAuxiliaryService service = existingSimMap.remove(sim.getPackageID());
            if (service != null)
            {
                // Existing SIM.  Handle cases that may require provisioning updates (e.g. MSISDN change)
                if (!SafetyUtil.safeEquals(service.getMultiSimMsisdn(), sim.getMsisdn()))
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Dummy MSISDN for SIM has changed from [" + service.getMultiSimMsisdn() + "] to [" + sim.getMsisdn()
                                + "].  Attempting to update secondary association for auxiliary service [" + this.getAuxSvcId()
                                + "] and subscription [" + this.getSubId() + "]...", null).log(ctx);
                    }
                    
                    service.setMultiSimMsisdn(sim.getMsisdn());
                    try
                    {
                        service = HomeSupportHelper.get(ctx).storeBean(ctx, service);
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Error changing dummy MSISDN from " + service.getMultiSimMsisdn()
                                + " to " + sim.getMsisdn() + " for SIM data [SubscriptionID=" + this.getSubId()
                                + ",AuxSvcId=" + this.getAuxSvcId()
                                + ",SecondaryAuxSvcId=" + service.getSecondaryIdentifier()
                                + ",PackageID=" + sim.getPackageID(), e).log(ctx);

                        FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
                    }

                    simUpdateOccurred = true;
                }
                if(sim.isSimSwapped())
                {
                	MultiSimRecordHolder newSim = sim.getNewSimAfterSwap();
                	if (LogSupport.isDebugEnabled(ctx))
                    {
                		
                		new DebugLogMsg(this, "MultiSim swap - OldImsi [" + service.getMultiSimImsi() + "] to New [" + newSim.getImsi()
                                + "].For auxiliary service [" + this.getAuxSvcId()+ "] and subscription [" + this.getSubId() + "]", null).log(ctx);
                    }
                        
                    service.setMultiSimPackage(newSim.getPackageID());
                    service.setMultiSimImsi(newSim.getImsi());
                    service.setMultiSimMsisdn(sim.getMsisdn());
                    try
                    {
                        service = HomeSupportHelper.get(ctx).storeBean(ctx, service);
                        simUpdateOccurred = true;
                    }
                    catch (HomeException e)
                    {
                    	new MinorLogMsg(this, "Error swapping sim from " + sim.getImsi()
                                + " to " + service.getMultiSimImsi() + " for SIM data [SubscriptionID=" + this.getSubId()
                                + ",AuxSvcId=" + this.getAuxSvcId()
                                + ",SecondaryAuxSvcId=" + service.getSecondaryIdentifier(), e).log(ctx);
                    	FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
                    	//modify swap data so as to log correct Er
                    	sim.setNewSimAfterSwap(null);
                    	continue;
                    }
                    
                    
                }
            }
            else
            {
                // New SIM.  Must create auxiliary service association.
                if (sub == null)
                {
                    continue;
                }

                if (freeSecondaryIds.isEmpty())
                {
                    String msg = "Unable to add SIM with package ID " + sim.getPackageID()
                    + ".  No free secondary identifiers are available for Multi-SIM services for subscription " + this.getSubId()
                    + " and auxiliary service " + this.getAuxSvcId();
                    FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new ExtensionInstallationException(msg, false));
                    continue;
                }

                long nextSecondaryId = freeSecondaryIds.poll();
                try
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "New SIM detected [PackageID=" + sim.getPackageID()
                                + "].  Attempting to create secondary association for auxiliary service [" + this.getAuxSvcId()
                                + "] and subscription [" + this.getSubId() + "]...", null).log(ctx);
                    }

                    // Create new service for new SIM
                    try
                    {
                        service = (SubscriberAuxiliaryService) XBeans.instantiate(SubscriberAuxiliaryService.class, ctx);
                    }
                    catch (Exception e)
                    {
                        service = new SubscriberAuxiliaryService();
                    }

                    service.setType(AuxiliaryServiceTypeEnum.MultiSIM);

                    service.setIdentifier(getAuxSvcId());
                    service.setSubscriberIdentifier(this.getSubId());
                    service.setAuxiliaryServiceIdentifier(getAuxSvcId());
                    service.setSecondaryIdentifier(nextSecondaryId);

                    service.setMultiSimPackage(sim.getPackageID());
                    service.setMultiSimImsi(sim.getImsi());
                    service.setMultiSimMsisdn(generateDummyMsisdn(ctx,sub.getMsisdn(), nextSecondaryId));

                    // going to provision it to set to true
                    service.setProvisioned(true);

                    service = HomeSupportHelper.get(ctx).createBean(ctx, service);

                    simUpdateOccurred = true;
                }
                catch (HomeException e)
                {
                    // Failed to create the service.  Put the ID that it would have used back in the queue.
                    freeSecondaryIds.offer(nextSecondaryId);

                    new MinorLogMsg(this, "Error creating SIM data [SubscriptionID=" + this.getSubId()
                            + ",AuxSvcId=" + this.getAuxSvcId()
                            + ",PackageID=" + sim.getPackageID(), e).log(ctx);

                    FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);

                    // Remove failed SIM from list.  This is primarily for ER logging purposes.
                    iter.remove();
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "SIM record removed from extension for failed SIM creation: " + sim, null).log(ctx);
                    }
                    continue;
                }
            }

            if (service != null)
            {
                // Update transient properties after processing the association in case the state changed
                sim.setMsisdn(service.getMultiSimMsisdn());
                sim.setProvCode(getProvCode(ctx, service));

                short chargeCode = getChargeCode(ctx, auxSvc, service);
                if (isChargePerSim())
                {
                    if (sim.getProvCode() == 0
                            && chargeCode != 0)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Attempting per-SIM charge for secondary association for auxiliary service [" + this.getAuxSvcId()
                                + "] and subscription [" + this.getSubId() + "]...", null).log(ctx);
                        }
                        
                        // Attempt the charge if it hasn't been charged yet
                        CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(sub, service);
                        chargeCode = Integer.valueOf(charger.charge(ctx, null)).shortValue();

                        simUpdateOccurred = true;
                    }
                }
                
                sim.setChargeCode(chargeCode);
            }
        }
        
        for (SubscriberAuxiliaryService deletedService : existingSimMap.values())
        {
            try
            {
                // Remove service as SIM was deleted from list
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Attempting to remove SIM package " + deletedService.getMultiSimPackage()
                            + " association for auxiliary service [" + this.getAuxSvcId()
                            + "] and subscription [" + this.getSubId() + "]...", null).log(ctx);
                }
                
                HomeSupportHelper.get(ctx).removeBean(ctx, deletedService);

                if (isChargePerSim())
                {
                    short chargeCode = getChargeCode(ctx, auxSvc, deletedService);
                    if (chargeCode == 0)
                    {
                        // Refund the prorated service fee if it was previously charged successfully
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Attempting per-SIM refund for secondary association for auxiliary service [" + this.getAuxSvcId()
                                + "] and subscription [" + this.getSubId() + "]...", null).log(ctx);
                        }
                        
                        CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(sub, deletedService);
                        charger.refund(ctx, null);
                    }
                }
                
                simUpdateOccurred = true;
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error removing SIM data [SubscriptionID=" + this.getSubId()
                        + ",AuxSvcId=" + this.getAuxSvcId()
                        + ",PackageID=" + deletedService.getMultiSimPackage(), e).log(ctx);
                
                FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
                
                // Add SIM back to list.  This is primarily for ER logging purposes.
                MultiSimRecordHolder record = generateMultiSimRecordHolder(ctx, deletedService);
                sims.add(record);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "SIM record added back to extension for failed SIM removal: " + record, null).log(ctx);
                }
            }
        }

        if (simUpdateOccurred)
        {
            ERLogger.createMultiSimProvisioningEr(ctx, this, MultiSimProvisioningTypeEnum.SIM_UPDATED);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deactivate(Context ctx) throws ExtensionInstallationException
    {
        uninstall(ctx);
    }
    
    /**
     * {@inheritDoc}
     */
    public void move(Context ctx, Object newContainer) throws ExtensionInstallationException
    {
        if (newContainer instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) newContainer;
            
            /*
             * From SgR:
             * 2.4.9. Account Move and mSIM Interactions
             *        The account move is a seemless operation from the user perspective, however, system removes
             *        all SIMs attached at the time of the move from the HLR and then reattaches them after the move.
             */ 
            // TODO: What?  Move doesn't normally do any provisioning, so I don't see why this is necessary ...
        }
    }
    
    public void changeMainMsisdn(Context ctx, String oldMsisdn, String newMsisdn)
    {
        List<MultiSimRecordHolder> sims = getSims();
        if (sims != null)
        {
            int counter = 1;
            for (MultiSimRecordHolder sim : sims)
            {
                if (sim != null)
                {
                    sim.setMsisdn(generateDummyMsisdn(ctx,newMsisdn, counter++));
                }
            }
        }
    }

    private String generateDummyMsisdn(Context ctx,String msisdn, long counter)
    {
        if(SystemSupport.generateDummyMsisdnForMultisim(ctx))
        {
	    	if (msisdn.length() > 3)
	        {
	            return String.valueOf(new StringBuilder(msisdn).insert(3, ZERO_LED_DECIMAL_FORMAT.format(counter)));
	        }
	        else
	        {
	            return msisdn + ZERO_LED_DECIMAL_FORMAT.format(counter);
	        }
        }
        return msisdn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuxSvcName()
    {
        AuxiliaryService auxiliaryService = getAuxiliaryService(ContextLocator.locate());
        if (auxiliaryService != null)
        {
            setAuxSvcName(auxiliaryService.getName());
        }
        return super.getAuxSvcName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCharge()
    {
        AuxiliaryService auxiliaryService = getAuxiliaryService(ContextLocator.locate());
        if (auxiliaryService != null)
        {
            setCharge(auxiliaryService.getCharge());
        }
        return super.getCharge();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getChargePerSim()
    {
        return getChargePerSim(ContextLocator.locate());
    }

    public boolean getChargePerSim(Context ctx)
    {
        AuxiliaryService auxiliaryService = getAuxiliaryService(ContextLocator.locate());
        if (auxiliaryService != null)
        {
            boolean isChargePerSim = MultiSimAuxSvcExtension.DEFAULT_CHARGEPERSIM;
            MultiSimAuxSvcExtension multiSimAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxiliaryService, MultiSimAuxSvcExtension.class);
            if (multiSimAuxSvcExtension != null)
            {
                isChargePerSim = multiSimAuxSvcExtension.isChargePerSim();
            }
            else 
            {
                LogSupport.minor(ctx, this,
                        "Unable to find required extension of type '" + MultiSimAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + auxiliaryService.getIdentifier());
            }
            setChargePerSim(isChargePerSim);
        }
        return super.getChargePerSim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMaxNumSims()
    {
        return getMaxNumSims(ContextLocator.locate());
    }
    
    public long getMaxNumSims(Context ctx)
    {
        AuxiliaryService auxiliaryService = getAuxiliaryService(ContextLocator.locate());
        if (auxiliaryService != null)
        {
            long maxNumSims = MultiSimAuxSvcExtension.DEFAULT_MAXNUMSIMS;
            MultiSimAuxSvcExtension multiSimAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxiliaryService, MultiSimAuxSvcExtension.class);
            if (multiSimAuxSvcExtension != null)
            {
                maxNumSims = multiSimAuxSvcExtension.getMaxNumSIMs();
            }
            else 
            {
                LogSupport.minor(ctx, this,
                        "Unable to find required extension of type '" + MultiSimAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + auxiliaryService.getIdentifier());
            }
            setMaxNumSims(maxNumSims);
        }
        return super.getMaxNumSims();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List getSims()
    {
        synchronized (SUB_AUX_SVC_LOCK)
        {
            if (sims_ == null)
            {
                Context ctx = ContextLocator.locate();
                
                List<MultiSimRecordHolder> sims = null;
                
                Collection<SubscriberAuxiliaryService> auxSvcs = getSimSpecificAuxiliaryService(ctx);
                if (auxSvcs != null)
                {
                    sims = new ArrayList<MultiSimRecordHolder>();

                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Adapting " + auxSvcs.size() + " auxiliary service associations to " + MultiSimRecordHolder.class.getName(), null).log(ctx);
                    }
                    
                    for (SubscriberAuxiliaryService association : auxSvcs)
                    {
                        MultiSimRecordHolder record = generateMultiSimRecordHolder(ctx, association);
                        
                        sims.add(record);
                        
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "SIM record added to extension: " + record, null).log(ctx);
                        }
                    }
                }
                
                if (sims != null)
                {
                    setSims(sims);
                }
            }
        }
        return super.getSims();
    }

    private MultiSimRecordHolder generateMultiSimRecordHolder(Context ctx, SubscriberAuxiliaryService association)
    {
        MultiSimRecordHolder record;
        try
        {
            record = (MultiSimRecordHolder) XBeans.instantiate(MultiSimRecordHolder.class, ctx);
        }
        catch (Exception e)
        {
            record = new MultiSimRecordHolder();
        }
        
        record.setPackageID(association.getMultiSimPackage());
        record.setImsi(association.getMultiSimImsi());
        record.setMsisdn(association.getMultiSimMsisdn());
        
        short provCode = getProvCode(ctx, association);
        record.setProvCode(provCode);
        
        short chargeCode = getChargeCode(ctx, getAuxiliaryService(ctx), association);
        record.setChargeCode(chargeCode);
        return record;
    }

    protected short getProvCode(Context ctx, SubscriberAuxiliaryService association)
    {
        LogMsg pm = new PMLogMsg(this.getClass().getName(), "getProvCode()");
        try
        {
            short provCode = MultiSimProvisioningTypeEnum.UNPROVISIONED_INDEX;
            boolean isSuspended = false;
            try
            {
                isSuspended = SuspendedEntitySupport.isObjectSuspended(ctx, association.getSubscriberIdentifier(), association);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error determining provisioning state of SIM Package " + association.getMultiSimPackage(), e).log(ctx);
            }
            if (isSuspended)
            {
                provCode = MultiSimProvisioningTypeEnum.SUSPENDED_INDEX;
            }
            else if (association.isProvisioned())
            {
                provCode = 0;
            }
            return provCode;
        }
        finally
        {
            pm.log(ctx);
        }
    }

    protected short getChargeCode(Context ctx, AuxiliaryService auxiliaryService, SubscriberAuxiliaryService association)
    {
        LogMsg pm = new PMLogMsg(this.getClass().getName(), "getChargeCode()");
        try
        {
            boolean isCharged = false;
            try
            {
                if (this.isChargePerSim())
                {
                    isCharged = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(ctx, 
                            getSubscriber(ctx), 
                            association, 
                            ChargedItemTypeEnum.AUXSERVICE, 
                            auxiliaryService.getChargingModeType(), 
                            auxiliaryService.getAdjustmentType(), 
                            auxiliaryService.getCharge(), 
                            CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                }
                else
                {
                    SubscriberAuxiliaryService dummyService = null;
                    try
                    {
                        dummyService = (SubscriberAuxiliaryService) XBeans.instantiate(SubscriberAuxiliaryService.class, ctx);
                    }
                    catch (Exception e)
                    {
                        dummyService = new SubscriberAuxiliaryService();
                    }
                    dummyService.setAuxiliaryServiceIdentifier(association.getAuxiliaryServiceIdentifier());
                    isCharged = RecurringRechargeSupport.isSubscriberChargedAndNotRefunded(ctx, 
                            getSubscriber(ctx), 
                            dummyService, 
                            ChargedItemTypeEnum.AUXSERVICE, 
                            auxiliaryService.getChargingModeType(), 
                            auxiliaryService.getAdjustmentType(), 
                            auxiliaryService.getCharge(), 
                            CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error determining charging state of SIM Package " + association.getMultiSimPackage(), e).log(ctx);
            }
            short chargeCode = Integer.valueOf(isCharged ? 0 : 1).shortValue();
            return chargeCode;

        }
        finally
        {
            pm.log(ctx);
        }
    }
    
    public AuxiliaryService getAuxiliaryService(Context ctx)
    {
        synchronized(AUX_SVC_LOCK)
        {
            if (auxSvc_ == null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Loading auxiliary service (" + this.getAuxSvcId() + ") for subscription " + this.getSubId(), null).log(ctx);
                }
                try
                {
                    auxSvc_ = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, this.getAuxSvcId());
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error retrieving auxiliary service [ID=" + this.getAuxSvcId() + "]", e).log(ctx);
                }

                if (LogSupport.isDebugEnabled(ctx))
                {
                    if (auxSvc_ == null)
                    {
                        new DebugLogMsg(this, "Auxiliary service (" + this.getAuxSvcId() + ") not found!", null).log(ctx);
                    }
                    if (auxSvc_ == null)
                    {
                        new DebugLogMsg(this, "Auxiliary service (" + this.getAuxSvcId() + ") loaded successfully.", null).log(ctx);
                    }
                }
            }
            return auxSvc_;
        }
    }
    
    private Collection<SubscriberAuxiliaryService> getSimSpecificAuxiliaryService(Context ctx)
    {
        And filter = new And();
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, this.getAuxSvcId()));
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, this.getSubId()));
        filter.add(new NEQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER));
        
        Collection<SubscriberAuxiliaryService> beans = null;
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Loading auxiliary service (" + this.getAuxSvcId()
                        + ") associations for subscription " + this.getSubId(), null).log(ctx);
            }
            beans = HomeSupportHelper.get(ctx).getBeans(ctx,
                    SubscriberAuxiliaryService.class, 
                    filter, 
                    true, SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving subscriber auxiliary services [ID=" + this.getAuxSvcId() + ",SubId=" + this.getSubId() + "]", e).log(ctx);
        }
        
        if (beans != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Auxiliary service (" + this.getAuxSvcId()
                        + ") associations for subscription " + this.getSubId() + " loaded.  Count=" + beans.size(), null).log(ctx);
            }
            
            beans = new ArrayList<SubscriberAuxiliaryService>(beans);
            Iterator<SubscriberAuxiliaryService> iter = beans.iterator();
            while (iter.hasNext())
            {
                SubscriberAuxiliaryService svc = iter.next();
                if (svc != null 
                        && svc.getType(ctx) != AuxiliaryServiceTypeEnum.MultiSIM)
                {
                    iter.remove();
                }
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Secondary Multi-SIM auxiliary service (" + this.getAuxSvcId()
                        + ") associations for subscription " + this.getSubId() + " loaded.  Count=" + beans.size(), null).log(ctx);
            }
        }
        else if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Auxiliary service (" + this.getAuxSvcId()
                    + ") associations for subscription " + this.getSubId() + " not found!", null).log(ctx);
        }
        
        return beans;
    }

    private transient Object AUX_SVC_LOCK = new Object();
    private transient Object SUB_AUX_SVC_LOCK = new Object();
    private transient AuxiliaryService auxSvc_ = null;
}
