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
package com.trilogy.app.crm.api.rmi.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.MultiSimRecordHolder;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtensionSequence_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimChargeType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimChargeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MultiSimSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.MutableMultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PackageReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlyMultiSimSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.ReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension;


/**
 * This adapter converts API Multi-SIM extensions to/from CRM Multi-SIM extensions
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class MultiSimSubscriberExtensionToApiAdapter extends SubscriberExtensionToApiAdapter
{
    private static final long serialVersionUID = 1L;
    public MultiSimSubscriberExtensionToApiAdapter(Subscriber subscriber)
    {
        super(subscriber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseSubscriptionExtensionReference toAPIReference(Context ctx, SubscriberExtension extension)
            throws HomeException
    {
        MultiSimSubscriptionExtensionReference apiReference = new MultiSimSubscriptionExtensionReference();
        ReadOnlyMultiSimSubscriptionExtension apiExtension = (toAPI(ctx, extension)).getBaseReadOnlySubscriptionExtensionSequence_type3().getMultiSim();
        
        apiReference.setAuxiliaryServiceId(apiExtension.getAuxiliaryServiceId());
        
        SubscriberToApiAdapter adapter = new SubscriberToApiAdapter();
        apiReference.setSubscriptionRef((SubscriptionReference) adapter.adapt(ctx, getSubscriber()));
        
        return apiReference;        
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlySubscriptionExtension toAPI(Context ctx, SubscriberExtension extension) throws HomeException
    {
        // Instantiating read only subscription extension
        ReadOnlyMultiSimSubscriptionExtension apiExtension;
        try
        {
            apiExtension = (ReadOnlyMultiSimSubscriptionExtension) XBeans.instantiate(ReadOnlyMultiSimSubscriptionExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new Read Only Multi-SIM extension. Using default constructor.", e).log(ctx);
            apiExtension = new ReadOnlyMultiSimSubscriptionExtension();
        }

        
        // Populating read only subscription extension
        MultiSimSubExtension crmExtension = (MultiSimSubExtension) extension;
        
        apiExtension.setAuxiliaryServiceId(crmExtension.getAuxSvcId());
        apiExtension.setCharge(crmExtension.getCharge());
        
        MultiSimChargeType chargeMode = crmExtension.isChargePerSim() ? MultiSimChargeTypeEnum.PER_SIM.getValue() : MultiSimChargeTypeEnum.PER_SERVICE.getValue();
        apiExtension.setChargeMode(chargeMode);
        
        apiExtension.setMaxNumSIMs(crmExtension.getMaxNumSims());

        List<MultiSimRecordHolder> sims = crmExtension.getSims();
        PackageReference[] refArray = adaptSimsToAPI(ctx, sims);
        apiExtension.setPackageRefs(refArray);
        
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();

        BaseReadOnlySubscriptionExtensionSequence_type3 choice = new BaseReadOnlySubscriptionExtensionSequence_type3();
        choice.setMultiSim(apiExtension);
        result.setBaseReadOnlySubscriptionExtensionSequence_type3(choice);

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriberExtension toCRM(Context ctx, Object obj, GenericParameter[] parameters) throws HomeException
    {
        MultiSimSubExtension crmExtension;
        try
        {
            crmExtension = (MultiSimSubExtension) XBeans.instantiate(MultiSimSubExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Error instantiating new Multi-SIM extension. Using default constructor.", e).log(ctx);
            crmExtension = new MultiSimSubExtension();
        }
        
        MultiSimSubscriptionExtension apiExtension;
        
        if (obj instanceof SubscriptionExtension)
        {
            apiExtension = ((SubscriptionExtension) obj).getBaseSubscriptionExtensionSequence_type3().getMultiSim();
        }
        else
        {
            apiExtension = (MultiSimSubscriptionExtension) obj;
        }

        crmExtension.setSubId(getSubscriber().getId());
        crmExtension.setBAN(getSubscriber().getBAN());
        crmExtension.setSpid(getSubscriber().getSpid());
        
        if (apiExtension.getAuxiliaryServiceId() != null)
        {
            crmExtension.setAuxSvcId(apiExtension.getAuxiliaryServiceId());
        }

        PackageReference[] packageRefs = apiExtension.getPackageRefs();
        List<MultiSimRecordHolder> sims = adaptSimsToCRM(ctx, crmExtension.getSims(), packageRefs,parameters);
        
        crmExtension.setSims(sims);
        
        return crmExtension;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(Context ctx, BaseSubscriptionExtensionReference extensionReference, 
            BaseMutableSubscriptionExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, MultiSimSubExtension.class, this);
        
        boolean extensionExists = false;
        And filter = new And();
        filter.add(new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        if (extensionReference instanceof MultiSimSubscriptionExtensionReference)
        {
            MultiSimSubscriptionExtensionReference multiSimRef = (MultiSimSubscriptionExtensionReference) extensionReference;
            Long auxiliaryServiceId = multiSimRef.getAuxiliaryServiceId();
            if (auxiliaryServiceId != null)
            {
                filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, auxiliaryServiceId));
            }
        }
        else
        {
            final String msg = "Subscription Extension type not supported: " + extension.getClass().getSimpleName();
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
        }
        List<MultiSimSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);
        for (MultiSimSubExtension crmExtension : crmExtensions)
        {
            extensionExists = true;
            updateMultiSimSubscriptionExtension(ctx, crmExtension, (MutableMultiSimSubscriptionExtension) extension,parameters);
            
            try
            {
                Home extensionHome = getExtensionHome(ctx, ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmExtension));
                if (extensionHome != null)
                {
                    Context sCtx = ctx.createSubContext();
                    CompoundIllegalStateException el = new CompoundIllegalStateException();
                    sCtx.put(ExceptionListener.class, el);
                    crmExtension = (MultiSimSubExtension) extensionHome.store(sCtx, crmExtension);  
                    if (el.getSize() > 0)
                    {
                        final String msg = "Extension updated with errors.  See entries for details.";
                        RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, el, msg, this);
                    }
                } 
                else
                {
                    final String msg = "Subscription Extension type not supported: " + crmExtension.getClass().getName();
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
                }
            }
            catch (CRMExceptionFault e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                final String msg = "Unable to update Multi-SIM Subscription Extension";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
            
            break;
        }
        return extensionExists;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Context ctx, BaseSubscriptionExtensionReference extension, 
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, MultiSimSubExtension.class, this);
        
        And filter = new And();
        filter.add(new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        if (extension instanceof MultiSimSubscriptionExtensionReference)
        {
            MultiSimSubscriptionExtensionReference multiSimRef = (MultiSimSubscriptionExtensionReference) extension;
            Long auxiliaryServiceId = multiSimRef.getAuxiliaryServiceId();
            if (auxiliaryServiceId != null)
            {
                filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, auxiliaryServiceId));
            }
        }
        List<MultiSimSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);
        for (MultiSimSubExtension crmExtension : crmExtensions)
        {
            try
            {
                Home extensionHome = getExtensionHome(ctx, ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmExtension));
                if (extensionHome != null)
                {
                    extensionHome.remove(ctx, crmExtension);
                }
                else
                {
                    final String msg = "Subscription Extension type not supported: " + crmExtension.getClass().getName();
                    RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, this);
                }
            }
            catch (CRMExceptionFault e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                final String msg = "Unable to remove Multi-SIM Subscription Extension";
                RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlySubscriptionExtension find(Context ctx, BaseSubscriptionExtensionReference extension)
            throws CRMExceptionFault
    {
        And filter = new And();
        filter.add(new EQ(SubscriberExtensionXInfo.SUB_ID, getSubscriber().getId()));
        if (extension instanceof MultiSimSubscriptionExtensionReference)
        {
            MultiSimSubscriptionExtensionReference multiSimRef = (MultiSimSubscriptionExtensionReference) extension;
            Long auxiliaryServiceId = multiSimRef.getAuxiliaryServiceId();
            if (auxiliaryServiceId != null)
            {
                filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, auxiliaryServiceId));
            }
        }
        List<MultiSimSubExtension> crmExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);
        for (MultiSimSubExtension crmExtension : crmExtensions)
        {
            try
            {
                return this.toAPI(ctx, crmExtension);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Multi-SIM Subscription Extension";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            break;
        }
        // Returning read only default subscription extension with correct choice
        final ReadOnlySubscriptionExtension result = new ReadOnlySubscriptionExtension();
        BaseReadOnlySubscriptionExtensionSequence_type3 choice = new BaseReadOnlySubscriptionExtensionSequence_type3();
        choice.setMultiSim(null);
        result.setBaseReadOnlySubscriptionExtensionSequence_type3(choice);
        return result;    
    }


    private PackageReference[] adaptSimsToAPI(Context ctx, List<MultiSimRecordHolder> sims)
    {
        List<PackageReference> refs = new ArrayList<PackageReference>();
        if (sims != null)
        {
            for (MultiSimRecordHolder sim : sims)
            {
                if (sim != null)
                {
                    PackageReference ref;
                    try
                    {
                        ref = (PackageReference) XBeans.instantiate(PackageReference.class, ctx);
                    }
                    catch (Exception e)
                    {
                        ref = new PackageReference();
                    }
                    ref.setPackageID(sim.getPackageID());
                    ref.setImsi(sim.getImsi());
                    ref.setMsisdn(sim.getMsisdn());
                    refs.add(ref);
                }
            }
        }
        PackageReference[] refArray = refs.toArray(new PackageReference[]{});
        return refArray;
    }


    private List<MultiSimRecordHolder> adaptSimsToCRM(Context ctx, List<MultiSimRecordHolder> oldSims, PackageReference[] refs,GenericParameter[] parameters) 
    {
        Map<String, MultiSimRecordHolder> oldSimMap = new HashMap<String, MultiSimRecordHolder>();
        if (oldSims != null)
        {
            for (MultiSimRecordHolder oldSim : oldSims)
            {
                if (oldSim != null)
                {
                    oldSimMap.put(oldSim.getPackageID(), oldSim);
                }
            }
        }
        
        Map<String, MultiSimRecordHolder> swappedSims = new HashMap<String, MultiSimRecordHolder>();
        
        GenericParameterParser genericParameterParser = null;
        
        if (parameters!=null)
        {
        	genericParameterParser = new GenericParameterParser(parameters);
        }
        try{
        	if(genericParameterParser != null)
        	{
	        	boolean isSimSwapped = Boolean.TRUE.equals(genericParameterParser.getParameter(APIGenericParameterSupport.SIM_SWAP_OCCURED, Boolean.class));
	        	String allSimsSwappingInfoParam = genericParameterParser.getParameter(APIGenericParameterSupport.SIM_SWAP_DETAILS, String.class);
	        	
	        	if(isSimSwapped && allSimsSwappingInfoParam != null)
	        	{
	        		String[] allSimsSwappingInfo = allSimsSwappingInfoParam.split(",");
	        		for(String swappingInfo : allSimsSwappingInfo)
	        		{
	        			String[] oldSimXnewSim = swappingInfo.split("\\|");
	        			MultiSimRecordHolder newSim = createSimRecordHolder(ctx);
	        			
	        			newSim.setPackageID(oldSimXnewSim[1]);
	        			swappedSims.put(oldSimXnewSim[0], newSim);
	        		}
	        	}
        	}
        }catch(CRMExceptionFault ce)
        {
        	new MinorLogMsg(this, "Exception while extracting sim swap info from Generic Parameters.", ce).log(ctx);
        }catch(Exception e)
        {
        	new MinorLogMsg(this, "Exception while parsing Generic Parameters for SlaveSim swap.", e).log(ctx);
        }
        
        List<MultiSimRecordHolder> sims = new ArrayList<MultiSimRecordHolder>();
        if (refs != null && refs.length > 0)
        {
            for (PackageReference ref : refs)
            {
                if (ref != null)
                {
                    MultiSimRecordHolder sim = createSimRecordHolder(ctx);
                    
                    sim.setPackageID(ref.getPackageID());
                    
                    MultiSimRecordHolder oldSim = oldSimMap.get(ref.getPackageID());
                    if (oldSim != null)
                    {
                        // Retain the old IMSI/dummy MSISDN values
                        sim.setImsi(oldSim.getImsi());
                        sim.setMsisdn(oldSim.getMsisdn());
                    }
                    MultiSimRecordHolder newSimForSamePkgId = swappedSims.get(ref.getPackageID());
                    if(newSimForSamePkgId != null)
                    {
                    	newSimForSamePkgId.setMsisdn(oldSim.getMsisdn());
                    	sim.setNewSimAfterSwap(newSimForSamePkgId);
                    }
                    sims.add(sim);
                }
            }
        }
        
        return sims;
    }
    
    private void updateMultiSimSubscriptionExtension(Context ctx, MultiSimSubExtension crmExtension, MutableMultiSimSubscriptionExtension extension,GenericParameter[] parameters) throws CRMExceptionFault
    {
        PackageReference[] packageRefs = extension.getPackageRefs();
        if (packageRefs != null)
        {
            List<MultiSimRecordHolder> sims = adaptSimsToCRM(ctx, crmExtension.getSims(), packageRefs,parameters);
            crmExtension.setSims(sims);            
        }else{
        	crmExtension.setSims(new ArrayList<MultiSimRecordHolder>());
        }
        
        
    }
    
    private MultiSimRecordHolder createSimRecordHolder(Context ctx)
    {
    	MultiSimRecordHolder sim;
        try
        {
            sim = (MultiSimRecordHolder) XBeans.instantiate(MultiSimRecordHolder.class, ctx);
        }
        catch (Exception e)
        {
            sim = new MultiSimRecordHolder();
        }
        return sim;
    }
}
