package com.trilogy.app.crm.bas.tps;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.TPSConfig;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.DebugLogMsg;

/** 
 * Parsing TPS entry into TPS record object
 * 
 * @author lxia
 */

// INSPECTED: 07/11/2003 ltse

public class IBISTPSInputStream  
{
    Context context; 
    InputStream in; 
    String last_line = null; 
    String prefix = ""; 
    protected int lineCount = 0;

    private final static DateFormat dataformat_ = new SimpleDateFormat("yyyyMMdd");

    public IBISTPSInputStream(Context ctx, InputStream inputStream)
    {
        super();
        dataformat_.setLenient(false); 
        this.context = ctx; 
        this.reader = new CommentSkippingReader(new BufferedReader(new InputStreamReader(inputStream)));
        this.in = inputStream; 
    }

    /**
     *  Verify the file header
     */

    public boolean verifyHeader()
    {
        try
        {
            String line = reader.readLine(); 
            lineCount ++;
            if (line == null)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(this, "empty TPS file ", null).log(context);
				}
                return false; 
            }

            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "TPS file header " + line, null).log(context);
            }
            if(line.trim().length() < 14)
            {
                return false;
            }

            line = line.substring(0, 14); 
            if ( !line.trim().endsWith( "tfr") && !line.trim().endsWith( "err"))
            {
                return false;
            }

            line = line.substring( 0, 11).concat( "err"); 
            prefix = line.substring( 0,2);
            this.context.put(TPSPipeConstant.TPS_PIPE_ERROR_FILE_HEADER, line );
            return true;
        }
        catch (IOException e)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "Invalid TPS file header ", e).log(context);
            }
            return false;
        }
    }

    /**
     * close the stream
     *
     */
    public void close()
    throws IOException
    { 
              in.close(); 
       
    }

    /**
     * read a TPS record from input stream
     * @param ctx
     * 		   a Context
     * @return
     * @throws Exception
    */
    public TPSRecord readTps(Context ctx)
        throws Exception
    {
        String line = reader.readLine();
        return processTPSRecord(ctx, line);
    }

    private TPSRecord processTPSRecord(Context ctx, String line)
        throws EOFException, InvalidTPSRecordException
    {
        if ( line == null )
        {
            throw new EOFException("the end of file");
        }
        lineCount ++;

        ctx.put(TPS_RAW_RECORD_ENTRY, line);
        TPSRecord tps = new TPSRecord();
        tps.setTpsInitial(prefix);
        tps.setRawline(line);
        ctx.put(TPSRecord.class, tps);

        if (checkIfLastLine(line, lineCount))
        {
            throw new EOFException("Found the trailer. Stop reading");
        }

        int pstart = 0; 
        try
        {
            int tpsPhoneLength = getTPSPhoneLength(ctx);

            // Check to see that the record has the correct number of characters (taking into account that the PAYMENT_DATE field is optional)
            // if the number of characters is in between the two expected number of characters then we will simply ignore the PAYMENT_DATE characters as there won't be sufficient characters
            // this behaviour is consistent with the previous implementation
            if (line.trim().length() < TPS_TOTAL_STATIC_CHARS + tpsPhoneLength  || line.trim().length() > TPS_TOTAL_STATIC_CHARS + tpsPhoneLength + TPS_FIELD_PAYMENT_DATE)
            {
                throw new InvalidTPSRecordException("Invalid record length at line number [lineCount=" + lineCount + ",lineLength=" + line.trim().length() + "]");
            }

            tps.setLocationCode( line.substring( pstart, pstart + TPS_FIELD_LOCATION_CODE).trim());
            pstart += TPS_FIELD_LOCATION_CODE;

            tps.setTransactionNum( line.substring(pstart, pstart + TPS_FIELD_TRANSACTION_NUMBER).trim());
            pstart += TPS_FIELD_TRANSACTION_NUMBER;

            tps.setCashierNum(line.substring( pstart, pstart + TPS_FIELD_CASHIER_NUMBER).trim() );
            pstart += TPS_FIELD_CASHIER_NUMBER;

            tps.setAccountNum( line.substring(pstart, pstart+TPS_FIELD_ACCOUNT_NUMBER).trim());
            pstart += TPS_FIELD_ACCOUNT_NUMBER;

            tps.setTelephoneNum(line.substring(pstart, pstart + tpsPhoneLength).trim());
            pstart += tpsPhoneLength;

            tps.setName( line.substring(pstart, pstart+TPS_FIELD_CUSTOMER_NAME).trim());
            pstart += TPS_FIELD_CUSTOMER_NAME;

            tps.setPaymentDate( formatDate( line.substring( pstart, pstart+ TPS_FIELD_PAYMENT_DATE).trim())); 
            pstart += TPS_FIELD_PAYMENT_DATE;

            tps.setAmount( Integer.parseInt ( line.substring( pstart, pstart+ TPS_FIELD_AMOUNT).trim())); 
            pstart += TPS_FIELD_AMOUNT;

            String txt = line.substring( pstart, pstart +  TPS_FIELD_PAYMENT_METHOD).trim();
            if (txt.trim().equalsIgnoreCase("CA"))
 				tps.setPaymentMethod( PaymentMethodEnum.CA ); 
			else if (txt.trim().equalsIgnoreCase("CH"))
				tps.setPaymentMethod( PaymentMethodEnum.CH ); 
			else if (txt.trim().equalsIgnoreCase("CC"))
				tps.setPaymentMethod( PaymentMethodEnum.CC ); 
			else if (txt.trim().equalsIgnoreCase("DC"))
				tps.setPaymentMethod( PaymentMethodEnum.DC ); 
			else if (txt.trim().equalsIgnoreCase("TB"))
				tps.setPaymentMethod( PaymentMethodEnum.TB ); 
			else 
				throw 	new InvalidTPSRecordException("Invalid payment method"); 
			pstart += TPS_FIELD_PAYMENT_METHOD; 
  		
  			tps.setPaymentDetail(line.substring(pstart, pstart+TPS_FIELD_PAYMENT_DETAIL).trim()); 
  			pstart += TPS_FIELD_PAYMENT_DETAIL;
  		
  			txt = line.substring(pstart, pstart + TPS_FIELD_PAYMENT_TYPE).trim();
  			
  			if (txt.trim().equalsIgnoreCase("BILL") ||
                txt.trim().equalsIgnoreCase("ADEP") ||
				txt.trim().equalsIgnoreCase("RDEP"))				
				tps.setPaymentType(txt); 
 			else 
 				throw new InvalidTPSRecordException("invalid payment type"); 
  			pstart += TPS_FIELD_PAYMENT_TYPE; 
  			
  			String voidString = line.substring(pstart, pstart + TPS_FIELD_VOID_ID);
  			if (voidString.equalsIgnoreCase("Y"))
  			{
  				tps.setVoidFlag(true);
  			}
  			else
  			{
  				tps.setVoidFlag(false);
  			}
  			pstart += TPS_FIELD_VOID_ID;
  			
            // check to see if the PAYMENT_DATE is present based on the length of message 
            if (line.trim().length() >= TPS_TOTAL_STATIC_CHARS + tpsPhoneLength + TPS_FIELD_PAYMENT_DATE)
            {
                String exportDateString = line.substring(pstart, pstart + TPS_FIELD_PAYMENT_DATE).trim();
                tps.setExportDate(formatDate(exportDateString));
                pstart += TPS_FIELD_PAYMENT_DATE;
            }
        }
        catch ( Exception e)
        {
            throw new InvalidTPSRecordException(e.getMessage()); 
        }
        return tps; 
    }	   
 
    public static short getTPSPhoneLength(Context ctx) throws HomeException
    {
        TPSConfig config = (TPSConfig) ctx.get(TPSConfig.class);
        if (config == null)
        {// Won't happen
            throw new HomeException("TPS Config not found in COntext.");
        }
        else
        {
            return config.getTPSMsisdnLength();
        }
    }
    
    /**
 	 * format a date from a string
 	 * @param d
 	 * 		  String
 	 * @return Date
 	 * @throws Exception
 	 */
	public Date formatDate(String d) throws Exception
	{
	   synchronized (dataformat_)
	   {
		  return dataformat_.parse(d);
	   }
	}	 


	private boolean checkIfLastLine(String line, int curCount)
	{
		try
		{
			int count = Integer.parseInt(line.substring(0,4));
			if (count == curCount)
			{
				return true;
			}
			
			return false;
		}
		catch (NumberFormatException ne)
		{
			return false;
		}
	}
	 
    public static final int TPS_HEADER_LENGTH = 2;
    private CommentSkippingReader reader;
	public static final int TPS_FIELD_LOCATION_CODE 		= 6; 
	public static final int TPS_FIELD_TRANSACTION_NUMBER 	= 6;
	public static final int TPS_FIELD_CASHIER_NUMBER		= 3; 
	public static final int TPS_FIELD_ACCOUNT_NUMBER		= 13;
	public static final int TPS_FIELD_CUSTOMER_NAME			= 40;
	public static final int TPS_FIELD_PAYMENT_DATE			= 8;
	public static final int TPS_FIELD_AMOUNT				= 10;
	public static final int TPS_FIELD_PAYMENT_METHOD		= 2;
	public static final int TPS_FIELD_PAYMENT_DETAIL		= 12;
	public static final int TPS_FIELD_PAYMENT_TYPE			= 4;
	public static final int TPS_FIELD_VOID_ID				= 1; 
	public static final int TPS_FIELD_EXPORT_DATE			= 8;

    public static final int TPS_TOTAL_STATIC_CHARS = 
        TPS_FIELD_LOCATION_CODE + 
        TPS_FIELD_TRANSACTION_NUMBER + 
        TPS_FIELD_CASHIER_NUMBER + 
        TPS_FIELD_ACCOUNT_NUMBER + 
        TPS_FIELD_CUSTOMER_NAME +
        TPS_FIELD_AMOUNT +
        TPS_FIELD_PAYMENT_METHOD +
        TPS_FIELD_PAYMENT_DETAIL +
        TPS_FIELD_PAYMENT_TYPE +
        TPS_FIELD_VOID_ID +
        TPS_FIELD_EXPORT_DATE; // 105

    public static final String TPS_RAW_RECORD_ENTRY = "TPS_RAW_RECORD_ENTRY"; 
}
