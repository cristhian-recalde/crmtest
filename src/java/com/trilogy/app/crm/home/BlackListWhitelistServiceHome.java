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

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Value;
//import com.trilogy.framework.xhome.filter.Logic;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

import com.trilogy.app.crm.bean.BlacklistWhitelistTemplate;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplateID;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplateTransientHome;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplateXInfo;
import com.trilogy.app.crm.bean.BlacklistWhitelistTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrPlp;
import com.trilogy.app.ff.ecare.rmi.TrPlpHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolder;
import com.trilogy.app.ff.ecare.rmi.TrPlpListHolderImpl;

/***
 * @author chandrachud.ingale
 * @since 9.6
 */
public class BlackListWhitelistServiceHome extends BlacklistWhitelistTemplateTransientHome
{

    private static final long             serialVersionUID = -2095300368597784305L;
    private static final SimpleDateFormat formatter        = new SimpleDateFormat("yyyyMMdd");


    public BlackListWhitelistServiceHome(final Context ctx)
    {
        super(ctx);
    }

    // The ID generated from UCRS I2004.5 createPLP would be ID for BlacklistWhitelistTemplate

    @Override
    public Object create(final Context ctx, final Object arg) throws HomeException
    {
        final BlacklistWhitelistTemplate blwlTemplate = (BlacklistWhitelistTemplate) arg;

        TrPlp createdPlp = null;
        String errorReason = null;
        Exception exception = null;
        int result = 0;

        try
        {
            TrPlp plpToCreate = populateTrPlp(blwlTemplate);
            TrPlpHolder plpHolder = new TrPlpHolderImpl();

            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, BlackListWhitelistServiceHome.class,
                    "Create blacklist whitelist template - createPLP().");
            }
            
