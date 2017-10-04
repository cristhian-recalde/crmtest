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
package com.trilogy.app.crm.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Principal;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.config.TFAClientConfig;
import com.trilogy.app.transferfund.soap.TransferfundSoapServiceStub;
import com.trilogy.app.transferfund.soap.data.xsd.AuthCredentials;
import com.trilogy.app.transferfund.soap.data.xsd.TransferRequestInput;
import com.trilogy.app.transferfund.soap.data.xsd.TransferRequestResponse;
import com.trilogy.app.transferfund.soap.interfaces.TransferRequest;
import com.trilogy.app.transferfund.soap.interfaces.TransferRequestResponseE;

/**
 * 
 * @author daniel.lee@redknee.com
 *
 */

public class AppTFAClient
    extends ContextAwareSupport
    implements PropertyChangeListener
{
    public class TransferFundsResponse
    {
        public int resultCode_;
        public String transactionId_;
        public long adjustmentAmount_;
        public long creditAmount_;
        public long debitAmount_;
    }

    public AppTFAClient(Context ctx)
        throws AgentException
    {
        setContext(ctx);
        TFAClientConfig cfg = (TFAClientConfig)getContext().get(TFAClientConfig.class);
        if(null == cfg)
        {
            throw new AgentException("System Error. TFA Client Config not found in Context.");
        }

        cfg.addPropertyChangeListener(this);

        soapClientURL_ = cfg.getURL();
        soapClientAuth_.setUserName(cfg.getUsername());
        soapClientAuth_.setPassWord(cfg.getPassword());
        loadSuccessCodes(cfg.getSuccessCodes());
        loadClient();
    }

    public TransferFundsResponse transferFunds(
        final Context ctx,
        short action,
        long amount,
        short amountType,
        String contributorChargingId,
        String currency,
        boolean expiryExtension,
        long transferTypeId,
        String recipientChargingId)
        throws Exception
    {
        TransferfundSoapServiceStub client = getClient();
        TransferRequestInput [] transferReq = new TransferRequestInput[1];
        transferReq[0] = new TransferRequestInput();
        transferReq[0].setAction(action);
        transferReq[0].setAmount(amount);
        transferReq[0].setAmountType(amountType);
        transferReq[0].setContributorChargingId(getChargingID(ctx, contributorChargingId));
        transferReq[0].setCurrencyType(currency);
        transferReq[0].setExpiryExtensionRequiredToRecipient(expiryExtension);
        transferReq[0].setTransferTypeId(transferTypeId);
        transferReq[0].setRecipientChargingId(getChargingID(ctx, recipientChargingId));

        TransferRequest req = new TransferRequest();
        req.setParam0(soapClientAuth_);
        req.setParam1(transferReq);
        req.setParam2(getAgentId(ctx));

        // send transfer request
        TransferRequestResponseE resp0 = client.transferRequest(req);

        TransferRequestResponse [] response = resp0.get_return();
        TransferFundsResponse ret = new TransferFundsResponse();

        ret.adjustmentAmount_ = response[0].getAdjustmentAmount();
        ret.creditAmount_ = response[0].getCreditAmount();
        ret.debitAmount_ = response[0].getDebitamount();
        ret.transactionId_ = response[0].getTransactionID();
        ret.resultCode_ = getResultCode(response[0].getTransferRequestException().getResponseCode());

        return ret;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(TFAClientConfig.URL_PROPERTY))
        {
            if(LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "Re-loading the TFA SOAP client with the URL [" + evt.getNewValue() + "]");
            }

            soapClientURL_ = (String)evt.getNewValue();
            loadClient();
        }
        else if (evt.getPropertyName().equals(TFAClientConfig.USERNAME_PROPERTY))
        {
            soapClientAuth_.setUserName((String)evt.getNewValue());
        }
        else if (evt.getPropertyName().equals(TFAClientConfig.PASSWORD_PROPERTY))
        {
            soapClientAuth_.setPassWord((String)evt.getNewValue());
        }
        else if (evt.getPropertyName().equals(TFAClientConfig.SUCCESSCODES_PROPERTY))
        {
            loadSuccessCodes((String)evt.getNewValue());
        }
    }

    private synchronized void loadClient()
    {
        try
        {
            soapClient_ = new TransferfundSoapServiceStub(soapClientURL_);
        }
        catch(Exception e)
        {
            soapClient_ = null;
            LogSupport.major(getContext(), this, "An error occured when trying to load the TFA SOAP client with URL [" + soapClientURL_ + "]", e);
        }
    }

    private synchronized TransferfundSoapServiceStub getClient()
        throws Exception
    {
        if(null != soapClient_)
        {
            return soapClient_;
        }
        else
        {
            throw new Exception("TFA SOAP client has not been properly initialized. Check the configuration.");
        }
    }

    protected String getAgentId(final Context ctx)
    {
        User user = (User)ctx.get(Principal.class);
        String agentId = "";
        if(null != user)
        {
            agentId = user.getId();
        }
        else
        {
            LogSupport.minor(ctx, this, "Unable to retrieve the AgentId from the Context.");
        }

        return agentId;
    }

    /**
     * 
     * This method reads in a comma seperated list of integers and parses them into an member variable array.
     * 
     * @param successCodes - a comma seperated list of integers
     */
    private void loadSuccessCodes(String successCodes)
    {
        String [] stringCodes = successCodes.split(",");
        successCodes_ = new int[stringCodes.length];
        for(int i = 0; i < stringCodes.length; i++)
        {
            try
            {
                successCodes_[i] = Integer.valueOf(stringCodes[i]).intValue();
            }
            catch(Exception e)
            {
                LogSupport.minor(getContext(), this, "There is a format error in the TFA Client Configuration.  Ensure that the success result codes is a comma seperated list of INTEGERS.");
            }
        }
    }

    /**
     * 
     * This method compares the given result code to the stored success result codes.
     * If there's a match it will return '0', otherwise it will return the result code as is.
     * 
     * @param resultCode - the reuslt code returned from AppTransferfund
     * @return - maps the success result codes to '0'.  Maintains the failure result codes.
     */
    private int getResultCode(int resultCode)
    {
        int ret = resultCode;
        for(int i = 0; i < successCodes_.length; i++)
        {
            if(successCodes_[i] == resultCode)
            {
                ret = 0;
                break;
            }
        }

        return ret;
    }


    /**
     * Gets the charging ID of the given identifier. This method assumes that a
     * blank identifier represents the "Operator" and will map to the
     * configurable Operator ID configured for the TFA client.
     *
     * @param context The operating context.
     * @param identifier The identifier of the contributor and/or recipient.
     * @return The appropriate charging ID.
     */
    private String getChargingID(final Context context, final String identifier)
    {
        final String chargingID;
        if (identifier == null || identifier.trim().length() == 0)
        {
            final TFAClientConfig config = (TFAClientConfig)context.get(TFAClientConfig.class);
            chargingID = Integer.toString(config.getOperatorChargingID());
        }
        else
        {
            chargingID = identifier;
        }

        return chargingID;
    }

    
    private AuthCredentials soapClientAuth_ = new AuthCredentials();
    private int [] successCodes_ = new int[0];
    private volatile String soapClientURL_ = "";
    private TransferfundSoapServiceStub soapClient_ = null;
}
