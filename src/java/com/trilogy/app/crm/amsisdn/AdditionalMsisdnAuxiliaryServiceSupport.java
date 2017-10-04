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

package com.trilogy.app.crm.amsisdn;

import java.util.Collection;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.osa.ecp.provision.ErrorCode;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Support class for Additional MSISDN Auxiliary Service feature.
 *
 * @author cindy.wong@redknee.com
 * @since Jul 15, 2007
 */
public final class AdditionalMsisdnAuxiliaryServiceSupport
{


    /**
     * Creates a new <code>AdditionalMsisdnAuxiliaryServiceSupport</code> instance. This
     * method is made private to prevent instantiation of utility class.
     */
    private AdditionalMsisdnAuxiliaryServiceSupport()
    {
        // empty
    }


    /**
     * Determines whether the feature is enabled.
     *
     * @param context
     *            The operating context.
     * @return Returns <code>true</code> if the feature is enabled, <code>false</code>
     *         otherwise.
     */
    public static boolean isAdditionalMsisdnEnabled(final Context context)
    {
        final LicenseMgr licenseManager = (LicenseMgr) context.get(LicenseMgr.class);
        return licenseManager.isLicensed(context, LicenseConstants.ADDITIONAL_MOBILE_NUMBER);
    }


    /**
     * Returns the additional MSISDN object associated with the provided
     * subscriber-auxiliary service association.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service association.
     * @return If the association is of an additional MSISDN auxiliary service, returns
     *         the additional MSISDN; otherwise, return <code>null</code>.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static Msisdn getAMsisdn(final Context context, final SubscriberAuxiliaryService association)
        throws HomeException
    {
        if (association == null)
        {
            throw new IllegalArgumentException("System error: Association must not be null");
        }
        else if (context == null)
        {
            throw new IllegalArgumentException("System error: Context must not be null");
        }

        final Home msisdnHome = (Home) context.get(MsisdnHome.class);
        if (msisdnHome == null)
        {
            throw new HomeException("System error: MsisdnHome not found in context");
        }
        
        Subscriber sub = SubscriberSupport.getSubscriber(context, association.getSubscriberIdentifier());
        
        final And and = new And();
        and.add(new EQ(MsisdnXInfo.SUB_AUX_SVC_ID, Long.valueOf(association.getIdentifier())));
        and.add(new EQ(MsisdnXInfo.BAN, sub.getBAN()));
        and.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.IN_USE));
        final Collection<Msisdn> msisdns = msisdnHome.select(context, and);
        if (msisdns.size() == 0)
        {
            final HomeException exception = new HomeException("Cannot find any MSISDN associated with this subscriber");
            LogSupport.minor(context, AdditionalMsisdnAuxiliaryServiceSupport.class,
                "Cannot update aMSISDN association " + association.getIdentifier()
                    + ": no MSISDN found for subscriber association " + association.getIdentifier(), exception);
            throw exception;
        }
        else if (msisdns.size() > 1)
        {
            LogSupport.info(context, AdditionalMsisdnAuxiliaryServiceSupport.class,
                "More than one MSISDN is associated with SubscriberAuxiliaryService " + association.getIdentifier()
                    + "; using the first one found");
        }
        return msisdns.iterator().next();
    }


    /**
     * Returns the additional MSISDN object associated with the provided
     * subscriber-auxiliary service association.
     *
     * @param context
     *            The operating context.
     * @param identifier
     *            The subscriber-auxiliary service association.
     * @return If the association is of an additional MSISDN auxiliary service, returns
     *         the additional MSISDN; otherwise, return <code>null</code>.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static Msisdn getAMsisdn(final Context context, final long identifier) throws HomeException
    {
        final SubscriberAuxiliaryService association = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(
            context, identifier);
        if (association == null)
        {
            throw new IllegalArgumentException("SubscriberAuxiliaryService " + identifier + " does not exist in home");
        }
        return getAMsisdn(context, association);
    }


    /**
     * Returns the additional MSISDN object associated with the provided subscriber and
     * auxiliary service.
     *
     * @param context
     *            The operating context.
     * @param auxiliaryServiceId
     *            The auxiliary service ID.
     * @param subscriberId
     *            ID of the subscriber to look up.
     * @return If the association is of an additional MSISDN auxiliary service, returns
     *         the additional MSISDN; otherwise, return <code>null</code>.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static Msisdn getAMsisdn(final Context context, final long auxiliaryServiceId, final String subscriberId)
        throws HomeException
    {
        final SubscriberAuxiliaryService association = SubscriberAuxiliaryServiceSupport
            .getSubscriberAuxiliaryServicesBySubIdAndSvcId(context, subscriberId, auxiliaryServiceId);

        if (association == null)
        {
            throw new IllegalArgumentException("No association exists between auxiliary service " + auxiliaryServiceId
                + " and subscriber " + subscriberId);
        }
        return getAMsisdn(context, association);
    }


    /**
     * Retrieve the appropriate aMSISDN.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service association.
     * @param service
     *            The auxiliary service.
     * @param subscriber
     *            The subscriber.
     * @return The aMSISDN of this association.
     * @throws HomeException
     *             Thrown if there are problems looking up the MSISDN.
     */
    public static Msisdn getAMsisdn(final Context ctx, final SubscriberAuxiliaryService association,
        final AuxiliaryService service, final Subscriber subscriber) throws HomeException
    {
        Msisdn aMsisdn = null;
        if (association != null && association.getAMsisdn(ctx) != null
            && !association.getAMsisdn(ctx).equals(SubscriberAuxiliaryService.DEFAULT_AMSISDN))
        {
            aMsisdn = MsisdnSupport.getMsisdn(ctx, association.getAMsisdn(ctx));
            if (aMsisdn == null)
            {
                throw new HomeException("Data integrity: mobile number " + association.getAMsisdn(ctx)
                    + " does not exist in home");
            }
        }
        else if (service != null && subscriber != null)
        {
            aMsisdn = getAMsisdn(ctx, service.getIdentifier(), subscriber.getId());
        }
        return aMsisdn;
    }


