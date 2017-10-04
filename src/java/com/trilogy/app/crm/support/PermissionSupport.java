/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.security.Permission;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.spi.UserAndGroupAuthSPI;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.GroupHome;
import com.trilogy.framework.xhome.auth.bean.GroupTransientHome;
import com.trilogy.framework.xhome.auth.bean.PermissionRow;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.auth.bean.UserTransientHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.ELangSupport;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.menu.XMenu;
import com.trilogy.framework.xhome.menu.XMenuHome;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Methods to use in permission migration.
 * Beanshell Scripts that use these classes can be found at:
 * 
 * \toolcrm\bulkload\branchMigration50To70
 *            Src\doc
 *            Src\beanshell 
 *
 *  @author jchen
 *
 */
public class PermissionSupport 
{

	/*
	 * Add one permission to Menu, 
	 * will not check if menu permission already implies this new permisison or not
	 */
	public static void addPermissionToMenu(Context ctx, String menuKey, String perm) throws HomeException
	{
		Home menuHome = (Home)ctx.get(XMenuHome.class);
		XMenu menu = (XMenu)menuHome.find(ctx, menuKey);
		PermissionRow pw = new PermissionRow(perm, null);
		ArrayList list = new ArrayList();
		list.add(pw);
		
		addPermissionRowsToMenu(ctx, menuHome, menu, list);		
	}
	
	public static void addPermissionsToMenu(Context ctx, String menuKey, List permissions) throws HomeException
	{
		Home menuHome = (Home)ctx.get(XMenuHome.class);
		XMenu menu = (XMenu)menuHome.find(ctx, menuKey);
		
		ArrayList permissionRows = new ArrayList();
		for (int i =0; i < permissions.size(); i++)
		{
			permissionRows.add(new PermissionRow((String)permissions.get(i), null));
		}
		addPermissionRowsToMenu(ctx, menuHome, menu, permissionRows);				
	}
	/**
	 * will not check if menu permission already implies this new permisison or not
	 * 
	 * !!! permission added, but don't know why system not working!
	 * @param ctx
	 * @param menuHome
	 * @param menu
	 * @param perms
	 * @throws HomeException
	 */
	public static void addPermissionRowsToMenu(Context ctx, Home menuHome, XMenu menu, List permissions) throws HomeException
	{
		//Context ctx = (Context) ContextLocator.locate().get("core");
		ELangSupport support = ELangSupport.instance();
		
		if (permissions.size() > 0  && menu != null)
		{
			Or or = new Or();

			for (Iterator i = permissions.iterator(); i.hasNext(); )
			{
				String perm = ((PermissionRow) i.next()).getPermission();
				Predicate p = support.getPermissionPredicate(ctx, perm);
				if (p != null)
					or.add(p);
				else
				{
				    new MajorLogMsg(PermissionSupport.class, "getPermissionPredicate null, perm=" + perm, null).log(ctx);
					//System.out.println("getPermissionPredicate null, perm=" + perm);
				}
			}

			if (menu.getMenuPredicate().equals(True.instance()))
			{
				menu.setMenuPredicate(or);
			}
			else
			{
				menu.setMenuPredicate(new Or().add(menu.getMenuPredicate()).add(or));
			}
			menuHome.store(ctx, menu);
		}
	}
	
	public static boolean isMenuPermissionLowerCheckLocally(Context ctx, String menuKey, String permission) throws HomeException
	{
		Home menuHome = (Home)ctx.get(XMenuHome.class);
		XMenu menu = (XMenu)menuHome.find(ctx, menuKey);
		return isMenuPermissionLowerCheckLocally(ctx, menu, permission);
		
	}
	
	/**
	 * recusively check with parents to see if menu permission defined implies permissionToCheck
	 * @param ctx
	 * @param menu
	 * @param permissionToCheck
	 * @return
	 * @throws HomeException
	 */
	public static boolean menuPermissionLowerThan(Context ctx, XMenu menu, String permissionToCheck) throws HomeException
	{
		boolean lower = false;
		if (isMenuPermissionLowerCheckLocally(ctx, menu, permissionToCheck))
			lower = true;
		else
		{
			lower = false;
			//locally implies, we still need to check parent, and up to root
			String parentKey = menu.getParentKey();
			if (parentKey != null && !parentKey.equalsIgnoreCase("root"))
			{
				Home menuHome = (Home)ctx.get(XMenuHome.class);
				XMenu parentMenu = (XMenu)menuHome.find(ctx, parentKey);
				if (parentMenu != null)
					lower = menuPermissionLowerThan(ctx, parentMenu, permissionToCheck);
			}
		}
		
		
		return lower;
	}
	/**
	 * check menu permission against with user permission,
	 * 
	 * it is similar to  
	 * @param ctx
	 * @param menu
	 * @param userPermission
	 * @return
	 * @throws HomeException
	 */
	public static boolean isMenuPermissionLowerCheckLocally(Context ctx, XMenu menu, String userPermission) throws HomeException
	{
		Context subCtx = ctx.createSubContext();
				
		Group grp = new Group();
		grp.setName("abc");
		List perms = new ArrayList();
		perms.add(new PermissionRow(userPermission, null));
		grp.setPermissions(perms);
		
		Home gropHome = new GroupTransientHome(ctx);
		subCtx.put(GroupHome.class, gropHome);
		gropHome.create(grp);
		
		User user = new User();
		user.setId("abc");
		user.setGroup(grp.getName());
		Home userHome = new UserTransientHome(ctx);
		subCtx.put(UserHome.class, userHome);
		userHome.create(user);
		
		subCtx.put(Principal.class, user);
		
		subCtx.put(AuthSPI.class, new UserAndGroupAuthSPI(subCtx));
//		return menu.f(subCtx, (AuthMgr)subCtx.get(AuthMgr.class));
		return !menu.f(subCtx, new AuthMgr(subCtx));		
	}
	
	
	   /**
     * Add a permission to all user groups in system
     * @param pmr
     * @throws HomeException
     */
    public static void addPermission(final Context ctx, final String pmr) throws HomeException, AgentException
	{
    	if (pmr != null && pmr.length() > 0)
    		addPermissions(ctx, new String[] { pmr });
	}

