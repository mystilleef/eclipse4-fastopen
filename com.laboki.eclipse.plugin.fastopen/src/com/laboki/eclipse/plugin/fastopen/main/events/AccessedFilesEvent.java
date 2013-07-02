package com.laboki.eclipse.plugin.fastopen.main.events;

import com.google.common.collect.ImmutableList;

public final class AccessedFilesEvent extends FilesEvent {

	public AccessedFilesEvent(final ImmutableList<String> files) {
		super(files);
	}
}
