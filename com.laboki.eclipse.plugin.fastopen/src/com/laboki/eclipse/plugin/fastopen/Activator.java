package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public final class Activator extends AbstractUIPlugin {

	private static Activator instance;

	public Activator() {
		Activator.instance = this;
	}

	@Override
	public void
	start(final BundleContext context) throws Exception {
		super.start(context);
		Plugin.INSTANCE.start();
	}

	@Override
	public void
	stop(final BundleContext context) throws Exception {
		Plugin.INSTANCE.stop();
		Activator.instance = null;
		super.stop(context);
	}

	public static Activator
	getInstance() {
		return Activator.instance;
	}

	public static Activator
	getDefault() {
		return Activator.instance;
	}
}
