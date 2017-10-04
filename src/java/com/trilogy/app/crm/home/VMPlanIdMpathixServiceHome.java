/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.VMPlan;
import com.trilogy.app.crm.voicemail.VoiceMailManageInterface;
import com.trilogy.app.crm.voicemail.client.MpathixClient;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AbstractClassAwareHome;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitor;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 14, 2005
 */
public class VMPlanIdMpathixServiceHome extends AbstractClassAwareHome
{

    private static final long serialVersionUID = 1L;


    public VMPlanIdMpathixServiceHome(Context ctx)
    {
        super(ctx, VMPlan.class);
    }


    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        throw new UnsupportedOperationException("NOP");
    }


    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        throw new UnsupportedOperationException("NOP");
    }


    public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        return getVMPlan(ctx, (String) obj);
    }


    public Collection select(Context ctx, Object obj) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        return getAllVMPlans(ctx);
    }


    public void remove(Context ctx, Object obj) throws HomeException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("NOP");
    }


    public void removeAll(Context ctx, Object obj) throws HomeException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("NOP");
    }


    public Visitor forEach(Context ctx, Visitor visitor, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException("NOP");
    }


    private List getAllVMPlans(Context ctx) throws HomeException
    {
        MpathixClient client = (MpathixClient) ctx.get(VoiceMailManageInterface.class); 
        return client.getAllVMPlans(ctx); 
    }


    private Object getVMPlan(Context ctx, String planId) throws HomeException
    {
        MpathixClient client = (MpathixClient) ctx.get(VoiceMailManageInterface.class); 
        return client.getVMPlan(ctx, planId); 

    }
}