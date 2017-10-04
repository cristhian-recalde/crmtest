package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.IllegalFormatWidthException;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.DDOutboundFileTrackStateEnum;
import com.trilogy.app.crm.bean.DDOutboundFileTrack;
import com.trilogy.app.crm.bean.DDOutboundFileTrackXInfo;
import com.trilogy.app.crm.bean.PaymentGatewayIntegrationConfig;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * 
 * This lifecycle agent will process the outbound file.
 * 
 * @author <a href='mailto:suyash.gaidhani@redknee.com'>Suyash Gaidhani</a>
 *
 */
public class DirectDebitOutboundFileProcessorLifecycleAgent  extends
LifecycleAgentScheduledTask 
{

	public DirectDebitOutboundFileProcessorLifecycleAgent(Context ctx, String agentId)
			throws AgentException 
			{

		super(ctx, agentId);
		PaymentGatewayIntegrationConfig config = (PaymentGatewayIntegrationConfig)ctx.get(PaymentGatewayIntegrationConfig.class);
		outboundProcessorThreadPool = new ThreadPool("OUTBOUND_PROCESSOR_THREADPOOL", config.getQueue(), config.getThreads(), new DirectDebitOutboundRecordProcessorAgent());
			}

	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException 
	{
		String inboundPath = "/opt/redknee/mnt/dd/inbound/ADM";
		TaskEntry task = TaskHelper.retrieve(ctx, CronConstant.DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_NAME);
		if(task != null)
		{
			String inboundPathConfig = task.getParam0();


			if(inboundPathConfig != null && inboundPathConfig.length()>0)
			{
				inboundPath = inboundPathConfig;
			}
		}

		Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());

		And filter = new And();
		filter.add(new LTE(DDOutboundFileTrackXInfo.GENERATION_DATE, today));
		filter.add(new EQ(DDOutboundFileTrackXInfo.STATE, DDOutboundFileTrackStateEnum.UNPROCESSED));

		//Get collection of beans for processing.
		Collection<DDOutboundFileTrack> trackers = HomeSupportHelper.get(ctx).getBeans(ctx, DDOutboundFileTrack.class, filter);

		for(DDOutboundFileTrack tracker : trackers)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this, "Processing DirectDebitOutboundFile Record [ ID:" 
						+ tracker.getId()
						+ " Generation Date : "
						+ tracker.getGenerationDate()
						+ " Path :"
						+ tracker.getPath()
						+ " Filename :"
						+ tracker.getFileName());
			}
			
			if(tracker.getFileName() != null && tracker.getPath() != null)
			{
				AdmerisDirectDebitOutboundFileReader reader = new AdmerisDirectDebitOutboundFileReader();
				AdmerisDirectDebitInboundFileWriter writer = new AdmerisDirectDebitInboundFileWriter();
				
				String errorDir = tracker.getPath().trim() + File.separator + "err";
				File errorFileDir = new File(errorDir);
				errorFileDir.mkdirs();
				
				AdmerisDirectDebitErrorFileWriter errorWriter = new AdmerisDirectDebitErrorFileWriter(errorDir,tracker.getFileName());

				//TODO sgaidhani: Add lifecycle support for abort operation.
				String outboundFileName = tracker.getPath().trim() + File.separator +  tracker.getFileName().trim();
				try
				{
					reader.init(ctx, outboundFileName);
					
					File inboundDir = new File(inboundPath);
					inboundDir.mkdirs();
					writer.init(ctx, inboundPath, "", tracker.getFileName());

					while(true)
					{
						String record = "";
						try{
							record = reader.readRecord();
							if(record != null)
							{
								if(!reader.verifyLength(ctx,record))
								{
									new MinorLogMsg(this, "Record width does not match with expected width. Unable to process the outbound TPS record : " + record,null).log(ctx);
									errorWriter.writeToErrorFile(ctx, record, "Record width does not match with expected width." );
									continue;
								}
								
								Context subContext = ctx.createSubContext();
								subContext.put(OUTBOUND_RECORD, record);
								subContext.put(OUTBOUND_FILENAME, outboundFileName);
								subContext.put(AdmerisDirectDebitInboundFileWriter.class, writer);
								subContext.put(AdmerisDirectDebitErrorFileWriter.class,errorWriter);
								outboundProcessorThreadPool.execute(subContext);
							}
							else
							{
								//Ignore record
								break;
							}
						}
//						catch(IllegalFormatWidthException fwe)
//						{
//							new MajorLogMsg(this, "Unable to process the outbound TPS record : " + record, fwe).log(ctx);
//							errorWriter.writeToErrorFile(ctx, record, "Record width does not match with expected width." );
//							continue;
//
//						}
						catch (EOFException e) 
						{
							//DO nothing
							if(LogSupport.isDebugEnabled(ctx))
							{
								LogSupport.debug(ctx, this, "End of File encountered in Filename : " + outboundFileName);
							}
							break;
						}
						catch (Exception e)
						{
							new MajorLogMsg(this, "Unable to process the outbound TPS record : " + record, e).log(ctx);
							errorWriter.writeToErrorFile(ctx, record, e.getLocalizedMessage() );
							continue;
						}
					}
					
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, this, "Successfully Completed processing for file : " + outboundFileName);
					}

					tracker.setState(DDOutboundFileTrackStateEnum.PROCESSED);
					HomeSupportHelper.get(ctx).storeBean(ctx, tracker);

				} 
				catch (Exception e)
				{
					new MajorLogMsg(this, "Execption encounterd while trying to process the file :" + outboundFileName, e).log(ctx);
				}
				finally
				{
					if(errorWriter.getErrorCount() > 0)
					{
						tracker.setState(DDOutboundFileTrackStateEnum.PROCESSED_WITH_ERRORS);
						HomeSupportHelper.get(ctx).storeBean(ctx, tracker);
					}

					waitForAllThreads();
					
					writer.close();
					reader.close(ctx);
					errorWriter.close();
				}
			}
		}
	}


	private void waitForAllThreads() {

		while(true)
		{
			boolean readyToTearDown = true;

			if (outboundProcessorThreadPool.getQueueCount() > 0 || outboundProcessorThreadPool.getThreadCount() > 0)
			{
				readyToTearDown = false;
			}

			if (!readyToTearDown)
			{
				try {
					Thread.sleep(2000);
				} 
				catch (InterruptedException e) 
				{
					//Do Nothing
				}
			}
			else
			{
				break;
			}
		}
	}
	
	final ThreadPool outboundProcessorThreadPool;
	public static final String OUTBOUND_RECORD = "OUTBOUND_RECORD";
	public static final String OUTBOUND_FILENAME = "OUTBOUND_FILENAME";
}

