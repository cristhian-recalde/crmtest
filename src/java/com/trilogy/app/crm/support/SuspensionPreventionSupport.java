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
package com.trilogy.app.crm.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.util.CollectionsUtils;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author sbanerjee
 *
 */
public class SuspensionPreventionSupport
{
    /**
     * 
     * @param <INDEX>
     * @param <ENTITY>
     * @param ctx
     * @param allSuspendedIds
     * @param allItems Source Map (Map keyed by the Indices)
     * @param predicate
     * @param resultSetKeys Ids of suspended entities
     * @return
     */
    public static <INDEX, ENTITY> boolean unprovisionSuspendedEntities(Context ctx,
            final Collection<INDEX> allSuspendedIds,
            final Map<INDEX, ENTITY> allItems,
            final TypedPredicate<INDEX> predicate,
            final Collection<INDEX> resultSetKeys)
    {
        Set<INDEX> toUnprovisionIds = new HashSet<INDEX>();
        CollectionsUtils.<INDEX>filter(ctx, toUnprovisionIds, allSuspendedIds, 
                predicate);
        
        resultSetKeys.addAll(toUnprovisionIds);
        for(INDEX id: toUnprovisionIds)
            allItems.remove(id);
        
        boolean didUnprovision = !toUnprovisionIds.isEmpty();
        return didUnprovision;
    }
    
    /**
     * 
     * @param <INDEX>
     * @param <ENTITY>
     * @param ctx
     * @param allSuspendedIds
     * @param allItems Source Collection (Collection of Indices)
     * @param predicate
     * @return
     */
    public static <INDEX, ENTITY> boolean unprovisionSuspendedEntities(Context ctx,
            final Collection<INDEX> allSuspendedIds,
            final Collection<INDEX> allItems,
            final TypedPredicate<INDEX> predicate,
            final Collection<INDEX> resultSetKeys)
    {
        Set<INDEX> toUnprovisionIds = new HashSet<INDEX>();
        CollectionsUtils.<INDEX>filter(ctx, toUnprovisionIds, allSuspendedIds, 
                predicate);
        
        resultSetKeys.addAll(toUnprovisionIds);
        for(INDEX id: toUnprovisionIds)
            allItems.remove(id);
        
        boolean didUnprovision = !toUnprovisionIds.isEmpty();
        return didUnprovision;
    }

    /**
     * 
     * @param <ENTITY>
     * @param ctx
     * @param allSuspendedEntities
     * @param predicate
     * @return
     */
    public static <ENTITY> Collection<ENTITY> getSuspendedEntitiesCollectionToUnprovision(Context ctx,
            final Collection<ENTITY> allSuspendedEntities,
            final TypedPredicate<ENTITY> predicate)
    {
        Set<ENTITY> toUnprovisionEntities = new HashSet<ENTITY>();
        CollectionsUtils.<ENTITY>filter(ctx, toUnprovisionEntities, allSuspendedEntities, 
                predicate);
        
        return toUnprovisionEntities;
    }
}