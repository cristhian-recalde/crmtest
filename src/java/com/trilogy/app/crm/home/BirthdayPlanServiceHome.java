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
package com.trilogy.app.crm.home;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.UserSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlan;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlanHolder;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlanHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlanListHolder;
import com.trilogy.app.ff.ecare.rmi.TrBirthdayPlanListHolderImpl;
/**
 * Redirects home calls to Friends and Family RMI Interface calls.
 * @author victor.stratan@redknee.com
 */
public class BirthdayPlanServiceHome extends HomeProxy
{
    private static final long serialVersionUID = 1L;
    
    public BirthdayPlanServiceHome(final Context ctx)
    {
        super(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.FNF_BIRTHDAYPLAN_LICENSE))
        {
            // do nothing if License is not Enabled. This is a plug. The user freindly error done in Validator.
            return obj;
        }

        new OMLogMsg(Common.OM_MODULE, Common.OM_BP_CREATION_ATTEMPT).log(ctx);

        final BirthdayPlan birthdayPlan = (BirthdayPlan) obj;

        int result = 0;
        TrBirthdayPlan ffBirthdayPlan = null;
        TrBirthdayPlanHolder ffBirthdayPlanHolder = null;

        TrBirthdayPlan createdBirthdayPlan = null;
        String errorReason = "";
        try
        {
            // Hard code the start and end date
            final Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            birthdayPlan.setStartDate(calendar.getTime());

            calendar.add(Calendar.YEAR, 20);
            birthdayPlan.setEndDate(calendar.getTime());

            ffBirthdayPlan = beanToBirthdayPlan(birthdayPlan);
            ffBirthdayPlanHolder = new TrBirthdayPlanHolderImpl();

            result = getRmiService(ctx).createBirthdayPlan(ffBirthdayPlan, ffBirthdayPlanHolder);
            createdBirthdayPlan = ffBirthdayPlanHolder.getValue();
            if (result == 0 && createdBirthdayPlan != null)
            {
                birthdayPlan.setID(createdBirthdayPlan.bdayPlanId);
            }
            else
            {
                errorReason = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
            }
        }
        catch (RemoteException e)
        {
            errorReason = e.getLocalizedMessage();
        }
        finally
        {
            if (result != 0 || createdBirthdayPlan == null || !errorReason.equals(""))
            {
                final String msg = "Failed to create Birthday Plan: " + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                CallingGroupERLogMsg.generateBPCreationER(
                        birthdayPlan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE,
                        ctx);

                new OMLogMsg(Common.OM_MODULE, Common.OM_BP_CREATION_FAIL).log(ctx);

                throw new HomeException(msg);
            }
        }

