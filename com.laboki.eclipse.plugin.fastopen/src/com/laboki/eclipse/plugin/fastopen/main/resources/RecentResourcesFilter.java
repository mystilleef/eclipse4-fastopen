package com.laboki.eclipse.plugin.fastopen.main.resources;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.main.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.FilterRecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.main.events.FilterRecentFilesResultEvent;

public final class RecentResourcesFilter implements Instance {

	private final List<RFile> rFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void updateRFiles(final FileResourcesEvent event) {
		new Task() {

			@Override
			public void execute() {
				RecentResourcesFilter.this.updateFiles(event.getrFiles());
			}
		}.begin();
	}

	private void updateFiles(final ImmutableList<RFile> rFiles) {
		this.rFiles.clear();
		this.rFiles.addAll(rFiles);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void filterRecentFiles(final FilterRecentFilesEvent event) {
		new Task() {

			private final List<RFile> recentFiles = RecentResourcesFilter.this.getFiles();

			@Override
			public void execute() {
				this.filter(event.getString());
			}

			private void filter(final String string) {
				final String trimmedString = string.trim();
				if (trimmedString.length() == 0) this.postEvent(this.recentFiles);
				else this.filterFiles(trimmedString);
			}

			private void filterFiles(final String string) {
				final String regexString = ".*" + string + ".*";
				this.postEvent(this.getFilteredList(regexString));
			}

			private void postEvent(final List<RFile> rFiles) {
				EventBus.post(new FilterRecentFilesResultEvent(ImmutableList.copyOf(rFiles)));
			}

			private List<RFile> getFilteredList(final String string) {
				final List<RFile> filteredList = Lists.newArrayList();
				for (final RFile rFile : this.recentFiles)
					if (this.matches(rFile, string)) filteredList.add(rFile);
				return filteredList;
			}

			private boolean matches(final RFile rFile, final String string) {
				return (rFile.getName().toLowerCase().matches(string.toLowerCase()));
			}
		}.begin();
	}

	private List<RFile> getFiles() {
		return Lists.newArrayList(this.rFiles);
	}

	@Override
	public Instance begin() {
		EventBus.register(this);
		return this;
	}

	@Override
	public Instance end() {
		EventBus.unregister(this);
		this.rFiles.clear();
		return this;
	}
}
