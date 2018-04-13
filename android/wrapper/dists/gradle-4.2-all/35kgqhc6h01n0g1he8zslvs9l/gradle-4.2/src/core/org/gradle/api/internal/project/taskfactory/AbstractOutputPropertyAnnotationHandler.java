/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.project.taskfactory;

import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.tasks.TaskOutputFilePropertyBuilder;

import java.util.Collection;
import java.util.concurrent.Callable;

public abstract class AbstractOutputPropertyAnnotationHandler implements PropertyAnnotationHandler {

    public void attachActions(final TaskPropertyActionContext context) {
        context.setValidationAction(new ValidationAction() {
            @Override
            public void validate(String propertyName, Object value, Collection<String> messages) {
                AbstractOutputPropertyAnnotationHandler.this.validate(propertyName, value, messages);
            }
        });
        context.setConfigureAction(new UpdateAction() {
            @Override
            public void update(TaskInternal task, final Callable<Object> futureValue) {
                createPropertyBuilder(context, task, futureValue)
                    .withPropertyName(context.getName())
                    .optional(context.isOptional());
                task.prependParallelSafeAction(new CreateOutputDirectoryTaskAction(context.getName(), futureValue));
            }
        });
    }

    protected abstract TaskOutputFilePropertyBuilder createPropertyBuilder(TaskPropertyActionContext context, TaskInternal task, Callable<Object> futureValue);

    protected abstract void beforeTask(Callable<Object> futureValue);

    protected abstract void validate(String propertyName, Object value, Collection<String> messages);

    private class CreateOutputDirectoryTaskAction implements Action<Task>, Describable {
        private final String propertyName;
        private final Callable<Object> futureValue;

        public CreateOutputDirectoryTaskAction(String propertyName, Callable<Object> futureValue) {
            this.propertyName = propertyName;
            this.futureValue = futureValue;
        }

        @Override
        public void execute(Task task) {
            beforeTask(futureValue);
        }

        @Override
        public String getDisplayName() {
            return "Create " + propertyName + " output directory";
        }
    }
}