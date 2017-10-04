/*
 * Created on Oct 25, 2004
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.cltc;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.provision.ProvisioningSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;

/**
 * @author jchen
 */
public class ServiceCreditLimitCheckSupport 
{   
    /**
     * 
     * @param ctx
     * @param sub
     * @return a new
     * @throws HomeException
     */
    public static Collection getDiffServiceIds(Context ctx,  Subscriber sub, Object logSource) throws HomeException
    {
        Collection notProvisioned = new HashSet();
        if (sub != null)
        {
            // TODO 2007-04-16 Improve performance use getRawPricePlanVersion() and remove non subscribed services
            /*
             * Sujeet: 
             * About: Collection targetService = ppVersion.getServices(ctx);
             * 
             * When we got the instance of sub, why can't we simply do sub.getServices(ctx), instead of 
             * ppVersion.getServices(ctx) which does cloning an all that? That one (former) as well claims
             * to return PricePlan services + the mandatory onces??
             */
            final PricePlanVersion ppVersion = sub.getPricePlan(ctx);
            if (ppVersion != null)
            {
                /*
                 *  FIX for TT#12071755026. With RP=true, the services is not charged if it were to cross the
                 *  Credit Limit. So, we can not allow those services to be brought back to active without
                 *  charges applied. The only services that are allowed are the ones which were CLTC-suspended.
                 *  
                 *  @see com.redknee.app.crm.bas.recharge.RetryRecurRechargeVisitor where suspended services 
                 *  with RP=true are recharged.
                 */
                // Collection targetService = ppVersion.getServices(ctx);
                Collection targetService = ppVersion.getServicesExcludingThoseMarkedRestrictProvisioning(ctx);
                
                if(LogSupport.isDebugEnabled(ctx))
                {
                    String msg = MessageFormat.format(
                        "Services for CLTC Check: Subscriber: {0} | PP-Version: {1} | ALL: {2} | RP-EXCLUDED: {3}", 
                            new Object[]{sub.getId(), Long.valueOf(ppVersion.getId()), ppVersion.getServices(ctx), 
                                ppVersion.getServicesExcludingThoseMarkedRestrictProvisioning(ctx)});
                    LogSupport.debug(ctx, logSource, msg);
                }
                    
                notProvisioned.addAll(targetService);
                notProvisioned.removeAll(sub.getProvisionedServices(ctx));
            }
        }
        return notProvisioned;
    }

    /**
     * Create Subscriber notes for service changed due to CLCT change
     * @param ctx
     * @param sub
     * @param provisionedSvc
     * @param unprovisionedSvc
     */
    public static void createSubscriberNoteCltcProvision(Context ctx, Subscriber sub, 
            Collection provisionedSvc, Collection suspendSvc) throws HomeException
    {
        StringBuilder msgProvision = new StringBuilder("Services ");
        if (suspendSvc.size() > 0)
        {
            msgProvision.append("suspend/unprovsion due to CLCT:");
            msgProvision.append(SubscriberServicesSupport.getServiceIdString(suspendSvc));
        }
        else
        {
            msgProvision.append("provisioned due to CLCT:");
            msgProvision.append(SubscriberServicesSupport.getServiceIdString(provisionedSvc));
        }
        
        if (provisionedSvc.size() != 0 || suspendSvc.size() != 0 )
        {
            msgProvision.append( ";");
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, sub.getId(), msgProvision.toString(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.CLCT_UPDATE);
        }
    }

    /**
     * Update the given Service records to Suspended due to CLTC
     * @param ctx
     * @param sub the subscriber affected by cltc
     * @param unprovisionedSvc list of services being unprovisioned due to cltc
     * @throws HomeException
     */
    public static void changeServiceStateDueToCLTC(Context ctx, Subscriber sub, Collection<SubscriberServices> suspendSvc)
        throws HomeException
    {
        
        ServiceStateEnum state = ServiceStateEnum.SUSPENDED; 
        if (suspendSvc.size() > 0)
        {

            //Suspend all services due to CLTC
            for (Iterator i=suspendSvc.iterator(); i.hasNext(); )
            {
                SubscriberServices service = (SubscriberServices) i.next();
                service.setProvisionAction(ServiceProvisionActionEnum.SUSPEND);
                SubscriberServicesSupport.createOrModifySubcriberService(ctx, sub, service.getServiceId(), 
                        ServiceStateEnum.SUSPENDED, SuspendReasonEnum.CLCT);
                
                try
                {
                    ProvisioningSupport.suspendService(ctx, sub, service.getService(ctx), null);
                   
                    service.setProvisionActionState(true);
                    service.setProvisionedState(state);
                    service.setSuspendReason(SuspendReasonEnum.CLCT);
                    SubscriberServicesSupport.createOrModifySubcriberService(ctx,service);
                    
                }
                catch (Exception ex)
                {
                    state = ServiceStateEnum.SUSPENDEDWITHERRORS; 
                    service.setProvisionedState(state );
                    service.setProvisionActionState(false);
                    service.setSuspendReason(SuspendReasonEnum.CLCT);
                    sub.updateSubscriberService(ctx, service, state);
                     new MinorLogMsg(ServiceCreditLimitCheckSupport.class, " Unable to suspend the service" + service.getServiceId() + 
                            " for sub=> " + sub.getId(), ex).log(ctx);
                }finally
                {
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub, 
                            HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, service.getService(ctx), state);
                }
                
            }
            
        }
    }
    
}
