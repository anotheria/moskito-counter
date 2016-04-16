package net.anotheria.moskitocounter;

import net.anotheria.moskito.aop.annotation.CountByParameter;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 29.11.13 17:57
 */
public class VersionCounter {
	@CountByParameter public void control(String version){

	}

	@CountByParameter public void inspect(String version){

	}
}
