package org.apereo.models;

public class Config {
    private boolean upgradeRecommended;
    private boolean upgradeRequired;

    public static class Builder {
        private boolean upgradeRecommended;
        private boolean upgradeRequired;

        public Builder setUpgradeRecommended(boolean upgradeRecommended) {
            this.upgradeRecommended = upgradeRecommended;
            return this;
        }

        public Builder setUpgradeRequired(boolean upgradeRequired) {
            this.upgradeRequired = upgradeRequired;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }

    private Config(Builder builder) {
        upgradeRecommended = builder.upgradeRecommended;
        upgradeRequired = builder.upgradeRequired;
    }

    public boolean isUpgradeRecommended() {
        return upgradeRecommended;
    }

    public boolean isUpgradeRequired() {
        return upgradeRequired;
    }


}
