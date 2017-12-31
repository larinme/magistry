package ru.ifmo.entity;

public class Source extends AbstractEntity {

    private final long id;
    private final String name;
    private final String url;

    public Source(long id, String name, String url) {
        super(id);
        this.id = id;
        this.name = name;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        return id == source.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
