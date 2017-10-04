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

import java.util.List;

import com.trilogy.app.crm.extension.PricePlanExtension;
import com.trilogy.app.crm.extension.PricePlanExtensionXInfo;
import com.trilogy.app.crm.support.generic.GenericParameterSupport;
import com.trilogy.app.crm.support.generic.exception.InvalidTypePassedException;
import com.trilogy.app.crm.support.generic.exception.KeyNotFoundInSpidException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author joe.chen@redknee.com
 */
public class PricePlanN extends AbstractPricePlanN implements Comparable, IdentifierAware
{
	private Context context_;
	private PricePlanExtension pricePlanExtension = null;
	
    public Context getContext()
    {
        if (context_ == null)
        {
        	context_ = ContextLocator.locate();
        }
        return context_;
    }
    
	public int compareTo(final Object obj)
    {
        final PricePlan other = (PricePlan) obj;

        if (getId() > other.getId())
        {
            return 1;
        }
        if (getId() < other.getId())
        {
            return -1;
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getIdentifier()
    {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    public void setIdentifier(final long id)
    {
        setId(id);
    }
    
    @Override
    public List getExtensionParameters()
    {
    	if(extensionParameters_ !=null && extensionParameters_.size()>0)
    		return extensionParameters_;
		return getExtensionParameters(getContext());
    }

    public List getExtensionParameters(Context context)
    {
    	GenericParameterSupport genericParameterSupport = (GenericParameterSupport)context.get(GenericParameterSupport.class);
    	if(genericParameterSupport == null)
    	{
    		if(LogSupport.isDebugEnabled(context))
    		{
    			LogSupport.debug(context, this, GenericParameterSupport.class.getName()+" is not installed in Context.");
    		}
			return null;
    	}

    	try 
    	{
    		extensionParameters_ = genericParameterSupport.getGenericAttributeContainerList(context, PricePlanExtension.class, getSpid(), getId(), PricePlanExtensionXInfo.PID);
    	} 
    	catch (HomeException e) 
    	{
    		LogSupport.minor(context, this, "Unable to retrieve list for PricePlanParameters."+e.getMessage());
    		return null;
    	}
    	return extensionParameters_;
    }
	

	@Override
	public <T> T getGenericParameter(Context ctx,String name,Class<T> key) throws InvalidTypePassedException,KeyNotFoundInSpidException
	{
		if(pricePlanExtension == null)
		{
			GenericParameterSupport genericParameterSupport = (GenericParameterSupport)ctx.get(GenericParameterSupport.class);
	    	if(genericParameterSupport == null)
	    	{
	    		if(LogSupport.isDebugEnabled(ctx))
	    		{
	    			LogSupport.debug(ctx, this, GenericParameterSupport.class.getName()+" is not installed in Context.");
	    		}
				return null;
	    	}
			List<PricePlanExtension> listOfPricePlanExtension = null;
			try 
			{
				listOfPricePlanExtension = genericParameterSupport.getGenericParameterFromExtensionTable(ctx, PricePlanExtension.class,
						getSpid(), getIdentifier(), PricePlanExtensionXInfo.PID);
				pricePlanExtension = new PricePlanExtension();
				pricePlanExtension.setPid(getIdentifier());
				pricePlanExtension.init(ctx,listOfPricePlanExtension);
			} 
			catch (HomeException e) {
				LogSupport.minor(ctx, this, "Unable to retrieve list for Price Plan Extension table."+e.getMessage());
	    		return null;
			}
		}
		return pricePlanExtension.getGenericParameter(ctx, name, key);
	}
	
	@Override
	public List<GenericParameter> getGenericParametersListFromTemplate(Context ctx) throws HomeException
	{
		GenericParameterSupport genericParameterSupport = (GenericParameterSupport)ctx.get(GenericParameterSupport.class);
    	if(genericParameterSupport == null)
    	{
    		if(LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, this, GenericParameterSupport.class.getName()+" is not installed in Context.");
    		}
			return null;
    	}
    	return genericParameterSupport.getGenericParametersListFromTemplate(ctx, PricePlanExtension.class, getSpid());
	}
}
