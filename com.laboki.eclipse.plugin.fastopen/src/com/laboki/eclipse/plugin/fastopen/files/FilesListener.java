package com.laboki.eclipse.plugin.fastopen.files;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.laboki.eclipse.plugin.fastopen.events.IndexFilesEvent;
import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.listeners.OpenerResourceChangeListener;
import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;

public final class FilesListener extends EventBusInstance
	implements
		IResourceDeltaVisitor {

	private static final String FAMILY = "FileListener task family";
	private static final int ONE_SECOND = 1000;
	private static final TaskMutexRule RULE = new TaskMutexRule();
	private final OpenerResourceChangeListener listener =
		new OpenerResourceChangeListener(this);

	@Override
	public boolean
	visit(final IResourceDelta delta) throws CoreException {
		if (FilesListener.isDelta(delta.getKind())) FilesListener.indexResources();
		return true;
	}

	private static boolean
	isDelta(final int kind) {
		return (kind == IResourceDelta.ADDED) || (kind == IResourceDelta.REMOVED);
	}

	private static void
	indexResources() {
		EditorContext.cancelJobsBelongingTo(FilesListener.FAMILY);
		FilesListener.startIndexService();
	}

	private static void
	startIndexService() {
		new Task() {

			@Override
			public void
			execute() {
				EventBus.post(new IndexFilesEvent());
			}
		}.setFamily(FilesListener.FAMILY)
			.setRule(FilesListener.RULE)
			.setDelay(FilesListener.ONE_SECOND)
			.start();
	}

	@Override
	public Instance
	start() {
		this.listener.start();
		return super.start();
	}

	@Override
	public Instance
	stop() {
		this.listener.stop();
		return super.stop();
	}
}
