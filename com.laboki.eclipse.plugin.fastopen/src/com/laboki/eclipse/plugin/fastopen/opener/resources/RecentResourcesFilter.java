package com.laboki.eclipse.plugin.fastopen.opener.resources;

import java.util.List;

import lombok.Synchronized;
import lombok.val;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesResultEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class RecentResourcesFilter implements Instance {

	private final List<RFile> rFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FileResourcesEvent event) {
		EditorContext.asyncExec(new Task() {

			@Override
			public void execute() {
				RecentResourcesFilter.this.updateFiles(event.getRFiles());
			}
		});
	}

	@Synchronized("rFiles")
	private void updateFiles(final ImmutableList<RFile> rFiles) {
		this.rFiles.clear();
		this.rFiles.addAll(rFiles);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FilterRecentFilesEvent event) {
		EditorContext.asyncExec(new Task() {

			@Override
			public void execute() {
				this.filter(event.getString());
			}

			private void filter(final String string) {
				val trimmedString = string.trim();
				if (trimmedString.length() == 0) this.postEvent(RecentResourcesFilter.this.getFiles());
				else this.filterFiles(trimmedString);
			}

			private void filterFiles(final String string) {
				val regexString = ".*" + string + ".*";
				this.postEvent(this.getFilteredList(regexString));
			}

			private void postEvent(final List<RFile> rFiles) {
				EventBus.post(new FilterRecentFilesResultEvent(ImmutableList.copyOf(rFiles)));
			}

			private List<RFile> getFilteredList(final String string) {
				final List<RFile> filteredList = Lists.newArrayList();
				for (final RFile rFile : RecentResourcesFilter.this.getFiles())
					if (this.matches(rFile, string)) filteredList.add(rFile);
				return filteredList;
			}

			private boolean matches(final RFile rFile, final String string) {
				return (rFile.getName().toLowerCase().matches(string.toLowerCase()));
			}
		});
	}

	@Synchronized("rFiles")
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
