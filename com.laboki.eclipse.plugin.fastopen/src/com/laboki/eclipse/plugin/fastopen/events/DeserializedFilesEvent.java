package com.laboki.eclipse.plugin.fastopen.events;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.ImmutableList;

public final class DeserializedFilesEvent {

	private final ImmutableList<IFile> files;

	public DeserializedFilesEvent(final ImmutableList<IFile> files) {
		this.files = files;
	}

	public ImmutableList<IFile>
	getFiles() {
		return this.files;
	}
}
