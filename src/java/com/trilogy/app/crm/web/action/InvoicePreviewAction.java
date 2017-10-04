package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.auth.SimplePermission;

/**
 * Overrides the BAN and cmd to call the Invoice Preview menu
 * 
 * TODO - 2009-06-26 - Refactor this along with ReportPreviewAction/ReportDeliveryAction, all subclasses of each, 
 *                     InvoiceViewerRequestServicer, InvoicePreviewFallbackBorder, InvoiceAccountNoteWebBorder,
 *                     all wallet reports derivations of each.  Together, it is all terrible and not maintainable.
 *                     
 * @author arturo.medina@redknee.com
 *
 */
public class InvoicePreviewAction extends ReportPreviewAction
{
    private static final long serialVersionUID = 1471297621237357279L;
    
    /**
     * Default constructor that initializes the invoice preview screen
     */
    public InvoicePreviewAction()
    {
        super("appCRMInvoiceViewerMenu");
    }
    
    /**
     * Default constructor that initializes the invoice preview screen
     */
    public InvoicePreviewAction(String action, String label)
    {
        super(action, label, "appCRMInvoiceViewerMenu");
    }

    /**
     * {@inheritDoc}
     */
    public InvoicePreviewAction(SimplePermission permission,
            String action, String label)
    {
        super(permission, action, label, "appCRMInvoiceViewerMenu");
    }

}
