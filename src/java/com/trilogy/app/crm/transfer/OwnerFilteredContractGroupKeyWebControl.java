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
package com.trilogy.app.crm.transfer;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;


/**
 * Provides a custom key-webcontrol for ContractGroup that is filtered based on
 * an owner.
 *
 * @author gary.anderson@redknee.com
 */
public class OwnerFilteredContractGroupKeyWebControl
    extends ContractGroupKeyWebControl
{
    /**
     * Provides an interface for determining the "owner" used to search for
     * ContractGroup.
     *
     * @author gary.anderson@redknee.com
     */
    public interface OwnerExtractor
    {
        /**
         * Gets the "owner" used to filter the list of available ContractGroup.
         *
         * @param context The operating context.
         * @return The "owner" used to filter. May be blank but must never be
         * null.
         */
        String getOwner(Context context);
    }


    /**
     * Creates a new KeyWebControl for ContractGroup that is filtered according
     * to the "owner" provided by the given extractor.
     *
     * @param extractor An extractor for obtaining an "owner".
     */
    public OwnerFilteredContractGroupKeyWebControl(final OwnerExtractor extractor)
    {
        extractor_ = extractor;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context context, final PrintWriter out, final String name, final Object obj)
    {
        // This method is overridden to invoke AbstractWebControl.wrapContext
        // because the AbstractKeyWebControl does not (although it should).
        super.toWeb(wrapContext(context), out, name, obj);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context wrapContext(final Context parentContext)
    {
        final Context context = super.wrapContext(parentContext.createSubContext());

        final String owner = extractor_.getOwner(context);
        final Set<String> allAccountIds = new HashSet<String>();
        allAccountIds.add(owner);
        context.put(TransferContractSupport.TRANSFER_CONTRACT_GROUP_ID, allAccountIds);
    
        return context;
    }


    /**
     * The extractor used to get the "owner" value for filtering.
     */
    private final OwnerExtractor extractor_;
}
