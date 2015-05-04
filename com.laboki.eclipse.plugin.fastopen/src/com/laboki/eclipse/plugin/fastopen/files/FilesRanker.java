package com.laboki.eclipse.plugin.fastopen.files;

import java.util.List;

import org.eclipse.core.resources.IFile;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.RankedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class FilesRanker extends EventBusInstance {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	protected final List<IFile> rankedFiles = Lists.newArrayList();

	@Subscribe
	public void
	eventHandler(final WorkspaceFilesEvent event) {
		new AsyncTask() {

			@Override
			public void
			execute() {
				this.updateFiles(event.getFiles());
				FilesRanker.this.broadcastEvent();
			}

			private void
			updateFiles(final ImmutableList<IFile> files) {
				FilesRanker.this.rankedFiles.clear();
				FilesRanker.this.rankedFiles.addAll(files);
			}
		}.setRule(FilesRanker.RULE).start();
	}

	@Subscribe
	public void
	eventHandler(final AccessedFilesEvent event) {
		new AsyncTask() {

			@Override
			public void
			execute() {
				this.rankFiles(event.getFiles());
				FilesRanker.this.broadcastEvent();
			}

			private void
			rankFiles(final ImmutableList<IFile> files) {
				final ImmutableList<IFile> accessedFiles = this.reorder(files);
				FilesRanker.this.rankedFiles.removeAll(accessedFiles);
				FilesRanker.this.rankedFiles.addAll(0, accessedFiles);
			}

			private ImmutableList<IFile>
			reorder(final ImmutableList<IFile> files) {
				if (files.size() < 2) return files;
				final Optional<IFile> file = this.getCurrentFile();
				if (!file.isPresent()) return files;
				return ImmutableList.copyOf(this.rearrange(files, file.get()));
			}

			private Optional<IFile>
			getCurrentFile() {
				return EditorContext.getFile(EditorContext.getEditor());
			}

			private List<IFile>
			rearrange(final ImmutableList<IFile> accessedFiles,
								final IFile currentFile) {
				final List<IFile> newFiles = Lists.newArrayList();
				newFiles.addAll(accessedFiles);
				final IFile file = accessedFiles.get(1);
				newFiles.remove(file);
				newFiles.remove(currentFile);
				newFiles.add(0, file);
				newFiles.add(1, currentFile);
				return newFiles;
			}
		}.setRule(FilesRanker.RULE).start();
	}

	protected void
	broadcastEvent() {
		EventBus.post(this.createEvent());
	}

	private RankedFilesEvent
	createEvent() {
		final ImmutableList<IFile> files =
			ImmutableList.copyOf(FilesRanker.this.rankedFiles);
		return new RankedFilesEvent(files);
	}
}
