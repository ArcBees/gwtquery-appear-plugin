/**
 * Copyright 2015 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.arcbees.gquery.appear.client;

public class AppearOptions {
    public static class AppearOffset {
        private final int top;
        private final int right;
        private final int bottom;
        private final int left;

        public AppearOffset(
                int top,
                int right,
                int bottom,
                int left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }

        public int getTop() {
            return top;
        }

        public int getRight() {
            return right;
        }

        public int getBottom() {
            return bottom;
        }

        public int getLeft() {
            return left;
        }
    }

    private static AppearOffset globalOffset;

    public static void setGlobalOffset(AppearOffset offset) {
        globalOffset = offset;
    }

    static {
        globalOffset = new AppearOffset(0, 0, 0, 0);
    }

    private AppearOffset offset;

    public AppearOffset getOffset() {
        return getFirstOr(offset, globalOffset);
    }

    public AppearOptions withOffset(AppearOffset offset) {
        this.offset = offset;
        return this;
    }

    private <T> T getFirstOr(T first, T global) {
        return first != null ? first : global;
    }
}
