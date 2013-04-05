package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

import com.google.common.collect.ImmutableList;
import com.laboki.eclipse.plugin.fastopen.opener.File;

public final class FileResourcesEvent {

	@Getter private final ImmutableList<File> files;

	public FileResourcesEvent(final ImmutableList<File> files) {
		this.files = files;
	}
}
