package org.mountainsensing.fetcher.operations;

import org.mountainsensing.fetcher.Operation;

/**
 *
 */
public abstract class DateOperation extends Operation {

    @Override
    public String getRessource() {
        return "date";
    }
}
