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
package com.trilogy.app.crm.bulkloader.generic;

import com.trilogy.app.crm.CoreCrmConstants;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.AccountNote;
import com.trilogy.app.crm.bean.GSMPackageXInfo;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberNote;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * GenericBeanBulkload consumer agent.  For multi-threading, separate the implementation that processes
 * the entire Bulkload request from the implementation that processes each individual CSV command.
 * 
 * 
 * @author angie.li@redknee.com
 *
 */
public class GenericBeanBulkloadAgent implements ContextAgent 
{
    GenericBeanBulkloadAgent(GenericBeanBulkloadManager manager)
    {
        manager_ = manager;
    }
    
    public void execute(Context ctx) throws AgentException 
    {
        String csvCommand = (String) ctx.get(BulkloadConstants.GENERIC_BEAN_BULKLOAD_CSV_COMMAND);

        if (csvCommand != null)
        {
            int sessionId = ctx.getInt(BulkloadConstants.GENERIC_BEAN_BULKLOAD_SESSION_ID, 0);
            if (csvCommand.trim().length() == 0)
            {
                logBulkloadMessage(ctx, sessionId, "Ignoring empty input.");
                return;
            }

            logOMLogMsg(ctx, BulkloadConstants.OM_BULKLOAD_ATTEMPT);
            PMLogMsg pm_bulkload = logPMLogMsg(ctx, BulkloadConstants.PM_BULKLOAD_RECORD);

            try
            {
                logBulkloadMessage(ctx, sessionId, "Start Bulkloading process. INPUT: \"" + csvCommand + "\"");

                Object bean = parseInput(ctx, csvCommand, sessionId);

                executeHomeOperationOnBean(ctx, bean, csvCommand, sessionId);

                logOMLogMsg(ctx, BulkloadConstants.OM_BULKLOAD_SUCCESS);
            }
            catch (BulkloadException e)
            {
                logOMLogMsg(ctx, BulkloadConstants.OM_BULKLOAD_FAILURE);
                logToErrorRecords(ctx, new String(csvCommand));
                logBulkloadMessage(ctx, e.getMessage(), e);
                logBulkloadMessage(ctx, sessionId, "UNsuccessful UPDATE: \"" + csvCommand + "\"");
            }
            finally
            {
                pm_bulkload.log(ctx);
                updateProgress(ctx);
                csvCommand = null;
                logBulkloadMessage(ctx, sessionId, "Completed Bulkloading process. ");
            }
        }
        else
        {
            new MinorLogMsg(this, "This agent was executed with csvCommand_ set to NULL. This is incorrect. ", null);
        }
    }

    /**
     * Updates the progress counter with one more additional completion.
     * @param ctx
     */
    private void updateProgress(Context ctx) 
    {
        manager_.updateProgress(ctx);
    }

    /**
     * Handle the Parsing the Input of the Bulkload command.
     * @return Null if the bean in the search criteria does not exists in the system.  Otherwise, returns the bean from the system, already modified 
     * by the changes requested in the Bulkload Commmand. 
     */
    private Object parseInput(Context ctx, final String csvCommand, final int sessionId) 
        throws BulkloadException
    {
        logOMLogMsg(ctx, BulkloadConstants.OM_PARSE_ATTEMPT);
        PMLogMsg pm = logPMLogMsg(ctx, BulkloadConstants.PM_PARSE_RECORD);
        
        Object bean = null;

        try
        {
            StringSeperator seperator = new StringSeperator(csvCommand, getBulkloader().getDelimiter());

            //Apply the CSV command to the identified bean.
            bean = getBulkloader().parse(seperator);
            
            logOMLogMsg(ctx, BulkloadConstants.OM_PARSE_SUCCESS);
        }
        catch(Throwable t)
        {
            logOMLogMsg(ctx, BulkloadConstants.OM_PARSE_FAILURE);
            
            //Log the CSV in the error log and log the error in the progress log and abort bulkloading this command. 
            throw new BulkloadException(sessionId, "Failure occurred while parsing CSV Command: " + csvCommand, t);
        }
        finally
        {
            pm.log(ctx);
        }
        return bean;
    }

