package com.trilogy.app.crm.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class OMDataFixer 
{

	   static void process(String id, String instant_id, String orignalFileName, String outFileName)
	   throws Exception 
	   {


		   BufferedReader in = new BufferedReader(new FileReader(orignalFileName));
		   PrintWriter out = new PrintWriter( new FileWriter( outFileName));


		   String text = in.readLine(); 
		   while ( text != null) 
		   {

			   StringTokenizer tokenizer = new StringTokenizer(text, ",");
			   int i = 0; 
			   out.print(id + "," + instant_id ); 

			   while (tokenizer.hasMoreTokens())
			   {
				   String s = tokenizer.nextToken().trim();

				   if (i > 1 && i < 7)
					   out.print( s );
				   else if ( i == 7 || i == 8) 
					   out.print(s.replaceAll(",", ":")); 

				   if (  i == 2 || i == 4  )
					   out.print(" ");  

				   if ( i != 0 && i != 2 && i != 4  && i != 8) 
					   out.print(","); 
				   ++i;  
			   }
			   out.print("\n"); 
			   text = in.readLine(); 
		   }

		   in.close(); 
		   out.flush(); 
		   out.close(); 

	   }
		
		
		
		static void Main(String[] args)
		{
			if (args.length != 4 )
			System.out.println("Usage: java OMDataFixer [baseline_id] [instant_id] [original file name] [output file Name]"); 	
			
			try {
				process(args[0], args[1], args[2], args[3]);
			} catch (Exception e )
			{
				e.printStackTrace(); 
			}
			
		}
}
