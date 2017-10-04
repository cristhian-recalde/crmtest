package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xhome.msp.SpidAwareXInfo;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.CountXProjection;
import com.trilogy.framework.xhome.xdb.MaxXProjection;
import com.trilogy.framework.xhome.xdb.MinXProjection;
import com.trilogy.framework.xhome.xdb.ProjectionProxy;
import com.trilogy.framework.xhome.xdb.SumXProjection;
import com.trilogy.framework.xhome.xdb.XProjection;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Select SPIDAware beans + shared beans
 * 
 * @author sbanerjee
 *
 */
public class SharedBeanGenericSelectionHome<BEAN extends SpidAware>
    extends HomeProxy
    implements Home
{

    public SharedBeanGenericSelectionHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Collection select(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        final Collection<BEAN> origList =  super.select(ctx, obj);
        Collection<BEAN> ret = getShareBeansAddedCollection(ctx, origList, obj);
        
        return ret;
    }


    /**
     * @param ctx
     * @param origList
     * @param obj other predicates
     * @return
     * @throws HomeException
     * @throws HomeInternalException
     * @throws UnsupportedOperationException
     */
    public Collection<BEAN> getShareBeansAddedCollection(Context ctx,
            final Collection<BEAN> origList, Object obj) throws HomeException,
            HomeInternalException, UnsupportedOperationException
    {
        Collection<BEAN> ret = new ArrayList();
        
        if(origList!=null)
            ret.addAll(origList);
        
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            Home newHome = findNewHomeForSharedBeans(ctx);
            
            if(newHome!=null)
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, this, "Fetching shared beans... from Home: "+ newHome);
                final int defaultSharedSpid = GeneralConfigSupport.getDefaultSharedSpid(ctx);
                Collection<BEAN> newValues = newHome.where(ctx, obj).select(ctx, new EQ(SpidAwareXInfo.SPID, Integer.valueOf( defaultSharedSpid)));
                
                if(newValues!=null)
                    ret.addAll(newValues);
            }
        }
        return ret;
    }

    /**
     * @param ctx
     * @return
     */
    protected Home findNewHomeForSharedBeans(Context ctx)
    {
        Home newHome = Homes.find(ctx, this, SpidAwareHome.class);
        newHome = newHome instanceof SpidAwareHome && ((SpidAwareHome)newHome).getDelegate() != null ? 
                ((SpidAwareHome)newHome).getDelegate() : 
                    null;
        return newHome;
    }
    
    @Override
    public Object find(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        Object origBean = super.find(ctx, obj);
        
        if(origBean!=null)
            return origBean;
        
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            Home newHome = findNewHomeForSharedBeans(ctx);
            
            if(newHome!=null)
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, this, "Finding in shared beans... from Home: "+ newHome);
                final int defaultSharedSpid = GeneralConfigSupport.getDefaultSharedSpid(ctx);
                BEAN sharedBean = (BEAN)newHome.find(ctx, obj);
                
                if(sharedBean!=null && sharedBean.getSpid()==defaultSharedSpid)
                    return sharedBean;
            }
        }
        
        return origBean;
    }
    
    @Override
    public Visitor forEach(Context ctx, Visitor visitor, Object where)
            throws HomeException, HomeInternalException
    {
        final Visitor origVisit = super.forEach(ctx, visitor, where);
        
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            Home newHome = findNewHomeForSharedBeans(ctx);
            if(newHome!=null)
            {
                final int defaultSharedSpid = GeneralConfigSupport.getDefaultSharedSpid(ctx);
                final Visitor v = newHome.where(ctx, where).forEach(ctx, origVisit, new EQ(SpidAwareXInfo.SPID, Integer.valueOf( defaultSharedSpid)));
            }
        }
        
        return origVisit;
    }
    
    
    @Override
    public Object cmd(Context ctx, Object arg) throws HomeException,
            HomeInternalException
    {
        if (arg instanceof XProjection)
        {
            
            /*
             * Note this is likely to return incorrect result, because of Framework's buggy code: 
             * WhereHome.cmd, which instead of using getWhere(ctx), uses getWhere() which in turn
             * assumes a local context with Princial.class set to null; there by causing the 
             * getWhere of SpidAwareHome to return 'Logic-->True' always (as opposed to Spid 
             * Predicate). 
             * 
             * As a result of that bug, the count may return beans in spid-agnostic way (while
             * it is expected that the 'count' should count only spid centric beans if 
             * SpidAwareHome is present in the home pipeline.
             */
            final Object origExec = super.cmd(ctx, arg);
            
            XProjection xproj = (XProjection)XBeans.getInstanceOf(ctx, arg, XProjection.class);
            XStatement xstmt = (XStatement) XBeans.getInstanceOf(ctx, arg, XStatement.class);
            if (xstmt != null)
            {
                if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
                {
                    Home newHome = findNewHomeForSharedBeans(ctx);
                    if(newHome!=null)
                    {
                        final int defaultSharedSpid = GeneralConfigSupport.getDefaultSharedSpid(ctx);
                        Object newExec = newHome.cmd(ctx, new ProjectionProxy(xproj, new And().add(
                                    new EQ(SpidAwareXInfo.SPID, Integer.valueOf( defaultSharedSpid))
                                ).add(xstmt)));
                        
                        if(newExec!=null)
                        {
                            newExec = amalgamate(xproj, xstmt, origExec, newExec);
                            
                            if(newExec!=null)
                                return newExec;
                        }
                        
                        
                    }
                }
           
              
           }
        }
        
        
        return super.cmd(ctx, arg);
    }

    /**
     * TODO for now, only Count is implemented (needed for Msisdn beans)
     * @param xproj The actual projection instance - usually, one of these four we need to
     * amalgamate - {@link CountXProjection}, 
     * {@link MaxXProjection}, {@link SumXProjection}, {@link MinXProjection}
     * @param xstmt Not needed for now.
     * @param origExec
     * @param newExec
     * @return
     */
    protected Object amalgamate(XProjection xproj, XStatement xstmt,
            Object origExec, Object newExec)
    {
        
        // SELECT count (*) ...
        if(xproj instanceof CountXProjection
                || xproj instanceof ProjectionProxy)
        {
            long count = 0;
            if (origExec instanceof Number)
                count = ((Number)origExec).longValue();
            
            if (newExec instanceof Number)
                count += ((Number)newExec).longValue();
            
            if(origExec instanceof Number || newExec instanceof Number)
                return Long.valueOf(count);
        }
            
        // TODO cases for Min, Max, Sum, *
        
        /*
         * No point in returning anything if we could not do it!
         */
        return null;
    }
    
    
    @Override
    public void remove(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
        super.remove(ctx, obj);
        
        BEAN bean = (BEAN)obj;
        
        if(GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx))
        {
            final int defaultSharedSpid = GeneralConfigSupport.getDefaultSharedSpid(ctx);
            if(bean.getSpid() == defaultSharedSpid)
            {
                Home newHome = findNewHomeForSharedBeans(ctx);
                if(newHome!=null)
                {
                    if(LogSupport.isDebugEnabled(ctx))
                        LogSupport.debug(ctx, this, "Deleting from shared beans... from Home: "+ newHome);
                    
                    newHome.remove(ctx, bean);
                }
            }
        }
    }
    
    /*
     * Do not override SelectAll as it will internally call Select(ctx, Obj) with Obj as Elang.TRUE
     */
    
}
