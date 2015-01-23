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

public class AppearImpl {
    private final AppearOptions options;

    private boolean isAppear;
    private boolean isDisappear;
    private boolean appeared;

    public AppearImpl(AppearOptions options) {
        this.options = options;
    }

    public AppearOptions getOptions() {
        return options;
    }

    public boolean isDisappear() {
        return isDisappear;
    }

    public void setDisappear(boolean isDisappear) {
        this.isDisappear = isDisappear;
    }

    public boolean isAppear() {
        return isAppear;
    }

    public void setAppear(boolean isAppear) {
        this.isAppear = isAppear;
    }

    public boolean isAppeared() {
        return appeared;
    }

    public void setAppeared(boolean appeared) {
        this.appeared = appeared;
    }
}
