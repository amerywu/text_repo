/*
 * Copyright (c) 2013-2014 Vehbi Sinan Tunalioglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ikoda.netio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Defines an OpenCPU service consumer class.
 *
 * TODO: Complete documentation.
 *
 * @author Vehbi Sinan Tunalioglu.
 */
public class IKodaOpenCPURuntimeEnvironment
{

	/**
	 * Defines enumeration class for allowed HTTP methods for OpenCPU communication.
	 */
	public enum HTTPMethods
	{
		GET, POST
	}

	/**
	 * Defines an HTTP status code enumeration class.
	 */
	public enum HTTPStatusCode
	{
		// Define HTTP status codes:
		OK(200), CREATED(201), FOUND(302), BAD_REQUEST(400), BAD_GATEWAY(502), SERVICE_UNAVAILABLE(503);

		/**
		 * Defines the numeric code of the HTTP status code.
		 */
		private int code;

		/**
		 * Constructor consuming the numeric HTTP status code.
		 * 
		 * @param code
		 */
		private HTTPStatusCode(int code)
		{
			this.code = code;
		}

		/**
		 * Returns the numeric HTTP Status code.
		 * 
		 * @return
		 */
		public int getCode()
		{
			return this.code;
		}
	}

	// Define HTTP Endpoint URL schemas:
	private static final String GlobalPackagePath = "/library/${package}";
	private static final String UserPackagePath = "/user/${user}/library/${package}";
	private static final String CranPackagePath = "/cran/${package}";
	private static final String BioconductorPackagePath = "/bioc/${package}";
	private static final String GithubPackagePath = "/github/${user}/${package}";
	private static final String SessionOutputPath = "/tmp/${key}";
	private static final String GistPath = "/gist/${user}";
	private static final String PackageInfoPath = "/info";
	private static final String PackageObjectPath = "/R/${function}";
	private static final String PackageDataPath = "/data";
	private static final String PackageManPath = "/man";
	private static final String SLASH = "/";

	private String rootPath;

	/**
	 * Defines the base URI of the OpenCPU service.
	 */
	private URI baseURI;

	/**
	 * Default constructor for the {@link OpenCPURuntimeEnvironment}.
	 *
	 * @throws URISyntaxException
	 *             URISyntaxException thrown.
	 */
	public IKodaOpenCPURuntimeEnvironment() throws URISyntaxException
	{
		rootPath = "/ocpu";
		this.setBaseURI(new URI("http", null, "localhost", 9999, "/ocpu", null, null));
	}

	public IKodaOpenCPURuntimeEnvironment(String scheme, String host, int port, String rootPath)
			throws URISyntaxException
	{
		this.rootPath = rootPath;
		this.setBaseURI(new URI(scheme, null, host, port, rootPath, null, null));
	}

	public IKodaOpenCPURuntimeEnvironment(String scheme, String userInfo, String host, int port, String rootPath)
			throws URISyntaxException
	{
		this.rootPath = rootPath;
		this.setBaseURI(new URI(scheme, userInfo, host, port, rootPath, null, null));
	}

	/**
	 * Returns the base URI of the OpenCPU service.
	 *
	 * @return The base URI of the OpenCPU service.
	 */
	public URI getBaseURI()
	{
		return baseURI;
	}

