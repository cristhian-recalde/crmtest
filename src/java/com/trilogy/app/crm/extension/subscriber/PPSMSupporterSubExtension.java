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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.LazyLoadBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.extension.CategoryExtension;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * 
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporterSubExtension extends AbstractPPSMSupporterSubExtension
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public PPSMSupporterSubExtension()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends CategoryExtension> getExtensionCategoryClass()
    {
        return PPSMSubscriberExtension.class;
    }

    /**
     * {@inheritDoc}
     */
    public PPSMSupporterSubExtension(Context ctx)
    {
        super();
        setContext(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx) throws IllegalStateException
    {
        Subscriber subscriber = getSubscriber(ctx);
        
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        if (subscriber != null && !subscriber.isPostpaid())
        {
            cise.thrown(new IllegalPropertyArgumentException(PPSMSupporterSubExtensionXInfo.SUB_ID, "PPSM Supporter subscription must be a POSTPAID subscription."));
        }
        else
        {
            if ((getChargingTemplates()==null || getChargingTemplates().size()==0) && (getScreeningTemplates()==null || getScreeningTemplates().size()==0))
            {
                cise.thrown(new IllegalPropertyArgumentException(PPSMSupporterSubExtensionXInfo.CHARGING_TEMPLATES, "At least one charging template or one screening template must be chosen."));
                cise.thrown(new IllegalPropertyArgumentException(PPSMSupporterSubExtensionXInfo.SCREENING_TEMPLATES, "At least one charging template or one screening template must be chosen."));
            }
    
            // Force reload of supported subscriber to make sure none was added.
            this.setSupportedSubscribers(null);
            
            for (Long templateId : getRemovedChargingTemplates(ctx))
            {
                for (PPSMSupporteeSubExtension supporteeExtension : getSupportedSubscribers(ctx))
                {
                    if (supporteeExtension.getChargingTemplate() == templateId.longValue())
                    {
                        cise.thrown(new IllegalPropertyArgumentException(PPSMSupporterSubExtensionXInfo.CHARGING_TEMPLATES, "Charging template " + templateId + " is in use by at least one of the supported subscriptions and cannot be removed."));
                        break;
                    }
                }
            }
            for (Long templateId : getRemovedScreeningTemplates(ctx))
            {
                for (PPSMSupporteeSubExtension supporteeExtension : getSupportedSubscribers(ctx))
                {
                    if (supporteeExtension.getScreeningTemplate() == templateId.longValue())
                    {
                        cise.thrown(new IllegalPropertyArgumentException(PPSMSupporterSubExtensionXInfo.SCREENING_TEMPLATES, "Screening template " + templateId + " is in use by at least one of the supported subscriptions and cannot be removed."));
                        break;
                    }
                }
            }
        }

        cise.throwAll();
    }


    /**
     * {@inheritDoc}
     */
    public void install(Context ctx) throws ExtensionInstallationException
    {
        createCreationNotes(ctx);
    }


    /**
     * {@inheritDoc}
     */
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        Subscriber subscriber = getSubscriber(ctx);
        try
        {
            final Home supporteeHome = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
                supporteeHome.where(ctx, new EQ(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, subscriber.getMSISDN()))
                    .forEach(new Visitor()
                    {
                        public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                        {
                            PPSMSupporteeSubExtension supporteeExtension = (PPSMSupporteeSubExtension) obj;
                            try
                            {
                                supporteeHome.remove(ctx, supporteeExtension);
                            }
                            catch (HomeException e)
                            {
                                LogSupport.minor(ctx, this,
                                        "Unable to remove PPSM Supportee extension for subscriber '"
                                                + supporteeExtension.getSubId() + "': " + e.getMessage(), e);
                            }
                            
                        }
                    });

                createRemovalNotes(ctx);
        }
        catch (HomeException e)
        {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Unable to uninstall PPSM Supporter Extension for subscription ");
            errorMessage.append(this.getSubId());
            errorMessage.append(". Some supported subscriptions may still have the PPSM Supportee extension.");

            LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
            throw new ExtensionInstallationException(errorMessage.toString(), e, true, false);
        }
    }

    public void deactivate(Context ctx) throws ExtensionInstallationException
    {
        Home home = (Home) ctx.get(PPSMSupporterSubExtensionHome.class);
        try
        {
            home.remove(ctx, this);
        }
        catch (HomeException e)
        {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Unable to deactivate PPSM Supporter Extension for subscription ");
            errorMessage.append(this.getSubId());
            errorMessage.append(" due to an error on URCS.");

            LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
            throw new ExtensionInstallationException(errorMessage.toString(), e, true, false);
        }
    }
    
    public boolean isValidForSubscriberType(SubscriberTypeEnum subscriberType)
    {
        return SubscriberTypeEnum.POSTPAID.equals(subscriberType);
    }

    
    /**
     * this functions {@inheritDoc}
     */
    public void update(Context ctx) throws ExtensionInstallationException
    {
        final Subscriber subscriber = getSubscriber(ctx);

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (subscriber != null && oldSubscriber != null && subscriber.getId().equals(oldSubscriber.getId())
                && subscriber.getId().equals(this.getSubId())
                && !subscriber.getMSISDN().equals(oldSubscriber.getMSISDN()))
        {
            final Home supporteeHome = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
            try
            {
                supporteeHome.where(ctx, new EQ(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, oldSubscriber.getMSISDN()))
                    .forEach(new Visitor()
                    {
                        public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                        {
                            PPSMSupporteeSubExtension supporteeExtension = (PPSMSupporteeSubExtension) obj;
                            supporteeExtension.setSupportMSISDN(subscriber.getMSISDN());
                            supporteeExtension.bypassURCS();
                            try
                            {
                                supporteeHome.store(ctx, supporteeExtension);
                            }
                            catch (HomeException e)
                            {
                                LogSupport.minor(ctx, this,
                                        "Unable to update PPSM Supportee extension for subscriber '"
                                                + supporteeExtension.getSubId() + "' to MSISDN '"
                                                + subscriber.getMSISDN() + "': " + e.getMessage(), e);
                            }
                            
                        }
                    });
            }
            catch (HomeException e)
            {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Unable to update PPSM Supporter Extension for subscription ");
                errorMessage.append(this.getSubId());
                errorMessage.append(". Some supported subscriptions may still have the old Supporter MSISDN.");

                LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
                throw new ExtensionInstallationException(errorMessage.toString(), e, true, false);
            }
        }

        createUpdateNotes(ctx);
    }
    
    private void createCreationNotes(Context ctx)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Subscription became a PPSM supporter. ");
            sb.append("Charging templates: ");
            sb.append(this.getChargingTemplates());
            sb.append(", Screening templates: ");
            sb.append(this.getScreeningTemplates());
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, sb.toString());
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubscriber(ctx).getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding creation of PPSM Supporter Sub Extension: " + e.getMessage(), e);
        }
    }
    
    private void createUpdateNotes(Context ctx)
    {
        boolean chargingTemplatesChanged = (getAddedChargingTemplates(ctx).size() > 0 || getRemovedChargingTemplates(ctx).size() > 0);
        boolean screeningTemplatesChanged = (getAddedScreeningTemplates(ctx).size() > 0 || getRemovedScreeningTemplates(ctx).size() > 0);
        
        if (chargingTemplatesChanged || screeningTemplatesChanged)
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                sb.append("PPSM supporter templates updated: ");
                if (chargingTemplatesChanged)
                {
                    sb.append("Charging Templates -> Added = ");
                    sb.append(this.getAddedChargingTemplates(ctx));
                    sb.append("Removed = ");
                    sb.append(this.getRemovedChargingTemplates(ctx));
                    sb.append(". ");
                }
                if (screeningTemplatesChanged)
                {
                    sb.append("Screening Templates -> Added = ");
                    sb.append(this.getAddedScreeningTemplates(ctx));
                    sb.append("Removed = ");
                    sb.append(this.getRemovedScreeningTemplates(ctx));
                    sb.append(". ");
                }

                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, sb.toString());
                }
                
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubscriber(ctx).getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to add subscriber note regarding creation of PPSM Supporter Sub Extension: " + e.getMessage(), e);
            }    
        }
    }
    
    private void createRemovalNotes(Context ctx)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Subscription stopped being a PPSM supporter.");
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, sb.toString());
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubscriber(ctx).getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding removing of PPSM Supporter Sub Extension: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void move(Context ctx, Object newContainer) throws ExtensionInstallationException
    {
        // Deactivate old PPSM Supporter Extension during move.
        deactivate(ctx);
    }
    
    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return ctx_;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }
    
    public Set<PPSMSupporteeSubExtension> getSupportedSubscribers()
    {
        return this.getSupportedSubscribers(getContext());
    }

    public Set<PPSMSupporteeSubExtension> getSupportedSubscribers(Context ctx)
    {
        if (ctx!=null && super.getSupportedSubscribers()==null)
        {
            Set<PPSMSupporteeSubExtension> supportedSubscribers = new HashSet<PPSMSupporteeSubExtension>();
            Home home = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
            try
            {
                Subscriber sub = BeanLoaderSupportHelper.get(ctx).getBean(ctx,Subscriber.class);
                if (sub == null || (this.getSubId()!=null & !this.getSubId().isEmpty() && !sub.getId().equals(this.getSubId())))
                {
                    sub = SubscriberSupport.getSubscriber(ctx, this.getSubId());
                }
                
                if (sub!=null && sub.getMSISDN()!=null && !sub.getMSISDN().isEmpty())
                {
                    supportedSubscribers.addAll(home.select(new EQ(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, sub.getMSISDN())));
                }
                super.setSupportedSubscribers(supportedSubscribers);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to retrieve PPSM Supportee Subscribers for subscriber " + this.getSubId(), e);
            }
        }
        return super.getSupportedSubscribers();
    }
    
    public void resetSavedTemplates(Context ctx)
    {
        setSavedChargingTemplates(buildString(getChargingTemplates(ctx)));
        setSavedScreeningTemplates(buildString(getScreeningTemplates(ctx)));
    }
    
    public void setChargingTemplates(Set chargingTemplates)
    {
        Set templates = chargingTemplates;
        if (templates!=null && templates.size()>0 && !(templates.iterator().next() instanceof String))
        {
            templates = new HashSet<String>();
            for (Object o : chargingTemplates)
            {
                templates.add(String.valueOf(o));
            }
        }
        super.setChargingTemplates(templates);
    }

    public void setScreeningTemplates(Set screeningTemplates)
    {
        Set templates = screeningTemplates;
        if (templates!=null && templates.size()>0 && !(templates.iterator().next() instanceof String))
        {
            templates = new HashSet<String>();
            for (Object o : screeningTemplates)
            {
                templates.add(String.valueOf(o));
            }
        }
        super.setScreeningTemplates(templates);
    }

    public Set<String> getChargingTemplates(Context ctx)
    {
        if (super.getSavedChargingTemplates()==null && ctx!=null)
        {
            try
            {
                Home home = (Home) ctx.get(PPSMSupporterChargingTemplateHome.class);
                Collection<PPSMSupporterChargingTemplate> mappings = (Collection<PPSMSupporterChargingTemplate>) home
                        .select(new EQ(PPSMSupporterChargingTemplateXInfo.SUB_ID, this.getSubId()));
                Set<String> templates = new HashSet<String>();
                for (PPSMSupporterChargingTemplate mapping : mappings)
                {
                    templates.add(String.valueOf(mapping.getIdentifier()));
                }
                setChargingTemplates(templates);
                
                setSavedChargingTemplates(buildString(templates));
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to load charging templates for PPSM Subscriber Extension of subscriber '" + this.getSubId() + "': " + e.getMessage(), e);
            }
        }
        
        return super.getChargingTemplates();
    }

    public Set<String> getScreeningTemplates(Context ctx)
    {
        if (super.getSavedScreeningTemplates()==null && ctx != null)
        {
            try
            {
                Home home = (Home) ctx.get(PPSMSupporterScreenTemplateHome.class);
                Collection<PPSMSupporterScreenTemplate> mappings = (Collection<PPSMSupporterScreenTemplate>) home
                        .select(new EQ(PPSMSupporterScreenTemplateXInfo.SUB_ID, this.getSubId()));
                Set<String> templates = new HashSet<String>();
                for (PPSMSupporterScreenTemplate mapping : mappings)
                {
                    templates.add(String.valueOf(mapping.getIdentifier()));
                }
                setScreeningTemplates(templates);
                
                setSavedScreeningTemplates(buildString(templates));
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to load charging templates for PPSM Subscriber Extension of subscriber '" + this.getSubId() + "': " + e.getMessage(), e);
            }
        }
        
        return super.getScreeningTemplates();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean lazyLoad(Context ctx, PropertyInfo property)
    {
        if (property != null)
        {
            if (isFrozen())
            {
                new InfoLogMsg(this, "Unable to lazy-load "
                        + property.getBeanClass().getSimpleName() + "." + property.getName()
                        + " because PPSM Supporter " + this.getSubId() + " is frozen.", null).log(ctx);
            }
            else if (property.getBeanClass().isAssignableFrom(this.getClass()))
            {
                PMLogMsg pm = new PMLogMsg(
                        LazyLoadBean.class.getName(), 
                        this.getClass().getSimpleName() + ".lazyLoad(" + property.getName() + ")");
                try
                {
                    if (this.getContext() == null)
                    {
                        this.setContext(ctx);
                    }
                    property.get(this);
                    return true;
                }
                catch (Throwable t)
                {
                    ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                    if (el != null)
                    {
                        el.thrown(new IllegalPropertyArgumentException(property, t.getMessage()));
                    }
                    new MinorLogMsg(this, "Error occured lazy-loading "
                            + property.getBeanClass().getSimpleName() + "." + property.getName()
                            + ": " + t.getMessage(), t).log(ctx);
                }
                finally
                {
                    pm.log(ctx);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean lazyLoadAllProperties(Context ctx)
    {
        PMLogMsg pm = new PMLogMsg(LazyLoadBean.class.getName(), PPSMSupporterSubExtension.class.getSimpleName() + ".lazyLoadAllProperties()");
        try
        {
            if (this.getContext() == null)
            {
                setContext(ctx);
            }
            lazyLoad(ctx, PPSMSupporterSubExtensionXInfo.CHARGING_TEMPLATES);
            lazyLoad(ctx, PPSMSupporterSubExtensionXInfo.SCREENING_TEMPLATES);

            return true;
        }
        catch (Throwable t)
        {
            ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (el != null)
            {
                el.thrown(t);
            }
            new MinorLogMsg(this, "Error occured lazy-loading properties for PPSM Supporter " + this.getSubId() + ": " + t.getMessage(), t).log(ctx);
        }
        finally
        {
            pm.log(ctx);
        }

        return false;
    }    

    @Override
    public Set<String> getChargingTemplates()
    {
        return getChargingTemplates(getContext());
    }
    
    @Override
    public Set<String> getScreeningTemplates()
    {
        return getScreeningTemplates(getContext());
    }

    public Set<Long> getAddedChargingTemplates(Context ctx)
    {
        Set<Long> templates = new HashSet<Long>();
        if (super.getSavedChargingTemplates()!=null)
        {
            Set<String> saved = buildSet(getSavedChargingTemplates());
            for (String templateId : getChargingTemplates())
            {
                if (!saved.contains(templateId))
                {
                    templates.add(Long.valueOf(templateId));
                }
            }
        }
        return templates;
    }
    
    public Set<Long> getAddedScreeningTemplates(Context ctx)
    {
        Set<Long> templates = new HashSet<Long>();
        if (super.getSavedScreeningTemplates()!=null)
        {
            Set<String> saved = buildSet(getSavedScreeningTemplates());
            for (String templateId : getScreeningTemplates())
            {
                if (!saved.contains(templateId))
                {
                    templates.add(Long.valueOf(templateId));
                }
            }
        }
        return templates;
    }

    public Set<Long> getRemovedChargingTemplates(Context ctx)
    {
        Set<Long> templates = new HashSet<Long>();
        if (super.getSavedChargingTemplates()!=null)
        {
            Set<String> saved = buildSet(getSavedChargingTemplates());
            for (String templateId : saved)
            {
                if (!getChargingTemplates().contains(templateId))
                {
                        templates.add(Long.valueOf(templateId));
                }
            }
        }
        return templates;
    }

    public Set<Long> getRemovedScreeningTemplates(Context ctx)
    {
        Set<Long> templates = new HashSet<Long>();
        if (super.getSavedScreeningTemplates()!=null)
        {
            Set<String> saved = buildSet(getSavedScreeningTemplates());
            for (String templateId : saved)
            {
                if (!getScreeningTemplates().contains(templateId))
                {
                        templates.add(Long.valueOf(templateId));
                }
            }
        }
        return templates;
    }

    private Set<String> buildSet(String str)
    {
        Set<String> set = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
            set.add(st.nextToken());
        }
        return set;
    }

    
    private String buildString(Set set)
    {
        Object[] arr = set.toArray();
        StringBuilder buff = new StringBuilder();
        
        for (int x=0; x<arr.length; x++)
        {
            if (x!=0) buff.append(",");
            buff.append(arr[x]);
        }

        return buff.toString();
    }

    
    public static PPSMSupporterSubExtension getPPSMSupporterSubscriberExtension(Context ctx, String subscriberId) throws HomeException
    {
        Home home = (Home) ctx.get(PPSMSupporterSubExtensionHome.class);
        return (PPSMSupporterSubExtension) home.find(ctx, new EQ(PPSMSupporterSubExtensionXInfo.SUB_ID, subscriberId));
    }

    protected transient Context ctx_ = null;
}
