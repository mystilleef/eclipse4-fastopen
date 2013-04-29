package com.laboki.eclipse.plugin.fastopen;

interface ITask {

	void execute();

	void asyncExec();

	void postExecute();
}
