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
package com.trilogy.app.crm.bundle.home;


import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.XInfoAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.XInfoAdapterHome;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.XStatementProxy;

/**
 * AdapterHome removes the BM elang and generates errors on select
 * this adapter will pass through the super.select method and adapt it
 * 
 * @author arturo.medina@redknee.com
 *
 */
public class BundleManagerXInfoAdapterHome extends XInfoAdapterHome
{
    /**
     * Default constructor
     */
    public BundleManagerXInfoAdapterHome()
    {
    }

    /**
     * Accepts the delegate and the adapter
     * @param delegate 
     * @param adapter 
     */
    public BundleManagerXInfoAdapterHome(Home delegate, XInfoAdapter adapter)
    {
        super(delegate, adapter);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3498587314421337588L;
    
    
    /** 
     * Remove the Predicate component of a where Object.
     * Overriding this method to return the where if it's the same 
     * @param ctx 
     * @param where 
     * @return 
     */
    public Object removePredicateFromWhere(Context ctx, Object where)
    {
       Predicate p = (Predicate) XBeans.getInstanceOf(ctx, where, Predicate.class);
       
       if ( p == null || p == where) return where;
       
       try
       {
          XStatement sql = (XStatement) XBeans.getInstanceOf(ctx, where, XStatement.class);
          
          // This bit is tricky.
          // I need to wrap the xstatement in an XStatementProxy in case it
          // implements Predicate also.  This is the only way to strip away
          // its Predicate-ness.
          
          return ( sql == null ) ?
             True.instance() :
             new EitherPredicate(True.instance(), new XStatementProxy(sql));
       }
       catch (Throwable t)
       {
          // It could happen that the Object sometimes implements
          // XStatement but not in the current configuration.  ELang
          // as it is currently implemented is an example.  
          return True.instance();
       }
    }


}
