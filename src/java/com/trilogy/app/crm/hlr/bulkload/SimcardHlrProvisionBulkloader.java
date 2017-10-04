package com.trilogy.app.crm.hlr.bulkload;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.HlrTemplate;
import com.trilogy.app.crm.bean.HlrTemplateHome;
import com.trilogy.app.crm.bean.SimcardHlrBulkLoadTask;
import com.trilogy.app.crm.hlr.CrmHlrServiceImpl;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.PPMLogMsg;
import com.trilogy.interfaces.crm.hlr.CrmHlrResponse;
import com.trilogy.interfaces.crm.hlr.InterfaceCrmHlrConstants;

public class SimcardHlrProvisionBulkloader
{

	public SimcardHlrProvisionBulkloader(SimcardHlrBulkLoadTask form)
	{
		this.task_ = form; 
	}
	
	public void process(Context ctx)
	{
		template = getTemplate(ctx,  task_.getTemplateId()); 
		
		getAllFiles(ctx, task_.getFilePath());
		
		
		for(File file : hlrFiles)
		{
			processSingleFile(ctx, file); 
			++hlrFileDone;

		}
		
    	ERLogger.generateSimcardHLRLoaderEr(ctx, -1, SystemSupport.getAgent(ctx), 
    			task_.getFilePath(), this.hlrCounter, this.hlrSuccess, template.getId()); 

	}
	
	private void getAllFiles( Context ctx, String s)
	{
		String[] path = task_.getFilePath().split(FILE_PATH_DELIM); 
		
		hlrFiles = new ArrayList<File>();
		
		
		for(int i=0; i< path.length; ++i)
		{
			
			File file = new File(path[i]); 
			if (file.isDirectory())
			{
				processSingleDirectory(ctx, file); 
			} else 
			{
				hlrFiles.add(file);  
			}
		}

	}
	
	
	
