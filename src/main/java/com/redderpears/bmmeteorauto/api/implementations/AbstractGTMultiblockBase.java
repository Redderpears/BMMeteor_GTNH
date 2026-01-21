package com.redderpears.bmmeteorauto.api.implementations;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;

public abstract class AbstractGTMultiblockBase<T extends MTEExtendedPowerMultiBlockBase<T>>
    extends MTEExtendedPowerMultiBlockBase<T> {

    protected AbstractGTMultiblockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected AbstractGTMultiblockBase(String aName) {
        super(aName);
    }

    @Nullable
    protected TileEntity getTileEntityAtRelativePosition(int @NotNull [] relativePos) {
        if (relativePos.length < 3) return null;
        int[] relativeCoords = new int[] { 0, 0, 0 };
        this.getExtendedFacing()
            .getWorldOffset(relativePos, relativeCoords);

        ChunkCoordinates worldCoords = this.getBaseMetaTileEntity()
            .getCoords();

        return this.getBaseMetaTileEntity()
            .getTileEntity(
                worldCoords.posX + relativeCoords[0],
                worldCoords.posY + relativeCoords[1],
                worldCoords.posZ + relativeCoords[2]);
    }
}
