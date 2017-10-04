package com.trilogy.app.crm.home;

import java.lang.reflect.Constructor;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XDBHome;
import com.trilogy.framework.xhome.xdb.XDBMgr;
import com.trilogy.framework.xhome.xdb.XDBSupport;
import com.trilogy.framework.xlog.log.LogSupport;

@SuppressWarnings("serial")
public class XDBAliasHome extends HomeProxy
{
    protected String alias;
    
    public XDBAliasHome(Context ctx, String alias, Home delegate)
    {
        super(ctx, delegate);
        
        this.alias = alias;
    }

    @SuppressWarnings("unchecked")
    public XDBAliasHome(Context ctx, Class beanType, String alias, String tableName)
    {
        this.alias = alias;
        Context xdbCtx = ctx.createSubContext(alias + " Subcontext");
        XDBSupport.putXDBAlias(xdbCtx, alias); 
        super.setContext(xdbCtx);
        
        Home home = createXDBHome(xdbCtx, beanType, tableName);
        super.setDelegate(home);
    }

    @SuppressWarnings("unchecked")
    private Home createXDBHome(Context ctx, Class beanType, String tableName)
    {
        Home home = null;
        
        Class xdbHome = XBeans.getClass(ctx, beanType, XDBHome.class);
        try
        {
            Constructor constructor = xdbHome.getConstructor(Context.class, String.class);
            home = (Home)constructor.newInstance(ctx, tableName);
        }
        catch (Exception e)
        {
            LogSupport.crit(ctx, this, "XDBAliasHome failed to inilialize XDB home.", e);
        }
        
        return home;
    }

    @Override
    public Object cmd(Context ctx, Object arg) throws HomeException, HomeInternalException
    {
        return super.cmd(getAliasContext(ctx), arg);
    }

    private Context getAliasContext(Context ctx)
    {
        Context xdbCtx = ctx.createSubContext();
        xdbCtx.put(XDBMgr.XDB_ALIAS, getAlias());
        return xdbCtx;
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        return super.create(getAliasContext(ctx), obj);
    }

    @Override
    public void drop(Context ctx) throws HomeException, HomeInternalException, UnsupportedOperationException
    {
        super.drop(getAliasContext(ctx));
    }

    @Override
    public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        return super.find(getAliasContext(ctx),obj);
    }

    @Override
    public Visitor forEach(Context ctx, Visitor visitor, Object where) throws HomeException, HomeInternalException
    {
        return super.forEach(getAliasContext(ctx), visitor, where);
    }

    @Override
    public void remove(Context ctx, Object bean) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        super.removeAll(getAliasContext(ctx), bean);
    }

    @Override
    public void removeAll(Context ctx, Object where) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        super.removeAll(getAliasContext(ctx),where);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection select(Context ctx, Object obj) throws HomeException, HomeInternalException,
            UnsupportedOperationException
    {
        return super.select(getAliasContext(ctx),obj);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        return super.store(getAliasContext(ctx),obj);
    }

    @Override
    public Home where(Context ctx, Object where)
    {
        return super.where(getAliasContext(ctx), where);
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }
}
