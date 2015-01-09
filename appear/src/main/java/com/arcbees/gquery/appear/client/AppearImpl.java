package com.arcbees.gquery.appear.client;

public class AppearImpl {
    private boolean isAppear;
    private boolean isDisappear;
    private boolean appeared;

    public AppearImpl() {
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
