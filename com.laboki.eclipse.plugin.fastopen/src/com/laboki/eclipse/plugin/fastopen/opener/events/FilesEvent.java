package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public class FilesEvent {

	private final ImmutableList<String> files;

	public FilesEvent(final ImmutableList<String> files) {
		this.files = files;
	}

	public ImmutableList<String> getFiles() {
		return this.files;
	}
}
