package com.laboki.eclipse.plugin.fastopen.resources;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.FilterRecentFilesResultEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class RecentResourcesFilter extends EventBusInstance {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	private final List<RFile> rFiles = Lists.newArrayList();
	private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE
		| Pattern.CANON_EQ
		| Pattern.UNICODE_CASE;

	@Subscribe
	public void
	eventHandler(final FileResourcesEvent event) {
		EditorContext.cancelJobsBelongingTo(EditorContext.UPDATE_R_FILES_TASK);
		this.updateRFiles(event.getrFiles());
	}

	private void
	updateRFiles(final ImmutableList<RFile> rFiles) {
		new Task() {

			@Override
			public void
			execute() {
				this.updateFiles();
			}

			private void
			updateFiles() {
				RecentResourcesFilter.this.rFiles.clear();
				RecentResourcesFilter.this.rFiles.addAll(rFiles);
			}
		}.setRule(RecentResourcesFilter.RULE)
			.setFamily(EditorContext.UPDATE_R_FILES_TASK)
			.start();
	}

	@Subscribe
	public void
	eventHandler(final FilterRecentFilesEvent event) {
		EditorContext
			.cancelJobsBelongingTo(EditorContext.FILTER_RECENT_FILES_TASK);
		this.filterRecentFiles(event.getString());
	}

	private void
	filterRecentFiles(final String string) {
		new Task() {

			private final List<RFile> recentFiles = RecentResourcesFilter.this
				.getFiles();

			@Override
			public void
			execute() {
				this.filter(string.trim());
			}

			private void
			filter(final String string) {
				if (string.isEmpty()) this.postEvent(this.recentFiles);
				else this.filterFiles(string);
			}

			private void
			filterFiles(final String string) {
				final String query = ".*" + Pattern.quote(string) + ".*";
				this.postEvent(this.getFilteredList(query));
			}

			private void
			postEvent(final List<RFile> rFiles) {
				EventBus.post(new FilterRecentFilesResultEvent(ImmutableList
					.copyOf(rFiles)));
			}

			private List<RFile>
			getFilteredList(final String query) {
				final List<RFile> filteredList = Lists.newArrayList();
				for (final RFile rFile : this.recentFiles)
					if (this.matchFound(rFile, query)) filteredList.add(rFile);
				return filteredList;
			}

			private boolean
			matchFound(final RFile rFile, final String query) {
				if (Pattern
					.compile(query, RecentResourcesFilter.PATTERN_FLAGS)
					.matcher(rFile.getName())
					.find()) return true;
				return false;
			}
		}.setFamily(EditorContext.FILTER_RECENT_FILES_TASK)
			.setRule(RecentResourcesFilter.RULE)
			.start();
	}

	private List<RFile>
	getFiles() {
		return Lists.newArrayList(this.rFiles);
	}

	@Override
	public Instance
	stop() {
		this.rFiles.clear();
		return super.stop();
	}
}
