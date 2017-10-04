/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.BooleanWebControl;
import com.trilogy.framework.xhome.webcontrol.CheckBoxWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * This web control adds a default/mandatory value state to an existing field.
 * It is intended for use in template-like beans.  For example, we might have
 * a template that is used to set some initial values of a bean.  The initial
 * values can be treated as 'default' values (values that are set initially
 * but can be changed later), or they can be treated as 'mandatory' values
 * (values that MUST appear as they do in the template and are read-only.
 *
 * The mandatory fields are stored in a List within the parent bean.  The list
 * is retrieved using the mandatoryListPropertyInfo, as provided by the caller.
 * 
 * The mandatory list must contain beans of type:
 *     com.redknee.framework.xhome.holder.StringHolder
 *
 * @author Aaron Gourley
 * @since 7.4.16
 */
public class MandatoryFieldWebControl extends ProxyWebControl
{
    protected final PropertyInfo myPropertyInfo_;
    protected final PropertyInfo mandatoryListPropertyInfo_;

    public MandatoryFieldWebControl(WebControl delegate, PropertyInfo myPropertyInfo, PropertyInfo mandatoryListPropertyInfo)
    {
        this(delegate, myPropertyInfo, mandatoryListPropertyInfo, null, null);
    }

    public MandatoryFieldWebControl(WebControl delegate, PropertyInfo myPropertyInfo, PropertyInfo mandatoryListPropertyInfo, String mandatoryText, String defaultText)
    {
        super(delegate);
        
        myPropertyInfo_ = myPropertyInfo;
        mandatoryListPropertyInfo_ = mandatoryListPropertyInfo;
        if( mandatoryText != null && defaultText != null )
        {
            trueFalseWc_ = new BooleanWebControl(mandatoryText, defaultText);
            defaultPreLabel_ = "&nbsp;";
            defaultPostLabel_ = "&nbsp;";
        }
        else
        {
            trueFalseWc_ = CheckBoxWebControl.instance();
            defaultPreLabel_ = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            defaultPostLabel_ = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        }
    }
    
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        
        Object bean = ctx.get(AbstractWebControl.BEAN);
        
        out.println("");

        List<StringHolder> mandatoryList = (List<StringHolder>)mandatoryListPropertyInfo_.get(bean);
        if( mandatoryList != null
                && (mode == EDIT_MODE || mode == CREATE_MODE) )
        {
            MessageMgr mmgr = new MessageMgr(ctx, this);
            out.print(mmgr.get(MandatoryFieldWebControl.class.getSimpleName() + ".mandatoryPreLabel", defaultPreLabel_));
            trueFalseWc_.toWeb(ctx, out, name + SEPERATOR + "mandatory", mandatoryList.contains(new StringHolder(myPropertyInfo_.getName())));
            out.print(mmgr.get(MandatoryFieldWebControl.class.getSimpleName() + ".mandatoryPostLabel", defaultPostLabel_)); 
        }
        super.toWeb(ctx, out, name + SEPERATOR + "data", obj);
    }
    
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Object bean = ctx.get(AbstractWebControl.BEAN);

        List<StringHolder> mandatoryList = (List<StringHolder>)mandatoryListPropertyInfo_.get(bean);
        if( mandatoryList != null )
        {
            Boolean isMandatory = (Boolean)CheckBoxWebControl.instance().fromWeb(ctx, req, name + SEPERATOR + "mandatory");
            if( isMandatory )
            {
                mandatoryList.add(new StringHolder(myPropertyInfo_.getName()));
            }
            else
            {
                mandatoryList.remove(new StringHolder(myPropertyInfo_.getName()));   
            }
        }

        return getDelegate().fromWeb(ctx, req, name + SEPERATOR + "data");
    }
    
    protected final WebControl trueFalseWc_;
    protected final String defaultPreLabel_;
    protected final String defaultPostLabel_;
}
