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
package com.trilogy.app.crm.contract;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Creates new adjustment types for new contracts, updates and look-ups existing adjustment
 * types for existing contracts.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class ContractTermAdjustmentTypeCreationHome extends HomeProxy
{

    /**
     * Creates a new ContractTermAdjustmentTypeCreationHome.
     * 
     * @param delegate The home to which we delegate.
     */
    public ContractTermAdjustmentTypeCreationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * Creates a new ContractTermAdjustmentTypeCreationHome.
     * 
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public ContractTermAdjustmentTypeCreationHome(final Context context, final Home delegate)
    {
        super(delegate);
        setContext(context);
    }

    // INHERIT
    @Override
    public Object create(Context ctx, final Object bean) throws HomeException
    {
        SubscriptionContractTerm contract = (SubscriptionContractTerm) bean;
        try
        {
            final AdjustmentType type = createAdjustmentType(ctx, contract);
            contract.setContractAdjustmentTypeId(type.getCode());            
        }
        catch (final HomeException exception)
        {
            // If an exception occurs, then we want to act as if the service
            // creation failed, so we must attempt to remove it.  We can ignore
            // any complaints during the removal.
            try
            {
                super.remove(ctx, contract);
            }
            catch (final Throwable throwable)
            {
                new MinorLogMsg(this, "Failed to remove defunct contract from database.", throwable).log(ctx);
            }

            throw exception;
        }
        SubscriptionContractTerm result = (SubscriptionContractTerm) super.create(ctx, bean);

        return result;
    }


    // INHERIT
    @Override
    public Object store(Context ctx, final Object bean) throws HomeException
    {
        final SubscriptionContractTerm contract = (SubscriptionContractTerm) bean;

        if (contract != null)
        {
            storeAdjustmentType(ctx, contract);
        }

        return super.store(ctx, contract);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        throw new HomeException("Can not remove contract term!");
    }
    
    /**
     * Creates an adjustment type for a service.
     *
     * @param ctx
     *            The operating context.
     * @param contract
     *            The contract to create adjustment type for.
     * @param name
     *            The name of the adjustment type to create.
     * @param glCode
     *            GL code to use for this adjustment type.
     * @return The new adjustment type with the provided name.
     * @throws HomeException
     *             Thrown if there are problems creating the adjustment type.
     */
    private AdjustmentType createAdjustmentType(final Context ctx, final SubscriptionContractTerm contract) throws HomeException
    {
        AdjustmentType type = null;
        try
        {
            type = (AdjustmentType) XBeans.instantiate(AdjustmentType.class, ctx);
        }
        catch (final Exception e)
        {
            throw new HomeException("Failed to instantiate AdjustmentType", e);
        }
        type.setParentCode(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(ctx,
                AdjustmentTypeEnum.Other));

        String desc = "[contract => " + contract.getId() + " ]";
        if (contract.getName().length() < 50)
        {
            type.setName(contract.getName());
        }
        else
        {
            type.setName(desc);
        }
        type.setDesc(desc);

        type.setAction(AdjustmentTypeActionEnum.EITHER);
        type.setCategory(false);
        type.setLoyaltyEligible(false);

        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(contract.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }

        information.setSpid(contract.getSpid());
        String serviceGlCode = contract.getAdjustmentGLCode();
        information.setGLCode(serviceGlCode);
      //  information.setInvoiceDesc();

        int taxAuthority = contract.getTaxAuthority();
        if (taxAuthority == -1)
        {
            taxAuthority = SpidSupport.getDefaultTaxAuthority(ctx,contract.getSpid() );
        }
        information.setTaxAuthority(taxAuthority);
    
        type.setAdjustmentSpidInfo(spidInformation);

        final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        type = (AdjustmentType) home.create(ctx, type);

        return type;
    }

 


    /**
     * Stores an AdjustmentType for the given Service.
     * 
     * @param service The SubscriptionContractTerm for which to update an AdjustmentType.
     */
    private void storeAdjustmentType(Context ctx, final SubscriptionContractTerm contract) throws HomeException
    {
        if (hasAdjustmentTypeDataChanged(ctx,contract))
        {
            final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
            AdjustmentType type = HomeSupportHelper.get(ctx).findBean(ctx, AdjustmentType.class,
                    new EQ(AdjustmentTypeXInfo.CODE, Integer.valueOf((int) contract.getContractAdjustmentTypeId())));
            if (type == null)
            {
                throw new HomeException("Unable to locate AdjustmentType [" + contract.getContractAdjustmentTypeId()
                        + "]");
            }
            
            final Map spidInformation = new HashMap(type.getAdjustmentSpidInfo());
            
            final Object key = Integer.valueOf(contract.getSpid());
            AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

            if (information == null)
            {
                information = new AdjustmentInfo();
                spidInformation.put(key, information);
            }

            information.setSpid(contract.getSpid());
            String serviceGlCode = contract.getAdjustmentGLCode();
            information.setGLCode(serviceGlCode);
          //  information.setInvoiceDesc();

            int taxAuthority = contract.getTaxAuthority();
            if (taxAuthority == -1)
            {
                taxAuthority = SpidSupport.getDefaultTaxAuthority(ctx,contract.getSpid() );
            }
            information.setTaxAuthority(taxAuthority);

            type.setAdjustmentSpidInfo(spidInformation);
            
            String desc = "[contract => " + contract.getId() + " ]";
            if (contract.getName().length() < 50)
            {
                type.setName(contract.getName());
            }
            else
            {
                type.setName(desc);
            }
            type.setDesc(desc);
            
            home.store(ctx, type);
        }
    }

    private boolean hasAdjustmentTypeDataChanged(final Context ctx, final SubscriptionContractTerm newTerm) throws HomeException
    {
        final SubscriptionContractTerm oldTerm = SubscriptionContractSupport.getSubscriptionContractTerm(ctx, newTerm.getId());
        
        
        if (newTerm.getName() != oldTerm.getName() )
        {
            return true;
        }
        if(newTerm.getTaxAuthority() != oldTerm.getTaxAuthority())
        {
            return true;
        }
        if(newTerm.getAdjustmentGLCode() != newTerm.getAdjustmentGLCode())
        {
            return true;
        }
        return false;
        
    }


}
