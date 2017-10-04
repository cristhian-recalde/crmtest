/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteOwnerTypeEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * This home is responsible for taking decision of whether to configshare notes or not.
 * @author sgaidhani
 * @since 9.7.1
 */
public class NotesAutoPushAllowedHome extends HomeProxy
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotesAutoPushAllowedHome(final Context ctx, Home delegate, NoteOwnerTypeEnum owner)
    {
        super(ctx,delegate);
        ownerType_ = owner;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	setAutoPushFlag(ctx, obj);
        return getDelegate().create(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	setAutoPushFlag(ctx, obj);
        return getDelegate().store(ctx, obj);
    }
    
    @Override
    public void remove(Context ctx, Object obj) throws HomeException,
    		HomeInternalException {
    	setAutoPushFlag(ctx, obj);
        getDelegate().remove(ctx, obj);
    }
    
	private void setAutoPushFlag(final Context ctx,
			final Object obj) throws HomeException {
		boolean isSync = true;
		if (ownerType_ == NoteOwnerTypeEnum.ACCOUNT)
		{
			Note note = (Note) obj;
			String banString = note.getIdIdentifier();
			Integer spidInt = AccountSupport.getAccountSpid(ctx, banString);
			if(spidInt != null)
			{
				CRMSpid spid = SpidSupport.getCRMSpid(ctx,spidInt);
				if(spid.getAutopushNotesToDcrm())
				{
					isSync = true;
				}
				else
				{
					isSync = false;
				}
			}

		}
        else if (ownerType_ == NoteOwnerTypeEnum.SUBSCRIPTION)
        {
        	Note note = (Note) obj;
        	String subString = note.getIdIdentifier();
        	Integer spidInt = SubscriberSupport.getSubscriberSpid(ctx, subString);
        	if(spidInt != null)
        	{
        		CRMSpid spid = SpidSupport.getCRMSpid(ctx, spidInt);
        		if(spid.getAutopushNotesToDcrm())
        		{
        			isSync = true;
        		}
        		else
        		{
        			isSync = false;
        		}
        	}
        }
		
		((Note)obj ).setAutoPush(isSync);
		
	}
    private NoteOwnerTypeEnum ownerType_;
}
