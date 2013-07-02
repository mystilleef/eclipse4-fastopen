package com.laboki.eclipse.plugin.fastopen.main;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.files.AccessedFiles;
import com.laboki.eclipse.plugin.fastopen.files.AccessedFilesSerializer;
import com.laboki.eclipse.plugin.fastopen.files.RecentFiles;
import com.laboki.eclipse.plugin.fastopen.resources.FileResources;
import com.laboki.eclipse.plugin.fastopen.resources.RecentResources;
import com.laboki.eclipse.plugin.fastopen.resources.RecentResourcesFilter;
import com.laboki.eclipse.plugin.fastopen.resources.WorkspaceResources;
import com.laboki.eclipse.plugin.fastopen.ui.Dialog;

public final class Services implements Instance {

	private final List<Instance> instances = Lists.newArrayList();

	public Services() {}

	@Override
	public Instance begin() {
		this.startServices();
		return this;
	}

	private void startServices() {
		this.startService(new Dialog());
		this.startService(new RecentResourcesFilter());
		this.startService(new RecentResources());
		this.startService(new AccessedFiles());
		this.startService(new AccessedFilesSerializer());
		this.startService(new RecentFiles());
		this.startService(new FileResources());
		this.startService(new WorkspaceResources());
		this.startService(Factory.INSTANCE);
	}

	private void startService(final Instance instance) {
		instance.begin();
		this.instances.add(instance);
	}

	@Override
	public Instance end() {
		this.stopServices();
		return this;
	}

	private void stopServices() {
		for (final Instance instance : ImmutableList.copyOf(this.instances))
			this.stopService(instance);
	}

	private void stopService(final Instance instance) {
		instance.end();
		this.instances.remove(instance);
	}
}
