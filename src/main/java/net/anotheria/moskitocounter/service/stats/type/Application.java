package net.anotheria.moskitocounter.service.stats.type;

import net.anotheria.util.StringUtils;

/**
 * App names enumeration.
 *
 * @author sshscp
 */
public enum Application {
	/**
	 * M. web ui.
	 */
	WEB_UI("webui"),
	/**
	 * M. inspect.
	 */
	INSPECT("inspect"),
	/**
	 * M. control.
	 */
	CONTROL("control"),
	/**
	 * Not defined.
	 */
	UNKNOWN("-");
	/**
	 * App name as string representation.
	 */
	private final String name;

	/**
	 * Constructor.
	 *
	 * @param name
	 * 		app name as string value
	 */
	Application(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Resolve {@link Application} by incoming string name.
	 * {@link Application#UNKNOWN} will be returned in case if no app with such name exists.
	 *
	 * @param name
	 * 		app name
	 * @return {@link Application}
	 */
	public static Application get(final String name) {
		if (StringUtils.isEmpty(name))
			return UNKNOWN;
		for (final Application app : values())
			if (app.getName().equals(name))
				return app;
		return UNKNOWN;
	}
}
