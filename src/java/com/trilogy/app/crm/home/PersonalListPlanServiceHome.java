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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanTransientHome;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.UserSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrPlp;
import com.trilogy.app.ff.ecare.rmi.TrPlpHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolderImpl;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.elang.Value;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;
/**
 * @author margarita.alp@redknee.com
 */
public class PersonalListPlanServiceHome extends PersonalListPlanTransientHome
{

    private static final long serialVersionUID = 1L;

    public PersonalListPlanServiceHome(final Context ctx)
    {
        super(ctx);
    }

    // Overwrite Home's methods
    // ------------------------
    @Override
    public Object create(final Context ctx, final Object arg) throws HomeException
    {
        //debugLogMsg(ctx,"create: " + arg);
        new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_CREATION_ATTEMPT).log(ctx);

        final PersonalListPlan plan = (PersonalListPlan) arg;

        int result = 0;
        TrPlp plp = null;
        TrPlpHolder plpHolder = null;

        TrPlp createdPlp = null;
        String errorReason = "";
        try
        {
            // Hard code the start and end date
            final Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            plan.setStartDate(calendar.getTime());

            calendar.add(Calendar.YEAR, 20);
            plan.setEndDate(calendar.getTime());

            plp = beanToPlp(plan);
            plpHolder = new TrPlpHolderImpl();

            result = getRmiService(ctx).createPLP(plp, plpHolder);
            createdPlp = plpHolder.getValue();
            if (result == 0 && createdPlp != null)
            {
                plan.setID(plpHolder.getValue().plpId);
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
            if (result != 0 || createdPlp == null || !errorReason.equals(""))
            {
                final String msg = "Failed to create PLP: " + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                CallingGroupERLogMsg.generatePLPCreationER(
                        plan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE,
                        ctx);

                new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_CREATION_FAIL).log(ctx);

                throw new HomeException(msg);
            }
        }

