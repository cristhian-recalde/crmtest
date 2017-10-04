package com.trilogy.app.crm.web.action;

import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.web.border.InvoiceAccountNoteWebBorder;


public class ReceiptViewAction extends SimpleWebAction
{
    
    public ReceiptViewAction()
    {
        super();
        cmdName_ = "appCRMReceiptViewerMenu";
    }
    
    public ReceiptViewAction(String cmdDelegate)
    {
        super();
        cmdName_ = cmdDelegate;
    }


    /**
     * Creates a new "view" action.
     *
     * @param action The name of the action associated with this link.
     * @param label The label of the link.
     */
    public ReceiptViewAction(final String action, final String label, String cmdDelegate)
    {
        super(action, label);
        cmdName_ = cmdDelegate;
    }


    /**
     * Creates a new "preview" action.
     *
     * @param permission The permissions required for the action.
     * @param action The name of the action associated with this link.
     * @param label The label of the link.
     */
    public ReceiptViewAction(final Permission permission, final String action, final String label, String cmdDelegate)
    {
        this(action, label, cmdDelegate);
        setPermission(permission);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Link modifyLink(Context ctx, Object bean, Link link)
    {
        Link newLink = super.modifyLink(ctx, bean, link);

        // Hide everything from the action's other page
        newLink.addRaw("header", "hide");
        newLink.addRaw("border", "hide");
        newLink.addRaw("menu", "hide");

        // Make it so that this action's link brings you to another page
        newLink.addRaw("cmd", cmdName_);

        // Make the destination page think that its ReceiptViewerForm is populated 
        final String transactionId;
        if (bean instanceof Transaction)
        {
            transactionId = String.valueOf(((Transaction) bean).getReceiptNum());
        }
        else
        {
            transactionId = WebAgents.getParameter(ctx, "key");
        }
        newLink.addRaw(".transactionId", transactionId);
        
        // Make the destination page think that its view button was clicked
        newLink.addRaw(DefaultButtonRenderer.BUTTON_KEY, VIEW_BUTTON);
        newLink.addRaw(VIEW_BUTTON + DefaultButtonRenderer.BUTTON_KEY + ".x", "submit");

        // Add a note message to the link.  This is used by the InvoiceAccountNoteWebBorder to
        // automatically create an account note to indicate an access attempt.
        newLink.addRaw(InvoiceAccountNoteWebBorder.MESSAGE, " - Receipt generation - ");
        
        return newLink;
    }  
    /**
     * The abstract command name
     */
    protected final String cmdName_;
    
    private final String VIEW_BUTTON = "View";
}
