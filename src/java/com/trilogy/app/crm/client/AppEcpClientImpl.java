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
package com.trilogy.app.crm.client;

import static com.redknee.app.crm.client.CorbaClientTrapIdDef.ECP_PROV_SVC_DOWN;
import static com.redknee.app.crm.client.CorbaClientTrapIdDef.ECP_PROV_SVC_UP;

import java.util.Arrays;

import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.osa.ecp.provision.ErrorCode;
import com.trilogy.app.osa.ecp.provision.ServiceID;
import com.trilogy.app.osa.ecp.provision.ServiceParameter;
import com.trilogy.app.osa.ecp.provision.SubProvision;
import com.trilogy.app.osa.ecp.provision.SubsProfile;
import com.trilogy.app.osa.ecp.provision.SubsProfileHolder;
import com.trilogy.app.osa.ecp.provision.SubProvisionPackage.ProvisioningServiceException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * ECP provisioning client.
 *
 * @author gary.anderson@redknee.com
 * 
 * Support clustered corba client
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 25, 2009 
 */
public class AppEcpClientImpl extends AbstractCrmClient<SubProvision> implements AppEcpClient
{

    /**
     * PM module name.
     */
    private static final String PM_MODULE = AppEcpClientImpl.class.getName();
    private static final String SERVICE_DESC = "CORBA client for ECP services";

    /**
     * Create a new instance of <code>AppEcpClient</code>.
     *
     * @param ctx
     *            The operating context.
     */
    public AppEcpClientImpl(final Context ctx)
    {
        super(ctx, SERVICE_NAME, SERVICE_DESC, SubProvision.class, ECP_PROV_SVC_DOWN, ECP_PROV_SVC_UP);
    }
    
