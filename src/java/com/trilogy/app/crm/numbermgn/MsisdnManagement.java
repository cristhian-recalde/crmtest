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

package com.trilogy.app.crm.numbermgn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountNote;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.clean.CronConstants;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.Technology;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupSupport;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class MsisdnManagement
{

    private static final String DEFAULT_USERID = "default";


    
    /**
     * Marks the MSISDN as owned by the ban specified in the request.  If the MSISDN is marked as a external MSISDN then
     * if the MSISDN doesn't currently exist in our system it will automatically be created.  If the MSISDN is not external
     * then if the MSISDN doesn't exist then we will throw an HomeException.  If the MSISDN is already owned by the BAN then
     * the request will trivially succeed.  Internal MSISDNs cannot be acquired by requests where the isExternal flag is true 
     * and vice-versa to ensure consistent use of the MSISDN.  An error will be thrown when a MSISDN is attempted to be 
     * claimed by a Group Account.
     * 
     * @param ctx                Operating context
     * @param msisdn            MSISDN to be claimed
     * @param ban                Account Identifier for the Account that will own the MSISDN
     * @param isExternal        indicator of whether the MSISDN is to be a External MSISDN
     * @param erReference        textual reference used for correlating requests
     * @throws HomeException    Thrown when an error occurs such as validation or invalid scenarios
     */
    public static void claimMsisdn(Context ctx, String msisdn, String ban, boolean isExternal, String erReference)
        throws HomeException, MsisdnAlreadyAcquiredException
    {
        String loggingHeader = "[MsisdnManagement::claimMsisdn (ban=" + ban +", msisdn=" + msisdn + ", isExternal=" + isExternal +")] ";
        Account account = AccountSupport.getAccount(ctx, ban);
        claimMsisdn(ctx, msisdn, account, isExternal, erReference);
    }
        
    public static void claimMsisdn(Context ctx, String msisdn, Account account, boolean isExternal, String erReference)
        throws HomeException, MsisdnAlreadyAcquiredException
    {
        String loggingHeader = "[MsisdnManagement::claimMsisdn (ban=" + account.getBAN() +", msisdn=" + msisdn + ", isExternal=" + isExternal +")] ";
        if (account == null)
        {
               // invalid scenario. Account doesn't exist for the specified BAN
               new DebugLogMsg(MsisdnManagement.class, loggingHeader + "No Account record found for the specified BAN.  Request will be rejected!", null).log(ctx);
               String msg = "Unable to find Account for [ban=" + account.getBAN() + "].  Please verify that the Account is provisioned to the system prior to trying to claim the Mobile Number.";
               throwException(ctx, msg);
        }
        
        // Reject attempt to claim if the target is a Group Account
        if (!account.isIndividual(ctx))
        {
            // if the msisdn is the Pool Msisdn of the Pool account, then it's for the special pool subscription
            if (!(account.isPooled(ctx) || account.getPoolMSISDN().equals(msisdn)))
            {
                String errmsg = "The Mobile Number Claim action is not allowed for Group Accounts.  Account " + account.getBAN()
                            + " is a Group Account and cannot claim Mobile Numbers.";
                throwException(ctx, errmsg);
            }
        }
        
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
        Home msisdnHome = (Home)ctx.get(MsisdnHome.class);
        
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Request to claim a MSISDN recieved.  About to query for existing MSISDN record...", null).log(ctx);
        Msisdn createMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);


       if (!isExternal && createMsisdn == null)
       {
           // invalid scenario. We may choose to add auto-create MSISDN in here
           new DebugLogMsg(MsisdnManagement.class, loggingHeader + "No MSISDN record found for the specified MSISDN.  Request will be rejected!", null).log(ctx);
           String msg = "Specified Mobile Number doesn't belong to a valid MSISDN Group.  Please verify that the Mobile Number is provisioned to the system prior to assigning to an Account.";
           throwException(ctx, msg);
       }
        
        if(!GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
            if (createMsisdn != null && createMsisdn.getBAN().equals(account.getBAN()) && createMsisdn.getState().equals(MsisdnStateEnum.IN_USE))
            {
                new DebugLogMsg(MsisdnManagement.class, "Account already owns this MSISDN so claiming is trivially complete.  MSISDN Claimed Successfully!", null).log(ctx);
                throw new MsisdnAlreadyAcquiredException("Account already owns this MSISDN");
            }
        
        
        if ( (createMsisdn != null && ctx.has(Common.BILLING_TYPE_CONVERSION)) || GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            createMsisdn.setSubscriberType(account.getSystemType());   
        }
        else if (createMsisdn != null && !account.getSystemType().equals(SubscriberTypeEnum.HYBRID)
                && !createMsisdn.getSubscriberType().equals(SubscriberTypeEnum.HYBRID)
                && !account.getSystemType().equals(createMsisdn.getSubscriberType()))
        {
            new DebugLogMsg(MsisdnManagement.class, "Account and MSISDN subscriber type are not valid [accountSubscriberType=" + account.getSystemType() + ", msisdnSubscriberType=" + createMsisdn.getSubscriberType() + "].  Claim MSISDN request will be denied.", null).log(ctx);
            String msg = "Account's System Type does not match with the Subscriber Type of the Mobile Number you have selected.  Please select a valid Mobile Number for this Account.";
            throwException(ctx, msg);
        }
        
        if(!GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
            if (createMsisdn != null && account.getSpid() != createMsisdn.getSpid())
            {
                new DebugLogMsg(MsisdnManagement.class, "Account and MSISDN's spid do not match [accountSpid=" + account.getSpid() + ", msisdnSpid=" + createMsisdn.getSpid() + "].  Claim request will be deined.", null).log(ctx);
                String msg = "Account's Service Provider does not match the Mobile Number's Service Provider setting.  Please select a Mobile Number that is of the same Service Provider as the Account being used.";
                throwException(ctx, msg);
            }
        
        if (createMsisdn != null && !createMsisdn.getState().equals(MsisdnStateEnum.AVAILABLE) && !createMsisdn.getBAN().equals(account.getBAN()))
        {
        		new DebugLogMsg(MsisdnManagement.class, "MSISDN is in a invalid state for claiming [msisdnState=" + createMsisdn.getState() + ", msisdnBan=" + createMsisdn.getBAN() + ", accountBan=" + account.getBAN() + "].  Claim request will be denied.", null ).log(ctx);
        		String msg = "The Mobile Number you have selected is not in a valid state for claiming.  Please select an alternative Mobile Number.";
        		throwException(ctx, msg);
        		
        }
        
        boolean isAddMsisdn = "aMsisdn".equals(erReference)?true:false;
        
        if(isExternal)
        {
            // verify that the MSISDN specified is actually an external MSISDN.
            // 1. MSISDN doesn't exist in CRM
            // 2. MSISDN exists in CRM but has the "external" flag enabled
            if (createMsisdn != null && createMsisdn.isExternal())
            {
                // valid MSISDN provided
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN record exists and is marked as External MSISDN.  Attempting to claim the MSISDN...", null).log(ctx);
                createMsisdn.setMsisdn(msisdn);
                createMsisdn.setSpid(spid.getSpid());
                createMsisdn.setSubscriberType(account.getSystemType());
                createMsisdn.setExternal(true);
                createMsisdn.setGroup(spid.getExternalMSISDNGroup());
                
                if(isAddMsisdn)
                	createMsisdn.setAMsisdn(isAddMsisdn);
                
                createMsisdn.claim(ctx, account.getBAN());
                msisdnHome.store(ctx,createMsisdn);
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Successfully claimed the MSISDN!", null).log(ctx);
            }
            else if (createMsisdn == null)
            {
                // valid scenario, we need to create a new MSISDN in this case
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "No MSISDN record found, new record will need to be created and claimed.  Attempting to claim the MSISDN...", null).log(ctx);
                createMsisdn = new Msisdn();
                createMsisdn.setMsisdn(msisdn);
                createMsisdn.setSpidGroup(createMsisdn.getSpid());
                createMsisdn.setSpid(spid.getSpid());
                createMsisdn.setSubscriberType(account.getSystemType());
                createMsisdn.setExternal(true);
                createMsisdn.setTechnology(Technology.getTechnology(ctx));
                createMsisdn.setGroup(spid.getExternalMSISDNGroup());                
                createMsisdn.claim(ctx, account.getBAN()); 
                msisdnHome.create(createMsisdn);
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Successfully claimed the MSISDN! msisdn = "+ createMsisdn, null).log(ctx);
            }
            else
            {	
                // invalid scenario, the entered MSISDN is not an external MSISDN
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "External Entry Mode selected, but MSISDN specified already exists and is not marked as External MSISDN.", null).log(ctx);
                String msg = "Mobile Number specified is currently part of an existing group and is not marked as a External Mobile Number.  " +
                        "If this subscriber is truely using this Mobile Number, please correct the Mobile Number record by mark it as a External Mobile Number.";
                throwException(ctx, msg);
            }
        }
        else
        {
            // verify that the MSISDN specified is a valid custom/msisdn-group MSISDN.
            // 1. MSISDN must exist in CRM
            
           if (createMsisdn != null && !createMsisdn.isExternal())
           {
               // valid MSISDN provided
               new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN Record found and is correctly marked for internal usage.  Attempting to claim the MSISDN...", null).log(ctx);
               createMsisdn.setMsisdn(msisdn);
               createMsisdn.setSpidGroup(createMsisdn.getSpid());
               createMsisdn.setSpid(spid.getSpid());
               createMsisdn.setExternal(false);
               if(isAddMsisdn)
               	createMsisdn.setAMsisdn(isAddMsisdn);
               
               createMsisdn.claim(ctx, account.getBAN());
               msisdnHome.store(createMsisdn);
               new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Successfully claimed the MSISDN! Msisdn = "+ createMsisdn, null).log(ctx);                   
           }
           else
           {
               // invalid scenario.  The MSISDN exists but is marked as a External MSISDN.
               new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN Record found, but is marked as a External MSISDN.  Request will be rejected!", null).log(ctx);
               String msg = "Mobile Number specified is currently part of an existing group and is marked as a External Mobile Number.  " +
                       "If this subscriber is truely using this Mobile Number, please correct the Mobile Number record by marking it as a Internal Mobile Number.";
               throwException(ctx, msg);
           }
        }
        
        // Insert logic for TFA Group Provisioning here        
        try
        {
            long publicGroupId = ctx.getLong(TransferContractSupport.TRANSFER_CONTRACT_PUBLIC_GROUP_ID, -1);
            long privateGroupId = ctx.getLong(TransferContractSupport.TRANSFER_CONTRACT_PRIVATE_GROUP_ID, -1);
            
            if (publicGroupId != -1)
            {
                MemberGroupSupport.addMemberToContractGroup(ctx, spid.getSpid(), publicGroupId, msisdn);
            }
            if (privateGroupId != -1)
            {
                MemberGroupSupport.addMemberToContractGroup(ctx, spid.getSpid(), privateGroupId, msisdn);
            }
        }
        catch (Exception e)
        {
            String msg = "[MsisdnManagement::claimMsisdn failed to provision MSISDN to contract group on TFA "
                + "[msisdn=" + msisdn + "].  Claim will continue regardless ";
            new MinorLogMsg(MsisdnManagement.class.getName(), msg, e).log(ctx);
            ProvisioningHomeException he = new ProvisioningHomeException(e.getMessage(), e);
            
            HTMLExceptionListener el = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
            if (el != null)
            {
                el.thrown(he);
            }  
        }
        
        /* TT 8101400042: Removing the PIN Generation from the Claim MSISDN Logic, 
         * since we have to support Airtime Subscriptions.  Airtime Subscripts will need to have
         * SMS services provisioned to have PIN Generation work properly.  See 
         * com.redknee.app.crm.home.sub.SubscriberPinManagerUpdateHome for details of the new
         * implementation.
         */
    }

    
    /**
     * Releases the MSISDN from the Account specified.  Release will be denied if there are subscriptions
     * still associated with the MSISDN.
     * 
     * @param ctx                        Operating Context
     * @param msisdn                    MSISDN that needs to be released
     * @param ban                        BAN of the Account from which the MSISDN is to be released.
     * @param erReference                 Free form string that is used as a reference in external communications to correlate the request.
     * @throws HomeInternalException
     * @throws HomeException
     */
    public static void releaseMsisdn(
                            Context ctx, 
                            String msisdn, 
                            String ban, 
                            String erReference) throws HomeInternalException, HomeException
    {
        String loggingHeader = "[MsisdnManagement::releaseMsisdn (originalMsisdn=" + msisdn +", ban=" + ban + ")] "; 
        Home msisdnHome = (Home)ctx.get(MsisdnHome.class);
        
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Request to release MSISDN has been made.  About to retrieve MSISDN record...", null).log(ctx);
        Msisdn originalMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);
        
        
        if(originalMsisdn == null)
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find a MSISDN record for the originalMsisdn specified!  Request to release will be rejected!", null).log(ctx);
            String msg = "Unable to find the specified Mobile Number in our system to release.  Please ensure the Mobile Number is provisioned and in use in the system prior to attempt to release.";
            throwException(ctx, msg);
        }
        
        if (!originalMsisdn.getBAN().equals(ban))
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN that is being requested for release isn't currently owned by the BAN specified [msisdnBan=" + originalMsisdn.getBAN() + ",requestBan=" + ban + "]", null).log(ctx);
            String msg = "Mobile Number that is being released isn't owned by the specified ban.  Failing release.";
            throwException(ctx, msg);
        }
        
        // check to see if there are any subscriptions attached to this MSISDN
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN record exists.  About to check if there are any subscriptions associated with it...", null).log(ctx);
        Collection<Subscriber> subscriptions = SubscriberSupport.getSubscriptionsByMSISDN(ctx, msisdn);
        
        if (subscriptions != null && !subscriptions.isEmpty())
        {
            // there are still subscriptions attached to this MSISDN we will not allow the MSISDN to be released
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Currently there are " + subscriptions.size() + " subscriptions associated with the MSISDN and thus release will be denied.", null).log(ctx);
            String msg = "Currently there are still " + subscriptions.size() + " subscriptions attached to this Mobile Number.  Please deactivate " +
                    "all subscriptions associated with this Mobile Number before reattempting to deassociate this Mobile Number with the current Account.";
            throwException(ctx, msg);
        }
        else
        {
            // safe to release the msisdn
            new DebugLogMsg(MsisdnManagement.class, "No subscriptions are associated with this MSISDN.  About to attempt to release the MSISDN...", null).log(ctx);

            originalMsisdn.release();
            msisdnHome.store(ctx,originalMsisdn);
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Successfully released the originalMsisdn! msisdn(new state) = "+ originalMsisdn, null).log(ctx);
        }

        // move this logic to MSISDNStateModifyAgent
        // we want to keep the contract groups should the account holder wish to re-acquire the msisdn
        // Insert logic for TFA Group Deprovisioning here
