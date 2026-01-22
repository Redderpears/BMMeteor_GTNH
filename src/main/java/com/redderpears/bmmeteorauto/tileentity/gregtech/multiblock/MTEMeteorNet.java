package com.redderpears.bmmeteorauto.tileentity.gregtech.multiblock;

import static WayofTime.alchemicalWizardry.common.summoning.meteor.MeteorRegistry.isValidMeteorFocusItem;
import static WayofTime.alchemicalWizardry.common.summoning.meteor.MeteorRegistry.meteorList;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.redderpears.bmmeteorauto.BMMeteor;
import com.redderpears.bmmeteorauto.api.implementations.AbstractGTMultiblockBase;

import WayofTime.alchemicalWizardry.api.items.interfaces.IBloodOrb;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.common.entity.projectile.EntityMeteor;
import WayofTime.alchemicalWizardry.common.items.BoundPickaxe;
import WayofTime.alchemicalWizardry.common.summoning.meteor.Meteor;
import WayofTime.alchemicalWizardry.common.summoning.meteor.MeteorComponent;
import WayofTime.alchemicalWizardry.common.summoning.meteor.MeteorRegistry;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import fox.spiteful.avaritia.items.tools.ItemPickaxeInfinity;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.MultiblockTooltipBuilder;

public class MTEMeteorNet extends AbstractGTMultiblockBase<MTEMeteorNet> implements ISurvivalConstructable {

    /************************************************************************************************************************************************************************************************
     * STRUCTURE
     */

