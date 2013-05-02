package com.laboki.eclipse.plugin.fastopen.opener.events;

import com.google.common.collect.ImmutableList;

public final class ModifiedFilesEvent extends FilesEvent {

	public ModifiedFilesEvent(final ImmutableList<Object> files) {
		super(files);
	}
}
