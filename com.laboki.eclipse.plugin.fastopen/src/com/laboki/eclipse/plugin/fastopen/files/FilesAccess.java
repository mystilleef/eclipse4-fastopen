package com.laboki.eclipse.plugin.fastopen.files;

import java.util.List;

import org.eclipse.core.resources.IFile;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class FilesAccess extends EventBusInstance {

	protected boolean canBroadcast = false;
	private static final TaskMutexRule RULE = new TaskMutexRule();
	protected final List<IFile> accessedFiles = Lists.newArrayList();

	@Subscribe
	public void
	eventHandler(final DeserializedFilesEvent event) {
		new Task() {

			@Override
			public void
			execute() {
				this.updateFiles();
				this.updateBroadcastFlag();
				FilesAccess.this.broadcastEvent();
			}

			private void
			updateFiles() {
				FilesAccess.this.accessedFiles.clear();
				FilesAccess.this.accessedFiles.addAll(event.getFiles());
			}

			private void
			updateBroadcastFlag() {
				FilesAccess.this.canBroadcast = true;
			}
		}.setRule(FilesAccess.RULE).start();
	}

	@Subscribe
	public void
	eventHandler(final PartActivationEvent event) {
		new AsyncTask() {

			@Override
			public void
			execute() {
				this.updateFiles();
				FilesAccess.this.broadcastEvent();
			}

			private void
			updateFiles() {
				if (FilesAccess.this.canBroadcast == false) return;
				final Optional<IFile> file = this.getFile();
				if (!file.isPresent()) return;
				FilesAccess.this.accessedFiles.remove(file.get());
				FilesAccess.this.accessedFiles.add(0, file.get());
			}

			private Optional<IFile>
			getFile() {
				return EditorContext.getFile(EditorContext.getEditor());
			}
		}.setRule(FilesAccess.RULE).start();
	}

	protected void
	broadcastEvent() {
		if (this.canBroadcast) EventBus.post(this.createEvent());
	}

	private AccessedFilesEvent
	createEvent() {
		final ImmutableList<IFile> files =
			ImmutableList.copyOf(this.accessedFiles);
		return new AccessedFilesEvent(files);
	}
}
