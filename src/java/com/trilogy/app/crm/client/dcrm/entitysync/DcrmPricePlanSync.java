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

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Picklist;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_priceplan;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Price Plan information. A special
 * concern of this sync is that the local primary key is a "long", but DCRM does
 * not support "long". As a result, this sync casts to "int" and uses
 * Integer.MAX_VALUE for any identifier too large to fit in an "int" and issues
 * MAJOR logs.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmPricePlanSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_priceplan";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmPricePlanSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<PricePlanHome> PIPELINE_KEY = PricePlanHome.class;


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
                updatePricePlan(context, (PricePlan)obj);
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
            guids[index] = ((Rkn_priceplan)businessEntities[index]).getRkn_priceplanid();
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

        final PricePlan pricePlan;

        if (evt.getSource() instanceof PricePlan)
        {
            pricePlan = (PricePlan)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            pricePlan = (PricePlan)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updatePricePlan(context, pricePlan);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createPricePlan(context, pricePlan);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + pricePlan, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param pricePlan The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final PricePlan pricePlan)
    {
        final long pricePlanID = pricePlan.getIdentifier();
        return getDcrmGuid(context, pricePlanID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param pricePlanID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final long pricePlanID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Note that DCRM does not support "long". See notes in class comments.
        condition.getValues().addValue((int)pricePlanID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_priceplanid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param pricePlan The bean to create in DCRM.
     */
    private void createPricePlan(final Context context, final PricePlan pricePlan)
    {
        final Rkn_priceplan dcrmPricePlan = convert(context, pricePlan);

        final Guid response = DcrmSyncSupport.create(context, dcrmPricePlan);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Price Plan Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Price Plan " + pricePlan.getIdentifier(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param pricePlan The bean to update in DCRM.
     */
    private void updatePricePlan(final Context context, final PricePlan pricePlan)
    {
        final Rkn_priceplan dcrmPricePlan = convert(context, pricePlan);
        final Guid dcrmGuid = getDcrmGuid(context, pricePlan);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmPricePlan.setRkn_priceplanid(key);

            DcrmSyncSupport.update(context, dcrmPricePlan);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Price Plan: " + pricePlan.getIdentifier(), null).log(context);
            createPricePlan(context, pricePlan);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param pricePlan The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_priceplan convert(final Context context, final PricePlan pricePlan)
    {
        final Rkn_priceplan dcrmPricePlan = new Rkn_priceplan();

        // Set the primary key.
        {
            final CrmNumber identifier = new CrmNumber();
            // Note that DCRM does not support "long". See notes in class comments.
            identifier.set_int((int)pricePlan.getIdentifier());
            dcrmPricePlan.setRkn_identifier(Long.toString(pricePlan.getId()));
        }

        // Set the name.
        {
            dcrmPricePlan.setRkn_name(pricePlan.getName());
        }

        // Set the type (prepaid/postpaid).
        {
            Lookup param = DcrmSubscriberTypeEnumSupport.getDcrmLookup(context, pricePlan.getPricePlanType());
            dcrmPricePlan.setRkn_billingtypeid(param);
        }

        // Set the subscription type.
        {
            Lookup param = DcrmSubscriptionTypeSync.getDcrmLookup(context, pricePlan.getSubscriptionType());
            dcrmPricePlan.setRkn_subscriptiontypeid(param);
        }

        // Set the technology.
        {
            Picklist param = DcrmTechnologyPicklistSupport.getPicklist(context, pricePlan.getTechnology());
            dcrmPricePlan.setRkn_technology(param);
        }

        // Set the version (which should be the current version).
        {
            CrmNumber param = new CrmNumber();
            param.set_int(pricePlan.getCurrentVersion());
            dcrmPricePlan.setRkn_version(param);
        }

        // Set the SPID.
        {
            final Lookup spid = DcrmServiceProviderSync.getDcrmLookup(context, pricePlan.getSpid());
            dcrmPricePlan.setRkn_serviceproviderid(spid);
        }

        return dcrmPricePlan;
    }
}
