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
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeChangeEvent;
import com.trilogy.framework.xhome.home.HomeChangeListener;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.NotifyingHomeItem;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_subscriptiontype;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Subscription Type information. A
 * special concern of this sync is that the local primary key is a "long", but
 * DCRM does not support "long". As a result, this sync casts to "int" and uses
 * Integer.MAX_VALUE for any identifier too large to fit in an "int" and issues
 * MAJOR logs.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmSubscriptionTypeSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_subscriptiontype";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmSubscriptionTypeSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<SubscriptionTypeHome> PIPELINE_KEY = SubscriptionTypeHome.class;


    /**
     * {@inheritDoc}
     */
    public void install(final Context context)
    {
        DcrmSyncSupport.addHomeChangeListner(context, PIPELINE_KEY, this);
    }


    /**
     * {@inheritDoc}
     */
    public void uninstall(final Context context)
    {
        DcrmSyncSupport.removeHomeChangeListner(context, PIPELINE_KEY, this);
    }


    /**
     * {@inheritDoc}
     */
    public void updateAll(final Context context)
    {
        final Visitor updateVisitor = new Visitor()
        {
            private static final long serialVersionUID = 1L;


            public void visit(final Context ctx, final Object obj)
                throws AbortVisitException
            {
                updateSubscriptionType(context, (SubscriptionType)obj);
            }
        };

        final Home home = (Home)context.get(PIPELINE_KEY);

        try
        {
            home.forEach(context, updateVisitor);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failure during update of all beans.", exception).log(context);
        }
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
            guids[index] = ((Rkn_subscriptiontype)businessEntities[index]).getRkn_subscriptiontypeid();
        }

        return guids;
    }


    /**
     * {@inheritDoc}
     */
    public void homeChange(final HomeChangeEvent evt)
    {
        final Context context = evt.getContext();
        if (!DcrmSupport.isEnabled(context))
        {
            return;
        }

        final SubscriptionType subscriptionType;

        if (evt.getSource() instanceof SubscriptionType)
        {
            subscriptionType = (SubscriptionType)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            subscriptionType = (SubscriptionType)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateSubscriptionType(context, subscriptionType);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createSubscriptionType(context, subscriptionType);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + subscriptionType, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param subscriptionType The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final SubscriptionType subscriptionType)
    {
        final long subscriptionTypeID = subscriptionType.getId();
        return getDcrmGuid(context, subscriptionTypeID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param subscriptionTypeID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final long subscriptionTypeID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Note that DCRM does not support "long". See notes in class comments.
        condition.getValues().addValue((int)subscriptionTypeID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_subscriptiontypeid", conditions, this);

        return primaryGuid;
    }


    /**
     * Gets the DCRM Lookup for the bean of the given ID.
     *
     * @param context The operating context.
     * @param subscriptionTypeID The ID of the bean for which a Lookup is needed.
     * @return The Lookup if one exists; null otherwise.
     */
    public static Lookup getDcrmLookup(final Context context, final long subscriptionTypeID)
    {
        final Lookup subscriptionType = new Lookup();
        subscriptionType.setType(ENTITY_NAME);

        final DcrmSubscriptionTypeSync subscriptionTypeSync =
            (DcrmSubscriptionTypeSync)DcrmSupport.getSync(context, KEY);

        final Guid dcrmGuid = subscriptionTypeSync.getDcrmGuid(context, subscriptionTypeID);
        subscriptionType.setGuid(dcrmGuid.getGuid());
        return subscriptionType;
    }

    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param subscriptionType The bean to create in DCRM.
     */
    private void createSubscriptionType(final Context context, final SubscriptionType subscriptionType)
    {
        final Rkn_subscriptiontype dcrmSubscriptionType = convert(context, subscriptionType);

        final Guid response = DcrmSyncSupport.create(context, dcrmSubscriptionType);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Subscription Type Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Subscription Type " + subscriptionType.getId(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param subscriptionType The bean to update in DCRM.
     */
    private void updateSubscriptionType(final Context context, final SubscriptionType subscriptionType)
    {
        final Rkn_subscriptiontype dcrmSubscriptionType = convert(context, subscriptionType);
        final Guid dcrmGuid = getDcrmGuid(context, subscriptionType);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmSubscriptionType.setRkn_subscriptiontypeid(key);

            DcrmSyncSupport.update(context, dcrmSubscriptionType);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Subscription Type: " + subscriptionType.getId(),
                null).log(context);
            createSubscriptionType(context, subscriptionType);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param subscriptionType The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_subscriptiontype convert(final Context context, final SubscriptionType subscriptionType)
    {
        final Rkn_subscriptiontype dcrmSubscriptionType = new Rkn_subscriptiontype();

        // Set the primary key.
        {
            final CrmNumber identifier = new CrmNumber();
            // Note that DCRM does not support "long". See notes in class
            // comments.
            identifier.set_int((int)subscriptionType.getId());
            dcrmSubscriptionType.setRkn_identifier(Long.toString(subscriptionType.getId()));
        }

        // Set the name.
        {
            dcrmSubscriptionType.setRkn_name(subscriptionType.getName());
        }

        // Set the description.
        {
            dcrmSubscriptionType.setRkn_description(subscriptionType.getDescription());
        }

        // Set the SPID.
        {
            //final Lookup spid = DcrmServiceProviderSync.getDcrmLookup(context, subscriptionType.getSpid());
            //dcrmSubscriptionType.setRkn_serviceproviderid(spid);
        }

        return dcrmSubscriptionType;
    }
}
