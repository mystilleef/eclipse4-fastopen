package com.laboki.eclipse.plugin.fastopen.resources;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesResultEvent;
import com.laboki.eclipse.plugin.fastopen.instance.AbstractEventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public final class RecentResourcesFilter extends AbstractEventBusInstance {

	private final List<RFile> rFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesEventHandler(final FileResourcesEvent event) {
		EditorContext.cancelJobsBelongingTo(EditorContext.UPDATE_R_FILES_TASK);
		this.updateRFiles(event);
	}

	private void updateRFiles(final FileResourcesEvent event) {
		new Task(EditorContext.UPDATE_R_FILES_TASK, 60) {

			@Override
			public void execute() {
				this.updateFiles(event.getrFiles());
			}

			private void updateFiles(final ImmutableList<RFile> rFiles) {
				RecentResourcesFilter.this.rFiles.clear();
				RecentResourcesFilter.this.rFiles.addAll(rFiles);
			}
		}.begin();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void filterRecentFilesEventHandler(final FilterRecentFilesEvent event) {
		EditorContext.cancelJobsBelongingTo(EditorContext.FILTER_RECENT_FILES_TASK);
		this.filterRecentFiles(event);
	}

	private void filterRecentFiles(final FilterRecentFilesEvent event) {
		new Task(EditorContext.FILTER_RECENT_FILES_TASK, 60) {

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
	public Instance end() {
		this.rFiles.clear();
		return super.end();
	}
}
