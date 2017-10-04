/*
 *  CommitRatioAgent.java
 *
 *  Author : kgreer
 *  Date   : Jul 04, 2005
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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.txn.TransactionException;
import com.trilogy.framework.xhome.txn.Transactions;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XDBProxy;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * A ContextAgent decorator which commits the current Transaction
 * after a configurable number of operations.
 *
 * @author  kgreer
 **/
public class CommitRatioAgent
	extends ContextAgentProxy
{

   public final static int DEFAULT_COMMIT_RATIO = 50;
   
   
   /** Number of operations to perform before commiting. **/
   protected int commitRatio_ = DEFAULT_COMMIT_RATIO;
   
   /** Number of uncommited operations performed. **/
   protected int count_ = 0;
   
   
   public CommitRatioAgent(int commitRatio, ContextAgent delegate)
   {
      super(delegate);
      
      setCommitRatio(commitRatio);
   }


   public CommitRatioAgent(ContextAgent delegate)
   {
      super(delegate);
   }

   
   ///////////////////////////////////////////// count support
   
   
   /** Increment operation count and commit if commit-ratio reached. **/
   public synchronized void incr(Context ctx)
      throws HomeException
   {
      if ( ++count_ >= getCommitRatio() )
      {
         count_ = 0;
         
         try
         {
            Transactions.commit(ctx);
         }
         catch (TransactionException e)
         {
            throw new HomeException("Unable to commit changes in CommitRatioAgent.", e);
         }
      }
   }
   
   
   ///////////////////////////////////////////// property CommitRatio
   
   public void setCommitRatio(int value)
   {
      commitRatio_ = value;
   }

   public int getCommitRatio()
   {
      return commitRatio_;
   }

   
   ///////////////////////////////////////////// impl ContextAgent
   
   public void execute(Context ctx)
      throws AgentException
   {
      ctx = ctx.createSubContext();
      
      XDB xdb = (XDB) ctx.get(XDB.class);
      
      // decorate XDB so that it counts updates
      ctx.put(XDB.class, new XDBProxy(xdb) {
         public int execute(Context ctx, String stmt)
            throws HomeException
         {
            try
            {
               return getDelegate().execute(ctx, stmt);
            }
            finally
            {
               incr(ctx);
            }
         }
         
         
         public int execute(Context ctx, XStatement stmt)
            throws HomeException
         {
            try
            {
               return getDelegate().execute(ctx, stmt);
            }
            finally
            {
               incr(ctx);
            }
         }
      });
      
      delegate(ctx);
   }



}


