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
import com.trilogy.framework.xhome.home.HomeChangeEvent;
import com.trilogy.framework.xhome.home.HomeChangeListener;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_billingtype;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to query DCRM for SubscriberTypeEnum information. This
 * class implements DcrmSync to allow it to be used for acquiring Lookups as
 * necessary, but does not have a Home on the BOSS side, and so does not require
 * actual synchronization.
 * 
 * @author gary.anderson@redknee.com
 */
public class DcrmSubscriberTypeEnumSupport
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_billingtype";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmSubscriberTypeEnumSync";


    /**
     * {@inheritDoc}
     */
    public void install(final Context context)
    {
        // Nothing to do -- no Home on the BOSS side.
    }


    /**
     * {@inheritDoc}
     */
    public void uninstall(final Context context)
    {
        // Nothing to do -- no Home on the BOSS side.
    }


    /**
     * {@inheritDoc}
     */
    public void updateAll(final Context context)
    {
        // Nothing to do -- no Home on the BOSS side.
    }


    /**
     * {@inheritDoc}
     */
    public String getEntityName()
    {
        return ENTITY_NAME;
    }


    /**
     * {@inheritDoc}
     */
    public Guid[] getDcrmGuids(final Context context, final BusinessEntity[] businessEntities)
    {
        final Guid[] guids = new Guid[businessEntities.length];

        for (int index = 0; index < businessEntities.length; ++index)
        {
            guids[index] = ((Rkn_billingtype)businessEntities[index]).getRkn_billingtypeid();
        }

        return guids;
    }


    /**
     * {@inheritDoc}
     */
    public void homeChange(final HomeChangeEvent evt)
    {
        // Nothing to do -- no Home on the BOSS side.
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param value The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final SubscriberTypeEnum value)
    {
        final short subscriberTypeID = value.getIndex();
        return getDcrmGuid(context, subscriberTypeID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param subscriberTypeID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final short subscriberTypeID)
    {
        /*
        The DCRM values match those of the CRM API, which differ from CRM internally.
        DCRM.PREPAID  == 0
        DCRM.POSTPAID == 1
        DCRM.HYBRID   == 2
        */
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        switch (subscriberTypeID)
        {
            case SubscriberTypeEnum.PREPAID_INDEX:
            {
                condition.getValues().addValue(0);
                break;
            }
            case SubscriberTypeEnum.POSTPAID_INDEX:
            {
                condition.getValues().addValue(1);
                break;
            }
            default:
            {
                condition.getValues().addValue(2);
                break;
            }
        }

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_billingtypeid", conditions, this);

        return primaryGuid;
    }


    /**
     * Gets the DCRM Lookup for the given value.
     *
     * @param context The operating context.
     * @param value The ID of the bean for which a Lookup is needed.
     * @return The Lookup if one exists; null otherwise.
     */
    public static Lookup getDcrmLookup(final Context context, final SubscriberTypeEnum value)
    {
        return getDcrmLookup(context, value.getIndex());
    }


    /**
     * Gets the DCRM Lookup for the bean of the given ID.
     *
     * @param context The operating context.
     * @param subscriberTypeID The ID of the bean for which a Lookup is needed.
     * @return The Lookup if one exists; null otherwise.
     */
    public static Lookup getDcrmLookup(final Context context, final short subscriberTypeID)
    {
        final Lookup spid = new Lookup();
        spid.setType(ENTITY_NAME);
    
        final DcrmServiceProviderSync spidSync =
            (DcrmServiceProviderSync)DcrmSupport.getSync(context, KEY);
    
        final Guid dcrmGuid = spidSync.getDcrmGuid(context, subscriberTypeID);
        spid.setGuid(dcrmGuid.getGuid());
        return spid;
    }

}
