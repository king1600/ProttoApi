package prottoapi;

import org.json.JSONObject;

public class ResponseObject {

	// class info
	public String url;
	public JSONObject headers;
	public Integer status;
	public String reason;
	private String contentData;
	private JSONObject jsonData;
	private byte[] byteData;
	
	// conditional fetching
	private boolean hasContent = false;
	private boolean hasBytes   = false;
	private boolean hasJson    = false;
	private boolean isReady    = false; 
	private Object contentLock;
	private Object bytesLock;
	private Object jsonLock;
	private Object readyLock;
	
	// initialize empty class objects
	public ResponseObject () {
		headers     = null;
		status      = null;
		reason      = null;
		contentData = null;
		byteData    = null;
		jsonData    = null;
		contentLock = new Object();
		bytesLock   = new Object();
		jsonLock    = new Object();
		readyLock   = new Object();
	}
	
	/**** event methods *****/
	
	public boolean onReady() throws Exception// returns when fetching is finished
	{
		if (!isReady)
		{
			synchronized(readyLock) 
			{
				readyLock.wait();
			}
			isReady = true;
		}
		return true;
	}
	
	public void setReady() throws Exception
	{
		synchronized(readyLock) 
		{
			readyLock.notifyAll();
		}
		isReady = true;
	}
	
	/***** set methods *****/
	
	// set Text
	public void setText(String text) {
		this.contentData = text;
		this.hasContent = true;
		
		// text() can now return
		synchronized(contentLock)
		{
			contentLock.notifyAll();
		}
	}
	
	// set Header & Json info
	public void setHeader(JSONObject header) throws Exception {
		// set header
		this.headers = header;
	}
	
	// set Json info
	public void setJson() throws Exception
	{
		try {
			// set json
			String contentType = (String)headers.get("Content-Type");
			if(contentType.contains("application/json"))
			{
				if(contentData.startsWith("{"))
				{
					jsonData = new JSONObject(contentData);
				}
			}
			// json() or jsonArray() can now return
			this.hasJson = true;
			synchronized(jsonLock)
			{
				jsonLock.notifyAll();
			}
		} catch (Exception e) {
			System.err.println("Error on Setting JSON: ");
			e.printStackTrace();
		}
	}
	
	// set Bytes Data
	public void setData(byte[] data) {
		this.byteData = data;
		this.hasBytes = true;
		
		// bytes can now return
		synchronized(bytesLock)
		{
			bytesLock.notifyAll();
		}
	}
	
	/***** get methods *****/
	
	// get text & lock until has object
	public String text () throws Exception {
		if (!this.hasContent)
		{
			synchronized(contentLock)
			{
				contentLock.wait();
			}
			this.hasContent = true;
		}
		return this.contentData;
	}
	
	// get reponse as jsonObject & lock until has object
	public JSONObject json() throws Exception {
		if (!this.hasJson)
		{
			synchronized(jsonLock)
			{
				jsonLock.wait();
			}
			this.hasJson = true;
		}
		return this.jsonData;
	}
	
	// get response in byte array form
	public byte[] bytes() throws Exception {
		if (!this.hasBytes)
		{
			synchronized(bytesLock)
			{
				bytesLock.wait();
			}
			this.hasBytes = true;
		}
		return this.byteData;
	}
}
