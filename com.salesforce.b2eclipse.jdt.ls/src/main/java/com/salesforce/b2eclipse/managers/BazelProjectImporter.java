/**
 * Copyright (c) 2020, Salesforce.com, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package com.salesforce.b2eclipse.managers;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.ls.core.internal.AbstractProjectImporter;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.salesforce.b2eclipse.abstractions.WorkProgressMonitor;
import com.salesforce.b2eclipse.config.BazelEclipseProjectFactory;
import com.salesforce.b2eclipse.importer.BazelProjectImportScanner;
import com.salesforce.b2eclipse.model.BazelPackageInfo;
import com.salesforce.b2eclipse.runtime.impl.EclipseWorkProgressMonitor;

@SuppressWarnings("restriction")
public final class BazelProjectImporter extends AbstractProjectImporter {

    private static final String WORKSPACE_FILE_NAME = "WORKSPACE";

    @Override
    public boolean applies(IProgressMonitor monitor) throws OperationCanceledException, CoreException {
        B2EPreferncesManager preferencesManager = B2EPreferncesManager.getInstance();

        setPluginConfig(preferencesManager);
        
        if (preferencesManager != null && !preferencesManager.isImportBazelEnabled()) {
            return false;
        }
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            return false;
        }
        File workspaceFile = new File(rootFolder, WORKSPACE_FILE_NAME);
        if (workspaceFile.exists()) {
            directories = Arrays.asList(Path.of(rootFolder.getPath(), new String[0]));
        } else {
            return false;
        }

        return true;

    }

    @Override
    public void importToWorkspace(IProgressMonitor monitor) throws OperationCanceledException, CoreException {
        BazelProjectImportScanner scanner = new BazelProjectImportScanner();

        BazelPackageInfo workspaceRootPackage = scanner.getProjects(rootFolder);

        if (workspaceRootPackage == null) {
            throw new IllegalArgumentException();
        }
        List<BazelPackageInfo> bazelPackagesToImport =
                workspaceRootPackage.getChildPackageInfos().stream().collect(Collectors.toList());

        WorkProgressMonitor progressMonitor = new EclipseWorkProgressMonitor(null);

        BazelEclipseProjectFactory.importWorkspace(workspaceRootPackage, bazelPackagesToImport, progressMonitor,
            monitor);
    }

    @Override
    public void reset() {

    }
    
    private void setPluginConfig(B2EPreferncesManager preferencesManager) {
    	Map<String, Object> configuration = JavaLanguageServerPlugin.getPreferencesManager().getPreferences().asMap();
        if (configuration == null) {
            configuration = new HashMap<>();
        }
        preferencesManager.setConfiguration(configuration);
    }

}
