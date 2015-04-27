package com.laboki.eclipse.plugin.fastopen.events;

import com.google.common.collect.ImmutableList;
import com.laboki.eclipse.plugin.fastopen.resources.RFile;

public final class FileResourcesEvent {

	private final ImmutableList<RFile> rFiles;

	public FileResourcesEvent(final ImmutableList<RFile> rFiles) {
		this.rFiles = rFiles;
	}

	public ImmutableList<RFile>
	getrFiles() {
		return this.rFiles;
	}
}
