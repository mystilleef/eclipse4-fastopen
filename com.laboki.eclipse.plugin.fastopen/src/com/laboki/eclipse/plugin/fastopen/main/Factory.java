package com.laboki.eclipse.plugin.fastopen.main;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;

import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;
import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public enum Factory implements Instance {
	INSTANCE;

	private static final IPartService PART_SERVICE = EditorContext
		.getPartService();
	private final PartListener partListener = new PartListener();

	@Override
	public Instance
	begin() {
		Factory.emitPartActivationEvent(Factory.PART_SERVICE.getActivePart());
		Factory.PART_SERVICE.addPartListener(this.partListener);
		return this;
	}

	@Override
	public Instance
	end() {
		Factory.PART_SERVICE.removePartListener(this.partListener);
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
			}.begin();
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
