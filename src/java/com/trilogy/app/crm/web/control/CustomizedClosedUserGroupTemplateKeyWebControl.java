package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CUGTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateKeyWebControl;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateXInfo;
import com.trilogy.app.crm.home.ClosedUserGroupTemplateServiceHome;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


public class CustomizedClosedUserGroupTemplateKeyWebControl extends ClosedUserGroupTemplateKeyWebControl
{

    protected PropertyInfo cugTemplateIDProperty_;
    
    protected CUGTypeEnum cugType_;

    public CustomizedClosedUserGroupTemplateKeyWebControl(PropertyInfo cugTemplateIDProperty)
    {
        super();
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(boolean autoPreview, PropertyInfo cugTemplateIDProperty)
    {
        super(autoPreview);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, PropertyInfo cugTemplateIDProperty)
    {
        super(listSize);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, boolean autoPreview, PropertyInfo cugTemplateIDProperty)
    {
        super(listSize, autoPreview);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, PropertyInfo cugTemplateIDProperty)
    {
        super(listSize, autoPreview, isOptional);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom, PropertyInfo cugTemplateIDProperty)
    {
        super(listSize, autoPreview, isOptional, allowCustom);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, PropertyInfo cugTemplateIDProperty)
    {
        super(listSize, autoPreview, optionalValue);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }
    
    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, PropertyInfo cugTemplateIDProperty, CUGTypeEnum cugType)
    {
        super(listSize, autoPreview, optionalValue);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
        this.cugType_ = cugType;        
    }

    public CustomizedClosedUserGroupTemplateKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom, PropertyInfo cugTemplateIDProperty)
    {
        super(listSize, autoPreview, optionalValue, allowCustom);
        this.cugTemplateIDProperty_ = cugTemplateIDProperty;
    }

    public void toWeb(Context ctx, PrintWriter out, String name, final Object obj)
    {   
        Context subCtx = filterInvalidCugTypeClosedUserGroupTemplates(ctx);
        subCtx = filterDeprecatedClosedUserGroupTemplates(subCtx);
        subCtx = sortClosedUserGroupTemplates(subCtx);
        subCtx.put(ClosedUserGroupTemplateServiceHome.ONLY_SPID_BASED_SELECT, Boolean.TRUE);
        super.toWeb(subCtx, out, name, obj);       
    }
    
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
    	Context subCtx = ctx.createSubContext();
    	subCtx.put(ClosedUserGroupTemplateServiceHome.ONLY_SPID_BASED_SELECT, Boolean.TRUE);
    	return super.fromWeb(subCtx, req, name);    	
    }

    /**
     * 
     * @param originalContext_
     * @return a sub context, and a filtered closed user group template home in context.
     * 
     */
    public Context sortClosedUserGroupTemplates(final Context ctx)
    {
        Context subContext = ctx.createSubContext();

        final Home originalHome = (Home) subContext.get(ClosedUserGroupTemplateHome.class);
        final Home newHome = new SortingHome(subContext, originalHome);
        subContext.put(ClosedUserGroupTemplateHome.class, newHome);            

        return subContext;
    }
 
    public Context filterDeprecatedClosedUserGroupTemplates(final Context ctx)
    {
        Context subContext = ctx.createSubContext();
        Object obj = ctx.get(AbstractWebControl.BEAN);
        Long cugTemplateID = null;
        
        if (cugTemplateIDProperty_ != null)
        {
            cugTemplateID = (Long) cugTemplateIDProperty_.get(obj);
        }
        
        int spid = ((SpidAware) obj).getSpid();
        
        if(spid < 0)
        {
            // Hack to fetch spid if it is not available in the bean. This hack is done for FNF extension.
            // A clean fix should be given for this.
            Account act = (Account) ctx.get(Account.class);
            if(act != null)
            {
                spid = act.getSpid();
            }
        }
        
        Predicate filter = null;
        
        if (spid > 0)
        {
            filter = new And().add(new EQ(ClosedUserGroupTemplateXInfo.DEPRECATED, Boolean.FALSE)).add(
                    new EQ(ClosedUserGroupTemplateXInfo.SPID, spid));
        }
        else
        {
            filter = new EQ(ClosedUserGroupTemplateXInfo.DEPRECATED, Boolean.FALSE);
        }
        
        // Adding selected CUG Template, if it's deprecated.
        if ((cugTemplateID != null) && (cugTemplateID.longValue()>=0))
        {
                filter = new Or().add(filter).add(new EQ(ClosedUserGroupTemplateXInfo.ID, cugTemplateID));
        }
        
        if(cugType_ != null)
        {
            filter = new And().add(filter).add(new EQ(ClosedUserGroupTemplateXInfo.CUG_TYPE, cugType_));
        }
        
        final Home originalHome = (Home) subContext.get(ClosedUserGroupTemplateHome.class);
        final Home newHome = new HomeProxy(subContext, originalHome).where(subContext, filter);
        subContext.put(ClosedUserGroupTemplateHome.class, newHome);            

        return subContext;
    }
    /**
     * 
     * @param originalContext_
     * @return a sub context, and a filtered closed user group template home in context.
     * 
     */
    public Context filterInvalidCugTypeClosedUserGroupTemplates(final Context ctx)
    {
        Object obj = ctx.get(AbstractWebControl.BEAN);
        if (obj instanceof ClosedUserGroup)
        {
            try
            {
                Context subContext = ctx.createSubContext();
                Home home = (Home) ctx.get(ClosedUserGroupHome.class);
                ClosedUserGroup cug = (ClosedUserGroup) obj;
                ClosedUserGroup oldCug = null;
                if (cug.getID() != 0)
                {
                    oldCug = ClosedUserGroupSupport.getCUG(subContext, cug.getID(), cug.getSpid());
                }
                if (oldCug!=null)
                {
                    CUGTypeEnum cugType = cug.getCugType(ctx);
                    Predicate filter = new EQ(ClosedUserGroupTemplateXInfo.CUG_TYPE, cugType);
                    final Home originalHome = (Home) subContext.get(ClosedUserGroupTemplateHome.class);
                    final Home newHome = new HomeProxy(subContext, originalHome).where(subContext, filter);
                    subContext.put(ClosedUserGroupTemplateHome.class, newHome);            
                    return subContext;
                }
            }
            catch (Throwable e)
            {
                //Ignored
            }
        }
        return ctx;
    }    

}