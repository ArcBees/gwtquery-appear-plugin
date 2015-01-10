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

import java.util.HashMap;
import java.util.Map;

import com.arcbees.gquery.appear.client.AppearOptions.AppearOffset;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.plugin.Observe;
import com.google.gwt.query.client.plugins.Plugin;
import com.google.gwt.user.client.Window;

public class Appear extends GQuery {
    public static final Class<Appear> Appear = GQuery.registerPlugin(Appear.class, new Plugin<Appear>() {
        public Appear init(GQuery gq) {
            return new Appear(gq);
        }
    });

    private static final String DISAPPEAR_EVENT = "disappear";
    private static final String APPEAR_EVENT = "appear";
    private static final Map<Element, AppearImpl> impls = new HashMap<>();

    private static HandlerRegistration handlerRegistration;

    protected Appear(GQuery gq) {
        super(gq);
    }

    public Appear appear(AppearOptions options, Function... f) {
        on(APPEAR_EVENT, f);

        for (Element element : elements()) {
            AppearImpl impl = getOrCreateImpl(element, options);
            impl.setAppear(true);
        }

        maybeInitHandlers();

        return this;
    }

    public Appear appear(Function... f) {
        return appear(new AppearOptions(), f);
    }

    public Appear appearOff() {
        off(APPEAR_EVENT);

        for (Element element : elements()) {
            AppearImpl impl = getImpl(element);
            if (impl != null) {
                if (impl.isDisappear()) {
                    impl.setAppear(false);
                } else {
                    impls.remove(impl);
                }
            }
        }

        maybeUnbindHandlers();

        return this;
    }

    public Appear disappear(Function... f) {
        return disappear(new AppearOptions(), f);
    }

    public Appear disappear(
            AppearOptions options,
            Function... f) {
        on(DISAPPEAR_EVENT, f);

        for (Element element : elements()) {
            AppearImpl impl = getOrCreateImpl(element, options);

            impl.setDisappear(true);
        }

        maybeInitHandlers();

        return this;
    }

    public Appear disappearOff() {
        off(DISAPPEAR_EVENT);

        for (Element element : elements()) {
            AppearImpl impl = getImpl(element);

            if (impl != null) {
                if (impl.isAppear()) {
                    impl.setDisappear(false);
                } else {
                    impls.remove(impl);
                }
            }
        }

        maybeUnbindHandlers();

        return this;
    }

    private void maybeUnbindHandlers() {
        if (impls.isEmpty()) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
    }

    private void maybeInitHandlers() {
        if (handlerRegistration == null) {
            final HandlerRegistration scrollHandler = Window.addWindowScrollHandler(new Window.ScrollHandler() {
                @Override
                public void onWindowScroll(Window.ScrollEvent event) {
                    checkElementsAppearance();
                }
            });

            final HandlerRegistration resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    checkElementsAppearance();
                }
            });

            final Observe observeHandler = $(Document.get()).as(Observe.Observe)
                    .observe(Observe.createInit()
                            .childList(true)
                            .subtree(true), new Function() {
                        @Override
                        public void f() {
                            checkElementsAppearance();
                        }
                    });

            handlerRegistration = new HandlerRegistration() {
                @Override
                public void removeHandler() {
                    scrollHandler.removeHandler();
                    resizeHandler.removeHandler();
                    observeHandler.disconnect();
                }
            };
        }
    }

    private AppearImpl getOrCreateImpl(Element element, AppearOptions options) {
        AppearImpl impl = getImpl(element);
        if (impl == null) {
            impl = new AppearImpl(options);
            impls.put(element, impl);
        }

        return impl;
    }

    private static void checkElementsAppearance() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                for (Map.Entry<Element, AppearImpl> entry : impls.entrySet()) {
                    Element element = entry.getKey();
                    AppearImpl impl = entry.getValue();

                    checkElementAppearance(element, impl);
                }
            }
        });
    }

    private static void checkElementAppearance(
            Element element,
            AppearImpl impl) {

        if ($(element).isVisible()) {
            if (isScrolledIntoView(element, impl)) {
                maybeOnElementAppeared(element, impl);
            } else {
                maybeOnElementDisappeared(element, impl);
            }
        } else {
            maybeOnElementDisappeared(element, impl);
        }
    }

    private static void maybeOnElementAppeared(Element element, AppearImpl impl) {
        if (!impl.isAppeared()) {
            $(element).as(Events).trigger(APPEAR_EVENT);
            impl.setAppeared(true);
        }
    }

    private static void maybeOnElementDisappeared(Element element, AppearImpl impl) {
        if (impl.isAppeared()) {
            $(element).as(Events).trigger(DISAPPEAR_EVENT);
            impl.setAppeared(false);
        }
    }

    private static boolean isScrolledIntoView(Element e, AppearImpl impl) {
        int docViewTop = Window.getScrollTop();
        int docViewBottom = docViewTop + Window.getClientHeight();
        int docViewLeft = Window.getScrollLeft();
        int docViewRight = docViewLeft + Window.getClientWidth();

        AppearOffset offset = impl.getOptions().getOffset();
        int elemTop = e.getAbsoluteTop() + offset.getTop();
        int elemLeft = e.getAbsoluteLeft() + offset.getLeft();
        int elemBottom = elemTop + Math.max(0, e.getOffsetHeight() - offset.getBottom());
        int elemRight = elemLeft + Math.max(0, e.getOffsetWidth() - offset.getRight());

        boolean isVerticalVisible = elemTop >= docViewTop && elemBottom <= docViewBottom
                || elemTop <= docViewBottom && elemBottom > docViewBottom
                || elemBottom >= docViewTop && elemTop < docViewTop;

        boolean isHorizontalVisible = elemLeft >= docViewLeft && elemRight <= docViewRight
                || elemLeft <= docViewRight && elemRight > docViewRight
                || elemRight >= docViewLeft && elemLeft < docViewLeft;

        return isVerticalVisible && isHorizontalVisible;
    }

    private static AppearImpl getImpl(Element e) {
        return impls.get(e);
    }
}
