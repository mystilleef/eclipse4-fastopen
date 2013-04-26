package com.laboki.eclipse.plugin.fastopen;

import lombok.ToString;

import org.eclipse.ui.IStartup;

import com.google.inject.Guice;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

@ToString
public final class Startup implements IStartup, Runnable {

	public Startup() {}

	@Override
	public void earlyStartup() {
		EditorContext.asyncExec(this);
	}

	@Override
	public void run() {
		Guice.createInjector(new InitModule());
		// EditorContext.asyncExec(Opener.INSTANCE);
	}
}
