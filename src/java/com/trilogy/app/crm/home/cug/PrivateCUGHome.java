package com.trilogy.app.crm.home.cug;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.home.ClosedUserGroupServiceHome;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class PrivateCUGHome
extends HomeProxy
{
	
	public PrivateCUGHome(Home delegate)
	{
		super(delegate); 
	}
	
	public Object create(final Context ctx, final Object obj)
	throws HomeException
	{
		ClosedUserGroup cug = (ClosedUserGroup) obj;
		AuxiliaryService auxservice = cug.getAuxiliaryService(ctx);
		if(auxservice == null)
		{
		    throw new HomeException ("Auxiliary Service not found for CUG template : " + cug.getCugTemplateID());
		}
    	if (auxservice.isPrivateCUG(ctx))
		{		
			if (cug.getOwnerMSISDN() == null || cug.getOwnerMSISDN().trim().length() < 1)
			{
				throw new HomeException ("Owner MSISDN must be specified for Private CUG."); 		
			} 
			if (cug.getOwner(ctx) == null)
			{
				throw new HomeException("Private CUG Owner MSISDN subscription must be a valid CRM postpaid subscription."); 
			}  
				
			if (cug.getOwner(ctx).getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
			{
				throw new HomeException("Private CUG Owner MSISDN subscription must be a postpaid subscription."); 
				
			}
			if (cug.getSpid()!= cug.getOwner(ctx).getSpid())
			{
				throw new HomeException("Private CUG Owner MSISDN subscription must have the same spid as the Private CUG."); 
				
			}
			
			if( ClosedUserGroupSupport73.isOwnerInActiveState(ctx, cug))
			{				
			    ClosedSub ownerSub = new ClosedSub();
			    ownerSub.setPhoneID(cug.getOwnerMSISDN());
				cug.getSubscribers().put(cug.getOwnerMSISDN(),ownerSub); 
			} 
			else 
			{
                throw new HomeException("Private CUG Owner MSISDN subscription must be in a chargeable state (ACTIVE or PTP)."); 
			}
			
		}
	    return super.create(ctx, obj); 
	}
	    
	    
	public Object store(final Context ctx, final Object obj)
	throws HomeException
	{
		ClosedUserGroup cug = (ClosedUserGroup) obj;
    	if (cug.getAuxiliaryService(ctx).isPrivateCUG(ctx))
		{		
				if(!cug.getSubscribers().keySet().contains(cug.getOwnerMSISDN()))
				{
					throw new HomeException ("Owner MSISDN must be in the close user subscriber list"); 					
				}
				
				if (!ClosedUserGroupSupport73.isOwnerInActiveState(ctx, cug))
				{
					if ( hasNewMember(ctx, cug))
					{
						throw new HomeException("Private CUG Owner MSISDN subscription is not in ACTIVE state, cannot add new member to Private CUG");
					}
				}
		}	
	  	return super.store(ctx, obj); 
	}
	
	
	private static boolean hasNewMember(Context ctx, ClosedUserGroup cug)
	throws HomeException
	{
	    ClosedUserGroup oldCug = (ClosedUserGroup) ctx.get(ClosedUserGroupServiceHome.OLD_CUG);
		Collection members = new ArrayList(); 
		
		members.addAll(cug.getSubscribers().keySet());
		members.removeAll(oldCug.getSubscribers().keySet()); 
		
		return members.size() != 0; 
		
	}
	    
}
