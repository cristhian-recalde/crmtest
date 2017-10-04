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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeChangeListener;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHomeCmd;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.client.dcrm.DcrmSync;

import com.trilogy.dynamics.crm.DCRMService;
import com.trilogy.dynamics.crm.DCRMServiceSupport;
import com.trilogy.dynamics.crm.crmservice._2006.coretypes.Moniker;
import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfString;
import com.trilogy.dynamics.crm.crmservice._2006.query.ColumnSet;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.FilterExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.LogicalOperator;
import com.trilogy.dynamics.crm.crmservice._2006.query.QueryExpression;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntityCollection;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmDateTime;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmMoney;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.AssociateEntitiesRequest;
import com.trilogy.dynamics.crm.crmservice.types.Guid;

import org.apache.axis2.databinding.utils.ConverterUtil;


/**
 * Provide support method for the synchronization support instances.
 *
 * @author gary.anderson@redknee.com
 */
public final class DcrmSyncSupport
{
    /**
     * Private constructor to discourage instantiation.
     */
    private DcrmSyncSupport()
    {
        // Discourage instantiation.
    }


    /**
     * Adds the given listener to the indicated pipeline. The pipeline must
     * contain a NotifyingHome decorator, or likewise that sets-up
     * HomeChangeListener support based on a {@link NotifyingHomeCmd}.
     *
     * @param context The operating context.
     * @param pipelineKey The key used to locate the pipeline in the context.
     * @param listener The listener to add to the pipeline.
     */
    static void addHomeChangeListner(final Context context, final Object pipelineKey, final HomeChangeListener listener)
    {
        final NotifyingHomeCmd registration = new NotifyingHomeCmd(listener);

        final Home pipeline = (Home)context.get(pipelineKey);

        try
        {
            if (pipeline == null)
            {
                throw new HomeException("Failed to locate pipeline for " + pipelineKey);
            }

            pipeline.cmd(context, registration);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(listener, "Failed to add HomeChangeListener for " + pipelineKey, exception).log(context);
        }
    }


    /**
     * Removes the given listener from the indicated pipeline. The pipeline must
     * contain a NotifyingHome decorator, or likewise that sets-up
     * HomeChangeListener support based on a {@link NotifyingHomeCmd}.
     *
     * @param context The operating context.
     * @param pipelineKey The key used to locate the pipeline in the context.
     * @param listener The listener to remove from the pipeline.
     */
    static void removeHomeChangeListner(final Context context, final Object pipelineKey,
        final HomeChangeListener listener)
    {
        final NotifyingHomeCmd registration = new NotifyingHomeCmd(listener, false);

        final Home pipeline = (Home)context.get(pipelineKey);

        try
        {
            if (pipeline == null)
            {
                throw new HomeException("Failed to locate pipeline for " + pipelineKey);
            }

            pipeline.cmd(context, registration);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(listener, "Failed to remove HomeChangeListener for " + pipelineKey, exception).log(context);
        }
    }


    /**
     * Provides a simple proxy to the system's {@link DCRMService#create}
     * method.
     *
     * @param context The operating context.
     * @param entity The entity to create in DCRM.
     * @return The GUID of the new entity instance, automatically assigned by
     * DCRM.
     */
    static Guid create(final Context context, final BusinessEntity entity)
    {
        return DCRMServiceSupport.getService(context).create(context, entity);
    }


    /**
     * Provides a simple proxy to the system's {@link DCRMService#update}
     * method.
     *
     * @param context The operating context.
     * @param entity The entity instance, with only those attributes set that
     * are being updated.
     */
    static void update(final Context context, final BusinessEntity entity)
    {
        DCRMServiceSupport.getService(context).update(context, entity);
    }


    /**
     * Provides a simple proxy to the system's {@link DCRMService#retrieveAll}
     * method.
     *
     * @param context The operating context.
     * @param query The query used to filter the full collection of entity
     * instances to only those of interest to the caller.
     * @return A collection of entity instances matching the given query.
     */
    static BusinessEntityCollection retrieveAll(final Context context, final QueryExpression query)
    {
        return DCRMServiceSupport.getService(context).retrieveAll(context, query);
    }


    /**
     * Gets the GUID of a particular bean. This method is a bit complicated due
     * to the dependence on static types within the CrmService WSDL. This method
     * performs a query on DCRM using the given condition(s) as a filter,
     * requests the guidAttributeName to be included in the return set, passes
     * the return set to the given sync which extracts the GUID(s). If the sync
     * extracts more than one GUID (which should not happen in production), one
     * is chosen arbitrarily as the proper GUID.
     *
     * @param context The operating context.
     * @param guidAttributeName The name of the DCRM attribute that contains the
     * GUID.
     * @param conditions The condition(s) for matching local primary key(s) to
     * DCRM attributes.
     * @param sync The synchronization support that can interpret the query
     * results of a specific DCRM entity to extract the GUID.
     * @return The GUID of the referenced bean if it exists; null otherwise.
     */
    public static Guid getGuid(final Context context, final String guidAttributeName,
        final ConditionExpression[] conditions, final DcrmSync sync)
    {
        final ColumnSet attributesSet = new ColumnSet();
        attributesSet.setAttributes(new ArrayOfString());
        attributesSet.getAttributes().addAttribute(guidAttributeName);

        final FilterExpression filter = new FilterExpression();
        filter.setFilterOperator(LogicalOperator.And);
        filter.setConditions(new ArrayOfConditionExpression());

        for (final ConditionExpression condition : conditions)
        {
            filter.getConditions().addCondition(condition);
        }

        final QueryExpression query = new QueryExpression();

        query.setEntityName(sync.getEntityName());
        query.setColumnSet(attributesSet);
        query.setCriteria(filter);

        final BusinessEntityCollection results = retrieveAll(context, query);

        final BusinessEntity[] businessEntities = results.getBusinessEntities().getBusinessEntity();

        final Guid[] guids = sync.getDcrmGuids(context, businessEntities);

        if (guids.length > 1)
        {
            final StringBuilder builder = new StringBuilder();

            for (final Guid guid : guids)
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }

                builder.append(guid.getGuid());
            }

            new MajorLogMsg(sync.getClass(), "More than one entity with the same key!  GUIDs: " + builder, null).log(context);
        }

        final Guid primaryGuid;

        if (guids.length > 0)
        {
            primaryGuid = guids[0];
        }
        else
        {
            primaryGuid = null;
        }
        return primaryGuid;
    }
    
    public static CrmMoney adaptToCrmMoney(final Context context, final long amount)
    {
        // TODO: This assumes two digit precision -- we have to make this configurable.
        final int SCALE = 2;

        final CrmMoney money = new CrmMoney();
        money.setDecimal(BigDecimal.valueOf(amount, SCALE));
        return money;
    }
    
    public static CrmDateTime adaptToCrmDateTime(final Context context, final Date date)
    {
        final CrmDateTime dateTime = new CrmDateTime();
        
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateTime.setString(ConverterUtil.convertToString(calendar));
        
        return dateTime;
    }
    
    static void associate(final Context context)
    {
	/*
        final AssociateEntitiesRequest request = new AssociateEntitiesRequest();
        
        request.setMoniker1(new Moniker());
        request.getMoniker1().setId(primaryGUID);
        request.getMoniker1().setName(primaryEntityName);
        
        request.setMoniker2(new Moniker());
        request.getMoniker2().setId(relatedGUID);
        request.getMoniker2().setName(relatedEntityName);
        
        request.setRelationshipName(relationshipName);
        */
        
    }
}
