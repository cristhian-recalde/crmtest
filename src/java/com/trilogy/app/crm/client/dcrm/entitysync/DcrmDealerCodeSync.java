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

import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_dealercode;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Dealer Code information.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmDealerCodeSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_dealercode";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmDealerCodeSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<DealerCodeHome> PIPELINE_KEY = DealerCodeHome.class;


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
                updateDealerCode(context, (DealerCode)obj);
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
            guids[index] = ((Rkn_dealercode)businessEntities[index]).getRkn_dealercodeid();
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

        final DealerCode dealerCode;

        if (evt.getSource() instanceof DealerCode)
        {
            dealerCode = (DealerCode)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            dealerCode = (DealerCode)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateDealerCode(context, dealerCode);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createDealerCode(context, dealerCode);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + dealerCode, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param dealerCode The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final DealerCode dealerCode)
    {
        final String dealerCodeID = dealerCode.getCode();
        return getDcrmGuid(context, dealerCodeID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param dealerCodeID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final String dealerCodeID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_dealercode");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        condition.getValues().addValue(dealerCodeID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_dealercodeid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param dealerCode The bean to create in DCRM.
     */
    private void createDealerCode(final Context context, final DealerCode dealerCode)
    {
        final Rkn_dealercode dcrmDealerCode = convert(context, dealerCode);

        final Guid response = DcrmSyncSupport.create(context, dcrmDealerCode);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Dealer Code Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Dealer Code " + dealerCode.getCode(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param dealerCode The bean to update in DCRM.
     */
    private void updateDealerCode(final Context context, final DealerCode dealerCode)
    {
        final Rkn_dealercode dcrmDealerCode = convert(context, dealerCode);
        final Guid dcrmGuid = getDcrmGuid(context, dealerCode);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmDealerCode.setRkn_dealercodeid(key);

            DcrmSyncSupport.update(context, dcrmDealerCode);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Dealer Code: " + dealerCode.getCode(), null).log(context);
            createDealerCode(context, dealerCode);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param dealerCode The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_dealercode convert(final Context context, final DealerCode dealerCode)
    {
        final Rkn_dealercode dcrmDealerCode = new Rkn_dealercode();

        // Set the primary key.
        {
            dcrmDealerCode.setRkn_identifier(dealerCode.getCode());
        }

        // Set the description.
        {
            dcrmDealerCode.setRkn_description(dealerCode.getDesc());
        }

        // Set the SPID.
        {
            final Lookup spid = DcrmServiceProviderSync.getDcrmLookup(context, dealerCode.getSpid());
            dcrmDealerCode.setRkn_serviceproviderid(spid);
        }

        return dcrmDealerCode;
    }
}
