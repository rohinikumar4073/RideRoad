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

package org.gradle.language.cpp.plugins;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.DirectoryVar;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.cpp.CppLibrary;
import org.gradle.language.cpp.internal.DefaultCppLibrary;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.gradle.language.cpp.CppBinary.DEBUGGABLE_ATTRIBUTE;

/**
 * <p>A plugin that produces a native library from C++ source.</p>
 *
 * <p>Assumes the source files are located in `src/main/cpp`, public headers are located in `src/main/public` and implementation header files are located in `src/main/headers`.</p>
 *
 * <p>Adds a {@link CppLibrary} extension to the project to allow configuration of the library.</p>
 *
 * @since 4.1
 */
@Incubating
public class CppLibraryPlugin implements Plugin<ProjectInternal> {
    private final FileOperations fileOperations;

    /**
     * Injects a {@link FileOperations} instance.
     *
     * @since 4.2
     */
    @Inject
    public CppLibraryPlugin(FileOperations fileOperations) {
        this.fileOperations = fileOperations;
    }

    @Override
    public void apply(final ProjectInternal project) {
        project.getPluginManager().apply(CppBasePlugin.class);

        TaskContainer tasks = project.getTasks();
        ConfigurationContainer configurations = project.getConfigurations();
        DirectoryVar buildDirectory = project.getLayout().getBuildDirectory();
        ObjectFactory objectFactory = project.getObjects();
        ProviderFactory providers = project.getProviders();

        // Add the library extension
        final CppLibrary library = project.getExtensions().create(CppLibrary.class, "library", DefaultCppLibrary.class, "main", project.getObjects(), fileOperations, providers, project.getConfigurations());
        project.getComponents().add(library);
        project.getComponents().add(library.getDebugSharedLibrary());
        project.getComponents().add(library.getReleaseSharedLibrary());

        // Configure the component
        library.getBaseName().set(project.getName());

        // Define the outgoing artifacts
        // TODO - move this to the base plugin

        final LinkSharedLibrary linkDebug = (LinkSharedLibrary) tasks.getByName("linkDebug");
        // TODO - make this lazy, make a query method on the link task
        final PlatformToolProvider platformToolChain = ((NativeToolChainInternal) linkDebug.getToolChain()).select((NativePlatformInternal) linkDebug.getTargetPlatform());
        // TODO - should reflect changes to the task configuration
        Provider<RegularFile> debugLinkFile = buildDirectory.file(providers.provider(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return platformToolChain.getSharedLibraryLinkFileName("lib/main/debug/" + library.getBaseName().get());
            }
        }));

        final LinkSharedLibrary linkRelease = (LinkSharedLibrary) tasks.getByName("linkRelease");
        // TODO - should reflect changes to the task configuration
        Provider<RegularFile> releaseLinkFile = buildDirectory.file(providers.provider(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return platformToolChain.getSharedLibraryLinkFileName("lib/main/release/" + library.getBaseName().get());
            }
        }));

        tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(linkDebug);

        // TODO - add lifecycle tasks
        // TODO - extract some common code to setup the configurations

        Configuration apiElements = configurations.create("cppApiElements");
        apiElements.extendsFrom(library.getApiDependencies());
        apiElements.setCanBeResolved(false);
        apiElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.C_PLUS_PLUS_API));
        // TODO - deal with more than one header dir, e.g. generated public headers
        Provider<File> publicHeaders = providers.provider(new Callable<File>() {
            @Override
            public File call() throws Exception {
                Set<File> files = library.getPublicHeaderDirs().getFiles();
                if (files.size() != 1) {
                    throw new UnsupportedOperationException(String.format("The C++ library plugin currently requires exactly one public header directory, however there are %d directories configured: %s", files.size(), files));
                }
                return files.iterator().next();
            }
        });
        apiElements.getOutgoing().artifact(publicHeaders);

        Configuration implementation = library.getImplementationDependencies();

        Configuration debugLinkElements = configurations.create("debugLinkElements");
        debugLinkElements.extendsFrom(implementation);
        debugLinkElements.setCanBeResolved(false);
        debugLinkElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_LINK));
        debugLinkElements.getAttributes().attribute(DEBUGGABLE_ATTRIBUTE, true);
        // TODO - should reflect changes to task output file
        debugLinkElements.getOutgoing().artifact(debugLinkFile, new Action<ConfigurablePublishArtifact>() {
            @Override
            public void execute(ConfigurablePublishArtifact artifact) {
                artifact.builtBy(linkDebug);
            }
        });

        Configuration debugRuntimeElements = configurations.create("debugRuntimeElements");
        debugRuntimeElements.extendsFrom(implementation);
        debugRuntimeElements.setCanBeResolved(false);
        debugRuntimeElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_RUNTIME));
        debugRuntimeElements.getAttributes().attribute(DEBUGGABLE_ATTRIBUTE, true);
        debugRuntimeElements.getOutgoing().artifact(linkDebug.getBinaryFile());

        Configuration releaseLinkElements = configurations.create("releaseLinkElements");
        releaseLinkElements.extendsFrom(implementation);
        releaseLinkElements.setCanBeResolved(false);
        releaseLinkElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_LINK));
        releaseLinkElements.getAttributes().attribute(DEBUGGABLE_ATTRIBUTE, false);
        // TODO - should reflect changes to task output file
        releaseLinkElements.getOutgoing().artifact(releaseLinkFile, new Action<ConfigurablePublishArtifact>() {
            @Override
            public void execute(ConfigurablePublishArtifact artifact) {
                artifact.builtBy(linkRelease);
            }
        });

        Configuration releaseRuntimeElements = configurations.create("releaseRuntimeElements");
        releaseRuntimeElements.extendsFrom(implementation);
        releaseRuntimeElements.setCanBeResolved(false);
        releaseRuntimeElements.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_RUNTIME));
        releaseRuntimeElements.getAttributes().attribute(DEBUGGABLE_ATTRIBUTE, false);
        releaseRuntimeElements.getOutgoing().artifact(linkRelease.getBinaryFile());
    }
}
