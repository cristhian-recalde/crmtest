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
package com.trilogy.app.crm.bean.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.TypeDependentExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.state.FinalStateAware;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.util.enabled.EnabledAware;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class AuxiliaryService extends AbstractAuxiliaryService implements FinalStateAware, EnabledAware
{

 	private Context context_;
    
    public Context getContext()
    {
        return context_;
    }
    
    public void setContext(Context context)
    {
        context_ = context;
    }
    public Set getPrepaidAuxiliaryBundles()
    {
    	return getPrepaidAuxiliaryBundles(getContext());
    }
    public Set<String> getPrepaidAuxiliaryBundles(Context ctx)
    {
    	Set<String>  auxBundlesSet = null;
        if (super.getPrepaidBundles()== null)
        {
        	auxBundlesSet = new HashSet<String>();
     	    setPrepaidAuxiliaryBundles(auxBundlesSet);
        }
        
        Set setObj = super.getPrepaidAuxiliaryBundles();
        Set<String> returnSet = new HashSet<String>();
        if(setObj != null)
        {
	        for(Object obj : setObj)
	        {
	        	returnSet.add(obj.toString());	
	        }
	        setPrepaidBundles(buildString(setObj));
	        setPrepaidAuxiliaryBundles(returnSet);
        }
        else
        {
        	returnSet = buildSet(super.getPrepaidBundles());	       
        	setPrepaidAuxiliaryBundles(returnSet);
        }
        return returnSet;
    }
    
    public Set getPostpaidAuxiliaryBundles()
    {
    	return getPostpaidAuxiliaryBundles(getContext());
    }
    public Set<String> getPostpaidAuxiliaryBundles(Context ctx)
    {
    	Set<String>  auxBundlesSet = null;
        if (super.getPrepaidBundles()== null)
        {
        	auxBundlesSet = new HashSet<String>();
     	    setPostpaidAuxiliaryBundles(auxBundlesSet);
        }
        
        Set setObj = super.getPostpaidAuxiliaryBundles();
        Set<String> returnSet = new HashSet<String>();
        if(setObj != null)
        {
	        for(Object obj : setObj)
	        {
	        	returnSet.add(obj.toString());	
	        }
	        setPostpaidBundles(buildString(setObj));
	        setPostpaidAuxiliaryBundles(returnSet);
        }
        else
        {
        	returnSet = buildSet(super.getPostpaidBundles());
        	setPostpaidAuxiliaryBundles(returnSet);
        }
        return returnSet;
    }
    private String buildString(Set set)
    {
    	StringBuilder buff = new StringBuilder();
    	if(set != null)
    	{
	        Object[] arr = set.toArray();
	        for (int x=0; x<arr.length; x++)
	        {
	            if (x!=0) buff.append(",");
	            buff.append(arr[x]);
	        }
    	}
        return buff.toString();
    }
    
    private Set<String> buildSet(String str)
    {
        Set<String> set = new HashSet<String>();
        if(str!= null)
        {
	        StringTokenizer st = new StringTokenizer(str,",");
	        while (st.hasMoreTokens()) {
	            set.add(st.nextToken());
	        }
        }
        return set;
    }

    public boolean isEnabled()
    {
        return this.getState() == AuxiliaryServiceStateEnum.ACTIVE;
    }

    public void setEnabled(final boolean flag)
    {
        if (!isInFinalState())
        {
            if (flag)
            {
                this.setState(AuxiliaryServiceStateEnum.ACTIVE);
            }
            else
            {
                this.setState(AuxiliaryServiceStateEnum.DEPRECATED);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public AbstractEnum getAbstractState()
    {
        return getState();
    }


    /**
     * {@inheritDoc}
     */
    public void setAbstractState(final AbstractEnum state)
    {
        setState((AuxiliaryServiceStateEnum) state);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Enum> getFinalStates()
    {
        return com.redknee.app.crm.bean.core.AuxiliaryService.FINAL_STATES;
    }


    /**
     * {@inheritDoc}
     */
    public <T extends Enum> boolean isFinalState(T state)
    {
        Collection<? extends Enum> finalStates = getFinalStates();
        return EnumStateSupportHelper.get().isOneOfStates(state, finalStates);
    }


    /**
     * {@inheritDoc}
     */
    public boolean isInFinalState()
    {
        Collection<? extends Enum> finalStates = getFinalStates();
        return EnumStateSupportHelper.get().isOneOfStates(this, finalStates);
    }
    

    public boolean isGroupChargable()
    {
        return isOfType(com.redknee.app.crm.bean.core.AuxiliaryService.GROUP_CHARGEABLE_SERVICES);
    }

    public boolean isHLRProvisionable()
    {
        return isOfType(com.redknee.app.crm.bean.core.AuxiliaryService.HLR_PROVISIONABLE_SERVICES);
    }

    /**
     * @param type List of possible enumerated subscription type values.
     * @return True if this subscription's type matches the input type
     */
    private boolean isOfType(Collection<AuxiliaryServiceTypeEnum> types)
    {
        if (types != null)
        {
            return types.contains(getType());
        }
        else
        {
            return false;
        }
    }

    public List getAuxiliaryServiceExtensions()
    {
        return getAuxiliaryServiceExtensions(ContextLocator.locate());
    }
    
    public List getAuxiliaryServiceExtensions(Context ctx)
    {
        synchronized (this)
        {
            if (super.getAuxiliaryServiceExtensions() == null)
            {
                try
                {
                    // To avoid deadlock, use a service "with extensions loaded" along with extension loading adapter.
                    AuxiliaryService auxServiceCopy = (AuxiliaryService) this.clone();
                    auxServiceCopy.setAuxiliaryServiceExtensions(new ArrayList());
                    
                    auxServiceCopy = (AuxiliaryService) new ExtensionLoadingAdapter<AuxiliaryServiceExtension>(AuxiliaryServiceExtension.class, AuxiliaryServiceExtensionXInfo.AUXILIARY_SERVICE_ID).adapt(ctx, auxServiceCopy);
                    auxServiceCopy = (AuxiliaryService) new ExtensionSpidAdapter().adapt(ctx, auxServiceCopy);
                    
                    this.setAuxiliaryServiceExtensions(auxServiceCopy.getAuxiliaryServiceExtensions());
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.");
                    LogSupport.debug(ctx, this, "Exception occurred loading extensions. Extensions NOT loaded.", e);
                }
            }
            else
            {
                for (ExtensionHolder holder : (List<ExtensionHolder>) super.getAuxiliaryServiceExtensions())
                {
                    if (holder.getExtension() instanceof SpidAware && this.getSpid()>0)
                    {
                        ((SpidAware) holder.getExtension()).setSpid(this.getSpid());
                    }
                }
            }
        }
        
        return super.getAuxiliaryServiceExtensions();
    }

    public PropertyInfo getExtensionHolderProperty()
    {
        return AuxiliaryServiceXInfo.AUXILIARY_SERVICE_EXTENSIONS;
    }

    public Collection<Extension> getExtensions()
    {
        return getExtensions(ContextLocator.locate());
    }
    
    public Collection<Extension> getExtensions(Context ctx)
    {
        Collection<ExtensionHolder> holders = (Collection<ExtensionHolder>) getExtensionHolderProperty().get(this);
        return ExtensionSupportHelper.get(ctx).unwrapExtensions(holders);
    }

    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        return com.redknee.app.crm.bean.core.AuxiliaryService.getExtensionTypes(ctx, this.getType());
    }
    @Override
    public int getTypeEnumIndex()
    {
        if (getType()!=null)
        {
            return getType().getIndex();
        }
        else
        {
            return -1;
        }
    }

    public boolean isPrivateCUG(Context ctx)
    {
        boolean result = false;
        if (this.getType() == AuxiliaryServiceTypeEnum.CallingGroup)
        {
            CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
            CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, this, CallingGroupAuxSvcExtension.class);
            if (callingGroupAuxSvcExtension!=null)
            {
                callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
            }
            else 
            {
                LogSupport.minor(ctx, this,
                        "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + this.getIdentifier());
            }
            result = CallingGroupTypeEnum.PCUG.equals(callingGroupType);
        }
        return result;
    }

    @Override
    public AbstractEnum getEnumType()
    {
        return getType();
    }
    
}
