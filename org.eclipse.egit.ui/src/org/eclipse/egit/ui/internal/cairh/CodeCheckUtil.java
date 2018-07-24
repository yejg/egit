package org.eclipse.egit.ui.internal.cairh;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.ui.internal.UIText;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.alibaba.smartfox.eclipse.job.CodeAnalysis;
import com.alibaba.smartfox.eclipse.ui.InspectionResultView;
import com.alibaba.smartfox.eclipse.ui.InspectionResults;
import com.alibaba.smartfox.eclipse.ui.LevelViolations;

/**
 * check code before commit to git server.
 * 
 * @author yejg
 */
public class CodeCheckUtil {
	public static final String P3C_VIEWID = "com.alibaba.smartfox.eclipse.ui.InspectionResultView";

	public static final String BLOCKER_LEVEL = "Blocker";

	public static final String CRITICAL_LEVEL = "Critical";

	public static boolean checkCode(Set<IFile> fileSet, Shell shell) {
		CountDownLatch cdl = new CountDownLatch(1);
		CodeAnalysisJobListener listener = new CodeAnalysisJobListener(cdl);
		Job.getJobManager().addJobChangeListener(listener);
		CodeAnalysis.INSTANCE.processResources(fileSet);
		try {
			cdl.await(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			EGitUILogger.logError("InterruptedException", e);
			MessageDialog.openError(shell, "Error", e.getMessage());
			return false;
		} finally {
			Job.getJobManager().removeJobChangeListener(listener);
		}

		IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(P3C_VIEWID);
		if (part == null) {
			MessageDialog.openError(shell, UIText.StagingView_notFoundAliPlugin, UIText.StagingView_installAliFirst);
			return false;
		}

		InspectionResultView view = (InspectionResultView) part;
		// get all elements in InspectionResultView
		InspectionResults result = (InspectionResults) view.treeViewer.getInput();
		List<LevelViolations> errors = result.getErrors();
		boolean hasBlockerOrCritical = false;
		if (errors != null && errors.size() > 0) {
			// about [level]: see com.alibaba.smartfox.eclipse.pmd.RulePriority
			// [Blocker, Critical, Major]
			for (LevelViolations levelViolations : errors) {
				String level = levelViolations.getLevel();
				if (BLOCKER_LEVEL.equals(level) || CRITICAL_LEVEL.equals(level)) {
					hasBlockerOrCritical = true;
					break;
				}
			}
			if (hasBlockerOrCritical) {
				MessageDialog.openError(shell, UIText.StagingView_codeCheckNotPass, UIText.StagingView_codeCheckNotPassDesc);
				return false;
			} else {
				MessageDialog dialog = new MessageDialog(shell, UIText.StagingView_codeCheckNotPass, null, UIText.StagingView_fixOrForceCommitDesc, MessageDialog.WARNING, 0, UIText.StagingView_fix,
						UIText.StagingView_forceCommit);
				// close dialog return -1; click [fix] return 0; click
				// [forceCommit] return 1;
				int code = dialog.open();
				if (code == 0) {
					return false;
				}
			}
		}
		return true;
	}
}
