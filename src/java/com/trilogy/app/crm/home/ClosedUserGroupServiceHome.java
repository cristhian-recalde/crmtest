/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.ClosedUserGroupTransientHome;
import com.trilogy.app.crm.bean.ClosedUserGroupXInfo;
import com.trilogy.app.crm.bean.PrivateCug;
import com.trilogy.app.crm.bean.PrivateCugHome;
import com.trilogy.app.crm.bean.PrivateCugXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.FFParamSetBuilder;
import com.trilogy.app.crm.home.cug.DeltaNewOldCugSubs;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrCug;
import com.trilogy.app.ff.ecare.rmi.TrCugHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrCugIdHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugIdHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.model.app.ff.param.ParameterSetHolderImpl;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.home.ClosedUserGroupServiceHome;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;

/**
 * This class implements a subset of Home functionality for Closed User Group
 * and delegates to the Friends & Family Remote service.
 *
 * @author jimmy.ng@redknee.com
 */
public class ClosedUserGroupServiceHome extends ClosedUserGroupTransientHome
{

    private static final long serialVersionUID = 1L;

    /**
	 * Creates a new ClosedUserGroupRMIHome.
	 *
	 * @param ctx The operating context.
	 */
	public ClosedUserGroupServiceHome(Context ctx)
	{
		super(ctx);
	}

	/**
	 * This method acts as a wrapper to the createCUG() method.
	 * Its task is to generate proper aggregation OMs and ERs.
	 *
	 * @param obj The Closed User Group to be created.
	 * 
	 * @return Object The created Closed User Group (with assigned ID).
	 */
	@Override
	public Object create(Context ctx, Object obj) throws HomeException
	{
		final ClosedUserGroup cug = (ClosedUserGroup) obj;

		try
		{
			final ClosedUserGroup created_cug = (ClosedUserGroup) createCUG(ctx, cug);

			/* Generating the ER for successful CUG creation is done after the 
			 * Auxiliary Service is successfully created. (see ClosedUserGroupERLogHome.java)
			 */
			
			return created_cug;
		}
		catch (final HomeException e)
		{
			CallingGroupERLogMsg.generateCUGCreationER(cug, 
				CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);

			throw e;
		}
		catch (final Exception e)
        {
            CallingGroupERLogMsg.generateCUGCreationER(cug, 
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, 
                    ctx);

            throw new HomeException(e.getMessage(), e);
        }	}

