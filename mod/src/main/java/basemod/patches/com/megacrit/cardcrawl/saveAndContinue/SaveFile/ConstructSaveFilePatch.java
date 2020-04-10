package basemod.patches.com.megacrit.cardcrawl.saveAndContinue.SaveFile;

import java.util.ArrayList;
import java.util.Map;

import basemod.abstracts.AbstractCardModifier;
import basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.CardModifierPatches;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;

import basemod.BaseMod;
import basemod.abstracts.CustomSavableRaw;

@SpirePatch(clz=SaveFile.class, method=SpirePatch.CONSTRUCTOR, paramtypez={SaveFile.SaveType.class})
public class ConstructSaveFilePatch
{
    public static void Prefix(SaveFile __instance, SaveFile.SaveType saveType) {
        // Card saves
        ModSaves.ArrayListOfJsonElement modCardSaves = new ModSaves.ArrayListOfJsonElement();
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            if (card instanceof CustomSavableRaw) {
                modCardSaves.add(((CustomSavableRaw)card).onSaveRaw());
            } else {
                modCardSaves.add(null);
            }
        }
        ModSaves.modCardSaves.set(__instance, modCardSaves);

        // Relic saves
        ModSaves.ArrayListOfJsonElement modRelicSaves = new ModSaves.ArrayListOfJsonElement();
        for (AbstractRelic relic : AbstractDungeon.player.relics) {
            if (relic instanceof CustomSavableRaw) {
                modRelicSaves.add(((CustomSavableRaw)relic).onSaveRaw());
            } else {
                modRelicSaves.add(null);
            }
        }
        ModSaves.modRelicSaves.set(__instance, modRelicSaves);

        // Mod saves
        ModSaves.HashMapOfJsonElement modSaves = new ModSaves.HashMapOfJsonElement();
        for (Map.Entry<String, CustomSavableRaw> field : BaseMod.getSaveFields().entrySet()) {
            modSaves.put(field.getKey(), field.getValue().onSaveRaw());
        }
        ModSaves.modSaves.set(__instance, modSaves);

        //Master deck AbstractCardModifiers
        ModSaves.ArrayListOfJsonElement cardModifierSaves = new ModSaves.ArrayListOfJsonElement();
        GsonBuilder builder = new GsonBuilder();
        if (CardModifierPatches.modifierAdapter == null) {
            CardModifierPatches.initializeAdapterFactory();
        }
        builder.registerTypeAdapterFactory(CardModifierPatches.modifierAdapter);
        Gson gson = builder.create();
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            ArrayList<AbstractCardModifier> cardModifierList = CardModifierPatches.CardModifierFields.cardModifiers.get(card);
            if (!cardModifierList.isEmpty()) {
                cardModifierSaves.add(gson.toJsonTree(cardModifierList));
            } else {
                cardModifierSaves.add(null);
            }
        }
        ModSaves.cardModifierSaves.set(__instance, cardModifierSaves);
    }
}