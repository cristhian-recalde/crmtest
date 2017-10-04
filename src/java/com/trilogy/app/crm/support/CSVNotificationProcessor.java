package com.trilogy.app.crm.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.FileFormatterConfig;
import com.trilogy.app.crm.notification.template.CSVNotificationTemplate;
import com.trilogy.app.crm.paymentmethod.PaymentMethodSupport;
import com.trilogy.app.crm.paymentmethod.exception.PaymentMethodProcessorException;
import com.trilogy.app.crm.paymentmethod.filegenerator.PaymentFileRecord;
import com.trilogy.app.crm.paymentmethod.filegenerator.PaymentMethodFileFormatter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class CSVNotificationProcessor
{
    private static final String           BAN_PROPERTY                                = "BAN";
    private static final String           DATE_VALUE                                  = "DATE_VALUE";

    private static final SimpleDateFormat sdf                                         = new SimpleDateFormat("yyyyMMdd");

    protected ThreadPoolExecutor          threadPool                                  = null;


    public CSVNotificationProcessor()
    {
        threadPool = new ThreadPoolExecutor(2, 5, 1000, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
    }


    public boolean process(final Context ctx, final CSVNotificationTemplate csvNotificationTemplate)
            throws PaymentMethodProcessorException
    {

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "[Entered]");
        }

        Context subCtx = ctx.createSubContext();
        try
        {
            FileFormatterConfig formatterConfig = PaymentMethodSupport.retrieveFileFormatterConfig(subCtx,
                    csvNotificationTemplate.getFileFormatter());
            NotificationFileGenerator fileGenerator = new NotificationFileGenerator(subCtx, formatterConfig,
                    csvNotificationTemplate);
            threadPool.execute(fileGenerator);
            return true;
        }
        catch (Exception e)
        {
            String message = "Failed to Create CSV Notification file. Exception occured : " + e.getMessage();
            LogSupport.major(subCtx, this, message, e);
            return false;
        }
    }

    protected class NotificationFileGenerator implements Runnable
    {
        Context                 subCtx;
        FileFormatterConfig     formatterConfig         = null;
        int                     spid                    = 0;
        CSVNotificationTemplate csvNotificationTemplate = null;


        /**
         * @param csvNotificationTemplate
         */
        public NotificationFileGenerator(Context ctx, FileFormatterConfig fileFormatterConfig,
                CSVNotificationTemplate csvNotificationTemplate)
        {
            this.formatterConfig = fileFormatterConfig;
            this.subCtx = ctx;
            this.csvNotificationTemplate = csvNotificationTemplate;
        }


        /**
         * {@inheritDoc}
         */
        public void run()
        {
            try
            {
                PaymentFileRecord fileRecordBean = new PaymentFileRecord();
                Date currentDate = new Date();
                String date = sdf.format(currentDate);

                // Fetch Processor config for DIRECT DEBIT, as we need to check if the previous method is DIRECT DEBIT
                // or not for Process Classification field in file

                Account account = (Account) subCtx.get(Account.class);
                String ban = null;
                if (account != null)
                {
                    ban = account.getBAN();
                }

                try
                {
                    // CREATE DATA ROW

                    subCtx.put(BAN_PROPERTY, ban);
                    subCtx.put(DATE_VALUE, date);

                    // Create Data ROW using file formatter config
                    StringBuffer data = PaymentMethodFileFormatter.formatFileData(subCtx, formatterConfig.getData(),
                            formatterConfig.getDelimiter());
                    if (data != null && data.length() > 0)
                    {
                        fileRecordBean.getDataRecordList().add(data);
                    }

                    // CREATE HEADER DATA
                    StringBuffer header = PaymentMethodFileFormatter.formatFileData(subCtx,
                            formatterConfig.getHeader(), formatterConfig.getDelimiter());
                    fileRecordBean.setHeader(header);

                    // CREATE TRAILER DATA
                    StringBuffer trailer = PaymentMethodFileFormatter.formatFileData(subCtx,
                            formatterConfig.getTrailer(), formatterConfig.getDelimiter());
                    fileRecordBean.setTrailer(trailer);

                    // CREATE END RECORD DATA
                    StringBuffer endRecord = PaymentMethodFileFormatter.formatFileData(subCtx,
                            formatterConfig.getEndRecord(), formatterConfig.getDelimiter());
                    fileRecordBean.setEndRecord(endRecord);
                }
                catch (Exception ex)
                {
                    LogSupport.major(subCtx, this, "Error occured while formatting CSV notification for file["
                            + csvNotificationTemplate.getOutboundFileName()
                            + "], still proceeding with file generation, ", ex);
                }

                writeDataToFile(subCtx, formatterConfig, fileRecordBean, currentDate);

                if (LogSupport.isDebugEnabled(subCtx))
                {
                    LogSupport.debug(subCtx, this, "Successfully generated CSV Notification for BAN : " + ban);
                }

            }
            catch (Exception e)
            {
                LogSupport.major(subCtx, this, "Error occured while generation CSV notification for file["
                        + csvNotificationTemplate.getOutboundFileName() + "]", e);
            }
        }


        private void writeDataToFile(Context ctx, FileFormatterConfig formatterConfig,
                PaymentFileRecord fileRecordBean, Date currentDate) throws Exception
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Writing data to file..");
            }
            NotificationFileWriter fileWriter = new NotificationFileWriter();
            String outboundFileName = csvNotificationTemplate.getOutboundFileName();
            fileWriter.init(ctx, csvNotificationTemplate.getOutboundFileDirectory(), outboundFileName,
                    csvNotificationTemplate.getOutboundFileExtension(), currentDate, true);
            fileWriter.populateDataInFile(fileRecordBean);
        }

    }
}