package ru.ifmo.entity;

public class Author extends AbstractEntity {

    private final String name;
    private final String city;

    public Author(long id, String name, String city) {
        super(id);
        this.name = name;
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        return id == author.id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
