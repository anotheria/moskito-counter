package net.anotheria.moskitocounter.service.stats.counter;

import net.anotheria.moskito.aop.annotation.CountByParameter;

/**
 * This class counts accesses to the pages in the tool.
 *
 * @author lrosenberg
 * @since 27.11.13 09:31
 */
public class InspectPageCounter {
	@CountByParameter
	public void countPage(String pageName){

	}
}
