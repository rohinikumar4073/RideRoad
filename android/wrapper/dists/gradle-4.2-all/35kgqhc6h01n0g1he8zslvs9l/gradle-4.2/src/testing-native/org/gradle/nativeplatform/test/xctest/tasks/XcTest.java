/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativeplatform.test.xctest.tasks;

import org.gradle.api.Incubating;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryVar;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.testing.Test;
import org.gradle.nativeplatform.test.xctest.internal.NativeTestExecuter;

import javax.inject.Inject;
import java.io.File;

/**
 * Executes XCTest tests. Test are always run in a single execution.
 *
 * @since 4.2
 */
@Incubating
public class XcTest extends Test {
    @Inject
    public XcTest(ObjectFactory objectFactory) {
        setTestExecuter(objectFactory.newInstance(NativeTestExecuter.class));

        getExtensions().getExtraProperties().set("testBundleDir", newOutputDirectory());
        getExtensions().getExtraProperties().set("workingDir", newOutputDirectory());
        setExecutable("java");
        setTestClassesDirs(getProject().files());
        setBootstrapClasspath(getProject().files());
        setClasspath(getProject().files());
    }

    @InputDirectory
    public File getTestBundleDir() {
        return ((DirectoryVar) getExtensions().getExtraProperties().get("testBundleDir")).getAsFile().get();
    }

    public void setTestBundleDir(File testBundleDir) {
        ((DirectoryVar) getExtensions().getExtraProperties().get("testBundleDir")).set(testBundleDir);
    }

    public void setTestBundleDir(Provider<? extends Directory> testBundleDir) {
        ((DirectoryVar) getExtensions().getExtraProperties().get("testBundleDir")).set(testBundleDir);
    }

    @Internal
    public File getWorkingDir() {
        return ((DirectoryVar) getExtensions().getExtraProperties().get("workingDir")).getAsFile().get();
    }

    public void setWorkingDir(File workingDir) {
        ((DirectoryVar) getExtensions().getExtraProperties().get("workingDir")).set(workingDir);
    }

    public void setWorkingDir(Provider<? extends Directory> workingDir) {
        ((DirectoryVar) getExtensions().getExtraProperties().get("workingDir")).set(workingDir);
    }
}
