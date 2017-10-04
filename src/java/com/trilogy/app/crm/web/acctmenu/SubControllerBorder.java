/*
 *  SubControllerBorder.java
 *
 *  Author : kgreer
 *  Date   : Apr 11, 2005
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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */ 
 
package com.trilogy.app.crm.web.acctmenu;

import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.framework.xhome.beans.Child;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.filter.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.support.IdentitySupportFunction;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.*;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import java.util.Collection;
import com.trilogy.framework.xlog.log.*;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.*;

import org.apache.commons.lang.StringUtils;


/** XStatement which finds Children by parent key. @deprecated use XInfoSubControllerBorder instead. **/
class ByParentXStatement
   implements XStatement
{
   
   String fieldName_;
   String parentKey_;

   public ByParentXStatement(String fieldName, String parentKey)
   {
      fieldName_ = fieldName;
      parentKey_ = parentKey;
   }
   
   public String createStatement(Context ctx)
   {
      return fieldName_ + " = ?";
   }
   
   public void set(Context ctx, XPreparedStatement ps)
      throws SQLException
   {
      ps.setString(parentKey_);
   }
         
}


/** Predicate which finds Children by parent key. **/
class ByParentPredicate
   implements Predicate
{
   
   Object parentKey_;
   public static final String ID_DELIMITER = "-";

   public ByParentPredicate(Object parentKey)
   {
      parentKey_ = parentKey;
   }
   
   public boolean f(Context ctx, Object bean)
   {
      Child child = (Child) bean;
      /*
       * TCBSUP-1673 : 20160524-092105 - BSS Cannot see Trouble Ticket
       * In 7x we can see all (trouble) ticket under Account Management.
       * But in 9x we can see only those tickets which are open through Account Management.
       * If XTroubleTicket secondaryRefId is null then we can see those tickets under Account Management but
       * if secondaryRefId is not null that means it created under Subscription Management and visible under Subscription 
       * Management Ticket tab only.
       * But requirement is , If we click on Account Management Ticket link then user can able to view all Tickets created against
       * that ban as well as against their subscription also.
       * 
       * existing code restrict subscription level created tickets under Account Management.
       * 
       * Following code is hack for Trouble Ticket only, so that subscription level tickets are alos visible under Account tab.
       * 
       */
      if(bean.getClass().getCanonicalName().equals("com.redknee.app.troubleticket.bean.TroubleTicket"))
      {
			if (!SafetyUtil.safeEquals(child.getParent(),	parentKey_)   // It check that request are for subscription trouble ticket.
					&& SafetyUtil.safeEquals(StringUtils.substringBefore(child.getParent().toString(),ID_DELIMITER), parentKey_)) //It check that parentKey_ is BAN and equal to PrimaryRefId
			{
				return true; // allow subscription level ticket in Account tab also.
			} else 
			{
				return SafetyUtil.safeEquals(child.getParent(), parentKey_); // legacy behaviour
			}
      }else
      {
    	  return SafetyUtil.safeEquals(child.getParent(), parentKey_);  // legacy behaviour
      }
   }
}

   

/**
 * A Border which restricts a WebController to work on a subset of data as specified
 * by a pre-selected Parent bean which has already been specified.
 *
 * This is a replacement for an embedded WebControllerWebControl.
 *
 * @author  kgreer
 **/
