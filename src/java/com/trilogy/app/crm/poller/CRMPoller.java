/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with REDKNEE.
 * 
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.service.poller.nbio.Poller;
import com.trilogy.service.poller.nbio.PollerConfig;
import com.trilogy.service.poller.nbio.PollerLogger;
import com.trilogy.service.poller.nbio.PollerStatusNotifier;
import com.trilogy.service.poller.nbio.event.EventProcessor;

/**
 * This is a base implementation of the Poller abstract class. This class adds ContextAware support, plus adds the
 * ability to convert from the application specific configuration bean to the poller library specific configuration
 * bean.
 * 
 * @author psperneac
 */
public abstract class CRMPoller extends Poller implements ContextAware, Runnable
{

   /**
    * Constructor. Delegates to the real poller constructor. 
    * @param config the poller config bean
    * @param logger a logger
    * @throws Exception
    */
   public CRMPoller(Context ctx, ErPollerConfig erPollerConfig, PollerConfig config, PollerLogger logger, int queueSize, int threads) throws Exception
   {
      super(config, logger);
      setContext(ctx);
      queueSize_ = queueSize;
      threads_ = threads;
      config_ = erPollerConfig;
      if(null == config_)
      {
          throw new NullPointerException("NULL ErPollerConfig passed in.");
      }
      loadCRMERHandlers();
   }
   
   @Override
   public void loadERHandlers()
   {
       // Can be overridden to load ER Handlers that do not require the context, queue size, and thread count
       // NOP
   }

   protected abstract void loadCRMERHandlers();

    /**
    * Constructor. Delegates to the real poller constructor. 
    * @param config the poller config bean
    * @throws java.lang.Exception
    */
   public CRMPoller(PollerConfig config) throws Exception
   {
      super(config);
   }

   /**
    * @see com.redknee.framework.xhome.context.ContextAware#getContext()
    */
   public Context getContext()
   {
      return this.context;
   }

   /**
    * @see com.redknee.framework.xhome.context.ContextAware#setContext(com.redknee.framework.xhome.context.Context)
    */
   public void setContext(Context context)
   {
      this.context = context;
   }

   /**
    * Generates a PollerConfig instance and transfers the values from
    * the crm poller config class in it. PollerConfig is defined in the 
    * ER poller library. 
    * @param _name the name of the poller
    * @param _notifier the class that receives the notification
    * @param _config the crm poller config
    * @return the poller configuration
    */
   public static PollerConfig getConfig(String _name, PollerStatusNotifier _notifier, ErPollerConfig _config)
   {
      PollerConfig config =
         new PollerConfig(
            _name,
            _config.getEventRecordDir(),
            _config.getPollerERParser(),
            _config.getLastByteFile(),
            _config.getCurrExtFile(),
            _config.getFilePollInterval(),
            _config.getFileTimeout(),
            _config.getReadPollInterval(),
            _config.getPauseBeforeNextFileDelay(),
            _config.getWriteLastByteInterval(),
            _config.getDefaultExt(),
            new PollerStatusNotifier[] { _notifier },
            _config.getMaxExtension(),
            _config.getExtPattern());

      return config;
   }
   
   
   public int getQueueSize()
   {
       return queueSize_;
   }
   
   
   public int getThreads()
   {
       return threads_;
   }

   public void addERHandler(int erid, EventProcessor eventProcessor)
   {
       addERHandler(String.valueOf(erid), eventProcessor);
   }

   public void addERHandler(long erid, EventProcessor eventProcessor)
   {
       addERHandler(String.valueOf(erid), eventProcessor);
   }

   public void addERHandler(short erid, EventProcessor eventProcessor)
   {
       addERHandler(String.valueOf(erid), eventProcessor);
   }
   
   @Override
   public void addERHandler(String erid, EventProcessor eventProcessor)
   {
       if( eventProcessor instanceof CRMProcessor )
       {
           processors_.add((CRMProcessor)eventProcessor);
       }
       super.addERHandler(erid, eventProcessor);
   }
   
   

/**
   * Starts the poller
    * @see java.lang.Runnable#run()
    */
   public void run()
   {
       startFileReader();
   }
   
   /**
    * Stops the poller
    */
   public void stop()
   {
       stopRunning();
       for( CRMProcessor processor : processors_ )
       {
           if( processor != null )
           {
               try
               {
                   processor.stop();   
               }
               catch( Throwable t )
               {}
           }
       }
   }
      
   private int queueSize_; // capacity of the queue waiting for an available thread
   private int threads_; // maximum number of threads to simultaneously execute
   private Context context = null;
   private List<CRMProcessor> processors_ = new ArrayList<CRMProcessor>();
   protected ErPollerConfig config_ = null;

}
