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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_1.types.note.NoteReference;

/**
 * Adapts Note object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class NoteToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptNoteToReference(ctx, (Note) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static NoteReference adaptNoteToReference(final Context ctx, final Note note)
    {
    	return adaptNoteToReference(ctx, note, new NoteReference());
    }
    
    public static NoteReference adaptNoteToReference(final Context ctx, final Note note, final NoteReference noteReference)
    {
    	noteReference.setIdentifier(Long.valueOf(note.getId()));
    	noteReference.setAccountOrSubscriptionID(note.getIdIdentifier());
    	noteReference.setType(note.getType());
    	noteReference.setSubType(note.getSubType());
    	noteReference.setCreated(CalendarSupportHelper.get().dateToCalendar(note.getCreated()));
    	noteReference.setAgent(note.getAgent());
    	noteReference.setNote(note.getNote());
    	noteReference.setShowOnInvoice(note.isShowOnInvoice());
    	return noteReference;
    }
    
}