	/**
	 * Calls a remote procedure (function) which consumes and produces JSON objects.
	 *
	 * @param cpackage
	 *            The name of the package of the function to be called.
	 * @param function
	 *            The name of the function to be called.
	 * @param input
	 *            The input as a JSON string.
	 * @return The output as a JSON string.
	 * @throws NetioException
	 *             Problem Error thrown.
	 * @throws NetioException
	 *             Runtime Error thrown.
	 */
	public String getDataWithHTTPRequest(String path) throws NetioException, NetioException
	{
		// Get an http client:
		HttpClient httpClient = HttpClientBuilder.create().build();

		String URL = this.getBaseURI().toString();
		StringBuilder sb = new StringBuilder();
		sb.append(URL);

		if (path.startsWith(rootPath))
		{
			path = path.substring(rootPath.length(), path.length());
		}

		if (path.startsWith(SLASH))
		{
			sb.append(path);
		}
		else
		{
			sb.append(SLASH);
			sb.append(path);
		}

		NioLog.getOcuLogger().info("\n\n\nGET REQUEST FULL URL: " + sb.toString());
		// Initialize the HTTP POST request:
		HttpGet request = new HttpGet(sb.toString());

		NioLog.getOcuLogger().info("\n\n\n\nheader set:\n\n\n ");
		// Set the request content type:
		request.setHeader("Content-type", "application/text");

		// Send the request and get the response:
		NioLog.getOcuLogger().info("Sending request to OpenCPU server (" + URL + ").");
		HttpResponse response = null;
		try
		{
			response = httpClient.execute(request);
			NioLog.getOcuLogger().info("\n\n\n\n" + response + "\n\n\n");
		}
		catch (IOException e)
		{
			NioLog.getOcuLogger().error("Cannot execute the HTTP request.");
			throw new NetioException("Cannot execute the HTTP request.");
		}

		// Get the response status code:
		int statusCode = response.getStatusLine().getStatusCode();

		// Check the response status code and act accordingly. Note that we
		// don't expect a 201 code
		// as JSON I/O RPC is producing plain 200 with the result in the
		// response body.
		if (statusCode == HTTPStatusCode.OK.getCode())
		{
			NioLog.getOcuLogger().info("Response received successfully.");

			// Get the HTTP entity:
			HttpEntity entity = response.getEntity();

			// Check the HTTP entity:
			if (entity == null)
			{
				NioLog.getOcuLogger().error("No content received from OpenCPU Server.");
				throw new NetioException("No content received from OpenCPU Server.");
			}

			// Return the results:
			try
			{
				return EntityUtils.toString(entity);
			}
			catch (IOException e)
			{
				NioLog.getOcuLogger().error("Cannot read the output from OpenCPU server response.");
				throw new NetioException("Cannot read the output from OpenCPU server response.");
			}
		}
		if (statusCode == HTTPStatusCode.CREATED.getCode())
		{
			NioLog.getOcuLogger().info("Response received successfully.");

			// Get the HTTP entity:
			HttpEntity entity = response.getEntity();

			// Check the HTTP entity:
			if (entity == null)
			{
				NioLog.getOcuLogger().error("No content received from OpenCPU Server.");
				throw new NetioException("No content received from OpenCPU Server.");
			}

			// Return the results:
			try
			{
				return EntityUtils.toString(entity);
			}
			catch (IOException e)
			{
				NioLog.getOcuLogger().error("Cannot read the output from OpenCPU server response.");
				throw new NetioException("Cannot read the output from OpenCPU server response.");
			}
		}
		else if (statusCode == HTTPStatusCode.FOUND.getCode())
		{
			NioLog.getOcuLogger().error("Response is redirected.");
			throw new NetioException("Response is redirected.");
		}
		else if (statusCode == HTTPStatusCode.BAD_REQUEST.getCode())
		{
			// Declare the error message:
			String message = "";
			// Get the HTTP entity:
			HttpEntity entity = response.getEntity();

			// Check the HTTP entity:
			if (entity == null)
			{
				NioLog.getOcuLogger().error("No content received from OpenCPU Server.");
				message = "No content received from OpenCPU Server.";
			}
			else
			{
				// Return the results:
				try
				{
					message = EntityUtils.toString(entity);
				}
				catch (IOException e)
				{
					NioLog.getOcuLogger().error("Cannot read the output from OpenCPU server response.");
					throw new NetioException("Cannot read the output from OpenCPU server response.");
				}
			}
			NioLog.getOcuLogger().error("Bad request: " + message);
			throw new NetioException("Bad Request: " + message);
		}
		else if (statusCode == HTTPStatusCode.BAD_GATEWAY.getCode())
		{
			NioLog.getOcuLogger().error("OpenCPU Server is not responsive (" + statusCode + ").");
			throw new NetioException("OpenCPU Server is not responsive (" + statusCode + ").");
		}
		else if (statusCode == HTTPStatusCode.SERVICE_UNAVAILABLE.getCode())
		{
			NioLog.getOcuLogger().error("OpenCPU Server is not responsive (" + statusCode + ").");
			throw new NetioException("OpenCPU Server is not responsive (" + statusCode + ").");
		}

		// TODO: Change the structure of this code as it reads like a VB code.
		NioLog.getOcuLogger().error("Unrecognized response from the OpenCPU Server: " + statusCode);
		throw new NetioException("Unrecognized response from the OpenCPU Server: " + statusCode);
	}

