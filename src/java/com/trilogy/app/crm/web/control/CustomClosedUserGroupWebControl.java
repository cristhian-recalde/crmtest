/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.home.CugSubsValidator;
import com.trilogy.app.crm.home.cug.DeltaNewOldCugSubs;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Provides a web control decorator that causes the delegate control to appear in DISPLAY
 * mode rather than EDIT mode if the principle in the context does not have a given
 * permission.
 * 
 * @author gary.anderson@redknee.com
 */
public class CustomClosedUserGroupWebControl extends ClosedUserGroupWebControl
{

    /**
     * Creates a new PermissionToEditWebControl for the given delegate web control and
     * permission.
     * 
     * @param delegate
     *            The web control to which we delegate.
     * @param permission
     *            The permission required to for the control to be editable.
     * 
     * @exception IllegalArgumentException
     *                Thrown if the permission is null.
     */
    /*
     * public CUGSubscriberMediatingWebControl( final WebControl delegate, final
     * Permission permission) { super(delegate);
     * 
     * if (permission == null) { throw new IllegalArgumentException(
     * "The permission is required to be non-null."); }
     * 
     * permission_ = permission; }
     */
    // INHERIT
    public void fromWeb(final Context ctx, final Object obj, final ServletRequest req, final String name)
    {
        super.fromWeb(ctx, obj, req, name);
        final ClosedUserGroup cug = (ClosedUserGroup) obj;
        final String cug_cmd = req.getParameter(ClosedUserGroupWebControl.BUTTON_KEY);
        if (cug_cmd != null)
        {
            if (cug_cmd.equals("Load"))
            {
                fillInClosedSubsFromBulkFile(ctx, cug);
            }
            else if (cug_cmd.equals(ADD_SUBSCRIPTIONS_CMD))
            {
                fillInClosedSubsFromAccount(ctx, cug);
            }
        }
    }


