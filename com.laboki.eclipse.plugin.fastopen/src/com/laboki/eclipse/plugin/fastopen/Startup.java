package com.laboki.eclipse.plugin.fastopen;

import lombok.ToString;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.google.inject.Guice;
import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

@ToString
public final class Startup extends AbstractHandler implements Runnable {

	public Startup() {
		Guice.createInjector(new InitModule());
	}

	@Override
	public void run() {
		EventBus.post(new ShowFastOpenDialogEvent());
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		EditorContext.asyncExec(this);
		return null;
	}
}