        return plan;
    }

    @Override
    public Object store(final Context ctx, final Object arg) throws HomeException
    {
        //debugLogMsg(ctx,"store: " + arg);

        final PersonalListPlan plan = (PersonalListPlan) arg;
        final PersonalListPlan oldPlan = (PersonalListPlan) find(ctx, Long.valueOf(plan.getID()));

        int result = 0;
        String errorReason = "";
        try
        {
            final TrPlp plpToSend = beanToPlp(plan);

            result = getRmiService(ctx).updatePLP(plpToSend);
            if (result == 0)
            {
                CallingGroupERLogMsg.generatePLPModificationER(
                        oldPlan,
                        plan,
                        CallingGroupERLogMsg.SUCCESS_RESULT_CODE,
                        ctx);

                new OMLogMsg(
                        Common.OM_MODULE,
                        Common.OM_PLP_MODIFICATION_SUCCESS).log(ctx);
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
                final String msg = "Failed to update PLP: " + plan.getID() + " :" + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                CallingGroupERLogMsg.generatePLPModificationER(
                        oldPlan,
                        plan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE,
                        ctx);

                new OMLogMsg(Common.OM_MODULE, Common.OM_PLP_MODIFICATION_FAIL).log(ctx);

                throw new HomeException(msg);
            }
        }

        // QUESTION: who does the update?
        return arg;
    }

    @Override
    public void remove(final Context ctx, final Object arg) throws HomeException
    {
        if (arg == True.instance())
        {
            //debugLogMsg(ctx,"removeAll");
            throw new UnsupportedOperationException("removeAll unsupported.");
        }

        //debugLogMsg(ctx,"remove: " + arg);

        final PersonalListPlan plan = (PersonalListPlan) arg;

        int result = 0;
        String errorReason = "";
        try
        {
            result = getRmiService(ctx).deletePLP(plan.getSpid(), plan.getID());
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
                final String msg = "Failed to delete PLP: " + plan.getID() + " :" + errorReason;

                new SeverityLogMsg(
                        SeverityEnum.MAJOR,
                        this.getClass().getName(),
                        msg,
                        null).log(ctx);

                CallingGroupERLogMsg.generatePLPDeletionER(
                        plan,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE,
                        ctx);

                throw new HomeException(msg);
            }
        }
    }

    @Override
    public Object find(final Context ctx, final Object arg) throws HomeException, HomeInternalException
    {
        //debugLogMsg(ctx,"find: " + arg);

        PersonalListPlan plan = null;
        final Long id = (Long) arg;

        int result = 0;
        String errorReason = "";
        TrPlp foundPlp = null;
        try
        {
            final TrPlpHolder plpHolder = new TrPlpHolderImpl();

            result = getRmiService(ctx).getPLP(UserSupport.getSpid(ctx), id.intValue(), plpHolder);
            foundPlp = plpHolder.getValue();
            if (result == 0 && foundPlp != null)
            {
                plan = plpToBean(foundPlp);
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
                final String msg = "Failed to find PLP for ID : " + id + ", : " + errorReason;

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
    public Collection select(final Context ctx, final Object where) throws HomeException, HomeInternalException
    {
        final Long fId = getPersonalListPlanSelectId(where);
        if (fId != null)
        {
            final Collection searchResult = new ArrayList();

            try
            {
                final FFECareRmiService rmiService = PersonalListPlanServiceHome.getRmiService(ctx);
                final TrPlpHolder holder = new TrPlpHolderImpl();
                int result = 0;
                result = rmiService.getPLP(UserSupport.getSpid(ctx), fId.longValue(), holder);
                if (result == 0 && holder.getValue() != null)
                {
                    final TrPlp receivedPlp = holder.getValue();
                    final PersonalListPlan plan = PersonalListPlanServiceHome.plpToBean(receivedPlp);
                    searchResult.add(plan);
                }
            }
            catch (Throwable e)
            {
                throw new HomeException(e);
            }

            return searchResult;
        }

        final String fName = getPersonalListPlanSelectName(where);
        final Integer fSpid = getPersonalListPlanSelectSpid(where);
        if (fName != null)
        {
            final Collection searchResult = new ArrayList();

            try
            {
                final FFECareRmiService rmiService = PersonalListPlanServiceHome.getRmiService(ctx);
                final TrPlpListHolder holder = new TrPlpListHolderImpl();
                int result = 0;
                result = rmiService.getPLPByName(UserSupport.getSpid(ctx), fName, holder);
                if (result == 0 && holder.getValue() != null && holder.getValue().trPlpList != null)
                {
                    final TrPlp[] receivedPlpList = holder.getValue().trPlpList;

                    // Check if the spid was passed in as well
                    boolean isSearchBySpid = false;
                    if (fSpid != null)
                    {
                        isSearchBySpid = true;
                    }

                    for (int i = 0; i < receivedPlpList.length; i++)
                    {
                        final PersonalListPlan plan = PersonalListPlanServiceHome.plpToBean(receivedPlpList[i]);
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
                final FFECareRmiService rmiService = PersonalListPlanServiceHome.getRmiService(ctx);
                final TrPlpListHolder holder = new TrPlpListHolderImpl();
                int result = 0;
                result = rmiService.getPLPBySPID(fSpid.intValue(), holder);
                if (result == 0 && holder.getValue() != null && holder.getValue().trPlpList != null)
                {
                    final TrPlp[] receivedPlpList = holder.getValue().trPlpList;
                    // Check if the spid was passed in as well
                    boolean isSearchById = false;
                    if (fId != null)
                    {
                        isSearchById = true;
                    }
                    for (int i = 0; i < receivedPlpList.length; i++)
                    {
                        final PersonalListPlan plan = PersonalListPlanServiceHome.plpToBean(receivedPlpList[i]);
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

        //debugLogMsg(ctx,"selectAll");
        throw new UnsupportedOperationException("selectAll unsupported.");
    }

    /**
     * While this is an "internal" method, they are made public because it might be useful.
     */
    public TrPlp beanToPlp(final PersonalListPlan bean)
    {
        final TrPlp remotePlp = new TrPlp(
                bean.getID(),
                bean.getName(),
                bean.getSpid(),
                bean.getVoiceDiscountType() == DiscountTypeEnum.DISCOUNT ? FRIENDS_AND_FAMILY_DISCOUNT : FRIENDS_AND_FAMILY_RATEPLAN,
                bean.getVoiceOutgoingValue(), // MOValue
                bean.getVoiceIncomingValue(), // MTValue
                bean.getSmsDiscountType() == DiscountTypeEnum.DISCOUNT ? FRIENDS_AND_FAMILY_DISCOUNT : FRIENDS_AND_FAMILY_RATEPLAN,
                bean.getSmsOutgoingValue(),
                bean.getSmsIncomingValue(),
                bean.getMaxSubscriberCount(),
                // TODO 2010-10-01 DateFormat access needs synchronization
                formatter.format(bean.getStartDate()),
                formatter.format(bean.getEndDate()),
                bean.getPlpServiceType().getIndex());
        

        return remotePlp;
    }

    /**
     * While this is an "internal" method, they are made public because it might be useful
     */
    public static PersonalListPlan plpToBean(final TrPlp plp) throws HomeException
    {
        final PersonalListPlan bean = new PersonalListPlan();

        bean.setID(plp.plpId);
        bean.setName(plp.name);
        bean.setSpid(plp.spId);
        bean.setMaxSubscriberCount(plp.maxSubs);
        bean.setPlpServiceType(CallingGroupServiceTypeEnum.get((short) plp.serviceType));
        bean.setVoiceDiscountType(plp.plpDiscountType == FRIENDS_AND_FAMILY_DISCOUNT ? DiscountTypeEnum.DISCOUNT
                : DiscountTypeEnum.RATE_PLAN);
        bean.setVoiceOutgoingValue(plp.MOValue);
        bean.setVoiceIncomingValue(plp.MTValue);
        bean.setSmsDiscountType(plp.smsDiscountType == FRIENDS_AND_FAMILY_DISCOUNT ? DiscountTypeEnum.DISCOUNT
                : DiscountTypeEnum.RATE_PLAN);
        bean.setSmsOutgoingValue(plp.smsMOValue);
        bean.setSmsIncomingValue(plp.smsMTValue);
        
        try
        {
            // TODO 2010-10-01 DateFormat access needs synchronization
            bean.setStartDate(formatter.parse(plp.startDate));
            bean.setEndDate(formatter.parse(plp.endDate));
        }
        catch (ParseException pe)
        {
            String errorMsg = "Unable to parse startDate/endDate for plp " + plp.plpId;
            
            HomeException homeException = new HomeException(errorMsg);
            throw homeException;
        }

        return bean;
    }

    public static FFECareRmiService getRmiService(final Context ctx) throws HomeException
    {
        try
        {
            return FFClosedUserGroupSupport.getFFRmiService(ctx, PersonalListPlanServiceHome.class);
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }
    }

    private void debugLogMsg(final Context ctx, final String msg)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, msg, null).log(ctx);
        }
    }

    private String getPersonalListPlanSelectName(final Object x)
    {
    	 Object value = getData(x, "Name" ,String.class, false);
         if ( value != null)            
             return (String) value;  
         
         return null;
    }

    private Integer getPersonalListPlanSelectSpid(final Object x)
    {
    	  Object value = getData(x, "Spid" , Integer.class ,false);
          if ( value != null)            
              return (Integer) value;  
          return null;
    }

    private Object getData(final Object x, final String key, final Class classtype , boolean isPrimaryKey)
    {
        if (x instanceof EQ)
        {
            final EQ eq = (EQ) x;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equals(key))
            {
                return (Long) eq.getArg2();
            }
            else if ((classtype.getName().equals(Long.class.getName()) || ( classtype.getName().equals(Integer.class.getName()))))
            {
                return eq.getArg1();
            }
        }
        else if ( x instanceof And )
        {
            final And and = (And)x;
            List conditions = and.getList();
            for (Object condition : conditions)
            {
                //final EQ eq = (EQ) condition;
            	final Value val = (Value) condition;
            	Object args = val.getArg1();
            	if(args instanceof EQ){
            		final EQ eq = (EQ)args;

            		//final String name =((com.redknee.framework.xhome.elang.In)(val.getArg1())).getArg().getName();
            		final String name = ((PropertyInfo) eq.getArg1()).getName();
            		if (name.equals(key))
            		{
            			return eq.getArg2();
            		}
            		else if(isPrimaryKey && classtype.getName().equals(eq.getArg2().getClass().getName()))// && (classtype.getName().equals(Long.class.getName()) || (classtype.getName().equals(Integer.class.getName()))))
            		{
            			return eq.getArg1();
            		}
            	}
            }
        
        }
        
        if (x instanceof Context)
        {
            new MinorLogMsg(this," Unexpected context " + x,null).log(getContext());
            return null;
        }

        return null;
    }
    private Long getPersonalListPlanSelectId(final Object x)
    {
        Object value = getData(x, "ID", Long.class , true);
        if ( value != null)            
            return (Long) value;   
        return null;
    }


    private static final int FRIENDS_AND_FAMILY_RATEPLAN = 1;
    private static final int FRIENDS_AND_FAMILY_DISCOUNT = 2;
    
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

}
