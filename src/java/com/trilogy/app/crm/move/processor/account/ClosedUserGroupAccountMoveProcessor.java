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
package com.trilogy.app.crm.move.processor.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.provision.CallingGroupProvisionAgent;
import com.trilogy.app.crm.provision.CallingGroupUnprovisionAgent;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * 
 * This class handles CUG move of Responsible to Responsible Account move request
 * 
 * 
 * @param <AMR>
 */
public class ClosedUserGroupAccountMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{

    static String MODULE = ClosedUserGroupAccountMoveProcessor.class.getName();


    public ClosedUserGroupAccountMoveProcessor(AMR request)
    {
        this(new BaseAccountMoveProcessor<AMR>(request));
    }


    public ClosedUserGroupAccountMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        super.validate(ctx);
    }


    /**
     * @{inheritDoc
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        // get all the subscribers under this account.
        // find out which of the subscribers are in cug
        // put these subscribers in a local list
        List<Subscriber> msisdnList = new ArrayList<Subscriber>();
        AMR request = getRequest();
        Account oldAccount = request.getOldAccount(ctx);
        Account rootAccountOld = null;
        ClosedUserGroup accountCugOld = null;
        try
        {
            rootAccountOld = oldAccount.getRootAccount(ctx);
            accountCugOld = ClosedUserGroupSupport.getCug(ctx, rootAccountOld.getBAN());
            if (accountCugOld != null)
            {
                // Filter Subscribers which are members of CUG
                Map subMap = accountCugOld.getSubscribers();
                getAllSubscribers(ctx, oldAccount, msisdnList);
                Iterator<Subscriber> iter = msisdnList.iterator();
                while (iter.hasNext())
                {
                    Subscriber sub = iter.next();
                    if (subMap.get(sub.getMsisdn()) == null)
                    {
                        iter.remove();
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Error while fetching all the subscriber of old account : " + request.getOldAccount(ctx);
            throw new MoveException(request, msg, e);
        }
        super.move(ctx);
        // iterate through map and remove all the subscribers from their cugs
        // add the subscribers to new cug
        ctx = ctx.createSubContext();
        ctx.put(ClosedUserGroupHome.class, ctx.get(MoveConstants.CUSTOM_CUG_HOME_CTX_KEY));
        if (accountCugOld != null)
        {
            try
            {
                Account newAccount = request.getNewAccount(ctx);
                Account newRootAccount = newAccount.getRootAccount(ctx);
                ClosedUserGroup newAccountCug = ClosedUserGroupSupport.getCug(ctx, newRootAccount.getBAN());
                // if new account does not have any cug associated with it, create a new
                // cug with old account's template id.
                long newCugTemplateID = newAccountCug != null ? newAccountCug.getCugTemplateID() : accountCugOld
                        .getCugTemplateID();
                for (Subscriber sub : msisdnList)
                {
                    String msisdn = sub.getMsisdn();
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Msisdn " + msisdn + " belongs to cug : " + accountCugOld.getID()
                                + " Removing msisdn from the CUG.");
                    }
                    CallingGroupUnprovisionAgent.unProvisionCUG(ctx, msisdn, rootAccountOld, accountCugOld);
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Subscription with Msisdn " + msisdn
                                + " has CUG Service. On account move it would be added to CUG of Root Account : "
                                + newRootAccount.getBAN());
                    }
                    if (newAccount != newRootAccount)
                    {
                        long cugId = CallingGroupProvisionAgent.provisionCUG(ctx, newRootAccount, newCugTemplateID,
                                msisdn);
                        // update auxiliary service for this subscription with secondary
                        // identifier equal to new cug id.
                        SubscriberAuxiliaryServiceSupport.updateSubscriberPrivateCugAuxiliaryServices(ctx, sub.getId(),
                                sub.getId(), accountCugOld.getID(), cugId);
                    }
                    else
                    {
                        Collection<SubscriberServices> currentServices = SubscriberServicesSupport
                                .getSubscribersServices(ctx, sub.getId()).values();
                        for (SubscriberServices subService : currentServices)
                        {
                            Service service = subService.getService(ctx);
                            if (service != null)
                            {
                                if (subService.getService().getType().equals(ServiceTypeEnum.CALLING_GROUP))
                                {
                                    services_.add(subService);
                                    serviceIds_.add(subService.getServiceId());
                                }
                            }
                        }
                        SubscriberServicesSupport.deleteSubscriberServiceRecords(ctx, sub.getId(), serviceIds_);
                        for (Long serviceId : serviceIds_)
                        {
                            SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                                    HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, serviceId,
                                    ServiceStateEnum.UNPROVISIONED);
                        }
                        SubscriberAuxiliaryService auxService = SubscriberAuxiliaryServiceSupport
                                .removeSubscriberPrivateCugAuxiliaryServices(ctx, sub.getId(), accountCugOld.getID());
                        if (auxService != null)
                        {
                            SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                                    HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.AUXSERVICE, auxService,
                                    ServiceStateEnum.UNPROVISIONED);
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                String msg = "Error while moving msisdns from old CUG " + accountCugOld.getID()
                        + " to new CUG. Account : " + request.getNewAccount(ctx);
                throw new MoveException(request, msg, e);
            }
        }
    }


    private void getAllSubscribers(Context ctx, Account account, List<Subscriber> subList) throws HomeException
    {
        Collection<Subscriber> immediateSubscribers = account.getImmediateChildrenSubscribers(ctx);
        subList.addAll(immediateSubscribers);
        Collection immediateAccounts = account.getImmediateChildrenAccounts(ctx);
        for (Object obj : immediateAccounts)
        {
            Account act = (Account) obj;
            getAllSubscribers(ctx, act, subList);
        }
    }

    private List<SubscriberServices> services_ = new ArrayList<SubscriberServices>();
    private Set<Long> serviceIds_ = new HashSet<Long>();
}
