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
package com.trilogy.app.crm.unit_test;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.license.DefaultLicenseMgr;
import com.trilogy.framework.license.License;
import com.trilogy.framework.license.LicenseHome;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.license.LicenseTransientHome;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.home.TransientHome;
import com.trilogy.framework.xhome.xdb.XDBHome;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;
import com.trilogy.tool.xtest.test.XTest;


/**
 * Provides utility functions for unit tests.
 *
 * @author gary.anderson@redknee.com
 */
public final
class UnitTestSupport
{
    /**
     * Prevents instantiation of this utility class.
     */
    private UnitTestSupport()
    {
        // Empty
    }


    /**
     * Gets the date for the given String with format "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @param date The String encoded date.
     * @return The Date object for the given String.
     */
    public static Date parseDate(final String date)
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setLenient(false);

        try
        {
            return format.parse(date);
        }
        catch (final ParseException exception)
        {
            final IllegalStateException newException =
                new IllegalStateException("Failed to parse dates.");

            newException.initCause(exception);

            throw newException;
        }
    }


    /**
     * Creates and installs a transient LicenseMgr into the given context.
     *
     * @param context The operating context.
     */
    public static void installLicenseManager(final Context context)
    {
        context.put(LicenseHome.class, new TransientFieldResettingHome(context, new LicenseTransientHome(context)));
        context.put(LicenseMgr.class, new DefaultLicenseMgr(context));
    }


    /**
     * Creates a new license with the given label.  This is meant to be used in
     * conjunction with the transient LicenseMgr.
     *
     * @see #installLicenseManager
     *
     * @param context The operating context.
     * @param name The name of the license.
     *
     * @exception HomeException Thrown if there is a problem creating the
     * License.
     */
    public static void createLicense(final Context context, final String name)
        throws HomeException
    {
        final License license = new License();
        license.setName(name);
        license.setKey(name);
        license.setEnabled(true);

        final Home licenseHome = (Home)context.get(LicenseHome.class);
        licenseHome.create(license);
    }

    
    /**
     * Temporary Solution. Put ctx.put("UseXDBHomes", true);
     * in Application.script to use XDBHomes instead
     * 
     * @param ctx
     *            the context where the home is inserted
     * @param cls
     *            the class of the bean that we want the home for
     * @return the home
     */
    public static Home createHome(Context ctx, Class cls)
    {
        try
        {
            new InfoLogMsg(UnitTestSupport.class, "Attempting to instantiate home for \"" + cls + "\".", null).log(ctx);

            Class homeClass = XBeans.getClass(ctx, cls, Home.class);
            
            Class       cl   = XBeans.getClass(ctx, cls, TransientHome.class);
            Constructor con  = cl.getConstructor(new Class[] { Context.class });
            Home        home = (Home) con.newInstance(new Object[] { ctx });
            
            if ( ctx.getBoolean("UseXDBHomes", false) )
            {
                cl  = XBeans.getClass(ctx, cls, XDBHome.class);
                con = cl.getConstructor(new Class[]    { Context.class, String.class });

                String tableName = "TEST" + cls.getName().toUpperCase();
                home = (Home) con.newInstance(new Object[] { ctx, tableName });
            }
            
            home = new SortingHome(home);
            
            ctx.put(homeClass, home);

            return home;
        }
        catch (Throwable t)
        {
            new MajorLogMsg(UnitTestSupport.class.getName(), "Failed to instantiate home for \"" + cls + "\".", t)
                .log(ctx);

            return null;
        }
    }

    /**
     * If the test case is using the XTest's context this method will return true.  Note 
     * that if you have configured your XTest Test using the non-context-aware suite() method
     * then you will be creating a context from scratch instead of using the XTest context and
     * so this method will return false.
     * 
     * @param ctx
     * @return true if the context passed in is the XTest context, false otherwise.
     */
	public static boolean isTestRunningInXTest(Context ctx)
	{
		
		return ctx.has(XTest.class);
	}

} // class
