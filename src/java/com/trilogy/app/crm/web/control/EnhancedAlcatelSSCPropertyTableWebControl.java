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
import java.util.Collection;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.AlcatelSSCProperty;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueEntry;
import com.trilogy.app.crm.bean.KeyValueEntryID;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.bean.webcontrol.CRMAlcatelSSCPropertyTableWebControl;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtension;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.KeyValueSupportHelper;

/**
 * AlcatelSSCPropertyTableWebControl which displays existing keys as read-only and does not allow
 * removal of mandatory keys.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class EnhancedAlcatelSSCPropertyTableWebControl extends CRMAlcatelSSCPropertyTableWebControl
{
    public EnhancedAlcatelSSCPropertyTableWebControl()
    {
    }
    
    WebControl key_wc_ = new ProxyWebControl(super.getKeyWebControl())
    {
        /**
         * Make the key field for all existing keys display only
         * 
         * {@inheritDoc}
         */
        @Override
        public void toWeb(Context ctx, final PrintWriter out, final String name, final Object obj)
        {
            Object parentBean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, AlcatelSSCSpidExtension.class);
            if (parentBean == null)
            {
                parentBean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, AlcatelSSCServiceExtension.class);
            }
            if (parentBean == null)
            {
                parentBean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, AlcatelSSCSubscriberExtension.class);
            }
            
            KeyValueEntryID entryID = null;
            if (parentBean instanceof AlcatelSSCSpidExtension)
            {
                entryID = new KeyValueEntryID(
                        AlcatelSSCSpidExtension.class.getName(),
                        String.valueOf(((AlcatelSSCSpidExtension)parentBean).getSpid()),
                        String.valueOf(obj));
            }
            else if (parentBean instanceof AlcatelSSCServiceExtension)
            {
                entryID = new KeyValueEntryID(
                        AlcatelSSCServiceExtension.class.getName(),
                        String.valueOf(((AlcatelSSCServiceExtension)parentBean).getServiceId()),
                        String.valueOf(obj));
            }
            else if (parentBean instanceof AlcatelSSCSubscriberExtension)
            {
                entryID = new KeyValueEntryID(
                        AlcatelSSCSubscriberExtension.class.getName(),
                        ((AlcatelSSCSubscriberExtension)parentBean).getSubId(),
                        String.valueOf(obj));
            }

            boolean keyExists = false;
            try
            {
                if (entryID != null)
                {
                    keyExists = HomeSupportHelper.get(ctx).findBean(ctx, KeyValueEntry.class, entryID) != null;
                }
            }
            catch (HomeException e)
            {
                // NOP
            }
            
            if (keyExists)
            {
                ctx = ctx.createSubContext();
                
                ctx.put("MODE", DISPLAY_MODE);

                out.print("<input type=\"hidden\" name=\"");
                out.print(name);
                out.print("\" value=\"" + obj + "\" />");
            }
            
            super.toWeb(ctx, out, name, obj);
        }
    };

    WebControl value_wc_ = new ProxyWebControl(value_wc)
    {
        /**
         * Make default value read-only
         * {@inheritDoc}
         */
        @Override
        public void toWeb(Context ctx, final PrintWriter out, final String name, final Object obj)
        {
            if (AlcatelSSCProperty.DEFAULT_VALUE.equals(obj))
            {
                ctx = ctx.createSubContext();
                ctx.put("MODE", DISPLAY_MODE);
                super.toWeb(ctx, out, name, "");
            }
            else
            {
                super.toWeb(ctx, out, name, obj);
            }
        }
    };

    @Override
    public WebControl getKeyWebControl()
    {
        return key_wc_;
    }

    @Override
    public WebControl getValueWebControl()
    {
        return value_wc_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void outputCheckBox(Context ctx, PrintWriter out, String name, Object bean, boolean isChecked)
    {
        if (!isChecked)
        {
            super.outputCheckBox(ctx, out, name, bean, isChecked);
            return;
        }
        
        AlcatelSSCProperty prop = (AlcatelSSCProperty) bean;

        Object parentBean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, AlcatelSSCSpidExtension.class);
        if (parentBean == null)
        {
            parentBean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, AlcatelSSCServiceExtension.class);
        }
        if (parentBean == null)
        {
            parentBean = BeanLoaderSupportHelper.get(ctx).getBean(ctx, AlcatelSSCSubscriberExtension.class);
        }
        
        Collection<KeyConfiguration> mandatoryKeys = null;
        if (parentBean instanceof AlcatelSSCSpidExtension)
        {
            mandatoryKeys = KeyValueSupportHelper.get(ctx).getConfiguredKeys(ctx, false, KeyValueFeatureEnum.ALCATEL_SSC_SPID);
        }
        else if (parentBean instanceof AlcatelSSCServiceExtension)
        {
            mandatoryKeys = KeyValueSupportHelper.get(ctx).getConfiguredKeys(ctx, false, KeyValueFeatureEnum.ALCATEL_SSC_SERVICE);
        }
        else if (parentBean instanceof AlcatelSSCSubscriberExtension)
        {
            mandatoryKeys = KeyValueSupportHelper.get(ctx).getConfiguredKeys(ctx, false, KeyValueFeatureEnum.ALCATEL_SSC_SUBSCRIPTION);
        }
        
        boolean found = false;
        if (mandatoryKeys != null)
        {
            for (KeyConfiguration mandatoryKey : mandatoryKeys)
            {
                if (SafetyUtil.safeEquals(mandatoryKey.getKey(), prop.getKey()))
                {
                    out.print("<td><input type=\"checkbox\" name=\"X");
                    out.print(name);
                    out.print("X\" disabled value=\"X\" checked=\"checked\" /> ");

                    out.print("<input type=\"hidden\" name=\"");
                    out.print(name);
                    out.print(SEPERATOR);
                    out.print("_enabled\" value=\"X\" /> </td>");
                    found = true;
                    break;
                }
            }
        }
        
        if (!found)
        {
            super.outputCheckBox(ctx, out, name, bean, isChecked);
        }
    }
    
}
