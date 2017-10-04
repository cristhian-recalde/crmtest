/*
 *  BundleAuxiliaryServiceAgent.java
 *
 *  Author : danny.ng@redknee.com
 *  Date   : Dec 16, 2005
 *
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
package com.trilogy.app.crm.clean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleAuxiliaryService;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXDBHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.support.BundleAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * This agent handles provisioning and unprovisioning of subscriber
 * auxiliary service bundles if the start or end date matches
 * the running date (usually the day the agent is run) which can be
 * set as a parameter within the cron task
 * 
 * Lots of this code is copied from ProvisionAuxiliaryServiceAgent
 * 
 * 9.8 : Refactored the code to avoid use of for-each-visitor pattern
 * Issue addressed : The visitor working on bundleAuxServ resultset would lock all the rows 
 * (or complete table, if lock escalation happens) till its processing is finished. TT#14010409011
 *  
 * @author danny.ng@redknee.com
 *
 */
public class BundleAuxiliaryServiceAgent implements ContextAgent
{

    private TaskEntry task = null;
    private Date runningDate = null;

    public BundleAuxiliaryServiceAgent()
    {
        super();
    }

    public BundleAuxiliaryServiceAgent(TaskEntry task)
    {
        super();
        this.task = task;
    }


    public void execute(Context ctx) throws AgentException
    {
        // Agent relies on Subscriber pipeline for bundle provisioning and charging
        // So, the only way to test it is to backdate the startdate in the
        // record. Reading a date from task parameter will defenetly not work.
        // victor.stratan@redknee.com

        runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        long runningDateInMilS = runningDate.getTime();

        try
        {
            ctx = ctx.createSubContext();

            // Store running date into sub context so others will know when
            // is the date we want it to run at
            ctx.put(CommonTime.RUNNING_DATE, runningDate);
            ctx.put(CronConstants.FROM_CRON_AGENT_CTX_KEY, Boolean.TRUE);

            //Home bundleHome = (Home) ctx.get(BundleAuxiliaryServiceHome.class);

            /*
             * Provision ones that are suppose to be provisioned
             * The bundle has met its start date
             */

            ctx.put(Common.FORCE_BM_PROVISION_CALL, Boolean.TRUE);
           
            List <String> auxSvcIdsList = new ArrayList<String>();
        	
            try 
            {
				auxSvcIdsList = getBundleAuxServiceIDsToProvision(ctx, runningDateInMilS);
			} 
            catch (HomeException e1) 
			{
				LogSupport.minor(ctx, this, "An exception occured while retrieving list of BundleAuxiliaryServiceIds eligible for provisioning.", e1);
				throw new AgentException("An exception occured while retrieving list of BundleAuxiliaryServiceIds eligible for provisioning.", e1);
			}
            
            if(!auxSvcIdsList.isEmpty())
            {
            	Iterator <String> i = ((ArrayList<String>) auxSvcIdsList).listIterator();
            	while(i.hasNext())
            	{
            		String bundleAuxSvcIdToProv = i.next();
            
		            BundleAuxiliaryService bundleAuxSvcsToProv = null;
		            try 
		            {
						bundleAuxSvcsToProv = (BundleAuxiliaryService) HomeSupportHelper.get(ctx).findBean(ctx,
										BundleAuxiliaryService.class, new EQ(BundleAuxiliaryServiceXInfo.BUNDLE_AUX_SERV_ID, bundleAuxSvcIdToProv));
					}
		            catch (HomeException e) 
		            {
		            	LogSupport.minor(ctx, this, "Exception occured while retrieving BundleAuxiliaryService "+bundleAuxSvcIdToProv, e);
					}
		            if(bundleAuxSvcsToProv != null)
		            {
		            	provisionEligibleBundles(ctx, bundleAuxSvcsToProv);
		            }
	            }
	            
            }
            else
            {
            	LogSupport.info(ctx, this, "No eligible BundleAuxiliaryService found for provisioning.");
            }
            
            /*
             * Unprovision ones that are supposed to be unprovisioned
             * The bundle has met its end date.
             */
            
            List <String> auxSvcIdsUnProvList = new ArrayList<String>();
        	
            try 
            {
				auxSvcIdsUnProvList = getBundleAuxServiceIDsToUnprovision(ctx, runningDateInMilS);
			} 
            catch (HomeException e2) 
			{
				LogSupport.minor(ctx, this, "An exception occured while retrieving list of BundleAuxiliaryServiceIds eligible for unprovisioning.", e2);
				throw new AgentException("An exception occured while retrieving list of BundleAuxiliaryServiceIds eligible for unprovisioning.", e2);
			}
            
            if(!auxSvcIdsUnProvList.isEmpty())
            {
            	Iterator <String> i = ((ArrayList<String>) auxSvcIdsUnProvList).listIterator();
            	while(i.hasNext())
            	{
            		String bundleAuxSvcIdToUnprov = i.next();
            
		            BundleAuxiliaryService bundleAuxSvcsToUnprov = null;
		            try 
		            {
		            	bundleAuxSvcsToUnprov = (BundleAuxiliaryService) HomeSupportHelper.get(ctx).findBean(ctx,
										BundleAuxiliaryService.class, new EQ(BundleAuxiliaryServiceXInfo.BUNDLE_AUX_SERV_ID, bundleAuxSvcIdToUnprov));
					}
		            catch (HomeException e) 
		            {
		            	LogSupport.minor(ctx, this, "Exception occured while retrieving BundleAuxiliaryService "+bundleAuxSvcIdToUnprov, e);
					}
		            if(bundleAuxSvcsToUnprov !=  null)
		            {
		            	unprovisionEligibleBundles(ctx, bundleAuxSvcsToUnprov);
		            }
	            }
            }
            else
            {
            	LogSupport.info(ctx, this, "No eligible BundleAuxiliaryService found for unprovisioning");
            }
        }
        finally
        {
            ctx.remove(Common.FORCE_BM_PROVISION_CALL);
        }
    }
    
    
    /**
     * Query and bring AuxiliaryBundleService IDs that are eligible for provisioning into a list at once.
     * @param ctx
     * @param runningDate
     * @return List of AuxiliaryBundleServiceIds to provision
     * @throws HomeException
     */
    private List<String> getBundleAuxServiceIDsToProvision(Context ctx, long runningDate) throws HomeException
    {
        final List <String> auxSvcIdsList = new ArrayList<String>();
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	//SELECT * FROM BUNDLEAUXSERV WHERE (provisioned =  @P0  and ((startDate <=  @P1  or  @P2  <= startDate and startDate <=  @P3 )))
    	
    	final String bundleAuxiliaryServiceTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, BundleAuxiliaryServiceHome.class,
    			BundleAuxiliaryServiceXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select ").append("BUNDLEAUXSERVID");
    	query.append(" from ").append(bundleAuxiliaryServiceTableName);
    	query.append(" where (").append("PROVISIONED").append(" = 'n'");
    	query.append(" and ").append("STARTDATE").append(" <= ").append(runningDate).append(")");
    	
    	String sql = query.toString(); 
    	try
    	{
	    	xdb.forEach(ctx, 
	    			new Visitor()
			    	{
						private static final long serialVersionUID = 1L;
			
						public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
						{
							try 
							{
								auxSvcIdsList.add(((XResultSet) obj).getString(1));
							} 
							catch (SQLException e) 
							{
								throw new AgentException(e);
							}
						}
			    	}
	    			, sql);
    	}
    	catch(Exception ex)
    	{
    		throw new HomeException(ex);
    	}
    	
    	return auxSvcIdsList;
    }
    
