package basemod.helpers;

import basemod.abstracts.AbstractCardModifier;
import basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.CardModifierPatches;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class CardModifierManager
{
    private static ArrayList<AbstractCardModifier> modifiers(AbstractCard c) {
        return CardModifierPatches.CardModifierFields.cardModifiers.get(c);
    }

    /**
     * adds a modifier (mod) to card.
     */
    public static void addModifier(AbstractCard card, AbstractCardModifier mod) {
        modifiers(card).add(mod);
        Collections.sort(modifiers(card));
        mod.onInitialApplication(card);
        card.initializeDescription();
    }

    /**
     * removes a specific instance of a modifier from card, but only if it's not inherent or the method is sent "true"
     */
    public static void removeSpecificModifier(AbstractCard card, AbstractCardModifier mod, boolean includeInherent) {
        if (modifiers(card).contains(mod) && (!mod.isInherent(card) || includeInherent)) {
            modifiers(card).remove(mod);
            mod.onRemove(card);
        }
        card.initializeDescription();
    }

    /**
     * removes all modifiers from card that match id. Inherent mods are only included if the method is sent "true"
     */
    public static void removeModifiersById(AbstractCard card, String id, boolean includeInherent) {
        Iterator<AbstractCardModifier> it = modifiers(card).iterator();
        while (it.hasNext()) {
            AbstractCardModifier mod = it.next();
            if (mod.identifier(card).equals(id) && (!mod.isInherent(card) || includeInherent)) {
                it.remove();
                mod.onRemove(card);
            }
        }
        card.initializeDescription();
    }

    /**
     * returns true if card has a modifier that matches id
     */
    public static boolean hasModifier(AbstractCard card, String id) {
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.identifier(card).equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns an ArrayList containing all modifiers that match id on card.
     */
    public static ArrayList<AbstractCardModifier> getModifiers(AbstractCard card, String id) {
        ArrayList<AbstractCardModifier> retVal = new ArrayList<>();
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.identifier(card).equals(id)) {
                retVal.add(mod);
            }
        }
        return retVal;
    }

    /**
     * removes all modifiers from card. Mods that are inherent are only included if the method is sent "true"
     */
    public static void removeAllModifiers(AbstractCard card, boolean includeInherent) {
        Iterator<AbstractCardModifier> it = modifiers(card).iterator();
        while (it.hasNext()) {
            AbstractCardModifier mod = it.next();
            if (!mod.isInherent(card) || includeInherent) {
                it.remove();
                mod.onRemove(card);
            }
        }
        card.initializeDescription();
    }

    /**
     * Copies the modifiers from oldCard onto newCard.
     * @param includeInherent - whether this method should consider Inherent modifiers.
     * @param replace - whether this method should replace existing modifiers on the card.
     * @param removeOld - whether the modifiers copied should be removed from the old card (IE, moved instead of copied)
     */
    public static void copyModifiers(AbstractCard oldCard, AbstractCard newCard, boolean includeInherent, boolean replace, boolean removeOld) {
        if (replace) {
            removeAllModifiers(newCard, includeInherent);
        }
        Iterator<AbstractCardModifier> it = modifiers(oldCard).iterator();
        while (it.hasNext()) {
            AbstractCardModifier mod = it.next();
            if (!mod.isInherent(oldCard) || includeInherent) {
                if (removeOld) {
                    it.remove();
                    mod.onRemove(oldCard);
                }
                AbstractCardModifier newMod = mod.makeCopy();
                modifiers(newCard).add(newMod);
                newMod.onInitialApplication(newCard);
            }
        }
        if (removeOld) {
            oldCard.initializeDescription();
        }
        newCard.initializeDescription();
    }

    public static void removeEndOfTurnModifiers(AbstractCard card) {
        Iterator<AbstractCardModifier> it = modifiers(card).iterator();
        while (it.hasNext()) {
            AbstractCardModifier mod = it.next();
            if (mod.removeAtEndOfTurn(card)) {
                it.remove();
                mod.onRemove(card);
            }
        }
        card.initializeDescription();
    }

    public static void removeWhenPlayedModifiers(AbstractCard card) {
        Iterator<AbstractCardModifier> it = modifiers(card).iterator();
        while (it.hasNext()) {
            AbstractCardModifier mod = it.next();
            if (mod.removeOnCardPlayed(card)) {
                it.remove();
                mod.onRemove(card);
            }
        }
        card.initializeDescription();
    }

    public static void onApplyPowers(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onApplyPowers(card);
        }
    }

    public static void onCreateDescription(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            card.rawDescription = mod.modifyDescription(card.rawDescription, card);
        }
    }

    public static void onUseCard(AbstractCard card, AbstractCreature target, UseCardAction action) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onUse(card, target, action);
        }
    }

    public static void onCardDrawn(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onDrawn(card);
        }
    }

    public static void onCardExhausted(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onExhausted(card);
        }
    }

    public static void onCardDiscarded(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onDiscarded(card);
        }
    }

    public static void onCardRetained(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onRetained(card);
        }
    }

    public static float onModifyDamage(float damage, AbstractCard card, AbstractMonster mo) {
        for (AbstractCardModifier mod : modifiers(card)) {
            damage = mod.modifyDamage(damage, card.damageTypeForTurn, card, mo);
        }
        return damage;
    }

    public static float onModifyDamageFinal(float damage, AbstractCard card, AbstractMonster mo) {
        for (AbstractCardModifier mod : modifiers(card)) {
            damage = mod.modifyDamageFinal(damage, card.damageTypeForTurn, card, mo);
        }
        return damage;
    }

    public static float onModifyBlock(float block, AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            block = mod.modifyBlock(block, card);
        }
        return block;
    }

    public static float onModifyBlockFinal(float block, AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            block = mod.modifyBlockFinal(block, card);
        }
        return block;
    }

    public static void onUpdate(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onUpdate(card);
        }
    }

    public static void onRender(AbstractCard card, SpriteBatch sb) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onRender(card, sb);
        }
    }

    public static void atEndOfTurn(AbstractCard card, CardGroup group) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.atEndOfTurn(card, group);
        }
    }

    public static void onOtherCardPlayed(AbstractCard card, AbstractCard otherCard, CardGroup group) {
        for (AbstractCardModifier mod : modifiers(card)) {
            mod.onOtherCardPlayed(card, otherCard, group);
        }
    }

    public static boolean canPlayCard(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            if (!mod.canPlayCard(card)) {
                return false;
            }
        }
        return true;
    }

    //the player is considered to have enough alternate cost when their energy + the total of alternate splittable resources >=
    // cost for turn, OR when any single non-splittable resource >= cost for turn.
    public static boolean hasEnoughAlternateCost(AbstractCard card) {
        ArrayList<AbstractCardModifier> splittableCosts = new ArrayList<>();
        ArrayList<AbstractCardModifier> nonSplittableCosts = new ArrayList<>();
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.canSplitCost(card)) {
                splittableCosts.add(mod);
            } else {
                nonSplittableCosts.add(mod);
            }

        }
        int amt = EnergyPanel.totalCount;
        for (AbstractCardModifier mod : splittableCosts) {
            int c = mod.getAlternateResource(card);
            if (c > -1) {
                amt += c;
            }
        }
        if (amt > card.costForTurn) {
            return true;
        }
        for (AbstractCardModifier mod : nonSplittableCosts) {
            int c = mod.getAlternateResource(card);
            if (c > amt) {
                amt = c;
                if (amt > card.costForTurn) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCostString(AbstractCard card, String currentString, Color color) {
        for (AbstractCardModifier mod : modifiers(card)) {
            currentString = mod.replaceCostString(card, currentString, color);
        }
        return currentString;
    }

    public static int getPreEnergyResourceAmount(AbstractCard card) {
        int tmp = 0;
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.prioritizeAlternateCost(card)) {
                tmp = Math.max(tmp, mod.getAlternateResource(card));
            }
        }
        return tmp;
    }

    public static int getPostEnergyResourceAmount(AbstractCard card) {
        int tmp = 0;
        for (AbstractCardModifier mod : modifiers(card)) {
            if (!mod.prioritizeAlternateCost(card)) {
                tmp = Math.max(tmp, mod.getAlternateResource(card));
            }
        }
        return tmp;
    }

    public static int getSplittableResourceAmount(AbstractCard card) {
        int tmp = 0;
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.canSplitCost(card)) {
                int c = mod.getAlternateResource(card);
                if (c > -1) {
                    tmp += c;
                }
            }
        }
        return tmp;
    }

    public static void spendPreEnergyResource(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.prioritizeAlternateCost(card)) {
                int c = mod.getAlternateResource(card);
                if (c >= card.costForTurn) {
                    mod.spendAlternateCost(card, card.costForTurn);
                    return;
                }
            }
        }
    }

    public static void spendPostEnergyResource(AbstractCard card) {
        for (AbstractCardModifier mod : modifiers(card)) {
            if (!mod.prioritizeAlternateCost(card)) {
                int c = mod.getAlternateResource(card);
                if (c >= card.costForTurn) {
                    mod.spendAlternateCost(card, card.costForTurn);
                    return;
                }
            }
        }
    }

    public static int spendPreEnergySplittableResource(AbstractCard card) {
        int remainingCost = card.costForTurn;
        for (AbstractCardModifier mod : modifiers(card)) {
            if (mod.prioritizeAlternateCost(card) && mod.canSplitCost(card)) {
                remainingCost = mod.spendAlternateCost(card, remainingCost);
                if (remainingCost <= 0) {
                    break;
                }
            }
        }
        return remainingCost;
    }

    public static void spendPostEnergySplittableResource(AbstractCard card, int remainingCost) {
        for (AbstractCardModifier mod : modifiers(card)) {
            if (!mod.prioritizeAlternateCost(card) && mod.canSplitCost(card)) {
                remainingCost = mod.spendAlternateCost(card, remainingCost);
                if (remainingCost <= 0) {
                    return;
                }
            }
        }
        System.out.println("CardModifierManager: WARNING: splittable resources spent for " + card + "without being sufficient!");
    }
}
