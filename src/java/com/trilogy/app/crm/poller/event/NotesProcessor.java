/**
 * 
 */
package com.trilogy.app.crm.poller.event;

import java.util.Iterator;
import java.util.List;

/**
 * @author skularajasingham
 *
 */
public abstract class NotesProcessor extends CRMProcessor {

    /**
     * Creates a new NotesProcessor.
     */
	public NotesProcessor() {
		super();
	}

    /**
     * This method formats the given list of parameters into a string
     * and returns the string for debugging purpose.
     *
     * @param _params The given list of paramters.
     * 
     * @return String The returning parameter list in String format.
     */
    public static String getDebugParams(List _params)
    {
        Iterator iParams = _params.iterator();
        int index = 0;

        StringBuilder buf = new StringBuilder();
        while (iParams.hasNext())
        {
            buf.append(index);
            buf.append("[");
            buf.append(CRMProcessorSupport.getField(_params, index));
            buf.append("] ");

            iParams.next();
            index++;
        }

        return buf.toString();
    }
    

}
