package com.laboki.eclipse.plugin.fastopen.main;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.laboki.eclipse.plugin.fastopen.files.FilesAccess;
import com.laboki.eclipse.plugin.fastopen.files.FilesFilter;
import com.laboki.eclipse.plugin.fastopen.files.FilesIndexer;
import com.laboki.eclipse.plugin.fastopen.files.FilesListener;
import com.laboki.eclipse.plugin.fastopen.files.FilesRanker;
import com.laboki.eclipse.plugin.fastopen.files.Serializer;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.ui.Dialog;

public final class Services implements Instance {

	private final List<Instance> instances = Lists.newArrayList();

	public Services() {}

	@Override
	public Instance
	start() {
		this.startServices();
		return this;
	}

	private void
	startServices() {
		this.startService(new Dialog());
		this.startService(new FilesFilter());
		// this.startService(new AccessedFiles());
		// this.startService(new AccessedFilesSerializer());
		// this.startService(new RecentFiles());
		this.startService(new FilesListener());
		this.startService(new FilesRanker());
		this.startService(new FilesAccess());
		this.startService(new Serializer());
		this.startService(new FilesIndexer());
		this.startService(Factory.INSTANCE);
	}

	private void
	startService(final Instance instance) {
		instance.start();
		this.instances.add(instance);
	}

	@Override
	public Instance
	stop() {
		this.stopServices();
		return this;
	}

	private void
	stopServices() {
		for (final Instance instance : ImmutableList.copyOf(this.instances))
			this.stopService(instance);
	}

	private void
	stopService(final Instance instance) {
		instance.stop();
		this.instances.remove(instance);
	}
}
