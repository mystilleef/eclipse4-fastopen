package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public final class AccessedFilesEvent extends FilesEvent {

	public AccessedFilesEvent(final ImmutableList<Object> files) {
		super(files);
	}
}
