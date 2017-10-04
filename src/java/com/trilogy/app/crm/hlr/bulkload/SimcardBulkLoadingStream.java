package com.trilogy.app.crm.hlr.bulkload;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.trilogy.app.crm.bas.tps.CommentSkippingReader;

public class SimcardBulkLoadingStream 
{

	public SimcardBulkLoadingStream(InputStream inputStream)
	{
		this.reader_ = new CommentSkippingReader(new BufferedReader(new InputStreamReader(inputStream)));
	}
	
	public SimcardInput readLine()
	throws EOFException, IOException, InvalidInputException
	{
		String line = reader_.readLine(); 
		
		if (line != null )
		{
			SimcardInput ret = getSimcardInput(line);
			ret.parse(); 
			return ret; 
		}
		
		throw new EOFException("the end of file");
	}
	
	
	public SimcardInput getSimcardInput(String line)
	throws InvalidInputException
	{
		// for now there is only one format of input, 
		// there could be more in future. need update this this method
		// when we know to how to identify different format. 
		SimcardInput ret =new DorySimcardInput(line);
		ret.parse();
		return ret; 
	}
	
	public void close()
	throws IOException
	{
		reader_.close();	
	}
	
	CommentSkippingReader reader_; 
}
