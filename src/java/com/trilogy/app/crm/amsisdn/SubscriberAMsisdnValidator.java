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
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.*;


/**
 * Validator class for subscriber additional MSISDNs.
 *
 * @author cindy.wong@redknee.com
 * @since Jul 21, 2007
 */
public class SubscriberAMsisdnValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberAMsisdnValidator</code>.
     */
    protected SubscriberAMsisdnValidator()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>SubscriberAMsisdnValidator</code>.
     *
     * @return An instance of <code>SubscriberAMsisdnValidator</code>.
     */
    public static SubscriberAMsisdnValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberAMsisdnValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object)
    {
        if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
        {
            final Subscriber subscriber = (Subscriber) object;

            /*
             * Do not check subscriber AMSISDN when the subscriber is going into inactive.
             */
            if (!isInactive(context, subscriber))
            {
                final Collection<SubscriberAuxiliaryService> auxiliaryServices = subscriber
                    .getAuxiliaryServices(context);
                final Set<String> aMsisdns = new HashSet<String>();
                final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

                final boolean hasVoiceService = validateHasVoiceService(context, subscriber, exceptions);

                // add main MSISDNs first
                addMainMsisdns(subscriber, aMsisdns);

                for (final SubscriberAuxiliaryService association : auxiliaryServices)
                {
                    if (association.getType(context) == AuxiliaryServiceTypeEnum.AdditionalMsisdn)
                    {
                        if (!hasVoiceService)
                        {
                            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.AUXILIARY_SERVICES,
                                "Additional MSISDN auxiliary service requires a valid voice service"));
                            break;
                        }
                        validateSubscriberAuxiliaryService(context, subscriber, association, aMsisdns, exceptions);
                    }
                }
                exceptions.throwAll();
            }
        }
    }


    /**
     * Verifies the subscriber has a voice service. Without a voice service, the
     * subscriber will not have a profile on ECP, thus no AMSISDN can be created.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being validated.
     * @param exceptions
     *            Exceptions to be thrown. Any exceptions which should be thrown would be
     *            added to this exception.
     * @return Returns <code>true</code> if the subscriber has a voice service,
     *         <code>false</code> otherwise.
     */
    protected boolean validateHasVoiceService(final Context context, final Subscriber subscriber,
        final CompoundIllegalStateException exceptions)
    {
        final Collection<ServiceFee2ID> services = subscriber.getServices();
        boolean hasVoiceService = false;
        final Home serviceHome = (Home) context.get(ServiceHome.class);
        if (serviceHome == null)
        {
            exceptions.thrown(new IllegalStateException("Service home does not exist in context!"));
            return false;
        }

        if (services != null)
        {
            for (final ServiceFee2ID obj : services)
            {
                try
                {
                    final Service service = (Service) serviceHome.find(context, obj.getServiceId());
                    if (service == null)
                    {
                        exceptions.thrown(new IllegalStateException("Subscriber service " + obj.getServiceId() + " does not exist"));
                    }
                    else
                    {
                        if (service.getType() == ServiceTypeEnum.VOICE)
                        {
                            hasVoiceService = true;
                            break;
                        }
                    }
                }
                catch (final HomeException exception)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(exception.getClass().getSimpleName());
                        sb.append(" caught in ");
                        sb.append("SubscriberAMsisdnValidator.validateHasEcpProfile(): ");
                        if (exception.getMessage() != null)
                        {
                            sb.append(exception.getMessage());
                        }
                        LogSupport.debug(context, this, sb.toString(), exception);
                    }
                    exceptions.thrown(exception);
                }
            }
        }
        return hasVoiceService;
    }


    /**
     * Validates an additional MSISDN subscriber auxiliary service.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            Subscriber being validated.
     * @param association
     *            The subscriber-auxiliary service association being validated.
     * @param aMsisdns
     *            The set of MSISDNs this subscriber has. If the additional MSISDN in this
     *            association is valid, the aMSISDN is added to this set.
     * @param exceptions
     *            Exceptions to be thrown. Any exceptions which should be thrown would be
     *            added to this exception.
     */
    protected void validateSubscriberAuxiliaryService(final Context context, final Subscriber subscriber,
        final SubscriberAuxiliaryService association, final Set<String> aMsisdns,
        final CompoundIllegalStateException exceptions)
    {
        final String aMsisdn = association.getAMsisdn(context);
        AuxiliaryService service = null;
        try
        {
            service = association.getAuxiliaryService(context);
        }
        catch (final HomeException exception)
        {
            new DebugLogMsg(this, "Exception caught while retrieving auxiliary service "
                + association.getAuxiliaryServiceIdentifier(), exception).log(context);
            exceptions.thrown(exception);
        }

        if (service == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.AUXILIARY_SERVICES,
                "Auxiliary service " + association.getIdentifier() + " does not exist"));
        }
        else
        {
            final StringBuilder serviceName = new StringBuilder();
            serviceName.append("Auxiliary Service \"");
            serviceName.append(service.getName());
            serviceName.append('"');
            if (aMsisdn == null || aMsisdn.length() == 0)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(serviceName.toString(),
                    "Additional MSISDN must not be null"));
            }
            else
            {
                // find the old association
                final SubscriberAuxiliaryService oldAssociation = SubscriberAuxiliaryServiceSupport
                    .getSubscriberAuxiliaryServicesBySubIdAndSvcId(context, association.getSubscriberIdentifier(),
                        association.getAuxiliaryServiceIdentifier());
                long id = association.getIdentifier();
                if (oldAssociation != null)
                {
                    id = oldAssociation.getIdentifier();
                }
                final Exception thrown = validateAdditionalMsisdn(context, aMsisdn, id, subscriber, serviceName
                    .toString(), aMsisdns);
                if (thrown == null)
                {
                    aMsisdns.add(aMsisdn);
                }
                else
                {
                    exceptions.thrown(thrown);
                }
            }
        }
    }


    /**
     * Validates additional MSISDN auxiliary service.
     *
     * @param context
     *            The operating context.
     * @param aMsisdn
     *            The additional MSISDN being assigned.
     * @param associationId
     *            ID of the subscriber-auxiliary service association.
     * @param subscriber
     *            ID of the subscriber being updated.
     * @param serviceName
     *            The additional MSISDN auxiliary service being validated.
     * @param aMsisdns
     *            The current set of MSISDNs this subscriber currently has.
     * @return The exception to be thrown, or <code>null</code> if the aMSISDN is valid.
     */
    protected Exception validateAdditionalMsisdn(final Context context, final String aMsisdn, final long associationId,
        final Subscriber subscriber, final String serviceName, final Set<String> aMsisdns)
    {
        Exception result = null;
        final StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Additional MSISDN ");
        errorMessage.append(aMsisdn);
        if (aMsisdns.contains(aMsisdn))
        {
            errorMessage.append(" is already used by this subscriber for other service");
            result = new IllegalPropertyArgumentException(serviceName, errorMessage.toString());
        }
        else
        {
            try
            {
                final Msisdn msisdn = MsisdnSupport.getMsisdn(context, aMsisdn);
                if (msisdn == null)
                {
                    errorMessage.append(" does not exist in database");
                    result = new IllegalPropertyArgumentException(serviceName, errorMessage.toString());
                }
                else
                {
                    boolean isAssignable = false;
                    isAssignable = isMsisdnAssignable(associationId, subscriber, msisdn);

                    if (!isAssignable)
                    {
                        errorMessage.append(" is not available for assignment");
                        result = new IllegalPropertyArgumentException(serviceName, errorMessage.toString());
                    }
                }
            }
            catch (final HomeException exception)
            {
                errorMessage.append(" cannot be found in database (");
                errorMessage.append(exception.getClass().getSimpleName());
                errorMessage.append(" caught");
                if (exception.getMessage() != null)
                {
                    errorMessage.append(": ");
                    errorMessage.append(exception.getMessage());
                }
                errorMessage.append(")");
                result = new IllegalPropertyArgumentException(serviceName, errorMessage.toString());
                new DebugLogMsg(this, exception.getClass().getSimpleName()
                    + " occurred in SubscriberAMsisdnValidator.validate(): " + exception.getMessage(), exception)
                    .log(context);
            }
        }
        return result;
    }


    /**
     * Determines if the MSISDN can be assigned to the subscriber as AMSISDN.
     *
     * @param associationId
     *            Subscriber auxiliary service association ID of the AMSISDN auxiliary
     *            service.
     * @param subscriber
     *            Subscriber ID.
     * @param msisdn
     *            MSISDN to be assigned.
     * @return Returns <code>true</code> if the MSISDN is available for the subscriber
     *         to claim, <code>false</code> otherwise.
     */
    private boolean isMsisdnAssignable(final long associationId, final Subscriber subscriber, final Msisdn msisdn)
    {
        boolean isAssignable = false;
        if (SafetyUtil.safeEquals(msisdn.getState(), MsisdnStateEnum.AVAILABLE))
        {
            isAssignable = true;
        }

        /*
         * TT 7100400071: Allow held MSISDNs to be assigned to the same subscriber holding
         * it.
         */
        else if (SafetyUtil.safeEquals(msisdn.getSubscriberID(), subscriber.getId()))
        {
            if (SafetyUtil.safeEquals(msisdn.getState(), MsisdnStateEnum.IN_USE))
            {
                isAssignable = msisdn.getAMsisdn() && msisdn.getSubAuxSvcId() == associationId;
            }
            else
            {
                isAssignable = true;
            }
        }

        /*
         * TT 7082700032: Check SPID, technology, and subscriber type.
         */
        if (msisdn.getTechnology() != subscriber.getTechnology())
        {
            isAssignable = false;
        }
        else if (msisdn.getSubscriberType() != subscriber.getSubscriberType())
        {
            isAssignable = false;
        }
        else if (msisdn.getSpid() != subscriber.getSpid())
        {
            isAssignable = false;
        }

        return isAssignable;
    }


    /**
     * Adds the main MSISDNs to the tested set.
     *
     * @param subscriber
     *            Subscriber being processed.
     * @param aMsisdns
     *            Set of aMSISDNs.
     */
    protected void addMainMsisdns(final Subscriber subscriber, final Set<String> aMsisdns)
    {
        if (subscriber.getMSISDN() != null && subscriber.getMSISDN().length() > 0)
        {
            aMsisdns.add(subscriber.getMSISDN());
        }
        if (subscriber.getFaxMSISDN() != null && subscriber.getFaxMSISDN().length() > 0)
        {
            aMsisdns.add(subscriber.getFaxMSISDN());
        }
        if (subscriber.getDataMSISDN() != null && subscriber.getDataMSISDN().length() > 0)
        {
            aMsisdns.add(subscriber.getDataMSISDN());
        }
    }


    /**
     * Determines whether the subscriber is considered inactive.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being examined.
     * @return Returns <code>true</code> if the subscriber is inactive, or the account
     *         it belongs to is inactive.
     */
    protected boolean isInactive(final Context context, final Subscriber subscriber)
    {
        boolean result = false;
        if (subscriber.getState() == SubscriberStateEnum.INACTIVE)
        {
            result = true;
        }
        else
        {
            Account account = (Account) context.get(Account.class);

            if (account != null && account.getState() == AccountStateEnum.INACTIVE)
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Singleton instance.
     */
    private static SubscriberAMsisdnValidator instance;
}
