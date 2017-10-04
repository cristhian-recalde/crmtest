package com.trilogy.app.crm.pos;

import java.util.Date;

import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;

public abstract class AbstractAccumulatorVisitor implements Visitor, CountProcessVisitor 
{

	/** 
     * Constructor that sets the end date for our lookup range. 
     * @param date
     */
    public AbstractAccumulatorVisitor(Context ctx, final Date date, POSLogWriter logWriter)
    {
        currentDate_ = date;
        numberVisited = 0;
        numberProcessed = 0;
        numberSuccessfullyProcessed = 0;
        logger = logWriter;
    }

	/**
     * Returns TRUE if the date given before the currentDate_.
     * Otherwise, returns FALSE.
     * @param dateOfExtraction
     * @return
     */
    protected boolean isDateOfExtractionBeforeCurrentDate(Date dateOfExtraction)
    {
        dateOfExtraction = CalendarSupportHelper.get().getDateWithNoTimeOfDay(dateOfExtraction);
        Date today = CalendarSupportHelper.get().getDateWithNoTimeOfDay(currentDate_);
        return dateOfExtraction.before(today);
    }
    
    /**
     * Increments the numberVisited by one.
     */
    public static synchronized void incrementNumberVisited()
    {
        numberVisited++;
    }
    
    /**
     * Returns the number of accounts visited by the visitor
     * @return
     */
    public int getNumberVisited()
    {
        return numberVisited;
    }
    
    /**
     * Returns the number of accounts processed by the visitor
     * @return
     */
    public int getNumberProcessed()
    {
        return numberProcessed;
    }
    
    /**
     * Increments the numberSuccessfullyProcessed by one.
     */
    public static synchronized void incrementNumberSuccessfullyProcessed()
    {
        numberSuccessfullyProcessed++;
    }
    
    /**
     * Increments the numberProcessed by one.
     */
    public static synchronized void incrementNumberProcessed()
    {
        numberProcessed++;
    }
    
    /**
     * Returns the number of accounts processed by the visitor
     * for which cashier records were successfully made
     * @return
     */
    public int getNumberSuccessfullyProcessed()
    {
        return numberSuccessfullyProcessed;
    }
    
    public Date getCurrentDate()
    {
        return currentDate_;
    }
    
    public POSLogWriter getLogger()
    {
        return logger;
    }
    
    /** The current date passed in from the Cron task**/
    protected static Date currentDate_;
    
    /** the number of accounts visited by this visitor */
    protected static int numberVisited;
    /** the number of accounts processed by this visitor */
    protected static int numberProcessed;
    /** the number of accounts processed successfully into cashier records by this visitor */
    protected static int numberSuccessfullyProcessed;
    /** File Writer for log **/
    protected static POSLogWriter logger;

    /**
	 * 
	 */
	private static final long serialVersionUID = -1108118326516519174L;
	

}
