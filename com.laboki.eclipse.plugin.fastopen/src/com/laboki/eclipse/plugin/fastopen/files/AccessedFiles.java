package com.laboki.eclipse.plugin.fastopen.files;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedAccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.events.RecentFilesModificationEvent;
import com.laboki.eclipse.plugin.fastopen.instance.AbstractEventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class AccessedFiles extends AbstractEventBusInstance {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	private final List<String> accessedFiles = Lists.newArrayList(EditorContext
		.getOpenEditorFilePaths());
	private static final int ACCESSED_FILES_REINDEX_WATERMARK = 3;

	@Subscribe
	@AllowConcurrentEvents
	public void
	updateAccessedFiles(final DeserializedAccessedFilesEvent event) {
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
	@AllowConcurrentEvents
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
		}
			.setRule(AccessedFiles.RULE)
			.setFamily(EditorContext.UPDATE_ACCESSED_FILES_TASK)
			.setDelay(1000)
			.start();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void
	partActivationEventHandler(@SuppressWarnings("unused") final PartActivationEvent event) {
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
				this.update(this.getAccessedFilesInsertionIndex(), path);
				AccessedFiles.this.updateAccessedFiles(ImmutableList
					.copyOf(this.aFiles));
				AccessedFiles.this.postEvent();
			}

			private int
			getAccessedFilesInsertionIndex() {
				return this.aFiles.size() == 0 ? 0 : 1;
			}

			private void
			moveCurrentFileToTopOfList() {
				if (this.aFiles.size() < AccessedFiles.ACCESSED_FILES_REINDEX_WATERMARK) return;
				this.update(0, this.aFiles.get(1));
			}

			private void
			update(final int index, final String path) {
				this.aFiles.remove(path);
				this.aFiles.add(index, path);
			}
		}
			.setRule(AccessedFiles.RULE)
			.setFamily(EditorContext.UPDATE_ACCESSED_FILES_TASK)
			.setDelay(60)
			.start();
	}

	private synchronized void
	updateAccessedFiles(final List<String> files) {
		this.accessedFiles.removeAll(files);
		this.accessedFiles.addAll(0, files);
		this.accessedFiles.remove("");
	}

	private void
	postEvent() {
		EventBus.post(new AccessedFilesEvent(ImmutableList.copyOf(this
			.getAccessedFiles())));
		// this.printAccessedFiles();
	}

	@SuppressWarnings("unused")
	private void
	printAccessedFiles() {
		System.out.println("===");
		for (final String file : this.getAccessedFiles())
			System.out.println(file);
		System.out.println("===");
	}

	private synchronized ArrayList<String>
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
