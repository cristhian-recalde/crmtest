/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily available.
 * Additionally, source code is, by its very nature, confidential information and
 * inextricably contains trade secrets and other information proprietary, valuable and
 * sensitive to Redknee, no unauthorised use, disclosure, manipulation or otherwise is
 * permitted, and may only be used in accordance with the terms of the licence agreement
 * entered into with Redknee Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transfer.membergroup;

import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.TfaRmiConfig;
import com.trilogy.app.crm.transfer.contract.tfa.TransferContractSupport;
import com.trilogy.app.transferfund.rmi.api.membergroup.MemberGroupService;
import com.trilogy.app.transferfund.rmi.api.membergroup.MemberGroupServiceException;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.data.ContractGroup;
import com.trilogy.app.transferfund.rmi.data.ContractGroupMember;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;


/**
 * Provides Support methods/data for TFA Interaction.
 * @author ling.tang@redknee.com
 */
public class MemberGroupSupport
{

    /**
     * Adds a member to the specified group on TFA
     * 
     * @param ctx : Context
     * @param spid : Spid
     * @param groupId : Group id to associate the member to
     * @param chargingId : Charging Id of member
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static ContractGroupMember addMemberToContractGroup(Context ctx, int spid, long groupId, String chargingId)
    throws HomeException, MemberGroupException
    {
        ContractGroupMember member = adaptTFAContractGroupMember(ctx, spid, groupId, chargingId);        
        return addMemberToContractGroup(ctx, member);
    }
    
    /**
     * Adds a member to public group on TFA
     * 
     * @param ctx
     * @param ownership
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static ContractGroupMember addMemberToPublicGroup(Context ctx, MsisdnOwnership ownership)
    throws HomeException, MemberGroupException
    {
        ContractGroupMember member = adaptTFAContractGroupMember(ctx, ownership.getSpid(), ownership.getPublicGroupId(), ownership.getMsisdn());        
        return addMemberToContractGroup(ctx, member);
    }
    
    /**
     * Adds a member to private group on TFA
     * 
     * @param ctx
     * @param ownership
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static ContractGroupMember addMemberToPrivateGroup(Context ctx, MsisdnOwnership ownership)
    throws HomeException, MemberGroupException
    {        
        ContractGroupMember member = adaptTFAContractGroupMember(ctx, ownership.getSpid(), ownership.getPrivateGroupId(), ownership.getMsisdn());    
        return addMemberToContractGroup(ctx, member);
    }
    
    /**
     * Provisions contract group member on TFA
     * 
     * @param ctx
     * @param member
     * @return
     * @throws HomeException
     */
    private static ContractGroupMember addMemberToContractGroup(Context ctx, ContractGroupMember member)
    throws HomeException, MemberGroupException
    {
        new DebugLogMsg(MemberGroupSupport.class.getName(), "addMemberToContractGroup [" + member + "]", null).log(ctx);
        
        MemberGroupService client = getMemberGroupServiceClient(ctx);
        try
        {
            return client.addContractGroupMember(getAuthCredentials(ctx), member, getErRef(ctx));
        }
        catch (ContractProvisioningException e)
        {
            throw new MemberGroupException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
        }
    }        
    
