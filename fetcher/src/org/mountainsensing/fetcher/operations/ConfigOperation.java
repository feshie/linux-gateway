package org.mountainsensing.fetcher.operations;

import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class ConfigOperation extends Operation {

    @Override
    public String getRessource() {
        return "config";
    }
}
