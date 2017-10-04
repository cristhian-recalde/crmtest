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
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.app.crm.transfer.TransferContract;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferContractFacade;
import com.trilogy.app.crm.transfer.contract.TransferContractNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractOwnerNotFoundException;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Decorator that provisions the transfer contract in the transfer logic.
 * @author arturo.medina@redknee.com
 *
 */
public class TransferContractServiceHome extends HomeProxy
{

    /**
     * Creates a new <code>TransferContractServiceHome</code>.
     *
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public TransferContractServiceHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }
    
    /**
     * the serial ID
     */
    private static final long serialVersionUID = -4326895319589312998L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException,
            HomeInternalException
    {
        if (obj instanceof TransferContract)
        {
            TransferContract contract = (TransferContract)obj;
            adaptPublicContract(ctx, contract);
            final TransferContractFacade service = (TransferContractFacade) ctx.get(TransferContractFacade.class);
            try
            {
                contract = service.createTransferContract(ctx, contract);
                unadaptPublicContract(ctx, contract);
                return super.create(ctx, contract);
            }
            catch (TransferContractException e)
            {
                unadaptPublicContract(ctx, contract);
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
        if (obj instanceof TransferContract)
        {
            final TransferContract contract = (TransferContract)obj;
            final TransferContractFacade service = (TransferContractFacade) ctx.get(TransferContractFacade.class);
            try
            {
                service.deleteTransferContract(ctx, contract.getIdentifier());
            }
            catch (TransferContractException e)
            {
                LogSupport.major(ctx, this, "TransferContractException while deleting the contract ", e);
                throw new HomeException(e);
            }
            catch (TransferContractNotFoundException e)
            {
                LogSupport.major(ctx, this, "TransferContractNotFoundException while deleting the contract ", e);
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

        return super.find(ctx, key);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection select(Context ctx, Object where) throws HomeException,
            HomeInternalException
    {
        Collection result = new ArrayList(); 
        Long id = getTransferContractSearchId(ctx, where);
        
        if (id != null)
        {
            TransferContract contract = doFind(ctx, id); 
            if (contract != null)
            {
                result.add(contract);
            }
            return result;
        }
        
        String ownerId = getTransferContractOwnerId(ctx, where);
        
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
     * Returns a collection of transfer contracts by the specified list of owners
     * Since the RMI interface does not support a selectAll function, the work around is to do a 
     * select by owner for every owner in a list of owners as an alternative
     * @param ctx the operating context
     * @return the collection of transfer contracts
     * @throws HomeException
     */
    private Collection selectAllContractsByOwners(Context ctx) throws HomeException
    {
        Collection<TransferContract> result = new ArrayList<TransferContract>();
        if (ctx.has(TransferContractSupport.TRANSFER_CONTRACT_OWNER_ID))
        {
            Set groupOwners = (Set<String>) ctx.get(TransferContractSupport.TRANSFER_CONTRACT_OWNER_ID);
            Iterator<String> iter = groupOwners.iterator();
            while (iter.hasNext())
            {
                String owner = iter.next();
                result.addAll(getContractsByOwner(ctx, owner));
            }
        }        
        return result;
    }
    private void adaptPublicContract(Context ctx, TransferContract contract) throws HomeException
    {
        if (contract == null)
        {
            return;
        }
        if (contract.getOwnerID().equals("") || contract.getPrivacy() == GroupPrivacyEnum.PUBLIC)
        {
            contract.setOwnerID(TransferContractSupport.getPublicGroupOwnerName(ctx));
        }
    }
    
    private void unadaptPublicContract(Context ctx, TransferContract contract) throws HomeException
    {
        if (contract == null)
        {
            return;
        }
        if (contract.getOwnerID().equals(TransferContractSupport.getPublicGroupOwnerName(ctx)))
        {
            contract.setPrivacy(GroupPrivacyEnum.PUBLIC);
            contract.setOwnerID("");
        }
        else
        {
            contract.setPrivacy(GroupPrivacyEnum.PRIVATE);
        }
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
        Collection<TransferContract> contracts = new ArrayList<TransferContract>();
        final TransferContractFacade service = 
            (TransferContractFacade) ctx.get(TransferContractFacade.class);
        try
        {
            contracts = service.retrieveTransferContractByOwner(ctx, ownerId);
            for (final TransferContract contract : contracts)
            {
                unadaptPublicContract(ctx, contract);
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
    private TransferContract doFind(Context ctx, Long id) throws HomeException
    {
        TransferContract contract = null;
        final TransferContractFacade service = 
            (TransferContractFacade) ctx.get(TransferContractFacade.class);
        try
        {
            contract = service.retrieveTransferContract(ctx, id);
            unadaptPublicContract(ctx, contract);
        }
        catch (TransferContractException e)
        {
            LogSupport.major(ctx, this, "TransferContractException while retrieving the contract ", e);            
        }
        catch (TransferContractNotFoundException e)
        {
            LogSupport.major(ctx, this, "TransferContractNotFoundException while retrieving the contract ", e);
        }
        return contract;
    }


    /**
     * Returns the contract ID from the where clause 
     * @param ctx
     * @param where
     * @return
     */
    private Long getTransferContractSearchId(Context ctx, Object where)
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
            new MinorLogMsg(this,"Unexpected context " + where , null).log(ctx);
            return null;
        }

        return null;
    }
    

    /**
     * Returns the owner ID from the where clause 
     * @param ctx
     * @param where
     * @return
     */
    private String getTransferContractOwnerId(Context ctx, Object where)
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
            new MinorLogMsg(this,"Unexpected context " + where , null).log(ctx);
            return null;

        }

        return null;
    }

    
}
