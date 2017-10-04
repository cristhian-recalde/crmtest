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
package com.trilogy.app.crm.provision.soap;

import java.security.Permission;

import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

import electric.util.holder.intOut;

/**
 * This class implements the SOAP API interface for provisioning and deactivating subscribers.
 *
 * @author imahalingam@redknee.com
 * @author amit.baid@redknee.com
 */
public class SubscriberProvisioner implements SubscriberProvisionInterface
{

    /**
     * Constructor
     * @param ctx the operating context
     */
    public SubscriberProvisioner(final Context ctx)
    {
        context_ = ctx;
    }

    public SubscriberInfo getSub(final String userName, final String password, final String msisdn,
            final intOut retCode) throws SoapServiceException
    {
        if (LogSupport.isDebugEnabled(context_))
        {
            LogSupport.debug(context_, this, " start getSub()");
        }
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "getSub(msisdn)");

        final SubscriberInfo newSubInfo = new SubscriberInfo();
        try
        {
            final int authCode = authenticateUser(userName, password);
            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " User authentication returnCode: " + authCode);
            }
            if (authCode == LOGIN_FAILED)
            {
                retCode.value = LOGIN_FAILED;
                return newSubInfo;
            }
            Subscriber matchingSub = null;
            boolean success = false;
            try
            {
                final Msisdn mobNum = MsisdnSupport.getMsisdn(context_, msisdn);
                if (mobNum == null)
                {
                    retCode.value = MSISDN_DOES_NOT_EXIST;
                    return newSubInfo;
                }
                else
                {
                    matchingSub = SubscriberSupport.lookupSubscriberForMSISDN(context_, msisdn);
                    success = true;
                }
            }
            catch (final HomeException he)
            {
                LogSupport.minor(context_, this, "Error occurred while looking up subscriber for Msisdn " + msisdn, he);
                success = false;
            }
            if (success)
            {
                setupSubscriberInfo(matchingSub, newSubInfo);
            }

        }
        catch (final Throwable t)
        {
            final String msg = "Exception thrown for msisdn \"" + msisdn + "\" in getSub(): ";
            LogSupport.info(context_, this, msg, t);
            throw new SoapServiceException(msg, t);
        }
        pmLogMsg.log(context_);
        retCode.value = SUCCESSFUL;
        return newSubInfo;
    }

    public int deactivateSub(final String userName, final String password, final String msisdn)
        throws SoapServiceException
    {
        if (LogSupport.isDebugEnabled(context_))
        {
            LogSupport.debug(context_, this, " start deactivateSub()");
        }
        int returnCode = -1;
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deactivateSub(msisdn)");

        try
        {
            final int authCode = authenticateUser(userName, password);
            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " User authentication returnCode: " + authCode);
            }
            if (authCode == LOGIN_FAILED)
            {
                return LOGIN_FAILED;
            }

            //get Subscriber for Msisdn.
            Subscriber matchingSub = null;
            try
            {
                matchingSub = SubscriberSupport.lookupSubscriberForMSISDN(context_, msisdn);
            }
            catch (final HomeException he)
            {
                if (he.getMessage().indexOf("Could not find Subscriber for") >= 0)
                {
                    return MSISDN_DOES_NOT_EXIST;
                }
                else
                {
                    throw new SoapServiceException("Unable to retreive subscriber for msisdn \"" + msisdn + "\"", he);
                }
            }

            //get Subscriber State
            final SubscriberStateEnum subState = matchingSub.getState();
            //if state is INACTIVE don't do anything.
            if (SubscriberStateEnum.INACTIVE.equals(subState))
            {
                return SUCCESSFUL;
            }
            //if state is LOCKED , set subscriber as ACTIVE first.
            else if (SubscriberStateEnum.LOCKED.equals(subState))
            {
                matchingSub.setState(SubscriberStateEnum.ACTIVE);

                final Home subscriberHome = (Home) context_.get(SubscriberHome.class);
                try
                {
                    subscriberHome.store(matchingSub);
                }
                catch (final Exception e)
                {
                    final String msg = "Exception thrown for msisdn \"" + msisdn + "\" in deactivateSub() "
                            + "when setting ACTIVE state a BARRED subscriber ";
                    throw new SoapServiceException(msg, e);
                }
            }
            matchingSub.setState(SubscriberStateEnum.INACTIVE);

            final Home subscriberHome = (Home) context_.get(SubscriberHome.class);
            try
            {
                subscriberHome.store(matchingSub);
                returnCode = SUCCESSFUL;
            }
            catch (final Exception e)
            {
                returnCode = DEACTIVATE_FAILED;
                LogSupport.minor(context_, this, " DEACTIVATE_FAILED: " + e.getMessage(), e);
            }
            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " end deactivateSub()");
            }

        }
        catch (final Throwable t)
        {
            final String msg = "Exception thrown for msisdn \"" + msisdn + "\" in deactivateSub(): ";
            LogSupport.info(context_, this, msg, t);
            throw new SoapServiceException(msg, t);
        }
        pmLogMsg.log(context_);
        return returnCode;
    }

    public int deleteSub(final String userName, final String password, final String msisdn) throws SoapServiceException
    {
        if (LogSupport.isDebugEnabled(context_))
        {
            LogSupport.debug(context_, this, " start deleteSub()");
        }
        int returnCode = -1;

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "deleteSub(msisdn)");

        try
        {
            final int authCode = authenticateUser(userName, password);
            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " User authentication returnCode: " + authCode);
            }
            if (authCode == LOGIN_FAILED)
            {
                return LOGIN_FAILED;
            }

            //get Subscriber for Msisdn.
            Subscriber matchingSub = null;
            try
            {
                matchingSub = SubscriberSupport.lookupSubscriberForMSISDN(context_, msisdn);
            }
            catch (final HomeException he)
            {
                if (he.getMessage().indexOf("Could not find Subscriber for") >= 0)
                {
                    return MSISDN_DOES_NOT_EXIST;
                }
                else
                {
                    throw new SoapServiceException("Unable to retreive subscriber for msisdn \"" + msisdn + "\"", he);
                }
            }
            if (matchingSub == null)
            {
                return MSISDN_DOES_NOT_EXIST;
            }
            else if (matchingSub.getState().getIndex() != SubscriberStateEnum.INACTIVE_INDEX)
            {
                LogSupport.info(context_, this,
                        "Can't delete subscriber [Msisdn: " + msisdn + "] as it is not Inactive");
                return DEACTIVATE_FAILED;
            }
            final String packageId = matchingSub.getPackageId();
            final TechnologyEnum subTechnologyType = matchingSub.getTechnology();
            final int spid = matchingSub.getSpid();

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " found matchingSub: ");
            }

            //get Account for subscriber
            final Account account = SubscriberSupport.lookupAccount(context_, matchingSub);
            if (account == null)
            {
                throw new SoapServiceException("Account not found for account no: " + matchingSub.getBAN());
            }

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " found matchingAccount: ");
            }

            //remove subscriber from crm.
            // TT 6092639524 not removing records any more
