package com.laboki.eclipse.plugin.fastopen.main.events;

import com.google.common.collect.ImmutableList;

public final class RecentFilesEvent extends FilesEvent {

	public RecentFilesEvent(final ImmutableList<String> files) {
		super(files);
	}
}
