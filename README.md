# ProttoApi
Java Semi-Asynchronous HTTP GET & POST minimal library

### Examples
```java
import java.io.*;
import org.json.JSONObject;
import prottoapi.ClientSession;
import prottoapi.ResponseObject;

int numOfWorkers = 4;
ClientSession session = new ClientSession(numOfWorkers);

// ... Performs requests and instructions here

session.close();
```

##### HTTP Get Request
```java
String url = "https://httpbin.org/get";

JSONObject parameters = new JSONObject();
parameters.put("key", "value");
parameters.put("key are", "auto url encoded");

ResponseObject resp = session.get(url, parameters);

if (resp.onReady()) { // holds until data is ready
	String textReceived   = resp.text();
	JSONObject recvInJson = resp.json();
	byte[] rawByteData    = resp.bytes(); 
}
```

##### Api mini-docs
```java
/*
session.get() or session.post()
	(String url, 
	JSONObject parameters,
	Object httpPostFormData,
	JSONOjbect extraHeaders);
*/
```

* url        : the raw string url (without parameters preferrably)
* parameters : json object of url parameters
* formdata   : object (normally string or json) to be posted in HTTP POST
* headers    : json object of extra headers to add upon http request

> All arguments are optional
  ex: session.get(url, null, null, headersJson);
  session.post(url, null, postdata);
 
##### Download file
```java
String downloadUrl = "http://downloadUrl/file.exe";
String[] urlParts  = downloadUrl.split("\\/");
String fileName    = urlParts[urlParts.length - 1];
String[] fileParts = fileName.split("\\.");
String extension   = fileParts[fileParts.length - 1];

String outPath       = "newfile." + extension;
FileOutputStream out = new FileOutputStream(outPath);

ResponseObject resp  = session.get(downloadUrl);

if (resp.isReady()) {
	try {
	
		byte[] urlData = resp.bytes();
		out.write(urlData);
		
	} catch (Exception e) {
		System.err.println("Error on downloading file!");
		e.printStackTrace();
	}
	out.close();
}
```