package io.fenogy.comix.model;

public class ComicJSONModel {

    private int id;
    private String name;
    private String author;
    private int author_id;
    private String size;
    private String description;
    private String category;
    private String language;
    private int pages;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private String uri;
    private boolean update;

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }


    public ComicJSONModel() {

    }

    public ComicJSONModel(int id, String name,String author,int author_id,int pages,
                          String size, String description, String category,String language,boolean update,String uri) {

        this.id = id;
        this.name = name;
        this.author = author;
        this.author_id = author_id;
        this.size = size;
        this.description = description;
        this.category = category;
        this.language = language;
        this.update = update;
        this.uri = uri;
        this.pages = pages;

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
    public int getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }



}
