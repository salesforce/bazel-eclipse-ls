package com.salesforce.b2eclipse.managers;

import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.managers.IBuildSupport;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager.CHANGE_TYPE;

@SuppressWarnings("restriction")
public class BazelBuildSupport implements IBuildSupport{
	
	private static final String BUILD_FILE_PATTERN = "**/BUILD";
	private static final String WORKSPACE_FILE_PATTERN = "**/WORKSPACE";

	@Override
	public boolean applies(IProject project) {
		return true;
	}
	
	@Override
	public void update(IProject project, boolean force, IProgressMonitor monitor) throws CoreException {
		System.out.print("Update");
	}

	@Override
	public boolean isBuildFile(IResource resource) {
		return true;
	}
	
	@Override
	public boolean isBuildLikeFileName(String fileName) {
		return fileName.equals("BUILD");
	}

	@Override
    public List<String> getWatchPatterns() {
        return Arrays.asList(BUILD_FILE_PATTERN, WORKSPACE_FILE_PATTERN);
    }
	
	@Override
	public boolean fileChanged(IResource resource, CHANGE_TYPE changeType, IProgressMonitor monitor) throws CoreException {
		return true;
	}

}