	public String parseFunctionCallResponse(String stringToParse, String targetReturnStringSuffix)
	{
		stringToParse = stringToParse.replaceAll("\r\n", "xx");
		stringToParse = stringToParse.replaceAll("\r", "");
		stringToParse = stringToParse.replaceAll("\n", "");
		String[] accessStrings = stringToParse.split("xx");
		NioLog.getOcuLogger().warn("accessStrings length " + accessStrings.length);

		for (int i = 0; i < accessStrings.length; i++)
		{

			if (accessStrings[i].toUpperCase().trim().endsWith(targetReturnStringSuffix.toUpperCase().trim()))
			{
				return accessStrings[i];
			}
		}
		NioLog.getOcuLogger().warn(targetReturnStringSuffix + " not found in " + stringToParse);
		return null;

	}

	/**
	 * Calls a remote procedure (function) which consumes and produces JSON objects.
	 *
	 * @param cpackage
	 *            The name of the package of the function to be called.
	 * @param function
	 *            The name of the function to be called.
	 * @param input
	 *            The input as a JSON string.
	 * @return The output as a JSON string.
	 * @throws NetioException
	 *             Problem Error thrown.
	 * @throws NetioException
	 *             Runtime Error thrown.
	 */
	public String rpc(String cpackage, String function, String input) throws NetioException
	{
		return rpc(cpackage, function, input, 90);
	}

