package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Synchronized;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ModifiedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class AccessedFiles {

	public static final String SERIALIZABLE_FILE_STRING = "accessed_files_list.ser";
	private static final int ACCESSED_FILES_REINDEX_WATERMARK = 3;
	@Getter private final Serializer serializer = new Serializer();
	private final List<String> accessedFiles = this.serializer.deserialize();

	public AccessedFiles() {}

	@Synchronized("accessedFiles")
	private ArrayList<String> getAccessedFiles() {
		return Lists.newArrayList(this.accessedFiles);
	}

	@Synchronized("accessedFiles")
	private void updateAccessedFiles(final List<String> files) {
		this.accessedFiles.clear();
		this.accessedFiles.addAll(files);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void partActivationChanged(@SuppressWarnings("unused") final PartActivationEvent event) {
		EditorContext.asyncExec(new Task("") {

			private final List<String> aFiles = AccessedFiles.this.getAccessedFiles();

			@Override
			public void execute() {
				this.moveCurrentFileToTopOfList();
				this.update(this.getAccessedFilesInsertionIndex(), EditorContext.getPath());
				AccessedFiles.this.updateAccessedFiles(this.aFiles);
				AccessedFiles.this.serialize();
				EventBus.post(AccessedFiles.this.emitFileEvent());
			}

			protected void moveCurrentFileToTopOfList() {
				if (this.aFiles.size() < AccessedFiles.ACCESSED_FILES_REINDEX_WATERMARK) return;
				this.update(0, this.aFiles.get(1));
			}

			protected void update(final int index, final String path) {
				this.aFiles.remove(path);
				this.aFiles.add(index, path);
			}

			protected int getAccessedFilesInsertionIndex() {
				return this.aFiles.size() == 0 ? 0 : 1;
			}
		});
	}

	@Subscribe
	@AllowConcurrentEvents
	public void modifiedFilesChanged(@SuppressWarnings("unused") final ModifiedFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			private final List<String> aFiles = AccessedFiles.this.getAccessedFiles();

			@Override
			public void execute() {
				this.removeFakePaths();
				AccessedFiles.this.updateAccessedFiles(this.aFiles);
				AccessedFiles.this.serialize();
				EventBus.post(AccessedFiles.this.emitFileEvent());
			}

			private void removeFakePaths() {
				final List<String> nonExistentFilePaths = EditorContext.nonExistentFilePaths(this.aFiles);
				this.aFiles.removeAll(nonExistentFilePaths);
			}
		});
	}

	private void serialize() {
		EditorContext.asyncExec(AccessedFiles.this.getSerializer());
	}

	private AccessedFilesEvent emitFileEvent() {
		return new AccessedFilesEvent(ImmutableList.copyOf(this.accessedFiles));
	}

	private final class Serializer implements Runnable {

		private final Scheduler scheduler = new Scheduler("SerializerJobScheduler");

		public Serializer() {
			EditorContext.emptyFile(AccessedFiles.SERIALIZABLE_FILE_STRING);
		}

		@Override
		public void run() {
			this.serialize();
		}

		private void serialize() {
			EditorContext.asyncExec(this.scheduler);
		}

		@SuppressWarnings("unchecked")
		public List<String> deserialize() {
			final Object files = EditorContext.deserialize(AccessedFiles.SERIALIZABLE_FILE_STRING);
			if (files == null) return new ArrayList<>();
			return (List<String>) files;
		}

		private final class Scheduler extends Job implements Runnable {

			public Scheduler(final String name) {
				super(name);
				this.setPriority(Job.DECORATE);
			}

			@Override
			public void run() {
				this.schedule();
			}

			@Override
			protected IStatus run(final IProgressMonitor arg0) {
				EditorContext.serialize(AccessedFiles.SERIALIZABLE_FILE_STRING, AccessedFiles.this.getAccessedFiles());
				return Status.OK_STATUS;
			}
		}
	}
}
