package engine;

import dto.Item;

import java.util.List;

public class User {
    /// Variables
    private final String name;
    private  final List<Item> items;

    public User(String newName, List<Item> newItems){
        name = newName;
        items = newItems; /// Do we need to initialize??
    }

    public String getName() { return name; }

    public List<Item> getItems() {
        return items;
    }
}
