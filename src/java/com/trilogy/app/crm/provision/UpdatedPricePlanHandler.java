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
package com.trilogy.app.crm.provision;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequest;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXDBHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.log.PricePlanVersionActivationEventRecord;
import com.trilogy.app.crm.support.MultiDbSupport;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InternalLogSupport;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Searches for applicable subscribers for a price-plan version update, and
 * updates those subscribers to the new version.
 *
 * @author gary.anderson@redknee.com
 */
public
class UpdatedPricePlanHandler
    implements ContextAware
{
    /**
     * Creates a new UpdatedPricePlanHandler that when run will update all
     * existing subscribers from the old PricePlanVersion to the new
     * PricePlanVersion.
     *
     * @param context The operating context.
     * @param plan The plan to which the versions belong.
     * @param newVersion The new PricePlanVersion.
     *
     * @exception IllegalArgumentException Thrown when any of the parameters is
     * null, when the two versions refer to different PricePlans, and when the
     * new version is not strictly newer than the old version.
     */
    public UpdatedPricePlanHandler(
        final Context context,
        final PricePlan plan,
        final PricePlanVersion newVersion)
    {
        // Throws IllegalArgumentException.
        validate(context, plan, newVersion);

        contextSupport_.setContext(context);

        plan_ = plan;
        newVersion_ = newVersion;
    }

    /**
     * @return
     */
    public Context getContext()
    {
        return contextSupport_.getContext();
    }

    /**
     * @param context
     */
    public void setContext(final Context context)
    {
        contextSupport_.setContext(context);
    }


    /**
     * Creates the requests in the database to have the associated subscribers
     * updated.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context.
     */
    public void createMergeRequest()
        throws HomeException
    {
        XDB xdb=(XDB) getContext().get(XDB.class);

        try
        {
            XStatement sql=new XStatement(){
                public String createStatement(Context ctx)
                {
                    return createMergeStatement(getContext());
                }

                public void set(Context ctx, XPreparedStatement ps) throws SQLException
                {
                    String requestId =getPricePlan().getId() +"_" +getNewVersion().getVersion();
                    ps.setString(requestId);
                    ps.setLong(System.currentTimeMillis());
                    ps.setInt(getPricePlan().getSpid());
                    ps.setLong(getNewVersion().getId());
                    ps.setString(requestId);
                }
            };

            xdb.execute(getContext(),sql);
        }
        catch (final HomeException exception)
        {
           throw new HomeException(
               "Failed to queue associated subscribers for price plan version update.",
               exception);
        }
    }

    /**
     * Creates the requests in the database to have the associated subscribers
     * updated.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context.
     */
    public void createRequests()
    	throws HomeException 
    {
        
    	int database=MultiDbSupportHelper.get(getContext()).getDbsType(getContext());
    	if (database==MultiDbSupport.MYSQL)
    	{
			createRequests4Mysql(); 
		} 
    	else if(database==MultiDbSupport.ORACLE || 
    	        database==MultiDbSupport.SQLSERVER) 
    	{
    	    // Oracle and SQL Server support merge statement 
			createMergeRequest();
		}     
    	else
    	{
    		if(LogSupport.isDebugEnabled(getContext()))
    		{
    			new DebugLogMsg(this,"Unknown database type: "+database,null).log(getContext());
    		}
    	}
    }

    
    /**
     * Creates the requests in the database to have the associated subscribers
     * updated.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context.
     */
    public void createRequests4Mysql()
        throws HomeException
    {
    	String stmt = createSQLRequestStatement4Mysql();
    	XDB xdb=(XDB) getContext().get(XDB.class);
    	xdb.execute(getContext(),stmt);
    }

    
    /**
     * Creates the SQL statment used to create the requests to update
     * subscribers on the current plan.  This SQL statement queries the
     * Subscriber profile table to final all the subscribers on the current plan
     * and adds entries for them to the PricePlanVersionUpdateRequest table.
     *
     * @return The SQL statment used to create the requests to update
     * subscribers on the current plan.
     */
    protected String createMergeStatement(Context ctx)
    {
        // The merge is expected to be atomic, so the ready flag is initialized
        // to 'y'.  For a more readable version of the statement, see the unit
        // test in testCreateSQLRequestStatement().
        final StringBuilder buffer = new StringBuilder();
        buffer.append("merge into ");
        buffer.append(MultiDbSupportHelper.get(ctx).getTableName(ctx,PricePlanVersionUpdateRequestHome.class,PricePlanVersionUpdateRequestXInfo.DEFAULT_TABLE_NAME));
        buffer.append(" D using (select ? " + getConcatenateSeparater() + " '_' " + getConcatenateSeparater() + " ");
        buffer.append(AbstractSubscriber.ID_PROPERTY);
        buffer.append(" as REQUESTID, ");
        buffer.append(AbstractSubscriber.ID_PROPERTY);
        buffer.append(", ");
        buffer.append(AbstractSubscriber.PRICEPLAN_PROPERTY);
        buffer.append(", 'y' as ");
        buffer.append(PricePlanVersionUpdateRequest.READY_PROPERTY);
        buffer.append(", ? as ");
        buffer.append(PricePlanVersionUpdateRequest.CREATIONDATE_PROPERTY);
        buffer.append(" from ");
        buffer.append(MultiDbSupportHelper.get(ctx).getTableName(ctx,SubscriberHome.class,SubscriberXInfo.DEFAULT_TABLE_NAME));
        buffer.append(" where ");
        buffer.append(AbstractSubscriber.SPID_PROPERTY);
        buffer.append(" = ? and ");
        buffer.append(AbstractSubscriber.PRICEPLAN_PROPERTY);
        buffer.append(" = ?) S on (S.");
        buffer.append(AbstractSubscriber.ID_PROPERTY);
        buffer.append(" = D.");
        buffer.append(PricePlanVersionUpdateRequest.SUBSCRIBERIDENTIFIER_PROPERTY);
        buffer.append(") when matched then update set D.");
        buffer.append(PricePlanVersionUpdateRequest.CREATIONDATE_PROPERTY);
        buffer.append(" = S.");
        buffer.append(PricePlanVersionUpdateRequest.CREATIONDATE_PROPERTY);
        buffer.append(" when not matched then insert values (");
        buffer.append(" ? " + getConcatenateSeparater() + " '_' " + getConcatenateSeparater() + " " );
        buffer.append(AbstractSubscriber.ID_PROPERTY);
        buffer.append(", ");
        buffer.append(AbstractSubscriber.ID_PROPERTY);
        buffer.append(", S.");
        buffer.append(AbstractSubscriber.PRICEPLAN_PROPERTY);
        buffer.append(", S.");
        buffer.append(PricePlanVersionUpdateRequest.READY_PROPERTY);
        buffer.append(", S.");
        buffer.append(PricePlanVersionUpdateRequest.CREATIONDATE_PROPERTY);
        buffer.append(")");
        buffer.append(getClosingQuerySyntax());
        

        return buffer.toString();
    }
    

    private String getConcatenateSeparater()
    {
        String separater = "||";
        int database = MultiDbSupportHelper.get(getContext()).getDbsType(getContext());
        if (database == MultiDbSupport.SQLSERVER)
        {
            separater = "+";
        }
        return separater;
    }

    public String getClosingQuerySyntax()
    {
        String separater = "";
        int database = MultiDbSupportHelper.get(getContext()).getDbsType(getContext());
        if (database == MultiDbSupport.SQLSERVER)
        {
            separater = ";";
        }
        return separater;
    }
    
    
    /**
     * Creates the SQL statment used to create the requests to update
     * subscribers on the current plan.  This SQL statement queries the
     * Subscriber profile table to final all the subscribers on the current plan
     * and adds entries for them to the PricePlanVersionUpdateRequest table.
     *
     * @return The SQL statment used to create the requests to update
     * subscribers on the current plan.
     */
    protected String createSQLRequestStatement4Mysql()
    {
        // The merge is expected to be atomic, so the ready flag is initialized
        // to 'y'.  For a more readable version of the statement, see the unit
        // test in testCreateSQLRequestStatement().
        String curTime = String.valueOf(System.currentTimeMillis());
        String requestId = curTime;
        final StringBuilder buffer = new StringBuilder();
        buffer.append("replace ");
        buffer.append("PricePlanVersionUpdateRequest");//PricePlanVersionUpdateRequestJDBCHome.DEFAULT_TABLE_NAME);
        buffer.append(" (select '");
        buffer.append(requestId);
        buffer.append("', ");
        buffer.append(AbstractSubscriber.ID_PROPERTY);
        buffer.append(", ");
        buffer.append(AbstractSubscriber.PRICEPLAN_PROPERTY);
        buffer.append(", 'y' ");
        buffer.append(", ").append(curTime);
        buffer.append(" from ");
        buffer.append("Subscriber");//SubscriberJDBCHome.DEFAULT_TABLE_NAME);
        buffer.append(" where ");
        buffer.append(AbstractSubscriber.SPID_PROPERTY);
        buffer.append(" = '").append(getPricePlan().getSpid()).append("'  and ");
        buffer.append(AbstractSubscriber.PRICEPLAN_PROPERTY);
        buffer.append(" = '").append(getNewVersion().getId()).append("' ) ");
 
        return buffer.toString();
    }    

    /**
     * Creates the SQL statment used to create the requests to update
     * subscribers on the current plan.  This SQL statement queries the
     * Subscriber profile table to final all the subscribers on the current plan
     * and adds entries for them to the PricePlanVersionUpdateRequest table.
     *
     * @return The SQL statment used to create the requests to update
     * subscribers on the current plan.
     */
    public String createSQLRequestStatement()
    {
    	int database=MultiDbSupportHelper.get(getContext()).getDbsType(getContext());
    	if (database==MultiDbSupport.MYSQL)
    	{
			return createSQLRequestStatement4Mysql(); 
		} 
    	else if(database==MultiDbSupport.ORACLE || database == MultiDbSupport.SQLSERVER) 
    	{
    		return createMergeStatement(getContext());
		}

    	if(LogSupport.isDebugEnabled(getContext()))
		{
			new DebugLogMsg(this,"Unknown database type: "+database,null).log(getContext());
		}
		
		return null;
    }
    
    /**
     * Gets the new PricePlanVersion.
     *
     * @return The new PricePlanVersion.
     */
    protected PricePlanVersion getNewVersion()
    {
        return newVersion_;
    }


    /**
     * Gets the PricePlan.
     *
     * @return The PricePlan.
     */
    protected PricePlan getPricePlan()
    {
        return plan_;
    }


    /**
     * Used by the constructors to ensure that the initialization parameters are valid.
     *
     * @param context The operating context.
     * @param plan The plan to which the versions belong.
     * @param newVersion The new PricePlanVersion.
     *
     * @exception IllegalArgumentException Thrown when any of the parameters is
     * null, when the two versions refer to different PricePlans, and when the
     * new version is not strictly newer than the old version.
     */
    private void validate(
        final Context context,
        final PricePlan plan,
        final PricePlanVersion newVersion)
    {
        if (context == null)
        {
            throw new IllegalArgumentException(
                "The context parameter is null.");
        }

        if (plan == null)
        {
            throw new IllegalArgumentException(
                "The plan parameter is null.");
        }

        if (newVersion == null)
        {
            throw new IllegalArgumentException(
                "The new price-plan version parameter is null.");
        }
    }


    /**
     * Updates the PricePlan so that it shows that it's current version is the
     * newly activated version, and assigns the activation date to the version.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context.
     */
    public void updatePricePlan()
        throws HomeException
    {
        try
        {
            Common.OM_PRICE_PLAN_VERSION_ACTIVATION.attempt(getContext());

            // First, update the PricePlan.
            final PricePlan plan;
            {
                final Home home = (Home)getContext().get(PricePlanHome.class);

                plan = getPricePlan();
                plan.setCurrentVersion(getNewVersion().getVersion());
                
                // check and change PricePlan state from Pending_Activation to Active.
                if(plan.getState() == PricePlanStateEnum.PENDING_ACTIAVTION)
                {
                    plan.setState(PricePlanStateEnum.ACTIVE);
                }

                home.store(getContext(),plan);
            }

            // Then, update the PricePlanVersion.
            final PricePlanVersion version;
            {
                final Home home = (Home)getContext().get(PricePlanVersionHome.class);

                version = (PricePlanVersion)home.find(getContext(),getNewVersion());

                final Date now = new Date();

                version.setActivation(now);
                getNewVersion().setActivation(now);

                home.store(getContext(),version);
            }

            new PricePlanVersionActivationEventRecord(plan, version, 0).generate(getContext());
            Common.OM_PRICE_PLAN_VERSION_ACTIVATION.success(getContext());
            
            // added handling for PricePlan switch if Switch PricePlan flag is enabled in PricePlan
            // added state change if versioning is successful
            // if SwitchSubsToNewPP is true, then switch all subscriber associated with grandfather-ppid with new PP
            if(getPricePlan().getGrandfatherPPId() > 0)
            {
                grandfatheredOldPricePlan(getPricePlan().getGrandfatherPPId());
                if(getPricePlan().getSwitchSubsToNewPP())
                {
                    processPricePlanSwitchIfPPSwitchOptionTrue(); 
                }
            }
        }
        catch (final HomeException exception)
        {
            Common.OM_PRICE_PLAN_VERSION_ACTIVATION.failure(getContext());
            throw exception;
        }
    }
    
    
    /**
     * Processes Price Plan Switch, if versioning PricePlan is having other price plan as grandfathered and switch PP option is true.
     */
    private void processPricePlanSwitchIfPPSwitchOptionTrue()
    {
        final Collection<Subscriber> subscribers;
        final Home subsHome = (Home) getContext().get(SubscriberHome.class); 
        
        try
        {
            subscribers = getSubscribers(getPricePlan().getGrandfatherPPId());
            
            if (subscribers == null || subscribers.isEmpty() ) 
            {
                if (LogSupport.isDebugEnabled(getContext())) {
                    new DebugLogMsg(this,
                            "No subscriber associated with Grandfathered price plan --"
                                    + getPricePlan().getGrandfatherPPId(), null).log(getContext());
                }
            }
            
            for (Subscriber subscriber : subscribers)
            {
                if (subscriber == null)
                {
                    InternalLogSupport.major("UpdatedPricePlanHandler.processPricePlanSwitchForGrandfatheredUsers()", "subscriber is null ", null);
                    continue;
                }
                if (subscriber.getStartDate() == null)
                {
                    InternalLogSupport.major("UpdatedPricePlanHandler.processPricePlanSwitchForGrandfatheredUsers()", "subscriber startDate/Activation Date is null ", null);
                    continue;
                }
                
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, "Subscribers -- [" + subscriber + "] is eligible for PricePlan Switch where new assigned PricePlan will be [" + getPricePlan() + "]");
                    new DebugLogMsg(this, "switching subscriber from current price plan "
                            + subscriber.getPricePlan(), null).log(getContext());
                }
                
                subscriber.setPricePlan(getPricePlan().getId());
                
                try
                {
                    subsHome.store(subscriber);
                }
                catch ( Exception e)
                {
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        new DebugLogMsg(this, "Fail to change price plan of subscriber : "
                                + subscriber.getMSISDN(), e).log(getContext());
                    }else
                    {
                        new DebugLogMsg(this, "Fail to change price plan of subscriber : "
                                + subscriber.getMSISDN() + " Issue is [" + e.getMessage()+"]", null).log(getContext());
                    }
                }
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, " to new price plan " + subscriber.getPricePlan(), null)
                            .log(getContext());
                }
            }
            
            // disable SwitchPPOPtion in new PricePlan
            disableSwitchSubscriberToNewPricePlan();
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failed to look up price plans.", exception).log(getContext());
            return;
        }
        catch (final Exception exception)
        {
            new MajorLogMsg(this, "Exceptions--", exception).log(getContext());
        }
    }
    
    
    private void disableSwitchSubscriberToNewPricePlan()
    {
        try
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, " Disabling PricePlan's SwitchSubsToNewPP flag", null).log(getContext());
            }
            final Home home = (Home)getContext().get(PricePlanHome.class);
            getPricePlan().setSwitchSubsToNewPP(false);
            home.store(getContext(),getPricePlan());
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(getContext())) 
            {
                new DebugLogMsg(this, "HomeException while disabling SwitchPPOption -- ["+ getPricePlan()+"]", e).log(getContext());
            }
            return;
        }
    }
    
    private void grandfatheredOldPricePlan(long oldPPId)
    {
        try
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, " Going to Change state from Active to Grandfather for PricePlan [" + oldPPId + "]", null).log(getContext());
            }
            final Home home = (Home)getContext().get(PricePlanHome.class);
            com.redknee.app.crm.bean.PricePlan plan = (com.redknee.app.crm.bean.PricePlan)home.find(getContext(), new And().add(new EQ(PricePlanXInfo.ID,oldPPId)));
            
            if(plan != null)
            {
                plan.setState(PricePlanStateEnum.GRANDFATHERED);
                home.store(getContext(),plan);
            }else
            {
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    new DebugLogMsg(this, " PricePlan didn't found with ID : [" + oldPPId + "]", null).log(getContext());
                }
            }
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(getContext())) 
            {
                new DebugLogMsg(this, "HomeException while Changing state from Active to Grandfather for PricePlan -- ["+ oldPPId+"]", e).log(getContext());
            }
            return;
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    private Collection<Subscriber> getSubscribers(final long pricePlan) throws HomeException
    {
        Collection<Subscriber> subscriber = new ArrayList<Subscriber>();
        final Home subHome = (Home) getContext().get(SubscriberHome.class);
        if (subHome == null)
        {
            throw new HomeException("SubscriberHome is not found in the context");
        }
        subscriber = subHome.select(getContext(), new And().add(new EQ(SubscriberXInfo.PRICE_PLAN,String.valueOf(pricePlan)))
                                                           .add(new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.INACTIVE_INDEX))
                                                           .add(new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.PENDING_INDEX))
                                                           .add(new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.AVAILABLE_INDEX)));
        return subscriber;
    }


    /**
     * The PricePlan.
     */
    private final PricePlan plan_;

    /**
     * The new version of the price plan.
     */
    private final PricePlanVersion newVersion_;

    /**
     * Provides ContextAware support for this class.  ContextAwareSupport is
     * abstract, so we must create a derivation of it.
     */
    private final ContextAware contextSupport_ = new ContextAwareSupport() { };

} // class
