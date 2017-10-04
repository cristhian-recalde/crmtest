package com.trilogy.app.crm.poller.error;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class ErrorFileOutputAgent implements ContextAgent
{
    public final static String ERROR_RECORD = "POLLER.ERROR_RECORD_KEY";
    
    public ErrorFileOutputAgent(Context ctx, String name, String errFilePrefix)
    {
        name_ = name;
        errFilePrefix_ = errFilePrefix;
        idKey = IdentifierEnum.ACCOUNT_CREATION_TEMPLATE_ID;
        
        try 
        {
            IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
                ctx,
                idKey,
                0L,
                2000000L,
                0L);
        } catch (HomeException e)
        {
            new MajorLogMsg(this, "faild to find ER error file sequence ", e).log(ctx); 
        }
    }
    
    public void execute(Context ctx) throws AgentException
    {
        String record = (String) ctx.get(ERROR_RECORD);
        
        if ( record == null )
        {
            new MinorLogMsg(name_,"Failed to insert record to ER Error file "
                    +" because the ER is empty.", null).log(ctx);
            return;
        }
        writeErrFile(ctx, record);
        
    }
    
    public void writeErrFile(Context ctx, String record)
    {
        try
        {

            PrintStream out = this.getOutStream(ctx); 
            
            if ( out != null )
            {   
                synchronized (out)
                {
                    out.println(record);
                }
            }   
        }
        catch(Exception e)
        {
            new MinorLogMsg(name_,
                    "IO error occured when writing " + record
                            + " to ER Error file ", e).log(ctx);
            // Reset the Output stream
            out_ = null;
           
        }
    }
    
    public void stop()
    {
        this.close(); 
    }
    /**
     * This method returns the name of the file for storing failed ER records.
     * 
     * @return The file name.
     */
    public String getErrFileNameBase(Context ctx) 
    {
        GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);

        return  config.getERErrDir() + File.separator + errFilePrefix_;
     }
    
    
    private String getNextFileName(Context ctx)
    throws HomeException
    {
        return getErrFileNameBase(ctx) + "."+ IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
                ctx,
                idKey,
                null); 
    }
    
    
    private synchronized PrintStream getOutStream(Context ctx)
    throws Exception
    {
            long size = this.size(); 
            
            if ( size >= MAX_SIZE)
            {
                close(); 
            }
            
            if (size  == -1   || size >= MAX_SIZE)              
            {
                fos_ = new FileOutputStream(getNextFile(ctx));
                out_= new PrintStream(fos_);
                fch_ = fos_.getChannel();               
            }
            return out_;            
        
      }
    
    private File getNextFile(Context ctx) throws HomeException, IOException
    {
        final File errorFile = new File(getNextFileName(ctx));
        {
            if (!errorFile.exists())
            {
                File dir = errorFile.getParentFile();
                if (!dir.exists())
                {
                    dir.mkdirs();
                }
                errorFile.createNewFile();
            }
        }
        return errorFile;
    }
    
    private void close()
    {
        synchronized (out_)
        {
            if ( fos_ != null )
            {
                try 
                {
                    fos_.flush(); 
                    fos_.close(); 
                }catch(IOException e)
                {
                
                }finally{
                    fos_ = null; 
                    out_ = null; 
                    fch_ = null; 
                }
            }   
        }
    }
    
    public long size()
    throws IOException
    {
        if (fch_ != null)
        {
            return fch_.position();         
        }

        return -1l;
    }


    private String name_ = "";
    private String errFilePrefix_="";
    private PrintStream out_ = null;
    
    protected FileChannel fch_ = null;
    protected FileOutputStream fos_ = null;

    public static final long MAX_SIZE = 10000000l; 
    private IdentifierEnum idKey; 
}