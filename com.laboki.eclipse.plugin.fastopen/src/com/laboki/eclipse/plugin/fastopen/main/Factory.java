package com.laboki.eclipse.plugin.fastopen.main;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Optional;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public enum Factory implements Instance {
	INSTANCE;

	private static final Optional<IPartService> PART_SERVICE = EditorContext
		.getPartService();
	private final PartListener partListener = new PartListener();

	@Override
	public Instance
	start() {
		if (!Factory.PART_SERVICE.isPresent()) return this;
		Factory
			.emitPartActivationEvent(Factory.PART_SERVICE.get().getActivePart());
		Factory.PART_SERVICE.get().addPartListener(this.partListener);
		return this;
	}

	@Override
	public Instance
	stop() {
		if (!Factory.PART_SERVICE.isPresent()) return this;
		Factory.PART_SERVICE.get().removePartListener(this.partListener);
		EditorContext.cancelAllJobs();
		return this;
	}

	public static void
	emitPartActivationEvent(final IWorkbenchPart part) {
		if (EditorContext.isInvalidPart(part)) return;
		EventBus.post(new PartActivationEvent());
	}

	private final class PartListener implements IPartListener {

		public PartListener() {}

		@Override
		public void
		partActivated(final IWorkbenchPart part) {
			new Task() {

				@Override
				public void
				execute() {
					Factory.emitPartActivationEvent(part);
				}
			}.start();
		}

		@Override
		public void
		partClosed(final IWorkbenchPart part) {}

		@Override
		public void
		partBroughtToTop(final IWorkbenchPart part) {}

		@Override
		public void
		partDeactivated(final IWorkbenchPart part) {}

		@Override
		public void
		partOpened(final IWorkbenchPart part) {}
	}
}
