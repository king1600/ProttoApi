package prottoapi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.json.JSONObject;

public class AsyncInfoLoader implements Runnable
{
	private AsyncRequestObject request;
	
	public AsyncInfoLoader(AsyncRequestObject req)
	{
		request = req;
		request.response.url = request.url;
	}
	
	public void getHeaders() throws Exception
	{
		// get Request headers
		JSONObject responseHeaders = new JSONObject();		 // header json
		
		try {
			Set<Entry<String, List<String>>> headers; 			 // raw headers
			headers = request.conn.getHeaderFields().entrySet(); // set raw headers
			for (Entry<String, List<String>> header : headers)   // iterate over raw header
			{ 
				String key   = "";          					 // key declaration
				String value = "";								 // value declaration
				key = header.getKey();					 // key initialization
				for (String s : header.getValue())
				{
					value = s; break;          		 // value initialization
				}
				if (value != null && key != null)
				{
					try 
					{
						// add header key + value to responseHeaders json object
						responseHeaders.put(key, value);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error fetching headers!");
			e.printStackTrace();
		}
		// add extra headers
		try {
			responseHeaders.put("Content-Type", request.conn.getContentType());
			responseHeaders.put("Content-Length", request.conn.getContentLength());
		} catch (Exception e) {
			System.err.println("Error getting content info!");
			e.printStackTrace();
		}
		request.response.setHeader(responseHeaders);
	}
	
	public void loadData() throws Exception
	{	
		// get input stream
		boolean canStream  = false;
		InputStream stream = null;
		String encoding    = request.conn.getContentEncoding();
		try
		{
			if (encoding != null)
			{
				if (encoding.contains("gzip"))
				{
					stream = new GZIPInputStream(request.conn.getInputStream());
					canStream = true;
				}
			}
			else
			{
				stream = request.conn.getInputStream();
				canStream = true;
			}
		}
		catch (Exception e)
		{
			System.err.println("Cannot Retrieve stream!: " + request.response.status + " " + request.response.reason);
		}
		
		if (!canStream) // quit if stream cannot be processed
		{
			request.response.setData(null);
			request.response.setText(null);
			request.response.setJson();
			request.response.setReady();
			return;
		}
		
		// setup buffered streams
		BufferedInputStream in    = new BufferedInputStream(stream);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		// read stream into byte array
		byte[] buffer = new byte[AsyncRequestObject.BUFFER_SIZE];
		int n = -1;
		while ((n = in.read(buffer)) != -1) 
		{
		    out.write(buffer, 0, n);
		}
		
		// get bytedata and string data
		byte[] byteData   = out.toByteArray();
		String stringData = new String(byteData, request.UTF_8);
		
		// close streams & cleanup objects
		out.close();
		stream.close();
		buffer = null;
		in     = null;
		out    = null;
		stream = null;
		
		// set the data
		request.response.setData(byteData);
		request.response.setText(stringData);
		request.response.setJson();
		request.response.setReady();
	}
	
	@Override
	public void run() // start fecthing info
	{
		try
		{
			// set the response Object's status
			int responseStatus = request.conn.getResponseCode();
			request.response.status = responseStatus;
			
			// set the reponse Object's reason
			String responseReason = request.conn.getResponseMessage();
			request.response.reason = responseReason;
			
			// set the response Object's headers
			getHeaders();
			
			// get content and byte data
			loadData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			request.response.setReady();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
