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
import com.trilogy.app.crm.transfer.TransferAgreement;
import com.trilogy.app.crm.transfer.contract.TransferAgreementFacade;
import com.trilogy.app.crm.transfer.contract.TransferAgreementNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
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
 * Decorator that provisions the transfer agreememtn in the transfer logic.
 * @author arturo.medina@redknee.com
 *
 */
public class TransferAgreementServiceHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = -70779176938250497L;

    /**
     * Creates a new <code>TransferAgreementServiceHome</code>.
     *
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public TransferAgreementServiceHome(final Context context, final Home delegate)
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
        if (obj instanceof TransferAgreement)
        {
            TransferAgreement agreement = (TransferAgreement)obj;
            adaptPublicAgreement(ctx, agreement);
            final TransferAgreementFacade service = (TransferAgreementFacade) ctx.get(TransferAgreementFacade.class);
            try
            {
                agreement = service.createTransferAgreement(ctx, agreement);
                unadaptPublicAgreement(ctx, agreement);

                return super.create(ctx, agreement);
            }
            catch (TransferContractException e)
            {
                unadaptPublicAgreement(ctx, agreement);
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
    public Object store(final Context ctx, final Object obj) throws HomeException,
            HomeInternalException
    {
        if (obj instanceof TransferAgreement)
        {
            TransferAgreement agreement = (TransferAgreement)obj;
            adaptPublicAgreement(ctx, agreement);
            final TransferAgreementFacade service = (TransferAgreementFacade) ctx.get(TransferAgreementFacade.class);
            try
            {
                agreement = service.updateTransferContract(ctx, agreement);
                unadaptPublicAgreement(ctx, agreement);

                return super.store(ctx, agreement);
            }
            catch (TransferContractException e)
            {
                unadaptPublicAgreement(ctx, agreement);
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
        if (obj instanceof TransferAgreement)
        {
            final TransferAgreement contract = (TransferAgreement)obj;
            final TransferAgreementFacade service = (TransferAgreementFacade) ctx.get(TransferAgreementFacade.class);
            try
            {
                service.deleteAgreement(ctx, contract.getIdentifier());
            }
            catch (TransferContractException e)
            {
                LogSupport.major(ctx, this, "TransferContractException while deleting the contract ", e);
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
            final TransferAgreement result = (TransferAgreement)col.iterator().next();
            return result;
        }

        return (TransferAgreement) super.find(ctx, key);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection select(Context ctx, Object where) throws HomeException,
            HomeInternalException
    {
        Collection result = new ArrayList(); 
        Long id = getTransferAgreementSearchId(ctx, where);
        
        if (id != null)
        {
            TransferAgreement agreement = doFind(ctx, id);
            if (agreement != null)
            {
                result.add(agreement);
            }
            return result;
        }
        
        String ownerId = getTransferAgreementOwnerId(ctx, where);
        
        if (ownerId != null && !ownerId.equals(""))
        {
            result = getAgreementsByOwner(ctx, ownerId);
        }
        else
        {
            result.addAll(selectAllAgreementsByOwners(ctx));
        }
        
        return result;
    }

    /**
     * Returns a collection of agreements by the specified list of owners
     * Since the RMI interface does not support a selectAll function, the work around is to do a 
     * select by owner for every owner in a list of owners as an alternative
     * @param ctx the operating context
     * @return the collection of agreements
     * @throws HomeException
     */
    private Collection selectAllAgreementsByOwners(Context ctx) throws HomeException
    {
        Collection<TransferAgreement> result = new ArrayList<TransferAgreement>();
        if (ctx.has(TransferContractSupport.TRANSFER_AGREEMENT_OWNER_ID))
        {
            Set groupOwners = (Set<String>) ctx.get(TransferContractSupport.TRANSFER_AGREEMENT_OWNER_ID);
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
                result.addAll(getAgreementsByOwner(ctx, owner));               
            }
        }        
        return result;
    }
    private void adaptPublicAgreement(Context ctx, TransferAgreement contract) throws HomeException
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
    
    private void unadaptPublicAgreement(Context ctx, TransferAgreement contract) throws HomeException
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
    private Collection getAgreementsByOwner(Context ctx, String ownerId) throws HomeException
    {
        Collection<TransferAgreement> agreements = new ArrayList<TransferAgreement>();
        final TransferAgreementFacade service = 
            (TransferAgreementFacade) ctx.get(TransferAgreementFacade.class);
        try
        {
            agreements = service.retrieveOwnerAgreements(ctx, ownerId);
            for (final TransferAgreement agreement : agreements)
            {
                unadaptPublicAgreement(ctx, agreement);
            }
        }
        catch (TransferContractException e)
        {
            LogSupport.major(ctx, this, "TransferContractException while retrieving the contract ", e);
        }
        return agreements;
    }


    /**
     * Returns a transfer contract by ID
     * @param ctx
     * @param id
     * @return
     * @throws HomeException
     */
    private TransferAgreement doFind(Context ctx, Long id) throws HomeException
    {
        TransferAgreement contract = null;
        final TransferAgreementFacade service = 
            (TransferAgreementFacade) ctx.get(TransferAgreementFacade.class);
        try
        {
            contract = service.retrieveAgreement(ctx, id);
            unadaptPublicAgreement(ctx, contract);
        }
        catch (TransferContractException e)
        {
            LogSupport.major(ctx, this, "TransferContractException while retrieving the contract ", e);
        }
        catch (TransferAgreementNotFoundException e)
        {
            LogSupport.major(ctx, this, "TransferAgreementNotFoundException while retrieving the contract ", e);
        }

        return contract;
    }


    /**
     * Returns the contract ID from the where clause 
     * @param ctx
     * @param where
     * @return
     */
    private Long getTransferAgreementSearchId(Context ctx, Object where)
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
        return null;
    }
    

    /**
     * Returns the owner ID from the where clause 
     * @param ctx
     * @param where
     * @return
     */
    private String getTransferAgreementOwnerId(Context ctx, Object where)
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
            return null;
        }

        return null;
    }

    
    
}