        return birthdayPlan;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.FNF_BIRTHDAYPLAN_LICENSE))
        {
            // do nothing if License is not Enabled. This is a plug. The user freindly error done in Validator.
            return obj;
        }

        final BirthdayPlan birthdayPlan = (BirthdayPlan) obj;
        final BirthdayPlan oldBirthdayPlan = (BirthdayPlan) find(ctx, Long.valueOf(birthdayPlan.getID()));

        int result = 0;
        String errorReason = "";
        try
        {
            final TrBirthdayPlan ffBirthdayPlan = beanToBirthdayPlan(birthdayPlan);

            result = getRmiService(ctx).updateBirthdayPlan(ffBirthdayPlan);
            if (result == 0)
            {
                CallingGroupERLogMsg.generateBPModificationER(
                        oldBirthdayPlan,
                        birthdayPlan,
                        CallingGroupERLogMsg.SUCCESS_RESULT_CODE,
                        ctx);

                new OMLogMsg(Common.OM_MODULE, Common.OM_BP_MODIFICATION_SUCCESS).log(ctx);
            }
            else
            {
                errorReason = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
            }
        }
        catch (RemoteException e)
        {
            errorReason = e.getLocalizedMessage();
        }
        finally
        {
            if (result != 0 || !errorReason.equals(""))
            {
                final String msg = "Failed to update Birthday Plan: " + birthdayPlan.getID() + " :" + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                CallingGroupERLogMsg.generateBPModificationER(
                        oldBirthdayPlan,
                        birthdayPlan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE,
                        ctx);

                new OMLogMsg(Common.OM_MODULE, Common.OM_BP_MODIFICATION_FAIL).log(ctx);

                throw new HomeException(msg);
            }
        }

        return obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        if (obj == True.instance())
        {
            throw new UnsupportedOperationException("removeAll unsupported.");
        }

        final BirthdayPlan plan = (BirthdayPlan) obj;

        int result = 0;
        String errorReason = "";
        try
        {
            result = getRmiService(ctx).deleteBirthdayPlan(plan.getSpid(), plan.getID());
            if (result != 0)
            {
                errorReason = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
            }
        }
        catch (RemoteException e)
        {
            errorReason = e.getLocalizedMessage();
        }
        finally
        {
            if (result != 0 || !errorReason.equals(""))
            {
                final String msg = "Failed to delete Birthday Plan: " + plan.getID() + " :" + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                CallingGroupERLogMsg.generateBPDeletionER(
                        plan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE,
                        ctx);

                throw new HomeException(msg);
            }
        }
    }

    @Override
    public Object find(final Context ctx, final Object obj) throws HomeException
    {
        BirthdayPlan plan = null;
        final Long id = (Long) obj;

        int result = 0;
        String errorReason = "";
        TrBirthdayPlan foundBirthdayPlan = null;
        try
        {
            final TrBirthdayPlanHolder ffBirthdayPlanHolder = new TrBirthdayPlanHolderImpl();

            result = getRmiService(ctx).getBirthdayPlan(UserSupport.getSpid(ctx), id.intValue(), ffBirthdayPlanHolder);
            foundBirthdayPlan = ffBirthdayPlanHolder.getValue();
            if (result == 0 && foundBirthdayPlan != null)
            {
                plan = birthdayPlanToBean(foundBirthdayPlan);
            }
            else
            {
                errorReason = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
            }
        }
        catch (Throwable e)
        {
            errorReason = e.getLocalizedMessage();
        }
        finally
        {
            if (result != 0 || !errorReason.equals(""))
            {
                final String msg = "Failed to find Birthday Plan for ID : " + id + ", : " + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                throw new HomeException(msg);
            }
        }

        return plan;
    }

    // Unsupported methods
    // -------------------
    /*
     * No Longer using PersonalListPlanBySpid,PersonalListPlanByName, PersonalListPlanById
     * to filter PLPs. Instead we look through "where" clause and parse through the EQ
     */
    @Override
    public Collection select(final Context ctx, final Object where) throws HomeException
    {
        final Long fId = getBirthdayPlanSelectId(where);
        if (fId != null)
        {
            final Collection searchResult = new ArrayList();

            try
            {
                final FFECareRmiService rmiService = getRmiService(ctx);
                final TrBirthdayPlanHolder holder = new TrBirthdayPlanHolderImpl();
                int result = 0;
                result = rmiService.getBirthdayPlan(UserSupport.getSpid(ctx), fId.longValue(), holder);
                if (result == 0 && holder.getValue() != null)
                {
                    final TrBirthdayPlan receivedPlp = holder.getValue();
                    final BirthdayPlan plan = birthdayPlanToBean(receivedPlp);
                    searchResult.add(plan);
                }
            }
            catch (Throwable e)
            {
                throw new HomeException(e);
            }

            return searchResult;
        }

        final String fName = getBirthdayPlanSelectName(where);
        final Integer fSpid = getBirthdayPlanSelectSpid(where);
        if (fName != null)
        {
            final Collection searchResult = new ArrayList();

            try
            {
                final FFECareRmiService rmiService = getRmiService(ctx);
                final TrBirthdayPlanListHolder holder = new TrBirthdayPlanListHolderImpl();
                int result = 0;
                result = rmiService.getBirthdayPlanByName(UserSupport.getSpid(ctx), fName, holder);
                if (result == 0 && holder.getValue() != null && holder.getValue().trBirthdayPlanList != null)
                {
                    final TrBirthdayPlan[] receivedBirthdayPlanList = holder.getValue().trBirthdayPlanList;

                    // Check if the spid was passed in as well
                    boolean isSearchBySpid = false;
                    if (fSpid != null)
                    {
                        isSearchBySpid = true;
                    }

                    for (int i = 0; i < receivedBirthdayPlanList.length; i++)
                    {
                        final BirthdayPlan plan = birthdayPlanToBean(receivedBirthdayPlanList[i]);
                        if (!isSearchBySpid || (isSearchBySpid && plan.getSpid() == fSpid.intValue()))
                        {
                            // Add the plan only if spid search wasn't requested
                            // or if it was requested and plan has it
                            searchResult.add(plan);
                        }
                    }
                }
            }
            catch (Throwable e)
            {
                throw new HomeException(e);
            }

            return searchResult;
        }

        //By Spid
        if (fSpid != null)
        {
            final Collection searchResult = new ArrayList();

            try
            {
                final FFECareRmiService rmiService = getRmiService(ctx);
                final TrBirthdayPlanListHolder holder = new TrBirthdayPlanListHolderImpl();
                int result = 0;
                result = rmiService.getBirthdayPlanBySPID(fSpid.intValue(), holder);
                if (result == 0 && holder.getValue() != null && holder.getValue().trBirthdayPlanList != null)
                {
                    final TrBirthdayPlan[] receivedPlpList = holder.getValue().trBirthdayPlanList;
                    // Check if the spid was passed in as well
                    boolean isSearchById = false;
                    if (fId != null)
                    {
                        isSearchById = true;
                    }
                    for (int i = 0; i < receivedPlpList.length; i++)
                    {
                        final BirthdayPlan plan = birthdayPlanToBean(receivedPlpList[i]);
                        if (!isSearchById || (isSearchById && plan.getID() == fId.longValue()))
                        {
                            searchResult.add(plan);
                        }
                    }
                }
            }
            catch (NumberFormatException e)
            {
                throw new HomeException(e);
            }
            catch (RemoteException e)
            {
                throw new HomeException(e);
            }

            return searchResult;
        }

        throw new UnsupportedOperationException("selectAll unsupported.");
    }

    /**
     * While this is an "internal" method, they are made public because it might be useful.
     */
    public TrBirthdayPlan beanToBirthdayPlan(final BirthdayPlan bean)
    {
        final TrBirthdayPlan ffBirthdayPlan = new TrBirthdayPlan(
                bean.getID(),
                bean.getSpid(),
                bean.getName(),
                bean.getVoiceDiscountType() == DiscountTypeEnum.DISCOUNT ? FRIENDS_AND_FAMILY_DISCOUNT : FRIENDS_AND_FAMILY_RATEPLAN, 
                bean.getVoiceOutgoingValue(),
                bean.getVoiceIncomingValue(), 
                bean.getSmsDiscountType() == DiscountTypeEnum.DISCOUNT ? FRIENDS_AND_FAMILY_DISCOUNT : FRIENDS_AND_FAMILY_RATEPLAN,
                bean.getSmsOutgoingValue(), 
                bean.getSmsIncomingValue(),
                bean.getBpServiceType().getIndex()
        );

        return ffBirthdayPlan;
    }

    /**
     * While this is an "internal" method, they are made public because it might be useful
     */
    public static BirthdayPlan birthdayPlanToBean(final TrBirthdayPlan ffBirthdayPlan)
    {
        final BirthdayPlan bean = new BirthdayPlan();

        DiscountTypeEnum beanDiscountType = null;

        bean.setID(ffBirthdayPlan.bdayPlanId);
        bean.setName(ffBirthdayPlan.name);
        bean.setSpid(ffBirthdayPlan.spId);

        bean.setBpServiceType(CallingGroupServiceTypeEnum.get((short) ffBirthdayPlan.serviceType));
        bean.setVoiceDiscountType(ffBirthdayPlan.bdayPlanDiscountType == FRIENDS_AND_FAMILY_DISCOUNT ? DiscountTypeEnum.DISCOUNT
            : DiscountTypeEnum.RATE_PLAN);
        bean.setVoiceOutgoingValue(ffBirthdayPlan.MOValue);
        bean.setVoiceIncomingValue(ffBirthdayPlan.MTValue);
        bean.setSmsDiscountType(ffBirthdayPlan.bdayPlanSmsDiscountType == FRIENDS_AND_FAMILY_DISCOUNT ? DiscountTypeEnum.DISCOUNT
                : DiscountTypeEnum.RATE_PLAN);
        bean.setSmsOutgoingValue(ffBirthdayPlan.smsMOValue);
        bean.setSmsIncomingValue(ffBirthdayPlan.smsMTValue);

        
        bean.setVoiceDiscountType(beanDiscountType);
        bean.setVoiceOutgoingValue(ffBirthdayPlan.MOValue);
        bean.setVoiceIncomingValue(ffBirthdayPlan.MTValue);

        return bean;
    }

    public static FFECareRmiService getRmiService(final Context ctx) throws HomeException
    {
        try
        {
            return FFClosedUserGroupSupport.getFFRmiService(ctx, BirthdayPlanServiceHome.class);
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }
    }

    private String getBirthdayPlanSelectName(final Object x)
    {
        if (x instanceof EQ)
        {
            final EQ eq = (EQ) x;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equals("Name"))
            {
                return (String) eq.getArg2();
            }
            else if (eq.getArg1() instanceof String)
            {
                return (String) eq.getArg1();
            }
        }

        if (x instanceof Context)
        {
            new MinorLogMsg(this,"Unexpected context"+ x,null).log(getContext());
            return null;

        }

        return null;
    }

    private Integer getBirthdayPlanSelectSpid(final Object x)
    {
        if (x instanceof EQ)
        {
            final EQ eq = (EQ) x;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equals("Spid"))
            {
                return (Integer) eq.getArg2();
            }
            else if (eq.getArg1() instanceof String)
            {
                return (Integer) eq.getArg1();
            }
        }

        if (x instanceof Context)
        {
            new MinorLogMsg(this,"Unexpected context"+ x,null).log(getContext());
            return null;
        }

        return null;
    }

    private Long getBirthdayPlanSelectId(final Object x)
    {
        if (x instanceof EQ)
        {
            final EQ eq = (EQ) x;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equals("ID"))
            {
                return (Long) eq.getArg2();
            }
            else if (eq.getArg1() instanceof Long)
            {
                return (Long) eq.getArg1();
            }
        }

        if (x instanceof Context)
        {
            new MinorLogMsg(this,"Unexpected context"+ x,null).log(getContext());
            return null;
        }

        return null;
    }

    private static final int FRIENDS_AND_FAMILY_RATEPLAN = 5;
    private static final int FRIENDS_AND_FAMILY_DISCOUNT = 6;
}
