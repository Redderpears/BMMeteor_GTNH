package com.redderpears.bmmeteorauto.loaders;

import static com.redderpears.bmmeteorauto.api.enums.ItemList.MeteorNet;
import static gregtech.api.enums.Mods.BloodMagic;

import com.redderpears.bmmeteorauto.tileentity.gregtech.multiblock.MTEMeteorNet;

public class RecipeLoader {

    public static void registerMTEs() {
        if (BloodMagic.isModLoaded()) {
            MeteorNet.set(new MTEMeteorNet(31794, "multimachine.meteornet", "Meteor Net").getStackForm(1));
        }
    }

}
