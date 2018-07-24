package org.eclipse.egit.ui.internal.cairh;

import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public class CodeAnalysisJobListener extends JobChangeAdapter {
	private CountDownLatch cdl;

	public CodeAnalysisJobListener(CountDownLatch cdl) {
		this.cdl = cdl;
	}

	@Override
	public void done(IJobChangeEvent event) {
		if ("P3C Code Analysis".equals(event.getJob().getName())) {
			cdl.countDown();
		}
	}

}
