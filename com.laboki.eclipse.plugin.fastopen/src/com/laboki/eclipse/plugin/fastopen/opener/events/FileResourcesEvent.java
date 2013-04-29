package com.laboki.eclipse.plugin.fastopen.opener.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;
import com.laboki.eclipse.plugin.fastopen.opener.resources.RFile;

public final class FileResourcesEvent {

	@Getter private final ImmutableList<RFile> rFiles;

	public FileResourcesEvent(final ImmutableList<RFile> rFiles) {
		this.rFiles = rFiles;
	}
}
