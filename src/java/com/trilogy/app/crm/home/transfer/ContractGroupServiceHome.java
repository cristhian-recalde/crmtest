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
package com.trilogy.app.crm.home.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.TfaRmiConfig;
import com.trilogy.app.crm.transfer.ContractGroup;
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.app.crm.transfer.contract.ContractGroupNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferContractGroupFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractOwnerNotFoundException;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Decorator that provisions the contract group or owner in the transfer logic.
 * @author arturo.medina@redknee.com
 *
 */
public class ContractGroupServiceHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 7863703133425437919L;

    /**
     * Creates a new <code>ContractGroupServiceHome</code>.
     *
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public ContractGroupServiceHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException,
            HomeInternalException
    {
        if (obj instanceof ContractGroup)
        {
            ContractGroup contract = adaptPublicGroup(ctx, (ContractGroup)obj);            
            
            final TransferContractGroupFacade service =
                (TransferContractGroupFacade) ctx.get(TransferContractGroupFacade.class);
            try
            {
                contract = service.createContractGroup(ctx, contract);
                return super.create(ctx, contract);
            }
            catch (TransferContractException e)
            {
                LogSupport.major(ctx, this, "TransferContractException while creating the contract ", e);
                throw new HomeException(e);
            }
        }
        return super.create(ctx, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException,
            HomeInternalException
    {
        super.remove(ctx, obj);
        if (obj instanceof ContractGroup)
        {
            final ContractGroup contract = adaptPublicGroup(ctx, (ContractGroup)obj);           
            
            final TransferContractGroupFacade service =
                (TransferContractGroupFacade) ctx.get(TransferContractGroupFacade.class);
            try
            {
                service.deleteContractGroup(ctx, contract.getIdentifier());
            }
            catch (TransferContractException e)
            {
                LogSupport.major(ctx, this, "TransferContractException while deleting the contract group", e);
                throw new HomeException(e);
            }
            catch (ContractGroupNotFoundException e)
            {
                LogSupport.major(ctx, this, "ContractGroupNotFoundException while deleting the contract group ", e);
                throw new HomeException(e);
            }
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object find(Context ctx, Object key) throws HomeException,
            HomeInternalException
    {
        if (key instanceof Long)
        {
            return doFind(ctx, ((Long)key).longValue());
        }
        
        Collection col=select(ctx,key);
        if(col!=null && col.size()>0)
        {
            return col.iterator().next();
        }

        return unadaptPublicGroup(ctx, (ContractGroup) super.find(ctx, key));        
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection select(Context ctx, Object where) throws HomeException,
            HomeInternalException
    {
        Collection result = new ArrayList(); 

        Long id = getContractGroupSearchId(ctx, where);
        
        if (id != null)
        {
            ContractGroup group = doFind(ctx, id);
            if (group != null)
            {
                result.add(group);
            }
            return result;
        }
        
        String ownerId = getContractGroupOwnerId(ctx, where);
        
        if (ownerId != null && !ownerId.equals(""))
        {
            result = getContractsByOwner(ctx, ownerId);
        }
        else
        {
            result.addAll(selectAllContractsByOwners(ctx));
        }
        
        return result;
    }

    /**
     * Returns a collection of contract groups by the specified list of owners
     * Since the RMI interface does not support a selectAll function, the work around is to do a 
     * select by owner for every owner in a list of owners as an alternative
     * @param ctx the operating context
     * @return the collection of contract groups
     * @throws HomeException
     */
    private Collection selectAllContractsByOwners(Context ctx) throws HomeException
    {
        Collection<ContractGroup> result = new ArrayList<ContractGroup>();
        if (ctx.has(TransferContractSupport.TRANSFER_CONTRACT_GROUP_ID))
        {
            Set groupOwners = (Set<String>) ctx.get(TransferContractSupport.TRANSFER_CONTRACT_GROUP_ID);
            Iterator<String> iter = groupOwners.iterator();
            while (iter.hasNext())
            {
                String owner = iter.next();
                // map default blank string to configured public group owner name
                if (owner.equals(""))
                {
                    try
                    {
                        owner = TransferContractSupport.getPublicGroupOwnerName(ctx);
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Public Group Owner name not found", e).log(ctx);
                        continue;
                    }
                }
                result.addAll(getContractsByOwner(ctx, owner));                
            }
        }        
        return result;
    }

    private ContractGroup adaptPublicGroup(Context ctx, ContractGroup contract) throws HomeException
    {
        if (contract == null)
        {
            return null;
        }
        if ("".equals(contract.getOwnerID()) || contract.getPrivacy() == GroupPrivacyEnum.PUBLIC)
        {
            contract.setOwnerID(TransferContractSupport.getPublicGroupOwnerName(ctx));
        }
        return contract;
    }
    
    private ContractGroup unadaptPublicGroup(Context ctx, ContractGroup contract) throws HomeException
    {
        if (contract == null)
        {
            return null;
        }
        if (!contract.getOwnerID().equals(TransferContractSupport.getPublicGroupOwnerName(ctx)))
        {
            contract.setPrivacy(GroupPrivacyEnum.PRIVATE);
        }
        else
        {
            contract.setPrivacy(GroupPrivacyEnum.PUBLIC);
            contract.setOwnerID("");
        }
        return contract;
    }
    
    /**
     * Returns a collection of transfer contract by the specified owner
     * @param ctx the operating context
     * @param ownerId the owner ID to search for
     * @return the collection of contracts
     * @throws HomeException 
     */
    private Collection getContractsByOwner(Context ctx, String ownerId) throws HomeException   
    {
        Collection<ContractGroup> contracts = new ArrayList<ContractGroup>();
        final TransferContractGroupFacade service = 
            (TransferContractGroupFacade) ctx.get(TransferContractGroupFacade.class);
        try
        {
            contracts = service.retrieveContractGroupByOwner(ctx, ownerId);
	    for (ContractGroup group : contracts)
            {
                unadaptPublicGroup(ctx,group);
            }
	    
        }
        catch (TransferContractException e)
        {
            LogSupport.major(ctx, this, "TransferContractException while retrieving the contract ", e);
        }
        catch (TransferContractOwnerNotFoundException e)
        {
            LogSupport.major(ctx, this, "TransferContractNotFoundException while retrieving the contract ", e);
        }
        return contracts;
    }


    /**
     * Returns a transfer contract by ID
     * @param ctx
     * @param id
     * @return
     * @throws HomeException
     */
    private ContractGroup doFind(Context ctx, Long id) throws HomeException
    {
        ContractGroup contract = null;
        final TransferContractGroupFacade service = 
            (TransferContractGroupFacade) ctx.get(TransferContractGroupFacade.class);
        try
        {
            contract = service.retrieveContractGroup(ctx, id.longValue());
        }
        catch (TransferContractException e)
        {
            LogSupport.major(ctx, this, "TransferContractException while retrieving the contract ", e);
        }
        catch (ContractGroupNotFoundException e)
        {
            LogSupport.major(ctx, this, "TransferContractNotFoundException while retrieving the contract ", e);
        }
        return unadaptPublicGroup(ctx, contract);
    }


    /**
     * Returns the contract ID from the where clause 
     * @param ctx
     * @param where
     * @return
     */
    private Long getContractGroupSearchId(Context ctx, Object where)
    {
        if (where instanceof EQ)
        {
            final EQ eq = (EQ) where;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equalsIgnoreCase("identifier"))
            {
                return (Long) eq.getArg2();
            }
            else if (eq.getArg1() instanceof Long)
            {
                return (Long) eq.getArg1();
            }
        }

        if (where instanceof Context)
        {
            new MinorLogMsg(this, "Unexpected context " + where, null).log(ctx);
            return null;
        }
        return (Long) ctx.get(TransferContractSupport.TRANSFER_CONTRACT_PRIVATE_GROUP_ID);
    }
    

    /**
     * Returns the owner ID from the where clause 
     * @param ctx
     * @param where
     * @return
     */
    private String getContractGroupOwnerId(Context ctx, Object where)
    {
        if (where instanceof EQ)
        {
            final EQ eq = (EQ) where;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equalsIgnoreCase("ownerID"))
            {
                String ownerID = (String)eq.getArg2();
                if (ownerID != null && ownerID.trim().length() == 0)
                {
                    final TfaRmiConfig config = (TfaRmiConfig)ctx.get(TfaRmiConfig.class);
                    ownerID = config.getPublicOwnerName();
                }

                return ownerID;
            }
            else if (eq.getArg1() instanceof String)
            {
                return (String) eq.getArg1();
            }
        }
        if (where instanceof Context)
        {
            new MinorLogMsg(this, "Unexpected context " + where, null).log(ctx);
        }
        return null;
    }

}
