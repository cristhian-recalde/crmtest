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
package com.trilogy.app.crm.bas.tps;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.pipe.ConveragedAccountSubscriberLookupAgent;
import com.trilogy.app.crm.bas.tps.pipe.ConvertTpsToTransactionAgent;
import com.trilogy.app.crm.bas.tps.pipe.HummingBirdAdjustTypeMappingAgent;
import com.trilogy.app.crm.bas.tps.pipe.HummingBirdDuplicationCheckingAgent;
import com.trilogy.app.crm.bas.tps.pipe.Pipeline;
import com.trilogy.app.crm.bas.tps.pipe.VoidFieldCheckingAgent;
import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;


/**
 * Process the TPS format for Hummingbird.
 * @author larry.xia@redknee.com
 */
public class HummingbirdTPSProcessor extends AbstractTPSProcessor
{

    /**
     * Default constructor doesn't accept any parameter.
     *
     */
    public HummingbirdTPSProcessor()
    {
        errCounter_ = 0;
        succesCounter_ = 0;
        successAmount_ = 0;
    }

    /**
     * Constructor that accepts the context and the file to be processed.
     * @param context the operating context
     * @param file the file to be opened and processed for transactions
     */
    public HummingbirdTPSProcessor(final Context context, final File file)
    {
        this();
        init(context, file);
    }

    /**
     * {@inheritDoc}
     */
    public void init(final Context context, final File file)
    {
        setContext(context);
        tpsFile_ = file;
        errFilename = tpsFile_.getName();
        errFilename = errFilename.substring(0, errFilename.lastIndexOf(".") + 1) + "err";
    }

    /**
     * {@inheritDoc}
     */
    public boolean processFile()
    {
        boolean ret = true;
        HummingbirdTPSInputStream in = null;
        final Context context = getContext().createSubContext();
        context.put(TPSProcessor.class, this);

        context.put(TPSPipeConstant.PIPELINE_TPS_KEY,
                   new VoidFieldCheckingAgent(
                   new HummingBirdAdjustTypeMappingAgent(
                   new HummingBirdDuplicationCheckingAgent(
                   new ConveragedAccountSubscriberLookupAgent(
          		   new ConvertTpsToTransactionAgent(null))))));
        setContext(context);
        try
        {
            in = new HummingbirdTPSInputStream(this.getContext(), new FileInputStream(tpsFile_));
            while (true)
            {
                final Context subContext = context.createSubContext();
                try
                {
                    final TPSRecord  tps  = in.readTps(subContext);
                    tps.setTpsFileName(tpsFile_.getName()); 
                    new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_ATTEMPT, 1).log(getContext());
                    Pipeline.pump(subContext, tps);
                    if (tps.getLastError() == null || tps.getLastError().length() <= 0)
                    {
                        ++succesCounter_;
                        successAmount_ += tps.getAmount();
                    }
                }
                catch (InvalidTPSRecordException te)
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_FAIL, 1).log(getContext());
                    ERLogger.genInvalidEntryER(subContext);

                    // Put the error message into the TPS record so that the message
                    // can be generated in the error file.
                    final TPSRecord tps = (TPSRecord) subContext.get(TPSRecord.class);
                    if (tps != null)
                    {
                        tps.setLastError(te.getMessage());
                    }

                    writeErrFile(subContext);
                    new EntryLogMsg(10532, this, "", "", null, te).log(getContext());
                }
            }
        }
        catch (EOFException ee)
        {
            new DebugLogMsg(this, "EOF Exception occured when processing TPS file.  This would always happen. "
                    + tpsFile_.getName(), ee).log(getContext());
        }
        catch (IOException ie)
        {
            new MinorLogMsg(this, "IO error occured when processing TPS file "
                    + tpsFile_.getName(), ie).log(getContext());
            ret = false;
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Exception occured when processing TPS file "
                    + tpsFile_.getName(), e).log(getContext());
            ret = false;
        }
        finally
        {
          
            try
            {
                 in.close();
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "IO error occured when closing TPS file "
                        + this.tpsFile_.getName(), e).log(getContext());
            }
            try
            {
             	 if (out != null)
                {
                    out.close();
                }

                ERLogger.writePaymentAtAccountLevelFileER(getContext(),
                        tpsFile_.getName(),
                        successAmount_,
                        failedAmount_,
                        succesCounter_,
                        errCounter_);
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "IO error occured when closing TPS Error file "
                        + errFilename, e).log(getContext());
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void writeErrFile(final Context ctx)
    {
        try
        {
            if (out == null)
            {
                final TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class);
                out = new PrintStream(new FileOutputStream(
                        new File(config.getErrorDirectory() + File.separator
                                + errFilename)));
                ctx.put(TPSPipeConstant.TPS_PIPE_ERROR_OUTPUT_STREAM, out);
            }

            final TPSRecord  tps  = (TPSRecord) ctx.get(TPSRecord.class);
            if (tps != null)
            {
                out.println(tps.getRawline());
                out.println("#ERROR - " + tps.getLastError());
                ++errCounter_;
                failedAmount_ += tps.getAmount();
            }

        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "IO error occured when writing to TPS Error file "
                    + errFilename, e).log(ctx);
        }
    }


    /**
     * header and trailer line are included.
     */
    private long errCounter_;

    /**
     * succeed tps records
     */
    private long succesCounter_;

    /**
     * Succes amount payments
     */
    private long successAmount_;

    /**
     * Failed amount
     */
    private long failedAmount_;
}
