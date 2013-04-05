package com.laboki.eclipse.plugin.fastopen.opener;

import java.util.List;

import lombok.Synchronized;
import lombok.val;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesResultEvent;

public final class RecentResourcesFilter {

	private final List<File> files = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FileResourcesEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				RecentResourcesFilter.this.updateFiles(event.getFiles());
			}
		});
	}

	@Synchronized("files")
	private void updateFiles(final ImmutableList<File> files) {
		this.files.clear();
		this.files.addAll(files);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void fileResourcesChanged(final FilterRecentFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				RecentResourcesFilter.this.filter(event.getString());
			}
		});
	}

	private void filter(final String string) {
		val trimmedString = string.trim();
		if (trimmedString.length() == 0) RecentResourcesFilter.postEvent(this.getFiles());
		else this.filterFiles(trimmedString);
	}

	private void filterFiles(final String string) {
		val regexString = ".*" + string + ".*";
		RecentResourcesFilter.postEvent(this.getFilteredList(regexString));
	}

	private static void postEvent(final List<File> files) {
		EventBus.post(new FilterRecentFilesResultEvent(ImmutableList.copyOf(files)));
	}

	private List<File> getFilteredList(final String string) {
		final List<File> filteredList = Lists.newArrayList();
		for (final File file : this.getFiles())
			if (RecentResourcesFilter.matches(file, string)) filteredList.add(file);
		return filteredList;
	}

	@Synchronized("files")
	private List<File> getFiles() {
		return Lists.newArrayList(this.files);
	}

	private static boolean matches(final File file, final String string) {
		return (file.getName().toLowerCase().matches(string.toLowerCase()));
	}
}
