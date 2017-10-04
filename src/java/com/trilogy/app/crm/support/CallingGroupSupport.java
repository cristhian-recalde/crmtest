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
import java.util.Iterator;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Provides utility methods for use with CallingGroups (PersonalListPlan,
 * ClosedUserGroup, etc.).
 *
 * @author gary.anderson@redknee.com
 */
public
class CallingGroupSupport
{
    /**
     * Gets a suitable display message for the result code returned from the
     * FFECareRmiService interface.  This code is highly dependent on the
     * FFECareRmiService interface and required manual checking to ensure
     * consistency.
     *
     * @param context The operating context.
     * @param result The result code.
     * @return A suitable display message for the result code.
     */
    public static String getDisplayMessageForReturnCode(
        final Context context,
        final int result)
    {
        return ExternalAppSupportHelper.get(context).getErrorCodeMessage(context, ExternalAppEnum.FF, result);
    }

    private static AuxiliaryService getAuxiliaryService(final Context ctx, final long id, final CallingGroupTypeEnum callingGroupType) throws HomeException
    {
        And filter = new And();
        filter.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_TYPE, callingGroupType));
        filter.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_IDENTIFIER, Long.valueOf(id)));
        
       
        CallingGroupAuxSvcExtension extension = HomeSupportHelper.get(ctx).findBean(ctx,  CallingGroupAuxSvcExtension.class, filter);
       
        return getAuxiliaryService(ctx, extension);
    }
    
    private static AuxiliaryService getAuxiliaryService(final Context ctx, CallingGroupAuxSvcExtension extension) throws HomeException
    {
        AuxiliaryService auxiliaryService = null;
        
        if (extension!=null)
        {
            auxiliaryService = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, new EQ(AuxiliaryServiceXInfo.IDENTIFIER, extension.getAuxiliaryServiceId()));
        }
        return auxiliaryService;
    }

    public static AuxiliaryService getAuxiliaryServiceForBirthdayPlan(final Context ctx, final long plpId) throws HomeException
    {
        return getAuxiliaryService(ctx, plpId, CallingGroupTypeEnum.BP);
    }

    public static AuxiliaryService getAuxiliaryServiceForPersonalListPlan(final Context ctx, final long bpId) throws HomeException
    {
        return getAuxiliaryService(ctx, bpId, CallingGroupTypeEnum.PLP);
    }
    
    /**
     * Search the AuxiliaryService home for the corresponding auxiliary service
     * entry for the given CUG.
     * 
     * @param ctx The operating context.
     * @param cug The given CUG
     * 
     * @return AuxiliaryService the auxiliary service entry found for the given
     * CUG.
     */
    public static AuxiliaryService getAuxiliaryServiceForCUGTemplate(
        final Context ctx,
        final long cugTemplateId)
        throws HomeException
    {
        And filter = new And();
        final Or  subCondition = new Or(); 
        subCondition.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_TYPE, CallingGroupTypeEnum.CUG));
        subCondition.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_TYPE, CallingGroupTypeEnum.PCUG));
        filter.add(subCondition);
        filter.add(new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_IDENTIFIER, Long.valueOf(cugTemplateId)));
        
       
        CallingGroupAuxSvcExtension extension = HomeSupportHelper.get(ctx).findBean(ctx,  CallingGroupAuxSvcExtension.class, filter);
       
        return getAuxiliaryService(ctx, extension);
    }
    
    


    /**
     * Indicates whether or not the given subscriber has any calling group
     * services.
     *
     * @param context The operating context.
     * @param subscriberIdentifier The identifier of the subscriber for which to
     * determine if there are any CallingGroup services.
     * 
     * @return True if the subscriber has any calling group services; false
     * otherwise.
     */
    public static boolean hasCallingGroupService(
        final Context context,
        final String subscriberIdentifier)
        throws HomeException
    {
        final Collection associations =
            SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(
                context,
                subscriberIdentifier);

        final Collection services =
            SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(
                context,
                associations,
                null);

        final Iterator serviceIterator = services.iterator();
        boolean found = false;
        while (serviceIterator.hasNext())
        {
            final AuxiliaryService service =
                (AuxiliaryService)serviceIterator.next();

            if (service.getType() == AuxiliaryServiceTypeEnum.CallingGroup)
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Sets the Friends and Family functionality in ECP on or off.
     *
     * @param subscriberIdentifier The subscriber for which to enable functions.
     * @param enabled True if the Friends and Family functions should be turned
     * on; false otherwise.
     *
     * @exception HomeException Thrown if the update fails.
     */
    public static void setFriendsAndFamilyEnabled(
        final Context ctx,
        final String subscriberIdentifier,
        final boolean enabled)
        throws HomeException
    {
        final AppEcpClient client =
            (AppEcpClient)ctx.get(AppEcpClient.class);

        final Home home = (Home)ctx.get(SubscriberHome.class);
        final Subscriber subscriber =
            (Subscriber)home.find(ctx,subscriberIdentifier);

        short result = 0;

        result = client.setFriendsAndFamilyEnabled(subscriber.getMSISDN(), enabled);

        if (result != 0)
        {
        	new MajorLogMsg( CallingGroupSupport.class,
                "Failed to update Friends and Family information in ECP for subscriber "
                + subscriber.getId() + 
                ". Friends and family returned result code = " + result, null).log(ctx);
        }
    }
    
    /**
     * Indicates whether or not the given subscriber has any calling group
     * services.
     *
     * @param context The operating context.
     * @param subscriber The  subscriber for which to determine if there
     * are any CallingGroup services.
     * 
     * @return True if the subscriber has any calling group services; false
     * otherwise.
     */
    public static boolean hasCallingGroupService(
        final Context context,
        final Subscriber subscriber)
        throws HomeException
    {
        final String identifier = subscriber.getId();
        return hasCallingGroupService(context, identifier);
    }


    /**
     * Indicates whether or not the given subscriber has any provisioned calling group
     * services.
     *
     * @param context The operating context.
     * @param subscriberIdentifier The identifier of the subscriber for which to
     * determine if there are any provisioned CallingGroup services.
     * 
     * @return True if the subscriber has any provisioned calling group services; false
     * otherwise.
     */
    public static boolean hasProvisionedCallingGroupService(
        final Context context,
        final String subscriberIdentifier)
        throws HomeException
    {
        final Collection associations =
            SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(
                context,
                subscriberIdentifier);

        // Get all provisioned associations for this subscriber.
        final Collection provisionedAssociations = new ArrayList();
        final Iterator itr = associations.iterator();
        while (itr.hasNext())
        {
            final SubscriberAuxiliaryService association =
                (SubscriberAuxiliaryService) itr.next();

            if (association.isProvisioned())
            {
                provisionedAssociations.add(association);
            }
        }

        final Collection services =
            SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(
                context,
                provisionedAssociations,
                null);

        final Iterator serviceIterator = services.iterator();
        boolean found = false;
        while (serviceIterator.hasNext())
        {
            final AuxiliaryService service =
                (AuxiliaryService) serviceIterator.next();

            if (service.getType() == AuxiliaryServiceTypeEnum.CallingGroup)
            {
                found = true;
                break;
            }
        }

        return found;
    }


    /**
     * Indicates whether or not the given subscriber has any provisioned calling group
     * services.
     *
     * @param context The operating context.
     * @param subscriber The subscriber for which to determine if there
     * are any provisioned CallingGroup services.
     * 
     * @return True if the subscriber has any provisioned calling group services; false
     * otherwise.
     */
    public static boolean hasProvisionedCallingGroupService(
        final Context context,
        final Subscriber subscriber)
        throws HomeException
    {
        final String identifier = subscriber.getId();
        return hasProvisionedCallingGroupService(context, identifier);
    }

} // class
