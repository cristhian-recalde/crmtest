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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateXInfo;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplate;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplateHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplateHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplateListHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugTemplateListHolderImpl;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class updates the ECP FnF module for the change to the CUG templates
 * 
 * @author ltse
 */
public class ClosedUserGroupTemplateServiceHome extends HomeProxy implements ClosedUserGroupTemplateHome
{

    private static final long serialVersionUID = 1L;
    
    
    public static final String ONLY_SPID_BASED_SELECT = "ONLY_SPID_BASED_SELECT";

    /**
     * Creates a new ClosedUserGroupTemplateServiceHome.
     * 
     * @param ctx
     *            The operating context.
     */
    public ClosedUserGroupTemplateServiceHome(Context ctx)
    {
        super(ctx, NullHome.instance());
    }


    /**
     * This method creates the CUG template at the FnF and logs failure ER
     * 
     * @param obj
     *            The Closed User Group Template to be created.
     * 
     * @return Object The created Closed User Group Template (with assigned ID).
     */
    public Object create(Context ctx, Object obj) throws HomeException
    {
        final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;
        try
        {
            final ClosedUserGroupTemplate createdCugTemplate = createFFCUGTemplate(ctx, cugTemplate);
            return createdCugTemplate;
        }
        catch (final HomeException e)
        {
            CallingGroupERLogMsg.generateCUGTemplateCreationER(cugTemplate,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            throw e;
        }
        catch (final Exception e)
        {
            CallingGroupERLogMsg.generateCUGTemplateCreationER(cugTemplate,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            throw new HomeException(e.getMessage(), e);
        }
    }


    /**
     * This method updates the CUG template at the FnF and log failure ER
     * 
     * @param obj
     *            The Closed User Group template to be updated.
     */
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;
        ClosedUserGroupTemplate oldCugTemplate = null;
        try
        {
            storeFFCUGTemplate(ctx, cugTemplate);
        }
        catch (final Exception e)
        {
            // log failure ER
            try
            {
                oldCugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, cugTemplate.getID(), cugTemplate.getSpid());
            }
            catch (HomeException fe)
            {
                new MinorLogMsg(this, "Fail to find the cug template", fe).log(ctx);
            }
            finally
            {
                CallingGroupERLogMsg.generateCUGTemplateModificationER(oldCugTemplate, cugTemplate,
                        CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            }
            
            if (e instanceof HomeException)
            {
                throw (HomeException)e;
            }
            else
            {
                throw new HomeException(e.getMessage(), e);
            }
        }
        
        return obj;
    }


    /**
     * This method removes the CUG template at the FnF and log failure ER proper
     * aggregation OMs and ERs.
     * 
     * @param obj
     *            The Closed User Group to be removed.
     */
    public void remove(Context ctx, Object obj) throws HomeException
    {
        if (obj == True.instance())
        {
            throw new UnsupportedOperationException("removeAll unsupported.");
        }
        final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;
        try
        {
            removeFFCUGTemplate(ctx, cugTemplate);
        }
        catch (final HomeException e)
        {
            CallingGroupERLogMsg.generateCUGTemplateDeletionER(cugTemplate, 
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            throw e;
        }
        catch (final Exception e)
        {
            CallingGroupERLogMsg.generateCUGTemplateDeletionER(cugTemplate, 
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            throw new HomeException(e.getMessage(), e);
        }
    }


    /**
     * Create a Closed User Group template with the corresponding Friends & Family Remote
     * service.
     * 
     * @param cugTemplate
     *            The Closed User Group template to be created.
     * 
     * @return Object The created Closed User Group template (with assigned ID).
     */
    public ClosedUserGroupTemplate createFFCUGTemplate(Context ctx, ClosedUserGroupTemplate cugTemplate)
            throws HomeException
    {
        debugMsg(ctx, "create: " + cugTemplate);
        try
        {
            final Calendar calendar = Calendar.getInstance();
            // Set start date to today.
            calendar.setTime(new Date());
            cugTemplate.setStartDate(calendar.getTime());
            
            // Set end date to 1000 years later.
            calendar.add(Calendar.YEAR, 1000);
            cugTemplate.setEndDate(calendar.getTime());
            
            final TrCugTemplate remote_cug = 
                FFClosedUserGroupSupport.convertCrmCugTemplateToFFCugTemplate(cugTemplate);

            final TrCugTemplateHolder created_cug = new TrCugTemplateHolderImpl();

            int result;
            try
            {
                result = FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).createCUGTemplate(remote_cug.getSpId(), remote_cug, created_cug);
            }
            catch (FFEcareException e)
            {
                result = ExternalAppSupport.NO_CONNECTION;
            }


            if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                if (created_cug != null && created_cug.getValue() != null)
                {
                    cugTemplate.setID(created_cug.getValue().getId());
                }
            }
            else
            {
                String msg = "Failed to create CUG template: " + cugTemplate.getName() + 
                    " with the result code [" + ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.FF, result) + "]";
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(msg);
            }
        }
        catch (RemoteException e)
        {
            String msg = "Failed to create CUG: " + cugTemplate.getName();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        return cugTemplate;
    }


    /**
     * Update a Closed User Group Template with the corresponding Friends & Family Remote
     * service.
     * 
     * @param obj
     *            The Closed User Group Template to be updated.
     */
    public void storeFFCUGTemplate(Context ctx, ClosedUserGroupTemplate cugTemplate) throws HomeException
    {
        debugMsg(ctx, "store: " + cugTemplate);
        try
        {
            final TrCugTemplate remote_cug =  
                FFClosedUserGroupSupport.convertCrmCugTemplateToFFCugTemplate(cugTemplate);

            int result;
            try
            {
                result = FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).updateCUGTemplate(remote_cug.getSpId(), remote_cug);
            }
            catch (FFEcareException e)
            {
                result = ExternalAppSupport.NO_CONNECTION;
            }
            
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = "Failed to update CUG: " + cugTemplate.getID() + " [" + 
                        ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.FF, result) + "]";
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(msg);
            }
        }
        catch (RemoteException e)
        {
            String msg = "Failed to update CUG: " + cugTemplate.getID();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
    }


    /**
     * Remove a Closed User Group Template with the corresponding Friends & Family Remote
     * service.
     * 
     * @param obj
     *            The Closed User Group to be removed.
     */
    public void removeFFCUGTemplate(Context ctx, Object obj) throws HomeException
    {
        debugMsg(ctx, "remove: " + obj);
        final ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) obj;
        try
        {
            int result;
            try
            {
                result = FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).deleteCUGTemplate(cugTemplate.getSpid(), cugTemplate.getID());
            }
            catch (FFEcareException e)
            {
                result = ExternalAppSupport.NO_CONNECTION;
            }

            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = "Failed to remove CUG: " + cugTemplate.getID() + " [" 
                    + ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.FF, result) + "]";
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(msg);
            }
        }
        catch (RemoteException e)
        {
            String msg = "Failed to remove CUG: " + cugTemplate.getID();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg);
        }
    }


    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#select(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public Collection select(Context ctx, Object what) throws HomeException, UnsupportedOperationException
    {
        debugMsg(ctx, "selectAll");
        Map<Long, Object> returnCollection = new HashMap<Long, Object>();
        Integer cugSpid = (Integer) ClosedUserGroupSupport.getPropertyArgumentFromFilter(what,
                ClosedUserGroupTemplateXInfo.SPID, Integer.class);
        Long cugTemplateId = (Long) ClosedUserGroupSupport.getPropertyArgumentFromFilter(what,
                ClosedUserGroupTemplateXInfo.ID, Long.class);       
        
        String cugTemplateName = (String) ClosedUserGroupSupport.getPropertyArgumentFromFilter(what,
                ClosedUserGroupTemplateXInfo.NAME, String.class);
        if(cugTemplateId != null && cugTemplateId == ClosedUserGroupTemplate.DEFAULT_ID)
        {
            cugTemplateId = null;
        }
        Collection spidLevelTemplates = selectAnd(ctx, cugTemplateId, cugTemplateName, cugSpid, what);
        if (spidLevelTemplates != null)
        {
            for (Object templateObj : spidLevelTemplates)
            {
                ClosedUserGroupTemplate template = (ClosedUserGroupTemplate) templateObj;
                if (returnCollection.get(template.getID()) == null)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Adding CUG template with id : " + template.getID());
                    }
                    returnCollection.put(template.getID(), template);
                }
            }
        }
        List returnList = new ArrayList();
        for (Object obj : returnCollection.values())
        {
            returnList.add(obj);
        }
        return returnList;
    }
    
    
    private Collection<ClosedUserGroupTemplate> selectAnd(Context ctx, Long cugTemplateId, String name, Integer cugTemplateSpid, Object what)
            throws HomeException
    {
        Collection<ClosedUserGroupTemplate> cugTemplateList = new ArrayList<ClosedUserGroupTemplate>();
        List<TrCugTemplate> trTemplateList = null;
        
     //   boolean onlySpidBasedSelect = ctx.has(ONLY_SPID_BASED_SELECT);
        
        boolean onlySpidBasedSelect = ctx.has(ONLY_SPID_BASED_SELECT);
        
        if (cugTemplateId != null && !onlySpidBasedSelect)
        {
            trTemplateList = getCUGTemplateByID(ctx, cugTemplateId, cugTemplateSpid);
        }
        else if (name != null  && !onlySpidBasedSelect)
        {
            trTemplateList = getCUGTemplateByName(ctx, name, cugTemplateSpid);
        }
        else if (cugTemplateSpid != null)
        {
            trTemplateList = getCUGTemplateBySPID(ctx, cugTemplateSpid);
        }
        Predicate p = (Predicate) XBeans.getInstanceOf(ctx, what, Predicate.class);
        if (p == null)
        {
            return new ArrayList();
        }        
        if (trTemplateList != null)
        {
            for (TrCugTemplate trTemplalate : trTemplateList)
            {
                ClosedUserGroupTemplate cugTemplate = FFClosedUserGroupSupport.convertFFCugTemplateToCRMCugTemplate(
                        ctx, trTemplalate);
                if (p.f(ctx, cugTemplate))
                {
                    cugTemplateList.add(cugTemplate);
                }
            }
        }
 
        return cugTemplateList;
    }
    

    List<TrCugTemplate> getCUGTemplateBySPID(Context ctx, final int spid) throws HomeException
    {
        List<TrCugTemplate> trTemplateList = new ArrayList<TrCugTemplate>();
        try
        {
            final TrCugTemplateListHolder holder = new TrCugTemplateListHolderImpl();
            debugMsg(ctx, "Fetching CUG Template for Spid :" + spid);
            FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).getCUGTemplateBySPID(spid, holder);
            if (holder != null && holder.getValue() != null)
            {
                for(TrCugTemplate temp : holder.getValue().trCugTemplateList)
                {
                    trTemplateList.add(temp);
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Failed to retrieve all CUGs.  ";
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg);
        }
        return trTemplateList;
    }
    
    
    List<TrCugTemplate> getCUGTemplateByID(Context ctx, long cugTemplateId,  final Integer cugTemplateSpid) throws HomeException
    {
        List<TrCugTemplate> trTemplateList = new ArrayList<TrCugTemplate>();
        try
        {            
            Collection spids = ClosedUserGroupSupport.getApplicableSpids(ctx, cugTemplateSpid);
            if (spids != null)
            {
                for (Object spidObj : spids)
                {
                    Spid spid = (Spid) spidObj;
                    debugMsg(ctx, "Fetching CUG Template for Spid :" + spid.getSpid() + " with Id : " + cugTemplateId);
                    try
                    {
                        final TrCugTemplateHolder holder = new TrCugTemplateHolderImpl();
                        FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).getCUGTemplate(spid.getId(), cugTemplateId, holder);
                        if (holder != null && holder.getValue() != null)
                        {
                            trTemplateList.add(holder.getValue());
                            break;                                
                        }
                    }
                    catch (Exception e)
                    {
                        String msg = "Failed to retrieve CUG templates for Spid :" + spid.getSpid()
                                + " with Id : " + cugTemplateId;
                        new MinorLogMsg(this, msg, e).log(ctx);
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Failed to retrieve CUG template with Id : " + cugTemplateId;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        return trTemplateList;
    }
    
    
    List<TrCugTemplate> getCUGTemplateByName(Context ctx, String name,  final Integer cugTemplateSpid) throws HomeException
    {
        List<TrCugTemplate> trTemplateList = new ArrayList<TrCugTemplate>();
        try
        {            
            Collection spids = ClosedUserGroupSupport.getApplicableSpids(ctx, cugTemplateSpid);
            if (spids != null)
            {
                for (Object spidObj : spids)
                {
                    Spid spid = (Spid) spidObj;
                    debugMsg(ctx, "Fetching CUG Template for Spid :" + spid.getSpid() + " with Name : " + name);
                    try
                    {
                        final TrCugTemplateListHolder holder = new TrCugTemplateListHolderImpl();
                        FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).getCUGTemplateByName(
                                spid.getId(), name, holder);
                        if (holder != null && holder.getValue() != null)
                        {
                            for(TrCugTemplate temp : holder.getValue().trCugTemplateList)
                            {
                                trTemplateList.add(temp);                                
                            }                            
                        }
                    }
                    catch (Exception e)
                    {
                        String msg = "Failed to retrieve CUG templates for Spid :" + spid.getSpid()
                                + " with Name : " + name;
                        new MinorLogMsg(this, msg, e).log(ctx);
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Failed to retrieve CUG templates with Name : " + name;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        return trTemplateList;
    }
        

    /**
     * Retrieve a Closed User Group having the given CUG ID (with the corresponding
     * Friends & Family Remote service).
     * 
     * @param key
     *            The CUG ID.
     * 
     * @return Object The retrieved Closed User Group.
     */
    public Object find(Context ctx, Object key) throws HomeException
    {
        debugMsg(ctx, "find: " + key);
        if (key instanceof Long)
        {
            final Long id = (Long) key;
            ClosedUserGroupTemplate bean = null;
            try
            {
                final TrCugTemplateHolder retrieved_cug = new TrCugTemplateHolderImpl();
                // FIXME THE SPID needs to be inputted

                	Collection spids = ClosedUserGroupSupport.getApplicableSpids(ctx, null);
                	if(spids != null)
                	{
                		int result = 9999;
                		for(Object spidObj : spids)
                		{
                			Spid spid = (Spid)spidObj;
                			try
                			{
                				if(LogSupport.isDebugEnabled(ctx))
                				{
                					LogSupport.debug(ctx, this, "Fetching CUG template for Spid :" + spid.getSpid() + " and Template ID :" + id.longValue());
                				}
                				result = FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass()).getCUGTemplate(spid.getSpid(), id.longValue(), retrieved_cug);
                			}
                			catch (FFEcareException e)
                			{
                				result = ExternalAppSupport.NO_CONNECTION;
                			}
                			if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
                			{
                				if (retrieved_cug != null)
                				{
                					bean = FFClosedUserGroupSupport.convertFFCugTemplateToCRMCugTemplate(
                							ctx, 
                							retrieved_cug.getValue());
                					break;
                				}
                			}
                		}
                		if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
            			{
                			//DO Nothing
            			}
                		else if (result == FFECareRmiConstants.FF_ECARE_CUG_TEMPLATE_NOT_FOUND)
                		{
                			return null;
                		}
                		else
                		{
                			String msg = "Failed to retrieve CUG: " + id.longValue() 
                					+ " [" + ExternalAppSupportHelper.get(ctx).getErrorCodeMessage(ctx, ExternalAppEnum.FF, result) + "]";
                			new MinorLogMsg(this, msg, null).log(ctx);
                			throw new HomeException(msg);
                		}
                	}
                }
            catch (Exception e)
            {
                String msg = "Failed to retrieve CUG: " + id.longValue();
                new MinorLogMsg(this, msg, e).log(ctx);
                throw new HomeException(msg);
            }
            return bean;
        }
        else
        {
            Collection col = select(ctx, key);
            if (col != null && col.size() > 0)
            {
                return col.iterator().next();
            }
            return super.find(ctx, key);
        }
    }


    /**
     * UNSUPPORTED OPERATION
     */
    public void drop(Context ctx) throws HomeException
    {
        throw new UnsupportedOperationException("drop unsupported.");
    }


        private void debugMsg(Context ctx, String msg)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, msg, null).log(ctx);
        }
    }
    
  

}