    /**
     * Handle the bean update in the system (Update/Create/Delete).
     * @param ctx
     * @param bean
     * @param csvCommand
     * @param sessionId
     * @throws BulkloadException
     */
    private void executeHomeOperationOnBean(Context ctx, Object bean, final String csvCommand, final int sessionId) 
        throws BulkloadException
    {
        if (bean != null)
        {
            logOMLogMsg(ctx, BulkloadConstants.OM_HOME_OPERATION_ATTEMPT);
            PMLogMsg pm = logPMLogMsg(ctx, BulkloadConstants.PM_HOME_OPERATION_RECORD);

            //Get Home of the Bean
            Home home = getBeanHome(ctx, getBulkloader().getBeanClass());
            BulkloaderActionEnum action = getBulkloader().getBulkloaderConfig().getAction();
            
            //Overwrite DefaultExceptionListener so the errors from here are not spilled onto the Bulkloading Execution Screen.
            ctx.put(ExceptionListener.class, new DefaultExceptionListener());
            
            try
            {
                if (home != null)
                {
                    if (action.equals(BulkloaderActionEnum.CREATE))
                    {
                    	bulkCreate(ctx, bean, home); 
                    }
                    else if (action.equals(BulkloaderActionEnum.UPDATE))
                    {
                    	 if(getBulkloader().getBeanClass().equals(SubscriberServices.class))
                         {                     
                         	int operationIndex = 4, subIdIndex =0;
                         	
                         	StringSeperator seperator = new StringSeperator(csvCommand, getBulkloader().getDelimiter());
                             
                         	ArrayList<String> allValues = getBulkloader().getInputParameters(seperator);
                         	
                         	String operation = allValues.get(operationIndex);    
                         	
                         	String subID = allValues.get(subIdIndex);                         	                         
                         	
                         	Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, subID);
                         	
                         	if (subscriber == null)
                         	{
                         		int mdnIndex = 1;
                         		String mdn = allValues.get(mdnIndex);
                         		subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, mdn);
            				}
                         	
                         	Home subscriberHome = (Home) ctx.get(SubscriberHome.class);
                         	long serviceId = ((SubscriberServices)bean).getService().getIdentifier();
                     		Set<ServiceFee2ID> servicesSet = subscriber.getServices();
                     		if(servicesSet == null)
                     		{
                     			servicesSet = new HashSet<ServiceFee2ID>();
                     		}
                         	if(operation.equalsIgnoreCase("ADD"))
                         	{
                         		servicesSet.add(new ServiceFee2ID(serviceId,SubscriberServicesUtil.DEFAULT_PATH));
                         		subscriber.setServices(servicesSet);
                         	    subscriber.addServiceToIntentToProvisionService(ctx, (SubscriberServices)bean);
                         	}
                         	else
                         	{
                         		servicesSet.remove(new ServiceFee2ID(serviceId,SubscriberServicesUtil.DEFAULT_PATH));
                         		subscriber.setServices(servicesSet);
                         		subscriber.removeServiceFromIntentToProvisionServices(ctx, ((SubscriberServices)bean).getServiceId());
                         	}
                         	subscriberHome.store(ctx, subscriber);
                         }
                         else
                         {
                         	 home.store(ctx, bean);
                         }
                    }
                    else if (action.equals(BulkloaderActionEnum.REMOVE))
                    {
                        home.remove(ctx, bean);
                    }                            
                }
                else
                {
                    String msg = "No Home for Bean exists in the context! BeanClass=" + getBulkloader().getBeanClass().getName();
                    new MajorLogMsg(this, msg, null).log(ctx);
                    throw new BulkloadException(sessionId, msg);
                }
                
                logOMLogMsg(ctx, BulkloadConstants.OM_HOME_OPERATION_SUCCESS);
            }
            catch (Throwable t)
            {
                logOMLogMsg(ctx, BulkloadConstants.OM_HOME_OPERATION_FAILURE);
    
                //If any reason aborts the Home action then log the CSV in the error log and log the error in the progress log.
                throw new BulkloadException(sessionId, "Failure occurred while processing Home action: " + action, t);
            }
            finally
            {
                pm.log(ctx);
            }
            
