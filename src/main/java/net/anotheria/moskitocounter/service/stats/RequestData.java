package net.anotheria.moskitocounter.service.stats;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import net.anotheria.util.StringUtils;

/**
 * Common request data holder, actually holds servlet request data.
 *
 * @author sshscp
 */
public class RequestData implements Serializable {
	/**
	 * Basic serial version UID.
	 */
	private static final long serialVersionUID = 1318460282947993474L;
	/**
	 * Remote ip.
	 */
	private String ip;
	/**
	 * Request path info itself.
	 */
	private String pathInfo;
	/**
	 * Request query string.
	 */
	private String queryString;
	/**
	 * Request parameters.
	 */
	private Map<String, String[]> parameters;
	/**
	 * Request headers.
	 */
	private Map<String, String[]> headers;

	public String getIp() {
		return ip;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public Map<String, String[]> getHeaders() {
		return headers;
	}

	/**
	 * Stuff builder facility.
	 */
	public static final class Builder {
		/**
		 * Remote ip.
		 */
		private String ip;
		/**
		 * Request path info itself.
		 */
		private String pathInfo = "";
		/**
		 * Request query string.
		 */
		private String queryString = "";
		/**
		 * Request parameters.
		 */
		private Map<String, String[]> parameters = Collections.emptyMap();
		/**
		 * Request headers.
		 */
		private Map<String, String[]> headers = Collections.emptyMap();

		/**
		 * Allow to provide ip.
		 *
		 * @param ip
		 * 		ip address
		 * @return {@link Builder}
		 */
		public Builder withIp(final String ip) {
			if (StringUtils.isEmpty(ip))
				return this;
			this.ip = ip;
			return this;
		}

		/**
		 * Allow to provide pInfo.
		 *
		 * @param pInfo
		 * 		requested pInfo
		 * @return {@link Builder}
		 */
		public Builder withPathInfo(final String pInfo) {
			if (StringUtils.isEmpty(pInfo))
				return this;
			this.pathInfo = pInfo;
			return this;
		}

		/**
		 * Allow to provide queryString.
		 *
		 * @param qStr
		 * 		query string
		 * @return {@link Builder}
		 */
		public Builder withQueryString(final String qStr) {
			if (StringUtils.isEmpty(qStr))
				return this;
			this.queryString = qStr;
			return this;
		}

		/**
		 * Allow to provide parameters.
		 *
		 * @param params
		 * 		parameters map
		 * @return {@link Builder}
		 */
		public Builder withParameters(final Map<String, String[]> params) {
			if (params == null)
				return this;
			this.parameters = params;
			return this;
		}

		/**
		 * Allow to provide parameters.
		 *
		 * @param headers
		 * 		parameters map
		 * @return {@link Builder}
		 */
		public Builder withHeaders(final Map<String, String[]> headers) {
			if (headers == null)
				return this;
			this.headers = headers;
			return this;
		}

		/**
		 * Creates {@link RequestData} instance.
		 *
		 * @return {@link RequestData}
		 */
		public RequestData build() {
			final RequestData result = new RequestData();
			result.ip = this.ip;
			result.pathInfo = this.pathInfo;
			result.queryString = this.queryString;
			result.parameters = this.parameters;
			result.headers = this.headers;

			return result;
		}


	}


}