    /**
     * Retrieves the appropriate bearer type for the association.
     *
     * @param ctx the operating context
     * @param association
     *            The subscriber-auxiliary service association.
     * @param service
     *            The auxiliary service.
     *
     * @return The bearer type ID of this association.
     */
    public static String getBearerType(Context ctx, final SubscriberAuxiliaryService association, final AuxiliaryService service)
    {
        String bearerType = null;
        if (association != null && association.getBearerType() != null
            && !association.getBearerType().equals(SubscriberAuxiliaryService.DEFAULT_BEARERTYPE))
        {
            bearerType = association.getBearerType(ctx);
        }
        else if (service != null)
        {
            AddMsisdnAuxSvcExtension extension = ExtensionSupportHelper.get(ctx).getExtension(ctx,service, AddMsisdnAuxSvcExtension.class);
            if (extension!=null)
            {
                bearerType = extension.getBearerType();
            }
            else
            {
                LogSupport.minor(ctx, AdditionalMsisdnAuxiliaryServiceSupport.class,
                        "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }
        }
        return bearerType;
    }


    /**
     * Creates a home exception based on the result code from ECP.
     *
     * @param aMsisdn
     *            The aMsisdn in action.
     * @param subscriber
     *            The subscriber in question.
     * @param resultCode
     *            The result code from ECP.
     * @return The corresponding error message for, or <code>null</code> if none is
     *         required.
     */
    private static String getEcpErrorMessage(final String aMsisdn, final Subscriber subscriber, final short resultCode)
    {
        final StringBuilder sb = new StringBuilder();
        switch (resultCode)
        {
            case ErrorCode.SUCCESS:
                break;
            case ErrorCode.MAIN_SUBSCRIBER_NOT_FOUND:
                sb.append("Subscriber MSISDN ");
                sb.append(subscriber.getMSISDN());
                sb.append(" is not yet provisioned on ECP");
                break;
            case ErrorCode.AMSISDN_EDIT_NOT_PERMITTED:
                sb.append("Additional MSISDN editing is not permitted");
                break;
            case ErrorCode.AMSISDN_LIMIT_EXCEEDED:
                sb.append("Additional MSISDN limit exceeded for subscriber ");
                sb.append(subscriber.getMSISDN());
                break;
            case ErrorCode.INTERNAL_ERROR:
                sb.append("ECP internal error");
                break;
            case ErrorCode.INVALID_PARAMETER:
                sb.append("Invalid parameter");
                break;
            case ErrorCode.SQL_ERROR:
                sb.append("ECP SQL error");
                break;
            case ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST:
                sb.append("Additional MSISDN ");
                sb.append(aMsisdn);
                sb.append(" already exist");
                break;
            case ErrorCode.SUBSCRIBER_NOT_FOUND:
                sb.append("Subscriber MSISDN ");
                sb.append(subscriber.getMSISDN());
                sb.append(" not found");
                break;
            case ErrorCode.UPDATE_NOT_ALLOWED:
                sb.append("Update not allowed");
                break;
            default:
                sb.append("Unknown ECP error");
        }
        sb.append(" [ECP result code = ");
        sb.append(resultCode);
        sb.append("]");
        return sb.toString();
    }


    /**
     * Updates the subscriber ID of a MSISDN during subscriber move.
     *
     * @param context
     *            The operating context.
     * @param oldSubscriberId
     *            The old subscriber ID.
     * @param newSubscriberId
     *            The new subscriber ID.
     * @throws HomeException
     *             Thrown if there are problems updating the MSISDN.
     */
    public static void updateAMsisdnSubscriberId(final Context context, final String oldSubscriberId,
        final String newSubscriberId) throws HomeException
    {
    	Subscriber sub = SubscriberSupport.getSubscriber(context, oldSubscriberId);
        Collection<com.redknee.app.crm.bean.core.Msisdn> msisdns = MsisdnSupport.getAcquiredMsisdn(context, sub.getBAN());
        if (msisdns != null)
        {
            for (final Msisdn msisdn : msisdns)
            {
            	if( msisdn.isAMsisdn())
            	{
            		MsisdnManagement.deassociateMsisdnWithSubscription(context, msisdn.getMsisdn(), oldSubscriberId, "aMsisdn");
            		MsisdnManagement.associateMsisdnWithSubscription(context, msisdn.getMsisdn(), newSubscriberId, "aMsisdn");
            	}
            }
        }
    }


    /**
     * Re-add all of the subscriber's AMSISDNs to ECP.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @throws HomeException
     *             Thrown if there are problems re-adding the AMSISDNs to ECP.
     */
    public static void syncSubscriberAMsisdnWithEcp(final Context context, final Subscriber subscriber)
        throws HomeException
    {
        if (isAdditionalMsisdnEnabled(context))
        {
            final Home home = (Home) context.get(MsisdnHome.class);
            if (home == null)
            {
                throw new HomeException("MSISDN home does not exist in context");
            }

            final AppEcpClient client = (AppEcpClient) context.get(AppEcpClient.class);
            if (client == null)
            {
                throw new HomeException("ECP provisioning client does not exist in context");
            }
            
            Collection<Msisdn> msisdns = MsisdnSupport.getAcquiredMsisdn(context, subscriber.getBAN());
            if (msisdns != null)
            {
                for (final Msisdn msisdn : msisdns)
                {
                	if( msisdn.isAMsisdn())
                	{
	                    final short result = client.addAMsisdn(subscriber.getMSISDN(), msisdn.getMsisdn());
	                    if (result != ErrorCode.SUCCESS)
	                    {
	                        final String error = getEcpErrorMessage(msisdn.getMsisdn(), subscriber, result);
	                        LogSupport.info(context, AdditionalMsisdnAuxiliaryServiceSupport.class,
	                            "ECP provisioning failed: " + error);
	                        throw new HomeException("Cannot synchronize AMSISDN " + msisdn.getMsisdn() + " of subscription "
	                            + subscriber.getId() + " with URCS Voice: " + error);
	                    }
                	}
                }
            }
        }
    }

    /**
     * Converts all the AMSISDNs of the subscriber to postpaid.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @throws HomeException
     *             Thrown if there are problems converting the MSISDNs to postpaid.
     */
    public static void convertMsisdnToPostpaid(final Context context, final Subscriber subscriber) throws HomeException
    {
        MsisdnManagement.convertMsisdn(context, subscriber.getMSISDN(), subscriber.getBAN(), SubscriberTypeEnum.POSTPAID,null, "CRM - AMSISDN Convert to Postpaid");
    }


    /**
     * Converts all the AMSISDNs of the subscriber to prepaid.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber in question.
     * @throws HomeException
     *             Thrown if there are problems converting the MSISDNs to prepaid.
     */
    public static void convertMsisdnToPrepaid(final Context context, final Subscriber subscriber) throws HomeException
    {
        MsisdnManagement.convertMsisdn(context, subscriber.getMSISDN(), subscriber.getBAN(), SubscriberTypeEnum.PREPAID, null, "CRM - AMSISDN Convert to Prepaid");
    }
}