            logBulkloadMessage(ctx, sessionId, "SUCCESSFUL UPDATE: \"" + csvCommand + "\"");
        }
        else
        {
            //Log failure to find the bean to modify.
            throw new BulkloadException(sessionId, "No such bean exists in CRM. Cannot proceed with modifying the bean.");
        }
    }
    
    

    private void bulkCreate(Context ctx, Object bean, Home home)
			throws HomeException, HomeInternalException 
	{
		
		//TT#12081652039. Subscriber Notes doesn't update after bulk loading SubscriberAuxiliaryServices. 
		//Below code has been added here because this(SubscriberAuxiliaryService) specific bulk load hits the SubscriberAuxiliaryServiceHomePipelineFactory,
		//but Updation of Subscriber Notes is done in SubscriberHomePipelineFactory which we can not move into SubscriberAuxiliaryServiceHomePipelineFactory.
		
		Subscriber oldSub = null;
		final StringBuilder noteBuff = new StringBuilder();
		
		Set<Long> oldAuxSrvIDs = new HashSet<Long>();
		Set<Long> newAuxSrvIDs = new HashSet<Long>();
		SubscriberAuxiliaryService auxAux = null;
		
		if (getBulkloader().getBeanClass().equals(SubscriberAuxiliaryService.class)) 
		{
	    	auxAux = (SubscriberAuxiliaryService) bean;
			oldSub = SubscriberSupport.getSubscriber(ctx,auxAux.getSubscriberIdentifier());
			oldAuxSrvIDs = oldSub.getAuxiliaryServiceIds(ctx);
		}
		
	    if (getBulkloader().getBeanClass().equals(GSMPackage.class)) 
	    {
	        GSMPackage gsmPackage = (GSMPackage) bean;
            if (null == gsmPackage.getIMSI1() || gsmPackage.getIMSI1().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.IMSI1, "IMSI1 NOT PROVIDED");
            }
            if (null == gsmPackage.getIMSI2() || gsmPackage.getIMSI2().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.IMSI2, "IMSI2 NOT PROVIDED");
            }
            if (null == gsmPackage.getPIN1() || gsmPackage.getPIN1().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.PIN1, "PIN1 NOT PROVIDED");
            }
            if (null == gsmPackage.getPIN2() || gsmPackage.getPIN2().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.PIN2, "PIN2 NOT PROVIDED");
            }
            if (null == gsmPackage.getPUK1() || gsmPackage.getPUK1().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.PUK1, "PUK1 NOT PROVIDED");
            }
            if (null == gsmPackage.getPUK2() || gsmPackage.getPUK2().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.PUK2, "PUK2 NOT PROVIDED");
            }
            if (null == gsmPackage.getADM1() || gsmPackage.getADM1().isEmpty() )
            {
                throw new MissingRequireValueException(GSMPackageXInfo.ADM1, "ADM1 NOT PROVIDED");
            }
	    }
		
		

		home.create(ctx, bean);
			
		if (getBulkloader().getBeanClass().equals(SubscriberAuxiliaryService.class)) 
		{
			newAuxSrvIDs.addAll(oldAuxSrvIDs);
			newAuxSrvIDs.add(auxAux.getAuxiliaryServiceIdentifier());

			if (newAuxSrvIDs != null && !newAuxSrvIDs.equals(oldAuxSrvIDs)) 
			{
				noteBuff.append("Subscriber updating succeeded\n");
				SubscriberNoteSupport.appendNoteContent("Auxiliary Services", new HashSet(oldAuxSrvIDs).toString(),
						new HashSet(newAuxSrvIDs).toString(), noteBuff);
			}
			
			SubscriberNoteSupport.createSubscriberNote(ctx, this, SubscriberNoteSupport.getCsrAgent(ctx, oldSub), oldSub.getId(), SystemNoteTypeEnum.EVENTS,
					SystemNoteSubTypeEnum.SUBUPDATE, noteBuff);
		}
		
	}
    
    
    /**
     * Resolve the home
     * @param ctx
     * @param beanClass
     * @return
     */
    private Home getBeanHome(Context ctx, Class beanClass) 
    {
        if(beanClass.equals(AccountNote.class))
        {
            return (Home) ctx.get(CoreCrmConstants.ACCOUNT_NOTE_HOME);
        }
        else if(beanClass.equals(SubscriberNote.class))
        {
            return (Home) ctx.get(CoreCrmConstants.SUBSCRIBER_NOTE_HOME);
        }
        
        Class homeClass = XBeans.getClass(ctx, beanClass, Home.class);
        Home origHome = (Home) ctx.get(homeClass);
        
        Home wrapperHomes = null;
        try
        {
            wrapperHomes = (Home)XBeans.getInstanceOf(ctx, beanClass, GenericBeanBulkloadHome.class);
        }
        catch(Throwable th)
        {
            wrapperHomes = null;
        }
        
      
        if(wrapperHomes==null)
            return origHome;
        
        if(LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, this, "Found New GenericBeanBulkloadHome facet: "+ wrapperHomes);
        
        Home lastHome = wrapperHomes;
        while(lastHome instanceof HomeProxy)
        {
            Home tmp = ((HomeProxy)lastHome).getDelegate();
            if(tmp==null || tmp instanceof NullHome)
            {
                ((HomeProxy)lastHome).setDelegate(origHome);
                break;
            }
            
            lastHome = tmp;
        }
        
        if(LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, this, "New wrapped Home: "+ wrapperHomes);
        
        
        return wrapperHomes;
    }
    
    private void logOMLogMsg(Context ctx, String omMsg) 
    {
        manager_.logOMLogMsg(ctx, omMsg);
    }

    private PMLogMsg logPMLogMsg(Context ctx, final String pm) 
    {
        return manager_.logPMLogMsg(ctx, pm);
    }

    private void logBulkloadMessage(Context ctx, int sessionId, final String msg) 
    {
        String sessionAndMsg = "[Session ID=" + sessionId + "] " + msg;
        manager_.logBulkloadMessage(ctx, sessionAndMsg, null);
    }
    
    private synchronized void logBulkloadMessage(Context ctx, final String msg, final BulkloadException e) 
    {
        manager_.logBulkloadMessage(ctx, msg, e);
    }
    
    private void logToErrorRecords(Context ctx, final String errorRecord)
    {
        manager_.logToErrorRecords(ctx, errorRecord);
    }
    
    private CSVParser getBulkloader()
    {
        return manager_.getBulkloader();
    }
    
    private GenericBeanBulkloadManager manager_;
}
