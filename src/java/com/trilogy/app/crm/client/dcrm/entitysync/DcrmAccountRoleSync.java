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

import java.util.List;

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

import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.AccountRoleHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassRow;
import com.trilogy.app.crm.client.dcrm.DcrmSupport;
import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.crmservice._2006.coretypes.Moniker;
import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmNumber;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Key;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.AssociateEntitiesRequest;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_accountrole;
import com.trilogy.dynamics.crm.crmservice.types.Guid;


/**
 * Provides a method to update DCRM with Account Role information. A special
 * concern of this sync is that the local primary key is a "long", but DCRM does
 * not support "long". As a result, this sync casts to "int" and uses
 * Integer.MAX_VALUE for any identifier too large to fit in an "int" and issues
 * MAJOR logs.
 *
 * @author gary.anderson@redknee.com
 */
public class DcrmAccountRoleSync
    implements HomeChangeListener, DcrmSync
{
    /**
     * The DCRM name of the entity.
     */
    private static final String ENTITY_NAME = "rkn_accountrole";


    /**
     * Useful for identifying this DcrmSync.
     */
    public static final String KEY = "DcrmAccountRoleSync";


    /**
     * The pipeline key used to get the pipeline from the operating context.
     */
    private static final Class<AccountRoleHome> PIPELINE_KEY = AccountRoleHome.class;


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
                updateAccountRole(context, (AccountRole)obj);
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
            guids[index] = ((Rkn_accountrole)businessEntities[index]).getRkn_accountroleid();
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

        final AccountRole accountRole;

        if (evt.getSource() instanceof AccountRole)
        {
            accountRole = (AccountRole)evt.getSource();
        }
        else if (evt.getSource() instanceof NotifyingHomeItem)
        {
            final NotifyingHomeItem source = (NotifyingHomeItem)evt.getSource();
            accountRole = (AccountRole)source.getNewObject();
        }
        else
        {
            new MinorLogMsg(this, "Unexpected HomeChangeEvent source: " + evt.getSource(), null).log(context);
            return;
        }

        if (evt.getOperation() == HomeOperationEnum.STORE)
        {
            updateAccountRole(context, accountRole);
        }
        else if (evt.getOperation() == HomeOperationEnum.CREATE)
        {
            createAccountRole(context, accountRole);
        }
        else if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Unsupported Home Operation " + evt.getOperation() + " for " + accountRole, null).log(context);
        }
    }


    /**
     * Gets the DCRM GUID of the given bean.
     *
     * @param context The operating context.
     * @param accountRole The bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final AccountRole accountRole)
    {
        final long accountRoleID = accountRole.getId();
        return getDcrmGuid(context, accountRoleID);
    }


    /**
     * Gets the DCRM GUID for the bean of the given ID.
     *
     * @param context The operating context.
     * @param accountRoleID The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    public Guid getDcrmGuid(final Context context, final long accountRoleID)
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName("rkn_identifier");
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        // Note that DCRM does not support "long". See notes in class comments.
        condition.getValues().addValue((int)accountRoleID);

        final ConditionExpression[] conditions = new ConditionExpression[]
        {
            condition,
        };

        final Guid primaryGuid = DcrmSyncSupport.getGuid(context, "rkn_accountroleid", conditions, this);

        return primaryGuid;
    }
    
    
    /**
     * Gets the DCRM Lookup for the bean of the given ID.
     *
     * @param context The operating context.
     * @param accountRoleID The ID of the bean for which a Lookup is needed.
     * @return The Lookup if one exists; null otherwise.
     */
    public static Lookup getDcrmLookup(final Context context, final long accountRoleID)
    {
        final Lookup accountRole = new Lookup();
        accountRole.setType(ENTITY_NAME);
    
        final DcrmAccountRoleSync accountRoleSync =
            (DcrmAccountRoleSync)DcrmSupport.getSync(context, KEY);
    
        final Guid dcrmGuid = accountRoleSync.getDcrmGuid(context, accountRoleID);
        accountRole.setGuid(dcrmGuid.getGuid());
        return accountRole;
    }



    /**
     * Creates a new bean in DCRM.
     *
     * @param context The operating context.
     * @param accountRole The bean to create in DCRM.
     */
    private void createAccountRole(final Context context, final AccountRole accountRole)
    {
        final Rkn_accountrole dcrmAccountRole = convert(context, accountRole);

        final Guid response = DcrmSyncSupport.create(context, dcrmAccountRole);

        if (response != null && LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Account Role Created: " + response.getGuid(), null).log(context);
        }
        else
        {
            new MajorLogMsg(this, "Failed to create Account Role " + accountRole.getId(), null).log(context);
        }
        
        for (final SubscriptionClassRow subscriptionClassReference : (List<SubscriptionClassRow>)accountRole.getAllowedSubscriptionClass())
        {
            addSubscriptionClass(context, response, subscriptionClassReference.getSubscriptionClass());
        }
    }

    
    private void addSubscriptionClass(final Context context, final Guid primaryGuid, final long subscriptionClassID)
    {
        final Moniker primary = new Moniker();
        primary.setId(primaryGuid);
        primary.setName(ENTITY_NAME);
        
        //final Lookup subscriptionClassLookup = 
    }

    /**
     * Updates the bean in DCRM, creating it if necessary.
     *
     * @param context The operating context.
     * @param accountRole The bean to update in DCRM.
     */
    private void updateAccountRole(final Context context, final AccountRole accountRole)
    {
        final Rkn_accountrole dcrmAccountRole = convert(context, accountRole);
        final Guid dcrmGuid = getDcrmGuid(context, accountRole);

        if (dcrmGuid != null)
        {
            final Key key = new Key();
            key.setGuid(dcrmGuid.getGuid());
            dcrmAccountRole.setRkn_accountroleid(key);

            DcrmSyncSupport.update(context, dcrmAccountRole);
        }
        else
        {
            new MajorLogMsg(this, "Failed to look-up corresponding Account Role: " + accountRole.getId(), null).log(context);
            createAccountRole(context, accountRole);
        }
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     *
     * @param context The operating context.
     * @param accountRole The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private Rkn_accountrole convert(final Context context, final AccountRole accountRole)
    {
        final Rkn_accountrole dcrmAccountRole = new Rkn_accountrole();

        // Set the primary key.
        {
            dcrmAccountRole.setRkn_identifier(Long.toString(accountRole.getId()));
        }

        // Set the name.
        {
            dcrmAccountRole.setRkn_name(accountRole.getName());
        }

        return dcrmAccountRole;
    }
}
