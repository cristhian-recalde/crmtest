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

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SuspendedEntity;
import com.trilogy.app.crm.bean.SuspendedEntityHome;
import com.trilogy.app.crm.bean.SuspendedEntityXInfo;
import com.trilogy.app.crm.refactoring.ServiceRefactoring_RefactoringClass;


/**
 * Support class for suspended entity.
 *
 * @author victor.stratan@redknee.com
 */
public final class SuspendedEntitySupport
{

    /**
     * Creates a new <code>SuspendedEntitySupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private SuspendedEntitySupport()
    {
        // empty
    }


    /**
     * Creates a suspended entity.
     *
     * @param context
     *            The operating context.
     * @param subId
     *            Subscriber ID.
     * @param entityId
     *            Entity ID.
     * @param secondaryId
     *            Secondary entity ID.
     * @param type
     *            Type of item suspended.
     * @throws HomeException
     *             Thrown by home.
     */
    public static void createSuspendedEntity(final Context context, final String subId, final long entityId,
            final long secondaryId, final Class type) throws HomeException
    {
        createSuspendedEntity(context, subId, entityId, secondaryId, getProperClassName(type));
    }
    
    protected static void createSuspendedEntity(final Context context, final String subId, final long entityId,
        final long secondaryId, final String type) throws HomeException
    {
        final Home suspEntityHome = (Home) context.get(SuspendedEntityHome.class);

        if (suspEntityHome != null)
        {
            final SuspendedEntity record = findSuspendedEntity(context, subId, entityId, secondaryId, type);
            if (record == null)
            {
                // only add a new record when one does not exist
                final SuspendedEntity entity = new SuspendedEntity();
                entity.setSubscriberId(subId);
                entity.setIdentifier(entityId);
                entity.setSecondaryIdentifier(secondaryId);
                entity.setType(type);
                suspEntityHome.create(context, entity);
            }
            else if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, SuspendedEntitySupport.class.getName(),
                    "LOGIC ERROR: Attempt to mark as suspended an already suspended entity! Sub: " + subId
                        + "  Entity: " + type + "  Entity ID: " + entityId, new Exception());
            }
        }

    }


    /**
     * Removes a suspended entity.
     *
     * @param ctx
     *            The operating context.
     * @param subId
     *            Subscriber ID.
     * @param identifier
     *            Suspended entity ID.
     * @param type
     *            Type of suspended entity.
     * @throws HomeException
     *             Thrown if there are problems removing the suspended entity.
     */
    public static void removeSuspendedEntity(final Context context, final String subId, final long entityId,
            final long secondaryId, final Class type) throws HomeException
    {
        removeSuspendedEntity(context, subId, entityId, secondaryId, getProperClassName(type));
    }

    protected static void removeSuspendedEntity(final Context context, final String subId, final long entityId,
            final long secondaryId, final String type) throws HomeException
        {
        SuspendedEntity entity = findSuspendedEntity(context, subId, entityId, secondaryId, type);
        while (entity != null)
        {
            final Home suspEntityHome = (Home) context.get(SuspendedEntityHome.class);
            suspEntityHome.remove(context, entity);
            entity = findSuspendedEntity(context, subId, entityId, secondaryId, type);
            if (entity != null && LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, SuspendedEntitySupport.class.getName(),
                    "LOGIC ERROR: Double suspended suspended entity record! Sub: " + subId + "  Entity: " + type
                        + "  Entity ID: " + entityId, new Exception());
            }
        }
    }


    /**
     * Looks up a suspended entity.
     *
     * @param ctx
     *            The operating context.
     * @param subId
     *            Subscriber ID.
     * @param entityId
     *            Entity ID.
     * @param secondaryId
     *            Secondary entity ID.
     * @param type
     *            Type of item suspended.
     * @return The corresponding suspended entity.
     * @throws HomeException
     *             Thrown by home.
     */
    public static SuspendedEntity findSuspendedEntity(final Context ctx, final String subId, final long entityId,
        final long secondaryId, final Class type) throws HomeException
    {
        return findSuspendedEntity(ctx, subId, entityId, secondaryId, getProperClassName(type));
    }
    
    protected static SuspendedEntity findSuspendedEntity(final Context ctx, final String subId, final long entityId,
            final long secondaryId, final String type) throws HomeException
        {
        final Home suspEntityHome = (Home) ctx.get(SuspendedEntityHome.class);
        SuspendedEntity entity = null;

        if (suspEntityHome != null)
        {
            final And filter = new And();

            filter.add(new EQ(SuspendedEntityXInfo.SUBSCRIBER_ID, subId));
            filter.add(new EQ(SuspendedEntityXInfo.IDENTIFIER, Long.valueOf(entityId)));
            filter.add(new EQ(SuspendedEntityXInfo.SECONDARY_IDENTIFIER, Long.valueOf(secondaryId)));
            filter.add(new EQ(SuspendedEntityXInfo.TYPE, type));
            entity = (SuspendedEntity) suspEntityHome.find(ctx, filter);
        }

        return entity;
    }

    /**
     * Looks up a list of suspended entity based on aux service ID and secondary ID.
     *
     * @param ctx
     *            The operating context.
     * @param entityId
     *            Entity ID.
     * @param secondaryId
     *            Secondary entity ID.
     * @param type
     *            Type of item suspended.
     * @return The corresponding list of suspended entity.
     * @throws HomeException
     *             Thrown by home.
     */
    public static Collection<SuspendedEntity> findSuspendedEntity(final Context ctx, final long entityId,
            final long secondaryId, final Class type) throws HomeException
    {
        return findSuspendedEntity(ctx, entityId, secondaryId, getProperClassName(type));
    }
    
    protected static Collection<SuspendedEntity> findSuspendedEntity(final Context ctx, final long entityId,
        final long secondaryId, final String type) throws HomeException
    {
        final Home suspEntityHome = (Home) ctx.get(SuspendedEntityHome.class);
        Collection<SuspendedEntity> result = null;

        if (suspEntityHome != null)
        {
            final And filter = new And();

            filter.add(new EQ(SuspendedEntityXInfo.IDENTIFIER, Long.valueOf(entityId)));
            filter.add(new EQ(SuspendedEntityXInfo.SECONDARY_IDENTIFIER, Long.valueOf(secondaryId)));
            filter.add(new EQ(SuspendedEntityXInfo.TYPE, type));
            result = suspEntityHome.select(ctx, filter);
        }

        return result;
    }

    /**
     * Returns a collection of Suspended Packages, Suspended Bundles and 
     * Auxiliary Services for a particular subscriber.
     *
     * @param context
     *            The operating context.
     * @param subId
     *            Subscriber Identifier.
     * @param type
     *            Type of suspended entity.
     * @return SuspendedEntities collection.
     */
    public static Collection getSuspendedEntities(final Context context, final String subId, final Class type)
    {
        return getSuspendedEntities(context, subId, getProperClassName(type));
    }
    
    protected static Collection getSuspendedEntities(final Context context, final String subId, final String type)
    {
        final Home suspendedHome = (Home) context.get(SuspendedEntityHome.class);
        Collection suspEnts = new ArrayList();

        if (suspendedHome != null)
        {
        	try
        	{
                final And filter = new And();
                filter.add(new EQ(SuspendedEntityXInfo.SUBSCRIBER_ID, subId));
                if (type != null)
        		{
        			filter.add(new EQ(SuspendedEntityXInfo.TYPE, type));
        		}
        		suspEnts = suspendedHome.where(context, filter).selectAll();
        	}
        	catch (final Exception e)
        	{
        		if (LogSupport.isDebugEnabled(context))
        		{
        			LogSupport.debug(context, SuspendedEntitySupport.class,
        					"Exception caught when looking up suspended entities of subscriber " + subId, e);
        		}
        	}
        }

        return suspEnts;
    }


    /**
     * Determines whether an entity is suspended.
     *
     * @param context
     *            The operating context.
     * @param subId
     *            Subscriber ID.
     * @param entityId
     *            Entity ID.
     * @param secondaryId
     *            Secondary entity ID.
     * @param type
     *            Type of item suspended.
     * @return Whether the entity is suspended.
     * @throws HomeException
     *             Thrown by home.
     */
    public static boolean isSuspendedEntity(final Context context, final String subId, final long entityId,
        final long secondaryId, final Class type) throws HomeException
    {
        final SuspendedEntity entity = findSuspendedEntity(context, subId, entityId, secondaryId, type);
        return entity != null;
    }



    /**
     * Determines if an object is suspended.
     *
     * @param ctx
     *            The operating context.
     * @param subId
     *            Subscriber identifier.
     * @param obj
     *            Entity.
     * @return Returns whether the entity is suspended for the subscriber.
     * @throws HomeException
     *             Thrown if there are problems looking up the suspended entity.
     */
    public static boolean isObjectSuspended(final Context ctx, final String subId, final Object obj)
        throws HomeException
    {
        boolean result = false;
        if (obj instanceof com.redknee.app.crm.bean.ServicePackage)
        {
            result = isSuspendedEntity(ctx, subId, ((com.redknee.app.crm.bean.ServicePackage) obj).getIdentifier(), -1, obj.getClass());
        }
        else if (obj instanceof com.redknee.app.crm.bean.ServiceFee2)
        {
        	// CRM 8.0, Service Refactoring, from now on Subscriber get all Suspended Services is done by querying the 
        	// SubscriberServices table
        	ServiceRefactoring_RefactoringClass.lookupSuspendedServicesUsingSubscriberServices();
        }
        else if (obj instanceof com.redknee.app.crm.bean.Service)
        {
            com.redknee.app.crm.bean.Service svc = (com.redknee.app.crm.bean.Service) obj;
            result = isSuspendedEntity(ctx, subId, svc.getIdentifier(), -1, com.redknee.app.crm.bean.ServiceFee2.class);
        }
        else if (obj instanceof com.redknee.app.crm.bundle.BundleFee)
        {
            result = isSuspendedEntity(ctx, subId, ((com.redknee.app.crm.bundle.BundleFee) obj).getId(), -1, obj.getClass());
        }
        else if (obj instanceof com.redknee.app.crm.bean.SubscriberAuxiliaryService)
        {
            final com.redknee.app.crm.bean.SubscriberAuxiliaryService association = (com.redknee.app.crm.bean.SubscriberAuxiliaryService) obj;
            result = isSuspendedEntity(ctx, subId, association.getAuxiliaryServiceIdentifier(), association
                .getSecondaryIdentifier(), com.redknee.app.crm.bean.AuxiliaryService.class);
        }

        return result;
    }


    /**
     * Moves all suspended entities (Services, Bundles, Aux Services, Service Packages)
     *  of a subscriber to a different subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param oldSubscriber
     *            Old subscriber.
     * @param newSubscriber
     *            New subscriber.
     */
    public static void moveSubscriber(final Context ctx, final Subscriber oldSubscriber,
    		final Subscriber newSubscriber)
    {
    	moveSuspendedEntities(ctx, oldSubscriber, newSubscriber);
    	SubscriberServicesSupport.moveSuspendedServices(ctx, oldSubscriber, newSubscriber);

        // SuspendEntitiesVisitor is changed to check the newly added suspensions only, so that it's not necessary to do following
        // TT 9062300020
    	//SuspendEntitiesVisitor suspendEntities = new SuspendEntitiesVisitor();
    	//suspendEntities.visit(ctx, newSubscriber);
    }


    /**
     * Moves any suspended entities (everything but services) out of the old subscriber
     * @param ctx the operating context
     * @param oldSubscriber the subscriber to be moved
     * @param newSubscriber the newly created subscriber
     */
    private static void moveSuspendedEntities(Context ctx,
    		Subscriber oldSubscriber,
    		Subscriber newSubscriber)
    {
    	Collection<SuspendedEntity> entities = getSuspendedEntities(ctx, oldSubscriber.getId(), (String) null);
    	for(SuspendedEntity entity: entities)
    	{
    		try
    		{
    			removeSuspendedEntity(ctx, oldSubscriber.getId(), entity.getIdentifier(), entity
                        .getSecondaryIdentifier(), entity.getType());
    		}
    		catch (HomeException e)
    		{
    			LogSupport.debug(ctx, SuspendedEntitySupport.class.getName(), 
    					"Cannot remove suspended entity record from subscriber=" + oldSubscriber.getId(), e);
    		}

    		try
    		{
    			createSuspendedEntity(ctx, newSubscriber.getId(), entity.getIdentifier(), entity
                        .getSecondaryIdentifier(), entity.getType());
    		}
    		catch (HomeException e)
    		{
    			LogSupport.debug(ctx, SuspendedEntitySupport.class.getName(), 
    					"Cannot create suspended entity record for subscriber=" + newSubscriber.getId(), e);
    		}
    	}

    }


    /**
     * Unsuspend all entities of a subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber whose entities are getting unsuspended.
     */
    public static void unsuspendAllEntities(final Context context, final Subscriber subscriber)
    {
   	    final Collection<SuspendedEntity> entities = getSuspendedEntities(context, subscriber.getId(), (String) null);
        for (final SuspendedEntity entity : entities)
        {
            try
            {
                removeSuspendedEntity(context, subscriber.getId(), entity.getIdentifier(), entity
                        .getSecondaryIdentifier(), entity.getType());
                
            	//TODO: Unsuspend all Services separately
            	ServiceRefactoring_RefactoringClass.unsuspendSuspendedServicesUsingSubscriberServices();
            }
            catch (final HomeException exception)
                {
                if (LogSupport.isDebugEnabled(context))
                    {
                    LogSupport.debug(context, SuspendedEntitySupport.class, "Cannot remove suspended entity record",
                        exception);
                    }
                }
            }
        }
    
    /**
     * This method returns the base class for the suspended entity type.
     * 
     * @param type
     */
    protected static String getProperClassName(Class type)
    {
        if (type == null)
        {
            return null;
        }
        
        if (com.redknee.app.crm.bean.ServiceFee2.class.isAssignableFrom(type))
        {
            return com.redknee.app.crm.bean.ServiceFee2.class.getName();
        }
        else if (com.redknee.app.crm.bundle.BundleFee.class.isAssignableFrom(type))
        {
            return com.redknee.app.crm.bundle.BundleFee.class.getName();
        }
        else if (com.redknee.app.crm.bean.AuxiliaryService.class.isAssignableFrom(type))
        {
            return com.redknee.app.crm.bean.AuxiliaryService.class.getName();
        }
        else if (com.redknee.app.crm.bean.ServicePackage.class.isAssignableFrom(type))
        {
            return com.redknee.app.crm.bean.ServicePackage.class.getName();
        }
        return type.getName();
    }
} // class