/*
        try
        {
            MemberGroupSupport.deleteMemberFromContractGroup(ctx, originalMsisdn.getMsisdn());
        }
        catch (Exception e)
        {
            String msg = "[MsisdnManagement::releaseMsisdn failed to deprovision MSISDN from contract group on TFA "
                + "[msisdn=" + originalMsisdn.getMsisdn() + "].  Release will continue regardless ";
            new MinorLogMsg(MsisdnManagement.class.getName(), msg, e).log(ctx);
            ProvisioningHomeException he = new ProvisioningHomeException(e.getMessage(), e);  
            
            HTMLExceptionListener el = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
            if (el != null)
                el.thrown(he);  
        }
*/

        /* TT 8101400042: Removing the PIN deletion from the Release MSISDN Logic.  See 
         * com.redknee.app.crm.home.sub.SubscriberPinManagerUpdateHome for details of the new
         * implementation. */
        
        return;
    }

    /**
     * Validates the MSISDN is of the right type and it is not currently in use by other
     * subscribers.
     *
     * @param ctx The operating context.
     * @param sub The subscriber being validated.
     * @param number The MSISDN being validated.
     * @param msisdnRef What field of the subscriber the MSISDN is used in (e.g. voice, data, fax).
     * @param optional Whether the MSISDN is optional.
     * @throws HomeException Thrown if there are problems looking up the MSISDN.
     */
    public static void validateMsisdnTypeAndNotInUse(final Context ctx, final Subscriber sub, final String number,
        final String msisdnRef, final boolean optional) throws HomeException
    {
        if (optional)
        {
            // this is for empty numbers like fax, data [psperneac]
            if (number == null || number.length() == 0)
            {
                return;
            }
        }
        final Msisdn dataMSISDN = MsisdnSupport.getMsisdn(ctx, number);

        /*
         * MSISDN in state of HELD will be reset to AVAILABLE by cron job after
         * pre-defined period, so here we assume that both AVAIABLE and HELD are valid
         * states for MSISDN.
         */
        if (dataMSISDN != null && dataMSISDN.getSubscriberType() != sub.getSubscriberType())
        {
            throw new HomeException("Provisioning Error 3004: Invalid " + msisdnRef + " [" + dataMSISDN
                + "] Subscriber Type [" + dataMSISDN.getSubscriberType().getDescription() + "]");
        }

        if (dataMSISDN != null && dataMSISDN.getState().equals(MsisdnStateEnum.IN_USE)
            && !SafetyUtil.safeEquals(sub.getBAN(), dataMSISDN.getBAN()))
        {
            throw new HomeException("Provisioning Error 3004: Invalid " + msisdnRef + " [" + dataMSISDN
                + "] State [" + dataMSISDN.getState().getDescription() + "]" + " and msisdn does not belong to "
                + sub.getId());
        }
    }


    public static void removeMsisdnFromContractGroup(final Context ctx, final String msisdn)
    {
        try
        {
            MemberGroupSupport.deleteMemberFromContractGroup(ctx, msisdn);
        }
        catch (Exception e)
        {
            String msg = "[MsisdnManagement::releaseMsisdn failed to deprovision MSISDN from contract group on TFA "
                + "[msisdn=" + msisdn + "].  Release will continue regardless ";
            new MinorLogMsg(MsisdnManagement.class.getName(), msg, e).log(ctx);
        }
    }

    public static void convertMsisdn(Context ctx, String msisdn, String ban, SubscriberTypeEnum newType, Subscriber convertedSub, String erReference) throws HomeException
    {
        String loggingHeader = "[MsisdnManagement::convertMsisdn (msisdn=" + msisdn +", ban=" + ban + ")] "; 
        Home msisdnHome = (Home)ctx.get(MsisdnHome.class);
        
        
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Request to convert MSISDN to [newType=" + newType + "] has been made.  About to retrieve MSISDN record...", null).log(ctx);
        Msisdn originalMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);

        if( originalMsisdn == null )
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find a MSISDN record for the originalMsisdn specified!  Request to convert will be rejected!", null).log(ctx);
            String msg = "Unable to find the specified Mobile Number in our system to convert.  Please ensure the Mobile Number is provisioned and in use in the system prior to attempt to convert.";
            throwException(ctx, msg);
        }
        
        if( originalMsisdn.getSubscriberType().equals(newType) )
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Requested subscriberType matches the MSISDN's subscriberType [type=" + originalMsisdn.getSubscriberType() + "].  Conversion is trivial and will return successfully!", null).log(ctx);
            return;
        }
        
        Account account = AccountSupport.getAccount(ctx, ban);
        if( account == null)
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find Account for the ban specified!  Request to convert will be rejected!", null).log(ctx);
            String msg = "Unable to find the specified Account in our system to convert.  Please ensure the Account is provisioned in the system prior to attempt to convert.";
            throwException(ctx, msg);
        }
        
        if( !account.getSystemType().equals(SubscriberTypeEnum.HYBRID) )
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Account is not Hybrid and thus conversion is not allowed.  Request to convert will be rejected!", null).log(ctx);
            String msg = "Account specified is not a Hybrid account and thus conversion is not allowed.";
            throwException(ctx, msg);
        }
        
        
        if (!originalMsisdn.getBAN().equals(ban))
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN that is being requested for conversion isn't currently owned by the BAN specified [msisdnBan=" + originalMsisdn.getBAN() + ",requestBan=" + ban + "]", null).log(ctx);
            String msg = "Mobile Number that is being converted isn't owned by the specified ban.  Failing conversion.";
            throwException(ctx, msg);
        }
        
        if (newType.equals(SubscriberTypeEnum.HYBRID))
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Requested conversion is to Hybrid which is trivially allowed.", null).log(ctx);
        }
        else
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "MSISDN record exists.  About to check if there are any subscriptions associated with it...", null).log(ctx);
            if (convertedSub != null )
            {
                if (convertedSub.getSubscriberType().equals(newType))
                {
                    new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Subscription [id=" + convertedSub.getId() + "] has a valid subscriberType for conversion request.", null).log(ctx);   
                }
            }
            else
            {
            Collection<Subscriber> subscriptions = SubscriberSupport.getSubscriptionsByMSISDN(ctx, msisdn);
            
            if(subscriptions == null || subscriptions.isEmpty())
            {
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "No subscriptions are associated with the MSISDN so conversion will be allowed.", null).log(ctx);
            }
            else
            {
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "About to iterate over associated subscriptions to validate the subscriberType of each record...", null).log(ctx);
                for (Iterator<Subscriber> subIter = subscriptions.iterator(); subIter.hasNext();)
                {
                    Subscriber subscriber = subIter.next();
                    if (!subscriber.getSubscriberType().equals(newType))
                    {
                        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Encountered a subscription which has a inconsistent subscriberType with the requested subscriberType [subscriptionSubType=" + subscriber.getSubscriberType() + ", requestedSubType=" + newType + "].  Conversion will be denied", null).log(ctx);
                        String msg = "Subscription with id=" + subscriber.getId() + " has a conflicting Subscriber Type to that being requested.  Please perform a conversion at the subscription level for this subscription before attempting Account level Mobile Number conversion.";
                        throwException(ctx, msg);
                    }
                    else
                    {
                        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Subscription [id=" + subscriber.getId() + "] has a valid subscriberType for conversion request.", null).log(ctx);
                    }
                }
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + "All subscriptions have a matching subscriberType so conversion will be allowed.", null).log(ctx);
            }
            }
        }
        
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "About to attempt to store converted MSISDN record...", null).log(ctx);
        originalMsisdn.setSubscriberType(newType);
        msisdnHome.store(ctx,originalMsisdn);
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Attempt to store modified MSISDN record completed successfully!", null).log(ctx);
    }
    
    /**
     * Associates the MSISDN with the subscription that specified by the subscriberId passed in.  If the Subscriber doesn't exist for the specified subscriberId
     * the request will be failed with a HomeException.
     * 
     * @param ctx
     * @param msisdn
     * @param subscriberId
     * @param resourceRef
     * @throws HomeException
     */
    public static void associateMsisdnWithSubscription(Context ctx, String msisdn, String subscriberId, String resourceRef) throws HomeException
    {
        String loggingHeader = "[MsisdnManagement::associateMsisdnWithSubscription (msisdn=" + msisdn +", subId=" + subscriberId + ")] ";
        Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
        if( subscriber == null )
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find Subscription for the subscriberId specified!  Request to associate with subscription will be rejected!", null).log(ctx);
            throw new HomeException("Unable to find the specified Subscription in our system to associate with subscription.  Please ensure the Subscription is provisioned in the system prior to attempt to associate with subscription.");
        }

        associateMsisdnWithSubscription(ctx, msisdn, subscriber, resourceRef);
        return;
        
    }
    
    public static void associateMsisdnWithSubscription(Context ctx, String msisdn, Subscriber subscriber, String resourceRef) throws HomeException
    {
        String loggingHeader = "[MsisdnManagement::associateMsisdnWithSubscription (msisdn=" + msisdn +", subId=" + subscriber.getId() + ")] "; 
        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Request to associate MSISDN subscription has been made.  About to retrieve MSISDN record...", null).log(ctx);
        Msisdn originalMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);

        if(originalMsisdn == null)
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find a MSISDN record for the msisdn specified!  Request to associate MSISDN will be rejected!", null).log(ctx);
            throw new HomeException("Unable to find the specified MSISDN in our system to associate with subscription.  Please ensure the MSISDN is provisioned and in use in the system prior to attempt to associate with subscription.");
        }

        if(!originalMsisdn.getBAN().equals(subscriber.getBAN()) || !originalMsisdn.getState().equals(MsisdnStateEnum.IN_USE))
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Specified MSISDN [ " + msisdn + " ] has not been claimed by the parent Account [ban=" + subscriber.getBAN() + ", msisdnBan=" + originalMsisdn.getBAN() + ", msisdnState=" + originalMsisdn.getState() + "].  Request to associate subscription will be denied.", null).log(ctx);
            throw new HomeException("Unable to associate MSISDN [ " + msisdn + " ] with subscription as the MSISDN has not been claimed by the parent Account.  Please attempt claiming the MSISDN prior to attempting to associating subscriptions to the MSISDN.");
        }

        if (!originalMsisdn.getTechnology().equals(TechnologyEnum.ANY)
                && !subscriber.getTechnology().equals(TechnologyEnum.NO_TECH)
                && !originalMsisdn.getTechnology().equals(subscriber.getTechnology()))
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Specified MSISDN [ " + msisdn + " ] has a different technology to the specified Subscription [msisdnTechnology=" + originalMsisdn.getTechnology() + ", subscriptionTechnology=" + subscriber.getTechnology() + "].  Request to associate subscription will be denied.", null).log(ctx);
            throw new HomeException("Unable to associate MSISDN with subscription as the MSISDN [ " + msisdn + " ] and subscription have a inconsistent Technology type specified.  Please attempt association with a MSISDN with a matcing Technology type.");
        }

        if(!originalMsisdn.getSubscriberType().equals(subscriber.getSubscriberType()) && !originalMsisdn.getSubscriberType().equals(SubscriberTypeEnum.HYBRID))
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Specified MSISDN [ " + msisdn + " ] has a different SubscriberType to the specified Subscription [msisdnSubType=" + originalMsisdn.getSubscriberType() + ", subscriptionSubType=" + subscriber.getSubscriberType() + "].  Request to associate subscription will be denied.", null).log(ctx);
            throw new HomeException("Unable to associate MSISDN [ " + msisdn + " ] with subscription as the MSISDN and subscription have a inconsistent Subscriber Type specified.  Please attempt association with a MSISDN with a matching or HYBRID Subscriber type.");
        }
        
        adjustHistoryForSubAssociation(ctx, msisdn, subscriber, resourceRef);
    }
    
    /**
     * <p>
     * Associate a {@link com.redknee.app.crm.bean.Msisdn} with an {@link Account}. This behavior was primarily added to support {@link AccountNote} creation in case 
     * a {@link Subscriber} activation/creation fails due to failure in charging the {@link Subscriber}'s Credit Card.
     * 
     * <p>
     *  Note: In case a credit card cannot be charged successfully in SubscriptionService.createSubscription() the Subscription Home pipeline execution
     *  is note triggered and no {@link MsisdnMgmtHistory} is present for that subscriber. This causes polling of ER1335 to fail and no Account Note can be 
     *  generated. This leads to a bad user experience as the reason for failure is not available to Subscriber/CSR. 
     * 
     * @param ctx
     * @param account
     * @param msisdn
     * @throws HomeException
     */
    public static void associateMsisdnWithBAN(Context ctx, String msisdn, Account account, String resourceRef , long subscriptionType) throws HomeException
    {
        String loggingHeader = "[MsisdnManagement::associateMsisdnWithAccount (msisdn=" + msisdn +", subId=" + account.getBAN() + ")] "; 
        
        if(LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx , MsisdnManagement.class, loggingHeader + "Request to associate MSISDN subscription has been made.  About to retrieve MSISDN record...", null);
        }
        Msisdn originalMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);

        if(originalMsisdn == null)
        {
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find a MSISDN record for the msisdn specified!  Request to associate MSISDN will be rejected!", null).log(ctx);
            throw new HomeException("Unable to find the specified MSISDN in our system to associate with account.  Please ensure the MSISDN is provisioned and in use in the system prior to attempt to associate with subscription.");
        }
        
        adjustHistoryForBanAssociation(ctx, msisdn, account, resourceRef , subscriptionType);
    }    

    public static void deassociateMsisdnWithSubscription(Context ctx, String msisdn, String subscriberId, String resourceRef) throws HomeException
    {
        String loggingHeader = "";
        if (LogSupport.isDebugEnabled(ctx))
        {
            loggingHeader = "[MsisdnManagement::deassociateMsisdnWithSubscription (msisdn=" + msisdn
                + ", subId=" + subscriberId + ")] ";
        }
        final Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
        if (subscriber == null)
        {
            final String msg = "Unable to find Subscription [" + subscriberId
                    + "] in our system to deassociate with MSISDN [" + msisdn
                    + "]. Please ensure the Subscription is provisioned in the system "
                    + "prior to attempt to deassociate with MSISDN.";
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(MsisdnManagement.class, loggingHeader + msg, null).log(ctx);
            }
            throw new HomeException(msg);
        }
        deassociateMsisdnWithSubscription(ctx, msisdn, subscriber, resourceRef);
        return;
    }

    public static void deassociateMsisdnWithSubscription(Context ctx, String msisdn, Subscriber subscriber, String resourceRef) throws HomeException
    {
        String loggingHeader = "[MsisdnManagement::deassociateMsisdnWithSubscription (msisdn=" + msisdn +", subId=" + subscriber.getId() + ")] "; 

        new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Request to deassociate MSISDN subscription has been made.  About to retrieve MSISDN record...", null).log(ctx);
        Msisdn originalMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);
        final String ban = subscriber.getBAN();
        if(originalMsisdn == null)
        {
        	
        	//Fix provided for Jira : TCBI-13695
         	  	// Forcefully delete the deactivated subscriber, When "Remove deactivate out-of-dated Subscriber from All application" task is running and MSISDN not available in system
         	if(ctx.getBoolean(CronConstants.SUBSCRIBER_CLEANUP_TASK, false) && subscriber.getState().equals(SubscriberStateEnum.INACTIVE))
         	
                	{
                		 if(LogSupport.isDebugEnabled(ctx))
         	   	     {
               	        LogSupport.debug(ctx,MsisdnManagement.class, "Removing Subsciber [" + subscriber.getBAN() + " ]. from system as MSISDN [" +msisdn +" ] already removed from system..", null);
                	     }
                		return;
                	}
        	
            new DebugLogMsg(MsisdnManagement.class, loggingHeader + "Unable to find a MSISDN record for the msisdn specified!  Request to deassociate MSISDN will be rejected!", null).log(ctx);
            throw new HomeException("Unable to find the specified MSISDN in our system to deassociate with subscription.  Please ensure the MSISDN is provisioned and in use in the system prior to attempt to deassociate with subscription.");
        }
        adjustHistoryForSubDisassociation(ctx, msisdn, subscriber);
        try
        {
            if(SpidSupport.getCRMSpid(ctx, subscriber.getSpid()).isAutoReleaseMSISDN())
            {
                releaseMsisdn(ctx, msisdn, ban, "Disassciation with Subscription");
            }
            
        } catch(Throwable t)
        {
            new DebugLogMsg(MsisdnManagement.class, "MSISDN ["+ msisdn + "] release after disassociation from Subscription was rejected. It can be ingnored because the MSISDN ["+ msisdn +"] could have vlaid reasons to stay acquired by the account ["+ ban + "].", t).log(ctx);
        }
        
    }
    
    private static void adjustHistoryForBanAssociation(Context ctx, String msisdn, Account account, String resourceRef , long subscriptionType) throws HomeException
    {
        Date currentDate = new Date();
        final Home historyHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        // check that the MSISDN isn't already associated with the subscription (causes a little gap in the time in the history) in which case we are trivially done
        
        if(LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx,MsisdnManagement.class, "MSISDN [" + msisdn + " ]. About to check if the last record for this subscription type  and msisdn to see if the association is already present...", null);
        }
        
        Map<String, List<MsisdnMgmtHistory>> historyList = getSubMsisdnHistoryMapForBan(ctx, msisdn, subscriptionType, currentDate);

        if (historyList.size() > 1)
        {
            new MinorLogMsg( MsisdnManagement.class, "MSISDN [" + msisdn + " ].Specified MSISDN [ " + msisdn + " ] has more 1 subscriber/account", null).log(ctx);
        }
        List<MsisdnMgmtHistory> requiredHistory = historyList.remove(account.getBAN());
        for(Map.Entry<String, List<MsisdnMgmtHistory>> subMsisdnHistory : historyList.entrySet())
        { 
            final String ban = subMsisdnHistory.getKey();
            final Collection<MsisdnMgmtHistory> history = subMsisdnHistory.getValue();
            
            if(LogSupport.isDebugEnabled(ctx))
            {
            	LogSupport.debug(ctx, MsisdnManagement.class, "MSISDN [" + msisdn + " ].Updating end-Timestamp to finish the current association with Account  [" + ban + "]", null);
            }
                   
            try
            {
                Visitors.forEach(ctx, history, new ToggleLatestSubscriberVisitor(false, ban, currentDate, historyHome));
            }
            catch (AgentException e)
            {
                throw new HomeException(e);
            }
        }        
        
        if(null == requiredHistory || requiredHistory.isEmpty())
        {
            MsisdnMgmtHistory history =  buildCurrentHistoryRecordForBanModificationEvent(ctx, msisdn, account, currentDate, resourceRef, subscriptionType);
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx,MsisdnManagement.class, "Appending subscription history to MsisdnMgmtHistory table [record=" + history + "]", null);
            }
            historyHome.create(ctx, history);
        } 
        else
        {
            LogSupport.debug(ctx, MsisdnManagement.class, "Current MSISDN history in MsisdnMgmtHistory table for MSISDN [" +msisdn +"] successfully!  Association between MSISDN [ " + msisdn + " ] and subscription [ " + account.getBAN() + " ] already exists!", null);
        }
    }    
    
    private static void adjustHistoryForSubAssociation(Context ctx, String msisdn, Subscriber subscriber, String resourceRef) throws HomeException
    {
        Date curDate = new Date();
        //System.out.print("******* ["+curDate+"] - adjustHistoryForSubAssociation Msisdn ["+ msisdn + "] Subscriber [" + subscriber.getId()  + "] **********");
        //Thread.dumpStack();
        final Home historyHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        // check that the MSISDN isn't already associated with the subscription (causes a little gap in the time in the history) in which case we are trivially done
        new DebugLogMsg(MsisdnManagement.class, "MSISDN [" + msisdn + " ]. About to check if the last record for this subscription type  and msisdn to see if the association is already present...", null).log(ctx);
        Map<String, List<MsisdnMgmtHistory>> historyList = getSubMsisdnHistoryMap(ctx, msisdn, subscriber.getSubscriptionType(), curDate);
        if (historyList.size() > 1)
        {
            new MinorLogMsg( MsisdnManagement.class, "MSISDN [" + msisdn + " ].Specified MSISDN [ " + msisdn + " ] has more 1 subscriber", null).log(ctx);
        }
        List<MsisdnMgmtHistory> requiredHistory = historyList.remove(subscriber.getId());
        for(Map.Entry<String, List<MsisdnMgmtHistory>> subMsisdnHistory : historyList.entrySet())
        { 
            final String subID = subMsisdnHistory.getKey();
            final Collection<MsisdnMgmtHistory> history = subMsisdnHistory.getValue();
            new DebugLogMsg(MsisdnManagement.class, "MSISDN [" + msisdn + " ].Updating end-Timestamp to finish the current association with Sub  [" + subID + "]", null)
                    .log(ctx);
            try
            {
                Visitors.forEach(ctx, history, new ToggleLatestSubscriberVisitor(false, subID, curDate, historyHome));
            }
            catch (AgentException e)
            {
                throw new HomeException(e);
            }
        } 
        if(null == requiredHistory || requiredHistory.isEmpty())
        {
            MsisdnMgmtHistory history =  buildCurrentHistoryRecord(ctx, msisdn, subscriber, curDate, resourceRef);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(MsisdnManagement.class, "Appending subscription history to MsisdnMgmtHistory table [record=" + history + "]", null).log(ctx);
            }
            historyHome.create(ctx, history);
        } 
        else
        {
            new DebugLogMsg(MsisdnManagement.class, "Current MSISDN history in MsisdnMgmtHistory table for MSISDN [" +msisdn +"] successfully!  Association between MSISDN [ " + msisdn + " ] and subscription [ " + subscriber.getId() + " ] already exists!", null).log(ctx);
        }
    }
    
    
    public static void portOutMsisdn(Context ctx, String msisdn) throws HomeException
    {
        portOutMsisdn(ctx, msisdn, null);
    }
    
    public static void portOutMsisdn(Context ctx, String msisdn, String ban) throws  HomeException
    {
        if (ban == null || ban.isEmpty())
        {
            Msisdn msisdnBean;
            {
                msisdnBean = MsisdnSupport.getMsisdn(ctx, msisdn);
                if (null == msisdnBean)
                {
                    throw new HomeException("Could not find MSISDN[" + msisdn + "] entity.");
                }
                ban = msisdnBean.getBAN();
            }
        }
       // Release MSISDN will fail if the MSISDNMGMTHISOTRY has this MSISDN in Use
        MsisdnManagement.releaseMsisdn(ctx, msisdn, ban, "Port out");
        // get the MSISDN state again, after release
        Msisdn msisdnBean = MsisdnSupport.getMsisdn(ctx, msisdn);
        if (MsisdnStateEnum.HELD != msisdnBean.getState())
        {
            new MinorLogMsg(MsisdnManagement.class, "Msisdn [" + msisdn + "] is in [ " + msisdnBean.getState()
                    + " ]. It should be in Held State as soon as it is released from BAN " + ban
                    + "]. Forcing in-held state for porting out", null).log(ctx);
            msisdnBean.setState(MsisdnStateEnum.HELD);
        }
        // A MSISDN that was ported in was never ours. We don't mark it ported out if goes but keep it in-held state.
        if(PortingTypeEnum.IN != msisdnBean.getPortingType())
        {
            msisdnBean.setPortingType(PortingTypeEnum.OUT);
        }
        HomeSupportHelper.get(ctx).storeBean(ctx, msisdnBean);
    }
    
    /*
     * Call this method only if you believe the MSISDN is being ported in
     */
    public static void markMsisdnPortedIn(Context ctx, String ban , String msisdn) throws HomeException
    {
        ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, msisdn);
        Msisdn msisdnBean = MsisdnSupport.getMsisdn(ctx, msisdn);
        if(MsisdnStateEnum.AVAILABLE != msisdnBean.getState())
        {
            if (!msisdnBean.getBAN().equals(ban))
            {
                if (MsisdnStateEnum.IN_USE == msisdnBean.getState())
                {
                    throw new HomeException("The MSISDN is already IN_USE by account [" + msisdnBean.getBAN() + "]. First release the MSISDN.");
                }
                else
                {
                    msisdnBean.setState(MsisdnStateEnum.HELD);
                    msisdnBean.setBAN(ban);
                }
            }
        }
        if (PortingTypeEnum.OUT == msisdnBean.getPortingType())
        {
            // it was ours, we got it back
            msisdnBean.setPortingType(PortingTypeEnum.NONE);
        }
        else if (PortingTypeEnum.NONE == msisdnBean.getPortingType())
        {
            throw new HomeException("MSISDN Already owned by the network. Invalid Port-In request");
        }
        else
        {
            // we aren't the original owners, would never be
            msisdnBean.setPortingType(PortingTypeEnum.IN);
        }
        HomeSupportHelper.get(ctx).storeBean(ctx, msisdnBean);
    }
    
    
    private static void adjustHistoryForSubDisassociation(Context ctx, String msisdn, Subscriber subscriber) throws HomeException
    {
        
        Date curDate = new Date();
        //System.out.print("******* ["+curDate+"] - adjustHistoryForSubDisassociation Msisdn ["+ msisdn + "] Subscriber [" + subscriber.getId()  + "] **********");
        //Thread.dumpStack();
        final Home historyHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        String subID = subscriber.getId();
        new DebugLogMsg(MsisdnManagement.class,"MSISDN ["  + msisdn + " ]. Checking out the current state from history",  null).log(ctx);
        Map<String, List<MsisdnMgmtHistory>> historyList = getSubMsisdnHistoryMap(ctx, msisdn,subscriber.getSubscriptionType(), curDate);
        if (historyList.size() > 1)
        {
            new MinorLogMsg(MsisdnManagement.class, "MSISDN [" + msisdn + " ].Specified MSISDN [ " + msisdn + " ] has more 1 subscriber", null).log(ctx);
        }
        List<MsisdnMgmtHistory> history = historyList.remove(subID);
        if(null != history && !history.isEmpty())
        {
            new DebugLogMsg(MsisdnManagement.class, "MSISDN [" + msisdn + " ].Updating end-Timestamp to finish the current association with Sub  [" + subID + "]", null).log(ctx);
            try
            {
                Visitors.forEach(ctx, history, new ToggleLatestSubscriberVisitor(false, subscriber.getId(), new Date(), historyHome));
            }
            catch (AgentException e)
            {
                throw new HomeException(e);
            }
        } else
        {
            new DebugLogMsg(MsisdnManagement.class ,"MSISDN [" + msisdn + " ].No association existed for the Sub ["+ subID +"]", null).log(ctx);
        }
        
    }
    
    
    public static Map<String, List<MsisdnMgmtHistory>> getSubMsisdnHistoryMap(Context ctx, String msisdn, long subscriptionTypeID , Date curDate) throws HomeException
    {
        final Predicate filter = new And().add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn))
                .add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIPTION_TYPE, subscriptionTypeID))
                .add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP, curDate))
                .add(new GT(MsisdnMgmtHistoryXInfo.END_TIMESTAMP, curDate));
        @SuppressWarnings("unchecked")
        final Collection<MsisdnMgmtHistory> historyList = ((Home) ctx.get(MsisdnMgmtHistoryHome.class)).select(ctx,
                filter);
        return buildSubMisdnHistoryMap(historyList);
    }
    
    private static Map<String, List<MsisdnMgmtHistory>> getSubMsisdnHistoryMapForBan(Context ctx, String msisdn, long subscriptionTypeID , Date curDate) throws HomeException
    {
        final Predicate filter = new And().add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn))
                .add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIPTION_TYPE, subscriptionTypeID))
                .add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP, curDate))
                .add(new GT(MsisdnMgmtHistoryXInfo.END_TIMESTAMP, curDate));
        @SuppressWarnings("unchecked")
        final Collection<MsisdnMgmtHistory> historyList = ((Home) ctx.get(MsisdnMgmtHistoryHome.class)).select(ctx,
                filter);

        final Map<String, List<MsisdnMgmtHistory>> subHistoryMap = new HashMap<String, List<MsisdnMgmtHistory>>();
       
        for (MsisdnMgmtHistory history : (Collection<MsisdnMgmtHistory>) historyList)
        {
            String ban = history.getBAN();
            if (null != ban && !ban.isEmpty())
            {
                List<MsisdnMgmtHistory> histories = subHistoryMap.get(ban);
                if (null == histories)
                {
                    histories = new ArrayList<MsisdnMgmtHistory>();
                    subHistoryMap.put(ban, histories);
                }
                histories.add(history);
            }
        }
    
        
        return subHistoryMap;
    }    
    
    
    public static Map<String, List<MsisdnMgmtHistory>> buildSubMisdnHistoryMap(Collection<MsisdnMgmtHistory> historyList)
    {
        final Map<String, List<MsisdnMgmtHistory>> subHistoryMap;
        {
            subHistoryMap = new HashMap<String, List<MsisdnMgmtHistory>>();
            for (MsisdnMgmtHistory history : (Collection<MsisdnMgmtHistory>) historyList)
            {
                String subId = history.getSubscriberId();
                if (null != subId && !subId.isEmpty())
                {
                    List<MsisdnMgmtHistory> histories = subHistoryMap.get(subId);
                    if (null == histories)
                    {
                        histories = new ArrayList<MsisdnMgmtHistory>();
                        subHistoryMap.put(subId, histories);
                    }
                    histories.add(history);
                }
            }
        }
        return subHistoryMap;
    }
    
    
    private static MsisdnMgmtHistory buildCurrentHistoryRecord(Context ctx, String msisdn,Subscriber subscriber, Date curDate, String resourceRef) throws HomeException
    {
    	final MsisdnMgmtHistory history = new MsisdnMgmtHistory();
        
        Date effectiveDate = (Date) ctx.get(MsisdnChangeAppendHistoryHome.MSISDN_EFFECTIVE_DATE,null);
    	if(effectiveDate == null)
    	{
    		if(subscriber.getStartDate()!=null)
    		{
    			if(subscriber.getStartDate().before(new Date()))
    			{
    				history.setTimestamp(subscriber.getStartDate());
    			}
    			else
    			{
    				history.setTimestamp(curDate);
    			}
    		}
    		else
    		{
    			history.setTimestamp(curDate);
    		}
    	}
    	
    	else if(effectiveDate.after(new Date()))
		{
    		history.setTimestamp(curDate);
		}

    	else
    	{
    		history.setTimestamp(effectiveDate);
    	}
        
        history.setTerminalId(msisdn);
        history.setSubscriberId(subscriber.getId());
        history.setSubscriptionType(subscriber.getSubscriptionType());
        history.setEvent(getHistoryEventSupport(ctx).getSubIdModificationEvent(ctx).getId());
        history.setUserId(getUserId(ctx));
        history.setDetail("migrate "+ resourceRef+" to ["+msisdn+"]");
        history.setLatest(true);
        return history;
    }
    
    private static MsisdnMgmtHistory buildCurrentHistoryRecordForBanModificationEvent(Context ctx , String msisdn , Account account , Date currentDate , String resourceRef , long subscriptionType)
    	throws HomeException
    {
        final MsisdnMgmtHistory history = new MsisdnMgmtHistory();
        history.setTimestamp(currentDate);
        history.setTerminalId(msisdn);
        history.setSubscriberId("");
        history.setSubscriptionType(subscriptionType);
        history.setEvent(getHistoryEventSupport(ctx).getBANModificationEvent(ctx).getId());
        history.setUserId(getUserId(ctx));
        history.setDetail("migrate "+ resourceRef+" to ["+msisdn+"]");
        history.setLatest(true);
        history.setBAN(account.getBAN());
        return history;    	
    }
    
    /**
     * Returns the history support class.
     *
     * @param ctx
     *            The operating context.
     * @return The history support class in the current context.
     */
    private static HistoryEventSupport getHistoryEventSupport(final Context ctx)
    {
        return (HistoryEventSupport) ctx.get(HistoryEventSupport.class);
    }

    /**
     * Returns the user ID to be used for appending history in the provided context.
     *
     * @param ctx
     *            The operating context.
     * @return The user ID to be used for appending history items. This is the name of the
     *         current user of the context. If the context has no user associated with it,
     *         {@link AppendNumberMgmtHistoryHome#DEFAULT_USERID} is used.
     */
    public static String getUserId(final Context ctx)
    {
        final AuthMgr authMgr = new AuthMgr(ctx);
        final User principal = (User) authMgr.getPrincipal();
        // FIXME: there will be no principal in a multinode envirnoment.
        if (principal != null)
        {
            return principal.getId();
        }
        return DEFAULT_USERID;
    }

    /**
     * Throw HomeException with a cause exception, so the given error message is shown on the screen in 
     * a controlled Errors dialogue box.
     * We would rather throw errors this way, because these are known error scenarios. 
     * Throwing HomeException(Msg) should be reserved to alert the user that something in the system is wrong.
     * @param ctx
     * @param msg Error message that should be displayed on the screen.
     */
    private static void throwException(Context ctx, String msg)
        throws HomeException
    {
        throw new HomeException(msg, new CompoundIllegalStateException(new IllegalStateException(msg)));
    }


}

