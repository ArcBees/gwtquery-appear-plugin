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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.arcbees.gquery.appear.client.AppearOptions.AppearOffset;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Predicate;
import com.google.gwt.query.client.plugin.Observe;
import com.google.gwt.query.client.plugins.Plugin;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
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
    private static final Function ON_SCROLL_FUNCTION = new Function() {
        @Override
        public void f() {
            hasScrolled = true;
        }
    };

    private static HandlerRegistration handlerRegistration;
    private static boolean useMutationObserver = true;
    private static boolean hasScrolled;
    private static boolean hasResized;
    private static boolean hasDomMutated;
    private static Observe mutationHandler;

    /**
     * @param useMutationObserver Set to true to use the MutationObserver (default: true)
     * @see <a href="http://www.w3.org/TR/dom/#interface-mutationobserver">W3C DOM4 MutationObserver</a>
     */
    public static void setUseMutationObserver(boolean useMutationObserver) {
        com.arcbees.gquery.appear.client.Appear.useMutationObserver = useMutationObserver;
        if (handlerRegistration != null) {
            mutationHandler = createMutationHandler();
        }
    }

    /**
     * Force checking the appearance status of all bound elements
     */
    public static void checkElementsAppearance() {
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

    private static void checkElementAppearance(Element element, AppearImpl impl) {

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
        int elemBottom = elemTop + e.getOffsetHeight() - offset.getBottom();
        int elemRight = elemLeft + e.getOffsetWidth() - offset.getRight();

        boolean isVerticalVisible = elemTop >= docViewTop && elemBottom <= docViewBottom
                || elemTop <= docViewBottom && elemBottom > docViewBottom
                || elemBottom >= docViewTop && elemTop < docViewTop;

        boolean isHorizontalVisible = elemLeft >= docViewLeft && elemRight <= docViewRight
                || elemLeft <= docViewRight && elemRight > docViewRight
                || elemRight >= docViewLeft && elemLeft < docViewLeft;

        return isVerticalVisible && isHorizontalVisible;
    }

    private static Observe createMutationHandler() {
        if (useMutationObserver) {
            return $(Document.get()).as(Observe.Observe)
                    .observe(Observe.createInit()
                            .childList(true)
                            .subtree(true), new Function() {
                        @Override
                        public void f() {
                            hasDomMutated = true;
                        }
                    });
        } else {
            return null;
        }
    }

    private static AppearImpl getImpl(Element e) {
        return impls.get(e);
    }

    protected Appear(GQuery gq) {
        super(gq);
    }

    /**
     * Force checking the appearance status of selected elements
     */
    public Appear checkAppearance() {
        for (Element element : elements()) {
            AppearImpl impl = getImpl(element);
            if (impl != null) {
                checkElementAppearance(element, impl);
            }
        }

        return this;
    }

    /**
     * @return true if all the elements are visible in the view area, false otherwise
     */
    public boolean isAppeared() {
        for (Element element : elements()) {
            AppearImpl impl = getImpl(element);
            if (impl != null) {
                if (!impl.isAppeared()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Binds functions to be triggered when the elements appear in the view area
     *
     * @param options
     * @param f       Functions to be executed when elements appear in the view area
     */
    public Appear appear(AppearOptions options, Function... f) {
        on(APPEAR_EVENT, f);

        for (Element element : elements()) {
            AppearImpl impl = getOrCreateImpl(element, options);
            impl.setAppear(true);
        }

        maybeInitHandlers();

        return this;
    }

    /**
     * Binds functions to be triggered when the elements appear in the view area
     *
     * @param f Functions to be executed when elements appear in the view area
     * @return this
     */
    public Appear appear(Function... f) {
        return appear(new AppearOptions(), f);
    }

    /**
     * Turns off appearance detection for the elements
     */
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

    /**
     * Binds functions to be triggered when the elements disappear from the view area
     *
     * @param f Functions to be executed when elements disappear from the view area
     */
    public Appear disappear(Function... f) {
        return disappear(new AppearOptions(), f);
    }

    /**
     * Binds functions to be triggered when the elements disappear from the view area
     *
     * @param options
     * @param f       Functions to be executed when elements disappear from the view area
     */
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

    /**
     * Turns off disappearance detection for the elements
     */
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

        refreshScrollEvents();
    }

    private void maybeInitHandlers() {
        if (handlerRegistration == null) {
            final HandlerRegistration scrollHandler = addWindowScrollHandler();
            final HandlerRegistration resizeHandler = addWindowResizeHandler();
            mutationHandler = createMutationHandler();

            final Timer checkForEventsTimer = createEventsTimer();
            checkForEventsTimer.scheduleRepeating(200);

            handlerRegistration = new HandlerRegistration() {
                @Override
                public void removeHandler() {
                    checkForEventsTimer.cancel();
                    scrollHandler.removeHandler();
                    resizeHandler.removeHandler();
                    if (mutationHandler != null) {
                        mutationHandler.disconnect();
                    }
                }
            };
        }

        refreshScrollEvents();
    }

    private void refreshScrollEvents() {
        final GQuery body = $(GQuery.body);
        $(new ArrayList<>(impls.keySet()))
                .unbind(Event.ONSCROLL, ON_SCROLL_FUNCTION)
                .filter(new Predicate() {
                    @Override
                    public boolean f(Element e, int index) {
                        return body.has(e).length() == 1;
                    }
                })
                .parents()
                .filter(new Predicate() {
                    @Override
                    public boolean f(Element e, int index) {
                        return isScrollable(e);
                    }
                })
                .scroll(ON_SCROLL_FUNCTION);
    }

    private Timer createEventsTimer() {
        return new Timer() {
            @Override
            public void run() {
                if (hasDomMutated) {
                    refreshScrollEvents();
                }

                if (hasScrolled || hasResized || hasDomMutated) {
                    hasScrolled = false;
                    hasResized = false;
                    hasDomMutated = false;
                    checkElementsAppearance();
                }
            }
        };
    }

    private HandlerRegistration addWindowResizeHandler() {
        return Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                hasResized = true;
            }
        });
    }

    private HandlerRegistration addWindowScrollHandler() {
        return Window.addWindowScrollHandler(new Window.ScrollHandler() {
            @Override
            public void onWindowScroll(Window.ScrollEvent event) {
                ON_SCROLL_FUNCTION.f();
            }
        });
    }

    private boolean isScrollable(Element e) {
        String auto = Style.Overflow.AUTO.getCssName();
        String scroll = Style.Overflow.SCROLL.getCssName();

        String overflow = e.getStyle().getOverflow();
        String overflowX = e.getStyle().getOverflowX();
        String overflowY = e.getStyle().getOverflowY();

        return auto.equalsIgnoreCase(overflow)
                || auto.equalsIgnoreCase(overflowX)
                || auto.equalsIgnoreCase(overflowY)
                || scroll.equalsIgnoreCase(overflow)
                || scroll.equalsIgnoreCase(overflowX)
                || scroll.equalsIgnoreCase(overflowY);
    }

    private AppearImpl getOrCreateImpl(Element element, AppearOptions options) {
        AppearImpl impl = getImpl(element);
        if (impl == null) {
            impl = new AppearImpl(options);
            impls.put(element, impl);
        }

        return impl;
    }
}
