package de.tobias.playpad.plugin.api.trigger;

import de.thecodelabs.utils.list.UniqList;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.plugin.api.WebApiPlugin$;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import org.dom4j.Element;

import java.util.List;
import java.util.UUID;

public class RemoteTriggerItem extends TriggerItem {

	private final String type;

	private UUID serverId;
	private List<UUID> uuids;
	private PadStatus newStatus;

	public RemoteTriggerItem(String type) {
		this.type = type;

		this.newStatus = PadStatus.PLAY;
		this.uuids = new UniqList<>();
	}

	public List<UUID> getCarts() {
		return uuids;
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
	public void performAction(Pad pad, Project project, IMainViewController controller, Profile profile) {
		WebApiPlugin$.MODULE$.getConnection(serverId).ifPresent(client -> {
			for (UUID uuid : uuids) {
				client.setPadStatus(uuid, newStatus);
			}
		});
	}

	@Override
	public TriggerItem copy() {
		RemoteTriggerItem clone = new RemoteTriggerItem(getType());

		clone.uuids = new UniqList<>();
		clone.uuids.addAll(uuids);
		clone.newStatus = newStatus;

		return clone;
	}

	private static final String SERVER_ELEMENT = "Server";
	private static final String CART_ELEMENT = "Cart";
	private static final String STATUS_ATTR = "Status";

	@Override
	public void load(Element element) {
		super.load(element);

		if (element.attributeValue(SERVER_ELEMENT) != null)
			setServerId(UUID.fromString(element.attributeValue(SERVER_ELEMENT)));

		if (element.attributeValue(STATUS_ATTR) != null)
			setNewStatus(PadStatus.valueOf(element.attributeValue(STATUS_ATTR)));

		for (Element cartElement : element.elements(CART_ELEMENT)) {
			uuids.add(UUID.fromString(cartElement.getStringValue()));
		}
	}

	@Override
	public void save(Element element) {
		super.save(element);

		element.addAttribute(SERVER_ELEMENT, serverId.toString());
		element.addAttribute(STATUS_ATTR, newStatus.name());

		for (UUID cart : uuids) {
			Element cartElement = element.addElement(CART_ELEMENT);
			cartElement.addText(String.valueOf(cart));
		}
	}

	public UUID getServerId() {
		return serverId;
	}

	public void setServerId(UUID serverId) {
		this.serverId = serverId;
	}
}
