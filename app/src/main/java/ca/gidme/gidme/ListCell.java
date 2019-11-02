package ca.gidme.gidme;

import android.support.annotation.NonNull;

public class ListCell implements Comparable{

    private String name;
    private String category;
    private boolean isSectionHeader;

    public ListCell(String name, String category)
    {
        this.name = name;
        this.category = category;
        isSectionHeader = false;
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public void setToSectionHeader()
    {
        isSectionHeader = true;
    }

    public boolean isSectionHeader()
    {
        return isSectionHeader;
    }

    public int compareTo(ListCell other) {
        return this.category.compareTo(other.category);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return 0;
    }
}