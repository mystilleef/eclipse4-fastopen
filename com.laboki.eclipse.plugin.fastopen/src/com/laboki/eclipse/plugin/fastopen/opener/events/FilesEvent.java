package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public class FilesEvent {

	private final ImmutableList<Object> files;

	public FilesEvent(final ImmutableList<Object> immutableList) {
		this.files = immutableList;
	}

	public ImmutableList<Object> getFiles() {
		return this.files;
	}
}
