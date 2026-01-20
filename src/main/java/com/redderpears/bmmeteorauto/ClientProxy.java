package com.redderpears.bmmeteorauto;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import kubatech.api.utils.ModUtils;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    // in kubatech:

    // calls super's init/starting/started/stopping methods (aka the ones in CommonProxy)

    public void preInit(FMLPreInitializationEvent event) {
        ModUtils.isClientSided = true;
        super.preInit(event);
    }

    public void init(FMLInitializationEvent event) {
        ModUtils.isClientSided = true;
        super.init(event);
    }
}