    public static void addPermissions(final Context ctx, final String[] pmr) throws HomeException, AgentException
    {
    	if (pmr != null && pmr.length > 0)
    	{
    		final Home grpHome = (Home)ctx.get(CRMGroupHome.class);
	    	List allGroups = (List)grpHome.selectAll();
	    	
	    	addPermissionsToGroups(ctx, pmr, allGroups);      
    	}
    }
    public static void addPermissionsToGroups(final Context ctx, final String[] newPermissions, String[] allGroups) throws AgentException 
	{
    	List grps = Arrays.asList(allGroups);
    	addPermissionsToGroups(ctx, newPermissions, grps);
    }
    /**
	 * @param ctx
	 * @param newPermissions
	 * @param allGroups
	 * @throws AgentException
	 */
	public static void addPermissionsToGroups(final Context ctx, final String[] newPermissions, List allGroups) throws AgentException {
		Visitors.forEach(ctx, allGroups,
				new Visitor()
				{
					public void visit(Context subCtx, Object obj)
					{
						Group grp = null;
						try
						{
							Home groupHome = (Home) subCtx.get(CRMGroupHome.class);
							grp = (Group) groupHome.find(subCtx, obj);
						}
						catch (HomeException he)
						{
							LogSupport.major(ctx, this, (String) obj + " was not found in CRMGroupHome.");
							//continue to the next Group ID
							return; 
						}
						try
						{
							addPermissionsToGroup(ctx, grp, newPermissions);
						}
						catch(Exception he)
						{
							String msg = "fail to add permission for group:" + grp + "," + he;
							new MajorLogMsg(this, msg, he).log(subCtx);
							//System.out.println(msg);
						}
					}

				});
	}

	/**
	 * @param ctx
	 * @param grp
	 * @param newPermissions
	 * @throws HomeException
	 */
	public static void addPermissionsToGroup(final Context ctx, Group grp, final String[] newPermissions) throws HomeException 
	{
		if (newPermissions != null)
		{
			final Home hgrpHome = (Home)ctx.get(CRMGroupHome.class);
			
			List permissions = new ArrayList(grp.getPermissions());
			
			boolean added = false;
			for (int i = 0; i < newPermissions.length; i++)
			{
				if (newPermissions[i] != null && newPermissions[i].length() > 0)
				{
					if (!AuthSupport.checkPermission(hgrpHome, grp.getName(), new SimplePermission(newPermissions[i])))
					{
						added = true;
						permissions.add(new PermissionRow(newPermissions[i], null));
					}
				}
			}
			
			if (added)
			{
				//TT6051734432: Permissions need to be SET in order for them to stick.
				grp.setPermissions(permissions);
				hgrpHome.store(ctx, grp);
			}
		}
	}

	public static void addPermissionRows(final Context ctx, final PermissionRow[] pmr) throws HomeException, AgentException
    {
    	if (pmr != null && pmr.length > 0)
    	{
    		addPermissions(ctx, permissionRowToString(pmr));
    		
    	}
    }
    
    public static PermissionRow[] permissionToRow(String[] pmr)
    {
    	PermissionRow[] rows = null;
    	if (pmr != null && pmr.length > 0)
    	{
    		rows = new PermissionRow[pmr.length];
    		for (int i = 0; i < pmr.length; i++)
    		{
    			rows[i] = new PermissionRow(pmr[i], null);
    		}    		
    	}
    	return rows;
    }
    
    public static String[] permissionRowToString(PermissionRow[] pmr)
    {
    	String[] rows = null;
    	if (pmr != null && pmr.length > 0)
    	{
    		rows = new String[pmr.length];
    		for (int i = 0; i < pmr.length; i++)
    		{
    			rows[i] = pmr[i].getPermission();
    		}    		
    	}
    	return rows;
    }
    
    
    
}
