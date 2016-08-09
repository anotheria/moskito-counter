package net.anotheria.moskitocounter.service.stats;

import net.anotheria.anoprise.metafactory.ServiceFactory;

/**
 * {@link ServiceFactory} for {@link StatCountService}
 *
 * @author sshscp
 */
public class StatCountServiceFactory implements ServiceFactory<StatCountService> {

	@Override
	public StatCountService create() {
		return new StatCountServiceImpl();
	}
}
