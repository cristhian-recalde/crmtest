package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


public class ReceiptSubscriberNoteWebBorder implements Border
{


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.web.border.Border#service(com.redknee.framework.xhome.context.Context, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.redknee.framework.xhome.webcontrol.RequestServicer)
     */
    public void service(Context ctx, HttpServletRequest req,
            HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException 
    {
        String message = req.getParameter(InvoiceAccountNoteWebBorder.MESSAGE);
        
        if ((message != null))
        {
            String primaryKey = req.getParameter(".transactionId");
            
            Transaction transaction = CoreTransactionSupportHelper.get(ctx).getTransaction(ctx, primaryKey);
            User user = (User) ctx.get(Principal.class);
            String principal = SYSTEM;
            
            if (user != null)
            {
                principal = user.getId();
            }
            
            if (transaction != null)
            {
                addNote(ctx, transaction, principal, message);
            }

        }
        delegate.service(ctx, req, res);
    }

    public void addNote(Context ctx, Transaction transaction, String principal, String message) 
    {
        Subscriber subscriber;
        try 
        {
            subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, transaction.getSubscriberID());
            
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, subscriber.getId(), principal + message + transaction.getReceiptNum() + " - " + new Date(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.RECEIPT);
        }
        catch (HomeException e) 
        {
            LogSupport.major(ctx, this, "Error when trying to add a note for receipt " + XBeans.getIdentifier(transaction) + " : " + e.getMessage(), e);
        }
    }

    private static final String SYSTEM = "system";

}
