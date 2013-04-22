package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.opener.Factory;

public enum Opener implements Runnable {
	INSTANCE;

	@Override
	public void run() {
		Factory.INSTANCE.begin();
	}
}
