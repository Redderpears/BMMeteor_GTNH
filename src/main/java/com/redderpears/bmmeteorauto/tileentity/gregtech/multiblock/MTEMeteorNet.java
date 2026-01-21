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
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_GLOW;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;

import java.util.ArrayList;
import java.util.List;

import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
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

import WayofTime.alchemicalWizardry.api.alchemy.energy.Reagent;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.common.entity.projectile.EntityMeteor;
import WayofTime.alchemicalWizardry.common.summoning.meteor.Meteor;
import WayofTime.alchemicalWizardry.common.summoning.meteor.MeteorComponent;
import WayofTime.alchemicalWizardry.common.summoning.meteor.MeteorRegistry;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
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
        int colorIndex, boolean aActive, boolean aRedstone) {
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
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 1, 0, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
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
        screenElements.widget(new TextWidget("cool pickaxe bro").setEnabled(widget -> validPickaxe))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> (findPickaxe() != null), val -> validPickaxe = val));
        screenElements.widget(new TextWidget("cool focus bro").setEnabled(widget -> validFocus))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> (findFocus() != null), val -> validFocus = val));
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
    private Reagent reagent;
    private double reagentProgress;
    private ItemStack pickaxe;
    private boolean validPickaxe;
    private boolean validFocus;
    private ItemStack focus;

    public class EntityFakeMeteor extends EntityMeteor {

        public EntityFakeMeteor(World par1World) {
            super(par1World);
            maxTicksInAir = 3600 * 20;
        }

        public EntityFakeMeteor(World par1World, double par2, double par4, double par6, int meteorID) {
            super(par1World, par2, par4, par6, meteorID);
            maxTicksInAir = 3600 * 20;
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
    public ItemStack findFocus() {

        for (ItemStack item : this.getStoredInputs()) {
            if (item != null && isValidMeteorFocusItem(item)) {
                return item;
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

    private static int[] CENTRAL_METEOR_POS = { 0, 10, 0 };


    private static final int fakeMeteorID = MeteorRegistry.getMeteorIDForItem(GTOreDictUnificator)
    private void summonFakeMeteor() {
        if (fakeMeteor != null && !fakeMeteor.isDead) return;
        if (100000 < SoulNetworkHandler.getCurrentEssence(meteorRitual.getOwner())) {
            SoulNetworkHandler.syphonFromNetwork(meteorRitual.getOwner(), 100000);
            fakeMeteor = new EntityFakeMeteor(
                meteorRitual.getWorld(),
                meteorRitual.getXCoord() + CENTRAL_METEOR_POS[0],
                meteorRitual.getYCoord() + CENTRAL_METEOR_POS[1],
                meteorRitual.getZCoord() + CENTRAL_METEOR_POS[2],
                );
            meteorRitual.getWorld()
                .spawnEntityInWorld(fakeMeteor);
        }
    }

    private static final int auxiliaryMeteorID = MeteorRegistry.getMeteorIDForItem(GTOreDictUnificator.getGem(Materials.Diamond, OrePrefixes.gemExquisite));
    private void summonAuxiliaryMeteors() {
        final int radius = meteorRitual.getWorld().rand.nextInt(200) + 150;
        final double speed = radius / 200.0;
        final double theta = meteorRitual.getWorld().rand.nextFloat() * Math.PI * 2;
        final double phi = meteorRitual.getWorld().rand.nextFloat() * Math.PI / 4 + Math.PI / 4;
        final double[] RELATIVE_POSE_U = { Math.cos(theta) * Math.cos(phi), Math.sin(phi),
            Math.sin(theta) * Math.cos(phi) };
        final double[] RELATIVE_POSE_P = { RELATIVE_POSE_U[0] * radius, RELATIVE_POSE_U[1] * radius,
            RELATIVE_POSE_U[2] * radius };
        final double[] RELATIVE_POSE_V = { -RELATIVE_POSE_U[0] * speed, -RELATIVE_POSE_U[1] * speed,
            -RELATIVE_POSE_U[2] * speed };
        EntityFakeMeteor meteor = new EntityFakeMeteor(
            meteorRitual.getWorld(),
            meteorRitual.getXCoord() + CENTRAL_METEOR_POS[0] + RELATIVE_POSE_P[0],
            meteorRitual.getYCoord() + CENTRAL_METEOR_POS[1] + RELATIVE_POSE_P[1],
            meteorRitual.getZCoord() + CENTRAL_METEOR_POS[2] + RELATIVE_POSE_P[2],
            auxiliaryMeteorID,
            200);
        meteor.motionX = RELATIVE_POSE_V[0];
        meteor.motionY = RELATIVE_POSE_V[1];
        meteor.motionZ = RELATIVE_POSE_V[2];
        BMMeteor.LOG
            .info("Summoned meteor at: " + RELATIVE_POSE_P[0] + " " + RELATIVE_POSE_P[1] + " " + RELATIVE_POSE_P[2]);
        meteorRitual.getWorld()
            .spawnEntityInWorld(meteor);
    }

    private void removeFakeMeteor() {
        if (fakeMeteor == null || fakeMeteor.isDead) return;
        fakeMeteor.setDead();
    }

    /************************************************************************************************************************************************************************************************
     *
     * ITEM OUTPUTS
     *
     */

    private void runRitual() {
        Meteor meteor = meteorList.get(MeteorRegistry.getMeteorIDForItem(focus));
        List<MeteorComponent> ores = meteor.ores;
        int radius = meteor.radius;
        List<MeteorComponent> filler = meteor.filler;
        double fillerChance = meteor.fillerChance;
        final double multiplier = 0.4;

        int totalBlockCount = (int) (4.0 / 3 * Math.PI * radius * radius * radius) + 1;
        List<ItemStack> recipeOutput = new ArrayList<>();

        // silk touch technically
        int oreWeight = MeteorComponent.getTotalListWeight(ores);
        int fillerWeight = MeteorComponent.getTotalListWeight(filler);

        for (MeteorComponent ore : ores) {
            int oreCount = (int) ((ore.getWeight() * (100 - fillerChance) * totalBlockCount / 100.0 / oreWeight)
                * multiplier);
            ore.getBlock().stackSize = oreCount;
            recipeOutput.add(ore.getBlock());
        }

        for (MeteorComponent fil : filler) {
            int fillerCount = (int) ((fil.getWeight() * (fillerChance) * totalBlockCount / 100.0 / fillerWeight)
                * multiplier);
            fil.getBlock().stackSize = fillerCount;
            recipeOutput.add(fil.getBlock());
        }

        BMMeteor.LOG.info("filler weight: " + fillerWeight);
        BMMeteor.LOG.info("recipeOutput size (stacks): " + oreWeight);

        this.mMaxProgresstime = 100;
        this.lEUt = -(int) (512 * 15.0 / 16);
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mOutputItems = recipeOutput.toArray(new ItemStack[0]);
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
        pickaxe = findPickaxe();
        focus = findFocus();
        if (pickaxe == null || focus == null) {
            BMMeteor.LOG.info("recipe did not run!");
            return CheckRecipeResultRegistry.NO_RECIPE;
        }
        if (this.getProgresstime() == 0) {
            runRitual();
        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }
}
