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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;

/**
 * This decorator home updates the corresponding Msisdn Group's fields like
 * free msisdns, held msisdns, in-use msisdns only if the Common.MOBILE_NUMBER_MONITOR license is enabled.
 *
 * @author manda.subramanyam@redknee.com
 */
public class MobileNumPoolMonitorHome extends HomeProxy
{
	
	static Map<Integer, Object> grpIdToLockMap = Collections.synchronizedMap(new LinkedHashMap(500 * 2, 0.5f, true)
    {
		@Override
        protected boolean removeEldestEntry(Map.Entry eldest)
        {
           return size() > 500;
        }

		@Override
		public Object get(Object key) 
		{
			Object value = super.get(key);
			if(value == null)
			{
				value = new Object();
				put(key, value);
			}
			
			return value;
		} 

        	
     });
    private static final String UNKNOWN_MSISDN_STATE_KEY = "UNKNOWN_MSISDN_STATE";
    private static final String UNKNOWN_MSISDN_STATE = "Unknown MSISDN state {0} while updateting MSISDN group.";

    /**
     * Creates a new MobileNumPoolMonitorHome.
     *
     * @param delegate The Home to which we delegate.
     */
    public MobileNumPoolMonitorHome(final Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final Msisdn msisdn = (Msisdn) super.create(ctx, obj);

        final LicenseMgr manager = (LicenseMgr) ctx.get(LicenseMgr.class);

        if (!manager.isLicensed(ctx, Common.MOBILE_NUMBER_MONITOR))
        {
            // nothing to do if feature not licensed
            return msisdn;
        }

        final int msisdnGrpId = msisdn.getGroup();
        final Home grpHome = (Home) ctx.get(MsisdnGroupHome.class);
        
        Object lock = grpIdToLockMap.get(msisdn.getGroup());
        
        synchronized(lock){
        	MsisdnGroup msisdnGroup = (MsisdnGroup) grpHome.find(ctx, Integer.valueOf(msisdnGrpId));
        	addMSISDNtoGroup(ctx, msisdnGroup, msisdn.getState());
        	grpHome.store(ctx, msisdnGroup);
        }

        return msisdn;
    }

    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        super.remove(ctx, obj);

        final LicenseMgr manager = (LicenseMgr) ctx.get(LicenseMgr.class);

        if (!manager.isLicensed(ctx, Common.MOBILE_NUMBER_MONITOR))
        {
            // nothing to do if feature not licensed
            return;
        }

        final Msisdn msisdn = (Msisdn) obj;
        final Home grpHome = (Home) ctx.get(MsisdnGroupHome.class);
        
        Object lock = grpIdToLockMap.get(msisdn.getGroup());
        

        synchronized(lock){
        	MsisdnGroup msisdnGroup = (MsisdnGroup) grpHome.find(ctx, Integer.valueOf(msisdn.getGroup()));
        	substractMSISDNfromGroup(ctx, msisdnGroup, msisdn.getState());
        	grpHome.store(ctx, msisdnGroup);
        }
    }

    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final LicenseMgr manager = (LicenseMgr) ctx.get(LicenseMgr.class);

        if (!manager.isLicensed(ctx, Common.MOBILE_NUMBER_MONITOR))
        {
            // nothing to do if feature not licensed
            return super.store(ctx, obj);
        }

        Msisdn msisdn = (Msisdn) obj;

        final Home msisdnHome = (Home) ctx.get(MsisdnHome.class);
        Msisdn oldMsisdn = (Msisdn) msisdnHome.find(ctx, new EQ(MsisdnXInfo.MSISDN, msisdn.getMsisdn()));
        oldMsisdn = oldMsisdn==null ? msisdn : oldMsisdn;
        
        msisdn = (Msisdn) super.store(ctx, obj);
        final int msisdnGrpId = msisdn.getGroup();
        final int oldMsisdnGrpId = oldMsisdn.getGroup();
        
        if (oldMsisdn.getState() != msisdn.getState())
        {
            // retrieve and store the group only if the state change requires the group values update
            final Home grpHome = (Home) ctx.get(MsisdnGroupHome.class);

            Object lock = grpIdToLockMap.get(msisdnGrpId);
            

            synchronized(lock){
            	MsisdnGroup msisdnGroup = (MsisdnGroup) grpHome.find(ctx, Integer.valueOf(msisdnGrpId));
            	addMSISDNtoGroup(ctx, msisdnGroup, msisdn.getState());
            	substractMSISDNfromGroup(ctx, msisdnGroup, oldMsisdn.getState());
            	grpHome.store(ctx, msisdnGroup);
            }
        }
        /* Support for MSISDN group updates as a part of TCB 9.5.7 - Dory Functional Gap : Issue 2 (TT 13123104005) */
        if (msisdnGrpId!=oldMsisdnGrpId)
         {
             final Home grpHome1 = (Home) ctx.get(MsisdnGroupHome.class);
    
             synchronized(this){
                 MsisdnGroup msisdnGroup = (MsisdnGroup) grpHome1.find(ctx, Integer.valueOf(msisdnGrpId));
                 MsisdnGroup oldMsisdnGroup = (MsisdnGroup) grpHome1.find(ctx, Integer.valueOf(oldMsisdnGrpId));
                 
                 addMSISDNtoGroup(ctx, msisdnGroup, oldMsisdn.getState());
                 substractMSISDNfromGroup(ctx, oldMsisdnGroup, oldMsisdn.getState());
                 grpHome1.store(ctx, msisdnGroup);
                 grpHome1.store(ctx, oldMsisdnGroup);
             }
         }          

        return msisdn;
    }

    private void substractMSISDNfromGroup(final Context ctx, final MsisdnGroup group, final MsisdnStateEnum state)
    {
        switch (state.getIndex())
        {
            case MsisdnStateEnum.AVAILABLE_INDEX:
                group.setAvailableMsisdns(group.getAvailableMsisdns() - 1);
                break;
            case MsisdnStateEnum.IN_USE_INDEX:
                group.setAssignedMsisdns(group.getAssignedMsisdns() - 1);
                break;
            case MsisdnStateEnum.HELD_INDEX:
                group.setNumberHeld(group.getNumberHeld() - 1);
                break;
            default:
                logUnknownStateError(ctx, state.getIndex());
                break;
        }
    }

    private void addMSISDNtoGroup(final Context ctx, final MsisdnGroup group, final MsisdnStateEnum state)
    {
        switch (state.getIndex())
        {
            case MsisdnStateEnum.AVAILABLE_INDEX:
                group.setAvailableMsisdns(group.getAvailableMsisdns() + 1);
                break;
            case MsisdnStateEnum.IN_USE_INDEX:
                group.setAssignedMsisdns(group.getAssignedMsisdns() + 1);
                break;
            case MsisdnStateEnum.HELD_INDEX:
                group.setNumberHeld(group.getNumberHeld() + 1);
                break;
            default:
                logUnknownStateError(ctx, state.getIndex());
                break;
        }
    }

    private void logUnknownStateError(final Context ctx, final short state)
    {
        final MessageMgr mgr = new MessageMgr(ctx, this);
        final String msg = mgr.get(UNKNOWN_MSISDN_STATE_KEY, UNKNOWN_MSISDN_STATE,
                new Object[]{Integer.valueOf(state)});
        LogSupport.minor(ctx, this, msg);
    }
}
