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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.transfer.ParticipantTypeEnum;
import com.trilogy.app.crm.transfer.TransferContract;
import com.trilogy.app.crm.transfer.TransferType;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferTypeFacade;


/**
 * Decorator that provisions the transfer type in the transfer logic.
 *
 * @author arturo.medina@redknee.com
 */
public class TransferTypeServiceHome
    extends HomeProxy
{
    /**
     * Creates a new <code>TransferTypeServiceHome</code>.
     *
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public TransferTypeServiceHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object find(final Context ctx, final Object key)
        throws HomeException
    {
        if (key instanceof Long)
        {
            final Long identifier = (Long)key;
            final Collection<TransferType> types = select(ctx, key);
            return doFind(ctx, identifier, types);
        }

        return super.find(ctx, key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TransferType> select(final Context ctx, final Object where)
        throws HomeException
    {
        Collection<TransferType> result = new ArrayList<TransferType>();
        result = getTransferTypes(ctx, getSpid(ctx));

        return result;
    }


    /**
     * Attempts to determine what service provider is relevant for the search.
     *
     * @param context The operating context.
     * @return A relevant SPID if one is found; 0 otherwise.
     */
    private int getSpid(final Context context)
    {
        // TODO: SPID within transfer types makes no sense, particularly because
        // CRM+ may not know the
        final int spid;

        final Object bean = context.get(AbstractWebControl.BEAN);
        if (bean != null && bean instanceof TransferContract)
        {
            final TransferContract contract = (TransferContract)bean;

            if (contract.getParticipantType() == ParticipantTypeEnum.CONTRACT_GROUP)
            {
                spid = contract.getSpid();
            }
            else
            {
                spid = contract.getContributingSpid();
            }
        }
        else
        {
            // Nothing else but to use the SPID of the agent.
            final User user = (User)context.get(Principal.class);
            if (user != null)
            {
                spid = user.getSpid();
            }
            else
            {
                // ???
                spid = 0;
            }
        }

        return spid;
    }


    /**
     * Finds a Transfer Type
     *
     * @param ctx The operating context.
     * @param identifier The identifier of the TransferType.
     * @param types The list of TransferTypes through which to search.
     * @return A TransferType with the given identifier if found; null otherwise.
     */
    private Object doFind(final Context ctx, final long identifier, final Collection<TransferType> types)
    {
        TransferType found = null;
        for (final TransferType type : types)
        {
            if (identifier == type.getIdentifier())
            {
                found = type;
            }
        }

        return found;
    }


    /**
     * Gets a list of available TransferTypes for the given service provider from TFA.
     *
     * @param ctx The operating context.
     * @param spid The service provider ID for which to query available TransferTypes.
     * @return A list of available TransferTypes.
     * @throws HomeException Thrown if there are problems trying to access the TransferType data.
     */
    private Collection<TransferType> getTransferTypes(final Context ctx, final int spid)
        throws HomeException
    {
        Collection<TransferType> contracts = new ArrayList<TransferType>();
        final TransferTypeFacade service = (TransferTypeFacade)ctx.get(TransferTypeFacade.class);
        try
        {
            contracts = service.retrieveTransferType(ctx, spid);
        }
        catch (final TransferContractException e)
        {
            LogSupport.major(ctx, this, "TransferContractException while retrieving the contract ", e);
            throw new HomeException(e);
        }
        return contracts;
    }


    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
