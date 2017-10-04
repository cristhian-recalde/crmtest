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
package com.trilogy.app.crm.transfer.membergroup;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.transferfund.rmi.api.membergroup.MemberGroupServiceException;
import com.trilogy.app.transferfund.rmi.api.membergroup.MemberGroupServiceInternalException;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.data.ContractGroup;
import com.trilogy.app.transferfund.rmi.data.ContractGroupMember;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;


public class RMINullMemberGroupExternalService extends RMIMemberGroupExternalService
{

    public RMINullMemberGroupExternalService(Context ctx, String hostname, int port, String service)
    {
        super(ctx, hostname, port, service);
    }


    public RMINullMemberGroupExternalService(Context ctx, String service) throws HomeException
    {
        super(ctx, service);
    }


    public RMINullMemberGroupExternalService(Context ctx) throws IllegalArgumentException
    {
        super(ctx);
    }
    
    public ContractGroupMember addContractGroupMember(AuthCredentials authCredentials, ContractGroupMember contractGroupMember, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return null;
    }
    
    public void removeMemberFromContractGroup(AuthCredentials authCredentials, long groupId, String chargingId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
    }
    
    public int deleteContractGroupMember(AuthCredentials authCredentials, String chargingId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return 0;
    }
    
    public ContractGroupMember[] retrieveContractGroupMembers(AuthCredentials authCredentials, long groupId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return null;
    }
    public ContractGroup[] retrieveContractGroupsForMember(AuthCredentials authCredentials, String chargingId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return null;
    }
    
    public ContractGroupMember addContractGroupMember(Context ctx, AuthCredentials authCredentials, ContractGroupMember contractGroupMember, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return null;
    }
    public void removeMemberFromContractGroup(Context ctx, AuthCredentials authCredentials, long groupId, String chargingId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {            
    }
    
    public int deleteContractGroupMember(Context ctx, AuthCredentials authCredentials, String chargingId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return 0;
    }
    
    public ContractGroupMember[] retrieveContractGroupMembers(Context ctx, AuthCredentials authCredentials, long groupId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return null;
    }
    
    public ContractGroup[] retrieveContractGroupsForMember(Context ctx, AuthCredentials authCredentials, String chargingId, String erRef)
    throws MemberGroupServiceException, MemberGroupServiceInternalException, ContractProvisioningException
    {
        return null;
    }
}
