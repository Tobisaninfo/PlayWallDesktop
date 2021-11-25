package de.tobias.playpad.trigger;

import de.thecodelabs.utils.list.UniqList;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.tigger.LocalPadTrigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.Element;

import java.util.List;
import java.util.UUID;

public class CartTriggerItem extends TriggerItem implements LocalPadTrigger {

	private List<UUID> uuids;
	private boolean allCarts;
	private PadStatus newStatus; // Only Play, Pause, Stop

	private final String type;

	CartTriggerItem(String type) {
		this.type = type;
		this.newStatus = PadStatus.PLAY;
		this.allCarts = false;
		this.uuids = new UniqList<>();
	}

	@Override
	public List<UUID> getCarts() {
		return uuids;
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
		return type;
	}

	@Override
	public void performAction(Pad source, Project project, IMainViewController controller, Profile profile) {
		if (allCarts) {
			for (Pad cart : project.getPads()) {
				if (cart.getUuid().equals(source.getUuid()))
					cart.setStatus(newStatus);
			}
		} else {
			for (UUID uuid : uuids) {
				if (!uuid.equals(source.getUuid())) {
					Pad pad = project.getPad(uuid);
					if (pad != null)
						pad.setStatus(newStatus);
				}
			}
		}
	}

	@Override
	public TriggerItem copy() {
		CartTriggerItem clone = new CartTriggerItem(getType());

		clone.uuids = new UniqList<>();
		clone.uuids.addAll(uuids);
		clone.allCarts = allCarts;
		clone.newStatus = newStatus;

		return clone;
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
			setAllCarts(Boolean.parseBoolean(element.attributeValue(ALLCARTS_ATTR)));

		for (Element cartElement : element.elements(CART_ELEMENT)) {
			uuids.add(UUID.fromString(cartElement.getStringValue()));
		}
	}

	@Override
	public void save(Element element) {
		super.save(element);

		element.addAttribute(STATUS_ATTR, newStatus.name());
		element.addAttribute(ALLCARTS_ATTR, String.valueOf(allCarts));

		for (UUID cart : uuids) {
			Element cartElement = element.addElement(CART_ELEMENT);
			cartElement.addText(String.valueOf(cart));
		}
	}
}
