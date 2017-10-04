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
package com.trilogy.app.crm.transfer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AppTFAClient;
import com.trilogy.app.crm.config.TFAClientConfig;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Provides a request servicer for initiating a transfer between subscribers.
 *
 * @author gary.anderson@redknee.com
 */
public class TransferFundsRequestServicer
    implements RequestServicer
{
    /**
     * Provides a key prefix for queries into the message manager to extract
     * result code descriptions. The result code is appended to this prefix to
     * obtain the full key.
     */
    public static final String RESULT_DESCRIPTION_KEY_PREFIX = "TransferFundsRequestServicer.Result";


    /**
     * {@inheritDoc}
     */
    public void service(final Context parentContext, final HttpServletRequest request,
        final HttpServletResponse response)
        throws IOException
    {
        final Context context = parentContext.createSubContext();
        context.setName(getClass().getName());
        final MessageMgr manager = new MessageMgr(context, getClass());
        final HTMLExceptionListener exceptionListener = new HTMLExceptionListener(manager);
        context.put(ExceptionListener.class, exceptionListener);

        // We write to a buffer rather than directly to the response so that
        // any error messages can be written to the top of the screen rather
        // than the bottom.
        final StringWriter buffer = new StringWriter();
        final PrintWriter out = new PrintWriter(buffer);

        final TransferFundsWebControl wc = new TransferFundsWebControl();

        final TransferFunds transfer = new TransferFunds();
        final FormRenderer formRenderer = (FormRenderer)context.get(FormRenderer.class, DefaultFormRenderer.instance());

        formRenderer.Form(out, context);
        // The addition of a simple <table> prevents the form from expanding to
        // fit the width of the browser screen.
        out.println("<table><tr><td>");

        final ButtonRenderer buttonRenderer =
            (ButtonRenderer)context.get(ButtonRenderer.class, DefaultButtonRenderer.instance());

        if (buttonRenderer.isButton(context, CONFIRM_IDENTITY))
        {
            initializeTransfer(context, transfer, request, wc);
            if (validIdentity(context, transfer))
            {
                showCalculateForm(context, out, transfer, wc, buttonRenderer);
            }
            else
            {
                showConfirmIdentityForm(context, out, transfer, wc, buttonRenderer);
            }
        }
        else if (buttonRenderer.isButton(context, CALCULATE))
        {
            initializeTransfer(context, transfer, request, wc);
            if (validAmounts(context, transfer))
            {
                transfer(context, transfer, false);

                if (transfer.getResults().getResultCode() == 0)
                {
                    transfer.setResults(null);
                    showCommitForm(context, out, transfer, wc, buttonRenderer);
                }
                else
                {
                    showCalculateForm(context, out, transfer, wc, buttonRenderer);
                }
            }
            else
            {
                showCalculateForm(context, out, transfer, wc, buttonRenderer);
            }
        }
        else if (buttonRenderer.isButton(context, COMMIT))
        {
            initializeTransfer(context, transfer, request, wc);
            transfer(context, transfer, true);
            if (transfer.getResults().getResultCode() == 0)
            {
                showResultsForm(context, out, transfer, wc);
            }
            else
            {
                showCommitForm(context, out, transfer, wc, buttonRenderer);
            }
        }
        else
        {
            showConfirmIdentityForm(context, out, transfer, wc, buttonRenderer);
        }

        BeanWebController.outputHelpLink(context, null, null, out, buttonRenderer);

        out.println("</td></tr></table>");
        formRenderer.FormEnd(out);

        final PrintWriter responseOut = response.getWriter();

        if (exceptionListener.hasErrors())
        {
            exceptionListener.toWeb(context, responseOut, null, null);
        }

        responseOut.write(buffer.getBuffer().toString());
    }


    /**
     * Initializes the given TransferFunds object.
     *
     * @param parentContext The operating context.
     * @param transfer The TransferFunds object.
     * @param request The HTTP request with data for the fromWeb() method.
     * @param wc The web-control used to get data from the request.
     */
    private void initializeTransfer(final Context parentContext, final TransferFunds transfer,
        final HttpServletRequest request, final TransferFundsWebControl wc)
    {
        final Context context = parentContext.createSubContext();

        // NOTE: Because of the use of "final" web controls, the mode must be
        // "CREATE" for the bean to be read in through fromWeb().
        context.put("MODE", OutputWebControl.CREATE_MODE);

        wc.fromWeb(context, transfer, request, "");

        try
        {
            updateParticipants(context, transfer);
        }
        catch (final HomeException exception)
        {
            noteException(context, exception);
        }
    }


    /**
     * Shows the form for the agent to confirm the identity of the contributor
     * and recipient.
     *
     * @param context The operating context.
     * @param out The PrintWriter to which screen elements should be written.
     * @param transfer The transfer parameters.
     * @param wc The webcontrol used to display the transfer parameters.
     * @param buttonRenderer The renderer to create buttons for progression.
     */
    private void showConfirmIdentityForm(final Context context, final PrintWriter out, final TransferFunds transfer,
        final TransferFundsWebControl wc, final ButtonRenderer buttonRenderer)
    {
        context.put("MODE", OutputWebControl.EDIT_MODE);

        transfer.setState(TransferFundStateEnum.SUBCRIPTION_IDENTIFICATION);

        AbstractWebControl.setMode(context, TransferFundsXInfo.CONTRIBUTOR_DETAILS, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.RECIPIENT_DETAILS, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.AMOUNT_TYPE, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.AMOUNT, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.TRANSFER_ADJUSTMENT_AMOUNT, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.DEBIT_AMOUNT, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.CREDIT_AMOUNT, ViewModeEnum.NONE);

        final boolean errorScenario = transfer.getResults() != null && transfer.getResults().getResultCode() != 0;

        if (errorScenario)
        {
            AbstractWebControl.setMode(context, TransferFundsXInfo.AMOUNT_TYPE, ViewModeEnum.READ_ONLY);
            AbstractWebControl.setMode(context, TransferFundsXInfo.AMOUNT, ViewModeEnum.READ_ONLY);
        }

        wc.toWeb(context, out, "", transfer);

        if (!errorScenario)
        {
            buttonRenderer.inputButton(out, context, this.getClass(), CONFIRM_IDENTITY, true);
        }
    }


    /**
     * Validates the identity information in the transfer parameters.
     *
     * @param context The operating context.
     * @param transfer The transfer parameters.
     * @return True if the identity information is valid; false otherwise.
     */
    private boolean validIdentity(final Context context, final TransferFunds transfer)
    {
        boolean valid = true;

        // Check recipient.
        if (valid)
        {
            final TFAClientConfig config = (TFAClientConfig)context.get(TFAClientConfig.class);

            final String recipient = transfer.getRecipientMobileNumber();
            valid = config.getAllowTransferToOperator()
                || (recipient != null && recipient.length() > 0);

            if (!valid)
            {
                noteException(context, new IllegalPropertyArgumentException(TransferFundsXInfo.RECIPIENT_MOBILE_NUMBER,
                    "Invalid number."));
            }
        }

        return valid;
    }


    /**
     * Shows the form for the agent to enter base amounts for the calculate of
     * transfer amount.
     *
     * @param context The operating context.
     * @param out The PrintWriter to which screen elements should be written.
     * @param transfer The transfer parameters.
     * @param wc The webcontrol used to display the transfer parameters.
     * @param buttonRenderer The renderer to create buttons for progression.
     */
    private void showCalculateForm(final Context context, final PrintWriter out, final TransferFunds transfer,
        final TransferFundsWebControl wc, final ButtonRenderer buttonRenderer)
    {
        context.put("MODE", OutputWebControl.EDIT_MODE);

        transfer.setState(TransferFundStateEnum.IDENTIFICATION_CONFIRMATION);

        final boolean errorScenario = transfer.getResults() != null && transfer.getResults().getResultCode() != 0;

        if (errorScenario)
        {
            AbstractWebControl.setMode(context, TransferFundsXInfo.AMOUNT_TYPE, ViewModeEnum.READ_ONLY);
            AbstractWebControl.setMode(context, TransferFundsXInfo.AMOUNT, ViewModeEnum.READ_ONLY);
        }

        AbstractWebControl.setMode(context, TransferFundsXInfo.TRANSFER_ADJUSTMENT_AMOUNT, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.DEBIT_AMOUNT, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.CREDIT_AMOUNT, ViewModeEnum.NONE);

        wc.toWeb(context, out, "", transfer);

        if (!errorScenario)
        {
            buttonRenderer.inputButton(out, context, this.getClass(), CALCULATE, true);
        }
    }


    /**
     * Validates the base amounts provided for calculation.
     *
     * @param context The operating context.
     * @param transfer The transfer parameters.
     * @return True if the amount provided are valid; false otherwise.
     */
    private boolean validAmounts(final Context context, final TransferFunds transfer)
    {
        return true;
    }


    /**
     * Shows the form for the agent to commit the transfer amount.
     *
     * @param context The operating context.
     * @param out The PrintWriter to which screen elements should be written.
     * @param transfer The transfer parameters.
     * @param wc The webcontrol used to display the transfer parameters.
     * @param buttonRenderer The renderer to create buttons for progression.
     */
    private void showCommitForm(final Context context, final PrintWriter out, final TransferFunds transfer,
        final TransferFundsWebControl wc, final ButtonRenderer buttonRenderer)
    {
        context.put("MODE", OutputWebControl.EDIT_MODE);

        transfer.setState(TransferFundStateEnum.CONFIGURING_AMOUNT);

        wc.toWeb(context, out, "", transfer);
        buttonRenderer.inputButton(out, context, this.getClass(), COMMIT, true);
    }


    /**
     * Shows the form for the agent to review the results of the transfer.
     *
     * @param context The operating context.
     * @param out The PrintWriter to which screen elements should be written.
     * @param transfer The transfer parameters.
     * @param wc The webcontrol used to display the transfer parameters.
     */
    private void showResultsForm(final Context context, final PrintWriter out, final TransferFunds transfer,
        final TransferFundsWebControl wc)
    {
        context.put("MODE", OutputWebControl.EDIT_MODE);

        AbstractWebControl.setMode(context, TransferFundsXInfo.CONTRIBUTOR_DETAILS, ViewModeEnum.NONE);
        AbstractWebControl.setMode(context, TransferFundsXInfo.RECIPIENT_DETAILS, ViewModeEnum.NONE);

        transfer.setState(TransferFundStateEnum.AMOUNT_CONFIRMATION);

        wc.toWeb(context, out, "", transfer);
    }


    /**
     * Updates the participants data within the given transfer.
     *
     * @param context The operating context.
     * @param transfer The transfer information, including participant data.
     * @throws HomeException Thrown if there are problems accessing data in the
     * context.
     */
    private void updateParticipants(final Context context, final TransferFunds transfer)
        throws HomeException
    {
        final Home transferTypesHome = (Home)context.get(TransferTypeHome.class);
        final TransferType tranferType =
            (TransferType)transferTypesHome.find(context, Long.valueOf(transfer.getTransferType()));

        updateParticipant(context, transfer.getContributorMobileNumber(), tranferType.getContributorTypeID(),
            transfer.getContributorDetails());

        updateParticipant(context, transfer.getRecipientMobileNumber(), tranferType.getRecipientTypeID(),
            transfer.getRecipientDetails());
    }


    /**
     * Updates the given Participant information for the given MSISDN when the
     * MSISDN is in active use by CRM+.
     *
     * @param context The operating context.
     * @param mobileNumber The mobile number of the participant.
     * @param subscriptionTypeID The subscription type of the participant.
     * @param details The participant data.
     */
    private void updateParticipant(final Context context, final String mobileNumber, final long subscriptionTypeID,
        final Participant details)
    {
        if (mobileNumber == null || mobileNumber.trim().length() == 0)
        {
            return;
        }

        Subscriber subscriber = null;

        try
        {
            subscriber = SubscriberSupport.lookupSubscriberForMSISDN(context, mobileNumber, subscriptionTypeID, null);
        }
        catch (final HomeException exception)
        {
            // There's no way to distinguish "MSISDN not found" from any
            // other exception.
            subscriber = null;
        }
        catch (final IllegalArgumentException exception)
        {
            // There's no way to distinguish "subscriber not found" from any
            // other exception.
            subscriber = null;
        }

        if (subscriber != null)
        {
            details.setSubscriberAccount(subscriber.getBAN());
            details.setSubscription(subscriber.getId());
        }
        else
        {
            final String unknown = "Unknown";
            details.setSubscriberAccount(unknown);
            details.setSubscription(unknown);
        }
    }


    /**
     * Performs the transfer of funds, or simply the calculation and validation.
     *
     * @param context The operating context.
     * @param transfer The transfer information.
     * @param commit True if the transfer is meant to be committed; false
     * otherwise (calculate and validate only).
     */
    private void transfer(final Context context, final TransferFunds transfer, final boolean commit)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("About to initiate transfer with commit==");
            builder.append(commit);
            builder.append(": ");
            builder.append(transfer);
            new DebugLogMsg(this, builder.toString(), null).log(context);
        }

        // NOTE: This is the method that the XCurrencyWebControl uses to get the
        // currency displayed in the form, and so is reused here. In the future,
        // we will likely have to allow currency to be explicitly specified.
        final Currency currency = (Currency)context.get(Currency.class, Currency.DEFAULT);

        // These are the parameters required by TFA.
        final long transferTypeID = transfer.getTransferType();
        final String contributorChargingId = transfer.getContributorMobileNumber();
        final String recipientChargingId = transfer.getRecipientMobileNumber();
        final long amount = transfer.getAmount();
        final String currencyType = currency.getCode();
        final short amountType = (short)transfer.getAmountType();
        final short action;
        if (commit)
        {
            // CONFIRMED TRANSFER
            action = 1;
        }
        else
        {
            // CALCULATE ONLY
            action = 2;
        }

        final TFAClientConfig config = (TFAClientConfig)context.get(TFAClientConfig.class);
        final boolean expiryExtentionRequiredToRecipient = config.getExpiryExtentionRequiredToRecipient();

        final AppTFAClient client = getTFAClient(context);
        if (null != client)
        {
            try
            {
                final AppTFAClient.TransferFundsResponse resp =
                    client.transferFunds(context, action, amount, amountType, contributorChargingId, currencyType,
                        expiryExtentionRequiredToRecipient, transferTypeID, recipientChargingId);

                if (resp.resultCode_ == 0)
                {
                    transfer.setTransferAdjustmentAmount(resp.adjustmentAmount_);
                    transfer.setCreditAmount(resp.creditAmount_);
                    transfer.setDebitAmount(resp.debitAmount_);
                }

                final TransferFundsResults results = new TransferFundsResults();
                results.setResultCode(resp.resultCode_);
                results.setResultDescription(getResultDescription(context, resp.resultCode_));

                if (commit)
                {
                    results.setTransactionID(resp.transactionId_);
                }

                transfer.setResults(results);
            }
            catch (final Exception e)
            {
                noteException(context, e);

                final TransferFundsResults results = new TransferFundsResults();
                results.setResultCode(-1);
                results.setResultDescription("Could not continue due to exception: " + e.getMessage());
                transfer.setResults(results);
            }
        }
        else
        {
            noteException(context, new IllegalStateException("Failed to find a transfer funds client."));

            final TransferFundsResults results = new TransferFundsResults();
            results.setResultCode(-1);
            results.setResultDescription("No connection available for initiating funds transfer.");
            transfer.setResults(results);
        }
    }


    /**
     * Gets the TFA client used to perform the transfer.
     *
     * @param context The operating context.
     * @return The TFA client used to perform the transfer.
     */
    private AppTFAClient getTFAClient(final Context context)
    {
        final AppTFAClient client = (AppTFAClient)context.get(AppTFAClient.class);

        return client;
    }


    /**
     * Provides a configurable mapping from result code to on-screen message.
     *
     * @param context The operating context.
     * @param resultCode The result code.
     * @return A textual description of the result code.
     */
    private String getResultDescription(final Context context, final int resultCode)
    {
        final String defaultDescription;
        if (resultCode == 0)
        {
            defaultDescription = "[{0}] Success.";
        }
        else
        {
            defaultDescription = "[{0}] General Failure.";
        }

        final String key = RESULT_DESCRIPTION_KEY_PREFIX + resultCode;
        final Object[] args =
        {
            Integer.toString(resultCode),
        };

        final MessageMgr messages = new MessageMgr(context, getClass());
        final String description = messages.get(key, defaultDescription, args);
        return description;
    }


    /**
     * Notes the given exception by adding it to the exception listener, and by
     * creating a minor log message.
     *
     * @param context The operating context.
     * @param throwable The exception to note.
     */
    private void noteException(final Context context, final Throwable throwable)
    {
        final ExceptionListener listener = (ExceptionListener)context.get(ExceptionListener.class);
        if (listener != null)
        {
            listener.thrown(throwable);
        }

        new MinorLogMsg(this, "Failure detected while collecting transfer data.", throwable).log(context);
    }


    /**
     * The label used for the "Confirm ID" button.
     */
    private static final String CONFIRM_IDENTITY = "Confirm ID";

    /**
     * The label used for the "Calculate Amounts" button.
     */
    private static final String CALCULATE = "Calculate Amounts";

    /**
     * The label used for the "Commit Transfer" button.
     */
    private static final String COMMIT = "Commit Transfer";
}