	/**
	 * This method acts as a wrapper to the storeCUG() method.
	 * Its task is to generate proper aggregation OMs and ERs.
	 *
	 * @param obj The Closed User Group to be updated.
	 */
    @Override
	public Object store(Context ctx, Object obj) throws HomeException
	{
		final ClosedUserGroup cug = (ClosedUserGroup) obj;

		try
		{
			storeCUG(ctx, cug);
		}
        catch (final HomeException e)
        {
            ClosedUserGroup oldCug = null;
            try
            {                
                oldCug = ClosedUserGroupSupport.getCUG(ctx, cug.getID(), cug.getSpid());
            }
            catch (Exception se)
            {               
            }
            
            CallingGroupERLogMsg.generateCUGModificationER(oldCug,cug,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            
            throw e;
        }
        catch (final Exception e)
        {
            ClosedUserGroup oldCug = null;
            try
            {
                oldCug = ClosedUserGroupSupport.getCUG(ctx, cug.getID(), cug.getSpid());
            }
            catch (Exception se)
            {                
            }            
            CallingGroupERLogMsg.generateCUGModificationER(oldCug,cug,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            
            throw new HomeException(e.getMessage(), e);
		}

		return obj;
	}

	/**
	 * This method acts as a wrapper to the removeCUG() method.
	 * Its task is to generate proper aggregation OMs and ERs.
	 *
	 * @param obj The Closed User Group to be removed.
	 */
    @Override
	public void remove(Context ctx, Object obj) throws HomeException
	{
		if (obj == True.instance())
		{
			throw new UnsupportedOperationException("removeAll unsupported.");
		}

		final ClosedUserGroup cug = (ClosedUserGroup) obj;

		try
		{
			removeCUG(ctx, cug);
		}
        catch (final HomeException e)
        {
            CallingGroupERLogMsg.generateCUGDeletionER((ClosedUserGroup)obj,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            throw e;
        }
        catch (final Exception e)
        {
            CallingGroupERLogMsg.generateCUGDeletionER((ClosedUserGroup)obj,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            throw new HomeException(e.getMessage(), e);
        }
	}

	/**
	 * Create a Closed User Group with the corresponding Friends & Family
	 * Remote service.
	 * 
	 * @param obj The Closed User Group to be created.
	 * 
	 * @return Object The created Closed User Group (with assigned ID).
	 */
	public Object createCUG(Context ctx, Object obj) throws HomeException
	{
		debugMsg(ctx, "create: " + obj);
        final ClosedUserGroup bean = (ClosedUserGroup) obj;
        try
        {
            // create the CUG instance
            final TrCugIdHolder returnedCugId = new TrCugIdHolderImpl();            
            ClosedUserGroupTemplate cugT = ClosedUserGroupSupport.getCugTemplate(ctx, bean.getCugTemplateID(), bean.getSpid());
            if (cugT == null)
            {
                String msg = "Failed to create CUG because CUG Template  " + bean.getCugTemplateID()
                        + " does not exist";
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(msg);
            }
            int result = getRmiService(ctx).createCUGAssociation(bean.getSpid(), bean.getCugTemplateID(),
                    new TrPeerMsisdn(bean.getSmsNotifyUser()), returnedCugId);
            if (result == FFECareRmiConstants.FF_ECARE_SUCCESS
                    && returnedCugId != null && returnedCugId.getValue() != null)
            {
                bean.setID(returnedCugId.getValue().cugId);
                bean.setSpid(bean.getSpid());
              
                if(bean.getName()== null || bean.getName().equals(""))
                   	bean.setName(cugT.getName());
            }
            else
            {
                String msg = "Failed to create CUG with the CUG template  " + bean.getCugTemplateID() + " ["
                        + FFECareRmiConstants.rcMessage(result) + "]";
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(msg);
            }
            // add sub to CUG
            @SuppressWarnings("unchecked")
            Map<String, ClosedSub> subsToBeAdded = bean.getSubscribers();
            new CugSubsValidator(bean).validateAllCases(ctx);
            result = getRmiService(ctx).addSubsToCUG(bean.getSpid(), bean.getID(),
                    FFClosedUserGroupSupport.convertToTrPeerMsisdn(subsToBeAdded),
                    new FFParamSetBuilder(ctx).getParameters(), new ParameterSetHolderImpl());
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                String msg = "Failed to add subscriber to CUG  " + bean.getID() + " ["
                        + FFECareRmiConstants.rcMessage(result) + "]";
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(msg);
            }
            
            
	    	if (bean.getAuxiliaryService(ctx).isPrivateCUG(ctx))
			{
				PrivateCug pcug = new PrivateCug(); 
				pcug.setID(bean.getID());
				pcug.setOwnerMSISDN(bean.getOwnerMSISDN()); 

				pcug.setAuxServiceID(bean.getAuxiliaryService(ctx).getID());

				Service ppService = (Service) ctx.get(Service.class);
				if(ppService != null)
				{
					pcug.setServiceID(ppService.getID());
				}
				
				Home pHome = (Home) ctx.get(PrivateCugHome.class);
				pHome.create(pcug); 
			}

            
        }
        catch (RemoteException e)
        {
            String msg = "Failed to create CUG instance for the template: " + bean.getCugTemplateID();
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new HomeException(msg);
        }
        return bean;

	}
	
	/**
	 * Update a Closed User Group with the corresponding Friends & Family
	 * Remote service.
	 * 
	 * @param obj The Closed User Group to be updated.
	 */
    public void storeCUG(Context ctx, Object obj) throws HomeException
    {
        debugMsg(ctx, "store: " + obj);
        final ClosedUserGroup cug = (ClosedUserGroup) obj;
        try
        {
            ClosedUserGroup oldCug = (ClosedUserGroup) ctx.get(ClosedUserGroupServiceHome.OLD_CUG);
            int spid = oldCug.getSpid();
            int result = 0;
            final TrPeerMsisdn[] newSubs;
            final TrPeerMsisdn[] removedSubs;
            final TrPeerMsisdn[] updateSubs;
            {
                final DeltaNewOldCugSubs subNewOldDelta = new DeltaNewOldCugSubs(cug.getSubscribers(), oldCug.getSubscribers());
                final Map<String, ClosedSub> subsToBeAdded = subNewOldDelta.getsubsToBeAdded();
                final Map<String, ClosedSub> subsToBeUpdated = subNewOldDelta.getsubsToBeUpdated();
                final Map<String, ClosedSub> subsToBeRemoved = subNewOldDelta.getsubsToBeRemoved();
                new CugSubsValidator(cug, subNewOldDelta).validateAllCases(ctx);
                newSubs = FFClosedUserGroupSupport.convertToTrPeerMsisdn(subsToBeAdded);
                updateSubs = FFClosedUserGroupSupport.convertToTrPeerMsisdn(subsToBeUpdated);
                removedSubs = FFClosedUserGroupSupport.convertToTrPeerMsisdn(subsToBeRemoved);
            }
            
            checkAuxSvc(ctx, oldCug, cug, newSubs);
            // update CUG template ID
            if (cug.getCugTemplateID() != oldCug.getCugTemplateID())
            {
                result = getRmiService(ctx).updateCUGAssociation(spid, cug.getID(), cug.getCugTemplateID());
                if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
                {
                    String msg = "Failed to update CUG template ID for the CUG instance " + cug.getID();
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(new FFEcareException(msg, result));
                }
                if (cug.getAuxiliaryService(ctx).isPrivateCUG(ctx))
                {
                    PrivateCug pcug =  ClosedUserGroupSupport73.findPrivateCug( ctx, cug.getID());
                  
                    pcug.setID(cug.getID());
                    pcug.setOwnerMSISDN(cug.getOwnerMSISDN()); 

                    pcug.setAuxServiceID(cug.getAuxiliaryService(ctx).getID());

                    Service ppService = (Service) ctx.get(Service.class);
                    if(ppService != null)
                    {
                        pcug.setServiceID(ppService.getID());
                    }
                    
                    Home pHome = (Home) ctx.get(PrivateCugHome.class);
                    pHome.store(pcug); 
                }
            }
            
            // remove the obselete subs first; if we add first, we might be over the max limit.
            if (removedSubs != null && removedSubs.length > 0)
            {
                result = getRmiService(ctx).removeSubsFromCUG(cug.getSpid(), cug.getID(), removedSubs, new FFParamSetBuilder(ctx).getParameters(),new ParameterSetHolderImpl());
                if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
                {
                    String msg = "Failed to remove subs from CUG " + cug.getID() + ", Result : " + result;
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(new FFEcareException(msg, result));
                }
            }
            // add or remove subscribers
            if (newSubs != null && newSubs.length > 0)
            {
                result = getRmiService(ctx).addSubsToCUG(cug.getSpid(), cug.getID(), newSubs, new FFParamSetBuilder(ctx).getParameters(),new ParameterSetHolderImpl());
                if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
                {
                    String msg = "Failed to add subs to CUG " + cug.getID() + ", Result : " + result;
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(new FFEcareException(msg, result));
                }
            }
            
            if (updateSubs != null && updateSubs.length > 0)
            {
                result = getRmiService(ctx).updateSubForCug(cug.getSpid(), cug.getID(), updateSubs, new FFParamSetBuilder(ctx).getParameters());
                if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
                {
                    String msg = "Failed to remove subs from CUG " + cug.getID() + ", Result : " + result;
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(new FFEcareException(msg, result));
                }
            }
            // update sms notify user
            if ((oldCug.getSmsNotifyUser() == null && cug.getSmsNotifyUser() != null)
                    || (oldCug.getSmsNotifyUser() != null && cug.getSmsNotifyUser() == null)
                    || (oldCug.getSmsNotifyUser() != null && cug.getSmsNotifyUser() != null && !oldCug
                            .getSmsNotifyUser().equals(cug.getSmsNotifyUser())))
            {
                result = getRmiService(ctx).updateCUGNotifyMsisdnWithSpid(cug.getSpid(), cug.getID(), cug.getSmsNotifyUser());
                if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
                {
                    String msg = "Failed to remove subs from CUG " + cug.getID() + ", Result : " + result;
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(new FFEcareException(msg, result));
                }
            }
        }
        catch (RemoteException e)
        {
            String msg = "Failed to update CUG: " + cug.getID();
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new HomeException(new FFEcareException(msg, ExternalAppSupport.REMOTE_EXCEPTION));
        }
    }

    private void checkAuxSvc(Context ctx, final ClosedUserGroup cug, final ClosedUserGroup oldCug,
            TrPeerMsisdn[] newSubs) throws HomeException
    {
        // new aux service
        final AuxiliaryService auxSvc = CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(ctx, cug
                .getCugTemplateID());
        AuxiliaryService oldAuxSvc = auxSvc;
        if (oldCug.getCugTemplateID() != cug.getCugTemplateID())
        {
            oldAuxSvc = CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(ctx, oldCug.getCugTemplateID());
        }
        if (auxSvc == null)
        {
            throw new HomeException("Cannot find the associate auxiliary service " + cug.getCugTemplateID());
        }
        else if (oldAuxSvc == null)
        {
            throw new HomeException("Cannot find the associate auxiliary service " + oldCug.getCugTemplateID());
        }
        else if (auxSvc.getState() == AuxiliaryServiceStateEnum.DEPRECATED && newSubs != null && newSubs.length > 0)
        {
            throw new HomeException("Cannot add msisdn to CUG " + cug.getID()
                    + " because its auxiliary service is in deprecate state");
        }
    }


    /**
     * Remove a Closed User Group with the corresponding Friends & Family Remote service.
     * 
     * @param obj
     *            The Closed User Group to be removed.
     */
    public void removeCUG(Context ctx, Object obj) throws HomeException
    {
        debugMsg(ctx, "remove: " + obj);
        final ClosedUserGroup bean = (ClosedUserGroup) obj;
        try
        {
            int result = getRmiService(ctx).deleteCUGAssociation(bean.getSpid(), bean.getID());
            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS && 
                result != FFECareRmiConstants.FF_ECARE_CUG_INSTANCE_NOT_FOUND)
            {
                String msg = "Failed to remove CUG " + bean.getID();
                new MinorLogMsg(this, msg, null).log(ctx);
                throw new HomeException(new FFEcareException(msg, result));
            }
            
            if (bean.getAuxiliaryService(ctx).isPrivateCUG(ctx))
            {           
                Home pHome = (Home) ctx.get(PrivateCugHome.class);
                Object pcug = pHome.find(new EQ(PrivateCugXInfo.ID, new Long(bean.getID()))); 
                if (pcug != null)
                {   
                    pHome.remove(ctx, pcug);
                }   
            }

        }
        catch (RemoteException e)
        {
            String msg = "Failed to remove CUG: " + bean.getID();
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new HomeException(new FFEcareException(msg, ExternalAppSupport.REMOTE_EXCEPTION));
        }
    }	
    
    /**
	 * @see com.redknee.framework.xhome.home.HomeSPI#select(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
    @Override
    public Collection select(Context ctx, Object where) throws HomeException, UnsupportedOperationException
    {
        // CUGs are stored on URCS. In order to support any kind of select, we'd have to
        // retrieve ALL cugs from URCS and perform a filter using the predicate. As each
        // subscriber can have one or more CUGs, it's not a good idea to do it. Therefore,
        // the current implementation only supports simple ANDs.
        new DebugLogMsg(this, "selectAll " + where, null).log(ctx);
        
        final Long cugId = (Long) ClosedUserGroupSupport.getPropertyArgumentFromFilter(where, ClosedUserGroupXInfo.ID, Long.class);
        final String name = (String) ClosedUserGroupSupport.getPropertyArgumentFromFilter(where, ClosedUserGroupXInfo.NAME, String.class);
        final Integer spid = (Integer) ClosedUserGroupSupport.getPropertyArgumentFromFilter(where, ClosedUserGroupXInfo.SPID, Integer.class);
        
        return selectAnd(ctx, cugId, name, spid);
    }

    @Override
    public Visitor forEach(Context ctx, Visitor visitor, Object where)
    throws HomeException
    {
        Collection retrievedObjects = select(ctx, where);
    
        for ( Iterator i = retrievedObjects.iterator() ; i.hasNext() ; )
        {
           try
           {
              Object bean = i.next();
              visitor.visit(ctx, bean);
           }
           catch (AbortVisitException e)
           {
              break;
           }
           catch (AgentException e)
           {
              // This is so that we preserve the type of the original HomeException
              if ( e.getCause() != null && e.getCause() instanceof HomeException )
              {
                 throw (HomeException) e.getCause();
              }
    
              throw new HomeException(e);
           }
        }
    
        return visitor;
     }   

    
    private boolean hasSpid(ClosedUserGroup cug, Integer spid)
    {
        boolean result = true;
        if (spid!=null)
        {
            result = (cug.getSpid() == spid.intValue());
        }
        return result;
    }
    
    private boolean hasName(ClosedUserGroup cug, String name)
    {
        boolean result = true;
        if (name!=null)
        {
            result = (cug.getName().startsWith(name));
        }
        return result;
    }    
    
    private Collection<ClosedUserGroup> selectAnd(Context ctx, Long cugId, String name, Integer spid) throws HomeException
    {
        Collection<ClosedUserGroup> cugList = null;
        if (cugId != null)
        {
            cugList = new ArrayList<ClosedUserGroup>();
            ClosedUserGroup cug = this.getCUGByID(ctx, cugId, spid);
            if (cug!=null && hasName(cug, name) && hasSpid(cug, spid))
            {
                cugList.add(cug);
            }
        }        
        else if (name != null && name.trim().length() > 0)
        {
            cugList = new ArrayList<ClosedUserGroup>();
            Collection<ClosedUserGroup> cugByNameList = this.getCUGByName(ctx, name.trim(), spid);
            for (ClosedUserGroup cug : cugByNameList)
            {
                if (hasSpid(cug, spid))
                {
                    cugList.add(cug);
                }
            }
        }
        else if (spid != null)
        {
            cugList = this.getCUGBySPID(ctx, spid.intValue());
        }
        
        return cugList;
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
    @Override
    public Object find(Context ctx, Object key) throws HomeException
    {
        debugMsg(ctx, "find: " + key);
        if (key instanceof Long)
        {
            final Long id = (Long) key;
            ClosedUserGroup bean = null;
            
            try
            {
            	final TrCugHolder retrieved_cug = new TrCugHolderImpl();
                // FIXME THE SPID needs to be inputted

               	Collection spids = ClosedUserGroupSupport.getApplicableSpids(ctx, null);
               	if(spids != null)
               	{
               		for(Object spidObj : spids)
               		{
               			Spid spid = (Spid)spidObj;
               			debugMsg(ctx, "Fetching CUG for Spid :" + spid.getSpid() + " and CUG for ID : " + id.longValue());
               			try
               			{
               				final int result = getRmiService(ctx).getCUG(spid.getSpid(), id.longValue(), retrieved_cug);
               				if (result == FFECareRmiConstants.FF_ECARE_SUCCESS)
               				{
               					if (retrieved_cug != null)
               					{
               						bean = FFClosedUserGroupSupport.convertFFCugToCrmCug(ctx, retrieved_cug.getValue());
               						break;
               					}
               				}
               			}
               			catch (RemoteException e)
                        {
                            String msg = "Failed to retrieve CUG for ID : " + id.longValue();
                            new MinorLogMsg(this, msg, e).log(ctx);
                        }
                        catch (HomeException e)
                        {
                            String msg = "Failed to retrieve CUG for ID : " + id.longValue();
                            new MinorLogMsg(this, msg, e).log(ctx);
                        }
               		}
               	}
               	else
               	{
               		String msg = "Failed to retrieve CUG for ID : " + id.longValue() +", as not able to retrieve SPIDs assigned to User.";
               		new MinorLogMsg(this, msg, null).log(ctx);
               		throw new HomeException(msg);
               	}
               	
            }
            catch (Exception e)
            {
                String msg = "Failed to retrieve CUG for ID : " + id.longValue();
                new MinorLogMsg(this, msg, e).log(ctx);
                throw new HomeException(msg, e);
            }
            
            return bean;
        }
        Collection col = select(ctx, key);
        if (col != null && col.size() > 0)
        {
            return col.iterator().next();
        }
        return super.find(ctx, key);
    }

    
	/**
	 * UNSUPPORTED OPERATION
	 */
    @Override
	public void drop(Context ctx) throws HomeException, UnsupportedOperationException
	{
		throw new UnsupportedOperationException("drop unsupported.");
	}

	/**
	 * Return the remote Friends & Family RMI service.
	 * 
	 * @return FFECareRmiService The Friends & Family RMI service.
	 */
	protected FFECareRmiService getRmiService(Context ctx) throws HomeException
	{
	    try
	    {
	        return FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass());
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }
	}

//    /**
//     * Find the new subscribers added to the CUG
//     * 
//     * @param bean
//     *            The given ClosedUserGroup bean.
//     * @param oldSubs
//     *            List of subscribers originally found in the CUG.
//     * 
//     * @return Array of msisdns newly added to the CUG.
//     */
//    public TrPeerMsisdn[] getNewSubsAddedToCUG(final ClosedUserGroup bean, final Collection oldSubs)
//    {
//        if (bean == null)
//        {
//            return null;
//        }
//        // Determine newly-added subscribers.
//        Set msisdns = new HashSet(bean.getSubscribers().keySet());
//        msisdns.removeAll(oldSubs);
//        return FFClosedUserGroupSupport.convertToTrPeerMsisdn(msisdns);
//    }
//
//
//    /**
//     * Find the subscribers removed from the CUG
//     * 
//     * @param bean
//     *            The given ClosedUserGroup bean.
//     * @param oldSubs
//     *            List of subscribers originally found in the CUG.
//     * 
//     * @return Array of msisdns newly removed from the CUG.
//     */
//    public TrPeerMsisdn[] getSubsRemovedFromCUG(final ClosedUserGroup bean, final Collection oldSubs)
//    {
//        if (bean == null)
//        {
//            return null;
//        }
//        // Determine removed subscribers.
//        HashSet<String> msisdns = new HashSet<String>(oldSubs);
//        msisdns.removeAll(bean.getSubscribers().keySet());
//        return FFClosedUserGroupSupport.convertToTrPeerMsisdn(msisdns);
//    }	

	/**
	 * Return the Closed User Group having the given CUG ID (with the
	 * corresponding Friends & Family Remote service).
	 * 
	 * @param id The given CUG ID.
	 * 
	 * @return ClosedUserGroup The result Closed User Group; or null if
	 * no matching Closed User Group could be found.
	 */
    public ClosedUserGroup getCUGByID(Context ctx, final long id, Integer cugSpid) throws HomeException, UnsupportedOperationException
    {
        debugMsg(ctx, "getCUGByID");
        ClosedUserGroup cug = null;
        try
        {
            final TrCugHolder holder = new TrCugHolderImpl();
            Collection spids =  ClosedUserGroupSupport.getApplicableSpids(ctx, cugSpid);
            if(spids != null)
           	{
           		for(Object spidObj : spids)
           		{
           			Spid spid = (Spid)spidObj;
           			debugMsg(ctx, "Fetching CUG for Spid :" + spid.getSpid() + " and CUG for ID :" + id);
           			try
           			{
           				int result = getRmiService(ctx).getCUG(spid.getSpid(), id, holder);
           				if (holder != null && holder.getValue() != null)
           				{
           					cug = FFClosedUserGroupSupport.convertFFCugToCrmCug(ctx, holder.getValue());
           					break;
           				}
           			}
           			catch (RemoteException e)
           	        {
           	            String msg = "Failed to retrieve CUG for ID " + id;
           	            new MinorLogMsg(this, msg, e).log(ctx);
           	        }
           	        catch (HomeException e)
           	        {
           	            String msg = "Failed to retrieve CUG for ID " + id;
           	            new MinorLogMsg(this, msg, e).log(ctx);
           	        }
           		}
           	}
           	else
           	{
           		String msg = "Failed to retrieve CUG for ID : " + id +", as not able to retrieve SPIDs assigned to User.";
           		new MinorLogMsg(this, msg, null).log(ctx);
           		throw new HomeException(msg);
           	}
        }
        catch (Exception e)
        {
            String msg = "Failed to retrieve CUG for ID : " + id;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        return cug;
    }

    /**
     * Return a list of Closed User Groups starting with the given CUG name (with the
     * corresponding Friends & Family Remote service).
     * 
     * @param name
     *            The given CUG name.
     * 
     * @return Collection The result list of Closed User Groups.
     */
    public Collection<ClosedUserGroup> getCUGByName(Context ctx, final String name, Integer cugSpid) throws HomeException,
            UnsupportedOperationException
    {
        debugMsg(ctx, "getCUGByName");
        List<TrCug> trCugList = new ArrayList<TrCug>();        
        try
        {
            final TrCugListHolder holder = new TrCugListHolderImpl();
            
            Collection spids = ClosedUserGroupSupport.getApplicableSpids(ctx, cugSpid);
           	if(spids != null)
           	{
           		for(Object spidObj : spids)
           		{
           			Spid spid = (Spid)spidObj;
           			debugMsg(ctx, "Fetching CUG for Spid :" + spid.getSpid() + " with Name : " + name);
           			try
           			{
            
           				getRmiService(ctx).getCUGByName(spid.getSpid(), name, holder);
           				if (holder != null && holder.getValue() != null)
           				{
           				    for(TrCug cug: holder.getValue().trCugList)
           				    {
           				        if(cug.getSpId() == spid.getSpid())
           				        {
           				            trCugList.add(cug);
           				        }
           				    }
           				}
           			}
           	        catch (RemoteException e)
           	        {
           	            String msg = "Failed to retrieve CUGs with Name : " + name;
           	            new MinorLogMsg(this, msg, e).log(ctx);
           	        }
           	        catch (HomeException e)
           	        {
           	            String msg = "Failed to retrieve CUGs with Name : " + name;
           	            new MinorLogMsg(this, msg, e).log(ctx);           	            
           	        }
           		}
           	}
           	else
           	{
           		String msg = "Failed to retrieve CUGs with Name : " + name +", as not able to retrieve SPIDs assigned to User.";
           		new MinorLogMsg(this, msg, null).log(ctx);
           		throw new HomeException(msg);
           	}
        }
        catch (Exception e)
        {
            String msg = "Failed to retrieve CUGs with Name : " + name;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        
        final Collection<ClosedUserGroup> cugList = new ArrayList<ClosedUserGroup>();
        for (TrCug cug : trCugList)
        {
            cugList.add(FFClosedUserGroupSupport.convertFFCugToCrmCug(ctx, cug));
        }
        return cugList;
    }


    /**
     * Return a list of Closed User Groups having the given SPID (with the
     * corresponding Friends & Family Remote service).
     * 
     * @param spid The given SPID.
     * 
     * @return Collection The result list of Closed User Groups.
     */
    public Collection<ClosedUserGroup> getCUGBySPID(Context ctx, final int spid) throws HomeException,
            UnsupportedOperationException
    {
        debugMsg(ctx, "getCUGBySPID");
        List<TrCug> trCugList = new ArrayList<TrCug>();
        try
        {
            final TrCugListHolder holder = new TrCugListHolderImpl();
            getRmiService(ctx).getCUGBySPID(spid, holder);
            if (holder != null && holder.getValue() != null)
            {
                for(TrCug cug: holder.getValue().trCugList)
                {
                    trCugList.add(cug);
                }
            }
        }
        catch (RemoteException e)
        {
            String msg = "Failed to get CUGs with SPID " + spid;
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(new FFEcareException(msg, ExternalAppSupport.REMOTE_EXCEPTION));
        }
        final Collection<ClosedUserGroup> cugList = new ArrayList<ClosedUserGroup>();
        for (TrCug cug : trCugList)
        {
            cugList.add(FFClosedUserGroupSupport.convertFFCugToCrmCug(ctx, cug));
        }
        return cugList;
    }


    private void debugMsg(Context ctx, String msg)
	{
		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, msg, null).log(ctx);
		}
	}

	private static final int CUG_RATEPLAN = 3;

	private static final int CUG_DISCOUNT = 4;

	public static final String OLD_CUG = "OLD_CUG";
	

}
