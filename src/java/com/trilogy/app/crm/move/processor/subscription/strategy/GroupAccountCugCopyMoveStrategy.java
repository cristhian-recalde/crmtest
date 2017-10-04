/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.provision.CallingGroupProvisionAgent;
import com.trilogy.app.crm.provision.CallingGroupUnprovisionAgent;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;


/**
 * Responsible to move subscription from one InBanCalling group CUG to another group
 * account InBanCalling CUG.
 * 
 * It performs validation required to complete its task successfully.
 * 
 * @author bpandey
 * @since 9.5.1
 */
public class GroupAccountCugCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{

    public GroupAccountCugCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        if (!ctx.has(MoveConstants.CUSTOM_CUG_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException("Custom Cug home not installed in context."));
        }
        SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        cise.throwAll();
        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);
        // If old Account's root ban has FNF extension associated with it, old
        // subscription could be a part of In ban cug
        // If old subscription is a part of In Ban cug it wil be removed from old CUG and
        // will be added to new CUG of new Account
        Context subCtx = ctx.createSubContext();
        subCtx.put(ClosedUserGroupHome.class, ctx.get(MoveConstants.CUSTOM_CUG_HOME_CTX_KEY));
        try
        {
        	Account oldAccount = oldSubscription.getAccount(subCtx);
        	Account rootOldAccount = oldAccount.getRootAccount(subCtx);
        	Account newAccount = newSubscription.getAccount(subCtx);
        	ClosedUserGroup oldAccountCug = ClosedUserGroupSupport.getCug(ctx, rootOldAccount.getBAN());
        	boolean oldAccountRemovedFromCug = CallingGroupUnprovisionAgent.unProvisionCUG(subCtx,
        			oldSubscription.getMsisdn(), rootOldAccount, oldAccountCug);
        	if (oldAccountRemovedFromCug)
        	{
        		if (!newAccount.isRootAccount())
        		{
        			long cugId = CallingGroupProvisionAgent.provisionCUG(subCtx, newAccount.getRootAccount(subCtx),
        					oldAccountCug.getCugTemplateID(), newSubscription.getMsisdn());
        			// update private cug auxiliary service.
        			SubscriberAuxiliaryServiceSupport.updateSubscriberPrivateCugAuxiliaryServices(subCtx,
        					oldSubscription.getId(), newSubscription.getId(), oldAccountCug.getID(), cugId);
        		} 
        		else
        		{
        			Collection<SubscriberServices> currentServices = SubscriberServicesSupport.getSubscribersServices(
        					ctx, newSubscription.getId()).values();            	
        			for (SubscriberServices subService : currentServices)
        			{
        				Service service = subService.getService(ctx);
        				if (service != null)
        				{
        					if(subService.getService().getType().equals(ServiceTypeEnum.CALLING_GROUP))
        					{
        						services_.add(subService);
        						serviceIds_.add(subService.getServiceId());
        					}
        				}
        			}
        			SubscriberServicesSupport.deleteSubscriberServiceRecords(subCtx, newSubscription.getId(),
        					serviceIds_);
        			for(Long serviceId: serviceIds_)
        			{
        				SubscriberSubscriptionHistorySupport.addProvisioningRecord(subCtx, newSubscription,
        						HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, serviceId,
        						ServiceStateEnum.UNPROVISIONED);
        			}
        			SubscriberAuxiliaryService auxService = SubscriberAuxiliaryServiceSupport.removeSubscriberPrivateCugAuxiliaryServices(subCtx,
        					oldSubscription.getId(), oldAccountCug.getID());
        			if(auxService != null)
        			{
        				SubscriberSubscriptionHistorySupport.addProvisioningRecord(subCtx, newSubscription,
        						HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.AUXSERVICE, auxService,
        						ServiceStateEnum.UNPROVISIONED);
        			}
        		}
        	}
        } 
        catch (Exception e)
        {
            throw new MoveException(request, "Error while moving msisdn : " + oldSubscription.getMsisdn()
                    + " from account cug", e);
        }
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }

    private List<SubscriberServices> services_ = new ArrayList<SubscriberServices>();
    private Set<Long> serviceIds_ = new HashSet<Long>();
}
