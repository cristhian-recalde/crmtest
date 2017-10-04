/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.PackageBatchConfigruation;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.util.snippet.webcontrol.dynamic.text.BlackListEffect;
import com.trilogy.util.snippet.webcontrol.dynamic.text.EffectColor;
import com.trilogy.util.snippet.webcontrol.dynamic.text.MaxLengthEffect;
import com.trilogy.util.snippet.webcontrol.dynamic.text.MinLengthEffect;
import com.trilogy.util.snippet.webcontrol.dynamic.text.RegexMatchingEffect;
import com.trilogy.util.snippet.webcontrol.dynamic.text.TextFieldDynamicWebControl;


public class PackageBatchIdDynamicTextWebCotrol extends TextFieldDynamicWebControl
{

    @Override
    public List<Effects> getListOfEffects(Context ctx, String name, Object obj)
    {
        List<Effects> effects = new ArrayList<Effects>();
        try
        {
            
            effects.add(new RegexMatchingEffect(EffectColor.RED)
            {

                @Override
                public String getPatternRegExToMatch(Context ctx, String name, Object obj)
                {
                    PackageBatchConfigruation config = (PackageBatchConfigruation)ctx.get(PackageBatchConfigruation.class);
                    if(null != config)
                    {
                        return config.getBatchIdRegexPattern();
                    }else
                    {
                        return PackageBatchConfigruation.DEFAULT_BATCHIDREGEXPATTERN; 
                    }
                    
                }
                
                @Override
                public String getMessageOnMismatch(Context ctx, String name, Object obj)
                {
                    return "Expected pattern " + getPatternRegExToMatch(ctx, name, obj);
                }
            });
            
            effects.add(new MaxLengthEffect(EffectColor.RED)
            {

                @Override
                public String getMessageOnExceedLength(Context ctx, String name, Object bean)
                {
                    return "Expected Max Length is " + getMaxLength(ctx, name, bean);
                }


                @Override
                public long getMaxLength(Context ctx, String name, Object bean)
                {
                    PackageBatchConfigruation config = (PackageBatchConfigruation) ctx
                            .get(PackageBatchConfigruation.class);
                    if (null != config)
                    {
                        return config.getBatchIdMaxLength();
                    }
                    else
                    {
                        return PackageBatchConfigruation.DEFAULT_BATCHIDMAXLENGTH;
                    }
                }
            });
            
            effects.add(new MinLengthEffect(EffectColor.RED)
            {

                @Override
                public String getMessageOnShortLength(Context ctx, String name, Object bean)
                {
                    return "Expected Min Length is " + getMinLength(ctx, name, bean);
                }


                @Override
                public long getMinLength(Context ctx, String name, Object bean)
                {
                    PackageBatchConfigruation config = (PackageBatchConfigruation) ctx
                            .get(PackageBatchConfigruation.class);
                    if (null != config)
                    {
                        return config.getBatchIdMinLength();
                    }
                    else
                    {
                        return PackageBatchConfigruation.DEFAULT_BATCHIDMINLENGTH;
                    }
                }
            });
            
            effects.add(new BlackListEffect(EffectColor.RED)
            {
                
                @Override
                public Map<String, String> getBlackListEntries(Context ctx, String name, Object obj)
                {
                    final Map<String, String> blackListEntries = new HashMap<String, String>();
                    fillInMapOfExistingBatchIds(ctx, blackListEntries);
                    return blackListEntries;
                }
            });
            
            return effects;
            
            
        }
        catch (Throwable t)
        {
            handleException(ctx,"Error in creting dynamic text effects", t);
        }
        return effects;
    }


    private Map<String, String> fillInMapOfExistingBatchIds(Context ctx, final Map<String, String> blackList)
    {
        // try
        // {
        // Home home = (Home) ctx.get(PackageBulkLoadHome.class);
        // home.forEach(ctx, new Visitor()
        // {
        //
        // private static final long serialVersionUID = 1L;
        //
        //
        // @Override
        // public void visit(Context ctx, Object bean) throws AgentException,
        // AbortVisitException
        // {
        // blackList.put(((PackageBulkLoad) bean).getBatchId(), "Already exists");
        // }
        // });
        // }
        // catch (Throwable t)
        // {
        // handleException(ctx, t);
        // }
        return blackList;
    }


    private void handleException(Context ctx, Throwable t)
    {
        ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != excl)
        {
            excl.thrown(t);
        }
        new MajorLogMsg(this, t.getMessage(), t).log(ctx);
    }


    private void handleException(Context ctx, String message, Throwable t)
    {
        handleException(ctx, new IllegalStateException(message, t));
    }
}
