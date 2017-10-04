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
package com.trilogy.app.crm.client.dcrm;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.client.dcrm.entitysync.DcrmAccountRoleSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmAccountTypeSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmActivationReasonCodeSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmBankSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmBillCycleSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmCreditCategorySync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmDealerCodeSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmDiscountClassSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmIdentificationSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmMsisdnGroupSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmOccupationSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmPaymentPlanSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmPricePlanSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmProvinceSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmServiceProviderSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmSubscriptionClassSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmSubscriptionTypeSync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmTaxAuthoritySync;
import com.trilogy.app.crm.client.dcrm.entitysync.DcrmTransactionMethodSync;


/**
 * Provides control over the propagation of data changes from Redknee's CRM to
 * Microsoft Dynamic CRM.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmChangePropagation
{
    /**
     * Creates a new instance of the propagation support.
     *
     * @param context The operating context.
     */
    public DcrmChangePropagation(final Context context)
    {
        installedSyncs_ = new HashMap<String, DcrmSync>();
    }


    /**
     * Adds the default synchronization supports to those participating in
     * change propagation. It is expected that this is called after
     * instantiation of this propagation support to initialize the default set.
     *
     * @param context The operating context.
     */
    public void addDefaultSyncs(final Context context)
    {
        addSync(context, DcrmServiceProviderSync.KEY, new DcrmServiceProviderSync());
        addSync(context, DcrmAccountTypeSync.KEY, new DcrmAccountTypeSync());
        addSync(context, DcrmBillCycleSync.KEY, new DcrmBillCycleSync());
        addSync(context, DcrmIdentificationSync.KEY, new DcrmIdentificationSync());
        addSync(context, DcrmAccountRoleSync.KEY, new DcrmAccountRoleSync());
        addSync(context, DcrmActivationReasonCodeSync.KEY, new DcrmActivationReasonCodeSync());
        addSync(context, DcrmDealerCodeSync.KEY, new DcrmDealerCodeSync());
        addSync(context, DcrmOccupationSync.KEY, new DcrmOccupationSync());
        addSync(context, DcrmCreditCategorySync.KEY, new DcrmCreditCategorySync());
        addSync(context, DcrmDiscountClassSync.KEY, new DcrmDiscountClassSync());
        addSync(context, DcrmTaxAuthoritySync.KEY, new DcrmTaxAuthoritySync());
        addSync(context, DcrmPaymentPlanSync.KEY, new DcrmPaymentPlanSync());
        addSync(context, DcrmBankSync.KEY, new DcrmBankSync());
        addSync(context, DcrmMsisdnGroupSync.KEY, new DcrmMsisdnGroupSync());
        addSync(context, DcrmSubscriptionTypeSync.KEY, new DcrmSubscriptionTypeSync());
        addSync(context, DcrmSubscriptionClassSync.KEY, new DcrmSubscriptionClassSync());
        addSync(context, DcrmTransactionMethodSync.KEY, new DcrmTransactionMethodSync());
        addSync(context, DcrmPricePlanSync.KEY, new DcrmPricePlanSync());
        addSync(context, DcrmProvinceSync.KEY, new DcrmProvinceSync());
    }


    /**
     * Adds a single synchronization support to those participating in change
     * propagation. That support will have its {@link DcrmSync#install} method
     * called as a result.
     *
     * @param context The operating context.
     * @param key The key by which this support can be retrieved or updated.
     * @param sync The synchronization support to add.
     */
    public final void addSync(final Context context, final String key, final DcrmSync sync)
    {
        if (!installedSyncs_.containsKey(key))
        {
            installedSyncs_.put(key, sync);
            sync.install(context);
        }
        else
        {
            throw new IllegalStateException(
                "A DcrmSync is already installed with that key.  Use the replaceSync() method to update or choose another key.");
        }
    }


    /**
     * Replaces an existing synchronization support instance with a new one. A
     * support must have already been added with the given key.
     *
     * @param context The operating context.
     * @param key The key used when the support was originally added.
     * @param sync The synchronization support used to replace an old one.
     * @return The old synchronization support that was replaced.
     * @exception IllegalStateException Thrown if no support is found for the
     * given key.
     */
    public DcrmSync replaceSync(final Context context, final String key, final DcrmSync sync)
    {
        final DcrmSync oldSync = installedSyncs_.get(key);

        if (installedSyncs_.containsKey(key))
        {
            oldSync.uninstall(context);
            installedSyncs_.put(key, sync);
            sync.install(context);
        }
        else
        {
            throw new IllegalStateException(
                "No DcrmSync is installed with that key.  Use the addSync() method to update or choose another key.");
        }

        return oldSync;
    }


    /**
     * Removes an existing synchronization support instance. A support must have
     * already been added with the given key.
     *
     * @param context The operating context.
     * @param key The key used when the support was originally added.
     * @return The old synchronization support that was removed.
     * @exception IllegalStateException Thrown if no support is found for the
     * given key.
     */
    public DcrmSync removeSync(final Context context, final String key)
    {
        final DcrmSync sync;
        if (installedSyncs_.containsKey(key))
        {
            sync = installedSyncs_.remove(key);
            sync.uninstall(context);
        }
        else
        {
            throw new IllegalStateException("No DcrmSync is installed with that key.");
        }

        return sync;
    }


    /**
     * Gets an existing synchronization support instance. A support must have
     * already been added with the given key.
     *
     * @param key The key used when the support was originally added.
     * @return The synchronization support that was added with the given key.
     * @exception IllegalStateException Thrown if no support is found for the
     * given key.
     */
    public DcrmSync getSync(final String key)
    {
        if (installedSyncs_.containsKey(key))
        {
            return installedSyncs_.get(key);
        }

        throw new IllegalStateException("No DcrmSync is installed with that key.");
    }


    /**
     * Initiates a bulk update to Microsoft Dynamics CRM of all beans accessible
     * to all installed synchronization supports. This is intended for
     * initializing a new deployment of Microsoft Dynamics CRM. To bulk update
     * only those bean for a specific synchronization support, use the
     * {@link #getSync} method to get the support, then call its
     * {@link DcrmSync#updateAll} method.
     *
     * @param context The operating context.
     */
    public void updateAll(final Context context)
    {
        for (final DcrmSync sync : installedSyncs_.values())
        {
            sync.updateAll(context);
        }
    }


    /**
     * The map of currently installed synchronization supports.
     */
    private final Map<String, DcrmSync> installedSyncs_;
}
