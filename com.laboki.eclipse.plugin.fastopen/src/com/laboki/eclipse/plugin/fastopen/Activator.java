package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class Activator extends AbstractUIPlugin {

	private static Activator instance;

	public Activator() {
		Activator.instance = this;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		Activator.instance = null;
	}

	public static ImageDescriptor getImageDescriptor(final String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(EditorContext.PLUGIN_NAME, path);
	}

	public static Activator getInstance() {
		return Activator.instance;
	}
}
