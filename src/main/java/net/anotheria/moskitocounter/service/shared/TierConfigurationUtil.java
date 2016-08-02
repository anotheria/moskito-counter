package net.anotheria.moskitocounter.service.shared;

import net.anotheria.anoprise.metafactory.Extension;
import net.anotheria.anoprise.metafactory.MetaFactory;
import net.anotheria.moskitocounter.service.stats.StatCountService;
import net.anotheria.moskitocounter.service.stats.StatCountServiceFactory;

/**
 * Service tier configuration.
 *
 * @author sshscp
 */
public final class TierConfigurationUtil {
	/**
	 * Pre-configure services.
	 */
	public static void configureServices() {
		MetaFactory.addFactoryClass(StatCountService.class, Extension.LOCAL, StatCountServiceFactory.class);
	}
}
