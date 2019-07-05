package io.fenogy.comix.model;

public class CatalogueItem {

    private int id;
    private String name;
    private String author;
    private int author_id;
    private String size;
    private String description;
    private String category;
    private String language;
    private String downloaded;
    private String uri;



    private int num_pages;
    private String thumb_name;

    public CatalogueItem() {

    }

    public CatalogueItem(int id, String name, String author, int author_id,
                         String size, String description, String category, String language,
                         String downloaded,String uri,int num_pages,String thumb_name) {

        this.id = id;
        this.name = name;
        this.author = author;
        this.author_id = author_id;
        this.size = size;
        this.description = description;
        this.category = category;
        this.language = language;
        this.downloaded = downloaded;
        this.uri = uri;
        this.num_pages = num_pages;
        this.thumb_name = thumb_name;

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

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }
    public int getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getNum_pages() {
        return num_pages;
    }

    public void setNum_pages(int num_pages) {
        this.num_pages = num_pages;
    }

    public String getThumb_name() {
        return thumb_name;
    }

    public void setThumb_name(String thumb_name) {
        this.thumb_name = thumb_name;
    }



}
