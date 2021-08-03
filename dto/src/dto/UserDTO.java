package dto;

import java.util.List;

public class UserDTO {
    private final String name;
    private  final List<Item> items;

    public UserDTO(String newName, List<Item> newItems) {
        name = newName;
        items = newItems;
    }

    public String getName() { return name; }

    public List<Item> getItems() {
        return items;
    }
}
