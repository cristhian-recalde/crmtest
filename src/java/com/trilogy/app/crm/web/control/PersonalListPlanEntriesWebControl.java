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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanEntrySelection;
import com.trilogy.app.crm.bean.PersonalListPlanEntrySelectionTableWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.NoActionsWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * Provides a web control for presenting PersonalListPlan MSISDN selection
 * options on the subscriber pane.  The intent is that this web control present
 * for the subscriber all available current selections plus just enough blank
 * entries to hit their PLP maximum.
 *
 * @author gary.anderson@redknee.com
 */
public class PersonalListPlanEntriesWebControl extends ProxyWebControl
{
    /**
     * Creates a new PersonalListPlanEntriesWebControl.
     */
    public PersonalListPlanEntriesWebControl()
    {
        msisdnWebControl = new NoActionsWebControl(createTableWebControl());
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(
        Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        final Subscriber subscriber = (Subscriber)context.get(AbstractWebControl.BEAN);

        Map plpList = subscriber.getPersonalListPlanEntries(); 
        
        if ( plpList != null )
        {   
            for (Iterator it = plpList.keySet().iterator(); it.hasNext(); )
            {
                Long key = (Long) it.next(); 
                toWeb(context, out, name, key.longValue(), (Set) plpList.get(key)); 
            }
        }
        //super.toWeb(context, out, name, obj); 
    }
    
    
    public void toWeb( Context context, 
            final PrintWriter out, 
            final String name,
            final long plpId,
            final Set msisdnSet
        )
    {
        context = context.createSubContext();
        context.setName(this.getClass().getName());

        final Iterator msisdnSetIterator = msisdnSet.iterator();

        // ArrayList<PersonalListPlanEntrySelection>
        final List msisdns = new ArrayList(msisdnSet.size());
        for (int n = 0; n < msisdnSet.size(); ++n)
        {
            final PersonalListPlanEntrySelection selection =
                new PersonalListPlanEntrySelection();
            selection.setSelectionIdentifier(n);
            selection.setMsisdn((String)msisdnSetIterator.next());
            selection.setChecked(true);

            msisdns.add(selection);
        }

        PersonalListPlan plan = null; 
        
        try 
        {
            plan= PersonalListPlanSupport.getPLPByID(context, plpId);
        } 
        catch (HomeException e)
        {
            
        }
        // Set the size of the entry table.
        {
            final int blanks;
            if (plan != null)
            {
                final int maxSubscriberCount =
                    Math.max(plan.getMaxSubscriberCount(), msisdnSet.size());

                final int availableEntries = maxSubscriberCount - msisdnSet.size();

                if (availableEntries == 0)
                {
                    blanks = -1;
                }
                else
                {
                    blanks = availableEntries;
                }
            }
            else
            {
                blanks = -1;
            }

            // NUM_OF_BLANKS controls the number of blank entries that appear at the
            // bottom of the list (meant for adding to the list).  The above
            // code ensures that blanks is never set to zero because doing so
            // adds a "New: " checkbox below the table.
            context.put(AbstractWebControl.NUM_OF_BLANKS, blanks);
        }

        if ( plan != null)
        {
            out.print("<font size=2>" + plan.getName()+ "</font>"); 
            msisdnWebControl.toWeb(context, out, name+"-" + plpId, msisdns);
        }   
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(
        final Context context,
        final ServletRequest request,
        final String name)
    {
        // The web control will only return to us a collection of the
        // PersonalListPlanEntrySelection that are checked on the subscriber
        // profile.  From that list, we need to derive a list of
        // SubscriberAuxiliaryServices to return to the subscriber.
        Map ret = new HashMap(); 
        final Subscriber subscriber = (Subscriber)context.get(AbstractWebControl.BEAN);

        Collection plps = subscriber.getPersonalListPlan(); 
        
        for ( Iterator it = plps.iterator(); it.hasNext();)
        {
            Long plpId = (Long) it.next();
            ArrayList selections = new ArrayList();
            msisdnWebControl.fromWeb(context, selections, request, name + "-"+plpId.longValue());
            final TreeSet msisdnSet = new TreeSet();
            for (int n = 0; n < selections.size(); ++n)
            {
                final PersonalListPlanEntrySelection selection =
                    (PersonalListPlanEntrySelection)selections.get(n);

                final String msisdn = selection.getMsisdn().trim();

                if (msisdn.length() != 0)
                {
                    msisdnSet.add(msisdn);
                }
            }
            
            ret.put(plpId, msisdnSet);
            
        }   
        subscriber.setPersonalListPlanEntries(ret); 

        return null;
    }


    /**
     * Creates the specialized TableWebControl that modifies the way the default
     * TableWebControl handles the accept/reject checkboxes that appear in the
     * first column of the table.
     *
     * @return A specialized TableWebControl.
     */
    private static WebControl createTableWebControl()
    {
        final WebControl control =
            new PersonalListPlanEntrySelectionTableWebControl()
            {
                // Used to look-up the actual mode in the context.
                public static final String CUSTOM_MODE_KEY = "REALMODE";

                // INHERIT
                public void toWeb(
                    Context context,
                    final PrintWriter out,
                    final String name,
                    final Object obj)
                {
                    // If the mode is DISPLAY, then the TableWebController will
                    // not add the column of accept/reject checkboxes.
                    final int mode = context.getInt("MODE", DISPLAY_MODE);
                    if (mode == DISPLAY_MODE)
                    {
                        context = context.createSubContext();
                        context.put(CUSTOM_MODE_KEY, mode);
                        context.put("MODE", EDIT_MODE);
                    }

                    super.toWeb(context, out, name, obj);
                }

                // INHERIT
                public void outputCheckBox(
                    final Context context,
                    final PrintWriter out,
                    final String name,
                    final Object bean,
                    final boolean isChecked)
                {
                    // We're overriding the normal usage of the accept/reject
                    // checkboxes to more explicitly associate
                    // acception/rejection with the beans.  The motivation for
                    // this behavior is that many table entries need to appear
                    // that are not selected.  Normal behavior of the table is
                    // to remove those entries that are not selected.
                    final PersonalListPlanEntrySelection selection =
                        (PersonalListPlanEntrySelection)bean;

                    // We always need the hidden value of the identifier of the
                    // bean.
                    out.print("<input type=\"hidden\" name=\"");
                    out.print(name);
                    out.print(SEPERATOR);
                    out.print("selectionIdentifier\" value=\"");
                    out.print(selection.getSelectionIdentifier());
                    out.println("\" />");

                    final int mode = context.getInt(CUSTOM_MODE_KEY, EDIT_MODE);
                    if (mode == DISPLAY_MODE && selection.isChecked())
                    {
                        // In the case that the actual mode is DISPLAY, we don't
                        // want editable checkboxes.  Instead, draw an "X".
                        out.print(" <td> &nbsp;<b>X</b><input type=\"hidden\" name=\"");
                        out.print(name);
                        out.print(SEPERATOR);
                        out.print("_enabled\" value=\"X\" />");

                        out.println("</td>");
                    }
                    else if (mode == DISPLAY_MODE)
                    {
                        // In the case that the actual mode is DISPLAY, we don't
                        // want editable checkboxes.  Instead, draw a blank.
                        out.print("<td>&nbsp;</td>");
                    }
                    else
                    {
                        // In any other actual mode besides DISPLAY, the default
                        // TableWebControl does what we want.
                        super.outputCheckBox(context, out, name, bean, selection.isChecked());
                    }
                }
            };

        return control;
    }

    static WebControl msisdnWebControl; 

} // class