public class SubControllerBorder
   implements Border, Serializable
{

   protected Class    parentClass_;
   protected Class    childClass_;
   protected Object   childHomeKey_;
   protected Object   parentHomeKey_;
	 
	 /**
		* Supplied bean factory allowing default factory replacement.
		*/
	 protected ContextFactory childFactory_ = null;
   
   /** Function for returning value from Parent to be used for joining with child. **/
   protected Function parentValueFunction_;

   /** SQL field name to use for XStatement.  If null, which it is by default, then don't use an XStatement, only a Predicate. **/
   protected String fieldName_ = null;


   public SubControllerBorder(Class parentCls, Class childCls)
   {
      this(parentCls, childCls, null);
   }
   
   
   public SubControllerBorder(Class parentCls, Class childCls, String fieldName)
   {
      this(
         parentCls,
         childCls,
         XBeans.getClass(ContextLocator.locate(), childCls, Home.class),
         fieldName);
   }
   
   
   public SubControllerBorder(Class parentCls, Class childCls, Object childHomeKey, String fieldName)
   {
      this(
         parentCls,
         childCls,
         childHomeKey,
         fieldName,
         new IdentitySupportFunction(parentCls));
   }
   
   
   public SubControllerBorder(Class parentCls, Class childCls, Object childHomeKey, String fieldName, Function parentValueFunction)
   {
      this(parentCls,
           childCls,
           childHomeKey,
           fieldName,
           parentValueFunction,
           XBeans.getClass(ContextLocator.locate(), parentCls, Home.class));
   }

   public SubControllerBorder(Class parentCls, Class childCls, Object childHomeKey, String fieldName, Function parentValueFunction, Object parentHomeKey)
   {
      setParentClass(parentCls);
      setChildClass(childCls);
      setChildHomeKey(childHomeKey);
      setFieldName(fieldName);
      setParentValueFunction(parentValueFunction);
      setParentHomeKey(parentHomeKey);
   }
   

   ///////////////////////////////////////////// property ParentValueFunction
   
   public SubControllerBorder setParentValueFunction(Function value)
   {
      parentValueFunction_ = value;
      
      return this;
   }

   public Function getParentValueFunction()
   {
      return parentValueFunction_;
   }

   
   ///////////////////////////////////////////// property ChildHomeKey
   
   public SubControllerBorder setChildHomeKey(Object value)
   {
      childHomeKey_ = value;
      
      return this;
   }

   public Object getChildHomeKey()
   {
      return childHomeKey_;
   }
   
   
   ///////////////////////////////////////////// property ChildClass
   
   public void setChildClass(Class value)
   {
      childClass_ = value;
   }

   public Class getChildClass()
   {
      return childClass_;
   }

   
   ///////////////////////////////////////////// property ParentClass
   
   public void setParentClass(Class value)
   {
      parentClass_ = value;
   }

   public Class getParentClass()
   {
      return parentClass_;
   }

   
   ///////////////////////////////////////////// property ParentHomeKey
   
   public SubControllerBorder setParentHomeKey(Object value)
   {
      parentHomeKey_ = value;
      
      return this;
   }

   public Object getParentHomeKey()
   {
      return parentHomeKey_;
   }
   
   
   public Object getParent(Context ctx)
   {
      Object parent = ctx.get(getParentClass());
      String parentKey = WebAgents.getParameter(ctx, "parentKey");
      boolean forceParentRefresh = false;

      // if the parentKey and the parent do not refer to the same key, force
      // a parent look up to refresh the parent
      if (parent != null && parentKey != null && parentKey.length() > 0)
      {
         if (!parentKey.equals(XBeans.getIdentifier(parent)))
         {
            forceParentRefresh = true;
         }
      }

      if (forceParentRefresh || (parent == null && getParentHomeKey() != null))
      {
         try
         {
            if (parentKey == null || parentKey.length() == 0)
            {
               String childKey = WebAgents.getParameter(ctx, "key");
							 if (childKey == null)
							 {
								 // TT6010928950
								 return null;
							 }

               Child child = (Child)((Home)ctx.get(getChildHomeKey())).find(ctx, childKey);
               
               if(child==null)
               {
            	   return null;
               }
               
               parentKey = (String)child.getParent();
            }
            parent = ((Home)ctx.get(getParentHomeKey())).find(ctx, parentKey);
            ctx.put(getParentClass(), parent);
         }
         catch (HomeException hEx)
         {
            new MinorLogMsg(this, "fail to look up parent from request parameters information", hEx).log(ctx);
         }
      }
      // save the parent in session for easy access
      Session.getSession(ctx).put(getParentClass(), parent);
      return parent;
   }
   
   
   ///////////////////////////////////////////// property FieldName
   
   public void setFieldName(String value)
   {
      fieldName_ = value;
   }

   public String getFieldName()
   {
      return fieldName_;
   }

   ///////////////////////////////////////////// property ChildFactory
   
   public SubControllerBorder setChildFactory(ContextFactory factory)
   {
      childFactory_ = factory;

			return this;
   }

   public ContextFactory getChildFactory()
   {
      return childFactory_;
   }

   
   ///////////////////////////////////////////////////// impl Border
   
   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
      throws ServletException, IOException
   {
      Object parent = getParent(ctx);

      /*
      if ( parent == null )
      {
         PrintWriter out = res.getWriter();
         
         out.println("<font color=red>Error: Parent not specified.</font>");
         out.println("<!--");
         out.println("Developer Error: missing " + getParentClass());
         out.println("-->");
         
         return;
      }
      */
      
      ctx = ctx.createSubContext();
   
      if ( parent != null )
			{
				if (getChildFactory() == null)
				{
      XBeans.putBeanFactory(ctx, getChildClass(), new ContextFactory()
      {
         public Object create(Context ctx)
         {
            try
            {
               Object          parent    = getParent(ctx);
               // Use the Parent Context ("..") so that we don't call ourself and cause an infinite recursion
               Child           child     = (Child) XBeans.instantiate(getChildClass(), (Context) ctx.get(".."));
               
               child.setParent(getParentValueFunction().f(ctx, parent));
               
               return modifyChild(ctx, child);
            }
            catch (Exception e)
            {
               return null;
            }
         }
      });
				}
				else
				{
					XBeans.putBeanFactory(ctx, getChildClass(), getChildFactory());
				}
			}

      // Decorate the Home
      ctx.put(getChildHomeKey(), decorateHome(ctx, (Home) ctx.get(getChildHomeKey())));

      delegate.service(ctx, req, res);
   }
   
   
   /**
    * Template method to customize child bean's if required.
    *
    * You don't need to set the Child's parent as that's already done.
    **/
   public Object modifyChild(Context ctx, Object child)
   {
      return child;
   }
   
   
   /**
    *  Decorate the Home with a HomePredicateFilterHome so that only Children
    *  from the Parent bean are available.
    **/
   public Home decorateHome(Context ctx, Home home)
   {
      Object parent = getParent(ctx);
      
      if ( parent == null )
      {
         return new HomeProxy(ctx, home)
	 {
		 public Collection select(Context ctx, Object obj)
		 	throws HomeException, HomeInternalException
		{
			throw new UnsupportedOperationException("Parent not specified.");
		}
	 };
      }
      
      final Object key = getParentValueFunction().f(ctx, parent);
      
      return new WhereHome(ctx, home, decorateWhere(ctx, parent, key, new ByParentPredicate(key)))
      {
         // Don't decorate find, only selectAll()
         // This is so that if they explicitly specify a key that doesn't belong to this parent that
         // they can still find it.
         public Object find(Context ctx, Object key)
            throws HomeException
         {
            return getDelegate().find(ctx, key);
         }
         
      };
   }


   /**
    * Performs a Logic.Either() on the standard Predicate and the XStatement created
    * by the createXStatement method.  If you wish to do something else then just
    * override this method and add a NOP impl of createXStatement.
    **/
   public Object decorateWhere(Context ctx, Object parent, Object key, Predicate where)
   {
      // Don't use XStatement if no FieldName set
      if ( getFieldName() == null )
      {
         return where;   
      }
      
      return new EitherPredicate(
         where,
         createXStatement(ctx, parent, key));
   }
   
   
   /** XStatement for efficient SQL support.  Override in subclass if using non-String field. **/
   public XStatement createXStatement(Context ctx, Object parent, final Object key)
   {
      return createXStatement(ctx, getFieldName(), (String) getParentValueFunction().f(ctx, parent));
   }

   /** open up access to the private class ByParentXStatement **/
   public XStatement createXStatement(Context ctx, String fieldName, String parentValue)
   {
      return new ByParentXStatement(fieldName, parentValue);
   }


}


