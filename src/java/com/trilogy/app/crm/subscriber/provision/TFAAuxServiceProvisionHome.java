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
package com.trilogy.app.crm.subscriber.provision;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.TFAAuxiliaryServiceExtensionTypeEnum;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.client.TFAAuxiliarServiceClientException;
import com.trilogy.app.crm.extension.auxiliaryservice.core.TFAAuxSvcExtension;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.LogSupport;

/*
 * TFA Home class for creating and removing TFA Auxiliary Service. 
 * 
 * @author bhagyashree.dhavalshankh@redknee.com
 * 
 */
public class TFAAuxServiceProvisionHome extends HomeProxy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TFAAuxServiceProvisionHome(Context ctx, Home home)
	{
		super(home); 
	}
	
	@Override
	public Object create(Context ctx,Object obj) throws HomeException
	{
		HTMLExceptionListener exceptions = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
		MessageMgr manager = new MessageMgr(ctx, this); 

		        if (exceptions == null)
		        {
		            exceptions = new HTMLExceptionListener(manager);
		            ctx.put(HTMLExceptionListener.class, exceptions);
		        }
		 ctx.put(ExceptionListener.class, exceptions);


		final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;

        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);
        AuxiliaryServiceTypeEnum auxServiceType = auxService.getType();
        
        if(!auxServiceType.equals(AuxiliaryServiceTypeEnum.TFA))
        {
        	return super.create(ctx,obj);
        }
            
        TFAAuxSvcExtension tfaExtension = TFAAuxSvcExtension.getTfaServiceExtention(ctx, auxService.getIdentifier());
        
        short tfaExtIndex = tfaExtension.getTfaServiceExtention().getIndex() ;
        
        if(tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_CONTR_BLACKLIST_INDEX || tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_CONTR_WHITELIST_INDEX ||
        		tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_RECP_BLACKLIST_INDEX || tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_RECP_WHITELIST_INDEX ){


        	final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
        	       	
        	try 
        	{
        		 List<AuxiliaryService> lst = new ArrayList<AuxiliaryService>();
        		 lst.add(auxService);
        		 
        		 TFAAuxServiceSupport.createTFAService(ctx,subscriber,lst);
        		 exceptions = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
        		 if(exceptions.hasErrors()){
        		 			return obj;
        		 }
        	}
        	catch (TFAAuxiliarServiceClientException e) 
        	{
        		StringBuilder strMsg = new StringBuilder();
        		strMsg.append("Unable to Provision Service(s) on TFA. "+e.getMessage());

        		if(LogSupport.isDebugEnabled(ctx))
        		{
        			LogSupport.debug(ctx, this,strMsg.toString());
        		}
        		//add exception handling logic here.
        		throw new HomeException(e.toString(),e.getCause());
        		

        	}
        }
        	return super.create(ctx,obj);
	}
	
    
    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException {
    	
    	HTMLExceptionListener exceptions = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
		MessageMgr manager = new MessageMgr(ctx, this); 

		        if (exceptions == null)
		        {
		            exceptions = new HTMLExceptionListener(manager);
		            ctx.put(HTMLExceptionListener.class, exceptions);
		        }
		 ctx.put(ExceptionListener.class, exceptions);
    	
    	final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;

        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);
        
        AuxiliaryServiceTypeEnum auxServiceType = auxService.getType();
        if(!auxServiceType.equals(AuxiliaryServiceTypeEnum.TFA))
        {
        	 super.remove(ctx,obj);
        	 return;
        }
               
		TFAAuxSvcExtension tfaExtension = TFAAuxSvcExtension
				.getTfaServiceExtention(ctx, auxService.getIdentifier());
		short tfaExtIndex = tfaExtension.getTfaServiceExtention().getIndex();
		if (tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_CONTR_BLACKLIST_INDEX
				|| tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_CONTR_WHITELIST_INDEX
				|| tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_RECP_BLACKLIST_INDEX
				|| tfaExtIndex == TFAAuxiliaryServiceExtensionTypeEnum.TFA_RECP_WHITELIST_INDEX) {

 	
			final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);

        	try 
        	{
        		List<AuxiliaryService> lst = new ArrayList<AuxiliaryService>();
        		lst.add(auxService);
        		TFAAuxServiceSupport.removeWhiteBlackList(ctx,subscriber,lst);
        	}
        	catch (TFAAuxiliarServiceClientException e) 
        	{
        		StringBuilder strMsg = new StringBuilder();
        		strMsg.append("Unable to Unprovision Service(s) on TFA. "+e.getMessage());

        		if(LogSupport.isDebugEnabled(ctx))
        		{
        			LogSupport.debug(ctx, this,strMsg.toString());
        		}
        		strMsg.setLength(0);
        		throw new HomeException(e.getMessage()); 
        
        	}
        }
    	
    	super.remove(ctx, obj);
    }
}
