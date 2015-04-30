package com.laboki.eclipse.plugin.fastopen.files;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedAccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public final class AccessedFilesSerializer extends EventBusInstance {

	public static final String SERIALIZABLE_FILE_PATH = EditorContext
		.getSerializableFilePath("accessed.files.ser");

	public AccessedFilesSerializer() {
		EditorContext.emptyFile(AccessedFilesSerializer.SERIALIZABLE_FILE_PATH);
	}

	@Subscribe
	@AllowConcurrentEvents
	public static synchronized void
	serializeAccessedFiles(final AccessedFilesEvent event) {
		new Task() {

			@Override
			public void
			execute() {
				this.serialize(event.getFiles());
			}

			private void
			serialize(final Collection<String> files) {
				if (files.size() == 0) return;
				EditorContext.serialize(
					AccessedFilesSerializer.SERIALIZABLE_FILE_PATH,
					files);
			}
		}.start();
	}

	@Override
	public Instance
	start() {
		AccessedFilesSerializer.postEvent();
		return super.start();
	}

	private static void
	postEvent() {
		EventBus.post(new DeserializedAccessedFilesEvent(ImmutableList
			.copyOf(AccessedFilesSerializer.deserialize())));
	}

	@SuppressWarnings("unchecked")
	private static List<String>
	deserialize() {
		final Object files =
			EditorContext
				.deserialize(AccessedFilesSerializer.SERIALIZABLE_FILE_PATH);
		if (files == null) return Lists.newArrayList();
		return (List<String>) files;
	}
}
