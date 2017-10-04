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
package com.trilogy.app.crm.notification;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.CreditEventTypeEnum;
import com.trilogy.app.crm.bean.CreditNotificationEvent;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.dunning.DunningProcessServer;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.DunningReportRecordXDBHome;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.dunning.config.DunningConfig;
import com.trilogy.app.crm.dunning.config.DunningConfigXInfo;
import com.trilogy.app.crm.dunning.notice.DunningNoticeAccountProcessor;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.app.crm.io.FileOutputStreamFactory;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.notification.template.BinaryNotificationTemplate;
import com.trilogy.app.crm.notification.template.EmailNotificationTemplate;
import com.trilogy.app.crm.notification.template.NotificationTemplate;
import com.trilogy.app.crm.notification.template.SmsNotificationTemplate;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.support.NotificationSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * This credit notice agent deals with credit notices for accounts that
 * may be warned/dunned/in arrears within day(s) from the running date.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class DunningNoticeLifecycleAgent extends AbstractCreditNoticeLifecycleAgent<Account>
{
    public DunningNoticeLifecycleAgent(Context ctx, String agentId)
            throws AgentException
    {
        super(ctx, agentId, AccountXInfo.CREDIT_CATEGORY);
    }
    
    /**
     * This method is designed to be overridden.  It provides the implementation class an opportunity to
     * customize the context that will be used for notification delivery.
     * 
     * @param ctx Operating Context
     * @param bean Bean which will have notifications delivered.
     * @return
     */
    protected Context wrapNotificationDeliveryContext(Context ctx, Account bean)
    {
    	
    		ctx.put("BAN" , ((Account)bean).getBAN() );
    	
    	return super.wrapNotificationDeliveryContext(ctx,bean);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanupNotificationEvents(Collection<CreditNotificationEvent> events)
    {
        super.cleanupNotificationEvents(events);
        if (events != null)
        {
            Iterator<CreditNotificationEvent> iter = events.iterator();
            while (iter.hasNext())
            {
                CreditNotificationEvent event = iter.next();
                switch (event.getEventTypeIndex())
                {
                case CreditEventTypeEnum.DUNNING_WARNING_INDEX:
                case CreditEventTypeEnum.DUNNING_DUNNED_INDEX:
                case CreditEventTypeEnum.DUNNING_IN_ARREARS_INDEX:
                    continue;
                default:
                    // This agent only supports dunning notifications
                    iter.remove();
                }
            }
        }
    }

    @Override
    protected List<KeyValueFeatureEnum> getKeyValueReplacementFeatures(Context ctx, CreditEventTypeEnum eventType)
    {
        List<KeyValueFeatureEnum> features = super.getKeyValueReplacementFeatures(ctx, eventType);
        features.add(KeyValueFeatureEnum.CREDIT_NOTICE);
        return features;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<NotificationTemplate> getTemplatesForCreditNotificationEvent(Context ctx, Account bean, CreditEventTypeEnum eventType, int occurrence)
    {   
        NotificationTypeEnum notificationType = null;
        switch (eventType.getIndex())
        {
        case CreditEventTypeEnum.DUNNING_WARNING_INDEX:
            notificationType = NotificationTypeEnum.WARNING_NOTICE;
            break;
        case CreditEventTypeEnum.DUNNING_DUNNED_INDEX:
            notificationType = NotificationTypeEnum.DUNNING_NOTICE;
            break;
        case CreditEventTypeEnum.DUNNING_IN_ARREARS_INDEX:
            notificationType = NotificationTypeEnum.IN_ARREARS_NOTICE;
            break;
        }

        Collection<NotificationTemplate> templates = null;
        
        try
        {
        	templates = NotificationSupportHelper.get(ctx).getTemplates(ctx, bean.getType(), bean.getLanguage(), occurrence, notificationType);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving " + notificationType + " templates for account " + bean.getBAN() + ". No " + eventType + " will be sent.", e).log(ctx);
        }
        
        return templates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RecipientInfo getRecipientInfo(Context ctx, Account bean, NotificationTemplate template)
    {
        RecipientInfo result = null;
        
        if (template instanceof EmailNotificationTemplate
                && bean.getEmailID() != null
                && bean.getEmailID().trim().length() > 0)
        {
            if (result == null)
            {
                result = new RecipientInfo();
            }
            
            EmailAddresses emailTo = new EmailAddresses();
            emailTo.setTo(Arrays.asList(new StringHolder[]{new StringHolder(bean.getEmailID())}));
            result.setEmailTo(emailTo );
        }
        
        if (template instanceof SmsNotificationTemplate)
        {
            Subscriber subscriber = null;
            
            try
            {
                subscriber = bean.getOwnerSubscriber(ctx);
                if (subscriber != null
                        && subscriber.hasServiceOfType(ctx, ServiceTypeEnum.SMS))
                {
                    if (result == null)
                    {
                        result = new RecipientInfo();
                    }
                    result.setSmsTo(subscriber.getMsisdn());
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error looking up owner subscriber for account " + bean.getBAN() + ".  No SMS notification will be sent.", e).log(ctx);
            }
        }

        if (template instanceof BinaryNotificationTemplate)
        {
            String filename = retrieveDunningNoticeFilename(ctx, bean);
            
            if (filename!=null)
            {
                FileOutputStreamFactory factory = new FileOutputStreamFactory();
                factory.setFilename(filename);
        
                if (result == null)
                {
                    result = new RecipientInfo();
                }
    
                result.setPostToGenerator(factory);
            }
        }
        
        return result;
    }
    
    private String retrieveDunningNoticeFilename(final Context ctx, final Account account)
    {
        String baseDir = null;
        try
        {
            DunningConfig cfg = HomeSupportHelper.get(ctx).findBean(ctx, DunningConfig.class, new EQ(DunningConfigXInfo.SPID, Integer.valueOf(account.getSpid())));
            if (cfg != null)
            {
                baseDir = cfg.getArchiveDirectory();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, SystemSupport.class, "Unable to retrieve dunning config for SPID " + account.getSpid() + ": " + e.getMessage());
        }
        
        if (baseDir!=null)
        {

            StringBuilder filePath = new StringBuilder();
            filePath.append(baseDir);
            
            if (!baseDir.endsWith("/"))
            {
                filePath.append("/");
            }
            
            String dunningDate = yyyy_MM_dd.format(new Date());
            
            filePath.append(dunningDate);
            filePath.append("/");
            
            filePath.append(dunningDate);
            filePath.append("-");
            filePath.append(account.getBAN());
            filePath.append(".pdf");
            
            String result = filePath.toString();
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this,
                        "Returning path for dunning notice. Account: " + account.getBAN() + ", Path: " + result);
            }
            
            return result;
        }
        else
        {
            LogSupport.minor(ctx, SystemSupport.class, "Unable to retrieve dunning config for SPID " + account.getSpid());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CreditNotificationInfo retrieveCreditNotificationInfo(Context ctx, Account bean, CreditNotificationEvent event)
    {
        CreditNotificationInfo result = new CreditNotificationInfo(ctx);

        DunningNoticeAccountProcessor processor = new DunningNoticeAccountProcessor(ctx, bean, event);
        result.setSendNotice(processor.sendNotice(ctx));
        
        if (result.sendNotice())
        {
            Context deliveryContext = ctx.createSubContext();
            deliveryContext.put(DunningReportRecord.class, processor.getDunningReportRecord());
            deliveryContext.put(AgedDebt.class, processor.getAgedDebt());
            deliveryContext.put(Subscriber.class, getPrimarySubscriber(ctx, bean));
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Account :");
                sb.append(bean.getBAN());
                sb.append(", DunningReportRecord : ");   
                sb.append(processor.getDunningReportRecord());
                sb.append(" Aged debt : ");
                sb.append(processor.getAgedDebt());
                sb.append(" Running Date : ");
                sb.append(CoreERLogger.formatERDateDayOnly(processor.getRunningDate()));
                LogSupport.debug(ctx, this, sb.toString());
            }
            result.setDeliveryContext(deliveryContext);
        }
        
        return result;
    }
    
    @Override
    protected Object getCustomFilter()
    {
    	Context ctx = getContext();
    	int spid = (Integer)ctx.get("SPID_ID");
    	LogSupport.debug(ctx, this, "SPID_ID"+ spid);
        return DunningProcessServer.getMainFilter(ctx,spid);
    }
    
    @Override
    protected Object getCustomFilter(final Context context, final CreditNotificationEvent event, final int creditCategoryId)
    {
        Object result = getCustomFilter();
        try
        {
            CreditCategory creditCategory = HomeSupportHelper.get(context).findBean(context, CreditCategory.class, new EQ(CreditCategoryXInfo.CODE, Integer.valueOf(creditCategoryId)));
            int spid = creditCategory.getSpid();
            context.put("SPID_ID",spid);
            
            Collection<DunningReport> reports = HomeSupportHelper.get(context).getBeans(context, DunningReport.class, new And().
                    add(new GTE(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(context).findDateDaysAfter(-event.getEventDateOffset(), new Date()))).
                    add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(spid))).
                    add(new NEQ(DunningReportXInfo.STATUS, Integer.valueOf(DunningReportStatusEnum.REFRESHING_INDEX))), 1, true, DunningReportXInfo.REPORT_DATE);
                
            if (reports!=null && reports.size()>0)
            {
                DunningReport report = reports.iterator().next();
                
                String tableName = MultiDbSupportHelper.get(context).getTableName(context,
                        DunningReportRecordHome.class,
                        DunningReportRecordXInfo.DEFAULT_TABLE_NAME);
                
                result = new EitherPredicate(True.instance(), (new SimpleXStatement("BAN IN (SELECT BAN FROM " + tableName + 
                        " WHERE REPORTDATE = "
                        + report.getReportDate().getTime() + " AND SPID = " + report.getSpid() + ")")));
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error looking up custom filter for event " + event + " and credit category "
                    + creditCategoryId + ": " + e.getMessage(), e).log(context);
        }
        
        return result;
    }

    /**
     * 
     * @param ctx
     * @param account
     * @return
     */
    public Subscriber getPrimarySubscriber(Context ctx, Account account)
    {
        Subscriber result = null;
        try
        {
            result = account.getOwnerSubscriber(ctx);
        }
        catch (Exception e)
        {
            StringBuffer msg = new StringBuffer();
            msg.append("Unable to find primary subscriber for account '");
            msg.append(account.getBAN());
            msg.append("': ");
            msg.append(e.getMessage());
            LogSupport.minor(ctx, this, msg.toString(), e);
        }

        return result;
    }

    public short getAccountState(com.redknee.framework.xhome.context.Context ctx,com.redknee.app.crm.bean.Account bean)
    {
    	return bean.getState().getIndex();
    }
    
    private static final SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");

}
