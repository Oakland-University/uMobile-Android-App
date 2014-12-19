package org.apereo.models;

import java.util.ArrayList;

public class Config {
    private boolean upgradeRecommended;
    private boolean upgradeRequired;
    private ArrayList<String> disabledPortlets;

    public static class Builder {
        private boolean upgradeRecommended;
        private boolean upgradeRequired;
        private ArrayList<String> disabledPortlets;

        public Builder setUpgradeRecommended(boolean upgradeRecommended) {
            this.upgradeRecommended = upgradeRecommended;
            return this;
        }

        public Builder setUpgradeRequired(boolean upgradeRequired) {
            this.upgradeRequired = upgradeRequired;
            return this;
        }

        public Builder setDisabledPortlets(ArrayList<String> disabledPortlets) {
            this.disabledPortlets = disabledPortlets;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }

    private Config(Builder builder) {
        upgradeRecommended = builder.upgradeRecommended;
        upgradeRequired = builder.upgradeRequired;
        disabledPortlets = builder.disabledPortlets;
    }

    public boolean isUpgradeRecommended() {
        return upgradeRecommended;
    }

    public boolean isUpgradeRequired() {
        return upgradeRequired;
    }

    public ArrayList<String> getDisabledPortlets() {
        return disabledPortlets;
    }

}
