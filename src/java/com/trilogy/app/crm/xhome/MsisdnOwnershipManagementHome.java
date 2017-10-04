package com.trilogy.app.crm.xhome;

import java.util.Collection;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupNotFoundException;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupSupport;
import com.trilogy.app.transferfund.rmi.data.ContractGroup;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class MsisdnOwnershipManagementHome extends HomeProxy
{
    public MsisdnOwnershipManagementHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }    

    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        MsisdnOwnership ownership = (MsisdnOwnership)obj;
        String loggingHeader = "[MsisdnOwnershipManagementHome::create (ban=" + ownership.getBAN() +", msisdn=" + ownership.getMsisdn() + ")] ";
        new DebugLogMsg(this, loggingHeader + "User is requesting to create new associate using entry type [entryType=" + ownership.getMsisdnEntryType() + "].", null).log(ctx);
        
        if (ownership.getPublicGroupId() != -1)
        {
            ctx.put(TransferContractSupport.TRANSFER_CONTRACT_PUBLIC_GROUP_ID, ownership.getPublicGroupId());
        }
        if (ownership.getPrivateGroupId() != -1)
        {
            ctx.put(TransferContractSupport.TRANSFER_CONTRACT_PRIVATE_GROUP_ID, ownership.getPrivateGroupId());
        }
        
        try
        {
            MsisdnManagement.claimMsisdn(ctx, ownership.getMsisdn(), ownership.getBAN(), isExternal(ownership), "");
        }
        catch(MsisdnAlreadyAcquiredException e)
        {
            throw new HomeException(e);
        }

        // to properly display the saved entry id in the GUI we need to set the primary key.
        ownership.setOriginalMsisdn(ownership.getMsisdn());
        return super.create(ctx, obj);
    }
    
    private boolean isExternal(MsisdnOwnership ownership)
    {
        return ownership.getMsisdnEntryType() == MsisdnEntryTypeEnum.EXTERNAL_INDEX;
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        MsisdnOwnership ownership = (MsisdnOwnership)obj;
        String loggingHeader = "[MsisdnOwnershipManagementHome::store (ban=" + ownership.getBAN() + ", originalMsisdn=" + ownership.getOriginalMsisdn() + ", newMsisdn=" + ownership.getMsisdn() + ")] "; 
        Msisdn originalMsisdn = MsisdnSupport.getMsisdn(ctx, ownership.getOriginalMsisdn());
        Home subscriberHome = (Home)ctx.get(SubscriberHome.class);
        int numOfFailures = 0;

        if(null == originalMsisdn)
        {
            // cannot find original MSISDN, create a placeholder
            originalMsisdn = new Msisdn();
            originalMsisdn.setBAN(ownership.getBAN());
            originalMsisdn.setMsisdn(ownership.getOriginalMsisdn());
        }

        if (!SafetyUtil.safeEquals(ownership.getOriginalSubscriberType(), ownership.getSubscriberType()))
        {
            new DebugLogMsg(this, loggingHeader + "Detected a subscriber conversion but will not do anything as this is currently not supported!", null).log(ctx);
            //TODO add logic for conversion behavior here.  There will be a need for handling the order of changeMsisdn and conversion.
        }
        
        if (!SafetyUtil.safeEquals(ownership.getOriginalMsisdn(), ownership.getMsisdn()))
        {
            // Change MSISDN is being requested
            new DebugLogMsg(this, loggingHeader + "Request to change the MSISDN received!  About to attempt claiming newMsisdn...", null).log(ctx);
            
            // acquire the new MSISDN
            if (ownership.getPublicGroupId() != -1)
            {
                ctx.put(TransferContractSupport.TRANSFER_CONTRACT_PUBLIC_GROUP_ID, ownership.getPublicGroupId());
            }
            if (ownership.getPrivateGroupId() != -1)
            {
                ctx.put(TransferContractSupport.TRANSFER_CONTRACT_PRIVATE_GROUP_ID, ownership.getPrivateGroupId());
            }

            try
            {
                MsisdnManagement.claimMsisdn(ctx, ownership.getMsisdn(), ownership.getBAN(), isExternal(ownership), "");
            }
            catch(MsisdnAlreadyAcquiredException e)
            {
                throw new HomeException(e);
            }
            new DebugLogMsg(this, loggingHeader + "Successfully claimed the newMsisdn!  About to retrieve subscriptions for oldMsisdn...", null).log(ctx);
        
            // for each subscription using that MSISDN we need to attempt a changeMsisdn on each subscription
            Collection<Subscriber> subscriptions = SubscriberSupport.getSubscriptionsByMSISDN(ctx, ownership.getOriginalMsisdn());
            new DebugLogMsg(this, loggingHeader + "Retrieved " + subscriptions.size() + " subscriptions that are attached to oldMsisdn.  About to attempt changing the MSISDN on each subscription...", null).log(ctx);
            
            for(Subscriber subscription : subscriptions)
            {
                try
                {
                    new DebugLogMsg(this, loggingHeader + "Changing MSISDN on subscription [id=" + subscription.getId() + "]...", null).log(ctx);
                    subscription.setMSISDN(ownership.getMsisdn());
                    subscriberHome.store(subscription);
                    new DebugLogMsg(this, loggingHeader + "Change MSISDN successful for subscription [id=" + subscription.getId() + "]", null).log(ctx);
                }
                catch (Exception e)
                {
                    // Caught a Exception
                    new InfoLogMsg(this, loggingHeader + "Encountered a Exception during attempt to changeMsisdn on subscription [id=" + subscription.getId() + "]", e).log(ctx);
                    numOfFailures++;
                }
            }          

            if(numOfFailures == 0)
            {
                // No failures so we can release the original MSISDN and allow it to move to the HELD state
                new DebugLogMsg(this, loggingHeader + "ChangeMSISDN for all subscriptions was successful.  Releasing originalMsisdn...", null).log(ctx);
                
                MsisdnManagement.releaseMsisdn(ctx, originalMsisdn.getMsisdn(), originalMsisdn.getBAN(), "");
                new DebugLogMsg(this, loggingHeader + "Successfully released the originalMsisdn!", null).log(ctx);

            }
            else
            {
                // Since there are still some subscriptions that failed to changeMsisdn we have to leave the original MSISDN in the IN-USE state
                new InfoLogMsg(this, numOfFailures + " subscriptions failed to changeMsisdn and thus msisdn [originalMsisdn" + ownership.getOriginalMsisdn() + "] will not be released.", null).log(ctx);
                //TODO Need to notify the user of what occured
            }
        }
        else
        // TFA Provisioning logic
        {
            // retrieve contract groups for MSISDN to determine associated public and private contract groups
            ContractGroup[] contractGroups = null;
            try
            {
                contractGroups = MemberGroupSupport.retrieveContractGroupsForMember(ctx, ownership.getMsisdn());
            }
            catch (MemberGroupNotFoundException nfe)
            {
                // this is thrown if the member currently does not belong to any contract groups
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "[MsisdnOwnershipManagementHome::store failed to retrieve contract groups for MSISDN on TFA "
                        + "[msisdn=" + ownership.getMsisdn() + "]", e).log(ctx);
                throw new ProvisioningHomeException(e.getMessage(), e);   
            }
            long oldPrivateGroupId = -1;
            long oldPublicGroupId = -1;
            if (contractGroups != null)
            {
                for (ContractGroup group: contractGroups)
                {
                    if (group.getGroupOwner().equals(TransferContractSupport.getPublicGroupOwnerName(ctx)))
                    {
                        oldPublicGroupId = group.getGroupId();
                    }
                    else
                    {
                        oldPrivateGroupId = group.getGroupId();
                    }
                }
            }
            
            // Private group id changed:
            // 1. Deprovision from old PrivateGroupId
            // 2. Provision to new PrivateGroupId
            if (oldPrivateGroupId != ownership.getPrivateGroupId())
            {
                if (oldPrivateGroupId != -1)
                {
                    new DebugLogMsg(this, "Private group id changed from "+oldPrivateGroupId + " to " + ownership.getPrivateGroupId(), null).log(ctx);
                    try
                    {
                        new DebugLogMsg(this, "Removing MSISDN from private group " + oldPrivateGroupId + " on TFA", null).log(ctx);
                        MemberGroupSupport.removeMemberFromContractGroup(ctx, ownership, oldPrivateGroupId);
                        new DebugLogMsg(this, "Successfully removed MSISDN from private group " + oldPrivateGroupId + " on TFA", null).log(ctx);
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(this, "[MsisdnOwnershipManagementHome::store failed to deprovision MSISDN from old private contract group on TFA "
                                + "[msisdn=" + ownership.getMsisdn()
                                + ", private groupId=" + oldPrivateGroupId + "]", e).log(ctx);
                    }
                }
                
                if (ownership.getPrivateGroupId() != -1)
                {
                    try
                    {
                        new DebugLogMsg(this, "Adding MSISDN to private group " + ownership.getPrivateGroupId() + " on TFA", null).log(ctx);
                        MemberGroupSupport.addMemberToPrivateGroup(ctx, ownership);
                        new DebugLogMsg(this, "Successfully added MSISDN to private group " + ownership.getPrivateGroupId() + " on TFA", null).log(ctx);                        
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(this, "[MsisdnOwnershipHome::store failed to provision MSISDN to new private contract group on TFA "
                                + "[msisdn=" + ownership.getMsisdn()
                                + ", private groupId=" + ownership.getPrivateGroupId() + "]", e).log(ctx);                        
                    }
                }
            }
            
            // Public group id change:
            // 1. Deprovision from old public group id
            // 2. Provision to new public group id
            if (oldPublicGroupId != ownership.getPublicGroupId())
            {
                if (oldPublicGroupId != -1)
                {
                    new DebugLogMsg(this, "Public group id changed from "+ oldPublicGroupId + " to " + ownership.getPublicGroupId(), null).log(ctx);
                    try
                    {
                        new DebugLogMsg(this, "Removing MSISDN from public group " + oldPublicGroupId + " on TFA", null).log(ctx);
                        MemberGroupSupport.removeMemberFromContractGroup(ctx, ownership, oldPublicGroupId);
                        new DebugLogMsg(this, "Successfully removed MSISDN from public group " + oldPublicGroupId + " on TFA", null).log(ctx);
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(this, "[MsisdnOwnershipHome::store failed to deprovision MSISDN from old public contract group on TFA "
                                + "[msisdn=" + ownership.getMsisdn()
                                + ", public groupId=" + oldPublicGroupId + "]", e).log(ctx);
                        
                        numOfFailures++;  
                    }
                }
                
                if (ownership.getPublicGroupId() != -1)
                {
                    try
                    {
                        new DebugLogMsg(this, "Adding MSISDN to public group " + ownership.getPublicGroupId() + " on TFA", null).log(ctx);
                        MemberGroupSupport.addMemberToPublicGroup(ctx, ownership);
                        new DebugLogMsg(this, "Successfully added MSISDN to public group " + ownership.getPublicGroupId() + " on TFA", null).log(ctx);                        
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(this, "[MsisdnOwnershipManagementHome::store failed to provision MSISDN to new public contract group on TFA "
                                + "[msisdn=" + ownership.getMsisdn()
                                + ", public groupId=" + ownership.getPublicGroupId() + "]", e).log(ctx);                        
                    }
                }
            }
        }

        // if everything was successful then technically the new Original MSISDN is the new MSISDN
        ownership.setOriginalMsisdn(ownership.getMsisdn());
        return super.store(ctx, obj);
    }

    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final MsisdnOwnership ownership = (MsisdnOwnership)obj;
        if (LogSupport.isDebugEnabled(ctx))
        {
            final String msg = "[MsisdnOwnershipManagementHome::remove (ban=" + ownership.getBAN()
                    + ", originalMsisdn=" + ownership.getOriginalMsisdn() + ")] "
                    + "Request to release MSISDN received!";

            new DebugLogMsg(this, msg, null).log(ctx);
        }
        MsisdnManagement.releaseMsisdn(ctx, ownership.getOriginalMsisdn(), ownership.getBAN(), "");
        
        super.remove(ctx, obj);
    }

    @Override
    public Object cmd(Context ctx, Object arg)
        throws HomeException
    {
        if(arg instanceof MsisdnOwnershipReAcquireCmd)
        {
            MsisdnOwnershipReAcquireCmd cmd = (MsisdnOwnershipReAcquireCmd)arg;
            String msisdn = cmd.getMsisdn();
            Msisdn msisdnObj = MsisdnSupport.getMsisdn(ctx, msisdn);

            if(null == msisdnObj)
            {
                throw new HomeException("MSISDN [" + msisdn + "] cannot be found in the home.");
            }

            try
            {
                MsisdnManagement.claimMsisdn(ctx, msisdn, msisdnObj.getBAN(), false, "");
            }
            catch(MsisdnAlreadyAcquiredException e)
            {
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "MSISDN [" + msisdn + "] has already been acquired.");
                }
            }

            return super.find(ctx, msisdn);
        }
        else
        {
            return super.cmd(arg);
        }
    }
}