    private int mCasing = 0;
    private static final int CASING_INDEX = 16;

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<MTEMeteorNet> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEMeteorNet>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(
                new String[][] { // spotless:off
                    {"b~b"},
                    {"bsb"},
                    {"sss"},
                })) //spotless:on
        .addElement('s', onElementPass(t -> t.mCasing++, ofBlock(GregTechAPI.sBlockCasings2, 0)))
        .addElement(
            'b',
            buildHatchAdder(MTEMeteorNet.class).atLeast(InputBus, OutputBus, Energy, Maintenance)
                .casingIndex(CASING_INDEX)
                .dot(1)
                .buildAndChain(onElementPass(t -> t.mCasing++, ofBlock(GregTechAPI.sBlockCasings2, 0))))
        .build();

    @Override
    public IStructureDefinition<MTEMeteorNet> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addInfo("balls")
            .toolTipFinisher("yo");
        return tt;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        if (!checkPiece(STRUCTURE_PIECE_MAIN, 1, 0, 0)) {
            removeFakeMeteor();
            return false;
        }

        hasRitual = checkRitual();
        if (hasRitual) {
            summonFakeMeteor();
            return true;
        }

        removeFakeMeteor();
        return false;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEMeteorNet(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean aActive, boolean aRedstone) { // TODO: BETTER TEXTURE
        if (side == facing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(CASING_INDEX),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(CASING_INDEX), TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(CASING_INDEX) };
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        // TODO: MAKE THIS CONTROLLER LOCATION CUSTOMIZABLE / FOUND IN STRUCTURE
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 1, 0, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        // TODO: MAKE THIS CONTROLLER LOCATION CUSTOMIZABLE / FOUND IN STRUCTURE
        // TODO: i.e. find the ~ in the STRUCTURE_PIECE_MAIN
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 1, 0, 0, elementBudget, env, true, true);
    }

    public MTEMeteorNet(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEMeteorNet(String aName) {
        super(aName);
    }

    /************************************************************************************************************************************************************************************************
     *
     * UI
     *
     */

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);
    }

    @Override
    public void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);
        screenElements.widget(new TextWidget("balls").setEnabled(widget -> hasMeteor))
            .widget(
                new FakeSyncWidget.BooleanSyncer(
                    () -> (fakeMeteor != null && fakeMeteor.isDead),
                    val -> hasMeteor = val));
        screenElements.widget(new TextWidget("your meteor failed bro").setEnabled(widget -> !meteorSucceeded))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> meteorSucceeded, val -> meteorSucceeded = val));
        screenElements.widget(new TextWidget("cool pickaxe").setEnabled(widget -> validPickaxe))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> (findPickaxe() != null), val -> validPickaxe = val));
        screenElements.widget(new TextWidget("cool focus").setEnabled(widget -> validFocus))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> (findFocus() != null), val -> validFocus = val));
        screenElements.widget(new TextWidget("cool orb bro").setEnabled(widget -> validOrb))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> (findOrb() != null), val -> validOrb = val));
    }

    /************************************************************************************************************************************************************************************************
     *
     * RITUAL VARS / HELPERS
     *
     */

    private static final String FALLING_TOWER_NAME = "AW019FallingTower";
    private EntityFakeMeteor fakeMeteor;
    private boolean hasMeteor = false;
    private boolean hasRitual = false;
    private ItemStack pickaxe;
    private ItemStack focus;
    private ItemStack orb;
    private boolean validPickaxe; // TODO: FIND A WAY TO MAKE THIS LESS AWFUL PLEASE (less variables, these bools are
                                  // used for gui info)
    private boolean validFocus;
    private boolean validOrb;
    private boolean meteorSucceeded;
    private World world;

    public class EntityFakeMeteor extends EntityMeteor {

        public EntityFakeMeteor(World par1World) {
            super(par1World);
            maxTicksInAir = 3600 * 20; // TODO: MAKE THIS ACTUALLY WORK LOL (EXTEND METEOR AIRTIME)
        }

        public EntityFakeMeteor(World par1World, double par2, double par4, double par6, int meteorID) {
            super(par1World, par2, par4, par6, meteorID);
            maxTicksInAir = 3600 * 20; // TODO: MAKE THIS ACTUALLY WORK LOL (EXTEND METEOR AIRTIME)
        }

        public EntityFakeMeteor(World par1World, double par2, double par4, double par6, int meteorID, int maxTicks) {
            super(par1World, par2, par4, par6, meteorID);
            maxTicksInAir = maxTicks;
        }

        @Override
        public void onImpact(MovingObjectPosition mop) {
            this.setDead();
        }

        @Override
        public void onImpact(Entity mop) {}
    }

    @Nullable
    public ItemStack findFocus() { // TODO: CONSOLIDATE THESE FINDERS INTO ONE FUNCTION?

        for (MTEHatchInputBus bus : this.mInputBusses) {
            for (int i = 0; i < bus.mInventory.length; i++) {
                ItemStack item = bus.mInventory[i];
                if (item != null && isValidMeteorFocusItem(item)) {
                    if (item.stackSize <= 0) {
                        bus.mInventory[i] = null;
                        continue;
                    } else {
                        return item;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public ItemStack findPickaxe() {

        for (ItemStack item : this.getStoredInputs()) {
            if (item != null && item.getItem() instanceof ItemPickaxe) {
                return item;
            }
        }

        return null;
    }

    @Nullable
    public ItemStack findOrb() {
        for (ItemStack item : this.getStoredInputs()) {
            if (item != null && item.getItem() instanceof IBloodOrb) {
                return item;
            }
        }

        return null;
    }

    public World getWorld() {
        if (this.world != null) return world;
        world = this.getBaseMetaTileEntity()
            .getWorld();
        return world;
    }

    public boolean acceptedPickaxe(ItemPickaxe pick) { // TODO: MAKE THIS DYNAMIC
        return pick instanceof BoundPickaxe || pick instanceof ItemPickaxeInfinity;
    }

    /************************************************************************************************************************************************************************************************
     *
     * RITUAL FUNCTIONALITY
     *
     */

    private TEMasterStone meteorRitual;

    private boolean findFallingMeteorRitual() {
        final int[][] VALID_RITUAL_POSES = { { 0, -1, 0 } }; // {to the right of the controller, above the controller,
                                                             // behind the controller}
        for (int[] pose : VALID_RITUAL_POSES) {
            TileEntity te = getTileEntityAtRelativePosition(pose);
            if (isFallingTowerRitual(te)) {
                BMMeteor.LOG.info("Found Ritual!");
                meteorRitual = (TEMasterStone) te;
                return true;
            }
        }
        meteorRitual = null;
        return false;
    }

    private boolean isFallingTowerRitual(@Nullable TileEntity te) {
        return te != null && !te.isInvalid()
            && te instanceof TEMasterStone ritualStone
            && ritualStone.getCurrentRitual()
                .equals(FALLING_TOWER_NAME)
            && !ritualStone.getOwner()
                .isEmpty();
    }

    private boolean checkRitual() { // checks if there is or could be a valid falling meteor ritual in the block space
                                    // immediately above the controller.
        if (findFallingMeteorRitual()) {
            return true;
        }

        return false;
    }

    private void removeFakeMeteor() {
        if (fakeMeteor == null || fakeMeteor.isDead) return;
        fakeMeteor.setDead();
    }

    private boolean canRunMeteor(Meteor meteor) { // TODO: IS THIS NECESSARY?
        // no orb or orb can't hold enough blood to summon meteor or ritual owner can't afford the meteor
        return !(orb == null || ((IBloodOrb) orb.getItem()).getMaxEssence() < meteor.cost
            || !SoulNetworkHandler.canSyphonFromOnlyNetwork(meteorRitual.getOwner(), meteor.cost));
    }

    private boolean tryToSyphon(String user, int amount) {
        if (SoulNetworkHandler.canSyphonFromOnlyNetwork(user, amount)) {
            SoulNetworkHandler.syphonFromNetwork(user, amount);
            return true;
        } else return false;
    }

    private boolean canSyphon(String user, int amount) {
        return SoulNetworkHandler.canSyphonFromOnlyNetwork(user, amount);
    }

    /************************************************************************************************************************************************************************************************
     *
     * FAKE METEORS
     *
     */

    private static final int[] CENTRAL_METEOR_POS = { 0, 10, 0 };
    private static final int fakeMeteorID = MeteorRegistry
        .getMeteorIDForItem(GTOreDictUnificator.getGem(Materials.Diamond, OrePrefixes.gemExquisite));

    private void summonFakeMeteor() { // TODO: MAKE THIS TOGGLABLE
        if (fakeMeteor != null && !fakeMeteor.isDead) return;
        if (100000 < SoulNetworkHandler.getCurrentEssence(meteorRitual.getOwner())) {
            if (!tryToSyphon(meteorRitual.getOwner(), 100000)) return; // drains 100k to spawn fake meteor
                                                                       // because it's funny
            fakeMeteor = new EntityFakeMeteor(
                getWorld(),
                meteorRitual.getXCoord() + CENTRAL_METEOR_POS[0],
                meteorRitual.getYCoord() + CENTRAL_METEOR_POS[1],
                meteorRitual.getZCoord() + CENTRAL_METEOR_POS[2],
                fakeMeteorID);
            getWorld().spawnEntityInWorld(fakeMeteor);
        }
    }

    private static final int auxiliaryMeteorID = MeteorRegistry
        .getMeteorIDForItem(GTOreDictUnificator.getGem(Materials.Diamond, OrePrefixes.gemExquisite));

    private void summonAuxiliaryMeteors() { // TODO: MAKE THESE CONSISTENTLY SMALL OR REMOVE THEM
                                            // TODO: MAKE THIS TOGGLABLE
        if (getBaseMetaTileEntity().isClientSide()) return;
        final int radius = getWorld().rand.nextInt(200) + 150;
        final double speed = radius / 200.0;
        final double theta = getWorld().rand.nextFloat() * Math.PI * 2;
        final double phi = getWorld().rand.nextFloat() * Math.PI / 4 + Math.PI / 4;
        final double[] RELATIVE_POSE_U = { Math.cos(theta) * Math.cos(phi), Math.sin(phi),
            Math.sin(theta) * Math.cos(phi) };
        final double[] RELATIVE_POSE_P = { RELATIVE_POSE_U[0] * radius, RELATIVE_POSE_U[1] * radius,
            RELATIVE_POSE_U[2] * radius };
        final double[] RELATIVE_POSE_V = { -RELATIVE_POSE_U[0] * speed, -RELATIVE_POSE_U[1] * speed,
            -RELATIVE_POSE_U[2] * speed };
        EntityFakeMeteor meteor = new EntityFakeMeteor(
            getWorld(),
            meteorRitual.getXCoord() + CENTRAL_METEOR_POS[0] + RELATIVE_POSE_P[0],
            meteorRitual.getYCoord() + CENTRAL_METEOR_POS[1] + RELATIVE_POSE_P[1],
            meteorRitual.getZCoord() + CENTRAL_METEOR_POS[2] + RELATIVE_POSE_P[2],
            auxiliaryMeteorID,
            200);
        meteor.motionX = RELATIVE_POSE_V[0];
        meteor.motionY = RELATIVE_POSE_V[1];
        meteor.motionZ = RELATIVE_POSE_V[2];
        getWorld().spawnEntityInWorld(meteor);
    }

    /************************************************************************************************************************************************************************************************
     *
     * ITEM OUTPUTS / RECIPE RUNNING
     *
     */

    private static final int BP_RIGHT_CLICK_VOLUME = 11 * 11 * 12; // TODO: MAKE CONFIGS FOR THESE
    private static final int AV_RIGHT_CLICK_VOLUME = 16 * 16 * 9;
    private static final int BP_RIGHT_CLICK_DELAY = 12 * 16;
    private static final int AV_RIGHT_CLICK_DELAY = 6 * 16;
    private static final int BP_METEOR_DELAY = 20 * 20;
    private static final int AV_METEOR_DELAY = 5 * 20;
    private static final double METEOR_BLOCK_COUNT_MULTIPLIER = 0.4; // TODO: MAKE BOOSTABLE?

    // makes the meteor 1/x times faster and with x times as many outputs, equal throughput and faster recipe times.
    private static final double METEOR_SPEED_SCALING_FACTOR = 0.5; // TODO: MAKE CUSTOMIZABLE TO THE USER

    private boolean runRitual() {
        Meteor meteor = meteorList.get(MeteorRegistry.getMeteorIDForItem(focus));

        meteorSucceeded = canRunMeteor(meteor);
        if (!meteorSucceeded) {
            return false;
        }

        // TODO: get pickaxe durability vs meteor size and only harvest that many ores+filler?
        // would need:
        // TODO: a way to merge multiple pickaxes' of different speed and durabilities and keep track of their
        // itemStacks for
        // removal
        // TODO: a way to toggle between this behavior
        // DONE special behavior with bound pickaxe (fast, consumes lp to simulate right click) and world breaker (even
        // faster)
        // NO fortune 3 = regular drops?
        // NO damage pickaxe if it can
        // DONE consume focus!
        BMMeteor.LOG.info(
            pickaxe.getItem()
                .getUnlocalizedName());

        // COPY METEOR INFORMATION
        List<MeteorComponent> ores = meteor.ores;
        int radius = meteor.radius;
        List<MeteorComponent> filler = meteor.filler;
        double fillerChance = meteor.fillerChance;

        int meteorCost = meteor.cost;
        int totalBlockCount = (int) (4.0 / 3 * Math.PI * radius * radius * radius) + 1;

        int recipeTime;
        List<ItemStack> recipeOutput = new ArrayList<>();

        // PICKAXE-SPECIFIC RULES:
        int miningLPCost = 0;
        if (pickaxe.getItem() instanceof BoundPickaxe) { // TODO: this should probably be consolidated into a helper
                                                         // class!
            final int rightClickCount = totalBlockCount / BP_RIGHT_CLICK_VOLUME + 1;
            miningLPCost = rightClickCount * 10000; // calculated right click count based on volume
            recipeTime = rightClickCount * BP_RIGHT_CLICK_DELAY + BP_METEOR_DELAY;
        } else if (pickaxe.getItem() instanceof ItemPickaxeInfinity) {
            final int rightClickCount = totalBlockCount / AV_RIGHT_CLICK_VOLUME + 1;
            recipeTime = rightClickCount * AV_RIGHT_CLICK_DELAY;
        } else { // TODO: ADD MORE PICKAXES
            recipeTime = totalBlockCount * 10 + AV_METEOR_DELAY;
        }

        if (!tryToSyphon(meteorRitual.getOwner(), meteorCost + miningLPCost + 100000)) return false;

        // silk touch technically, oh well.
        int oreWeight = MeteorComponent.getTotalListWeight(ores);
        int fillerWeight = MeteorComponent.getTotalListWeight(filler);

        for (MeteorComponent ore : ores) {
            double oreCount = ((ore.getWeight() * (100 - fillerChance) * totalBlockCount / 100.0 / oreWeight)
                * METEOR_BLOCK_COUNT_MULTIPLIER
                * METEOR_SPEED_SCALING_FACTOR);
            int finalOreCount = (int) oreCount;
            finalOreCount += (getWorld().rand.nextFloat() < oreCount % 1d ? 1 : 0); // TODO: LAMBDAIZE THIS, MAYBE VIA
                                                                                    // CONFIG?
            ore.getBlock().stackSize = finalOreCount;
            recipeOutput.add(ore.getBlock());
        }

        for (MeteorComponent fil : filler) {
            double fillerCount = (int) ((fil.getWeight() * (fillerChance) * totalBlockCount / 100.0 / fillerWeight)
                * METEOR_BLOCK_COUNT_MULTIPLIER
                * METEOR_SPEED_SCALING_FACTOR);
            int finalFillerCount = (int) fillerCount;
            finalFillerCount += (getWorld().rand.nextFloat() < fillerCount % 1d ? 1 : 0); // TODO: LAMBDAIZE THIS, MAYBE
                                                                                          // VIA CONFIG?
            fil.getBlock().stackSize = finalFillerCount;
            recipeOutput.add(fil.getBlock());
        }

        BMMeteor.LOG.info("filler weight: " + fillerWeight);
        BMMeteor.LOG.info("recipeOutput size (stacks): " + oreWeight);

        // drains extra 100k for
        // ritual cost
        focus.stackSize--;
        if (focus.stackSize <= 0) findFocus();

        this.mMaxProgresstime = (int) (recipeTime * METEOR_SPEED_SCALING_FACTOR); // TODO: LAMBDAIZE THIS
        this.lEUt = -(int) (512 * 15.0 / 16);
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mOutputItems = recipeOutput.toArray(new ItemStack[0]);
        return true;
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPreTick(aBaseMetaTileEntity, aTick);
        if (this.getProgresstime() % 3 == 1) {
            summonAuxiliaryMeteors();
        }
    }

    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        findFallingMeteorRitual();
        pickaxe = findPickaxe();
        focus = findFocus();
        orb = findOrb();
        if (pickaxe == null || focus == null || orb == null || !acceptedPickaxe((ItemPickaxe) pickaxe.getItem())) {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }
        if (this.getProgresstime() == 0) {
            if (runRitual()) {
                return CheckRecipeResultRegistry.SUCCESSFUL;
            } else {
                return CheckRecipeResultRegistry.CYCLE_IDLE;
            }
        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }
}