    private List<String> getBundleAuxServiceIDsToUnprovision(Context ctx, long runningDate) throws HomeException
    {
        final List <String> auxSvcIdsList = new ArrayList<String>();
    	
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	final String bundleAuxiliaryServiceTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, BundleAuxiliaryServiceHome.class,
    			BundleAuxiliaryServiceXInfo.DEFAULT_TABLE_NAME);
    	
    	StringBuilder query = new StringBuilder();
    	query.append("select ").append("BUNDLEAUXSERVID");
    	query.append(" from ").append(bundleAuxiliaryServiceTableName);
    	query.append(" where ").append("ENDDATE").append(" < ").append(runningDate);
    	query.append(" and ").append("PROVISIONED").append(" = 'y'");
    	 
    	String sql = query.toString(); 
    	try
    	{
	    	xdb.forEach(ctx, 
	    			new Visitor()
			    	{
						private static final long serialVersionUID = 1L;
			
						public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException 
						{
							try 
							{
								auxSvcIdsList.add(((XResultSet) obj).getString(1));
							} 
							catch (SQLException e) 
							{
								throw new AgentException(e);
							}
						}
			    	}
	    			, sql);
    	}
    	catch(Exception ex)
    	{
    		throw new HomeException(ex);
    	}
    	
    	return auxSvcIdsList;
    }
    
    

	/**
	 * Method that provisions eligible bundles to the subscriber
	 * @param ctx
	 * @param bundle
	 * @throws AgentException
	 */
	private void provisionEligibleBundles(Context ctx, BundleAuxiliaryService bundleObj) throws AgentException
	{
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Processing bundle : " + bundleObj.getId()
					+ " for provisioning to Subscriber " + bundleObj.getSubscriberId());
		}
		
		BundleAuxiliaryService bundle = null;
        try
        {
            bundle = (BundleAuxiliaryService) bundleObj.clone();
        }
        catch (CloneNotSupportedException e2)
        {
            LogSupport.minor(ctx, this, "Could not clone bundle " + bundleObj.getId(), e2);
            return;
        }
		
		Subscriber sub = null;
		try
		{
			sub = SubscriberSupport.lookupSubscriberForSubId(ctx, bundle.getSubscriberId());
		}
		catch (HomeException e1)
		{
			LogSupport.minor(ctx, this, "Could not find subscriber " + bundle.getSubscriberId() + " in database. Will not process bundle "+bundle.getId(), e1);
			return;
		}

		if (sub == null)
		{
			LogSupport.minor(ctx, this, "Could not find subscriber " + bundle.getSubscriberId() 
					+ " in database.  Investigate missing subscriber and remove this bundle auxiliary service entry if subscriber is missing.");
			return;
		}
		else
		{
			SubscriberStateEnum state = sub.getState();
			if (state == SubscriberStateEnum.PENDING || state == SubscriberStateEnum.AVAILABLE 	|| state == SubscriberStateEnum.INACTIVE)
			{
				LogSupport.info(ctx, this, "State of Subscriber with ID "+sub.getId()+ " is " +state+ ". Will not process bundle "+bundle.getId());
				return;
			}
			else
			{
				/*
			     * Check if the bundle has already been provisioned, if it has, just
			     * update the bundle entry in the BundleAuxiliaryServiceHome
			     */
				boolean isProvisioned = false;
				try
				{
					// Check if bundle got provisioned on BM
					isProvisioned = BundleAuxiliaryServiceSupport.isBucketProvisioned(ctx, (int) sub.getSubscriptionType(), sub.getMSISDN(), bundle.getId());
				}
				catch (Exception e)
				{
					LogSupport.minor(ctx, this, "Error while checking if the bundle was provisioned on BM. Bundle '" + bundle.getId()
							+ "' subscriber '" + sub.getMSISDN() + "'", e);
					return;
				}
				
				Home bundleAuxiliaryServiceHome = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
				
				if (isProvisioned)
				{
				/*
				 * Bundle was already provisioned on BM, just update the BundleAuxiliaryService's
				 * provisioned flag to true
				 */
					bundle.setProvisioned(true);
					 try
					{
						LogSupport.info(ctx, this, "Bundle "+ bundle+" was already provisioned to subscriber "+sub.getId()+" on BM. Updating provisioned flag to true." );
											
						bundleAuxiliaryServiceHome.store(ctx, bundle);
					}
					catch (Exception e)
					{
						LogSupport.minor(ctx, this, "Error while marking record as provisioned. Bundle '" + bundle.getId()
								+ "' Subscriber '" + bundle.getSubscriberId() + "'", e);
						return;
					}
				}
				else
				{
					/*
					 * Bundle hasn't been provisioned, so remove the bundle
					 * from the subscriber, and then add it back so to activate
					 * the necessary effects of the provisioning
					 */
					final Map<Long, BundleFee> bundles = sub.getBundles();
					final Map<Long, BundleFee> addBundles = new HashMap<Long, BundleFee>();
					
					for (Iterator<BundleFee> i = bundles.values().iterator(); i.hasNext();)
					{
						BundleFee fee = i.next();
						try
						{
							if (fee.getStartDate().before(new Date()) &&
									!BundleAuxiliaryServiceSupport.isBucketProvisioned(ctx, (int) sub.getSubscriptionType(),
											sub.getMSISDN(), fee.getId()))
							{
								addBundles.put(Long.valueOf(fee.getId()),fee);
							}
						}
						catch (Exception e)
						{
							LogSupport.minor(ctx, this, "BundleAuxiliaryServiceAgent encountered an error checking if the bundle was provisioned. . Bundle '" + bundle.getId()
								+ "' subscriber '" + sub.getMSISDN() + "'");
							return;
						}
					}
					
					
					for (Iterator<BundleFee> i = addBundles.values().iterator(); i.hasNext();)
					{
						bundles.remove(Long.valueOf(i.next().getId()));
					}
					
					// Store the subscriber minus bundles which start now on the running date
					sub.setBundles(bundles);
					final Home subXDBHome = (Home) ctx.get(SubscriberXDBHome.class);
					try
					{
						subXDBHome.store(ctx, sub);
					}
					catch (HomeException e)
					{
						LogSupport.minor(ctx, this, "BundleAuxiliaryService task encountered an error storing the subscriber "+sub.getId()+" into database.", e);
					}
					
					for (Iterator<BundleFee> i = addBundles.values().iterator(); i.hasNext();)
					{
						BundleFee fee = i.next();
						bundles.put(Long.valueOf(fee.getId()),fee);
					}
					
					// Set the new list of selected bundles
					sub.setBundles(bundles);
					
					final Home subHome = (Home) ctx.get(SubscriberHome.class);
					try
					{
						subHome.store(ctx, sub);
					}
					catch (HomeException e)
					{
						LogSupport.minor(ctx, this, "BundleAuxiliaryService task encountered an error storing the subscriber "+sub.getId()+" into database.", e);
					}
					
					/*
					 * Check if it got provisioned, if it did, update this entry's provisioned flag to true
					 */
					try
					{
						isProvisioned = BundleAuxiliaryServiceSupport.isBucketProvisioned(ctx, (int) sub.getSubscriptionType(),
								sub.getMSISDN(), bundle.getId());
						if (isProvisioned)
						{
							LogSupport.info(ctx, this, "Bundle provisioning on BM successful for subscriber "+sub.getId()+" updating bundle provision flag to true for bundle "+bundle.getId());
							
							// Update entry from BundleAuxiliaryServiceHome
							Home bundleHome = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
							bundle.setProvisioned(true);
							// Need to create subcontext so not to interfere with the DB connection being
							// used by the forEach iterating through this home
							bundleHome.store(ctx.createSubContext(), bundle);
						}
						else
						{
							/*
							 * NOOP - the provisioning failed for any number of reasons,  
							 * we'll leave this bundle entry with the provisioned flag to false
							 * so this agent can process it again some other time and
							 * maybe it'll work then
							 */
							LogSupport.minor(ctx, this, "BundleAuxiliaryServiceAgent: Bundle did not provision sucessfully "+bundle.getId(), null);
						 }
					}
					catch (Exception e)
					{
						LogSupport.minor(ctx, this, "BundleAuxiliaryService task encountered an error processing the bundle "+bundle.getId(), e);
					}
				}			
			}
		}

	}
	
	/**
	 * Method that unprovisions the eligible bundles
	 * @param ctx
	 * @param bundle
	 * @return
	 * @throws AgentException
	 */
	private void unprovisionEligibleBundles(Context ctx, BundleAuxiliaryService bundle) throws AgentException
	{
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Processing bundle " + bundle.getId()
					+ " for unprovisioning for Subscriber " + bundle.getSubscriberId());
		}
		Subscriber sub = null;
		try
		{
			sub = SubscriberSupport.lookupSubscriberForSubId(ctx, bundle.getSubscriberId());
		}
		catch (HomeException e1)
		{
			LogSupport.minor(ctx, this, "Could not find subscriber " + bundle.getSubscriberId() + " in database. Will not process bundle "+bundle.getId(), e1);
			return;
		}

		if (sub == null)
		{
			LogSupport.minor(ctx, this, "Could not find subscriber " + bundle.getSubscriberId() + " in database.  Investigate missing subscriber and remove this bundle auxiliary service entry if subscriber is missing.");
			return;
		}
		else
		{
			SubscriberStateEnum state = sub.getState();
			if (state == SubscriberStateEnum.INACTIVE)
			{
				LogSupport.minor(ctx, this, "State of Subscriber with ID "+sub.getId()+ " is " +state+ ". Will not process bundle "+bundle.getId());
				return;
			}
			else
			{
				Map<Long, BundleFee> bundles = sub.getBundles();
				bundles.remove(Long.valueOf(bundle.getId()));
				
				// Set the new list of selected bundles
				sub.setBundles(bundles);
				
				Home subHome = (Home) ctx.get(SubscriberHome.class);
				try
				{
					subHome.store(ctx, sub);
				}
				catch (Exception e)
				{
					LogSupport.minor(ctx, this, "BundleAuxiliaryServiceAgent encountered an error storing the subscriber " 
							+ sub.getId() + " for bundle " + bundle.getId(), e);
					return;
				}
					
				 // Remove entry from BundleAuxiliaryServiceHome
		        Home bundleHome = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
		        try
		        {
		        	if(LogSupport.isDebugEnabled(ctx))
		        	{
		        		LogSupport.debug(ctx, this, "Removing bundle from BundleAuxiliaryServiceHome " + bundle.getId());
		        	}
		            
		            bundleHome.remove(ctx, bundle);  
		        }
		        catch (Exception e)
		        {
		            LogSupport.minor(ctx, this, "Error while removing bundle record. Bundle '" + bundle.getId()
		                    + "' subscriber '" + sub.getId() + "'", e);            
		        }
			}
		}
	}

}


