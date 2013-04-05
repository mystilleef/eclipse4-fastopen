package com.laboki.eclipse.plugin.fastopen.opener.files;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.AccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.events.DeserializedAccessedFilesEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class AccessedFilesSerializer {

	public static final String SERIALIZABLE_FILE_PATH = EditorContext.getSerializableFilePath("accessed.files.ser");

	public AccessedFilesSerializer() {
		EditorContext.emptyFile(AccessedFilesSerializer.SERIALIZABLE_FILE_PATH);
		AccessedFilesSerializer.postEvent();
	}

	private static void postEvent() {
		EventBus.post(new DeserializedAccessedFilesEvent(ImmutableList.copyOf(AccessedFilesSerializer.deserialize())));
	}

	@SuppressWarnings("unchecked")
	private static List<String> deserialize() {
		final Object files = EditorContext.deserialize(AccessedFilesSerializer.SERIALIZABLE_FILE_PATH);
		if (files == null) return Lists.newArrayList();
		return (List<String>) files;
	}

	@Subscribe
	@AllowConcurrentEvents
	public static void accessedFilesChanged(final AccessedFilesEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				AccessedFilesSerializer.serialize(event.getFiles());
			}
		});
	}

	private static void serialize(final Collection<String> files) {
		if (files.size() == 0) return;
		EditorContext.serialize(AccessedFilesSerializer.SERIALIZABLE_FILE_PATH, files);
	}
}
