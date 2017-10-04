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

import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmBoolean;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_paymentmethodtype;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Transaction Method information. A
 * special concern of this sync is that the local primary key is a "long", but
 * DCRM does not support "long". As a result, this sync casts to "int" and uses
 * Integer.MAX_VALUE for any identifier too large to fit in an "int" and issues
 * MAJOR logs.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmTransactionMethodSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_paymentmethodtype";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmTransactionMethodSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<TransactionMethodHome> PIPELINE_KEY = TransactionMethodHome.class;


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
                updateTransactionMethod(context, (TransactionMethod)obj);
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
            guids[index] = ((Rkn_paymentmethodtype)businessEntities[index]).getRkn_paymentmethodtypeid();
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

        final TransactionMethod transactionMethod;

        if (evt.getSource() instanceof TransactionMethod)
        {
            transactionMethod = (TransactionMethod)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            transactionMethod = (TransactionMethod)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateTransactionMethod(context, transactionMethod);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createTransactionMethod(context, transactionMethod);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + transactionMethod, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param transactionMethod The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final TransactionMethod transactionMethod)
    {
        final long transactionMethodID = transactionMethod.getIdentifier();
        return getDcrmGuid(context, transactionMethodID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param transactionMethodID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final long transactionMethodID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Note that DCRM does not support "long". See notes in class comments.
        condition.getValues().addValue((int)transactionMethodID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_paymentmethodtypeid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param transactionMethod The bean to create in DCRM.
     */
    private void createTransactionMethod(final Context context, final TransactionMethod transactionMethod)
    {
        final Rkn_paymentmethodtype dcrmTransactionMethod = convert(context, transactionMethod);

        final Guid response = DcrmSyncSupport.create(context, dcrmTransactionMethod);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Transaction Method Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Transaction Method " + transactionMethod.getIdentifier(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param transactionMethod The bean to update in DCRM.
     */
    private void updateTransactionMethod(final Context context, final TransactionMethod transactionMethod)
    {
        final Rkn_paymentmethodtype dcrmTransactionMethod = convert(context, transactionMethod);
        final Guid dcrmGuid = getDcrmGuid(context, transactionMethod);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmTransactionMethod.setRkn_paymentmethodtypeid(key);

            DcrmSyncSupport.update(context, dcrmTransactionMethod);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Transaction Method: " + transactionMethod.getIdentifier(), null).log(context);
            createTransactionMethod(context, transactionMethod);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param transactionMethod The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_paymentmethodtype convert(final Context context, final TransactionMethod transactionMethod)
    {
        final Rkn_paymentmethodtype dcrmTransactionMethod = new Rkn_paymentmethodtype();

        // Set the primary key.
        {
            dcrmTransactionMethod.setRkn_identifier(Long.toString(transactionMethod.getIdentifier()));
        }

        // Set the name.
        {
            dcrmTransactionMethod.setRkn_name(transactionMethod.getName());
        }

        // Set the description.
        {
            dcrmTransactionMethod.setRkn_description(transactionMethod.getDescription());
        }

        // Set the uses back account property.
        {
            CrmBoolean param = new CrmBoolean();
            param.set_boolean(transactionMethod.isBankAccountUsed());
            dcrmTransactionMethod.setRkn_usesbankaccount(param);
        }

        // Set the uses bank transit property.
        {
            CrmBoolean param = new CrmBoolean();
            param.set_boolean(transactionMethod.isBankTransitUsed());
            dcrmTransactionMethod.setRkn_usesbanktransit(param);
        }

        // Set the uses date property.
        {
            CrmBoolean param = new CrmBoolean();
            param.set_boolean(transactionMethod.isDateUsed());
            dcrmTransactionMethod.setRkn_usesdate(param);
        }

        // Set the uses identifier property.
        {
            CrmBoolean param = new CrmBoolean();
            param.set_boolean(transactionMethod.isIdentifierUsed());
            dcrmTransactionMethod.setRkn_usesidentifier(param);
        }

        // Set the uses name property.
        {
            CrmBoolean param = new CrmBoolean();
            param.set_boolean(transactionMethod.isNameUsed());
            dcrmTransactionMethod.setRkn_usesname(param);
        }

        return dcrmTransactionMethod;
    }
}
