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

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_serviceprovider;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Service Provider information.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmServiceProviderSync
    implements DcrmSync, HomeChangeListener
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_serviceprovider";

    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmServiceProviderSync";

    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<CRMSpidHome> PIPELINE_KEY = CRMSpidHome.class;


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
                updateSpid(context, (CRMSpid)obj);
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
            guids[index] = ((Rkn_serviceprovider)businessEntities[index]).getRkn_serviceproviderid();
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

        final CRMSpid spid;

        if (evt.getSource() instanceof CRMSpid)
        {
            spid = (CRMSpid)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            spid = (CRMSpid)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateSpid(context, spid);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createSpid(context, spid);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + spid, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID if the given bean.
     *
     * @param context The operating context.
     * @param spid The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final CRMSpid spid)
    {
        final int spidID = spid.getId();
        return getDcrmGuid(context, spidID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param spidID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final int spidID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        condition.getValues().addValue(spidID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_serviceproviderid", conditions, this);

        return primaryGuid;
    }


    /**
     * Gets the DCRM Lookup for the bean of the given ID.
     *
     * @param context The operating context.
     * @param spidID The ID of the bean for which a Lookup is needed.
     * @return The Lookup if one exists; null otherwise.
     */
    public static Lookup getDcrmLookup(final Context context, final int spidID)
    {
        final Lookup spid = new Lookup();
        spid.setType(ENTITY_NAME);
    
        final DcrmServiceProviderSync spidSync =
            (DcrmServiceProviderSync)DcrmSupport.getSync(context, KEY);
    
        final Guid dcrmGuid = spidSync.getDcrmGuid(context, spidID);
        spid.setGuid(dcrmGuid.getGuid());
        return spid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param spid The bean to create in DCRM.
     */
    private void createSpid(final Context context, final CRMSpid spid)
    {
        final Rkn_serviceprovider dcrmServiceProvider = convert(context, spid);

        final Guid response = DcrmSyncSupport.create(context, dcrmServiceProvider);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Service Provider Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Service Provider " + spid.getId(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param spid The bean to update in DCRM.
     */
    private void updateSpid(final Context context, final CRMSpid spid)
    {
        final Rkn_serviceprovider dcrmServiceProvider = convert(context, spid);
        final Guid dcrmGuid = getDcrmGuid(context, spid);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmServiceProvider.setRkn_serviceproviderid(key);

            DcrmSyncSupport.update(context, dcrmServiceProvider);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Service Provider: " + spid.getId(), null).log(context);
            createSpid(context, spid);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param spid The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_serviceprovider convert(final Context context, final CRMSpid spid)
    {
        final Rkn_serviceprovider dcrmServiceProvider = new Rkn_serviceprovider();

        // Set the ID.
        {
            dcrmServiceProvider.setRkn_identifier(Integer.toString(spid.getId()));
        }

        // Set the name.
        {
            dcrmServiceProvider.setRkn_name(spid.getName());
        }

        return dcrmServiceProvider;
    }

}
