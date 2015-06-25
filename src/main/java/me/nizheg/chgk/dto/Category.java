package me.nizheg.chgk.dto;

import java.io.Serializable;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class Category implements Serializable {

    public static final String ALL = "all";
    public static final String CURRENT = "current";
    public static final String TOUR = "tournament";
    public static final String UNKNOWN_COMPLEXITY = "db_unknown";
    private static final long serialVersionUID = 9217885342669830164L;
    private String id;
    private String name;
    private Integer position;

    public Category() {
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
