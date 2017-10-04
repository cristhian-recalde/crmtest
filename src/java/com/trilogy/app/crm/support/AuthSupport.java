/*
 *  Support.java
 *
 *  Author : kgreer Date : Apr 08, 2004
 *
 *  This code is a protected work and subject to domestic and international
 *  copyright law(s). A complete listing of authors of this work is readily
 *  available. Additionally, source code is, by its very nature, confidential
 *  information and inextricably contains trade secrets and other information
 *  proprietary, valuable and sensitive to Redknee, no unauthorised use,
 *  disclosure, manipulation or otherwise is permitted, and may only be used in
 *  accordance with the terms of the licence agreement entered into with Redknee
 *  Inc. and/or its subsidiaries.
 *
 *  Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.security.Permission;
import java.security.Principal;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimit;
import com.trilogy.app.crm.validator.UserDailyAdjustmentLimitTransactionValidator;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.Constants;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.PermissionRow;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 *  Authentication related support methods.
 *
 * @author     Jchen
 * @created    this class is copied from latest FW\trunk\com.redknee.framework.auth.spi.Support
 */
public class AuthSupport
   implements Constants
{
   /**
    *  The name of this Component for the purposes of generating Entry Logs. 
    **/
   protected final static String COMPONENT = "Framework.Authentication";


   /**
    *  Deactivate a user and set their DeactivationReason field. Please note
    *  that the paramaters should never be set to null values.
    *
    * @param  ctx
    * @param  username
    * @param  reason
    * @param  alarm            Description of the Parameter
    * @return                  Description of the Return Value
    * @throws  LoginException
    */
   public static boolean suspendUser(Context ctx, String username, String reason, boolean alarm)
      throws LoginException
   {
      try
      {
         Home home = (Home) ctx.get(UserHome.class);
         User user = (User) home.find(ctx, username);

         if (user != null && user.isActivated())
         {
            user.setActivated(false);
            user.setDeactivationReason(reason);
            home.store(ctx,user);

            if (alarm)
            {
               new EntryLogMsg(10841L, AuthSupport.class, COMPONENT, null, new String[]{reason}, null).log(ctx);
            }
         }
      }
      catch (HomeException e)
      {
         // Who cares
      }

      throw new LoginException(reason);
   }


   /**
    *  Check a Permission against a Group. This logic has been moved into a
    *  static method so that it can be reused by other AuthenticationServices
    *  which perhaps don't use User's but still want to use Group's. Please
    *  ensure that these parameters are not set to null values.
    *
    * @param  groupHome
    * @param  groupKey
    * @param  permission
    *
    * @return true if permission is granted, false if not. 
    */
   public static boolean checkPermission(Home groupHome, String groupKey, Permission permission)
   {
      try
      {
         Group group = (Group) groupHome.find(groupKey);
         
         if ( group == null )
         {
            // TODO: log this user without a group
            return false;
         }
         
         String permissionName = permission.getName();
         
         if ( permissionName.equals(SimplePermission.ANY) )
         {
            return true;
         }

         do
         {
	         for ( Iterator i = group.getPermissions().iterator() ; i.hasNext() ; )
	         {
	            PermissionRow row = (PermissionRow) i.next();
	            Permission    p   = new SimplePermission(row.getPermission());
	            
	            if ( p.implies(permission) )
	            {
	               return true;
	            }
	         }
					 // check parent groups
	         group = (Group) groupHome.find(group.getGroup());
         }
         while ( group != null );
         
         // if permission is like "admin.group.<groupname>" then grant permission if
         // group is descendent of this group
         if ( permissionName.startsWith(PERMISSION_PREFIX) )
         {
            String childKey = permissionName.substring(PERMISSION_PREFIX.length());
            
            return isDescendent(groupHome, groupKey, childKey);
         }
         
         return false;
      }
      catch (HomeException e)
      {
         // TODO: log
         e.printStackTrace();
         return false;
      }
   }
   
   
   /**
    * Check if one group has another as an ancestor. 
    *
    * @param groupKey the id of the potential ancestory group 
    * @param childKey the id of the potential descendent group
    **/
   public static boolean isDescendent(Home groupHome, String groupKey, String childKey)
   {
      // prevent infinite loop if someone makes a group with "" as name
      if ( "".equals(childKey) ) return false;
      
      // This would give users permission to their own group which I don't think 
      // would be a good idea
      // if ( SafetyUtil.safeEquals(groupKey, childKey) ) return true;
      
      try
      {
         Group group = (Group) groupHome.find(childKey);
      
         if ( group == null ) return false;
         
         return
            SafetyUtil.safeEquals(groupKey, group.getGroup()) ||
            isDescendent(groupHome, groupKey, group.getGroup());
      }
      catch (HomeException e)
      {
         return false;  
      }
   }
   
   /**
    * Check if the principal in context has the given permission
    * 
    * @param ctx 
    *           The operating context
    * @param permission
    *           The permission to check
    * @return 
    *           true if principal has permission, false otherwise
    */
   public static boolean hasPermission(Context ctx, Permission permission)
   {
       boolean result = false;
       
       final Context session = Session.getSession(ctx);
       
       Principal principal = null;
       if (session != null)
       {
           principal = (Principal) session.get(Principal.class);
       }
       if (principal == null)
       {
           principal = (Principal) ctx.get(Principal.class);
       }

       final AuthSPI auth = (AuthSPI) ctx.get(AuthSPI.class);
       if (principal != null
               && auth != null
               && auth.checkPermission(ctx, principal, permission))
       {
           result = true;
       }
       
       return result;
   }
   
   public static long getUserLimitFromGroup(Context ctx, String userID) throws HomeException
   {
       Home userHome = (Home) ctx.get(UserHome.class);
       Home groupHome = (Home) ctx.get(CRMGroupHome.class);
       final User user = (User) userHome.find(userID);

       if (user == null)
       {
           return UserDailyAdjustmentLimit.DEFAULT_CONFIGUREDLIMIT;
       }

       final CRMGroup userGroup = (CRMGroup) groupHome.find(ctx, user.getGroup());

       if (userGroup == null)
       {
           final IllegalStateException exception = new IllegalStateException("Error while retrieving agent's group.");
           LogSupport.minor(ctx, UserDailyAdjustmentLimitTransactionValidator.class, "Unable to retrieve group '" + user.getGroup() + "'.", null);
           throw exception;
       }

       return userGroup.getDailyAdjustmentsLimit();
   }

}
