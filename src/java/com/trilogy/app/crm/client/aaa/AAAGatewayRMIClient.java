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
package com.trilogy.app.crm.client.aaa;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.trilogy.app.crm.bean.AAARmiServiceConfig;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.EVDOStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.client.RemoteServiceStatus;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.service.aaa.AAAServiceException;
import com.trilogy.service.aaa.AAAServiceInternalException;
import com.trilogy.service.aaa.ConnectionFailureException;
import com.trilogy.service.aaa.RMIAAAService;


/**
 * Provides a client that provisions subscriber profiles through the RMI interface of the
 * AAA Gateway.
 * @author gary.anderson@redknee.com
 * @author deepak.mishra@redknee.com
 */
public class AAAGatewayRMIClient extends ContextAwareSupport implements AAAClient, RemoteServiceStatus, ContextFactory
{
    private static final String SERVICE_NAME = "AAAGatewayRMIClient";
    private static final String SERVICE_DESCRIPTION = "RMI client for AAA Gateway";
    
    /**
     * Creates and initializes a new AAAGatewayRMIClient.
     * 
     * @param context
     *            operating context The operating context.
     */
    public AAAGatewayRMIClient(final Context ctx)
    {
        rMIAAAServiceClient_ = null;
        setContext(ctx);
        }


    /*
     * @param context The operating context. @param subscriber Subscriber whose profile to
     * be created. (non-Javadoc)
     * 
     * @see com.redknee.app.crm.client.aaa.AAAClient#createProfile(com.redknee.framework.xhome.context.Context,
     *      com.redknee.app.crm.bean.Subscriber)
     */
    public void createProfile(final Context context, final Subscriber subscriber) throws AAAClientException
    {
        final RMIAAAService service = lookupService(context);

        
        try
        {
            final com.redknee.service.aaa.Subscriber sub = getMappedSubscriber(context, subscriber);
            setAaaSubcriberState(context, sub, subscriber); 
            service.createSubscriber(getServiceKey(), sub);
            

        } catch (RemoteException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
            }
        catch (final ConnectionFailureException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
            }
        catch (Exception e)
        {
            throw new AAAClientException(
                    "AAA Service Exception occured.Can not create profile for Subscriber ID."
                            + subscriber.getId() + " MSISDN :" + subscriber.getMSISDN(), e);
            }
      
        }


    /**
     * @param context
     *            The operating context.
     * @param subscriber
     *            Subscriber whose profile to be deleted.
     */
    public void deleteProfile(final Context context, final Subscriber subscriber) throws AAAClientException
    {
 
        
        final RMIAAAService service = lookupService(context);
        try
        {
            final com.redknee.service.aaa.Subscriber sub = getMappedSubscriber(context, subscriber);

             service.deleteSubscriber(getServiceKey(), sub);
        }
        catch(com.redknee.service.aaa.SubscriberNotFoundException snf)
        {
			new InfoLogMsg(this, "Subscriber [" + subscriber.getId()
					+ "] not present at AAA.", null).log(context);
            }
        catch (RemoteException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
            }
        catch (final ConnectionFailureException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
            }
       
        catch (Exception e)
        {
            throw new AAAClientException("AAA Service Exception occured.Can not delete profile.", e);
            }
        
        }


    /**
     * @param context
     *            The operating context.
     * @param subscriber
     *            Subscriber whose profile is queried.
     * @return True if profile is enabled
     * @throws AAAClientException
     *             In case of any problem while performing the operation or Subscriber not
     *             found.
     */
    public boolean isProfileEnabled(final Context context, final Subscriber subscriber) throws AAAClientException
    {
 
        final RMIAAAService service = lookupService(context);
        
        try
        {
            com.redknee.service.aaa.Subscriber sub = getMappedSubscriber(context, subscriber);
           
            sub = service.findSubscriber(getServiceKey(), sub);

                
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "Returning state from AAAServer for Sub . " + subscriber.getMSISDN() + " "
                        + sub.getState(), null).log(context);
            }
           
           if ( sub == null )
        {
               throw new AAAClientException("AAA profile not found for sub ." + subscriber.getId());
            }
           
