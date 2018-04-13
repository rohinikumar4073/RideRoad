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

package org.gradle.nativeplatform.test.xctest.internal;

import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.swift.SwiftBinary;
import org.gradle.language.swift.internal.DefaultSwiftBinary;
import org.gradle.language.swift.internal.DefaultSwiftComponent;
import org.gradle.nativeplatform.test.xctest.SwiftXCTestSuite;

public class DefaultSwiftXCTestSuite extends DefaultSwiftComponent implements SwiftXCTestSuite {
    private final DefaultSwiftBinary executable;

    public DefaultSwiftXCTestSuite(String name, ObjectFactory objectFactory, FileOperations fileOperations, ProviderFactory providerFactory, ConfigurationContainer configurations) {
        super(name, fileOperations, providerFactory, configurations);
        executable = new DefaultSwiftBinary(name + "Exe", objectFactory, getModule(), true, getSwiftSource(), configurations, getImplementationDependencies());
    }

    @Override
    public SwiftBinary getDevelopmentBinary() {
        return executable;
    }

    @Override
    public SwiftBinary getExecutable() {
        return executable;
    }
}
