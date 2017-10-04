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
package com.trilogy.app.crm.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupException;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupNotFoundException;
import com.trilogy.app.crm.transfer.membergroup.MemberGroupSupport;
import com.trilogy.app.transferfund.rmi.data.ContractGroup;


/**
 * Provides a home for integration with TFA Transfer Contract Management.
 *
 * @author gary.anderson@redknee.com
 */
public class ContractGroupMembershipServiceHome
    extends HomeProxy
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context context, final Object obj)
        throws HomeException
    {
        if (!(obj instanceof ContractGroupMembership))
        {
            throw new HomeException("Invalid create object: " + obj);
        }

        final ContractGroupMembership member = (ContractGroupMembership)obj;

        final Home groupHome = (Home)context.get(ContractGroupHome.class);
        final com.redknee.app.crm.transfer.ContractGroup group =
            (com.redknee.app.crm.transfer.ContractGroup)groupHome.find(context, Long.valueOf(member.getGroupID()));

        if (group == null)
        {
            throw new HomeException("Unable to find contract group with ID " + member.getGroupID());
        }

        final int spid = group.getSpid();
        
        Msisdn msisdn = MsisdnSupport.getMsisdn(context, member.getMobileNumber());
        
        if(msisdn == null || !msisdn.getState().equals(MsisdnStateEnum.IN_USE))
        {
        	throw new HomeException("Mobile number " + member.getMobileNumber() + " is not currently owned by an account in the system.");
        } 

        try
        {
            MemberGroupSupport.addMemberToContractGroup(context, spid, member.getGroupID(), member.getMobileNumber());
        }
        catch (final MemberGroupException exception)
        {
            throw new HomeException("Failed to add mobile number " + member.getMobileNumber() + " to group ID "
                + member.getGroupID(), exception);
        }

        return member;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object find(final Context context, final Object obj)
        throws HomeException
    {
        if (obj instanceof ContractGroupMembershipID)
        {	
        	ContractGroupMembershipID id = (ContractGroupMembershipID)obj;
        	
        	try
			{
				ContractGroup[] groups = MemberGroupSupport.retrieveContractGroupsForMember(context, id.getMobileNumber());
				for (ContractGroup contractGroup : groups)
				{
					if (contractGroup.getGroupId() == id.getGroupID())
					{
						// found a matching contract group
						return obj;
					}
				}
			}
			catch (MemberGroupNotFoundException e)
			{
				new DebugLogMsg(this, "Encountered a MemberGroupNotFoundException while trying to lookup ContractGroups for mobile number " + id.getMobileNumber() + ".", null).log(context);
			}
			catch (MemberGroupException e)
			{
				new DebugLogMsg(this, "Encountered a MemberGroupException while trying to lookup ContractGroups for mobile number " + id.getMobileNumber() + ".", e).log(context);
			}
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context context, final Object obj)
        throws HomeException
    {
        if (!(obj instanceof ContractGroupMembership))
        {
            throw new HomeException("Invalid remove object: " + obj);
        }

        final ContractGroupMembership member = (ContractGroupMembership)obj;

        try
        {
            MemberGroupSupport.removeMemberFromContractGroup(context, member.getGroupID(), member.getMobileNumber());
        }
        catch (final MemberGroupException exception)
        {
            throw new HomeException("Failed to remove mobile number " + member.getMobileNumber() + " from group ID "
                + member.getGroupID(), exception);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection select(final Context context, final Object obj)
        throws HomeException
    {
        final ContractGroupMembershipSearch criteria = getSearchCriteria(context, obj);

        if (criteria == null)
        {
            return Collections.emptyList();
        }

        final List<ContractGroupMembership> results = new ArrayList<ContractGroupMembership>();

        try
        {
            final ContractGroup[] groups =
                MemberGroupSupport.retrieveContractGroupsForMember(context, criteria.getMobileNumber());

            if (groups != null)
            {
                for (final ContractGroup group : groups)
                {
                    if (group.getGroupId() == criteria.getGroupID())
                    {
                        final ContractGroupMembership membership = new ContractGroupMembership();
                        membership.setGroupID(criteria.getGroupID());
                        membership.setMobileNumber(criteria.getMobileNumber());

                        results.add(membership);
                        break;
                    }
                }
            }
        }
        catch (final MemberGroupNotFoundException exception)
        {
            // Nothing to do -- this is not really exceptional.
        }
        catch (final MemberGroupException exception)
        {
            throw new HomeException("Failed to look up membership of mobile number " + criteria.getMobileNumber()
                + " in contract group with ID " + criteria.getGroupID(), exception);
        }

        return results;
    }


    /**
     * Gets the search criteria in the Context.
     *
     * @param context The operating context.
     * @param where The "where" part of the select() call.
     * @return The search criteria if found; null otherwise.
     */
    protected ContractGroupMembershipSearch getSearchCriteria(final Context context, final Object where)
    {
        if (!(where instanceof Context))
        {
            return null;
        }
        
        // Context is longer passed around
        return null;
    }


    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
}
