package com.laboki.eclipse.plugin.fastopen.files;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.WorkspaceFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class Serializer extends EventBusInstance {

	private static final TaskMutexRule RULE = new TaskMutexRule();
	protected static final String PATH = EditorContext
		.getSerializableFilePath("accessed.files.ser");

	public Serializer() {
		EditorContext.emptyFile(Serializer.PATH);
	}

	@Subscribe
	public static void
	eventHandler(final WorkspaceFilesEvent event) {
		new Task() {

			@Override
			public void
			execute() {
				this.broadcastEvent();
			}

			private void
			broadcastEvent() {
				EventBus.post(this.createEvent());
			}

			private DeserializedFilesEvent
			createEvent() {
				return new DeserializedFilesEvent(this.getFiles());
			}

			private ImmutableList<IFile>
			getFiles() {
				final List<IFile> accessedFiles =
					this.getFilesFromPaths(Serializer.deserialize(), event.getFiles());
				return ImmutableSet.copyOf(accessedFiles).asList();
			}

			private List<IFile>
			getFilesFromPaths(final List<String> paths,
												final ImmutableList<IFile> files) {
				final List<IFile> accessedFiles = Lists.newArrayList();
				for (final String path : paths)
					this.updateAccessedFiles(files, accessedFiles, path);
				return accessedFiles;
			}

			private void
			updateAccessedFiles(final ImmutableList<IFile> files,
													final List<IFile> accessedFiles,
													final String path) {
				final IFile file = this.getFile(path, files);
				if (file != null) accessedFiles.add(file);
			}

			private IFile
			getFile(final String path, final ImmutableList<IFile> files) {
				for (final IFile file : files)
					if (EditorContext.getURIPath(file).get().equals(path)) return file;
				return null;
			}
		}.setRule(Serializer.RULE).start();
	}

	@Subscribe
	public static void
	eventHandler(final AccessedFilesEvent event) {
		new Task() {

			@Override
			public void
			execute() {
				Serializer.serialize(Serializer.getPaths(event.getFiles()));
			}
		}.setRule(Serializer.RULE).start();
	}

	protected static void
	serialize(final Collection<String> files) {
		if (files.isEmpty()) return;
		EditorContext.serialize(Serializer.PATH, files);
	}

	@SuppressWarnings("unchecked")
	protected static List<String>
	deserialize() {
		final List<String> files =
			(List<String>) EditorContext.deserialize(Serializer.PATH);
		if ((files == null) || files.isEmpty()) return Lists.newArrayList();
		return files;
	}

	protected static List<String>
	getPaths(final ImmutableList<IFile> files) {
		final List<String> paths = Lists.newArrayList();
		for (final IFile file : files)
			paths.add(EditorContext.getURIPath(file).get());
		return ImmutableSet.copyOf(paths).asList();
	}
}
