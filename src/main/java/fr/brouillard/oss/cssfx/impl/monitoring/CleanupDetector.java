package fr.brouillard.oss.cssfx.impl.monitoring;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 - 2020 CSSFX by Matthieu Brouillard
 * %%
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
 * #L%
 */

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;

public class CleanupDetector {

    private static HashSet<WeakReferenceWithRunnable> references = new HashSet<WeakReferenceWithRunnable>();
    private static ReferenceQueue referenceQueue = new ReferenceQueue();

    static {
        Thread cleanupDetectorThread = new Thread(() -> {
            while (true) {
                try {
                    WeakReferenceWithRunnable r = (WeakReferenceWithRunnable) referenceQueue.remove();
                    references.remove(r);
                    r.r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, "CSSFX-cleanup-detector");
        cleanupDetectorThread.setDaemon(true);
        cleanupDetectorThread.start();
    }

    /**
     * The runnable gets executed after the object has been collected by the GC.
     */
    public static void onCleanup(Object obj, Runnable r) {
        onCleanup(new WeakReferenceWithRunnable(obj, referenceQueue, r));
    }
    /**
     * This version of the method can be used to provide more information
     * in the heap dump by extending WeakReferenceWithRunnable.
     */
    public static void onCleanup(WeakReferenceWithRunnable weakref) {
        references.add(weakref);
    }

    /**
     * This class can be extended to provide more meta information to the method onCleanup.
     */
    public static class WeakReferenceWithRunnable extends WeakReference {
        Runnable r = null;
        WeakReferenceWithRunnable(Object ref, ReferenceQueue queue, Runnable r) {
            super(ref, queue);
            this.r = r;
        }
    }
}