package edu.lockhaven.bloom_gnatt;

/**
 * Interface for on click listeners for adapters.
 */
public interface AdapterInterface {

    /**
     * A listener for sending id information across activities.
     * @param id Is a generic id.
     */
    void onClick(int id);

    /**
     * A listener for deleting tasks and activities.
     * @param id Is a generic id.
     */
    void onDelete(int id);

}
