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

package org.gradle.internal.operations.notify;

import com.google.common.collect.Lists;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.GradleInternal;
import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.progress.BuildOperationDescriptor;
import org.gradle.internal.progress.BuildOperationListener;
import org.gradle.internal.progress.BuildOperationListenerManager;
import org.gradle.internal.progress.OperationFinishEvent;
import org.gradle.internal.progress.OperationStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BuildOperationNotificationBridge implements BuildOperationNotificationListenerRegistrar, Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildOperationNotificationBridge.class);

    private final RecordingBuildOperationNotificationListener recordingListener;
    private final BuildOperationListenerManager listenerManager;
    private BuildOperationListener transformingListener;
    private BuildOperationNotificationListener registeredListener;
    private boolean stopped;

    BuildOperationNotificationBridge(BuildOperationListenerManager buildOperationListenerManager) {
        this.listenerManager = buildOperationListenerManager;
        recordingListener = new RecordingBuildOperationNotificationListener();
        transformingListener = new TransformingListener(recordingListener);
    }

    public void start(GradleInternal gradle) {
        listenerManager.addListener(transformingListener);

        // ensure we only store events for configuration phase to keep overhead small
        // when build scan plugin is not applied
        gradle.rootProject(new Action<Project>() {
            @Override
            public void execute(Project project) {
                project.afterEvaluate(new Action<Project>() {
                    @Override
                    public void execute(Project project) {
                        if (registeredListener == null) {
                            stop();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void registerBuildScopeListener(BuildOperationNotificationListener notificationListener) {
        preventDuplicateRegistration(notificationListener);

        // Stop and remove the recording listener: we ain't gonna use it
        listenerManager.removeListener(transformingListener);
        recordingListener.stop();

        // Add a new transforming listener directly on top of the supplied listener: this ensures a consistent event stream.
        transformingListener = new TransformingListener(notificationListener);
        listenerManager.addListener(transformingListener);
    }

    @Override
    public void registerBuildScopeListenerAndReceiveStoredOperations(BuildOperationNotificationListener notificationListener) {
        preventDuplicateRegistration(notificationListener);

        this.recordingListener.replayAndAttach(notificationListener);
    }

    private void preventDuplicateRegistration(BuildOperationNotificationListener notificationListener) {
        if (registeredListener != null) {
            throw new IllegalStateException("listener is already registered (implementation class " + registeredListener.getClass().getName() + ")");
        }
        registeredListener = notificationListener;
    }

    @Override
    public void stop() {
        if (!stopped) {
            listenerManager.removeListener(transformingListener);
            recordingListener.stop();
            stopped = true;
        }
    }

    /*
        Note: the intention here is to work towards not having to create new objects
        to meet the notification object interfaces.
        Instead, the base types like BuildOperationDescriptor should implement them natively.
        However, this will require restructuring this type and associated things such as
        OperationStartEvent. This will happen later.
     */

    private static class TransformingListener implements BuildOperationListener {

        private final BuildOperationNotificationListener notificationListener;

        private final Map<Object, Object> parents = new ConcurrentHashMap<Object, Object>();
        private final Map<Object, Object> active = new ConcurrentHashMap<Object, Object>();

        private TransformingListener(BuildOperationNotificationListener notificationListener) {
            this.notificationListener = notificationListener;
        }

        @Override
        public void started(BuildOperationDescriptor buildOperation, OperationStartEvent startEvent) {
            Object id = buildOperation.getId();
            Object parentId = buildOperation.getParentId();
            if (parentId != null) {
                if (active.containsKey(parentId)) {
                    parents.put(id, parentId);
                } else {
                    parentId = parents.get(parentId);
                    if (parentId != null) {
                        parents.put(id, parentId);
                    }
                }
            }

            if (buildOperation.getDetails() == null) {
                return;
            }

            active.put(id, "");

            Started notification = new Started(startEvent.getStartTime(), id, parentId, buildOperation.getDetails());

            try {
                notificationListener.started(notification);
            } catch (Throwable e) {
                LOGGER.debug("Build operation notification listener threw an error on " + notification, e);
                maybeThrow(e);
            }
        }

        private void maybeThrow(Throwable e) {
            if (e instanceof Error && !(e instanceof LinkageError)) {
                throw (Error) e;
            }
        }

        @Override
        public void finished(BuildOperationDescriptor buildOperation, OperationFinishEvent finishEvent) {
            Object id = buildOperation.getId();
            Object parentId = parents.remove(id);
            if (active.remove(id) == null) {
                return;
            }

            Finished notification = new Finished(finishEvent.getEndTime(), id, parentId, buildOperation.getDetails(), finishEvent.getResult(), finishEvent.getFailure());
            try {
                notificationListener.finished(notification);
            } catch (Throwable e) {
                LOGGER.debug("Build operation notification listener threw an error on " + notification, e);
                maybeThrow(e);
            }
        }
    }

    private static class RecordingBuildOperationNotificationListener implements BuildOperationNotificationListener {
        private List<Object> storedEvents = Lists.newArrayList();
        private BuildOperationNotificationListener delegate;

        synchronized void replayAndAttach(BuildOperationNotificationListener listener) {

            delegate = listener;
            for (Object storedEvent : storedEvents) {
                if (storedEvent instanceof BuildOperationStartedNotification) {
                    delegate.started((BuildOperationStartedNotification) storedEvent);
                } else {
                    delegate.finished((BuildOperationFinishedNotification) storedEvent);
                }
            }
            storedEvents = null;
        }

        public boolean isActive() {
            return delegate != null;
        }

        @Override
        public synchronized void started(BuildOperationStartedNotification notification) {
            if (isActive()) {
                delegate.started(notification);
            } else {
                storedEvents.add(notification);
            }
        }

        @Override
        public synchronized void finished(BuildOperationFinishedNotification notification) {
            if (isActive()) {
                delegate.finished(notification);
            } else {
                storedEvents.add(notification);
            }
        }

        public void stop() {
            delegate = null;
            storedEvents = null;
        }
    }

    private static class Started implements BuildOperationStartedNotification {

        private final long timestamp;
        private final Object id;
        private final Object parentId;
        private final Object details;

        private Started(long timestamp, Object id, Object parentId, Object details) {
            this.timestamp = timestamp;
            this.id = id;
            this.parentId = parentId;
            this.details = details;
        }

        @Override
        public long getNotificationOperationStartedTimestamp() {
            return timestamp;
        }

        @Override
        public Object getNotificationOperationId() {
            return id;
        }

        @Override
        public Object getNotificationOperationParentId() {
            return parentId;
        }

        @Override
        public Object getNotificationOperationDetails() {
            return details;
        }

        @Override
        public String toString() {
            return "BuildOperationStartedNotification{"
                + "id=" + id
                + ", parentId=" + parentId
                + ", timestamp=" + timestamp
                + ", details=" + details
                + '}';
        }
    }

    private static class Finished implements BuildOperationFinishedNotification {

        private final long timestamp;
        private final Object id;
        private final Object parentId;
        private final Object details;
        private final Object result;
        private final Throwable failure;

        private Finished(long timestamp, Object id, Object parentId, Object details, Object result, Throwable failure) {
            this.timestamp = timestamp;
            this.id = id;
            this.parentId = parentId;
            this.details = details;
            this.result = result;
            this.failure = failure;
        }

        @Override
        public long getNotificationOperationFinishedTimestamp() {
            return timestamp;
        }

        @Override
        public Object getNotificationOperationId() {
            return id;
        }

        @Nullable
        @Override
        public Object getNotificationOperationParentId() {
            return parentId;
        }

        @Override
        public Object getNotificationOperationDetails() {
            return details;
        }

        @Override
        public Object getNotificationOperationResult() {
            return result;
        }

        @Override
        public Throwable getNotificationOperationFailure() {
            return failure;
        }

        @Override
        public String toString() {
            return "BuildOperationFinishedNotification{"
                + "id=" + id
                + ", parentId=" + parentId
                + ", timestamp=" + timestamp
                + ", details=" + details
                + ", result=" + result
                + ", failure=" + failure
                + '}';
        }
    }
}
