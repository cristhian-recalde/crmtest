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

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides an interface for a synchronization support that manages the
 * propagation of updates from the local system to the Microsoft Dynamics CRM
 * for a specific entity.
 *
 * @author gary.anderson@redknee.com
 */
public interface DcrmSync
{

    /**
     * Installs the home listener.
     *
     * @param context The operating context.
     */
    void install(Context context);


    /**
     * Uninstalls the home listener.
     *
     * @param context The operating context.
     */
    void uninstall(Context context);


    /**
     * Copies all beans to the remote machine, creating new instances as
     * necessary.
     *
     * @param context The operating context.
     */
    void updateAll(Context context);


    /**
     * Gets the name of the DCRM entity to which the DcrmSync belongs.
     *
     * @return The name of the DCRM entity to which the DcrmSync belongs.
     */
    String getEntityName();


    /**
     * Collects the GUIDs from the given Business Entities.
     *
     * @param context The operating context.
     * @param businessEntities The entities from which to pull GUIDs.
     * @return The GUIDs (keys) of the given entities.
     */
    Guid[] getDcrmGuids(Context context, BusinessEntity[] businessEntities);

}