            result = getRmiService(ctx).createPLP(plpToCreate, plpHolder);
            createdPlp = plpHolder.getValue();
            if (result == 0 && createdPlp != null)
            {
                blwlTemplate.setIdentifier(createdPlp.plpId);
                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, BlackListWhitelistServiceHome.class,
                        "Created blacklist whitelist template - createPLP() returned ID : " + createdPlp.plpId);
                }
            }
            else
            {
                errorReason = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
            }
        }
        catch (RemoteException e)
        {
            errorReason = e.getLocalizedMessage();
            exception = e;
        }
        finally
        {
            if (result != 0 || createdPlp == null || errorReason != null)
            {
                final String msg = "Failed to create Blacklist Whitelist template : " + errorReason;

                new SeverityLogMsg(SeverityEnum.MAJOR, this.getClass().getName(), msg, exception).log(ctx);

                throw new HomeException(msg);
            }
        }
        return blwlTemplate;
    }


    @Override
    public Object store(final Context ctx, final Object arg) throws HomeException
    {
        throw new UnsupportedOperationException("Blacklist/Whitelist template not editable.");
    }


    @Override
    public void remove(final Context ctx, final Object arg) throws HomeException
    {
        throw new UnsupportedOperationException("Blacklist/Whitelist remove operation not unsupported.");
    }


    @Override
    public Object find(final Context ctx, final Object arg) throws HomeException, HomeInternalException
    {
        BlacklistWhitelistTemplate blwlTemplate = null;
        
        final BlacklistWhitelistTemplateID blwlTemplateId = (BlacklistWhitelistTemplateID) arg;

        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, BlackListWhitelistServiceHome.class,
                "Call to find() blacklist whitelist template for SPID : " + blwlTemplateId.getSpid() + " with ID : " + blwlTemplateId.getIdentifier());
        }
        int result = 0;
        String errorReason = null;
        Throwable throwable = null;
        TrPlp foundTrPlp = null;
        try
        {
            final TrPlpHolder plpHolder = new TrPlpHolderImpl();

            result = getRmiService(ctx).getPLP(blwlTemplateId.getSpid(), blwlTemplateId.getIdentifier(), plpHolder);
            foundTrPlp = plpHolder.getValue();
            if (result == 0 && foundTrPlp != null)
            {
                blwlTemplate = populateBlWl(foundTrPlp, ctx);
            }
            else
            {
                errorReason = CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result);
            }
        }
        catch (Throwable e)
        {
            errorReason = e.getLocalizedMessage();
            throwable = e;
        }
        finally
        {
            if (result != 0 || errorReason != null)
            {
                final String msg = "Failed to find Blacklist/Whitelist template for ID : " + blwlTemplateId.getIdentifier() + ", : " + errorReason;

                new SeverityLogMsg(SeverityEnum.MAJOR, this.getClass().getName(), msg, throwable).log(ctx);

                throw new HomeException(msg);
            }
        }

        return blwlTemplate;
    }


    /**
     * {@inheritDoc}
     * 
     * Supports only select templates for a spid
     */
    public Collection<BlacklistWhitelistTemplate> select(Context ctx, Object where) throws HomeException, HomeInternalException
    {
        final Integer spid = getPersonalListPlanSelectSpid(where);
      //By Spid
        if (spid != null)
        {
            final Collection<BlacklistWhitelistTemplate> searchResult = new ArrayList<BlacklistWhitelistTemplate>();
            try
            {
                final FFECareRmiService rmiService = getRmiService(ctx);
                final TrPlpListHolder holder = new TrPlpListHolderImpl();
                int result = rmiService.getPLPBySPID(spid.intValue(), holder);
                if (result == 0 && holder.getValue() != null && holder.getValue().trPlpList != null)
                {
                    final TrPlp[] receivedPlpList = holder.getValue().trPlpList;
                    for (int i = 0; i < receivedPlpList.length; i++)
                    {
                        if (receivedPlpList[i].plpType == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX
                                || receivedPlpList[i].plpType == BlacklistWhitelistTypeEnum.WHITELIST_INDEX)
                        {
                            final BlacklistWhitelistTemplate template = populateBlWl(receivedPlpList[i], ctx);
                            searchResult.add(template);
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
        return super.select(ctx, where);
    }
    
    
    private Integer getPersonalListPlanSelectSpid(final Object x)
    {
    	  Object value = getData(x, "spid" , Integer.class ,false);
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
                
            	final Value val = (Value) condition;
            	Object args = val.getArg1();
            	if(args instanceof EQ){
            		final EQ eq = (EQ)args;

            		
            		final String name = ((PropertyInfo) eq.getArg1()).getName();
            		if (name.equals(key))
            		{
            			return eq.getArg2();
            		}
            		else if(isPrimaryKey && classtype.getName().equals(eq.getArg2().getClass().getName()))
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
    
    public TrPlp populateTrPlp(final BlacklistWhitelistTemplate bean)
    {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        bean.setStartDate(calendar.getTime());
        calendar.add(Calendar.YEAR, 999);
        bean.setEndDate(calendar.getTime());

        final TrPlp remotePlp = new TrPlp();
        remotePlp.setSpId(bean.getSpid());
        remotePlp.setName(bean.getName());
        remotePlp.setStartDateString(formatter.format(bean.getStartDate()));
        remotePlp.setStopDateString(formatter.format(bean.getEndDate()));
        remotePlp.setType(bean.getType().getIndex());
        remotePlp.maxSubs = bean.getMaxSubscribersAllowed();
        return remotePlp;
    }


    public static BlacklistWhitelistTemplate populateBlWl(final TrPlp trPlp, Context ctx)
    {
        final BlacklistWhitelistTemplate bean = new BlacklistWhitelistTemplate();

        bean.setIdentifier(trPlp.plpId);
        bean.setName(trPlp.name);
        bean.setSpid(trPlp.spId);
        bean.setMaxSubscribersAllowed((short) trPlp.maxSubs);
        bean.setType(BlacklistWhitelistTypeEnum.get((short) trPlp.plpType));

        CallingGroupTypeEnum callingGroupTypeEnum = null;

        if (trPlp.plpType == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX)
        {
            callingGroupTypeEnum = CallingGroupTypeEnum.BL;
        }
        else if (trPlp.plpType == BlacklistWhitelistTypeEnum.WHITELIST_INDEX)
        {
            callingGroupTypeEnum = CallingGroupTypeEnum.WL;
        }
            
        if (callingGroupTypeEnum != null)
        {
            try
            {
                BlacklistWhitelistTemplateServiceExtension extension = ServiceSupport
                        .getBlacklistWhitelistTemplateExtension(ctx, trPlp.plpId, callingGroupTypeEnum);
                if (extension != null)// would never be null, unless entry is deleted manually or by mistake
                {
                    bean.setGLCode(extension.getGlCode());
                }
            }
            catch (HomeException e)
            {
                new MajorLogMsg(BlackListWhitelistServiceHome.class,
                        "Exception during blacklist whitelist extension fetch for plp " + trPlp.plpId, e).log(ctx);
            }
        }
        
        
        try
        {
            bean.setStartDate(formatter.parse(trPlp.startDate));
            bean.setEndDate(formatter.parse(trPlp.endDate));
        }
        catch (ParseException pe)
        {
            new MinorLogMsg(BlackListWhitelistServiceHome.class,
                    "Unable to parse startDate/endDate for plp " + trPlp.plpId, pe).log(ctx);
        }
        return bean;
    }


    public static FFECareRmiService getRmiService(final Context ctx) throws HomeException
    {
        try
        {
            return FFClosedUserGroupSupport.getFFRmiService(ctx, BlackListWhitelistServiceHome.class);
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }
    }
}