    /**
     * Removes a member from public group on TFA
     * 
     * @param ctx
     * @param ownership
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static void removeMemberFromPublicGroup(Context ctx, MsisdnOwnership ownership)
    throws HomeException, MemberGroupException
    {
        removeMemberFromContractGroup(ctx, ownership.getPublicGroupId(), ownership.getMsisdn());
    }
    
    /**
     * Removes a member from private group on TFA
     * 
     * @param ctx
     * @param ownership
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static void removeMemberFromPrivateGroup(Context ctx, MsisdnOwnership ownership)
    throws HomeException, MemberGroupException
    {
        removeMemberFromContractGroup(ctx, ownership.getPrivateGroupId(), ownership.getMsisdn());
    }
    
    /**
     * Removes a member from the specified group on TFA
     * 
     * @param ctx
     * @param ownership
     * @param groupId
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static void removeMemberFromContractGroup(Context ctx, MsisdnOwnership ownership, long groupId)
    throws HomeException, MemberGroupException
    {
        removeMemberFromContractGroup(ctx, groupId, ownership.getMsisdn());
    }    
    
    /**
     * Deprovisions a contract group member on TFA
     * 
     * @param ctx
     * @param groupId
     * @param chargingId
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static void removeMemberFromContractGroup(Context ctx, long groupId, String chargingId)
    throws HomeException, MemberGroupException
    {
        new DebugLogMsg(MemberGroupSupport.class.getName(), "removeMemberFromContractGroup [groupId=" + groupId + ", chargingId=" + chargingId + "]", null).log(ctx);
        
        MemberGroupService client = getMemberGroupServiceClient(ctx);
        try
        {
            client.removeMemberFromContractGroup(getAuthCredentials(ctx), groupId, chargingId, getErRef(ctx));
        }
        catch (ContractProvisioningException e)
        {
            throw new MemberGroupException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
        }
    }
    
    /**
     * Deletes a member from all groups on TFA
     * 
     * @param ctx
     * @param ownership
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static int deleteMemberFromContractGroup(Context ctx, MsisdnOwnership ownership)
    throws HomeException, MemberGroupException
    {
        return deleteMemberFromContractGroup(ctx, ownership.getMsisdn());
    } 
    
    /**
     * Deletes a member from all groups on TFA
     * 
     * @param ctx
     * @param chargingId
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static int deleteMemberFromContractGroup(Context ctx, String chargingId)
    throws HomeException, MemberGroupException
    {
        new DebugLogMsg(MemberGroupSupport.class.getName(), "deleteMemberFromContractGroup [chargingId=" + chargingId + "]", null).log(ctx);
        
        MemberGroupService client = getMemberGroupServiceClient(ctx);
        try
        {
            return client.deleteContractGroupMember(getAuthCredentials(ctx), chargingId, getErRef(ctx));
        }
        catch (ContractProvisioningException e)
        {
            throw new MemberGroupException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);            
        }
    }                    
    
    /**
     * Retrieves all members of the specified group from TFA
     * 
     * @param ctx
     * @param groupId
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static ContractGroupMember[] retrieveContractGroupMembers(Context ctx, long groupId)
    throws HomeException, MemberGroupException
    {
        new DebugLogMsg(MemberGroupSupport.class.getName(), "retrieveContractGroupMembers [groupId=" + groupId +"]", null).log(ctx);
        
        MemberGroupService client = getMemberGroupServiceClient(ctx);  

        try
        {
            return client.retrieveContractGroupMembers(getAuthCredentials(ctx), groupId, getErRef(ctx));
        }
        catch (ContractProvisioningException e)
        {
            throw new MemberGroupException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
        }
        
    }
    
    /**
     * Retrieves all Contract Groups that the member is associated with on TFA
     * 
     * @param ctx
     * @param chargingId : Charging id of member
     * @return
     * @throws HomeException
     * @throws MemberGroupServiceException
     * @throws ContractProvisioningException
     */
    public static ContractGroup[] retrieveContractGroupsForMember(Context ctx, String chargingId)
    throws HomeException, MemberGroupNotFoundException, MemberGroupException
    {
        new DebugLogMsg(MemberGroupSupport.class.getName(), "retrieveContractGroupsForMember [chargingId=" + chargingId + "]", null).log(ctx);
        
        MemberGroupService client = getMemberGroupServiceClient(ctx);  
        try
        {
            return client.retrieveContractGroupsForMember(getAuthCredentials(ctx), chargingId, getErRef(ctx));
        }
        catch (ContractProvisioningException e)
        {
            if (e.getResponseCode() == TransferContractSupport.ERR_TRANSFER_GROUP_NOTEXISTS)
            {
                throw new MemberGroupNotFoundException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
            throw new MemberGroupException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
        }
    }
    
    /**
     * Returns a ContractGroupMember with the given parameters
     * 
     * @param ctx : Context
     * @param spid : spid of the contract group member
     * @param groupId : groupID which the charging ID shall be associated with
     * @param chargingId : the actual chargingID of the subscriber
     * @return : ContractGroupMember object with the given properties
     */
    private static ContractGroupMember adaptTFAContractGroupMember(Context ctx, int spid, long groupId, String chargingId)    
    {
         ContractGroupMember member = new ContractGroupMember();
         member.setSpid(spid);
         member.setGroupId(groupId);
         member.setChargingId(chargingId);
         
         return member;
    }
      
    /**
     * @param ctx
     * @return : TfaRmiMemberGroupExternalService is returned from context.
     * @throws HomeException
     */
    public static MemberGroupService getMemberGroupServiceClient(Context ctx) throws HomeException
    {
        MemberGroupService client = (MemberGroupService) ctx.get(MemberGroupService.class);
        if (client == null)
        {
            throw new HomeException("Could not fetch MemberGroupService from context.");
        }
        return client;
    }

    /**
     * 
     * @param ctx
     * @return : returns authorization credentials for RMI service
     * @throws HomeException
     */
    public static AuthCredentials getAuthCredentials(final Context ctx)
    throws HomeException
    {        
        AuthCredentials authCredentials = new AuthCredentials();
        authCredentials.setUserName(getLoginId(ctx));
        authCredentials.setPassWord(getPassword(ctx));
        return authCredentials;
    }



    /**
     * @param ctx
     * @return : returns loginId of the logged in user
     * @throws HomeException
     */
    public static String getLoginId(final Context ctx) throws HomeException
    {
        TfaRmiConfig config = getConfig(ctx);
        return config.getUsername();
    }

    /**
     * @param ctx
     * @return : returns decrypted password of the logged in user.
     * @throws HomeException
     */
    public static String getPassword(final Context ctx) throws HomeException
    {
        TfaRmiConfig config = getConfig(ctx);
        return config.getPassword(ctx);
    }
    
    /**
     * @param ctx
     * @return : returns ER-Reference sent to TFA for tracking purpose.
     * @throws HomeException
     */
    public static String getErRef(final Context ctx) throws HomeException
    {
        TfaRmiConfig config = getConfig(ctx);
        return config.getErReference();
    }
    
    public static TfaRmiConfig getConfig(final Context ctx) throws HomeException
    {
        TfaRmiConfig config = (TfaRmiConfig) ctx.get(TfaRmiConfig.class);
        if (config == null)
        {
            throw new HomeException("System Error: TfaRmiConfig not found in context");
        }
        return config;
    }
    
}
