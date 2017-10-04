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

import com.trilogy.app.crm.bean.ActivationReasonCode;
import com.trilogy.app.crm.bean.ActivationReasonCodeHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_activationreasoncode;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Activation Reason Code information.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmActivationReasonCodeSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_activationreasoncode";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmActivationReasonCodeSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<ActivationReasonCodeHome> PIPELINE_KEY = ActivationReasonCodeHome.class;


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
                updateActivationReasonCode(context, (ActivationReasonCode)obj);
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
            guids[index] = ((Rkn_activationreasoncode)businessEntities[index]).getRkn_activationreasoncodeid();
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

        final ActivationReasonCode activationReasonCode;

        if (evt.getSource() instanceof ActivationReasonCode)
        {
            activationReasonCode = (ActivationReasonCode)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            activationReasonCode = (ActivationReasonCode)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateActivationReasonCode(context, activationReasonCode);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createActivationReasonCode(context, activationReasonCode);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + activationReasonCode,
                null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param activationReasonCode The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final ActivationReasonCode activationReasonCode)
    {
        final int activationReasonCodeID = activationReasonCode.getReasonID();
        return getDcrmGuid(context, activationReasonCodeID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param activationReasonCodeID The ID of the bean for which a GUID is
     * needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final int activationReasonCodeID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Cast because DCRM doesn't support short.
        condition.getValues().addValue((int)activationReasonCodeID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_activationreasoncodeid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param activationReasonCode The bean to create in DCRM.
     */
    private void createActivationReasonCode(final Context context, final ActivationReasonCode activationReasonCode)
    {
        final Rkn_activationreasoncode dcrmActivationReasonCode = convert(context, activationReasonCode);

        final Guid response = DcrmSyncSupport.create(context, dcrmActivationReasonCode);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Activation Reason Code Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Activation Reason Code " + activationReasonCode.getReasonID(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param activationReasonCode The bean to update in DCRM.
     */
    private void updateActivationReasonCode(final Context context, final ActivationReasonCode activationReasonCode)
    {
        final Rkn_activationreasoncode dcrmActivationReasonCode = convert(context, activationReasonCode);
        final Guid dcrmGuid = getDcrmGuid(context, activationReasonCode);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmActivationReasonCode.setRkn_activationreasoncodeid(key);

            DcrmSyncSupport.update(context, dcrmActivationReasonCode);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Activation Reason Code: "
                + activationReasonCode.getReasonID(), null).log(context);
            createActivationReasonCode(context, activationReasonCode);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param activationReasonCode The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_activationreasoncode convert(final Context context, final ActivationReasonCode activationReasonCode)
    {
        final Rkn_activationreasoncode dcrmActivationReasonCode = new Rkn_activationreasoncode();

        // Set the primary key.
        {
            dcrmActivationReasonCode.setRkn_identifier(Integer.toString(activationReasonCode.getReasonID()));
        }

        // Set the name.
        {
            dcrmActivationReasonCode.setRkn_name(activationReasonCode.getMessage());
        }

        // Set the SPID.
        {
            final Lookup spid = DcrmServiceProviderSync.getDcrmLookup(context, activationReasonCode.getSpid());
            dcrmActivationReasonCode.setRkn_serviceproviderid(spid);
        }

        return dcrmActivationReasonCode;
    }
}
