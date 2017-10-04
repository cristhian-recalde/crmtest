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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;

import com.trilogy.app.osa.ecp.provision.ErrorCode;
import com.trilogy.app.osa.ecp.provision.ServiceParameter;
import com.trilogy.app.osa.ecp.provision.SubsProfile;


/**
 * Test client for ECP provisioning. AppFf and AppHomezone related methods are not yet
 * implemented.
 *
 * @author cindy.wong@redknee.com
 * @since Jul 21, 2007
 */
public class AppEcpTestClient extends ContextAwareSupport implements AppEcpClient
{

    /**
     * Create a new instance of <code>AppEcpTestClient</code>.
     *
     * @param context
     *            The operating context.
     */
    public AppEcpTestClient(final Context context)
    {
        super();
        super.setContext(context);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized short addAMsisdn(final String mainMsisdn, final String aMsisdn)
    {
        short result = ErrorCode.INTERNAL_ERROR;
        if (mainMsisdn == null || aMsisdn == null)
        {
            result = ErrorCode.INVALID_PARAMETER;
        }
        else if (!this.subscribers_.containsKey(mainMsisdn))
        {
            result = ErrorCode.MAIN_SUBSCRIBER_NOT_FOUND;
        }
        else if (this.subscribers_.containsKey(aMsisdn))
        {
            result = ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST;
        }
        else if (findAMsisdnParent(aMsisdn) != null)
        {
            result = ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST;
        }
        else
        {
            Set<String> amsisdns = this.amsisdns_.get(mainMsisdn);
            if (amsisdns == null)
            {
                amsisdns = new HashSet<String>();
            }
            if (amsisdns.size() >= this.maxAMsisdn_)
            {
                result = ErrorCode.AMSISDN_LIMIT_EXCEEDED;
            }
            amsisdns.add(aMsisdn);
            this.amsisdns_.put(mainMsisdn, amsisdns);
            result = ErrorCode.SUCCESS;
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int addSubscriber(final String msisdn, final int spid, final String imsi,
        final String currency, final int ratePlan, final long expiry, final int classOfService, final int state,
        final String pin, final int language, final String timeRegionId, final String groupAccount,
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
        profile.timeRegionID = timeRegionId;
        profile.groupAccount = groupAccount;
        profile.billingNumber = billingNumber;
        return addSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int addSubscriber(final String msisdn, final int spid, final String imsi,
        final String currency, final int ratePlan, final long expiry, final int classOfService, final int state,
        final String pin, final int language, final String timeRegionId, final String groupAccount)
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
        profile.timeRegionID = timeRegionId;
        profile.groupAccount = groupAccount;
        profile.billingNumber = "";
        return addSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int addSubscriber(final SubsProfile profile)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (profile == null || profile.msisdn == null)
        {
            result = ErrorCode.INVALID_PARAMETER;
        }
        else if (this.subscribers_.containsKey(profile.msisdn))
        {
            result = ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST;
        }
        else
        {
            final String parent = findAMsisdnParent(profile.msisdn);
            if (parent != null)
            {
                result = ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST;
            }
            else
            {
                this.subscribers_.put(profile.msisdn, profile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int deleteSubscriber(final String msisdn)
    {
        int result = ErrorCode.SUBSCRIBER_NOT_FOUND;

        if (msisdn == null)
        {
            result = ErrorCode.INVALID_PARAMETER;
        }
        else if (this.subscribers_.containsKey(msisdn))
        {
            this.subscribers_.remove(msisdn);
            if (this.amsisdns_.containsKey(msisdn))
            {
                this.amsisdns_.remove(msisdn);
            }
            result = ErrorCode.SUCCESS;
        }
        else
        {
            final String parent = findAMsisdnParent(msisdn);
            if (parent != null)
            {
                this.amsisdns_.get(parent).remove(msisdn);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized short disableHomezone(final String msisdn)
    {
        // not implemented
        return ErrorCode.SUCCESS;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized short enableHomezone(final String msisdn)
    {
        // not implemented
        return ErrorCode.SUCCESS;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized SubsProfile getSubsProfile(final String msisdn)
    {
        return this.subscribers_.get(msisdn);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void invalidate(final Throwable t)
    {
        // do nothing
    }


    /**
     * {@inheritDoc}
     */
    public synchronized short setFriendsAndFamilyEnabled(final String msisdn, final boolean enabled)
    {
        // not implemented
        return ErrorCode.SUCCESS;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateClassOfService(final String msisdn, final int classOfService)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (findAMsisdnParent(msisdn) != null)
        {
            result = ErrorCode.AMSISDN_EDIT_NOT_PERMITTED;
        }
        else
        {
            final SubsProfile profile = this.subscribers_.get(msisdn);
            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.classOfService = classOfService;
                this.subscribers_.put(msisdn, profile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateGroupAccount(final String msisdn, final String groupAccount)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (findAMsisdnParent(msisdn) != null)
        {
            result = ErrorCode.AMSISDN_EDIT_NOT_PERMITTED;
        }
        else
        {
            final SubsProfile profile = this.subscribers_.get(msisdn);
            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.groupAccount = groupAccount;
                this.subscribers_.put(msisdn, profile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateImsi(final String msisdn, final String imsi)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (findAMsisdnParent(msisdn) != null)
        {
            result = ErrorCode.AMSISDN_EDIT_NOT_PERMITTED;
        }
        else
        {
            final SubsProfile profile = this.subscribers_.get(msisdn);
            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.imsi = imsi;
                this.subscribers_.put(msisdn, profile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateRatePlan(final String msisdn, final int ratePlan)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (findAMsisdnParent(msisdn) != null)
        {
            result = ErrorCode.AMSISDN_EDIT_NOT_PERMITTED;
        }
        else
        {
            final SubsProfile profile = this.subscribers_.get(msisdn);
            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.ratePlan = ratePlan;
                this.subscribers_.put(msisdn, profile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized short updateServices(final String msisdn, final ServiceParameter[] paramSet)
    {
        // not implemented
        return ErrorCode.SUCCESS;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateSubscriber(final String msisdn, final int spid, final String imsi,
        final String currency, final int ratePlan, final long expiry, final int classOfService, final int state,
        final String pin, final int language, final String timeRegionId, final String groupAccount,
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
        profile.timeRegionID = timeRegionId;
        profile.groupAccount = groupAccount;
        profile.billingNumber = billingNumber;
        return updateSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateSubscriber(final String msisdn, final int spid, final String imsi,
        final String currency, final int ratePlan, final long expiry, final int classOfService, final int state,
        final String pin, final int language, final String timeRegionId, final String groupAccount)
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
        profile.timeRegionID = timeRegionId;
        profile.groupAccount = groupAccount;
        profile.billingNumber = "";
        return updateSubscriber(profile);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateSubscriber(final SubsProfile profile)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (profile == null)
        {
            result = ErrorCode.INVALID_PARAMETER;
        }
        else if (findAMsisdnParent(profile.msisdn) != null)
        {
            result = ErrorCode.AMSISDN_EDIT_NOT_PERMITTED;
        }
        else
        {
            final SubsProfile storedProfile = this.subscribers_.get(profile.msisdn);
            if (storedProfile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                this.subscribers_.put(profile.msisdn, storedProfile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized int updateSubscriberState(final String msisdn, final int state)
    {
        int result = ErrorCode.INTERNAL_ERROR;
        if (findAMsisdnParent(msisdn) != null)
        {
            result = ErrorCode.AMSISDN_EDIT_NOT_PERMITTED;
        }
        else
        {
            final SubsProfile profile = this.subscribers_.get(msisdn);
            if (profile == null)
            {
                result = ErrorCode.SUBSCRIBER_NOT_FOUND;
            }
            else
            {
                profile.state = state;
                this.subscribers_.put(msisdn, profile);
                result = ErrorCode.SUCCESS;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public short changeAMsisdn(final String ownerMsisdn, final String oldAMsisdn, final String newAMsisdn)
    {
        short result = ErrorCode.INTERNAL_ERROR;
        if (ownerMsisdn == null)
        {
            result = ErrorCode.INVALID_PARAMETER;
        }
        else if (this.subscribers_.get(ownerMsisdn) == null)
        {
            result = ErrorCode.SUBSCRIBER_NOT_FOUND;
        }
        else if (!SafetyUtil.safeEquals(ownerMsisdn, findAMsisdnParent(oldAMsisdn)))
        {
            result = ErrorCode.INVALID_PARAMETER;
        }
        else if (findAMsisdnParent(newAMsisdn) != null || this.subscribers_.get(newAMsisdn) != null)
        {
            result = ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST;
        }
        else
        {
            Set<String> amsisdns = this.amsisdns_.get(ownerMsisdn);
            if (amsisdns == null)
            {
                amsisdns = new HashSet<String>();
            }
            if (amsisdns.size() >= this.maxAMsisdn_)
            {
                result = ErrorCode.AMSISDN_LIMIT_EXCEEDED;
            }
            amsisdns.remove(oldAMsisdn);
            amsisdns.add(newAMsisdn);
            this.amsisdns_.put(ownerMsisdn, amsisdns);
            result = ErrorCode.SUCCESS;
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public short changeMsisdn(final String oldMsisdn, final String newMsisdn)
    {
        short result = ErrorCode.INTERNAL_ERROR;
        if (oldMsisdn == null || newMsisdn == null)
        {
            result = ErrorCode.INTERNAL_ERROR;
        }
        else if (!this.subscribers_.containsKey(oldMsisdn))
        {
            result = ErrorCode.SUBSCRIBER_NOT_FOUND;
        }
        else if (this.subscribers_.containsKey(newMsisdn))
        {
            result = ErrorCode.SUBSCRIBER_INFO_ALREADY_EXIST;
        }
        else
        {
            final SubsProfile profile = this.subscribers_.get(oldMsisdn);
            this.subscribers_.remove(oldMsisdn);
            profile.msisdn = newMsisdn;
            this.subscribers_.put(newMsisdn, profile);
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public String getRemoteInfo()
    {
        return "AppEcpClient test client";
    }


    /**
     * {@inheritDoc}
     */
    public String getServiceDescription()
    {
        return "AppEcpClient";
    }


    /**
     * {@inheritDoc}
     */
    public String getServiceName()
    {
        return "AppEcpClient";
    }


    /**
     * {@inheritDoc}
     */
    public boolean isServiceAlive()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public void connectionDown()
    {
        // do nothing
    }


    /**
     * {@inheritDoc}
     */
    public void connectionUp()
    {
        // do nothing
    }


    /**
     * Sets the maximum number of additional MSISDNs allowed per subscriber.
     *
     * @param maxAMsisdn
     *            maximum number of additional MSISDNS allowed per subscriber.
     */
    public synchronized void setMaxAMsisdn(final int maxAMsisdn)
    {
        this.maxAMsisdn_ = maxAMsisdn;
    }


    /**
     * Finds the parent of the provided aMSISDN.
     *
     * @param aMsisdn
     *            Additional MSISDN.
     * @return Parent of the aMSISDN.
     */
    private synchronized String findAMsisdnParent(final String aMsisdn)
    {
        for (final Entry<String, Set<String>> entry : this.amsisdns_.entrySet())
        {
            if (entry.getValue().contains(aMsisdn))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Maximum number of aMsisdns each subscriber is allowed to have.
     */
    private int maxAMsisdn_ = 15;

    /**
     * ECP subscribers.
     */
    private final Map<String, SubsProfile> subscribers_ = new HashMap<String, SubsProfile>();

    /**
     * Additional MSISDNs.
     */
    private final Map<String, Set<String>> amsisdns_ = new HashMap<String, Set<String>>();

}
