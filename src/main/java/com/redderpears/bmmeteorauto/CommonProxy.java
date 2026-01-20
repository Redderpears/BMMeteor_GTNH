package com.redderpears.bmmeteorauto;

import static com.redderpears.bmmeteorauto.loaders.BlockRegister.registerBlocks;
import static com.redderpears.bmmeteorauto.loaders.ItemRegister.registerItems;

import com.redderpears.bmmeteorauto.loaders.RecipeLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

// reverse engineering kubatech (thanks)
public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile()); // loads configurations

        BMMeteor.LOG.info(Config.greeting); // sanity check logging since there's no other content yet
        BMMeteor.LOG.info("I am BMMeteor at version " + Tags.VERSION);

        registerItems();
        registerBlocks();

        // in kubatech:

        /*
         * initializes config through Config (it's in its own folder for some reason) **
         * runs synchronizeConfiguration(), custom to ensure MobHandler & debug are updated to the new config. **
         * registers a new FMLEventHandler with FMLCommonHandler, for hook
         * registers a new PlayerDataManager with MinecraftForge's EVENT_BUS
         * registers items and blocks, helper functions from other java files (they put it in ./loaders/ItemLoaders |
         * BlockLoader, include at top)
         ************************ ^^^^^^^^^^^^ IMPORTANT
         * if MobsInfo is loaded, initialize kuba's MobHandlerLoader (for eec prolly)
         */
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        BMMeteor.LOG.info(Config.greeting); // sanity check logging since there's no other content yet
        BMMeteor.LOG.info("I am BMMeteor at version " + Tags.VERSION + " registering the thing I made!");
        RecipeLoader.registerMTEs();
        // in kubatech:

        /*
         * registers recipes from other mods, done here to ensure the other mods have loaded. ***
         * YOU NEED TO DO THIS TOO YOU IDIOT, ONCE MINETWEAKER LOADS METEOR RECIPES FROM BM!! ***
         * if MineTweaker is loaded, init that too.
         */

    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {

        // in kubatech:

        /*
         * adds recipe from loader
         * registers commands via kubatech CommandHandler
         */
    }

    // everything else not used
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {

    }
}