	/**
	 * Calls a remote procedure (function) which consumes and produces JSON objects.
	 *
	 * @param cpackage
	 *            The name of the package of the function to be called.
	 * @param function
	 *            The name of the function to be called.
	 * @param input
	 *            The input as a JSON string.
	 * @return The output as a JSON string.
	 * @throws NetioException
	 *             Problem Error thrown.
	 * @throws NetioException
	 *             Runtime Error thrown.
	 */
	public String rpc(String cpackage, String function, String input, int timeout) throws NetioException
	{
		// Get an http client:

		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		// Construct the path.
		String URL = this.getBaseURI().toString() + new StrSubstitutor(new HashMap<String, String>()
		{
			{
				put("package", cpackage);
				put("function", function);
			}
		}).replace(GlobalPackagePath + PackageObjectPath);
		NioLog.getOcuLogger().info("\n\n\nPOST REQUEST URL: " + URL);
		// Initialize the HTTP POST request:
		HttpPost request = new HttpPost(URL);

		NioLog.getOcuLogger().info("\n\n\n\nheader set:\n\n\n ");
		// Set the request content type:
		request.setHeader("Content-type", "application/json");

		// Set the payload:
		NioLog.getOcuLogger().info("header set: ");
		try
		{
			request.setEntity(new StringEntity(input));
		}
		catch (UnsupportedEncodingException e)
		{
			NioLog.getOcuLogger().error("The encoding of the input is not supported.");
			throw new NetioException("The encoding of the input is not supported.");
		}

		// Send the request and get the response:
		NioLog.getOcuLogger().info("Sending request to OpenCPU server (" + URL + ").");
		HttpResponse response = null;
		try
		{

			response = httpClient.execute(request);
			NioLog.getOcuLogger().info("\n\n\n\n" + response + "\n\n\n");
		}
		catch (IOException e)
		{
			NioLog.getOcuLogger().error("Cannot execute the HTTP request.");
			throw new NetioException("Cannot execute the HTTP request." + e.getMessage(), e);
		}

		// Get the response status code:
		int statusCode = response.getStatusLine().getStatusCode();

		// Check the response status code and act accordingly. Note that we
		// don't expect a 201 code
		// as JSON I/O RPC is producing plain 200 with the result in the
		// response body.
		if (statusCode == HTTPStatusCode.OK.getCode())
		{
			NioLog.getOcuLogger().info("Response received successfully.");

			// Get the HTTP entity:
			HttpEntity entity = response.getEntity();

			// Check the HTTP entity:
			if (entity == null)
			{
				NioLog.getOcuLogger().error("No content received from OpenCPU Server.");
				throw new NetioException("No content received from OpenCPU Server.");
			}

			// Return the results:
			try
			{
				return EntityUtils.toString(entity);
			}
			catch (IOException e)
			{
				NioLog.getOcuLogger().error("Cannot read the output from OpenCPU server response.");
				throw new NetioException("Cannot read the output from OpenCPU server response.");
			}
		}
		if (statusCode == HTTPStatusCode.CREATED.getCode())
		{
			NioLog.getOcuLogger().info("Response received successfully.");

			// Get the HTTP entity:
			HttpEntity entity = response.getEntity();

			// Check the HTTP entity:
			if (entity == null)
			{
				NioLog.getOcuLogger().error("No content received from OpenCPU Server.");
				throw new NetioException("No content received from OpenCPU Server.");
			}

			// Return the results:
			try
			{
				return EntityUtils.toString(entity);
			}
			catch (IOException e)
			{
				NioLog.getOcuLogger().error("Cannot read the output from OpenCPU server response.");
				throw new NetioException("Cannot read the output from OpenCPU server response.");
			}
		}
		else if (statusCode == HTTPStatusCode.FOUND.getCode())
		{
			NioLog.getOcuLogger().error("Response is redirected.");
			throw new NetioException("Response is redirected.");
		}
		else if (statusCode == HTTPStatusCode.BAD_REQUEST.getCode())
		{
			// Declare the error message:
			String message = "";
			// Get the HTTP entity:
			HttpEntity entity = response.getEntity();

			// Check the HTTP entity:
			if (entity == null)
			{
				NioLog.getOcuLogger().error("No content received from OpenCPU Server.");
				message = "No content received from OpenCPU Server.";
			}
			else
			{
				// Return the results:
				try
				{
					message = EntityUtils.toString(entity);
				}
				catch (IOException e)
				{
					NioLog.getOcuLogger().error("Cannot read the output from OpenCPU server response.");
					throw new NetioException("Cannot read the output from OpenCPU server response.");
				}
			}
			NioLog.getOcuLogger().error("Bad request: " + message);
			throw new NetioException("Bad Request: " + message);
		}
		else if (statusCode == HTTPStatusCode.BAD_GATEWAY.getCode())
		{
			NioLog.getOcuLogger().error("OpenCPU Server is not responsive (" + statusCode + ").");
			throw new NetioException("OpenCPU Server is not responsive (" + statusCode + ").");
		}
		else if (statusCode == HTTPStatusCode.SERVICE_UNAVAILABLE.getCode())
		{
			NioLog.getOcuLogger().error("OpenCPU Server is not responsive (" + statusCode + ").");
			throw new NetioException("OpenCPU Server is not responsive (" + statusCode + ").");
		}

		// TODO: Change the structure of this code as it reads like a VB code.
		NioLog.getOcuLogger().error("Unrecognized response from the OpenCPU Server: " + statusCode);
		throw new NetioException("Unrecognized response from the OpenCPU Server: " + statusCode);
	}

	/**
	 * Sets the base URI of the OpenCPU service.
	 *
	 * @param baseURI
	 *            The base URI of the OpenCPU service.
	 */
	public void setBaseURI(URI baseURI)
	{
		this.baseURI = baseURI;
	}
}
