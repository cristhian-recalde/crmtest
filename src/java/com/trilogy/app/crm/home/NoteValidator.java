/*
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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteType;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 * @author Aaron Gourley
 * @since 7.7
 */
public class NoteValidator implements Validator
{
    private static Validator instance_ = null;
    public static Validator instance()
    {
        if( instance_ == null )
        {
            instance_ = new NoteValidator();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        
        if( obj instanceof Note )
        {
            Note note = ((Note)obj);
            if( note.getNote() == null
                    || note.getNote().trim().length() == 0 )
            {
                exceptions.thrown(new MissingRequireValueException(NoteXInfo.NOTE));
            }
            String noteType = note.getType();
            try
            {
                if (null==noteType)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(NoteXInfo.TYPE, "Note type missing."));
                }
                else if (HomeSupportHelper.get(ctx).hasBeans(ctx, NoteType.class, noteType))
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(NoteXInfo.TYPE, "Invalid note type: " + noteType));
                } 
            }
            catch (HomeException e)
            {
                exceptions.thrown(new IllegalStateException("Error validating note type: " + e.getMessage(), e));
            }
        }
        
        exceptions.throwAll();
    }

}
