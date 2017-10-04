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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.security.Permission;
import java.util.List;
import java.util.TimeZone;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOptionXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.WebControlSupportHelper;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Performes Account/Subscriber View customizaton for Account beans.
 * 
 * TODO Replace this file using field level predicates on the Account XMenus.
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class AccountViewCustomizationWebControl extends SubscriberViewCustomizationWebControl
{
    public AccountViewCustomizationWebControl(WebControl delegate)
    {
        super(delegate);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context subCtx = ctx.createSubContext();
        Account account = (Account)obj;
        configureAccountView(subCtx, account);
        warnChangeInPaymentPlan(subCtx, out, account);

        // Putting account time zone in the context.
        subCtx.put(TimeZone.class, TimeZone.getTimeZone(account.getTimeZone(ctx)));

        int mode = subCtx.getInt("MODE", DISPLAY_MODE);
        if( mode == CREATE_MODE )
        {
            // Set fields as read-only if they are marked as mandatory in the template
            protectMandatoryACTFields(subCtx, account);

            if (account.isIndividual(ctx) && !account.isRootAccount())
            {
                customResponsibleInPooledAcount(subCtx, account);
            }
        }

        if( AccountStateEnum.INACTIVE.equals(account.getState()) )
        {
            PropertyInfo extensionProperty = AccountXInfo.ACCOUNT_EXTENSIONS;
            WebControlSupportHelper.get(subCtx).setPropertyReadOnly(subCtx, extensionProperty);
        }

        super.toWeb(subCtx, out, name, obj);
    }

    
    protected void configureAccountView(Context ctx, Account account)
    {
        customizeAccountHierachy(ctx, account);
        customizePromiseToPayDate(ctx, account);
        customizePaymentPlan(ctx, account);
        customizeMomType(ctx, account);
        configureSubscriberView(ctx, account);
    }
    
    
    /**
     * Performs Account Hierachy View customizations.
     */
    protected void customizeAccountHierachy(Context ctx, Account account)
    {        
        if (!SystemSupport.supportsAccountHierachy(ctx))
        {
            //system support hierachy
            WebControlSupportHelper.get(ctx).hideProperties(ctx, new PropertyInfo[]
            {
                AccountXInfo.RESPONSIBLE,
                AccountXInfo.PARENT_BAN
            });
        }
        else
        {
            if (ctx.getInt("MODE", DISPLAY_MODE) != CREATE_MODE)
            {
                WebControlSupportHelper.get(ctx).setPropertiesReadOnly(ctx, new PropertyInfo[]
                {
                    AccountXInfo.RESPONSIBLE,
                    AccountXInfo.PARENT_BAN
                });
            }

            if (!account.isResponsible())
            {
                WebControlSupportHelper.get(ctx).hideProperty(ctx, AccountXInfo.TAX_EXEMPTION);                
            }
            
            if (!account.isRootAccount())
            {
                // child billcycle should be the same as parents.
                WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, AccountXInfo.BILL_CYCLE_ID);

                // copy the value from parent if creating the bean
                if (ctx.getInt("MODE", DISPLAY_MODE) == CREATE_MODE)
                {
                    try
                    {
                        account.setBillCycleID(account.getParentAccount(ctx).getBillCycleID());
                    }
                    catch (HomeException e)
                    {
                        LogSupport.debug(ctx, this, "Unable to retreive Parent Account " + account.getParentBAN(), e);
                    }
                    
                    if (!account.isResponsible())
                    {
                        // For accounts within a hierarchy that are non-responsible, set the Invoice Delivery Option to system configured default or "NONE"
                        // Set to default of 3 (None), which is the non-responsible default in the default journal (app-crm-core-data.sch)
                        long nonResponsibleDefault = 3L;
                        try
                        {
                            InvoiceDeliveryOption defaultOption = HomeSupportHelper.get(ctx).findBean(ctx, 
                                    InvoiceDeliveryOption.class, 
                                    new EQ(InvoiceDeliveryOptionXInfo.NON_RESPONSIBLE_DEFAULT, true));
                            if (defaultOption != null)
                            {
                                nonResponsibleDefault = defaultOption.getId();
                            }
                        }
                        catch (HomeException e)
                        {
                            //Failed to retrieve the Default delivery option, set with default
                            new MinorLogMsg(this, "Failed to retrieve default non-responsible invoice delivery option.  Using system default value of " + nonResponsibleDefault, e).log(ctx);
                        }
                        account.setInvoiceDeliveryOption(nonResponsibleDefault);
                    }
                }
            }
        }
    }
    
    
    protected void customizePromiseToPayDate(Context ctx, Account account)
    {
        if (AccountStateEnum.PROMISE_TO_PAY.equals(account.getState()))
        {
            if ( !account.isResponsible() )
            {
                WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, AccountXInfo.PROMISE_TO_PAY_DATE);
            }
        }
        else
        {
            WebControlSupportHelper.get(ctx).hideProperty(ctx, AccountXInfo.PROMISE_TO_PAY_DATE);
        }
    }
    
    
    protected void customizePaymentPlan(Context ctx, Account account)
    {
        final Permission PAYMENT_PLAN_READ = new SimplePermission("");
        final Permission PAYMENT_PLAN_WRITE = new SimplePermission("app.crm.paymentplan.switch");
    
        /* [Angie] 2005/12/21: Originally the app.crm.paymentplan.switch permission was used to control who has 
         * access to remove Accounts from Payment Plan. However, requirements now want the ability to remove/add 
         * accounts to payment plan to be bundled. This means that CRM Agents (for example) will not have the ability */
        AbstractWebControl.setMode(
                ctx, 
                AccountXInfo.PAYMENT_PLAN, 
                new ContextFactory()
                {
                    @Override
                    public Object create(Context fCtx)
                    {
                        return AbstractWebControl.check(fCtx, PAYMENT_PLAN_READ, PAYMENT_PLAN_WRITE);
                    }
                });
    }
    
    
    /**
     * Display Warning message box when the given account is entering Payment Plan.
     * Only perform the check when Payment Plan is Licensed.
     */
    protected void warnChangeInPaymentPlan(Context ctx, PrintWriter out, Account account)
    {
        if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx)
                && account.getPaymentPlan() != PaymentPlanSupport.INVALID_PAYMENT_PLAN_ID 
                && AccountSupport.hasPaymentPlanChanged(ctx, account) )
        {
            String warningHeader = "Payment Plan"; 
            String warningMsg = "You are about to install this account on a Payment Plan.<br>"
                + "If you do not wish to continue,<br>" 
                + "please select --- from the Payment Plan drop down. ";
            
			out.println("<table width=\"100%\"><tbody><tr><td><center>");
			out.println("<table dir=\"ltr\" rules=\"all\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" bordercolor=\"#003366\" bgcolor=\"#003565\">");
			out.println("<tbody>");
			out.println("<tr><th><font color=\"#FFFFFF\">Warning</font></th></tr>");
			out.println("<tr><td>");
			out.println("<table bgcolor=\"#efefef\" border=\"0\" bordercolor=\"#003366\" cellpadding=\"3\" cellspacing=\"3\" width=\"100%\"><tbody>");
            out.println("<tr bgcolor=\"#E3E3E3\"><td><font color=\"red\"><b>" + warningHeader + "</b></font></td>");
            out.println("<td>" + warningMsg + "</td>");
			out.println("</tr></tbody></table>");
			out.println("</td></tr></tbody></table>");
			out.println("</center></td></tr></tbody></table>");
        }
    }
    
    
    protected void customizeMomType(Context ctx, Account account)
    {        
        if (ctx.getInt("MODE", DISPLAY_MODE) != CREATE_MODE)
        {
            if (account.isVpn() || account.isIcm())
            {
                WebControlSupportHelper.get(ctx).setPropertiesReadOnly(ctx, new PropertyInfo[]
                {
                    AccountXInfo.VPN,
                    AccountXInfo.ICM
                });
            }
        }
    }


    protected void configureSubscriberView(Context ctx, Account account)
    {
        hideAccountRelatedProperties(ctx);

        // Hide properties that are not applicable to prepaid subscribers
        if ( SafetyUtil.safeEquals(SubscriberTypeEnum.PREPAID, account.getSystemType()) )
        {
            hidePostpaidProperties(ctx);
        }

        // Hide properties that are not applicable to postpaid subscribers
        if ( SafetyUtil.safeEquals(SubscriberTypeEnum.POSTPAID, account.getSystemType()) )
        {
            hidePrepaidProperties(ctx);
        }

		if (!account.isPooled(ctx))
        {
            hideQuotaProperties(ctx);
        }
    }

    protected void customResponsibleInPooledAcount(Context ctx, Account account)
    {
        try
        {
            final Account parentAccount = account.getParentAccount(ctx);
            if (parentAccount == null)
            {
                LogSupport.debug(ctx, this, "Unable to determine parent account");
            }
            else
            {
                if (parentAccount.isPooled(ctx))
                {
                    account.setResponsible(false);
                    WebControlSupportHelper.get(ctx).setPropertyReadOnly(ctx, AccountXInfo.RESPONSIBLE);
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Unable to determine parent account", e);
        }
    }

    /**
     * Sets the view-mode of mandatory fields (as indicated in the ACT) to read-only in the account.
     *
     * @param ctx The operating context
     * @param account Account
     */
    private void protectMandatoryACTFields(Context ctx, Account account)
    {
        long actId = account.getActId();
        if( actId != -1 )
        {
            Home actHome = (Home)ctx.get(AccountCreationTemplateHome.class);
            AccountCreationTemplate act = null;
            try
            {
                act = (AccountCreationTemplate)actHome.find(ctx, Long.valueOf(actId));
            }
            catch (HomeException e)
            {
                new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + AccountViewCustomizationWebControl.class.getSimpleName() + ".toWeb(): " + e.getMessage(), e).log(ctx);
            }
            if( act != null )
            {
                List<StringHolder> mandatoryFields = act.getMandatoryFields();
                for( StringHolder fieldName : mandatoryFields )
                {
                    AbstractWebControl.setMode(ctx, AccountXInfo.instance().getName() + "." + fieldName.getString(), ViewModeEnum.READ_ONLY);
                }
            }
        }
    }
}
