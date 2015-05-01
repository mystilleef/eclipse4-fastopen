package com.laboki.eclipse.plugin.fastopen.files;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesModificationEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class RecentFiles extends EventBusInstance {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	private static final String FAMILY = "RecentFiles task family";
	protected final List<String> recentFiles = Lists.newArrayList();

	@Subscribe
	@AllowConcurrentEvents
	public void
	eventHandler(final ModifiedFilesEvent event) {
		EditorContext.cancelJobsBelongingTo(RecentFiles.FAMILY);
		this.emitUpdatedRecentFiles(event.getFiles());
	}

	private void
	emitUpdatedRecentFiles(final ImmutableList<String> files) {
		new Task() {

			@Override
			public void
			execute() {
				EditorContext
					.cancelJobsBelongingTo(EditorContext.EMIT_UPDATED_RECENT_FILES_TASK);
				this.resetRecentFiles();
				EventBus.post(new RecentFilesModificationEvent(RecentFiles.this
					.getRecentFiles()));
			}

			private void
			resetRecentFiles() {
				RecentFiles.this.recentFiles.clear();
				RecentFiles.this.recentFiles.addAll(files);
				RecentFiles.this.recentFiles.remove("");
			}
		}.setRule(RecentFiles.RULE).setFamily(RecentFiles.FAMILY).start();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void
	eventHandler(final AccessedFilesEvent event) {
		EditorContext
			.cancelJobsBelongingTo(EditorContext.EMIT_UPDATED_RECENT_FILES_TASK);
		this.emitRecentFilesEvent(event.getFiles());
	}

	private void
	emitRecentFilesEvent(final ImmutableList<String> files) {
		new Task() {

			@Override
			public void
			execute() {
				this.mergeAccessedAndRecentFiles();
				EventBus.post(new RecentFilesEvent(RecentFiles.this.getRecentFiles()));
			}

			private void
			mergeAccessedAndRecentFiles() {
				RecentFiles.this.recentFiles.removeAll(files);
				RecentFiles.this.recentFiles.addAll(0, files);
				RecentFiles.this.recentFiles.remove("");
			}
		}.setFamily(RecentFiles.FAMILY).setRule(RecentFiles.RULE).start();
	}

	protected ImmutableList<String>
	getRecentFiles() {
		return ImmutableList.copyOf(Sets.newLinkedHashSet(this.recentFiles));
	}

	@Override
	public Instance
	stop() {
		this.recentFiles.clear();
		return super.stop();
	}
}
