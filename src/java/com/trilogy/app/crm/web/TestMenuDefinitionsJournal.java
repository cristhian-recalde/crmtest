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
package com.trilogy.app.crm.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ProxyContext;
import com.trilogy.framework.xhome.journal.JournalException;
import com.trilogy.framework.xhome.journal.JournalSupport;
import com.trilogy.framework.xhome.journal.executor.JournalExecutor;
import com.trilogy.framework.xhome.journal.executor.SimpleJournalExecutor;
import com.trilogy.framework.xhome.menu.XMenuHome;
import com.trilogy.framework.xhome.menu.XMenu;
import com.trilogy.framework.xhome.menu.XMenuBeanShellConfig;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Test menu definitions in journal files.
 *
 * @author victor.stratan@redknee.com
 */
public class TestMenuDefinitionsJournal extends ContextAwareTestCase
{
    public static final String MENU_JOURNAL_FILE_NAME = "META-INF/journal/app-crm-menu.sch";
    public static final String BOOTSTRAP_FILE_NAME = "META-INF/journal/bootstrap.sch";
    public static final String CONFIG_FILE_NAME = "META-INF/journal/junit-menu-test-conf.sch";

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestMenuDefinitionsJournal(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestMenuDefinitionsJournal.class);
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        super.tearDown();
    }

    public void testOpenJournalFile() throws IOException
    {
        final InputStream input = this.getClass().getClassLoader().getResourceAsStream(MENU_JOURNAL_FILE_NAME);

        assertNotNull("Unable to open menu journal file", input);

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final String line = reader.readLine();
        reader.close();
        assertNotNull("Journal file is empty", line);
    }

    public void testDuplicateKeys() throws JournalException
    {
        Context ctx = getContext().createSubContext("core");
        ctx.put(JournalExecutor.class, new SimpleJournalExecutor());

        final FakeXMenuHome holder = new FakeXMenuHome(ctx);
        ctx.put(XMenuHome.class, holder);
        FakeContext fakeCtx = new FakeContext(ctx, new FakeExtraHome());
        ctx = fakeCtx;
        try
        {
                JournalSupport.executeResourceJournal(ctx, BOOTSTRAP_FILE_NAME);
        } catch (AgentException e)
        {
        	throw new JournalException("IOException while executing "+BOOTSTRAP_FILE_NAME+" exception:"+e.getMessage(),e);
        }

		try {
			JournalSupport.executeResourceJournal(ctx, CONFIG_FILE_NAME);
		} catch (AgentException e) {
			throw new JournalException("IOException while executing "
					+ CONFIG_FILE_NAME + " exception:" + e.getMessage(), e);

		}

        String duplicatesMsg = "Duplicates found: ";
        if (holder.duplicates_.size() > 0)
        {
            duplicatesMsg = duplicatesMsg + holder.duplicates_;
            System.out.println(duplicatesMsg);
        }
        if (holder.nonGrouped_.size() > 0)
        {
            System.out.println("Not grouped = " + holder.nonGrouped_);
        }
        if (holder.beanShelled_.size() > 0)
        {
            System.out.println("use beanshell = " + holder.beanShelled_);
        }
        if (holder.removed_.size() > 0)
        {
            System.out.println("removed beans = " + holder.removed_);
        }
        String otherMsg = "Other beans in the menu journal. Home keys: ";
        if (fakeCtx.other_.size() > 0)
        {
            otherMsg = otherMsg + fakeCtx.other_;
            System.out.println(otherMsg);
        }

        assertEquals(duplicatesMsg, 0, holder.duplicates_.size());
        // TODO activate this check. Fix the journal.
        //assertEquals(otherMsg, 0, fakeCtx.other_.size());
    }

    class FakeXMenuHome extends HomeProxy
    {
        Set keys_ = new HashSet();
        Set duplicates_ = new HashSet();
        Set beanShelled_ = new HashSet();
        Set removed_ = new HashSet();
        Set nonGrouped_ = new HashSet();
        Set parents_ = new HashSet();
        Object lastParent_ = null;
        long loaded_;

        public FakeXMenuHome(Context ctx)
        {
            super(ctx);
        }
        
        public Object create(Context ctx, Object obj) throws HomeException
        {
            XMenu menu = (XMenu) obj;
            loaded_++;
            if (!keys_.add(menu.getKey()))
            {
                duplicates_.add(menu.getKey());
            }
            if (menu.getConfig() instanceof XMenuBeanShellConfig)
            {
                beanShelled_.add(menu.getKey());
            }
            if (lastParent_ == null)
            {
                lastParent_ = menu.getParentKey();
            }
            else if (!lastParent_.equals(menu.getParentKey()))
            {
                if (!parents_.add(menu.getParentKey()))
                {
                    nonGrouped_.add(menu.getKey());
                }
            }
            return menu;
        }

        public void remove(Context ctx, Object obj) throws HomeException
        {
            XMenu menu = (XMenu) obj;
            removed_.add(menu.getKey());
        }
    }

    class FakeExtraHome extends HomeProxy
    {
        public Object find(Context ctx, Object obj) throws HomeException
        {
            System.out.println("FIND: " + obj);
            return super.find(ctx, obj);
        }

        public Collection select(Context ctx, Object obj) throws HomeException
        {
            System.out.println("SELECT: " + obj);
            return super.select(ctx, obj);
        }

        public Object create(Context ctx, Object obj) throws HomeException
        {
            return obj;
        }

        public Object store(Context ctx, Object obj) throws HomeException
        {
            return obj;
        }

        public void remove(Context ctx, Object obj) throws HomeException
        {
        }
    }

    class FakeContext extends ProxyContext
    {
        Set other_ = new HashSet();
        Home home_;
        FakeContext(Context ctx, FakeExtraHome home)
        {
            super(ctx);
            home_ = home;
        }

        public Object get(Context ctx, Object key)
        {
            return super.get(ctx, key);
        }

        public Object get(Object key)
        {
            if (key.equals("core"))
            {
                return this;
            }
            Class[] impls = ((Class) key).getInterfaces();
            Set impl = new HashSet();
            for (int i = 0; i < impls.length; i++)
            {
                impl.add(impls[i]);
            }
            if (impl.contains(Home.class))
            {
                if (!key.equals(XMenuHome.class))
                {
                    other_.add(key);
                    return home_;
                }
            }
            return super.get(key);
        }
    }
}
