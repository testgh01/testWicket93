/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.ws;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.ServletRequestCopy;
import org.apache.wicket.protocol.ws.api.WebSocketRequest;
import org.apache.wicket.protocol.ws.api.WebSocketResponse;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.concurrent.Executor;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;

/**
 *
 */
public class WebSocketSettings implements IWebSocketSettings
{
	/**
	 * The executor that handles the processing of Web Socket push message broadcasts.
	 */
	private Executor webSocketPushMessageExecutor = new SameThreadExecutor();

	/**
	 * The executor that handles broadcast of the {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
	 * via Wicket's event bus.
	 */
	private Executor sendPayloadExecutor = new SameThreadExecutor();

	/**
	 * Tracks all currently connected WebSocket clients
	 */
	private IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();

	@Override
	public IWebSocketSettings setWebSocketPushMessageExecutor(Executor executor)
	{
		Args.notNull(executor, "executor");
		this.webSocketPushMessageExecutor = executor;
		return this;
	}

	@Override
	public IWebSocketConnectionRegistry getConnectionRegistry()
	{
		return connectionRegistry;
	}

	@Override
	public IWebSocketSettings setConnectionRegistry(IWebSocketConnectionRegistry connectionRegistry)
	{
		Args.notNull(connectionRegistry, "connectionRegistry");
		this.connectionRegistry = connectionRegistry;
		return this;
	}

	@Override
	public Executor getWebSocketPushMessageExecutor()
	{
		return webSocketPushMessageExecutor;
	}

	/**
	 * The executor that broadcasts the {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
	 * via Wicket's event bus.
	 * Default executor does all the processing in the caller thread.
	 *
	 * @param sendPayloadExecutor
	 *            The executor used for broadcasting the events with web socket payloads to
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}s and
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketResource}s.
	 */
	public WebSocketSettings setSendPayloadExecutor(Executor sendPayloadExecutor)
	{
		Args.notNull(sendPayloadExecutor, "sendPayloadExecutor");
		this.sendPayloadExecutor = sendPayloadExecutor;
		return this;
	}

	/**
	 * The executor that broadcasts the {@link org.apache.wicket.protocol.ws.api.event.WebSocketPayload}
	 * via Wicket's event bus.
	 *
	 * @return
	 *            The executor used for broadcasting the events with web socket payloads to
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}s and
	 *            {@link org.apache.wicket.protocol.ws.api.WebSocketResource}s.
	 */
	public Executor getSendPayloadExecutor()
	{
		return sendPayloadExecutor;
	}

	/**
	 * A factory method for the {@link org.apache.wicket.request.http.WebResponse}
	 * that should be used to write the response back to the client/browser
	 *
	 * @param connection
	 *              The active web socket connection
	 * @return the response object that should be used to write the response back to the client
	 */
	public WebResponse newWebSocketResponse(IWebSocketConnection connection)
	{
		return new WebSocketResponse(connection);
	}

	/**
	 * A factory method for the {@link org.apache.wicket.request.http.WebRequest}
	 * that should be used in the WebSocket processing request cycle
	 *
	 * @param request
	 *              The upgraded http request
	 * @param filterPath
	 *              The configured filter path of WicketFilter in web.xml
	 * @return the request object that should be used in the WebSocket processing request cycle
	 */
	public WebRequest newWebSocketRequest(HttpServletRequest request, String filterPath)
	{
		return new WebSocketRequest(new ServletRequestCopy(request), filterPath);
	}

	/**
	 * Simple executor that runs the tasks in the caller thread.
	 */
	public static class SameThreadExecutor implements Executor
	{
		@Override
		public void run(Runnable command)
		{
			command.run();
		}

		@Override
		public <T> T call(Callable<T> callable) throws Exception
		{
			return callable.call();
		}
	}
}
