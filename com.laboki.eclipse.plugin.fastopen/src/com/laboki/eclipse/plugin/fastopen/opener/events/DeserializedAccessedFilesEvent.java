package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public final class DeserializedAccessedFilesEvent {

	private final ImmutableList<String> files;

	public DeserializedAccessedFilesEvent(final ImmutableList<String> files) {
		this.files = files;
	}

	public ImmutableList<String> getFiles() {
		return this.files;
	}
}
