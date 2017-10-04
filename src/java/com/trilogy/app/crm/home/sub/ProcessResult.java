package com.trilogy.app.crm.home.sub;

/**
 * @author lzou
 * @date   Dec 22, 2003
 *
 * Used when a subscriber gets removed from home, holding
 * result code returned by different processes.
 *
 */
public class ProcessResult
{
    protected int result_;
	/**
	 * 
	 */
	public ProcessResult()
	{
	      result_ = 0;   
	}
      
    public ProcessResult(int initial)
    {
          result_ = initial;
    }
      
    public void setResultCode(int result)
    {
           result_ = result;
    }
    
    public int getResultCode()
    {
          return result_;
    }

    public String toString()
    {
        return "ProcessResult[" + result_ + "]";
    }
}