    /**
     * {@inheritDoc}
     */
    public int addSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount, final String billingNumber)
    {
        SubsProfile profile = null;
        profile = subsProfile(msisdn, spid, imsi, currency, ratePlan, expiry, classOfService, state, pin, language, 0,
            timeRegionId, groupAccount, billingNumber);

        return addSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public int addSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount)
    {
        SubsProfile profile = null;
        profile = subsProfile(msisdn, spid, imsi, currency, ratePlan, expiry, classOfService, state, pin, language, 0,
            timeRegionId, groupAccount, "");

        return addSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public int addSubscriber(final SubsProfile profile)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "addSubscriber()");

        try
        {
            int result = ExternalAppSupport.NO_CONNECTION;
            final SubProvision service = getService();

            if (service != null)
            {
                try
                {
                    result = service.add(profile);
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                catch (final ProvisioningServiceException e)
                {
                    new MinorLogMsg(this, "Fail to add new subscriber " + profile.msisdn + ". Attempt update", null)
                        .log(getContext());

                    // create failed. Attempt to update instead
                    result = updateSubscriber(profile);
                }
                catch(final Throwable t)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }

                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, "result of addSubscriber is " + result + " with profile.state "
                        + profile.state, null).log(getContext());
                }
            }
            return result;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

    }


    /**
     * {@inheritDoc}
     */
    public int updateSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount, final String billingNumber)
    {

        SubsProfile profile = null;
        profile = subsProfile(msisdn, spid, imsi, currency, ratePlan, expiry, classOfService, state, pin, language, 0,
            timeRegionId, groupAccount, billingNumber);

        return updateSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public int updateSubscriber(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final String timeRegionId, final String groupAccount)
    {

        SubsProfile profile = null;
        profile = subsProfile(msisdn, spid, imsi, currency, ratePlan, expiry, classOfService, state, pin, language, 0,
            timeRegionId, groupAccount, "");

        return updateSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public int updateSubscriber(final SubsProfile profile)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateSubscriber()");

        try
        {
            int result = ExternalAppSupport.NO_CONNECTION;
            final SubProvision service = getService();
            if (service != null)
            {
                try
                {
                    result = service.edit(profile);

                    if (result == ErrorCode.SUBSCRIBER_NOT_FOUND)
                    {
                        if (LogSupport.isDebugEnabled(getContext()))
                        {
                            new DebugLogMsg(this, "Subscriber " + profile.msisdn
                                + " not found during update; trying to add", null).log(getContext());
                        }

                        result = service.add(profile);
                    }
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                catch (final ProvisioningServiceException e)
                {
                    new MinorLogMsg(this, "Fail to modify subscriber " + profile.msisdn, e).log(getContext());
                    result = ExternalAppSupport.REMOTE_EXCEPTION;
                }
                catch(final Throwable t)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }

                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, "result of updateSubscriber is " + result + " with profile.state "
                        + profile.state, null).log(getContext());
                }
            }
            return result;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

    }


    /**
     * {@inheritDoc}
     */
    public short addAMsisdn(final String msisdn, final String aMsisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "addAMsisdn()");

        try
        {
            short result = ExternalAppSupport.NO_CONNECTION;
            final SubProvision service = getService();

            if (service != null)
            {
                try
                {
                    result = service.addAMsisdn(aMsisdn, msisdn);
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                catch (final ProvisioningServiceException e)
                {
                    new MinorLogMsg(this, "Fail to add aMSISDN " + aMsisdn + " to subscriber " + msisdn, e)
                        .log(getContext());
                    result = ExternalAppSupport.REMOTE_EXCEPTION;
                }
                catch(final Throwable t)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, "result of addAMsisdn is " + result, null).log(getContext());
                }
            }
            return result;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

    }


    /**
     * {@inheritDoc}
     */
    public int updateSubscriberState(final String msisdn, final int state)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateSubscriberState()");

        int result = ExternalAppSupport.NO_CONNECTION;
        try
        {
            final SubsProfile profile = getSubsProfile(msisdn);

            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.state = state;
                result = updateSubscriber(profile);
            }
        }
        catch (final IllegalStateException exception)
        {
            result = ExternalAppSupport.NO_CONNECTION;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public int updateGroupAccount(final String msisdn, final String groupAccount)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateGroupAccount()");
        int result = ExternalAppSupport.NO_CONNECTION;
        try
        {
            final SubsProfile profile = getSubsProfile(msisdn);

            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.groupAccount = groupAccount;

                result = updateSubscriber(profile);
            }
        }
        catch (final IllegalStateException exception)
        {
            result = ExternalAppSupport.NO_CONNECTION;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public int updateRatePlan(final String msisdn, final int ratePlan)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateRatePlan()");

        int result = ExternalAppSupport.NO_CONNECTION;
        try
        {
            final SubsProfile profile = getSubsProfile(msisdn);

            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.ratePlan = ratePlan;

                result = updateSubscriber(profile);
            }
        }
        catch (final IllegalStateException exception)
        {
            result = ExternalAppSupport.NO_CONNECTION;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public int updateImsi(final String msisdn, final String imsi)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateImsi()");

        int result = ExternalAppSupport.NO_CONNECTION;
        try
        {
            final SubsProfile profile = getSubsProfile(msisdn);

            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.imsi = imsi;

                result = updateSubscriber(profile);
            }
        }
        catch (final IllegalStateException exception)
        {
            result = ExternalAppSupport.NO_CONNECTION;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public int updateClassOfService(final String msisdn, final int classOfService)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateClassOfService()");

        int result;
        try
        {
            final SubsProfile profile = getSubsProfile(msisdn);

            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.classOfService = classOfService;

                result = updateSubscriber(profile);
            }
        }
        catch (final IllegalStateException exception)
        {
            result = ExternalAppSupport.NO_CONNECTION;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public short setFriendsAndFamilyEnabled(final String msisdn, final boolean enabled)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "enableFriendsAndFamily()");

        short result = ExternalAppSupport.NO_CONNECTION;

        try
        {
            final SubProvision service = getService();

            if (service != null)
            {
                try
                {
                    if (enabled)
                    {
                        result = service.subFF_Enable(msisdn);
                    }
                    else
                    {
                        result = service.subFF_Disable(msisdn);
                    }
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                catch (final ProvisioningServiceException e)
                {
                    new MinorLogMsg(this, "Fail to provision Friends and Family information for msisdn " + msisdn, e)
                        .log(getContext());

                    result = ExternalAppSupport.REMOTE_EXCEPTION;
                }
                catch(final Throwable t)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
            }
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public int deleteSubscriber(final String msisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteSubscriber()");
        try
        {
            int result = ExternalAppSupport.NO_CONNECTION;
            final SubProvision service = getService();

            if (service != null)
            {
                try
                {
                    result = service.delete(msisdn);

                    if (result == ErrorCode.SUBSCRIBER_NOT_FOUND)
                    {
                        if (LogSupport.isDebugEnabled(getContext()))
                        {
                            new DebugLogMsg(this, "Subscriber not found in deleteSubscriber: " + msisdn, null)
                                .log(getContext());
                        }

                        // it's not really an error
                        result = 0;
                    }
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                catch (final ProvisioningServiceException e)
                {
                    new MinorLogMsg(this, "Fail to delete msisdn " + msisdn, e).log(getContext());
                    result = ExternalAppSupport.REMOTE_EXCEPTION;
                }
                catch(final Throwable t)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
            }
            return result;
        }
        finally
        {
            pmLogMsg.log(getContext());
        }
    }


    /**
     * {@inheritDoc}
     */
    public SubsProfile getSubsProfile(final String msisdn)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSubsProfile()");
        try
        {
            final SubProvision service = getService();
            final SubsProfileHolder profileHolder = new SubsProfileHolder();
            short result = ErrorCode.SUCCESS;
            if (service == null)
            {
                throw new IllegalStateException("Failed to get ECP service connection.");
            }

            try
            {
                result = service.query(msisdn, profileHolder);
            }
            catch (final org.omg.CORBA.COMM_FAILURE commFail)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
                throw new IllegalStateException("ECP communication failure.");
            }
            catch (final ProvisioningServiceException e)
            {
                new MinorLogMsg(this, "Fail to retrieve subscriber " + msisdn, e).log(getContext());
                profileHolder.value = null;
            }
            catch(final Throwable t)
            {
                result = ExternalAppSupport.COMMUNICATION_FAILURE;
                throw new IllegalStateException("ECP communication failure.");
            }

            final SubsProfile profile;
            if (result == ErrorCode.SUCCESS)
            {
                profile = profileHolder.value;
            }
            else
            {
                profile = null;
            }

            return profile;
        }
        finally
        {
            pmLogMsg.log(getContext());

        }
    }


    /**
     * {@inheritDoc}
     */
    public short enableHomezone(final String msisdn)
    {
        // dont know about serviceId right now
        final ServiceParameter[] paramArr = new ServiceParameter[1];
        final ServiceParameter param = new ServiceParameter(ServiceID.HOMEZONE, true);
        paramArr[0] = param;
        return updateServices(msisdn, paramArr);
    }


    /**
     * {@inheritDoc}
     */
    public short disableHomezone(final String msisdn)
    {
        // dont know about serviceId right now
        final ServiceParameter[] paramArr = new ServiceParameter[1];
        final ServiceParameter param = new ServiceParameter(ServiceID.HOMEZONE, false);
        paramArr[0] = param;
        return updateServices(msisdn, paramArr);
    }


    /**
     * {@inheritDoc}
     */
    public short updateServices(final String msisdn, final ServiceParameter[] paramSet)
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "updateServices()");

        short result = ExternalAppSupport.NO_CONNECTION;

        try
        {
            if (paramSet == null)
            {
                throw new IllegalStateException("The parameter set passed to update the services to ECP is empty");
            }

            final SubProvision service = getService();

            if (service != null)
            {
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    // specifying the initial length of the StringBuilder's buffer
                    final StringBuilder msg = new StringBuilder(70 + 15 * paramSet.length);
                    msg.append("Update services parameters passed to ECP:: Msisdn:");
                    msg.append(msisdn);
                    msg.append(" services:[");
                    for (final ServiceParameter element : paramSet)
                    {
                        msg.append('(');
                        msg.append(element.serviceID);
                        msg.append(',');
                        msg.append(element.value);
                        msg.append(')');
                    }
                    msg.append(']');

                    new DebugLogMsg(this, msg.toString(), null).log(getContext());
                }
                try
                {
                    result = service.updateServices(msisdn, paramSet);
                }
                catch (final org.omg.CORBA.COMM_FAILURE commFail)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
                catch (final ProvisioningServiceException e)
                {
                    new MinorLogMsg(this, "Fail to update the services:" + Arrays.toString(paramSet)
                            + " for msisdn " + msisdn, e).log(getContext());

                    result = ExternalAppSupport.REMOTE_EXCEPTION;
                }
                catch(final Throwable t)
                {
                    result = ExternalAppSupport.COMMUNICATION_FAILURE;
                }
            }
        }
        finally
        {
            pmLogMsg.log(getContext());
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public short changeAMsisdn(final String ownerMsisdn, final String oldAMsisdn, final String newAMsisdn)
    {
        short result = ErrorCode.SUCCESS;
        // TODO should the oldAMsisdn be verified first?
        result = (short) deleteSubscriber(oldAMsisdn);
        if (result == ErrorCode.SUCCESS)
        {
            result = this.addAMsisdn(ownerMsisdn, newAMsisdn);
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public short changeMsisdn(final String oldMsisdn, final String newMsisdn)
    {
        short result = ErrorCode.SUCCESS;
        final SubsProfile profile = getSubsProfile(oldMsisdn);
        if (profile == null)
        {
            result = ErrorCode.SUBSCRIBER_NOT_FOUND;
        }
        else
        {
            profile.msisdn = newMsisdn;
            result = (short) addSubscriber(profile);
            if (result == ErrorCode.SUCCESS)
            {
                result = (short) deleteSubscriber(oldMsisdn);
            }
        }
        return result;
    }


    /**
     * Creates a subscriber profile.
     *
     * @param msisdn
     *            Subscriber MSISDN.
     * @param spid
     *            Service provider ID.
     * @param imsi
     *            Subscriber IMSI.
     * @param currency
     *            Preferred currency of the subscriber.
     * @param ratePlan
     *            ECP rate plan ID.
     * @param expiry
     *            Expiry date.
     * @param classOfService
     *            Class of Service associated with the subscriber.
     * @param state
     *            Subscriber state.
     * @param pin
     *            Subscriber PIN.
     * @param language
     *            Preferred language of the subscriber.
     * @param bucketRatingBalance
     *            Bucket rating balance of the subscriber.
     * @param timeRegionId
     *            Default time zone of the subscriber.
     * @param groupAccount
     *            Group account ID of the subscriber.
     * @param billingNumber
     *            The number to charge against.
     * @return Subscriber profile with the provided contents.
     */
    private SubsProfile subsProfile(final String msisdn, final int spid, final String imsi, final String currency,
        final int ratePlan, final long expiry, final int classOfService, final int state, final String pin,
        final int language, final int bucketRatingBalance, final String timeRegionId, final String groupAccount,
        final String billingNumber)
    {
        final SubsProfile profile = new SubsProfile();
        profile.msisdn = msisdn;
        profile.spid = spid;
        profile.imsi = imsi;
        profile.currencyType = currency;
        profile.ratePlan = ratePlan;
        profile.expiry = expiry;
        profile.classOfService = classOfService;
        profile.state = state;
        profile.pin = pin;
        profile.language = language;
        profile.bucketRatingBalance = bucketRatingBalance;
        profile.timeRegionID = timeRegionId;
        profile.groupAccount = groupAccount;
        profile.billingNumber = billingNumber;
        return profile;
    }


}