    /**
     * Get eligible subscribers from account for importing.
     * 
     * @param ctx
     * @param ban
     * @return
     */
    public Collection getEligibleSubsFromAccount(final Context ctx, final String ban)
    {
        Collection subs = null;
        try
        {
            Account account = AccountSupport.getAccount(ctx, ban);
            if (account != null)
            {
                subs = AccountSupport.getAllSubscribers(ctx, account);
                subs = getEligibleSubsFromCollection(ctx, subs);
            }
        }
        catch (Exception e)
        {
            final ExceptionListener exceptions = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (exceptions != null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException("Account",
                        "Exception when lookup subscribers in account " + ban));
            }
        }
        return subs;
    }


    /**
     * Filter invalid state subscribers from the collection passed in
     * 
     * @param ctx
     * @param subs
     * @return
     * @throws HomeException
     * @throws AgentException
     */
    @SuppressWarnings("rawtypes")
    public Collection getEligibleSubsFromCollection(final Context ctx, Collection subs) throws HomeException,
            AgentException
    {
        ListBuildingVisitor subRet = new ListBuildingVisitor();
        Visitors.forEach(ctx, subs, subRet, new Not(new In(SubscriberXInfo.STATE, SUBSCRIBER_STATES_RESTRICTGED)));
        return subRet;
    }


    @SuppressWarnings("unchecked")
    private boolean isProcessFileSuccess(Context ctx, File file, ClosedUserGroup cug)
    {
        String line = null;
        boolean success = true;
        try
        {
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            int i = 0;
            final Map<String, ClosedSub> subsAlreadyLoaded = cug.getSubscribers();
            final Map<String, ClosedSub> subsFromFile = new HashMap<String, ClosedSub>();
            do
            {
                i++;
                line = fileReader.readLine();
                if (line == null)
                {
                    break;
                }
                line = line.trim();
                if (line.isEmpty())
                {
                    // ignore blank lines
                    continue;
                }
                String[] pairs = line.split(",");
                try
                {
                    final ClosedSub closedSub;
                    {
                        closedSub = new ClosedSub();
                        if (pairs.length > 1)
                        {
                            closedSub.setPhoneID(pairs[0]);
                            closedSub.setShortCode(pairs[1]);
                        }
                        else if (pairs.length > 0)
                        {
                            closedSub.setPhoneID(pairs[0]);
                        }
                        else
                        {
                            closedSub.setPhoneID(line);
                        }
                    }
                    subsFromFile.put(closedSub.getPhoneID(), closedSub);
                }
                catch (Throwable t)
                {
                    handleException(ctx, "Ignoring entry [ " + line + "] because of error [ " + t.getMessage() + "]", t);
                }
            }
            while (null != line);
            try
            {
                new CugSubsValidator(cug, new DeltaNewOldCugSubs(subsFromFile, subsAlreadyLoaded))
                        .validateAllCases(ctx);
            }
            catch (Throwable t)
            {
                handleException(ctx, t);
            }
            cug.getSubscribers().putAll(subsFromFile);
        }
        catch (EOFException eof)
        {
            new InfoLogMsg(this, "Finished prosessing CUG Bukk Load from File []", null).log(ctx);
        }
        catch (IOException io)
        {
            handleException(ctx, io);
            success = false;
        }
        return success;
    }


    // INHERIT
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Context subContext = ctx.createSubContext();
        ClosedUserGroup cug = (ClosedUserGroup) obj;
        ctx.put(ClosedUserGroup.class, cug);
        // cug.setSubscribers(new TreeSet(cug.getSubscribers())); // Sort the MSISDN list
        super.toWeb(subContext, out, name, cug);
    }

    /**
     * Return a list of Closed User Groups having the given SPID (with the corresponding
     * Friends & Family Remote service).
     * 
     * @param spid
     *            The given SPID.
     * 
     * @return Collection The result list of Closed User Groups.
     */
    public static String ADD_SUBSCRIPTIONS_CMD = "Add Subscriptions";


    private ClosedUserGroup fillInClosedSubsFromBulkFile(Context ctx, ClosedUserGroup cug)
    {
        try
        {
            File file = new File(cug.getBulkLoadFile().trim());
            if (file.exists() && file.isFile())
            {
                isProcessFileSuccess(ctx, file, cug);
            }
            else
            {
                handleException(ctx, "File [" + cug.getBulkLoadFile() + "] either does not exist ore is not valid",
                        null);
            }
        }
        catch (NullPointerException e)
        {
            // no problem, field not submitted
        }
        catch (Throwable t)
        {
            handleException(ctx,
                    "Internal Error with uploading file:" + cug.getBulkLoadFile() + " due to " + t.getMessage(), t);
        }
        return cug;
    }


    private ClosedUserGroup fillInClosedSubsFromAccount(Context ctx, ClosedUserGroup cug)
    {
        AuxiliaryService aux = null;
        // Changed to the HTMLExceptionListener so that the errors would be logged
        // on the screen
        // instead of just in the log, which is difficult to check.
        try
        {
            aux = CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(ctx, cug.getCugTemplateID());
        }
        catch (HomeException e)
        {
            LogSupport.crit(ctx, this, "HomeException: " + e.getMessage(), e);
        }
        /*
         * TT5071921435: Added a new Deprecate option for auxiliary service. When the
         * auxiliary service is deprecated, for a CUG, it will return an error when the
         * user tries to add any subscriber into that CUG.
         */
        if (aux != null && aux.getState() == AuxiliaryServiceStateEnum.DEPRECATED)
        {
            handleException(ctx, "Cannot add subscriber because Auxiliary is in deprecate state", null);
        }
        else
        {
            final String ban = cug.getBAN().trim();
            if (ban.length() != 0)
            {
                final Collection subs = getEligibleSubsFromAccount(ctx, ban);
                if (null != subs && !subs.isEmpty())
                {
                    final Iterator subs_itr = subs.iterator();
                    while (subs_itr.hasNext())
                    {
                        final Subscriber sub = (Subscriber) subs_itr.next();
                        final String msisdn = sub.getMSISDN();
                        ClosedSub closedSub = new ClosedSub();
                        closedSub.setPhoneID(msisdn);
                        cug.getSubscribers().put(msisdn, closedSub);
                    }
                }
                else
                {
                    handleException(ctx, "No Eligible Subscribers in Account [" + ban + "]", null);
                }
            }
        }
        return cug;
    }


    private void handleException(Context ctx, String message, Throwable t)
    {
        Exception exception = new IllegalStateException(message, t);
        handleException(ctx, exception);
    }


    private void handleException(Context ctx, Throwable t)
    {
        ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != excl)
        {
            excl.thrown(t);
        }
        new MinorLogMsg(this, t.getMessage(), t).log(ctx);
    }

    private static final Set<SubscriberStateEnum> SUBSCRIBER_STATES_RESTRICTGED = new HashSet<SubscriberStateEnum>(
            Arrays.asList(SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE, SubscriberStateEnum.MOVED));
} // class
