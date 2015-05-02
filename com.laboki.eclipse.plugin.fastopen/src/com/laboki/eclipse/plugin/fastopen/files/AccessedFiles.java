package com.laboki.eclipse.plugin.fastopen.files;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedAccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesModificationEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class AccessedFiles extends EventBusInstance {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	private final List<String> accessedFiles = Lists.newArrayList(EditorContext
		.getOpenEditorFilePaths());
	private static final int WATERMARK = 3;

	@Subscribe
	public void
	eventHandler(final DeserializedAccessedFilesEvent event) {
		new AsyncTask() {

			@Override
			public void
			execute() {
				AccessedFiles.this.updateAccessedFiles(event.getFiles());
				AccessedFiles.this.updateAccessedFiles(AccessedFiles.this
					.getAccessedFiles());
				this.arrangeFiles();
			}

			private void
			arrangeFiles() {
				final List<String> files = AccessedFiles.this.getAccessedFiles();
				if (files.size() < 2) return;
				this.insertCurrentPath(files);
				AccessedFiles.this.updateAccessedFiles(Lists.newArrayList(files));
			}

			private void
			insertCurrentPath(final List<String> files) {
				final String path = EditorContext.getPath();
				files.remove(path);
				files.add(1, path);
			}
		}.setRule(AccessedFiles.RULE).start();
	}

	@Subscribe
	public void
	recentFilesModificationEventHandler(final RecentFilesModificationEvent event) {
		EditorContext
			.cancelJobsBelongingTo(EditorContext.UPDATE_ACCESSED_FILES_TASK);
		this.updateAccessedFilesList(event);
	}

	private void
	updateAccessedFilesList(final RecentFilesModificationEvent event) {
		new Task() {

			private final ImmutableList<String> modifiedFiles = event.getFiles();

			@Override
			public void
			execute() {
				AccessedFiles.this.updateAccessedFiles(this
					.removeDeletedFilesFromAccessList());
				AccessedFiles.this.postEvent();
			}

			private ImmutableList<String>
			removeDeletedFilesFromAccessList() {
				final List<String> files = Lists.newArrayList();
				for (final String file : AccessedFiles.this.getAccessedFiles())
					if (this.modifiedFiles.contains(file)) files.add(file);
				return ImmutableList.copyOf(files);
			}
		}.setRule(AccessedFiles.RULE)
			.setFamily(EditorContext.UPDATE_ACCESSED_FILES_TASK)
			.setDelay(250)
			.start();
	}

	@Subscribe
	public void
	eventHandler(final PartActivationEvent event) {
		EditorContext
			.cancelJobsBelongingTo(EditorContext.UPDATE_ACCESSED_FILES_TASK);
		this.updateAccessedFilesList();
	}

	private void
	updateAccessedFilesList() {
		new AsyncTask() {

			private final List<String> aFiles = AccessedFiles.this
				.getAccessedFiles();

			@Override
			public void
			execute() {
				final String path = EditorContext.getPath();
				if (path.length() == 0) return;
				this.moveCurrentFileToTopOfList();
				this.update(this.getInsertionIndex(), path);
				AccessedFiles.this.updateAccessedFiles(ImmutableList
					.copyOf(this.aFiles));
				AccessedFiles.this.postEvent();
			}

			private void
			moveCurrentFileToTopOfList() {
				if (this.aFiles.size() < AccessedFiles.WATERMARK) return;
				this.update(0, this.aFiles.get(1));
			}

			private void
			update(final int index, final String path) {
				this.aFiles.remove(path);
				this.aFiles.add(index, path);
			}

			private int
			getInsertionIndex() {
				return this.aFiles.size() == 0 ? 0 : 1;
			}
		}.setRule(AccessedFiles.RULE)
			.setFamily(EditorContext.UPDATE_ACCESSED_FILES_TASK)
			.start();
	}

	protected void
	updateAccessedFiles(final List<String> files) {
		this.accessedFiles.removeAll(files);
		this.accessedFiles.addAll(0, files);
		this.accessedFiles.remove("");
	}

	protected void
	postEvent() {
		EventBus.post(this.newAccessedFilesEvent());
	}

	private AccessedFilesEvent
	newAccessedFilesEvent() {
		return new AccessedFilesEvent(
			ImmutableList.copyOf(this.getAccessedFiles()));
	}

	protected ArrayList<String>
	getAccessedFiles() {
		return Lists.newArrayList(this.accessedFiles);
	}

	@Override
	public Instance
	stop() {
		this.accessedFiles.clear();
		return super.stop();
	}
}
