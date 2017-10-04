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
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.auth.SimplePermission;

/**
 * Overrides the BAN and cmd to call the Wallet Preview menu
 * 
 * TODO - 2009-06-26 - Refactor this along with ReportPreviewAction/ReportDeliveryAction, all subclasses of each, 
 *                     InvoiceViewerRequestServicer, InvoicePreviewFallbackBorder, InvoiceAccountNoteWebBorder,
 *                     all wallet reports derivations of each.  Together, it is all terrible and not maintainable.
 *                     
 * @author arturo.medina@redknee.com
 *
 */
public class WalletReportPreviewAction extends ReportPreviewAction
{
    private static final long serialVersionUID = -6882718418761072545L;
    
    /**
     * Default constructor that initializes the wallet report preview screen
     */
    public WalletReportPreviewAction()
    {
        super("appCRMWalletReportViewerMenu");
    }
    
    /**
     * Default constructor that initializes the wallet report preview screen
     */
    public WalletReportPreviewAction(String action, String label)
    {
        super(action, label, "appCRMWalletReportViewerMenu");
    }

    /**
     * {@inheritDoc}
     */
    public WalletReportPreviewAction(SimplePermission permission,
            String action, String label)
    {
        super(permission, action, label, "appCRMWalletReportViewerMenu");
    }
}
