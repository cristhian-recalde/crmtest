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
package com.trilogy.app.crm.extension.subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberAuxiliaryServiceCharger;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.FacetMgrUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.NARY;
import com.trilogy.framework.xhome.elang.Pair;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.elang.Value;
import com.trilogy.framework.xhome.home.AbstractHome;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.AbstractValueVisitor;
import com.trilogy.framework.xhome.visitor.FindVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.RemoveAllVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.xdb.AndXStatement;
import com.trilogy.framework.xhome.xdb.CompoundXStatement;
import com.trilogy.framework.xhome.xdb.OrXStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This home for Multi-SIM subscription extensions redirects to SubscriberAuxiliaryService and 
 * works together with the extension to handle service and SIM creation.
 * 
 * It also adapts extension based queries to auxiliary service based queries for cross-home
 * compatibility. 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class MultiSimSubExtensionAuxSvcFlatteningHome extends AbstractHome
{
    public static final String EXTENSION_TRIGGERED_REMOVE = "EXTENSION_TRIGGERED_REMOVE";


    public MultiSimSubExtensionAuxSvcFlatteningHome(Context ctx)
    {
        super(ctx);
    }


    /**
     * {@inheritDoc}
     */
    public Object create(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        Object result = bean;
        if (bean instanceof MultiSimSubExtension)
        {
            MultiSimSubExtension ext = (MultiSimSubExtension) bean;
            
            String subId = ext.getSubId();
            long auxSvcId = ext.getAuxSvcId();
            
            AuxiliaryService auxSvc = HomeSupportHelper.get(ctx).findBean(ctx, AuxiliaryService.class, auxSvcId);
            if (auxSvc == null || !AuxiliaryServiceTypeEnum.MultiSIM.equals(auxSvc.getType()))
            {
                AuxiliaryServiceTypeEnum type = (auxSvc == null ? null : auxSvc.getType());
                throw new HomeException("Can't add Multi-SIM extension for non-Multi-SIM auxiliary service [" + auxSvcId + " - " + type + "] to subscription " + subId);
            }
            
            And filter = new And();
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subId));
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, auxSvcId));
            filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER));
            
            if (HomeSupportHelper.get(ctx).hasBeans(ctx, SubscriberAuxiliaryService.class, filter))
            {
                throw new HomeException("Multi-SIM extension already exists for subscription " + subId + " and auxiliary service " + auxSvcId);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Creating primary Multi-SIM auxiliary service association from subscription extension [SubId=" + subId + ",AuxSvcId=" + auxSvcId + "]...", null).log(ctx);
            }
            
            SubscriberAuxiliaryService association = null;
            try
            {
                association = (SubscriberAuxiliaryService) XBeans.instantiate(SubscriberAuxiliaryService.class, ctx);
            }
            catch (Exception e)
            {
                association = new SubscriberAuxiliaryService();
            }

            association.setSubscriberIdentifier(subId);
            association.setAuxiliaryServiceIdentifier(auxSvcId);
            association.setAuxiliaryService(auxSvc);
            association.setType(AuxiliaryServiceTypeEnum.MultiSIM);
            association.setProvisioned(Boolean.TRUE);
            association = HomeSupportHelper.get(ctx).createBean(ctx, association);

            if (association != null
                    && !ext.isChargePerSim())
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Attempting per-Service charge for Multi-SIM auxiliary service [" + ext.getAuxSvcId()
                            + "] and subscription [" + ext.getSubId() + "]...", null).log(ctx);
                }
                   
                CrmCharger charger = new SubscriberAuxiliaryServiceCharger(ext.getSubscriber(ctx), association);
                charger.charge(ctx, null);
            }
            
            try
            {
                Context sCtx = ctx.createSubContext();

                DefaultExceptionListener el = new DefaultExceptionListener();
                sCtx.put(ExceptionListener.class, el);

                // Update the extension to create the individual SIMs if there are any in the list at time of creation.
                ext.update(sCtx);
                
                if (el.hasErrors())
                {
                    int numOfErrors = el.numOfErrors();
                    String msg = "Multi-SIM extension created, but failed to create one or more SIMs.  [Count=" + numOfErrors + "].";
                    new MinorLogMsg(this, msg + "  See DEBUG logs for error details.", null).log(ctx);
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        int i=1;
                        for (Throwable t : (List<Throwable>) el.getExceptions())
                        {
                            new DebugLogMsg(this, "Error occurred creating individual SIM (Error " + i + "/" + numOfErrors + ")", t).log(ctx);
                        }
                    }
                    throw new HomeException(msg, (Throwable) el.getExceptions().iterator().next());
                }
            }
            catch (Exception e)
            {
                throw new HomeException("Multi-SIM extension created, but failed to create one or more SIMs: " + e.getMessage(), e);
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, Object bean) throws HomeException, HomeInternalException
    {
        // Note: ExtensionInstallationHome will trigger the aux service association update required.
        if (bean instanceof MultiSimSubExtension)
        {
            // Clear the SIMs list so that they get refreshed on the pipeline's outflow.  This will
            // ensure that the latest list of SIMs is used in the out-flow.
            ((MultiSimSubExtension) bean).setSims(null);
        }
        return bean;
    }


    /**
     * {@inheritDoc}
     */
    public void remove(Context ctx, Object bean) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        if (bean instanceof MultiSimSubExtension
                && !ctx.getBoolean(MultiSimAuxSvcExtension.SERVICE_TRIGGERED_REMOVE, Boolean.FALSE))
        {
            MultiSimSubExtension ext = (MultiSimSubExtension) bean;
            
            String subId = ext.getSubId();
            long auxSvcId = ext.getAuxSvcId();
            
            SubscriberAuxiliaryService association = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx, subId, auxSvcId, SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER);
            if (association != null)
            {
                Context sCtx = ctx.createSubContext();
                sCtx.put(EXTENSION_TRIGGERED_REMOVE, Boolean.TRUE);
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Removing primary Multi-SIM auxiliary service association for subscription extension [SubId=" + subId + ",AuxSvcId=" + auxSvcId + "]...", null).log(ctx);
                }
                
                HomeSupportHelper.get(ctx).removeBean(sCtx, association);

                if (!ext.isChargePerSim())
                {
                    short chargeCode = ext.getChargeCode(ctx, association.getAuxiliaryService(ctx), association);
                    if (chargeCode == 0)
                    {
                        // Refund the prorated service fee if it was previously charged successfully
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Attempting per-Service refund for Multi-SIM association for auxiliary service [" + ext.getAuxSvcId()
                                + "] and subscription [" + ext.getSubId() + "]...", null).log(ctx);
                        }
                        
                        CrmCharger  charger = new SubscriberAuxiliaryServiceCharger(ext.getSubscriber(sCtx), association);
                        charger.refund(ctx, null);
                    }
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void removeAll(Context ctx, Object where) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        forEach(ctx, new RemoveAllVisitor(this), where);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object find(Context ctx, Object where) throws HomeException
    {
        if (where instanceof MultiSimSubExtension)
        {
            // This case is easy, so skip the complicated where clause handling
            return generateNewExtension(ctx, (MultiSimSubExtensionID) ((MultiSimSubExtension) where).ID());
        }
        
        where = HomeSupportHelper.get(ctx).wrapKeyWithEQ(ctx, MultiSimSubExtension.class, where);
        Visitor result = forEach(ctx, new FindVisitor(ctx, where), where);
        result = Visitors.find(result, AbstractValueVisitor.class);
        if (result instanceof AbstractValueVisitor)
        {
            return ((AbstractValueVisitor)result).getValue();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public Collection select(Context ctx, Object where) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        if (where instanceof MultiSimSubExtension)
        {
            // This case is easy, so skip the complicated where clause handling
            return Arrays.asList(new MultiSimSubExtension[] {generateNewExtension(ctx, (MultiSimSubExtensionID) ((MultiSimSubExtension) where).ID())});
        }
        
        Visitor result = forEach(ctx, new ListBuildingVisitor(), where);
        result = Visitors.find(result, Collection.class);
        if (result instanceof Collection)
        {
            return (Collection)result;
        }
        return new ArrayList();
    }


    /**
     * {@inheritDoc}
     */
    public Visitor forEach(Context ctx, Visitor visitor, Object where) throws HomeException, HomeInternalException
    {
        where = HomeSupportHelper.get(ctx).wrapKeyWithEQ(ctx, MultiSimSubExtension.class, where);
        
        Collection<MultiSimSubExtension> results = null;
        try
        {
            Collection<SubscriberAuxiliaryService> services = getSubscriberAuxiliaryServices(ctx, where);
            if (services != null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Found " + services.size() + " services matching query.  Converting to Multi-SIM subscription extensions...", null).log(ctx);
                }
                
                results = new ArrayList<MultiSimSubExtension>();
                for (SubscriberAuxiliaryService service : services)
                {
                    if (service != null && AuxiliaryServiceTypeEnum.MultiSIM.equals(service.getType(ctx)))
                    {
                        long auxSvcId = service.getAuxiliaryServiceIdentifier();
                        String subId = service.getSubscriberIdentifier();

                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "Creating Multi-SIM subscription extension for subscription [" + subId + "] and auxiliary service [" + auxSvcId + "]", null).log(ctx);
                        }
                        
                        MultiSimSubExtensionID extId = new MultiSimSubExtensionID(subId, auxSvcId);
                        MultiSimSubExtension extension = generateNewExtension(ctx, extId);
                        results.add(extension);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new HomeException("Error occurred retrieving subscriber auxiliary services: " + e.getMessage(), e);
        }
        
        if (results != null)
        {
            try
            {
                return Visitors.forEach(ctx, results, visitor, where);
            }
            catch (AgentException e)
            {
                throw new HomeException("Error occurred retrieving subscriber auxiliary services: " + e.getMessage(), e);
            }   
        }
        
        return visitor;
    }


    protected MultiSimSubExtension generateNewExtension(Context ctx, MultiSimSubExtensionID bean)
    {
        MultiSimSubExtension result = null;

        String subId = bean.getSubId();
        long auxSvcId = bean.getAuxSvcId();

        try
        {
            result = (MultiSimSubExtension) XBeans.instantiate(MultiSimSubExtension.class, ctx);
        }
        catch (Exception e)
        {
            result = new MultiSimSubExtension();
        }

        result.setAuxSvcId(auxSvcId);
        result.setSubId(subId);

        AuxiliaryService auxSvc = result.getAuxiliaryService(ctx);
        if (auxSvc != null)
        {
            boolean isChargePerSim = MultiSimAuxSvcExtension.DEFAULT_CHARGEPERSIM;
            long maxNumSims = MultiSimAuxSvcExtension.DEFAULT_MAXNUMSIMS;
            MultiSimAuxSvcExtension multiSimAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSvc, MultiSimAuxSvcExtension.class);
            if (multiSimAuxSvcExtension != null)
            {
                isChargePerSim = multiSimAuxSvcExtension.isChargePerSim();
                maxNumSims = multiSimAuxSvcExtension.getMaxNumSIMs();
            }
            else 
            {
                LogSupport.minor(ctx, this,
                        "Unable to find required extension of type '" + MultiSimAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + auxSvc.getIdentifier());
            }
            result.setSpid(auxSvc.getSpid());
            result.setAuxSvcName(auxSvc.getName());
            result.setChargePerSim(isChargePerSim);
            result.setCharge(auxSvc.getCharge());
            result.setMaxNumSims(maxNumSims);
        }

        return result;
    }


    protected Collection<SubscriberAuxiliaryService> getSubscriberAuxiliaryServices(Context ctx, Object where) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "getSubscriberAuxiliaryServices(): Received query: " + where, null).log(ctx);
        }
        
        where = adaptWhereClause(ctx, where);

        if (where instanceof MultiSimSubExtension)
        {
            MultiSimSubExtension extId = (MultiSimSubExtension) where;
            
            where = new And();
            ((And)where).add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, extId.getAuxSvcId()));
            ((And)where).add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, extId.getSubId()));
        }
        
        And filter = new And();
        filter.add(where);
        filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER));
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "getSubscriberAuxiliaryServices(): Proxying query to SubscriberAuxiliaryService home: " + where, null).log(ctx);
        }
        
        return HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberAuxiliaryService.class, filter);
    }


    private Object cloneWhereClause(Context ctx, Object where)
    {
        if (where instanceof Context)
        {
            Object id = XBeans.getInstanceOf(ctx, where, MultiSimSubExtensionID.class);
            if (id == null)
            {
                where = XBeans.getInstanceOf(ctx, where, XStatement.class);
            }
            else
            {
                where = XBeans.getInstanceOf(ctx, id, XStatement.class);
            }
        }
        if (where instanceof AbstractBean)
        {
            try
            {
                Object newWhere = FacetMgrUtil.instantiate(ctx, where.getClass());
                XBeans.copy(ctx, where, newWhere);
                return newWhere;
            }
            catch (Exception e)
            {
                // NOP
            }
        }
        if (where instanceof XCloneable)
        {
            try
            {
                return ((XCloneable) where).clone();
            }
            catch (Exception e)
            {
                // NOP
            }
        }
        return where;
    }


    private Object adaptWhereClause(Context ctx, Object where)
    {   
        where = cloneWhereClause(ctx, where);
        Object result = where;
        if (where instanceof NARY)
        {
            List list = ((NARY) where).getList();
            if (list != null)
            {
                List newEntries = new ArrayList();
                for (Object entry : list)
                {
                    Object newEntry = adaptWhereClause(ctx, entry);
                    logWherePortionSwap(ctx, entry, newEntry);
                    newEntries.add(newEntry);
                }
                ((NARY) where).setList(newEntries);
            }
        }
        else if (where instanceof Pair)
        {
            Pair pair = (Pair) where;
            
            Object arg1 = pair.getArg1();
            arg1 = adaptWhereClause(ctx, arg1);
            logWherePortionSwap(ctx, pair.getArg1(), arg1);
            
            pair.setArg1(arg1);
            
            Object arg2 = pair.getArg2();
            arg2 = adaptWhereClause(ctx, arg2);
            logWherePortionSwap(ctx, pair.getArg2(), arg2);
            
            pair.setArg2(arg2);
        }
        else if (where instanceof Value)
        {
            Value value = (Value) where;
            
            Object arg1 = value.getArg1();
            arg1 = adaptWhereClause(ctx, arg1);
            logWherePortionSwap(ctx, value.getArg1(), arg1);
            
            value.setArg1(arg1);
        }
        else if (where instanceof In)
        {
            In in = (In) where;
            PropertyInfo arg = in.getArg();
            Object newArg = adaptWhereClause(ctx, arg);
            if (!(newArg instanceof PropertyInfo))
            {
                return null;
            }

            logWherePortionSwap(ctx, arg, newArg);
            
            in.setArg((PropertyInfo) newArg);
        }
        else if (where instanceof PropertyInfo)
        {
            PropertyInfo prop = (PropertyInfo) where;
            if (MultiSimSubExtension.class.isAssignableFrom(prop.getBeanClass()))
            {
                if (MultiSimSubExtensionXInfo.AUX_SVC_ID.equals(prop))
                {
                    result = SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER;
                }
                else if (MultiSimSubExtensionXInfo.SUB_ID.equals(prop))
                {
                    result = SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER;
                }
                else if (SubscriberExtensionXInfo.SUB_ID.equals(prop))
                {
                    result = SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER;
                }
            }
            else if (SubscriberExtension.class.isAssignableFrom(prop.getBeanClass()))
            {
                if (SubscriberExtensionXInfo.SUB_ID.equals(prop))
                {
                    result = SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER;
                }
            }
            
            logWherePortionSwap(ctx, where, result);
        }
        else if (where instanceof MultiSimSubExtensionXStatement)
        {
            Object bean = ((MultiSimSubExtensionXStatement) where).getBean();
            if (bean instanceof MultiSimSubExtension)
            {
                result = new And();
                ((And)result).add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, ((MultiSimSubExtension) bean).getAuxSvcId()));
                ((And)result).add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, ((MultiSimSubExtension) bean).getSubId()));
            }
        }
        else if (where instanceof CompoundXStatement)
        {
            final XStatement localResult;
            if (where instanceof OrXStatement)
            {
                localResult = new OrXStatement();
            }
            else if (where instanceof AndXStatement)
            {
                localResult = new AndXStatement();
            }
            else
            {
                localResult = new CompoundXStatement();
            }
            
            try
            {
                final Object holder = where;
                ((CompoundXStatement) where).forEach(ctx, new Visitor()
                {
                    public void visit(Context vCtx, Object obj) throws AgentException, AbortVisitException
                    {
                        if (obj == holder)
                        {
                            return;
                        }
                        
                        Object newEntry = adaptWhereClause(vCtx, obj);
                        if (newEntry instanceof XStatement)
                        {
                            ((CompoundXStatement) localResult).add((XStatement) newEntry);
                            logWherePortionSwap(vCtx, obj, newEntry);
                        }
                        else if (newEntry instanceof String)
                        {
                            ((CompoundXStatement) localResult).add((String) newEntry);
                            logWherePortionSwap(vCtx, obj, newEntry);
                        }
                    }
                });
   
                result = new com.redknee.app.crm.filter.EitherPredicate(True.instance(), localResult);
            }
            catch (AgentException e)
            {
                // NOP
            }
        }
        
        return result;
    }


    private void logWherePortionSwap(Context ctx, Object original, Object newValue)
    {
        if (newValue != original)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Swapping out [" + original + "] for [" + newValue + "] in where clause in preparation for SubscriberAuxiliaryService query...", null).log(ctx);
            }
        }
    }

}
