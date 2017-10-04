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
package com.trilogy.app.crm.poller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.notesdetail.NotesDetail;
import com.trilogy.app.crm.config.NotesPollerConfig;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;

/**
 * Creates a Notes Details
 *
 * @author skularajasingham
 */
public class NotesDetailCreator implements Constants
{

    public NotesDetailCreator(Context ctx)
    {
        super();

        confIndices = (NotesPollerConfig) ctx.get(NotesPollerConfig.class);
        if (confIndices == null)
        {
            new EntryLogMsg(13147, this, "", "", new Object[]{confIndices}, null).log(ctx);
            LogSupport.crit(ctx, this,
                    "Notes ER Indices are not correctly configured.");
        }
        else
        {
            validIndices = setErIndices(ctx, confIndices);   // Safe to call in the Constructor ??
        }
    }

    public String truncateField(String str, int len)
    {
        if (str==null) return null;
        
        return (str.length()>len?str.substring(0, len):str);
    }
    public NotesDetail createNotesDetail(Context ctx, ProcessorInfo info,
                                       List params) throws ParseException, HomeException, AgentException
    {
        try 
        {
            CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),this);
        } 
        catch ( FilterOutException e)
        {
            return null; 
        }

        new DebugLogMsg(this, "\n\n\t Params after processing = " + params + "\n\n", null).log(ctx);


        NotesDetail note = new NotesDetail();

        if (validIndices)
        {      
            note.setErid_value(retrieveValue(params, NOTES_ER_INDEX_CONFIGURED, this.NOTES_ER_INDEX, 
                    NotesDetail.ERID_VALUE_WIDTH, confIndices.getErid_default()));

            note.setSpid_value(retrieveValue(params, NOTES_SPID_INDEX_CONFIGURED, this.NOTES_SPID_INDEX, 
                    NotesDetail.SPID_VALUE_WIDTH, confIndices.getSpid_default()));

            note.setMsisdn_value(retrieveValue(params, NOTES_MSISDN_INDEX_CONFIGURED, this.NOTES_MSISDN_INDEX, 
                    NotesDetail.MSISDN_VALUE_WIDTH, confIndices.getMsisdn_default()));
            
            note.setBan_value(retrieveValue(params, NOTES_BAN_INDEX_CONFIGURED, this.NOTES_BAN_INDEX, 
                    NotesDetail.BAN_VALUE_WIDTH, confIndices.getBan_defaul()));
            
            note.setNotetype_value(retrieveValue(params, NOTES_NOTETYPE_INDEX_CONFIGURED, this.NOTES_NOTETYPE_INDEX, 
                    NotesDetail.NOTETYPE_VALUE_WIDTH, confIndices.getNotetype_default()));
            
            note.setSubtype_value(retrieveValue(params, NOTES_SUBTYPE_INDEX_CONFIGURED, this.NOTES_SUBTYPE_INDEX, 
                    NotesDetail.SUBTYPE_VALUE_WIDTH, confIndices.getSubtype_default()));
            
            note.setAgent_value(retrieveValue(params, NOTES_AGENT_INDEX_CONFIGURED, this.NOTES_AGENT_INDEX, 
                    NotesDetail.AGENT_VALUE_WIDTH, confIndices.getAgent_default()));
            
            note.setSubject_value(retrieveValue(params, NOTES_SUBJECT_INDEX_CONFIGURED, this.NOTES_SUBJECT_INDEX, 
                    NotesDetail.SUBJECT_VALUE_WIDTH, confIndices.getSubject_default()));
            
            note.setResult_value(retrieveValue(params, NOTES_RESULT_INDEX_CONFIGURED, this.NOTES_RESULT_INDEX, 
                    NotesDetail.RESULT_VALUE_WIDTH, confIndices.getResult_default()));
            
            note.setNote_value(retrieveValue(params, NOTES_NOTE_INDEX_CONFIGURED, this.NOTES_NOTE_INDEX, 
                    NotesDetail.NOTE_VALUE_WIDTH, confIndices.getNote_default()));

            note.setShowOnInvoice_value(retrieveBooleanValue(params, NOTES_SHOWONINVOICE_INDEX_CONFIGURED, 
                    this.NOTES_SHOWONINVOICE_INDEX, confIndices.getShowOnInvoice_default()));
            
            String subscriptionType = retrieveValue(params, 
                    NOTES_SUBSCRIPTION_TYPE_INDEX_CONFIGURED, this.NOTES_SUBSCRIPTION_TYPE_INDEX, 
                    10, String.valueOf(confIndices.getSubscriptionType_default()));
            if (subscriptionType.trim().length() > 0)
            {
                note.setSubscriptionType_value(Long.parseLong(subscriptionType));
            }
        }
        else
        {
            LogSupport.minor(ctx, this, "Configured ER Indices required to parse Notes ER are invalid.");
            return null;
        }

        return note;
    }

    // Should it be synchronized ?
    private boolean setErIndices(final Context ctx, final NotesPollerConfig confIndices)
    {
        boolean isSuccess = false;
        try
        {
            this.NOTES_ER_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getErid_index());
            if (this.NOTES_ER_INDEX != -1)
            {
                this.NOTES_ER_INDEX_CONFIGURED = true;
            }
            
            this.NOTES_SPID_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getSpid_index());
            if (this.NOTES_SPID_INDEX != -1)
            {
                this.NOTES_SPID_INDEX_CONFIGURED = true;
            }
            
            this.NOTES_MSISDN_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getMsisdn_index());
            if (this.NOTES_MSISDN_INDEX != -1)
            {
                this.NOTES_MSISDN_INDEX_CONFIGURED = true;
            }

            this.NOTES_BAN_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getBan_index());
            if (this.NOTES_BAN_INDEX != -1)
            {
                this.NOTES_BAN_INDEX_CONFIGURED = true;
            }

            this.NOTES_NOTETYPE_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getNotetype_index());
            if (this.NOTES_NOTETYPE_INDEX != -1)
            {
                this.NOTES_NOTETYPE_INDEX_CONFIGURED = true;
            }

            this.NOTES_SUBTYPE_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getSubtype_index());
            if (this.NOTES_SUBTYPE_INDEX != -1)
            {
                this.NOTES_SUBTYPE_INDEX_CONFIGURED = true;
            }
            
            this.NOTES_AGENT_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getAgent_index());
            if (this.NOTES_AGENT_INDEX != -1)
            {
                this.NOTES_AGENT_INDEX_CONFIGURED = true;
            }

            this.NOTES_SUBJECT_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getSubject_index());
            if (this.NOTES_SUBJECT_INDEX != -1)
            {
                this.NOTES_SUBJECT_INDEX_CONFIGURED = true;
            }
            
            this.NOTES_NOTE_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getNote_index());
            if (this.NOTES_NOTE_INDEX != -1)
            {
                this.NOTES_NOTE_INDEX_CONFIGURED = true;
            }

            this.NOTES_RESULT_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getResult_index());
            if (this.NOTES_RESULT_INDEX != -1)
            {
                this.NOTES_RESULT_INDEX_CONFIGURED = true;
            }
            
            this.NOTES_SHOWONINVOICE_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getShowOnInvoice_index());
            if (this.NOTES_SHOWONINVOICE_INDEX != -1)
            {
                this.NOTES_SHOWONINVOICE_INDEX_CONFIGURED = true;
            }
            
            this.NOTES_SUBSCRIPTION_TYPE_INDEX = validateAndRetrieveConfiguredIndex(confIndices.getSubscriptionType_index());
            if (this.NOTES_SUBSCRIPTION_TYPE_INDEX != -1)
            {
                this.NOTES_SUBSCRIPTION_TYPE_INDEX_CONFIGURED = true;
            }
            
            isSuccess = true;

        }
        catch (final NumberFormatException nfe)
        {
            LogSupport.minor(ctx, this,
                    "Error Parsing the configured ER Indices in the Generic Sub/Account Notes Poller Configuration. ", nfe);
            isSuccess = false;
        }
        return isSuccess;
    }



    public List<NotesDetail> createNotesDetails(Context ctx, ProcessorInfo info, List params) throws ParseException, HomeException, AgentException
    {
        List<NotesDetail> list = new ArrayList<NotesDetail>();
        NotesDetail detail = createNotesDetail(ctx, info, params);
        if (detail != null)
        {
            list.add(detail);
        }
        return list;
    }
    
    /**
     * Retrieves the specified configuration value, if it was set in the Generic Account/Sub Note Poller 
     * configuration.  Otherwise, returns -1.
     * @param index - configured index value to validate
     */
    private int validateAndRetrieveConfiguredIndex(String index)
    {
        if (index != null && index.trim().length() > 0)
        {
            return Integer.parseInt(index);
        }
        else
        {
            return -1;
        }
    }
    
    /**
     * Return the String value of the token at the given index.  If there is no such value,
     * then return the given default Value.
     * @param params parameters from the ER.
     * @param isIndexConfigured
     * @param index
     * @param maxWidth  The returned
     * @param defaultValue
     * @return
     */
    private String retrieveValue(final List params, 
            final boolean isIndexConfigured, 
            final int index, 
            final int maxWidth, 
            final String defaultValue)
    {
        String value = CRMProcessorSupport.getField(params, index);
        if (isIndexConfigured && value != null && value.trim().length()>0)
        {
            //value is from the ER.
        }
        else
        {
            //No Value in the ER so replace with default Value.
            value = defaultValue;
        }
        value = value.trim();
        // Trim the length of the values.
        if (maxWidth > 0)
        {
            value = this.truncateField(value, maxWidth);
        }
        return value;
    }

    /**
     * Return the boolean value of the token at the given index.  If there is no such value,
     * then return the given default value.
     * @param params parameters from the ER
     * @param isIndexConfigured
     * @param index
     * @param defaultValue
     * @return
     */
    private boolean retrieveBooleanValue(final List params, 
            final boolean isIndexConfigured, 
            final int index, 
            final boolean defaultValue)
    {
        boolean showOnInvoice = false;
        String valuefromER = CRMProcessorSupport.getField(params, index);
        if (isIndexConfigured &&  valuefromER != null && valuefromER.trim().length()>0)
        {
            //value is from the ER.
            showOnInvoice = CRMProcessorSupport.getBoolean(valuefromER, defaultValue);
        }
        else
        {
            //Value is not set in the ER
            showOnInvoice = defaultValue;
        }
        return showOnInvoice;
    }


    private int NOTES_ER_INDEX = -1;
    private int NOTES_SPID_INDEX = -1;
    private int NOTES_MSISDN_INDEX = -1;
    private int NOTES_BAN_INDEX = -1;
    private int NOTES_NOTETYPE_INDEX = -1;
    private int NOTES_SUBTYPE_INDEX=-1;
    private int NOTES_AGENT_INDEX=-1;
    private int NOTES_SUBJECT_INDEX=-1;
    private int NOTES_RESULT_INDEX=-1;
    private int NOTES_NOTE_INDEX=-1;
    private int NOTES_SHOWONINVOICE_INDEX=-1;
    private int NOTES_SUBSCRIPTION_TYPE_INDEX=-1;
    
    
    private boolean NOTES_ER_INDEX_CONFIGURED = false;
    private boolean NOTES_SPID_INDEX_CONFIGURED = false;
    private boolean NOTES_MSISDN_INDEX_CONFIGURED = false;
    private boolean NOTES_BAN_INDEX_CONFIGURED = false;
    private boolean NOTES_NOTETYPE_INDEX_CONFIGURED = false;
    private boolean NOTES_SUBTYPE_INDEX_CONFIGURED = false;
    private boolean NOTES_AGENT_INDEX_CONFIGURED = false;
    private boolean NOTES_SUBJECT_INDEX_CONFIGURED = false;
    private boolean NOTES_NOTE_INDEX_CONFIGURED = false;
    private boolean NOTES_RESULT_INDEX_CONFIGURED = false;
    private boolean NOTES_SHOWONINVOICE_INDEX_CONFIGURED = false;
    private boolean NOTES_SUBSCRIPTION_TYPE_INDEX_CONFIGURED = false;
    private boolean validIndices = false;
    private NotesPollerConfig confIndices = null;
}
