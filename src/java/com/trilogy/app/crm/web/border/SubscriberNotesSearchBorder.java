/*
 *  SubscriberNotesSearchBorder
 *
 */
 
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.filter.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.*;
import com.trilogy.framework.xhome.web.agent.*;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.renderer.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.webcontrol.*;
import com.trilogy.framework.xhome.xdb.XDBSupport;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import com.trilogy.framework.xhome.support.DateUtil;


/**
 * A Custom SearchBorder for Subscriber Notes.
 *
 * This will be generated from an XGen template in the future but for now
 * I'm still experimenting with the design.  Also, some common helper classes
 * will be created for each Search type.
 *
 * Add this Border before the WebController, not as one of either its
 * Summary or Detail borders.
 *
 **/
public class  SubscriberNotesSearchBorder
    extends SearchBorder
{
    public  SubscriberNotesSearchBorder(final Context context)
    {
        super(context, Note.class, new SubscriberNotesSearchWebControl());
       // TYPE
        addAgent(
            new WildcardSelectSearchAgent(NoteXInfo.TYPE, SubscriberNotesSearchXInfo.TYPE, false)

        );
        // SUBTYPE
        addAgent(
            new WildcardSelectSearchAgent(NoteXInfo.SUB_TYPE,SubscriberNotesSearchXInfo.SUB_TYPE, false)

        );
        // Agent
        addAgent(
            new SelectSearchAgent(NoteXInfo.AGENT, SubscriberNotesSearchXInfo.AGENT, false)
   
        );
    }
}