            return (sub.getState().getIndex() == com.redknee.service.aaa.StateEnum.ACTIVE_INDEX);
        }
         catch (RemoteException e)
        {
             reset();
             throw new AAAClientException("RMIService is not available.", e);
         }
         catch (final ConnectionFailureException e)
            {
             reset();
             throw new AAAClientException("RMIService is not available.", e);
            }
         catch (Exception e)
        {
            throw new AAAClientException(
                    "Profile info retrieval failure for Subscriber ID."
                            + subscriber.getId() + " MSISDN :" + subscriber.getMSISDN(), e);
        }
            }


    /**
     * @param context
     *            The operating context.
     * @param subscriber
     *            Subscriber whose profile is queried.
     * @param enabled
     *            True if to change the satate to ACTIVE .False to change the state to
     *            DEACTIVE
     * @throws AAAClientException
     *             In case of any problem while performing the operation .
     */
    public void setProfileEnabled(final Context context, final Subscriber subscriber, final boolean enabled)
            throws AAAClientException
    {
 
        final RMIAAAService service = lookupService(context);
        try
        {
            final com.redknee.service.aaa.Subscriber sub = getMappedSubscriber(context, subscriber);
            final boolean aaaEnabled = isProfileEnabled(context, subscriber); 
            
            if (enabled && !aaaEnabled)
            { // if profiled is not enabled in on the AAA then only attempt to enable
                // it.
                sub.setState(com.redknee.service.aaa.StateEnum.ACTIVE);
                 
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(this, "Going to activate Sub. " + subscriber.getMSISDN(), null).log(context);
                }
 
                service.changeStatus(getServiceKey(), sub);
             
            }
            else if (!enabled && aaaEnabled)
            {// if profiled is not disabled in on the AAA then only attempt to disable
                // it.
            if (LogSupport.isDebugEnabled(context))
            {
                    new DebugLogMsg(this, "GOING to deactivate Sub. " + subscriber.getMSISDN(), null).log(context);
            }
              
                sub.setState(com.redknee.service.aaa.StateEnum.DEACTIVE);
                service.changeStatus(getServiceKey(), sub);
 
        }
        }
        catch (RemoteException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
            }
        catch (final ConnectionFailureException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
            }
        catch (Exception e)
        {
            throw new AAAClientException("AAA Service Exception occured Can not set  profile to " + enabled
                    + " for Subscriber ID." + subscriber.getId() + " MSISDN :" + subscriber.getMSISDN(), e);
            }
        }


    /**
     * @param context
     *            The operating context.
     * @param oldSubscriber
     * 
     * @param newSubscriber
     * 
     * @throws AAAClientException
     *             In case of any problem while performing the operation .
     */
    public void updateProfile(final Context context, final Subscriber oldSubscriber, final Subscriber newSubscriber)
            throws AAAClientException
    {
                    if (LogSupport.isDebugEnabled(context))
                    {
            new DebugLogMsg(this, "oldSubscriber ["+oldSubscriber+"]"+" newSubscriber ["+newSubscriber+"]", null).log(context);
                    }
    	// In case of conversion from Prepaid to postpaid or vice versa no update is required at AAA Server
    	if(oldSubscriber.getSubscriberType()!=newSubscriber.getSubscriberType())
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                new DebugLogMsg(this, "No update is send on AAA Server for Conversion from Prepaid to PostPaid or vice versa.", null).log(context);
                    }
    		return;
                }
    	
        if (isDunningRelatedAction(context, oldSubscriber, newSubscriber))
        { // for dunning related modification
            performDunningRelatedAction(context, newSubscriber);
                    }
        else if (isUpdateRequired(context, newSubscriber, oldSubscriber))
        {// for other modification
            updateSubscriberProfile(context, newSubscriber, oldSubscriber);
                }
                    }


    /**
     * @param context
     *            The operating context.
     * @param newSubscriber
     * @param oldSubscriber
     * @return true if any field of subscriber is changed that need to be updated on AAA
     */
    private boolean isUpdateRequired(Context context, Subscriber newSubscriber, Subscriber oldSubscriber)
    {
        // TODO 2008-08-21 this should be called on account address change
//        return (!(newSubscriber.getAddress1().equals(oldSubscriber.getAddress1())
//                && newSubscriber.getAddress2().equals(oldSubscriber.getAddress2())
//                && newSubscriber.getAddress3().equals(oldSubscriber.getAddress3())
//                && newSubscriber.getPackageId().equals(oldSubscriber.getPackageId())
//                && newSubscriber.getState().getIndex() == oldSubscriber.getState().getIndex()));
        return (!(newSubscriber.getPackageId().equals(oldSubscriber.getPackageId())
                && newSubscriber.getState().getIndex() == oldSubscriber.getState().getIndex()));
    }


    /**
     * @param context
     *            The operating context.
     * @param oldSubscriber
     * @param newSubscriber
     * @return true if state transition occured due to dunning action.
     */
    private boolean isDunningRelatedAction(Context context, Subscriber oldSubscriber, Subscriber newSubscriber)
    {
        return (oldSubscriber.getState().getIndex() != newSubscriber.getState().getIndex()
                && (newSubscriber.getState().getIndex() == SubscriberStateEnum.NON_PAYMENT_WARN_INDEX
                || newSubscriber.getState().getIndex() == SubscriberStateEnum.IN_ARREARS_INDEX
                || newSubscriber.getState().getIndex() == SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX
                || newSubscriber.getState().getIndex() == SubscriberStateEnum.PROMISE_TO_PAY_INDEX
                || oldSubscriber.getState().getIndex() == SubscriberStateEnum.NON_PAYMENT_WARN_INDEX
                || oldSubscriber.getState().getIndex() == SubscriberStateEnum.IN_ARREARS_INDEX
                || oldSubscriber.getState().getIndex() == SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX
                || oldSubscriber.getState().getIndex() == SubscriberStateEnum.PROMISE_TO_PAY_INDEX));
    }


    /**
     * @param context
     *            The operating context.
     * @param oldPackage
     *            Package before update
     * @param newPackage
     *            Package after update.
     */
    public void updateProfile(final Context context, final TDMAPackage oldPackage, final TDMAPackage newPackage)
            throws AAAClientException
    {
        final RMIAAAService service = lookupService(context);

        if (isTDMALoginPwdChanged(oldPackage, newPackage))
        {
            com.redknee.service.aaa.Subscriber  origionalSub = getMappedSubscriber(context, oldPackage);
     
            try
            {
                 origionalSub = service.findSubscriber(getServiceKey(), origionalSub);
                 try{
                 service.deleteSubscriber(getServiceKey(), origionalSub);
                 }catch(com.redknee.service.aaa.SubscriberNotFoundException snf)
                 {
                     new InfoLogMsg(this, "Subscriber with UserID on AAA Server[" + origionalSub.getUserId()
                             + "] not present at AAA.", null).log(context);
            }
                 
                try
            {
                    com.redknee.service.aaa.Subscriber newSub = getMappedSubscriber( context, 
                            (com.redknee.service.aaa.Subscriber) origionalSub.clone(), newPackage);

                    
                    service.createSubscriber(getServiceKey(), newSub);

                }
                catch (CloneNotSupportedException e)
                {
                    e.printStackTrace();
                }
            }
            catch (RemoteException e)
            {
                reset();
                throw new AAAClientException("RMIService is not available.", e);
            }
            catch (final ConnectionFailureException e)
                {
                reset();
                throw new AAAClientException("RMIService is not available.", e);
                }
            catch (Exception e)
            {
                throw new AAAClientException(
                        "AAA Service Exception occured.Update profile failure to accomodate package update of PKGID:"
                                + oldPackage.getPackId(), e);
            }
 
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "The package change of Package Id:" + oldPackage.getPackId()
                        + " need not to propagated.", null).log(context);
        }
    }
    }


    /**
     * 
     * @param oldPackage
     * @param newPackage
     * @return
     */
    private boolean isTDMALoginPwdChanged(TDMAPackage oldPackage, TDMAPackage newPackage)
    {
        return (!(oldPackage.getServiceLogin1().equals(newPackage.getServiceLogin1())
                && oldPackage.getServiceLogin2().equals(newPackage.getServiceLogin2())
                && oldPackage.getServicePassword1().equals(newPackage.getServicePassword1()) 
                && oldPackage.getServicePassword2().equals(newPackage.getServicePassword2())
                && oldPackage.getCallbackID().equals(newPackage.getCallbackID())
                && oldPackage.getRadiusProfileName().equals(oldPackage.getRadiusProfileName())));
    }


    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return SERVICE_NAME;
    }


    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }


    public boolean isAlive()
    {
        return (rMIAAAServiceClient_ != null);
            }


    /**
     * @param context
     * @param subscriber
     * @return the Subscriber which corresponds to Subscriber of AAA ,performing the
     *         mapping of CRM Subscriber to AAA subscriber
     * @throws HomeException
     */
    private com.redknee.service.aaa.Subscriber getMappedSubscriber(Context context, Subscriber subscriber)
            throws HomeException
    {
        com.redknee.service.aaa.Subscriber sub = new com.redknee.service.aaa.Subscriber();
        final Account account = subscriber.getAccount(context);
        String address = account.getBillingAddress1() + "," + account.getBillingAddress2()
                + "," + account.getBillingAddress3();
        if (address.length() > com.redknee.service.aaa.Subscriber.ADDRESS_WIDTH)
            address = address.substring(0, com.redknee.service.aaa.Subscriber.ADDRESS_WIDTH - 1);
        sub.setAddress(address);
        TDMAPackage tDMAPackage = getTDMAPackage(context, subscriber);

        return getMappedSubscriber(context, sub,  tDMAPackage);
    }    
      
    /**
     * 
     * @param context
     * @param tDMAPackage
     * @return
     */
    private  com.redknee.service.aaa.Subscriber  getMappedSubscriber(
            final Context context, 
            final TDMAPackage tDMAPackage) 
    {
        com.redknee.service.aaa.Subscriber sub = new com.redknee.service.aaa.Subscriber();
        return getMappedSubscriber(context, sub,  tDMAPackage);
       
    }
    
    /**
     * 
     * @param context
     * @param sub
     * @param tDMAPackage
     * @return
     */
   private  com.redknee.service.aaa.Subscriber  getMappedSubscriber(
           final Context context, 
           final com.redknee.service.aaa.Subscriber sub, 
           final TDMAPackage tDMAPackage) 
   {
       final  AAARmiServiceConfig config = (AAARmiServiceConfig) getContext().get(AAARmiServiceConfig.class);

       
        sub.setChapPassword(tDMAPackage.getServicePassword1());
        // sub.setChapPassword(tDMAPackage.getESN());
        sub.setTerminalChapPassword(tDMAPackage.getServicePassword2());        
       //sub.setTerminalChapPassword(tDMAPackage.getServicePassword1());
        sub.setPassword(tDMAPackage.getServiceLogin1());
        //sub.setPassword(tDMAPackage.getMin());
        sub.setUserId(tDMAPackage.getServiceLogin1());
        //sub.setUserId(tDMAPackage.getMin());
        sub.setFirstName(tDMAPackage.getServiceLogin1());
        //sub.setFirstName(tDMAPackage.getMin());
        sub.setLastName(tDMAPackage.getServiceLogin1());
        //sub.setLastName(tDMAPackage.getMin());
        sub.setCallingStationId(tDMAPackage.getServiceLogin1());
        //sub.setCallingStationId(tDMAPackage.getMin());
     
        sub.setCustomer(config.getCustomerName());
        sub.setTerminalCustomer(config.getTerminalCustomerName());
        
        
        sub.setCallbackID(tDMAPackage.getCallbackID());
        sub.setRadiusProfileName(tDMAPackage.getRadiusProfileName()); 
        return sub;
    }

  /**
   * 
   * @param context
   * @param sub
   * @param subscriber
   * @return
   */  
   private  com.redknee.service.aaa.Subscriber  setAaaSubcriberState(
           final Context context, 
           final com.redknee.service.aaa.Subscriber sub, 
           final Subscriber subscriber) 
   {
       if (subscriber.getState().getIndex() != SubscriberStateEnum.ACTIVE_INDEX)
       {
           sub.setState(com.redknee.service.aaa.StateEnum.DEACTIVE);
           if (LogSupport.isDebugEnabled(context))
           {
               new DebugLogMsg(this,
                       "Subscriber profile is provisioned on AAAServer as Inactive", null)
                       .log(context);
           }
       }
       else
       {
           sub.setState(com.redknee.service.aaa.StateEnum.ACTIVE);
           if (LogSupport.isDebugEnabled(context))
           {
               new DebugLogMsg(this,
                       "In Active state Subscriber profile is provisioned on AAAServer as active", null)
                       .log(context);
           }

       }
   
       return sub; 
       
   }
   
   /**
    * 
    * @param context
    * @param subscriber
    * @return
    * @throws HomeException
    */
    private TDMAPackage getTDMAPackage(Context context, Subscriber subscriber) throws HomeException
    {
        Home tDMAPackageHome = (Home) context.get(TDMAPackageHome.class);
        
        And and = new And();
        and.add(new EQ(TDMAPackageXInfo.SPID, subscriber.getSpid()));
        and.add(new EQ(TDMAPackageXInfo.PACK_ID, subscriber.getPackageId()));
        		
       /* TDMAPackage tDMAPackage = (TDMAPackage) tDMAPackageHome.find(new EQ(TDMAPackageXInfo.PACK_ID,
                subscriber.getPackageId()));*/
        
        TDMAPackage tDMAPackage = (TDMAPackage) tDMAPackageHome.find(and);
        
        return tDMAPackage;
    }


    /**
     * 
     * @param ctx
     * @param sub
     * @param newSubscriber
     * @param oldSubscriber
     * @return
     * @throws AAAClientException
     * @throws AAAServiceInternalException
     * @throws AAAServiceException
     * @throws RemoteException
     */
    private com.redknee.service.aaa.StateEnum getSubscriberState(Context ctx, com.redknee.service.aaa.Subscriber sub,
            Subscriber newSubscriber, Subscriber oldSubscriber) throws AAAClientException, AAAServiceInternalException,
            AAAServiceException, RemoteException
    {
 
        final RMIAAAService service = lookupService(ctx);

        com.redknee.service.aaa.StateEnum state;
        SubscriberStateEnum oldState = oldSubscriber.getState();
        SubscriberStateEnum newState = newSubscriber.getState();
        if (oldState.getIndex() == SubscriberStateEnum.ACTIVE_INDEX
                && (newState.getIndex() == SubscriberStateEnum.SUSPENDED_INDEX 
                || newState.getIndex() == SubscriberStateEnum.LOCKED_INDEX))
        {
            // deactivate subscriber.
            state = com.redknee.service.aaa.StateEnum.DEACTIVE;
        }
        else if ((oldState.getIndex() == SubscriberStateEnum.SUSPENDED_INDEX
                || oldState.getIndex() == SubscriberStateEnum.LOCKED_INDEX 
                || oldState.getIndex() == SubscriberStateEnum.AVAILABLE_INDEX)
                && newState.getIndex() == SubscriberStateEnum.ACTIVE_INDEX)
        { // reactivate subscriber
            state = com.redknee.service.aaa.StateEnum.ACTIVE;
        }
        else
        { //set the state to same that is there on the AAA server
            com.redknee.service.aaa.Subscriber tempSub = new com.redknee.service.aaa.Subscriber();
            tempSub.setUserId(sub.getUserId());
            tempSub.setCustomer(sub.getCustomer());
            tempSub.setTerminalCustomer(sub.getTerminalCustomer());
            tempSub = service.findSubscriber(getServiceKey(), tempSub);
            state = tempSub.getState();
        }
        return state;
    }


    /**
     * 
     * @param context
     * @param newSubscriber
     * @param oldSubscriber
     * @throws AAAClientException
     */
    private void updateSubscriberProfile(Context context, Subscriber newSubscriber, Subscriber oldSubscriber)
            throws AAAClientException
    {
        final RMIAAAService service = lookupService(context);

        try
        {
            com.redknee.service.aaa.Subscriber sub = getMappedSubscriber(context, newSubscriber);
           // if the package swap 
           if(oldSubscriber!=null && (!newSubscriber.getPackageId().equals(oldSubscriber.getPackageId())))
           {
        	   TDMAPackage oldPackage=PackageSupportHelper.get(context).getTDMAPackage(context, oldSubscriber.getPackageId(), oldSubscriber.getSpid());
        	   TDMAPackage newPackage=PackageSupportHelper.get(context).getTDMAPackage(context, newSubscriber.getPackageId(), newSubscriber.getSpid());
        	   updateProfile(context,oldPackage,newPackage);
           }	
           else
           {
                if (oldSubscriber.getState().getIndex() != SubscriberStateEnum.INACTIVE_INDEX
                        && newSubscriber.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX)
                {
                    deleteProfile(context, newSubscriber);
                }
                else
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        new DebugLogMsg(this, "Is there only state transition for : " + newSubscriber.getMSISDN()
                                + " is(T/F) : " + onlyStateTransition(context, newSubscriber, oldSubscriber), null)
                                .log(context);
                    }
                    if (onlyStateTransition(context, newSubscriber, oldSubscriber))
                    {
                        // if transition from inactive to active then create the
                        // profile on AAAServer
                        if (oldSubscriber.getState() == SubscriberStateEnum.INACTIVE
                                && newSubscriber.getState() == SubscriberStateEnum.ACTIVE)
                        {
                            createProfile(context, newSubscriber);
                            return;
                        }
                        boolean activate = getSubscriberState(context, sub, newSubscriber, oldSubscriber).getIndex() == com.redknee.service.aaa.StateEnum.ACTIVE_INDEX;
                        if (LogSupport.isDebugEnabled(context))
                        {
                            new DebugLogMsg(this, "Activate is required for sub : " + newSubscriber.getMSISDN()
                                    + " is(T/F) : " + activate, null).log(context);
                        }
                        setProfileEnabled(context, newSubscriber, activate);
                    }
                    else
                    {
                        sub.setState(getSubscriberState(context, sub, newSubscriber, oldSubscriber));
                        service.modifySubscriber(getServiceKey(), sub);
                    }
                }
            }
        }
        catch (RemoteException e)
        {
            reset();
            throw new AAAClientException("RMIService is not available.", e);
        }
        catch (Exception e)
        {
            throw new AAAClientException("AAA Service Exception occured", e);
        }
        
    }


    /**
     * 
     * @param context
     * @param newSubscriber
     * @param oldSubscriber
     * @return
     */
    private boolean onlyStateTransition(Context context, Subscriber newSubscriber, Subscriber oldSubscriber)
    {
        // TODO the adddress change can be checked only on Account change
//        return ((newSubscriber.getAddress1().equals(oldSubscriber.getAddress1())
//                && newSubscriber.getAddress2().equals(oldSubscriber.getAddress2())
//                && newSubscriber.getAddress3().equals(oldSubscriber.getAddress3())
//                && newSubscriber.getPackageId().equals(oldSubscriber.getPackageId()))
//                && (newSubscriber.getState().getIndex() != oldSubscriber.getState().getIndex()));
        return ((newSubscriber.getPackageId().equals(oldSubscriber.getPackageId()))
                && (newSubscriber.getState().getIndex() != oldSubscriber.getState().getIndex()));
    }

    /**
     * 
     * @param context
     * @param newSubscriber
     * @throws AAAClientException
     */
    public void performDunningRelatedAction(Context context, Subscriber newSubscriber) throws AAAClientException
    {
        CRMSpid spid = (CRMSpid) ReportUtilities.findByPrimaryKey(context, CRMSpidHome.class,
                Integer.valueOf(newSubscriber.getSpid()));
        switch (newSubscriber.getState().getIndex())
        {
        case SubscriberStateEnum.NON_PAYMENT_WARN_INDEX:
            setProfileEnabled(context, newSubscriber, spid.getEVDOwarningAction().equals(EVDOStateEnum.ACTIVE));
            break;
        case SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
            setProfileEnabled(context, newSubscriber, spid.getEVDOdunningAction().equals(EVDOStateEnum.ACTIVE));
            break;
        case SubscriberStateEnum.IN_ARREARS_INDEX:
            setProfileEnabled(context, newSubscriber, spid.getEVDOinArrearsAction().equals(EVDOStateEnum.ACTIVE));
            break;
        // activate the subscriber as PTP is Pseudo Active State
        case SubscriberStateEnum.PROMISE_TO_PAY_INDEX:
        case SubscriberStateEnum.ACTIVE_INDEX:
            setProfileEnabled(context, newSubscriber, true);
        default:// Other case not require any handling
        }
    }


    public synchronized void reset()
    {
        rMIAAAServiceClient_ = null;
    }


    /**
     * Forces a reset on the context factory in the given context, resulting in an attempt
     * to recreate the remote object.
     * 
     * @param context
     *            The operating context.
     */
    public void reset(Context context)
    {
        context = context.createSubContext(FORCE_RESET);
        context.put(FORCE_RESET, true);
        context.get(AAAGatewayRMIClient.class);
    }


    /**
     * 
     */
    public synchronized Object create(final Context ctx)
    {
        final boolean forceReset = ctx.getBoolean(FORCE_RESET, false);
        if (rMIAAAServiceClient_ == null || forceReset  )
        {
            try {
                rMIAAAServiceClient_ = lookupService(ctx);
               
            } 
            catch (AAAClientException e )
            { 
              reset(); 
              new MajorLogMsg(this, "fail to initialize AAA service. ", e).log(ctx); 
            }
        }
        // return rMIAAAServiceClient_;
        getContext().put(RMIAAAService.class, rMIAAAServiceClient_);
        return this;
    }


    /**
     * 
     * @param context
     * @return
     * @throws AAAClientException
     */
    private RMIAAAService lookupService(final Context context)
    throws AAAClientException
    {
        if (rMIAAAServiceClient_ != null && !context.getBoolean(FORCE_RESET, false))
        {    
            return rMIAAAServiceClient_;
        }
        else
        {
            final  AAARmiServiceConfig config = (AAARmiServiceConfig) context.get(AAARmiServiceConfig.class);

            try
            {
                Registry reg = LocateRegistry.getRegistry(config.getHostname(), config.getPort());
                rMIAAAServiceClient_ = (RMIAAAService) reg.lookup(config.getServiceName());
                getContext().put(RMIAAAService.class, rMIAAAServiceClient_);
                if ( rMIAAAServiceClient_ != null )
                {
                    return rMIAAAServiceClient_;
                }
                else 
                {
                    throw  new AAAClientException("AAA Connection is down");                    
                }
            }
            catch (RemoteException re)
            {
                new EntryLogMsg(13095, this, "", "", new String[]
                    {}, re).log(context);
                reset();
                throw  new AAAClientException("AAA Connection is down");
            }
            catch (NotBoundException nbe)
            {
                new EntryLogMsg(13095, this, "", "", new String[]
                    {}, nbe).log(context);
                reset();
                throw  new AAAClientException("AAA Connection is down");
            }
            /*
             * catch (CloneNotSupportedException e) { new EntryLogMsg(13095, this, "", "",
             * new String[] {}, e).log(context); reset(); return null; }
             */
        }
    }

    /**
     * 
     * @return
     */
    private String getServiceKey()
    {
        final  AAARmiServiceConfig config = (AAARmiServiceConfig) getContext().get(AAARmiServiceConfig.class);
        return config.getServiceKey(); 
    }
    
    
    private RMIAAAService rMIAAAServiceClient_;

    /**
     * A boolean can be set in the context with this key to force a reset during a call to
     * create(). If true then a reset is forced and an attempt to recreate the remote
     * object is made; normal specified behaviour is followed if false or unspecified.
     */
    public static final String FORCE_RESET = AAAGatewayRMIClient.class.getName() + ".FORCE_RESET";

    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        final  AAARmiServiceConfig config = (AAARmiServiceConfig) getContext().get(AAARmiServiceConfig.class);
        return SystemStatusSupportHelper.get().generateConnectionStatus(config.getHostname(), config.getPort(), isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
}
