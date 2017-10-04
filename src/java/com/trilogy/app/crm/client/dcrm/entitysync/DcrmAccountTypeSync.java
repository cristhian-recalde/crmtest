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

import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmBoolean;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Picklist;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_accounttype;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Account Type information. A special
 * concern of this sync is that the local primary key is a "long", but DCRM does
 * not support "long". As a result, this sync casts to "int" and uses
 * Integer.MAX_VALUE for any identifier too large to fit in an "int" and issues
 * MAJOR logs.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmAccountTypeSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_accounttype";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmAccountTypeSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<AccountCategoryHome> PIPELINE_KEY = AccountCategoryHome.class;


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
                updateAccountType(context, (AccountCategory)obj);
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
            guids[index] = ((Rkn_accounttype)businessEntities[index]).getRkn_accounttypeid();
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

        final AccountCategory accountType;

        if (evt.getSource() instanceof AccountCategory)
        {
            accountType = (AccountCategory)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            accountType = (AccountCategory)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateAccountType(context, accountType);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createAccountType(context, accountType);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + accountType, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param accountType The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final AccountCategory accountType)
    {
        final long accountTypeID = accountType.getIdentifier();
        return getDcrmGuid(context, accountTypeID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param accountTypeID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final long accountTypeID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Note that DCRM does not support "long". See notes in class comments.
        condition.getValues().addValue((int)accountTypeID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_accounttypeid", conditions, this);

        return primaryGuid;
    }


    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param accountType The bean to create in DCRM.
     */
    private void createAccountType(final Context context, final AccountCategory accountType)
    {
        final Rkn_accounttype dcrmAccountType = convert(context, accountType);

        final Guid response = DcrmSyncSupport.create(context, dcrmAccountType);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Account Type Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Account Type " + accountType.getIdentifier(), null).log(context);
        }
    }


    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param accountType The bean to update in DCRM.
     */
    private void updateAccountType(final Context context, final AccountCategory accountType)
    {
        final Rkn_accounttype dcrmAccountType = convert(context, accountType);
        final Guid dcrmGuid = getDcrmGuid(context, accountType);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmAccountType.setRkn_accounttypeid(key);

            DcrmSyncSupport.update(context, dcrmAccountType);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Account Type: " + accountType.getIdentifier(), null).log(context);
            createAccountType(context, accountType);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param accountType The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_accounttype convert(final Context context, final AccountCategory accountType)
    {
        final Rkn_accounttype dcrmAccountType = new Rkn_accounttype();

        // Set the primary key.
        {
            final CrmNumber identifier = new CrmNumber();
            long identifierValue = accountType.getIdentifier();

            if (identifierValue > Integer.MAX_VALUE)
            {
                new MajorLogMsg(this, "Primary key ID exceeds \"int\" type of CrmNumber: " + identifierValue, null).log(context);
                identifierValue = Integer.MAX_VALUE;
            }

            // Note that DCRM does not support "long". See notes in class
            // comments.
            identifier.set_int((int)identifierValue);
            dcrmAccountType.setRkn_identifier(Long.toString(identifierValue));
        }

        // Set the name.
        {
            dcrmAccountType.setRkn_name(accountType.getName());
        }

 

        // Set the type.
        /*
        {
            // DCRM.Subscriber == 0 == BOSS.Subscriber
            // DCRM.Group == 1 == BOSS.Subscriber
            // DCRM.Group_Pooled == 2 != BOSS.Group_Pooled == 3
            final Picklist listType = new Picklist();
            switch (accountType.getType().getIndex())
            {
                case 0:
                case 1:
                {
                    listType.set_int(accountType.getType().getIndex());
                    break;
                }
                case 3:
                {
                    listType.set_int(2);
                    break;
                }
                default:
                {
                    throw new IllegalStateException("Account type " + accountType.getIdentifier()
                        + " has an unexpected type: " + accountType.getType());
                }
            }

            dcrmAccountType.setRkn_customertype(listType);
        }
        */

        return dcrmAccountType;
    }
}