	private void processSingleDirectory(Context ctx, 
			final File dire)
	{
		File[] file = dire.listFiles();
		
		for(int i=0; i< file.length; ++i)
		{
			
			if (file[i].isDirectory())
			{
				processSingleDirectory(ctx, file[i]); 
			} else 
			{
				hlrFiles.add(file[i]); 
			}
		}

	}
	
	
	private void processSingleFile(Context ctx, 
			final File file)
	{
		int currentFileLinesDone =0;
		Collection<SimcardInput>  inputs = new ArrayList<SimcardInput>(); 
		String redoFileName = this.task_.getRedoFilePath() + File.separator + file.getName() + ".redo";
		String logFileName = this.task_.getLogPath() + File.separator + file.getName() + ".log";
		
		PrintWriter redoOut = null; 
		PrintWriter logOut = null; 
		
		SimcardBulkLoadingStream inputStream=null; 
		try 
		{
			if (this.template == null)
			{
				logOut = new PrintWriter( new File(logFileName)); 
				logOut.println("Fail to log file " + file.getAbsolutePath() + 
						" because can not find the hlr template " + task_.getTemplateId() ); 
					
			}
			
			inputStream = new SimcardBulkLoadingStream(new FileInputStream(file));
			
			while (true)
			{
				try 
				{
					inputs.add( inputStream.readLine());
					++hlrCounter;
				} catch (EOFException e)
				{
					break; 
				} catch (InvalidInputException e)
				{
					++currentFileLinesDone;
					try 
					{
						if (redoOut == null )
						{
							redoOut = new PrintWriter( new File(redoFileName)); 
						}
					
						redoOut.print(e.getRawLine());
					
						if (logOut == null)
						{
							logOut = new PrintWriter( new File(logFileName)); 
						}
					
						logOut.println(e.getRawLine()); 
						logOut.println(e.getMessage() ); 
					} catch (IOException e1)
					{
						new MinorLogMsg(this, "fail to log simcard hlr bulk load failure for line" + e.getRawLine(), e).log(ctx);
					}	
				} 
			}	
		} catch (IOException e)
		{
			try 
			{
				if (logOut == null)
				{
					logOut = new PrintWriter(new File(logFileName)); 
				}
				
				logOut.println("IO Exception, fail to continue process this file"); 
				new MajorLogMsg(this, "IO Exception, fail to continue process this file" + file.getName(), e).log(ctx);
			} catch (IOException e1)
			{
				new MinorLogMsg(this, "fail to log simcard hlr bulk load failure for " + file.getName(), e).log(ctx);
			}	
		}finally
		{
			if(inputStream != null )
			{
				try 
				{
					inputStream.close(); 
				} catch (IOException e)
				{
					new MinorLogMsg(this, "fail to close simcard bulk loading file " + file.getName(), e).log(ctx);
				}
			}
		}
		
		for(SimcardInput simInput: inputs)
		{
			
			
			CrmHlrResponse response = provision(ctx, simInput); 
				
			if (response.getCrmHlrCode()!= InterfaceCrmHlrConstants.HLR_SUCCESS)
			{
					try 
					{
						if (redoOut == null )
						{
							redoOut = new PrintWriter(new File(redoFileName)); 
						}
					
						redoOut.print(simInput.getRawline());
					
						if (logOut == null)
						{
							logOut = new PrintWriter( new File(logFileName)); 
						}
					
						logOut.println(simInput.getRawline()); 
						logOut.println("fail to update HLR, the crm HLR code is " + response.getCrmHlrCode() + 
							" the driver code is " + response.getDriverHlrCode() + 
							" the original hlr code is " + response.getRawHlrCode() ); 
						logOut.println("the original hlr response is " + response.getRawHlrData());
					} catch (IOException e)
					{
						new MinorLogMsg(this, "fail to log simcard hlr bulk load failure for " + simInput.getRawline(), e).log(ctx);
					}

			} else 
			{
				++hlrSuccess; 
				try
				{
					if (logOut == null)
					{
						logOut = new PrintWriter( new File(logFileName)); 
					}
				
					logOut.println(simInput.getRawline()); 
					logOut.println("Updating HLR Succed, the original hlr response is " + response.getRawHlrData());
					
				} catch (IOException e)
				{
					new MinorLogMsg(this, "fail to create simcard hlr bulk load log for " + simInput.getRawline(), e).log(ctx);
				}

			}
			++currentFileLinesDone; 
			reportProgress(ctx,currentFileLinesDone, inputs.size() ); 
		}
		
		if (redoOut != null)
		{
			redoOut.close(); 
		}
		if (logOut != null)
		{
			logOut.close(); 
		}
		
		
	}
	

	
	private CrmHlrResponse provision(Context ctx, SimcardInput simcard)
	{
		final PMLogMsg pmLogMsg = new PMLogMsg(SIMCARD_HLR_PM_MODULE,SIMCARD_HLR_PM_ACTION);

		String cmd = getTemplateCommand(); 
		
		cmd = simcard.mapToCommand(cmd);
		
		final CrmHlrServiceImpl service = CrmHlrServiceImpl.instance(); 
		
		CrmHlrResponse ret =  service.process(ctx, (short) task_.getHlrId(), cmd);
		
		pmLogMsg.log(ctx);
		
		return ret;
				
	}
		
	private String getTemplateCommand()
	{
		return this.template.getHlrCommandTemplate(); 
	}
	
	
	private HlrTemplate getTemplate(Context ctx, long id)
	{
		HlrTemplate ret = null; 
		
		Home home = (Home) ctx.get(HlrTemplateHome.class); 
		try 
		{
			ret = (HlrTemplate)home.find(Long.valueOf(id)); 
		} catch (HomeException e)
		{
			new MajorLogMsg(this, "fail to find HLR template" + id, e).log(ctx); 
		}
		
		return ret; 
	}
	
    private void reportProgress(Context ctx, long count, long total)
    {
        PPMLogMsg ppmLogMsg = (PPMLogMsg) ctx.get(PPMLogMsg.class);
        if (null != ppmLogMsg)
        {
        	if (hlrFiles.size() == 1)
        	{	
        		ppmLogMsg.progress(ctx, count, total);
        	} else 
        	{
        		ppmLogMsg.progress(ctx, hlrFileDone, hlrFiles.size());
        	}
        }
    }

    
    
	private long hlrSuccess=0; 
	private long hlrCounter=0; 
	
	private int  hlrFileDone=0;
	private Collection<File> hlrFiles; 
	private SimcardHlrBulkLoadTask task_; 
	private HlrTemplate template;
	
	public static final String FILE_PATH_DELIM = ";"; 
	
	public static final String SIMCARD_HLR_PM_MODULE = "SimcarHlrBulkLoading";
	public static final String SIMCARD_HLR_PM_ACTION ="update"; 
}
