/*
 *  AccountNotesSearchBorder
 *
 */
 
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.AccountNotesSearchWebControl;
import com.trilogy.app.crm.bean.AccountNotesSearchXInfo;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

/**
 * A Custom SearchBorder for Account Notes.
 *
 * This will be generated from an XGen template in the future but for now
 * I'm still experimenting with the design.  Also, some common helper classes
 * will be created for each Search type.
 *
 * Add this Border before the WebController, not as one of either its
 * Summary or Detail borders.
 *
 **/
public class  AccountNotesSearchBorder
    extends SearchBorder
{
    public  AccountNotesSearchBorder(Context context)
    {
        super(context, Note.class, new AccountNotesSearchWebControl());
      
        setHomeKey("AccountNoteHome");
        
        // TYPE
        addAgent(new WildcardSelectSearchAgent(NoteXInfo.TYPE, AccountNotesSearchXInfo.TYPE, false));
        // SUBTYPE
        addAgent(new WildcardSelectSearchAgent(NoteXInfo.SUB_TYPE, AccountNotesSearchXInfo.SUB_TYPE, false));
      // Limit
      addAgent(new LimitSearchAgent(AccountNotesSearchXInfo.LIMIT));
    }
}

