package de.tobias.playpad.trigger;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public class CartTriggerItem extends TriggerItem {

	private List<Integer> carts;
	private boolean allCarts;
	private PadStatus newStatus; // Only Play, Pause, Stop

	public CartTriggerItem() {
		newStatus = PadStatus.PLAY;
		allCarts = false;
		carts = new ArrayList<Integer>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean add(Integer e) {
				if (!contains(e))
					return super.add(e);
				else
					return false;
			}
		};
	}

	public List<Integer> getCarts() {
		return carts;
	}

	public boolean isAllCarts() {
		return allCarts;
	}

	public void setAllCarts(boolean allCarts) {
		this.allCarts = allCarts;
	}

	public PadStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(PadStatus newStatus) {
		if (newStatus == PadStatus.PLAY || newStatus == PadStatus.PAUSE || newStatus == PadStatus.STOP)
			this.newStatus = newStatus;
	}

	@Override
	public String getType() {
		return CartTriggerItemConnect.TYPE;
	}

	@Override
	public void performAction(Pad pad, ProjectV2 project, IMainViewController controller, Profile profile) {
		if (allCarts) {
			for (Pad cart : project.getPads()) {
				if (cart.getIndex() != pad.getIndex())
					cart.setStatus(newStatus);
			}
		} else {
			// TODO Cart Trigger mit Pages und Index --> PadIndex
//			for (int cart : carts) {
//				if (cart != pad.getIndex())
//					project.getPad(cart).setStatus(newStatus);
//			}
		}
	}

	private static final String CART_ELEMENT = "Cart";
	private static final String STATUS_ATTR = "Status";
	private static final String ALLCARTS_ATTR = "all";

	@Override
	public void load(Element element) {
		super.load(element);

		if (element.attributeValue(STATUS_ATTR) != null)
			setNewStatus(PadStatus.valueOf(element.attributeValue(STATUS_ATTR)));
		if (element.attributeValue(ALLCARTS_ATTR) != null)
			setAllCarts(Boolean.valueOf(element.attributeValue(ALLCARTS_ATTR)));

		for (Object cartObj : element.elements(CART_ELEMENT)) {
			if (cartObj instanceof Element) {
				Element cartElement = (Element) cartObj;
				carts.add(Integer.valueOf(cartElement.getStringValue()));
			}
		}
	}

	@Override
	public void save(Element element) {
		super.save(element);

		element.addAttribute(STATUS_ATTR, newStatus.name());
		element.addAttribute(ALLCARTS_ATTR, String.valueOf(allCarts));

		for (int cart : carts) {
			Element cartElement = element.addElement(CART_ELEMENT);
			cartElement.addText(String.valueOf(cart));
		}
	}

	public void setCartsString(String string) {
		if (string != null) {
			carts.clear();
			string = string.replace(" ", "");
			for (String part : string.split(",")) {
				if (part.contains("-")) {
					if (part.split("-").length == 2) {
						int start = Integer.valueOf(part.split("-")[0]);
						int end = Integer.valueOf(part.split("-")[1]);

						for (int i = start; i <= end; i++) {
							carts.add(i - 1);
						}
					}
				} else {
					int cart = Integer.valueOf(part);
					carts.add(cart - 1);
				}
			}
			carts.sort(Integer::compareTo);
		}
	}

	public String getCartsString() {
		String string = "";
		int startValue = -1;

		for (int i = 0; i < carts.size(); i++) {
			if (i + 1 < carts.size()) {
				if (carts.get(i) + 1 == carts.get(i + 1)) {
					if (startValue == -1) {
						startValue = carts.get(i);
					}
				} else {
					if (startValue != -1)
						string += (startValue + 1) + "-" + (carts.get(i) + 1) + ",";
					else
						string += (carts.get(i) + 1) + ",";
					startValue = -1;

				}
			} else {
				if (startValue == -1) {
					string += (carts.get(i) + 1) + ",";
				} else {
					string += (startValue + 1) + "-" + (carts.get(i) + 1) + ",";
				}
			}
		}
		if (string.isEmpty()) {
			return null;
		}
		return string.substring(0, string.length() - 1);
	}
}
