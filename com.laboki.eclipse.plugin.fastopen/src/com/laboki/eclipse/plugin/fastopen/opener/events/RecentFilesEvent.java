package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public final class RecentFilesEvent extends FilesEvent {

	public RecentFilesEvent(final ImmutableList<Object> immutableList) {
		super(immutableList);
	}
}
