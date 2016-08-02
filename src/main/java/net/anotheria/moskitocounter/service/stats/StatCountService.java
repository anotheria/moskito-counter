package net.anotheria.moskitocounter.service.stats;

import net.anotheria.anoprise.metafactory.Service;

/**
 * Counter service entry.
 *
 * @author sshscp
 */
public interface StatCountService extends Service {
	/**
	 * Count incoming request.
	 *
	 * @param requestData {@link RequestData}
	 */
	void count(final RequestData requestData);
}
