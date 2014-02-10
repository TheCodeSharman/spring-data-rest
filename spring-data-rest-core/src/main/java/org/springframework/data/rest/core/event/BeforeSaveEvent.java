package org.springframework.data.rest.core.event;

/**
 * Emitted before an entity is saved into the repository.
 */
public class BeforeSaveEvent extends RepositoryEvent {

	private static final long serialVersionUID = -1404580942928384726L;

	private final Object existing;

	public BeforeSaveEvent(Object incoming, Object existing) {
		super(incoming);
		this.existing = existing;
	}

	/**
	 * Get the existing object.
	 * 
	 * @return The entity existing entity before the merge.
	 */
	public Object getExisting() {
		return existing;
	}

}
