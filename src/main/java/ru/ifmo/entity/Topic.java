package ru.ifmo.entity;

public class Topic extends AbstractEntity {

    private final Source source;
    private final String path;
    private final String thema;
    private final String title;
    public Topic(long id, Source source, String path, String thema, String title) {
        super(id);
        this.source = source;
        this.path = path;
        this.thema = thema;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Topic topic = (Topic) o;

        return id == topic.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public Source getSource() {
        return source;
    }

    public String getPath() {
        return path;
    }

    public String getThema() {
        return thema;
    }

    public String getTitle() {
        return title;
    }
}
