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
package com.trilogy.app.crm.client.dcrm.entitysync;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.technology.TechnologyEnum;

import com.trilogy.dynamics.crm.crmservice._2006.webservices.Picklist;

/**
 * Provides support for integrating with the DCRM Picklist version of the
 * TechnologyEnum.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmTechnologyPicklistSupport
{
    /**
     * Creates a DCRM Picklist value corresponding to the given enumeration
     * value.
     *
     * @param context The operating context.
     * @param value The enumeration value for which to get a corresponding
     * Picklist value.
     * @return The DCRM Picklist value corresponding to the given enumeration
     * value.
     */
    public static Picklist getPicklist(final Context context, final TechnologyEnum value)
    {
        return getPicklist(context, value.getIndex());
    }

    
    /**
     * Creates a DCRM Picklist value corresponding to the given enumeration
     * value.
     *
     * @param context The operating context.
     * @param value The enumeration value for which to get a corresponding
     * Picklist value.
     * @return The DCRM Picklist value corresponding to the given enumeration
     * value.
     */
    public static Picklist getPicklist(final Context context, final short value)
    {
        final Picklist picklistvalue = new Picklist();

        /*
        DCRM.GSM  == 0 == BOSS.GSM
        DCRM.TDMA == 1 == BOSS.TDMA
        DCRM.CDMA == 2 == BOSS.CDMA
        DCRM.ANY  == 3 == BOSS.ANY
        ????      == 4 == BOSS.VSAT
        ????      == 31000 == BOSS.NO_TECHNOLOGY
        */

        switch (value)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            {
                picklistvalue.set_int(value);
                break;
            }
            default:
            {
                throw new IllegalStateException("Unsupported TechnologyEnum value: " + value);
            }
        }

        return picklistvalue;
    }
}