//            final Home subscriberHome = (Home) context_.get(SubscriberHome.class);
//            subscriberHome.remove(matchingSub);
            returnCode = SUCCESSFUL;

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " removed Subscriber from SubscriberHome for msisdn: " + msisdn);
            }

            // remove only individual accounts
            // TT 6092639524 not removing records any more
//            if (account.getAccountType(context_).isIndividual())
//            {
                //remove Account from crm.
//                final Home accountHome = (Home) context_.get(AccountHome.class);
//                accountHome.remove(account);
//            }

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " removed Account from AccountHome for msisdn: " + msisdn);
            }

            //remove msisdn from msisdnHome.
            // TT 6092639524 not removing records any more
//            MsisdnSupport.removeMsisdn(context_, msisdn);
            final Msisdn msisdnObject = MsisdnSupport.getMsisdn(context_, msisdn);
            final String subscriberId = msisdnObject.getSubscriberID(context_);

            if (subscriberId == null || subscriberId.equals(matchingSub.getId()) || subscriberId.length() == 0)
            {
                // only modify the msisdn object state if MSISDN is still assigned to this subscriber
                // or is not assigned to any subscriber
                MsisdnManagement.deassociateMsisdnWithSubscription(context_, msisdn, subscriberId, "voiceMsisdn");
                MsisdnManagement.releaseMsisdn(context_, msisdn, account.getBAN(), "CRM - SOAP SubscriberProvisioner - deleteSub");
            }

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " removed Msisdn from MsisdnHome for msisdn: " + msisdn);
            }

            //remove package from packageHome.
            // TT 6092639524 not removing records any more
            final GenericPackage simcard = PackageSupportHelper.get(context_).getPackage(context_, subTechnologyType, packageId, spid);
            final Home packageHome = PackageSupportHelper.get(context_).returnPackageHomeBasedOnTechnology(context_, subTechnologyType);
//            packageHome.remove(simcard);
            simcard.setState(PackageStateEnum.HELD);
            packageHome.store(simcard);

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " removed Package from PackageHome for packageId: " + packageId);
            }

            if (LogSupport.isDebugEnabled(context_))
            {
                LogSupport.debug(context_, this, " end deleteSub()");
            }
        }
        catch (final Throwable t)
        {
            final String msg = "Exception thrown for msisdn \"" + msisdn + "\" in deleteSub(): ";
            LogSupport.info(context_, this, msg, t);
            throw new SoapServiceException(msg, t);
        }
        pmLogMsg.log(context_);
        return returnCode;
    }

    private int authenticateUser(final String login, final String passwd)
    {
        final Context subCtx = context_.createSubContext();
        int result = LOGIN_FAILED;
        if (LogSupport.isDebugEnabled(context_))
        {
            LogSupport.debug(context_, this, " start authenticateUser()");
        }

        try
        {
            Session.setSession(subCtx, subCtx);
            final AuthSPI auth = (AuthSPI) subCtx.get(AuthSPI.class);
            auth.login(subCtx, login, passwd);
            if (AuthSupport.hasPermission(subCtx, permission_))
            {
                result = SUCCESSFUL;
            }
            else
            {
                result = LOGIN_FAILED;
            }
        }
        catch (final LoginException le)
        {
            result = LOGIN_FAILED;
        }
        return result;
    }

    private void setupSubscriberInfo(final Subscriber newSub, final SubscriberInfo newSubInfo)
    {
        newSubInfo.dateCreated = newSub.getDateCreated() == null ? "null" : newSub.getDateCreated().toString();
        // TODO 2008-08-22 name no longer part of Subscriber
        newSubInfo.firstName = "";
        newSubInfo.lastName = "";
        newSubInfo.MSISDN = newSub.getMSISDN();
        newSubInfo.spid = newSub.getSpid();
        newSubInfo.startDate = newSub.getStartDate() == null ? "null" : newSub.getStartDate().toString();
        newSubInfo.state = newSub.getState().getIndex();
        newSubInfo.subscriberType = newSub.getSubscriberType().getIndex();
    }

    private final Context context_;
    private final Permission permission_ = new SimplePermission(Common.PROVISIONERS_GROUP_PERMISSION);
    private static final String PM_MODULE = SubscriberProvisioner.class.getName();

}
