/**
 * 
 */
package com.trilogy.app.crm.poller.event;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.config.NotesPollerConfig;
import com.trilogy.app.crm.config.NotesPollerConfigHome;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.agent.NotesAgent;

/**
 * @author skularajasingham
 *
 */
public class NotesUpdateProcessor extends NotesProcessor implements Constants {


	public NotesUpdateProcessor(Context ctx, int queueSize, int threads)
	    throws Exception
	{
		super();
        Home notehome = (Home) ctx.get(NotesPollerConfigHome.class);
        ErPollerConfig cfg = (ErPollerConfig)ctx.get(ErPollerConfig.class);
        NotesPollerConfig notesPollerConfig = (NotesPollerConfig)notehome.find(ctx, cfg.getId());
        if (notesPollerConfig == null)
        {
            final String message = "Unable to find configuration for Notes Poller for Poller "
                    + cfg.getId();
            final Exception ex = new Exception(message);
            LogSupport.major(ctx, this, message, ex);
            throw ex;
        }
        ctx.put(NotesPollerConfig.class, notesPollerConfig);
		init(ctx, "NotesUpdates", "NotesUpdates", queueSize, threads, new NotesAgent(ctx,this));
	}

	
	/**
	 * Parses one Notes ER
	 *
	 * @param date the date of the ER
	 * @param erid the id of the ER
	 * @param record the ER itself
	 * @param startIndex the index of the first nonparsed char in the ER
	 * @throws NumberFormatException
	 * @throws IndexOutOfBoundsException
	 * @see com.redknee.service.poller.nbio.event.EventProcessor#process(long,java.lang.String, char[], int)
	 */
	@Override
    public void process(long date, String erid, char[] record, int startIndex) 
		throws NumberFormatException,IndexOutOfBoundsException
	{
        Context ctx = getContext().createSubContext();
        ctx.put(ProcessorInfo.class, new ProcessorInfo(date, erid, record, startIndex));
        
        try
        {
            threadPool_.execute(ctx);
        }
        catch (AgentException e)
        {
            new MinorLogMsg(this, "Failed to process Notes ER because of Exception " + e.getMessage(), e).log(getContext());
            
            saveErrorRecord(ctx, record);
        }
	}

}
