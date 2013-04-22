package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class Handler extends AbstractHandler implements Runnable {

	public Handler() {}

	@Override
	public Object execute(final ExecutionEvent arg0) throws ExecutionException {
		EditorContext.asyncExec(this);
		return null;
	}

	@Override
	public void run() {
		EventBus.post(new ShowFastOpenDialogEvent());
	}
}
