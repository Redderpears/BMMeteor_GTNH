package com.redderpears.bmmeteorauto.api.implementations;

import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;

public abstract class AbstractGTMultiblockBase<T extends MTEExtendedPowerMultiBlockBase<T>>
    extends MTEExtendedPowerMultiBlockBase<T> {

    protected AbstractGTMultiblockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected AbstractGTMultiblockBase(String aName) {
        super(aName);
    }

}
