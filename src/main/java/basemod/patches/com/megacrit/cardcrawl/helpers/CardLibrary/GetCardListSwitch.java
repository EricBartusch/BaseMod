package basemod.patches.com.megacrit.cardcrawl.helpers.CardLibrary;

import java.util.ArrayList;
import java.util.Map;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.CardLibrary.LibraryType;

@SpirePatch(cls="com.megacrit.cardcrawl.helpers.CardLibrary", method="getCardList")
public class GetCardListSwitch {
	@SpireInsertPatch(rloc=644, localvars={"retVal"})
	public static void Insert(Object typeObj, Object retValObj) {
		LibraryType type = (LibraryType) typeObj;
		@SuppressWarnings("unchecked")
		ArrayList<AbstractCard> retVal = (ArrayList<AbstractCard>) retValObj;
		if (!type.toString().equals("COLORLESS") && !type.toString().equals("CURSE") &&
				!type.toString().equals("RED") && !type.toString().equals("GREEN") &&
				!type.toString().equals("BLUE") && !type.toString().equals("STATUS")) {
			for (Map.Entry<String, AbstractCard> c : CardLibrary.cards.entrySet()) {
				if(c.getValue().color.toString().equals(type.toString())) {
					retVal.add(c.getValue());
				}
			}
		}
	}
}
