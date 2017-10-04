/*
 *  CommitRatioHome.java
 *
 *  Author : kgreer
 *  Date   : Sep 12, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */ 
 
package com.trilogy.app.crm.home.calldetail;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.NullContextAgent;
import com.trilogy.framework.xhome.home.AbstractClassAwareHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.txn.DefaultTransaction;
import com.trilogy.framework.xhome.txn.Transaction;
import com.trilogy.framework.xhome.txn.TransactionException;
import com.trilogy.framework.xhome.txn.Transactions;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.JDBCXDB;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Disable auto-commit for a Home but commit after a fixed number of updates.
 *
 * @author  kgreer
 **/
public class CommitRatioHome
   extends HomeProxy
{

   public final static String COMMIT_CMD = "CommitRatioHome.Commit";
   
   
   // Transactional Context
   protected Context txnCtx_ = null;

   protected CommitRatioAgent commiter_ = null;
   
   
   public CommitRatioHome(Context ctx, int commitRatio, Home delegate)
   {
      super(ctx, delegate);
      
      commiter_ = new CommitRatioAgent(commitRatio, NullContextAgent.instance());
   }

   
   public CommitRatioHome(Context ctx, Home delegate)
   {
      super(ctx, delegate);
      
      commiter_ = new CommitRatioAgent(NullContextAgent.instance());
   }
   
   
   public void incr()
   {
      try
      {
         commiter_.incr(getTxnContext());
      }
      catch (Throwable t)
      {
         resetTxn();
      }
   }
   
   
   public synchronized void resetTxn()
   {
      try
      {
         Transactions.commit(getTxnContext());
      }
      catch (Throwable t)
      {
      }
      
      try
      {
         DefaultTransaction txn = (DefaultTransaction) getTxnContext().get(Transaction.class);
         
         JDBCXDB xdb = txn.getXDB();

         txn.setXDB(null);
         
         if ( xdb != null ) xdb.close();
      }
      catch (Throwable t)
      {
      }
      
      txnCtx_ = null;
   }

   
   public synchronized Context getTxnContext()
   {
      if ( txnCtx_ == null )
      {
          txnCtx_ = getContext().createSubContext();
          
          DefaultTransaction txn = new DefaultTransaction(txnCtx_);
          
          txnCtx_.put(Transaction.class, txn);
      }
      
      return txnCtx_;
   }
   
   
   
   //////////////////////////////////////// SPI Impl
	
	public synchronized Object create(Context ctx, Object obj)
      throws HomeException, HomeInternalException
	{
      try
      {
         Object ret = getDelegate(ctx).create(getTxnContext(), obj);
         
         incr();
         
         return ret;
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
   
	
	public synchronized Object store(Context ctx, Object obj)
      throws HomeException, HomeInternalException
	{
      try
      {
         Object ret = getDelegate(ctx).store(getTxnContext(), obj);
         
         incr();
         
         return ret;
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
   
	
   public synchronized Object find(Context ctx, Object obj)
      throws HomeException, HomeInternalException
   {
      try
      {
         return getDelegate(ctx).find(getTxnContext(), obj);
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
   }


   public synchronized void remove(Context ctx, Object obj)
      throws HomeException,  HomeInternalException
	{
      if ( ! ( obj instanceof com.redknee.framework.xhome.beans.AbstractBean ) )
      {
         new MajorLogMsg(this, "Non-Bean provided to remove()", new Exception("Non-Bean provide to remove()")).log(ctx);
      }
      
      try
      {
         getDelegate(ctx).remove(getTxnContext(), obj);
         
         incr();
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
	

	public synchronized void removeAll(Context ctx, Object where)
		throws HomeException,  HomeInternalException,  UnsupportedOperationException
	{
      try
      {
         getDelegate(ctx).removeAll(getTxnContext(), where);
         
         incr();
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
	

   /** I'm not sure that this should be supported. KGR **/
	public synchronized void drop(Context ctx)
		throws HomeException,  HomeInternalException,  UnsupportedOperationException
	{
      try
      {
         getDelegate().drop(getTxnContext());
         incr();
         resetTxn();
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
	

	public synchronized Collection select(Context ctx, Object obj)
		throws HomeException,  HomeInternalException
	{
      try
      {
         return getDelegate(ctx).select(getTxnContext(), obj);
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}


	public synchronized Visitor forEach(Context ctx, Visitor visitor, Object where)
		throws HomeException,  HomeInternalException
	{
      try
      {
         return getDelegate(ctx).forEach(getTxnContext(), visitor, where);
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
	
	
	public synchronized Object cmd(Context ctx, Object arg)
		throws HomeException,  HomeInternalException
	{
      if ( AbstractClassAwareHome.IDENTITY_SUPPORT_CMD.equals(arg) )
      {
         return getIdentitySupport(ctx);
      }

      if ( COMMIT_CMD.equals(arg) )
      {
         try
         {
            Transactions.commit(getTxnContext());
         }
         catch (TransactionException e)
         {
            throw new HomeException("Unable to commit changes in CommitRatioAgent.", e);
         }
         
         return Boolean.TRUE;
      }

      try
      {
         return getDelegate(ctx).cmd(ctx, arg);
      }
      catch (HomeInternalException e)
      {
         resetTxn();
         
         throw e;
      }
	}
 

}


